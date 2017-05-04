package uk.gov.justice.services.core.extension;

import static java.util.Collections.synchronizedList;
import static org.slf4j.LoggerFactory.getLogger;
import static uk.gov.justice.services.core.annotation.ComponentNameUtil.componentFrom;
import static uk.gov.justice.services.core.annotation.ServiceComponentLocation.componentLocationFrom;

import uk.gov.justice.domain.annotation.Event;
import uk.gov.justice.services.core.annotation.AnyLiteral;
import uk.gov.justice.services.core.annotation.CustomServiceComponent;
import uk.gov.justice.services.core.annotation.FrameworkComponent;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.handler.registry.HandlerRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;

import org.slf4j.Logger;

/**
 * Scans all beans and processes framework specific annotations.
 */
public class AnnotationScanner implements Extension {

    private static final Logger LOGGER = getLogger(HandlerRegistry.class);

    private List<Object> events = synchronizedList(new ArrayList<>());

    @SuppressWarnings("unused")
    <T> void processAnnotatedType(@Observes final ProcessAnnotatedType<T> pat) {
        final AnnotatedType<T> annotatedType = pat.getAnnotatedType();
        if (annotatedType.isAnnotationPresent(Event.class)) {
            events.add(new EventFoundEvent(annotatedType.getJavaClass(), annotatedType.getAnnotation(Event.class).value()));
        }
    }

    @SuppressWarnings("unused")
    void afterDeploymentValidation(@Observes final AfterDeploymentValidation event, final BeanManager beanManager) {
        final Set<Bean<?>> beans = beanManager.getBeans(Object.class, AnyLiteral.create());
        beans.forEach(this::processBean);

        fireAllCollectedEvents(beanManager);
    }

    private void processBean(final Bean<?> bean) {
        final Class<?> beanClass = bean.getBeanClass();
        if (beanClass.isAnnotationPresent(ServiceComponent.class)
                || beanClass.isAnnotationPresent(FrameworkComponent.class)
                || beanClass.isAnnotationPresent(CustomServiceComponent.class)) {
            processServiceComponentsForEvents(bean);
        }
    }

    /**
     * Processes bean for annotations and adds events to the list.
     *
     * @param bean a bean that has an annotation and could be of interest to the framework wiring.
     */
    private void processServiceComponentsForEvents(final Bean<?> bean) {
        final Class<?> clazz = bean.getBeanClass();
        LOGGER.info("Identified ServiceComponent {}", clazz.getSimpleName());

        events.add(new ServiceComponentFoundEvent(componentFrom(clazz), bean, componentLocationFrom(clazz)));
    }


    private void fireAllCollectedEvents(final BeanManager beanManager) {
        events.forEach(beanManager::fireEvent);
    }
}

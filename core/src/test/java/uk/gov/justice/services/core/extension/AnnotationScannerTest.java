package uk.gov.justice.services.core.extension;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_API;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_CONTROLLER;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;
import static uk.gov.justice.services.core.annotation.Component.QUERY_API;

import uk.gov.justice.domain.annotation.Event;
import uk.gov.justice.services.core.annotation.CustomServiceComponent;
import uk.gov.justice.services.core.annotation.Direct;
import uk.gov.justice.services.core.annotation.FrameworkComponent;
import uk.gov.justice.services.core.annotation.Remote;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.annotation.ServiceComponentLocation;

import java.util.HashSet;

import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.ProcessAnnotatedType;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AnnotationScannerTest {

    private static final String TEST_EVENT_NAME = "Test-Event";

    @Mock
    private AfterDeploymentValidation afterDeploymentValidation;

    @Mock
    private ProcessAnnotatedType processAnnotatedType;

    @Mock
    private AnnotatedType annotatedType;

    @Mock
    private BeanManager beanManager;

    @Mock
    private Bean<Object> bean;

    private AnnotationScanner annotationScanner;

    @Before
    public void setup() {
        annotationScanner = new AnnotationScanner();
    }

    @Test
    public void shouldFireCommandApiFoundEventWithCommandApi() throws Exception {
        doReturn(TestCommandApiHandler.class).when(bean).getBeanClass();

        verifyIfServiceComponentFoundEventFiredWith(bean);
    }

    @Test
    public void shouldFireCommandControllerFoundEventWithCommandController() throws Exception {
        doReturn(TestCommandController.class).when(bean).getBeanClass();

        verifyIfServiceComponentFoundEventFiredWith(bean);
    }

    @Test
    public void shouldFireCommandHandlerFoundEventWithCommandHandler() throws Exception {
        doReturn(TestCommandHandler.class).when(bean).getBeanClass();

        verifyIfServiceComponentFoundEventFiredWith(bean);
    }

    @Test
    public void shouldFireRemoteQueryApiHandlerFoundEventWithRemoteQueryApi() throws Exception {
        doReturn(TestRemoteQueryApiHandler.class).when(bean).getBeanClass();

        verifyIfRemoteServiceComponentFoundEventFiredWith(bean);
    }

    @Test
    public void shouldFireDirectQueryApiHandlerFoundEventWithRemoteQueryApi() throws Exception {
        doReturn(TestDirectQueryApiHandler.class).when(bean).getBeanClass();

        verifyIfRemoteServiceComponentFoundEventFiredWith(bean);
    }


    @Test
    public void shouldFireServiceComponentFoundEventForFrameworkComponentAnnotation() throws Exception {
        doReturn(TestFrameworkComponent.class).when(bean).getBeanClass();

        verifyIfServiceComponentFoundEventFiredWith(bean);
    }

    @Test
    public void shouldFireServiceComponentFoundEventForCustomServiceComponentAnnotation() throws Exception {
        doReturn(TestCustomServiceComponent.class).when(bean).getBeanClass();

        verifyIfServiceComponentFoundEventFiredWith(bean);
    }

    @Test
    public void shouldFireEventFoundEventWithTestEvent() throws Exception {
        verifyIfEventFoundEventFiredWith(processAnnotatedType);
    }


    @Test
    public void shouldNotFireAnyEventWithNoHandler() throws Exception {
        doReturn(Object.class).when(bean).getBeanClass();

        mockBeanManagerGetBeansWith(bean);

        annotationScanner.afterDeploymentValidation(afterDeploymentValidation, beanManager);

        verify(beanManager, never()).fireEvent(any());
    }

    @SuppressWarnings("serial")
    private void mockBeanManagerGetBeansWith(Bean<Object> handler) {
        doReturn(new HashSet<Bean<Object>>() {
            {
                add(handler);
            }
        }).when(beanManager).getBeans(any(), any());
    }

    private void mockProcessAnnotatedType() {
        doReturn(annotatedType).when(processAnnotatedType).getAnnotatedType();
        doReturn(true).when(annotatedType).isAnnotationPresent(Event.class);
        doReturn(TestEvent.class).when(annotatedType).getJavaClass();
        doReturn(TestEvent.class.getAnnotation(Event.class)).when(annotatedType).getAnnotation(Event.class);
    }

    private void verifyIfServiceComponentFoundEventFiredWith(final Bean<Object> handler) {
        final ArgumentCaptor<ServiceComponentFoundEvent> captor = ArgumentCaptor.forClass(ServiceComponentFoundEvent.class);
        mockBeanManagerGetBeansWith(handler);

        annotationScanner.afterDeploymentValidation(afterDeploymentValidation, beanManager);

        verify(beanManager).fireEvent(captor.capture());
        assertThat(captor.getValue(), instanceOf(ServiceComponentFoundEvent.class));
        assertThat(captor.getValue().getLocation(), equalTo(ServiceComponentLocation.LOCAL));
    }

    private void verifyIfRemoteServiceComponentFoundEventFiredWith(final Bean<Object> handler) {
        final ArgumentCaptor<ServiceComponentFoundEvent> captor = ArgumentCaptor.forClass(ServiceComponentFoundEvent.class);
        mockBeanManagerGetBeansWith(handler);

        annotationScanner.afterDeploymentValidation(afterDeploymentValidation, beanManager);

        verify(beanManager).fireEvent(captor.capture());
        assertThat(captor.getValue(), instanceOf(ServiceComponentFoundEvent.class));
        assertThat(captor.getValue().getLocation(), equalTo(ServiceComponentLocation.REMOTE));
    }

    @SuppressWarnings("unchecked")
    private void verifyIfEventFoundEventFiredWith(final ProcessAnnotatedType processAnnotatedType) {
        final ArgumentCaptor<EventFoundEvent> captor = ArgumentCaptor.forClass(EventFoundEvent.class);
        mockProcessAnnotatedType();

        annotationScanner.processAnnotatedType(processAnnotatedType);
        annotationScanner.afterDeploymentValidation(afterDeploymentValidation, beanManager);

        verify(beanManager).fireEvent(captor.capture());
        assertThat(captor.getValue(), instanceOf(EventFoundEvent.class));
    }

    @ServiceComponent(COMMAND_API)
    public static class TestCommandApiHandler {
    }

    @ServiceComponent(COMMAND_CONTROLLER)
    public static class TestCommandController {
    }

    @ServiceComponent(COMMAND_HANDLER)
    public static class TestCommandHandler {
    }

    @FrameworkComponent("COMPONENT_ABC")
    public static class TestFrameworkComponent {
    }

    @CustomServiceComponent("COMPONENT_XYZ")
    public static class TestCustomServiceComponent {
    }

    @Remote
    @ServiceComponent(QUERY_API)
    public static class TestRemoteQueryApiHandler {
    }

    @Direct
    @ServiceComponent(QUERY_API)
    public static class TestDirectQueryApiHandler {
    }

    @Event(TEST_EVENT_NAME)
    public static class TestEvent {
    }

    public static class TestProvider {

    }
}

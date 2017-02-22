package uk.gov.justice.raml.maven.lintchecker.utils;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.join;
import static org.slf4j.LoggerFactory.getLogger;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;

public class HandlerScanner {

    private static final Logger LOGGER = getLogger(HandlerScanner.class);

    private final Collection<URL> basePackage;

    public HandlerScanner(final Collection<URL> basePackage) {
        this.basePackage = basePackage;
    }

    private Reflections configureReflections(final Collection<URL> basePackage) {
        System.out.println("entered configure reflections : " + basePackage.iterator().next());

        final URL[] urls = basePackage.toArray(new URL[0]);

        final ConfigurationBuilder configuration = new ConfigurationBuilder()
                .setUrls(basePackage)
                .addClassLoader(new URLClassLoader(urls))
                .setScanners(new MethodAnnotationsScanner(), new TypeAnnotationsScanner(), new SubTypesScanner(false));

        final Reflections reflections = new Reflections(configuration);

        return reflections;
    }

    private List<String> scanForActions(final Reflections reflections) {

        final List<String> serviceComponents = reflections.getTypesAnnotatedWith(ServiceComponent.class)
                .stream()
                .map(Class::getName)
                .collect(toList());

        System.out.println("serviceComponents: " + join(" : ", serviceComponents));

        final Set<Method> methodsAnnotatedWith = reflections.getMethodsAnnotatedWith(Handles.class);



        methodsAnnotatedWith.forEach(method -> {
            System.out.println("Method : " + method.getName());
            System.out.println("Declaring class " + method.getDeclaringClass().getName());
        });

        final List<Annotation> annotations = methodsAnnotatedWith.stream()
                .map(method -> method.getAnnotations())
                .flatMap(annotations1 -> Arrays.stream(annotations1))
                .collect(toList());

        annotations.forEach(annotation -> System.out.println("Annotation: " + annotation.annotationType().getName()));

        return reflections
                .getMethodsAnnotatedWith(Handles.class)
                .stream()
                .peek(method -> System.out.println("Found annotated method: " + method.getDeclaringClass().getSimpleName()))
                .filter(method -> serviceComponents.contains(method.getDeclaringClass().getName()))
                .peek(method -> System.out.println("Found annotated service component method: " + method.getDeclaringClass().getSimpleName()))
                .map(this::actionName)
                .collect(toList());
    }

    private String actionName(final Method method) {

        final List<Annotation> annotations = Arrays.asList(method.getDeclaredAnnotations());
        final List<Annotation> handlesAnnotations = annotations
                .stream()
                .peek(annotation -> System.out.println("Peek: PRE actionName() : " + annotation.annotationType()))
                .filter(annotation -> "uk.gov.justice.services.core.annotation.Handles".equals(annotation.annotationType().getName()))
                .peek(annotation -> System.out.println("Peek: POST actionName() : " + annotation.annotationType()))
                .collect(toList());

        System.out.println("SIZE: " + handlesAnnotations.size());

        final Annotation annotation1 = handlesAnnotations.get(0);

        ((Handles) annotation1).value();

        try {
            final Field action = annotation1.getClass().getDeclaredFigit eld("value");
            System.out.println("action " + action);
        } catch (NoSuchFieldException e) {
            System.out.println(e);
        }

        handlesAnnotations.stream()
                .peek(annotation -> System.out.println(((Handles)annotation).value()));

        return method.getAnnotation(Handles.class).value();
    }

    public Collection<String> getHandlesActions() {
        return scanForActions(configureReflections(basePackage));
    }
}
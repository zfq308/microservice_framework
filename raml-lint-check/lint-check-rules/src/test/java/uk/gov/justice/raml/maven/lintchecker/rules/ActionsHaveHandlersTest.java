package uk.gov.justice.raml.maven.lintchecker.rules;

import static org.mockito.Mockito.when;
import static uk.gov.justice.raml.maven.lintchecker.configuration.TestConfiguration.testConfig;
import static uk.gov.justice.services.test.utils.common.reflection.ReflectionUtils.setField;

import uk.gov.justice.raml.maven.lintchecker.LintCheckConfiguration;
import uk.gov.justice.raml.maven.lintchecker.LintCheckerException;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.reflections.util.ClasspathHelper;

@RunWith(MockitoJUnitRunner.class)
public class ActionsHaveHandlersTest {

    @Mock
    private MavenProject mavenProject;

    @Mock
    private Log log;

    @Test
    public void shouldMatchAllActionsToHandlers() throws LintCheckerException, DependencyResolutionRequiredException {

        when(mavenProject.getRuntimeClasspathElements()).thenReturn(urls());

        final ActionsHaveHandlers actionsHaveHandlers = new ActionsHaveHandlers();
        setField(actionsHaveHandlers, "basePackage", testConfig().basePackage());
        actionsHaveHandlers.execute(testConfig().ramlGET(), configuration());

    }

    @Test(expected = LintCheckerException.class)
    public void shouldThrowLintCheckerException() throws LintCheckerException {

        final ActionsHaveHandlers actionsHaveHandlers = new ActionsHaveHandlers();
        setField(actionsHaveHandlers, "basePackage", testConfig().basePackage());
        actionsHaveHandlers.execute(testConfig().ramlGETmissing(), configuration());

    }

    private List<String> urls() {
        return ClasspathHelper.forPackage(testConfig().basePackage())
                .stream()
                .map(url -> {
                    try {
                        return Paths.get(url.toURI());
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    } return null;
                })
                .map(Path::toString)
                .collect(Collectors.toList());
    }

    private LintCheckConfiguration configuration() {
        return new LintCheckConfiguration(mavenProject, log);
    }

}
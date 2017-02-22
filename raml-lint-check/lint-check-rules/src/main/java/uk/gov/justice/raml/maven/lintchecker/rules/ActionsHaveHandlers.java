package uk.gov.justice.raml.maven.lintchecker.rules;

import static java.lang.String.format;
import static java.lang.String.join;
import static uk.gov.justice.raml.maven.lintchecker.utils.RamlActionFinder.actionsFrom;

import uk.gov.justice.raml.maven.lintchecker.LintCheckConfiguration;
import uk.gov.justice.raml.maven.lintchecker.LintCheckerException;
import uk.gov.justice.raml.maven.lintchecker.utils.HandlerScanner;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.raml.model.Raml;

public class ActionsHaveHandlers implements LintCheckRule{

    private String basePackage; //Initialised from maven config by maven

    @Override
    public void execute(final Raml raml, final LintCheckConfiguration config) throws LintCheckerException {

        config.getLog().info("Executing raml lint checker");
        config.getLog().info("basePackage: " + basePackage);
        final HandlerScanner scanner;

        Collection<String> actionsFromHandlers = null;

        try {
            final List<URL> urls = config.getMavenProject().getRuntimeClasspathElements()
                    .stream()
                    .map(s -> {
                        try {
                            return Paths.get(s).toUri().toURL();
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        } return null;
                    }).collect(Collectors.toList());

            urls.forEach(url -> config.getLog().info("URL: " + url.getPath()));

            scanner = new HandlerScanner(urls);
            actionsFromHandlers = scanner.getHandlesActions();
        } catch (DependencyResolutionRequiredException e) {
            e.printStackTrace();
        }

        config.getLog().info("actionsFromHandlers : " + actionsFromHandlers.size());
        config.getLog().info("handlers found: " +  join(" , ", actionsFromHandlers));

        final Collection<String> actionsFromRaml = actionsFrom(raml);

        final Collection<String> unmatchedHandlers = new ArrayList<>(actionsFromHandlers);
        unmatchedHandlers.removeAll(actionsFromRaml);

        final Collection<String> unmatchedActions = new ArrayList<>(actionsFromRaml);
        unmatchedActions.removeAll(actionsFromHandlers);

        if(!unmatchedActions.isEmpty() || !unmatchedHandlers.isEmpty()) {
            throw new LintCheckerException(exceptionMessage(unmatchedActions, unmatchedHandlers));
        }
    }

    private String exceptionMessage(final Collection<String> actions, final Collection<String> handlers) {

        final String message = "Actions have handlers lint check rule failure, " +
                "the following actions in raml have no valid handlers: %s and the " +
                "following handlers have been found without matching actions in raml: %s";

        return format(message, join(", ", actions), join(", ", handlers));
    }
}

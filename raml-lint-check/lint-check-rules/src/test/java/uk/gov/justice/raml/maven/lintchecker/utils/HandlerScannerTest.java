package uk.gov.justice.raml.maven.lintchecker.utils;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.justice.raml.maven.lintchecker.configuration.TestConfiguration.testConfig;

import java.util.Collection;

import org.junit.Test;
import org.reflections.util.ClasspathHelper;


public class HandlerScannerTest {

    @SuppressWarnings({"unchecked"})
    @Test
    public void shouldMatchValidActions() {

        final HandlerScanner handlerScanner =
                new HandlerScanner(ClasspathHelper.forPackage(testConfig().basePackage()));

        final Collection<String> handlesActions = handlerScanner.getHandlesActions();

        assertThat(handlesActions.size(), is(2));
        assertThat(handlesActions, hasItems(is("test.firstcommand"), is("test.secondcommand")));
        assertThat(handlesActions, not(hasItem(is("test.thirdcommand"))));
    }
}
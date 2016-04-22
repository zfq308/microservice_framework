package uk.gov.justice.services.adapters.test.utils.config;

import org.junit.rules.TemporaryFolder;
import uk.gov.justice.raml.core.GeneratorConfig;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class GeneratorConfigUtil {
    
    public static GeneratorConfig configurationWithBasePackage(final String basePackageName,
                                                               final TemporaryFolder outputFolder,
                                                               final Map<String, String> generatorProperties) {
        Path outputPath = Paths.get(outputFolder.getRoot().getAbsolutePath());
        return new GeneratorConfig(outputPath, outputPath, basePackageName, generatorProperties);
    }

}
package com.jin.practice.config;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

class ApplicationPropertiesSecurityTest {

    @Test
    void productionSecretsComeFromEnvironmentPlaceholders() throws IOException {
        Properties properties = new Properties();
        try (var reader = Files.newBufferedReader(Path.of("src/main/resources/application.properties"))) {
            properties.load(reader);
        }

        assertThat(properties.getProperty("spring.datasource.password")).startsWith("${");
        assertThat(properties.getProperty("jwt.secret")).startsWith("${");
        assertThat(properties.getProperty("spring.datasource.username")).startsWith("${");
    }
}

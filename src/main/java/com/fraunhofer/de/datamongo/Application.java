package com.fraunhofer.de.datamongo;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    /**
     * Creates the OpenAPI main description. The description contains general project information
     * such as e.g. title, version and contact information.
     *
     * @return The OpenAPI description.
     * @throws IOException Throws an exception if the properties cannot be loaded from file.
     */
    @Bean
    public OpenAPI customOpenAPI() throws IOException {
        final var properties = new Properties();
        try (InputStream inputStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("application.properties")) {
            properties.load(inputStream);
        }

        return new OpenAPI()
                .components(new Components())
                .info(new Info()
                        .title(properties.getProperty("title"))
                        .description(properties.getProperty("project_desc"))
                        .version(properties.getProperty("version"))
                        .contact(new Contact()
                                .name(properties.getProperty("title"))
                                .url(properties.getProperty("contact_url"))
                                .email(properties.getProperty("contact_email"))
                        )
                        .license(new License()
                                .name(properties.getProperty("license"))
                                .url(properties.getProperty("license_url")))
                );
    }

    @Bean
    public WebMvcConfigurer corsConfigurer()
    {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry
                        .addMapping("/**")
                        .allowedOrigins("*")
                        .allowedHeaders("*");
            }
        };
    }
}

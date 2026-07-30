package com.apexon.railEasy.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI / Swagger documentation configuration, including the JWT bearer scheme.
 */
@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI railEasyOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("RailEasy Train Ticket API")
                        .description("Reactive REST API for train ticket search and booking")
                        .version("v1.0.0")
                        .contact(new Contact().name("RailEasy Team").email("support@raileasy.com"))
                        .license(new License().name("Apache 2.0")))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}


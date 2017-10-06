/*
 * Copyright (c) 2013 The Finnish Board of Education - Opetushallitus
 *
 * This program is free software: Licensed under the EUPL, Version 1.1 or - as
 * soon as they will be approved by the European Commission - subsequent versions
 * of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at: http://ec.europa.eu/idabc/eupl
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * European Union Public Licence for more details.
 */
package fi.vm.sade.eperusteet.ylops.resource.config;

import com.fasterxml.classmate.GenericType;
import com.fasterxml.classmate.TypeResolver;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.schema.AlternateTypeRules;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.paths.AbstractPathProvider;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger1.annotations.EnableSwagger;

import javax.servlet.ServletContext;
import java.util.Optional;
import java.util.concurrent.Callable;

import static com.google.common.base.Predicates.not;
import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * @author jhyoty
 */
@Configuration
@EnableSwagger
@Profile(value = {"!dev"})
public class SwaggerConfig {
    private static final Logger LOG = LoggerFactory.getLogger(SwaggerConfig.class);

    @Autowired
    private TypeResolver typeResolver;

    @Bean
    public Docket swaggerApi(ServletContext ctx) {
        LOG.debug("Starting Swagger API");


        return new Docket(DocumentationType.SWAGGER_12)
                .apiInfo(apiInfo())
                .pathProvider(new RelativeSwaggerPathProvider(ctx))
                .select()
                .apis(not(RequestHandlerSelectors.withClassAnnotation(InternalApi.class)))
                .build()
                .directModelSubstitute(JsonNode.class, Object.class)
                .genericModelSubstitutes(ResponseEntity.class, Optional.class)
                .alternateTypeRules(
                        AlternateTypeRules.newRule(
                                typeResolver.resolve(new GenericType<Callable<ResponseEntity<Object>>>() {
                                }),
                                typeResolver.resolve(Object.class)
                        )
                );
    }

    /**
     * API Info as it appears on the swagger-ui page
     */
    private ApiInfo apiInfo() {
        Contact contact = null;
        return new ApiInfo(
                "Oppijan verkkopalvelukokonaisuus / ePerusteet julkinen rajapinta",
                "",
                "Spring MVC API based on the swagger 1.2 spec",
                "https://confluence.csc.fi/display/oppija/Rajapinnat+toisen+asteen+ja+perusasteen+toimijoille",
                contact,
                "EUPL 1.1",
                "http://ec.europa.eu/idabc/eupl"
        );
    }

    private class RelativeSwaggerPathProvider extends AbstractPathProvider {
        String ROOT = "/";
        private final ServletContext servletContext;

        RelativeSwaggerPathProvider(ServletContext servletContext) {
            super();
            this.servletContext = servletContext;
        }

        @Override
        protected String applicationPath() {
            return isNullOrEmpty(servletContext.getContextPath())
                    ? ROOT : servletContext.getContextPath() + "/api";
        }

        @Override
        protected String getDocumentationPath() {
            return ROOT;
        }
    }
}

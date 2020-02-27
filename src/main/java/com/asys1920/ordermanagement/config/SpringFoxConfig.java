package com.asys1920.ordermanagement.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.service.Tag;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2WebMvc;

import java.util.Collections;

@EnableSwagger2WebMvc
@Configuration
public class SpringFoxConfig {
    @Bean
    public Docket docket() {
        return new Docket(DocumentationType.SWAGGER_2)
                .tags(new Tag("Order Entity", "Repository for Order entities"))
                .apiInfo(new ApiInfo(
                        "ASYS car rental userservice",
                        "Some custom description of API.",
                        "API V1",
                        "Terms of service",
                        new Contact("John Doe", "www.example.com", "myeaddress@company.com"),
                        "License of API", "API license URL", Collections.emptyList()));
    }

}
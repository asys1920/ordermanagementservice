package com.asys1920.ordermanagement.adapter;

import com.asys1920.ordermanagement.config.RestTemplatesConfiguration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;

public abstract class ServiceAdapter {

    RestTemplatesConfiguration restTemplatesConfiguration;
    RestTemplate restTemplate;

    public ServiceAdapter(RestTemplatesConfiguration restTemplatesConfiguration, RestTemplateBuilder restTemplateBuilder) {
        this.restTemplatesConfiguration = restTemplatesConfiguration;
        this.restTemplate = restTemplateBuilder.build();
    }
}

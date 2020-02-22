
package com.asys1920.ordermanagement.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.NoSuchElementException;

@Configuration
@ConfigurationProperties(value = "ordermanagement")
public class RestTemplatesConfiguration {

    private static final String USER = "user";
    private static final String CAR = "car";
    private static final String ACCOUNTING = "accounting";

    private Map<String, String> services;

    public String getAccountingHost() {
        return getHost(ACCOUNTING);
    }

    private String getCar() {
        return getHost(CAR);
    }

    public String getUserHost() {
        return getHost(USER);
    }

    private String getHost(String serviceName) {
        String host = services.get(serviceName);
        if (host == null) {
            throw new NoSuchElementException(serviceName + " has not been configured in the application.yml.");
        }
        return host;
    }

    public void setServices(Map<String, String> services) {
        this.services = services;
    }
}
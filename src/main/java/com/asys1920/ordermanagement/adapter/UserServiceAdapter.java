package com.asys1920.ordermanagement.adapter;

import com.asys1920.dto.UserDTO;
import com.asys1920.ordermanagement.config.RestTemplatesConfiguration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import com.asys1920.model.User;

@Service
public class UserServiceAdapter extends ServiceAdapter {

    UserServiceAdapter(RestTemplatesConfiguration restTemplatesConfiguration, RestTemplateBuilder restTemplateBuilder) {
        super(restTemplatesConfiguration, restTemplateBuilder);
    }

    public User getUser() {
        restTemplate.getForObject(restTemplatesConfiguration.getUserHost(), UserDTO.class);
        return null;
    }
}

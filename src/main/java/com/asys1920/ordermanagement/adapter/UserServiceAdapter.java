package com.asys1920.ordermanagement.adapter;

import com.asys1920.dto.UserDTO;
import com.asys1920.mapper.UserMapper;
import com.asys1920.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class UserServiceAdapter {
    @Value("http://localhost:8080/users/")
    private String userServiceUrl;
    final RestTemplate restTemplate;

    public UserServiceAdapter(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }


    public User getUser(Long userId) {
        UserDTO userDTO = restTemplate
                .getForObject(userServiceUrl + userId, UserDTO.class);
        return UserMapper.INSTANCE.userDTOtoUser(userDTO);
    }
}
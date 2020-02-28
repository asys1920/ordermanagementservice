package com.asys1920.ordermanagement.adapter;

import com.asys1920.dto.UserDTO;
import com.asys1920.mapper.UserMapper;
import com.asys1920.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.naming.ServiceUnavailableException;

@Component
public class UserServiceAdapter {
    @Value("${user.url}")
    private String userServiceUrl;
    final RestTemplate restTemplate;

    public UserServiceAdapter(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    /**
     * Fetches a user from the user service
     * @param userId id of the user to be fetched
     * @return the use object associated with the given id
     */
    public User getUser(Long userId) throws ServiceUnavailableException {
        try {
            UserDTO userDTO = restTemplate
                    .getForObject(userServiceUrl + userId, UserDTO.class);
            return UserMapper.INSTANCE.userDTOtoUser(userDTO);
        } catch (Exception ex) {
            throw new ServiceUnavailableException("UserService is currently unavailable. Please try again later.");
        }
    }
}

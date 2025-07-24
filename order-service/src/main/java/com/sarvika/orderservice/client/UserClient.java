package com.sarvika.orderservice.client;

import com.sarvika.orderservice.dto.UserDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.beans.factory.annotation.Value;


@Component
public class UserClient {

    private final RestTemplate restTemplate;
    
    @Value("${user.service.url}")
    private String userServiceUrl;

    @Autowired
    public UserClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public UserDTO getUserById(Long userId) {
        try {
            // String url = "http://localhost:8081/api/users/" + userId;
            String url = userServiceUrl + "/users/" + userId;
            return restTemplate.getForObject(url, UserDTO.class);
        } catch (HttpClientErrorException.NotFound e) {
            return null;
        }
    }
}

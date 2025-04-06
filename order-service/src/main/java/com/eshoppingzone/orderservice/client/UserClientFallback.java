package com.eshoppingzone.orderservice.client;

import com.eshoppingzone.orderservice.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserClientFallback implements UserClient {
    @Override
    public User getUserById(Long id) {
        // Return a dummy user or null or throw a custom exception
        System.out.println("User service is down, fallback executed!");
        return null;
    }
}

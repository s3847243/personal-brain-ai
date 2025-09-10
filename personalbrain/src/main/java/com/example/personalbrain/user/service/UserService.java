package com.example.personalbrain.user.service;

import org.springframework.stereotype.Service;

import com.example.personalbrain.user.model.User;
import com.example.personalbrain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    public void deleteAccount(User user) {
        userRepository.delete(user);
    }
}   
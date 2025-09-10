package com.example.personalbrain.user.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.personalbrain.user.model.User;

public interface UserRepository extends JpaRepository<User,UUID> {

    Optional<User> findById(UUID userId);
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    
}
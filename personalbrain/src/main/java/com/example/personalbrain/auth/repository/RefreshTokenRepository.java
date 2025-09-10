package com.example.personalbrain.auth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.personalbrain.auth.model.RefreshToken;
import com.example.personalbrain.user.model.User;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    int deleteByUser(User user);
    Optional<RefreshToken> findByUser(User user);

}

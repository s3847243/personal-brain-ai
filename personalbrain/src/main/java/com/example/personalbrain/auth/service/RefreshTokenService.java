package com.example.personalbrain.auth.service;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.personalbrain.auth.model.RefreshToken;
import com.example.personalbrain.auth.repository.RefreshTokenRepository;
import com.example.personalbrain.user.model.User;
import com.example.personalbrain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    @Value("${app.jwt.refreshExpirationMs}")
    private Long refreshTokenDurationMs;

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    public RefreshToken createRefreshToken(User user) {
        Optional<RefreshToken> existingToken = refreshTokenRepository.findByUser(user);
    
        if (existingToken.isPresent()) {
            RefreshToken token = existingToken.get();
            token.setToken(UUID.randomUUID().toString());
            token.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
            return refreshTokenRepository.save(token);
        } else {
            RefreshToken newToken = new RefreshToken();
            newToken.setUser(user);
            newToken.setToken(UUID.randomUUID().toString());
            newToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
            return refreshTokenRepository.save(newToken);
        }
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);
            throw new RuntimeException("Refresh token expired. Please login again.");
        }
        return token;
    }

    public int revokeByUserId(UUID userId) {
        return refreshTokenRepository.deleteByUser(
                userRepository.findById(userId).orElseThrow());
    }
    public Optional<RefreshToken> findByToken(String token){
        return refreshTokenRepository.findByToken(token);
    }
}
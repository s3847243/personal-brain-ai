package com.example.personalbrain.auth.service;
import java.time.Instant;
import java.util.UUID;

import org.bouncycastle.its.ITSPublicEncryptionKey.symmAlgorithm;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.personalbrain.auth.dto.AuthResponse;
import com.example.personalbrain.auth.dto.LoginRequest;
import com.example.personalbrain.auth.dto.RegisterRequest;
import com.example.personalbrain.user.dto.UserDTO;
import com.example.personalbrain.user.model.User;
import com.example.personalbrain.user.repository.UserRepository;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final AuthenticationManager authenticationManager;


  

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }
    
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setJobTitle(request.getJobTitle());
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());
    
        userRepository.save(user);
    
        String accessToken = jwtService.generateToken(user);
        String refreshToken = refreshTokenService.createRefreshToken(user).getToken();
    
        return AuthResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .build();
    }

    public AuthResponse login(LoginRequest request) {
         authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new RuntimeException("Invalid credentials"));
    
       
    
        String accessToken = jwtService.generateToken(user);
        String refreshToken = refreshTokenService.createRefreshToken(user).getToken();
        System.out.println(accessToken + "  accessToken");
        return AuthResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .user(UserDTO.from(user))
            .build();
    }
    public User verifyToken(String accessToken) {
        try {
            // Validate and parse the JWT token
            Claims claims = jwtService.getClaims(accessToken);
            
            // Extract user information from token
            UUID userId = UUID.fromString(claims.getSubject());

            // Optionally fetch fresh user data from database
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            return user;
            
        } catch (Exception e) {
            throw new RuntimeException("Invalid or expired access token");
        }
    }
}
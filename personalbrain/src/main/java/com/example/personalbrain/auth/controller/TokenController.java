package com.example.personalbrain.auth.controller;
import java.time.Duration;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.personalbrain.auth.dto.AuthResponse;
import com.example.personalbrain.auth.model.RefreshToken;
import com.example.personalbrain.auth.service.AuthService;
import com.example.personalbrain.auth.service.JwtService;
import com.example.personalbrain.auth.service.RefreshTokenService;
import com.example.personalbrain.user.dto.UserDTO;
import com.example.personalbrain.user.model.User;
import com.example.personalbrain.user.model.UserPrincipal;

import jakarta.servlet.http.HttpServletResponse;


import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class TokenController {

    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;
    private final AuthService authService;


  
}
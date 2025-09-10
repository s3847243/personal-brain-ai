package com.example.personalbrain.user.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.personalbrain.user.dto.UserDTO;
import com.example.personalbrain.user.model.User;
import com.example.personalbrain.user.model.UserPrincipal;
import com.example.personalbrain.user.service.UserService;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteAccount(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            HttpServletResponse response) {

        if (userPrincipal == null || userPrincipal.getUser() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        ResponseCookie accessCookie = ResponseCookie.from("accessToken", "")
            .httpOnly(true)
            .secure(false) 
            .path("/")
            .maxAge(0)
            .sameSite("Lax")
            .build();

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", "")
            .httpOnly(true)
            .secure(false)
            .path("/")
            .maxAge(0)
            .sameSite("Lax")
            .build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        userService.deleteAccount(userPrincipal.getUser());

        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/me")
    public UserDTO me(@AuthenticationPrincipal UserPrincipal principal) {
        User u = principal.getUser(); 
        return UserDTO.from(u);
    }

        
    
}

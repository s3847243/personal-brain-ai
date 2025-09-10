package com.example.personalbrain.auth.dto;


import com.example.personalbrain.user.dto.UserDTO;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor

public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private UserDTO user;




    public AuthResponse(String accessToken, String refreshToken, UserDTO user) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.user = user;
    }



}
package com.example.personalbrain.user.dto;

import java.util.UUID;

import com.example.personalbrain.user.model.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private UUID id;
    private String email;
    private String fullName;
    private String jobTitle;
    
    public static UserDTO from(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .jobTitle(user.getJobTitle())
                .build();
    }
}
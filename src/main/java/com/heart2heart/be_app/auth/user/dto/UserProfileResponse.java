package com.heart2heart.be_app.auth.user.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class UserProfileResponse {
    private UUID id;
    private String email;
    private String name;
    private String phoneNumber;
    private String role;

    public UserProfileResponse(UUID id, String email, String name, String phoneNumber, String role) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.role = role;
    }
}

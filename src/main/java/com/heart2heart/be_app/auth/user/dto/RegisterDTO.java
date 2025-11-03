package com.heart2heart.be_app.auth.user.dto;

import lombok.Data;

@Data
public class RegisterDTO {
    private String password;
    private String email;
    private String phone;
    private String fullName;
}

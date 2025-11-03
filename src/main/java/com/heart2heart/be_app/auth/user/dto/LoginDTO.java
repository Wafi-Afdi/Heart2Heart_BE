package com.heart2heart.be_app.auth.user.dto;


import lombok.Data;

@Data
public class LoginDTO {
    private String email;
    private String password;
}

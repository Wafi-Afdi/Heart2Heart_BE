package com.heart2heart.be_app.auth.user.dto;

import lombok.Data;

@Data
public class RegisterRespons {
    private Boolean isSuccess;
    private String message;

    public RegisterRespons(String message, Boolean isSuccess) {
        this.message = message;
        this.isSuccess = isSuccess;
    }
}

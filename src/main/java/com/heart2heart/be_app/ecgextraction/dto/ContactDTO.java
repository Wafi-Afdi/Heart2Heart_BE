package com.heart2heart.be_app.ecgextraction.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class ContactDTO {
    private UUID userId;
    private String name;
    private String email;
    private String phone;
}

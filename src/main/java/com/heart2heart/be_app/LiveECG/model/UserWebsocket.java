package com.heart2heart.be_app.LiveECG.model;

import lombok.Data;

@Data
public class UserWebsocket {
    private String email;
    private String name;

    public UserWebsocket(String email , String name) {
        this.email = email;
        this.name = name;
    }
}

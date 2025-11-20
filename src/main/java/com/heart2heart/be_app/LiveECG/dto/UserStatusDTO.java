package com.heart2heart.be_app.LiveECG.dto;

import lombok.Data;

@Data
public class UserStatusDTO {
    private String name;
    private String email;
    private Boolean isAmbulatory;
    private Boolean isBluetoothConnected;
    private Boolean isDisconnecting;
}

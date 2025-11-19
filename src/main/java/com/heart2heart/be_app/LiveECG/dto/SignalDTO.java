package com.heart2heart.be_app.LiveECG.dto;

import lombok.Data;

@Data
public class SignalDTO {
    private Float signal;
    private String ts;
}

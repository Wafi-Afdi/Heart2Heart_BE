package com.heart2heart.be_app.LiveECG.dto;

import lombok.Data;

import java.util.List;

@Data
public class LiveDataDTO {
    private List<SignalDTO> ecgList;
}

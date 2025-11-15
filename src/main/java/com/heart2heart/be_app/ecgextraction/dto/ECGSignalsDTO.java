package com.heart2heart.be_app.ecgextraction.dto;

import lombok.Data;

import java.util.List;

@Data
public class ECGSignalsDTO {
    private String userId;
    private List<ECGSignalDTO> ecgData;
}

package com.heart2heart.be_app.ArrhythmiaReport.dto;

import lombok.Data;

@Data
public class SOSRequestDTO {
    private String ts;
    private Float lat;
    private Float longitude;
}

package com.heart2heart.be_app.ArrhythmiaReport.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TachyOrBradyDTO {
    private String ts;
    private String type;
}

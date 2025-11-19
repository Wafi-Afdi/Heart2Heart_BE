package com.heart2heart.be_app.ArrhythmiaReport.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class SaveSegmentDTO {
    private String ts;
    private String reportId;
    private String userId;
    private int totalSecondToSave;
}
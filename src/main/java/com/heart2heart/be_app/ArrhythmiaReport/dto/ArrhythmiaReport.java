package com.heart2heart.be_app.ArrhythmiaReport.dto;

import com.heart2heart.be_app.ArrhythmiaReport.model.ECGSegment;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ArrhythmiaReport {
    private String reportId;
    private String reportType;
    private List<ECGSegment> ecgSegment;
    private LocalDateTime timestamp;
}

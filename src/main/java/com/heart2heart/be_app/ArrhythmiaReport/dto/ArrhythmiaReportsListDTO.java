package com.heart2heart.be_app.ArrhythmiaReport.dto;

import lombok.Data;

import java.util.List;

@Data
public class ArrhythmiaReportsListDTO {
    private String userId;
    private List<ArrhythmiaReport> arrhythmiaReports;
}

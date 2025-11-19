package com.heart2heart.be_app.ArrhythmiaReport.dto;

import lombok.Data;

import java.util.List;

@Data
public class EcgListDTO {
    private List<Float> ecgSignal;
}

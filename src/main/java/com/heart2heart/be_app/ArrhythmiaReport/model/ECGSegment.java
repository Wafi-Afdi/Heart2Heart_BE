package com.heart2heart.be_app.ArrhythmiaReport.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ECGSegment {
    private String userId;
    private Float signal;
    private Boolean asystole;
    private Boolean rPeak;
    private LocalDateTime ts;
}

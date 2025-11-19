package com.heart2heart.be_app.ArrhythmiaReport.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ECGSegment {
    private String userId;
    private List<Float> signal;
    private LocalDateTime start;
    private LocalDateTime end;
}

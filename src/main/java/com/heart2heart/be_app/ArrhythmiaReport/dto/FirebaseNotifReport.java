package com.heart2heart.be_app.ArrhythmiaReport.dto;

import lombok.Data;

@Data
public class FirebaseNotifReport {
    private String userId;
    private String topic;
    private String username;
    private String report;
}

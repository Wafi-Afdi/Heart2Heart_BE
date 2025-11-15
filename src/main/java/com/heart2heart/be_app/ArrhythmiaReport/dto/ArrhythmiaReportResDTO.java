package com.heart2heart.be_app.ArrhythmiaReport.dto;

import com.heart2heart.be_app.auth.user.dto.UserProfileResponse;
import lombok.Data;

@Data
public class ArrhythmiaReportResDTO {
    private ArrhythmiaReport report;
    private Boolean isEmpty;
    private UserProfileResponse userData;
}

package com.heart2heart.be_app.ecgextraction.dto;

import lombok.Data;

@Data
public class ECGSignalDTO {
    private Float signal;
    private Boolean rp;
    private Boolean flat;
    private String ts;
}

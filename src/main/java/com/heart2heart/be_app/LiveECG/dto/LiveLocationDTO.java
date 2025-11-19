package com.heart2heart.be_app.LiveECG.dto;

import lombok.Data;

@Data
public class LiveLocationDTO {
    private Float lat;
    private Float longitude;
    private String city;
    private String country;
}

package com.heart2heart.be_app.ecgextraction.dto;

import lombok.Data;

import java.util.List;

@Data
public class BPMUserDataDTO {
    private String userId;
    private List<BPMDataDTO> bpmDatas;
}

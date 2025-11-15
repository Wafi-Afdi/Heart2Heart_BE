package com.heart2heart.be_app.ecgextraction.service;

import com.heart2heart.be_app.auth.user.model.User;
import com.heart2heart.be_app.ecgextraction.dto.BPMDataDTO;
import com.heart2heart.be_app.ecgextraction.dto.BPMUserDataDTO;
import com.heart2heart.be_app.ecgextraction.dto.ECGSignalDTO;
import com.heart2heart.be_app.ecgextraction.model.BPMDataModel;
import com.heart2heart.be_app.ecgextraction.model.ECGSignalModel;
import com.heart2heart.be_app.ecgextraction.repository.BPMDataRepository;
import com.heart2heart.be_app.ecgextraction.repository.ECGSignalRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BpmService {
    private final BPMDataRepository bpmRepo;


    public BpmService(BPMDataRepository _bpmRepo) {
        this.bpmRepo = _bpmRepo;
    }

    @Transactional
    public void saveBPM(User user, List<BPMDataDTO> bpmDatas) {
        List<BPMDataModel> entities = bpmDatas.stream()
                .map(dto -> bpmDTOConverterToModel(user, dto))
                .collect(Collectors.toList());

        bpmRepo.saveAll(entities);
    }

    private BPMDataModel bpmDTOConverterToModel(User user,BPMDataDTO data) {
        return BPMDataModel.builder()
                .user(user)
                .bpm(data.getBpm())
                .timestamp(LocalDateTime.parse(data.getTs()))
                .build();
    }
}

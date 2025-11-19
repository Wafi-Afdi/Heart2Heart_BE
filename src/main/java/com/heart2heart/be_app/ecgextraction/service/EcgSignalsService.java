package com.heart2heart.be_app.ecgextraction.service;

import com.heart2heart.be_app.ArrhythmiaReport.model.ECGSegment;
import com.heart2heart.be_app.auth.user.model.User;
import com.heart2heart.be_app.ecgextraction.dto.ECGSignalDTO;
import com.heart2heart.be_app.ecgextraction.model.ECGSignalModel;
import com.heart2heart.be_app.ecgextraction.repository.ECGSignalRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EcgSignalsService {
    private final ECGSignalRepository ecgSignalRepository;


    public EcgSignalsService(ECGSignalRepository ecgSignalRepository) {
        this.ecgSignalRepository = ecgSignalRepository;
    }

    @Transactional
    public void saveSignals(User user, List<ECGSignalDTO> dtoList) {

        // 1. Convert the list of DTOs to a list of Entities
        List<ECGSignalModel> entities = dtoList.stream()
                .map(dto -> convertDtoToEntity(user, dto))
                .collect(Collectors.toList());

        // 2. Save all entities in a single batch operation
        ecgSignalRepository.saveAll(entities);
    }

    @Transactional
    public List<ECGSegment> getECGSegment(User user, LocalDateTime ts, Integer totalSeconds) {

        LocalDateTime endTime = ts;
        LocalDateTime startTime = ts.minusSeconds(6);
        List<ECGSignalModel> rawSignals = ecgSignalRepository
                .findECGRecordsWithRangeTime(user.getId(), startTime, endTime)
                .orElse(Collections.emptyList());

        List<ECGSegment> segmentDTOs = rawSignals.stream()
                .map(model -> {
                    // Use setters as requested:
                    ECGSegment segment = new ECGSegment();
                    segment.setSignal(model.getSignal());
                    segment.setAsystole(model.getAsystole());
                    segment.setRPeak(model.getRPeak());
                    segment.setTs(model.getTimestamp());
                    return segment;
                })
                .collect(Collectors.toList());

        return segmentDTOs;
    }

    private ECGSignalModel convertDtoToEntity(User user, ECGSignalDTO dto) {
        // Here we perform the mapping from DTO fields to Entity fields
        return ECGSignalModel.builder()
                .user(user)
                .signal(dto.getSignal())
                .asystole(dto.getFlat())
                .rPeak(dto.getRp())
                .timestamp(LocalDateTime.parse(dto.getTs()))
                .build();
    }
}

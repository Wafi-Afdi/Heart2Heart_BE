package com.heart2heart.be_app.ecgextraction.controller;

import com.heart2heart.be_app.auth.user.model.User;
import com.heart2heart.be_app.ecgextraction.dto.ECGSignalDTO;
import com.heart2heart.be_app.ecgextraction.dto.ECGSignalsDTO;
import com.heart2heart.be_app.ecgextraction.model.ECGSignalModel;
import com.heart2heart.be_app.ecgextraction.repository.ECGSignalRepository;
import com.heart2heart.be_app.ecgextraction.service.EcgDataPublisher;
import com.heart2heart.be_app.ecgextraction.service.EcgSignalsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ecgSignal")
@CrossOrigin
public class ECGSignalController {
    private ECGSignalRepository ecgSignalRepo;
    private final EcgSignalsService ecgService;
    private final EcgDataPublisher ecgDataPublisher;
    private static final Logger log = LoggerFactory.getLogger(ECGSignalController.class);

    @Autowired
    public ECGSignalController(ECGSignalRepository _ecgSignalRepo, EcgSignalsService _ecgService, EcgDataPublisher _ecgDataPublisher) {
        ecgSignalRepo = _ecgSignalRepo;
        ecgService = _ecgService;
        ecgDataPublisher = _ecgDataPublisher;
    }

    @PostMapping("/publish")
    public ResponseEntity<?> publishSignal(@AuthenticationPrincipal User user, @RequestBody ECGSignalsDTO signals) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated.");
        }

        if (signals.getEcgData() == null || signals.getEcgData().isEmpty()) {
            return ResponseEntity.badRequest().body("No ECG data provided.");
        }
        try {
            ecgDataPublisher.publishEcgSignals(signals);

            log.info("Successfully saved {} signal(s) for user {}",
                    signals.getEcgData().size(), user.getId());

            return ResponseEntity.ok("Signals saved");
        } catch (DateTimeParseException e) {
            log.warn("Failed to parse timestamp for user {}: {}", user.getId(), e.getMessage());
            return ResponseEntity.badRequest().body("Invalid timestamp format: " + e.getMessage());

        } catch (DataIntegrityViolationException e) {
            log.warn("Data integrity violation for user {}: {}", user.getId(), e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Duplicate data: " + e.getMessage());
        } catch (Exception e) {
            log.error("Failed to save signals for user {}", user.getId(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An internal error occurred.");
        }
    }

    @GetMapping("/range")
    public ResponseEntity<?> getSignalRange(@AuthenticationPrincipal User user,
                                            @RequestParam(name = "start", required = true) String start,
                                            @RequestParam(name = "end", required = true) String end,
                                            @RequestParam(name = "id", required = false) String userId
    ) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated.");
        }
        try {
            LocalDateTime startTime = LocalDateTime.parse(start);
            LocalDateTime endTime = LocalDateTime.parse(end);
            Optional<List<ECGSignalModel>> signalsOpt;
            List<ECGSignalModel> signals;
            if (userId != null) {
                signalsOpt = ecgSignalRepo.findECGRecordsWithRangeTime(
                        UUID.fromString(userId),
                        startTime,
                        endTime
                );
            } else {
                signalsOpt = ecgSignalRepo.findECGRecordsWithRangeTime(
                        user.getId(),
                        startTime,
                        endTime
                );
            }

            if (signalsOpt.isPresent()) {
                signals = signalsOpt.get();
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Data is empty");
            }

            List<ECGSignalDTO> signalDTOs = signals.stream()
                    .map(this::convertEntityToDto)
                    .collect(Collectors.toList());
            ECGSignalsDTO data = new ECGSignalsDTO();
            data.setEcgData(signalDTOs);
            return ResponseEntity.ok(data);

        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest().body("Invalid timestamp format: " + e.getMessage());

        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Duplicate data: " + e.getMessage());

        } catch (Exception e) {
            log.error("Failed to save signals for user {}", user.getId(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An internal error occurred.");
        }
    }

    private ECGSignalDTO convertEntityToDto(ECGSignalModel model) {
        ECGSignalDTO dto = new ECGSignalDTO();
        dto.setSignal(model.getSignal());
        dto.setFlat(model.getAsystole());
        dto.setRp(model.getRPeak());
        dto.setTs(model.getTimestamp().toString());
        return dto;
    }
}

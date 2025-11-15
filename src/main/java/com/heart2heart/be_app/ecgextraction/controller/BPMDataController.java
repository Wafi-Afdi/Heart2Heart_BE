package com.heart2heart.be_app.ecgextraction.controller;

import com.heart2heart.be_app.auth.user.model.User;
import com.heart2heart.be_app.ecgextraction.dto.BPMDataDTO;
import com.heart2heart.be_app.ecgextraction.dto.BPMUserDataDTO;
import com.heart2heart.be_app.ecgextraction.model.BPMDataModel;
import com.heart2heart.be_app.ecgextraction.model.ECGSignalModel;
import com.heart2heart.be_app.ecgextraction.repository.BPMDataRepository;
import com.heart2heart.be_app.ecgextraction.service.BpmPublisher;
import com.heart2heart.be_app.ecgextraction.service.BpmService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/bpmData")
@CrossOrigin
public class BPMDataController {
    private BPMDataRepository bpmRepo;
    private BpmService bpmService;
    private static final Logger log = LoggerFactory.getLogger(BPMDataController.class);
    private final BpmPublisher bpmPublisher;

    @Autowired
    public BPMDataController(BPMDataRepository _bpmRepo, BpmService _bpmService, BpmPublisher bpmPublisher) {
        bpmRepo = _bpmRepo;
        bpmService = _bpmService;
        this.bpmPublisher = bpmPublisher;
    }

    @PostMapping("/publish")
    public ResponseEntity<?> publishBPM(@AuthenticationPrincipal User user, @RequestBody BPMUserDataDTO bpmData) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated.");
        }
        if (bpmData.getBpmDatas() == null || bpmData.getBpmDatas().isEmpty()) {
            return ResponseEntity.badRequest().body("No BPM data provided.");
        }

        try {
            // Call the service to perform the logic
            bpmPublisher.publishBPMData(bpmData);

            return ResponseEntity.ok("Signals saved successfully.");
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
            Optional<List<BPMDataModel>> bpmOpt;
            List<BPMDataModel> bpmData;
            String userIdStore;
            if (userId != null) {
                userIdStore = userId;
                bpmOpt = bpmRepo.findBPMRecordsWithRangeTime(
                        UUID.fromString(userId),
                        startTime,
                        endTime
                );
            } else {
                userIdStore = user.getId().toString();
                bpmOpt = bpmRepo.findBPMRecordsWithRangeTime(
                        user.getId(),
                        startTime,
                        endTime
                );
            }
            if (bpmOpt.isPresent()) {
                bpmData = bpmOpt.get();
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Data is empty");
            }

            List<BPMDataDTO> listBpmDataDto = bpmData.stream()
                    .map(this::convertBPMEntityToDTO)
                    .collect(Collectors.toList());
            BPMUserDataDTO bpmUserDataDTORes = new BPMUserDataDTO();
            bpmUserDataDTORes.setBpmDatas(listBpmDataDto);
            bpmUserDataDTORes.setUserId(userIdStore);
            return ResponseEntity.ok(bpmUserDataDTORes);
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest().body("Invalid timestamp format: " + e.getMessage());

        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Duplicate data: " + e.getMessage());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An internal error occurred.");
        }
    }

    private BPMDataDTO convertBPMEntityToDTO(BPMDataModel bpmData) {
        BPMDataDTO bpmDataDTO = new BPMDataDTO();
        bpmDataDTO.setBpm(bpmData.getBpm());
        bpmDataDTO.setTs(bpmDataDTO.getTs());

        return bpmDataDTO;
    }
}

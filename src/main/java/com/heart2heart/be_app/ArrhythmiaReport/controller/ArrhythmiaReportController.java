package com.heart2heart.be_app.ArrhythmiaReport.controller;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.heart2heart.be_app.ArrhythmiaReport.dto.*;
import com.heart2heart.be_app.ArrhythmiaReport.model.ArrhythmiaReportModel;
import com.heart2heart.be_app.ArrhythmiaReport.repository.ArrhythmiaReportRepo;
import com.heart2heart.be_app.ArrhythmiaReport.service.ReportPublisherService;
import com.heart2heart.be_app.ArrhythmiaReport.service.ReportService;
import com.heart2heart.be_app.auth.user.dto.UserProfileResponse;
import com.heart2heart.be_app.auth.user.model.User;
import com.heart2heart.be_app.ecgextraction.service.EcgSignalsService;
import com.heart2heart.be_app.firebase.FirebaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/report")
@CrossOrigin
public class ArrhythmiaReportController {
    private ArrhythmiaReportRepo arrhythmiaReportRepo;
    private FirebaseService firebaseService;
    private ReportService reportService;
    private ReportPublisherService reportPublisherService;
    private EcgSignalsService ecgSignalsService;

    @Autowired
    public ArrhythmiaReportController(ArrhythmiaReportRepo arrhythmiaReportRepo,
                                      FirebaseService firebaseService,
                                      ReportService reportService,
                                      ReportPublisherService reportPublisherService,
                                      EcgSignalsService ecgSignalsService
    ) {
        this.arrhythmiaReportRepo = arrhythmiaReportRepo;
        this.firebaseService = firebaseService;
        this.reportService = reportService;
        this.reportPublisherService = reportPublisherService;
        this.ecgSignalsService = ecgSignalsService;
    }

    @PostMapping("/SOS")
    public ResponseEntity<?> SOSEmergency(@AuthenticationPrincipal User user, @RequestBody SOSRequestDTO sosRequestDTO) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated.");
        }

        try {
            // firebaseService.sendSOSNotificationToTopic("SOS", user, sosRequestDTO);

            reportPublisherService.sendNotification("SOS", user, "SOS");

            ArrhythmiaReportModel reportEntity = new ArrhythmiaReportModel();
            reportEntity.setReportType(ArrhythmiaReportModel.ReportType.SOS);
            reportEntity.setSegment(ecgSignalsService.getECGSegment(user, LocalDateTime.parse(sosRequestDTO.getTs()), 6));
            reportEntity.setUser(user);
            reportEntity.setTimestamp(LocalDateTime.parse(sosRequestDTO.getTs()));

            arrhythmiaReportRepo.save(reportEntity);
            return ResponseEntity.status(HttpStatus.OK).body("SOS sent");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An internal error occurred.");
        }
    }

    @PostMapping("/generateDiagnosis")
    public ResponseEntity<?> generateDiagnosis(@AuthenticationPrincipal User user, @RequestBody DiagnosisRequestDTO diagnosisRequestDTO) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated.");
        }

        ArrhythmiaReportModel reportEntity = new ArrhythmiaReportModel();
        reportEntity.setReportType(ArrhythmiaReportModel.ReportType.Unknown);
        reportEntity.setSegment(null);
        reportEntity.setUser(user);
        reportEntity.setTimestamp(LocalDateTime.parse(diagnosisRequestDTO.getTs()));
        // reportEntity.setSegment(ecgSignalsService.getECGSegment(user, LocalDateTime.parse(diagnosisRequestDTO.getTs()), 20));



        UUID savedReport = reportService.saveNewReport(reportEntity);

        SaveSegmentDTO saveSegmentPub = new SaveSegmentDTO();
        saveSegmentPub.setTs(diagnosisRequestDTO.getTs());
        saveSegmentPub.setTotalSecondToSave(6);
        saveSegmentPub.setReportId(savedReport.toString());
        saveSegmentPub.setUserId(user.getId().toString());

        reportPublisherService.saveSegmentReport(saveSegmentPub);

        return ResponseEntity.status(HttpStatus.OK).body("Diagnosis is being processed");
    }

    @PostMapping("/generateTachyOrBrady")
    public ResponseEntity<?> generateTachyOrBrady(@AuthenticationPrincipal User user, @RequestBody TachyOrBradyDTO tachyOrBradyDTO) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated.");
        }

        ArrhythmiaReportModel reportEntity = new ArrhythmiaReportModel();
        reportEntity.setReportType(ArrhythmiaReportModel.ReportType.Unknown);
        reportEntity.setSegment(null);
        reportEntity.setUser(user);
        reportEntity.setTimestamp(LocalDateTime.parse(tachyOrBradyDTO.getTs()));
        SaveSegmentDTO saveSegmentPub = new SaveSegmentDTO();
        switch (tachyOrBradyDTO.getType()) {
            case "Tachycardia" -> {
                reportEntity.setReportType(ArrhythmiaReportModel.ReportType.Tachycardia);
                UUID savedReport = reportService.saveNewReport(reportEntity);
                saveSegmentPub.setTs(tachyOrBradyDTO.getTs());
                saveSegmentPub.setTotalSecondToSave(180);
                saveSegmentPub.setReportId(savedReport.toString());
                saveSegmentPub.setUserId(user.getId().toString());
                reportPublisherService.saveSegmentReport(saveSegmentPub);
                reportPublisherService.sendNotification("report", user, "Tachycardia");
            }
            case "Bradycardia" -> {
                reportEntity.setReportType(ArrhythmiaReportModel.ReportType.Bradycardia);
                UUID savedReport = reportService.saveNewReport(reportEntity);
                saveSegmentPub.setTs(tachyOrBradyDTO.getTs());
                saveSegmentPub.setTotalSecondToSave(180);
                saveSegmentPub.setReportId(savedReport.toString());
                saveSegmentPub.setUserId(user.getId().toString());
                reportPublisherService.saveSegmentReport(saveSegmentPub);
                reportPublisherService.sendNotification("report", user, "Bradycardia");
            }
            case "Asystole" -> {
                reportEntity.setReportType(ArrhythmiaReportModel.ReportType.Asystole);
                UUID savedReport = reportService.saveNewReport(reportEntity);
                saveSegmentPub.setTs(tachyOrBradyDTO.getTs());
                saveSegmentPub.setTotalSecondToSave(10);
                saveSegmentPub.setReportId(savedReport.toString());
                saveSegmentPub.setUserId(user.getId().toString());
                reportPublisherService.saveSegmentReport(saveSegmentPub);
                reportPublisherService.sendNotification("report", user, "Asystole");
            }
            default -> reportEntity.setReportType(ArrhythmiaReportModel.ReportType.Unknown);
        }




        return ResponseEntity.status(HttpStatus.OK).body("Diagnosis is saved");
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllReportByUser(@AuthenticationPrincipal User user,
                                                @RequestParam(name = "userId", required = true) String userId) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated.");
        }
        ArrhythmiaReportsListDTO reportListRes = new ArrhythmiaReportsListDTO();

        Optional<List<ArrhythmiaReportModel>> reportsOpt;
        List<ArrhythmiaReportModel> reports;
        reportsOpt = arrhythmiaReportRepo.getReportByUserId(UUID.fromString(userId));
        if (reportsOpt.isEmpty()) {
            reportListRes.setUserId(userId);
            reportListRes.setArrhythmiaReports(Collections.emptyList());
            return ResponseEntity.ok(reportListRes);
        } else {
            reports = reportsOpt.get();
        }
        List<ArrhythmiaReport> reportListEntity = reports.stream()
                .map(this::fromArrhythmiaEntityToDTO)
                .collect(Collectors.toList());
        reportListRes.setArrhythmiaReports(reportListEntity);
        reportListRes.setUserId(userId);

        return ResponseEntity.ok(reportListRes);
    }

    @GetMapping("/")
    public ResponseEntity<?> getReportById(@AuthenticationPrincipal User user,
                                                @RequestParam(name = "reportId", required = true) String reportId) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated.");
        }
        ArrhythmiaReportResDTO reportRes = new ArrhythmiaReportResDTO();

        Optional<ArrhythmiaReportModel> reportsOpt;
        ArrhythmiaReportModel report;
        reportsOpt = arrhythmiaReportRepo.getReportById(UUID.fromString(reportId));
        if (reportsOpt.isEmpty()) {
            reportRes.setIsEmpty(true);
            reportRes.setReport(null);
            return ResponseEntity.ok(reportRes);
        } else {
            report = reportsOpt.get();
        }
        ArrhythmiaReport reportResult = new ArrhythmiaReport();
        reportResult.setTimestamp(report.getTimestamp());
        reportResult.setEcgSegment(report.getSegment());
        reportResult.setReportType(fromReportType(report.getReportType()));
        reportResult.setReportId(report.getId().toString());
        reportRes.setReport(reportResult);
        reportRes.setIsEmpty(false);

        User userDataEntity = report.getUser();

        UserProfileResponse userData = new UserProfileResponse(userDataEntity.getId(), userDataEntity.getEmail(), userDataEntity.getName(), userDataEntity.getPhoneNumber(), userDataEntity.getRole().toString());
        reportRes.setUserData(userData);

        return ResponseEntity.ok(reportRes);
    }

    private ArrhythmiaReport fromArrhythmiaEntityToDTO(ArrhythmiaReportModel model) {
        ArrhythmiaReport data  = new ArrhythmiaReport();
        data.setReportType(fromReportType(model.getReportType()));
        data.setEcgSegment(model.getSegment());
        data.setTimestamp(model.getTimestamp());
        data.setReportId(model.getId().toString());

        return data;
    }


    private String fromReportType(ArrhythmiaReportModel.ReportType reportType) {
        if (reportType == ArrhythmiaReportModel.ReportType.SOS) {
            return "SOS";
        } else if (reportType == ArrhythmiaReportModel.ReportType.AFib) {
            return "Atrial Fibrillation";
        } else if (reportType == ArrhythmiaReportModel.ReportType.VT) {
            return "Ventricular Tachycardia";
        } else if (reportType == ArrhythmiaReportModel.ReportType.VFib) {
            return "Ventricular Fibrillation";
        } else if (reportType == ArrhythmiaReportModel.ReportType.Bradycardia) {
            return "Bradycardia";
        } else if (reportType == ArrhythmiaReportModel.ReportType.Tachycardia) {
            return "Tachycardia";
        } else if (reportType == ArrhythmiaReportModel.ReportType.Asystole) {
            return "Asystole";
        } else if (reportType == ArrhythmiaReportModel.ReportType.Normal) {
            return "Normal Rhythm";
        } else {
            return "Processing";
        }
    }
}

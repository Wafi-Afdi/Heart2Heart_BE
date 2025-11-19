package com.heart2heart.be_app.ArrhythmiaReport.service;

import com.heart2heart.be_app.ArrhythmiaReport.dto.ArrhythmiaReport;
import com.heart2heart.be_app.ArrhythmiaReport.dto.SaveSegmentDTO;
import com.heart2heart.be_app.ArrhythmiaReport.model.ArrhythmiaReportModel;
import com.heart2heart.be_app.ArrhythmiaReport.repository.ArrhythmiaReportRepo;
import com.heart2heart.be_app.auth.user.model.User;
import com.heart2heart.be_app.auth.user.repository.UserRepository;
import com.heart2heart.be_app.config.RabbitMQConfig;
import com.heart2heart.be_app.ecgextraction.service.EcgSignalsService;
import com.heart2heart.be_app.firebase.FirebaseService;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class ReportSubscriberService {
    private static final Logger log = LoggerFactory.getLogger(ReportSubscriberService.class);

    private final FirebaseService firebaseService;
    private final ReportService reportService;
    private final ArrhythmiaReportRepo arrhythmiaReportRepo;
    private final ClassifierEndpointService classifierEndpointService;
    private final ReportPublisherService reportPublisherService;
    private final EcgSignalsService ecgSignalsService;
    private final EntityManager entityManager;

    @Autowired
    public ReportSubscriberService(FirebaseService firebaseService, ReportService reportService, ArrhythmiaReportRepo arrhythmiaReportRepo, ClassifierEndpointService classifierEndpointService, ReportPublisherService reportPublisherService, EcgSignalsService ecgSignalsService, EntityManager entityManager) {
        this.firebaseService = firebaseService;
        this.reportService = reportService;
        this.arrhythmiaReportRepo = arrhythmiaReportRepo;
        this.classifierEndpointService = classifierEndpointService;
        this.reportPublisherService = reportPublisherService;
        this.ecgSignalsService = ecgSignalsService;
        this.entityManager = entityManager;
    }

    @RabbitListener(queues = RabbitMQConfig.REPORT_QUEUE_NAME)
    public void SubscribeReportQueue(UUID reportId) {
        try {
            Optional<ArrhythmiaReportModel> reportsOpt;
            ArrhythmiaReportModel report;
            reportsOpt = arrhythmiaReportRepo.getReportById(reportId);
            if (reportsOpt.isEmpty()) {
                log.error("Failed to find report: {}", reportId.toString());
                return;
            } else {
                report = reportsOpt.get();
            }

            ArrhythmiaReport reportRequestDTO = new ArrhythmiaReport();
            reportRequestDTO.setReportType(fromReportType(report.getReportType()));
            reportRequestDTO.setEcgSegment(report.getSegment());
            reportRequestDTO.setTimestamp(report.getTimestamp());

            // Call API
            // var classificationResult = classifierEndpointService.processArrhythmiaAnalysis(reportRequestDTO);

            // report.setReportType(fromString(classificationResult.diagnosis()));

            Thread.sleep(3000);

            report.setReportType(ArrhythmiaReportModel.ReportType.AFib);

            arrhythmiaReportRepo.save(report);

            // Call FCM
            firebaseService.sendReportNotification("report", report.getUser(), "Afib");

        } catch (Exception e) {
            log.error("Failed to save generate report: {}", reportId.toString());
        }
    }

    @RabbitListener(queues = RabbitMQConfig.SAVE_SEGMENT_QUEUE_NAME)
    public void subscribeSaveSegment(SaveSegmentDTO saveSegmentDTO) {
        try {
            Optional<ArrhythmiaReportModel> reportsOpt;
            ArrhythmiaReportModel report;
            reportsOpt = arrhythmiaReportRepo.getReportById(UUID.fromString(saveSegmentDTO.getReportId()));
            if (reportsOpt.isEmpty()) {
                log.error("Failed to find report: {}", saveSegmentDTO.getReportId());
                return;
            } else {
                report = reportsOpt.get();
            }

            User user = entityManager.getReference(User.class, UUID.fromString(saveSegmentDTO.getUserId()));

            report.setSegment(ecgSignalsService.getECGSegment(user, LocalDateTime.parse(saveSegmentDTO.getTs()), saveSegmentDTO.getTotalSecondToSave()));

            if (report.getReportType() != ArrhythmiaReportModel.ReportType.Asystole
                    && report.getReportType() != ArrhythmiaReportModel.ReportType.Bradycardia
                    && report.getReportType() != ArrhythmiaReportModel.ReportType.Tachycardia
            ) {
                reportPublisherService.PublishReportIdToBeClassified(UUID.fromString(saveSegmentDTO.getReportId()));
            }

            arrhythmiaReportRepo.save(report);

        } catch (Exception e) {
            log.error("Failed to save generate report: {}", saveSegmentDTO.getReportId());
        }
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

    private ArrhythmiaReportModel.ReportType fromString(String input) {
        return switch (input) {
            case "SOS" -> ArrhythmiaReportModel.ReportType.SOS;
            case "Atrial Fibrillation" -> ArrhythmiaReportModel.ReportType.AFib;
            case "Ventricular Tachycardia" -> ArrhythmiaReportModel.ReportType.VT;
            case "Ventricular Fibrillation" -> ArrhythmiaReportModel.ReportType.VFib;
            case "Bradycardia" -> ArrhythmiaReportModel.ReportType.Bradycardia;
            case "Tachycardia" -> ArrhythmiaReportModel.ReportType.Tachycardia;
            case "Asystole" -> ArrhythmiaReportModel.ReportType.Asystole;
            case "Normal Rhythm" -> ArrhythmiaReportModel.ReportType.Normal;
            default -> ArrhythmiaReportModel.ReportType.Unknown;
        };
    }
}

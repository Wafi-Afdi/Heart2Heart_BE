package com.heart2heart.be_app.ArrhythmiaReport.service;

import com.heart2heart.be_app.ArrhythmiaReport.dto.ArrhythmiaReport;
import com.heart2heart.be_app.ArrhythmiaReport.model.ArrhythmiaReportModel;
import com.heart2heart.be_app.ArrhythmiaReport.repository.ArrhythmiaReportRepo;
import com.heart2heart.be_app.auth.user.repository.UserRepository;
import com.heart2heart.be_app.config.RabbitMQConfig;
import com.heart2heart.be_app.firebase.FirebaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class ReportSubscriberService {
    private static final Logger log = LoggerFactory.getLogger(ReportSubscriberService.class);

    private final FirebaseService firebaseService;
    private final ReportService reportService;
    private final ArrhythmiaReportRepo arrhythmiaReportRepo;
    private final ClassifierEndpointService classifierEndpointService;

    @Autowired
    public ReportSubscriberService(FirebaseService firebaseService, ReportService reportService, ArrhythmiaReportRepo arrhythmiaReportRepo, ClassifierEndpointService classifierEndpointService) {
        this.firebaseService = firebaseService;
        this.reportService = reportService;
        this.arrhythmiaReportRepo = arrhythmiaReportRepo;
        this.classifierEndpointService = classifierEndpointService;
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
            var classificationResult = classifierEndpointService.processArrhythmiaAnalysis(reportRequestDTO);

            report.setReportType(fromString(classificationResult.diagnosis()));

            arrhythmiaReportRepo.save(report);

            // Call FCM
            firebaseService.sendReportNotification("/report", report.getUser(), classificationResult.diagnosis());

        } catch (Exception e) {
            log.error("Failed to save generate report: {}", reportId.toString());
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

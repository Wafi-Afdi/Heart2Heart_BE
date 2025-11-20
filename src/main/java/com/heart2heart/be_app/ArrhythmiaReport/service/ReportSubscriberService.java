package com.heart2heart.be_app.ArrhythmiaReport.service;

import com.heart2heart.be_app.ArrhythmiaReport.dto.*;
import com.heart2heart.be_app.ArrhythmiaReport.model.ArrhythmiaReportModel;
import com.heart2heart.be_app.ArrhythmiaReport.model.ECGSegment;
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
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

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
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public ReportSubscriberService(FirebaseService firebaseService, ReportService reportService, ArrhythmiaReportRepo arrhythmiaReportRepo, ClassifierEndpointService classifierEndpointService, ReportPublisherService reportPublisherService, EcgSignalsService ecgSignalsService, EntityManager entityManager, SimpMessagingTemplate messagingTemplate) {
        this.firebaseService = firebaseService;
        this.reportService = reportService;
        this.arrhythmiaReportRepo = arrhythmiaReportRepo;
        this.classifierEndpointService = classifierEndpointService;
        this.reportPublisherService = reportPublisherService;
        this.ecgSignalsService = ecgSignalsService;
        this.entityManager = entityManager;
        this.messagingTemplate = messagingTemplate;
    }

    @RabbitListener(queues = RabbitMQConfig.REPORT_QUEUE_NAME)
    public void SubscribeReportQueue(ReportRequestDTO reportDTO) {
        try {
            Optional<ArrhythmiaReportModel> reportsOpt;
            ArrhythmiaReportModel report;
            reportsOpt = arrhythmiaReportRepo.getReportById(UUID.fromString(reportDTO.getReportId()));
            if (reportsOpt.isEmpty()) {
                log.error("Failed to find report: {}", reportDTO.getReportId());
                return;
            } else {
                report = reportsOpt.get();
            }

            EcgListDTO reportRequestDTO = new EcgListDTO();
            reportRequestDTO.setEcgSignal(report.getSegment().getSignal());

            // Call API
            var classificationResult = classifierEndpointService.processArrhythmiaAnalysis(reportRequestDTO);

            report.setReportType(fromDiagnosis(classificationResult.result()));

            //Thread.sleep(3000);

            // report.setReportType(ArrhythmiaReportModel.ReportType.AFib);

            String notificationReport = "Normal Rhythm";
            if (report.getReportType() == ArrhythmiaReportModel.ReportType.Normal) {

            } else if (report.getReportType() == ArrhythmiaReportModel.ReportType.VT) {
                notificationReport = "Ventricular Tachycardia";
            } else if (report.getReportType() == ArrhythmiaReportModel.ReportType.VFib) {
                notificationReport = "Ventricular Fibrillation";
            } else if (report.getReportType() == ArrhythmiaReportModel.ReportType.AFib) {
                notificationReport = "Atrial Fibrillation";
            }

            String topic = "/topic/notification";
//            messagingTemplate.convertAndSend("/topic/notification", """
//                    {
//                    }
//                    """);

            arrhythmiaReportRepo.save(report);

            // Call FCM
            String result = firebaseService.sendReportNotification("report", report.getUser(), notificationReport);
            log.info("Firebase: {}", result);

        } catch (Exception e) {
            e.printStackTrace();
            log.error("Failed to save generate report: {}", reportDTO.getReportId());
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


            arrhythmiaReportRepo.save(report);

            if (report.getReportType() != ArrhythmiaReportModel.ReportType.Bradycardia
                    && report.getReportType() != ArrhythmiaReportModel.ReportType.Tachycardia
            ) {
                reportPublisherService.PublishReportIdToBeClassified(saveSegmentDTO.getReportId());
            }


        } catch (Exception e) {
            log.error("Failed to save generate report: {}", saveSegmentDTO.getReportId());
        }
    }

    @RabbitListener(queues = RabbitMQConfig.NOTIF_QUEUE_NAME)
    public void subscribeFirebaseNotif(FirebaseNotifReport firebaseNotifReport) {
        try {
            if (Objects.equals(firebaseNotifReport.getTopic(), "SOS")) {
                firebaseService.sendSOSNotificationToTopic2(firebaseNotifReport.getUsername(), firebaseNotifReport.getUserId());
            }
            else {
                firebaseService.sendReportNotification2(firebaseNotifReport.getUsername(), firebaseNotifReport.getUserId(), firebaseNotifReport.getReport());
            }
        } catch (Exception e) {
            log.error("Failed to save send notif: {}", firebaseNotifReport.getUserId());
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

    private ArrhythmiaReportModel.ReportType fromDiagnosis(String input) {
        return switch (input) {
            case "N" -> ArrhythmiaReportModel.ReportType.Normal;
            case "AFIB" -> ArrhythmiaReportModel.ReportType.AFib;
            case "VT" -> ArrhythmiaReportModel.ReportType.VT;
            case "VF" -> ArrhythmiaReportModel.ReportType.VFib;
            default -> ArrhythmiaReportModel.ReportType.Unknown;
        };
    }
}

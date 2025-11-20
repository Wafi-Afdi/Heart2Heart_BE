package com.heart2heart.be_app.ArrhythmiaReport.service;

import com.heart2heart.be_app.ArrhythmiaReport.dto.FirebaseNotifReport;
import com.heart2heart.be_app.ArrhythmiaReport.dto.ReportRequestDTO;
import com.heart2heart.be_app.ArrhythmiaReport.dto.SaveSegmentDTO;
import com.heart2heart.be_app.auth.user.model.User;
import com.heart2heart.be_app.config.RabbitMQConfig;
import com.heart2heart.be_app.ecgextraction.service.BpmPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ReportPublisherService {
    private static final Logger log = LoggerFactory.getLogger(ReportPublisherService.class);
    private final RabbitTemplate rabbitTemplate;

    public ReportPublisherService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void PublishReportIdToBeClassified(String id) {
        log.info("Publishing report id: {}", id);

        ReportRequestDTO reportRequestDTO = new ReportRequestDTO();
        reportRequestDTO.setReportId(id);

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.REPORT_EXCHANGE_NAME,
                RabbitMQConfig.REPORT_ROUTING_KEY,
                reportRequestDTO
        );
    }

    public void saveSegmentReport(SaveSegmentDTO saveSegmentDTO) {
        log.info("Saving segment id: {}", saveSegmentDTO.getReportId());

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.SAVE_SEGMENT_EXCHANGE_NAME,
                RabbitMQConfig.SAVE_SEGMENT_ROUTING_KEY,
                saveSegmentDTO
        );
    }

    public void sendNotification(String topic, User user, String report) {
        FirebaseNotifReport firebaseNotifReport = new FirebaseNotifReport();
        firebaseNotifReport.setReport(report);
        firebaseNotifReport.setTopic(topic);
        firebaseNotifReport.setUsername(user.getName());
        firebaseNotifReport.setUserId(user.getId().toString());

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.NOTIF_EXCHANGE_NAME,
                RabbitMQConfig.NOTIF_ROUTING_KEY,
                firebaseNotifReport
        );
    }
}

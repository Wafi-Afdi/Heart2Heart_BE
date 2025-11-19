package com.heart2heart.be_app.ArrhythmiaReport.service;

import com.heart2heart.be_app.ArrhythmiaReport.dto.SaveSegmentDTO;
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

    public void PublishReportIdToBeClassified(UUID id) {
        log.info("Publishing report id: {}", id);

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.REPORT_EXCHANGE_NAME,
                RabbitMQConfig.REPORT_ROUTING_KEY,
                id
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
}

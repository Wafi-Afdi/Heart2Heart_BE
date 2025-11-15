package com.heart2heart.be_app.ecgextraction.service;

import com.heart2heart.be_app.config.RabbitMQConfig;
import com.heart2heart.be_app.ecgextraction.dto.BPMUserDataDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BpmPublisher {
    private static final Logger log = LoggerFactory.getLogger(BpmPublisher.class);
    private final RabbitTemplate rabbitTemplate;

    @Autowired
    public BpmPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishBPMData(BPMUserDataDTO bpmUserDataDTO) {
        log.info("Publishing ECG data for user: {}", bpmUserDataDTO.getUserId());

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.BPM_EXCHANGE_NAME,
                RabbitMQConfig.BPM_ROUTING_KEY,
                bpmUserDataDTO
        );
    }
}

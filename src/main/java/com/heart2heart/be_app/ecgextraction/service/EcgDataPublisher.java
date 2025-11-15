package com.heart2heart.be_app.ecgextraction.service;

import com.heart2heart.be_app.config.RabbitMQConfig;
import com.heart2heart.be_app.ecgextraction.dto.ECGSignalsDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EcgDataPublisher {

    private static final Logger log = LoggerFactory.getLogger(EcgDataPublisher.class);
    private final RabbitTemplate rabbitTemplate;

    @Autowired
    public EcgDataPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishEcgSignals(ECGSignalsDTO signalsDTO) {
        log.info("Publishing ECG data for user: {}", signalsDTO.getUserId());

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.ECG_EXCHANGE_NAME,
                RabbitMQConfig.ECG_ROUTING_KEY,
                signalsDTO
        );
    }
}

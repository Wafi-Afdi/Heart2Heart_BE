package com.heart2heart.be_app.ecgextraction.service;

import com.heart2heart.be_app.auth.user.model.User;
import com.heart2heart.be_app.auth.user.repository.UserRepository;
import com.heart2heart.be_app.config.RabbitMQConfig;
import com.heart2heart.be_app.ecgextraction.dto.ECGSignalsDTO;
import com.heart2heart.be_app.ecgextraction.repository.ECGSignalRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class EcgSubscriber {
    private static final Logger log = LoggerFactory.getLogger(EcgSubscriber.class);

    private final ECGSignalRepository ecgSignalRepository;
    private final EcgSignalsService ecgSignalsService;
    private final UserRepository userRepository;

    @Autowired
    public EcgSubscriber(ECGSignalRepository ecgSignalRepository, EcgSignalsService ecgSignalsService, UserRepository userRepository) {
        this.ecgSignalRepository = ecgSignalRepository;
        this.ecgSignalsService = ecgSignalsService;
        this.userRepository = userRepository;
    }

    @RabbitListener(queues = RabbitMQConfig.ECG_QUEUE_NAME)
    public void handleEcgSignals(ECGSignalsDTO signalsDTO) {
        try{

            User user = userRepository.findById(UUID.fromString(signalsDTO.getUserId()))
                    .orElseThrow(() -> new RuntimeException("User not found: " + signalsDTO.getUserId()));
            ecgSignalsService.saveSignals(user, signalsDTO.getEcgData());
            log.info("Successfully saved {} ECG signal points for user: {}", signalsDTO.getEcgData().size(), signalsDTO.getUserId());
        } catch (Exception e) {
            log.error("Failed to save signals for user {}", signalsDTO.getUserId());
        }
    }

}

package com.heart2heart.be_app.ecgextraction.service;

import com.heart2heart.be_app.auth.user.model.User;
import com.heart2heart.be_app.auth.user.repository.UserRepository;
import com.heart2heart.be_app.config.RabbitMQConfig;
import com.heart2heart.be_app.ecgextraction.dto.BPMUserDataDTO;
import com.heart2heart.be_app.ecgextraction.repository.BPMDataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class BpmSubscriber {
    private static final Logger log = LoggerFactory.getLogger(BpmSubscriber.class);

    private final UserRepository userRepository;
    private final BPMDataRepository bpmDataRepository;
    private final BpmService bpmService;

    @Autowired
    public BpmSubscriber(UserRepository userRepository, BPMDataRepository bpmDataRepository, BpmService bpmService) {
        this.userRepository = userRepository;
        this.bpmDataRepository = bpmDataRepository;
        this.bpmService = bpmService;
    }

    @RabbitListener(queues = RabbitMQConfig.BPM_QUEUE_NAME)
    public void BpmListener(BPMUserDataDTO bpmUserDataDTO) {
        try{

            User user = userRepository.findById(UUID.fromString(bpmUserDataDTO.getUserId()))
                    .orElseThrow(() -> new RuntimeException("User not found: " + bpmUserDataDTO.getUserId()));
            bpmService.saveBPM(user, bpmUserDataDTO.getBpmDatas());
            log.info("Successfully saved {} BPM data points for user: {}", bpmUserDataDTO.getBpmDatas().size(), bpmUserDataDTO.getUserId());
        } catch (Exception e) {
            log.error("Failed to save signals for user {}", bpmUserDataDTO.getUserId());
        }
    }
}

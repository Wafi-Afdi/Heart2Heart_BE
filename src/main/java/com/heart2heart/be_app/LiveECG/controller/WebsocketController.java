package com.heart2heart.be_app.LiveECG.controller;

import com.heart2heart.be_app.LiveECG.dto.ArrhytmiaReportWsDTO;
import com.heart2heart.be_app.LiveECG.dto.LiveDataDTO;
import com.heart2heart.be_app.LiveECG.dto.LiveLocationDTO;
import com.heart2heart.be_app.LiveECG.dto.UserStatusDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
public class WebsocketController {
    private static final Logger log = LoggerFactory.getLogger(WebsocketController.class);

    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public WebsocketController(SimpMessagingTemplate messagingTemplate) {

        this.messagingTemplate = messagingTemplate;
    }

    public void broadcastStatus(String statusJson) {
        messagingTemplate.convertAndSend("/topic/status", statusJson);
    }

    public void receiveChartData(String point) {
        messagingTemplate.convertAndSend("/topic/chart", point);
    }

    // --- 3. Data B (messages) ---
    @MessageMapping("/message/send")
    public void receiveMessage(String msg) {
        log.info("MESSAGE " + msg);
        messagingTemplate.convertAndSend("/topic/messages", msg);
    }

    @MessageMapping("/ping")
    public void receivePing(String msg, Principal principal) {
        log.info("PING from [{}]: {}", principal.getName(), msg);
    }

    @MessageMapping("/liveData")
    public void receiveLiveData(LiveDataDTO payload, Principal principal) {
        log.info("LIVE DATA received from [{}]: {}", principal.getName(), payload.getEcgList().toString());
        messagingTemplate.convertAndSend("/topic/liveData", payload);
    }

    @MessageMapping("/user")
    public void receiveUser(UserStatusDTO payload, Principal principal) {
        log.info("Use status received from [{}]: {}", principal.getName(), payload.toString());
        messagingTemplate.convertAndSend("/topic/user", payload);
    }


    @MessageMapping("/location")
    public void receiveLocationData(LiveLocationDTO payload, Principal principal) {
        log.info("LOCATION received from [{}]: {}", principal.getName(), payload.toString());
        messagingTemplate.convertAndSend("/topic/location", payload);
    }

    @MessageMapping("/notification")
    public void receiveNotification(ArrhytmiaReportWsDTO payload, Principal principal) {
        log.warn("NOTIFICATION received from [{}]: {}", principal.getName(), payload.toString());
        messagingTemplate.convertAndSend("/topic/notification", payload);
    }
}

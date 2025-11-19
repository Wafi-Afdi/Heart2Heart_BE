package com.heart2heart.be_app.LiveECG.controller;

import com.heart2heart.be_app.ArrhythmiaReport.service.ReportSubscriberService;
import com.heart2heart.be_app.LiveECG.model.UserWebsocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebSocketListener {

    private static final Logger log = LoggerFactory.getLogger(WebSocketListener.class);

    // Stores SessionID -> Username
    private final Map<String, UserWebsocket> sessionUserMap = new ConcurrentHashMap<>();

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());

        // Get the user (Principal) we set in the interceptor
        java.security.Principal userPrincipal = accessor.getUser();


        String username = userPrincipal.getName();
        String sessionId = accessor.getSessionId();
        String name = null;

        Message<?> connectMessage = (Message<?>) accessor.getMessageHeaders().get("simpConnectMessage");

        if (connectMessage != null) {
            MessageHeaders connectHeaders = connectMessage.getHeaders();

            // Get the "simpSessionAttributes" map from the nested message's headers
            @SuppressWarnings("unchecked")
            Map<String, Object> sessionAttributes = (Map<String, Object>) connectHeaders.get("simpSessionAttributes");

            if (sessionAttributes != null) {
                name = (String) sessionAttributes.get("name");
            } else {
                log.warn("Could not find 'simpSessionAttributes' map in connect message headers.");
            }
        } else {
            log.warn("Could not find 'simpConnectMessage' in SessionConnectedEvent.");
        }

        if (userPrincipal == null) {
            log.error("User principal is null on connect. Interceptor may not be working.");
            return;
        }


        // Store the user
        sessionUserMap.put(sessionId, new UserWebsocket(username, name));

        log.info("User {} connected. Session: {}", username, sessionId);

        // Send to /topic/room as requested
        String statusPayload = String.format("{\"name\": \"%s\", \"email\": \"%s\", \"isDisconnecting\": \"false\"}", name, username);
        messagingTemplate.convertAndSend("/topic/status", statusPayload);
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();

        // Retrieve and remove the user from the map
        UserWebsocket userAttribut = sessionUserMap.remove(sessionId);

        if (userAttribut != null) {
            log.info("User {} disconnected. Session: {}", userAttribut.getName(), sessionId);

            // Send to /topic/room as requested
            String statusPayload = String.format("{\"name\": \"%s\", \"email\": \"%s\", \"isDisconnecting\": \"true\"}", userAttribut.getName(), userAttribut.getEmail());
            messagingTemplate.convertAndSend("/topic/status", statusPayload);
        } else {
            log.warn("Disconnect event for unknown session: {}", sessionId);
        }
    }
}

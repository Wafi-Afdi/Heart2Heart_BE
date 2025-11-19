package com.heart2heart.be_app.LiveECG.service;

import com.heart2heart.be_app.auth.user.service.JWTService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtChannelInterceptor implements ChannelInterceptor {

    private static final Logger log = LoggerFactory.getLogger(JwtChannelInterceptor.class);
    private final JWTService jwtService;

    @Autowired
    public JwtChannelInterceptor(JWTService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        // Check if it's a CONNECT frame
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            log.info("Intercepting STOMP CONNECT frame...");

            // Get the 'Authorization' header
            String authHeader = accessor.getFirstNativeHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7); // Remove "Bearer " prefix

                try {
                    String username = jwtService.extractUsername(token);
                    String name = jwtService.extractName(token);

                    if (username != null && jwtService.isTokenValid(token, username)) {
                        Authentication userAuth = new UsernamePasswordAuthenticationToken(
                                username, null, new ArrayList<>() // No authorities needed
                        );

                        accessor.setUser(userAuth);
                        Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
                        if (sessionAttributes != null) {
                            sessionAttributes.put("username", username);
                            sessionAttributes.put("name", name);
                        }
                        accessor.setHeader("Something", "Home");
                        log.info("Authenticated WebSocket user: {} {}", username, name);
                        return message;
                    } else {
                        log.warn("Invalid JWT token received.");
                        throw new MessageDeliveryException("Invalid JWT token");
                    }
                } catch (Exception e) {
                    log.error("Error validating JWT token: {}", e.getMessage());
                    throw new MessageDeliveryException("JWT validation error: " + e.getMessage());
                }
            } else {
                // No token or invalid header
                log.warn("Missing or invalid 'Authorization' header in CONNECT frame.");
                throw new MessageDeliveryException("Missing or invalid 'Authorization' header.");
            }
        }

        // For all other message types (SUBSCRIBE, SEND, etc.),
        // the accessor.getUser() will already be set if the connection was successful.
        return message;
    }
}

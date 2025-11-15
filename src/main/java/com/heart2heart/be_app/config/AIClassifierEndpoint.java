package com.heart2heart.be_app.config;

import com.heart2heart.be_app.ArrhythmiaReport.dto.ArrhythmiaReport;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class AIClassifierEndpoint {
    @Value("${app.python.url}")
    private String pythonEndpoint;

    @Bean
    public WebClient classifierWebClient(WebClient.Builder webClientBuilder) {
        return webClientBuilder
                // Define a common base URL for the external API
                .baseUrl(pythonEndpoint)
                // Set default headers, like authorization or content type
                .defaultHeader("Accept", "application/json")
                .defaultHeader("User-Agent", "Heart2Heart-Client/1.0")
                // Define connection and read timeouts
                .build();
    }
}

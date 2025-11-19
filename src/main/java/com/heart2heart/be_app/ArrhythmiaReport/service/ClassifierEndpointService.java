package com.heart2heart.be_app.ArrhythmiaReport.service;

import com.heart2heart.be_app.ArrhythmiaReport.dto.ArrhythmiaReport;
import com.heart2heart.be_app.ArrhythmiaReport.dto.EcgListDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

record ArrhythmiaAnalysisResponse(String result) {}

@Service
public class ClassifierEndpointService {
    private final WebClient classifierWebClient;

    public ClassifierEndpointService(WebClient classifierWebClient) {
        this.classifierWebClient = classifierWebClient;
    }


    public ArrhythmiaAnalysisResponse processArrhythmiaAnalysis(EcgListDTO report) {
        String resourcePath = "/generate-diagnosis";
        Mono<ArrhythmiaAnalysisResponse> monoResponse = classifierWebClient.post()
                .uri(resourcePath)
                .bodyValue(report)
                .retrieve()
                .bodyToMono(ArrhythmiaAnalysisResponse.class);

        return monoResponse.block();
    }
}

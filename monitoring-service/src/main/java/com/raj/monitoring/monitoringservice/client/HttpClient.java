package com.raj.monitoring.monitoringservice.client;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;

@Component
public class HttpClient {

    private final WebClient webClient;

    public HttpClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public HealthCheckResult checkHealth(String url, int timeoutSeconds) {
        Instant startTime = Instant.now();

        try {
            Integer statusCode = webClient.get()
                    .uri(url)
                    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                    .exchangeToMono(response -> Mono.just(response.statusCode().value()))
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .onErrorReturn(0)
                    .block();

            Instant endTime = Instant.now();
            long responseTime = Duration.between(startTime, endTime).toMillis();

            boolean isUp = statusCode != null && statusCode >= 200 && statusCode < 400;

            return HealthCheckResult.builder()
                    .success(isUp)
                    .statusCode(statusCode)
                    .responseTimeMs(responseTime)
                    .build();

        } catch (Exception e) {
            Instant endTime = Instant.now();
            long responseTime = Duration.between(startTime, endTime).toMillis();

            return HealthCheckResult.builder()
                    .success(false)
                    .statusCode(0)
                    .responseTimeMs(responseTime)
                    .errorMessage(e.getMessage())
                    .build();
        }
    }
}
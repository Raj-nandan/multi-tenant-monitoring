package com.raj.monitoring.monitoringservice.client;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HealthCheckResult {

    private boolean success;
    private Integer statusCode;
    private Long responseTimeMs;
    private String errorMessage;
}

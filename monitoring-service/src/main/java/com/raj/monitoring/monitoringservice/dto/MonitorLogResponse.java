package com.raj.monitoring.monitoringservice.dto;

import com.raj.monitoring.monitoringservice.entity.MonitorLog;
import com.raj.monitoring.monitoringservice.entity.MonitorStatus;

import java.time.Instant;

public class MonitorLogResponse {

    private String id;
    private String monitorId;
    private MonitorStatus status;
    private Long responseTimeMs;
    private Integer statusCode;
    private String errorMessage;
    private Instant createdAt;

    public static MonitorLogResponse fromEntity(MonitorLog log) {
        MonitorLogResponse response = new MonitorLogResponse();
        response.setId(log.getId());
        response.setMonitorId(log.getMonitor().getId());
        response.setStatus(log.getStatus());
        response.setResponseTimeMs(log.getResponseTimeMs());
        response.setStatusCode(log.getStatusCode());
        response.setErrorMessage(log.getErrorMessage());
        response.setCreatedAt(log.getCreatedAt());
        return response;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getMonitorId() { return monitorId; }
    public void setMonitorId(String monitorId) { this.monitorId = monitorId; }
    public MonitorStatus getStatus() { return status; }
    public void setStatus(MonitorStatus status) { this.status = status; }
    public Long getResponseTimeMs() { return responseTimeMs; }
    public void setResponseTimeMs(Long responseTimeMs) { this.responseTimeMs = responseTimeMs; }
    public Integer getStatusCode() { return statusCode; }
    public void setStatusCode(Integer statusCode) { this.statusCode = statusCode; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}

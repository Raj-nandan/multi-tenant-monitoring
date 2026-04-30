package com.raj.monitoring.monitoringservice.dto;

import com.raj.monitoring.monitoringservice.entity.Monitor;
import com.raj.monitoring.monitoringservice.entity.MonitorStatus;

import java.time.Instant;

public class MonitorResponse {

    private String id;
    private String tenantId;
    private String name;
    private String url;
    private Integer checkIntervalSeconds;
    private Integer timeoutSeconds;
    private MonitorStatus status;
    private Long lastResponseTimeMs;
    private Instant lastCheckedAt;
    private Instant nextCheckAt;
    private Instant createdAt;
    private Instant updatedAt;

    public static MonitorResponse fromEntity(Monitor monitor) {
        MonitorResponse response = new MonitorResponse();
        response.setId(monitor.getId());
        response.setTenantId(monitor.getTenantId());
        response.setName(monitor.getName());
        response.setUrl(monitor.getUrl());
        response.setCheckIntervalSeconds(monitor.getCheckIntervalSeconds());
        response.setTimeoutSeconds(monitor.getTimeoutSeconds());
        response.setStatus(monitor.getStatus());
        response.setLastResponseTimeMs(monitor.getLastResponseTimeMs());
        response.setLastCheckedAt(monitor.getLastCheckedAt());
        response.setNextCheckAt(monitor.getNextCheckAt());
        response.setCreatedAt(monitor.getCreatedAt());
        response.setUpdatedAt(monitor.getUpdatedAt());
        return response;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public Integer getCheckIntervalSeconds() { return checkIntervalSeconds; }
    public void setCheckIntervalSeconds(Integer checkIntervalSeconds) { this.checkIntervalSeconds = checkIntervalSeconds; }
    public Integer getTimeoutSeconds() { return timeoutSeconds; }
    public void setTimeoutSeconds(Integer timeoutSeconds) { this.timeoutSeconds = timeoutSeconds; }
    public MonitorStatus getStatus() { return status; }
    public void setStatus(MonitorStatus status) { this.status = status; }
    public Long getLastResponseTimeMs() { return lastResponseTimeMs; }
    public void setLastResponseTimeMs(Long lastResponseTimeMs) { this.lastResponseTimeMs = lastResponseTimeMs; }
    public Instant getLastCheckedAt() { return lastCheckedAt; }
    public void setLastCheckedAt(Instant lastCheckedAt) { this.lastCheckedAt = lastCheckedAt; }
    public Instant getNextCheckAt() { return nextCheckAt; }
    public void setNextCheckAt(Instant nextCheckAt) { this.nextCheckAt = nextCheckAt; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}

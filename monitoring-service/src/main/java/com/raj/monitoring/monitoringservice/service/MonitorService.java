package com.raj.monitoring.monitoringservice.service;

import com.raj.monitoring.monitoringservice.client.HealthCheckResult;
import com.raj.monitoring.monitoringservice.client.HttpClient;
import com.raj.monitoring.monitoringservice.dto.CreateMonitorRequest;
import com.raj.monitoring.monitoringservice.dto.MonitorLogResponse;
import com.raj.monitoring.monitoringservice.dto.MonitorResponse;
import com.raj.monitoring.monitoringservice.dto.UpdateMonitorRequest;
import com.raj.monitoring.monitoringservice.entity.Monitor;
import com.raj.monitoring.monitoringservice.entity.MonitorLog;
import com.raj.monitoring.monitoringservice.entity.MonitorStatus;
import com.raj.monitoring.monitoringservice.exception.ResourceAlreadyExistsException;
import com.raj.monitoring.monitoringservice.exception.ResourceNotFoundException;
import com.raj.monitoring.monitoringservice.repository.MonitorLogRepository;
import com.raj.monitoring.monitoringservice.repository.MonitorRepository;
import com.raj.monitoring.monitoringservice.util.TenantContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@Transactional
public class MonitorService {

    private final MonitorRepository monitorRepository;
    private final MonitorLogRepository monitorLogRepository;
    private final HttpClient httpClient;

    public MonitorService(MonitorRepository monitorRepository, MonitorLogRepository monitorLogRepository, HttpClient httpClient) {
        this.monitorRepository = monitorRepository;
        this.monitorLogRepository = monitorLogRepository;
        this.httpClient = httpClient;
    }

    public MonitorResponse createMonitor(CreateMonitorRequest request) {
        String tenantId = TenantContext.getTenantId();

        if (monitorRepository.existsByTenantIdAndUrl(tenantId, request.getUrl())) {
            throw new ResourceAlreadyExistsException("A monitor with this URL already exists for your tenant");
        }

        Monitor monitor = Monitor.builder()
                .tenantId(tenantId)
                .name(request.getName())
                .url(request.getUrl())
                .checkIntervalSeconds(request.getCheckIntervalSeconds())
                .timeoutSeconds(request.getTimeoutSeconds())
                .status(MonitorStatus.UNKNOWN)
                .nextCheckAt(Instant.now())
                .build();

        monitor = monitorRepository.save(monitor);
        return MonitorResponse.fromEntity(monitor);
    }

    @Transactional(readOnly = true)
    public List<MonitorResponse> getAllMonitors() {
        String tenantId = TenantContext.getTenantId();
        return monitorRepository.findByTenantId(tenantId).stream()
                .map(MonitorResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<MonitorResponse> getMonitorsPaginated(Pageable pageable) {
        String tenantId = TenantContext.getTenantId();
        Page<Monitor> monitors = monitorRepository.findAll(
                org.springframework.data.domain.PageRequest.of(pageable.getPageNumber(), pageable.getPageSize())
        );
        return monitors.map(MonitorResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public MonitorResponse getMonitor(String id) {
        String tenantId = TenantContext.getTenantId();
        Monitor monitor = monitorRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new ResourceNotFoundException("Monitor not found"));
        return MonitorResponse.fromEntity(monitor);
    }

    public MonitorResponse updateMonitor(String id, UpdateMonitorRequest request) {
        String tenantId = TenantContext.getTenantId();
        Monitor monitor = monitorRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new ResourceNotFoundException("Monitor not found"));

        if (request.getName() != null) {
            monitor.setName(request.getName());
        }
        if (request.getUrl() != null) {
            if (!monitor.getUrl().equals(request.getUrl()) &&
                monitorRepository.existsByTenantIdAndUrl(tenantId, request.getUrl())) {
                throw new ResourceAlreadyExistsException("A monitor with this URL already exists for your tenant");
            }
            monitor.setUrl(request.getUrl());
        }
        if (request.getCheckIntervalSeconds() != null) {
            monitor.setCheckIntervalSeconds(request.getCheckIntervalSeconds());
        }
        if (request.getTimeoutSeconds() != null) {
            monitor.setTimeoutSeconds(request.getTimeoutSeconds());
        }

        monitor = monitorRepository.save(monitor);
        return MonitorResponse.fromEntity(monitor);
    }

    public void deleteMonitor(String id) {
        String tenantId = TenantContext.getTenantId();
        Monitor monitor = monitorRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new ResourceNotFoundException("Monitor not found"));
        monitorRepository.delete(monitor);
    }

    @Transactional(readOnly = true)
    public Page<MonitorLogResponse> getMonitorLogs(String monitorId, Pageable pageable) {
        String tenantId = TenantContext.getTenantId();
        // Verify monitor belongs to tenant
        monitorRepository.findByTenantIdAndId(tenantId, monitorId)
                .orElseThrow(() -> new ResourceNotFoundException("Monitor not found"));
        return monitorLogRepository.findByTenantIdAndMonitorId(tenantId, monitorId, pageable)
                .map(MonitorLogResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public List<MonitorLogResponse> getRecentLogs(String monitorId) {
        String tenantId = TenantContext.getTenantId();
        monitorRepository.findByTenantIdAndId(tenantId, monitorId)
                .orElseThrow(() -> new ResourceNotFoundException("Monitor not found"));
        return monitorLogRepository.findTop10ByMonitorIdOrderByCreatedAtDesc(monitorId).stream()
                .map(MonitorLogResponse::fromEntity)
                .toList();
    }

    public void executeHealthCheck(Monitor monitor) {
        HealthCheckResult result = httpClient.checkHealth(monitor.getUrl(), monitor.getTimeoutSeconds());

        MonitorStatus newStatus = result.isSuccess() ? MonitorStatus.UP : MonitorStatus.DOWN;

        monitor.setStatus(newStatus);
        monitor.setLastResponseTimeMs(result.getResponseTimeMs());
        monitor.setLastCheckedAt(Instant.now());
        monitor.setNextCheckAt(Instant.now().plusSeconds(monitor.getCheckIntervalSeconds()));

        monitorRepository.save(monitor);

        MonitorLog log = MonitorLog.builder()
                .tenantId(monitor.getTenantId())
                .monitor(monitor)
                .status(newStatus)
                .responseTimeMs(result.getResponseTimeMs())
                .statusCode(result.getStatusCode())
                .errorMessage(result.getErrorMessage())
                .build();

        monitorLogRepository.save(log);
    }

    public List<Monitor> getDueMonitors() {
        return monitorRepository.findDueMonitors(Instant.now());
    }
}

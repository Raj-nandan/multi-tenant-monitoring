package com.raj.monitoring.monitoringservice.scheduler;

import com.raj.monitoring.monitoringservice.entity.Monitor;
import com.raj.monitoring.monitoringservice.service.MonitorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class HealthCheckScheduler {

    private static final Logger log = LoggerFactory.getLogger(HealthCheckScheduler.class);

    private final MonitorService monitorService;

    public HealthCheckScheduler(MonitorService monitorService) {
        this.monitorService = monitorService;
    }

    @Scheduled(fixedRate = 5000)
    public void performScheduledHealthChecks() {
        List<Monitor> dueMonitors = monitorService.getDueMonitors();

        if (dueMonitors.isEmpty()) {
            return;
        }

        log.info("Found {} monitors due for health check", dueMonitors.size());

        for (Monitor monitor : dueMonitors) {
            try {
                log.debug("Checking health of monitor: {} ({})", monitor.getName(), monitor.getUrl());
                monitorService.executeHealthCheck(monitor);
            } catch (Exception e) {
                log.error("Failed to check health for monitor {}: {}", monitor.getId(), e.getMessage());
            }
        }
    }
}

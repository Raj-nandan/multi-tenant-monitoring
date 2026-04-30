package com.raj.monitoring.monitoringservice.controller;

import com.raj.monitoring.monitoringservice.dto.ApiResponse;
import com.raj.monitoring.monitoringservice.dto.CreateMonitorRequest;
import com.raj.monitoring.monitoringservice.dto.MonitorLogResponse;
import com.raj.monitoring.monitoringservice.dto.MonitorResponse;
import com.raj.monitoring.monitoringservice.dto.UpdateMonitorRequest;
import com.raj.monitoring.monitoringservice.service.MonitorService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/monitors")
public class MonitorController {

    private final MonitorService monitorService;

    public MonitorController(MonitorService monitorService) {
        this.monitorService = monitorService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<MonitorResponse>> createMonitor(
            @Valid @RequestBody CreateMonitorRequest request) {
        MonitorResponse response = monitorService.createMonitor(request);
        return ResponseEntity.ok(ApiResponse.success("Monitor created successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<MonitorResponse>>> getAllMonitors() {
        List<MonitorResponse> monitors = monitorService.getAllMonitors();
        return ResponseEntity.ok(ApiResponse.success(monitors));
    }

    @GetMapping("/paginated")
    public ResponseEntity<ApiResponse<Page<MonitorResponse>>> getMonitorsPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<MonitorResponse> monitors = monitorService.getMonitorsPaginated(pageable);
        return ResponseEntity.ok(ApiResponse.success(monitors));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MonitorResponse>> getMonitor(@PathVariable String id) {
        MonitorResponse monitor = monitorService.getMonitor(id);
        return ResponseEntity.ok(ApiResponse.success(monitor));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MonitorResponse>> updateMonitor(
            @PathVariable String id,
            @Valid @RequestBody UpdateMonitorRequest request) {
        MonitorResponse response = monitorService.updateMonitor(id, request);
        return ResponseEntity.ok(ApiResponse.success("Monitor updated successfully", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMonitor(@PathVariable String id) {
        monitorService.deleteMonitor(id);
        return ResponseEntity.ok(ApiResponse.success("Monitor deleted successfully", null));
    }

    @GetMapping("/{id}/logs")
    public ResponseEntity<ApiResponse<List<MonitorLogResponse>>> getRecentLogs(@PathVariable String id) {
        List<MonitorLogResponse> logs = monitorService.getRecentLogs(id);
        return ResponseEntity.ok(ApiResponse.success(logs));
    }

    @GetMapping("/{id}/logs/paginated")
    public ResponseEntity<ApiResponse<Page<MonitorLogResponse>>> getMonitorLogs(
            @PathVariable String id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<MonitorLogResponse> logs = monitorService.getMonitorLogs(id, pageable);
        return ResponseEntity.ok(ApiResponse.success(logs));
    }
}

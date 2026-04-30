package com.raj.monitoring.monitoringservice.repository;

import com.raj.monitoring.monitoringservice.entity.MonitorLog;
import com.raj.monitoring.monitoringservice.entity.MonitorStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface MonitorLogRepository extends JpaRepository<MonitorLog, String> {

    Page<MonitorLog> findByTenantId(String tenantId, Pageable pageable);

    Page<MonitorLog> findByTenantIdAndMonitorId(String tenantId, String monitorId, Pageable pageable);

    List<MonitorLog> findTop10ByMonitorIdOrderByCreatedAtDesc(String monitorId);

    @Query("SELECT ml FROM MonitorLog ml WHERE ml.monitor.id = :monitorId AND ml.createdAt >= :from ORDER BY ml.createdAt DESC")
    List<MonitorLog> findByMonitorIdAndTimeRange(String monitorId, Instant from);

    long countByTenantIdAndStatus(String tenantId, MonitorStatus status);

    long countByMonitorIdAndStatus(String monitorId, MonitorStatus status);
}

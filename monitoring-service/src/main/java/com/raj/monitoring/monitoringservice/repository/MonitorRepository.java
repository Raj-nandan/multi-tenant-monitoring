package com.raj.monitoring.monitoringservice.repository;

import com.raj.monitoring.monitoringservice.entity.Monitor;
import com.raj.monitoring.monitoringservice.entity.MonitorStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface MonitorRepository extends JpaRepository<Monitor, String> {

    List<Monitor> findByTenantId(String tenantId);

    List<Monitor> findByTenantIdAndStatus(String tenantId, MonitorStatus status);

    Optional<Monitor> findByTenantIdAndId(String tenantId, String id);

    boolean existsByTenantIdAndUrl(String tenantId, String url);

    @Query("SELECT m FROM Monitor m WHERE m.nextCheckAt <= :now ORDER BY m.nextCheckAt ASC")
    List<Monitor> findDueMonitors(Instant now);

    @Query("SELECT m FROM Monitor m WHERE m.status = :status AND m.nextCheckAt <= :now")
    List<Monitor> findDueMonitorsByStatus(MonitorStatus status, Instant now);
}

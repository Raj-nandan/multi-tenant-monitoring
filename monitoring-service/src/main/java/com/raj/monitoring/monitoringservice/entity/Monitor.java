package com.raj.monitoring.monitoringservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Duration;
import java.time.Instant;

@Entity
@Table(name = "monitors", indexes = {
    @Index(name = "idx_monitor_tenant", columnList = "tenantId"),
    @Index(name = "idx_monitor_status", columnList = "status"),
    @Index(name = "idx_monitor_next_check", columnList = "nextCheckAt")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Monitor {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String tenantId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String url;

    @Column(nullable = false)
    private Integer checkIntervalSeconds;

    @Column(nullable = false)
    private Integer timeoutSeconds;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MonitorStatus status;

    private Long lastResponseTimeMs;

    private Instant lastCheckedAt;

    private Instant nextCheckAt;

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;
}

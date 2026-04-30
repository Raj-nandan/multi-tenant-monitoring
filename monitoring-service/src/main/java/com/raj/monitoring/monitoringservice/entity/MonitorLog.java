package com.raj.monitoring.monitoringservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "monitor_logs", indexes = {
    @Index(name = "idx_log_monitor", columnList = "monitorId"),
    @Index(name = "idx_log_tenant", columnList = "tenantId"),
    @Index(name = "idx_log_status", columnList = "status"),
    @Index(name = "idx_log_created", columnList = "createdAt")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonitorLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String tenantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "monitor_id", nullable = false)
    private Monitor monitor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MonitorStatus status;

    private Long responseTimeMs;

    private Integer statusCode;

    @Column(length = 2048)
    private String errorMessage;

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;
}

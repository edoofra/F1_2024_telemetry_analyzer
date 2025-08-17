package com.edoofra.f1_telemetry_analyzer.persistence.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;


@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "lap")
@Builder(toBuilder = true, setterPrefix = "with")
public class Lap {

    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "session_id", nullable = false)
    private UUID sessionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", insertable = false, updatable = false)
    private Session session;

    @Column(name = "lap_number")
    private Integer lapNumber;

    @Column(name = "lap_time_ms", nullable = false)
    private Integer lapTimeMs;

    @Column(name = "sector_1_time_ms")
    private Integer sector1TimeMs;

    @Column(name = "sector_2_time_ms")
    private Integer sector2TimeMs;

    @Column(name = "sector_3_time_ms")
    private Integer sector3TimeMs;

    @Column(name = "race_position")
    private Integer racePosition;

    @Column(name = "lap_distance_m")
    private Integer lapDistanceM;

    @Column(name = "total_distance_m")
    private Integer totalDistanceM;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
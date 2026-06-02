package com.edoofra.f1_telemetry_analyzer.persistence.domain;

import com.edoofra.f1_telemetry_analyzer.persistence.domain.enums.SessionTypeEnum;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "session")
@Builder(toBuilder = true, setterPrefix = "with")
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "game_session_id", unique = true)
    private String gameSessionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private SessionTypeEnum type;

    @Column(name = "track_name")
    private String trackName;

    @Column(name = "weather")
    private String weather;

    @Column(name = "track_temperature")
    private Integer trackTemperature;

    @Column(name = "air_temperature")
    private Integer airTemperature;

    @Column(name = "safety_car_status")
    private Integer safetyCarStatus;

    @Column(name = "num_safety_car_periods")
    private Integer numSafetyCarPeriods;

    @Column(name = "num_virtual_safety_car_periods")
    private Integer numVirtualSafetyCarPeriods;

    @Column(name = "num_red_flag_periods")
    private Integer numRedFlagPeriods;

    @Column(name = "game_version")
    private String gameVersion;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
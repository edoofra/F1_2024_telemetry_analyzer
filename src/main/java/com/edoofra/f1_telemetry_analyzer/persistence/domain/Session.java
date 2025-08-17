package com.edoofra.f1_telemetry_analyzer.persistence.domain;

import com.edoofra.f1_telemetry_analyzer.persistence.domain.enums.SessionTypeEnum;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.GenericGenerator;

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
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "game_session_id", unique = true)
    private String gameSessionId;

    @Column(name = "type")
    private SessionTypeEnum type;

    @Column(name = "track_name")
    private String trackName;

    @Column(name = "weather")
    private String weather;

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
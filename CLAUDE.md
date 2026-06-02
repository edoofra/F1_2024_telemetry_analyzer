# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run Commands

```bash
mvn clean package          # Build and package
mvn spring-boot:run        # Run locally (requires PostgreSQL)
mvn test                   # Run all tests
mvn test -Dtest=ClassName  # Run a single test class
mvn test -Dtest=ClassName#methodName  # Run a single test method
```

CI runs on GitHub Actions (`.github/workflows/maven.yml`) on push/PR to `main`.

## Architecture

The system ingests F1 2024 telemetry UDP packets (60 Hz, port 20777) and processes them through a pipeline:

```
F1 Game UDP (port 20777)
  → Spring Integration UnicastReceivingChannelAdapter
  → MessageChannel (DirectChannel sync | QueueChannel async, cap 1000)
  → UdpTelemetryHandler (@ServiceActivator)
  → TelemetryBufferManager → TelemetryRingBuffer<byte[]> (cap 10 000)
  → TelemetryProcessingService (background poll thread, 10 ms interval)
  → HeaderParsingService (little-endian binary parsing)
  → Session / Lap persistence (PostgreSQL via Spring Data JPA + Flyway)
```

### Key Design Decisions

- **Ring buffer over queue**: fixed capacity prevents unbounded memory growth; oldest packets are silently overwritten when full. Guarded by `ReentrantReadWriteLock` (multiple concurrent readers, single writer) and `AtomicInteger` counters for stats.
- **Sync vs async channel**: controlled by `f1.telemetry.processing.async`. `DirectChannel` processes on the UDP thread; `QueueChannel(1000)` decouples ingestion from processing.
- **Background poll thread**: `TelemetryProcessingService` polls the ring buffer every 10 ms, fully decoupling 60 Hz ingestion from heavier processing work.

### Module Layout

| Package | Responsibility |
|---|---|
| `buffer/` | `TelemetryRingBuffer<T>` — circular, thread-safe, fixed-capacity |
| `config/` | `UdpListenerConfig`, `TelemetryBufferConfig`, `TelemetryProcessingConfig` |
| `controller/` | REST endpoints under `/api/telemetry/` (stats, health, start/stop) |
| `model/` | `TelemetryPacketHeader`, `BufferStats` |
| `persistence/` | JPA entities (`Session`, `Lap`), repositories, Flyway migrations |
| `service/` | `UdpTelemetryHandler`, `TelemetryBufferManager`, `TelemetryProcessingService`, `HeaderParsingService` |
| `util/` | Binary/little-endian parsing helpers |

### Persistence

- **Flyway** manages schema: `V1.0_CREATE_SESSION_TABLE.sql`, `V1.1_CREATE_LAP_TIMES_TABLE.sql`.
- `Session` — UUID PK, unique `gameSessionId`, type/trackName/weather, open/close timestamps.
- `Lap` — UUID PK, FK to session, lap/sector times (ms), race position, distances (m).
- `Lap.session` uses `LAZY` fetch to avoid N+1 queries.

### Observability

- Micrometer counters/timers on `UdpTelemetryHandler`: `telemetry.packets.received`, `telemetry.packets.processed`, `telemetry.packets.errors`, `telemetry.packet.processing.time`.
- `GET /api/telemetry/health` returns `HEALTHY/DEGRADED/STOPPED/DISABLED` based on drop rate > 5 %.
- Prometheus scrape endpoint: `/actuator/prometheus`.

## Testing

- **`@IntegrationTest`** — composite annotation combining `@SpringBootTest`, `@Import(TestContainerConfig.class)`, `@ActiveProfiles("test")`.
- Integration tests spin up a real PostgreSQL 15 via TestContainers (reusable container for speed).
- Unit tests for ring buffer cover FIFO, overwrite, thread-safety (concurrent `ExecutorService`), and edge cases.

## Key Configuration Properties

```properties
f1.telemetry.udp.port=20777
f1.telemetry.udp.socket-buffer-size=8192
f1.telemetry.buffer.capacity=10000
f1.telemetry.buffer.stats-interval=10s
f1.telemetry.processing.enabled=true
f1.telemetry.processing.thread-count=1
f1.telemetry.processing.poll-interval-ms=10
f1.telemetry.processing.async=false   # true → QueueChannel, false → DirectChannel
f1.telemetry.processing.enable-metrics=true
```

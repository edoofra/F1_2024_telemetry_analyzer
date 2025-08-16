# F1_2024_telemetry_analyzer

The F1 2024 PS5 Telemetry Visualizer is a real-time data analysis application that captures, processes, and visualizes telemetry data from the F1 2024 PlayStation 5 game. The system receives UDP telemetry streams at 60Hz, processes the data in near real-time, persists session data to a database, and provides interactive visualizations through a web interface. The application enables F1 players to analyze their racing performance through comprehensive data insights and exportable reports.

The architecture follows a microservices pattern with clear separation of concerns: UDP data ingestion, real-time processing, persistence, and web presentation layers.

## Architecture

### High-Level Architecture

```mermaid
graph TB
    PS5[F1 2024 PS5 Game] -->|UDP 60Hz| UDP_LISTENER[UDP Listener Service]
    UDP_LISTENER --> RING_BUFFER[In-Memory Ring Buffer]
    RING_BUFFER --> SESSION_SERVICE[Session Processing Service]
    SESSION_SERVICE --> POSTGRES[(PostgreSQL Database)]
    
    WEB_CLIENT[React Frontend] -->|HTTP/WebSocket| REST_API[Spring Boot REST API]
    REST_API --> SESSION_SERVICE
    REST_API --> POSTGRES
    
    RING_BUFFER -->|Real-time Events| WEBSOCKET[WebSocket Handler]
    WEBSOCKET --> WEB_CLIENT
```

### Component Architecture

The system is organized into the following main components:

1. **UDP Telemetry Ingestion Layer**
   - UDP Listener Service
   - Packet Parser and Validator
   - Ring Buffer Manager

2. **Data Processing Layer**
   - Session Management Service
   - Real-time Event Processor
   - Data Transformation Service

3. **Persistence Layer**
   - PostgreSQL Database
   - Repository Pattern Implementation
   - Transaction Management

4. **API Layer**
   - REST Controllers
   - WebSocket Handlers
   - Data Export Services

5. **Frontend Layer**
   - React Application
   - Real-time Chart Components
   - Session Management UI

package com.edoofra.f1_telemetry_analyzer.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * TestContainer configuration for running integration tests with PostgreSQL database.
 * This configuration provides a containerized PostgreSQL instance for testing,
 * eliminating the need for an external database during test execution.
 */
@TestConfiguration(proxyBeanMethods = false)
public class TestContainerConfig {
    
    /**
     * Creates a PostgreSQL TestContainer that can be shared across multiple tests.
     * The container is configured with the same PostgreSQL version used in production
     * and automatically provides connection details to Spring Boot.
     * 
     * @return PostgreSQL container configured for F1 telemetry testing
     */
    @Bean
    @ServiceConnection
    PostgreSQLContainer<?> postgresContainer() {
        return new PostgreSQLContainer<>(DockerImageName.parse("postgres:15-alpine"))
                .withDatabaseName("f1_telemetry_test")
                .withUsername("testuser")
                .withPassword("testpass")
                .withReuse(true); // Reuse container across test runs for better performance
    }
}
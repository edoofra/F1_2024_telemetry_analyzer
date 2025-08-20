package com.edoofra.f1_telemetry_analyzer.annotation;

import com.edoofra.f1_telemetry_analyzer.config.TestContainerConfig;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Composite annotation for integration tests that require a full Spring context
 * with TestContainers for database testing.
 * 
 * This annotation combines:
 * - @SpringBootTest for full application context
 * - @Import(TestContainerConfig.class) for PostgreSQL TestContainer
 * - @ActiveProfiles("test") for test-specific configuration
 * 
 * Usage:
 * @IntegrationTest
 * class MyIntegrationTest {
 *     // Test methods that require database access
 * }
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest
@Import(TestContainerConfig.class)
@ActiveProfiles("test")
public @interface IntegrationTest {
}
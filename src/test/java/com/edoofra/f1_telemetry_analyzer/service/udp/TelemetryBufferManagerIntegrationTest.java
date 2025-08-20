package com.edoofra.f1_telemetry_analyzer.service.udp;

import com.edoofra.f1_telemetry_analyzer.annotation.IntegrationTest;
import com.edoofra.f1_telemetry_analyzer.model.BufferStats;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for TelemetryBufferManager using TestContainers.
 * These tests verify the buffer manager works correctly within a full Spring context
 * with a real PostgreSQL database provided by TestContainers.
 */
@IntegrationTest
@TestPropertySource(properties = {
    "f1.telemetry.buffer.capacity=50",
    "f1.telemetry.buffer.stats-interval=1"
})
class TelemetryBufferManagerIntegrationTest {

    @Autowired
    private TelemetryBufferManager bufferManager;

    @BeforeEach
    void setUp() {
        // Clear buffer state before each test to ensure test isolation
        bufferManager.clearBuffer();
        
        // Wait a brief moment for any background operations to complete
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Test
    @DisplayName("Should initialize buffer manager with correct configuration")
    void shouldInitializeBufferManagerWithCorrectConfiguration() {
        // Assert that the buffer manager is properly configured
        assertNotNull(bufferManager, "Buffer manager should be autowired");
        
        BufferStats stats = bufferManager.getBufferStats();
        assertEquals(50, stats.capacity(), "Buffer capacity should match test configuration");
        assertEquals(0, stats.currentSize(), "Initial size should be 0");
        assertTrue(stats.isEmpty(), "Buffer should initially be empty");
    }

    @Test
    @DisplayName("Should handle packet processing in Spring context")
    void shouldHandlePacketProcessingInSpringContext() {
        // Arrange
        byte[] testPacket = {0x01, 0x02, 0x03, 0x04};

        // Act
        boolean success = bufferManager.addTelemetryPacket(testPacket);
        
        // Assert
        assertTrue(success, "Should successfully add packet");
        
        BufferStats stats = bufferManager.getBufferStats();
        assertEquals(1, stats.currentSize(), "Buffer size should increase");
        assertEquals(1, stats.totalReceived(), "Total received count should increase");
        assertFalse(stats.isEmpty(), "Buffer should no longer be empty");
    }

    @Test
    @DisplayName("Should handle buffer overflow correctly")
    void shouldHandleBufferOverflowCorrectly() {
        // Arrange - fill buffer to capacity
        byte[] testPacket = {0x01, 0x02, 0x03, 0x04};
        
        // Fill the buffer (capacity is 50)
        for (int i = 0; i < 50; i++) {
            bufferManager.addTelemetryPacket(testPacket);
        }
        
        // Act - add one more packet to trigger overflow
        boolean success = bufferManager.addTelemetryPacket(testPacket);
        
        // Assert
        assertTrue(success, "Should still succeed even when buffer is full");
        
        BufferStats stats = bufferManager.getBufferStats();
        assertEquals(50, stats.currentSize(), "Buffer size should remain at capacity");
        assertEquals(51, stats.totalReceived(), "Total received should include overflow packet");
        assertEquals(1, stats.totalDropped(), "Should track one dropped packet");
        assertTrue(stats.dropRate() > 0, "Drop rate should be greater than 0");
    }

    @Test
    @DisplayName("Should retrieve packets in FIFO order")
    void shouldRetrievePacketsInFIFOOrder() {
        // Arrange
        byte[] packet1 = {0x01};
        byte[] packet2 = {0x02};
        byte[] packet3 = {0x03};
        
        // Act
        bufferManager.addTelemetryPacket(packet1);
        bufferManager.addTelemetryPacket(packet2);
        bufferManager.addTelemetryPacket(packet3);
        
        // Assert
        byte[] retrieved1 = bufferManager.getNextTelemetryPacket();
        byte[] retrieved2 = bufferManager.getNextTelemetryPacket();
        byte[] retrieved3 = bufferManager.getNextTelemetryPacket();
        
        assertArrayEquals(packet1, retrieved1, "First packet should be retrieved first");
        assertArrayEquals(packet2, retrieved2, "Second packet should be retrieved second");
        assertArrayEquals(packet3, retrieved3, "Third packet should be retrieved third");
        
        BufferStats stats = bufferManager.getBufferStats();
        assertTrue(stats.isEmpty(), "Buffer should be empty after retrieving all packets");
    }

    @Test
    @DisplayName("Should handle null packet gracefully")
    void shouldHandleNullPacketGracefully() {
        // Act
        boolean success = bufferManager.addTelemetryPacket(null);
        
        // Assert
        assertFalse(success, "Should return false for null packet");
        
        BufferStats stats = bufferManager.getBufferStats();
        assertEquals(0, stats.currentSize(), "Buffer size should remain 0");
        assertEquals(0, stats.totalReceived(), "Total received should remain 0");
    }

    @Test
    @DisplayName("Should calculate buffer utilization correctly")
    void shouldCalculateBufferUtilizationCorrectly() {
        // Arrange
        byte[] testPacket = {0x01, 0x02, 0x03, 0x04};
        
        // Act - fill buffer to 60% capacity (30 out of 50)
        for (int i = 0; i < 30; i++) {
            bufferManager.addTelemetryPacket(testPacket);
        }
        
        // Assert
        BufferStats stats = bufferManager.getBufferStats();
        assertEquals(60.0, stats.utilizationPercentage(), 0.1, 
            "Utilization should be 60%");
        assertEquals(20, stats.availableCapacity(), 
            "Available capacity should be 20");
    }
}
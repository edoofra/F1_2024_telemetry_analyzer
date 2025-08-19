package com.edoofra.f1_telemetry_analyzer.service.udp;

import com.edoofra.f1_telemetry_analyzer.service.parsing.HeaderParsingService;
import com.edoofra.f1_telemetry_analyzer.service.udp.TelemetryBufferManager;
import com.edoofra.f1_telemetry_analyzer.service.udp.UdpTelemetryHandler;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UdpTelemetryHandlerTest {

    @Mock
    private HeaderParsingService headerParsingService;
    
    @Mock
    private TelemetryBufferManager telemetryBufferManager;
    
    private MeterRegistry meterRegistry;
    private UdpTelemetryHandler handler;
    
    private static final byte[] SAMPLE_PACKET = {0x01, 0x02, 0x03, 0x04};
    
    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        handler = new UdpTelemetryHandler(headerParsingService, telemetryBufferManager, meterRegistry);
    }

    @Nested
    @DisplayName("Constructor and Initialization")
    class ConstructorAndInitialization {

        @Test
        @DisplayName("Should initialize all required metrics")
        void shouldInitializeAllMetrics() {
            // Verify timer is registered
            Timer processingTimer = meterRegistry.find("telemetry.packet.processing.time").timer();
            assertNotNull(processingTimer, "Processing timer should be registered");

            // Verify counters are registered
            Counter receivedCounter = meterRegistry.find("telemetry.packets.received").counter();
            Counter processedCounter = meterRegistry.find("telemetry.packets.processed").counter();
            Counter errorCounter = meterRegistry.find("telemetry.packets.errors").counter();
            
            assertNotNull(receivedCounter, "Received counter should be registered");
            assertNotNull(processedCounter, "Processed counter should be registered");
            assertNotNull(errorCounter, "Error counter should be registered");
        }

        @Test
        @DisplayName("Should have proper metric descriptions")
        void shouldHaveProperMetricDescriptions() {
            Timer processingTimer = meterRegistry.find("telemetry.packet.processing.time").timer();
            assertEquals("Time taken to process each telemetry packet", processingTimer.getId().getDescription());

            Counter receivedCounter = meterRegistry.find("telemetry.packets.received").counter();
            assertEquals("Total number of telemetry packets received", receivedCounter.getId().getDescription());
        }
    }

    @Nested
    @DisplayName("Packet Processing")
    class PacketProcessing {

        @Test
        @DisplayName("Should process valid packet successfully")
        void shouldProcessValidPacketSuccessfully() {
            // Arrange
            Message<byte[]> message = MessageBuilder.withPayload(SAMPLE_PACKET).build();
            when(telemetryBufferManager.addTelemetryPacket(SAMPLE_PACKET)).thenReturn(true);

            // Act
            handler.handleTelemetryData(message);

            // Assert
            verify(telemetryBufferManager).addTelemetryPacket(SAMPLE_PACKET);
            
            // Verify metrics
            Counter receivedCounter = meterRegistry.find("telemetry.packets.received").counter();
            Counter processedCounter = meterRegistry.find("telemetry.packets.processed").counter();
            
            assertEquals(1.0, receivedCounter.count(), "Should increment received counter");
            assertEquals(1.0, processedCounter.count(), "Should increment processed counter");
        }

        @Test
        @DisplayName("Should handle buffer manager returning false")
        void shouldHandleBufferManagerReturningFalse() {
            // Arrange
            Message<byte[]> message = MessageBuilder.withPayload(SAMPLE_PACKET).build();
            when(telemetryBufferManager.addTelemetryPacket(SAMPLE_PACKET)).thenReturn(false);

            // Act
            handler.handleTelemetryData(message);

            // Assert
            verify(telemetryBufferManager).addTelemetryPacket(SAMPLE_PACKET);
            
            // Verify metrics
            Counter receivedCounter = meterRegistry.find("telemetry.packets.received").counter();
            Counter errorCounter = meterRegistry.find("telemetry.packets.errors").counter();
            Counter processedCounter = meterRegistry.find("telemetry.packets.processed").counter();
            
            assertEquals(1.0, receivedCounter.count(), "Should increment received counter");
            assertEquals(1.0, errorCounter.count(), "Should increment error counter");
            assertEquals(0.0, processedCounter.count(), "Should not increment processed counter");
        }

        @Test
        @DisplayName("Should handle buffer manager throwing exception")
        void shouldHandleBufferManagerThrowingException() {
            // Arrange
            Message<byte[]> message = MessageBuilder.withPayload(SAMPLE_PACKET).build();
            when(telemetryBufferManager.addTelemetryPacket(SAMPLE_PACKET))
                .thenThrow(new RuntimeException("Buffer error"));

            // Act & Assert - should not throw exception
            assertDoesNotThrow(() -> handler.handleTelemetryData(message));
            
            // Verify metrics
            Counter receivedCounter = meterRegistry.find("telemetry.packets.received").counter();
            Counter errorCounter = meterRegistry.find("telemetry.packets.errors").counter();
            Counter processedCounter = meterRegistry.find("telemetry.packets.processed").counter();
            
            assertEquals(1.0, receivedCounter.count(), "Should increment received counter");
            assertEquals(1.0, errorCounter.count(), "Should increment error counter");
            assertEquals(0.0, processedCounter.count(), "Should not increment processed counter");
        }

        @Test
        @DisplayName("Should handle empty packet")
        void shouldHandleEmptyPacket() {
            // Arrange
            byte[] emptyPacket = new byte[0];
            Message<byte[]> message = MessageBuilder.withPayload(emptyPacket).build();
            when(telemetryBufferManager.addTelemetryPacket(emptyPacket)).thenReturn(true);

            // Act
            handler.handleTelemetryData(message);

            // Assert
            verify(telemetryBufferManager).addTelemetryPacket(emptyPacket);
            
            Counter receivedCounter = meterRegistry.find("telemetry.packets.received").counter();
            assertEquals(1.0, receivedCounter.count(), "Should still process empty packets");
        }
    }

    @Nested
    @DisplayName("Metrics Tracking")
    class MetricsTracking {

        @Test
        @DisplayName("Should track processing time")
        void shouldTrackProcessingTime() {
            // Arrange
            Message<byte[]> message = MessageBuilder.withPayload(SAMPLE_PACKET).build();
            when(telemetryBufferManager.addTelemetryPacket(SAMPLE_PACKET)).thenReturn(true);

            // Act
            handler.handleTelemetryData(message);

            // Assert
            Timer processingTimer = meterRegistry.find("telemetry.packet.processing.time").timer();
            assertEquals(1, processingTimer.count(), "Should record one timing sample");
            assertTrue(processingTimer.totalTime(TimeUnit.NANOSECONDS) > 0, "Should record positive processing time");
        }

        @Test
        @DisplayName("Should track multiple packets")
        void shouldTrackMultiplePackets() {
            // Arrange
            Message<byte[]> message = MessageBuilder.withPayload(SAMPLE_PACKET).build();
            when(telemetryBufferManager.addTelemetryPacket(SAMPLE_PACKET)).thenReturn(true);

            // Act
            handler.handleTelemetryData(message);
            handler.handleTelemetryData(message);
            handler.handleTelemetryData(message);

            // Assert
            Counter receivedCounter = meterRegistry.find("telemetry.packets.received").counter();
            Counter processedCounter = meterRegistry.find("telemetry.packets.processed").counter();
            Timer processingTimer = meterRegistry.find("telemetry.packet.processing.time").timer();
            
            assertEquals(3.0, receivedCounter.count(), "Should track all received packets");
            assertEquals(3.0, processedCounter.count(), "Should track all processed packets");
            assertEquals(3, processingTimer.count(), "Should record timing for all packets");
        }

        @Test
        @DisplayName("Should track mixed success and failure scenarios")
        void shouldTrackMixedSuccessAndFailureScenarios() {
            // Arrange
            Message<byte[]> message = MessageBuilder.withPayload(SAMPLE_PACKET).build();
            when(telemetryBufferManager.addTelemetryPacket(SAMPLE_PACKET))
                .thenReturn(true)  // First call succeeds
                .thenReturn(false) // Second call fails
                .thenThrow(new RuntimeException("Error")); // Third call throws

            // Act
            handler.handleTelemetryData(message); // Success
            handler.handleTelemetryData(message); // Failure (returns false)
            handler.handleTelemetryData(message); // Exception

            // Assert
            Counter receivedCounter = meterRegistry.find("telemetry.packets.received").counter();
            Counter processedCounter = meterRegistry.find("telemetry.packets.processed").counter();
            Counter errorCounter = meterRegistry.find("telemetry.packets.errors").counter();
            
            assertEquals(3.0, receivedCounter.count(), "Should track all received packets");
            assertEquals(1.0, processedCounter.count(), "Should track only successful packets");
            assertEquals(2.0, errorCounter.count(), "Should track both failure types");
        }
    }

    @Nested
    @DisplayName("Integration Scenarios")
    class IntegrationScenarios {

        @Test
        @DisplayName("Should handle high frequency packet processing")
        void shouldHandleHighFrequencyPacketProcessing() {
            // Arrange - simulate F1 60Hz telemetry
            Message<byte[]> message = MessageBuilder.withPayload(SAMPLE_PACKET).build();
            when(telemetryBufferManager.addTelemetryPacket(SAMPLE_PACKET)).thenReturn(true);

            // Act - process 60 packets (simulating 1 second of telemetry)
            for (int i = 0; i < 60; i++) {
                handler.handleTelemetryData(message);
            }

            // Assert
            Counter receivedCounter = meterRegistry.find("telemetry.packets.received").counter();
            Counter processedCounter = meterRegistry.find("telemetry.packets.processed").counter();
            Timer processingTimer = meterRegistry.find("telemetry.packet.processing.time").timer();
            
            assertEquals(60.0, receivedCounter.count());
            assertEquals(60.0, processedCounter.count());
            assertEquals(60, processingTimer.count());
            
            // Verify buffer manager was called for each packet
            verify(telemetryBufferManager, times(60)).addTelemetryPacket(SAMPLE_PACKET);
        }

        @Test
        @DisplayName("Should maintain metrics consistency under concurrent access")
        void shouldMaintainMetricsConsistencyUnderConcurrentAccess() throws InterruptedException {
            // Arrange
            Message<byte[]> message = MessageBuilder.withPayload(SAMPLE_PACKET).build();
            when(telemetryBufferManager.addTelemetryPacket(SAMPLE_PACKET)).thenReturn(true);

            // Act - simulate concurrent packet processing
            Thread[] threads = new Thread[10];
            for (int i = 0; i < 10; i++) {
                threads[i] = new Thread(() -> {
                    for (int j = 0; j < 10; j++) {
                        handler.handleTelemetryData(message);
                    }
                });
                threads[i].start();
            }

            // Wait for all threads to complete
            for (Thread thread : threads) {
                thread.join();
            }

            // Assert
            Counter receivedCounter = meterRegistry.find("telemetry.packets.received").counter();
            Counter processedCounter = meterRegistry.find("telemetry.packets.processed").counter();
            
            assertEquals(100.0, receivedCounter.count(), "Should handle concurrent metrics updates");
            assertEquals(100.0, processedCounter.count(), "Should handle concurrent metrics updates");
        }
    }
}
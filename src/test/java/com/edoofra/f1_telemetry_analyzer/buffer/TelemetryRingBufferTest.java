package com.edoofra.f1_telemetry_analyzer.buffer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class TelemetryRingBufferTest {

    private TelemetryRingBuffer<String> buffer;
    private static final int DEFAULT_CAPACITY = 5;

    @BeforeEach
    void setUp() {
        buffer = new TelemetryRingBuffer<>(DEFAULT_CAPACITY);
    }

    @Nested
    @DisplayName("Basic Operations")
    class BasicOperations {

        @Test
        @DisplayName("Should initialize with correct capacity and empty state")
        void shouldInitializeCorrectly() {
            assertEquals(DEFAULT_CAPACITY, buffer.capacity());
            assertEquals(0, buffer.size());
            assertTrue(buffer.isEmpty());
            assertFalse(buffer.isFull());
        }

        @Test
        @DisplayName("Should add single item successfully")
        void shouldAddSingleItem() {
            assertTrue(buffer.put("item1"));
            assertEquals(1, buffer.size());
            assertFalse(buffer.isEmpty());
            assertFalse(buffer.isFull());
        }

        @Test
        @DisplayName("Should retrieve single item successfully")
        void shouldRetrieveSingleItem() {
            buffer.put("item1");
            assertEquals("item1", buffer.get());
            assertEquals(0, buffer.size());
            assertTrue(buffer.isEmpty());
        }

        @Test
        @DisplayName("Should peek item without removing it")
        void shouldPeekItem() {
            buffer.put("item1");
            assertEquals("item1", buffer.peek());
            assertEquals(1, buffer.size());
            assertEquals("item1", buffer.peek());
        }

        @Test
        @DisplayName("Should return null when getting from empty buffer")
        void shouldReturnNullWhenEmpty() {
            assertNull(buffer.get());
            assertNull(buffer.peek());
        }
    }

    @Nested
    @DisplayName("Buffer Full Scenarios")
    class BufferFullScenarios {

        @Test
        @DisplayName("Should fill buffer to capacity")
        void shouldFillToCapacity() {
            for (int i = 0; i < DEFAULT_CAPACITY; i++) {
                assertTrue(buffer.put("item" + i));
            }
            assertEquals(DEFAULT_CAPACITY, buffer.size());
            assertTrue(buffer.isFull());
        }

        @Test
        @DisplayName("Should overwrite oldest item when buffer is full")
        void shouldOverwriteOldestItem() {
            for (int i = 0; i < DEFAULT_CAPACITY; i++) {
                buffer.put("item" + i);
            }
            
            assertTrue(buffer.put("newItem"));
            assertEquals(DEFAULT_CAPACITY, buffer.size());
            assertTrue(buffer.isFull());

            // The oldest item "item0" should be overwritten
            assertEquals("item1", buffer.get());
        }

        @Test
        @DisplayName("Should maintain FIFO order during overwrite")
        void shouldMaintainFIFODuringOverwrite() {
            for (int i = 0; i < DEFAULT_CAPACITY + 2; i++) {
                buffer.put("item" + i);
            }
            
            assertEquals("item2", buffer.get());
            assertEquals("item3", buffer.get());
            assertEquals("item4", buffer.get());
            assertEquals("item5", buffer.get());
            assertEquals("item6", buffer.get());
            assertNull(buffer.get());
        }
    }

    @Nested
    @DisplayName("FIFO Behavior")
    class FIFOBehavior {

        @Test
        @DisplayName("Should maintain FIFO order")
        void shouldMaintainFIFOOrder() {
            buffer.put("first");
            buffer.put("second");
            buffer.put("third");
            
            assertEquals("first", buffer.get());
            assertEquals("second", buffer.get());
            assertEquals("third", buffer.get());
        }

        @Test
        @DisplayName("Should maintain order with mixed operations")
        void shouldMaintainOrderWithMixedOps() {
            buffer.put("item1");
            buffer.put("item2");
            assertEquals("item1", buffer.get());
            
            buffer.put("item3");
            buffer.put("item4");
            assertEquals("item2", buffer.get());
            assertEquals("item3", buffer.get());
            assertEquals("item4", buffer.get());
        }
    }

    @Nested
    @DisplayName("Clear Operation")
    class ClearOperation {

        @Test
        @DisplayName("Should clear empty buffer")
        void shouldClearEmptyBuffer() {
            buffer.clear();
            assertTrue(buffer.isEmpty());
            assertEquals(0, buffer.size());
        }

        @Test
        @DisplayName("Should clear partially filled buffer")
        void shouldClearPartiallyFilled() {
            buffer.put("item1");
            buffer.put("item2");
            buffer.clear();
            
            assertTrue(buffer.isEmpty());
            assertEquals(0, buffer.size());
            assertNull(buffer.get());
            assertNull(buffer.peek());
        }

        @Test
        @DisplayName("Should clear full buffer")
        void shouldClearFullBuffer() {
            for (int i = 0; i < DEFAULT_CAPACITY; i++) {
                buffer.put("item" + i);
            }
            
            buffer.clear();
            assertTrue(buffer.isEmpty());
            assertFalse(buffer.isFull());
            assertEquals(0, buffer.size());
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("Should handle null items")
        void shouldHandleNullItems() {
            assertTrue(buffer.put(null));
            assertEquals(1, buffer.size());
            assertNull(buffer.get());
        }

        @Test
        @DisplayName("Should work with capacity of 1")
        void shouldWorkWithCapacityOne() {
            TelemetryRingBuffer<String> singleBuffer = new TelemetryRingBuffer<>(1);
            
            assertTrue(singleBuffer.put("item1"));
            assertTrue(singleBuffer.isFull());
            
            assertTrue(singleBuffer.put("item2"));
            assertEquals("item2", singleBuffer.get());
        }

        @Test
        @DisplayName("Should handle large capacity")
        void shouldHandleLargeCapacity() {
            TelemetryRingBuffer<Integer> largeBuffer = new TelemetryRingBuffer<>(10000);
            assertEquals(10000, largeBuffer.capacity());
            
            for (int i = 0; i < 5000; i++) {
                assertTrue(largeBuffer.put(i));
            }
            assertEquals(5000, largeBuffer.size());
        }
    }

    @Nested
    @DisplayName("Thread Safety")
    class ThreadSafety {

        @Test
        @DisplayName("Should handle concurrent puts and gets")
        void shouldHandleConcurrentOperations() throws InterruptedException {
            TelemetryRingBuffer<Integer> concurrentBuffer = new TelemetryRingBuffer<>(1000);
            ExecutorService executor = Executors.newFixedThreadPool(10);
            CountDownLatch latch = new CountDownLatch(2);
            AtomicInteger putCount = new AtomicInteger(0);
            AtomicInteger getCount = new AtomicInteger(0);

            executor.submit(() -> {
                try {
                    for (int i = 0; i < 500; i++) {
                        concurrentBuffer.put(i);
                        putCount.incrementAndGet();
                    }
                } finally {
                    latch.countDown();
                }
            });

            executor.submit(() -> {
                try {
                    Thread.sleep(10);
                    for (int i = 0; i < 400; i++) {
                        if (concurrentBuffer.get() != null) {
                            getCount.incrementAndGet();
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    latch.countDown();
                }
            });

            assertTrue(latch.await(5, TimeUnit.SECONDS));
            assertEquals(500, putCount.get());
            assertTrue(getCount.get() > 0);
            
            executor.shutdown();
        }

        @Test
        @DisplayName("Should handle multiple concurrent readers")
        void shouldHandleMultipleConcurrentReaders() throws InterruptedException {
            TelemetryRingBuffer<String> concurrentBuffer = new TelemetryRingBuffer<>(100);
            
            for (int i = 0; i < 50; i++) {
                concurrentBuffer.put("item" + i);
            }

            ExecutorService executor = Executors.newFixedThreadPool(5);
            CountDownLatch latch = new CountDownLatch(5);
            List<String> allResults = Collections.synchronizedList(new ArrayList<>());

            for (int i = 0; i < 5; i++) {
                executor.submit(() -> {
                    try {
                        for (int j = 0; j < 10; j++) {
                            String item = concurrentBuffer.get();
                            if (item != null) {
                                allResults.add(item);
                            }
                        }
                    } finally {
                        latch.countDown();
                    }
                });
            }

            assertTrue(latch.await(5, TimeUnit.SECONDS));
            assertEquals(50, allResults.size());
            
            executor.shutdown();
        }
    }

    @Nested
    @DisplayName("Performance Characteristics")
    class PerformanceCharacteristics {

        @Test
        @DisplayName("Should handle rapid put/get cycles")
        void shouldHandleRapidCycles() {
            TelemetryRingBuffer<Integer> perfBuffer = new TelemetryRingBuffer<>(1000);
            
            long startTime = System.nanoTime();
            
            for (int cycle = 0; cycle < 10; cycle++) {
                for (int i = 0; i < 100; i++) {
                    perfBuffer.put(i);
                }
                
                for (int i = 0; i < 100; i++) {
                    assertNotNull(perfBuffer.get());
                }
            }
            
            long endTime = System.nanoTime();
            long duration = (endTime - startTime) / 1_000_000;
            
            assertTrue(duration < 1000, "Operations took too long: " + duration + "ms");
        }

        @Test
        @DisplayName("Should maintain consistent performance with overwrite")
        void shouldMaintainPerformanceWithOverwrite() {
            TelemetryRingBuffer<Integer> perfBuffer = new TelemetryRingBuffer<>(10);
            
            long startTime = System.nanoTime();
            
            for (int i = 0; i < 1000; i++) {
                perfBuffer.put(i);
            }
            
            long endTime = System.nanoTime();
            long duration = (endTime - startTime) / 1_000_000;
            
            assertTrue(duration < 100, "Overwrite operations took too long: " + duration + "ms");
            assertEquals(10, perfBuffer.size());
        }
    }
}
package com.edoofra.f1_telemetry_analyzer.model;

/**
 * Represents statistics about a telemetry buffer's current state and usage.
 * This record provides insights into buffer utilization, performance metrics,
 * and operational statistics for monitoring and debugging purposes.
 *
 * @param currentSize   the number of items currently stored in the buffer
 * @param capacity      the maximum number of items the buffer can hold
 * @param totalReceived the total number of packets received since initialization
 * @param totalDropped  the total number of packets dropped due to buffer overflow
 * @param dropRate      the percentage of packets dropped (0-100)
 */
public record BufferStats(
        int currentSize,
        int capacity,
        long totalReceived,
        long totalDropped,
        double dropRate
) {
    
    /**
     * Checks if the buffer is currently empty.
     *
     * @return true if the buffer contains no items, false otherwise
     */
    public boolean isEmpty() {
        return currentSize == 0;
    }
    
    /**
     * Checks if the buffer is currently at full capacity.
     *
     * @return true if the buffer has reached its maximum capacity, false otherwise
     */
    public boolean isFull() {
        return currentSize == capacity;
    }
    
    /**
     * Calculates the current buffer utilization as a percentage.
     *
     * @return the percentage of buffer capacity currently in use (0-100)
     */
    public double utilizationPercentage() {
        return (double) currentSize / capacity * 100.0;
    }
    
    /**
     * Calculates the success rate of packet processing.
     *
     * @return the percentage of packets successfully processed (0-100)
     */
    public double successRate() {
        if (totalReceived == 0) {
            return 100.0;
        }
        return 100.0 - dropRate;
    }
    
    /**
     * Gets the number of available slots in the buffer.
     *
     * @return the number of items that can still be added before the buffer is full
     */
    public int availableCapacity() {
        return capacity - currentSize;
    }
}
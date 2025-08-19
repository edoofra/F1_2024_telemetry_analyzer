package com.edoofra.f1_telemetry_analyzer.buffer;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * A thread-safe ring buffer implementation for telemetry data.
 * This buffer allows concurrent access for reading and writing,
 * ensuring that telemetry data can be stored and retrieved efficiently.
 *
 * @param <T> the type of items stored in the buffer
 */
public class TelemetryRingBuffer<T> {

    /**
     * The internal buffer array that holds the telemetry data.
     * It is of fixed size defined by the capacity.
     */
    private final Object[] buffer;

    /**
     * The maximum number of items the buffer can hold.
     * Once this limit is reached, the oldest items will be overwritten.
     */
    private final int capacity;

    /**
     * The index of the head of the buffer, where items are read from.
     * It is updated when an item is retrieved.
     */
    private final AtomicInteger head = new AtomicInteger(0);

    /**
     * The index of the tail of the buffer, where new items are written to.
     * It is updated when an item is added.
     */
    private final AtomicInteger tail = new AtomicInteger(0);

    /**
     * The current size of the buffer, indicating how many items are stored.
     * It is updated when items are added or removed.
     */
    private final AtomicInteger size = new AtomicInteger(0);

    /**
     * A read-write lock to ensure thread-safe access to the buffer.
     * It allows multiple readers or a single writer at any time.
     */
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    
    public TelemetryRingBuffer(int capacity) {
        this.capacity = capacity;
        this.buffer = new Object[capacity];
    }

    /**
     * Adds an item to the buffer.
     * If the buffer is full, it will overwrite the oldest item to avoid overflow.
     * This method is thread-safe and can be called concurrently due to the use of a write lock.
     *
     * @param item the item to add
     * @return true if the item was added successfully
     */
    public boolean put(T item) {
        lock.writeLock().lock();
        try {
            int currentTail = tail.get();
            buffer[currentTail] = item;

            // Update the tail index to the next position, wrapping around if necessary
            // the %capacity ensures it wraps around when reaching the end
            // of the buffer, effectively creating a circular buffer.
            tail.set((currentTail + 1) % capacity);
            
            if (size.get() == capacity) {
                // we need to move the head forward if the buffer is full
                // this effectively overwrites the oldest item
                // incrementing head to point to the next item
                head.set((head.get() + 1) % capacity);
            } else {
                size.incrementAndGet();
            }
            
            return true;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Retrieves and removes the item at the head of the buffer.
     * If the buffer is empty, it returns null.
     * This method is thread-safe and can be called concurrently due to the use of a read lock.
     *
     * @return the item at the head of the buffer, or null if the buffer is empty
     */
    @SuppressWarnings("unchecked")
    public T get() {
        lock.readLock().lock();
        try {
            if (size.get() == 0) {
                return null;
            }
            
            int currentHead = head.get();
            T item = (T) buffer[currentHead];
            buffer[currentHead] = null;
            head.set((currentHead + 1) % capacity);
            size.decrementAndGet();
            
            return item;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Retrieves the item at the head of the buffer without removing it.
     * If the buffer is empty, it returns null.
     * This method is thread-safe and can be called concurrently due to the use of a read lock.
     *
     * @return the item at the head of the buffer, or null if the buffer is empty
     */
    @SuppressWarnings("unchecked")
    public T peek() {
        lock.readLock().lock();
        try {
            if (size.get() == 0) {
                return null;
            }
            return (T) buffer[head.get()];
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Returns the current size of the buffer, indicating how many items are stored.
     * This method is thread-safe and can be called concurrently.
     *
     * @return the number of items currently in the buffer
     */
    public int size() {
        return size.get();
    }

    /**
     * Returns the maximum capacity of the buffer.
     * This method is thread-safe and can be called concurrently.
     *
     * @return the maximum number of items the buffer can hold
     */
    public int capacity() {
        return capacity;
    }

    /**
     * Checks if the buffer is empty.
     * This method is thread-safe and can be called concurrently.
     *
     * @return true if the buffer contains no items, false otherwise
     */
    public boolean isEmpty() {
        return size.get() == 0;
    }

    /**
     * Checks if the buffer is full.
     * This method is thread-safe and can be called concurrently.
     *
     * @return true if the buffer has reached its maximum capacity, false otherwise
     */
    public boolean isFull() {
        return size.get() == capacity;
    }

    /**
     * Clears the buffer, removing all items and resetting the head, tail, and size.
     * This method is thread-safe and can be called concurrently due to the use of a write lock.
     */
    public void clear() {
        lock.writeLock().lock();
        try {
            for (int i = 0; i < capacity; i++) {
                buffer[i] = null;
            }
            head.set(0);
            tail.set(0);
            size.set(0);
        } finally {
            lock.writeLock().unlock();
        }
    }
}
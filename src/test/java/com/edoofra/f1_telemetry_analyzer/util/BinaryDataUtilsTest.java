package com.edoofra.f1_telemetry_analyzer.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BinaryDataUtils Tests")
class BinaryDataUtilsTest {

    @Test
    @DisplayName("Should prevent instantiation")
    void shouldPreventInstantiation() {
        assertThrows(UnsupportedOperationException.class, () -> {
            new BinaryDataUtils();
        });
    }

    @Test
    @DisplayName("Should read unsigned short from byte array (little-endian)")
    void shouldReadUnsignedShort() {
        // Arrange: 0x1234 in little-endian format (0x34, 0x12)
        byte[] data = {0x34, 0x12, 0x00, 0x00};
        
        // Act
        int result = BinaryDataUtils.getUnsignedShort(data, 0);
        
        // Assert
        assertEquals(0x1234, result);
    }

    @Test
    @DisplayName("Should read unsigned short with maximum value")
    void shouldReadUnsignedShortMaxValue() {
        // Arrange: 0xFFFF in little-endian format
        byte[] data = {(byte) 0xFF, (byte) 0xFF};
        
        // Act
        int result = BinaryDataUtils.getUnsignedShort(data, 0);
        
        // Assert
        assertEquals(65535, result); // 0xFFFF = 65535
    }

    @Test
    @DisplayName("Should read 32-bit integer from byte array (little-endian)")
    void shouldReadInt() {
        // Arrange: 0x12345678 in little-endian format (0x78, 0x56, 0x34, 0x12)
        byte[] data = {0x78, 0x56, 0x34, 0x12, 0x00, 0x00, 0x00, 0x00};
        
        // Act
        int result = BinaryDataUtils.getInt(data, 0);
        
        // Assert
        assertEquals(0x12345678, result);
    }

    @Test
    @DisplayName("Should read negative 32-bit integer")
    void shouldReadNegativeInt() {
        // Arrange: -1 in little-endian format (all bits set)
        byte[] data = {(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
        
        // Act
        int result = BinaryDataUtils.getInt(data, 0);
        
        // Assert
        assertEquals(-1, result);
    }

    @Test
    @DisplayName("Should read 64-bit long from byte array (little-endian)")
    void shouldReadLong() {
        // Arrange: 0x123456789ABCDEF0 in little-endian format
        byte[] data = {
            (byte) 0xF0, (byte) 0xDE, (byte) 0xBC, (byte) 0x9A,
            (byte) 0x78, (byte) 0x56, (byte) 0x34, (byte) 0x12
        };
        
        // Act
        long result = BinaryDataUtils.getLong(data, 0);
        
        // Assert
        assertEquals(0x123456789ABCDEF0L, result);
    }

    @Test
    @DisplayName("Should read maximum 64-bit long value")
    void shouldReadMaxLong() {
        // Arrange: Long.MAX_VALUE in little-endian format
        byte[] data = {
            (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
            (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0x7F
        };
        
        // Act
        long result = BinaryDataUtils.getLong(data, 0);
        
        // Assert
        assertEquals(Long.MAX_VALUE, result);
    }

    @Test
    @DisplayName("Should read 32-bit float from byte array (little-endian)")
    void shouldReadFloat() {
        // Arrange: 3.14159f in little-endian IEEE 754 format
        float expectedValue = 3.14159f;
        int floatBits = Float.floatToIntBits(expectedValue);
        byte[] data = {
            (byte) (floatBits & 0xFF),
            (byte) ((floatBits >>> 8) & 0xFF),
            (byte) ((floatBits >>> 16) & 0xFF),
            (byte) ((floatBits >>> 24) & 0xFF)
        };
        
        // Act
        float result = BinaryDataUtils.getFloat(data, 0);
        
        // Assert
        assertEquals(expectedValue, result, 0.00001f);
    }

    @Test
    @DisplayName("Should read float with special values")
    void shouldReadSpecialFloatValues() {
        // Test NaN
        int nanBits = Float.floatToIntBits(Float.NaN);
        byte[] nanData = {
            (byte) (nanBits & 0xFF),
            (byte) ((nanBits >>> 8) & 0xFF),
            (byte) ((nanBits >>> 16) & 0xFF),
            (byte) ((nanBits >>> 24) & 0xFF)
        };
        
        float nanResult = BinaryDataUtils.getFloat(nanData, 0);
        assertTrue(Float.isNaN(nanResult));
        
        // Test Positive Infinity
        int infBits = Float.floatToIntBits(Float.POSITIVE_INFINITY);
        byte[] infData = {
            (byte) (infBits & 0xFF),
            (byte) ((infBits >>> 8) & 0xFF),
            (byte) ((infBits >>> 16) & 0xFF),
            (byte) ((infBits >>> 24) & 0xFF)
        };
        
        float infResult = BinaryDataUtils.getFloat(infData, 0);
        assertEquals(Float.POSITIVE_INFINITY, infResult);
    }

    @Test
    @DisplayName("Should read values with non-zero offset")
    void shouldReadWithOffset() {
        // Arrange: Test data with offset
        byte[] data = {
            0x00, 0x00, 0x00, 0x00, // padding
            0x34, 0x12, 0x00, 0x00  // 0x1234 at offset 4
        };
        
        // Act
        int result = BinaryDataUtils.getUnsignedShort(data, 4);
        
        // Assert
        assertEquals(0x1234, result);
    }
}
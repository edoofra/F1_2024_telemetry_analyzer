package com.edoofra.f1_telemetry_analyzer.util;

/**
 * Utility class for parsing binary data from byte arrays.
 * All methods assume little-endian byte order (Intel standard).
 * This class cannot be instantiated.
 */
public final class BinaryDataUtils {

    /**
     * Private constructor to prevent instantiation.
     */
    BinaryDataUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Reads an unsigned 16-bit integer from byte array (little-endian).
     *
     * @param data   the byte array to read from
     * @param offset the starting position in the array
     * @return the unsigned 16-bit integer value
     */
    public static int getUnsignedShort(byte[] data, int offset) {
        return ((data[offset + 1] & 0xFF) << 8) | (data[offset] & 0xFF);
    }

    /**
     * Reads a 32-bit integer from byte array (little-endian).
     *
     * @param data   the byte array to read from
     * @param offset the starting position in the array
     * @return the 32-bit integer value
     */
    public static int getInt(byte[] data, int offset) {
        return ((data[offset + 3] & 0xFF) << 24) | ((data[offset + 2] & 0xFF) << 16) |
               ((data[offset + 1] & 0xFF) << 8) | (data[offset] & 0xFF);
    }

    /**
     * Reads a 64-bit long from byte array (little-endian).
     *
     * @param data   the byte array to read from
     * @param offset the starting position in the array
     * @return the 64-bit long value
     */
    public static long getLong(byte[] data, int offset) {
        return ((long)(data[offset + 7] & 0xFF) << 56) | ((long)(data[offset + 6] & 0xFF) << 48) |
               ((long)(data[offset + 5] & 0xFF) << 40) | ((long)(data[offset + 4] & 0xFF) << 32) |
               ((long)(data[offset + 3] & 0xFF) << 24) | ((long)(data[offset + 2] & 0xFF) << 16) |
               ((long)(data[offset + 1] & 0xFF) << 8) | (long)(data[offset] & 0xFF);
    }

    /**
     * Reads a 32-bit float from byte array (little-endian).
     *
     * @param data   the byte array to read from
     * @param offset the starting position in the array
     * @return the 32-bit float value
     */
    public static float getFloat(byte[] data, int offset) {
        int bits = getInt(data, offset);
        return Float.intBitsToFloat(bits);
    }

    // Alias methods for F1 telemetry parsing compatibility
    
    /**
     * Reads an unsigned 8-bit integer from byte array.
     * 
     * @param data   the byte array to read from
     * @param offset the starting position in the array
     * @return the unsigned 8-bit integer value (0-255)
     */
    public static int readUInt8(byte[] data, int offset) {
        return data[offset] & 0xFF;
    }
    
    /**
     * Reads an unsigned 16-bit integer from byte array (little-endian).
     * Alias for getUnsignedShort().
     * 
     * @param data   the byte array to read from
     * @param offset the starting position in the array
     * @return the unsigned 16-bit integer value
     */
    public static int readUInt16(byte[] data, int offset) {
        return getUnsignedShort(data, offset);
    }
    
    /**
     * Reads an unsigned 32-bit integer from byte array (little-endian).
     * Returns as long to handle full unsigned range.
     * 
     * @param data   the byte array to read from
     * @param offset the starting position in the array
     * @return the unsigned 32-bit integer value as long
     */
    public static long readUInt32(byte[] data, int offset) {
        return getInt(data, offset) & 0xFFFFFFFFL;
    }
    
    /**
     * Reads a 32-bit float from byte array (little-endian).
     * Alias for getFloat().
     * 
     * @param data   the byte array to read from
     * @param offset the starting position in the array
     * @return the 32-bit float value
     */
    public static float readFloat(byte[] data, int offset) {
        return getFloat(data, offset);
    }
}
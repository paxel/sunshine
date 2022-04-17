package paxel.sunshine.api.memory;

import paxel.sunshine.api.datatypes.ULong;

public interface RichReadOnlyRandomAccessMemory extends ReadOnlyRandomAccessMemory {

    /**
     * Get the byte at index as unsigned value.
     * 
     * @param index The index.
     * @return the unsigned value as short.
     */
    short getUByteAt(long index);

    /**
     * Get the two bytes at index as signed value.
     * 
     * @param index The index.
     * @return the signed value as short.
     */
    short getInt16At(long index);

    /**
     * Get the two bytes at index as unsigned value.
     * 
     * @param index The index.
     * @return the unsigned value as int.
     */
    int getUInt16At(long index);

    /**
     * Get the four bytes at index as signed value.
     * 
     * @param index The index.
     * @return the signed value as int.
     */
    int getInt32At(long index);

    /**
     * Get the four bytes at index as unsigned value.
     * 
     * @param index The index.
     * @return the unsigned value as long.
     */
    long getUInt32At(long index);

    /**
     * Get the eight bytes at index as signed value.
     * 
     * @param index The index.
     * @return the signed value as long.
     */
    long getInt64At(long index);

    /**
     * Get the eight bytes at index as unsigned value.
     * 
     * @param index The index.
     * @return the unsigned value as ULong.
     */
    ULong getUInt64At(long index);

    /**
     * Get the four bytes at index as normal precession value.
     * 
     * @param index The index.
     * @return the signed value as float.
     */
    float getFloatAt(long index);

    /**
     * Get the eight bytes at index as double precession value.
     * 
     * @param index The index.
     * @return the signed value as double.
     */
    double getDoubleAt(long index);

    /**
     * Get the given number of bytes at index as String.
     * 
     * @param index The index.
     * @param length The number of bytes.
     * @return the String.
     */
    String getStringAt(long index, int length);

    /**
     * Get the given number of bytes at index as {@link ReadOnlyRandomAccessMemory}.
     * 
     * @param index The index.
     * @param length The number of bytes.
     * @return the bytes.
     */
    ReadOnlyRandomAccessMemory getDataAt(long index, int length);
}

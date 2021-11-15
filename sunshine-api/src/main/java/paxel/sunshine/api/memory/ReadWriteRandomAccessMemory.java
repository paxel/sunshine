package paxel.sunshine.api.memory;

import java.io.IOException;

public interface ReadWriteRandomAccessMemory extends ReadOnlyRandomAccessMemory {
    /**
     * Overwrites the byte at index with the new value.
     *
     * @param index The index of the new byte.
     * @param value The new value at the index.
     * @throws IndexOutOfBoundsException in case the index is less than 0 or bigger than the RAM.
     */
    void putByteAt(long index, byte value);

    /**
     * Reads parts of the given array into the RandomAccessMemory
     *
     * @param index         The position in the RAM where to write the data.
     * @param source        The source of the data.
     * @param offsetInArray The offset in the source data.
     * @param length        The length of data to be written.
     * @throws IndexOutOfBoundsException in case the index is less than 0 or bigger than the RAM or the length is bigger than the remaining bytes.
     * @throws IllegalArgumentException  in case the offset is less than 0 or bigger than the source or the length is bigger than the remaining bytes in the source.
     * @throws NullPointerException      in case the source is null.
     */
    void copyFromSource(long index, byte[] source, int offsetInArray, int length);

    /**
     * Checks is the given type of source is supported.
     *
     * @param source The source type.
     * @return {@code true} in case the {@link #copyFromSource(long, Object)} will accept instances of the given source.
     */
    boolean supportsSource(Class<?> source);

    /**
     * Reads Bytes into the RandomAccessMemory. The amount is defined by the Source and the size of the RAM.
     *
     * @param index  The index where to put the bytes in the RAM.
     * @param source The source of the bytes.
     * @param <T>    The type of the source
     * @return the number of bytes put at index.
     * @throws IOException in case the source causes it.
     */
    <T> long copyFromSource(long index, T source) throws IOException;
}

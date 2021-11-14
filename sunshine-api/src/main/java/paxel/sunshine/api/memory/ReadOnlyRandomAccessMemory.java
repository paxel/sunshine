package paxel.sunshine.api.memory;

import java.io.IOException;

public interface ReadOnlyRandomAccessMemory {
    /**
     * Receive the byte at position index.
     *
     * @param index The index of the byte.
     * @return the byte
     * @throws IndexOutOfBoundsException in case the index is less than 0 or bigger than the RAM.
     */
    public byte getByteAt(long index);

    /**
     * Copy the bytes at position index into the dst byte array.
     *
     * @param index The index of the first byte.
     * @param dst   The destination bytes.
     * @throws IndexOutOfBoundsException in case the index is less than 0 or bigger than the RAM or the dst is bigger than the remaining bytes.
     * @throws NullPointerException      in case the dst is null.
     */
    public void getBytesAt(long index, byte[] dst);

    /**
     * Retrieve the bytes at position index in a new byte array.
     *
     * @param index  The index of the first byte.
     * @param length The number of the bytes to receive.
     * @throws IndexOutOfBoundsException in case the index is less than 0 or bigger than the RAM or the length is bigger than the remaining bytes.
     */
    public byte[] getBytesAt(long index, int length);

    /**
     * Test if the Implementation can write Bytes into the given Class. If the result is true, the {@link #writeBytesInto(long, int, Object)} method should accept an instance as destination.
     *
     * @param sink the sink type.
     * @return {@code true} in case the type is supported.
     * @throws NullPointerException in case the sink is null.
     */
    public boolean supportsSink(Class<?> sink);

    /**
     * This writes bytes into the given target if supported.
     *
     * @param <T>    the type of the sink.
     * @param index  The index of the first byte.
     * @param length The number of bytes to write.
     * @param dst    The sink to receive the bytes.
     * @throws IndexOutOfBoundsException in case the index is less than 0 or bigger than the RAM or the length is bigger than the remaining bytes.
     * @throws NullPointerException      in case the dst is null.
     * @throws IllegalArgumentException  in case the type is not supported.
     * @throws IOException               in case the dst throws it.
     * @return
     */
    public <T> long writeBytesInto(long index, int length, T dst) throws IOException;

    /**
     * retrieve the size of the Ram.
     *
     * @return the number of bytes in this Random Access Memory.
     */
    public long size();
}

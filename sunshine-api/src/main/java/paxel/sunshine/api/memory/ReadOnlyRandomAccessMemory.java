package paxel.sunshine.api.memory;

import java.io.IOException;

/**
 * Simplest form of RAM. This only supports bytes and byte arrays as
 */
public interface ReadOnlyRandomAccessMemory {
	/**
	 * Receive the byte at position index.
	 *
	 * @param index The index of the byte.
	 * @return the byte
	 * @throws IndexOutOfBoundsException in case the index is less than 0 or bigger
	 *                                   than the RAM.
	 */
	byte getByteAt(long index);

	/**
	 * Retrieve the bytes at position index in a new byte array.
	 *
	 * @param index  The index of the first byte.
	 * @param length The number of the bytes to receive.
	 * @throws IndexOutOfBoundsException in case the index is less than 0 or bigger
	 *                                   than the RAM or the length is bigger than
	 *                                   the remaining bytes.
	 */
	byte[] getBytesAt(long index, int length);

	/**
	 * Retrieve all the bytes in a new byte array.
	 */
	byte[] getBytes();

	/**
	 * Copy the bytes at position index into the destination byte array.
	 *
	 * @param index       The index of the first byte.
	 * @param destination The destination bytes.
	 * @throws IndexOutOfBoundsException in case the index is less than 0 or bigger
	 *                                   than the RAM or the destination is bigger
	 *                                   than the remaining bytes.
	 * @throws NullPointerException      in case the destination is null.
	 */
	void copyToDestination(long index, byte[] destination);

	/**
	 * Copy the bytes at position index into the destination byte array.
	 *
	 * @param index             The index of the first byte.
	 * @param destination       The destination bytes.
	 * @param destinationOffset The offset in the destination bytes.
	 * @throws IndexOutOfBoundsException in case the index is less than 0 or bigger
	 *                                   than the RAM or the destination is bigger
	 *                                   than the remaining bytes.
	 * @throws NullPointerException      in case the destination is null.
	 */
	void copyToDestination(long index, byte[] destination, int destinationOffset, int length);

	/**
	 * Test if the Implementation can write Bytes into the given Class. If the
	 * result is true, the {@link #copyToDestination(long, int, Object)} method
	 * should accept an instance as destination.
	 *
	 * @param destinationClass the destination type.
	 * @return {@code true} in case the type is supported.
	 * @throws NullPointerException in case the destination is null.
	 */
	boolean supportsDestination(Class<?> destinationClass);

	/**
	 * This writes bytes into the given target if supported.
	 *
	 * @param <T>         the type of the destination.
	 * @param index       The index of the first byte.
	 * @param length      The number of bytes to write.
	 * @param destination The destination to receive the bytes.
	 * @return the number of bytes written.
	 * @throws IndexOutOfBoundsException in case the index is less than 0 or bigger
	 *                                   than the RAM or the length is bigger than
	 *                                   the remaining bytes.
	 * @throws NullPointerException      in case the destination is null.
	 * @throws IllegalArgumentException  in case the type is not supported.
	 * @throws IOException               in case the destination throws it.
	 */
	<T> long copyToDestination(long index, int length, T destination) throws IOException;

	/**
	 * retrieve the size of the Ram.
	 *
	 * @return the number of bytes in this Random Access Memory.
	 */
	long size();
}

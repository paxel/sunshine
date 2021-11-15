package paxel.sunshine.api.memory;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;

import static java.util.Objects.requireNonNull;

public class ByteBufferRoRam implements RichReadOnlyRandomAccessMemory {

    private final ByteBuffer byteBuffer;

    /**
     * This will construct a new instance using the given ByteBuffer.
     * This class will use the given ByteBuffer from pos 0 to limit and change the position if needed.
     * But it will not change the content of the ByteBuffer (and its slices)
     *
     * @param byteBuffer The Ram.
     */
    public ByteBufferRoRam(ByteBuffer byteBuffer) {
        requireNonNull(byteBuffer);
        byteBuffer.position(0);
        this.byteBuffer = byteBuffer;
    }

    @Override
    public byte getByteAt(long index) {
        validate(index, 1);
        return byteBuffer.get((int) index);
    }


    @Override
    public void copyToDestination(long index, byte[] destination) {
        requireNonNull(destination);
        validate(index, destination.length);
        byteBuffer.position((int) index);
        byteBuffer.get(destination, 0, destination.length);
    }

    @Override
    public void copyToDestination(long index, byte[] destination, int destinationOffset, int length) {
        requireNonNull(destination);
        validate(index, length);
        validate(destination,0,length);
        byteBuffer.position((int) index);
        byteBuffer.get(destination, destinationOffset, length);
    }

    private void validate(byte[] destination, int index, int length) {
        if (index < 0)
            throw new IllegalArgumentException("index was < 0 :" + index);
        if (index > destination.length)
            throw new IllegalArgumentException("index was > array " + destination.length + " :" + index);
        if (index + length > destination.length)
            throw new IllegalArgumentException(String.format("length %d after index was > array %d :%d", length, destination.length, index));
    }


    @Override
    public byte[] getBytesAt(long index, int length) {
        validate(index, length);
        byte[] result = new byte[length];
        byteBuffer.position((int) index);
        byteBuffer.get(result, 0, length);
        return result;
    }

    @Override
    public boolean supportsDestination(Class<?> destinationClass) {
        if (destinationClass.isAssignableFrom(ByteBuffer.class))
            return true;
        if (destinationClass.isAssignableFrom(OutputStream.class))
            return true;
        if (destinationClass.isAssignableFrom(WritableByteChannel.class))
            return true;
        return false;
    }

    @Override
    public <T> long copyToDestination(long index, int length, T destination) throws IOException {
        requireNonNull(destination);
        validate(index, length);
        if (destination instanceof ByteBuffer) {
            byteBuffer.position(0);
            ByteBuffer slice = byteBuffer.slice();
            slice.position((int) index);
            slice.limit((int) (index + length));
            ((ByteBuffer) destination).put(slice);
            return length;
        } else if (destination instanceof OutputStream) {
            WritableByteChannel writableByteChannel = Channels.newChannel((OutputStream) destination);
            return copyToDestination(index, length, writableByteChannel);
        } else if (destination instanceof WritableByteChannel) {
            byteBuffer.position(0);
            ByteBuffer slice = byteBuffer.slice();
            slice.position((int) index);
            slice.limit((int) (index + length));
            ((WritableByteChannel) destination).write(slice);
            return length;
        }
        throw new IllegalArgumentException("Unsupported destination type " + destination.getClass());
    }

    private void validate(long index, int length) {
        if (index > Integer.MAX_VALUE)
            throw new IndexOutOfBoundsException("index was > Integer.MAX_VALUE (unsupported for ByteBuffer) :" + index);
        if (index < 0)
            throw new IndexOutOfBoundsException("index was < 0 :" + index);
        if (index > byteBuffer.limit())
            throw new IndexOutOfBoundsException("index was > limit " + byteBuffer.limit() + " :" + index);
        if (index + length > byteBuffer.limit())
            throw new IndexOutOfBoundsException(String.format("length %d after index was > limit %d :%d", length, byteBuffer.limit(), index));
    }

    @Override
    public long size() {
        return byteBuffer.limit();
    }
}

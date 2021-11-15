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
    public void copyToDest(long index, byte[] dest) {
        requireNonNull(dest);
        validate(index, dest.length);
        byteBuffer.position((int) index);
        byteBuffer.get(dest, 0, dest.length);
    }

    @Override
    public void copyToDest(long index, byte[] dest, int destOffset, int length) {
        requireNonNull(dest);
        validate(index, length);
        validate(dest,0,length);
        byteBuffer.position((int) index);
        byteBuffer.get(dest, destOffset, length);
    }

    private void validate(byte[] dest, int index, int length) {
        if (index < 0)
            throw new IllegalArgumentException("index was < 0 :" + index);
        if (index > dest.length)
            throw new IllegalArgumentException("index was > array " + dest.length + " :" + index);
        if (index + length > dest.length)
            throw new IllegalArgumentException(String.format("length %d after index was > array %d :%d", length, dest.length, index));
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
    public boolean supportsSink(Class<?> sink) {
        if (sink.isAssignableFrom(ByteBuffer.class))
            return true;
        if (sink.isAssignableFrom(OutputStream.class))
            return true;
        if (sink.isAssignableFrom(WritableByteChannel.class))
            return true;
        return false;
    }

    @Override
    public <T> long writeBytesToDest(long index, int length, T dest) throws IOException {
        requireNonNull(dest);
        validate(index, length);
        if (dest instanceof ByteBuffer) {
            byteBuffer.position(0);
            ByteBuffer slice = byteBuffer.slice();
            slice.position((int) index);
            slice.limit((int) (index + length));
            ((ByteBuffer) dest).put(slice);
            return length;
        } else if (dest instanceof OutputStream) {
            WritableByteChannel writableByteChannel = Channels.newChannel((OutputStream) dest);
            return writeBytesToDest(index, length, writableByteChannel);
        } else if (dest instanceof WritableByteChannel) {
            byteBuffer.position(0);
            ByteBuffer slice = byteBuffer.slice();
            slice.position((int) index);
            slice.limit((int) (index + length));
            ((WritableByteChannel) dest).write(slice);
            return length;
        }
        throw new IllegalArgumentException("Unsupported dest type " + dest.getClass());
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

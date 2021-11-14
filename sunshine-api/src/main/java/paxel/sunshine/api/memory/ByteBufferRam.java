package paxel.sunshine.api.memory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import static java.util.Objects.requireNonNull;

public class ByteBufferRam implements RichReadWriteRandomAccessMemory {

    private final ByteBufferRoRam roRam;
    private final ByteBuffer byteBuffer;

    public ByteBufferRam(ByteBuffer byteBuffer) {
        this.byteBuffer = byteBuffer;
        roRam = new ByteBufferRoRam(byteBuffer);
    }

    @Override
    public byte getByteAt(long index) {
        return roRam.getByteAt(index);
    }

    @Override
    public void getBytesAt(long index, byte[] dst) {
        roRam.getBytesAt(index, dst);
    }

    @Override
    public byte[] getBytesAt(long index, int length) {
        return roRam.getBytesAt(index, length);
    }

    @Override
    public boolean supportsSink(Class<?> sink) {
        return roRam.supportsSink(sink);
    }

    @Override
    public <T> long writeBytesInto(long index, int length, T dst) throws IOException {
        return roRam.writeBytesInto(index, length, dst);
    }

    @Override
    public long size() {
        return roRam.size();
    }

    @Override
    public void putByteAt(long index, byte value) {
        validate(index);
        byteBuffer.put((int) index, value);
    }

    @Override
    public void writeBytesIntoRam(long index, byte[] source, int offsetInArray, int length) {
        requireNonNull(source);
        validate(index);
        // we try to keep the internal position as it was
        byteBuffer.position((int) index);
        byteBuffer.put(source, offsetInArray, length);
        byteBuffer.position(0);
    }

    @Override
    public boolean supportsSource(Class<?> source) {
        requireNonNull(source);
        if (source.isAssignableFrom(ByteBuffer.class))
            return true;
        else if (source.isAssignableFrom(ReadableByteChannel.class))
            return true;
        else if (source.isAssignableFrom(InputStream.class))
            return true;
        return false;
    }

    @Override
    public <T> long writeBytesIntoRam(long index, T source) throws IOException {
        requireNonNull(source);
        validate(index);
        if (source instanceof ByteBuffer) {
            byteBuffer.position((int) index);
            byteBuffer.put((ByteBuffer) source);
            int position = byteBuffer.position();
            byteBuffer.position(0);
            return position - index;
        } else if (source instanceof InputStream) {
            ReadableByteChannel readableByteChannel = Channels.newChannel((InputStream) source);
            return writeBytesIntoRam(index, source);
        } else if (source instanceof ReadableByteChannel) {
            byteBuffer.position((int) index);
            int read = ((ReadableByteChannel) source).read(byteBuffer);
            int position = byteBuffer.position();
            byteBuffer.position(0);
            return position - index;
        }
        throw new IllegalArgumentException("Unsupported source type " + source.getClass());
    }

    private void validate(long index) {
        if (index > Integer.MAX_VALUE)
            throw new IndexOutOfBoundsException("index was > Integer.MAX_VALUE (unsupported for ByteBuffer) :" + index);
        if (index < 0)
            throw new IndexOutOfBoundsException("index was < 0 :" + index);
        if (index > byteBuffer.limit())
            throw new IndexOutOfBoundsException("index was > limit " + byteBuffer.limit() + " :" + index);
    }
}

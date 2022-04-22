package paxel.sunshine.api.memory.bytebuffer;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;

import paxel.sunshine.api.datatypes.ULong;
import paxel.sunshine.api.memory.ReadOnlyRandomAccessMemory;
import paxel.sunshine.api.memory.RichReadOnlyRandomAccessMemory;
import paxel.sunshine.api.memory.RichReadWriteRandomAccessMemory;

public class ByteBufferRam implements RichReadWriteRandomAccessMemory, RichReadOnlyRandomAccessMemory {

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
    public void copyToDestination(long index, byte[] destination) {
        roRam.copyToDestination(index, destination);
    }

    @Override
    public void copyToDestination(long index, byte[] destination, int destinationOffset, int length) {
        roRam.copyToDestination(index, destination, destinationOffset, length);
    }

    @Override
    public byte[] getBytesAt(long index, int length) {
        return roRam.getBytesAt(index, length);
    }

    @Override
    public byte[] getBytes() {
        return getBytesAt(0, byteBuffer.limit());
    }

    @Override
    public boolean supportsDestination(Class<?> destinationClass) {
        return roRam.supportsDestination(destinationClass);
    }

    @Override
    public <T> long copyToDestination(long index, int length, T destination) throws IOException {
        return roRam.copyToDestination(index, length, destination);
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
    public void copyFromSource(long index, byte[] source, int offsetInArray, int length) {
        requireNonNull(source);
        validate(index);
        byteBuffer.position((int) index);
        byteBuffer.put(source, offsetInArray, length);
    }

    @Override
    public boolean supportsSource(Class<?> source) {
        requireNonNull(source);
        if (source.isAssignableFrom(ByteBuffer.class)) {
            return true;
        } else if (source.isAssignableFrom(ReadableByteChannel.class)) {
            return true;
        } else if (source.isAssignableFrom(InputStream.class)) {
            return true;
        }
        return false;
    }

    @Override
    public <T> long copyFromSource(long index, T source) throws IOException {
        requireNonNull(source);
        validate(index);
        if (source instanceof ByteBuffer) {
            byteBuffer.position((int) index);
            byteBuffer.put((ByteBuffer) source);
            int position = byteBuffer.position();
            return position - index;
        } else if (source instanceof InputStream) {
            ReadableByteChannel readableByteChannel = Channels.newChannel((InputStream) source);
            return copyFromSource(index, readableByteChannel);
        } else if (source instanceof ReadableByteChannel) {
            byteBuffer.position((int) index);
            ((ReadableByteChannel) source).read(byteBuffer);
            int position = byteBuffer.position();
            return position - index;
        }
        throw new IllegalArgumentException("Unsupported source type " + source.getClass());
    }

    private void validate(long index) {
        if (index > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("index was > Integer.MAX_VALUE (unsupported for ByteBuffer) :" + index);
        }
        if (index < 0) {
            throw new IllegalArgumentException("index was < 0 :" + index);
        }
        if (index > byteBuffer.limit()) {
            throw new IllegalArgumentException("index was > limit " + byteBuffer.limit() + " :" + index);
        }
    }

    @Override
    public void putUByteAt(long index, short value) {
        if (value > (short) 0xff || value < 0) {
            throw new IllegalArgumentException("Invalid UByte value " + value);
        }
        byteBuffer.put((int) index, (byte) value);
    }

    @Override
    public void putInt16At(long index, short value) {
        byteBuffer.putShort((int) index, value);
    }

    @Override
    public void putUInt16At(long index, int value) {
        if (value > 0xffff || value < 0) {
            throw new IllegalArgumentException("Invalid UInt16 value " + value);
        }
        byteBuffer.putShort((int) index, (short) value);
    }
    
    @Override
    public void putInt32At(long index, int value) {
        byteBuffer.putInt((int) index, value);
    }

    @Override
    public void putUInt32At(long index, long value) {
        if (value > 0xffff_ffffL || value < 0) {
            throw new IllegalArgumentException("Invalid UInt32 value " + value);
        }
        byteBuffer.putInt((int) index, (int) value);
    }

    @Override
    public void putInt64At(long index, long value) {
        byteBuffer.putLong((int) index, value);
    }

    @Override
    public void putUInt64At(long index, ULong value) {
        byteBuffer.putLong((int) index, value.getSignedValue());
    }

    @Override
    public void putFloatAt(long index, float value) {
        byteBuffer.putFloat((int) index, value);
    }

    @Override
    public void putDoubleAt(long index, double value) {
        byteBuffer.putDouble((int) index, value);
    }

    @Override
    public void putStringAt(long index, CharSequence value) {
        byteBuffer.position((int) index);
        byteBuffer.put(value.toString().getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void putStringAt(long index, CharSequence value, int offset, int length) {
        byteBuffer.position((int) index);
        byteBuffer.put(value.toString().substring(offset, offset + length).getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void putDataAt(long index, ReadOnlyRandomAccessMemory value) {
        // TODO
    }

    @Override
    public void putDataAt(long index, ReadOnlyRandomAccessMemory value, int offset, int length) {
        // TODO
    }

    @Override
    public short getUByteAt(long index) {
        return (short) (byteBuffer.get((int) index) & 0xff);
    }

    @Override
    public short getInt16At(long index) {
        return byteBuffer.getShort((int) index);
    }

    @Override
    public int getUInt16At(long index) {
        return ((int) byteBuffer.getShort((int) index)) & 0xffff;
    }

    @Override
    public int getInt32At(long index) {
        return byteBuffer.getInt((int) index);
    }

    @Override
    public long getUInt32At(long index) {
        return Integer.toUnsignedLong(byteBuffer.getInt((int) index));
    }

    @Override
    public long getInt64At(long index) {
        return byteBuffer.getLong((int) index);
    }

    @Override
    public ULong getUInt64At(long index) {
        return new ULong(byteBuffer.getLong((int) index));
    }

    @Override
    public float getFloatAt(long index) {
        return byteBuffer.getFloat((int) index);
    }

    @Override
    public double getDoubleAt(long index) {
        return byteBuffer.getDouble((int) index);
    }

    @Override
    public String getStringAt(long index, int length) {
        byte[] bytes = new byte[length];
        byteBuffer.position((int) index);
        byteBuffer.get(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    @Override
    public ReadOnlyRandomAccessMemory getDataAt(long index, int length) {
        byteBuffer.position((int) index);
        ByteBuffer slice = byteBuffer.slice();
        slice.limit(length);
        return new ByteBufferRoRam(slice);
    }

}

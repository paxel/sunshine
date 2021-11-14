package paxel.sunshine.api.memory;

import java.io.IOException;

public interface ReadWriteRandomAccessMemory extends ReadOnlyRandomAccessMemory {
    public void putByteAt(long index, byte value);

    public void writeBytesIntoRam(long index, byte[] source, int offsetInArray, int length);

    public boolean supportsSource(Class<?> source);

    public <T> long writeBytesIntoRam(long index, T source) throws IOException;
}

package paxel.sunshine.api.memory;

import paxel.sunshine.api.datatypes.ULong;

public interface RichReadWriteRandomAccessMemory extends ReadWriteRandomAccessMemory {
    void putUByteAt(long index, short value);

    void putInt16At(long index, short value);

    void putUInt16At(long index, int value);

    void putInt32At(long index, int value);

    void putUInt32At(long index, long value);

    void putInt64At(long index, long value);

    void putUInt64At(long index, ULong value);

    void putFloatAt(long index, float value);

    void putDoubleAt(long index, double value);

    void putStringAt(long index, CharSequence value);

    void putStringAt(long index, CharSequence value, int offset, int length);

    void putDataAt(long index, ReadOnlyRandomAccessMemory value);

    void putDataAt(long index, ReadOnlyRandomAccessMemory value, int offset, int length);
}

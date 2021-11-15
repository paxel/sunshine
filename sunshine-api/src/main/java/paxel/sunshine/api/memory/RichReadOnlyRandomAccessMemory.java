package paxel.sunshine.api.memory;

import paxel.sunshine.api.datatypes.ULong;

public interface RichReadOnlyRandomAccessMemory extends ReadOnlyRandomAccessMemory {

    short getUByteAt(long index);

    short getInt16At(long index);

    int getUInt16At(long index);

    int getInt32At(long index);

    long getUInt32At(long index);

    long getInt64At(long index);

    ULong getUInt64At(long index);

    float getFloatAt(long index);

    double getDoubleAt(long index);

    String getStringAt(long index, int length);

    ReadOnlyRandomAccessMemory getDataAt(long index, int length);
}

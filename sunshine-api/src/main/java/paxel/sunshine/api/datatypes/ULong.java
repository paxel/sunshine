package paxel.sunshine.api.datatypes;

/**
 * This represents the missing datatype of an unsigned long using a signed long.
 */
public class ULong implements Comparable<ULong> {
    private final long signedValue;

    public static ULong fromUnsignedString(String unsignedValue) {
        return new ULong(Long.parseUnsignedLong(unsignedValue));
    }

    public ULong(long value) {
        this.signedValue = value;
    }

    public long getSignedValue() {
        return signedValue;
    }


    @Override
    public int compareTo(ULong o) {
        return Long.compareUnsigned(signedValue, o.signedValue);
    }

    @Override
    public String toString() {
        return Long.toUnsignedString(signedValue);
    }

    /**
     * Checks if the wrapped value is < 0 (and therefore can not be represented by a signed value)
     *
     * @return {@code false} if the unsigned value can be represented by a signed value.
     */
    public boolean isSignedValueLessThanZero() {
        return signedValue < 0;
    }


}

package paxel.sunshine.api.memory.bytebuffer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.StandardCharsets;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import org.junit.Test;
import paxel.sunshine.api.datatypes.ULong;

public class ByteBufferRamTest {

    @Test
    public void writeToByteBuffer() throws IOException {
        ByteBufferRam byteBufferRam = new ByteBufferRam(ByteBuffer.wrap("ABBA-DOMINO-78".getBytes(StandardCharsets.UTF_8)));
        ByteBuffer dst = ByteBuffer.allocate(6);

        assertThat(byteBufferRam.supportsDestination(ByteBuffer.class), is(true));
        long written = byteBufferRam.copyToDestination(5, 6, dst);

        assertThat(written, is(6L));
        assertThat(dst.get(0), is((byte) 'D'));
        assertThat(dst.get(1), is((byte) 'O'));
        assertThat(dst.get(2), is((byte) 'M'));
        assertThat(dst.get(3), is((byte) 'I'));
        assertThat(dst.get(4), is((byte) 'N'));
        assertThat(dst.get(5), is((byte) 'O'));
    }

    @Test
    public void writeToOutputStream() throws IOException {
        ByteBufferRam byteBufferRam = new ByteBufferRam(ByteBuffer.wrap("ABBA-DOMINO-78".getBytes(StandardCharsets.UTF_8)));
        ByteArrayOutputStream dst = new ByteArrayOutputStream();

        assertThat(byteBufferRam.supportsDestination(OutputStream.class), is(true));
        long written = byteBufferRam.copyToDestination(5, 6, dst);
        byte[] bytes = dst.toByteArray();
        assertThat(written, is(6L));
        assertThat(bytes.length, is(6));
        assertThat(bytes[0], is((byte) 'D'));
        assertThat(bytes[1], is((byte) 'O'));
        assertThat(bytes[2], is((byte) 'M'));
        assertThat(bytes[3], is((byte) 'I'));
        assertThat(bytes[4], is((byte) 'N'));
        assertThat(bytes[5], is((byte) 'O'));
    }

    @Test
    public void writeToWritableByteChannel() throws IOException {
        ByteBufferRam byteBufferRam = new ByteBufferRam(ByteBuffer.wrap("ABBA-DOMINO-78".getBytes(StandardCharsets.UTF_8)));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        WritableByteChannel dst = Channels.newChannel(out);
        assertThat(byteBufferRam.supportsDestination(WritableByteChannel.class), is(true));
        long written = byteBufferRam.copyToDestination(5, 6, dst);
        byte[] bytes = out.toByteArray();
        assertThat(written, is(6L));
        assertThat(bytes.length, is(6));
        assertThat(bytes[0], is((byte) 'D'));
        assertThat(bytes[1], is((byte) 'O'));
        assertThat(bytes[2], is((byte) 'M'));
        assertThat(bytes[3], is((byte) 'I'));
        assertThat(bytes[4], is((byte) 'N'));
        assertThat(bytes[5], is((byte) 'O'));
    }

    @Test
    public void getByte() throws IOException {
        ByteBufferRam byteBufferRam = new ByteBufferRam(ByteBuffer.wrap("ABBA-DOMINO-78".getBytes(StandardCharsets.UTF_8)));
        assertThat(byteBufferRam.size(), is(14L));
        assertThat(byteBufferRam.getByteAt(4), is((byte) '-'));
        assertThat(byteBufferRam.getByteAt(11), is((byte) '-'));
    }

    @Test
    public void getBytes() throws IOException {
        ByteBufferRam byteBufferRam = new ByteBufferRam(ByteBuffer.wrap("ABBA-DOMINO-78".getBytes(StandardCharsets.UTF_8)));
        assertThat(byteBufferRam.size(), is(14L));
        assertThat(byteBufferRam.getBytes(), is("ABBA-DOMINO-78".getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    public void getBytesAt() throws IOException {
        ByteBufferRam byteBufferRam = new ByteBufferRam(ByteBuffer.wrap("ABBA-DOMINO-78".getBytes(StandardCharsets.UTF_8)));
        byte[] bytesAt = byteBufferRam.getBytesAt(12, 2);

        assertThat(new String(bytesAt), is("78"));
    }

    @Test
    public void copyToDestination() throws IOException {
        ByteBufferRam byteBufferRam = new ByteBufferRam(ByteBuffer.wrap("ABBA-DOMINO-78".getBytes(StandardCharsets.UTF_8)));
        byte[] dest = new byte[4];
        byteBufferRam.copyToDestination(0, dest);
        assertThat(new String(dest), is("ABBA"));
    }

    @Test
    public void copyToDestination2() throws IOException {
        ByteBufferRam byteBufferRam = new ByteBufferRam(ByteBuffer.wrap("ABBA-DOMINO-78".getBytes(StandardCharsets.UTF_8)));
        byte[] dest = new byte[4];
        byteBufferRam.copyToDestination(7, dest);
        assertThat(new String(dest), is("MINO"));
    }

    @Test
    public void getBytesLength() throws IOException {
        ByteBufferRam byteBufferRam = new ByteBufferRam(ByteBuffer.wrap("ABBA-DOMINO-78".getBytes(StandardCharsets.UTF_8)));
        byte[] dest = new byte[6];
        byteBufferRam.copyToDestination(0, dest, 0, 2);
        byteBufferRam.copyToDestination(5, dest, 2, 2);
        byteBufferRam.copyToDestination(12, dest, 4, 2);
        assertThat(new String(dest), is("ABDO78"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidTarget() throws IOException {
        ByteBufferRam byteBufferRam = new ByteBufferRam(
                ByteBuffer.wrap("ABBA-DOMINO-78".getBytes(StandardCharsets.UTF_8)));
        byte[] dest = new byte[6];
        byteBufferRam.copyToDestination(0, dest, -1, 2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidTarget2() throws IOException {
        ByteBufferRam byteBufferRam = new ByteBufferRam(
                ByteBuffer.wrap("ABBA-DOMINO-78".getBytes(StandardCharsets.UTF_8)));
        byte[] dest = new byte[6];
        byteBufferRam.copyToDestination(0, dest, 0, 12);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidTarget3() throws IOException {
        ByteBufferRam byteBufferRam = new ByteBufferRam(
                ByteBuffer.wrap("ABBA-DOMINO-78".getBytes(StandardCharsets.UTF_8)));
        byte[] dest = new byte[6];
        byteBufferRam.copyToDestination(0, dest, 6, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidTarget4() throws IOException {
        ByteBufferRam byteBufferRam = new ByteBufferRam(
                ByteBuffer.wrap("ABBA-DOMINO-78".getBytes(StandardCharsets.UTF_8)));
        byte[] dest = new byte[6];
        byteBufferRam.copyToDestination(-1, dest, 6, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidTarget5() throws IOException {
        ByteBufferRam byteBufferRam = new ByteBufferRam(
                ByteBuffer.wrap("ABBA-DOMINO-78".getBytes(StandardCharsets.UTF_8)));
        byte[] dest = new byte[6];
        byteBufferRam.copyToDestination(20, dest, 6, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidTarget6() throws IOException {
        ByteBufferRam byteBufferRam = new ByteBufferRam(
                ByteBuffer.wrap("ABBA-DOMINO-78".getBytes(StandardCharsets.UTF_8)));
        byte[] dest = new byte[6];
        byteBufferRam.copyToDestination(Integer.MAX_VALUE + 1L, dest, 6, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidTarget7() throws IOException {
        ByteBufferRam byteBufferRam = new ByteBufferRam(
                ByteBuffer.wrap("ABBA-DOMINO-78".getBytes(StandardCharsets.UTF_8)));
        byte[] dest = new byte[6];
        byteBufferRam.copyToDestination(12, dest, 6, 3);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStringBufferTarget() throws IOException {
        ByteBufferRam byteBufferRam = new ByteBufferRam(
                ByteBuffer.wrap("ABBA-DOMINO-78".getBytes(StandardCharsets.UTF_8)));
        byteBufferRam.copyToDestination(0, 0, new StringBuffer());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStringBufferSource() throws IOException {
        ByteBufferRam byteBufferRam = new ByteBufferRam(
                ByteBuffer.wrap("ABBA-DOMINO-78".getBytes(StandardCharsets.UTF_8)));
        byteBufferRam.copyFromSource(0, new StringBuffer());
    }

    @Test
    public void putByte() throws IOException {
        ByteBufferRam byteBufferRam = new ByteBufferRam(ByteBuffer.wrap("ABBA-DOMINO-78".getBytes(StandardCharsets.UTF_8)));
        byteBufferRam.putByteAt(0, (byte) '+');
        assertThat(byteBufferRam.getByteAt(0), is((byte) '+'));
    }

    @Test
    public void putByte2() throws IOException {
        ByteBufferRam byteBufferRam = new ByteBufferRam(ByteBuffer.wrap("ABBA-DOMINO-78".getBytes(StandardCharsets.UTF_8)));
        byteBufferRam.putByteAt(13, (byte) '+');
        assertThat(byteBufferRam.getByteAt(13), is((byte) '+'));
    }

    @Test
    public void copyFromBytes() throws IOException {
        ByteBufferRam byteBufferRam = new ByteBufferRam(ByteBuffer.wrap("ABBA-DOMINO-78".getBytes(StandardCharsets.UTF_8)));
        byteBufferRam.copyFromSource(0, "OLDI".getBytes(StandardCharsets.UTF_8), 0, 4);
        assertThat(byteBufferRam.getBytesAt(0L, 4), is("OLDI".getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    public void copyFromByteBuffer() throws IOException {
        ByteBufferRam byteBufferRam = new ByteBufferRam(ByteBuffer.wrap("ABBA-DOMINO-78".getBytes(StandardCharsets.UTF_8)));
        ByteBuffer source = ByteBuffer.wrap("ANTIANTI".getBytes(StandardCharsets.UTF_8));

        assertThat(byteBufferRam.supportsSource(ByteBuffer.class), is(true));

        byteBufferRam.copyFromSource(0, source.limit(6).position(2));
        assertThat(byteBufferRam.getBytesAt(0L, 5), is("TIAN-".getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    public void copyFromInputStream() throws IOException {
        ByteBufferRam byteBufferRam = new ByteBufferRam(ByteBuffer.wrap("ABBA-DOMINO-78".getBytes(StandardCharsets.UTF_8)));
        ByteArrayInputStream source = new ByteArrayInputStream("TINA".getBytes(StandardCharsets.UTF_8));
        assertThat(byteBufferRam.supportsSource(InputStream.class), is(true));

        byteBufferRam.copyFromSource(0, source);
        assertThat(byteBufferRam.getBytesAt(0L, 5), is("TINA-".getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    public void copyFromReadableChannel() throws IOException {
        ByteBufferRam byteBufferRam = new ByteBufferRam(ByteBuffer.wrap("ABBA-DOMINO-78".getBytes(StandardCharsets.UTF_8)));
        ByteArrayInputStream stream = new ByteArrayInputStream("TINA".getBytes(StandardCharsets.UTF_8));
        ReadableByteChannel source = Channels.newChannel(stream);
        assertThat(byteBufferRam.supportsSource(ReadableByteChannel.class), is(true));

        byteBufferRam.copyFromSource(0, source);
        assertThat(byteBufferRam.getBytesAt(0L, 5), is("TINA-".getBytes(StandardCharsets.UTF_8)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidRead() throws IOException {
        ByteBufferRam byteBufferRam = new ByteBufferRam(ByteBuffer.wrap("ABBA-DOMINO-78".getBytes(StandardCharsets.UTF_8)));
        byteBufferRam.copyFromSource(-1, "OLDI".getBytes(StandardCharsets.UTF_8), 0, 4);
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidRead2() throws IOException {
        ByteBufferRam byteBufferRam = new ByteBufferRam(ByteBuffer.wrap("ABBA-DOMINO-78".getBytes(StandardCharsets.UTF_8)));
        byteBufferRam.copyFromSource(30, "OLDI".getBytes(StandardCharsets.UTF_8), 0, 4);
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidRead3() throws IOException {
        ByteBufferRam byteBufferRam = new ByteBufferRam(ByteBuffer.wrap("ABBA-DOMINO-78".getBytes(StandardCharsets.UTF_8)));
        byteBufferRam.copyFromSource(Integer.MAX_VALUE + 1L, "OLDI".getBytes(StandardCharsets.UTF_8), 0, 4);
    }

    @Test
    public void testStringBufferTargetNotSupported() throws IOException {
        ByteBufferRam byteBufferRam = new ByteBufferRam(
                ByteBuffer.wrap("ABBA-DOMINO-78".getBytes(StandardCharsets.UTF_8)));
        assertThat(byteBufferRam.supportsDestination(StringBuffer.class), is(false));
    }

    @Test
    public void testStringBufferSourceNotSupported() throws IOException {
        ByteBufferRam byteBufferRam = new ByteBufferRam(
                ByteBuffer.wrap("ABBA-DOMINO-78".getBytes(StandardCharsets.UTF_8)));
        assertThat(byteBufferRam.supportsSource(StringBuffer.class), is(false));
    }

    @Test
    public void getPutUByte() throws IOException {
        ByteBuffer b = getByteBuffer();
        ByteBufferRam byteBufferRam = new ByteBufferRam(b);
        byteBufferRam.putUByteAt(0, (short) 254);
        byteBufferRam.putUByteAt(10, (short) 0);
        assertThat(byteBufferRam.getUByteAt(0), is((short) 254));
        assertThat(byteBufferRam.getUByteAt(10), is((short) 0));
    }

    @Test
    public void getPutInt16() throws IOException {
        ByteBuffer b = getByteBuffer();
        ByteBufferRam byteBufferRam = new ByteBufferRam(b);
        byteBufferRam.putInt16At(0, Short.MIN_VALUE);
        byteBufferRam.putInt16At(10, Short.MAX_VALUE);
        assertThat(byteBufferRam.getInt16At(0), is(Short.MIN_VALUE));
        assertThat(byteBufferRam.getInt16At(10), is(Short.MAX_VALUE));
    }

    @Test
    public void getPutUInt16() throws IOException {
        ByteBuffer b = getByteBuffer();
        ByteBufferRam byteBufferRam = new ByteBufferRam(b);
        byteBufferRam.putUInt16At(0, Short.MAX_VALUE + 1);
        byteBufferRam.putUInt16At(10, 0);
        assertThat(byteBufferRam.getUInt16At(0), is(32768));
        assertThat(byteBufferRam.getUInt16At(10), is(0));
    }

    @Test
    public void getPutInt32() throws IOException {
        ByteBuffer b = getByteBuffer();
        ByteBufferRam byteBufferRam = new ByteBufferRam(b);
        byteBufferRam.putInt32At(0, Integer.MAX_VALUE);
        byteBufferRam.putInt32At(10, Integer.MIN_VALUE);
        assertThat(byteBufferRam.getInt32At(0), is(Integer.MAX_VALUE));
        assertThat(byteBufferRam.getInt32At(10), is(Integer.MIN_VALUE));
    }

    @Test
    public void getPutUInt32() throws IOException {
        ByteBuffer b = getByteBuffer();
        ByteBufferRam byteBufferRam = new ByteBufferRam(b);
        byteBufferRam.putUInt32At(0, Integer.MAX_VALUE + 1L);
        byteBufferRam.putUInt32At(10, 0);
        assertThat(byteBufferRam.getUInt32At(0), is(2147483648L));
        assertThat(byteBufferRam.getUInt32At(10), is(0L));
    }

    @Test
    public void getPutInt64() throws IOException {
        ByteBuffer b = getByteBuffer();
        ByteBufferRam byteBufferRam = new ByteBufferRam(b);
        byteBufferRam.putInt64At(0, Long.MAX_VALUE);
        byteBufferRam.putInt64At(10, Long.MIN_VALUE);
        assertThat(byteBufferRam.getInt64At(0), is(Long.MAX_VALUE));
        assertThat(byteBufferRam.getInt64At(10), is(Long.MIN_VALUE));
    }

    @Test
    public void getUInt64() throws IOException {
        ByteBuffer b = getByteBuffer();
        ByteBufferRam byteBufferRam = new ByteBufferRam(b);
        byteBufferRam.putUInt64At(0, new ULong(Long.MAX_VALUE));
        byteBufferRam.putUInt64At(10, new ULong(0L));
        assertThat(byteBufferRam.getUInt64At(0), is(new ULong(Long.MAX_VALUE)));
        assertThat(byteBufferRam.getUInt64At(10), is(new ULong(0L)));
    }

    @Test
    public void getFloat() throws IOException {
        ByteBuffer b = getByteBuffer();
        b.putFloat(0, 1.0f);
        b.putFloat(10, 1.0f);
        ByteBufferRam byteBufferRam = new ByteBufferRam(b);
        assertThat(byteBufferRam.getFloatAt(0), is(1.0f));
        assertThat(byteBufferRam.getFloatAt(10), is(1.0f));
    }

    @Test
    public void getDouble() throws IOException {
        ByteBuffer b = getByteBuffer();
        b.putDouble(0, 1.0);
        b.putDouble(10, 1.0);
        ByteBufferRam byteBufferRam = new ByteBufferRam(b);
        assertThat(byteBufferRam.getDoubleAt(0), is(1.0));
        assertThat(byteBufferRam.getDoubleAt(10), is(1.0));
    }

    @Test
    public void getString() throws IOException {
        ByteBuffer b = getByteBuffer();
        ByteBufferRam byteBufferRam = new ByteBufferRam(b);
        assertThat(byteBufferRam.getStringAt(80, 13), is("ABCDEFGHIJKLM"));
        assertThat(byteBufferRam.getStringAt(82, 9), is("CDEFGHIJK"));
    }

    @Test
    public void getData() throws IOException {
        ByteBuffer b = getByteBuffer();
        ByteBufferRam byteBufferRam = new ByteBufferRam(b);
        assertThat(byteBufferRam.getDataAt(80, 13).getBytes(), is("ABCDEFGHIJKLM".getBytes(StandardCharsets.UTF_8)));
        assertThat(byteBufferRam.getDataAt(82, 9).getBytes(), is("CDEFGHIJK".getBytes(StandardCharsets.UTF_8)));
    }

    private ByteBuffer getByteBuffer() {
        ByteBuffer b = ByteBuffer.allocate(100);
        for (int i = 0; i < 80; i++) {
            b.put((byte) 0xff);
        }
        b.put("ABCDEFGHIJKLM".getBytes(StandardCharsets.UTF_8));
        return b;
    }
}

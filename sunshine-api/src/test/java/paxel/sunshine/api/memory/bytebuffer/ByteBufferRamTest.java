package paxel.sunshine.api.memory.bytebuffer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

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

import org.junit.Test;

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
    public void getBytesAt() throws IOException {
        ByteBufferRam byteBufferRam = new ByteBufferRam(ByteBuffer.wrap("ABBA-DOMINO-78".getBytes(StandardCharsets.UTF_8)));
        byte[] bytesAt = byteBufferRam.getBytesAt(12, 2);

        assertThat(new String(bytesAt), is("78"));
    }

    @Test
    public void getBytes() throws IOException {
        ByteBufferRam byteBufferRam = new ByteBufferRam(ByteBuffer.wrap("ABBA-DOMINO-78".getBytes(StandardCharsets.UTF_8)));
        byte[] dest = new byte[4];
        byteBufferRam.copyToDestination(0, dest);
        assertThat(new String(dest), is("ABBA"));
    }

    @Test
    public void getBytes2() throws IOException {
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


}
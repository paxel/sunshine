package paxel.sunshine.api.memory;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class ByteBufferRoRamTest {

    @Test
    public void writeToByteBuffer() throws IOException {
        ByteBufferRoRam byteBufferRam = new ByteBufferRoRam(ByteBuffer.wrap("ABBA-DOMINO-78".getBytes(StandardCharsets.UTF_8)));
        ByteBuffer dst = ByteBuffer.allocate(6);

        assertThat(byteBufferRam.supportsSink(ByteBuffer.class), is(true));
        long written = byteBufferRam.writeBytesToDest(5, 6, dst);

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
        ByteBufferRoRam byteBufferRam = new ByteBufferRoRam(ByteBuffer.wrap("ABBA-DOMINO-78".getBytes(StandardCharsets.UTF_8)));
        ByteArrayOutputStream dst = new ByteArrayOutputStream();

        assertThat(byteBufferRam.supportsSink(OutputStream.class), is(true));
        long written = byteBufferRam.writeBytesToDest(5, 6, dst);
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
        ByteBufferRoRam byteBufferRam = new ByteBufferRoRam(ByteBuffer.wrap("ABBA-DOMINO-78".getBytes(StandardCharsets.UTF_8)));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        WritableByteChannel dst = Channels.newChannel(out);
        assertThat(byteBufferRam.supportsSink(WritableByteChannel.class), is(true));
        long written = byteBufferRam.writeBytesToDest(5, 6, dst);
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
        ByteBufferRoRam byteBufferRam = new ByteBufferRoRam(ByteBuffer.wrap("ABBA-DOMINO-78".getBytes(StandardCharsets.UTF_8)));
        assertThat(byteBufferRam.size(), is(14L));
        assertThat(byteBufferRam.getByteAt(4), is((byte) '-'));
        assertThat(byteBufferRam.getByteAt(11), is((byte) '-'));
    }

    @Test
    public void getBytesAt() throws IOException {
        ByteBufferRoRam byteBufferRam = new ByteBufferRoRam(ByteBuffer.wrap("ABBA-DOMINO-78".getBytes(StandardCharsets.UTF_8)));
        byte[] bytesAt = byteBufferRam.getBytesAt(12, 2);

        assertThat(new String(bytesAt), is("78"));
    }

    @Test
    public void getBytes() throws IOException {
        ByteBufferRoRam byteBufferRam = new ByteBufferRoRam(ByteBuffer.wrap("ABBA-DOMINO-78".getBytes(StandardCharsets.UTF_8)));
        byte[] dest = new byte[4];
        byteBufferRam.copyToDest(0, dest);
        assertThat(new String(dest), is("ABBA"));
    }

    @Test
    public void getBytes2() throws IOException {
        ByteBufferRoRam byteBufferRam = new ByteBufferRoRam(ByteBuffer.wrap("ABBA-DOMINO-78".getBytes(StandardCharsets.UTF_8)));
        byte[] dest = new byte[4];
        byteBufferRam.copyToDest(7, dest);
        assertThat(new String(dest), is("MINO"));
    }


    @Test
    public void getBytesLength() throws IOException {
        ByteBufferRoRam byteBufferRam = new ByteBufferRoRam(ByteBuffer.wrap("ABBA-DOMINO-78".getBytes(StandardCharsets.UTF_8)));
        byte[] dest = new byte[6];
        byteBufferRam.copyToDest(0, dest, 0, 2);
        byteBufferRam.copyToDest(5, dest, 2, 2);
        byteBufferRam.copyToDest(12, dest, 4, 2);
        assertThat(new String(dest), is("ABDO78"));
    }

}
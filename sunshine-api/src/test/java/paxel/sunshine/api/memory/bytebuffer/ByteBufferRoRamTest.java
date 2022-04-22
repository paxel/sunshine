package paxel.sunshine.api.memory.bytebuffer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.StandardCharsets;

import org.junit.Test;

import paxel.sunshine.api.datatypes.ULong;

public class ByteBufferRoRamTest {

	@Test
	public void writeToByteBuffer() throws IOException {
		ByteBufferRoRam byteBufferRam = new ByteBufferRoRam(
				ByteBuffer.wrap("ABBA-DOMINO-78".getBytes(StandardCharsets.UTF_8)));
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
		ByteBufferRoRam byteBufferRam = new ByteBufferRoRam(
				ByteBuffer.wrap("ABBA-DOMINO-78".getBytes(StandardCharsets.UTF_8)));
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
		ByteBufferRoRam byteBufferRam = new ByteBufferRoRam(
				ByteBuffer.wrap("ABBA-DOMINO-78".getBytes(StandardCharsets.UTF_8)));
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
		ByteBufferRoRam byteBufferRam = new ByteBufferRoRam(
				ByteBuffer.wrap("ABBA-DOMINO-78".getBytes(StandardCharsets.UTF_8)));
		assertThat(byteBufferRam.size(), is(14L));
		assertThat(byteBufferRam.getByteAt(4), is((byte) '-'));
		assertThat(byteBufferRam.getByteAt(11), is((byte) '-'));
	}

	@Test
	public void getBytesAt() throws IOException {
		ByteBufferRoRam byteBufferRam = new ByteBufferRoRam(
				ByteBuffer.wrap("ABBA-DOMINO-78".getBytes(StandardCharsets.UTF_8)));
		byte[] bytesAt = byteBufferRam.getBytesAt(12, 2);

		assertThat(new String(bytesAt), is("78"));
	}

	@Test
	public void getBytes() throws IOException {
		ByteBufferRoRam byteBufferRam = new ByteBufferRoRam(
				ByteBuffer.wrap("ABBA-DOMINO-78".getBytes(StandardCharsets.UTF_8)));
		byte[] dest = new byte[4];
		byteBufferRam.copyToDestination(0, dest);
		assertThat(new String(dest), is("ABBA"));
	}

	@Test
	public void getBytes2() throws IOException {
		ByteBufferRoRam byteBufferRam = new ByteBufferRoRam(
				ByteBuffer.wrap("ABBA-DOMINO-78".getBytes(StandardCharsets.UTF_8)));
		byte[] dest = new byte[4];
		byteBufferRam.copyToDestination(7, dest);
		assertThat(new String(dest), is("MINO"));
	}

	@Test
	public void getBytesLength() throws IOException {
		ByteBufferRoRam byteBufferRam = new ByteBufferRoRam(
				ByteBuffer.wrap("ABBA-DOMINO-78".getBytes(StandardCharsets.UTF_8)));
		byte[] dest = new byte[6];
		byteBufferRam.copyToDestination(0, dest, 0, 2);
		byteBufferRam.copyToDestination(5, dest, 2, 2);
		byteBufferRam.copyToDestination(12, dest, 4, 2);
		assertThat(new String(dest), is("ABDO78"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInvalidTarget() throws IOException {
		ByteBufferRoRam byteBufferRam = new ByteBufferRoRam(
				ByteBuffer.wrap("ABBA-DOMINO-78".getBytes(StandardCharsets.UTF_8)));
		byte[] dest = new byte[6];
		byteBufferRam.copyToDestination(0, dest, -1, 2);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInvalidTarget2() throws IOException {
		ByteBufferRoRam byteBufferRam = new ByteBufferRoRam(
				ByteBuffer.wrap("ABBA-DOMINO-78".getBytes(StandardCharsets.UTF_8)));
		byte[] dest = new byte[6];
		byteBufferRam.copyToDestination(0, dest, 0, 12);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInvalidTarget3() throws IOException {
		ByteBufferRoRam byteBufferRam = new ByteBufferRoRam(
				ByteBuffer.wrap("ABBA-DOMINO-78".getBytes(StandardCharsets.UTF_8)));
		byte[] dest = new byte[6];
		byteBufferRam.copyToDestination(0, dest, 6, 1);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInvalidTarget4() throws IOException {
		ByteBufferRoRam byteBufferRam = new ByteBufferRoRam(
				ByteBuffer.wrap("ABBA-DOMINO-78".getBytes(StandardCharsets.UTF_8)));
		byte[] dest = new byte[6];
		byteBufferRam.copyToDestination(-1, dest, 6, 1);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInvalidTarget5() throws IOException {
		ByteBufferRoRam byteBufferRam = new ByteBufferRoRam(
				ByteBuffer.wrap("ABBA-DOMINO-78".getBytes(StandardCharsets.UTF_8)));
		byte[] dest = new byte[6];
		byteBufferRam.copyToDestination(20, dest, 6, 1);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInvalidTarget6() throws IOException {
		ByteBufferRoRam byteBufferRam = new ByteBufferRoRam(
				ByteBuffer.wrap("ABBA-DOMINO-78".getBytes(StandardCharsets.UTF_8)));
		byte[] dest = new byte[6];
		byteBufferRam.copyToDestination(Integer.MAX_VALUE + 1L, dest, 6, 1);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInvalidTarget7() throws IOException {
		ByteBufferRoRam byteBufferRam = new ByteBufferRoRam(
				ByteBuffer.wrap("ABBA-DOMINO-78".getBytes(StandardCharsets.UTF_8)));
		byte[] dest = new byte[6];
		byteBufferRam.copyToDestination(12, dest, 6, 3);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testStringBufferTarget() throws IOException {
		ByteBufferRoRam byteBufferRam = new ByteBufferRoRam(
				ByteBuffer.wrap("ABBA-DOMINO-78".getBytes(StandardCharsets.UTF_8)));
		byte[] dest = new byte[6];
		byteBufferRam.copyToDestination(0, 0, new StringBuffer());
	}

	@Test
	public void testStringBufferTargetNotSupported() throws IOException {
		ByteBufferRoRam byteBufferRam = new ByteBufferRoRam(
				ByteBuffer.wrap("ABBA-DOMINO-78".getBytes(StandardCharsets.UTF_8)));
		assertThat(byteBufferRam.supportsDestination(StringBuffer.class), is(false));
	}

	@Test
	public void getUByte() throws IOException {
		ByteBuffer b = getByteBuffer();
		ByteBufferRoRam byteBufferRam = new ByteBufferRoRam(b);
		assertThat(byteBufferRam.getUByteAt(0), is((short) 0xff));
		assertThat(byteBufferRam.getUByteAt(10), is((short) 0xff));
	}

	@Test
	public void getInt16() throws IOException {
		ByteBuffer b = getByteBuffer();
		ByteBufferRoRam byteBufferRam = new ByteBufferRoRam(b);
		assertThat(byteBufferRam.getInt16At(0), is((short) 0xffff));
		assertThat(byteBufferRam.getInt16At(10), is((short) 0xffff));
	}

	@Test
	public void getUInt16() throws IOException {
		ByteBuffer b = getByteBuffer();
		ByteBufferRoRam byteBufferRam = new ByteBufferRoRam(b);
		assertThat(byteBufferRam.getUInt16At(0), is(0xffff));
		assertThat(byteBufferRam.getUInt16At(10), is(0xffff));
	}

	@Test
	public void getInt32() throws IOException {
		ByteBuffer b = getByteBuffer();
		ByteBufferRoRam byteBufferRam = new ByteBufferRoRam(b);
		assertThat(byteBufferRam.getInt32At(0), is(0xffff_ffff));
		assertThat(byteBufferRam.getInt32At(10), is(0xffff_ffff));
	}

	@Test
	public void getUInt32() throws IOException {
		ByteBuffer b = getByteBuffer();
		ByteBufferRoRam byteBufferRam = new ByteBufferRoRam(b);
		assertThat(byteBufferRam.getUInt32At(0), is(0xffff_ffffL));
		assertThat(byteBufferRam.getUInt32At(10), is(0xffff_ffffL));
	}

	@Test
	public void getInt64() throws IOException {
		ByteBuffer b = getByteBuffer();
		ByteBufferRoRam byteBufferRam = new ByteBufferRoRam(b);
		assertThat(byteBufferRam.getInt64At(0), is(0xffff_ffffffff_ffffL));
		assertThat(byteBufferRam.getInt64At(10), is(0xffff_ffffffff_ffffL));
	}

	@Test
	public void getUInt64() throws IOException {
		ByteBuffer b = getByteBuffer();
		ByteBufferRoRam byteBufferRam = new ByteBufferRoRam(b);
		assertThat(byteBufferRam.getUInt64At(0), is(new ULong(0xffff_ffffffff_ffffL)));
		assertThat(byteBufferRam.getUInt64At(10), is(new ULong(0xffff_ffffffff_ffffL)));
	}

	@Test
	public void getFloat() throws IOException {
		ByteBuffer b = getByteBuffer();
		b.putFloat(0, 1.0f);
		b.putFloat(10, 1.0f);
		ByteBufferRoRam byteBufferRam = new ByteBufferRoRam(b);
		assertThat(byteBufferRam.getFloatAt(0), is(1.0f));
		assertThat(byteBufferRam.getFloatAt(10), is(1.0f));
	}

	@Test
	public void getDouble() throws IOException {
		ByteBuffer b = getByteBuffer();
		b.putDouble(0, 1.0);
		b.putDouble(10, 1.0);
		ByteBufferRoRam byteBufferRam = new ByteBufferRoRam(b);
		assertThat(byteBufferRam.getDoubleAt(0), is(1.0));
		assertThat(byteBufferRam.getDoubleAt(10), is(1.0));
	}

	@Test
	public void getString() throws IOException {
		ByteBuffer b = getByteBuffer();
		ByteBufferRoRam byteBufferRam = new ByteBufferRoRam(b);
		assertThat(byteBufferRam.getStringAt(80, 13), is("ABCDEFGHIJKLM"));
		assertThat(byteBufferRam.getStringAt(82, 9), is("CDEFGHIJK"));
	}

	@Test
	public void getData() throws IOException {
		ByteBuffer b = getByteBuffer();
		ByteBufferRoRam byteBufferRam = new ByteBufferRoRam(b);
		assertThat(byteBufferRam.getDataAt(80, 13).getBytes(), is("ABCDEFGHIJKLM".getBytes(StandardCharsets.UTF_8)));
		assertThat(byteBufferRam.getDataAt(82, 9).getBytes(), is("CDEFGHIJK".getBytes(StandardCharsets.UTF_8)));
	}

	private ByteBuffer getByteBuffer() {
		ByteBuffer b = ByteBuffer.allocate(100);
		for (int i = 0; i < 80; i++)
			b.put((byte) 0xff);
		b.put("ABCDEFGHIJKLM".getBytes(StandardCharsets.UTF_8));
		return b;
	}
}
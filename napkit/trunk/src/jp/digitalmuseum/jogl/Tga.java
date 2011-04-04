package jp.digitalmuseum.jogl;

import java.io.*;
import java.nio.*;
import java.net.*;

/**
 * Class for loading TGA image file.
 *
 * @author Jun KATO
 * @see <a href="http://paulbourke.net/dataformats/tga/">http://paulbourke.net/dataformats/tga/</a>
 */
public class Tga {

	private Header header = null;

	private byte[] data = null;

	/**
	 * Screen origin bit.
	 * <dl>
	 * 	<dt>0</dt><dd>Origin in lower left-hand corner.</dd>
	 * 	<dt>1</dt><dd>Origin in upper left-hand corner.</dd>
	 * </dl>
	 */
	private boolean screenOriginBit = true;

	public Tga(URL url) throws IOException {
		BufferedInputStream in;
		try {
			in = new BufferedInputStream(url.openStream());
		} catch (IOException e) {
			// File not found.
			throw e;
		}

		byte[] headerInfo = new byte[18];
		try {
			read(in, headerInfo);
		} catch (IOException e) {
			// Failed to read header.
			throw e;
		}

		ByteBuffer bb = ByteBuffer.wrap(headerInfo);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		header = new Header();
		header.id = bb.get();
		header.hasColorMap = bb.get();
		header.type = bb.get();
		header.colorMapOrigin = bb.getShort();
		header.colorMapLength = bb.getShort();
		header.colorMapEntrySize = bb.get();
		header.x = bb.getShort();
		header.y = bb.getShort();
		header.width = bb.getShort();
		header.height = bb.getShort();
		header.bitDepth = bb.get();
		header.imageDescriptorByte = bb.get();
		if ((header.imageDescriptorByte & 0x02) != 0) {
			screenOriginBit = false;
		}

		data = new byte[header.width * header.height * (header.bitDepth / 8)];
		try {
			read(in, data);
		} catch (IOException e) {
			// Failed to read image data.
			throw e;
		}
	}

	private int read(InputStream in, byte[] data) throws IOException {
		int sz = 0;
		while (sz < data.length) {
			int read = in.read(data, sz, data.length - sz);
			if (read < 0) {
				if (sz == 0) {
					return -1;
				}
				break;
			}
			sz += read;
		}
		return sz;
	}

	public byte[] getDataReference() {
		return data;
	}

	public int getWidth() {
		return header.width;
	}

	public int getHeight() {
		return header.height;
	}

	public boolean isVerticallyInverted() {
		return !screenOriginBit;
	}

	/**
	 * TGA file header.
	 */
	@SuppressWarnings("unused")
	private static class Header {
		byte id;
		byte hasColorMap;
		byte type;
		short colorMapOrigin;
		short colorMapLength;
		byte colorMapEntrySize;
		short x;
		short y;
		short width;
		short height;
		byte bitDepth;
		byte imageDescriptorByte;
	}
}

package jp.digitalmuseum.utils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Arrays;

public class MixedDataReader {
	private static final String defaultCharset = "Shift_JIS";
	private BufferedInputStream bis = null ;
	private Charset charset;

	public MixedDataReader(InputStream is) {
		this(is, defaultCharset);
	}

	public MixedDataReader(InputStream is, String charsetName) {
		this(is, Charset.forName(charsetName));
	}

	public MixedDataReader(InputStream is, Charset charset) {
		bis = new BufferedInputStream(is) ;
		this.charset = charset;
	}

	public int read(byte[] b) throws IOException {
		int sz = 0;
		while (sz < b.length) {
			int read = bis.read(b, sz, b.length - sz);
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

	public String readLine() throws IOException {
		byte[] buf = new byte[256];
		int sz = 0;
		int ch;
		while(true) {
			ch = bis.read();
			if (ch == '\n' || ch == '\r' || ch < 0) {
				break;
			}
			buf[sz ++] = (byte) ch;
			if (sz == buf.length) {
				buf = Arrays.copyOf(buf, sz + 128);
			}
		}

		if (ch == '\r') {
			bis.mark(1);
			int nch = bis.read();
			if (nch != '\n') {
				bis.reset();
			}
		} else if (ch < 0 && sz == 0) {
			return null;
		}

		return new String(Arrays.copyOf(buf, sz), charset);
	}

	public void close() throws IOException {
		bis.close() ;
	}
}

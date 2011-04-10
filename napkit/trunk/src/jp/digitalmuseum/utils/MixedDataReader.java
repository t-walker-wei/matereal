/*
 * PROJECT: napkit at http://mr.digitalmuseum.jp/
 * ----------------------------------------------------------------------------
 *
 * This file is part of NyARToolkit Application Toolkit.
 *
 * NyARToolkit Application Toolkit, or simply "napkit",
 * is a simple wrapper library for NyARToolkit.
 *
 * ----------------------------------------------------------------------------
 *
 * License version: GPL 3.0
 *
 * napkit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * napkit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with napkit. If not, see <http://www.gnu.org/licenses/>.
 */
package jp.digitalmuseum.utils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * Class for reading InputStream which contains mixed data of binary and text.
 */
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

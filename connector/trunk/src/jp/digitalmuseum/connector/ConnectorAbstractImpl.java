/*
 * PROJECT: connector at http://digitalmuseum.jp/en/software/
 * ----------------------------------------------------------------------------
 *
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is connector.
 *
 * The Initial Developer of the Original Code is Jun Kato.
 * Portions created by the Initial Developer are
 * Copyright (C) 2009 Jun Kato. All Rights Reserved.
 *
 * Contributor(s): Jun Kato
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either of the GNU General Public License Version 2 or later (the "GPL"),
 * or the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 */
package jp.digitalmuseum.connector;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public abstract class ConnectorAbstractImpl implements Connector {
	private static final long serialVersionUID = -6942192285671509229L;
	private transient OutputStream outputStream;
	private transient InputStream inputStream;

	public void disconnect() {
		outputStream = null;
		inputStream = null;
	}

	protected void setOutputStream(OutputStream outputStream) {
		this.outputStream = outputStream;
	}

	public OutputStream getOutputStream() {
		return outputStream;
	}

	protected void setInputStream(InputStream inputStream) {
		this.inputStream = inputStream;
	}

	public InputStream getInputStream() {
		return inputStream;
	}

	public boolean write(String s) {
		if (s == null) {
			return false;
		}

		// Write as a byte array.
		return write(s.getBytes());
	}

	public boolean write(byte b) {
		try {
			if (outputStream == null) {
				return false;
			}
			outputStream.write(b);
			outputStream.flush();

			// System.out.println((int)b);
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	public boolean write(int i) {
		try {
			if (outputStream == null) {
				return false;
			}
			outputStream.write(i);
			outputStream.flush();

			// System.out.println(i);
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	public boolean write(byte[] byteArray) {
		try {
			if (outputStream == null) {
				return false;
			}
			outputStream.write(byteArray);
			outputStream.flush();

			// for (byte b : byteArray) {
			//	System.out.println((int)b);
			// }
			// System.out.println(new String(byteArray));
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	public int read() {
		try {
			if (inputStream == null) {
				return -1;
			}
			return getInputStream().read();
		} catch (IOException e) {
			return -1;
		}
	}

	public int read(byte[] data) {
		try {
			if (inputStream == null) {
				return -1;
			}
			return getInputStream().read(data);
		} catch (IOException e) {
			return -1;
		}
	}

	public int read(byte[] data, int off, int len) {
		try {
			if (inputStream == null) {
				return -1;
			}
			return getInputStream().read(data, off, len);
		} catch (IOException e) {
			return -1;
		}
	}

	public int readAll(byte[] data) {
		try {
			if (inputStream == null) {
				return -1;
			}

			int read = 0;
			int off = 0;
			while ((read = getInputStream().read(data, off, data.length - off)) > 0) {
				off += read;
				waitForResponse();
			}
			return off;
		} catch (IOException e) {
			return -1;
		}
	}

	public boolean waitForResponse() {
		return waitForResponse(200);
	}

	public boolean waitForResponse(int ms) {
		try {
			int max = ms / 10;
			int count = 0;
			while (inputStream.available() <= 0 && count < max) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					count = max - 1;
					break;
				}
				count ++;
			}
			return count < max;
		} catch (IOException e) {
			return false;
		}
	}
}

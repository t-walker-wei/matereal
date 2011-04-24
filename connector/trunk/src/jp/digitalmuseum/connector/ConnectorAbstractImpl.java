/*
 * PROJECT: connector at http://mr.digitalmuseum.jp/
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
 * The Initial Developer of the Original Code is Jun KATO.
 * Portions created by the Initial Developer are
 * Copyright (C) 2009 Jun KATO. All Rights Reserved.
 *
 * Contributor(s): Jun KATO
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
		if (outputStream == null) {
			return false;
		}

		try {
			outputStream.write(b);
			outputStream.flush();

			// System.out.println((int)b);

		} catch (IOException e) {
			return false;
		}
		return true;
	}

	public boolean write(int i) {
		if (outputStream == null) {
			return false;
		}

		try {
			outputStream.write(i);
			outputStream.flush();

			// System.out.println(i);

		} catch (IOException e) {
			return false;
		}
		return true;
	}

	public boolean write(byte[] byteArray) {
		if (!isConnected()) {
			connect();
		}
		if (outputStream == null) {
			return false;
		}

		try {
			outputStream.write(byteArray);
			outputStream.flush();

			// for (byte b : byteArray) {
			//	System.out.println((int)b);
			// }

		} catch (IOException e) {
			return false;
		}

		// System.out.println(new String(byteArray));
		return true;
	}

	public int readInt() throws IOException {
		return getInputStream().read();
	}
}

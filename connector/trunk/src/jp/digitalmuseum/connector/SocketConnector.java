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
import java.net.Socket;


public final class SocketConnector extends ConnectorAbstractImpl {
	private static final long serialVersionUID = -2742909372817735417L;
	final public static String CON_PREFIX2 = "http://";
	final public static String CON_PREFIX = "tcp:";
	final public static int DEFAULT_PORT = 80;
	private transient Socket socket;
	private String host;
	private int port;

	public SocketConnector(String con) {
		parseConnectionString(con);
		if (host == null) {
			throw new IllegalArgumentException();
		}
	}

	private void parseConnectionString(String con) {

		// Remove prefix.
		if (con.toLowerCase().startsWith(CON_PREFIX)) {
			parseConnectionString(con.substring(CON_PREFIX.length()));
			return;
		}
		if (con.toLowerCase().startsWith(CON_PREFIX2)) {
			parseConnectionString(con.substring(CON_PREFIX2.length()));
			return;
		}

		// Check port.
		final String[] addrs = con.split(":");
		if (addrs.length <= 2) {
			host = addrs[0];
			port = addrs.length > 1 ?
					Integer.parseInt(addrs[1]) : DEFAULT_PORT;
		}
	}

	public boolean connect() {

		if (isConnected()) {
			return true;
		}

		// Open a connection.
		try {
			socket = new Socket(host, port);
			setOutputStream(socket.getOutputStream());
			setInputStream(socket.getInputStream());
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	public void disconnect() {
		super.disconnect();
		try {
			if (isConnected()) {
				socket.close();
			}
		} catch (IOException e) {
			// Do nothing.
		}
		socket = null;
	}

	public boolean isConnected() {
		return socket != null;
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	public String getConnectionString() {
		return CON_PREFIX+host+":"+port;
	}

}

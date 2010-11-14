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

import javax.microedition.io.StreamConnection;


public final class BluetoothConnector extends ConnectorAbstractImpl {
	final public static String CON_PREFIX = "btspp://";
	private StreamConnection streamConnection;
	private String host;

	public BluetoothConnector(String con) {
		host = formatConnectionString(con);
		if (host == null) {
			throw new IllegalArgumentException();
		}
	}

	public static boolean checkConnectionString(String con) {
		return formatConnectionString(con) != null;
	}

	private static String formatConnectionString(String con) {

		// Remove prefix.
		if (con.startsWith(CON_PREFIX)) {
			return formatConnectionString(con.substring(CON_PREFIX.length()));
		}

		// Compact colon separated address.
		final String[] addrs = con.split(":");
		if (addrs.length > 1) {
			final StringBuilder builder = new StringBuilder();
			for (String addr : addrs) {
				if (is8bitAddress(addr)) {
					return null;
				}
				builder.append(addr);
			}
			return formatConnectionString(builder.toString());
		}

		// Check address.
		if (!is96bitAddress(con)) {
			return null;
		}
		return con;
	}

	private static boolean is8bitAddress(String addr) {
		return (addr.length() == 2 &&
				isHexChar(addr.charAt(0)) &&
				isHexChar(addr.charAt(1)));
	}

	private static boolean is96bitAddress(String addr) {
		if (addr.length() != 12) {
			return false;
		}
		for (int i = addr.length()-1; i >= 0; i --) {
			if (!isHexChar(addr.charAt(i))) {
				return false;
			}
		}
		return true;
	}

	private static boolean isHexChar(char c) {
		return ((c >= '0' && c <= '9') ||
				(c >= 'a' && c <= 'f') ||
				(c >= 'A' && c <= 'F'));
	}

	public boolean connect() {

		// Open a connection.
		try {
			streamConnection = (StreamConnection)
				javax.microedition.io.Connector.open(
					getConnectionString()+":1;authenticate=false;encrypt=false;master=false",
					javax.microedition.io.Connector.READ_WRITE);
			setOutputStream(streamConnection.openOutputStream());
			setInputStream(streamConnection.openInputStream());
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	@Override
	public void disconnect() {
		super.disconnect();
		try {
			streamConnection.close();
		} catch (IOException e) {
			// Do nothing.
		}
		streamConnection = null;
	}

	public boolean isConnected() {
		return streamConnection != null;
	}

	public String getConnectionString() {
		return CON_PREFIX+host;
	}

}

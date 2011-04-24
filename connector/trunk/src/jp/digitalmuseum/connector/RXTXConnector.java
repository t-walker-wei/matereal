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

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;

public final class RXTXConnector extends ConnectorAbstractImpl {
	private static final long serialVersionUID = 7565866420854582460L;
	final public static String CON_PREFIX = "COM:";
	private String portName;
	private transient CommPort port;

	public RXTXConnector(String con) {
		parseConnectionString(con);
	}

	private void parseConnectionString(String con) {

		// Remove prefix.
		if (con.startsWith(CON_PREFIX)) {
			parseConnectionString(con.substring(CON_PREFIX.length()));
			return;
		}

		// Get port name.
		portName = con;
	}

	public boolean connect() {

		// Open the port.
		try {
			final CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
			port = portIdentifier.open("class", 2000);
			setInputStream(port.getInputStream());
			setOutputStream(port.getOutputStream());
		} catch (Exception e) {
			disconnect();
			return false;
		}
		return true;
	}

	/**
	 * @return Returns the CommPort object used for this connection.
	 */
	public CommPort getCommPort() {
		return port;
	}

	public void disconnect() {
		super.disconnect();

		if (port != null) {
			port.close();
			port = null;
		}
		setInputStream(null);
		setOutputStream(null);
	}

	public boolean isConnected() {
		return port != null;
	}

	public String getConnectionString() {
		return CON_PREFIX+portName;
	}

}

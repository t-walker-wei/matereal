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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;
import javax.microedition.io.StreamConnection;


public final class BluetoothConnector extends ConnectorAbstractImpl {
	private static final long serialVersionUID = 5420574858269993109L;
	final public static String CON_PREFIX = "btspp://";
	private StreamConnection streamConnection;
	private String host;

	public BluetoothConnector(String con) {
		host = formatConnectionString(con);
		if (host == null) {
			if (con != null) {

				// Remove prefix.
				if (con.toLowerCase().startsWith(CON_PREFIX)) {
					con = con.substring(CON_PREFIX.length());
				}

				// Look for friendly name.
				Set<RemoteDevice> devices = queryDevices();
				for (RemoteDevice device : devices) {
					try {
						final String friendlyName = device.getFriendlyName(false);
						if (con.equals(friendlyName)) {
							host = device.getBluetoothAddress();
							return;
						}
					} catch (IOException e) {
						// Do nothing and continue when connection fails.
					}
				}
			}
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

		if (isConnected()) {
			return true;
		}

		// Open a connection.
		try {
			streamConnection = (StreamConnection)
				javax.microedition.io.Connector.open(
					getConnectionString()+":1;authenticate=false;encrypt=false;master=false",
					javax.microedition.io.Connector.READ_WRITE);
			setOutputStream(streamConnection.openOutputStream());
			setInputStream(streamConnection.openInputStream());
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	@Override
	public void disconnect() {
		super.disconnect();
		if (streamConnection != null) {
			try {
					streamConnection.close();
			} catch (IOException e) {
				// Do nothing.
			}
			streamConnection = null;
		}
	}

	public boolean isConnected() {
		return streamConnection != null;
	}

	public String getConnectionString() {
		return CON_PREFIX+host;
	}

	public static Set<RemoteDevice> queryDevices() {
		final Set<RemoteDevice> devices = new HashSet<RemoteDevice>();
		try {
			LocalDevice localDevice = LocalDevice.getLocalDevice();
			System.out.println("Bluetooth Host Address: " + localDevice.getBluetoothAddress());
			System.out.println("Bluetooth Host Name: " + localDevice.getFriendlyName());
			System.out.println("Starting device discovery...");
			final DiscoveryAgent agent = localDevice.getDiscoveryAgent();
			agent.startInquiry(DiscoveryAgent.GIAC, new DiscoveryListener() {
				public void servicesDiscovered(int transID, ServiceRecord[] serviceRecords) { }
				public void serviceSearchCompleted(int transID, int responseCode) { }
				public void inquiryCompleted(int discoveryType) {
					synchronized (agent) {
						agent.notify();
					}
				}
				public void deviceDiscovered(RemoteDevice remoteDevice, DeviceClass deviceClass) {
					devices.add(remoteDevice);
				}
			});
			synchronized (agent) {
				agent.wait();
				System.out.println("Completed device discovery.");
			}
		} catch (BluetoothStateException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return devices;
	}

	public static String[] queryFriendNames() {

		// Query devices.
		final Set<RemoteDevice> devices = queryDevices();
		final ArrayList<String> friendlyNames = new ArrayList<String>();
		for (RemoteDevice device : devices) {
			try {
				String friendlyName = device.getFriendlyName(false);
				friendlyNames.add(friendlyName);
			} catch (IOException e) {
				// Do nothing and continue when connection fails.
			}
		}

		// Convert to a String array and returns it.
		String[] friendlyNamesArray = new String[friendlyNames.size()];
		friendlyNamesArray = friendlyNames.toArray(friendlyNamesArray);
		return friendlyNamesArray;
	}

	public static String[] queryIdentifiers() {

		// Query devices.
		final Set<RemoteDevice> devices = queryDevices();
		final ArrayList<String> ids = new ArrayList<String>();
		for (RemoteDevice device : devices) {
			ids.add(CON_PREFIX + device.getBluetoothAddress());
		}

		// Convert to a String array and returns it.
		String[] idsArray = new String[ids.size()];
		idsArray = ids.toArray(idsArray);
		return idsArray;
	}
}

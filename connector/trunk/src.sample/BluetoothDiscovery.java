import java.io.IOException;

import javax.bluetooth.RemoteDevice;

import jp.digitalmuseum.connector.BluetoothConnector;

public class BluetoothDiscovery {

	public static void main(String[] args) {
		for (RemoteDevice remoteDevice : BluetoothConnector.queryDevices()) {
			System.out.print(remoteDevice.getBluetoothAddress());
			System.out.print(": ");
			try {
				String friendlyName = remoteDevice.getFriendlyName(true);
				System.out.println(friendlyName);
			} catch (IOException e) {
				System.out.println(" (not available)");
				continue;
			}
		}
	}
}

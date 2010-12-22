import jp.digitalmuseum.connector.BluetoothConnector;

public class BluetoothAddressDiscovery {

	public static void main(String[] args) {
		for (String connectionString : BluetoothConnector.queryIdentifiers()) {
			System.out.println(connectionString);
		}
	}
}

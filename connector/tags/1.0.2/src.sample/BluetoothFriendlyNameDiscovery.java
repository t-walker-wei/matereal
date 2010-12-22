import jp.digitalmuseum.connector.BluetoothConnector;

public class BluetoothFriendlyNameDiscovery {

	public static void main(String[] args) {
		for (String friendlyName : BluetoothConnector.queryFriendNames()) {
			System.out.println(friendlyName);
		}
	}
}

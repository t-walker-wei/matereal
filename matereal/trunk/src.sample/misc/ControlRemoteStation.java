package misc;

import jp.digitalmuseum.connector.RXTXConnector;
import jp.digitalmuseum.mr.Matereal;
import jp.digitalmuseum.mr.entity.RemoteStation;
import jp.digitalmuseum.mr.entity.RemoteStation.RemoteStationCore;

/**
 * Control RemoteStation.
 *
 * @author Jun KATO
 */
public class ControlRemoteStation {

	public static void main(String[] args) {
		new ControlRemoteStation();
	}

	public ControlRemoteStation() {

		RXTXConnector connector = new RXTXConnector("COM:/dev/tty.usbserial-00002480");
		connector.connect(115200,
				RXTXConnector.DATABITS_8,
				RXTXConnector.STOPBITS_1,
				RXTXConnector.PARITY_NONE);

		try {
			Thread.sleep(7000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		RemoteStation rs = new RemoteStation(connector);
		RemoteStationCore rsCore = rs.requestResource(RemoteStationCore.class, null);
		rsCore.blinkLED(3);

		byte[] data = rsCore.receiveCommand();
		if (data != null) {
			System.out.println("Received command:");
			int idx = 0;
			for (byte b : data) {
				int i = b & 0xff;
				System.out.print(String.format("%02x", i));
				if (idx % 8 == 7) {
					System.out.println();
				}
			}
		}

		rsCore.blinkLED(3);

		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		for (int i = 0; i < 4; i ++) {
			boolean result = rsCore.sendCommand(data, RemoteStation.PORT1 + i);
			System.out.print("The command sent to port ");
			System.out.print(i + 1);
			System.out.print(": ");
			System.out.println(result ? "OK" : "NG");
		}

		Matereal.getInstance().dispose();
	}
}

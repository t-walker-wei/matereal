package misc;

import jp.digitalmuseum.mr.entity.RemoteStation;
import jp.digitalmuseum.mr.entity.Robot;
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
		Robot robot = new RemoteStation("COM:COM7");
		RemoteStationCore core = robot.requestResource(RemoteStationCore.class, null);
		System.out.println(core.blinkLED() ? "OK" : "NG");
	}
}

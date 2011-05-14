import jp.digitalmuseum.mr.Matereal;
import jp.digitalmuseum.mr.entity.NetTansor;
import jp.digitalmuseum.mr.entity.Robot;
import jp.digitalmuseum.mr.resource.WheelsController;

/**
 * Go forward for 7 seconds and stop.
 *
 * @author Jun KATO
 */
public class GoForward {

	public static void main(String[] args) {
		Robot robot = new NetTansor("http://192.168.32.94:8081");
		WheelsController wheels = robot.requestResource(WheelsController.class, GoForward.class);
		wheels.goForward();
		try {
			Thread.sleep(7000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			wheels.stopWheels();
			robot.freeResource(wheels, GoForward.class);
			Matereal.getInstance().dispose();
		}
	}
}

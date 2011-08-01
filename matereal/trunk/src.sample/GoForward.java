import jp.digitalmuseum.mr.Matereal;
import jp.digitalmuseum.mr.entity.Noopy2;
import jp.digitalmuseum.mr.entity.Robot;
import jp.digitalmuseum.mr.resource.WheelsController;

/**
 * Go forward for 7 seconds and stop.
 *
 * @author Jun KATO
 */
public class GoForward {

	public static void main(String[] args) {

		Robot robot = new Noopy2("btspp://646E6C00DCA3");

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

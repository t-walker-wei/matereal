package robot;
import com.phybots.Phybots;
import com.phybots.entity.Robot;
import com.phybots.resource.WheelsController;


/**
 * Go forward for 7 seconds and stop.
 *
 * @author Jun Kato
 */
public class GoForward {

	public static void main(String[] args) {

		Robot robot = RobotInfo.getRobot();

		WheelsController wheels = robot.requestResource(WheelsController.class, GoForward.class);
		wheels.goForward();

		try {
			Thread.sleep(7000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			wheels.stopWheels();
			robot.freeResource(wheels, GoForward.class);
			Phybots.getInstance().dispose();
		}
	}
}

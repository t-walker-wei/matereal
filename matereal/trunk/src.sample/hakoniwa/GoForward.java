package hakoniwa;
import com.phybots.Phybots;
import com.phybots.entity.Robot;
import com.phybots.gui.DisposeOnCloseFrame;
import com.phybots.gui.ImageProviderPanel;
import com.phybots.hakoniwa.Hakoniwa;
import com.phybots.hakoniwa.HakoniwaRobot;
import com.phybots.resource.WheelsController;
import com.phybots.utils.ScreenPosition;


/**
 * Go forward for 7 seconds and stop.
 *
 * @author Jun Kato
 */
public class GoForward {

	public static void main(String[] args) {

		// Run hakoniwa.
		Hakoniwa hakoniwa = new Hakoniwa(640, 480);
		hakoniwa.start();

		// Make a window for showing captured image.
		DisposeOnCloseFrame frame = new DisposeOnCloseFrame(new ImageProviderPanel(hakoniwa) {
			private static final long serialVersionUID = 1L;

			@Override public void dispose() {
				super.dispose();
				Phybots.getInstance().dispose();
			}
		});
		frame.setResizable(false);
		frame.setFrameSize(hakoniwa.getWidth(), hakoniwa.getHeight());

		// Connect to a robot. Acquire resource.
		Robot robot = new HakoniwaRobot("Hakobot",
				hakoniwa.screenToReal(new ScreenPosition(320, 240)));
		WheelsController wheels = robot.requestResource(WheelsController.class, GoForward.class);

		// Go forward for 7 seconds.
		wheels.goForward();
		try {
			Thread.sleep(7000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			wheels.stopWheels();
			robot.freeResource(wheels, GoForward.class);
		}
	}
}

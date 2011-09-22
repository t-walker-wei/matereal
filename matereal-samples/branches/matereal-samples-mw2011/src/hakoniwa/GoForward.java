package hakoniwa;
import jp.digitalmuseum.mr.Matereal;
import jp.digitalmuseum.mr.entity.Robot;
import jp.digitalmuseum.mr.gui.DisposeOnCloseFrame;
import jp.digitalmuseum.mr.gui.ImageProviderPanel;
import jp.digitalmuseum.mr.hakoniwa.Hakoniwa;
import jp.digitalmuseum.mr.hakoniwa.HakoniwaRobot;
import jp.digitalmuseum.mr.resource.WheelsController;
import jp.digitalmuseum.utils.ScreenPosition;

/**
 * Go forward for 7 seconds and stop.
 *
 * @author Jun KATO
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
				Matereal.getInstance().dispose();
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

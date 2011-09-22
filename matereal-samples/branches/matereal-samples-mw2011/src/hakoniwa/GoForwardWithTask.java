package hakoniwa;
import jp.digitalmuseum.mr.Matereal;
import jp.digitalmuseum.mr.entity.Robot;
import jp.digitalmuseum.mr.gui.DisposeOnCloseFrame;
import jp.digitalmuseum.mr.gui.ImageProviderPanel;
import jp.digitalmuseum.mr.hakoniwa.Hakoniwa;
import jp.digitalmuseum.mr.hakoniwa.HakoniwaRobot;
import jp.digitalmuseum.mr.task.GoForward;
import jp.digitalmuseum.utils.ScreenPosition;

/**
 * Assign one task to a robot. Get the robot to go forward for 7 seconds.
 *
 * @author Jun KATO
 */
public class GoForwardWithTask {

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

		// Connect to a robot. Instantiate a task.
		Robot robot = new HakoniwaRobot("Hakobot",
				hakoniwa.screenToReal(new ScreenPosition(320, 240)));
		GoForward goForward = new GoForward();

		// Assign the task to the robot.
		if (goForward.assign(robot)) {
			goForward.start();
			try {
				Thread.sleep(7000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			goForward.stop();
		}
	}
}

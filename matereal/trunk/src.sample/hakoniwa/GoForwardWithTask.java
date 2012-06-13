package hakoniwa;
import com.phybots.Phybots;
import com.phybots.entity.Robot;
import com.phybots.gui.DisposeOnCloseFrame;
import com.phybots.gui.ImageProviderPanel;
import com.phybots.hakoniwa.Hakoniwa;
import com.phybots.hakoniwa.HakoniwaRobot;
import com.phybots.task.GoForward;
import com.phybots.utils.ScreenPosition;


/**
 * Assign one task to a robot. Get the robot to go forward for 7 seconds.
 *
 * @author Jun Kato
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
				Phybots.getInstance().dispose();
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

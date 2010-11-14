

import jp.digitalmuseum.mr.Matereal;
import jp.digitalmuseum.mr.activity.Action;
import jp.digitalmuseum.mr.activity.ActivityDiagram;
import jp.digitalmuseum.mr.activity.TimeoutTransition;
import jp.digitalmuseum.mr.gui.DisposeOnCloseFrame;
import jp.digitalmuseum.mr.gui.ImageProviderPanel;
import jp.digitalmuseum.mr.hakoniwa.Hakoniwa;
import jp.digitalmuseum.mr.hakoniwa.HakoniwaRobot;
import jp.digitalmuseum.mr.message.ActivityEvent;
import jp.digitalmuseum.mr.message.Event;
import jp.digitalmuseum.mr.message.EventListener;
import jp.digitalmuseum.mr.message.ActivityEvent.STATUS;
import jp.digitalmuseum.mr.task.GoForward;
import jp.digitalmuseum.mr.task.Stop;
import jp.digitalmuseum.utils.ScreenPosition;

/**
 * Assign one task to a robot. Get the robot to go forward for 5 seconds.
 *
 * @author Jun KATO
 */
public class UseTaskOnHakoniwa {
	Hakoniwa hakoniwa;
	HakoniwaRobot robot;

	public static void main(String[] args) {
		new UseTaskOnHakoniwa();
	}

	/**
	 * Get the robot to go forward.
	 */
	public UseTaskOnHakoniwa() {

		// Run hakoniwa.
		hakoniwa = new Hakoniwa(640, 480);
		hakoniwa.start();

		// Make a window for showing captured image.
		final DisposeOnCloseFrame frame = new DisposeOnCloseFrame(new ImageProviderPanel(hakoniwa));
		frame.setResizable(false);
		frame.setFrameSize(hakoniwa.getWidth(), hakoniwa.getHeight());

		robot = new HakoniwaRobot("test",
				hakoniwa.screenToReal(new ScreenPosition(320, 240)));
		Action a = new Action(robot, new GoForward());
		Action b = new Action(robot, new Stop());

		final ActivityDiagram ad = new ActivityDiagram();
		ad.add(a);
		ad.add(b);
		ad.setInitialNode(a);
		ad.addTransition(new TimeoutTransition(a, b, 5000));
		ad.start();
		ad.addEventListener(new EventListener() {

			public void eventOccurred(Event e) {
				if (e instanceof ActivityEvent) {
					if (e.getSource() == ad &&
							((ActivityEvent) e).getStatus() == STATUS.LEFT) {
						Matereal.getInstance().dispose();
					}
				}
			}
		});
	}
}

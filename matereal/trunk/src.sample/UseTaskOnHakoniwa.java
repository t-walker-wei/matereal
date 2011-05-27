

import jp.digitalmuseum.mr.Matereal;
import jp.digitalmuseum.mr.entity.Robot;
import jp.digitalmuseum.mr.gui.DisposeOnCloseFrame;
import jp.digitalmuseum.mr.gui.ImageProviderPanel;
import jp.digitalmuseum.mr.hakoniwa.Hakoniwa;
import jp.digitalmuseum.mr.hakoniwa.HakoniwaRobot;
import jp.digitalmuseum.mr.message.Event;
import jp.digitalmuseum.mr.message.EventListener;
import jp.digitalmuseum.mr.message.ImageUpdateEvent;
import jp.digitalmuseum.mr.message.LocationUpdateEvent;
import jp.digitalmuseum.mr.task.GoForward;
import jp.digitalmuseum.mr.task.Stop;
import jp.digitalmuseum.mr.workflow.Action;
import jp.digitalmuseum.mr.workflow.Workflow;
import jp.digitalmuseum.mr.workflow.TimeoutTransition;
import jp.digitalmuseum.utils.ScreenPosition;

/**
 * Assign one task to a robot. Get the robot to go forward for 5 seconds.
 *
 * @author Jun KATO
 */
public class UseTaskOnHakoniwa implements EventListener {
	DisposeOnCloseFrame frame;

	public static void main(String[] args) {
		new UseTaskOnHakoniwa();
	}

	/**
	 * Get the robot to go forward.
	 */
	public UseTaskOnHakoniwa() {

		Matereal.getInstance().addEventListener(new EventListener() {

			@Override
			public void eventOccurred(Event e) {
				if (e instanceof ImageUpdateEvent ||
						e instanceof LocationUpdateEvent) {
					return;
				}
				System.out.println(e);
			}
		});

		// Run hakoniwa.
		Hakoniwa hakoniwa = new Hakoniwa(640, 480);
		hakoniwa.start();

		// Make a window for showing captured image.
		frame = new DisposeOnCloseFrame(new ImageProviderPanel(hakoniwa));
		frame.setResizable(false);
		frame.setFrameSize(hakoniwa.getWidth(), hakoniwa.getHeight());

		Robot robot = new HakoniwaRobot(hakoniwa.screenToReal(new ScreenPosition(320, 240)));
		Action a = new Action(robot, new GoForward());
		Action b = new Action(robot, new Stop());

		Workflow ad = new Workflow();
		ad.add(a);
		ad.add(b);
		ad.setInitialNode(a);
		ad.addTransition(new TimeoutTransition(a, b, 1000));
		ad.start();

		ad.addEventListener(this);
	}

	@Override
	public void eventOccurred(Event e) {
		frame.dispose();
		Matereal.getInstance().dispose();
	}
}



import jp.digitalmuseum.mr.Matereal;
import jp.digitalmuseum.mr.activity.Action;
import jp.digitalmuseum.mr.activity.ActivityDiagram;
import jp.digitalmuseum.mr.activity.TimeoutTransition;
import jp.digitalmuseum.mr.entity.MindstormsNXT;
import jp.digitalmuseum.mr.entity.Robot;
import jp.digitalmuseum.mr.task.GoForward;
import jp.digitalmuseum.mr.task.Stop;

/**
 * Assign one task to a robot. Get the robot to go forward for 5 seconds.
 *
 * @author Jun KATO
 */
public class UseTask {

	public static void main(String[] args) {
		new UseTask();
	}

	/**
	 * Get the robot to go forward.
	 */
	public UseTask() {

		Robot robot = new MindstormsNXT("Mindstorms NXT", "btspp://00165306523e");
		Action a = new Action(robot, new GoForward());
		Action b = new Action(robot, new Stop());

		ActivityDiagram ad = new ActivityDiagram();
		ad.add(a);
		ad.add(b);
		ad.setInitialNode(a);
		ad.addTransition(new TimeoutTransition(a, b, 1000));
		ad.start();
	}

	@Override
	public void finalize() {
		Matereal.getInstance().dispose();
	}
}

import jp.digitalmuseum.mr.Matereal;
import jp.digitalmuseum.mr.entity.MindstormsNXT;
import jp.digitalmuseum.mr.entity.Robot;
import jp.digitalmuseum.mr.message.Event;
import jp.digitalmuseum.mr.message.EventListener;
import jp.digitalmuseum.mr.task.GoForward;
import jp.digitalmuseum.mr.task.Stop;
import jp.digitalmuseum.mr.workflow.Action;
import jp.digitalmuseum.mr.workflow.Workflow;
import jp.digitalmuseum.mr.workflow.TimeoutTransition;

/**
 * Assign one task to a robot. Get the robot to go forward for 5 seconds.
 *
 * @author Jun KATO
 */
public class UseTask implements EventListener {

	public static void main(String[] args) {
		new UseTask();
	}

	/**
	 * Get the robot to go forward.
	 */
	public UseTask() {

		Robot robot = new MindstormsNXT("btspp://00165306523e");
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
		Matereal.getInstance().dispose();
	}
}

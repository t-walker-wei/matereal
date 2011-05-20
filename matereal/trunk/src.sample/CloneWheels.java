

import java.util.HashSet;

import jp.digitalmuseum.mr.Matereal;
import jp.digitalmuseum.mr.entity.CloneWheelsRobot;
import jp.digitalmuseum.mr.entity.MindstormsNXT;
import jp.digitalmuseum.mr.entity.NetTansor;
import jp.digitalmuseum.mr.entity.Noopy;
import jp.digitalmuseum.mr.entity.Robot;
import jp.digitalmuseum.mr.entity.Roomba;
import jp.digitalmuseum.mr.task.GoBackward;
import jp.digitalmuseum.mr.task.GoForward;
import jp.digitalmuseum.mr.task.SpinLeft;
import jp.digitalmuseum.mr.task.SpinRight;
import jp.digitalmuseum.mr.task.Stop;
import jp.digitalmuseum.mr.task.Task;
import jp.digitalmuseum.mr.workflow.Action;
import jp.digitalmuseum.mr.workflow.Workflow;
import jp.digitalmuseum.mr.workflow.TimeoutTransition;

/**
 * Assign tasks to a virtual robot. The robot works as a proxy of five real robots.
 *
 * @author Jun KATO
 */
public class CloneWheels {

	public static void main(String[] args) {
		new CloneWheels();
	}

	public CloneWheels() {
		final HashSet<Robot> robots = new HashSet<Robot>();
		robots.add(new NetTansor("http://192.168.1.103:8081", "NetTansor Web"));
		robots.add(new NetTansor("http://192.168.1.104:8081", "NetTansor"));
		robots.add(new Noopy("btspp://000195090A7D", "Noopy"));
		robots.add(new MindstormsNXT("btspp://00165306523e", "MindstormsNXT"));
		robots.add(new Roomba("btspp://00066600d69a", "Roomba"));
		final Robot virtualRobot = new CloneWheelsRobot(robots);

		// Initialize task objects.
		final Task[] tasks = new Task[] {
			new GoForward(),
			new GoBackward(),
			new SpinLeft(),
			new SpinRight(),
			new GoForward(),
			new GoBackward(),
			new SpinLeft(),
			new SpinRight(),
			new Stop()
		};

		// Connect tasks with timeout transitions.
		Workflow ad = new Workflow();
		Action action = null;
		for (Task task : tasks) {
			Action newAction = new Action(virtualRobot, task);
			ad.add(new Action(virtualRobot, task));
			if (action != null) {
				ad.addTransition(new TimeoutTransition(action, newAction, 1000));
			}
			action = newAction;
		}
		ad.start();
	}

	public void dispose() {
		Matereal.getInstance().dispose();
	}
}
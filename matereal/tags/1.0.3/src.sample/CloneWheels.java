

import java.util.HashSet;

import jp.digitalmuseum.mr.Matereal;
import jp.digitalmuseum.mr.activity.Action;
import jp.digitalmuseum.mr.activity.ActivityDiagram;
import jp.digitalmuseum.mr.activity.TimeoutTransition;
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
		robots.add(new NetTansor("NetTansor Web", "http://192.168.1.103:8081"));
		robots.add(new NetTansor("NetTansor", "http://192.168.1.104:8081"));
		robots.add(new Noopy("Noopy", "btspp://000195090A7D"));
		robots.add(new MindstormsNXT("MindstormsNXT", "btspp://00165306523e"));
		robots.add(new Roomba("Roomba", "btspp://00066600d69a"));
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
		ActivityDiagram ad = new ActivityDiagram();
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
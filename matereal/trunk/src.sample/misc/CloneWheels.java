package misc;


import java.util.HashSet;

import com.phybots.Phybots;
import com.phybots.entity.CloneWheelsRobot;
import com.phybots.entity.MindstormsNXT;
import com.phybots.entity.NetTansor;
import com.phybots.entity.Noopy;
import com.phybots.entity.Robot;
import com.phybots.entity.Roomba;
import com.phybots.task.GoBackward;
import com.phybots.task.GoForward;
import com.phybots.task.SpinLeft;
import com.phybots.task.SpinRight;
import com.phybots.task.Stop;
import com.phybots.task.Task;
import com.phybots.workflow.Action;
import com.phybots.workflow.TimeoutTransition;
import com.phybots.workflow.Workflow;


/**
 * Assign tasks to a virtual robot. The robot works as a proxy of five real robots.
 *
 * @author Jun Kato
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
		Workflow workflow = new Workflow();
		Action action = null;
		for (Task task : tasks) {
			Action newAction = new Action(virtualRobot, task);
			workflow.add(new Action(virtualRobot, task));
			if (action != null) {
				workflow.addTransition(new TimeoutTransition(action, newAction, 1000));
			}
			action = newAction;
		}
		workflow.start();
	}

	public void dispose() {
		Phybots.getInstance().dispose();
	}
}
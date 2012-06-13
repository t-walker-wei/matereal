package robot;
import com.phybots.Phybots;
import com.phybots.entity.PhysicalRobot;
import com.phybots.message.Event;
import com.phybots.message.EventListener;
import com.phybots.message.WorkflowNodeEvent;
import com.phybots.message.WorkflowNodeStatus;
import com.phybots.task.GoForward;
import com.phybots.task.MobileTask;
import com.phybots.task.SpinLeft;
import com.phybots.task.Stop;
import com.phybots.workflow.Action;
import com.phybots.workflow.TimeoutTransition;
import com.phybots.workflow.Workflow;


/**
 * Make a robot go forward and spin left for 10 times. Test of using a workflow graph.
 *
 * @author Jun Kato
 */
public class DanceDanceDance implements EventListener {

	public static void main(String[] args) {
		new DanceDanceDance();
	}

	public DanceDanceDance() {

		Phybots.getInstance().showDebugFrame();

		PhysicalRobot robot = RobotInfo.getRobot();
		robot.connect();

		// Construct a workflow graph.
		Workflow workflow = new Workflow();

		// Repeat going forward and spinning left for 10 times.
		Action head = null, tail = null;
		for (int loop = 0; loop < 10; loop++) {
			Action a = new Action(robot, fullSpeed(new GoForward()));
			Action b = new Action(robot, fullSpeed(new SpinLeft()));
			workflow.add(a, b);
			workflow.addTransition(new TimeoutTransition(a, b, 10000));
			if (tail == null) {
				head = a;
			} else {
				workflow.addTransition(new TimeoutTransition(tail, a, 10000));
			}
			tail = b;
		}

		// Stop at last.
		Action stop = new Action(robot, new Stop());
		workflow.add(stop);
		workflow.addTransition(new TimeoutTransition(tail, stop, 500));
		workflow.setInitialNode(head);
		workflow.addEventListener(this);
		workflow.start();
	}

	public MobileTask fullSpeed(MobileTask mobileTask) {
		mobileTask.setSpeed(100);
		mobileTask.setRotationSpeed(100);
		return mobileTask;
	}

	public void eventOccurred(Event e) {
		if (e instanceof WorkflowNodeEvent &&
				((WorkflowNodeEvent) e).getStatus() == WorkflowNodeStatus.LEFT) {
			Phybots.getInstance().dispose();
		}
	}
}

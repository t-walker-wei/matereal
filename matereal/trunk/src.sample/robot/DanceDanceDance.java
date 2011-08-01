package robot;
import jp.digitalmuseum.mr.Matereal;
import jp.digitalmuseum.mr.entity.Noopy2;
import jp.digitalmuseum.mr.entity.PhysicalRobot;
import jp.digitalmuseum.mr.message.Event;
import jp.digitalmuseum.mr.message.EventListener;
import jp.digitalmuseum.mr.message.WorkflowNodeEvent;
import jp.digitalmuseum.mr.message.WorkflowNodeStatus;
import jp.digitalmuseum.mr.task.GoForward;
import jp.digitalmuseum.mr.task.MobileTask;
import jp.digitalmuseum.mr.task.SpinLeft;
import jp.digitalmuseum.mr.task.Stop;
import jp.digitalmuseum.mr.workflow.Action;
import jp.digitalmuseum.mr.workflow.Workflow;
import jp.digitalmuseum.mr.workflow.TimeoutTransition;

/**
 * Make a robot go forward and spin left for 10 times. Test of using a workflow graph.
 *
 * @author Jun KATO
 */
public class DanceDanceDance implements EventListener {

	public static void main(String[] args) {
		new DanceDanceDance();
	}

	public DanceDanceDance() {

		PhysicalRobot robot = new Noopy2("btspp://646E6C00DCB2");
		robot.connect();

		// Construct a workflow graph.
		Workflow workflow = new Workflow();

		// Repeat going forward and spinning left for 10 times.
		Action head = null, tail = null;
		for (int loop = 0; loop < 10; loop++) {
			Action a = new Action(robot, fullSpeed(new GoForward()));
			Action b = new Action(robot, fullSpeed(new SpinLeft()));
			workflow.add(a, b);
			workflow.addTransition(new TimeoutTransition(a, b, 1000));
			if (tail == null) {
				head = a;
			} else {
				workflow.addTransition(new TimeoutTransition(tail, a, 1000));
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
			Matereal.getInstance().dispose();
		}
	}
}

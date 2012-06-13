package hakoniwa;
import com.phybots.Phybots;
import com.phybots.entity.Robot;
import com.phybots.gui.DisposeOnCloseFrame;
import com.phybots.gui.ImageProviderPanel;
import com.phybots.hakoniwa.Hakoniwa;
import com.phybots.hakoniwa.HakoniwaRobot;
import com.phybots.task.GoForward;
import com.phybots.task.Stop;
import com.phybots.utils.ScreenPosition;
import com.phybots.workflow.Action;
import com.phybots.workflow.TimeoutTransition;
import com.phybots.workflow.Workflow;


/**
 * Assign one task to a robot. Get the robot to go forward for 7 seconds.
 *
 * @author Jun Kato
 */
public class GoForwardWithWorkflow {

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

		// Construct a workflow graph.
		Workflow workflow = new Workflow();
		Action goForward = new Action(robot, new GoForward());
		Action stop = new Action(robot, new Stop());
		workflow.add(goForward);
		workflow.add(stop);
		workflow.addTransition(new TimeoutTransition(goForward, stop, 7000));

		// Set the initial node and start running the graph.
		workflow.setInitialNode(goForward);

		workflow.start();
	}
}

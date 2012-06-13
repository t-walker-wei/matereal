package hakoniwa;
import javax.swing.SwingUtilities;

import com.phybots.Phybots;
import com.phybots.entity.Robot;
import com.phybots.gui.DisposeOnCloseFrame;
import com.phybots.gui.ImageProviderPanel;
import com.phybots.hakoniwa.Hakoniwa;
import com.phybots.hakoniwa.HakoniwaRobot;
import com.phybots.task.GoForward;
import com.phybots.task.SpinLeft;
import com.phybots.task.Stop;
import com.phybots.utils.ScreenPosition;
import com.phybots.workflow.Action;
import com.phybots.workflow.Fork;
import com.phybots.workflow.Join;
import com.phybots.workflow.TimeoutTransition;
import com.phybots.workflow.Transition;
import com.phybots.workflow.Workflow;


/**
 * Make 4 robots do the same things in parallel. Test of using a workflow graph.
 *
 * @author Jun Kato
 */
public class DanceDanceDance {
	Hakoniwa hakoniwa;

	public static void main(String[] args) {
		new DanceDanceDance();
	}

	public DanceDanceDance() {

		// Run hakoniwa.
		hakoniwa = new Hakoniwa(640, 480);
		hakoniwa.start();

		// Construct a workflow graph.
		final Robot[] robots = new Robot[4];
		final Workflow workflow = new Workflow();
		final Action[] initialNodes = new Action[robots.length];
		final Action[] finalNodes = new Action[robots.length];
		for (int i = 0; i < robots.length; i ++) {
			robots[i] = new HakoniwaRobot("Robot No."+i,
					hakoniwa.screenToReal(new ScreenPosition(320+(i-2)*40, 240)));

			// Repeat going forward and spinning left for 3 times.
			Action tail = null;
			for (int loop = 0; loop < 3; loop ++) {
				Action a = new Action(robots[i], new GoForward());
				Action b = new Action(robots[i], new SpinLeft());
				workflow.add(a, b);
				workflow.addTransition(new TimeoutTransition(a, b, 3000));
				if (loop == 0) {
					initialNodes[i] = a;
				} else {
					workflow.addTransition(new TimeoutTransition(tail, a, 3000));
				}
				tail = b;
			}

			// Stop at last.
			finalNodes[i] = new Action(robots[i], new Stop());
			workflow.add(finalNodes[i]);
			workflow.addTransition(new TimeoutTransition(tail, finalNodes[i], 3000));
		}

		// Run 4 robots in parallel.
		Fork fork = new Fork(initialNodes);
		Join join = new Join(finalNodes);
		workflow.add(fork);
		workflow.add(join);
		workflow.addTransition(new Transition(fork, join));
		workflow.setInitialNode(fork);
		workflow.start();

		// Make windows for showing a workflow graph and status of hakoniwa.
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				final DisposeOnCloseFrame frame = new DisposeOnCloseFrame(new ImageProviderPanel(hakoniwa)) {
					private static final long serialVersionUID = 1L;
					@Override
					public void dispose() {
						super.dispose();
						Phybots.getInstance().dispose();
					}
				};
				frame.setResizable(false);
				frame.setFrameSize(hakoniwa.getWidth(), hakoniwa.getHeight());
				frame.setTitle("Dance dance dance.");
			}
		});
	}
}

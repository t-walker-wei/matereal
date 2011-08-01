package hakoniwa;
import javax.swing.SwingUtilities;

import jp.digitalmuseum.mr.Matereal;
import jp.digitalmuseum.mr.entity.Robot;
import jp.digitalmuseum.mr.gui.DisposeOnCloseFrame;
import jp.digitalmuseum.mr.gui.ImageProviderPanel;
import jp.digitalmuseum.mr.hakoniwa.Hakoniwa;
import jp.digitalmuseum.mr.hakoniwa.HakoniwaRobot;
import jp.digitalmuseum.mr.task.GoForward;
import jp.digitalmuseum.mr.task.SpinLeft;
import jp.digitalmuseum.mr.task.Stop;
import jp.digitalmuseum.mr.workflow.Action;
import jp.digitalmuseum.mr.workflow.Workflow;
import jp.digitalmuseum.mr.workflow.Fork;
import jp.digitalmuseum.mr.workflow.Join;
import jp.digitalmuseum.mr.workflow.TimeoutTransition;
import jp.digitalmuseum.mr.workflow.Transition;
import jp.digitalmuseum.utils.ScreenPosition;

/**
 * Make 4 robots do the same things in parallel. Test of using a workflow graph.
 *
 * @author Jun KATO
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
						Matereal.getInstance().dispose();
					}
				};
				frame.setResizable(false);
				frame.setFrameSize(hakoniwa.getWidth(), hakoniwa.getHeight());
				frame.setTitle("Dance dance dance.");
			}
		});
	}
}

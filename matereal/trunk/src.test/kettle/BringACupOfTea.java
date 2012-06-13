package kettle;

import javax.swing.SwingUtilities;

import com.phybots.Phybots;
import com.phybots.entity.Robot;
import com.phybots.gui.*;
import com.phybots.gui.workflow.WorkflowViewPane;
import com.phybots.hakoniwa.Hakoniwa;
import com.phybots.hakoniwa.HakoniwaCylinder;
import com.phybots.hakoniwa.HakoniwaRobot;
import com.phybots.message.Event;
import com.phybots.message.EventListener;
import com.phybots.message.ImageUpdateEvent;
import com.phybots.task.Push;
import com.phybots.utils.Location;
import com.phybots.workflow.Action;
import com.phybots.workflow.Fork;
import com.phybots.workflow.Join;
import com.phybots.workflow.Node;
import com.phybots.workflow.TimeoutTransition;
import com.phybots.workflow.Transition;
import com.phybots.workflow.Workflow;

import kettle.task.Boil;
import kettle.task.Pour;
import kettle.task.Stop;

/**
 * Bring a cup of tea.
 *
 * @author Jun Kato
 */
public class BringACupOfTea {
	private Hakoniwa hakoniwa;

	public static void main(String[] args) {
		new BringACupOfTea();
	}

	public BringACupOfTea() {

		// Run hakoniwa.
		hakoniwa = new Hakoniwa();
		hakoniwa.start();

		// Prepare devices.
		final double x = hakoniwa.getRealWidth()/2;
		final double y = hakoniwa.getRealHeight()/2;
		final Robot robot = new HakoniwaRobot("Servebot", new Location(x, y, -Math.PI*3/4));
		final HakoniwaCylinder mug = new HakoniwaCylinder("Mug", x+50, y+50, 16, 0);
		final Kettle kettle = new Kettle("Kettle");

		// Entities and marker detector are already initialized.
		final Workflow workflow = new Workflow();
		Action push = new Action(robot, new Push(mug, hakoniwa.getPosition(kettle)));
		Action boil = new Action(kettle, new Boil());
		Action pour = new Action(kettle, new Pour());
		Action stop = new Action(kettle, new Stop());
		Action stop2 = new Action(kettle, new Stop());
		Fork fork = new Fork(push, boil);
		Join join = new Join(push, boil);
		workflow.add(new Node[] { fork, push, boil, join, pour, stop, stop2 });
		workflow.addTransition(new Transition(fork, join));
		workflow.addTransition(new Transition(join, pour));
		workflow.addTransition(new TimeoutTransition(pour, stop, 5000));
		workflow.setInitialNode(fork);
		workflow.start();
		// Hot water will be poured into the mug for five seconds.

		// Show a workflow graph.
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				WorkflowViewPane pane = new WorkflowViewPane(workflow);
				final DisposeOnCloseFrame viewer = new DisposeOnCloseFrame(pane);
				viewer.setFrameSize(640, 480);

				// Make a window for showing captured image.
				final DisposeOnCloseFrame frame = new DisposeOnCloseFrame(new ImageProviderPanel(hakoniwa)) {
					private static final long serialVersionUID = 1L;

					@Override public void dispose() {
						super.dispose();
						viewer.dispose();
						Phybots.getInstance().dispose();
					}
				};
				frame.setResizable(false);
				frame.setFrameSize(hakoniwa.getWidth(), hakoniwa.getHeight());

				// Repaint the window every time the image is updated.
				hakoniwa.addEventListener(new EventListener() {
					public void eventOccurred(Event e) {
						if (e instanceof ImageUpdateEvent) {
							frame.repaint();
						}
					}
				});
			}
		});
	}
}

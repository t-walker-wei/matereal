package kettle;

import javax.swing.SwingUtilities;

import jp.digitalmuseum.mr.Matereal;
import jp.digitalmuseum.mr.activity.Action;
import jp.digitalmuseum.mr.activity.ActivityDiagram;
import jp.digitalmuseum.mr.activity.Fork;
import jp.digitalmuseum.mr.activity.Join;
import jp.digitalmuseum.mr.activity.Node;
import jp.digitalmuseum.mr.activity.TimeoutTransition;
import jp.digitalmuseum.mr.activity.Transition;
import jp.digitalmuseum.mr.entity.Robot;
import jp.digitalmuseum.mr.gui.*;
import jp.digitalmuseum.mr.gui.activity.ActivityDiagramPane;
import jp.digitalmuseum.mr.hakoniwa.Hakoniwa;
import jp.digitalmuseum.mr.hakoniwa.HakoniwaCylinder;
import jp.digitalmuseum.mr.hakoniwa.HakoniwaRobot;
import jp.digitalmuseum.mr.message.Event;
import jp.digitalmuseum.mr.message.EventListener;
import jp.digitalmuseum.mr.message.ImageUpdateEvent;
import jp.digitalmuseum.mr.task.Push;
import jp.digitalmuseum.utils.Location;
import kettle.task.Boil;
import kettle.task.Pour;
import kettle.task.Stop;

/**
 * Bring a cup of tea.
 *
 * @author Jun KATO
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
		final ActivityDiagram ad = new ActivityDiagram();
		Action push = new Action(robot, new Push(mug, hakoniwa.getPosition(kettle)));
		Action boil = new Action(kettle, new Boil());
		Action pour = new Action(kettle, new Pour());
		Action stop = new Action(kettle, new Stop());
		Action stop2 = new Action(kettle, new Stop());
		Fork fork = new Fork(push, boil);
		Join join = new Join(push, boil, stop);
		ad.add(new Node[] { fork, push, boil, join, pour, stop, stop2 });
		ad.addTransition(new TimeoutTransition(push, stop, 10000));
		ad.addTransition(new Transition(fork, join));
		ad.addTransition(new Transition(join, pour));
		ad.addTransition(new TimeoutTransition(pour, stop, 5000));
		ad.setInitialNode(fork);
		ad.start();
		// Hot water will be poured into the mug for five seconds.

		// Show an activity diagram.
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				ActivityDiagramPane pane = new ActivityDiagramPane(ad);
				final DisposeOnCloseFrame viewer = new DisposeOnCloseFrame(pane);
				viewer.setFrameSize(640, 480);

				// Make a window for showing captured image.
				final DisposeOnCloseFrame frame = new DisposeOnCloseFrame(new ImageProviderPanel(hakoniwa)) {
					private static final long serialVersionUID = 1L;

					@Override public void dispose() {
						super.dispose();
						viewer.dispose();
						Matereal.getInstance().dispose();
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

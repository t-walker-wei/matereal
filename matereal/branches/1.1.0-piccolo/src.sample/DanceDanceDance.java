import jp.digitalmuseum.mr.Matereal;
import jp.digitalmuseum.mr.activity.Action;
import jp.digitalmuseum.mr.activity.ActivityDiagram;
import jp.digitalmuseum.mr.activity.Fork;
import jp.digitalmuseum.mr.activity.Join;
import jp.digitalmuseum.mr.activity.TimeoutTransition;
import jp.digitalmuseum.mr.activity.Transition;
import jp.digitalmuseum.mr.entity.Robot;
import jp.digitalmuseum.mr.gui.DisposeOnCloseFrame;
import jp.digitalmuseum.mr.gui.ImageProviderPanel;
import jp.digitalmuseum.mr.hakoniwa.Hakoniwa;
import jp.digitalmuseum.mr.hakoniwa.HakoniwaRobot;
import jp.digitalmuseum.mr.message.ActivityEvent;
import jp.digitalmuseum.mr.message.Event;
import jp.digitalmuseum.mr.message.EventListener;
import jp.digitalmuseum.mr.message.ActivityEvent.STATUS;
import jp.digitalmuseum.mr.task.GoForward;
import jp.digitalmuseum.mr.task.SpinLeft;
import jp.digitalmuseum.mr.task.Stop;
import jp.digitalmuseum.utils.ScreenPosition;

/**
 * Make 4 robots do the same things in parallel. Test of using an activity diagram.
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

		// Construct an activity diagram.
		final Robot[] robots = new Robot[4];
		final ActivityDiagram ad = new ActivityDiagram();
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
				ad.add(a, b);
				ad.addTransition(new TimeoutTransition(a, b, 3000));
				if (loop == 0) {
					initialNodes[i] = a;
				} else {
					ad.addTransition(new TimeoutTransition(tail, a, 3000));
				}
				tail = b;
			}

			// Stop at last.
			finalNodes[i] = new Action(robots[i], new Stop());
			ad.add(finalNodes[i]);
			ad.addTransition(new TimeoutTransition(tail, finalNodes[i], 3000));
		}

		// Run 4 robots in parallel.
		Fork fork = new Fork(initialNodes);
		Join join = new Join(finalNodes);
		ad.add(fork);
		ad.add(join);
		ad.addTransition(new Transition(fork, join));
		ad.setInitialNode(fork);

		// Make windows for showing an activity diagram and status of hakoniwa.
		final DisposeOnCloseFrame graph = new DisposeOnCloseFrame(ad.newActivityDiagramCanvas());
		final DisposeOnCloseFrame frame = new DisposeOnCloseFrame(new ImageProviderPanel(hakoniwa)) {
			private static final long serialVersionUID = 1L;
			@Override
			public void dispose() {
				Matereal.getInstance().dispose();
				graph.dispose();
				super.dispose();
			}
		};
		frame.setResizable(false);
		frame.setFrameSize(hakoniwa.getWidth(), hakoniwa.getHeight());
		frame.setTitle("Dance dance dance.");

		// Change title of the main window when all tasks were completed.
		ad.addEventListener(new EventListener() {

			public void eventOccurred(Event e) {
				if (e instanceof ActivityEvent) {
					if (e.getSource() == ad &&
							((ActivityEvent) e).getStatus() == STATUS.LEFT) {
						frame.setTitle("All tasks were completed.");
					}
				}
			}
		});
		ad.start();
	}
}

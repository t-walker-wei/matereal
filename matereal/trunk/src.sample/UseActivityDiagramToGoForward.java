import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import jp.digitalmuseum.mr.Matereal;
import jp.digitalmuseum.mr.activity.Action;
import jp.digitalmuseum.mr.activity.ActivityDiagram;
import jp.digitalmuseum.mr.activity.TimeoutTransition;
import jp.digitalmuseum.mr.entity.Robot;
import jp.digitalmuseum.mr.gui.DisposeOnCloseFrame;
import jp.digitalmuseum.mr.gui.ImageProviderPanel;
import jp.digitalmuseum.mr.hakoniwa.Hakoniwa;
import jp.digitalmuseum.mr.hakoniwa.HakoniwaRobot;
import jp.digitalmuseum.mr.task.GoForward;
import jp.digitalmuseum.mr.task.Stop;
import jp.digitalmuseum.utils.ScreenPosition;

/**
 * Assign one task to a robot. Get the robot to go forward for 7 seconds.
 *
 * @author Jun KATO
 */
public class UseActivityDiagramToGoForward {

	public static void main(String[] args) {

		//
		Matereal.getInstance().showDebugFrame();

		// Run hakoniwa.
		Hakoniwa hakoniwa = new Hakoniwa(640, 480);
		hakoniwa.start();

		// Connect to a robot. Instantiate a task.
		Robot robot = new HakoniwaRobot("Hakobot",
				hakoniwa.screenToReal(new ScreenPosition(320, 240)));

		// Test serialization.
		try {
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("test.dat"));
			oos.writeObject(robot);

			ObjectInputStream ois = new ObjectInputStream(new FileInputStream("test.dat"));
			robot = (Robot) ois.readObject();
			hakoniwa = ((HakoniwaRobot) robot).getHakoniwa();

		} catch (Exception e) {
			e.printStackTrace();
		}

		// Make a window for showing captured image.
		DisposeOnCloseFrame frame = new DisposeOnCloseFrame(new ImageProviderPanel(hakoniwa) {
			private static final long serialVersionUID = 1L;

			@Override public void dispose() {
				Matereal.getInstance().dispose();
			}
		});
		frame.setResizable(false);
		frame.setFrameSize(hakoniwa.getWidth(), hakoniwa.getHeight());

		// Construct an activity diagram.
		ActivityDiagram ad = new ActivityDiagram();
		Action goForward = new Action(robot, new GoForward());
		Action stop = new Action(robot, new Stop());
		ad.add(goForward);
		ad.add(stop);
		ad.addTransition(new TimeoutTransition(goForward, stop, 7000));

		// Set the initial node and start running the graph.
		ad.setInitialNode(goForward);

		ad.start();
	}
}

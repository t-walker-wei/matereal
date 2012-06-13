package marker.robot;
import java.awt.BasicStroke;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.phybots.Phybots;
import com.phybots.entity.Robot;
import com.phybots.gui.DisposeOnCloseFrame;
import com.phybots.gui.ImageProviderPanel;
import com.phybots.resource.WheelsController;
import com.phybots.service.Camera;
import com.phybots.service.MarkerDetector;
import com.phybots.task.Move;
import com.phybots.task.Task;
import com.phybots.utils.Position;
import com.phybots.utils.ScreenPosition;

import jp.digitalmuseum.napkit.NapMarker;
import jp.digitalmuseum.napkit.gui.TypicalMDCPane;

/**
 * Show a frame with a view of the world coordinate.
 *
 * @author Jun Kato
 */
public class ClickAndRun {
	private Task move;
	final private ScreenPosition goal = new ScreenPosition();

	public static void main(String[] args) {
		new ClickAndRun();
	}

	public ClickAndRun() {

		// Destination is not specified at first.
		goal.setNotFound(true);

		// Initialize a robot.
		final Robot robot = RobotInfo.getRobot();

		// Run a camera.
		// Let users select a device to capture images.
		String identifier = (String) JOptionPane.showInputDialog(null,
				"Select a device to capture images.", "Device list",
				JOptionPane.QUESTION_MESSAGE, null,
				Camera.queryIdentifiers(), null);
		if (identifier == null  || identifier.length() <= 0) {
			return;
		}
		final Camera camera = new Camera(identifier);
		camera.setSize(800, 600);
		camera.start();

		// Run a marker detector.
		final MarkerDetector detector = new MarkerDetector();
		detector.addMarker(new NapMarker("markers\\4x4_48.patt", 5.5), robot);
		detector.setImageProvider(camera);
		detector.start();

		// Show a configuration window.
		final JFrame configFrame = new DisposeOnCloseFrame(
				new TypicalMDCPane(detector));

		// Initialize a main panel.
		final ImageProviderPanel panel = new ImageProviderPanel(camera) {
			private static final long serialVersionUID = 1L;
			@Override public void paintComponent(Graphics g) {
				super.paintComponent(g);
				final Graphics2D g2 = (Graphics2D) g;
				g2.translate(getOffsetX(), getOffsetY());

				// Paint detection results.
				g2.setStroke(new BasicStroke(4));
				g2.setColor(Color.orange);
				detector.paint(g2);

				// Paint the destination.
				if (!goal.isNotFound()) {
					g2.setColor(Color.green);
					g2.fillOval(
							goal.getX()-5,
							goal.getY()-5,
							10, 10);
				}
			}
			@Override public void dispose() {
				configFrame.dispose();
				super.dispose();
				Phybots.getInstance().dispose();
			}
		};

		// Add a mouse listener.
		panel.addMouseListener(new MouseAdapter() {
			final private Position destination =
				new Position();

			@Override public void mouseReleased(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1) {

					// Get a clicked position.
					goal.set(e.getX(), e.getY());
					panel.getScreenToImageOut(goal, goal);
					camera.screenToRealOut(goal, destination);

					// Assign a task moving to the position.
					final Task task = robot.getAssignedTask(WheelsController.class);
					if (task != null) {
						task.stop();
					}
					move = new Move(destination);
					if (move.assign(robot)) {
						move.start();
					}
				}
			}
		});

		// Show a world.
		final JFrame frame = new DisposeOnCloseFrame(panel);
		frame.setSize(710, 660);
	}
}

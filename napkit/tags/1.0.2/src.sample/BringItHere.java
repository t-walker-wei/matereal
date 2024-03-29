import java.awt.BasicStroke;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Set;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import jp.digitalmuseum.capture.VideoCaptureFactoryImpl;
import jp.digitalmuseum.mr.Matereal;
import jp.digitalmuseum.mr.entity.Entity;
import jp.digitalmuseum.mr.entity.Mini;
import jp.digitalmuseum.mr.entity.PhysicalBox;
import jp.digitalmuseum.mr.gui.DisposeOnCloseFrame;
import jp.digitalmuseum.mr.gui.ImageProviderPanel;
import jp.digitalmuseum.mr.gui.utils.EntityPainter;
import jp.digitalmuseum.mr.gui.utils.VectorFieldPainter;
import jp.digitalmuseum.mr.resource.WheelsController;
import jp.digitalmuseum.mr.service.MarkerDetector;
import jp.digitalmuseum.mr.service.Camera;
import jp.digitalmuseum.mr.task.Push;
import jp.digitalmuseum.mr.task.Task;
import jp.digitalmuseum.mr.task.VectorFieldTask;
import jp.digitalmuseum.napkit.NapDetectionResult;
import jp.digitalmuseum.napkit.NapMarker;
import jp.digitalmuseum.napkit.gui.TypicalMDCPane;
import jp.digitalmuseum.utils.Position;
import jp.digitalmuseum.utils.ScreenPosition;

/**
 * Bring it here!
 * Test of assigning a task to a robot.
 *
 * @author Jun KATO
 */
public class BringItHere {
	private Camera camera;
	private MarkerDetector detector;
	private VectorFieldTask push;
	private ScreenPosition goal = null;

	public static void main(String[] args) {
		new BringItHere();
	}

	public BringItHere() {

		// Run a camera.
		// Let users select a device to capture images.
		final String identifier = (String) JOptionPane.showInputDialog(null,
				"Select a device to capture images.", "Device list",
				JOptionPane.QUESTION_MESSAGE, null,
				new VideoCaptureFactoryImpl().queryIdentifiers(), null);
		if ((identifier != null) && (identifier.length() > 0)) {
			camera = new Camera(identifier);
		} else {
			camera = new Camera();
		}
		camera.setSize(800, 600);
		camera.setRealSize(80, 60);
		camera.start();

		// Run a marker detector.
		detector = new MarkerDetector();
		detector.loadCameraParameter("calib_qcam.dat");
		detector.start();

		// Show a configuration window.
		final JFrame configFrame = new DisposeOnCloseFrame(new TypicalMDCPane(
				detector));

		// Initialize a robot.
		final Mini robot = new Mini("Mini", "btspp://000666015818");

		// Initialize boxes.
		final Entity[] entities = new Entity[1];
		entities[0] = new PhysicalBox("Milk chocolate", 10, 8);
		detector.put(new NapMarker("markers\\4x4_35.patt", 5.5), entities[0]);
		detector.put(new NapMarker("markers\\4x4_907.patt", 5.5), robot);

		// Show detection results in real-time.
		final ImageProviderPanel panel = new ImageProviderPanel(camera) {
			private static final long serialVersionUID = 1L;
			private transient final VectorFieldPainter vectorFieldPainter =
					new VectorFieldPainter(5);
			private transient final EntityPainter entityPainter =
					new EntityPainter(0.5);
			private transient final Stroke stroke =
					new BasicStroke(5);
			private transient final AlphaComposite alphaComp = AlphaComposite
					.getInstance(AlphaComposite.SRC_OVER, .3f);
			private transient final AlphaComposite alphaComp2 = AlphaComposite
					.getInstance(AlphaComposite.SRC_OVER, .7f);

			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				final Graphics2D g2 = (Graphics2D) g;
				g2.translate(getOffsetX(), getOffsetY());
				final Composite comp = g2.getComposite();

				// Draw vectors.
				g2.setComposite(alphaComp);
				g.setColor(Color.blue);
				vectorFieldPainter.paint(push, g2);
				g2.setComposite(alphaComp2);
				g2.setColor(Color.black);
				g2.fillRect(0, 0, getWidth(), 35);

				// Draw entities.
				g2.setColor(Color.green);
				for (Entity e : entities) {
					entityPainter.paint(g2, e);
				}
				entityPainter.paint(g2, robot);

				// Get detected results.
				final Set<NapDetectionResult> results = detector.getResults();

				// Draw each detected result.
				for (final NapDetectionResult result : results) {

					// Draw corners
					g2.setColor(Color.orange);
					detector.paint(g2, result);

					// Draw information for the square.
					g.setColor(Color.cyan);
					final ScreenPosition point = result.getPosition();
					g2.drawLine(point.getX(), point.getY(), point.getX() + 55,
							point.getY() + 43);
					g2.drawRect(point.getX() + 55, point.getY() + 23, 200, 40);
					g2.drawString("Confidence: " + result.getConfidence(),
							point.getX() + 64, point.getY() + 40);
					g2.drawString("Position: " + point, point.getX() + 64,
							point.getY() + 56);
				}

				// Draw status.
				g2.setComposite(comp);
				g2.drawLine(0, 35, getWidth(), 35);
				g2.setColor(Color.white);
				if (goal == null) {
					g2.drawString("Click to set the destination.", 10, 30);
				} else {
					if (push != null) {
						g2.drawString("Status: " + push, 10, 30);
					} else {
						g2.drawString("Status: Stopped", 10, 30);
					}
					Stroke s = g2.getStroke();
					g2.setStroke(stroke);
					g2.setColor(Color.black);
					g2.drawLine(goal.getX() - 5, goal.getY() - 5,
							goal.getX() + 5, goal.getY() + 5);
					g2.drawLine(goal.getX() - 5, goal.getY() + 5,
							goal.getX() + 5, goal.getY() - 5);
					g2.setStroke(s);
				}

				// Draw detected number of squares.
				g2.setColor(Color.cyan);
				g2.drawString("detected: "
						+ (results == null ? 0 : results.size()), 10,
						getHeight() - 10);
				g2.dispose();
			}
		};

		// Bring the clicked object to the location in front of the user.
		panel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {

				// Stop the previously assigned task.
				final Task task = robot.getAssignedTask(WheelsController.class);
				if (task != null)
					task.stop();

				// Set the goal position.
				final int x = e.getX(), y = e.getY();
				if (goal == null) {
					goal = new ScreenPosition(x, y);
					return;
				}

				// Push the clicked object to the goal.
				final Entity entity = getClickedEntity(x, y);
				if (entity != null && entity != robot) {
					push = new Push(entity, camera.screenToReal(goal));
					if (push.assign(robot))
						push.start();
				}
			}

		});

		final DisposeOnCloseFrame frame = new DisposeOnCloseFrame(panel) {
			private static final long serialVersionUID = 1L;

			@Override
			public void dispose() {
				configFrame.dispose();
				super.dispose();
				Matereal.getInstance().dispose();
			}
		};
		frame.setFrameSize(camera.getWidth(), camera.getHeight());
	}

	/**
	 * Get clicked entity.
	 *
	 * @param x
	 * @param y
	 * @return Clicked entity
	 */
	private Entity getClickedEntity(int x, int y) {
		Position p = camera.screenToReal(new ScreenPosition(x, y));
		for (Entity e : Matereal.getInstance().getEntities()) {
			if (detector.contains(e, p)) {
				return e;
			}
		}
		return null;
	}
}

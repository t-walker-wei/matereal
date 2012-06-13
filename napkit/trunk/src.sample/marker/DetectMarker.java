package marker;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.phybots.Phybots;
import com.phybots.gui.DisposeOnCloseFrame;
import com.phybots.gui.ImageProviderPanel;
import com.phybots.service.Camera;
import com.phybots.service.MarkerDetector;
import com.phybots.utils.Array;
import com.phybots.utils.ScreenPosition;

import jp.digitalmuseum.napkit.NapDetectionResult;
import jp.digitalmuseum.napkit.NapMarker;
import jp.digitalmuseum.napkit.gui.MarkerDetectorPanel;
import jp.digitalmuseum.napkit.gui.MarkerEntityPanel;

/**
 * Run marker detection and show its results.
 *
 * @author Jun Kato
 */
public class DetectMarker {

	public static void main(String[] args) {
		new DetectMarker();
	}

	public DetectMarker() {

		// Run a camera.
		// Let users select a device to capture images.
		final String identifier = (String) JOptionPane.showInputDialog(null,
				"Select a device to capture images.", "Device list",
				JOptionPane.QUESTION_MESSAGE, null, Camera.queryIdentifiers(), null);
		Camera camera;
		if ((identifier != null) && (identifier.length() > 0)) {
			camera = new Camera(identifier);
		} else {
			camera = new Camera();
		}
		camera.setSize(800, 600);
		camera.start();

		// Run a marker detector.
		final MarkerDetector detector = new MarkerDetector();
		detector.setImageProvider(camera);
		detector.start();

		// Show a configuration window.
		final JFrame configFrame = new DisposeOnCloseFrame(new MarkerDetectorPanel(detector));
		configFrame.setSize(640, 480);

		// Show a marker/entity panel.
		final JFrame markerFrame = new DisposeOnCloseFrame(new MarkerEntityPanel(detector));
		markerFrame.setSize(640, 480);

		// Detect a marker.
		final NapMarker marker = MarkerInfo.getRobotMarker();
		detector.addMarker(marker);

		// Show detection results in real-time.
		final DisposeOnCloseFrame frame = new DisposeOnCloseFrame(
				new ImageProviderPanel(camera) {
					private static final long serialVersionUID = 1L;
					private transient Stroke stroke;
					@Override public void paintComponent(Graphics g) {
						super.paintComponent(g);
						final Graphics2D g2 = (Graphics2D) g;
						if (stroke == null) {
							stroke = new BasicStroke(5);
						}
						g2.setStroke(stroke);
						g2.translate(getOffsetX(), getOffsetY());

						// Get detected results.
						final Array<NapDetectionResult> results = detector.getResults();

						// Draw each detected result.
						for (final NapDetectionResult result : results) {

							// Draw corners
							g2.setColor(Color.orange);
							detector.paint(g2, result);

							// Draw information for the square.
							g2.setColor(Color.cyan);
							final ScreenPosition point = result.getPosition();
							g2.drawLine(point.getX(), point.getY(), point.getX()+55, point.getY()+43);
							g2.drawRect(point.getX()+55, point.getY()+23, 200, 40);
							drawString(g2, "Confidence: "+result.getConfidence(), point.getX()+64, point.getY()+40);
							drawString(g2, "Position: "+point, point.getX()+64, point.getY()+56);
						}

						// Draw detected number of squares.
						drawString(g2, "detected: "+(results == null ? 0 : results.size()),
								10, getHeight()-10);
						g2.dispose();
					}

					/**
					 * Draw a string with 1px simple black border.
					 */
					private void drawString(Graphics g, String s, int x, int y) {
						g.setColor(Color.black);
						g.drawString(s, x+1, y);
						g.drawString(s, x-1, y);
						g.drawString(s, x, y-1);
						g.drawString(s, x, y+1);
						g.setColor(Color.cyan);
						g.drawString(s, x, y);
					}
				}) {
			private static final long serialVersionUID = 1L;
			@Override public void dispose() {
				configFrame.dispose();
				markerFrame.dispose();
				super.dispose();
				Phybots.getInstance().dispose();
			}
		};
		frame.setFrameSize(camera.getWidth(), camera.getHeight());
	}
}

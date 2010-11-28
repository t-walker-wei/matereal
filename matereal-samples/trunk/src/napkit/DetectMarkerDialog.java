package napkit;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

import jp.digitalmuseum.capture.VideoCaptureFactoryImpl;
import jp.digitalmuseum.mr.Matereal;
import jp.digitalmuseum.mr.gui.DisposeOnCloseFrame;
import jp.digitalmuseum.mr.service.MarkerDetector;
import jp.digitalmuseum.mr.service.Camera;
import jp.digitalmuseum.napkit.gui.MarkerDetectorPanel;

/**
 * Run marker detection and show its results.
 *
 * @author Jun KATO
 */
public class DetectMarkerDialog {

	public static void main(String[] args) {

		// Set look and feel.
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Run a camera.
		// Let users select a device to capture images.
		String identifier = (String) JOptionPane.showInputDialog(null,
				"Select a device to capture images.", "Device list",
				JOptionPane.QUESTION_MESSAGE, null, new VideoCaptureFactoryImpl()
						.queryIdentifiers(), null);
		Camera camera;
		if ((identifier != null) && (identifier.length() > 0)) {
			camera = new Camera(identifier);
		} else {
			camera = new Camera();
		}
		camera.start();

		// Run a marker detector.
		MarkerDetector detector = new MarkerDetector();
		detector.start();

		// Show a configuration window.
		new DisposeOnCloseFrame(new MarkerDetectorPanel(detector) {
			private static final long serialVersionUID = 1L;
			@Override public void dispose() {
				super.dispose();
				System.out.println("disposing?");
				Matereal.getInstance().dispose();
			}
		});
	}
}

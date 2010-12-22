
import javax.swing.JOptionPane;

import jp.digitalmuseum.capture.VideoCaptureDS;
import jp.digitalmuseum.capture.VideoCaptureFactoryImpl;
import jp.digitalmuseum.mr.Matereal;
import jp.digitalmuseum.mr.gui.*;
import jp.digitalmuseum.mr.service.Camera;

/**
 * Run a camera service to capture images.
 *
 * @author Jun KATO
 */
public class RunCamera {

	public static void main(String[] args) {
		new RunCamera();
	}

	public RunCamera() {

		// Run a camera.
		// Let users select a device to capture images.
		final String identifier = (String) JOptionPane.showInputDialog(null,
				"Select a device to capture images.", "Device list",
				JOptionPane.QUESTION_MESSAGE, null, new VideoCaptureFactoryImpl()
						.queryIdentifiers(), null);
		VideoCaptureDS capture = new VideoCaptureDS();
		try {
			capture.setSource(identifier);
			capture.setSize(800, 600);
			capture.start();
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		if (capture.getWidth() != 800 ||
				capture.getHeight() != 600) {
			JOptionPane.showMessageDialog(null, "Please select 800x600 pixels for capturing image resolution the next dialog.");
			capture.showFormatDialog();
			if (capture.getWidth() != 800 ||
					capture.getHeight() != 600) {
				System.err.println("Failed to capture images in resolution of 800x600 pixels.");
			}
		}
		Camera camera = new Camera(capture);
		camera.start();

		// Make and show a window for showing captured image.
		DisposeOnCloseFrame frame = new DisposeOnCloseFrame(
				new ImageProviderPanel(camera)) {
			private static final long serialVersionUID = 1L;

			@Override public void dispose() {
				super.dispose();

				// Shutdown Matereal when the window is closed.
				Matereal.getInstance().dispose();
			}
		};
		frame.setFrameSize(camera.getWidth(), camera.getHeight());
	}
}

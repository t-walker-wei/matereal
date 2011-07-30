import javax.swing.JOptionPane;

import jp.digitalmuseum.mr.Matereal;
import jp.digitalmuseum.mr.gui.*;
import jp.digitalmuseum.mr.service.Camera;

/**
 * Run a camera service to capture images.
 *
 * @author Jun KATO
 */
public class RunCameraWithDialog {

	public static void main(String[] args) {
		new RunCameraWithDialog();
	}

	public RunCameraWithDialog() {

		// Run a camera.
		// Let users select a device to capture images.
		String identifier = (String) JOptionPane.showInputDialog(null,
				"Select a device to capture images.", "Device list",
				JOptionPane.QUESTION_MESSAGE, null, Camera.queryIdentifiers(), null);
		Camera camera = new Camera(identifier);
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

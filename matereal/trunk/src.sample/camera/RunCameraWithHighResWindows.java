package camera;


import javax.swing.JOptionPane;

import com.phybots.Phybots;
import com.phybots.gui.*;
import com.phybots.service.Camera;

import jp.digitalmuseum.capture.VideoCapture;
import jp.digitalmuseum.capture.VideoCaptureDS;
import jp.digitalmuseum.capture.VideoCaptureFactory;
import jp.digitalmuseum.capture.VideoCaptureFactoryImpl;

/**
 * Run a camera service to capture images.
 *
 * @author Jun Kato
 */
public class RunCameraWithHighResWindows {

	public static void main(String[] args) {
		new RunCameraWithHighResWindows();
	}

	public RunCameraWithHighResWindows() {

		// Run a camera.
		// Let users select a device to capture images.
		VideoCaptureFactory factory = new VideoCaptureFactoryImpl();
		String identifier = (String) JOptionPane.showInputDialog(null,
				"Select a device to capture images.", "Device list",
				JOptionPane.QUESTION_MESSAGE, null, factory.queryIdentifiers(), null);
		VideoCapture capture = factory.newInstance(identifier);
		try {
			capture.setSize(800, 600);
			capture.start();
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		if (capture instanceof VideoCaptureDS &&
				(capture.getWidth() != 800 ||
				capture.getHeight() != 600)) {
			JOptionPane.showMessageDialog(null, "Please select 800x600 pixels for capturing image resolution the next dialog.");
			((VideoCaptureDS) capture).showFormatDialog();
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

				// Shutdown Phybots when the window is closed.
				Phybots.getInstance().dispose();
			}
		};
		frame.setFrameSize(camera.getWidth(), camera.getHeight());
	}
}

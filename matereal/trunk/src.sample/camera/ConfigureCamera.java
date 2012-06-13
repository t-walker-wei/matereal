package camera;


import javax.swing.JFrame;

import com.phybots.Phybots;
import com.phybots.gui.*;
import com.phybots.service.Camera;


/**
 * Run a camera and show its configuration window.
 *
 * @author Jun Kato
 */
public class ConfigureCamera {

	public static void main(String[] args) {
		new ConfigureCamera();
	}

	public ConfigureCamera() {

		// Run a camera.
		final Camera camera = new Camera();
		camera.setSize(320, 240);
		camera.start();

		// Show a configuration window.
		final JFrame frame = new DisposeOnCloseFrame(
				new CoordProviderPanel(camera)) {
			private static final long serialVersionUID = 1L;

			@Override public void dispose() {
				super.dispose();

				// Shutdown Phybots when the window is closed.
				Phybots.getInstance().dispose();
			}
		};
		frame.setSize(500,410);
	}
}

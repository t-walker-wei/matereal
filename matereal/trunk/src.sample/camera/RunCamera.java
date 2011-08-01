package camera;
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
		Camera camera = new Camera();
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

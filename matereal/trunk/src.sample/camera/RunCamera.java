package camera;
import com.phybots.Phybots;
import com.phybots.gui.*;
import com.phybots.service.Camera;


/**
 * Run a camera service to capture images.
 *
 * @author Jun Kato
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

				// Shutdown Phybots when the window is closed.
				Phybots.getInstance().dispose();
			}
		};
		frame.setFrameSize(camera.getWidth(), camera.getHeight());
	}
}

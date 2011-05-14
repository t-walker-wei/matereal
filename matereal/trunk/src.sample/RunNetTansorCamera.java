import java.awt.image.BufferedImage;

import jp.digitalmuseum.mr.Matereal;
import jp.digitalmuseum.mr.entity.NetTansor;
import jp.digitalmuseum.mr.entity.Robot;
import jp.digitalmuseum.mr.gui.*;
import jp.digitalmuseum.mr.service.ImageProvider.ImageListener;
import jp.digitalmuseum.mr.task.Capture;

/**
 * Run a camera service to capture images.
 *
 * @author Jun KATO
 */
public class RunNetTansorCamera {

	public static void main(String[] args) {
		new RunNetTansorCamera();
	}

	public RunNetTansorCamera() {

		// Run the camera of NetTansor.
		final Robot robot = new NetTansor("http://192.168.1.111");
		final Capture capture = new Capture();
		capture.setInterval(1000/15);
		capture.assign(robot);
		capture.start();

		// Show a window when the first image is captured.
		capture.addImageListener(new ImageListener() {
			public void imageUpdated(BufferedImage image) {

				// Don't call this listener anymore.
				capture.removeImageListener(this);

				// Make and show a window for showing captured image.
				DisposeOnCloseFrame frame = new DisposeOnCloseFrame(
						new ImageProviderPanel(capture)) {
					private static final long serialVersionUID = 1L;

					@Override
					public void dispose() {
						super.dispose();
						capture.stop();
						Matereal.getInstance().dispose();
					}
				};
				frame.setFrameSize(image.getWidth(), image.getHeight());
			}
		});
	}
}

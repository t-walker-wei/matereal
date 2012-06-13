package misc;
import java.awt.image.BufferedImage;

import com.phybots.Phybots;
import com.phybots.entity.NetTansor;
import com.phybots.entity.Robot;
import com.phybots.gui.*;
import com.phybots.service.ImageProvider.ImageListener;
import com.phybots.task.Capture;


/**
 * Run a camera service to capture images.
 *
 * @author Jun Kato
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
						Phybots.getInstance().dispose();
					}
				};
				frame.setFrameSize(image.getWidth(), image.getHeight());
			}
		});
	}
}

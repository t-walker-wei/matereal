package sample.test;
import com.phybots.Phybots;
import com.phybots.entity.NetTansor;
import com.phybots.entity.Robot;
import com.phybots.gui.*;
import com.phybots.message.Event;
import com.phybots.message.EventListener;
import com.phybots.message.ImageUpdateEvent;
import com.phybots.task.Capture;


/**
 * Run a camera service.
 *
 * @author Jun Kato
 */
public class RunNetTansorCamera {

	public static void main(String[] args) {
		new RunNetTansorCamera();
	}

	public RunNetTansorCamera() {

		// Run a Net Tansor camera.
		final Robot robot = new NetTansor("Tansor", "http://192.168.32.92:8081");
		final Capture capture = new Capture();
		capture.assign(robot);
		capture.start();

		capture.addEventListener(new EventListener() {
			public void eventOccurred(Event e) {
				if (e instanceof ImageUpdateEvent) {

					// Make and show a window for showing captured image.
					DisposeOnCloseFrame frame = new DisposeOnCloseFrame(
							new ImageProviderPanel(capture)) {
						private static final long serialVersionUID = 1L;

						@Override public void dispose() {
							super.dispose();

							// Shutdown Phybots when the window is closed.
							Phybots.getInstance().dispose();
						}
					};
					frame.setFrameSize(capture.getWidth(), capture.getHeight());

					// Do not call this listener any more.
					capture.removeEventListener(this);
				}
			}
		});
	}
}

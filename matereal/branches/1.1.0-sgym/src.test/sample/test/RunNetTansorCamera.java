package sample.test;
import jp.digitalmuseum.mr.Matereal;
import jp.digitalmuseum.mr.entity.NetTansor;
import jp.digitalmuseum.mr.entity.Robot;
import jp.digitalmuseum.mr.gui.*;
import jp.digitalmuseum.mr.message.Event;
import jp.digitalmuseum.mr.message.EventListener;
import jp.digitalmuseum.mr.message.ImageUpdateEvent;
import jp.digitalmuseum.mr.task.Capture;

/**
 * Run a camera service.
 *
 * @author Jun KATO
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

							// Shutdown Matereal when the window is closed.
							Matereal.getInstance().dispose();
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

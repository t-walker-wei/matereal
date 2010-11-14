package matereal;


import jp.digitalmuseum.mr.*;
import jp.digitalmuseum.mr.gui.*;
import jp.digitalmuseum.mr.service.TestService;

/**
 * Run two services in different intervals.
 *
 * @author Jun KATO
 */
public class RunTestService {

	public static void main(String[] args) {
		new RunTestService();
	}

	public RunTestService() {

		// Run every 500ms.
		TestService testService = new TestService();
		testService.setInterval(500);
		testService.start();

		// Run every 1000ms.
		TestService testService2 = new TestService();
		testService2.setInterval(1000);
		testService2.start();

		// Show a service monitor.
		final DisposeOnCloseFrame frame = new DisposeOnCloseFrame(
				new MonitorPanel()) {
			private static final long serialVersionUID = 1L;

			@Override public void dispose() {
				super.dispose();

				// Shutdown Matereal when the window is closed.
				Matereal.getInstance().dispose();
			}
		};
		frame.setFrameSize(640, 480);
	}
}
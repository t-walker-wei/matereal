

import com.phybots.*;
import com.phybots.gui.*;
import com.phybots.service.ServiceGroup;
import com.phybots.service.TestService;


/**
 * Run two services in different intervals.
 *
 * @author Jun Kato
 */
public class RunTestService {

	public static void main(String[] args) {
		new RunTestService();
	}

	public RunTestService() {

		// Show a service monitor.
		final DisposeOnCloseFrame frame = new DisposeOnCloseFrame(
				new ServiceMonitorPanel()) {
			private static final long serialVersionUID = 1L;

			@Override public void dispose() {
				super.dispose();

				// Shutdown Phybots when the window is closed.
				Phybots.getInstance().dispose();
			}
		};
		frame.setFrameSize(640, 480);

		// Run every 500ms.
		TestService testService = new TestService();
		testService.setInterval(500);
		testService.start();

		// Run every 1000ms.
		TestService testService2 = new TestService();
		testService2.setInterval(1000);
		testService2.start();

		// Run every 1500ms.
		ServiceGroup serviceGroup = new ServiceGroup();
		serviceGroup.add(new TestService());
		serviceGroup.add(new TestService());
		serviceGroup.setInterval(1500);
		serviceGroup.start();
	}
}
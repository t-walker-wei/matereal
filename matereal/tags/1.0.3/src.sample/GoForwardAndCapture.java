import jp.digitalmuseum.mr.Matereal;
import jp.digitalmuseum.mr.entity.NetTansor;
import jp.digitalmuseum.mr.entity.Robot;
import jp.digitalmuseum.mr.gui.DisposeOnCloseFrame;
import jp.digitalmuseum.mr.gui.ImageProviderPanel;
import jp.digitalmuseum.mr.task.Capture;
import jp.digitalmuseum.mr.task.GoForward;

/**
 * Test code to go forward and capture images simultaneously.
 *
 * @author Jun KATO
 */
public class GoForwardAndCapture {

	public static void main(String[] args) {
		new GoForwardAndCapture();
	}

	public GoForwardAndCapture() {
		Robot robot = new NetTansor("Tansor", "http://192.168.32.92:8081");
		GoForward gf = new GoForward();
		Capture cap = new Capture();

		if (gf.assign(robot)) {
			gf.start();
			if (cap.assign(robot)) {
				cap.start();

				// NetTansor goes forward and captures images simultaneously.
				new DisposeOnCloseFrame(new ImageProviderPanel(cap)) {
					private static final long serialVersionUID = 1L;
					public void dispose() {
						super.dispose();
						Matereal.getInstance().dispose();
					}
				}.setFrameSize(cap.getWidth(), cap.getHeight());

				// The tasks continue till the window is closed.
			};
		}
	}
}

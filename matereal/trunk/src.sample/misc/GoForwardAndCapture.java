package misc;
import com.phybots.Phybots;
import com.phybots.entity.NetTansor;
import com.phybots.entity.Robot;
import com.phybots.gui.DisposeOnCloseFrame;
import com.phybots.gui.ImageProviderPanel;
import com.phybots.task.Capture;
import com.phybots.task.GoForward;


/**
 * Test code to go forward and capture images simultaneously.
 *
 * @author Jun Kato
 */
public class GoForwardAndCapture {

	public static void main(String[] args) {
		new GoForwardAndCapture();
	}

	public GoForwardAndCapture() {
		Robot robot = new NetTansor("http://192.168.32.92:8081");
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
						Phybots.getInstance().dispose();
					}
				}.setFrameSize(cap.getWidth(), cap.getHeight());

				// The tasks continue till the window is closed.
			};
		}
	}
}

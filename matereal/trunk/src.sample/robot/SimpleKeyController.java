package robot;
import java.awt.Graphics2D;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import com.phybots.Phybots;
import com.phybots.entity.PhysicalRobot;
import com.phybots.gui.DrawableFrame;
import com.phybots.resource.WheelsController;


/**
 * Show a controller GUI for a robot.
 *
 * @author Jun Kato
 */
public class SimpleKeyController {
	private final DrawableFrame drawableFrame;
	private final PhysicalRobot robot;
	private final WheelsController wheels;

	public static void main(String[] args) {
		new SimpleKeyController();
	}

	public SimpleKeyController() {
		super();

		robot = RobotInfo.getRobot();
		robot.connect();
		wheels = robot.requestResource(WheelsController.class, this);

		drawableFrame = new DrawableFrame() {
			private static final long serialVersionUID = 1L;

			@Override
			public void dispose() {
				super.dispose();
				robot.freeResource(wheels, this);
				Phybots.getInstance().dispose();
			}
			public void paint2D(Graphics2D g) {
				g.setColor(getBackground());
				g.fillRect(0, 0, getWidth(), getHeight());
				g.setColor(getForeground());
				g.drawString("Status: "+wheels.getStatus().toString(), 10, 20);
			}
		};
		drawableFrame.setFrameSize(320, 240);
		drawableFrame.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				switch (e.getKeyCode()) {
				case KeyEvent.VK_UP:
				case KeyEvent.VK_KP_UP:
					goForward();
					break;
				case KeyEvent.VK_LEFT:
				case KeyEvent.VK_KP_LEFT:
					spinLeft();
					break;
				case KeyEvent.VK_RIGHT:
				case KeyEvent.VK_KP_RIGHT:
					spinRight();
					break;
				case KeyEvent.VK_DOWN:
				case KeyEvent.VK_KP_DOWN:
					goBackward();
					break;
				case KeyEvent.VK_ESCAPE:
				default:
					stop();
					break;
				}
				drawableFrame.repaint();
			}
		});
	}

	private void stop() {
		wheels.stopWheels();
	}

	private void goForward() {
		wheels.goForward();
	}

	private void goBackward() {
		wheels.goBackward();
	}

	private void spinRight() {
		wheels.spinRight();
	}

	private void spinLeft() {
		wheels.spinLeft();
	}
}
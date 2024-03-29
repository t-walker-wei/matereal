

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import jp.digitalmuseum.mr.Matereal;
import jp.digitalmuseum.mr.entity.MindstormsNXT;
import jp.digitalmuseum.mr.entity.Robot;
import jp.digitalmuseum.mr.gui.DisposeOnCloseFrame;
import jp.digitalmuseum.mr.gui.DrawableFrame;
import jp.digitalmuseum.mr.gui.ImageProviderPanel;
import jp.digitalmuseum.mr.hakoniwa.Hakoniwa;
import jp.digitalmuseum.mr.hakoniwa.HakoniwaRobot;
import jp.digitalmuseum.mr.resource.DifferentialWheelsController;
import jp.digitalmuseum.mr.resource.Wheels.SPIN;
import jp.digitalmuseum.mr.service.Service;
import jp.digitalmuseum.mr.service.ServiceAbstractImpl;
import jp.digitalmuseum.utils.Position;

/**
 * Show a controller GUI for a robot.
 * Set focus to the controller GUI and press arrow keys to navigate a robot manually.
 *
 * @author Jun KATO
 */
public class KeyController {
	private enum KEY { UP, DOWN, LEFT, RIGHT, ESC, NONE }
	private KEY key;
	private static final boolean DEBUG = false;
	private final DrawableFrame drawableFrame;
	private DisposeOnCloseFrame hakoniwaFrame;
	private DifferentialWheelsController wheels;
	private SPIN spin;
	private int speed;
	private int innerSpeed;

	public static void main(String[] args) {
		new KeyController();
	}

	public KeyController() {
		super();

		final Robot robot;
		if (DEBUG) {
			robot = initHakoniwa();
		} else {
			robot = new MindstormsNXT("btspp://00165305B308");
		}
		wheels = robot.requestResource(DifferentialWheelsController.class, this);

		key = KEY.NONE;
		spin = null;
		speed = 0;
		innerSpeed = 100;

		drawableFrame = new DrawableFrame() {
			private static final long serialVersionUID = 1L;

			@Override
			public void dispose() {
				super.dispose();
				robot.freeResource(wheels, KeyController.this);
				if (hakoniwaFrame != null) {
					hakoniwaFrame.dispose();
				}
				Matereal.getInstance().dispose();
			}
			public void paint2D(Graphics2D g) {
				g.setColor(Color.white);
				g.fillRect(0, 0, getWidth(), getHeight());
				g.setColor(Color.black);
				g.drawString(String.format("Power: %d (Inner wheel %d%%)", speed, innerSpeed), 10, 20);
				g.drawString("Spinning direction: "+(spin == null ? "STRAIGHT" : spin.toString()), 10, 40);
				g.drawRect(getFrameWidth()/2-100, getFrameHeight()/2-50, 10, 100);
				g.drawRect(getFrameWidth()/2+90, getFrameHeight()/2-50, 10, 100);
				g.setColor(Color.lightGray);
				g.drawOval(getFrameWidth()/2-30, getFrameHeight()/2-30, 60, 60);
				g.drawRect(getFrameWidth()/2-15, getFrameHeight()/2-10, 5, 20);
				g.drawRect(getFrameWidth()/2+10, getFrameHeight()/2-10, 5, 20);
				g.setColor(Color.red);
				g.drawString("Pow.left", getFrameWidth()/2-100, getFrameHeight()/2+70);
				g.drawString("Pow.right", getFrameWidth()/2+90, getFrameHeight()/2+70);
				fillRect(g, getFrameWidth()/2-99, getFrameHeight()/2, 9, -(
						spin == SPIN.LEFT ? speed*innerSpeed/100 : speed)/2);
				fillRect(g, getFrameWidth()/2+91, getFrameHeight()/2, 9, -(
						spin == SPIN.RIGHT ? speed*innerSpeed/100 : speed)/2);
			}
			private void fillRect(Graphics2D g, int x, int y, int width, int height) {
				g.fillRect(
						width  > 0 ? x : x+width +1,
						height > 0 ? y : y+height+1,
						width  > 0 ? width  : -width,
						height > 0 ? height : -height);
			}
		};
		drawableFrame.setFrameSize(320, 240);
		drawableFrame.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				switch (e.getKeyCode()) {
				case KeyEvent.VK_UP:
				case KeyEvent.VK_KP_UP:
					key = KEY.UP;
					break;
				case KeyEvent.VK_DOWN:
				case KeyEvent.VK_KP_DOWN:
					key = KEY.DOWN;
					break;
				case KeyEvent.VK_LEFT:
				case KeyEvent.VK_KP_LEFT:
					key = KEY.LEFT;
					break;
				case KeyEvent.VK_RIGHT:
				case KeyEvent.VK_KP_RIGHT:
					key = KEY.RIGHT;
					break;
				case KeyEvent.VK_ESCAPE:
				default:
					key = KEY.ESC;
					break;
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
				key = KEY.NONE;
			}
		});

		Service service = new ServiceAbstractImpl() {
			public void run() {
				switch (key) {
				case UP:
					speedUp();
					break;
				case DOWN:
					speedDown();
					break;
				case LEFT:
					turnLeft();
					break;
				case RIGHT:
					turnRight();
					break;
				case ESC:
					KeyController.this.stop();
					break;
				case NONE:
					damp();
					break;
				}
			}
		};
		service.setInterval(100);
		service.start();
	}

	private Robot initHakoniwa() {

		// Initialize hakoniwa.
		final Hakoniwa hakoniwa = new Hakoniwa();
		hakoniwa.setAntialiased(true);
		hakoniwa.setViewportSize(400, 320);
		hakoniwa.start();

		// Make and show a window for showing captured image.
		hakoniwaFrame = new DisposeOnCloseFrame(new ImageProviderPanel(hakoniwa));
		hakoniwaFrame.setTitle("Hakoniwa viewer");
		hakoniwaFrame.setFrameSize(hakoniwa.getWidth(), hakoniwa.getHeight());

		// Instantiate a robot in the hakoniwa.
		return new HakoniwaRobot(
					"test",
					new Position(
							hakoniwa.getRealWidth()/2,
							hakoniwa.getRealHeight()/2));
	}

	private void drive() {
		if (speed <= 0) {
			wheels.stopWheels();
		} else if (spin == null) {
			wheels.setSpeed(speed);
			wheels.goForward();
		} else {
			wheels.setRotationSpeed(speed);
			wheels.curve(spin, innerSpeed);
		}
		drawableFrame.repaint();
	}

	private void stop() {
		speed = 0;
		drive();
	}

	private void damp() {
		if (innerSpeed < 100 || speed > 0) {
			if (innerSpeed < 100) {
				innerSpeed += 10;
			}
			if (speed > 0) {
				speed -= 5;
			}
			drive();
		}
	}

	private void speedUp() {
		if (speed < 100) speed += 5;
		spin = null;
		innerSpeed = 100;
		drive();
	}

	private void speedDown() {
		if (speed > 0) speed -= 5;
		spin = null;
		innerSpeed = 100;
		drive();
	}

	private void turnLeft() {
		if (spin == null) {
			spin = SPIN.LEFT;
			innerSpeed = 90;
		} else {
			switch (spin) {
			case RIGHT:
				spin = null;
				break;
			case LEFT:
				if (innerSpeed > -100) {
					innerSpeed -= 10;
				}
				break;
			}
		}
		drive();
	}

	private void turnRight() {
		if (spin == null) {
			spin = SPIN.RIGHT;
			innerSpeed = 90;
		} else {
			switch (spin) {
			case LEFT:
				spin = null;
				break;
			case RIGHT:
				if (innerSpeed > -100) {
					innerSpeed -= 10;
				}
				break;
			}
		}
		drive();
	}
}

package robot.noopy;
import java.awt.Graphics2D;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.SwingUtilities;

import com.phybots.Phybots;
import com.phybots.entity.Noopy2;
import com.phybots.entity.Noopy2.DCMotorController;
import com.phybots.entity.Noopy2.Port;
import com.phybots.gui.DrawableFrame;

import robot.RobotInfo;


/**
 * キーボード操作に応じてDC3ポートに接続されたDCモータの回転方向・速度を切り替える。<br />
 * <b>※キーを押しすぎるとコマンドの送りすぎで固まる。</b>
 *
 * @author Jun Kato
 */
public class NoopyWithDCMotorExtension {
	private DrawableFrame drawableFrame;
	private Noopy2 robot;
	private DCMotorController motor;
	private int speed;

	public static void main(String[] args) {
		new NoopyWithDCMotorExtension();
	}

	public NoopyWithDCMotorExtension() {
		super();

		robot = RobotInfo.getNoopyRobot();
		robot.addExtension(DCMotorController.class, Port.DC3);
		motor = robot.requestResource(DCMotorController.class, this);
		speed = motor.getSpeed();
		robot.connect();

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				drawableFrame = new DrawableFrame() {
					private static final long serialVersionUID = 1L;

					@Override
					public void dispose() {
						super.dispose();
						robot.freeResource(motor, NoopyWithDCMotorExtension.this);
						Phybots.getInstance().dispose();
					}

					public void paint2D(Graphics2D g) {
						g.setColor(getBackground());
						g.fillRect(0, 0, getWidth(), getHeight());
						g.setColor(getForeground());
						g.drawString(String.format(
								"Status: %s (speed: %d)",
								motor.getStatus().toString(),
								motor.getSpeed()
							), 10, 20);
					}
				};
				drawableFrame.setFrameSize(320, 240);
				drawableFrame.addKeyListener(new KeyAdapter() {

					@Override
					public void keyPressed(KeyEvent e) {
						switch (e.getKeyCode()) {
						case KeyEvent.VK_LEFT:
						case KeyEvent.VK_KP_LEFT:
							ccw();
							break;
						case KeyEvent.VK_RIGHT:
						case KeyEvent.VK_KP_RIGHT:
							clw();
							break;
						case KeyEvent.VK_UP:
						case KeyEvent.VK_KP_UP:
							spdUp();
							break;
						case KeyEvent.VK_DOWN:
						case KeyEvent.VK_KP_DOWN:
							spdDown();
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
		});
	}

	/**
	 * 時計回り
	 */
	private void clw() {
		motor.clw();
	}

	/**
	 * 反時計回り
	 */
	private void ccw() {
		motor.ccw();
	}

	/**
	 * 停止
	 */
	private void stop() {
		motor.stop();
	}

	/**
	 * スピードアップ
	 */
	private void spdUp() {
		speed += 10;
		if (speed > 255) {
			speed = 255;
		}
		motor.setSpeed(speed);
	}

	/**
	 * スピードダウン
	 */
	private void spdDown() {
		speed -= 10;
		if (speed < 10) {
			speed = 10;
		}
		motor.setSpeed(speed);
	}
}
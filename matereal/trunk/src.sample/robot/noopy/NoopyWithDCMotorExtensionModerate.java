package robot.noopy;
import java.awt.Graphics2D;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.SwingUtilities;

import com.phybots.Phybots;
import com.phybots.entity.Noopy2;
import com.phybots.entity.Resource;
import com.phybots.entity.Noopy2.DCMotorController;
import com.phybots.entity.Noopy2.Port;
import com.phybots.entity.Noopy2.DCMotorController.Command;
import com.phybots.gui.DrawableFrame;
import com.phybots.task.TaskAbstractImpl;

import robot.RobotInfo;


/**
 * キーボード操作に応じてDC3ポートに接続されたDCモータの回転方向・速度を切り替える。<br />
 * <b>※キーの押しすぎ対応版。</b>
 *
 * @author Jun Kato
 */
public class NoopyWithDCMotorExtensionModerate {
	private DrawableFrame drawableFrame;
	private Noopy2 robot;
	private MotorTask motorTask;

	public static void main(String[] args) {
		new NoopyWithDCMotorExtensionModerate();
	}

	public NoopyWithDCMotorExtensionModerate() {
		super();

		// ロボット初期化
		robot = RobotInfo.getNoopyRobot();
		robot.addExtension(DCMotorController.class, Port.DC3);
		robot.connect();

		// 100msに一度しかコマンドを送らないように配慮
		motorTask = new MotorTask();
		motorTask.setInterval(100);
		motorTask.assign(robot);
		motorTask.start();

		// 以下GUI
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				drawableFrame = new DrawableFrame() {
					private static final long serialVersionUID = 1L;

					@Override
					public void dispose() {
						super.dispose();
						motorTask.stop();
						Phybots.getInstance().dispose();
					}

					public void paint2D(Graphics2D g) {
						g.setColor(getBackground());
						g.fillRect(0, 0, getWidth(), getHeight());
						g.setColor(getForeground());
						g.drawString(String.format(
								"Status: %s (speed: %d)",
								motorTask.getStatus().toString(),
								motorTask.getSpeed()
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
					}
				});
			}
		});
	}

	/**
	 * 時計回り
	 */
	private void clw() {
		motorTask.clw();
	}

	/**
	 * 反時計回り
	 */
	private void ccw() {
		motorTask.ccw();
	}

	/**
	 * 停止
	 */
	private void stop() {
		motorTask.stp();
	}

	/**
	 * スピードアップ
	 */
	private void spdUp() {
		motorTask.spdUp();
	}

	/**
	 * スピードダウン
	 */
	private void spdDown() {
		motorTask.spdDown();
	}

	/**
	 * DCモーターを操作するタスク
	 */
	private class MotorTask extends TaskAbstractImpl {
		private static final long serialVersionUID = 1L;
		private Command command = Command.STP;
		private Command newCommand = command;
		private int speed = 100;
		private int newSpeed = speed;

		/**
		 * このタスクが要求するリソース(DCモーター)を返すメソッド
		 */
		@Override
		public List<Class<? extends Resource>> getRequirements() {
			List<Class<? extends Resource>> requirements =
				super.getRequirements();
			requirements.add(DCMotorController.class);
			return requirements;
		}

		/**
		 * タスク開始後、定期的に実行される処理が書かれたメソッド
		 */
		public void run() {
			if (speed != newSpeed || command != newCommand) {
				getResourceMap().get(DCMotorController.class).drive(
						newCommand, newSpeed);
				command = newCommand;
				speed = newSpeed;
				drawableFrame.repaint();
			}
		}

		public void clw() {
			newCommand = Command.CLW;
		}

		public void ccw() {
			newCommand = Command.CCW;
		}

		public void stp() {
			newCommand = Command.STP;
		}

		public void spdUp() {
			newSpeed = speed + 10;
			if (newSpeed > 255) {
				newSpeed = 255;
			}
		}

		public void spdDown() {
			newSpeed = speed - 10;
			if (newSpeed < 10) {
				newSpeed = 10;
			}
		}

		public Command getStatus() {
			return command;
		}

		public int getSpeed() {
			return speed;
		}
	}
}
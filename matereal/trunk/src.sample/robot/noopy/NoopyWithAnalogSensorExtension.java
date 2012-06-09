package robot.noopy;
import java.util.List;

import com.phybots.Phybots;
import com.phybots.entity.Noopy2;
import com.phybots.entity.Resource;
import com.phybots.entity.Noopy2.AnalogSensor;
import com.phybots.entity.Noopy2.Port;
import com.phybots.task.Task;
import com.phybots.task.TaskAbstractImpl;

import robot.RobotInfo;



public class NoopyWithAnalogSensorExtension {

	public static void main(String[] args) {
		new NoopyWithAnalogSensorExtension();
	}

	public NoopyWithAnalogSensorExtension() {

		// アナログセンサつきのNoopy
		Noopy2 noopy = RobotInfo.getNoopyRobot();
		noopy.addExtension(AnalogSensor.class, Port.AN0);

		// Noopyにセンサの値を読み取り続けるタスクを割り当てる
		Task readAnalogSensor = new ReadAnalogSensor();
		if (readAnalogSensor.assign(noopy)) {

			// 100msに一度センサの値を読み取る
			readAnalogSensor.setInterval(100);

			// タスク開始
			readAnalogSensor.start();

			// 適当なところでタスク停止
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			readAnalogSensor.stop();
		}

		// 最後にMaterealをシャットダウン
		Phybots.getInstance().dispose();
	}

	private static class ReadAnalogSensor extends TaskAbstractImpl {
		private static final long serialVersionUID = 1L;
		private AnalogSensor analogSensor;

		/**
		 * このタスクが要求するリソース(アナログセンサ)を返すメソッド
		 */
		@Override
		public List<Class<? extends Resource>> getRequirements() {
			List<Class<? extends Resource>> requirements =
				super.getRequirements();
			requirements.add(AnalogSensor.class);
			return requirements;
		}

		/**
		 * このタスクが始まるときの処理が書かれたメソッド
		 */
		protected void onStart() {
			this.analogSensor = getResourceMap().get(AnalogSensor.class);
		}

		/**
		 * タスク開始後、定期的に実行される処理が書かれたメソッド
		 */
		public void run() {
			int value = analogSensor.readValue();
			if (value >= 0) {
				System.out.println(value);
			}
		}
	}
}

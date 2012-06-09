package robot.noopy;
import java.util.List;

import com.phybots.Phybots;
import com.phybots.entity.Noopy2;
import com.phybots.entity.Resource;
import com.phybots.entity.Noopy2.Accelerometer;
import com.phybots.task.Task;
import com.phybots.task.TaskAbstractImpl;

import robot.RobotInfo;



public class NoopyWithAccelerometerExtension {

	public static void main(String[] args) {
		new NoopyWithAccelerometerExtension();
	}

	public NoopyWithAccelerometerExtension() {

		// 加速度センサつきのNoopy
		Noopy2 noopy = RobotInfo.getNoopyRobot();
		noopy.addExtension(Accelerometer.class);

		// Noopyにセンサの値を読み取り続けるタスクを割り当てる
		Task readAcceleration = new ReadAcceleration();
		if (readAcceleration.assign(noopy)) {

			// 100msに一度センサの値を読み取る
			readAcceleration.setInterval(100);

			// タスク開始
			readAcceleration.start();

			// 適当なところでタスク停止
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			readAcceleration.stop();
		}

		// 最後にMaterealをシャットダウン
		Phybots.getInstance().dispose();
	}

	private static class ReadAcceleration extends TaskAbstractImpl {
		private static final long serialVersionUID = 1L;
		private Accelerometer accelerometer;
		private float[] acceleration;

		/**
		 * このタスクが要求するリソース(加速度センサ)を返すメソッド
		 */
		@Override
		public List<Class<? extends Resource>> getRequirements() {
			List<Class<? extends Resource>> requirements =
				super.getRequirements();
			requirements.add(Accelerometer.class);
			return requirements;
		}

		/**
		 * このタスクが始まるときの処理が書かれたメソッド
		 */
		protected void onStart() {
			this.accelerometer = getResourceMap().get(Accelerometer.class);
			this.acceleration = new float[3];
		}

		/**
		 * タスク開始後、定期的に実行される処理が書かれたメソッド
		 */
		public void run() {
			accelerometer.readValues(acceleration);
			System.out.println(String.format(
					"%3.3fmg %3.3fmg %3.3fmg",
					acceleration[0],
					acceleration[1],
					acceleration[2]));
		}
	}
}

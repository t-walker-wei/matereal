package robot.noopy;
import java.util.List;

import robot.RobotInfo;

import jp.digitalmuseum.mr.Matereal;
import jp.digitalmuseum.mr.entity.Noopy2;
import jp.digitalmuseum.mr.entity.Noopy2.BendSensor;
import jp.digitalmuseum.mr.entity.Resource;
import jp.digitalmuseum.mr.task.Task;
import jp.digitalmuseum.mr.task.TaskAbstractImpl;


public class NoopyWithBendSensorExtension {

	public static void main(String[] args) {
		new NoopyWithBendSensorExtension();
	}

	public NoopyWithBendSensorExtension() {

		// ベンドセンサつきのNoopy
		Noopy2 noopy = RobotInfo.getRobot();
		noopy.addExtension(BendSensor.class);

		// Noopyにセンサの値を読み取り続けるタスクを割り当てる
		Task readBendSensor = new ReadBendSensor();
		if (readBendSensor.assign(noopy)) {

			// 100msに一度センサの値を読み取る
			readBendSensor.setInterval(100);

			// タスク開始
			readBendSensor.start();

			// 適当なところでタスク停止
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			readBendSensor.stop();
		}

		// 最後にMaterealをシャットダウン
		Matereal.getInstance().dispose();
	}

	private static class ReadBendSensor extends TaskAbstractImpl {
		private static final long serialVersionUID = 1L;
		private BendSensor bendSensor;

		/**
		 * このタスクが要求するリソース(ベンドセンサ)を返すメソッド
		 */
		@Override
		public List<Class<? extends Resource>> getRequirements() {
			List<Class<? extends Resource>> requirements =
				super.getRequirements();
			requirements.add(BendSensor.class);
			return requirements;
		}

		/**
		 * このタスクが始まるときの処理が書かれたメソッド
		 */
		protected void onStart() {
			this.bendSensor = getResourceMap().get(BendSensor.class);
		}

		/**
		 * タスク開始後、定期的に実行される処理が書かれたメソッド
		 */
		public void run() {
			System.out.println(bendSensor.readValue());
		}
	}
}

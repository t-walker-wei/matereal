import jp.digitalmuseum.mr.Matereal;
import jp.digitalmuseum.mr.message.Event;
import jp.digitalmuseum.mr.message.EventListener;

/**
 * Show Matereal Debug Window.
 *
 * @author Jun KATO
 */
public class ShowDebugFrame {

	public static void main(String[] args) {

		Matereal.getInstance().showDebugFrame();

		Matereal.getInstance().addEventListener(new EventListener() {
			public void eventOccurred(Event e) {
				System.out.println(e.toString());
			}
		});
	}
}

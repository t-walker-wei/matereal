import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

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

		Matereal.getInstance().getDebugFrame().addWindowListener(
				new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				Matereal.getInstance().dispose();
			}
		});

		Matereal.getInstance().addEventListener(new EventListener() {
			public void eventOccurred(Event e) {
				System.out.println(e.toString());
			}
		});

		Matereal.getInstance().showDebugFrame();
	}
}

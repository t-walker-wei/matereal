import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import com.phybots.Phybots;
import com.phybots.message.Event;
import com.phybots.message.EventListener;


/**
 * Show Phybots Debug Window.
 *
 * @author Jun Kato
 */
public class ShowDebugFrame {

	public static void main(String[] args) {

		Phybots.getInstance().getDebugFrame().addWindowListener(
				new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				Phybots.getInstance().dispose();
			}
		});

		Phybots.getInstance().addEventListener(new EventListener() {
			public void eventOccurred(Event e) {
				System.out.println(e.toString());
			}
		});

		Phybots.getInstance().showDebugFrame();
	}
}

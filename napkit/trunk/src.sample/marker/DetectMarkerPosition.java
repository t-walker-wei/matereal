package marker;
import javax.swing.JFrame;

import com.phybots.Phybots;
import com.phybots.gui.DisposeOnCloseFrame;
import com.phybots.message.Event;
import com.phybots.message.EventListener;
import com.phybots.message.LocationUpdateEvent;
import com.phybots.service.Camera;
import com.phybots.service.MarkerDetector;
import com.phybots.utils.ScreenPosition;

import jp.digitalmuseum.napkit.NapDetectionResult;
import jp.digitalmuseum.napkit.NapMarker;
import jp.digitalmuseum.napkit.gui.MarkerDetectorPanel;

/**
 * Run marker detection and print its results.
 *
 * @author Jun Kato
 */
public class DetectMarkerPosition {

	public static void main(String[] args) {
		new DetectMarkerPosition();
	}

	public DetectMarkerPosition() {

		// Run a camera.
		Camera camera = new Camera();
		camera.start();

		// Run a marker detector.
		final MarkerDetector detector = new MarkerDetector();
		detector.setImageProvider(camera);
		detector.start();

		// Detect a marker.
		final NapMarker marker = MarkerInfo.getEntityMarker();
		detector.addMarker(marker);

		// Print position of the marker.
		detector.addEventListener(new EventListener() {

			public void eventOccurred(Event e) {
				if (e instanceof LocationUpdateEvent) {
					NapDetectionResult result = detector.getResult(marker);
					if (result != null) {
						ScreenPosition p = result.getPosition();
						System.out.println(String.format("%s (Confidence: %3g)",
								p, result.getConfidence()));
					}
				} else {
					System.out.println(e);
				}
			}
		});

		// Show a configuration window.
		JFrame configFrame = new DisposeOnCloseFrame(new MarkerDetectorPanel(detector)) {
			private static final long serialVersionUID = 1L;

			@Override public void dispose() {
				super.dispose();
				Phybots.getInstance().dispose();
			}
		};
		configFrame.setSize(640, 480);
	}
}

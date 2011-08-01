package marker;
import javax.swing.JFrame;

import jp.digitalmuseum.mr.Matereal;
import jp.digitalmuseum.mr.gui.DisposeOnCloseFrame;
import jp.digitalmuseum.mr.message.Event;
import jp.digitalmuseum.mr.message.EventListener;
import jp.digitalmuseum.mr.message.LocationUpdateEvent;
import jp.digitalmuseum.mr.service.MarkerDetector;
import jp.digitalmuseum.mr.service.Camera;
import jp.digitalmuseum.napkit.NapDetectionResult;
import jp.digitalmuseum.napkit.NapMarker;
import jp.digitalmuseum.napkit.gui.MarkerDetectorPanel;
import jp.digitalmuseum.utils.ScreenPosition;

/**
 * Run marker detection and print its results.
 *
 * @author Jun KATO
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
				Matereal.getInstance().dispose();
			}
		};
		configFrame.setSize(640, 480);
	}
}

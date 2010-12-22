package sample;

import jp.digitalmuseum.capture.VideoCapture;
import jp.digitalmuseum.capture.VideoCaptureQT;

/** Capture images with QuickTime. */
public class CaptureQT extends AbstractCapture {
	private static final long serialVersionUID = 1L;

	public static void main(String[] argv) {
		new CaptureQT();
	}

	public CaptureQT() {

		// Initialize capture object.
		final VideoCapture capture = new VideoCaptureQT();

		// Start to capture.
		initialize(capture);
		System.out.println(capture.getIdentifier());
	}

}

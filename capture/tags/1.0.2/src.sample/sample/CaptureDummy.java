package sample;

import jp.digitalmuseum.capture.VideoCapture;
import jp.digitalmuseum.capture.VideoCaptureDummy;


/** This simply shows a frame with its background painted in black. */
public class CaptureDummy extends AbstractCapture {
	private static final long serialVersionUID = 1L;

	public static void main(String[] argv) {
		new CaptureDummy();
	}

	public CaptureDummy() {
		final VideoCapture capture = new VideoCaptureDummy();
		initialize(capture);
	}

}

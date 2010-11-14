package sample;

import de.humatic.dsj.DSFilterInfo;
import de.humatic.dsj.DSJException;
import de.humatic.dsj.DSMediaType;
import de.humatic.dsj.DSFilterInfo.DSPinInfo;

import jp.digitalmuseum.capture.VideoCapture;
import jp.digitalmuseum.capture.VideoCaptureDS;

/** Capture images with DirectShow. */
public class CaptureDS extends AbstractCapture {
	private static final long serialVersionUID = 1L;

	public static void main(String[] argv) {
		new CaptureDS();
	}

	public CaptureDS() {

		// List up available devices.
		System.out.println("Available devices:");
		for (DSFilterInfo fi : VideoCaptureDS.queryDevices()) {
			System.out.print("  ");
			System.out.println(fi.getName());
			System.out.print("   ");
			System.out.println(fi.getPath());
			DSPinInfo[] pins;
			try {
				pins = fi.getDownstreamPins();
			} catch (DSJException e) {
				pins = null;
			}
			if (pins == null) {
				System.out.println("    (no pins available)");
			} else
				for (DSPinInfo pin : pins) {
					System.out.print("    ID=");
					System.out.print(pin.getID());
					System.out.print(":");
					System.out.println(pin.getName());
					for (DSMediaType format : pin.getFormats()) {
						System.out.print("      ");
						System.out.println(format);
					}
				}
		}

		// Configure capture object.
		final VideoCapture capture = new VideoCaptureDS();
		try {
			capture.setSize(800, 600);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Start to capture.
		initialize(capture);
		System.out.println("Capturing video with " + capture.getIdentifier()
				+ "\nat " + capture.getWidth() + "x" + capture.getHeight()
				+ ", " + capture.getFrameRate() + "fps.");
	}

}

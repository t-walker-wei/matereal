package sample;

import javax.swing.JOptionPane;

import de.humatic.dsj.DSFilterInfo;
import de.humatic.dsj.DSJException;
import de.humatic.dsj.DSMediaType;
import de.humatic.dsj.DSFilterInfo.DSPinInfo;

import jp.digitalmuseum.capture.VideoCapture;
import jp.digitalmuseum.capture.VideoCaptureDS;
import jp.digitalmuseum.capture.VideoCaptureFactoryImpl;

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

			// Let users select a device to capture images.
			final String identifier = (String) JOptionPane.showInputDialog(this,
					"Select a device to capture images.", "Device list",
					JOptionPane.QUESTION_MESSAGE, null, new VideoCaptureFactoryImpl()
							.queryIdentifiers(), null);
			if ((identifier != null) && (identifier.length() > 0)) {
				capture.setSource(identifier);
			}
			capture.setSize(800, 600);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Start to capture.
		initialize(capture);
		if (capture.getWidth() != 800 ||
				capture.getHeight() != 600) {
			JOptionPane.showMessageDialog(this, "Please select 800x600 pixels for capturing image resolution in the next dialog.");
			((VideoCaptureDS) capture).showFormatDialog();
			if (capture.getWidth() != 800 ||
					capture.getHeight() != 600) {
				System.err.println("Failed to capture images in resolution of 800x600 pixels.");
				capture.stop();
				return;
			}
			setFrameSize(capture.getWidth(), capture.getHeight());
		}
		System.out.println("Capturing images with " + capture.getIdentifier()
				+ "\nat " + capture.getWidth() + "x" + capture.getHeight()
				+ ", " + capture.getFrameRate() + "fps.");
	}

}

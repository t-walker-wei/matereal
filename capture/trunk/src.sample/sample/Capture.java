package sample;

import javax.swing.JOptionPane;

import jp.digitalmuseum.capture.VideoCaptureFactoryImpl;

public class Capture extends AbstractCapture {
	private static final long serialVersionUID = 1L;

	public static void main(String[] argv) {
		new Capture();
	}

	public Capture() {

		// Let users select a device to capture images.
		final String identifier = (String) JOptionPane.showInputDialog(this,
				"Select a device to capture images.", "Device list",
				JOptionPane.QUESTION_MESSAGE, null, new VideoCaptureFactoryImpl()
						.queryIdentifiers(), null);
		if ((identifier != null) && (identifier.length() > 0)) {
			initialize(new VideoCaptureFactoryImpl().newInstance(identifier));
		} else {
			dispose();
		}
	}
}

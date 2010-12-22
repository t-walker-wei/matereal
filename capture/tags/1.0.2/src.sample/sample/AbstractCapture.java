package sample;

import java.awt.Graphics;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import javax.swing.JFrame;
import javax.swing.JPanel;

import jp.digitalmuseum.capture.VideoCapture;
import jp.digitalmuseum.capture.VideoGrabber;
import jp.digitalmuseum.capture.VideoListener;

/** Capture video from webcam and show it in a window. */
public abstract class AbstractCapture extends JFrame {
	private static final long serialVersionUID = 1L;
	VideoCapture capture;
	VideoGrabber grabber;

	/** Setup a capture object and GUI parts. */
	public void initialize(VideoCapture capture) {

		// Start to capture video.
		grabber = new VideoGrabber(capture);
		try {
			grabber.start();
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		// Initialize a canvas.
		MainPanel panel = new MainPanel();
		grabber.addVideoListener(panel);
		getContentPane().add(panel);

		// Initialize a window.
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setResizable(false);
		setVisible(true);
		setFrameSize(capture.getWidth(), capture.getHeight());
	}

	public void setFrameSize(int width, int height) {
		final Insets insets = getInsets();
		setSize(width + insets.left + insets.right,
				height + insets.top + insets.bottom);
	}

	/** Dispose the capture object and the GUI parts. */
	public void dispose() {
		if (grabber != null) {
			grabber.stop();
		}
		super.dispose();
	}

	/** Main panel to paste video images. */
	class MainPanel extends JPanel implements VideoListener {
		private static final long serialVersionUID = 1L;
		private BufferedImage image = null;

		public void paint(Graphics g) {
			if (image != null)
				g.drawImage(image, 0, 0, null);
		}

		public void update(Graphics g) {
			paint(g);
		}

		public void imageUpdated(BufferedImage image) {
			this.image = image;
			repaint();
		}
	}

}

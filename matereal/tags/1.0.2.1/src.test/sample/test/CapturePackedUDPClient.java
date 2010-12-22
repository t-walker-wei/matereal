package sample.test;

import java.net.DatagramSocket;

import jp.digitalmuseum.capture.VideoCapturePackedUDP;
import jp.digitalmuseum.mr.Matereal;
import jp.digitalmuseum.mr.gui.*;
import jp.digitalmuseum.mr.service.Camera;

/**
 * Capture client.
 *
 * @author Jun KATO
 */
public class CapturePackedUDPClient {

	public static void main(String[] args) {
		new CapturePackedUDPClient();
	}

	public CapturePackedUDPClient() {

		// Run a camera.
		final VideoCapturePackedUDP capture = new VideoCapturePackedUDP();
		capture.setSize(
				CapturePackedUDPServer.WIDTH,
				CapturePackedUDPServer.HEIGHT);
		try {
			capture.setSource(new DatagramSocket(
					CapturePackedUDPServer.PORT));
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		final Camera camera = new Camera(capture);
		camera.start();

		// Make and show a window for showing captured image.
		final DisposeOnCloseFrame frame = new DisposeOnCloseFrame(
				new ImageProviderPanel(camera)) {
			private static final long serialVersionUID = 1L;
			@Override public void dispose() {
				super.dispose();
				Matereal.getInstance().dispose();
			}
		};
		frame.setTitle("Client");
		frame.setResizable(false);
		frame.setFrameSize(camera.getWidth(), camera.getHeight());
	}
}

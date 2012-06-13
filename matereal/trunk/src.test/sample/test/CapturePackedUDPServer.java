package sample.test;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.ArrayDeque;
import java.util.Queue;

import javax.swing.JOptionPane;

import com.phybots.Phybots;
import com.phybots.gui.DisposeOnCloseFrame;
import com.phybots.gui.ImageProviderPanel;
import com.phybots.service.Camera;
import com.phybots.service.ServiceAbstractImpl;

import jp.digitalmuseum.capture.VideoCapturePackedUDP;

/**
 * Capture server.
 *
 * @author Jun Kato
 */
class CapturePackedUDPServer {
	public final static int WIDTH = 320;
	public final static int HEIGHT = 240;
	public final static int PORT = 7777;
	private DatagramSocket socket;
	private InetSocketAddress remoteAddress;
	private Camera camera;

	public static void main(String[] argv) {
		new CapturePackedUDPServer();
	}

	public CapturePackedUDPServer() {

		// Run a camera.
		// Let users select a device to capture images.
		String identifier = (String) JOptionPane.showInputDialog(null,
				"Select a device to capture images.", "Device list",
				JOptionPane.QUESTION_MESSAGE, null, Camera.queryIdentifiers(), null);
		camera = new Camera(identifier);
		camera.setSize(WIDTH, HEIGHT);
		camera.start();

		remoteAddress = new InetSocketAddress("127.0.0.1", PORT);
		try {
			socket = new DatagramSocket();
			if (remoteAddress.getHostName().endsWith(".255")) socket.setBroadcast(true);
		} catch (SocketException e) {
			e.printStackTrace();
			return;
		}

		new ServiceAbstractImpl() {
			private static final long serialVersionUID = -626221097758274630L;
			private Queue<DatagramPacket> packets =
					new ArrayDeque<DatagramPacket>();
			public synchronized void run() {
					try {
						VideoCapturePackedUDP.explode(camera.getImage(), remoteAddress, packets);
						for (DatagramPacket packet : packets) {
							wait(10);
							socket.send(packet);
						}
						packets.clear();
					} catch (InterruptedException e) {
						// Do nothing.
					} catch (Exception e) {
						e.printStackTrace();
					}
			}
		}.start();

		// Make and show a window for showing captured image.
		DisposeOnCloseFrame frame = new DisposeOnCloseFrame(
				new ImageProviderPanel(camera)) {
			private static final long serialVersionUID = 1L;
			@Override public void dispose() {
				super.dispose();
				Phybots.getInstance().dispose();
			}
		};

		frame.setTitle("Server");
		frame.setResizable(false);
		frame.setFrameSize(camera.getWidth(), camera.getHeight());
	}
}

package sample.test;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.LinkedList;
import java.util.Queue;

import jp.digitalmuseum.capture.VideoCapturePackedUDP;
import jp.digitalmuseum.mr.Matereal;
import jp.digitalmuseum.mr.gui.DisposeOnCloseFrame;
import jp.digitalmuseum.mr.gui.ImageProviderPanel;
import jp.digitalmuseum.mr.service.Camera;
import jp.digitalmuseum.mr.service.ServiceAbstractImpl;

/**
 * Capture server.
 *
 * @author Jun KATO
 */
class CapturePackedUDPServer {
	public final static int WIDTH = 320;
	public final static int HEIGHT = 240;
	public static final int PORT = 7777;
	private DatagramSocket socket;
	private InetSocketAddress remoteAddress;
	private Camera camera;

	public static void main(String[] argv) {
		new CapturePackedUDPServer();
	}

	public CapturePackedUDPServer() {

		camera = new Camera(WIDTH, HEIGHT);
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
			final private Queue<DatagramPacket> packets =
					new LinkedList<DatagramPacket>();
			public synchronized void run() {
					try {
						VideoCapturePackedUDP.explode(camera.getImage(), remoteAddress, packets);
						for (DatagramPacket packet : packets) {
							wait(10);
							socket.send(packet);
						}
						packets.clear();
					} catch (Exception e) {
						e.printStackTrace();
					}
			}
		}.start();

		// Make and show a window for showing captured image.
		final DisposeOnCloseFrame frame = new DisposeOnCloseFrame(
				new ImageProviderPanel(camera)) {
			private static final long serialVersionUID = 1L;
			@Override public void dispose() {
				super.dispose();
				Matereal.getInstance().dispose();
			}
		};
		frame.setTitle("Server");
		frame.setResizable(false);
		frame.setFrameSize(camera.getWidth(), camera.getHeight());
	}
}

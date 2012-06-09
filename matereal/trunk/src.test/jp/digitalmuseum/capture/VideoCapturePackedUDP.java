/**
 * VideoCapturePackedUDP
 *
 * Copyright (c) 2009 arc@dmz
 * http://digitalmuseum.jp/
 * All rights reserved.
 */
package jp.digitalmuseum.capture;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayDeque;
import java.util.Queue;

import com.phybots.service.Service;
import com.phybots.service.ServiceAbstractImpl;


public class VideoCapturePackedUDP extends VideoCaptureAbstractImpl {

	public static int DEFAULT_TIMEOUT = 1000;
	private static int udpid;
	private static int id;

	private final static ByteArrayOutputStream outStream;
	private final static DataOutputStream out;

	static {
		outStream = new ByteArrayOutputStream();
		out = new DataOutputStream(outStream);
	}

	private Service packetReceiver;
	private DatagramSocket socket;
	private BufferedImage image;

	public VideoCapturePackedUDP(int width, int height) {
		super(width, height);
	}

	public VideoCapturePackedUDP() { super(); }

	public void start() {
		packetReceiver = new PacketReceiver();
		packetReceiver.start();
	}

	public void stop() {
		packetReceiver.stop();
	}

	@Override
	synchronized byte[] tryGrabFrameData() {
		if (image != null) {
			if (image.getType() != BufferedImage.TYPE_3BYTE_BGR) {
				BufferedImage bgrImage =
						new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
				bgrImage.getGraphics().drawImage(image, 0, 0, null);
				image = bgrImage;
			}
			return ((DataBufferByte) image.getData().getDataBuffer()).getData();
		}
		return null;
	}

	public String getIdentifier() {
		return "udp://"+socket.getInetAddress();
	}

	public String getName() {
		return getIdentifier();
	}

	public void pause() {
		packetReceiver.pause();
	}

	public void resume() {
		packetReceiver.resume();
	}

	public boolean setFrameRate(float fps) {
		return false;
	}

	public boolean setSize(int width, int height) {
		this.width = width;
		this.height = height;
		return true;
	}

	public void setSource(Object source) throws Exception {
		if (source instanceof DatagramSocket) {
			socket = (DatagramSocket) source;
			setTimeout(DEFAULT_TIMEOUT);
		}
	}

	public void setTimeout(int timeout) throws SocketException {
		socket.setSoTimeout(timeout);
	}

	private class PacketReceiver extends ServiceAbstractImpl {
		private static final long serialVersionUID = 8777204912730057359L;
		private ByteArrayOutputStream outStream;
		private DatagramPacket datagramPacket;
		private Queue<ImagePacket> packets;
		private ImagePacket imagePacket;
		private short currentId = 0;

		public PacketReceiver() {
			outStream = new ByteArrayOutputStream();
			imagePacket = new ImagePacket();
			packets = new ArrayDeque<ImagePacket>();
			datagramPacket = new DatagramPacket(
					new byte[ImagePacket.MAX_UDPLEN],
					ImagePacket.MAX_UDPLEN);
		}

		public void run() {
			try {

				// Receive a packet and parse data.
				socket.receive(datagramPacket);
				imagePacket.read(
						new DataInputStream(
								new ByteArrayInputStream(datagramPacket.getData())));

				// Check packet contents and add it to the set.
				if (imagePacket.getId() != currentId) {
					packets.clear();
					currentId = imagePacket.getId();
				}
				packets.add(imagePacket);

				// Check whether the hash-map is full or not.
				if (packets.size() == imagePacket.getPages()) {
					for (ImagePacket p : packets) {
						outStream.write(p.getData());
						p.clear();
					}
					synchronized (VideoCapturePackedUDP.this) {
						image = ImageCodec.decodeImage(outStream.toByteArray());
					}
					outStream.reset();
					packets.clear();
				}

			} catch (SocketTimeoutException e) {
				// Do nothing.
				return;
			} catch (IOException e) {
				// Do nothing.
				return;
			}
		}
	}

	public static void explode(BufferedImage source, InetSocketAddress remoteAddress, Queue<DatagramPacket> packets) throws Exception {

		byte[] data = ImageCodec.encodeImage(source);
		int length = data.length;

		short pages = (short) (length / ImagePacket.MAX_DATALEN +
				(length % ImagePacket.MAX_DATALEN != 0 ? 1 : 0));

		ImagePacket p = new ImagePacket();
		p.setId((short) id ++);
		p.setData(data);
		p.setPages(pages);

		for (short curpage = 0; curpage < pages; curpage ++) {
			int curlength = curpage >= pages - 1 ?
					length % ImagePacket.MAX_DATALEN : ImagePacket.MAX_DATALEN;
			p.setUdpid((short) udpid ++);
			p.setCurpage(curpage);
			p.setCurlength(curlength);
			p.write(out);

			DatagramPacket packet = new DatagramPacket(outStream.toByteArray(), outStream.size(), remoteAddress);
			packets.add(packet);

			outStream.reset();
		}
	}

}

/**
 * VideoCaptureJMF
 *
 * Copyright (c) 2009 arc@dmz
 * http://digitalmuseum.jp/
 * All rights reserved.
 */
package jp.digitalmuseum.capture;

import java.awt.Dimension;
import java.util.ArrayList;

import javax.media.Buffer;
import javax.media.CaptureDeviceInfo;
import javax.media.CaptureDeviceManager;
import javax.media.Format;
import javax.media.Manager;
import javax.media.MediaLocator;
import javax.media.Player;
import javax.media.control.FormatControl;
import javax.media.control.FrameGrabbingControl;
import javax.media.format.VideoFormat;
import javax.media.protocol.CaptureDevice;
import javax.media.protocol.DataSource;


/**
 * Capture images using Java Media Framework.
 *
 * @author Jun Kato
 */
public class VideoCaptureJMF extends VideoCaptureAbstractImpl {

	/** Device locator. */
	private MediaLocator locator;
	/** Capture device */
	private DataSource dataSource;
	/** Video format used for capturing. */
	private VideoFormat videoFormat;
	/** Player object used for capturing images. */
	private Player player;
	/** Frame grabber. */
	private FrameGrabbingControl grabber;

	/** Available device list (cache). */
	private static CaptureDeviceInfo[] deviceList;
	/** Device in-use list */
	private static boolean[] deviceInUse;

	/** Constructor without options. */
	public VideoCaptureJMF() {
		super();
	}

	/** Constructor with size specification. */
	public VideoCaptureJMF(int width, int height) {
		super(width, height);
	}

	public String getIdentifier() {
		if (locator == null) {
			throw new IllegalStateException();
		}
		return locator.toExternalForm();
	}

	public String getName() {
		return getIdentifier();
	}

	public void setSource(Object source) throws Exception {

		// Specification by a JMF object.
		if (source instanceof MediaLocator) {
			setLocator((MediaLocator) source);
			return;
		}
		if (source instanceof CaptureDeviceInfo) {
			setLocator(((CaptureDeviceInfo) source)
					.getLocator());
		}

		// Specification by a device index.
		if (source instanceof Integer) {
			setLocator(
					queryDevices()[Integer.class.cast(source)]
							.getLocator());
			return;
		}

		// Specification by an identifier.
		if (source instanceof String) {
			setLocator(new MediaLocator((String) source));
		}

		throw new IllegalArgumentException("Source specification by an illegal type of object.");
	}

	public void start() throws Exception {

		// If capturing is already started, do nothing.
		if (isStarted()) {
			return;
		}

		// Use the default device if not specified.
		if (locator == null) {
			setLocator(lookForUnusedLocator());
		}

		// Set start flag.
		isStarted = true;

		// Instantiate DSCapture object.
		initCapture();
	}

	public void pause() {
		if (!isStarted()) {
			return;
		}
		player.stop();
		isPaused = true;
	}

	public void resume() {
		if (!isStarted() ||
				!isPaused()) {
			return;
		}
		player.start();
		isPaused = false;
	}

	public void stop() {
		disposeCapture();
		isStarted = false;
		isPaused = false;
	}

	@Override
	byte[] tryGrabFrameData() {
		if (!isStarted() || isPaused()) {
			return null;
		}

		// Grab a frame.
		Buffer buffer = grabber.grabFrame();
		byte[] pixels = (byte[]) buffer.getData();
		if (pixels == null) {
			return null;
		}

		// Reorder pixels from RGB to BGR format.
		int
			offset_dst = 0,
			offset_src = getWidth()*(getHeight()-1)*3;
		final int bandWidth = getWidth()*3;
		for (int x = 0; x < getWidth(); x ++) {
			for (int y = 0; y < getHeight()/2; y ++) {
				final byte
					r = pixels[offset_src + 0],
					g = pixels[offset_src + 1],
					b = pixels[offset_src + 2];
				pixels[offset_src + 0] = pixels[offset_dst + 2];
				pixels[offset_src + 1] = pixels[offset_dst + 1];
				pixels[offset_src + 2] = pixels[offset_dst + 0];
				pixels[offset_dst + 0] = b;
				pixels[offset_dst + 1] = g;
				pixels[offset_dst + 2] = r;
				offset_dst += bandWidth;
				offset_src -= bandWidth;
			}
			offset_dst += 3;
			offset_src += 3;
		}
		return isGrayScale() ? RawImageUtils.rgbToGrayScale(pixels) : pixels;
	}

	public boolean setFrameRate(float fps) {

		// Get the format with framerate specification.
		if (videoFormat == null) {
			videoFormat = getDefaultVideoFormat();
		}
		videoFormat = (VideoFormat) videoFormat.intersects(new VideoFormat(
				videoFormat.getEncoding(),
				null,
				Format.NOT_SPECIFIED,
				null,
				fps));
		if (!isStarted()) {
			return videoFormat.getFrameRate() == fps;
		}

		return setCaptureFormat((CaptureDevice) dataSource, videoFormat);
	}

	public boolean setSize(int width, int height) {

		// Get the format with size specification.
		if (videoFormat == null) {
			videoFormat = getDefaultVideoFormat();
		}
		videoFormat = (VideoFormat) videoFormat.intersects(new VideoFormat(
				videoFormat.getEncoding(),
				new Dimension(width, height),
				Format.NOT_SPECIFIED,
				null,
				Format.NOT_SPECIFIED));
		if (!isStarted()) {
			final Dimension d = videoFormat.getSize();
			return d.width == width &&
					d.height == height;
		}

		return setCaptureFormat((CaptureDevice) dataSource, videoFormat);
	}

// VideoCaptureJMF original functions defined below.

	public void setLocator(MediaLocator locator) {

		// Check if capturing is not started.
		if (isStarted()) {
			throw new IllegalStateException("Source filter can be set only when capturing is not started.");
		}

		// Set the locator.
		this.locator = locator;
	}

	/**
	 * Instantiate Player object and start capturing.
	 */
	private void initCapture() throws Exception {

		// Create a DataSource object.
		try {
			dataSource = Manager.createDataSource(locator);
			dataSource.connect();
		} catch (Exception e) {
			throw e;
		}

		// Set the format used for capturing.
		if (!setCaptureFormat((CaptureDevice) dataSource, videoFormat)) {
			throw new IllegalStateException("Unsupported video format selected: "+videoFormat.toString());
		}

		// Initialize player and grabber.
		player = Manager.createRealizedPlayer(dataSource);
		player.start();
		grabber = (FrameGrabbingControl) player.getControl(
				"javax.media.control.FrameGrabbingControl");

		// Check the filter as used.
		CaptureDeviceInfo[] devices = queryDevices();
		for (int i = 0; i < deviceInUse.length; i ++) {
			if (locator.toExternalForm()
					.equals(devices[i].getLocator().toExternalForm())) {
				deviceInUse[i] = true;
				break;
			}
		}

		// Wait till data arrives.
		while (true) {
			Buffer buffer = grabber.grabFrame();
			if (buffer != null) {
				byte[] pixels = (byte[]) buffer.getData();
				if (pixels != null) {
					break;
				}
			}
			try { Thread.sleep(100); }
			catch (InterruptedException e) { }
		}
	}

	/**
	 * Dispose Player object.
	 */
	private void disposeCapture() {

		// Check if capturing is started.
		if (!isStarted()) {
			return;
		}

		// Stop capturing.
		player.stop();
		player.close();
		player = null;
		grabber = null;

		// Check the device as unused.
		CaptureDeviceInfo[] devices = queryDevices();
		for (int i = 0; i < deviceInUse.length; i ++) {
			if (locator.toExternalForm()
					.equals(devices[i].getLocator().toExternalForm())) {
				deviceInUse[i] = false;
				break;
			}
		}
	}

	/**
	 * @see #initCapture()
	 * @see #disposeCapture()
	 */
	private MediaLocator lookForUnusedLocator() {
		CaptureDeviceInfo[] devices = queryDevices();
		if (devices != null) {
			for (int i = 0; i < deviceInUse.length; i ++) {
				if (deviceInUse[i] == false) {
					return devices[i].getLocator();
				}
			}
		}
		return null;
	}

	private boolean setCaptureFormat(CaptureDevice captureDevice, Format format) {
		final FormatControl[] formatControls = captureDevice.getFormatControls();
		if (formatControls.length < 1) return false;

		final FormatControl formatControl = formatControls[0];
		return formatControl.setFormat(format) == null ? false : true;
	}

	/**
	 * @return Returns the default video format used for capturing.
	 */
	private static VideoFormat getDefaultVideoFormat() {
		return new VideoFormat(VideoFormat.RGB);
	}

	/**
	 * @return Returns available capture devices.
	 */
	public static CaptureDeviceInfo[] queryDevices() {
		if (deviceList == null) {
			deviceList = (CaptureDeviceInfo[]) CaptureDeviceManager.getDeviceList(
					getDefaultVideoFormat()).toArray();
		}
		return deviceList.clone();
	}

	/** Get a list of identifiers of available devices. */
	public static String[] queryIdentifiers() {

		// Query devices.
		final CaptureDeviceInfo[] devices = queryDevices();
		final ArrayList<String> ids = new ArrayList<String>();
		for (CaptureDeviceInfo info : devices) {
			ids.add(info.getLocator().toExternalForm());
		}

		// Convert to a String array and returns it.
		String[] idsArray = new String[ids.size()];
		idsArray = ids.toArray(idsArray);
		return idsArray;
	}
}

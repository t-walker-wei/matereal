/*
 * PROJECT: capture at http://digitalmuseum.jp/en/software/
 * ----------------------------------------------------------------------------
 *
 * This file is part of Webcam capture package.
 * Webcam capture package, or simply "capture",
 * is a simple package for capturing real-time images using webcams.
 *
 * ----------------------------------------------------------------------------
 *
 * License version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is capture.
 *
 * The Initial Developer of the Original Code is Jun Kato.
 * Portions created by the Initial Developer are
 * Copyright (C) 2009 Jun Kato. All Rights Reserved.
 *
 * Contributor(s): Jun Kato
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either of the GNU General Public License Version 2 or later (the "GPL"),
 * or the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 */
package jp.digitalmuseum.capture;

import quicktime.Errors;
import quicktime.QTException;
import quicktime.QTSession;
import quicktime.qd.PixMap;
import quicktime.qd.QDConstants;
import quicktime.qd.QDGraphics;
import quicktime.qd.QDRect;
import quicktime.std.StdQTConstants;
import quicktime.std.StdQTException;
import quicktime.std.sg.SGDeviceList;
import quicktime.std.sg.SGVideoChannel;
import quicktime.std.sg.SequenceGrabber;
import quicktime.util.RawEncodedImage;


/**
 * Capture images using QuickTime for Java.
 *
 * @author Jun Kato
 */
@SuppressWarnings("deprecation")
public class VideoCaptureQT extends VideoCaptureAbstractImpl {
	private static String IDENTIFIER_PREFIX = "channel://";
	/** Whether VM is running on Windows or not. */
	private static boolean isWindows;
	/** QuickTime rectangle */
	private QDRect bounds;
	/** QuickTime sequence grabber */
	private SequenceGrabber grabber;
	private static SequenceGrabber activeGrabber;
	/** QuickTime video channel */
	private SGVideoChannel channel;
	private static SGVideoChannel activeChannel;
	/** QuickTime image object */
	private RawEncodedImage rawEncodedImage;

	/** Whether QuickTime session is already opened. */
	private static boolean sessionOpened;
	/** Available device list (cache). */
	private static String[] deviceList;

	/** Pixels data */
	private byte[] pixels;
	/** Temporary storage for pixels data */
	private int[] pixels_int;

	static {
		final String osName = System.getProperty("os.name").toLowerCase();
		isWindows = osName.startsWith("windows");
	}

	/** Constructor without options. */
	public VideoCaptureQT() {
		super();
	}

	/** Constructor with size specification. */
	public VideoCaptureQT(int width, int height) {
		super(width, height);
	}

	public String getIdentifier() {
		return IDENTIFIER_PREFIX+getName();
	}

	public String getName() {
		if (channel == null) {
			throw new IllegalStateException();
		}
		try {
			return channel.getInfo().getName();
		} catch (StdQTException e) {
			return null;
		}
	}

	public void setSource(Object source) throws Exception {

		// Specification by a filter index.
		if (source instanceof Integer) {
			setSource(
					queryIdentifiers()[Integer.class.cast(source)]);
			return;
		}

		// Specification by an identifier.
		if (source instanceof String) {
			final String identifier = (String) source;
			if (identifier.startsWith(IDENTIFIER_PREFIX)) {
				final String[] deviceList = queryIdentifiers();
				for (int i = 0; i < deviceList.length; i ++) {
					if (identifier.equals(deviceList[i])) {
						openSession();
						grabber = activeGrabber;
						channel = activeChannel;
						channel.setDevice(identifier.substring(IDENTIFIER_PREFIX.length()));
						return;
					}
				}
			}
			throw new Exception("Specified source ("+identifier+") not found.");
		}

		throw new IllegalArgumentException("Source specification by an illegal type of object.");
	}

	public void start() throws Exception {

		// Use the default device if not specified.
		if (grabber == null) {
			openSession();
			grabber = activeGrabber;
			channel = activeChannel;
		}
		channel.setUsage(StdQTConstants.seqGrabPreview);

		// Configure the channel
		isStarted = true;
		setSize(width, height);
		setFrameRate(fps);

		// Start capturing
		grabber.prepare(true, false); // useless?
		grabber.startPreview();

		// Get the real size for capturing.
		final int realWidth = rawEncodedImage.getRowBytes() / (isWindows? 3 : 4);
		if (realWidth != width) {
			setSize(realWidth, height);
		}

		// Wait till data arrives.
		grabber.idle();
	}

	public void pause() {
		if (isStarted == false ||
				isPaused) {
			return;
		}
		try {
			grabber.stop();
		} catch (StdQTException e) {
			isPaused = false;
			return;
		}
		isPaused = true;
	}

	public void resume() {
		if (isStarted == false ||
				isPaused == false) {
			return;
		}
		try {
			grabber.startPreview();
		} catch (StdQTException e) {
			isStarted = false;
			return;
		}
		isPaused = false;
	}

	public void stop() {
		if (!isStarted) {
			return;
		}
		try {
			grabber.stop();
			grabber.release();
			grabber.disposeChannel(channel);
		} catch (Exception e) {
			// All exception can be ignored because nothing can be done.
		}
		if (grabber.equals(activeGrabber)) {
			activeGrabber = null;
		}
		if (channel.equals(activeChannel)) {
			activeChannel = null;
		}
		grabber = null;
		channel = null;
		isStarted = false;
		isPaused = false;
	}

	@Override
	public byte[] tryGrabFrameData() {
		if (!isStarted || isPaused) { return null; }

		try {
			grabber.idle();
		} catch (QTException e) {
			// Do nothing?
		}

		// Windows
		if (isWindows) {
			rawEncodedImage.copyToArray(0, pixels, 0, pixels.length);

		// Mac OSX
		} else {
			rawEncodedImage.copyToArray(0, pixels_int, 0, pixels_int.length);
			for (int idx_byte = 0, idx = 0; idx < pixels_int.length; idx ++) {
				pixels[idx_byte ++] = (byte) (pixels_int[idx]       & 0xff);
				pixels[idx_byte ++] = (byte) (pixels_int[idx] >> 8  & 0xff);
				pixels[idx_byte ++] = (byte) (pixels_int[idx] >> 16);
			}
		}

		return isGrayScale() ? RawImageUtils.rgbToGrayScale(pixels) : pixels.clone();
	}

	public boolean setFrameRate(float fps) {

		// If capturing is not started,
		// simply set the field value and return true.
		if (!isStarted()) {
			this.fps = fps;
			return true;
		}

		try {
			// Set frame rate.
			channel.setFrameRate(fps);

			// Get frame rate.
			this.fps = channel.getFrameRate();
		} catch (StdQTException e) {
			// Do nothing
		}

		// Setting frame rate to 30.0 resulted in
		// 30.30303 fps (DirectShow) and 30.303024 fps (QuickTime).
		// To treat this result as a success,
		// cast both fps values as integer and compare them.
		return (int) this.fps == (int) fps;
	}

	public boolean setSize(int width, int height) {

		// If capturing is not started,
		// simply set the field value and return true.
		if (!isStarted()) {
			this.width = width;
			this.height = height;
			return true;
		}

		// Initialize memory space for storing image data.
		bounds = new QDRect(width, height);
		try {
			final QDGraphics graphics = new QDGraphics(
					isWindows ?
							QDConstants.k24RGBPixelFormat :
							(quicktime.util.EndianOrder.isNativeLittleEndian() ?
								QDConstants.k32BGRAPixelFormat : QDGraphics.kDefaultPixelFormat),
					bounds);
			grabber.setGWorld(graphics, null);
			channel.setBounds(bounds);
			final PixMap pixmap = graphics.getPixMap();
			rawEncodedImage = pixmap.getPixelData();
		} catch (QTException e) {
			return false;
		}

		final int realWidth = rawEncodedImage.getRowBytes() /
				(isWindows ? 3 : 4);
		final boolean succeeded = realWidth == width;
		width = realWidth;

		pixels = new byte[width*height*3];
		pixels_int = new int[width*height];
		return succeeded;
	}

	private static void openSession() {
		if (sessionOpened) {
			return;
		}
		try {
			QTSession.open();
			activeGrabber = new SequenceGrabber();
			activeChannel = new SGVideoChannel(activeGrabber);
		} catch (QTException e) {
			// Do nothing
		}
		sessionOpened = true;
	}

	private static void closeSession() {
		if (!sessionOpened) {
			return;
		}
		try {
			activeGrabber.disposeChannel(activeChannel);
			QTSession.close();
		} catch (StdQTException e) {
			// Do nothing.
		}
		activeChannel = null;
		activeGrabber = null;
		sessionOpened = false;
	}

	public synchronized static String[] queryIdentifiers() {
		if (deviceList == null) {
			final boolean newlyOpenedSession = !sessionOpened;
			if (newlyOpenedSession) {
				openSession();
			}
			try {
				final SGDeviceList list = activeChannel.getDeviceList(0);
				deviceList = new String[list.getCount()];
				for (int i = 0; i < list.getCount(); i++) {
					deviceList[i] = IDENTIFIER_PREFIX+list.getDeviceName(i).getName();
				}
				if (newlyOpenedSession) {
					closeSession();
				}
			} catch (QTException qte) {
				switch (qte.errorCode()) {
				case Errors.couldntGetRequiredComponent:
					throw new RuntimeException("Couldn't find any capture devices, " +
					"read the video reference for more info.");
				default:
					qte.printStackTrace();
					throw new RuntimeException("Problem listing capture devices, " +
					"read the video reference for more info.");
				}
			} catch (NullPointerException e) {
				return null;
			}
		}
		return deviceList == null ? null : deviceList.clone();
	}
}

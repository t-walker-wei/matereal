/*
 * PROJECT: Phybots at http://phybots.com/
 * ----------------------------------------------------------------------------
 *
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
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
 * The Original Code is Phybots.
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
package com.phybots.service;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

import javax.swing.JComponent;

import com.phybots.gui.CoordProviderPanel;
import com.phybots.message.ImageUpdateEvent;
import com.phybots.message.ServiceUpdateEvent;
import com.phybots.service.HomographyCoordProviderAbstractImpl;
import com.phybots.utils.Array;

import jp.digitalmuseum.capture.VideoCapture;
import jp.digitalmuseum.capture.VideoCaptureDummy;
import jp.digitalmuseum.capture.VideoCaptureFactoryImpl;

/**
 * Camera capture service.
 * <dl>
 *  <dt>DirectShow</dt><dd>filter:[(filter name)|(filter path)|(filter index)]</dd>
 *  <dt>QuickTime</dt><dd>channel:[(channel name)|(channel index)]</dd>
 * </dl>
 *
 * @author Jun Kato
 */
public class Camera extends HomographyCoordProviderAbstractImpl {
	private static final long serialVersionUID = -8691594856197985417L;
	final public static String SERVICE_NAME = "Camera";
	final public static int DEFAULT_WIDTH = 640;
	final public static int DEFAULT_HEIGHT = 480;

	/** VideoCapture object used for image capturing. */
	private transient VideoCapture capture;

	/** Captured image data in a BGR byte array. */
	private transient byte[] pixels;
	/** Captured image width and height. */
	private int width, height;
	/** Captured image as a BufferedImage object. */
	private transient BufferedImage image;
	/** Image updated flag. */
	private boolean imageUpdated;

	private String name = null;
	private Array<ImageListener> listeners;

	public Camera() {
		super();
		initialize(null, DEFAULT_WIDTH, DEFAULT_HEIGHT);
	}

	public Camera(VideoCapture capture) {
		super();
		initialize(capture.getWidth(), capture.getHeight());
		setSource(capture);
	}

	public Camera(String identifier) {
		super();
		initialize(identifier, DEFAULT_WIDTH, DEFAULT_HEIGHT);
	}

	public Camera(String identifier, int width, int height) {
		super();
		initialize(identifier, width, height);
	}

	public Camera(int width, int height) {
		super();
		initialize(null, width, height);
	}

	private void initialize(String identifier, int width, int height) {
		initialize(width, height);
		setSource(identifier);
	}

	private void initialize(int width, int height) {
		this.width = width;
		this.height = height;
		resetRectangle();
		imageUpdated = true;
		listeners = new Array<ImageListener>();
	}

	@Override
	public String getName() {
		String newName;
		try {
			newName = capture.getName();
		} catch (Exception e) {
			newName = super.getName();
		}
		if (name == null ||
				!name.equals(newName)) {
			name = newName;
			distributeEvent(new ServiceUpdateEvent(this, "name", name));
		}
		return name;
	}

	public byte[] getImageData() {
		if (pixels != null) {
			synchronized (pixels) {
				return pixels.clone();
			}
		}
		return null;
	}

	public BufferedImage getImage() {
		if (imageUpdated) {
			synchronized (this) {

				// Set data elements of the raster of the image object
				// to the captured image data.
				try {
					byte[] data = ((DataBufferByte)image.getRaster().getDataBuffer()).getData();
					System.arraycopy(pixels, 0, data, 0, data.length);
				} catch (Exception e) {
					// BufferedImage won't be refreshed
					// when setting data elements failed.
				}
				imageUpdated = false;
			}
		}
		return image;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public void drawImage(Graphics g) {
		drawImage(g, 0, 0);
	}

	public void drawImage(Graphics g, int x, int y) {
		g.drawImage(getImage(), x, y, null);
	}

	public String getIdentifier() {
		return capture.getIdentifier();
	}

	public long getActualInterval() {
		return (long) (1000f/capture.getFrameRate());
	}

	public boolean setSource(VideoCapture newCapture) {
		try {
			newCapture.setSize(width, height);
			if (isStarted()) {
				newCapture.setFrameRate(1000f/getInterval());
			}
			// Otherwise, getInterval() returns 0.
		} catch (Exception e) {
			if (capture == null) {
				capture = new VideoCaptureDummy(width, height);
			}
			return false;
		}
		capture = newCapture;
		updateSize();
		distributeEvent(new ServiceUpdateEvent(this, "source", capture));
		return true;
	}

	public boolean setSource(String identifier) {
		if (capture != null &&
				identifier != null &&
				identifier.equals(capture.getIdentifier())) {
			return true;
		}
		return setSource(identifier == null ?
				new VideoCaptureFactoryImpl().newInstance() :
				new VideoCaptureFactoryImpl().newInstance(identifier));
	}

	public boolean setSize(int width, int height) {
		synchronized (capture) {
			final boolean succeeded = capture.setSize(width, height);
			if (succeeded) {
				updateSize();
				distributeEvent(new ServiceUpdateEvent(this, "size", new int[] { width, height }));
			}
			return succeeded;
		}
	}

	public void run(){

		// Update size of the image if needed.
		if (width != capture.getWidth() ||
				height != capture.getHeight()) {
			updateSize();
		}

		// Since substitution of the reference is an atomic operation,
		// this statement does not need to be synchronized.
		pixels = capture.grabFrameData();
		imageUpdated = true;
		distributeEvent(new ImageUpdateEvent(this));

		synchronized (listeners) {
			if (listeners.size() > 0) {
				for (ImageListener listener : listeners) {
					listener.imageUpdated(getImage());
				}
			}
		}
	}

	/**
	 * This method throws an IllegalStateException when it failed to start capturing images.
	 * After the exception is thrown, this service will not start.
	 *
	 * @throws IllegalstateException
	 */
	@Override
	protected void onStart() {
		try {
			capture.start();
			updateSize();
		} catch (Exception e) {
			// If an error occurred starting capture,
			// simply throw an exception and do not start this service.
			throw new IllegalStateException(e);
		}
	}

	@Override
	protected void onPause() {
		capture.pause();
	}

	@Override
	protected void onResume() {
		capture.resume();
	}

	@Override
	protected void onStop() {
		capture.stop();
	}

	/**
	 * Update size of the image.
	 * @param width
	 * @param height
	 */
	private void updateSize() {
		width = capture.getWidth();
		height = capture.getHeight();
		image = new BufferedImage(width, height,
				BufferedImage.TYPE_3BYTE_BGR);
		resetRectangle();
	}

	@Override
	public JComponent getConfigurationComponent() {
		return new CoordProviderPanel(Camera.this);
	}

	public void addImageListener(ImageListener listener) {
		synchronized (listeners) {
			listeners.push(listener);
		}
	}

	public boolean removeImageListener(ImageListener listener) {
		synchronized (listeners) {
			return listeners.remove(listener);
		}
	}

	public static String[] queryIdentifiers() {
		return new VideoCaptureFactoryImpl().queryIdentifiers();
	}
}

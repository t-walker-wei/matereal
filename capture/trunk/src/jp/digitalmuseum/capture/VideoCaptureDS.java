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

import java.awt.Dimension;
import java.util.ArrayList;

import de.humatic.dsj.DSCapture;
import de.humatic.dsj.DSFilterInfo;
import de.humatic.dsj.DSFiltergraph;
import de.humatic.dsj.DSJException;
import de.humatic.dsj.DSMediaType;
import de.humatic.dsj.DSCapture.CaptureDevice;
import de.humatic.dsj.DSFilterInfo.DSPinInfo;

/**
 * Capture images using DirectShow of Windows Platform through dsj (DirectShow for Java).
 *
 * @author Jun Kato
 */
public class VideoCaptureDS extends VideoCaptureAbstractImpl {
	private static String IDENTIFIER_PREFIX = "filter:";
	/** Filter graph for capturing. */
	private DSCapture capture;
	/** Filter for capturing. */
	private DSFilterInfo filter;
	/** Pin of the filter for capturing. */
	private DSPinInfo pin;
	/** Device list (cache) */
	private static DSFilterInfo[] filters;
	/** Device in-use list */
	private static boolean[] filterInUse;

	static {
		// Useless?
		DSFilterInfo.doNotRender();
	}

	/** Constructor without options. */
	public VideoCaptureDS() {
		super();
	}

	/** Constructor with size specification. */
	public VideoCaptureDS(int width, int height) {
		super(width, height);
	}

	public String getIdentifier() {
		if (filter == null) {
			throw new IllegalStateException();
		}
		return IDENTIFIER_PREFIX+filter.getPath();
	}

	public String getName() {
		if (filter == null) {
			throw new IllegalStateException();
		}
		return filter.getName();
	}

	public void setSource(Object source) throws Exception {

		// Specification by a dsj object.
		if (source instanceof DSFilterInfo) {
			setSourceFilter((DSFilterInfo) source);
			return;
		}

		// Specification by a filter index.
		if (source instanceof Integer) {
			setSourceFilter(
					queryDevices()[Integer.class.cast(source)]);
			return;
		}

		// Specification by an identifier.
		if (source instanceof String) {

			// The identifier must start with IDENTIFIER_PREFIX.
			final String identifier = (String) source;
			if (identifier.startsWith(IDENTIFIER_PREFIX)) {
				final String name = identifier.substring(IDENTIFIER_PREFIX.length());
				try {
					final int filterIndex = Integer.parseInt(name);
					setSourceFilter(queryDevices()[filterIndex]);
					return;
				} catch (NumberFormatException e) {
					for (DSFilterInfo f : queryDevices()) {
						if (name.equals(f.getPath())) {
							setSourceFilter(f);
							return;
						} else if (name.equals(f.getName())) {
							setSourceFilter(f);
							return;
						}
					}
				}
			}
			throw new IllegalArgumentException("Specified source not found.");
		}
		throw new IllegalArgumentException("Source specification by an illegal type of object.");
	}

	public void start() throws Exception {

		// If capturing is already started, do nothing.
		if (isStarted()) {
			return;
		}

		// Use the default device if not specified.
		if (filter == null) {
			setSourceFilter(lookForUnusedFilter());
			if (filter == null) {
				throw new Exception("No camera is ready to use.");
			}
		}
		isStarted = true;

		// Choose a downstream pin to capture images.
		pin = null;
		float currentFps = Float.MAX_VALUE;
		try {
			for (DSPinInfo p : filter.getDownstreamPins()) {

				// Capture YUY2 and RGB none-compressed images by default.
				final DSMediaType[] formats = p.getFormats();
				for (int i = 0; i < formats.length; i++) {
					final DSMediaType format = formats[i];
					if (format.getWidth() == width
							&& format.getHeight() == height
							&& (format.getSubTypeString().contains("RGB")
									|| format.getSubTypeString().contains("YUY2"))) {
						if (Math.abs(fps - format.getFrameRate()) <
								Math.abs(fps - currentFps)) {
							pin = p;
							p.setPreferredFormat(i);
							currentFps = format.getFrameRate();
						}
					}
				}
				if (pin != null && (int) currentFps == (int) fps) {
					break;
				}
			}
			if (pin == null) {
				// throw new IllegalStateException("No suitable pin to capture was found for filter:"+filter);
				pin = filter.getDownstreamPins()[0];
			}

		} catch (DSJException e) {
			// Can't retrieve pin list from the filter.
			// (Do nothing.)
		}

		// Instantiate a capture object.
		capture = new DSCapture(
				DSFiltergraph.JAVA_POLL,
				filter, false, DSFilterInfo.doNotRender(), null);
		capture.play();

		// Check the filter as used.
		final DSFilterInfo[] filters = queryDevices();
		for (int i = 0; i < filterInUse.length; i ++) {
			if (filter.equals(filters[i])) {
				filterInUse[i] = true;
				break;
			}
		}

		// Wait till data arrives.
		updateInformation();
	}

	public void pause() {
		if (!isStarted() ||
				isPreparing()) {
			return;
		}
		capture.pause();
		isPaused = true;
	}

	public void resume() {
		if (!isStarted() ||
				isPreparing() ||
				!isPaused()) {
			return;
		}
		capture.play();
		isPaused = false;
	}

	public void stop() {
		disposeCapture();
		isStarted = false;
		isPaused = false;
	}

	@Override
	byte[] tryGrabFrameData() {
		if (!isStarted() ||
				isPreparing() ||
				isPaused()) {
			return null;
		}
		int size = 0;
		try {
			size = capture.getDataSize();
			final byte[] pixels = capture.getData();
			if (pixels == null) {
				return null;
			} else if (isGrayScale()) {
				return RawImageUtils.rgbToGrayScale(pixels);
			} else {
				return pixels;
			}
		} catch (DSJException e) {
			if (size > 0) {
				// Don't know why this happens...
				return null;
			}
			throw e;
		}
	}

	public boolean setFrameRate(float fps) {

		// If capturing is not started,
		// simply set the field value and return true.
		if (!isStarted()) {
			this.fps = fps;
			return true;
		}
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
		return this.width == width &&
				this.height == height;
	}

// VideoCaptureDS original functions defined below.

	/**
	 * Use a specified filter to capture images.
	 */
	public void setSourceFilter(DSFilterInfo filter) {

		// Check if capturing is not started.
		if (isStarted()) {
			throw new IllegalStateException("Source filter can be set only when capturing is not started.");
		}

		// Set the filter.
		this.filter = filter;
	}

	private void updateInformation() {
		while (true) {
			if (capture.getDataSize() > 0) {
				break;
			}
			try { Thread.sleep(100); }
			catch (InterruptedException e) { }
		}
		final Dimension d = capture.getDisplaySize();
		width = d.width;
		height = d.height;
		fps = capture.getFrameRate();
	}

	/**
	 * @return Returns if capturing process is preparing to start.
	 */
	private boolean isPreparing() {
		return isStarted() && capture == null;
	}

	/**
	 * Dispose DSCapture object.
	 */
	private void disposeCapture() {

		// Check if capturing is started.
		if (!isStarted()) {
			return;
		}

		// Stop capturing.
		try {
			capture.stop();
			capture.dispose();
			capture = null;
		} catch (Exception e) {
			// All exception can be ignored because nothing can be done.
		}

		// Check the filter as unused.
		final DSFilterInfo[] filters = queryDevices();
		for (int i = 0; i < filterInUse.length; i ++) {
			if (filter.equals(filters[i])) {
				filterInUse[i] = false;
				break;
			}
		}
	}

	/**
	 * @see #initCapture()
	 * @see #disposeCapture()
	 */
	private DSFilterInfo lookForUnusedFilter() {
		DSFilterInfo[] filters = queryDevices();
		if (filters != null) {
			for (int i = 0; i < filterInUse.length; i ++) {
				if (filterInUse[i] == false &&
						filters[i].getDownstreamPins() != null) {
					return filters[i];
				}
			}
		}
		return null;
	}

	/**
	 * Get all formats supported by the device.
	 */
	public DSMediaType[] getFormats() {
		if (pin == null) {
			return null;
		}
		return pin.getFormats();
	}

	/**
	 * Get current format preferred by the device.
	 */
	public DSMediaType getPreferredFormat() {
		if (pin == null) {
			return null;
		}
		final int preferredFormatIndex = pin.getPreferredFormat();
		return pin.getFormats()[
				preferredFormatIndex < 0 ?
						0 :
						preferredFormatIndex];
	}

	/**
	 * Show dialog for setting parameters of the device (saturation etc.).
	 */
	public void showDialog() {
		if (capture != null) {
			CaptureDevice device = capture.getActiveVideoDevice();
			if (device != null) {
				device.showDialog(CaptureDevice.WDM_DEVICE);
			}
		}
	}

	/**
	 * Show dialog for setting parameters of the device (resolution and frame-rate).
	 */
	public void showFormatDialog() {
		if (capture != null) {
			CaptureDevice device = capture.getActiveVideoDevice();
			if (device != null) {
				if (device.showDialog(CaptureDevice.WDM_CAPTURE) >= 0) {
					updateInformation();
				}
			}
		}
	}

// Utility methods defined below.

	/**
	 * @return Returns available filters (i.e. devices) for capturing.
	 */
	public static DSFilterInfo[] queryDevices() {
		// [0] returns video devices,
		// while [1] returns audio devices.
		if (filters == null) {
			//
			// Calling queryDevices() results in further calling of queryDevices(65), which gets stuck!
			// For a temporary workaround, I'm using queryDevices(0) which came into effect in my environment.
			//
			filters = DSCapture.queryDevices(0)[0];
			if (filters == null) {
				filterInUse = null;
				return null;
			}
			if (filters.length > 0) {
				filterInUse = new boolean[filters.length];
			}
		}
		return filters.clone();
	}

	/** Get a list of identifiers of available devices. */
	public static String[] queryIdentifiers() {

		// Query devices.
		final DSFilterInfo[] filters = queryDevices();
		final ArrayList<String> ids = new ArrayList<String>();
		for (int i = 0; i < filters.length; i ++) {
			if (!filterInUse[i]) {
				DSFilterInfo filter = filters[i];
				ids.add(IDENTIFIER_PREFIX+filter.getPath());
			}
		}

		// Convert to a String array and returns it.
		return ids.toArray(new String[0]);
	}
}

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

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;


/**
 * Abstract class of VideoCapture.<br />
 * VideoCapture implementation classes are recommended to extend this abstract class for convenience.
 *
 * @author Jun Kato
 */
public abstract class VideoCaptureAbstractImpl implements VideoCapture {
	/** Resolution of capturing images. */
	int
		width = VideoCapture.DEFAULT_WIDTH,
		height = VideoCapture.DEFAULT_HEIGHT;
	/** Frame rate for capturing. */
	float fps = VideoCapture.DEFAULT_FRAMERATE;
	/** Whether capturing is started or not. */
	boolean isStarted;
	/** Whether capturing is paused or not. */
	boolean isPaused;

	/** BufferedImage for holding captured frame. */
	private BufferedImage image;

	/** Whether capturing images are in gray scale or not. */
	private boolean isGrayScale;

	/** Whether capturing images are reversed by axis. */
	private boolean isReversedX, isReversedY;

	/** Constructor without options. */
	public VideoCaptureAbstractImpl() {
		// Do nothing.
	}

	/** Constructor with size specification. */
	public VideoCaptureAbstractImpl(int width, int height) {
		setSize(width, height);
	}

	final public void setGrayScale(boolean isGrayScale) {
		if (this.isGrayScale != isGrayScale) {
			image = null;
			this.isGrayScale = isGrayScale;
		}
	}

	final public void reverseX(boolean reverseX) {
		isReversedX = reverseX;
	}

	final public void reverseY(boolean reverseY) {
		isReversedY = reverseY;
	}

	final public void reverse(boolean reverseX, boolean reverseY) {
		reverseX(reverseX);
		reverseY(reverseY);
	}

	final public boolean isGrayScale() {
		return isGrayScale;
	}

	final public boolean isReversedX() {
		return isReversedX;
	}

	final public boolean isReversedY() {
		return isReversedY;
	}

	public BufferedImage grabFrame() {

		// If this is the first time to be called
		// since the instantiation or configuration change,
		// initialize BufferedImage.
		if (image == null ||
				image.getWidth() != getWidth() ||
				image.getHeight() != getHeight()) {
			if (getWidth() == 0 || getHeight() == 0) {
				return null;
			}
			if (isGrayScale()) {
				image = new BufferedImage(getWidth(), getHeight(),
						BufferedImage.TYPE_BYTE_GRAY);
			} else {
				image = new BufferedImage(getWidth(), getHeight(),
						BufferedImage.TYPE_3BYTE_BGR);
			}
		}

		// Set data elements of the raster of the image object
		// to the captured image data.
		try {
			byte[] imageData = ((DataBufferByte)image.getRaster().getDataBuffer()).getData();
			System.arraycopy(grabFrameData(), 0, imageData, 0, imageData.length);
			// image.getRaster().setDataElements(0, 0, getWidth(), getHeight(), grabFrameData());
		} catch (Exception e) {
			// BufferedImage won't be refreshed
			// when setting data elements failed.
		}
		return image;
	}

	final public byte[] grabFrameData() {
		final byte[] pixels = tryGrabFrameData();

		// When no additional operation needed, simply return it.
		if (!isReversedX && !isReversedY) {
			return pixels;

		// Reverse pixels
		} else if (isGrayScale()) {
			return RawImageUtils.reverseGrayScale(
					pixels,
					getWidth(),
					isReversedX, isReversedY);
		} else {
			return RawImageUtils.reverse(
					pixels,
					getWidth(),
					isReversedX, isReversedY);
		}
	}

	/** Try capturing an image (grabbing frame data). */
	abstract byte[] tryGrabFrameData();

	public boolean isStarted() {
		return isStarted;
	}

	public boolean isPaused() {
		return isPaused;
	}

	public float getFrameRate() {
		return fps;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

}

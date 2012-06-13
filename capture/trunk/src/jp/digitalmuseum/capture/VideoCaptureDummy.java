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

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;


/**
 * Dummy class providing a static image.
 *
 * @author Jun Kato
 */
public class VideoCaptureDummy extends VideoCaptureAbstractImpl {
	/** Resolution of capturing images. */
	private int
		width = VideoCapture.DEFAULT_WIDTH,
		height = VideoCapture.DEFAULT_HEIGHT;
	/** Frame rate for capturing. */
	private float fps = VideoCapture.DEFAULT_FRAMERATE;
	/** Whether capturing is started or not. */
	private boolean isStarted;
	/** Whether capturing is paused or not. */
	private boolean isPaused;

	/** Raw data */
	private byte[] pixels;
	private byte[] pixels_gray;

	/** Constructor without options. */
	public VideoCaptureDummy() {
		super();
	}

	/** Constructor with size specification. */
	public VideoCaptureDummy(int width, int height) {
		super(width, height);
	}

	public String getIdentifier() {
		return "dummy";
	}

	public String getName() {
		return "dummy";
	}

	public void setSource(Object source) throws Exception {

		// Specification by a BufferedImage.
		if (source instanceof BufferedImage) {
			updateImageBuffer(
					BufferedImage.class.cast(source));
			return;
		}

		throw new IllegalArgumentException("Source specification by an illegal type of object.");
	}

	public void start() throws Exception {

		// Use the default black image if not specified.
		if (pixels == null) {
			updateImageBuffer();
		}
	}

	@Override
	public boolean isStarted() {
		return isStarted;
	}

	public void pause() {
		if (pixels == null ||
				isStarted == false) {
			return;
		}
		isPaused = true;
	}

	public void resume() {
		if (pixels == null ||
				isStarted == false ||
				isPaused == false) {
			return;
		}
		isPaused = false;
	}

	public void stop() {
		isStarted = false;
		isPaused = false;
	}

	@Override
	public byte[] tryGrabFrameData() {
		if (pixels == null) return null;
		return isGrayScale() ? pixels_gray.clone() : pixels.clone();
	}

	public boolean setFrameRate(float fps) {
		this.fps = fps;
		return true;
	}

	@Override
	public float getFrameRate() {
		return fps;
	}

	public boolean setSize(int width, int height) {

		// Resize image
		this.width = width;
		this.height = height;
		return true;
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}

	private void updateImageBuffer() {
		pixels = new byte[width*height*3];
		pixels_gray = new byte[width*height];
	}

	private void updateImageBuffer(BufferedImage newImage) {
		if (newImage == null) {
			updateImageBuffer();
			return;
		}

		// Convert the image to have its data buffer in a byte array.
		final BufferedImage image = new BufferedImage(
				width, height, BufferedImage.TYPE_3BYTE_BGR);
		Graphics g = image.createGraphics();

		// Draw it, and get it!
		if (g.drawImage(newImage, 0, 0, null)) {
			pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData()
				.clone(); // Cloning is to prevent memory leak. (no use?)
			pixels_gray = RawImageUtils.rgbToGrayScale(pixels);
		}
	}

}

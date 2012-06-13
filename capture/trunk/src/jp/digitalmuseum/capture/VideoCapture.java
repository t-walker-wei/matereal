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


/**
 * Interface for providing a way to capture images in real-time
 * by web cameras and other capturing devices.
 *
 * @author Jun Kato
 */
public interface VideoCapture {

	/** Default value for capturing frame rate. */
	final public static float DEFAULT_FRAMERATE = 30;
	final public static int DEFAULT_WIDTH = 640;
	final public static int DEFAULT_HEIGHT = 480;

	/**
	 * Get a String expression of the identifier for this instance,
	 * with which users can rebuild the instance over sessions.
	 *
	 * @return String identifier.
	 * @see #getName()
	 */
	public String getIdentifier();

	/**
	 * Get a name for this instance.
	 * Compared to getIdentifier(), this method may return the same name with another instance.
	 *
	 * @see #getIdentifier()
	 */
	public String getName();

	/**
	 * Set frame rate (frame per second).
	 * @return Whether setting is succeeded or failed.
	 */
	public boolean setFrameRate(float fps);

	/**
	 * Get frame rate (frame per second).
	 * @return Frame per second.
	 */
	public float getFrameRate();

	/**
	 * Set resolution.
	 * @return Whether setting is succeeded or failed.
	 */
	public boolean setSize(int width, int height);

	/**
	 * Get width of the resolution.
	 * @return Width of the resolution in pixels.
	 */
	public int getWidth();

	/**
	 * Get height of the resolution.
	 * @return Height of the resolution in pixels.
	 */
	public int getHeight();

	/**
	 * Set gray scale mode.
	 */
	public void setGrayScale(boolean getGrayScale) throws Exception;

	/**
	 * Grab a frame in BufferedImage.
	 * @return Grabbed frame in BufferedImage.
	 */
	public BufferedImage grabFrame();

	/**
	 * Grab a frame in a byte array.
	 * @return Grabbed frame in a byte array.
	 */
	public byte[] grabFrameData();

	/** Specify a source for capturing images. */
	public void setSource(Object source) throws Exception;

	/**
	 * Start capturing images.
	 */
	public void start() throws Exception;

	/**
	 * Pause capturing.<br />
	 * Capturing process must be started before calling this method.
	 */
	public void pause();

	/**
	 * Resume capturing.<br />
	 * Capturing process must be started before calling this method.
	 */
	public void resume();

	/** Stop capturing. */
	public void stop();

	/** Reverse captured images by axis.
	 * @param reverseX Whether to reverse images by X-axis or not.
	 * @param reverseY Whether to reverse images by Y-axis or not.
	 */
	public void reverse(boolean reverseX, boolean reverseY);

	/** Reverse captured images by X-axis. */
	public void reverseX(boolean reverse);

	/** Reverse captured images by Y-axis. */
	public void reverseY(boolean reverse);

	/**
	 * Get gray scale mode.
	 * @return Whether capturing images in gray scale or not.
	 */
	public boolean isGrayScale();

	/**
	 * Get if capturing is started or not.
	 * @return Whether capturing is started or not.
	 */
	public boolean isStarted();

	/**
	 * Get if capturing is paused or not.
	 * @return Whether capturing is paused or not.
	 */
	public boolean isPaused();

	/** Get whether captured images are reversed by X-axis. */
	public boolean isReversedX();

	/** Get whether captured images are reversed by Y-axis. */
	public boolean isReversedY();

}
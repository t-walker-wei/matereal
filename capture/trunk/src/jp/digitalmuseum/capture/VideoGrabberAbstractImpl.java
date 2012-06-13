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

import java.awt.event.ActionListener;

import javax.swing.Timer;

/**
 * Abstract implementation of video grabbing classes.
 *
 * @author Jun Kato
 * @see VideoCapture
 * @see VideoGrabber
 * @see VideoRawGrabber
 */
public abstract class VideoGrabberAbstractImpl implements ActionListener {

	/** Video capturing object. */
	private VideoCapture capturer;

	/** Lately occurred exception. */
	private Exception e;

	/** Timer object for periodic running. */
	private Timer timer;

	/** Frame rate for capturing. */
	private float fps = VideoCaptureAbstractImpl.DEFAULT_FRAMERATE;

	public VideoGrabberAbstractImpl() {
		e = null;
		initialize();
	}

	public VideoGrabberAbstractImpl(VideoCapture capturer) {
		this.capturer = capturer;
		if (capturer != null) {
			setupTimer(capturer.getFrameRate());
		}
		e = null;
		initialize();
	}

	protected abstract void initialize();

	/**
	 * @see VideoCapture#setFrameRate(float)
	 */
	public boolean setFrameRate(float fps) {
		if (capturer != null) {
			final boolean succeeded = capturer.setFrameRate(fps);
			if (!succeeded) {
				return false;
			}
		}
		setupTimer(fps);
		return true;
	}

	private void setupTimer(float fps) {
		if (timer != null) {
			timer.stop();
		}
		timer = new Timer((int) (1000.0f / fps), this);
		this.fps = fps;
	}

	/**
	 * Specify a capturer.
	 *
	 * @see #setFrameRate(float)
	 */
	public boolean setCapturer(VideoCaptureAbstractImpl capturer) {
		this.capturer = capturer;
		return setFrameRate(fps);
	}

	/**
	 * @return Returns the specified capturer.
	 */
	public VideoCapture getCapturer() {
		return capturer;
	}

	/**
	 * @see VideoCapture#start()
	 */
	public void start() throws Exception {
		timer.start();
		if (!capturer.isStarted()) {
			capturer.start();
		}
	}

	/**
	 * @see VideoCapture#pause()
	 */
	public void pause() {
		timer.stop();
		if (capturer == null || capturer.isStarted()) {
			return;
		}
		capturer.pause();
	}

	/**
	 * @see VideoCapture#resume()
	 */
	public void resume() {
		timer.start();
		capturer.resume();
	}

	/**
	 * @see VideoCapture#stop()
	 */
	public void stop() {
		timer.stop();
		capturer.stop();
	}

	/**
	 * @return Returns the last exception occurred in this grabber.
	 * @see #setFrameRate(float)
	 */
	public Exception getLastException() {
		return e;
	}

}

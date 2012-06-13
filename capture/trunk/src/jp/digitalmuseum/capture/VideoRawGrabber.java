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

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;


/**
 * Grab image data in a byte array and delivery them to listeners.
 *
 * @author Jun Kato
 * @see VideoListener
 */
public class VideoRawGrabber extends VideoGrabberAbstractImpl {

	private List<VideoRawListener> rawListeners;

	public VideoRawGrabber() { super(); }
	public VideoRawGrabber(VideoCapture capture) { super(capture); }

	protected void initialize () {
		rawListeners = new ArrayList<VideoRawListener>();
	}

	/** Add a VideoRawListener. */
	public void addVideoRawListener(VideoRawListener listener) {
		rawListeners.add(listener);
	}

	/** Remove a VideoRawListener. */
	public void removeVideoRawListener(VideoRawListener listener) {
		rawListeners.remove(listener);
	}

	final public void actionPerformed(ActionEvent e) {
		final VideoCapture capturer = getCapturer();
		final byte[] data = capturer.grabFrameData();
		for (VideoRawListener listener : rawListeners) {
			listener.imageUpdated(data, capturer.isGrayScale());
		}
	}

}

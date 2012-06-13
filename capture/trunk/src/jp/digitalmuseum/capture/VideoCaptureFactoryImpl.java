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


/**
 * Factory class for known VideoCapture implementation classes.
 *
 * @author Jun Kato
 */
public class VideoCaptureFactoryImpl implements VideoCaptureFactory {
	private static String osName;
	private static String getOsName() {
		if (osName == null) {
			osName = System.getProperty("os.name").toLowerCase();
		}
		return osName;
	}

	public VideoCapture newInstance() {

		// Switch implementation class by the operating system.
		String osname = getOsName();

		// Windows: DirectShow
		if (osname.startsWith("windows")) {
			return new VideoCaptureDS();
		}

		// Mac OSX: QuickTime
		else if (osname.startsWith("mac")) {
			return new VideoCaptureQT();
		}

		// Linux: JMF
		return new VideoCaptureJMF();
	}

	public VideoCapture newInstance(String identifier) {
		final VideoCapture capture = newInstance();
		try {
			capture.setSource(identifier);
		} catch (Exception e) {
			e.printStackTrace();
			throw new IllegalArgumentException("Specified capture device not found ("+identifier+").");
		}
		return capture;
	}


	public String[] queryIdentifiers() {

		// Switch implementation by the operating system.
		String osname = getOsName();

		// Windows: DirectShow
		if (osname.startsWith("windows")) {
			return VideoCaptureDS.queryIdentifiers();
		}

		// Mac OSX: QuickTime
		else if (osname.startsWith("mac")) {
			return VideoCaptureQT.queryIdentifiers();
		}

		// Linux: JMF
		return VideoCaptureJMF.queryIdentifiers();
	}
}

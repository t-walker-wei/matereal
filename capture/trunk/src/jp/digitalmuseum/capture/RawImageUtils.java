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
 * Utility class for handling image data in a byte array.
 *
 * @author Jun Kato
 */
public class RawImageUtils {

	/**
	 * Convert image data from RGB to gray-scale in a byte array.
	 */
	public static byte[] rgbToGrayScale(byte[] pixels) {
		byte[] pixels_gray = new byte[pixels.length/3];
		int offset = 0, offset_rgb = 0;
		while (offset < pixels_gray.length) {
			pixels_gray[offset ++] = (byte)
					(((pixels[offset_rgb ++] & 0xff)
					+ (pixels[offset_rgb ++] & 0xff)
					+ (pixels[offset_rgb ++] & 0xff))/3);
		}
		return pixels_gray;
	}

	/**
	 * Reverse gray-scale image data in a byte array.
	 * @param pixels Image data in a byte array.
	 * @param width Width of the image [pixel].
	 * @param reverseX To reverse the image by X axis or not.
	 * @param reverseY To reverse the image by Y axis or not.
	 * @return Reversed image data in a new byte array.
	 */
	public static byte[] reverseGrayScale(final byte[] pixels, int width, boolean reverseX, boolean reverseY) {
		final byte[] pixels_publish = new byte[pixels.length];
		final int length = pixels.length;
		final int height = length/width;

		// Reverse X and Y
		if (reverseX && reverseY) {
			for (int i = 0; i < length; i ++) {
				pixels_publish[i] = pixels[length - i - 1];
			}

		// Reverse X
		} else if (reverseX) {
			int offset = 0;
			for (int y = 0; y < height; y ++) {
				int offset_source = offset + width - 1;
				for (int x = 0; x < width; x ++) {
					pixels_publish[offset ++] = pixels[offset_source --];
				}
			}

		// Reverse Y
		} else if (reverseY) {
			int offset = 0;
			for (int y = 0; y < height; y ++) {
				System.arraycopy(pixels, length - offset - width, pixels_publish, offset, width);
				offset += width;
			}

		// Do nothing
		} else {
			System.arraycopy(pixels, 0, pixels_publish, 0, length);
		}

		return pixels_publish;
	}

	/**
	 * Convert an image data array in RGB order to BGR or vice versa.
	 * @param pixels Image data in a byte array.
	 * @return Converted image data array.
	 */
	public static byte[] rgbToBgr(final byte[] pixels) {
		final byte[] pixels_publish = new byte[pixels.length];
		int idx = 0;
		for (int i = 0; i < pixels.length/3; i ++) {
			pixels_publish[idx]     = pixels[idx + 2];
			pixels_publish[idx + 1] = pixels[idx + 1];
			pixels_publish[idx + 2] = pixels[idx];
			idx += 3;
		}
		return pixels_publish;
	}

	/**
	 * Reverse RGB/BGR image data in a byte array.
	 * @param pixels Image data in a byte array.
	 * @param width Width of the image [pixel].
	 * @param reverseX To reverse the image by X axis or not.
	 * @param reverseY To reverse the image by Y axis or not.
	 * @return Reversed image data in a new byte array.
	 */
	public static byte[] reverse(final byte[] pixels, int width, boolean reverseX, boolean reverseY) {
		final byte[] pixels_publish = new byte[pixels.length];
		final int length = pixels.length;
		final int bandwidth = width*3;
		final int height = length/bandwidth;

		// Reverse X and Y
		if (reverseX && reverseY) {
			for (int i = 0; i < length;) {
				pixels_publish[i + 0] = pixels[length - i - 3];
				pixels_publish[i + 1] = pixels[length - i - 2];
				pixels_publish[i + 2] = pixels[length - i - 1];
				i += 3;
			}

		// Reverse X
		} else if (reverseX) {
			int offset = 0;
			for (int y = 0; y < height; y ++) {
				int offset_source = offset + bandwidth - 3;
				for (int x = 0; x < width; x ++) {
					pixels_publish[offset ++] = pixels[offset_source + 0];
					pixels_publish[offset ++] = pixels[offset_source + 1];
					pixels_publish[offset ++] = pixels[offset_source + 2];
					offset_source -= 3;
				}
			}

		// Reverse Y
		} else if (reverseY) {
			int offset = 0;
			for (int y = 0; y < height; y ++) {
				System.arraycopy(pixels, length - offset - bandwidth, pixels_publish, offset, bandwidth);
				offset += bandwidth;
			}

		// Do nothing
		} else {
			System.arraycopy(pixels, 0, pixels_publish, 0, length);
		}

		return pixels_publish;
	}
}

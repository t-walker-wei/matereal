/*
 * PROJECT: napkit at http://mr.digitalmuseum.jp/
 * ----------------------------------------------------------------------------
 *
 * This file is part of NyARToolkit Application Toolkit.
 *
 * NyARToolkit Application Toolkit, or simply "napkit",
 * is a simple wrapper library for NyARToolkit.
 *
 * ----------------------------------------------------------------------------
 *
 * License version: GPL 3.0
 *
 * napkit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * napkit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with napkit. If not, see <http://www.gnu.org/licenses/>.
 */
package jp.digitalmuseum.napkit;

import java.awt.image.BufferedImage;
import java.util.Set;

import com.phybots.utils.Array;
import com.phybots.utils.ScreenRectangle;



/**
 * Interface for detecting markers.
 *
 * @author Jun KATO
 */
public interface NapMarkerDetector {
	final public static int MAX_SQUARE = 300;
	final public static int THRESHOLD_DEFAULT = 100;
	final public static int THRESHOLD_MIN = 0;
	final public static int THRESHOLD_MAX = 255;

	/**
	 * Get markers to be detected.
	 *
	 * @return Set of markers to be detected.
	 */
	public abstract Set<NapMarker> getMarkers();

	/**
	 * Add markers to detect.
	 *
	 * @param markers
	 * @return Returns false if the given set is null or specified markers have mixed formats.
	 */
	public abstract boolean addMarkers(Set<NapMarker> markers);

	/**
	 * Add a marker to detect.
	 *
	 * @param marker
	 * @return Returns false if the specified marker has different formats from the existing markers.
	 */
	public abstract boolean addMarker(NapMarker marker);

	/**
	 * Remove markers to detect.
	 *
	 * @param markers
	 */
	public abstract void removeMarkers(Set<NapMarker> markers);

	/**
	 * Remove a marker to detect.
	 *
	 * @param marker
	 */
	public abstract void removeMarker(NapMarker marker);

	/**
	 * Load a camera parameter from a file.
	 *
	 * @param fileName Name of a camera parameter file.
	 */
	public abstract boolean loadCameraParameter(String fileName);

	/**
	 * Load a camera parameter from a file,
	 * and specify width and height of the screen.
	 *
	 * @param fileName Name of a camera parameter file.
	 * @param width Width of the image captured by the camera.
	 * @param height Height of the image captured by the camera.
	 */
	public abstract boolean loadCameraParameter(String fileName, int width, int height);

	public abstract int getWidth();
	public abstract int getHeight();
	public abstract void setSize(int width, int height);

	/**
	 * Set threshold to binarize full color/gray scale image.
	 *
	 * @param threshold
	 */
	public abstract void setThreshold(int threshold);

	/**
	 * Get the specified threshold value.
	 *
	 * @return
	 */
	public abstract int getThreshold();

	/**
	 * Get the binarized image used for the last detection.
	 *
	 * @return
	 */
	public abstract BufferedImage getBinarizedImage();

	/**
	 * Detect markers in the provided image.
	 *
	 * @param imageData Image data
	 * @return Detection results
	 */
	public Array<NapDetectionResult> detectMarker(Object imageData);

	/**
	 * Set pixel reader by its name.<br>
	 * Pixel reader is used for reading color of each pixel of the image data in {@link #detectMarker(Object)}.
	 * <dl>
	 *	<dt>BYTE1D_B8G8R8_24</dt>
	 *		<dd>Image data is provided as a byte array in BGRBGR... order. (default)</dd>
	 *	<dt>BYTE1D_R8G8B8_24</dt>
	 *		<dd>Image data is provided as a byte array in RGBRGB... order.</dd>
	 *	<dt>BYTE1D_B8G8R8X8_32</dt>
	 *		<dd>Image data is provided as a byte array in BGRXBGRX... order where X means alpha value.</dd>
	 *	<dt>BYTE1D_X8R8G8B8_32</dt>
	 *		<dd>Image data is provided as a byte array in XRGBXRGB... order where X means alpha value.</dd>
	 *	<dt>INT1D_GRAY_8</dt>
	 *		<dd>Image data is provided as an integer array, each of whose element represents gray-scaled value.</dd>
	 *	<dt>INT1D_X8R8G8B8_32</dt>
	 *		<dd>Image data is provided as an integer array, each of whose element represents RGB color with alpha value.</dd>
	 * </dl>
	 *
	 * @param readerName Name of a pixel reader.
	 * @return Whether specified pixel reader is available or not.
	 */
	public boolean setPixelReader(String readerName);

	public abstract NapDetectionResult getResult(NapMarker marker);

	public abstract Array<NapDetectionResult> getResults();

	public abstract Array<ScreenRectangle> getSquares();

	public abstract boolean isTransMatEnabled();

	public abstract void setTransMatEnabled(boolean isTransMatEnabled);

	public abstract double[] getCameraProjectionMatrix();

	public abstract void getCameraProjectionMatrixOut(double[] cameraProjectionMatrix);

	/**
	 * Use {@link #getResults()} instead.
	 */
	@Deprecated
	public abstract Array<NapDetectionResult> getLastMarkerDetectionResult();

	/**
	 * Use {@link #getSquares()} instead.
	 */
	@Deprecated
	public abstract Array<ScreenRectangle> getLastSquareDetectionResult();
}

/*
 * PROJECT: napkit at http://matereal.sourceforge.jp/
 * ----------------------------------------------------------------------------
 *
 * This file is part of NyARToolkit Application Toolkit.
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

import jp.digitalmuseum.utils.ScreenLocation;
import jp.digitalmuseum.utils.ScreenRectangle;
import jp.digitalmuseum.utils.Array;
import jp.digitalmuseum.utils.ScreenPosition;


/**
 * Detection result class. Immutable.
 *
 * @author Jun KATO
 * @see Array
 */
public class NapDetectionResult {
	final private ScreenRectangle screenRectangle = new ScreenRectangle();
	final private ScreenLocation screenLocation = new ScreenLocation();
	private double confidence;
	private NapMarker marker;

	public NapDetectionResult(
			NapMarker marker, ScreenRectangle screenRectangle, double confidence, int direction) {
		this.marker = marker;
		this.confidence = confidence;

		// Calculate position.
		int x = 0, y = 0, i = 0;
		for (ScreenPosition p : screenRectangle) {
			x += p.getX();
			y += p.getY();
			this.screenRectangle.set((direction + 3 + (i ++))%4, p);
		}
		screenLocation.setLocation(
				x/4,
				y/4,
				this.screenRectangle.getRotation());
	}

	/**
	 * Get the detected marker.
	 */
	public NapMarker getMarker() {
		return marker;
	}

	/**
	 * Get confidence of the detected marker.
	 */
	public double getConfidence() {
		return confidence;
	}

	/**
	 * Get location (center position and direction) of the detected marker.
	 * @return
	 */
	public ScreenLocation getLocation() {
		return new ScreenLocation(screenLocation);
	}

	/**
	 * @see #getLocation()
	 */
	public void getLocationOut(ScreenLocation screenLocation) {
		screenLocation.setLocation(this.screenLocation);
	}

	/**
	 * Get center position of the detected marker.
	 * @return
	 */
	public ScreenPosition getPosition() {
		return screenLocation.getPosition();
	}

	/**
	 * @see #getPosition()
	 */
	public void getPositionOut(ScreenPosition screenPosition) {
		screenLocation.getPositionOut(screenPosition);
	}

	/**
	 * Get direction of the detected marker in [radius].
	 */
	public double getDirection() {
		return screenLocation.getRotation();
	}

	/**
	 * Get rectangle positions.
	 * @return
	 */
	public ScreenRectangle getSquare() {
		return screenRectangle;
	}
}
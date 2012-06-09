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

import com.phybots.utils.ScreenRectangle;

/**
 * Detection result class. Immutable.
 *
 * @author Jun KATO
 */
public class NapAssumedDetectionResult extends NapDetectionResult {
	private double[] transformationMatrix;

	NapAssumedDetectionResult(
			NapMarker marker, ScreenRectangle screenRectangle, double confidence, int direction, double[] transformationMatrix) {
		super(marker, screenRectangle, confidence, direction);
		this.transformationMatrix = transformationMatrix.clone();
	}

	@Override
	public boolean getTransformationMatrix(double[] transformationMatrix) {
		return getTransformationMatrix(transformationMatrix, true);
	}

	@Override
	public boolean getTransformationMatrix(double[] transformationMatrix, boolean continuous) {
		System.arraycopy(
				this.transformationMatrix, 0,
				transformationMatrix, 0,
				transformationMatrix.length);
		return true;
	}
}
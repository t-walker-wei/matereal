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

import com.phybots.utils.ScreenPosition;
import com.phybots.utils.ScreenRectangle;

public class NapCameraRelation {
	private static double[] matrix1 = new double[16];
	private static double[] matrix2 = new double[16];
	private double[] primaryToSecondary;
	private double[] secondaryToPrimary;
	private int[] viewport;
	private double[] position;
	private ScreenPosition screenPosition;
	private ScreenRectangle screenRectangle;

	private NapCameraRelation() {
		viewport = new int[4];
		position = new double[3];
		screenPosition = new ScreenPosition();
		screenRectangle = new ScreenRectangle();
	}

	public static NapCameraRelation calcCameraRelation(
			Iterable<NapDetectionResult> primaryCamResults,
			Iterable<NapDetectionResult> secondaryCamResults) {
		NapDetectionResult result1 = null;
		NapDetectionResult result2 = null;
		double confidence = 0;
		for (NapDetectionResult r1 : primaryCamResults) {
			for (NapDetectionResult r2 : secondaryCamResults) {
				if (r2.getMarker() == r1.getMarker()) {
					NapDetectionResult r = r2;
					double c = r1.getConfidence() * r.getConfidence();
					if (c > confidence) {
						result1 = r1;
						result2 = r;
						confidence = c;
					}
				}
			}
		}
		if (result1 == null) {
			return null;
		}
		return calcCameraRelation(result1, result2);
	}

	public static NapCameraRelation calcCameraRelation(
			NapDetectionResult primaryCamResult, NapDetectionResult secondaryCamResult) {
		double[] trans1 = NapCameraRelation.matrix1;
		double[] trans1inv = NapCameraRelation.matrix2;

		if (!primaryCamResult.getTransformationMatrix(trans1)) {
			return null;
		}
		NapUtils.invertMatrix4x4Out(trans1, trans1inv);

		double[] trans2 = NapCameraRelation.matrix1;
		if (!secondaryCamResult.getTransformationMatrix(trans2, false)) {
			return null;
		}

		NapCameraRelation relation = new NapCameraRelation();
		relation.primaryToSecondary = NapUtils.multiplyMatrix4x4(trans2, trans1inv);
		relation.secondaryToPrimary = NapUtils.invertMatrix4x4(relation.primaryToSecondary);
		return relation;
	}

	public double[] assumeModelViewMatrix(NapDetectionResult result, boolean primaryCamIsUnavailable) {
		double[] assumedModelViewMatrix = NapCameraRelation.matrix2;
		if (assumeModelViewMatrixOut(result, primaryCamIsUnavailable, assumedModelViewMatrix)) {
			return assumedModelViewMatrix.clone();
		}
		return null;
	}

	public boolean assumeModelViewMatrixOut(NapDetectionResult result, boolean primaryCamIsUnavailable,
			double[] assumedModelViewMatrix) {
		double[] mvMatrix = NapCameraRelation.matrix1;

		// Get result from secondary camera.
		if (primaryCamIsUnavailable) {
			if (secondaryToPrimary == null || !result.getTransformationMatrix(mvMatrix, false)) {
				return false;
			}
			NapUtils.multiplyMatrix4x4Out(secondaryToPrimary, mvMatrix, assumedModelViewMatrix);
		}

		// Get result from primary camera.
		else {
			if (primaryToSecondary == null || !result.getTransformationMatrix(mvMatrix, false)) {
				return false;
			}
			NapUtils.multiplyMatrix4x4Out(primaryToSecondary, mvMatrix, assumedModelViewMatrix);
		}
		return true;
	}

	public void assumePositionOut(double x, double y, double z,
			double[] assumedModelViewMatrix,
			double[] cameraProjectionMatrix,
			int width, int height,
			ScreenPosition p) {
		viewport[2] = width;
		viewport[3] = height;
		NapUtils.calcProjection(0, 0, 0,
				assumedModelViewMatrix,
				cameraProjectionMatrix,
				viewport,
				position);
		p.set((int) position[0], (int) position[1]);
	}

	public NapAssumedDetectionResult assumeDetectionResult
			(NapMarkerDetector detector, NapDetectionResult result) {
		return assumeDetectionResult(detector, result, true);
	}

	public NapAssumedDetectionResult assumeDetectionResult(
			NapMarkerDetector detector, NapDetectionResult result, boolean primaryCamIsUnavailable) {

		double[] assumedModelViewMatrix = NapCameraRelation.matrix2;
		assumeModelViewMatrixOut(result, primaryCamIsUnavailable, assumedModelViewMatrix);

		double[] cameraProjectionMatrix = NapCameraRelation.matrix1;
		detector.getCameraProjectionMatrixOut(cameraProjectionMatrix);

		NapMarker marker = result.getMarker();
		double d = marker.getRealSize() / 2;

		int width = detector.getWidth();
		int height = detector.getHeight();

		assumePositionOut(-d, -d, 0,
				assumedModelViewMatrix, cameraProjectionMatrix, width, height, screenPosition);
		screenRectangle.set(0, screenPosition);
		assumePositionOut(-d,  d, 0,
				assumedModelViewMatrix, cameraProjectionMatrix, width, height, screenPosition);
		screenRectangle.set(1, screenPosition);
		assumePositionOut( d,  d, 0,
				assumedModelViewMatrix, cameraProjectionMatrix, width, height, screenPosition);
		screenRectangle.set(2, screenPosition);
		assumePositionOut( d, -d, 0,
				assumedModelViewMatrix, cameraProjectionMatrix, width, height, screenPosition);
		screenRectangle.set(3, screenPosition);

		NapAssumedDetectionResult assumedResult = new NapAssumedDetectionResult(
				marker, screenRectangle, result.getConfidence(), 0, assumedModelViewMatrix);
		return assumedResult;
	}

}

package jp.digitalmuseum.napkit;

public class NapCameraRelation {
	private static double[] matrix1 = new double[16];
	private static double[] matrix2 = new double[16];
	private double[] primaryToSecondary;
	private double[] secondaryToPrimary;

	private NapCameraRelation() {
		// Do nothing.
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

	public NapAssumedDetectionResult assumeDetectionResult(NapDetectionResult result) {
		return assumeDetectionResult(result, true);
	}

	public NapAssumedDetectionResult assumeDetectionResult(NapDetectionResult result, boolean primaryCamIsUnavailable) {
		// TODO
		return null;
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

	// TODO
	public void assumePositionOut() {
		/*
		int[] viewport = new int[] { 0, 0, capture.getWidth(), capture.getHeight() };
		double[] winPos = new double[3];
		NapUtils.calcProjection(0, 0, 0,
				assumedModelViewMatrix,
				detector.getCameraProjectionMatrix(),
				viewport,
				winPos);
		*/
	}
}

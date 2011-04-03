/*
 * PROJECT: NyARToolkit
 * --------------------------------------------------------------------------------
 * This work is based on the original ARToolKit developed by
 *   Hirokazu Kato
 *   Mark Billinghurst
 *   HITLab, University of Washington, Seattle
 * http://www.hitl.washington.edu/artoolkit/
 *
 * The NyARToolkit is Java edition ARToolKit class library.
 * Copyright (C)2008-2009 Ryo Iizuka
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * For further information please contact.
 *	http://nyatla.jp/nyatoolkit/
 *	<airmail(at)ebony.plala.or.jp> or <nyatla(at)nyatla.jp>
 *
 */
package jp.digitalmuseum.napkit;

import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import jp.digitalmuseum.utils.Array;
import jp.digitalmuseum.utils.ScreenRectangle;
import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.NyARMat;
import jp.nyatla.nyartoolkit.core.match.NyARMatchPattDeviationColorData;
import jp.nyatla.nyartoolkit.core.match.NyARMatchPattResult;
import jp.nyatla.nyartoolkit.core.param.NyARParam;
import jp.nyatla.nyartoolkit.core.pickup.INyARColorPatt;
import jp.nyatla.nyartoolkit.core.pickup.NyARColorPatt_Perspective_O2;
import jp.nyatla.nyartoolkit.core.rasterfilter.rgb2bin.INyARRasterFilter_Rgb2Bin;
import jp.nyatla.nyartoolkit.core.rasterfilter.rgb2bin.NyARRasterFilter_ARToolkitThreshold;
import jp.nyatla.nyartoolkit.core.squaredetect.NyARCoord2Linear;
import jp.nyatla.nyartoolkit.core.squaredetect.NyARSquare;
import jp.nyatla.nyartoolkit.core.squaredetect.NyARSquareContourDetector;
import jp.nyatla.nyartoolkit.core.squaredetect.NyARSquareContourDetector_Rle;
import jp.nyatla.nyartoolkit.core.types.NyARBufferType;
import jp.nyatla.nyartoolkit.core.types.NyARIntPoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;
import jp.nyatla.nyartoolkit.core.types.NyARLinear;

public class NapMarkerDetectorImpl implements NapMarkerDetector {
	private NyARParam param;
	private Set<NapMarker> markers;
	private DetectSquareCallback detectorCallback;
	private INyARRasterFilter_Rgb2Bin binarizationFilter;
	private Array<NapDetectionResult> results;
	private Array<ScreenRectangle> squares;

	// initialized at updateScreenSize
	private NyARSquareContourDetector squareDetector;
	private NyARBinRaster binarizedImage;
	private NyARRgbRaster_BGR image;

	// initialized at updateMarkerSize
	private INyARColorPatt squareImage;
	private NyARMatchPattDeviationColorData deviationData;

	// updated by setThreshold
	private int threshold = THRESHOLD_DEFAULT;

	// used for transformation matrix calculation
	private boolean isTransMatEnabled;
	private NapTransMat transmat;
	private NyARCoord2Linear coordline;
	private double[] cameraProjectionMatrix;
	private double viewDistanceMin = 0.1;
	private double viewDistanceMax = 100.0;

	public NapMarkerDetectorImpl() {
		param = NapUtils.getInitialCameraParameter();
		markers = new HashSet<NapMarker>();
		detectorCallback = new DetectSquareCallback();
		try {
			transmat = new NapTransMat(param);
			binarizationFilter = new NyARRasterFilter_ARToolkitThreshold(THRESHOLD_DEFAULT, NyARBufferType.BYTE1D_B8G8R8_24);
		} catch (NyARException e) {
			// This won't occur.
		}
		results = new Array<NapDetectionResult>();
		squares = new Array<ScreenRectangle>();
		updateScreenSize();
	}

	public boolean addMarkers(Set<NapMarker> markers) {
		boolean needUpdateMarkerSize = true;
		int markerWidth = -1, markerHeight = -1;
		if (markers.size() > 0) {
			final NapMarker sampleMarker = markers.iterator().next();
			markerWidth = sampleMarker.getWidth();
			markerHeight = sampleMarker.getHeight();
			needUpdateMarkerSize = false;
		}
		for (NapMarker marker : markers) {
			if (markerWidth == -1 && markerHeight == -1) {
				markerWidth = marker.getWidth();
				markerHeight = marker.getHeight();
			} else {
				if (markerWidth != marker.getWidth()
						|| markerHeight != marker.getHeight()) {
					return false;
				}
			}
			this.markers.add(marker);
		}
		if (needUpdateMarkerSize) {
			updateMarkerSize();
		}
		return true;
	}

	public boolean addMarker(NapMarker marker) {
		if (markers.size() == 0) {
			markers.add(marker);
			updateMarkerSize();
			return true;
		}
		final NapMarker sampleMarker = markers.iterator().next();
		if (sampleMarker.getWidth() != marker.getWidth()
				|| sampleMarker.getHeight() != marker.getHeight()) {
			return false;
		}
		markers.add(marker);
		return true;
	}

	public boolean removeMarkers(Set<NapMarker> markers) {
		boolean result = true;
		for (NapMarker marker : markers) {
			result = markers.remove(marker) && result;
		}
		return result;
	}

	public boolean removeMarker(NapMarker marker) {
		return markers.remove(marker);
	}

	public Array<NapDetectionResult> detectMarker(byte[] imageData) {
		synchronized (squareDetector) {
			squares.clear();
			results.clear();
			try {
				image.wrapBuffer(imageData);
				binarizationFilter.doFilter(image, binarizedImage);
				squareDetector.detectMarkerCB(binarizedImage, detectorCallback);
			} catch (NyARException e) {
				e.printStackTrace();
			}
			return results;
		}
	}

	public Array<NapDetectionResult> getLastMarkerDetectionResult() {
		synchronized (squareDetector) {
			return new Array<NapDetectionResult>(results);
		}
	}

	public Array<ScreenRectangle> getLastSquareDetectionResult() {
		synchronized (squareDetector) {
			return new Array<ScreenRectangle>(squares);
		}
	}

	public void loadCameraParameter(String fileName) {

		// Back up the screen size.
		final NyARIntSize size = param.getScreenSize();
		final int width = size.w, height = size.h;

		// Load the parameter file.
		try {
			param.loadARParamFromFile(fileName);
		} catch (NyARException e) {
			throw new IllegalArgumentException(e);
		}

		// Restore the screen size.
		param.changeScreenSize(width, height);
		updateScreenSize();
	}

	public void loadCameraParameter(String fileName, int width, int height) {
		try {
			param.loadARParamFromFile(fileName);
		} catch (NyARException e) {
			throw new IllegalArgumentException(e);
		}
		param.changeScreenSize(width, height);
		updateScreenSize();
	}

	public int getWidth() {
		return param.getScreenSize().w;
	}

	public int getHeight() {
		return param.getScreenSize().h;
	}

	public void setSize(int width, int height) {
		param.changeScreenSize(width, height);
		updateScreenSize();
	}

	public int getThreshold() {
		return threshold;
	}

	public void setThreshold(int threshold) {
		this.threshold = threshold;
		((NyARRasterFilter_ARToolkitThreshold) binarizationFilter).setThreshold(threshold);
	}

	public synchronized BufferedImage getBinarizedImage() {
		if (binarizedImage == null) {
			return null;
		}
		return binarizedImage.getImage();
	}

	public boolean isTransMatEnabled() {
		return isTransMatEnabled;
	}

	public void setTransMatEnabled(boolean isTransMatEnabled) {
		this.isTransMatEnabled = isTransMatEnabled;
	}

	public double[] getCameraProjectionMatrix() {
		double[] cameraProjectionMatrix = new double[16];
		getCameraProjectionMatrixOut(cameraProjectionMatrix);
		return cameraProjectionMatrix;
	}

	public void getCameraProjectionMatrixOut(double[] cameraProjectionMatrix) {
		if (this.cameraProjectionMatrix == null) {
			this.cameraProjectionMatrix = new double[16];
			toCameraFrustumRH(param, this.cameraProjectionMatrix);
		}
		System.arraycopy(
				this.cameraProjectionMatrix, 0,
				cameraProjectionMatrix, 0,
				16);
	}

	/**
	 * Update screen size and initialize camera parameter related fields.
	 */
	private void updateScreenSize() {
		final NyARIntSize size = param.getScreenSize();
		try {
			squareDetector = new NyARSquareContourDetector_Rle(size);
			image = new NyARRgbRaster_BGR(size.w, size.h);
			binarizedImage = new NyARBinRaster(size.w, size.h);
			coordline = new NyARCoord2Linear(size, param.getDistortionFactor());
			cameraProjectionMatrix = null; // @see #getCameraProjectionMatrixOut(double[])
		} catch (NyARException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Update marker size and initialize workspaces for comparing a marker with images.
	 */
	private void updateMarkerSize() {
		final NapMarker sampleMarker = markers.iterator().next();
		final int markerWidth = sampleMarker.getWidth();
		final int markerHeight = sampleMarker.getHeight();
		squareImage = new NyARColorPatt_Perspective_O2(markerWidth, markerHeight, 4, 25);
		deviationData = new NyARMatchPattDeviationColorData(markerWidth, markerHeight);
	}

	public void setViewDistanceMin(double i_new_value) {
		this.viewDistanceMin = i_new_value;
	}

	public void setViewDistanceMax(double i_new_value) {
		this.viewDistanceMax = i_new_value;
	}

	/**
	 * void arglCameraFrustumRH(const ARParam *cparam, const double focalmin,
	 * const double focalmax, GLdouble m_projection[16]) 関数の置き換え
	 * NyARParamからOpenGLのProjectionを作成します。
	 *
	 * @param i_arparam
	 * @param o_gl_projection
	 *            double[16]を指定して下さい。
	 */
	private void toCameraFrustumRH(NyARParam i_arparam, double[] o_gl_projection) {
		NyARMat trans_mat = new NyARMat(3, 4);
		NyARMat icpara_mat = new NyARMat(3, 4);
		double[][] p = new double[3][3], q = new double[4][4];
		int i, j;

		final NyARIntSize size = i_arparam.getScreenSize();
		final int width = size.w;
		final int height = size.h;

		i_arparam.getPerspectiveProjectionMatrix().decompMat(icpara_mat,
				trans_mat);

		double[][] icpara = icpara_mat.getArray();
		double[][] trans = trans_mat.getArray();
		for (i = 0; i < 4; i++) {
			icpara[1][i] = (height - 1) * (icpara[2][i]) - icpara[1][i];
		}

		for (i = 0; i < 3; i++) {
			for (j = 0; j < 3; j++) {
				p[i][j] = icpara[i][j] / icpara[2][2];
			}
		}
		q[0][0] = (2.0 * p[0][0] / (width - 1));
		q[0][1] = (2.0 * p[0][1] / (width - 1));
		q[0][2] = -((2.0 * p[0][2] / (width - 1)) - 1.0);
		q[0][3] = 0.0;

		q[1][0] = 0.0;
		q[1][1] = -(2.0 * p[1][1] / (height - 1));
		q[1][2] = -((2.0 * p[1][2] / (height - 1)) - 1.0);
		q[1][3] = 0.0;

		q[2][0] = 0.0;
		q[2][1] = 0.0;
		q[2][2] = (viewDistanceMax + viewDistanceMin)
				/ (viewDistanceMin - viewDistanceMax);
		q[2][3] = 2.0 * viewDistanceMax * viewDistanceMin
				/ (viewDistanceMin - viewDistanceMax);

		q[3][0] = 0.0;
		q[3][1] = 0.0;
		q[3][2] = -1.0;
		q[3][3] = 0.0;

		for (i = 0; i < 4; i++) { // Row.
			// First 3 columns of the current row.
			for (j = 0; j < 3; j++) { // Column.
				o_gl_projection[i + j * 4] = q[i][0] * trans[0][j] + q[i][1]
						* trans[1][j] + q[i][2] * trans[2][j];
			}
			// Fourth column of the current row.
			o_gl_projection[i + 3 * 4] = q[i][0] * trans[0][3] + q[i][1]
					* trans[1][3] + q[i][2] * trans[2][3] + q[i][3];
		}
		return;
	}

	private class DetectSquareCallback implements
			NyARSquareContourDetector.IDetectMarkerCallback {
		private final NyARMatchPattResult matchingResult = new NyARMatchPattResult();
		private NyARIntPoint2d[] __tmp_vertex = NyARIntPoint2d.createArray(4);

		public void onSquareDetect(
				NyARSquareContourDetector squareDetector,
				int[] coordX, int[] coordY, int coordNum,
				int[] vertexIndex) throws NyARException {

			// 輪郭座標から頂点リストに変換
			NyARIntPoint2d[] vertex = this.__tmp_vertex;
			vertex[0].x = coordX[vertexIndex[0]];
			vertex[0].y = coordY[vertexIndex[0]];
			vertex[1].x = coordX[vertexIndex[1]];
			vertex[1].y = coordY[vertexIndex[1]];
			vertex[2].x = coordX[vertexIndex[2]];
			vertex[2].y = coordY[vertexIndex[2]];
			vertex[3].x = coordX[vertexIndex[3]];
			vertex[3].y = coordY[vertexIndex[3]];
			ScreenRectangle square = NapUtils.convertToScreenRectangle(vertex);
			squares.push(square);

			// 最初のパターン候補を取得
			int direction;
			double confidence;
			Iterator<NapMarker> markerIterator = markers.iterator();
			if (!markerIterator.hasNext()) {
				return;
			}

			// 画像を取得
			if (!squareImage.pickFromRaster(image, vertex)) {
				return;
			}

			// 取得パターンをカラー差分データに変換して評価する。
			deviationData.setRaster(squareImage);

			// 最も一致するパターンを割り当てる。
			NapMarker marker = markerIterator.next();
			marker.getPattern().evaluate(deviationData, matchingResult);
			direction = matchingResult.direction;
			confidence = matchingResult.confidence;
			while (markerIterator.hasNext()) {
				NapMarker m = markerIterator.next();
				m.getPattern().evaluate(deviationData, matchingResult);
				if (confidence > matchingResult.confidence) {
					continue;
				}
				// もっと一致するマーカーがあったぽい
				marker = m;
				direction = matchingResult.direction;
				confidence = matchingResult.confidence;
			}

			// 最も一致したマーカ情報を、この矩形の情報として記録する。
			final NapDetectionResult result;
			NyARSquare sq = null;
			if (isTransMatEnabled) {
				sq = new NyARSquare();
				for (int i = 0; i < 4; i ++) {
					int idx = (i+4-direction) % 4;
					coordline.coord2Line(vertexIndex[idx], vertexIndex[(idx+1)%4], coordX, coordY, coordNum, sq.line[i]);
				}
				for (int i = 0; i < 4; i++) {
					if(!NyARLinear.crossPos(sq.line[i],sq.line[(i + 3) % 4],sq.sqvertex[i])){
						sq = null;
						break;
						// throw new NyARException();
					}
				}
			}
			if (sq == null) {
				result = new NapDetectionResult(
						marker, square, confidence, direction);
			} else {
				result = new NapDetectionResult(
						marker, square, confidence, direction, transmat, sq);
			}
			results.push(result);
		}
	}
}

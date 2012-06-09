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

import java.nio.IntBuffer;

import javax.media.opengl.glu.GLU;

import com.phybots.utils.ScreenPosition;
import com.phybots.utils.ScreenRectangle;

import jp.nyatla.nyartoolkit.core.param.NyARParam;
import jp.nyatla.nyartoolkit.core.types.NyARIntPoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;

/**
 * Napkit-related utility class.
 *
 * @author Jun KATO
 */
public class NapUtils {

	public static int M00 = 0 + 0 * 4;
	public static int M01 = 0 + 1 * 4;
	public static int M02 = 0 + 2 * 4;
	public static int M03 = 0 + 3 * 4;

	public static int M10 = 1 + 0 * 4;
	public static int M11 = 1 + 1 * 4;
	public static int M12 = 1 + 2 * 4;
	public static int M13 = 1 + 3 * 4;

	public static int M20 = 2 + 0 * 4;
	public static int M21 = 2 + 1 * 4;
	public static int M22 = 2 + 2 * 4;
	public static int M23 = 2 + 3 * 4;

	public static int M30 = 3 + 0 * 4;
	public static int M31 = 3 + 1 * 4;
	public static int M32 = 3 + 2 * 4;
	public static int M33 = 3 + 3 * 4;

	private static double[] in = new double[16];
	private static double[] out = new double[16];

	public static double[] invertMatrix4x4(double[] matrix) {
		if (invertMatrix4x4Out(matrix, out)) {
			return out.clone();
		}
		return null;
	}

	public static void main(String[] args) {

		double[] matrix = new double[16];
		for (int i = 0; i < 4; i ++) {
			matrix[i + i * 4] = 1;
		}
		printMatrix(matrix);
		System.out.println();

		double[] inverted = invertMatrix4x4(matrix);
		System.out.println("inverted:");
		printMatrix(inverted);
		System.out.println();

		double[] multiplied = multiplyMatrix4x4(matrix, inverted);
		System.out.println("multiplied:");
		printMatrix(multiplied);
	}

	public static void printMatrix(double[] matrix) {
		for (int i = 0; i < 4; i ++) {
			for (int j = 0; j < 4; j ++) {
				System.out.print(String.format("%.4f", matrix[i + j * 4]));
				if (j + 1 < 4) {
					System.out.print(" ");
				} else {
					System.out.println();
				}
			}
		}
	}

	public static boolean invertMatrix4x4Out(double[] matrix, double[] result) {

		double s = determinantMatrix4x4(matrix);

		if (s == 0.0) {
			return false;
		}

		result[M00] = matrix[M11]*(matrix[M22]*matrix[M33] - matrix[M23]*matrix[M32]) + matrix[M12]*(matrix[M23]*matrix[M31] - matrix[M21]*matrix[M33]) + matrix[M13]*(matrix[M21]*matrix[M32] - matrix[M22]*matrix[M31]);
		result[M01] = matrix[M21]*(matrix[M02]*matrix[M33] - matrix[M03]*matrix[M32]) + matrix[M22]*(matrix[M03]*matrix[M31] - matrix[M01]*matrix[M33]) + matrix[M23]*(matrix[M01]*matrix[M32] - matrix[M02]*matrix[M31]);
		result[M02] = matrix[M31]*(matrix[M02]*matrix[M13] - matrix[M03]*matrix[M12]) + matrix[M32]*(matrix[M03]*matrix[M11] - matrix[M01]*matrix[M13]) + matrix[M33]*(matrix[M01]*matrix[M12] - matrix[M02]*matrix[M11]);
		result[M03] = matrix[M01]*(matrix[M13]*matrix[M22] - matrix[M12]*matrix[M23]) + matrix[M02]*(matrix[M11]*matrix[M23] - matrix[M13]*matrix[M21]) + matrix[M03]*(matrix[M12]*matrix[M21] - matrix[M11]*matrix[M22]);

		result[M10] = matrix[M12]*(matrix[M20]*matrix[M33] - matrix[M23]*matrix[M30]) + matrix[M13]*(matrix[M22]*matrix[M30] - matrix[M20]*matrix[M32]) + matrix[M10]*(matrix[M23]*matrix[M32] - matrix[M22]*matrix[M33]);
		result[M11] = matrix[M22]*(matrix[M00]*matrix[M33] - matrix[M03]*matrix[M30]) + matrix[M23]*(matrix[M02]*matrix[M30] - matrix[M00]*matrix[M32]) + matrix[M20]*(matrix[M03]*matrix[M32] - matrix[M02]*matrix[M33]);
		result[M12] = matrix[M32]*(matrix[M00]*matrix[M13] - matrix[M03]*matrix[M10]) + matrix[M33]*(matrix[M02]*matrix[M10] - matrix[M00]*matrix[M12]) + matrix[M30]*(matrix[M03]*matrix[M12] - matrix[M02]*matrix[M13]);
		result[M13] = matrix[M02]*(matrix[M13]*matrix[M20] - matrix[M10]*matrix[M23]) + matrix[M03]*(matrix[M10]*matrix[M22] - matrix[M12]*matrix[M20]) + matrix[M00]*(matrix[M12]*matrix[M23] - matrix[M13]*matrix[M22]);

		result[M20] = matrix[M13]*(matrix[M20]*matrix[M31] - matrix[M21]*matrix[M30]) + matrix[M10]*(matrix[M21]*matrix[M33] - matrix[M23]*matrix[M31]) + matrix[M11]*(matrix[M23]*matrix[M30] - matrix[M20]*matrix[M33]);
		result[M21] = matrix[M23]*(matrix[M00]*matrix[M31] - matrix[M01]*matrix[M30]) + matrix[M20]*(matrix[M01]*matrix[M33] - matrix[M03]*matrix[M31]) + matrix[M21]*(matrix[M03]*matrix[M30] - matrix[M00]*matrix[M33]);
		result[M22] = matrix[M33]*(matrix[M00]*matrix[M11] - matrix[M01]*matrix[M10]) + matrix[M30]*(matrix[M01]*matrix[M13] - matrix[M03]*matrix[M11]) + matrix[M31]*(matrix[M03]*matrix[M10] - matrix[M00]*matrix[M13]);
		result[M23] = matrix[M03]*(matrix[M11]*matrix[M20] - matrix[M10]*matrix[M21]) + matrix[M00]*(matrix[M13]*matrix[M21] - matrix[M11]*matrix[M23]) + matrix[M01]*(matrix[M10]*matrix[M23] - matrix[M13]*matrix[M20]);

		result[M30] = matrix[M10]*(matrix[M22]*matrix[M31] - matrix[M21]*matrix[M32]) + matrix[M11]*(matrix[M20]*matrix[M32] - matrix[M22]*matrix[M30]) + matrix[M12]*(matrix[M21]*matrix[M30] - matrix[M20]*matrix[M31]);
		result[M31] = matrix[M20]*(matrix[M02]*matrix[M31] - matrix[M01]*matrix[M32]) + matrix[M21]*(matrix[M00]*matrix[M32] - matrix[M02]*matrix[M30]) + matrix[M22]*(matrix[M01]*matrix[M30] - matrix[M00]*matrix[M31]);
		result[M32] = matrix[M30]*(matrix[M02]*matrix[M11] - matrix[M01]*matrix[M12]) + matrix[M31]*(matrix[M00]*matrix[M12] - matrix[M02]*matrix[M10]) + matrix[M32]*(matrix[M01]*matrix[M10] - matrix[M00]*matrix[M11]);
		result[M33] = matrix[M00]*(matrix[M11]*matrix[M22] - matrix[M12]*matrix[M21]) + matrix[M01]*(matrix[M12]*matrix[M20] - matrix[M10]*matrix[M22]) + matrix[M02]*(matrix[M10]*matrix[M21] - matrix[M11]*matrix[M20]);

		multiplyMatrix4x4Out(result, 1/s, result);
		return true;
	}

	public static double[] multiplyMatrix4x4(double[] m1, double scale) {
		double[] result = new double[16];
		multiplyMatrix4x4Out(m1, scale, result);
		return result;
	}

	public static void multiplyMatrix4x4Out(double[] m1, double scale, double[] result) {
		for (int i = 0; i < m1.length; i ++) {
			result[i] = m1[i] * scale;
		}
	}

	public static double[] multiplyMatrix4x4(double[] m1, double[] m2) {
		double[] result = new double[16];
		multiplyMatrix4x4Out(m1, m2, result);
		return result;
	}

	public static void multiplyMatrix4x4Out(double[] m1, double[] m2, double[] result) {
		result[M00] = m1[M00]*m2[M00] + m1[M01]*m2[M10] + m1[M02]*m2[M20] + m1[M03]*m2[M30];
		result[M01] = m1[M00]*m2[M01] + m1[M01]*m2[M11] + m1[M02]*m2[M21] + m1[M03]*m2[M31];
		result[M02] = m1[M00]*m2[M02] + m1[M01]*m2[M12] + m1[M02]*m2[M22] + m1[M03]*m2[M32];
		result[M03] = m1[M00]*m2[M03] + m1[M01]*m2[M13] + m1[M02]*m2[M23] + m1[M03]*m2[M33];

		result[M10] = m1[M10]*m2[M00] + m1[M11]*m2[M10] + m1[M12]*m2[M20] + m1[M13]*m2[M30];
		result[M11] = m1[M10]*m2[M01] + m1[M11]*m2[M11] + m1[M12]*m2[M21] + m1[M13]*m2[M31];
		result[M12] = m1[M10]*m2[M02] + m1[M11]*m2[M12] + m1[M12]*m2[M22] + m1[M13]*m2[M32];
		result[M13] = m1[M10]*m2[M03] + m1[M11]*m2[M13] + m1[M12]*m2[M23] + m1[M13]*m2[M33];

		result[M20] = m1[M20]*m2[M00] + m1[M21]*m2[M10] + m1[M22]*m2[M20] + m1[M23]*m2[M30];
		result[M21] = m1[M20]*m2[M01] + m1[M21]*m2[M11] + m1[M22]*m2[M21] + m1[M23]*m2[M31];
		result[M22] = m1[M20]*m2[M02] + m1[M21]*m2[M12] + m1[M22]*m2[M22] + m1[M23]*m2[M32];
		result[M23] = m1[M20]*m2[M03] + m1[M21]*m2[M13] + m1[M22]*m2[M23] + m1[M23]*m2[M33];

		result[M30] = m1[M30]*m2[M00] + m1[M31]*m2[M10] + m1[M32]*m2[M20] + m1[M33]*m2[M30];
		result[M31] = m1[M30]*m2[M01] + m1[M31]*m2[M11] + m1[M32]*m2[M21] + m1[M33]*m2[M31];
		result[M32] = m1[M30]*m2[M02] + m1[M31]*m2[M12] + m1[M32]*m2[M22] + m1[M33]*m2[M32];
		result[M33] = m1[M30]*m2[M03] + m1[M31]*m2[M13] + m1[M32]*m2[M23] + m1[M33]*m2[M33];
	}

	public static double determinantMatrix4x4(double[] matrix) {
		return
			 (matrix[M00]*matrix[M11] - matrix[M01]*matrix[M10])*(matrix[M22]*matrix[M33] - matrix[M23]*matrix[M32])
			-(matrix[M00]*matrix[M12] - matrix[M02]*matrix[M10])*(matrix[M21]*matrix[M33] - matrix[M23]*matrix[M31])
			+(matrix[M00]*matrix[M13] - matrix[M03]*matrix[M10])*(matrix[M21]*matrix[M32] - matrix[M22]*matrix[M31])
			+(matrix[M01]*matrix[M12] - matrix[M02]*matrix[M11])*(matrix[M20]*matrix[M33] - matrix[M23]*matrix[M30])
			-(matrix[M01]*matrix[M13] - matrix[M03]*matrix[M11])*(matrix[M20]*matrix[M32] - matrix[M22]*matrix[M30])
			+(matrix[M02]*matrix[M13] - matrix[M03]*matrix[M12])*(matrix[M20]*matrix[M31] - matrix[M21]*matrix[M30]);
	}

	public static void convertMatrix4x4toGl(double[] matrix) {
		matrix[M10] = -matrix[M10];
		matrix[M11] = -matrix[M11];
		matrix[M12] = -matrix[M12];
		matrix[M13] = -matrix[M13];

		matrix[M20] = -matrix[M20];
		matrix[M21] = -matrix[M21];
		matrix[M22] = -matrix[M22];
		matrix[M23] = -matrix[M23];

		matrix[M03] = matrix[M03] * 0.025;
		matrix[M13] = matrix[M13] * 0.025;
		matrix[M23] = matrix[M23] * 0.025;
	}

	/**
	 * This method just does the same calculation with gluProject method of {@link GLU} class.
	 *
	 * @see GLU#gluProject(double, double, double, java.nio.DoubleBuffer, java.nio.DoubleBuffer, IntBuffer, java.nio.DoubleBuffer)
	 * @param x
	 * @param y
	 * @param z
	 * @param modelViewMatrix
	 * @param cameraProjectionMatrix
	 * @param viewport
	 * @param result
	 * @return
	 */
    public static boolean calcProjection(double x, double y, double z, double[] modelViewMatrix, double[] cameraProjectionMatrix, int[] viewport, double[] result)
    {
		in[0] = x;
		in[1] = y;
		in[2] = z;
		in[3] = 1.0D;
		multiplyMatrix4x4Out(modelViewMatrix, in, out);
		multiplyMatrix4x4Out(cameraProjectionMatrix, out, in);
		if (in[3] == 0.0D) {
			return false;
		} else {
			in[3] = (1.0D / in[3]) * 0.5D;
			in[0] = in[0] * in[3] + 0.5D;
			in[1] = in[1] * in[3] + 0.5D;
			in[2] = in[2] * in[3] + 0.5D;
			result[0] = in[0] * (double) viewport[2] + (double) viewport[0];
			result[1] = in[1] * (double) viewport[3] + (double) viewport[1];
			result[2] = in[2];
			return true;
		}
    }

	public static ScreenPosition convertToScreenPosition(NyARIntPoint2d vertex) {
		return new ScreenPosition(vertex.x, vertex.y);
	}

	public static ScreenRectangle convertToScreenRectangle(NyARIntPoint2d[] vertex) {
		return new ScreenRectangle(
				NapUtils.convertToScreenPosition(vertex[0]),
				NapUtils.convertToScreenPosition(vertex[1]),
				NapUtils.convertToScreenPosition(vertex[2]),
				NapUtils.convertToScreenPosition(vertex[3])
			);
	}

	/**
	 * Returns a default camera parameter object.
	 */
	public static NyARParam getInitialCameraParameter() {
		final NyARParam p = new NyARParam();
		final NyARIntSize size = p.getScreenSize();
		size.w = 640;
		size.h = 480;
		p.setValue(new double[] {
			318.5,
			263.5,
			26.2,
			1.0127565206658486
		}, new double[] {
			700.9514702992245,
			0.0,
			316.5,
			0.0,
			0.0,
			726.0941816535367,
			241.5,
			0.0,
			0.0,
			0.0,
			1.0,
			0.0
		});
		return p;
	}
}

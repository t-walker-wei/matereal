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

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.param.NyARParam;
import jp.nyatla.nyartoolkit.core.squaredetect.NyARSquare;
import jp.nyatla.nyartoolkit.core.transmat.NyARTransMat;
import jp.nyatla.nyartoolkit.core.transmat.NyARTransMatResult;

public class NapTransMat {
	private NyARTransMat transmat;

	public NapTransMat(NyARParam param) throws NyARException {
		transmat = new NyARTransMat(param);
	}

	public void transMat(NyARSquare square, NapMarker marker,
			double[] transformationMatrix, boolean continuous) throws NyARException {
		if (continuous) {
			transmat.transMatContinue(square, marker.getOffset(), marker.transMatResult);
		} else {
			transmat.transMat(square, marker.getOffset(), marker.transMatResult);
		}
		toCameraViewRH(marker.transMatResult, transformationMatrix);
	}

	/**
	 * NyARTransMatResultをOpenGLの行列へ変換します。
	 *
	 * @param nyResult
	 * @param glResult
	 * @throws NyARException
	 */
	public static void toCameraViewRH(NyARTransMatResult nyResult, double[] glResult) throws NyARException {
		glResult[0 + 0 * 4] = nyResult.m00;
		glResult[0 + 1 * 4] = nyResult.m01;
		glResult[0 + 2 * 4] = nyResult.m02;
		glResult[0 + 3 * 4] = nyResult.m03;
		glResult[1 + 0 * 4] = nyResult.m10;
		glResult[1 + 1 * 4] = nyResult.m11;
		glResult[1 + 2 * 4] = nyResult.m12;
		glResult[1 + 3 * 4] = nyResult.m13;
		glResult[2 + 0 * 4] = nyResult.m20;
		glResult[2 + 1 * 4] = nyResult.m21;
		glResult[2 + 2 * 4] = nyResult.m22;
		glResult[2 + 3 * 4] = nyResult.m23;
		glResult[3 + 0 * 4] = 0.0;
		glResult[3 + 1 * 4] = 0.0;
		glResult[3 + 2 * 4] = 0.0;
		glResult[3 + 3 * 4] = 1.0;
	}
}

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
	 * @param i_ny_result
	 * @param o_gl_result
	 * @throws NyARException
	 */
	public void toCameraViewRH(NyARTransMatResult i_ny_result, double[] o_gl_result) throws NyARException {
		o_gl_result[0 + 0 * 4] = i_ny_result.m00;
		o_gl_result[0 + 1 * 4] = i_ny_result.m01;
		o_gl_result[0 + 2 * 4] = i_ny_result.m02;
		o_gl_result[0 + 3 * 4] = i_ny_result.m03 * 0.025;
		o_gl_result[1 + 0 * 4] = -i_ny_result.m10;
		o_gl_result[1 + 1 * 4] = -i_ny_result.m11;
		o_gl_result[1 + 2 * 4] = -i_ny_result.m12;
		o_gl_result[1 + 3 * 4] = -i_ny_result.m13 * 0.025;
		o_gl_result[2 + 0 * 4] = -i_ny_result.m20;
		o_gl_result[2 + 1 * 4] = -i_ny_result.m21;
		o_gl_result[2 + 2 * 4] = -i_ny_result.m22;
		o_gl_result[2 + 3 * 4] = -i_ny_result.m23 * 0.025;
		o_gl_result[3 + 0 * 4] = 0.0;
		o_gl_result[3 + 1 * 4] = 0.0;
		o_gl_result[3 + 2 * 4] = 0.0;
		o_gl_result[3 + 3 * 4] = 1.0;
	}
}

/*
 * PROJECT: NyARToolkit JOGL utilities.
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

import java.nio.*;
import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;

import jp.digitalmuseum.mr.service.Camera;
import jp.digitalmuseum.mr.service.MarkerDetector;
import jp.nyatla.nyartoolkit.NyARException;

/**
 * NyARToolkit用のJOGL支援関数群
 */
public class NapGLUtil {
	private GL gl;
	private GLU glu;

	public NapGLUtil(GL gl) {
		this.gl = gl;
		this.glu = new GLU();
	}

	/**
	 * カメラ画像をバックグラウンドに書き出す。
	 *
	 * @param camera
	 * @param zoom
	 */
	public void drawBackGround(Camera camera, double i_zoom)
			throws NyARException {
		drawBackGround(camera.getImageData(), camera.getWidth(), camera.getHeight(),i_zoom);
	}

	/**
	 * カメラ画像をバックグラウンドに書き出す。
	 *
	 * @param camera
	 * @param zoom
	 */
	public void drawBackGround(byte[] data, int width, int height, double zoom) throws NyARException {
		IntBuffer texEnvModeSave = IntBuffer.allocate(1);
		boolean lightingSave;
		boolean depthTestSave;

		// Prepare an orthographic projection, set camera position for 2D
		// drawing, and save GL state.
		gl.glGetTexEnviv(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, texEnvModeSave);
		if (texEnvModeSave.array()[0] != GL.GL_REPLACE) {
			gl.glTexEnvi(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE,
					GL.GL_REPLACE);
		}

		lightingSave = gl.glIsEnabled(GL.GL_LIGHTING);
		if (lightingSave == true) {
			gl.glDisable(GL.GL_LIGHTING);
		}

		depthTestSave = gl.glIsEnabled(GL.GL_DEPTH_TEST);
		if (depthTestSave == true) {
			gl.glDisable(GL.GL_DEPTH_TEST);
		}

		gl.glMatrixMode(GL.GL_PROJECTION);
		gl.glPushMatrix();
		gl.glLoadIdentity();
		glu.gluOrtho2D(0.0, width, 0.0, height);

		gl.glMatrixMode(GL.GL_MODELVIEW);
		gl.glPushMatrix();
		gl.glLoadIdentity();
		arglDispImageStateful(data, width, height, zoom);

		gl.glMatrixMode(GL.GL_PROJECTION);
		gl.glPopMatrix();

		gl.glMatrixMode(GL.GL_MODELVIEW);
		gl.glPopMatrix();

		if (depthTestSave) {
			gl.glEnable(GL.GL_DEPTH_TEST);
		}
		if (lightingSave) {
			gl.glEnable(GL.GL_LIGHTING);
		}

		if (texEnvModeSave.get(0) != GL.GL_REPLACE) {
			gl.glTexEnvi(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, texEnvModeSave.get(0));
		}
		gl.glEnd();
	}

	/**
	 * arglDispImageStateful関数モドキ
	 *
	 * @param camera
	 * @param zoom
	 */
	private void arglDispImageStateful(byte[] data, int width, int height, double zoom)
			throws NyARException {
		javax.media.opengl.GL gl_ = this.gl;
		float zoomf;
		IntBuffer params = IntBuffer.allocate(4);
		zoomf = (float) zoom;
		gl_.glDisable(GL.GL_TEXTURE_2D);
		gl_.glGetIntegerv(GL.GL_VIEWPORT, params);
		gl_.glPixelZoom(
				zoomf * ((float) (params.get(2)) / (float) width),
				-zoomf * ((float) (params.get(3)) / (float) height));
		gl_.glWindowPos2f(0.0f, (float) height);
		gl_.glPixelStorei(GL.GL_UNPACK_ALIGNMENT, 1);
		ByteBuffer buf = ByteBuffer.wrap(data);
		gl_.glDrawPixels(width, height, GL.GL_BGR,
				GL.GL_UNSIGNED_BYTE, buf);
	}

	private double[] cameraProjectionMatrix = new double[16];
	private double[] modelViewMatrix = new double[16];

	public boolean preDisplay(MarkerDetector detector, NapDetectionResult result) {
		detector.getCameraProjectionMatrixOut(cameraProjectionMatrix);
		return preDisplay(result);
	}

	public boolean preDisplay(NapMarkerDetector detector, NapDetectionResult result) {
		detector.getCameraProjectionMatrixOut(cameraProjectionMatrix);
		return preDisplay(result);
	}

	private boolean preDisplay(NapDetectionResult result) {

		// Projection transformation.
		gl.glMatrixMode(GL.GL_PROJECTION);
		gl.glPushMatrix();
		gl.glLoadMatrixd(cameraProjectionMatrix, 0);

		// Viewing transformation.
		gl.glMatrixMode(GL.GL_MODELVIEW);
		gl.glPushMatrix();
		gl.glLoadIdentity();
		if (result.getTransformationMatrix(modelViewMatrix)) {
			gl.glLoadMatrixd(modelViewMatrix, 0);
			return true;
		}
		return false;
	}

	public void postDisplay() {
		gl.glMatrixMode(GL.GL_PROJECTION);
		gl.glPopMatrix();
		gl.glMatrixMode(GL.GL_MODELVIEW);
		gl.glPopMatrix();
	}
}
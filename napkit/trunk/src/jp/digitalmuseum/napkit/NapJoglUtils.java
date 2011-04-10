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

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;

public class NapJoglUtils {
	public GLU glu;
	private GL gl;
	private double[] cameraProjectionMatrix = new double[16];
	private double[] modelViewMatrix = new double[16];

	public NapJoglUtils(GL gl) {
		this.gl = gl;
		this.glu = new GLU();
	}

	/**
	 * Show background image.
	 *
	 * @param data image data
	 * @param width
	 * @param height
	 * @param zoom
	 */
	public void drawBackGround(byte[] data, int width, int height, double zoom) {

		// Prepare an orthographic projection, set camera position for 2D
		// drawing, and save GL state.

		IntBuffer texEnvModeSave = IntBuffer.allocate(1);
		gl.glGetTexEnviv(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, texEnvModeSave);
		if (texEnvModeSave.array()[0] != GL.GL_REPLACE) {
			gl.glTexEnvi(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_REPLACE);
		}

		boolean lightingSave = gl.glIsEnabled(GL.GL_LIGHTING);
		if (lightingSave == true) {
			gl.glDisable(GL.GL_LIGHTING);
		}

		boolean depthTestSave = gl.glIsEnabled(GL.GL_DEPTH_TEST);
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

		// Show background image.

		gl.glDisable(GL.GL_TEXTURE_2D);

		IntBuffer params = IntBuffer.allocate(4);
		gl.glGetIntegerv(GL.GL_VIEWPORT, params);

		float zoomf = (float) zoom;
		gl.glPixelZoom(
				zoomf * ((float) (params.get(2)) / (float) width),
				-zoomf * ((float) (params.get(3)) / (float) height));
		gl.glWindowPos2f(0.0f, (float) height);
		gl.glPixelStorei(GL.GL_UNPACK_ALIGNMENT, 1);

		ByteBuffer buf = ByteBuffer.wrap(data);
		gl.glDrawPixels(width, height, GL.GL_BGR, GL.GL_UNSIGNED_BYTE, buf);

		// Restore settings.

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

	public boolean preDisplay(NapMarkerDetector detector, NapDetectionResult result) {
		if (result.getTransformationMatrix(modelViewMatrix)) {
			NapUtils.convertMatrix4x4toGl(modelViewMatrix);
			preDisplay(detector, modelViewMatrix);
			return true;
		}
		return false;
	}

	public void preDisplay(NapMarkerDetector detector, double[] modelViewMatrix) {

		detector.getCameraProjectionMatrixOut(cameraProjectionMatrix);

		// Projection transformation.
		gl.glMatrixMode(GL.GL_PROJECTION);
		gl.glPushMatrix();
		gl.glLoadMatrixd(cameraProjectionMatrix, 0);

		// Viewing transformation.
		gl.glMatrixMode(GL.GL_MODELVIEW);
		gl.glPushMatrix();
		gl.glLoadIdentity();
		gl.glLoadMatrixd(modelViewMatrix, 0);
	}

	public void postDisplay() {
		gl.glMatrixMode(GL.GL_PROJECTION);
		gl.glPopMatrix();
		gl.glMatrixMode(GL.GL_MODELVIEW);
		gl.glPopMatrix();
	}
}
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
package jp.digitalmuseum.jogl;

import java.nio.ByteBuffer;

import javax.media.opengl.GL;

/**
 * Base class for holding a three-dimensional model object for JOGL.
 */
public class JoglModel {
	protected GL gl;
	protected JoglTextureManager textureManager;
	protected boolean hasOwnTextureManager;
	protected boolean isVboEnabled;
	protected int frontFace;
	protected JoglCoordinates coordinates;
	protected Point minPos;
	protected Point maxPos;
	protected JoglObject[] joglObjects;

	/**
	 * Default constructor.
	 *
	 * @param gl
	 *            OpenGL object.
	 * @param textureManager
	 *            JoglTextureManager for managing textures. Create new one inside the instance if this parameter is null.
	 * @param coordinates
	 *            JoglCoordinates for managing coordinates.
	 * @param isVboEnabled
	 *            Whether to use VBO or not.
	 */
	protected JoglModel(GL gl, JoglTextureManager textureManager, JoglCoordinates coordinates, boolean isVboEnabled) {

		this.gl = gl;

		if (textureManager == null) {
			this.textureManager = new JoglTextureManager(this.gl);
			hasOwnTextureManager = true;
		} else {
			this.textureManager = textureManager;
			hasOwnTextureManager = false;
		}

		if(coordinates == null) {
			this.coordinates = new JoglCoordinates_ARToolKit();
		} else {
			this.coordinates = coordinates;
		}

		if (isVboEnabled) {
			this.isVboEnabled = isExtensionSupported(gl, "GL_ARB_vertex_buffer_object");
		}

		frontFace = GL.GL_CCW;
		minPos = null;
		maxPos = null;
		joglObjects = null;
	}

	/**
	 * Set visibility of an object.
	 *
	 * @param objectName
	 *            Name of an object.
	 * @param isVisible
	 *            Visibility.
	 */
	public void setObjectVisible(String objectName, boolean isVisible) {
		if (joglObjects == null) {
			return;
		}
		for (int o = 0; o < joglObjects.length; o++) {
			if (objectName.equals(joglObjects[o].name)) {
				joglObjects[o].isVisible = isVisible;
				break;
			}
		}
		updateMinMax();
	}

	/**
	 * Set visibility of material.
	 *
	 * @param materialName
	 *            Name of material.
	 * @param isVisible
	 *            Visibility.
	 */
	public void setMaterialVisible(String materialName, boolean isVisible) {
		if (joglObjects == null) {
			return;
		}
		for (int o = 0; o < joglObjects.length; o++) {
			for (int m = 0; m < joglObjects[o].materials.length; m++) {
				if (materialName.equals(joglObjects[o].materials[m].name)) {
					joglObjects[o].materials[m].isVisible = isVisible;
					break;
				}
			}
		}
		updateMinMax();
	}

	/**
	 * Set visibility of material of an object.
	 *
	 * @param objectName
	 *            Name of an object.
	 * @param materialName
	 *            Name of material.
	 * @param isVisible
	 *            Visibility.
	 */
	public void setMaterialVisible(String objectName, String materialName,
			boolean isVisible) {
		if (joglObjects == null) {
			return;
		}
		for (int o = 0; o < joglObjects.length; o++) {
			if (!objectName.equals(joglObjects[o].name)) {
				continue;
			}
			for (int m = 0; m < joglObjects[o].materials.length; m++) {
				if (materialName.equals(joglObjects[o].materials[m].name)) {
					joglObjects[o].materials[m].isVisible = isVisible;
					break;
				}
			}
		}
		updateMinMax();
	}

	private void updateMinMax() {
		if (joglObjects == null) {
			return;
		}
		minPos = null;
		maxPos = null;
		for (int o = 0; o < joglObjects.length; o++) {
			if (joglObjects[o].isVisible) {
				if (minPos == null) {
					minPos = new Point(joglObjects[o].minPos);
				} else {
					minPos.updateMinimum(joglObjects[o].minPos);
				}
				if (maxPos == null) {
					maxPos = new Point(joglObjects[o].maxPos);
				} else {
					maxPos.updateMinimum(joglObjects[o].maxPos);
				}
			}
		}
	}

	/**
	 * Draw this object.
	 */
	public void draw() {
		draw(1.0f) ;
	}

	/**
	 * Draw this object with the specified transparency.
	 *
	 * @param alpha 0 (completely transparent) - 1.0 (not transparent)
	 */
	public void draw(float alpha) {
		if (joglObjects == null) {
			return;
		}

		// Backup settings.
		boolean isBlendEnabled = gl.glIsEnabled(GL.GL_BLEND);
		int[] intBlendFunc = new int[2];
		gl.glGetIntegerv(GL.GL_BLEND_SRC, intBlendFunc, 0);
		gl.glGetIntegerv(GL.GL_BLEND_DST, intBlendFunc, 1);
		if (!isBlendEnabled) {
			gl.glEnable(GL.GL_BLEND);
		}
		gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);

		int[] intFrontFace = new int[1];
		gl.glGetIntegerv(GL.GL_FRONT_FACE, intFrontFace, 0);
		gl.glFrontFace(frontFace);

		boolean isTexture2dEnabled = gl.glIsEnabled(GL.GL_TEXTURE_2D);

		float[] color = new float[4];
		for (JoglObject joglObject : joglObjects) {
			if (joglObject == null ||
					!joglObject.isVisible) {
				continue;
			}
			for (int m = 0; m < joglObject.materials.length; m++) {
				JoglMaterial joglMaterial = joglObject.materials[m];
				if (joglMaterial == null ||
						!joglMaterial.isVisible) {
					continue;
				}

				if (joglMaterial.textureID != 0) {
					gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP);
					gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP);
					if (!isTexture2dEnabled) {
						gl.glEnable(GL.GL_TEXTURE_2D);
					}
				}

				if (joglMaterial.isSmoothShading) {
					gl.glShadeModel(GL.GL_SMOOTH);
				} else {
					gl.glShadeModel(GL.GL_FLAT);
				}

				gl.glColor4f(joglMaterial.color[0], joglMaterial.color[1], joglMaterial.color[2], joglMaterial.color[3]);
				if (joglMaterial.dif != null) {
					System.arraycopy(joglMaterial.dif, 0, color, 0, joglMaterial.dif.length);
					color[3] *= alpha;
					gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_DIFFUSE, color, 0);
				}
				if (joglMaterial.amb != null) {
					gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_AMBIENT, joglMaterial.amb, 0);
				}
				if (joglMaterial.spc != null) {
					System.arraycopy(joglMaterial.spc, 0, color, 0, joglMaterial.spc.length);
					color[3] *= alpha;
					gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_SPECULAR, color, 0);
				}
				if (joglMaterial.emi != null) {
					gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_EMISSION, joglMaterial.emi, 0);
				}
				if (joglMaterial.power != null) {
					gl.glMaterialf(GL.GL_FRONT_AND_BACK, GL.GL_SHININESS, joglMaterial.power[0]);
				}

				if (joglMaterial.textureID != 0) {
					gl.glBindTexture(GL.GL_TEXTURE_2D, joglMaterial.textureID);
				}

				if (isVboEnabled) {
					gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, joglObject.vboIds[m]);
					gl.glInterleavedArrays(joglMaterial.interleaveFormat, 0, 0);
				} else {
					joglMaterial.interleaved.position(0);
					gl.glInterleavedArrays(joglMaterial.interleaveFormat, 0, joglMaterial.interleaved);
				}
				gl.glDrawArrays(GL.GL_TRIANGLES, 0, joglMaterial.numVertices);

				if (joglMaterial.textureID != 0) {
					gl.glBindTexture(GL.GL_TEXTURE_2D, 0);
				}
				if (isVboEnabled) {
					gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, 0);
				}

				// Restore settings (1)
				if (joglMaterial.textureID != 0 && !isTexture2dEnabled) {
					gl.glDisable(GL.GL_TEXTURE_2D);
				} else if (joglMaterial.textureID == 0 && isTexture2dEnabled) {
					gl.glEnable(GL.GL_TEXTURE_2D);
				}
			}
		}

		// Restore settings (2)
		if (!isBlendEnabled) {
			gl.glDisable(GL.GL_BLEND);
		}
		gl.glBlendFunc(intBlendFunc[0], intBlendFunc[1]);
		gl.glFrontFace(intFrontFace[0]);
	}

	public Point getMaxPos() {
		Point p = new Point();
		getMaxPosOut(p);
		return p;
	}

	public void getMaxPosOut(Point p) {
		p.set(maxPos.data);
	}

	public Point getMinPos() {
		Point p = new Point();
		getMinPosOut(p);
		return p;
	}

	public void getMinPosOut(Point p) {
		p.set(minPos.data);
	}

	/**
	 * Free used memory spaces.
	 */
	public void clear() {
		if (joglObjects == null) {
			return;
		}
		for (int o = 0; o < joglObjects.length; o++) {
			if (joglObjects[o].vboIds != null) {
				gl.glDeleteBuffersARB(
						joglObjects[o].vboIds.length, joglObjects[o].vboIds, 0);
			}
		}
		joglObjects = null;
		if (hasOwnTextureManager) {
			textureManager.clear();
			textureManager = null;
		}
	}

	/**
	 * Calculate normal vector of a triangle face.
	 *
	 * @param vertices
	 *            Array of the coordinates of the vertices
	 * @param indexVertexA
	 *            Index of vertex A in the array
	 * @param indexVertexB
	 *            Index of vertex B in the array
	 * @param indexVertexC
	 *            Index of vertex C in the array
	 * @return Normal vector
	 */
	protected Point calcNormal(Point[] vertices, int indexVertexA, int indexVertexB, int indexVertexC) {

		// Vector ab
		Point vectorAB = vertices[indexVertexB].sub(vertices[indexVertexA]);

		// Vector bc
		Point vectorBC = vertices[indexVertexC].sub(vertices[indexVertexB]);

		// Calculate normal vector.
		Point ret = new Point(
				vectorAB.getY() * vectorBC.getZ() - vectorAB.getZ() * vectorBC.getY(),
				vectorAB.getZ() * vectorBC.getX() - vectorAB.getX() * vectorBC.getZ(),
				vectorAB.getX() * vectorBC.getY() - vectorAB.getY() * vectorBC.getX());
		ret.normalize();
		return ret;
	}

	protected static void append(StringBuilder sb, int[] array) {
		if (array == null) {
			sb.append("null");
			return;
		}
		for (int i = 0; i < array.length; i ++) {
			sb.append(array[i]);
			if (i != array.length - 1) sb.append(" ");
		}
	}

	protected static void append(StringBuilder sb, long[] array) {
		if (array == null) {
			sb.append("null");
			return;
		}
		for (int i = 0; i < array.length; i ++) {
			sb.append(array[i]);
			if (i != array.length - 1) sb.append(" ");
		}
	}

	protected static void append(StringBuilder sb, float[] array) {
		if (array == null) {
			sb.append("null");
			return;
		}
		for (int i = 0; i < array.length; i ++) {
			sb.append(array[i]);
			if (i != array.length - 1) sb.append(" ");
		}
	}

	/**
	 * Returns whether current OpenGL implementation supports the specified extension.
	 * @param gl OpenGL object
	 * @param targetExtension Name of OpenGL extension
	 * @return Whether current OpenGL implementation supports the specified extension.
	 */
	public static boolean isExtensionSupported(GL gl, String targetExtension) {
		String extensions = gl.glGetString(GL.GL_EXTENSIONS);
		int p;
		while ((p = extensions.indexOf(targetExtension)) != -1) {
			extensions = extensions.substring(p);
			String[] s = extensions.split(" ", 2);
			if (s[0].trim().equals(targetExtension)) {
				return true;
			}
			extensions = s[1];
		}
		return false;
	}

	/**
	 * Class which represents a JOGL material.
	 */
	public static class JoglMaterial {
		String name;
		boolean isVisible = true;
		float[] color = null;
		float[] dif = null;
		float[] amb = null;
		float[] emi = null;
		float[] spc = null;
		float[] power = null;
		boolean isSmoothShading = true;
		int numVertices;
		int textureID;
		int interleaveFormat;
		ByteBuffer interleaved = null;
	}

	/**
	 * Class which represents a JOGL object.
	 */
	public static class JoglObject {
		String name = null;
		boolean isVisible = true;
		JoglMaterial[] materials = null;
		int[] vboIds = null;
		Point minPos = null;
		Point maxPos = null;
	}

	/**
	 * Class for holding a float array.
	 */
	public static class FloatArray {
		protected float[] data;

		public FloatArray(int arrayLength) {
			data = new float[arrayLength];
		}

		public void setData(float[] data) {
			for (int i = 0; i < this.data.length && i < data.length; i ++) {
				this.data[i] = data[i];
			}
		}

		public void set(float... data) {
			setData(data);
		}

		public float[] getData() {
			return data.clone();
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("[");
			append(sb, data);
			sb.append("]");
			return sb.toString();
		}
	}

	/**
	 * Class for holding UV texture mapping parameters.
	 */
	public static class UV extends FloatArray {
		public UV() {
			super(2);
		}
		public UV(UV uv) {
			this();
			setData(uv.data);
		}
		public UV(float... data) {
			this();
			setData(data);
		}

		public void setU(float u) { data[0] = u; }
		public void setV(float v) { data[1] = v; }
		public float getU() { return data[0]; }
		public float getV() { return data[1]; }
	}

	/**
	 * Class for holding color parameter.
	 */
	public static class Color extends FloatArray {
		public Color() {
			super(4);
		}
		public Color(Color color) {
			this();
			setData(color.data);
		}
		public Color(float... data) {
			this();
			setData(data);
		}

		public void setR(float r) { data[0] = r; }
		public void setG(float g) { data[1] = g; }
		public void setB(float b) { data[2] = b; }
		public void setA(float a) { data[3] = a; }
		public float getR() { return data[0]; }
		public float getG() { return data[1]; }
		public float getB() { return data[2]; }
		public float getA() { return data[3]; }
	}

	/**
	 * Class for holding three-dimensional coordinates of a point.
	 */
	public static class Point extends FloatArray {
		public Point() {
			super(3);
		}
		public Point(Point point) {
			this();
			setData(point.data);
		}
		public Point(float... data) {
			this();
			setData(data);
		}

		public void setX(float x) { data[0] = x; }
		public void setY(float y) { data[1] = y; }
		public void setZ(float z) { data[2] = z; }
		public float getX() { return data[0]; }
		public float getY() { return data[1]; }
		public float getZ() { return data[2]; }

		public Point add(Point point) {
			Point p = new Point();
			addOut(point, p);
			return p;
		}

		public void addOut(Point point, Point p) {
			for (int i = 0; i < data.length; i ++) {
				p.data[i] = data[i] + point.data[i];
			}
		}

		public Point sub(Point point) {
			Point p = new Point();
			subOut(point, p);
			return p;
		}

		public void subOut(Point point, Point p) {
			for (int i = 0; i < data.length; i ++) {
				p.data[i] = data[i] - point.data[i];
			}
		}

		public Point scale(float scale) {
			Point p = new Point();
			scaleOut(scale, p);
			return p;
		}

		public Point scaleOut(float scale, Point p) {
			for (int i = 0; i < data.length; i ++) {
				p.data[i] = data[i] * scale;
			}
			return this;
		}

		public void normalize() {
			float normSq = 0;
			for (int i = 0; i < data.length; i ++) {
				normSq += data[i]*data[i];
			}
			float norm = (float) Math.sqrt(normSq);
			for (int i = 0; i < data.length; i ++) {
				data[i] = data[i] / norm;
			}
		}

		public void updateMinimum(Point point) {
			for (int i = 0; i < data.length; i ++) {
				if (data[i] > point.data[i]) {
					data[i] = point.data[i];
				}
			}
		}

		public void updateMaximum(Point point) {
			for (int i = 0; i < data.length; i ++) {
				if (data[i] < point.data[i]) {
					data[i] = point.data[i];
				}
			}
		}
	}

	/**
	 * Class for holding three-dimensional coordinates of a light source.
	 */
	public static class LightSourcePoint extends Point {
		public LightSourcePoint() {
			super();
			data = new float[4];
		}
		public LightSourcePoint(LightSourcePoint point) {
			this();
			setData(point.data);
		}
		public LightSourcePoint(float... data) {
			this();
			setData(data);
		}
	}
}

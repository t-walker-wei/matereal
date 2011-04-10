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

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import javax.media.opengl.GL;

import jp.digitalmuseum.utils.MixedDataReader;

/**
 * Metasequoia 3D model file (*.mqo) loader for Java OpenGL (JOGL.)
 */
public class JoglModelMqo extends JoglModel {
	private URL url;

	private static final Field[] materialFields = MqoMaterial.class.getDeclaredFields();
	private static final Field[] objectFields = MqoObject.class.getDeclaredFields();
	private static final Field[] faceFields = MqoFace.class.getDeclaredFields();

	private static URL createURL(String url) {
		try {
			return new URL(url);
		} catch (MalformedURLException e) {
			try {
				return new URL("file:"+url);
			} catch (MalformedURLException e1) {
				return null;
			}
		}
	}

	public JoglModelMqo(GL gl, String url) {
		this(gl, createURL(url));
	}

	public JoglModelMqo(GL gl, String url, float scale) {
		this(gl, createURL(url), scale);
	}

	public JoglModelMqo(GL gl, String url, JoglTextureManager textureManager) {
		this(gl, createURL(url), textureManager);
	}

	public JoglModelMqo(GL gl, String url, JoglTextureManager textureManager, float scale) {
		this(gl, createURL(url), textureManager, scale);
	}

	public JoglModelMqo(GL gl, String url, JoglTextureManager textureManager, float scale, JoglCoordinates coordinates) {
		this(gl, createURL(url), textureManager, scale, coordinates);
	}

	public JoglModelMqo(GL gl, URL url) {
		this(gl, url, null);
	}

	public JoglModelMqo(GL gl, URL url, float scale) {
		this(gl, url, null, scale);
	}

	public JoglModelMqo(GL gl, URL url, JoglTextureManager textureManager) {
		this(gl, url, textureManager, 1.0f);
	}

	public JoglModelMqo(GL gl, URL url, JoglTextureManager textureManager, float scale) {
		this(gl, url, textureManager, scale, null);
	}

	public JoglModelMqo(GL gl, URL url, JoglTextureManager textureManager, float scale, JoglCoordinates coordinates) {
		this(gl, url, textureManager, scale, coordinates, true);
	}

	/**
	 * Default constructor.
	 *
	 * @param gl
	 *            OpenGL object.
	 * @param url
	 *            URL object which locates a MQO file to load.
	 * @param textureManager
	 *            JoglTextureManager for managing textures. Create new one inside the instance if this parameter is null.
	 * @param scale
	 *            Scale for the model.
	 * @param coordinates
	 *            JoglCoordinates for managing coordinates.
	 * @param isVboEnabled
	 *            Whether to use VBO or not.
	 */
	public JoglModelMqo(GL gl, URL url, JoglTextureManager textureManager, float scale, JoglCoordinates coordinates, boolean isVboEnabled) {
		super(gl, textureManager, coordinates, isVboEnabled);

		this.url = url;

		frontFace = GL.GL_CW;

		// Load MQO file.
		List<MqoMaterial> materials = new ArrayList<MqoMaterial>();
		List<MqoObject> objects = new ArrayList<MqoObject>();
		try {
			MixedDataReader mdr = new MixedDataReader(url.openStream());
			String line;
			while ((line = mdr.readLine()) != null) {
				line = line.trim();
				if (line.length() > 0 &&
						line.charAt(line.length() - 1) == '{') {

					// Get chunk type.
					String chunkType = line.substring(0, line.indexOf(' ')).toLowerCase();

					// Get chunk option.
					int chunkOptionStart = chunkType.length() + 1;
					int chunkOptionEnd = line.length() - 2;
					if (line.charAt(chunkOptionStart) == '"') {
						chunkOptionEnd = skipQuotedString(line, chunkOptionStart);
						chunkOptionStart ++;
					}
					String chunkOption = null;
					if (chunkOptionStart < chunkOptionEnd) {
						chunkOption = line.substring(chunkOptionStart, chunkOptionEnd);
					}

					// Parse chunk contents.
					if (chunkType.equals("material")) {
						parseMaterialChunk(mdr, materials);
					} else if (chunkType.equals("object")) {
						parseObjectChunk(mdr, chunkOption, scale, objects);
					}
				}
			}
			mdr.close();
		} catch (IOException e) {
			// Do nothing.
		}

		/*
		System.out.println("Materials:");
		for (MqoMaterial m : materials) {
			System.out.print("  ");
			System.out.println(m);
		}
		System.out.println("Objects:");
		for (MqoObject o : objects) {
			System.out.print("  ");
			System.out.println(o);
		}
		*/

		// Convert MQO objects to GL objects.
		List<JoglObject> glObjects = new ArrayList<JoglObject>();
		for (MqoObject object : objects) {
			JoglObject glObject = makeGLObject(materials, object);
			if (glObject == null) {
				continue;
			}
			glObjects.add(glObject);
			if (glObject.isVisible) {
				if (minPos == null) {
					if (glObject.minPos != null) {
						minPos = new Point(glObject.minPos);
					}
				} else {
					minPos.updateMinimum(glObject.minPos);
				}
				if (maxPos == null) {
					if (glObject.maxPos != null) {
						maxPos = new Point(glObject.maxPos);
					}
				} else {
					maxPos.updateMaximum(glObject.maxPos);
				}
			}
		}
		joglObjects = glObjects.toArray(new JoglObject[0]);
	}

	private JoglObject makeGLObject(List<MqoMaterial> materials, MqoObject object) {
		JoglObject glObject = null;
		List<JoglMaterial> glMaterials = new ArrayList<JoglMaterial>();
		Point[] vertexNormals = calcVertexNormals(object);
		for (int i = 0; i < materials.size(); i++) {
			JoglMaterial glMaterial = makeGLMaterial(materials.get(i), i, object, vertexNormals);
			if (glMaterial != null) {
				glMaterials.add(glMaterial);
			}
		}
		if (glMaterials.size() == 0) {
			return null;
		}
		glObject = new JoglObject();
		glObject.name = object.name;
		glObject.materials = glMaterials.toArray(new JoglMaterial[0]);
		glObject.isVisible = (object.visible != 0);
		glObject.minPos = new Point(object.minPos);
		glObject.maxPos = new Point(object.maxPos);
		if (!isVboEnabled) {
			return glObject;
		}
		glObject.vboIds = new int[glObject.materials.length];
		gl.glGenBuffersARB(glObject.materials.length, glObject.vboIds, 0);
		for (int m = 0; m < glObject.materials.length; m++) {
			glObject.materials[m].interleaved.position(0);
			gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, glObject.vboIds[m]);
			gl.glBufferDataARB(GL.GL_ARRAY_BUFFER_ARB,
					glObject.materials[m].interleaved.capacity(), glObject.materials[m].interleaved,
					GL.GL_STATIC_DRAW_ARB);
		}
		return glObject;
	}

	private JoglMaterial makeGLMaterial(MqoMaterial material, int materialIndex, MqoObject object, Point[] vertexNormals) {
		JoglMaterial joglMaterial = new JoglMaterial();
		List<Point> vertices = new ArrayList<Point>();
		List<Point> normalVectors = new ArrayList<Point>();
		List<UV> uvs = new ArrayList<UV>();
		List<Color> colors = new ArrayList<Color>();
		boolean uvValid = false;
		boolean colorValid = false;
		for (int f = 0; f < object.face.length; f++) {
			MqoFace face = object.face[f];
			if (face.M != materialIndex) {
				continue;
			}

			Point normalVector = calcNormal(object.vertex,
					face.V[0],
					face.V[1],
					face.V[2]);
			for (int v = 0; v < 3; v++) {

				// Vertex and its normal vector.
				int vertexId = face.V[v];
				vertices.add(object.vertex[vertexId]);
				float s = (float) Math.acos(
						normalVector.getX() * vertexNormals[vertexId].getX() +
						normalVector.getY() * vertexNormals[vertexId].getY() +
						normalVector.getZ() * vertexNormals[vertexId].getZ());
				if (object.facet < s) {
					normalVectors.add(normalVector);
				} else {
					normalVectors.add(vertexNormals[vertexId]);
				}

				// UV
				UV uv = new UV();
				if (face.UV == null) {
					uv.set(0, 0);
				} else {
					uv.set(face.UV[v * 2 + 0], face.UV[v * 2 + 1]);
					uvValid = true;
				}
				uvs.add(uv);

				// Color
				Color color = new Color();
				if (face.COL == null) {
					if (material.col == null) {
						color.set(1.0f, 1.0f, 1.0f, 1.0f);
					} else {
						color.setData(material.col);
					}
				} else {
					long col = face.COL[v];
					color.set((float) (col & 0xff) / 255f,
							(float) (col >> 8 & 0xff) / 255f,
							(float) (col >> 16 & 0xff) / 255f,
							(float) (col >> 24 & 0xff) / 255f);
					colorValid = true;
				}
				colors.add(color);
			}
		}

		joglMaterial.name = material.name;
		joglMaterial.textureID = textureManager.getGLTexture(url, material.tex, material.aplane, false);

		uvValid = uvValid && (joglMaterial.textureID != 0);

		if (vertices.size() == 0) {
			return null;
		}

		Point[] verticesArray = vertices.toArray(new Point[0]);

		joglMaterial.numVertices = verticesArray.length;
		joglMaterial.interleaveFormat = GL.GL_N3F_V3F;
		if (uvValid && colorValid) {
			joglMaterial.interleaveFormat = GL.GL_T2F_C4F_N3F_V3F;
		} else if (uvValid) {
			joglMaterial.interleaveFormat = GL.GL_T2F_N3F_V3F;
		} else if (colorValid) {
			joglMaterial.interleaveFormat = GL.GL_C4F_N3F_V3F;
		}
		joglMaterial.interleaved = ByteBuffer.allocateDirect(Float.SIZE * (
				// 頂点の数 * 3 (x, y, z)
				joglMaterial.numVertices * 3 +
				// 頂点法線の数 (＝頂点の数)
				joglMaterial.numVertices * 3 +
				// UVの数 (頂点の数 * 2)
				((uvValid) ? joglMaterial.numVertices * 2 : 0) +
				// 頂点色の数 (頂点の数 x 4)
				((colorValid) ? joglMaterial.numVertices * 4 : 0)));
		joglMaterial.interleaved.order(ByteOrder.nativeOrder());
		joglMaterial.interleaved.position(0);

		UV[] uvsArray = uvs.toArray(new UV[0]);
		Color[] colorsArray = colors.toArray(new Color[0]);
		Point[] normalVectorsArray = normalVectors.toArray(new Point[0]);
		for (int v = 0; v < joglMaterial.numVertices; v++) {
			if (uvValid) {
				joglMaterial.interleaved.putFloat(uvsArray[v].getU());
				joglMaterial.interleaved.putFloat(uvsArray[v].getV());
			}
			if (colorValid) {
				joglMaterial.interleaved.putFloat(colorsArray[v].getR());
				joglMaterial.interleaved.putFloat(colorsArray[v].getG());
				joglMaterial.interleaved.putFloat(colorsArray[v].getB());
				joglMaterial.interleaved.putFloat(colorsArray[v].getA());
			}
			joglMaterial.interleaved.putFloat(normalVectorsArray[v].getX());
			joglMaterial.interleaved.putFloat(normalVectorsArray[v].getY());
			joglMaterial.interleaved.putFloat(normalVectorsArray[v].getZ());
			joglMaterial.interleaved.putFloat(verticesArray[v].getX());
			joglMaterial.interleaved.putFloat(verticesArray[v].getY());
			joglMaterial.interleaved.putFloat(verticesArray[v].getZ());
		}

		if (material.col != null) {
			joglMaterial.color = new float[material.col.length];
			for (int c = 0; c < material.col.length; c ++) {
				joglMaterial.color[c] = material.col[c];
			}
			if (material.dif >= 0) {
				joglMaterial.dif = new float[material.col.length];
				for (int c = 0; c < material.col.length; c++) {
					joglMaterial.dif[c] = material.dif * material.col[c];
				}
				joglMaterial.dif[3] = material.col[3];
			}
			if (material.amb >= 0) {
				joglMaterial.amb = new float[material.col.length];
				for (int c = 0; c < material.col.length; c++) {
					joglMaterial.amb[c] = material.amb * material.col[c];
				}
				joglMaterial.amb[3] = material.col[3];
			}
			if (material.emi >= 0) {
				joglMaterial.emi = new float[material.col.length];
				for (int c = 0; c < material.col.length; c++) {
					joglMaterial.emi[c] = material.emi * material.col[c];
				}
				joglMaterial.emi[3] = material.col[3];
			}
			if (material.spc >= 0) {
				joglMaterial.spc = new float[material.col.length];
				for (int c = 0; c < material.col.length; c++) {
					joglMaterial.spc[c] = material.spc * material.col[c];
				}
				joglMaterial.spc[3] = material.col[3];
			}
		}
		if (material.power >= 0) {
			joglMaterial.power = new float[1];
			joglMaterial.power[0] = material.power;
		}
		joglMaterial.isSmoothShading = object.shading != 0;
		return joglMaterial;
	}

	/**
	 * Calculate vertex normals for the specifiedMQO object.
	 *
	 * @param mqoObject
	 *            Loaded MQO object.
	 * @return Normal vectors for the face of the object.
	 */
	private Point[] calcVertexNormals(MqoObject mqoObject) {
		Point[] normalVectors = new Point[mqoObject.vertex.length];

		// To calculate vertex normal, sum up surface normals around the vertex.
		for (int f = 0; f < mqoObject.face.length; f++) {
			Point surfaceNormal = calcNormal(
					mqoObject.vertex,
					mqoObject.face[f].V[0],
					mqoObject.face[f].V[1],
					mqoObject.face[f].V[2]);
			if (surfaceNormal != null) {
				for (int i = 0; i < 3; i++) {
					if (normalVectors[mqoObject.face[f].V[i]] == null) {
						normalVectors[mqoObject.face[f].V[i]] = new Point(0, 0, 0);
					}
					normalVectors[mqoObject.face[f].V[i]].add(surfaceNormal);
				}
			}
		}

		// Normalize the vectors.
		for (int v = 0; v < normalVectors.length; v++) {
			if (normalVectors[v] != null) {
				normalVectors[v].normalize();
			}
		}
		return normalVectors;
	}

	private void parseMaterialChunk(MixedDataReader mdr, List<MqoMaterial> materials) throws IOException {
		String line;
		while ((line = mdr.readLine()) != null) {
			line = line.trim();
			if (line.equals("}")) {
				break;
			}
			MqoMaterial material = new MqoMaterial();

			// Format of the line:
			// %s shader(%d) vcol(%d) col(%.3f %.3f %.3f %.3f) dif(%.3f) amb(%.3f) emi(%.3f) spc(%.3f)
			// power(%.2f) tex(%s) alpha(%s) bump(%s)
			// proj_type(%d) proj_pos(%.3f %.3f %.3f) proj_scale(%.3f %.3f %.3f) proj_angle(%.3f %.3f %.3f)

			// Get name of the material.
			material.name = line.substring(0, line.indexOf(' '));
			int materialNameLength = material.name.length();
			if (material.name.charAt(0) == '"') {
				material.name = line.substring(1, skipQuotedString(line, 0));
			}

			// Get the rest.
			parseFields(line, materialNameLength + 1, material, materialFields);

			materials.add(material);
			// System.out.println("Material: " + material.toString());
		}
	}

	private void parseObjectChunk(MixedDataReader mdr, String objectName, float scale, List<MqoObject> objects) throws IOException {
		MqoObject object = new MqoObject();
		object.name = objectName;
		String line;
		while ((line = mdr.readLine()) != null) {
			line = line.trim();
			if (line.equals("}")) {
				break;
			}

			// Get key (depth, folding, scale, ...)
			String key = line.substring(0, line.indexOf(' ')).toLowerCase();
			// System.out.println("Parsing object chunk: "+key);

			if (key.equals("vertex")) {
				object.vertex = parseObjectVertices(mdr, scale, object);
			} else if (key.equals("bvertex")) {
				object.vertex = parseObjectBinaryVertices(mdr, scale, object);
			} else if (key.equals("face")) {
				object.face = parseObjectFaces(mdr);
			} else {
				String value = line.substring(key.length() + 1);
				for (Field field : objectFields) {
					if (field.getName().equals(key)) {
						try {
							setField(object, field, value);
						} catch (IllegalAccessException e) {
							// Never happens.
						}
					}
				}
			}
		}
		if (object.face != null) {
			objects.add(object);
			// System.out.println("Object: " + object.name);
		}
	}

	private Point[] parseObjectVertices(MixedDataReader mdr, float scale, MqoObject object) throws IOException {
		List<Point> points = new ArrayList<Point>();
		String line;
		while ((line = mdr.readLine()) != null) {
			line = line.trim();
			if (line.equals("}")) {
				break;
			}
			String[] xyz = line.split(" ");

			Point point = coordinates.convert(new Point(
					Float.valueOf(xyz[0])*scale,
					Float.valueOf(xyz[1])*scale,
					Float.valueOf(xyz[2])*scale));
			points.add(point);

			object.minPos.updateMinimum(point);
			object.maxPos.updateMaximum(point);
		}
		return points.toArray(new Point[0]);
	}

	private Point[] parseObjectBinaryVertices(MixedDataReader mdr, float scale, MqoObject object) throws IOException {
		List<Point> points = new ArrayList<Point>();
		String line;
		while ((line = mdr.readLine()) != null) {
			line = line.trim();
			if (line.equals("}")) {
				break;
			}

			// Get key (Vector, weit or color.)
			String key = line.substring(0, line.indexOf(' ')).toLowerCase();
			if (key.equals("vector")) {
				int numVerticesStringStart = key.length() + 1;
				int numVerticesStringEnd = line.indexOf(' ', numVerticesStringStart);
				String numVerticesString = line.substring(numVerticesStringStart, numVerticesStringEnd);
				int numVertices = Integer.valueOf(numVerticesString);
				/*
				int dataSize = Integer.valueOf(
						line.substring(line.indexOf('[') + 1, line.indexOf(']')));
				if (numVertices * 4 != dataSize) {
					// Each vertex data consumes 4 bytes (32bit float.)
				}
				*/
				byte[] buf = new byte[12];
				float[] xyz = new float[3];
				for (int i = 0; i < numVertices; i ++) {
					if (mdr.read(buf) != 12) {
						break;
					}
					ByteBuffer bb = ByteBuffer.wrap(buf);
					bb.order(ByteOrder.LITTLE_ENDIAN);
					bb.asFloatBuffer().get(xyz);

					Point point = new Point(xyz).scale(scale);
					point = coordinates.convert(point);
					points.add(point);

					object.minPos.updateMinimum(point);
					object.maxPos.updateMaximum(point);
				}
			} else {
				// Skip weit or color chunk.
				while ((line = mdr.readLine()) != null) {
					line = line.trim();
					if (line.equals("}")) {
						break;
					}
				}
			}
		}
		return points.toArray(new Point[0]);
	}

	private MqoFace[] parseObjectFaces(MixedDataReader mdr) {
		List<MqoFace> faces = new ArrayList<MqoFace>();
		String line;
		try {
			while ((line = mdr.readLine()) != null) {
				line = line.trim();
				if (line.equals("}")) {
					break;
				}

				int numVerticesEnd = line.indexOf(' ');
				int numVertices;
				try {
					numVertices = Integer.valueOf(line.substring(0, numVerticesEnd));
				} catch (NumberFormatException e) {
					// Skip this line.
					continue;
				}

				MqoFace face = new MqoFace();
				parseFields(line, numVerticesEnd + 1, face, faceFields);

				if (face.V == null || numVertices != face.V.length ||
						face.UV == null || numVertices*2 != face.UV.length) {
					// System.err.println("Parse failed: "+line+" (V:"+face.V+", UV:"+face.UV+")");
					continue;
				}

				if (numVertices == 3) {
					faces.add(face);
				} else if (numVertices == 4) {
					MqoFace[] dividedFaces = face.divide();
					if (dividedFaces != null) {
						for (MqoFace f : dividedFaces) {
							faces.add(f);
						}
					}
				}
			}
		} catch (IOException e) {
			// Do nothing.
		}
		if (faces.size() == 0) {
			return null;
		}
		return faces.toArray(new MqoFace[0]);
	}

	/**
	 * Parse "key1(value1) key2(value2) ..."-style string.
	 *
	 * @param line Line to parse.
	 * @param i Character index of the line to start parsing.
	 * @param object Object to set field values.
	 * @param fields Field array representing fields of the given object.
	 */
	private void parseFields(String line, int i, Object object, Field[] fields) {
		int j;
		while ((j = line.indexOf('(', i)) > 0) {

			// Get key.
			String key = line.substring(i, j).trim();
			j ++;

			// Skip double quotations.
			String value;
			if (line.charAt(j) == '"') {
				int k = skipQuotedString(line, j, true, ")");
				value = line.substring(j + 1, k);
				i = k + 3; // Skip '") '.
			} else {
				i = line.indexOf(')', j);
				if (i < 0) {
					value = line.substring(j);
					i = line.length() - 1;
				} else {
					value = line.substring(j, i);
					i += 2; // Skip ') '.
				}
			}

			for (Field field : fields) {
				if (field.getName().equals(key)) {
					try {
						setField(object, field, value);
					} catch (IllegalAccessException e) {
						// Never happens.
					}
				}
			}
		}
	}

	/**
	 * Set fields of the given object to the given values.
	 *
	 * @param obj An object to set value.
	 * @param field Field to set value.
	 * @param value Value to be set.
	 *
	 * @throws IllegalAccessException
	 */
	private void setField(Object obj, Field field, String value) throws IllegalAccessException {
		final Class<?> type = field.getType();
		if (type == int.class) {
			field.set(obj, Integer.parseInt(value));
		} else if (type == long.class) {
			field.set(obj, Long.valueOf(value));
		} else if (type == String.class) {
			field.set(obj, value);
		} else if (type == float.class) {
			field.set(obj, Float.parseFloat(value));
		} else if (type == int[].class) {
			String[] values = value.split(" ");
			int[] integers = new int[values.length];
			for (int i = 0; i < values.length; i++) {
				integers[i] = Integer.parseInt(values[i]);
			}
			field.set(obj, integers);
		} else if (type == long[].class) {
			String[] values = value.split(" ");
			long[] longs = new long[values.length];
			for (int i = 0; i < values.length; i++) {
				longs[i] = Long.parseLong(values[i]);
			}
			field.set(obj, longs);
		} else if (type == float[].class) {
			String[] values = value.split(" ");
			float[] floats = new float[values.length];
			for (int i = 0; i < values.length; i++) {
				floats[i] = Float.parseFloat(values[i]);
			}
			field.set(obj, floats);
		}
	}

	private int skipQuotedString(String line, int j) {
		return skipQuotedString(line, j, false, null);
	}

	private int skipQuotedString(String line, int j, boolean multipleString, String delimiter) {
		if (line.charAt(j) == '"') {
			while (true) {
				while (true) {
					j = line.indexOf('"', j + 1);
					if (j == -1) {
						j = line.length() - 1;
						break;
					} else if (line.charAt(j - 1) != '\\') {
						break;
					}
				}
				if (!multipleString) {
					break;
				}
				int l = line.indexOf('"', j + 1);
				int m = delimiter == null ? l : line.indexOf(delimiter, j + 1);
				if (l == -1 || m < l) {
					break;
				}
				j = l;
			}
		}
		return j;
	}

	/**
	 * Class for holding material information.
	 */
	private class MqoMaterial {
		String name;
		int shader;
		int vcol;
		float[] col = new float[4];
		float dif = -1;
		float amb = -1;
		float emi = -1;
		float spc = -1;
		float power = -1;
		String tex = null;
		String aplane = null;
		String bump = null;
		int proj_type = -1;
		float[] proj_pos = new float[3];
		float[] proj_scale = new float[3];
		float[] proj_angle = new float[3];

		@Override
		public String toString() {
			return String.format("%s shader(%d) vcol(%d) col(%.3f %.3f %.3f %.3f) " +
					"dif(%.3f) amb(%.3f) emi(%.3f) spc(%.3f) power(%.2f) tex(%s) alpha(%s) bump(%s) " +
					"proj_type(%d) " +
					"proj_pos(%.3f %.3f %.3f) " +
					"proj_scale(%.3f %.3f %.3f) " +
					"proj_angle(%.3f %.3f %.3f)",
					name, shader, vcol, col[0], col[1], col[2], col[3],
					dif,amb, emi, spc, power, tex, aplane, bump,
					proj_type,
					proj_pos[0], proj_pos[1], proj_pos[2],
					proj_scale[0], proj_scale[1], proj_scale[2],
					proj_angle[0], proj_angle[1], proj_angle[2]);
		}
	}

	/**
	 * Class for holding object information.
	 */
	@SuppressWarnings("unused")
	private static class MqoObject {
		String name;
		int depth;
		int folding;
		float[] scale;
		float[] rotation;
		float[] translation;
		int patch;
		int segment;
		int visible;
		int locking;
		int shading;
		float facet;
		float[] color;
		int color_type;
		int mirror;
		int mirror_axis;
		float mirror_dis;
		int lathe;
		int lathe_axis;
		int lathe_seg;
		Point[] vertex;
		MqoFace[] face;

		public Point maxPos = new Point(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
		public Point minPos = new Point(Float.MIN_VALUE, Float.MIN_VALUE, Float.MIN_VALUE);

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append(name);
			sb.append(" (num of vertices: ");
			sb.append(vertex == null ? 0 : vertex.length);
			sb.append(", num of faces: ");
			sb.append(face == null ? 0 : face.length);
			sb.append(")");
			return sb.toString();
		}
	}

	private static class MqoFace {
		int[] V;
		int M;
		float[] UV;
		long[] COL;

		/**
		 * Divide a rectangle face into two triangle faces:<br>
		 * <pre> 0  3 ->  0  0  3
		 *  __          __
		 * |  |     /\ \  /
		 * |__| -> /__\ \/
		 *
		 * 1  2 -> 1   2 2</pre>
		 *
		 * @return
		 */
		public MqoFace[] divide() {
			if (V == null || V.length != 4) {
				return null;
			}
			MqoFace[] faces = new MqoFace[2];
			for (int i = 0; i < 2; i ++) {
				faces[i] = new MqoFace();
				faces[i].V = new int[3];
				faces[i].M = M;
				faces[i].UV = new float[6];
				if (COL != null) {
					faces[i].COL = new long[3];
				}
				int k = 0;
				for (int j = 0; j < 4; j ++) {
					if ((i == 0 && j == 3) ||
							(i == 1 && j == 1)) {
						continue;
					}
					faces[i].V[k] = V[j];
					faces[i].UV[k*2+0] = UV[j*2+0];
					faces[i].UV[k*2+1] = UV[j*2+1];
					if (COL != null) {
						faces[i].COL[k] = COL[j];
					}
					k ++;
				}
			}
			return faces;
		}

		@Override
		public String toString() {
			if (V == null) {
				return "";
			}
			StringBuilder sb = new StringBuilder();
			sb.append(V.length);
			sb.append(" V(");
			append(sb, V);
			sb.append(" M(");
			sb.append(M);
			sb.append(") UV(");
			append(sb, UV);
			sb.append(") COL(");
			append(sb, COL);
			sb.append(")");
			return sb.toString();
		}
	}
}

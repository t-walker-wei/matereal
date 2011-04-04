package jp.digitalmuseum.jogl;

import java.nio.ByteBuffer;

import javax.media.opengl.GL;

public class JoglModelBase {

	protected GL gl;

	/**
	 * テクスチャ管理クラス
	 */
	protected TextureManager textureManager = null;

	/**
	 * テクスチャ管理クラスをこのクラスで作成したかどうか
	 */
	protected boolean hasOwnTextureManager = false;

	/**
	 * モデルデータの高さ方向の最低値を原点に補正するかどうか
	 */
	protected boolean isUpperGround = false;

	/**
	 * VBO（頂点配列バッファ）を使用するかどうか
	 */
	protected boolean isUseVBO = false;

	/**
	 * 表面の設定
	 */
	protected int frontFace = -1;

	/**
	 * 座標系情報
	 */
	protected JoglCoordinates coordinates = null;

	/**
	 * モデルの頂点の最低値
	 */
	protected Point minPos = null;

	/**
	 * モデルの頂点の最小値
	 */
	protected Point maxPos = null;

	/**
	 * 描画用内部データ
	 */
	protected JoglObject[] joglObjects;

	public void clear() {
		if (joglObjects == null)
			return;
		for (int o = 0; o < joglObjects.length; o++) {
			if (joglObjects[o].vboIds != null)
				gl.glDeleteBuffersARB(
						joglObjects[o].vboIds.length, joglObjects[o].vboIds, 0);
		}
		joglObjects = null;
		if (hasOwnTextureManager) {
			textureManager.clear();
			textureManager = null;
		}
	}

	/**
	 *
	 * @param gl
	 *            OpenGLコマンド群をカプセル化したクラス
	 * @param textureManager
	 *            テクスチャ管理クラス（nullならこのクラス内部に作成）
	 * @param scale
	 *            モデルの倍率
	 * @param coordinates
	 *            表示座標情報クラス
	 * @param isUpperGround
	 *            モデルデータの高さ方向の最低値を原点に補正するかどうか
	 * @param isUseVBO
	 *            頂点配列バッファを使用するかどうか
	 */
	protected JoglModelBase(GL gl, TextureManager textureManager, JoglCoordinates coordinates, boolean isUpperGround, boolean isUseVBO) {

		this.gl = gl;

		if (textureManager == null) {
			this.textureManager = new TextureManager(this.gl);
			hasOwnTextureManager = true;
		} else {
			this.textureManager = textureManager;
		}

		if(coordinates == null) {
			this.coordinates = new JoglCoordinates() ;
		} else {
			this.coordinates = coordinates;
		}

		this.isUpperGround = isUpperGround;

		if (isUseVBO) {
			this.isUseVBO = JoglUtils.isExtensionSupported(gl, "GL_ARB_vertex_buffer_object");
		}

		//OpenGLのデフォルト（表面からみた並びは左回り）
		this.frontFace = GL.GL_CCW;

		this.joglObjects = null;
	}

	private void updateMinMax() {
		if( joglObjects == null ) return ;
		minPos = null ;
		maxPos = null ;
		for( int o = 0 ; o < joglObjects.length ; o++ ) {
			if( joglObjects[o].isVisible ) {
				if( minPos == null ) minPos = new Point(joglObjects[o].minPos) ;
				else minPos.updateMinimum(joglObjects[o].minPos) ;
				if( maxPos == null ) maxPos = new Point(joglObjects[o].maxPos) ;
				else maxPos.updateMinimum(joglObjects[o].maxPos) ;
			}
		}
	}
	/**
	 * 描画有無を変更する<br>
	 * @param objectName	オブジェクト名
	 * @param isVisible	描画有無
	 */
	public void objectVisible(String objectName,boolean isVisible) {
		if( joglObjects == null ) return ;
		for( int o = 0 ; o < joglObjects.length ; o++ ) {
			if( objectName.equals(joglObjects[o].name) ) {
				joglObjects[o].isVisible = isVisible ;
				break ;
			}
		}
		updateMinMax() ;
	}
	/**
	 * 描画有無を変更する<br>
	 * @param materialtName	マテリアル名
	 * @param isVisible	描画有無
	 */
	public void materialVisible(String materialtName,boolean isVisible) {
		if( joglObjects == null ) return ;
		for( int o = 0 ; o < joglObjects.length ; o++ ) {
			for( int m = 0 ; m < joglObjects[o].materials.length ; m++ ) {
				if( materialtName.equals(joglObjects[o].materials[m].name) ) {
					joglObjects[o].materials[m].isVisible = isVisible ;
					break ;
				}
			}
		}
		updateMinMax() ;
	}
	/**
	 * 描画有無を変更する<br>
	 * @param objectName	オブジェクト名
	 * @param materialtName	マテリアル名
	 * @param isVisible	描画有無
	 */
	public void materialVisible(String objectName,String materialtName,boolean isVisible) {
		if( joglObjects == null ) return ;
		for( int o = 0 ; o < joglObjects.length ; o++ ) {
			if( ! objectName.equals(joglObjects[o].name) ) continue ;
			for( int m = 0 ; m < joglObjects[o].materials.length ; m++ ) {
				if( materialtName.equals(joglObjects[o].materials[m].name) ) {
					joglObjects[o].materials[m].isVisible = isVisible ;
					break ;
				}
			}
		}
		updateMinMax() ;
	}
	private boolean[] setEnables = null ;
	private int[] setEnablesInteger = null ;
	private boolean isChangeScale = false ;
	private boolean isNormalize = false ;
	/**
	 * 描画に必要なglEnable処理を一括して行う。<br>
	 * glEnableするものは<br>
	 * GL_DEPTH_TEST<br>
	 * GL_ALPHA_TEST<br>
	 * GL_NORMALIZE（scaleが1.0以外の場合のみ）<br>
	 * これらが必要ないことがわかっているときは手動で設定するほうがよいと思います<br>
	 *@param scale 描画するサイズ（１倍以外はＯｐｅｎＧＬに余計な処理が入る）
	 */
	public void enables(float scale) {
		setEnables = new boolean[2] ;
		setEnablesInteger = new int[2] ;
		setEnablesInteger[0] = GL.GL_DEPTH_TEST ;
		setEnablesInteger[1] = GL.GL_ALPHA_TEST ;
		for( int i = 0 ; i < setEnables.length ; i++ ) {
			setEnables[i] = gl.glIsEnabled(setEnablesInteger[i]) ;//現在の状態を取得
			if( ! setEnables[i] ) gl.glEnable(setEnablesInteger[i]) ;//現在、無効なら有効にする
		}

		if( scale != 1.0 ) {
			isChangeScale = true ;
			isNormalize = gl.glIsEnabled(GL.GL_NORMALIZE) ;
			gl.glPushMatrix() ;//スケールの変更はマトリックスの保存で元に戻す
			gl.glScalef(scale,scale,scale) ;
			if( ! isNormalize ) gl.glEnable(GL.GL_NORMALIZE) ;//スケールを変えるときはOpenGLに法線の計算をしてもらわないといけない
		}

	}
	/**
	 * 描画で使ったフラグ（enables()で設定したもの）をおとす<br>
	 * glDsableするものは<br>
	 * GL_DEPTH_TEST<br>
	 * GL_ALPHA_TEST<br>
	 * GL_NORMALIZE<br>
	 */
	public void disables() {
		if( setEnables != null && setEnablesInteger != null ) {
			for( int i = 0 ; i < setEnables.length ; i++ ) {
				if( ! setEnables[i] ) gl.glDisable(setEnablesInteger[i]) ;
			}
		}
		if( isChangeScale ) {
			gl.glPopMatrix() ;
			if( ! isNormalize ) gl.glDisable(GL.GL_NORMALIZE) ;
		}
	}
	/**
	 * 描画<br>
	 * 内部に持っているデータを描画する
	 */
	public void draw() {
		draw(1.0f) ;
	}
	/**
	 * 描画<br>
	 * 内部に持っているデータを描画する
	 *
	 *@param alpha	描画する透明度（０～１）
	 */
	public void draw(float alpha) {
		if (joglObjects == null) {
			return;
		}

		boolean isGL_BLEND = gl.glIsEnabled(GL.GL_BLEND);
		int[] intBlendFunc = new int[2];
		gl.glGetIntegerv(GL.GL_BLEND_SRC, intBlendFunc, 0);
		gl.glGetIntegerv(GL.GL_BLEND_DST, intBlendFunc, 1);
		if (!isGL_BLEND) {
			gl.glEnable(GL.GL_BLEND);
		}
		gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);

		int[] intFrontFace = new int[1];
		gl.glGetIntegerv(GL.GL_FRONT_FACE, intFrontFace, 0);
		gl.glFrontFace(frontFace);

		boolean isGL_TEXTURE_2D = gl.glIsEnabled(GL.GL_TEXTURE_2D);

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

				// OpenGLの描画フラグ設定
				if (joglMaterial.textureID != 0) {
					gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP);
					gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP);
					if (!isGL_TEXTURE_2D) {
						gl.glEnable(GL.GL_TEXTURE_2D);
					}
				}

				if (joglMaterial.isSmoothShading) {
					gl.glShadeModel(GL.GL_SMOOTH);
				} else {
					gl.glShadeModel(GL.GL_FLAT);
				}

				// 色関係の設定
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

				// テクスチャの設定
				if (joglMaterial.textureID != 0) {
					gl.glBindTexture(GL.GL_TEXTURE_2D, joglMaterial.textureID);
				}

				// 描画実行
				if (isUseVBO) {
					gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, joglObject.vboIds[m]);
					gl.glInterleavedArrays(joglMaterial.interleaveFormat, 0, 0);
				} else {
					joglMaterial.interleaved.position(0);
					gl.glInterleavedArrays(joglMaterial.interleaveFormat, 0, joglMaterial.interleaved);
				}
				gl.glDrawArrays(GL.GL_TRIANGLES, 0, joglMaterial.numVertices);

				// 設定をクリア
				if (joglMaterial.textureID != 0) {
					gl.glBindTexture(GL.GL_TEXTURE_2D, 0);
				}
				if (isUseVBO) {
					gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, 0);
				}

				// 設定を復帰 (1)
				if (joglMaterial.textureID != 0 && !isGL_TEXTURE_2D) {
					gl.glDisable(GL.GL_TEXTURE_2D);
				} else if (joglMaterial.textureID == 0 && isGL_TEXTURE_2D) {
					gl.glEnable(GL.GL_TEXTURE_2D);
				}
			}
		}

		// 設定を復帰 (2)
		if (!isGL_BLEND) {
			gl.glDisable(GL.GL_BLEND);
		}
		gl.glBlendFunc(intBlendFunc[0], intBlendFunc[1]);
		gl.glFrontFace(intFrontFace[0]);
	}

	public Point getMaxPos() {
		return maxPos;
	}

	public Point getMinPos() {
		return minPos;
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

	public static class JoglObject {
		String name = null;
		boolean isVisible = true;
		JoglMaterial[] materials = null;
		int[] vboIds = null;
		Point minPos = null;
		Point maxPos = null;
	}

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

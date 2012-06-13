package marker;
import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLJPanel;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.phybots.Phybots;
import com.phybots.gui.DisposeOnCloseFrame;
import com.phybots.service.Camera;
import com.phybots.service.MarkerDetector;
import com.phybots.service.ServiceGroup;
import com.phybots.utils.Array;
import com.sun.opengl.util.Animator;
import com.sun.opengl.util.FPSAnimator;

import jp.digitalmuseum.napkit.NapDetectionResult;
import jp.digitalmuseum.napkit.NapJoglUtils;
import jp.digitalmuseum.napkit.gui.MarkerDetectorPanel;

/**
 * Run marker detection and show its results.
 *
 * @author Jun Kato
 */
public class DetectMarkerWith3DCGOverlay implements GLEventListener {

	public static void main(String[] args) {
		new DetectMarkerWith3DCGOverlay();
	}

	private MarkerDetector detector;
	private Camera camera;
	private DisposeOnCloseFrame frame;

	private GL gl;
	private NapJoglUtils util;
	private Animator animator;
	private int polyList = 0;

	private int fps = 15;

	public DetectMarkerWith3DCGOverlay() {

		// Run two services as one service group.
		ServiceGroup serviceGroup = new ServiceGroup();
		serviceGroup.setInterval(1000/fps);
		serviceGroup.start();

		// Run a camera.
		// Let users select a device to capture images.
		String identifier = (String) JOptionPane.showInputDialog(null,
				"Select a device to capture images.", "Device list",
				JOptionPane.QUESTION_MESSAGE, null,
				Camera.queryIdentifiers(), null);
		if ((identifier != null) && (identifier.length() > 0)) {
			camera = new Camera(identifier);
		} else {
			camera = new Camera();
		}
		camera.setSize(800, 600);
		camera.start(serviceGroup);

		// Run a marker detector.
		detector = new MarkerDetector();
		detector.setImageProvider(camera);
		detector.setInterval(1000/fps);
		detector.setTransMatEnabled(true);

		// Show a configuration window.
		final JFrame configFrame = new DisposeOnCloseFrame(
				new MarkerDetectorPanel(detector));
		configFrame.setSize(640, 480);

		// Detect a marker.
		detector.addMarker(MarkerInfo.getRobotMarker());
		detector.start(serviceGroup);

		// Show detection results in real-time.
		GLJPanel panel = new GLJPanel();
		panel.addGLEventListener(this);
		frame = new DisposeOnCloseFrame(panel) {
			private static final long serialVersionUID = 1L;

			@Override
			public void dispose() {
				configFrame.dispose();
				super.dispose();
				animator.stop();
				Phybots.getInstance().dispose();
			}
		};
		frame.setFrameSize(camera.getWidth(), camera.getHeight());
	}

	public void init(GLAutoDrawable drawable) {
		gl = drawable.getGL();
		gl.glEnable(GL.GL_DEPTH_TEST);
		gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

		util = new NapJoglUtils(gl);

		animator = new FPSAnimator(drawable, fps);
		animator.start();
	}

	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
		gl.glViewport(0, 0, width, height);
		gl.glMatrixMode(GL.GL_PROJECTION);
		gl.glLoadIdentity();
		gl.glMatrixMode(GL.GL_MODELVIEW);
		gl.glLoadIdentity();
	}

	public void display(GLAutoDrawable drawable) {

		// Clear the buffer and draw background.
		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
		util.drawBackGround(camera.getImageData(), camera.getWidth(), camera.getHeight(), 1.0);

		// Draw a cube if markers are detected.
		final Array<NapDetectionResult> results = detector.getResults();
		for (NapDetectionResult result : results) {
			if (result.getConfidence() > 0.5) {
				if (util.preDisplay(detector, result)) {
					drawCube();
					util.postDisplay();
				}
			}
		}
	}

	/**
	 * Draw a cube.
	 */
	private void drawCube() {
		if (polyList == 0) {
			initCube();
		}
		gl.glPushMatrix();
		gl.glTranslatef(0.0f, 0.0f, 0.05f);
		gl.glDisable(GL.GL_LIGHTING);
		gl.glCallList(polyList);
		gl.glPopMatrix();
	}

	private void initCube() {

		float fSize = 0.05f;
		int f, i;
		float[][] cubeVertices = new float[][] {
			{  1.0f,  1.0f,  1.0f },
			{  1.0f, -1.0f,  1.0f },
			{ -1.0f, -1.0f,  1.0f },
			{ -1.0f,  1.0f,  1.0f },
			{  1.0f,  1.0f, -1.0f },
			{  1.0f, -1.0f, -1.0f },
			{ -1.0f, -1.0f, -1.0f },
			{ -1.0f,  1.0f, -1.0f }
		};
		float[][] cubeVertexColors = new float[][] {
			{ 1.0f, 1.0f, 1.0f },
			{ 1.0f, 1.0f, 0.0f },
			{ 0.0f, 1.0f, 0.0f },
			{ 0.0f, 1.0f, 1.0f },
			{ 1.0f, 0.0f, 1.0f },
			{ 1.0f, 0.0f, 0.0f },
			{ 0.0f, 0.0f, 0.0f },
			{ 0.0f, 0.0f, 1.0f }
		};

		short[][] cubeFaces = new short[][] {
			{ 3, 2, 1, 0 },
			{ 2, 3, 7, 6 },
			{ 0, 1, 5, 4 },
			{ 3, 0, 4, 7 },
			{ 1, 2, 6, 5 },
			{ 4, 5, 6, 7 }
		};

		polyList = gl.glGenLists(1);
		gl.glNewList(polyList, GL.GL_COMPILE);
		gl.glBegin(GL.GL_QUADS);
		for (f = 0; f < cubeFaces.length; f++)
			for (i = 0; i < 4; i++) {
				gl.glColor3f(cubeVertexColors[cubeFaces[f][i]][0],
						cubeVertexColors[cubeFaces[f][i]][1],
						cubeVertexColors[cubeFaces[f][i]][2]);
				gl.glVertex3f(cubeVertices[cubeFaces[f][i]][0] * fSize,
						cubeVertices[cubeFaces[f][i]][1] * fSize,
						cubeVertices[cubeFaces[f][i]][2] * fSize);
			}
		gl.glEnd();
		gl.glColor3f(0.0f, 0.0f, 0.0f);
		for (f = 0; f < cubeFaces.length; f++) {
			gl.glBegin(GL.GL_LINE_LOOP);
			for (i = 0; i < 4; i++)
				gl.glVertex3f(cubeVertices[cubeFaces[f][i]][0] * fSize,
						cubeVertices[cubeFaces[f][i]][1] * fSize,
						cubeVertices[cubeFaces[f][i]][2] * fSize);
			gl.glEnd();
		}
		gl.glEndList();
	}

	public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged) {
		// Do nothing.
	}
}

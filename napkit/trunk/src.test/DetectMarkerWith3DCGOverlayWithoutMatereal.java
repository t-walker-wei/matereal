import java.awt.Dimension;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLJPanel;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.sun.opengl.util.Animator;
import com.sun.opengl.util.FPSAnimator;

import jp.digitalmuseum.capture.VideoCapture;
import jp.digitalmuseum.capture.VideoCaptureFactoryImpl;
import jp.digitalmuseum.napkit.NapDetectionResult;
import jp.digitalmuseum.napkit.NapJoglUtils;
import jp.digitalmuseum.napkit.NapMarker;
import jp.digitalmuseum.napkit.NapMarkerDetector;
import jp.digitalmuseum.napkit.NapMarkerDetectorImpl;

/**
 * Run marker detection and show its results.
 *
 * @author Jun Kato
 */
public class DetectMarkerWith3DCGOverlayWithoutMatereal implements GLEventListener {

	public static void main(String[] args) {
		new DetectMarkerWith3DCGOverlayWithoutMatereal();
	}

	private NapMarkerDetector detector;
	private VideoCapture capture;
	private JFrame frame;

	private GL gl;
	private NapJoglUtils util;
	private Animator animator;
	private int polyList = 0;

	private int fps = 15;

	public DetectMarkerWith3DCGOverlayWithoutMatereal() {

		// Run a camera.
		// Let users select a device to capture images.
		VideoCaptureFactoryImpl factory = new VideoCaptureFactoryImpl();
		String identifier = (String) JOptionPane.showInputDialog(null,
				"Select a device to capture images.", "Device list",
				JOptionPane.QUESTION_MESSAGE, null,
				factory.queryIdentifiers(), null);
		if ((identifier != null) && (identifier.length() > 0)) {
			capture = factory.newInstance(identifier);
		} else {
			capture = factory.newInstance();
		}
		capture.setSize(800, 600);
		capture.setFrameRate(fps);
		try {
			capture.start();
		} catch (Exception e) {
			System.err.println("Failed to start camera.");
			return;
		}

		// Run a marker detector.
		detector = new NapMarkerDetectorImpl();
		detector.loadCameraParameter("calib_qcam.dat");
		detector.setTransMatEnabled(true);
		detector.setSize(capture.getWidth(), capture.getHeight());

		// Detect a marker.
		detector.addMarker(new NapMarker("markers\\4x4_78.patt", 45));
		detector.addMarker(new NapMarker("markers\\4x4_907.patt", 45));

		// Show detection results in real-time.
		GLJPanel panel = new GLJPanel();
		panel.addGLEventListener(this);
		panel.setPreferredSize(new Dimension(capture.getWidth(), capture.getHeight()));
		frame = new JFrame() {
			private static final long serialVersionUID = 1L;

			@Override
			public void dispose() {
				capture.stop();
				super.dispose();
				animator.stop();
			}
		};
		frame.add(panel);
		frame.pack();
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setVisible(true);
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

		// Grab an image frame and detect markers in the image.
		byte[] data = capture.grabFrameData();
		detector.detectMarker(data);

		// Clear the buffer and draw background.
		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
		util.drawBackGround(data, capture.getWidth(), capture.getHeight(), 1.0);

		// Draw a cube if markers are detected.
		boolean first = true;
		for (NapDetectionResult result : detector.getResults()) {
			if (result.getConfidence() > 0.5) {
				if (util.preDisplay(detector, result)) {
					if (first) {
						frame.setTitle("Marker detected at "+result.getPosition()+". (confidence:"+result.getConfidence()+")");
						first = false;
					}
					drawCube();
					util.postDisplay();
				}
			}
		}
		if (first) {
			frame.setTitle("No marker detected.");
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
		gl.glTranslatef(0.0f, 0.0f, 0.5f);
		gl.glDisable(GL.GL_LIGHTING);
		gl.glCallList(polyList);
		gl.glPopMatrix();
	}

	private void initCube() {

		float fSize = 0.5f;
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

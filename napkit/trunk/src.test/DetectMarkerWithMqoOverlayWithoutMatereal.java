import java.awt.Dimension;
import java.net.URL;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLException;
import javax.media.opengl.GLJPanel;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.sun.opengl.util.Animator;
import com.sun.opengl.util.FPSAnimator;

import jp.digitalmuseum.capture.VideoCapture;
import jp.digitalmuseum.capture.VideoCaptureFactoryImpl;
import jp.digitalmuseum.jogl.JoglModel;
import jp.digitalmuseum.jogl.JoglModelMqo;
import jp.digitalmuseum.jogl.JoglCoordinates_ARToolKit;
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
public class DetectMarkerWithMqoOverlayWithoutMatereal implements GLEventListener {

	public static void main(String[] args) {
		new DetectMarkerWithMqoOverlayWithoutMatereal();
	}

	private NapMarkerDetector detector;
	private VideoCapture capture;
	private JFrame frame;

	private GL gl;
	private NapJoglUtils util;
	private Animator animator;

	private int fps = 15;

	public DetectMarkerWithMqoOverlayWithoutMatereal() {

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
				if (data != null) {
					data.clear();
				}
				try {
					animator.stop();
				} catch (GLException e) {
					// Do nothing.
				}
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

		initModel();

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
					drawModel();
					util.postDisplay();
				}
			}
		}
		if (first) {
			frame.setTitle("No marker detected.");
		}
	}

	private final String mqoFileName = "model/JSS_miku/Jss_miku.mqo";
	private final float scale = .05f;
	// private final String mqoFileName = "./model/gradriel/gradriel_pose.mqo";
	// private final float scale = .02f;
	// private final String mqoFileName = "model/Lat式ミク/miltukumiku.mqo";
	// private final float scale = .005f;
	// private final String mqoFileName = "model/box.mqo";
	// private final float scale = .01f;

	private JoglModel data;
	private float translate;
	private void initModel() {
		if (data == null) {
			try {
				data = new JoglModelMqo(gl, new URL("file:"+mqoFileName), null,
						scale, new JoglCoordinates_ARToolKit(), true);

				// 髪の毛が足より下にあるので、地面に足をつけようとするとこうなる。
				translate = -data.getMinPos().getZ() / 2;
				// translate = -data.getMinPos().getZ();
			} catch (Exception e) {
				e.printStackTrace();
				data = null;
			}
		}
	}

	private void drawModel() {
		if (data != null) {
			gl.glPushMatrix();
			gl.glTranslatef(0, 0, translate);
			data.draw();
			gl.glPopMatrix();
		}
	}

	public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged) {
		// Do nothing.
	}
}

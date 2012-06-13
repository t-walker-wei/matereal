package marker;
import java.net.URL;

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

import jp.digitalmuseum.jogl.JoglCoordinates_ARToolKit;
import jp.digitalmuseum.jogl.JoglModel;
import jp.digitalmuseum.jogl.JoglModelMqo;
import jp.digitalmuseum.napkit.NapDetectionResult;
import jp.digitalmuseum.napkit.NapJoglUtils;
import jp.digitalmuseum.napkit.gui.MarkerDetectorPanel;

/**
 * Run marker detection and show its results.
 *
 * @author Jun Kato
 */
public class DetectMarkerWithMqoOverlay implements GLEventListener {

	public static void main(String[] args) {
		new DetectMarkerWithMqoOverlay();
	}

	private MarkerDetector detector;
	private Camera camera;
	private DisposeOnCloseFrame frame;

	private GL gl;
	private NapJoglUtils util;
	private Animator animator;

	private int fps = 15;

	public DetectMarkerWithMqoOverlay() {

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

		// Clear the buffer and draw background.
		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
		util.drawBackGround(camera.getImageData(), camera.getWidth(), camera.getHeight(), 1.0);

		// Draw a cube if markers are detected.
		final Array<NapDetectionResult> results = detector.getResults();
		for (NapDetectionResult result : results) {
			if (result.getConfidence() > 0.5) {
				if (util.preDisplay(detector, result)) {
					drawModel();
					util.postDisplay();
				}
			}
		}
	}

	private final String mqoFileName = "model/JSS_miku/Jss_miku.mqo";
	private final float scale = .005f;

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

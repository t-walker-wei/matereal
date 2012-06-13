import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLJPanel;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.sun.opengl.util.Animator;
import com.sun.opengl.util.FPSAnimator;

import jp.digitalmuseum.capture.VideoCapture;
import jp.digitalmuseum.capture.VideoCaptureFactoryImpl;
import jp.digitalmuseum.jogl.JoglCoordinates_ARToolKit;
import jp.digitalmuseum.jogl.JoglModel;
import jp.digitalmuseum.jogl.JoglModelMqo;
import jp.digitalmuseum.napkit.NapCameraRelation;
import jp.digitalmuseum.napkit.NapDetectionResult;
import jp.digitalmuseum.napkit.NapJoglUtils;
import jp.digitalmuseum.napkit.NapMarker;
import jp.digitalmuseum.napkit.NapMarkerDetector;
import jp.digitalmuseum.napkit.NapMarkerDetectorImpl;
import jp.digitalmuseum.napkit.NapUtils;

/**
 * Run marker detection and show its results.
 *
 * @author Jun Kato
 */
public class DetectMarkerWithMqoOverlayWith2Cameras implements GLEventListener {

	public static void main(String[] args) {
		new DetectMarkerWithMqoOverlayWith2Cameras();
	}

	private NapMarkerDetector detector;
	private VideoCapture[] captures;
	private JFrame frame;

	private GL gl;
	private NapJoglUtils util;
	private Animator animator;

	private int fps = 15;
	private int cameraIndex, hoveredButtonIndex;
	private VideoCapture capture;

	private boolean calcCamRelation;
	private NapCameraRelation cr;
	private int camBaseIndex;

	public DetectMarkerWithMqoOverlayWith2Cameras() {

		// Run a camera.
		// Let users select a device to capture images.
		VideoCaptureFactoryImpl factory = new VideoCaptureFactoryImpl();
		captures = new VideoCapture[2];
		for (int i = 0; i < captures.length; i ++) {
			VideoCapture capture;
			String label = i == 0 ? "first" : "second";
			String identifier = (String) JOptionPane.showInputDialog(null,
					"Select "+label+" device to capture images.", "Device list",
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
				System.err.println("Failed to start "+label+" camera.");
				return;
			}
			captures[i] = capture;
		}
		cameraIndex = 0;
		capture = captures[cameraIndex];

		// Run a marker detector.
		detector = new NapMarkerDetectorImpl();
		detector.loadCameraParameter("calib_qcam.dat");
		detector.setTransMatEnabled(true);
		detector.setSize(capture.getWidth(), capture.getHeight());

		// Detect two markers.
		detector.addMarker(new NapMarker("markers\\4x4_78.patt", 45));
		detector.addMarker(new NapMarker("markers\\4x4_907.patt", 45));

		// Show detection results in real-time.
		SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				GLJPanel panel = new GLJPanel() {
					private static final long serialVersionUID = -941291616022451135L;
					private Composite alphaComp = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .7f);

					@Override
					public void paintComponent(Graphics g) {
						super.paintComponent(g);
						Graphics2D g2 = (Graphics2D) g;
						Composite comp = g2.getComposite();

						g2.setComposite(alphaComp);
						g2.setColor(Color.black);
						for (int i = 0; i < captures.length + 1; i++) {
							g2.fillRect(5 + i * 115, 5, 110, 30);
						}

						g2.setComposite(comp);
						g2.setColor(Color.white);
						for (int i = 0; i < captures.length; i++) {
							g2.drawString("Camera " + (i + 1), 10 + i * 115, 30);
						}
						g2.drawString("Calc cam relation", 10 + captures.length * 115, 30);

						g2.setColor(Color.red);
						g2.drawRect(5 + cameraIndex * 115, 5, 109, 29);
						if (hoveredButtonIndex >= 0 &&
								hoveredButtonIndex <= captures.length) {
							g2.setColor(Color.yellow);
							g2.drawRect(6 + hoveredButtonIndex * 115, 6, 107, 27);
						}
					}
				};
				panel.addGLEventListener(DetectMarkerWithMqoOverlayWith2Cameras.this);
				panel.addMouseListener(new MouseAdapter() {

					@Override
					public void mouseReleased(MouseEvent e) {
						int index = getButtonIndex(e.getX(), e.getY());
						if (index >= 0) {
							if (index < captures.length) {
								cameraIndex = index;
								capture = captures[cameraIndex];
							} else {
								calcCamRelation = true;
							}
						}
					}
				});
				panel.addMouseMotionListener(new MouseMotionAdapter() {

					@Override
					public void mouseMoved(MouseEvent e) {
						hoveredButtonIndex = getButtonIndex(e.getX(), e.getY());
					}
				});
				panel.setPreferredSize(new Dimension(capture.getWidth(), capture.getHeight()));
				frame = new JFrame() {
					private static final long serialVersionUID = 1L;

					@Override
					public void dispose() {
						for (VideoCapture capture : captures) {
							capture.stop();
						}
						super.dispose();
						if (animator.isAnimating()) {
							animator.stop();
						}
					}
				};
				frame.add(panel);
				frame.pack();
				frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				frame.setVisible(true);
			}
		});
	}

	private int getButtonIndex(int x, int y) {

		if (y < 5 || y > 35) {
			return -1;
		}

		return (x - 5) % 115 <= 110 ? (x - 5) / 115 : - 1;
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

	private Set<NapMarker> retryingSet = new HashSet<NapMarker>();
	private Set<NapDetectionResult> resultSet = new HashSet<NapDetectionResult>();

	public void display(GLAutoDrawable drawable) {

		// Grab an image frame and detect markers in the image.
		byte[] data = capture.grabFrameData();
		detector.detectMarker(data);

		// Clear the buffer and draw background.
		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
		util.drawBackGround(data, capture.getWidth(), capture.getHeight(), 1.0);

		// Detect markers from the primary camera.
		detector.detectMarker(capture.grabFrameData());
		retryingSet.clear();
		resultSet.clear();
		for (NapMarker marker : detector.getMarkers()) {
			NapDetectionResult result = detector.getResult(marker);
			if (result == null) {
				retryingSet.add(marker);
			} else {
				resultSet.add(result);
			}
		}

		// Draw characters if markers are detected.
		for (NapDetectionResult result : resultSet) {
			if (result.getConfidence() > 0.5) {
				if (util.preDisplay(detector, result)) {
					drawModel();
					util.postDisplay();
				}
			}
		}

		// Detect markers from the secondary camera.
		if (!retryingSet.isEmpty() || calcCamRelation) {
			detector.detectMarker(captures[1 - cameraIndex].grabFrameData());

			// Calculate relation of two cameras.
			if (calcCamRelation) {
				cr = NapCameraRelation.calcCameraRelation(resultSet, detector.getResults());
				camBaseIndex = cameraIndex;
				calcCamRelation = false;
			}

			// Assume result from the secondary camera.
			else if (cr != null) {
				for (NapMarker marker : retryingSet) {
					NapDetectionResult result = detector.getResult(marker);
					if (result != null) {

						// Draw characters if markers are detected.
						double[] assumedModelViewMatrix = cr.assumeModelViewMatrix(result, camBaseIndex == cameraIndex);
						if (assumedModelViewMatrix != null) {
							NapUtils.convertMatrix4x4toGl(assumedModelViewMatrix);
							util.preDisplay(detector, assumedModelViewMatrix);
							drawModel();
							util.postDisplay();
						}
					}
				}
			}
		}
	}

	private final String mqoFileName = "model/JSS_miku/Jss_miku.mqo";
	private final float scale = .05f;

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

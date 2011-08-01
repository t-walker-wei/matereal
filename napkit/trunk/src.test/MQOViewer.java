import java.awt.Dimension;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.net.URL;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLJPanel;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import jp.digitalmuseum.jogl.JoglCoordinates_ARToolKit;
import jp.digitalmuseum.jogl.JoglModel;
import jp.digitalmuseum.jogl.JoglModelMqo;

import com.sun.opengl.util.Animator;
import com.sun.opengl.util.FPSAnimator;
import com.sun.opengl.util.GLUT;


public class MQOViewer implements GLEventListener, Runnable {
	private JFrame frame;

	private GL gl;
	private GLUT glut;
	private Animator animator;

	private final String mqoFileName = "model/ninja.mqo";
	private final float scale = .01f;

	private JoglModel data;

	private int fps = 15;
	private float angleX;
	private float angleY;
	private float zoom = 1;
	private int prevMouseX;
	private int prevMouseY;

	public static void main(String[] args) {
		new MQOViewer();
	}

	public MQOViewer() {
		SwingUtilities.invokeLater(this);
	}

	public void run() {
		GLJPanel panel = new GLJPanel();
		panel.addGLEventListener(this);
		panel.setPreferredSize(new Dimension(640, 640));
		panel.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				prevMouseX = e.getX();
				prevMouseY = e.getY();
			}
		});
		panel.addMouseMotionListener(new MouseMotionAdapter() {

			public void mouseDragged(MouseEvent e) {
				int x = e.getX();
				int y = e.getY();

				Dimension size = e.getComponent().getSize();

				if ((e.getModifiers() & InputEvent.BUTTON1_MASK) != 0) {
					float thetaY = 360.0f * ((float) (x - prevMouseX) / size.width);
					float thetaX = 360.0f * ((float) (prevMouseY - y) / size.height);
					angleX -= thetaX;
					angleY += thetaY;
					frame.setTitle("Rotation: " + String.format("%.3f, %.3f", angleX, angleY));
				} else if ((e.getModifiers() & InputEvent.BUTTON3_MASK) != 0) {
					zoom += (float) (x - prevMouseX) * 2 / size.width;
					frame.setTitle("Zoom: " + String.format("%.3f", zoom));
				}

				prevMouseX = x;
				prevMouseY = y;
			}
		});
		frame = new JFrame() {
			private static final long serialVersionUID = 1L;

			@Override
			public void dispose() {
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

		// Enable lighting.
		gl.glEnable(GL.GL_LIGHTING);
		gl.glEnable(GL.GL_LIGHT0);

		float[] position = { -10.0f, 10.0f, 10.0f, 0.0f };
		float[] specular = { 1.0f, 1.0f, 1.0f, 1.0f };
		float[] diffuse = { 1.0f, 1.0f, 1.0f, 1.0f };
		float[] ambient = { 0.8f, 0.8f, 0.8f, 1.0f };

		gl.glLightfv(GL.GL_LIGHT0, GL.GL_POSITION, position, 0);
		gl.glLightfv(GL.GL_LIGHT0, GL.GL_SPECULAR, specular, 0);
		gl.glLightfv(GL.GL_LIGHT0, GL.GL_DIFFUSE, diffuse, 0);
		gl.glLightfv(GL.GL_LIGHT0, GL.GL_AMBIENT, ambient, 0);

		// gl.glEnable(GL.GL_NORMALIZE);

		glut = new GLUT();

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

		// Draw the model.
		gl.glPushMatrix();
		gl.glScalef(zoom, zoom, zoom);
		gl.glRotatef(angleX, 1.0f, 0.0f, 0.0f);
		gl.glRotatef(angleY, 0.0f, 1.0f, 0.0f);
		if (data != null) {
	        data.draw();
		}
		// glut.glutSolidCube(1.0f);
        gl.glPopMatrix();
	}

	private void initModel() {
		if (data == null) {
			try {
				data = new JoglModelMqo(gl, new URL("file:"+mqoFileName), null, scale, new JoglCoordinates_ARToolKit(), true);
			} catch (Exception e) {
				e.printStackTrace();
				data = null;
			}
		}
	}

	public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged) {
		// Do nothing.
	}
}

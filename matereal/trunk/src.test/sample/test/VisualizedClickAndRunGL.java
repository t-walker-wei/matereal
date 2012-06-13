package sample.test;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.nio.ByteBuffer;
import java.util.Random;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLJPanel;

import com.phybots.Phybots;
import com.phybots.gui.*;
import com.phybots.hakoniwa.Hakoniwa;
import com.phybots.hakoniwa.HakoniwaRobot;
import com.phybots.resource.WheelsController;
import com.phybots.task.Move;
import com.phybots.task.Task;
import com.phybots.task.VectorFieldTask;
import com.phybots.utils.Location;
import com.phybots.utils.Position;
import com.phybots.utils.ScreenPosition;
import com.phybots.utils.Vector2D;
import com.sun.opengl.util.Animator;
import com.sun.opengl.util.BufferUtil;
import com.sun.opengl.util.FPSAnimator;


/**
 * Click and run!
 *
 * @author Jun Kato
 */
public class VisualizedClickAndRunGL {
	private final static int DEFAULT_INTERVAL = 50;
	final private Hakoniwa hakoniwa;
	final HakoniwaRobot hakoniwaRobot;
	final DisposeOnCloseFrame frame;
	final private ScreenPosition goalPosition = new ScreenPosition();
	private Task moveTo;

	public static void main(String[] args) {
		new VisualizedClickAndRunGL();
	}

	public VisualizedClickAndRunGL() {

		// Run hakoniwa.
		hakoniwa = new Hakoniwa();
		hakoniwa.setAntialiased(true);
		hakoniwa.setViewportSize(640, 480);
		hakoniwa.setViewportScale(100);
		hakoniwa.setInterval(DEFAULT_INTERVAL);
		hakoniwa.start();

		// Instantiate a robot.
		hakoniwaRobot =
				new HakoniwaRobot("test", new Location(hakoniwa.getRealWidth()/2, hakoniwa.getRealHeight()/2, -Math.PI*3/4));

		// Make and show a window for showing captured image.
		final MyGLJPanel panel = new MyGLJPanel();
		final Animator animator = new FPSAnimator(
				panel,
				DEFAULT_INTERVAL,
				false);
		frame = new DisposeOnCloseFrame(panel) {
			private static final long serialVersionUID = 1L;
			@Override
			public void dispose() {
				super.dispose();
				animator.stop();
				Phybots.getInstance().dispose();
			}
		};
		// frame.setResizable(false);
		frame.setFrameSize(hakoniwa.getWidth(), hakoniwa.getHeight());

		panel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				goalPosition.set(e.getX(), e.getY());
				final Task task = hakoniwaRobot.getAssignedTask(WheelsController.class);
				if (task != null) {
					task.stop();
				}
				moveTo = new Move(hakoniwa.screenToReal(goalPosition));
				if (moveTo.assign(hakoniwaRobot)) {
					moveTo.start();
				}
			}
		});

		// Repaint the window periodically.
		animator.start();
		/*
		Phybots.getInstance().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { frame.repaint(); }
		});
		*/
	}

	private class MyGLJPanel extends GLJPanel implements GLEventListener {
		private static final long serialVersionUID = 1L;
		private static final int NPN = 64;
		private static final int NMESH = 80;
		private static final float DM = (float) (1.0/(NMESH-1.0));
		private static final float SCALE = 4.0f;
		private static final int Npat = 32;
		private static final byte alpha = (byte) (0.12*256);
		private int iframe = 0;
		private float tmax = 640/(SCALE*NPN);
		private float dmax = SCALE/640;
		private GL gl;
		final Position p = new Position();
		final Vector2D v = new Vector2D();

		public MyGLJPanel() {
			addGLEventListener(this);
		}

		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			render2DForeground((Graphics2D)g);
		}

		private void render2DForeground(Graphics2D g) {
			final Task task = hakoniwaRobot.getAssignedTask(WheelsController.class);
			hakoniwa.drawImage(g);
			g.setColor(Color.black);
			if (task != null) {
				g.drawString("Status: "+task, 10, 30);
				g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				final Stroke s = g.getStroke();
				g.setStroke(new BasicStroke(2));
				g.drawLine(
						goalPosition.getX()-5,
						goalPosition.getY()-5,
						goalPosition.getX()+5,
						goalPosition.getY()+5);
				g.drawLine(
						goalPosition.getX()-5,
						goalPosition.getY()+5,
						goalPosition.getX()+5,
						goalPosition.getY()-5);
				g.setStroke(s);
			}
			else {
				g.drawString("Status: Goal not specified", 10, 30);
			}
			g.drawLine(10, 35, getWidth()-10, 35);
		}

		public void init(GLAutoDrawable drawable) {
			gl = drawable.getGL();
			gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_REPEAT);
			gl.glTexParameteri(GL.GL_TEXTURE_2D,
					GL.GL_TEXTURE_WRAP_T, GL.GL_REPEAT);
			gl.glTexParameteri(GL.GL_TEXTURE_2D,
					GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
			gl.glTexParameteri(GL.GL_TEXTURE_2D,
					GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
			gl.glTexEnvf(GL.GL_TEXTURE_ENV,
					GL.GL_TEXTURE_ENV_MODE, GL.GL_REPLACE);
			gl.glEnable(GL.GL_TEXTURE_2D);
			gl.glShadeModel(GL.GL_FLAT);
			gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
			gl.glClear(GL.GL_COLOR_BUFFER_BIT);
			makePatterns(gl);
		}

		private void makePatterns(GL gl) {
			final Random random = new Random();
			final byte[] lut = new byte[256];
			final int[][] phase = new int[NPN][NPN];
			final ByteBuffer pat = BufferUtil.newByteBuffer(NPN*NPN*4);
			int i, j, k, t;
			for (i = 0; i < 256; i++) lut[i] = (byte) (i < 127 ? 0xcc : 0xff);
			for (i = 0; i < NPN; i++)
			for (j = 0; j < NPN; j++) phase[i][j] = random.nextInt(256);
			for (k = 0; k < Npat; k++) {
				t = k*256/Npat;
				for (i = 0; i < NPN; i++) {
					for (j = 0; j < NPN; j++) {
						final byte color = lut[(t + phase[i][j]) % 256];
						pat.put(color);
						pat.put(color);
						pat.put(color);
						pat.put(alpha);
					}
				}
				pat.rewind();
				gl.glNewList(k + 1, GL.GL_COMPILE);
				gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, 4, NPN, NPN, 0,
						GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, pat);
				gl.glEndList();
			}
		}

		public void display(GLAutoDrawable drawable) {

			// Drift texture along with the flow field.
			if (moveTo != null && moveTo.isStarted()) {
				int i, j;
				float x1, x2, y, px, py;
				for (i = 0; i < NMESH-1; i++) {
					x1 = DM*i; x2 = x1 + DM;
					gl.glBegin(GL.GL_QUAD_STRIP);
					for (j = 0; j < NMESH; j++) {
						y = DM*j;
						gl.glTexCoord2f(x1, y);

						double norm;
						p.set(x1*hakoniwa.getRealWidth(),
								y*hakoniwa.getRealHeight());
						((VectorFieldTask) moveTo).getVectorOut(p, v);
						norm = v.getNorm();
						px = (float) (x1+v.getX()*dmax/norm);
						py = (float) (y+v.getY()*dmax/norm);
						// getDP(x1, y, &px, &py);

						gl.glVertex2f(px, py);
						gl.glTexCoord2f(x2, y);

						p.set(x2*hakoniwa.getRealWidth(),
								y*hakoniwa.getRealHeight());
						((VectorFieldTask) moveTo).getVectorOut(p, v);
						norm = v.getNorm();
						px = (float) (x2+v.getX()*dmax/norm);
						py = (float) (y+v.getY()*dmax/norm);
						// getDP(x2, y, &px, &py);

						gl.glVertex2f(px, py);
					}
					gl.glEnd();
				}
			}
			iframe ++;

			// Blend with background image.
			gl.glEnable(GL.GL_BLEND);
			/*
			final ImageProvider imageProvider = hakoniwa;
			gl.glDisable(GL.GL_TEXTURE_2D);
			gl.glPixelStorei(GL.GL_UNPACK_ALIGNMENT, 1);
			gl.glPixelZoom(1f, -1f);
			gl.glPixelTransferf(GL.GL_ALPHA_SCALE, .2f);
			gl.glWindowPos2i(0, frame.getFrameHeight()-1);
			gl.glDrawPixels(imageProvider.getWidth(), imageProvider.getHeight(),
					GL.GL_BGR,
					GL.GL_UNSIGNED_BYTE,
					ByteBuffer.wrap(imageProvider.getImageData()));
			gl.glPixelTransferf(GL.GL_ALPHA_SCALE, 1f);
			gl.glEnable(GL.GL_TEXTURE_2D);
			*/

			// Blend with fresh noise.
			gl.glCallList(iframe % Npat + 1);
			gl.glBegin(GL.GL_QUAD_STRIP);
			gl.glTexCoord2f(0f,		0f);	gl.glVertex2f(0f, 0f);
			gl.glTexCoord2f(0f,		tmax);	gl.glVertex2f(0f, 1f);
			gl.glTexCoord2f(tmax,	0f);	gl.glVertex2f(1f, 0f);
			gl.glTexCoord2f(tmax,	tmax);	gl.glVertex2f(1f, 1f);
			gl.glEnd();
			gl.glDisable(GL.GL_BLEND);

			// Copy the image to texture memory.
			gl.glCopyTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGB,
					0, frame.getFrameHeight()-hakoniwa.getHeight(), hakoniwa.getWidth(), hakoniwa.getHeight(), 0);
		}

		public void reshape(GLAutoDrawable drawable,
				int x, int y, int width, int height)
		{
			gl.glViewport(0, frame.getFrameHeight()-hakoniwa.getHeight(), hakoniwa.getWidth(), hakoniwa.getHeight());
			gl.glMatrixMode(GL.GL_PROJECTION);
			gl.glLoadIdentity();
			gl.glTranslatef(-1f, -1f, 0f);
			gl.glScalef(2f, 2f, 1f);
		}

		public void displayChanged(GLAutoDrawable drawable,
				boolean modeChanged, boolean deviceChanged)
		{
		}
	}
}

package piccolo;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.nodes.PText;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolox.PFrame;
import edu.umd.cs.piccolox.nodes.PLine;
import edu.umd.cs.piccolox.nodes.PNodeCache;

import jp.digitalmuseum.mr.Matereal;
import jp.digitalmuseum.mr.entity.Entity;
import jp.digitalmuseum.mr.entity.Robot;
import jp.digitalmuseum.mr.gui.*;
import jp.digitalmuseum.mr.gui.utils.VectorFieldPainter;
import jp.digitalmuseum.mr.hakoniwa.Hakoniwa;
import jp.digitalmuseum.mr.hakoniwa.HakoniwaBox;
import jp.digitalmuseum.mr.hakoniwa.HakoniwaRobot;
import jp.digitalmuseum.mr.message.Event;
import jp.digitalmuseum.mr.message.EventListener;
import jp.digitalmuseum.mr.message.ImageUpdateEvent;
import jp.digitalmuseum.mr.resource.WheelsController;
import jp.digitalmuseum.mr.task.Push;
import jp.digitalmuseum.mr.task.Task;
import jp.digitalmuseum.mr.task.VectorFieldTask;
import jp.digitalmuseum.utils.Location;
import jp.digitalmuseum.utils.Position;
import jp.digitalmuseum.utils.ScreenPosition;

/**
 * Bring it here!
 * Test of assigning a task to a robot.
 *
 * @author Jun KATO
 */
public class BringItHere {
	private Hakoniwa hakoniwa;
	private VectorFieldTask push;
	private ScreenPosition goal = null;

	public static void main(String[] args) {
		new BringItHere();
	}

	public BringItHere() {

		// Run hakoniwa.
		hakoniwa = new Hakoniwa();
		hakoniwa.start();

		final Robot robot;
		final Entity[] entities;
		robot = new HakoniwaRobot("My robot", new Location(hakoniwa.getRealWidth()/2 - 30, hakoniwa.getRealHeight()/2, -Math.PI*3/4));
		entities = new Entity[5];
		final double x = hakoniwa.getRealWidth()/2;
		final double y = hakoniwa.getRealHeight()/2;
		final double r = 200;
		for (int i = 0; i < entities.length; i ++) {
			double theta = Math.PI*i/(entities.length-1);
			entities[i] = new HakoniwaBox(
					"Box "+i,
					x + Math.cos(theta) * r,
					y - Math.sin(theta) * r,
					20*(i+1),
					15*(i+1),
					theta);
		}

		// Show information of the robot in 200x70 rectangle.
		PPath pRectangle = new PPath(new Rectangle(0, 0, 200, 70));

		PPath pBorder = new PPath(new Line2D.Float(70, 0, 70, 70));
		pRectangle.addChild(pBorder);

		PPath pBorder2 = new PPath(new Line2D.Float(75, 25, 195, 25));
		pRectangle.addChild(pBorder2);

		PText pText = new PText();
		pText.translate(75, 5);
		pText.setConstrainWidthToTextWidth(false);
		pText.setConstrainHeightToTextHeight(false);
		pText.setText(robot.getName());
		pText.setWidth(120);
		pText.setHeight(20);
		pRectangle.addChild(pText);

		PPath pRobotPath = new PPath(robot.getShape());
		pRectangle.addChild(pRobotPath);
		pRobotPath.translate(35, 35);
		double w = pRobotPath.getWidth(), h = pRobotPath.getHeight();
		pRobotPath.scale(w > 0 && h > 0 ? (w < h ? 60 / h : 60 / w) : 1);

		PFrame pFrame = new PFrame();
		pFrame.getCanvas().getLayer().addChild(pRectangle);

		// Show status of hakoniwa.
		final DrawableFrame frame = new DrawableFrame() {
			private static final long serialVersionUID = 1L;
			private transient final VectorFieldPainter vectorFieldPainter =
					new VectorFieldPainter(hakoniwa);
			private transient final Stroke stroke =
					new BasicStroke(5);
			private transient final AlphaComposite alphaComp = AlphaComposite
					.getInstance(AlphaComposite.SRC_OVER, .3f);
			private transient final AlphaComposite alphaComp2 = AlphaComposite
					.getInstance(AlphaComposite.SRC_OVER, .7f);

			@Override public void dispose() {
				super.dispose();
				Matereal.getInstance().dispose();
			}

			@Override
			public void paint2D(Graphics2D g2) {
				final Task task = robot.getAssignedTask(WheelsController.class);
				g2.setColor(Color.white);
				g2.fillRect(0, 0, getFrameWidth(), getFrameHeight());
				final Composite comp = g2.getComposite();

				// Draw vectors.
				g2.setComposite(alphaComp);
				g2.setColor(Color.red);
				vectorFieldPainter.paint(push, g2);
				g2.setComposite(alphaComp2);
				g2.setColor(Color.black);
				g2.fillRect(0, 0, getFrameWidth(), 35);

				// Draw status.
				g2.setComposite(comp);
				g2.drawLine(0, 35, getFrameWidth(), 35);
				g2.setColor(Color.white);
				if (goal == null) {
					g2.drawString("Click to set the destination.", 10, 30);
				} else {
					if (task != null) {
						g2.drawString("Status: "+task, 10, 30);
					} else {
						g2.drawString("Status: Stopped", 10, 30);
					}
					Stroke s = g2.getStroke();
					g2.setStroke(stroke);
					g2.setColor(Color.black);
					g2.drawLine(
							goal.getX()-5,
							goal.getY()-5,
							goal.getX()+5,
							goal.getY()+5);
					g2.drawLine(
							goal.getX()-5,
							goal.getY()+5,
							goal.getX()+5,
							goal.getY()-5);
					g2.setStroke(s);
				}
				hakoniwa.drawImage(g2);
			}
		};
		frame.setResizable(false);
		frame.setFrameSize(hakoniwa.getWidth(), hakoniwa.getHeight());

		// Bring the clicked object to the location in front of the user.
		frame.getPanel().addMouseListener(new MouseAdapter() {
			public void mouseReleased(MouseEvent e) {
				int x = e.getX(), y = e.getY();
				if (goal == null) {
					goal = new ScreenPosition(x, y);
					return;
				}
				Entity entity = getClickedEntity(x, y);
				if (entity != null && entity != robot) {
					push = new Push(entity, hakoniwa.screenToReal(goal));
					if (push.assign(robot)) push.start();
				}
			}
		});

		// Repaint the window every time the image is updated.
		hakoniwa.addEventListener(new EventListener() {
			public void eventOccurred(Event e) {
				if (e instanceof ImageUpdateEvent) {
					frame.repaint();
				}
			}
		});
	}

	/**
	 * Get clicked entity.
	 * @param x
	 * @param y
	 * @return Clicked entity
	 */
	private Entity getClickedEntity(int x, int y) {
		Position p = hakoniwa.screenToReal(new ScreenPosition(x, y));
		for (Entity e : Matereal.getInstance().getEntities()) {
			if (hakoniwa.contains(e, p)) {
				return e;
			}
		}
		return null;
	}
}

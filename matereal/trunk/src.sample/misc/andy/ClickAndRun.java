package misc.andy;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import com.phybots.gui.DrawableFrame;
import com.phybots.hakoniwa.Hakoniwa;
import com.phybots.hakoniwa.HakoniwaRobot;
import com.phybots.p5.*;
import com.phybots.p5.andy.Andy;
import com.phybots.p5.andy.Entity;
import com.phybots.p5.andy.Location;
import com.phybots.p5.andy.LocationListener;
import com.phybots.p5.andy.MobileRobot;
import com.phybots.p5.andy.Position;


/**
 * Click and run!
 *
 * @author Jun Kato
 */
public class ClickAndRun {
	Hakoniwa hakoniwa;
	MobileRobot robot;

	public static void main(String[] args) {
		new ClickAndRun();
	}

	public ClickAndRun() {

		// Run hakoniwa.
		hakoniwa = new Hakoniwa(640, 480);
		hakoniwa.start();

		// Instantiate a robot.
		robot = new MobileRobot(new HakoniwaRobot("test"));

		// Make a window for showing captured image.
		final DrawableFrame frame = new DrawableFrame() {
			private static final long serialVersionUID = 1L;

			@Override public void dispose() {
				super.dispose();
				Andy.getInstance().dispose();
			}

			@Override
			public void paint2D(Graphics2D g) {
				g.setColor(Color.white);
				g.fillRect(0, 0, getFrameWidth(), getFrameHeight());
				g.setColor(Color.black);
				Position goal;
				if ((goal = robot.getGoal()) != null) {
					g.drawString("Status: "+robot.getStatus(), 10, 30);
					final int x = goal.getScreenX(), y = goal.getScreenY();
					g.drawLine(x-5, y-5, x+5, y+5);
					g.drawLine(x-5, y+5, x+5, y-5);
				}
				else {
					g.drawString("Status: Goal not specified", 10, 30);
				}
				g.drawLine(10, 35, 175, 35);
				hakoniwa.drawImage(g);
			}
		};
		frame.setResizable(false);
		frame.setFrameSize(hakoniwa.getWidth(), hakoniwa.getHeight());

		// Go to the clicked location when clicked.
		frame.getPanel().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				robot.moveTo(e.getX(), e.getY());
			}
		});

		// Repaint the window periodically.
		robot.addLocationListener(new LocationListener() {

			public void locationUpdated(Entity entity, Location location) {
				frame.repaint();
			}
		});
	}
}
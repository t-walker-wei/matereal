package matereal;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JFrame;
import javax.swing.JPanel;

import jp.digitalmuseum.mr.entity.Noopy;
import jp.digitalmuseum.mr.entity.Robot;
import jp.digitalmuseum.mr.gui.DisposeOnCloseFrame;

public class DoAction {
	Robot robot;

	public static void main(String[] args) {
		new DoAction();
	}

	public DoAction() {
		robot = new Noopy("My robot");
		JFrame frame = new DisposeOnCloseFrame(new MyPanel(this));
		frame.setVisible(true);
	}

	public void action(int repeat) {
		for (int i = 0; i < repeat; i ++) {



		}
	}

	private static class MyPanel extends JPanel implements MouseListener {
		private static final long serialVersionUID = 1L;
		private DoAction doAction;
		public MyPanel(DoAction doAction) {
			this.doAction = doAction;
			addMouseListener(this);
		}

		public void mouseClicked(MouseEvent e) {
			doAction.action(5);
		}

		public void mouseEntered(MouseEvent e) {
		}

		public void mouseExited(MouseEvent e) {
		}

		public void mousePressed(MouseEvent e) {
		}

		public void mouseReleased(MouseEvent e) {
		}
	}
}

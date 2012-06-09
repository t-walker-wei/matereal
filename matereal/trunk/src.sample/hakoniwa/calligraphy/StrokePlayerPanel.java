package hakoniwa.calligraphy;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JCheckBox;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


import javax.swing.JToolBar;
import javax.swing.JButton;

import com.phybots.entity.Robot;
import com.phybots.gui.ImageProviderPanel;
import com.phybots.resource.WheelsController;
import com.phybots.service.CoordProvider;
import com.phybots.task.DrawPath;
import com.phybots.task.Move;
import com.phybots.task.Rotate;
import com.phybots.task.Task;
import com.phybots.utils.Position;
import com.phybots.utils.ScreenPosition;
import com.phybots.workflow.Action;
import com.phybots.workflow.Fork;
import com.phybots.workflow.Join;
import com.phybots.workflow.Transition;
import com.phybots.workflow.Workflow;

public class StrokePlayerPanel extends JPanel implements WizardComponent {

	private static final long serialVersionUID = 1L;
	private JPanel strokePlayerPanel = null;
	private JCheckBox jStrokeCheckBox = null;
	private transient CoordProvider imageProvider;
	private transient PathsProvider pathProvider;
	private transient Workflow workflow;
	private transient Robot[] robots;
	private transient Stroke stroke;  //  @jve:decl-index=0:
	private transient List<Path> paths;
	private transient boolean isShowingIdealPaths = true;

	/**
	 * This is the default constructor
	 */
	public StrokePlayerPanel(CoordProvider coordProvider, Workflow workflow) {
		super();
		this.imageProvider = coordProvider;
		this.workflow = workflow;
		initialize();
	}

	public void setPathsProvider(PathsProvider pathProvider) {
		this.pathProvider = pathProvider;
	}

	public PathsProvider getPathsProvider() {
		return pathProvider;
	}

	public void setRobots(Robot[] robots) {
		this.robots = robots;
	}

	public void setStroke(Stroke stroke) {
		this.stroke = stroke;
	}

	public Stroke getStroke() {
		return stroke;
	}

	public void setShowingIdealPaths(boolean isShowingIdealPaths) {
		this.isShowingIdealPaths = isShowingIdealPaths;
		getJStrokeCheckBox().setSelected(isShowingIdealPaths);
	}

	public boolean isShowingIdealPaths() {
		return isShowingIdealPaths;
	}

	public void onLoad() {
		play();
	}

	public void onUnload() {
		// Do nothing.
	}

	private void play() {
		synchronized (pathProvider) {
			paths = pathProvider.getPaths();
			int forks = robots.length > paths.size() ? paths.size() : robots.length;
			if (forks == 0) {
				return;
			}

			// Construct a workflow graph.
			Action[] inits = new Action[forks];
			Action[] tails = new Action[forks];
			int offset = 0;
			int base = paths.size() / forks;
			int rest = paths.size() - forks * base;
			for (int r = 0; r < forks; r ++) {
				int my = base + (r < rest ? 1 : 0);
				Action tail = null;
				for (int i = 0; i < my; i ++) {

					// Get path.
					Path path = paths.get(offset + i);
					if (path.size() <= 1) {
						continue;
					}

					// Convert to the real world coordinates.
					List<Position> p = new ArrayList<Position>();
					for (ScreenPosition sp : path) {
						p.add(imageProvider.screenToReal(sp));
					}

					// Add nodes and transitions for the robot to the graph.
					Action head = new Action(robots[r], new Move(p.get(0)));
					Action a = new Action(robots[r], new Rotate(p.get(1)));
					Action b = new Action(robots[r], new DrawPath(p));
					workflow.add(head);
					workflow.add(a);
					workflow.add(b);
					if (tail == null) {
						inits[r] = head;
					} else {
						workflow.addTransition(new Transition(tail, head));
					}
					workflow.addTransition(new Transition(head, a));
					workflow.addTransition(new Transition(a, b));
					tail = b;
				}
				tails[r] = tail;
				offset += my;
			}
			Fork fork = new Fork(inits);
			Join join = new Join(tails);
			workflow.add(fork, join);
			workflow.addTransition(new Transition(fork, join));
			workflow.setInitialNode(fork);
			workflow.start();
		}
	}

	private void saveImage() {
		final JFileChooser filechooser = new JFileChooser();

		int selected = filechooser.showSaveDialog(getParent());
		if (selected == JFileChooser.APPROVE_OPTION) {
			File file = filechooser.getSelectedFile();
			BufferedImage image = new BufferedImage(
					imageProvider.getWidth(),
					imageProvider.getHeight(),
					BufferedImage.TYPE_INT_ARGB);
			final Graphics2D g = image.createGraphics();
			imageProvider.drawImage(g);
			paintStatus(g);
			g.dispose();
			try {
				ImageIO.write(image, "PNG", file);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}

	private JToolBar jToolBar = null;
	private JButton jSaveImageButton = null;

	/**
	 * This method initializes this
	 *
	 * @return void
	 */
	private void initialize() {
		this.setSize(300, 200);
		this.setLayout(new BorderLayout());
		this.add(getStrokePlayerPanel(), BorderLayout.CENTER);
		this.add(getJStrokeCheckBox(), BorderLayout.NORTH);
		this.add(getJToolBar(), BorderLayout.SOUTH);
	}

	/**
	 * This method initializes strokePlayerPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getStrokePlayerPanel() {
		if (strokePlayerPanel == null) {
			strokePlayerPanel = new ImageProviderPanel(imageProvider) {
				private static final long serialVersionUID = 1L;

				@Override
				public void paintComponent(Graphics g) {
					super.paintComponent(g);
					Graphics2D g2 = (Graphics2D)g;
					g2.translate(getOffsetX(), getOffsetY());
					paintStatus(g2);
				}
			};
			strokePlayerPanel.addMouseListener(new MouseAdapter() {

				@Override
				public void mouseClicked(MouseEvent e) {
					saveImage();
				}
			});
		}
		return strokePlayerPanel;
	}

	private void paintStatus(Graphics2D g) {
		g.setRenderingHint(
				RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		// Paint path.
		if (isShowingIdealPaths() && paths != null) {
			final Stroke originalStroke = g.getStroke();
			if (stroke != null) {
				g.setStroke(stroke);
			}
			g.setColor(Color.black);
			for (Path path : paths) {
				path.paint(g);
			}
			g.setStroke(originalStroke);
		}

		// Paint status of the robot.
		int y = 30;
		for (Robot robot : robots) {
			Task assignedTask = robot.getAssignedTask(WheelsController.class);
			if (assignedTask != null) {
				g.drawString(robot.getName() + ": " + assignedTask.toString(), 5, y);
				y += 16;
			}
		}
	}

	/**
	 * This method initializes jStrokeCheckBox
	 *
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getJStrokeCheckBox() {
		if (jStrokeCheckBox == null) {
			jStrokeCheckBox = new JCheckBox();
			jStrokeCheckBox.setText("Show ideal paths (original strokes)");
			jStrokeCheckBox.setSelected(isShowingIdealPaths);
			jStrokeCheckBox.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					setShowingIdealPaths(jStrokeCheckBox.isSelected());
				}
			});
		}
		return jStrokeCheckBox;
	}

	/**
	 * This method initializes jToolBar
	 *
	 * @return javax.swing.JToolBar
	 */
	private JToolBar getJToolBar() {
		if (jToolBar == null) {
			jToolBar = new JToolBar();
			jToolBar.add(getJSaveImageButton());
		}
		return jToolBar;
	}

	/**
	 * This method initializes jSaveImageButton
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getJSaveImageButton() {
		if (jSaveImageButton == null) {
			jSaveImageButton = new JButton();
			jSaveImageButton.setText("Capture current status");
			jSaveImageButton.setToolTipText("Capture current status and save as an image file.");
			jSaveImageButton.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					saveImage();
				}
			});
		}
		return jSaveImageButton;
	}

}

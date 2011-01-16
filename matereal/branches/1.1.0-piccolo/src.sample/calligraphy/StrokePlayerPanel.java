package calligraphy;

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

import jp.digitalmuseum.mr.activity.Action;
import jp.digitalmuseum.mr.activity.ActivityDiagram;
import jp.digitalmuseum.mr.activity.Fork;
import jp.digitalmuseum.mr.activity.Transition;
import jp.digitalmuseum.mr.entity.Robot;
import jp.digitalmuseum.mr.gui.DisposeOnCloseFrame;
import jp.digitalmuseum.mr.gui.ImageProviderPanel;
import jp.digitalmuseum.mr.message.ActivityEvent;
import jp.digitalmuseum.mr.message.Event;
import jp.digitalmuseum.mr.message.EventListener;
import jp.digitalmuseum.mr.message.ActivityEvent.STATUS;
import jp.digitalmuseum.mr.resource.WheelsController;
import jp.digitalmuseum.mr.service.CoordProvider;
import jp.digitalmuseum.mr.task.DrawPath;
import jp.digitalmuseum.mr.task.Move;
import jp.digitalmuseum.mr.task.Rotate;
import jp.digitalmuseum.mr.task.Task;
import jp.digitalmuseum.utils.Position;
import jp.digitalmuseum.utils.ScreenPosition;

import javax.swing.JToolBar;
import javax.swing.JButton;

public class StrokePlayerPanel extends JPanel implements WizardComponent {

	private static final long serialVersionUID = 1L;
	private JPanel strokePlayerPanel = null;
	private JCheckBox jStrokeCheckBox = null;
	private transient CoordProvider imageProvider;
	private transient PathsProvider pathProvider;
	private transient Robot[] robots;
	private transient Stroke stroke;  //  @jve:decl-index=0:
	private transient List<Path> paths;
	private transient boolean isShowingIdealPaths = true;

	/**
	 * This is the default constructor
	 */
	public StrokePlayerPanel(CoordProvider coordProvider) {
		super();
		this.imageProvider = coordProvider;
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

			// Construct an activity diagram.
			final ActivityDiagram ad = new ActivityDiagram();
			Action[] inits = new Action[forks];
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

					// Add nodes and transitions for the robot to the diagram.
					Action head = new Action(robots[r], new Move(p.get(0)));
					Action a = new Action(robots[r], new Rotate(p.get(1)));
					Action b = new Action(robots[r], new DrawPath(p));
					ad.add(head);
					ad.add(a);
					ad.add(b);
					if (i == 0) {
						inits[r] = head;
					} else {
						ad.addTransition(new Transition(tail, head));
					}
					ad.addTransition(new Transition(head, a));
					ad.addTransition(new Transition(a, b));
					tail = b;
				}
				offset += my;
			}
			Fork fork = new Fork(inits);
			ad.add(fork);
			ad.setInitialNode(fork);

			// Show the activity diagram.
			final DisposeOnCloseFrame frame = new DisposeOnCloseFrame(ad.newActivityDiagramCanvas());
			frame.setTitle("Activity viewer");

			// Set event listener to notice the completion of the tasks.
			ad.addEventListener(new EventListener() {

				public void eventOccurred(Event e) {
					if (e instanceof ActivityEvent) {
						if (e.getSource() == ad &&
								((ActivityEvent) e).getStatus() == STATUS.LEFT) {
							frame.setTitle("Activity viewer : all tasks were finished.");
						}
					}
				}
			});

			// Start the activity diagram.
			ad.start();
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

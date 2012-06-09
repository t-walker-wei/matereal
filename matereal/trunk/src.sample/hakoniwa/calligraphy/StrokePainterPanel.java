package hakoniwa.calligraphy;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.JButton;

import com.phybots.gui.ImageProviderPanel;
import com.phybots.service.ImageProvider;


public class StrokePainterPanel extends JPanel implements PathsProvider {

	private static final long serialVersionUID = 1L;
	private static final int RESAMPLE_PIXELS = 20;
	private ImageProviderPanel strokePainterPanel = null;
	private JToolBar jToolBar = null;
	private JButton jClearButton = null;
	private transient ImageProvider imageProvider;
	private transient Path currentPath;
	private transient ArrayList<Path> paths;
	private transient Stroke stroke;

	/**
	 * This is the default constructor
	 */
	public StrokePainterPanel(ImageProvider imageProvider) {
		super();
		this.imageProvider = imageProvider;
		paths = new ArrayList<Path>();
		initialize();
	}

	public void setStroke(Stroke stroke) {
		this.stroke = stroke;
	}

	public Stroke getStroke() {
		return stroke;
	}

	public synchronized List<Path> getPaths() {
		return new ArrayList<Path>(paths);
	}

	public synchronized void clearPaths() {
		paths.clear();
	}

	/**
	 * This method initializes this
	 *
	 * @return void
	 */
	private void initialize() {
		this.setSize(300, 200);
		this.setLayout(new BorderLayout());
		this.add(getJToolBar(), BorderLayout.NORTH);
		this.add(getStrokePainterPanel(), BorderLayout.CENTER);
	}

	/**
	 * This method initializes strokePanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getStrokePainterPanel() {
		if (strokePainterPanel == null) {
			strokePainterPanel = new ImageProviderPanel(imageProvider) {
				private static final long serialVersionUID = 1L;

				@Override
				public void paintComponent(Graphics g) {
					super.paintComponent(g);
					Graphics2D g2 = (Graphics2D)g;
					g2.setRenderingHint(
							RenderingHints.KEY_ANTIALIASING,
							RenderingHints.VALUE_ANTIALIAS_ON);
					g2.translate(getOffsetX(), getOffsetY());

					// Paint path.
					if (stroke != null) {
						g2.setStroke(stroke);
					}
					g2.setColor(Color.black);
					for (Path path : paths) {
						path.paint(g2);
					}
					if (currentPath != null) {
						g2.setColor(Color.red);
						currentPath.paint(g2);
					}
				}
			};

			strokePainterPanel.addMouseListener(new MouseListener() {
				private boolean in = false;
				public void mousePressed(MouseEvent e) {
					if (in) {
						currentPath = new Path();
					}
				}
				public void mouseReleased(MouseEvent e) {
					if (currentPath.size() > 0) {
						currentPath.resample(RESAMPLE_PIXELS);
						if (currentPath.size() > 0) {
							synchronized (strokePainterPanel) {
								paths.add(currentPath);
							}
							currentPath = null;
						}
						repaint();
					}
				}
				public void mouseEntered(MouseEvent e) {
					in = true;
				}
				public void mouseExited(MouseEvent e) {
					in = false;
				}
				public void mouseClicked(MouseEvent e) {
				}
			});

			strokePainterPanel.addMouseMotionListener(new MouseMotionListener(){
				public void mouseDragged(MouseEvent e) {
					currentPath.add(
							strokePainterPanel.getScreenToImageX(e.getX()),
							strokePainterPanel.getScreenToImageY(e.getY()));
					repaint();
				}
				public void mouseMoved(MouseEvent e) {
				}
			});
		}
		return strokePainterPanel;
	}

	/**
	 * This method initializes jToolBar
	 *
	 * @return javax.swing.JToolBar
	 */
	private JToolBar getJToolBar() {
		if (jToolBar == null) {
			jToolBar = new JToolBar();
			jToolBar.add(getJClearButton());
		}
		return jToolBar;
	}

	/**
	 * This method initializes jClearButton
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getJClearButton() {
		if (jClearButton == null) {
			jClearButton = new JButton();
			jClearButton.setText("Clear");
			jClearButton.setToolTipText("Clear all strokes.");
			jClearButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					clearPaths();
				}
			});
		}
		return jClearButton;
	}

}

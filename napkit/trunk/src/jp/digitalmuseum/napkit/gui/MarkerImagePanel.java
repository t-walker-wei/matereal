package jp.digitalmuseum.napkit.gui;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import jp.digitalmuseum.napkit.NapMarker;

public class MarkerImagePanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private NapMarker marker;
	private BufferedImage image;
	private int zoom = 0;

	public MarkerImagePanel() {
		super();
	}

	public void setMarker(NapMarker marker) {
		this.marker = marker;
		zoom = 0;
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (marker == null) {
			return;
		}

		int w = getWidth() - 19;
		int h = getHeight() - 10;
		if (w < 64 || h < 16) {
			return;
		}

		int z = w / h < 4 ? w / 64 : h / 16;
		if (zoom != z) {
			image = new BufferedImage(z * 64 + 9, z * 16, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g2 = image.createGraphics();
			for (int direction = 0; direction < 4; direction ++) {
				Image image = marker.getImage(direction);
				g2.drawImage(image, direction * (z * 16 + 3), 0, z * 16, z * 16, null);
			}
			g2.dispose();
			zoom = z;
		}
		int ox = 5 + (w - (z * 64 + 9)) / 2;
		int oy = 5 + (h - z * 16) / 2;
		g.drawRect(ox - 3, oy - 3, z * 64 + 9 + 6, z * 16 + 6);
		g.drawImage(image, ox, oy, null);
	}
}

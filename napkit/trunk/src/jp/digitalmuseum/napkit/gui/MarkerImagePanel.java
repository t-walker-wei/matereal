/*
 * PROJECT: napkit at http://mr.digitalmuseum.jp/
 * ----------------------------------------------------------------------------
 *
 * This file is part of NyARToolkit Application Toolkit.
 *
 * NyARToolkit Application Toolkit, or simply "napkit",
 * is a simple wrapper library for NyARToolkit.
 *
 * ----------------------------------------------------------------------------
 *
 * License version: GPL 3.0
 *
 * napkit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * napkit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with napkit. If not, see <http://www.gnu.org/licenses/>.
 */
package jp.digitalmuseum.napkit.gui;

import java.awt.Color;
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
	private int currentZoom = 0;

	public MarkerImagePanel() {
		super();
	}

	public void setMarker(NapMarker marker) {
		this.marker = marker;
		currentZoom = 0;
		repaint();
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (marker == null) {
			return;
		}
		int spaceWidth = getWidth() - 19;
		int spaceHeight = getHeight() - 10;
		int zoom;
		if (spaceWidth < 128 || spaceHeight < 32) {
			if (spaceHeight <= 0) {
				return;
			}
			zoom = 1;
		} else {
			zoom = spaceWidth / spaceHeight < 4 ? spaceWidth / 128 : spaceHeight / 32;
		}
		if (currentZoom != zoom) {
			image = new BufferedImage(zoom * 128 + 9, zoom * 32, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g2 = image.createGraphics();
			g2.setColor(Color.black);
			for (int direction = 0; direction < 4; direction ++) {
				Image image = marker.getImage(direction);
				g2.fillRect(direction * (zoom * 32 + 3), 0, zoom * 32, zoom * 32);
				g2.drawImage(image, direction * (zoom * 32 + 3) + zoom * 8, zoom * 8, zoom * 16, zoom * 16, null);
			}
			g2.dispose();
			currentZoom = zoom;
		}
		int offsetX = 5;
		if (spaceWidth < 128 || spaceHeight < 32) {
			int imageWidth, imageHeight;
			if (spaceWidth / spaceHeight < 4) {
				imageWidth = spaceWidth + 9;
				imageHeight = 32 * (spaceWidth + 9) / 137;
			} else {
				imageWidth = (32 * 4 + 9) * spaceHeight / 32;
				imageHeight = spaceHeight;
			}
			int offsetY = 5 + (spaceHeight - imageHeight) / 2;
			g.drawImage(image, offsetX, offsetY, imageWidth, imageHeight, null);
		} else {
			int offsetY = 5 + (spaceHeight - zoom * 32) / 2;
			g.drawRect(offsetX - 3, offsetY - 3, zoom * 32 * 4 + 9 + 5, zoom * 32 + 5);
			g.drawImage(image, offsetX, offsetY, null);
		}
	}
}

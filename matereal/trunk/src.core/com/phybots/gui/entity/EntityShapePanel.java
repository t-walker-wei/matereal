/*
 * PROJECT: Phybots at http://phybots.com/
 * ----------------------------------------------------------------------------
 *
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Phybots.
 *
 * The Initial Developer of the Original Code is Jun Kato.
 * Portions created by the Initial Developer are
 * Copyright (C) 2009 Jun Kato. All Rights Reserved.
 *
 * Contributor(s): Jun Kato
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either of the GNU General Public License Version 2 or later (the "GPL"),
 * or the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 */
package com.phybots.gui.entity;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import javax.swing.JPanel;

import com.phybots.entity.Entity;


public class EntityShapePanel extends JPanel {
	private static final long serialVersionUID = -4091511207180695499L;
	private Shape shape;
	private double zoom;

	public EntityShapePanel(Entity entity) {
		shape = entity.getShape();
		initialize();
	}

	private void initialize() {
		if (shape == null) {
			zoom = 1;
		} else {
			Rectangle2D bounds = shape.getBounds2D();
			double w = bounds.getWidth();
			double h = bounds.getHeight();
			zoom = w > 0 && h > 0 ? (w < h ? 60 / h : 60 / w) : 1;
		}
	}

	@Override
	public void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		AffineTransform af = g2.getTransform();
		int x = (getWidth() - 70) / 2;
		int y = (getHeight() - 70) / 2;
		g2.setColor(Color.white);
		g2.fillRect(x + 1, y + 1, 68, 68);
		g2.setColor(getForeground());
		g2.drawRect(x, y, 69, 69);
		if (shape != null) {
			g2.translate(x + 35, y + 35);
			g2.scale(zoom, zoom);
			g2.draw(shape);
		}
		g2.setTransform(af);
	}
}

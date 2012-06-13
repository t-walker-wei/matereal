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
package com.phybots.gui.utils;

import java.awt.Graphics2D;

import com.phybots.Phybots;
import com.phybots.service.CoordProvider;
import com.phybots.task.VectorFieldTask;
import com.phybots.utils.Position;
import com.phybots.utils.ScreenPosition;


public class VectorFieldPainter {
	private double length = 20;
	private double arrowLength = 20;
	public int nx = 30;
	private CoordProvider coordProvider;
	private VectorFieldTask vectorTask;
	private final Position p = new Position();
	private final Position p2 = new Position();
	private final ScreenPosition sp = new ScreenPosition();
	private final ScreenPosition sp2 = new ScreenPosition();

	public VectorFieldPainter() {
		findCoordProvider();
	}

	public VectorFieldPainter(double length) {
		this();
		this.length = length;
	}

	public VectorFieldPainter(CoordProvider coordProvider) {
		this.coordProvider = coordProvider;
	}

	public void setVectorLength(double length) {
		this.length = length;
	}

	public double getLength() {
		return length;
	}

	public void setCoordProvider(CoordProvider coordProvider) {
		this.coordProvider = coordProvider;
	}

	public CoordProvider getCoordProvider() {
		return coordProvider;
	}

	private void findCoordProvider() {
		coordProvider = Phybots.getInstance()
				.lookForService(CoordProvider.class);
	}

	public void setVectorTask(VectorFieldTask vectorTask) {
		this.vectorTask = vectorTask;
	}

	public VectorFieldTask getVectorTask() {
		return vectorTask;
	}

	public void paint(Graphics2D g) {
		paint(coordProvider, vectorTask, g);
	}

	public void paint(VectorFieldTask vectorTask, Graphics2D g) {
		paint(coordProvider, vectorTask, g);
	}

	public void paint(CoordProvider coordProvider, VectorFieldTask vectorTask, Graphics2D g) {
		final double d = coordProvider.getRealWidth()/(nx);
		final int ny = (int) (coordProvider.getRealHeight()/d)+1;
		for (int i = 0; i <= nx; i ++) {
			p.setX(d*i);
			for (int j = 0; j <= ny; j ++) {
				p.setY(d*j);
				if (vectorTask != null) {
					vectorTask.getVectorOut(p, p2);
					double l = p2.getNorm()/length;
					if (l > 0) {
						p2.set(p.getX()+p2.getX()/l, p.getY()+p2.getY()/l);
					} else p2.set(p);
				} else p2.set(p);
				coordProvider.realToScreenOut(p, sp);
				coordProvider.realToScreenOut(p2, sp2);
				g.drawLine(sp.getX(), sp.getY(), sp2.getX(), sp2.getY());
				if (p.getX() == p2.getX() && p.getY() == p2.getY()) {
					continue;
				}
				final double rot = sp.getRelativeDirection(sp2);
				sp.set(
						sp2.getX()+(int)(Math.cos(rot-Math.PI*5/6)*arrowLength/3),
						sp2.getY()+(int)(Math.sin(rot-Math.PI*5/6)*arrowLength/3));
				g.drawLine(sp.getX(), sp.getY(), sp2.getX(), sp2.getY());
				sp.set(
						sp2.getX()+(int)(Math.cos(rot+Math.PI*5/6)*arrowLength/3),
						sp2.getY()+(int)(Math.sin(rot+Math.PI*5/6)*arrowLength/3));
				g.drawLine(sp.getX(), sp.getY(), sp2.getX(), sp2.getY());
			}
		}
	}
}

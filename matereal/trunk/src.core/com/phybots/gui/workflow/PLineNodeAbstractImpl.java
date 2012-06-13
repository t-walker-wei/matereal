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
package com.phybots.gui.workflow;

import java.awt.Color;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;

import com.phybots.gui.workflow.layout.Layout.Coordinate;
import com.phybots.gui.workflow.layout.Layout.Line;
import com.phybots.workflow.Fork;
import com.phybots.workflow.Join;
import com.phybots.workflow.Node;


import edu.umd.cs.piccolo.activities.PActivity;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.util.PUtil;

public abstract class PLineNodeAbstractImpl extends PPath implements PStrokeColorActivity.Target {
	private static final long serialVersionUID = -8586619092371481425L;
	private Color color;

	public PLineNodeAbstractImpl() {
		setPickable(false);
	}

	public Color getStrokeColor() {
		return color;
	}

	public void setStrokeColor(Color color) {
		setStrokePaint(color);
		this.color = color;
	}

	protected abstract Color getDefaultStrokeColor();

	public abstract Node getSource();

	public abstract Node getDestination();

	PActivity setLine(Line line) {
		setStrokeColor(WorkflowViewCanvas.getBackgroundColor());
		Coordinate[] coords = line.coordinates;
		int p = WorkflowViewCanvas.getPadding();
		int mx = WorkflowViewCanvas.getMarginX();
		int my = WorkflowViewCanvas.getMarginY();
		int aw = PNodeAbstractImpl.getAreaWidth();
		int ah = PNodeAbstractImpl.getAreaHeight();
		int cw = PControlNode.getAreaWidth();
		int w = mx + aw, h = my + ah;
		Point2D[] points = new Point2D[2 + (coords.length - 2 > 0 ? 2 : 0)];

		// Draw lines.
		if (getSource() instanceof Join) {
			points[0] = new Point2D.Float(
					coords[0].y * w + cw + p,
					coords[0].x * h + ah/2 + p);
		} else {
			points[0] = new Point2D.Float(
					coords[0].y * w + aw + p,
					coords[0].x * h + ah/2 + p);
		}
		if (getDestination() instanceof Fork) {
			points[points.length - 1] = new Point2D.Float(
					coords[coords.length - 1].y * w + w - cw + p,
					coords[coords.length - 1].x * h + ah/2 + p);
		} else {
			points[points.length - 1] = new Point2D.Float(
					coords[coords.length - 1].y * w + p,
					coords[coords.length - 1].x * h + ah/2 + p);
		}
		if (coords.length == 3) {
			points[1] = new Point2D.Float(
					coords[1].y * w + p,
					coords[1].x * h + ah/2 + p);
			points[2] = new Point2D.Float(
					coords[1].y * w + aw + p,
					coords[1].x * h + ah/2 + p);
		} else if (coords.length == 4) {
			points[1] = new Point2D.Float(
					coords[1].y * w + p,
					coords[1].x * h + ah/2 + p);
			points[2] = new Point2D.Float(
					coords[2].y * w + aw + p,
					coords[2].x * h + ah/2 + p);
		}
		if (line.isReversed) {
			for (int i = 0; i < points.length / 2; i++) {
				Point2D tmp = points[i];
				points[i] = points[points.length - i - 1];
				points[points.length - i - 1] = tmp;
			}
		}

		setLine(points);
		return new PStrokeColorActivity(
				200, PUtil.DEFAULT_ACTIVITY_STEP_RATE, this, getDefaultStrokeColor());
	}

	void setLine(Point2D[] points) {
		GeneralPath path = new GeneralPath();
		for (int i = 0; i < points.length; i ++) {
			Point2D p = points[i];
			if (i == 0) {
				path.moveTo(p.getX(), p.getY());
			} else {
				path.lineTo(p.getX(), p.getY());
			}
		}

		// Arrow settings.
		int b = 7;
		double theta = Math.toRadians(12);
		Point2D start = points[points.length - 2];
		Point2D end = points[points.length - 1];
		double alpha = Math.atan2(
				end.getY() - start.getY(),
				end.getX() - start.getX());
		double dx1 = b * Math.cos(alpha + theta);
		double dy1 = b * Math.sin(alpha + theta);
		double dx2 = b * Math.cos(alpha - theta);
		double dy2 = b * Math.sin(alpha - theta);
		path.lineTo(end.getX() - dx1, end.getY() - dy1);
	    path.moveTo(end.getX(), end.getY());
	    path.lineTo(end.getX() - dx2, end.getY() - dy2);
		setPathTo(path);
	}
}

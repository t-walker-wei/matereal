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

import java.awt.BasicStroke;
import java.awt.Color;

import com.phybots.gui.workflow.layout.Layout.Coordinate;
import com.phybots.workflow.Node;


import edu.umd.cs.piccolo.activities.PActivity;
import edu.umd.cs.piccolo.nodes.PPath;

public abstract class PNodeAbstractImpl extends PPath {
	private static final long serialVersionUID = 3592199380497357141L;
	private Node node;
	private static final int width = 200;
	private static final int height = 70;

	public static int getAreaWidth() {
		return width;
	}

	public static int getAreaHeight() {
		return height;
	}

	public PNodeAbstractImpl(Node node) {
		this.node = node;
		setPaint(Color.white);
		setStrokePaint(Color.black);
	}

	void setAsInitialNode() {
		setStroke(new BasicStroke(2f));
	}

	PActivity setPosition(Coordinate coord) {
		int p = WorkflowViewCanvas.getPadding();
		int w = WorkflowViewCanvas.getMarginX() + getAreaWidth();
		int h = WorkflowViewCanvas.getMarginY() + getAreaHeight();
		return animateToPositionScaleRotation(coord.y * w + p, coord.x * h + p, 1.0f, 0.0f, 200);
	}

	Node getNode() {
		return node;
	}

	public void onEnter() {
		setStrokePaint(Color.red);
		repaint();
	}

	public void onLeave() {
		setStrokePaint(Color.black);
		repaint();
	}
}

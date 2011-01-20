/*
 * PROJECT: matereal at http://mr.digitalmuseum.jp/
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
 * The Original Code is matereal.
 *
 * The Initial Developer of the Original Code is Jun KATO.
 * Portions created by the Initial Developer are
 * Copyright (C) 2009 Jun KATO. All Rights Reserved.
 *
 * Contributor(s): Jun KATO
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
package jp.digitalmuseum.mr.gui.activity;

import java.awt.Color;
import java.awt.geom.Point2D;

import edu.umd.cs.piccolo.nodes.PText;

import jp.digitalmuseum.mr.activity.Node;
import jp.digitalmuseum.mr.activity.TimeoutTransition;
import jp.digitalmuseum.mr.activity.Transition;

public class PTransitionLineNode extends PLineNodeAbstractImpl {
	private static final long serialVersionUID = 5091941901786318920L;
	private Transition transition;
	private PText text;
	private static Color color = new Color(100, 100, 100);

	public PTransitionLineNode(Transition transition, PNodeAbstractImpl pSourceNode, PNodeAbstractImpl pDestinationNode) {
		super(pSourceNode, pDestinationNode);
		this.transition = transition;
		setStrokePaint(color);
		if (transition instanceof TimeoutTransition) {
			long timeout = ((TimeoutTransition) transition).getTimeout();
			text = new PText(String.format("%.1fs", (float)timeout/1000));
			text.setFont(text.getFont().deriveFont(10f));
			text.setConstrainWidthToTextWidth(true);
			text.setPaint(Color.white);
			addChild(text);
		} else {
			text = null;
		}
	}

	public Transition getTransition() {
		return transition;
	}

	public Node getSource() {
		return transition.getSource();
	}

	public Node getDestination() {
		return transition.getDestination();
	}

	@Override
	protected void setLine(Point2D start, Point2D end) {
		super.setLine(start, end);
		if (text != null) {
			text.setOffset((start.getX()+end.getX()-text.getWidth())/2, (start.getY()+end.getY())/2+5);
		}
	}
}

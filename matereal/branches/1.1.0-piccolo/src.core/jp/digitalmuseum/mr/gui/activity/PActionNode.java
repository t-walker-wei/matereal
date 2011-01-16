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

import java.awt.Rectangle;
import java.awt.geom.Line2D;

import jp.digitalmuseum.mr.activity.Action;
import jp.digitalmuseum.mr.entity.Robot;

import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.nodes.PText;

public class PActionNode extends PNodeAbstractImpl {
	private static final long serialVersionUID = 2317496617529842863L;
	private Action action;

	public PActionNode(Action action) {
		super(new Rectangle(0, 0, 200, 70));
		this.action = action;
		Robot robot = action.getRobot();

		PPath pBorder = new PPath(new Line2D.Float(70, 0, 70, 70));
		addChild(pBorder);

		PPath pBorder2 = new PPath(new Line2D.Float(75, 25, 195, 25));
		addChild(pBorder2);

		PText pText = new PText();
		pText.translate(75, 5);
		pText.setConstrainWidthToTextWidth(false);
		pText.setConstrainHeightToTextHeight(false);
		pText.setText(robot.getName());
		pText.setWidth(120);
		pText.setHeight(20);
		addChild(pText);

		PPath pRobotPath = new PPath(robot.getShape());
		addChild(pRobotPath);
		pRobotPath.translate(35, 35);
		double w = pRobotPath.getWidth(), h = pRobotPath.getHeight();
		pRobotPath.scale(w > 0 && h > 0 ? (w < h ? 60 / h : 60 / w) : 1);
	}

	public Action getAction() {
		return action;
	}
}

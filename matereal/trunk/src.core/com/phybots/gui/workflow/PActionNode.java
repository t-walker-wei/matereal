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
import java.awt.Rectangle;
import java.awt.geom.Line2D;

import com.phybots.entity.Robot;
import com.phybots.message.Event;
import com.phybots.message.EventListener;
import com.phybots.message.ServiceEvent;
import com.phybots.message.ServiceStatus;
import com.phybots.message.ServiceUpdateEvent;
import com.phybots.workflow.Action;


import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.nodes.PText;

public class PActionNode extends PNodeAbstractImpl {
	private static final long serialVersionUID = 2317496617529842863L;

	public PActionNode(Action action) {
		super(action);
		Robot robot = action.getRobot();
		action.getTask().addEventListener(new EventListener() {
			public void eventOccurred(Event e) {
				if (e instanceof ServiceEvent) {
					ServiceEvent se = (ServiceEvent) e;
					if (se.getStatus() == ServiceStatus.DISPOSED) {
						se.getSource().removeEventListener(this);
					}
				} else if (e instanceof ServiceUpdateEvent) {
					PActionNode.this.repaint();
				}
			}
		});

		setPathTo(new Rectangle(0, 0, 200, 70));

		PPath pBorder = new PPath(new Line2D.Float(70, 0, 70, 70));
		pBorder.setPickable(false);
		addChild(pBorder);

		PPath pBorder2 = new PPath(new Line2D.Float(75, 25, 195, 25));
		pBorder2.setPickable(false);
		addChild(pBorder2);

		PText pRobotNameText = new PText();
		pRobotNameText.translate(75, 5);
		pRobotNameText.setConstrainWidthToTextWidth(false);
		pRobotNameText.setText(robot.getName());
		pRobotNameText.setWidth(120);
		pRobotNameText.setPickable(false);
		addChild(pRobotNameText);

		PText pTaskText = new PText();
		pTaskText.translate(75, 30);
		pTaskText.setConstrainWidthToTextWidth(false);
		pTaskText.setText(action.getTask().toString());
		pTaskText.setWidth(120);
		pTaskText.setPickable(false);
		addChild(pTaskText);

		PPath pRobotPath = new PPath(robot.getShape());
		pRobotPath.translate(35, 35);
		double w = pRobotPath.getWidth(), h = pRobotPath.getHeight();
		pRobotPath.scale(w > 0 && h > 0 ? (w < h ? 60 / h : 60 / w) : 1);
		pRobotPath.setPickable(false);
		pRobotPath.setStrokePaint(Color.lightGray);
		addChild(pRobotPath);

		PText pRobotTypeNameText = new PText();
		pRobotTypeNameText.translate(5, 5);
		pRobotTypeNameText.setConstrainWidthToTextWidth(false);
		pRobotTypeNameText.setText(robot.getTypeName());
		pRobotTypeNameText.setWidth(60);
		pRobotTypeNameText.setPickable(false);
		addChild(pRobotTypeNameText);
	}

	public Action getNode() {
		return (Action) super.getNode();
	}
}

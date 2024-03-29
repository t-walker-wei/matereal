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
package jp.digitalmuseum.mr.gui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import jp.digitalmuseum.mr.entity.HumanBeing;
import jp.digitalmuseum.mr.entity.HumanBeingWithPen;
import jp.digitalmuseum.mr.resource.Pen;

public class HumanBeingFrame extends DrawableFrame {
	private static final long serialVersionUID = 1L;
	private HumanBeing robot;
	private BufferedImage imageGoForward;
	private BufferedImage imageSpinLeft;
	private BufferedImage imageSpinRight;
	private BufferedImage imagePen;

	public HumanBeingFrame(HumanBeing robot) {
		this.robot = robot;
		try {
			imageGoForward = ImageIO.read(new File("images/GoForward.png"));
			imageSpinLeft  = ImageIO.read(new File("images/SpinLeft.png"));
			imageSpinRight = ImageIO.read(new File("images/SpinRight.png"));
			imagePen = ImageIO.read(new File("images/Pen.png"));
		} catch (IOException e) {
			// Do nothing.
		}
	}

	@Override
	public void paint2D(Graphics2D g) {
		g.setColor(Color.white);
		g.fillRect(0, 0, getFrameWidth(), getFrameHeight());
		g.setColor(Color.black);
		g.drawString("Legs: "+robot.getStatusOfLegs(), 10, 20);
		BufferedImage statusImage = null;
		switch (robot.getStatusOfLegs()) {
		case GO_FORWARD:
			statusImage = imageGoForward;
			break;
		case SPIN_LEFT:
			statusImage = imageSpinLeft;
			break;
		case SPIN_RIGHT:
			statusImage = imageSpinRight;
			break;
		}
		if (statusImage != null) {
			g.drawImage(statusImage,
					(getFrameWidth()-statusImage.getWidth())/2,
					50, null);
		}
		if (robot instanceof HumanBeingWithPen) {
			g.drawString("Pen: "+
					((HumanBeingWithPen) robot).getStatusOfPen(), 10, 40);
			if (imagePen != null) {
				final int x = (getFrameWidth()-imagePen.getWidth())/2;
				final int baseY = 45 + (imageGoForward == null ? 0 : imageGoForward.getHeight());
				int offsetY = ((HumanBeingWithPen) robot).getStatusOfPen()
						== Pen.STATUS.DOWN ? 20 : 0;
				g.drawImage(imagePen,
						x,
						baseY + offsetY, null);
				offsetY += imagePen.getHeight();
				g.drawLine(x, baseY + offsetY, getFrameWidth() - x, baseY + offsetY);
			}
		}
	}
}

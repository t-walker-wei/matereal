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
package com.phybots.gui;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JPanel;

public class MeterPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private int percentage;
	private int margin = 5;
	private Color borderColor = Color.black;
	private Color backgroundColor = Color.white;
	private Color foregroundColor = Color.red;

	/**
	 * This is the default constructor
	 */
	public MeterPanel() {
		super();
		initialize();
	}

	/**
	 * This method initializes this
	 *
	 * @return void
	 */
	private void initialize() {
		// Do nothing.
	}

	public int getPercentage() {
		return percentage;
	}

	public void setPercentage(int percentage) {
		if (percentage < 0) {
			percentage = 0;
		} else if (percentage > 100) {
			percentage = 100;
		}
		this.percentage = percentage;
	}

	public int getMargin() {
	    return margin;
	}

	public void setMargin(int margin) {
	    this.margin = margin;
	}

	public Color getBorderColor() {
	    return borderColor;
	}

	public void setBorderColor(Color borderColor) {
	    this.borderColor = borderColor;
	}

	public Color getBackgroundColor() {
	    return backgroundColor;
	}

	public void setBackgroundColor(Color backgroundColor) {
	    this.backgroundColor = backgroundColor;
	}

	public Color getForegroundColor() {
	    return foregroundColor;
	}

	public void setForegroundColor(Color foregroundColor) {
	    this.foregroundColor = foregroundColor;
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		final int
				width = getWidth() - margin*2,
				height = getHeight() - margin*2;
		g.setColor(borderColor);
		g.drawRect(margin, margin, width, height);
		g.setColor(backgroundColor);
		g.fillRect(margin+1, margin+1, width-2, height-2);
		g.setColor(foregroundColor);
		g.fillRect(margin+2, margin+2, (width-4)*percentage/100, height-3);
	}

}

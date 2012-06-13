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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;

import javax.swing.JComponent;
import javax.swing.JFrame;

/**
 * Standard JFrame with DISPOSE_ON_CLOSE option.<br />
 * With a disposable component specified in the constructor,
 * the component will be disposed
 * at the same time when this JFrame is disposed.
 *
 * @author Jun Kato
 */
public class DisposeOnCloseFrame extends JFrame {
	private static final long serialVersionUID = 1L;
	private JComponent component;

	public DisposeOnCloseFrame() {
		this.component = null;
		initialize();
	}

	public DisposeOnCloseFrame(JComponent component) {
		this.component = component;
		initialize();
	}

	private void initialize() {
		if (component != null) {
			setLayout(new BorderLayout());
			add(component, BorderLayout.CENTER);
			pack();
		}
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setLocationRelativeTo(null);
		setVisible(true);
	}

	public void dispose() {
		if (component != null && component instanceof DisposableComponent) {
			((DisposableComponent) component).dispose();
		}
		super.dispose();
	}

	/**
	 * Set the insets size of this frame.
	 *
	 * @param width
	 * @param height
	 */
	public void setFrameSize(int width, int height) {
		final Insets insets = getInsets();
		final Dimension d = new Dimension(
				width+insets.left+insets.right,
				height+insets.top+insets.bottom);
		setPreferredSize(d);
		setSize(d);
		if (component != null) {
			d.setSize(width, height);
			component.setPreferredSize(d);
			component.setSize(d);
		}
	}

	public int getFrameWidth() {
		final Insets insets = getInsets();
		return getWidth()-insets.left-insets.right;
	}

	public int getFrameHeight() {
		final Insets insets = getInsets();
		return getHeight()-insets.top-insets.bottom;
	}
}

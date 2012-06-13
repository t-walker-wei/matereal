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

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import com.phybots.message.Event;
import com.phybots.message.EventListener;
import com.phybots.message.ImageUpdateEvent;
import com.phybots.message.ServiceEvent;
import com.phybots.message.ServiceStatus;
import com.phybots.service.ImageProvider;
import com.phybots.utils.ScreenPosition;


/**
 * Panel class for configuring ImageProvider.
 *
 * @author Jun Kato
 * @see com.phybots.service.ImageProvider
 */
public class ImageProviderPanel extends JPanel implements DisposableComponent {
	private static final long serialVersionUID = 1L;
	private transient ImageProvider source;
	private transient BufferedImage image;
	private transient EventListener eventListener;

	public ImageProviderPanel(ImageProvider imageProvider) {
		this.source = imageProvider;
		setPreferredSize(new Dimension(source.getWidth(), source.getHeight()));

		// Add event listener to the source.
		eventListener = new EventListener() {
			public void eventOccurred(Event e) {
				if (e instanceof ImageUpdateEvent) {
					image = source.getImage();
					repaint();
				} else if (e instanceof ServiceEvent) {
					if (((ServiceEvent) e).getStatus() == ServiceStatus.DISPOSED) {
						dispose();
					}
				}
			}
		};
		source.addEventListener(eventListener);
	}

	public void dispose() {
		setEnabled(false);
		if (source != null) {
			source.removeEventListener(eventListener);
			source = null;
		}
	}

	public ImageProvider getSource() {
		return source;
	}

	public int getOffsetX() {
		return (getWidth() -
				(image == null ? 0 : image.getWidth()))/2;
	}

	public int getOffsetY() {
		return (getHeight() -
				(image == null ? 0 : image.getHeight()))/2;
	}

	public int getScreenToImageX(int x) {
		return x - getOffsetX();
	}

	public int getScreenToImageY(int y) {
		return y - getOffsetY();
	}

	public ScreenPosition getScreenToImage(ScreenPosition screenPosition) {
		final ScreenPosition imagePosition = new ScreenPosition();
		getScreenToImageOut(screenPosition, imagePosition);
		return imagePosition;
	}

	public void getScreenToImageOut(ScreenPosition screenPosition, ScreenPosition imagePosition) {
		imagePosition.set(
				getScreenToImageX(screenPosition.getX()),
				getScreenToImageY(screenPosition.getY()));
	}

	public int getImageToScreenX(int x) {
		return x + getOffsetX();
	}

	public int getImageToScreenY(int y) {
		return y + getOffsetY();
	}

	public ScreenPosition getImageToScreen(ScreenPosition imagePosition) {
		final ScreenPosition screenPosition = new ScreenPosition();
		getImageToScreenOut(imagePosition, screenPosition);
		return screenPosition;
	}

	public void getImageToScreenOut(ScreenPosition imagePosition, ScreenPosition screenPosition) {
		screenPosition.set(
				getImageToScreenX(imagePosition.getX()),
				getImageToScreenY(imagePosition.getY()));
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (image != null) {
			g.drawImage(image, getOffsetX(), getOffsetY(), null);
		}
	}
}

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
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;

import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import javax.swing.JPanel;

import com.phybots.Phybots;
import com.phybots.entity.Entity;
import com.phybots.entity.PhysicalEntity;
import com.phybots.message.Event;
import com.phybots.message.EventListener;
import com.phybots.message.ImageUpdateEvent;
import com.phybots.message.ServiceEvent;
import com.phybots.message.ServiceStatus;
import com.phybots.service.CoordProvider;
import com.phybots.service.LocationProvider;
import com.phybots.utils.Location;


/**
 * Panel class providing a view of the world in the absolute coordinate.
 *
 * @author Jun Kato
 * @see com.phybots.service.CoordProvider
 */
public class CoordViewPanel extends JPanel implements DisposableComponent {
	private static final long serialVersionUID = 1L;
	private static final double ZOOM = 0.8;
	private transient CoordProvider source;
	private final transient EventListener eventListener;
	private transient double realWidth, realHeight;

	/**
	 * This is the default constructor
	 */
	public CoordViewPanel(CoordProvider coordProvider) {
		source = coordProvider;

		// Add event listener to the source.
		eventListener = new EventListener() {
			public void eventOccurred(Event e) {
				if (e instanceof ImageUpdateEvent) {
					realWidth = source.getRealWidth();
					realHeight = source.getRealHeight();
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

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		// TODO speed up and offset bug fix
		final Graphics2D g2 = (Graphics2D) g;
		g2.setFont(Phybots.getInstance().getDefaultFont());
		final int
			width = getWidth(),
			height = getHeight();
		final double scale = realHeight*width < realWidth*height ?
				ZOOM*width/realWidth : ZOOM*height/realHeight;
		g2.setRenderingHint(
				RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(
				RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g2.scale(scale, scale);
		g2.translate(
				(width/scale-realWidth)/2,
				(height/scale-realHeight)/2);

		final Rectangle2D rect = new Rectangle2D.Double(0, 0, realWidth, realHeight);
		g2.setColor(Color.white);
		g2.fill(rect);
		g2.setColor(Color.black);
		g2.draw(rect);
		g2.setColor(Color.black);
		for (LocationProvider provider :
				Phybots.getInstance().lookForServices(LocationProvider.class)) {
			if (source == provider.getCoordProvider()) {
				for (Entity e : provider.getEntities()) {

					final Location location = provider.getLocation(e);
					if (location.isNotFound()) {
						continue;
					}

					final AffineTransform at = g2.getTransform();
					g2.translate(location.getX(), location.getY());

					final Shape shape = e instanceof PhysicalEntity ?
							((PhysicalEntity) e).getShape() : null;
					if (shape == null) {
						final String name = e.getName();
						if (name != null) {
							g2.drawString(name, 0, 0);
						}
					} else {
						final java.awt.Rectangle rct = shape.getBounds();
						g2.draw(shape);
						final String name = e.getName();
						if (name != null) {
							g2.drawString(name, rct.width/2+2, g2.getFontMetrics().getAscent()/2);
						}
					}

					g2.setTransform(at);
				}
			}
		}
	}

}

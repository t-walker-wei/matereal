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
package com.phybots.gui.utils;

import java.awt.Graphics;
import java.awt.Shape;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.phybots.Phybots;
import com.phybots.entity.Entity;
import com.phybots.service.CoordProvider;
import com.phybots.service.LocationProvider;
import com.phybots.utils.Location;
import com.phybots.utils.Position;
import com.phybots.utils.ScreenPosition;


/**
 * Helper class for drawing an entity to a canvas.
 *
 * @author Jun Kato
 */
public class EntityPainter {
	private CoordProvider coordProvider;
	private LocationProvider locationProvider;
	private HashMap<Entity, Path> entityPathMap;
	private double flatness;

	public EntityPainter() {
		findCoordProvider();
		findLocationProvider();
		entityPathMap = new HashMap<Entity, Path>();
	}

	public EntityPainter(double flatness) {
		this();
		this.flatness = flatness;
	}

	public double getFlatness() {
		return flatness;
	}

	public void setFlatness(double flatness) {
		this.flatness = flatness;
	}

	/**
	 * Set a CoordProvider.
	 * @param coordProvider
	 */
	public void setCoordProvider(CoordProvider coordProvider) {
		this.coordProvider = coordProvider;
	}

	/**
	 * Get a CoordProvider.
	 * @return Assigned CoordProvider.
	 */
	public CoordProvider getCoordProvider() {
		return coordProvider;
	}

	/**
	 * Set a LocationProvider.
	 * @param locationProvider
	 */
	public void setLocationProvider(LocationProvider locationProvider) {
		this.locationProvider = locationProvider;
	}

	/**
	 * Get a LocationProvider
	 * @return Assigned LocationProvider
	 */
	public LocationProvider getLocationProvider() {
		return locationProvider;
	}

	private void findCoordProvider() {
		coordProvider = Phybots.getInstance()
				.lookForService(CoordProvider.class);
	}

	private void findLocationProvider() {
		locationProvider = Phybots.getInstance()
				.lookForService(LocationProvider.class);
	}

	final Location location = new Location();
	final Position relativePosition = new Position();
	final ScreenPosition screenPosition = new ScreenPosition();
	final ScreenPosition previousScreenPosition = new ScreenPosition();

	/**
	 * Paint an entity in the specified graphic context.
	 *
	 * @param g
	 * @param e
	 */
	public void paint(Graphics g, Entity e) {
		locationProvider.getLocationOut(e, location);
		if (location.isNotFound()) {
			return;
		}
		for (List<Position> list : getPath(e)) {
			location.getRelativePositionOut(list.get(list.size()-1), relativePosition);
			coordProvider.realToScreenOut(
					relativePosition,
					previousScreenPosition);
			for (Position p : list) {
				location.getRelativePositionOut(p, relativePosition);
				coordProvider.realToScreenOut(
						relativePosition,
						screenPosition);
				g.drawLine(
						previousScreenPosition.getX(), previousScreenPosition.getY(),
						screenPosition.getX(), screenPosition.getY());
				previousScreenPosition.set(screenPosition);
			}
		}
	}

	/**
	 * Get a flattened Path of the shape of the specified entity.
	 *
	 * @param e
	 * @return
	 */
	public Path getPath(Entity e) {
		Path path = entityPathMap.get(e);
		Shape shape = e.getShape();
		if (path != null || shape == null) {
			return path;
		}
		path = new Path();
		List<Position> list = null;
		if (shape instanceof Rectangle2D) {
			Rectangle2D rect = (Rectangle2D) shape;
			list = new ArrayList<Position>();
			double x = rect.getX(), y = rect.getY();
			double w = rect.getWidth(), h = rect.getHeight();
			list.add(new Position(y,     x    ));
			list.add(new Position(y + h, x    ));
			list.add(new Position(y + h, x + w));
			list.add(new Position(y    , x + w));
			path.add(list);
			return path;
		}
		final PathIterator iterator = new FlatteningPathIterator(shape.getPathIterator(null), flatness);
		while (!iterator.isDone()) {
			double coords[] = new double[6];
			int type = iterator.currentSegment(coords);
			switch (type) {
			case PathIterator.SEG_MOVETO:
				if (list != null) {
					path.add(list);
				}
				list = new ArrayList<Position>();
				list.add(new Position(
								coords[1],
								coords[0]));
				break;
			case PathIterator.SEG_LINETO:
				list.add(new Position(
								coords[1],
								coords[0]));
				break;
			case PathIterator.SEG_CLOSE:
				break;
			case PathIterator.SEG_CUBICTO:
			case PathIterator.SEG_QUADTO:
				System.err.println("Unsupported segment type: "+type);
				break;
			}
			iterator.next();
		}
		path.add(list);
		return path;
	}

	/**
	 * Path information, points in a list.
	 *
	 * @author Jun Kato
	 */
	public static class Path extends ArrayList<List<Position>> {
		private static final long serialVersionUID = 1L;
	}
}

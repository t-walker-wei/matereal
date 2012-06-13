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
package com.phybots.task;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;

import com.phybots.entity.Resource;
import com.phybots.message.ImageUpdateEvent;
import com.phybots.resource.Camera;
import com.phybots.service.ImageProvider;
import com.phybots.task.TaskAbstractImpl;
import com.phybots.utils.Array;


public class Capture extends TaskAbstractImpl implements ImageProvider {
	private static final long serialVersionUID = 4743985668109775616L;
	private Camera camera;
	private transient BufferedImage image;
	private transient Array<ImageListener> listeners;

	public Capture() {
		initialize();
	}

	private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
		ois.defaultReadObject();
		initialize();
	}

	@Override
	protected void initialize() {
		super.initialize();
		listeners = new Array<ImageListener>();
	}

	@Override
	public List<Class<? extends Resource>> getRequirements() {
		List<Class<? extends Resource>> resourceTypes = super.getRequirements();
		resourceTypes.add(Camera.class);
		return resourceTypes;
	}

	protected void onStart() {
		camera = getResourceMap().get(Camera.class);
	}

	public void run() {
		image = camera.getImage();
		if (image != null) {
			distributeEvent(new ImageUpdateEvent(this));
			synchronized (listeners) {
				if (listeners.size() > 0) {
					for (ImageListener listener : listeners) {
						listener.imageUpdated(getImage());
					}
				}
			}
		}
	}

	public void drawImage(Graphics g) {
		drawImage(g, 0, 0);
	}

	public void drawImage(Graphics g, int x, int y) {
		g.drawImage(image, x, y, null);
	}

	public int getWidth() {
		return image.getWidth();
	}

	public int getHeight() {
		return image.getHeight();
	}

	public BufferedImage getImage() {
		return image;
	}

	public byte[] getImageData() {
		throw new UnsupportedOperationException();
	}

	public void addImageListener(ImageListener listener) {
		synchronized (listeners) {
			listeners.push(listener);
		}
	}

	public boolean removeImageListener(ImageListener listener) {
		synchronized (listeners) {
			return listeners.remove(listener);
		}
	}
}

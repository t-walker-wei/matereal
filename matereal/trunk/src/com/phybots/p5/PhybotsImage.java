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
package com.phybots.p5;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.Arrays;

import com.phybots.message.Event;
import com.phybots.message.EventListener;
import com.phybots.message.ImageUpdateEvent;
import com.phybots.message.ServiceEvent;
import com.phybots.message.ServiceStatus;
import com.phybots.service.ImageProvider;

import processing.core.PImage;

/**
 * Utility class that bridges Processing and Java2D rendering.
 * 
 * @author Jun Kato
 */
public class PhybotsImage extends PImage implements EventListener {
	private ImageProvider imageProvider;
	private BufferedImage image;
	private Graphics2D g2;
	public byte[] data;
	public int dataWidth;
	public int dataHeight;
	public boolean crop;
	public int cropX;
	public int cropY;
	public int cropW;
	public int cropH;

	public PhybotsImage(ImageProvider imageProvider) {
		this.imageProvider = imageProvider;
		imageProvider.addEventListener(this);
		if (!imageProvider.isStarted()) {
			imageProvider.start();
		}
		this.dataWidth = imageProvider.getWidth();
		this.dataHeight = imageProvider.getHeight();
		init(dataWidth, dataHeight, 1);
	}

	public PhybotsImage(int width, int height) {
		image = new BufferedImage(width, height,
				BufferedImage.TYPE_3BYTE_BGR);
		data = ((DataBufferByte)image.getRaster().getDataBuffer()).getData();
		g2 = image.createGraphics();
		this.dataWidth = width;
		this.dataHeight = height;
		init(dataWidth, dataHeight, 1);
	}

	public Graphics2D beginDraw() {
		return g2;
	}

	public void endDraw() {
		updateImage(data);
	}

	public void clear() {
		if (data != null) {
			Arrays.fill(data, (byte)0xff);
		}
	}

	public void crop(int x, int y, int w, int h) {
		crop = true;
		cropX = Math.max(0, x);
		cropY = Math.max(0, y);
		cropW = Math.min(w, dataWidth);
		cropH = Math.min(dataHeight, y + h) - cropY;
		if (cropW != width || cropH != height)
			init(w, h, 1);
	}

	public void noCrop() {
		crop = false;
	}

	public void dispose() {
		if (imageProvider != null) {
			imageProvider.removeEventListener(this);
			imageProvider.stop();
		}
		if (g2 != null) {
			g2.dispose();
		}
	}

	public void eventOccurred(Event e) {
		if (e instanceof ImageUpdateEvent) {
			updateImage(((ImageProvider) e.getSource()).getImageData());
		} else if (e instanceof ServiceEvent
				&& ((ServiceEvent) e).getStatus() == ServiceStatus.DISPOSED) {
			imageProvider = null;
		}
	}

	private void updateImage(byte[] data) {
		loadPixels();
		synchronized (pixels) {
			if (data == null) {
				return;
			}
			int index = 0;
			if (crop) {
				int byteIndex = cropX * 3;
				final int byteOffset = (dataWidth - cropW) * 3;
				for (int y = 0; y < cropH; y++) {
					for (int x = 0; x < cropW; x++) {
						pixels[index++] =
							(data[byteIndex++] & 0xff)
							|((data[byteIndex++] & 0xff) << 8)
							|((data[byteIndex++] & 0xff) << 16);
					}
					byteIndex += byteOffset;
				}
			} else {
				int byteIndex = 0;
				final int dataLength = dataWidth * dataHeight;
				while (index < dataLength) {
					pixels[index++] =
						(data[byteIndex++] & 0xff)
						|((data[byteIndex++] & 0xff) << 8)
						|((data[byteIndex++] & 0xff) << 16);
				}
			}
			updatePixels();
		}
	}
}

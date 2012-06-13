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
package com.phybots.entity;

import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.io.IOException;
import java.io.ObjectInputStream;

import javax.swing.JComponent;

import com.phybots.Phybots;
import com.phybots.gui.entity.EntityPanel;
import com.phybots.message.EntityEvent;
import com.phybots.message.EntityStatus;
import com.phybots.message.EntityUpdateEvent;
import com.phybots.message.Event;
import com.phybots.message.EventListener;
import com.phybots.utils.Array;


public class EntityImpl implements Entity {
	private static final long serialVersionUID = -3099866179891615970L;

	private transient Array<EventListener> listeners;

	/** Name of this entity. */
	private String name;

	/** Shape of this entity. */
	private Shape shape;

	private boolean isDisposed;

	public EntityImpl() {
		initialize();
	}

	public EntityImpl(String name) {
		setName(name);
		initialize();
	}

	private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
		ois.defaultReadObject();
		initialize();
	}

	protected void initialize() {
		listeners = new Array<EventListener>();
		isDisposed = false;
		Phybots.getInstance().registerEntity(this);

		// Distribute this event.
		distributeEvent(
				new EntityEvent(
						this, EntityStatus.INSTANTIATED));
	}

	public boolean isDisposed() {
		return isDisposed;
	}

	/**
	 * @see Entity#dispose()
	 */
	public void dispose() {
		if (!isDisposed() && !Phybots.getInstance().isDisposing()) {
			distributeEvent(new EntityEvent(this, EntityStatus.DISPOSED));
			Phybots.getInstance().unregisterEntity(this);
		}
		listeners.clear();
		isDisposed = true;
	}

	/**
	 * @see Entity#getShape()
	 */
	public Shape getShape() {
		if (shape == null) {
			shape = new Ellipse2D.Double(-5, -5, 10, 10);
		}
		return shape;
	}

	// Event related methods.

	/**
	 * Add an event listener.
	 */
	public void addEventListener(EventListener listener) {
		listeners.push(listener);
	}

	/**
	 * Remove an event listener.
	 *
	 * @return Returns whether removal succeeded or not.
	 */
	public boolean removeEventListener(EventListener listener) {
		if (listener == null) {
			return false;
		}
		return listeners.remove(listener);
	}

	/**
	 * Distribute an event to listeners.
	 *
	 * @param e
	 */
	protected void distributeEvent(Event e) {
		for (EventListener listener : listeners) {
			listener.eventOccurred(e);
		}
	}

	/**
	 * Set name of this entity.<br />
	 *
	 * @param name
	 */
	final public void setName(String name) {
		this.name = name;
		if (listeners != null) {
			distributeEvent(new EntityUpdateEvent(this, "name", name));
		}
	}

	final public String getName() {
		return name;
	}

	public String getTypeName() {
		return getClass().getSimpleName();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		final String name = getName();
		if (name == null) {
			sb.append(getTypeName());
		} else {
			sb.append(name);
			sb.append(" (");
			sb.append(getTypeName());
			sb.append(")");
		}
		return sb.toString();
	}

	public JComponent getConfigurationComponent() {
		return new EntityPanel(this);
	}
}
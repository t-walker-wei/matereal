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
package com.phybots.service;

import java.io.IOException;
import java.io.ObjectInputStream;

import javax.swing.JComponent;
import javax.swing.JPanel;

import com.phybots.Phybots;
import com.phybots.message.Event;
import com.phybots.message.EventListener;
import com.phybots.message.ServiceEvent;
import com.phybots.message.ServiceStatus;
import com.phybots.message.ServiceUpdateEvent;
import com.phybots.utils.Array;


/**
 * Abstract implementation for Service interface.<br />
 * All Service implementation classes must extend this abstract class.
 *
 * @author Jun Kato
 */
public abstract class ServiceAbstractImpl implements Service {
	private static final long serialVersionUID = -768269750331766789L;
	public final static int DEFAULT_INTERVAL = 33;
	private transient Phybots.Canceller canceller;
	private ServiceGroup serviceGroup;
	private transient Array<EventListener> listeners;
	private transient long birthDate;
	private long interval;
	private boolean isStarted;
	private boolean isPaused;
	private boolean isDisposed;

	public ServiceAbstractImpl() {
		initialize();
	}

	private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
		ois.defaultReadObject();
		boolean isStarted = this.isStarted;
		boolean isPaused = this.isPaused;
		initialize();
		if (isStarted) {
			start(serviceGroup);
			if (isPaused) {
				pause();
			}
		}
	}

	protected void initialize() {
		listeners = new Array<EventListener>();
		interval = DEFAULT_INTERVAL;
		isStarted = false;
		isPaused = false;
		isDisposed = false;
		Phybots.getInstance().registerService(this);
		distributeEvent(
				new ServiceEvent(
						this, ServiceStatus.INSTANTIATED));
	}

	public void dispose() {
		if (isStarted()) {
			stop();
		}
		if (!isDisposed() && !Phybots.getInstance().isDisposing()) {
			distributeEvent(new ServiceEvent(this, ServiceStatus.DISPOSED));
			Phybots.getInstance().unregisterService(this);
			listeners.clear();
		}
		isDisposed = true;
	}

	final public synchronized void setInterval(long interval) {
		this.interval = interval;
		if (isStarted()) {
			if (serviceGroup != null) {
				throw new IllegalStateException("Execution interval of a service which belongs to a service group can only be changed by calling setInterval method of the service group.");
			}
			stopRunning();
			startRunning();
		}
		distributeEvent(new ServiceUpdateEvent(this, "interval", interval));
	}

	public synchronized long getInterval() {
		if (!isStarted()) {
			return 0;
		}
		return serviceGroup != null ? serviceGroup.getInterval() : this.interval;
	}

	public synchronized void setServiceGroup(ServiceGroup serviceGroup) {
		if (this.serviceGroup == serviceGroup) {
			return;
		}

		// Unregister from the old service group.
		if (this.serviceGroup != null) {
			this.serviceGroup.unregisterService(this);
			this.serviceGroup = null;
		}
		boolean isStarted = isStarted();
		if (isStarted) {
			stop();
		}

		// Register to the new service group.
		this.serviceGroup = serviceGroup;
		if (serviceGroup != null) {
			serviceGroup.registerService(this);
		}
		if (isStarted) {
			start();
		}
		distributeEvent(new ServiceUpdateEvent(this, "service group", serviceGroup));
	}

	public synchronized ServiceGroup getServiceGroup() {
		return serviceGroup;
	}

	synchronized public void start(ServiceGroup serviceGroup) {
		setServiceGroup(serviceGroup);
		start();
	}

	public synchronized void start() {
		if (isStarted()) {
			return;
		}
		isStarted = true;

		// Save the date of birth.
		birthDate = System.currentTimeMillis();

		// Start running this service.
		onStart();
		if (serviceGroup == null) {
			startRunning();
		} else if (!serviceGroup.isStarted()) {
			serviceGroup.start();
		}

		// Distribute this event.
		distributeEvent(new ServiceEvent(ServiceAbstractImpl.this,
				ServiceStatus.STARTED));
	}

	synchronized public void stop() {
		if (!isStarted()) {
			return;
		}
		isStarted = false;

		// Stop running this service.
		if (serviceGroup == null) {
			stopRunning();
		} else if (serviceGroup.isStarted()) {
			serviceGroup.stop();
		}
		onStop();

		// Reset the born time of this service.
		birthDate = 0;

		// Distribute this event.
		distributeEvent(new ServiceEvent(ServiceAbstractImpl.this,
				ServiceStatus.STOPPED));
	}

	public synchronized void pause() {
		if (!isStarted() || isPaused()) {
			return;
		}

		// Stop running this service.
		if (serviceGroup == null) {
			stopRunning();
		} else {
			if (!serviceGroup.isPaused()) {
				serviceGroup.pause();
				return;
			}
		}

		// Distribute this event.
		isPaused = true;
		onPause();
		distributeEvent(new ServiceEvent(ServiceAbstractImpl.this,
				ServiceStatus.PAUSED));

		if (serviceGroup != null && !serviceGroup.isPaused()) {
			serviceGroup.pause();
		}
	}

	public synchronized void resume() {
		if (!isStarted()) {
			return;
		}

		// Resume running this service.
		onResume();
		if (serviceGroup == null) {
			startRunning();
		}

		// Distribute this event.
		isPaused = false;
		distributeEvent(new ServiceEvent(ServiceAbstractImpl.this,
				ServiceStatus.RESUMED));
	}

	private void startRunning() {
		if (!Phybots.getInstance().isDisposing()) {
			canceller = Phybots.getInstance().scheduleAtFixedRate(
				ServiceAbstractImpl.this,
				ServiceAbstractImpl.this.interval);
		}
	}

	private void stopRunning() {
		if (!Phybots.getInstance().isDisposing()) {
			if (canceller == null) {
				// This happens under the following conditions:
				//		* the service belongs to a service group.
				//		* the service (and the service group) is started.
				//		* this.setServiceGroup(null) is called.
			} else {
				canceller.cancel();
				canceller = null;
			}
		}
	}

	public synchronized boolean isStarted() {
		return isStarted;
	}

	public synchronized boolean isPaused() {
		return isPaused;
	}

	public synchronized boolean isDisposed() {
		return isDisposed;
	}

	public synchronized long getAliveTime() {
		return birthDate > 0 ? System.currentTimeMillis() - birthDate : -1;
	}

	public JComponent getConfigurationComponent() {
		return new JPanel();
	}

	public String getName() {
		return getClass().getSimpleName();
	}

	@Override
	public String toString() {
		try {
			String name = getName();
			if (name != null) {
				return name;
			}
		} catch (Exception e) {
			// Do nothing.
		}
		return getClass().getSimpleName();
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
		return listeners.remove(listener);
	}

	/**
	 * Called when this service starts.<br />
	 * <br />
	 * This method is assured to be called before the first call of {@link Service#run()}.
	 */
	protected void onStart() {
		// Do nothing unless overrode by subclasses.
	}

	/**
	 * Called when this service stops.<br />
	 * <br />
	 * This method is assured to be called after the last call of {@link Service#run()}.
	 */
	protected void onStop() {
		// Do nothing unless overrode by subclasses.
	}

	/**
	 * Called when this service pauses.
	 */
	protected void onPause() {
		// Do nothing unless overrode by subclasses.
	}

	/**
	 * Called when this service resumes from pausing.
	 */
	protected void onResume() {
		// Do nothing unless overrode by subclasses.
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
}

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
package jp.digitalmuseum.mr.service;

import javax.swing.JComponent;
import javax.swing.JPanel;

import jp.digitalmuseum.mr.Matereal;
import jp.digitalmuseum.mr.message.Event;
import jp.digitalmuseum.mr.message.EventListener;
import jp.digitalmuseum.mr.message.ServiceEvent;
import jp.digitalmuseum.mr.message.ServiceUpdateEvent;
import jp.digitalmuseum.utils.Array;

/**
 * Abstract implementation for Service interface.<br />
 * All Service implementation classes must extend this abstract class.
 *
 * @author Jun KATO
 */
public abstract class ServiceAbstractImpl implements Service {
	public final static int DEFAULT_INTERVAL = 33;
	private Matereal.Canceller canceller;
	private ServiceGroup serviceGroup;
	private Array<EventListener> listeners;
	private long birthDate;
	private long interval;
	private boolean isPaused;

	public ServiceAbstractImpl() {
		listeners = new Array<EventListener>();
		interval = DEFAULT_INTERVAL;
	}

	final public synchronized void setInterval(long interval) {
		this.interval = interval;
		if (isStarted()) {
			restart();
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
		if (isStarted()) {
			stop();
			start(serviceGroup);
			distributeEvent(new ServiceUpdateEvent(this, "service group", serviceGroup));
		} else {
			this.serviceGroup = serviceGroup;
		}
	}

	public synchronized ServiceGroup getServiceGroup() {
		return serviceGroup;
	}

	synchronized public void start() {
		start(null);
	}

	synchronized public void start(ServiceGroup serviceGroup) {
		if (isStarted()) {
			stop();
		}
		this.serviceGroup = serviceGroup;

		// Start this service.
		if (serviceGroup == null) {
			doStart();
		} else {
			serviceGroup.registerService(this);
		}
		Matereal.getInstance().registerService(this);

		// Save the date of birth.
		birthDate = System.currentTimeMillis();

		// Distribute this event.
		distributeEvent(
				new ServiceEvent(
						ServiceAbstractImpl.this,
						ServiceEvent.STATUS.STARTED));
		onStart();
	}

	private void doStart() {
		if (!Matereal.getInstance().isDisposing()) {
			canceller = Matereal.getInstance().scheduleAtFixedRate(
				ServiceAbstractImpl.this,
				ServiceAbstractImpl.this.interval);
		}
	}

	synchronized private void restart() {
		start(serviceGroup);
	}

	public synchronized void pause() {
		if (!isStarted()) {
			return;
		}
		isPaused = true;

		// Distribute this event.
		distributeEvent(
				new ServiceEvent(
						ServiceAbstractImpl.this,
						ServiceEvent.STATUS.PAUSED));
		onPause();
	}

	public synchronized void resume() {
		if (!isStarted()) {
			return;
		}
		isPaused = false;

		// Distribute this event.
		distributeEvent(
				new ServiceEvent(
						ServiceAbstractImpl.this,
						ServiceEvent.STATUS.RESUMED));
		onResume();
	}

	synchronized public void stop() {
		if (!isStarted()) {
			return;
		}

		// Unregister this service.
		if (serviceGroup == null) {
			canceller.cancel();
			canceller = null;
		} else {
			serviceGroup.unregisterService(this);
			serviceGroup = null;
		}
		if (!Matereal.getInstance().isDisposing()) {
			Matereal.getInstance().unregisterService(this);
		}

		// Reset the born time of this service.
		birthDate = 0;

		// Distribute this event.
		distributeEvent(
				new ServiceEvent(
						ServiceAbstractImpl.this,
						ServiceEvent.STATUS.STOPPED));
		onStop();
	}

	public synchronized boolean isStarted() {
		return canceller != null || serviceGroup != null;
	}

	public synchronized boolean isPaused() {
		return isPaused;
	}

	public JComponent getConfigurationComponent() {
		return new JPanel();
	}

	public String getName() {
		return "";
	}

	public synchronized long getAliveTime() {
		return birthDate > 0 ? System.currentTimeMillis() - birthDate : -1;
	}

	@Override
	public String toString() {
		return getName();
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
	 * Called when this service starts.
	 */
	protected void onStart() {
		// Do nothing unless overrode by subclasses.
	}

	/**
	 * Called when this service stops.
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

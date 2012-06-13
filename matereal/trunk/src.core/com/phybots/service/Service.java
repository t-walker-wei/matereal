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

import java.io.Serializable;

import javax.swing.JComponent;

import com.phybots.message.EventProvider;


/**
 * Service interface.
 *
 * @author Jun Kato
 */
public interface Service extends Runnable, EventProvider, Serializable {

	/**
	 * Start this service.	 *
	 * @see #start(ServiceGroup)
	 * @see #pause()
	 * @see #resume()
	 * @see #stop()
	 */
	public void start();

	/**
	 * Start this service on the specified service group. Calling this method is equivalent to calling start() after setServiceGroup(serviceGroup).
	 *
	 * @param serviceGroup
	 * @see #start()
	 */
	public void start(ServiceGroup serviceGroup);

	public void dispose();

	/**
	 * Pause this service. When paused, run() won't be called till resume() is called.
	 *
	 * @see #resume()
	 */
	public void pause();

	/**
	 * Resume this service. Only affective when this service is paused.
	 *
	 * @see #pause()
	 */
	public void resume();

	/**
	 * Stop this service. When stopped, run() won't be called any more.
	 * To restart this service, call start().
	 *
	 * @see #start()
	 */
	public void stop();

	/**
	 * @return Returns whether this service is started or not.
	 */
	public boolean isStarted();

	/**
	 * @return Returns whether this service is paused or not.
	 */
	public boolean isPaused();

	/**
	 * @return Returns whether this service is disposed or not.
	 */
	public boolean isDisposed();

	/**
	 * Set duration of one cycle in milliseconds.
	 * @param period Duration of a period in milliseconds.
	 */
	public abstract void setInterval(long period);

	/**
	 * Get duration of one cycle in milliseconds.
	 * @return Returns the duration
	 */
	public abstract long getInterval();

	/**
	 * Set the service group to which this service belongs.
	 */
	public void setServiceGroup(ServiceGroup serviceGroup);

	/**
	 * @return Returns the service group to which this service belongs.
	 */
	public ServiceGroup getServiceGroup();

	/**
	 * @return Returns a Swing component for configuring this service.
	 * @see JComponent
	 */
	public JComponent getConfigurationComponent();

	/**
	 * @return Returns the name of this service.
	 */
	public String getName();

	/**
	 * @return Returns time elapsed since the service started.
	 */
	public long getAliveTime();
}

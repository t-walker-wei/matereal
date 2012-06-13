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

import java.util.Iterator;
import java.util.List;

import com.phybots.Phybots;
import com.phybots.utils.Array;


/**
 * Class for grouping services.
 *
 * @author Jun Kato
 * @see Phybots
 */
public class ServiceGroup extends ServiceAbstractImpl {
	private static final long serialVersionUID = -813365521557471308L;
	private static int instances = 0;
	private int id;
	private Array<Service> services;

	public ServiceGroup() {
		services = new Array<Service>();
		id = ++ instances;
	}

	public String getName() {
		StringBuilder sb = new StringBuilder();
		sb.append("Service group (");
		sb.append(id);
		sb.append(")");
		return sb.toString();
	}

	public ServiceGroup(Service... servicesArray) {
		this();
		for (Service service : servicesArray) {
			services.push(service);
		}
	}

	public synchronized void run() {
		for (Service service : services) {
			service.run();
		}
	}

	public void add(Service service) {
		if (service != null) {
			service.setServiceGroup(this);
		}
	}

	public void remove(Service service) {
		if (service != null) {
			service.setServiceGroup(null);
		}
	}

	/**
	 * Register given service to run in this service group.
	 * @param service
	 */
	synchronized void registerService(Service service) {
		services.push(service);
	}

	/**
	 * Unregister given service to stop running in this service group.
	 * @param service
	 * @return True when succeeded in clearing registration.
	 */
	synchronized boolean unregisterService(Service service) {
		return services.remove(service);
	}

	/**
	 * Unregister all services related to this service group.
	 * @return Returns all unregistered services.
	 */
	public synchronized List<Service> clearServices() {
		final List<Service> list = services.asList();
		for (Service service : services) {
			service.stop();
		}
		return list;
	}

	/**
	 * Get all services related to this service group.
	 * @return
	 */
	public synchronized List<Service> getServices() {
		return services.asList();
	}

	/**
	 * Merge other service group with this group.<br />
	 * Merged group will be disposed using dispose().
	 */
	public synchronized void mergeServiceGroup(ServiceGroup serviceGroup) {
		final List<Service> services = serviceGroup.getServices();
		for (Service service : services) {
			serviceGroup.unregisterService(service);
			registerService(service);
		}
		serviceGroup.stop();
	}

	public Iterator<Service> iterator() {
		return services.iterator();
	}

	@Override
	protected synchronized void onStart() {
		for (Service service : services) {
			if (service instanceof ServiceAbstractImpl) {
				ServiceAbstractImpl.class.cast(service).start(this);
			}
		}
	}

	@Override
	protected void onStop() {
		for (Service service : services) {
			if (service instanceof ServiceAbstractImpl) {
				ServiceAbstractImpl.class.cast(service).stop();
			}
		}
	}

	protected void onPause() {
		for (Service service : services) {
			if (service instanceof ServiceAbstractImpl) {
				ServiceAbstractImpl.class.cast(service).pause();
			}
		}
	}

	protected void onResume() {
		for (Service service : services) {
			if (!service.isPaused()) {
				// This service requested to resume this service group.
			} else {
				if (service instanceof ServiceAbstractImpl) {
					ServiceAbstractImpl.class.cast(service).resume();
				}
			}
		}
	}
}

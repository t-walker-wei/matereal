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
package jp.digitalmuseum.mr;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.swing.UIManager;

import jp.digitalmuseum.mr.entity.Entity;
import jp.digitalmuseum.mr.gui.DisposeOnCloseFrame;
import jp.digitalmuseum.mr.gui.MonitorPanel;
import jp.digitalmuseum.mr.message.Event;
import jp.digitalmuseum.mr.message.EventListener;
import jp.digitalmuseum.mr.message.EventProvider;
import jp.digitalmuseum.mr.message.ServiceEvent;
import jp.digitalmuseum.mr.message.ServiceEvent.STATUS;
import jp.digitalmuseum.mr.service.Service;
import jp.digitalmuseum.mr.service.ServiceGroup;
import jp.digitalmuseum.mr.service.ServiceHolder;
import jp.digitalmuseum.utils.Array;

/**
 * Main class of matereal. Singleton.
 *
 * @author Jun KATO
 */
public final class Matereal implements ServiceHolder, EventProvider, EventListener {
	final public static String LIBRARY_NAME = "Matereal";
	final private static String THREAD_NAME = "Matereal Thread";
	public static int DEFAULT_NUM_THREADS = 20;
	private static Matereal instance;
	private Array<EventListener> listeners;
	private ScheduledExecutorService executor;
	private Set<Entity> entities;
	private Set<Service> services;
	private PrintStream out;
	private PrintStream err;
	private DisposeOnCloseFrame debugFrame;
	private boolean isDisposing = false;

// Singleton and application related methods.

	/**
	 * Private constructor
	 */
	private Matereal() {
		super();
		out = System.out;
		err = System.err;
		out.println("--Starting matereal.");
		listeners = new Array<EventListener>();
		executor = Executors.newScheduledThreadPool(DEFAULT_NUM_THREADS);
		entities = new HashSet<Entity>();
		services = new HashSet<Service>();
		setLookAndFeel();
	}

	/**
	 * Get the singleton instance of Matereal.
	 * @return Returns the singleton instance of this class.
	 */
	public synchronized static Matereal getInstance() {
		if (instance == null) {
			instance = new Matereal();
		}
		return instance;
	}

	public PrintStream getOutStream() {
		return out;
	}

	public void setOutStream(PrintStream out) {
		this.out = out;
	}

	public PrintStream getErrorStream() {
		return err;
	}

	public void setErrorStream(PrintStream err) {
		this.err = err;
	}

	public void showDebugFrame() {
		if (debugFrame == null) {
			MonitorPanel monitor = new MonitorPanel();
			debugFrame = new DisposeOnCloseFrame(monitor) {
				private static final long serialVersionUID = -3861483678329980836L;
				@Override
				public void dispose() {
					super.dispose();
					Matereal.this.debugFrame = null;
				}
			};
		}
		debugFrame.setVisible(true);
	}

	public void hideDebugFrame() {
		debugFrame.setVisible(false);
	}

	public void disposeDebugFrame() {
		if (debugFrame != null) {
			debugFrame.dispose();
			debugFrame = null;
		}
	}

	public String getName() {
		return THREAD_NAME;
	}

	public synchronized boolean isDisposing() {
		return isDisposing;
	}

	/**
	 * Stop all services and prepare for shutting down Matereal.
	 */
	public synchronized void dispose() {
		if (isDisposing) {
			return;
		}
		disposeDebugFrame();
		isDisposing = true;
		out.println("--Shutting down matereal.");
		synchronized (services) {
			for (Service service : services) {
				service.stop();
			}
			services.clear();
		}
		synchronized (entities) {
			for (Entity entity : entities) {
				entity.dispose();
			}
			entities.clear();
		}
		executor.shutdownNow();
		out.print("--THANK YOU. GOOD ");
		final int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
		if (hour > 6 && hour < 17) {
			out.println("DAY!");
		} else {
			out.println("NIGHT!");
		}
	}

// Core functions.

	/**
	 * Called by Entity constructor.
	 * @param entity
	 * @see jp.digitalmuseum.mr.entity.Entity
	 */
	public void registerEntity(Entity entity) {
		synchronized (entities) {
			entities.add(entity);
			entity.addEventListener(this);
		}
	}

	/**
	 * Called by Entity.dispose().
	 * @param entity
	 * @return Returns if unregistered successfully.
	 */
	public boolean unregisterEntity(Entity entity) {
		synchronized (entities) {
			if (entities.remove(entity)) {
				entity.removeEventListener(this);
				return true;
			}
			return false;
		}
	}

	/**
	 * Called by Service constructor.
	 * @param service
	 * @see jp.digitalmuseum.mr.service.Service
	 */
	public void registerService(Service service) {
		synchronized (services) {
			services.add(service);
			service.addEventListener(this);
		}
	}

	/**
	 * Called by Service.dispose().
	 * @param service
	 * @return Returns if unregistered successfully.
	 */
	public boolean unregisterService(Service service) {
		synchronized (services) {
			return services.remove(service);
		}
	}

	/**
	 * Get a set of entities.
	 */
	public Set<Entity> getEntities() {
		synchronized (entities) {
			return new HashSet<Entity>(entities);
		}
	}

	/**
	 * Get a list of services.
	 */
	public List<Service> getServices() {
		synchronized (services) {
			return new ArrayList<Service>(services);
		}
	}

	/**
	 * Get a set of service groups.
	 */
	public Set<ServiceGroup> getServiceGroups() {
		return lookForServices(ServiceGroup.class);
	}

	/**
	 * Look for entities with specific class managed by matereal.
	 *
	 * @param <T> Class to look for.
	 * @param classObject Class object of the class <T>.
	 * @return
	 */
	public <T extends Entity> Set<T> lookForEntities(Class<T> classObject) {
		Set<T> results = new HashSet<T>();
		for (Entity entity : entities) {
			if (classObject.isAssignableFrom(entity.getClass())) {
				results.add(classObject.cast(entity));
			}
		}
		return results;
	}

	/**
	 * Look for services with specific class/interface.
	 *
	 * @param <T> Class to look for.
	 * @param classObject Class object of the class <T>.
	 * @return
	 * @see ServiceGroup
	 */
	public <T extends Service> Set<T> lookForServices(Class<T> classObject) {

		// Look in matereal thread.
		return lookForServices(classObject, this);
	}

	/**
	 * Look for a service with specific class/interface.
	 *
	 * @param <T> Class to look for.
	 * @param classObject Class object of the class <T>.
	 * @return
	 * @see ServiceGroup
	 */
	public <T extends Service> T lookForService(Class<T> classObject) {

		// Look in matereal thread.
		return lookForService(classObject, this);
	}

	/**
	 * Look for services with specific class/interface in a service group.
	 *
	 * @param <T> Class to look for.
	 * @param classObject Class object of the class <T>.
	 * @param serviceIterator Iterator to look in.
	 * @return Returns services satisfying given conditions.
	 * @see #lookForServices(Class)
	 */
	public <T extends Service> Set<T> lookForServices(Class<T> classObject, Iterable<Service> serviceIterator) {
		Set<T> services = new HashSet<T>();
		synchronized (serviceIterator) {
			for (Service service : serviceIterator) {
				if (classObject.isAssignableFrom(service.getClass())) {
					services.add(classObject.cast(service));
				}
			}
		}
		return services;
	}

	/**
	 * Look for a service with specific class/interface in a service group.
	 *
	 * @param <T> Class to look for.
	 * @param classObject Class object of the class <T>.
	 * @param serviceIterator Iterator to look in.
	 * @return Returns services satisfying given conditions.
	 * @see #lookForService(Class)
	 */
	public <T extends Service> T lookForService(Class<T> classObject, Iterable<Service> serviceIterator) {
		synchronized (serviceIterator) {
			for (Service service : serviceIterator) {
				if (classObject.isAssignableFrom(service.getClass())) {
					return classObject.cast(service);
				}
			}
		}
		return null;
	}

	public Canceller scheduleAtFixedRate(final Runnable runnable, final long interval) {
		final Canceller canceller = new Canceller();
		executor.submit(new Runnable() {
			public void run() {
				ScheduledFuture<?> future = executor.scheduleAtFixedRate(
						runnable, interval, interval, TimeUnit.MILLISECONDS);
				canceller.setFuture(future);
				try {
					// TODO add support for re-initializing executor when the number of services reaches its maximum.
					while (true) {
						future.get();
					}
				} catch (ExecutionException e) {
					if (isDisposing) {
						return;
					}
					err.print("Service ");
					String name = null;
					if (runnable instanceof Service) {
						name = ((Service) runnable).getName();
					}
					if (name != null) {
						err.print("(");
						err.print(name);
						err.print(") ");
					}
					err.print("killed by: ");
					e.getCause().printStackTrace();
					if (runnable instanceof Service) {
						if (!((Service) runnable).isStarted()) {
							return;
						}
					}
					out.print("Restarting ");
					out.print(name == null ? "the service" : name);
					out.println(".");
					scheduleAtFixedRate(runnable, interval);
				} catch (InterruptedException e) {
					// Do nothing.
				}
			}
		});
		return canceller;
	}

	public class Canceller {
		private Future<?> future;
		private void setFuture(Future<?> future) {
			this.future = future;
		}
		public void cancel() {
			executor.submit(new Runnable() {
				public void run() {
					future.cancel(false);
				}
			});
		}
	}

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

	@Override
	public void eventOccurred(Event e) {
		if (e instanceof ServiceEvent &&
				((ServiceEvent) e).getStatus() == STATUS.STOPPED) {
			e.getSource().removeEventListener(this);
		}
		for (EventListener listener : listeners) {
			listener.eventOccurred(e);
		}
	}

	/**
	 * Set look and feel of swing components.
	 */
	private void setLookAndFeel() {
		try {
			UIManager.setLookAndFeel(
					UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			// Do nothing.
		}
	}

	@Override
	public String toString() {
		return LIBRARY_NAME;
	}

	public Iterator<Service> iterator() {
		return new ServiceIterator();
	}

	private class ServiceIterator implements Iterator<Service> {
		private Iterator<Service> iterator;
		private Iterator<Service> currentIterator;

		public ServiceIterator() {
			iterator = services.iterator();
			currentIterator = iterator;
		}

		public ServiceIterator(ServiceGroup serviceGroup) {
			iterator = serviceGroup.iterator();
			currentIterator = iterator;
		}

		public boolean hasNext() {
			if (currentIterator.hasNext()) {
				return true;
			}
			if (currentIterator != iterator) {
				currentIterator = iterator;
				return currentIterator.hasNext();
			}
			return false;
		}

		public Service next() {
			Service next = currentIterator.next();
			if (next instanceof ServiceGroup) {
				currentIterator = new ServiceIterator(
						ServiceGroup.class.cast(next));
				return currentIterator.next();
			}
			return next;
		}

		public void remove() {
			currentIterator.remove();
		}
	}
}

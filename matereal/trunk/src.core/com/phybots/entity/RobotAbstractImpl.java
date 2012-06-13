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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import javax.swing.JComponent;

import com.phybots.gui.entity.RobotPanel;
import com.phybots.task.Task;


/**
 * Abstract implementation of Robot.<br />
 * Robot classes must extend this abstract class.
 *
 * @author Jun Kato
 */
public abstract class RobotAbstractImpl extends EntityImpl implements Robot {
	private static final long serialVersionUID = 7934515270996412490L;

	/** Name of the type of this robot. */
	private String typeName;

	public RobotAbstractImpl() {
		super();
	}

	public RobotAbstractImpl(String name) {
		super(name);
	}

	@Override
	protected void initialize() {
		typeName = getClass().getSimpleName();
		super.initialize();
	}

	/**
	 * Set a name of the type of this robot.<br />
	 * <b>Caution:</b> This method should only be called by a subclass of RobotAbstractImpl.
	 *
	 * @param typeName
	 */
	protected final void setTypeName(String typeName) {
		this.typeName = typeName;
	}

	public final String getTypeName() {
		return typeName;
	}

	/**
	 * This method must be implemented by child classes.<br />
	 * <b>Caution:</b> Do not call this method outside this class.
	 *
	 * @return Returns a set of all resources.
	 */
	protected List<ResourceAbstractImpl> getResources() {
		return new ArrayList<ResourceAbstractImpl>();
	}

	public final List<Class<? extends Resource>> getResourceTypes() {
		final ArrayList<Class<? extends Resource>> set =
				new ArrayList<Class<? extends Resource>>();

		// Find out types of every resource
		for (Resource resource : getResources()) {
			set.add(resource.getClass());
		}
		return set;
	}

	public final List<Class<? extends Resource>> getAvailableExclusiveResourceTypes() {
		final ArrayList<Class<? extends Resource>> list =
			new ArrayList<Class<? extends Resource>>();

		// Find out types of every resource
		for (ResourceAbstractImpl resource : getResources()) {
			if (resource instanceof ExclusiveResource &&
					ExclusiveResource.class.cast(resource).isWritable()) {
				list.add(resource.getClass());
			}
		}
		return list;
	}

	/**
	 *
	 */
	public final Task getAssignedTask(Class<? extends Resource> resourceType) {
		for (Resource resource : getResources()) {
			if (resourceType.isInstance(resource)) {
				if (resource instanceof ExclusiveResource) {
					Object writer = ((ExclusiveResource) resource).getWriter();
					if (writer instanceof Task) {
						return (Task) writer;
					}
				}
				for (Object reader : resource.getReaders()) {
					if (reader instanceof Task) {
						return (Task) reader;
					}
				}
			}
		}
		return null;
	}

	/**
	 *
	 */
	public final Set<Task> getAssignedTasks(Class<? extends Resource> resourceType) {
		Set<Task> tasks = new HashSet<Task>();
		for (Resource resource : getResources()) {
			if (resourceType.isInstance(resource)) {
				if (resource instanceof ExclusiveResource) {
					Object writer = ((ExclusiveResource) resource).getWriter();
					if (writer instanceof Task) {
						tasks.add((Task) writer);
					}
				}
				for (Object reader : resource.getReaders()) {
					if (reader instanceof Task) {
						tasks.add((Task) reader);
					}
				}
			}
		}
		return tasks;
	}

	public synchronized ResourceMap
			requestResources(Collection<Class<? extends Resource>> resourceTypes, Object object) {

		// Look for the desired resources.
		final List<Resource> resources = new ArrayList<Resource>(getResources());
		final ResourceMap resourceMap = new ResourceMap();
		for (Class<? extends Resource> resourceType : resourceTypes) {
			Iterator<Resource> it = resources.iterator();
			while (it.hasNext()) {
				Resource resource = it.next();
				if (resourceType.isInstance(resource)) {
					if (ExclusiveResource.class.isAssignableFrom(resourceType)) {
						if (!(resource instanceof ResourceAbstractImpl) ||
								!((ResourceAbstractImpl) resource).isWritable()) {
							continue;
						}
					}
					resourceMap.put(resourceType, resource);
					it.remove();
					break;
				}
			}
		}

		// If some of the resources cannot be found, return null.
		if (resourceMap.size() != resourceTypes.size()) {
			return null;
		}

		// Assign task to the exclusive resources.
		for (Entry<Class<? extends Resource>, Resource> e : resourceMap) {
			Resource resource = e.getValue();
			if (ExclusiveResource.class.isAssignableFrom(e.getKey()) &&
					(e.getValue() instanceof ResourceAbstractImpl)) {
				((ResourceAbstractImpl) resource).setWriter(object);
			} else {
				((ResourceAbstractImpl) resource).addReader(object);

			}
		}
		return resourceMap;
	}

	public synchronized <T extends Resource> T requestResource(Class<T> resourceType, Object object) {

		// Look for the desired resource.
		for (Resource resource : getResources()) {
			if (resourceType.isInstance(resource)) {
				if (!(resource instanceof ResourceAbstractImpl)) {
					continue;
				}
				if (ExclusiveResource.class.isAssignableFrom(resourceType)) {
					if (!((ResourceAbstractImpl) resource).setWriter(object)) {
						continue;
					}
				} else {
					((ResourceAbstractImpl) resource).addReader(object);
				}
				return resourceType.cast(resource);
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public synchronized Resource requestResource(String resourceClassName, Object object) {
		Class<?> classObject;
		try {
			classObject = Class.forName(String.format("%s$%s",
					getClass().toString(),
					resourceClassName));
			if (Resource.class.isAssignableFrom(classObject)) {
				return requestResource((Class<? extends Resource>) classObject, object);
			}
		} catch (ClassNotFoundException e) {
			// Do nothing.
		}
		return null;
	}

	public synchronized void freeResources(Collection<Resource> resources, Object object) {
		for (Resource resource : resources) {
			if (resource instanceof ResourceAbstractImpl) {
				((ResourceAbstractImpl) resource).free(object);
			}
		}
	}

	public synchronized void freeResource(Resource resource, Object object) {
		if (resource instanceof ResourceAbstractImpl) {
			((ResourceAbstractImpl) resource).free(object);
		}
	}

	@Override
	public JComponent getConfigurationComponent() {
		return new RobotPanel(this);
	}

	public JComponent getResourceComponent(Class<? extends Resource> resourceType) {
		for (Resource resource : getResources()) {
			if (resourceType.isInstance(resource)) {
				return resource.getConfigurationComponent();
			}
		}
		return null;
	}

	public List<JComponent> getResourceComponents() {
		return getResourceComponents(getResourceTypes());
	}

	public List<JComponent> getResourceComponents(Collection<Class<? extends Resource>> resourceTypes) {
		final List<Resource> resources = new ArrayList<Resource>(getResources());
		final List<JComponent> components = new ArrayList<JComponent>();

		for (Class<? extends Resource> resourceType : resourceTypes) {
			Iterator<Resource> it = resources.iterator();
			while (it.hasNext()) {
				Resource resource = it.next();
				if (resourceType.isInstance(resource)) {
					components.add(resource.getConfigurationComponent());
					it.remove();
					break;
				}
			}
		}
		return components;
	}
}

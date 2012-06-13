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

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.swing.JComponent;

import com.phybots.message.EventProvider;
import com.phybots.task.Task;


/**
 * Robot interface.<br />
 * Real and virtual robots implement this interface.
 *
 * @author Jun Kato
 * @see RobotAbstractImpl
 * @see ProxyRobot
 */
public interface Robot extends Entity, EventProvider, Serializable {

	/**
	 * Get a name of the type of this robot.
	 */
	public String getTypeName();

	/**
	 * @return Returns a set of resource interfaces.
	 */
	public List<Class<? extends Resource>> getResourceTypes();

	/**
	 * @return Returns a set of available resource interfaces.
	 */
	public List<Class<? extends Resource>> getAvailableExclusiveResourceTypes();

	/**
	 * @return Returns assigned task of a resource.
	 */
	public Task getAssignedTask(Class<? extends Resource> resourceType);

	/**
	 * @return Returns assigned task of a resource.
	 */
	public Set<Task> getAssignedTasks(Class<? extends Resource> resourceType);

	/**
	 * Request resources with specified interfaces.
	 * @param resourceTypes
	 * @return Returns the resource if succeeded. Otherwise, returns null.
	 */
	public ResourceMap requestResources(
			Collection<Class<? extends Resource>> resourceTypes, Object object);

	/**
	 * Request resource with specified interface.
	 * @param resourceType
	 * @return Returns the resource if succeeded. Otherwise, returns null.
	 */
	public <T extends Resource> T requestResource(
			Class<T> resourceType, Object object);

	/**
	 * Request resource with specified interface.
	 * @param resourceClassName
	 * @return Returns the resource if succeeded. Otherwise, returns null.
	 */
	public Resource requestResource(
			String resourceClassName, Object object);

	public void freeResources(
			Collection<Resource> resources, Object object);

	public void freeResource(
			Resource resource, Object object);

	public JComponent getResourceComponent(Class<? extends Resource> resourceType);
	public List<JComponent> getResourceComponents();
	public List<JComponent> getResourceComponents(Collection<Class<? extends Resource>> resourceTypes);
}
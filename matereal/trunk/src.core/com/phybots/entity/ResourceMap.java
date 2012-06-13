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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ResourceMap implements Iterable<Map.Entry<Class<? extends Resource>, Resource>>, Serializable {
	private static final long serialVersionUID = 2730930267963392638L;
	private HashMap<Class<? extends Resource>, Resource> map;

	public ResourceMap() {
		map = new HashMap<Class<? extends Resource>, Resource>();
	}

	public<T extends Resource> T get(Class<T> classObject) {
		Resource resource = map.get(classObject);
		if (resource != null) {
			return classObject.cast(resource);
		}
		return null;
	}

	public boolean put(Class<? extends Resource> classObject, Resource resource) {
		if (classObject.isAssignableFrom(resource.getClass())) {
			map.put(classObject, resource);
			return true;
		} else {
			return false;
		}
	}

	public Iterator<Map.Entry<Class<? extends Resource>, Resource>> iterator() {
		return map.entrySet().iterator();
	}

	public int size() {
		return map.size();
	}

	public Collection<Resource> resources() {
		return map.values();
	}
}

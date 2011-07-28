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
package jp.digitalmuseum.mr.gui.entity;

import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

import jp.digitalmuseum.mr.entity.Entity;
import jp.digitalmuseum.mr.entity.Robot;
import jp.digitalmuseum.utils.ClassUtils;

public class EntityTypeComboBox extends JComboBox {
	private static final long serialVersionUID = 194429312094257135L;
	private Set<Class<?>> classSet;

	public EntityTypeComboBox() {
		setModel(new DefaultComboBoxModel());
		classSet = new HashSet<Class<?>>();
		updateList();
	}

	@SuppressWarnings("unchecked")
	public void updateList() {
		List<Class<?>> classObjects =
			ClassUtils.getClasses("jp.digitalmuseum.mr");
		for (Class<?> classObject : classObjects) {

			if (classSet.contains(classObject)) {
				continue;
			}

			if (!Entity.class.isAssignableFrom(classObject)) {
				continue;
			}

			int mod = classObject.getModifiers();
			if (Modifier.isAbstract(mod) ||
					Modifier.isInterface(mod)) {
				continue;
			}

			((DefaultComboBoxModel) getModel()).addElement(
					new EntityClass((Class<? extends Robot>) classObject));
			classSet.add(classObject);
		}
	}

	public Entity newEntityInstance() {
		if (getSelectedItem() != null) {
			return ((EntityClass) getSelectedItem()).newInstance();
		}
		return null;
	}

	public static class EntityClass {
		private Class<? extends Entity> entityClassObject;
		private String className;

		public EntityClass(Class<? extends Entity> entityClassObject) {
			this.entityClassObject = entityClassObject;
			this.className = entityClassObject.toString();
			this.className = this.className.substring(
					this.className.lastIndexOf('.') + 1);
		}

		public Entity newInstance() {
			try {
				return entityClassObject.newInstance();
			} catch (InstantiationException e) {
				//
			} catch (IllegalAccessException e) {
				//
			}
			return null;
		}

		@Override
		public String toString() {
			return className;
		}
	}
}
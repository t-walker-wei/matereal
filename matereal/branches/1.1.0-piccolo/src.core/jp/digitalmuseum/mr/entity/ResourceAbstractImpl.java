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
package jp.digitalmuseum.mr.entity;

import java.util.HashSet;
import java.util.Set;

/**
 * Abstract implementation of Resource.<br />
 * Resource implementation classes must extend this abstract class.
 *
 * @author Jun KATO
 */
public abstract class ResourceAbstractImpl implements Resource {
	private RobotAbstractImpl robot;
	private Object writer;
	private Set<Object> readers;

	protected ResourceAbstractImpl() {
		readers = new HashSet<Object>();
	}

	protected ResourceAbstractImpl(RobotAbstractImpl robot) {
		this();
		this.robot = robot;
	}

	/**
	 * @return Returns a robot to which this resource belongs.
	 */
	final public RobotAbstractImpl getRobot() {
		return robot;
	}

	final public Set<Object> getReaders() {
		return new HashSet<Object>(readers);
	}

	final void addReader(Object reader) {
		readers.add(reader);
	}

	final void removeReader(Object reader) {
		readers.remove(reader);
	}

	/**
	 * @return
	 */
	final public Object getWriter() {
		return writer;
	}

	/**
	 *
	 * @param writer
	 * @return
	 */
	final boolean setWriter(Object writer) {
		if (!isWritable()) {
			return false;
		}
		this.writer = writer;
		return true;
	}

	/**
	 * Returns whether this resource is free or not.
	 * @return
	 */
	final public boolean isWritable() {
		return writer == null;
	}

	/**
	 * Called when finished using this resource.
	 * @param task
	 */
	final void free(Object task) {
		if (task == writer) {
			onFree();
			writer = null;
		}
	}

	/**
	 * Called when disposing this resource.<br />
	 * <b>Caution:</b> This method should not be called outside this class.
	 *
	 * @see #free(Object)
	 */
	protected void onFree() {
		//
	}
}
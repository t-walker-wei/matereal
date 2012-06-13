/*
 * PROJECT: Phybots at http://phybots.com/
 * ----------------------------------------------------------------------------
 *
 * This file is part of Phybots.
 * Phybots is a Java/Processing toolkit for making robotic things.
 *
 * ----------------------------------------------------------------------------
 *
 * License version: MPL 1.1/GPL 2.0/LGPL 2.1
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
package com.phybots.utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Array with variable length.
 *
 * @author Jun Kato
 */
public class Array<T> implements Iterable<T>, Serializable {
	private static final long serialVersionUID = -1129675547003612162L;

	final public static int INITIAL_SIZE = 10;

	private Set<ArrayIterator> iterators;

	/** Array for holding elements. */
	private Object[] elements = new Object[INITIAL_SIZE];

	/** Length of valid results. */
	private int validSize;

	public Array() {
		iterators = new HashSet<ArrayIterator>();
	}

	public Array(Array<T> old) {
		this();
		if (old == null) {
			return;
		}

		// Copy the elements.
		realloc(old.size());
		for (T e : old) {
			elements[validSize ++] = e;
		}
	}

	/** Extend the holder size to the specified length. */
	public synchronized void realloc(int length) {
		if (length <= elements.length) {
			return;
		}
		Class<?> type = elements.getClass().getComponentType();
		@SuppressWarnings("unchecked")
		T[] newElements = (T[]) java.lang.reflect.Array.newInstance(type, length);
		System.arraycopy(elements, 0, newElements, 0,
				length > elements.length ? elements.length : length);
		elements = newElements;
	}

	/** Returns the number of elements. */
	public int size() { return validSize; }

	/** Clear all elements. */
	public synchronized void clear() {
		Arrays.fill(elements, null);
		validSize = 0;
	}

	/** Put an element to the specified index. */
	public synchronized void put(T e, int index) {
		if (index < 0 || index + 1 > validSize) {
			throw new ArrayIndexOutOfBoundsException();
		}
		elements[index] = e;
	}

	/** Insert an element to the specified index. */
	public synchronized void insert(T e, int index) {
		if (index < 0 || index > validSize) {
			throw new ArrayIndexOutOfBoundsException();
		}
		if (validSize + 1 >= elements.length) {
			realloc(elements.length + 1);
		}
		System.arraycopy(elements, index, elements, index + 1, validSize - index);
		elements[index] = e;
		validSize ++;
	}

	/** Push an element. */
	public synchronized void push(T e) {
		if (validSize + 1 >= elements.length) {
			realloc(elements.length+1);
		}
		elements[validSize ++] = e;
	}

	public synchronized boolean contains(T e) {
		for (int i = 0; i < validSize; i ++) {
			if (e.equals(elements[i])) {
				return true;
			}
		}
		return false;
	}

	/** Remove an element. */
	public synchronized void remove(int index) {
		for (int i = index; i < validSize-1;) {
			elements[i] = elements[++ i];
		}
		elements[-- validSize] = null;
		for (ArrayIterator it : iterators) {
			if (it.counter > index) {
				it.counter --;
			}
		}
	}

	/** Remove a specified element. */
	public synchronized boolean remove(T e) {
		if (e == null) {
			return false;
		}
		for (int i = 0; i < validSize; i ++) {
			if (e.equals(elements[i])) {
				remove(i);
				return true;
			}
		}
		return false;
	}

	/** Get an element. */
	@SuppressWarnings("unchecked")
	public synchronized T get(int index) {
		if (index > validSize) {
			throw new ArrayIndexOutOfBoundsException();
		}
		return (T) elements[index];
	}

	/** Return this array as a List object. */
	public synchronized List<T> asList() {
		final ArrayList<T> list = new ArrayList<T>();
		for (T e : this) {
			list.add(e);
		}
		return list;
	}

	public Iterator<T> iterator() {
		return new ArrayIterator();
	}

	private class ArrayIterator implements Iterator<T> {
		private int counter;

		public ArrayIterator() {
			synchronized (Array.this) {
				counter = 0;
				iterators.add(this);
			}
		}
		public boolean hasNext() {
			synchronized (Array.this) {
				boolean hasNext = counter < validSize && counter >= 0;
				if (!hasNext) {
					iterators.remove(this);
				}
				return hasNext;
			}
		}

		@SuppressWarnings("unchecked")
		public T next() {
			synchronized (Array.this) {
				if (counter >= validSize || counter < 0) {
					throw new NoSuchElementException();
				}
				return (T) elements[counter ++];
			}
		}

		public void remove() {
			synchronized (Array.this) {
				Array.this.remove(counter - 1);
			}
		}
	}
}

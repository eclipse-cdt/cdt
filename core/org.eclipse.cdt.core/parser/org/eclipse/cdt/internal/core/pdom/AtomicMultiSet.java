/*******************************************************************************
 * Copyright (c) 2011, 2014 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom;

import java.util.HashMap;

/**
 * Thread-safe reference counted set of objects.
 *
 * @param <T> Type of the objects contained in the set.
 */
public class AtomicMultiSet<T> {
	private final HashMap<T, Integer> map = new HashMap<>();

	/**
	 * Adds object to the set if it was not present, or increments its reference count otherwise.
	 *
	 * @param object The object to add to the set.
	 * @return Reference count of the object after the operation.
	 */
	public synchronized int add(T object) {
		Integer count = map.get(object);
		if (count == null) {
			count = Integer.valueOf(1);
		} else {
			count = Integer.valueOf(count.intValue() + 1);
		}
		map.put(object, count);
		return count.intValue();
	}

	/**
	 * Decrements reference count of the object in the set and removes the object if its reference
	 * count reaches zero.
	 *
	 * @param object The object to remove from the set.
	 * @return Reference count of the object after the operation, or -1 if the object was not
	 *     present in the set.
	 */
	public synchronized int remove(T object) {
		Integer count = map.remove(object);
		if (count == null) {
			return -1;
		}
		int n = count.intValue() - 1;
		if (n <= 0) {
			return n;
		}
		map.put(object, Integer.valueOf(n));
		return n;
	}

	public synchronized void clear() {
		map.clear();
	}

	public synchronized boolean contains(T object) {
		return map.containsKey(object);
	}

	public synchronized int getCount(T object) {
		Integer count = map.get(object);
		return count != null ? count.intValue() : 0;
	}

	public synchronized boolean isEmpty() {
		return map.isEmpty();
	}

	public synchronized int size() {
		return map.size();
	}

	@Override
	public synchronized String toString() {
		return map.toString();
	}
}

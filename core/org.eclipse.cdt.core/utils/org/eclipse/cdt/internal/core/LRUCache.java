/*******************************************************************************
 * Copyright (c) 2013 Andrew Gvozdev and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Andrew Gvozdev - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core;

import java.util.LinkedHashMap;
import java.util.Map.Entry;

/**
 * A simple cache with limited number of items in the cache. LRUCache discards the Least Recently Used items first.
 * Based on {@link LinkedHashMap}. Note that {@link LinkedHashMap} has built-in facility to support cache like that
 * which is described in its JavaDoc.
 */
public class LRUCache<K, V> extends LinkedHashMap<K, V> {
	private int fLimit;

	/**
	 * Constructs an empty LRUCache with the specified limit on the number of items in the cache.
	 *
	 * @param limit - the maximum number of items to keep in the cache.
	 */
	public LRUCache(int limit) {
		super(limit, 0.75f, true);
		fLimit = limit;
	}

	/**
	 * Constructs an empty LRUCache with the specified initial capacity and limit on the number of items in the cache.
	 *
	 * @param initialCapacity - initial capacity.
	 * @param limit - the maximum number of items to keep in the cache.
	 */
	public LRUCache(int initialCapacity, int limit) {
		super(initialCapacity, 0.75f, true);
		fLimit = limit;
	}

	@Override
	protected boolean removeEldestEntry(Entry<K, V> eldest) {
		return size() >= fLimit;
	}
}
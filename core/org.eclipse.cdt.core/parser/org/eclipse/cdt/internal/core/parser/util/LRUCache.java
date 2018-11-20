/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.parser.util;

import java.util.LinkedHashMap;
import java.util.Map.Entry;

/**
 * LRUCache based on {@link LinkedHashMap}.
 */
public class LRUCache<K, V> extends LinkedHashMap<K, V> {
	private int fLimit;

	public LRUCache(int limit) {
		super(16, 0.75f, true);
		fLimit = limit;
	}

	@Override
	protected boolean removeEldestEntry(Entry<K, V> eldest) {
		return size() >= fLimit;
	}
}

/*****************************************************************
 * Copyright (c) 2011 Texas Instruments and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Patrick Chuong (Texas Instruments) - Add support for icon overlay in the debug view (Bug 334566)
 *****************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.cdt.debug.ui.IPinProvider.IPinElementColorDescriptor;

/**
 * This class tracks the color for the pinned context.
 */
class GdbPinColorTracker {
	private class Pair {
		Integer refCount;
		String context;

		Pair(String context, Integer refCount) {
			this.context = context;
			this.refCount = refCount;
		}
	}

	private static List<Pair> gsColorBuckets = Collections.synchronizedList(new ArrayList<Pair>());

	/**
	 * The static instance.
	 */
	static GdbPinColorTracker INSTANCE = new GdbPinColorTracker();

	/**
	 * Singleton object - make constructor private.
	 */
	private GdbPinColorTracker() {
	}

	int addRef(String context) {
		if (context == null)
			return IPinElementColorDescriptor.UNDEFINED;

		// look in the buckets and see if it is already exist
		for (int i = 0; i < gsColorBuckets.size(); ++i) {
			Pair pair = gsColorBuckets.get(i);
			if (pair.context.equals(context) && pair.refCount > 0) {
				pair.refCount++;
				return i % IPinElementColorDescriptor.DEFAULT_COLOR_COUNT;
			}
		}

		// if not exist in the buckets, then add to the bucket collection
		int size = gsColorBuckets.size();
		int index = size;
		for (int i = 0; i < size; ++i) {
			Pair pair = gsColorBuckets.get(i);
			if (pair.refCount <= 0) {
				index = i;
				gsColorBuckets.remove(index);
				break;
			}
		}
		gsColorBuckets.add(index, new Pair(context, 1));
		return (index) % IPinElementColorDescriptor.DEFAULT_COLOR_COUNT;
	}

	void removeRef(String context) {
		if (context == null)
			return;

		for (int i = 0; i < gsColorBuckets.size(); ++i) {
			Pair pair = gsColorBuckets.get(i);
			if (pair.context.equals(context)) {
				pair.refCount--;

				return;
			}
		}
	}
}

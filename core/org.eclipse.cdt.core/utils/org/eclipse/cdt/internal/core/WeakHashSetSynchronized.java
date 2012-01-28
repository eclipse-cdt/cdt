/*******************************************************************************
 *  Copyright (c) 2012 Andrew Gvozdev and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *     Andrew Gvozdev - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core;

/**
 * A synchronized hashset whose values can be garbage collected.
 */
public class WeakHashSetSynchronized<T> extends WeakHashSet<T> {
	/**
	 * Constructor.
	 */
	public WeakHashSetSynchronized() {
		super(5);
	}

	/**
	 * Constructor.
	 *
	 * @param size - initial capacity.
	 */
	public WeakHashSetSynchronized(int size) {
		super(size);
	}

	@Override
	synchronized public T add(T obj) {
		return super.add(obj);
	}
	@Override
	synchronized public T get(T obj) {
		return super.get(obj);
	}
	@Override
	synchronized public T remove(T obj) {
		return super.remove(obj);
	}
}

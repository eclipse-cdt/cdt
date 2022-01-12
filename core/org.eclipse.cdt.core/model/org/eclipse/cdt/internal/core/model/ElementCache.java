/*******************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.model;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.IOpenable;
import org.eclipse.cdt.internal.core.util.OverflowingLRUCache;

/**
 * An LRU cache of <code>CElements</code>.
 *
 * This class is similar to the JDT ElementCache class.
 */
public class ElementCache<T> extends OverflowingLRUCache<IOpenable, T> {

	/**
	 * Constructs a new element cache of the given size.
	 */
	public ElementCache(int size) {
		super(size);
	}

	/**
	 * Constructs a new element cache of the given size.
	 */
	public ElementCache(int size, int overflow) {
		super(size, overflow);
	}

	/**
	 * Returns true if the element is successfully closed and
	 * removed from the cache, otherwise false.
	 *
	 * <p>NOTE: this triggers an external removal of this element
	 * by closing the element.
	 */
	@Override
	protected boolean close(LRUCacheEntry<IOpenable, T> entry) {
		IOpenable element = entry._fKey;
		try {
			if (element.hasUnsavedChanges()) {
				return false;
			}
			element.close();
			return true;
		} catch (CModelException npe) {
			return false;
		}
	}

	/**
	 * Returns a new instance of the receiver.
	 */
	@Override
	protected OverflowingLRUCache<IOpenable, T> newInstance(int size, int overflow) {
		return new ElementCache<>(size, overflow);
	}
}

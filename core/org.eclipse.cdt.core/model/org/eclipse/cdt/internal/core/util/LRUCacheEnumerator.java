/*******************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.util;

import java.util.Enumeration;

/**
 *	The <code>LRUCacheEnumerator</code> returns its elements in
 *	the order they are found in the <code>LRUCache</code>, with the
 *	most recent elements first.
 *
 *	Once the enumerator is created, elements which are later added
 *	to the cache are not returned by the enumerator.  However,
 *	elements returned from the enumerator could have been closed
 *	by the cache.
 *
 *  This class is similar to the JDT LRUCacheEnumerator class.
 */
public class LRUCacheEnumerator<T> implements Enumeration<T> {
	/**
	 *	Current element;
	 */
	protected LRUEnumeratorElement<T> fElementQueue;

	public static class LRUEnumeratorElement<T> {
		/**
		 *	Value returned by <code>nextElement()</code>;
		 */
		public T fValue;

		/**
		 *	Next element
		 */
		public LRUEnumeratorElement<T> fNext;

		/**
		 * Constructor
		 */
		public LRUEnumeratorElement(T value) {
			fValue = value;
		}
	}
	/**
	 *	Creates a CacheEnumerator on the list of <code>LRUEnumeratorElements</code>.
	 */
	public LRUCacheEnumerator(LRUEnumeratorElement<T> firstElement) {
		fElementQueue = firstElement;
	}
	/**
	 * Returns true if more elements exist.
	 */
	@Override
	public boolean hasMoreElements() {
		return fElementQueue != null;
	}
	/**
	 * Returns the next element.
	 */
	@Override
	public T nextElement() {
		T temp = fElementQueue.fValue;
		fElementQueue = fElementQueue.fNext;
		return temp;
	}
}


/*******************************************************************************
 * Copyright (c) 2006, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mike Kucera (IBM Corporation) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser.util;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Useful utility methods for dealing with Collections.
 * 
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public final class CollectionUtils {

	private CollectionUtils() {
		// this class has just static utility methods
	}

	/**
	 * Returns an iterator that iterates backwards over the given list.
	 * The remove() method is not implemented and will throw UnsupportedOperationException.
	 * The returned iterator does not support the remove() method.
	 * 
	 * @throws NullPointerException if list is null
	 */
	public static <T> Iterator<T> reverseIterator(final List<T> list) {
		return new Iterator<T>() {	
			ListIterator<T> iterator = list.listIterator(list.size());
			
			@Override
			public boolean hasNext() {
				return iterator.hasPrevious();
			}
			@Override
			public T next() {
				return iterator.previous();
			}
			@Override
			public void remove() {
				throw new UnsupportedOperationException("remove() not supported"); //$NON-NLS-1$
			}
		};
	}

	/**
	 * Allows a foreach loop to iterate backwards over a list
	 * from the end to the start.
	 * 
	 * e.g.
	 * for(Object o : reverseIterable(list)) { ... }
	 * 
	 * @throws NullPointerException if list is null
	 */
	public static <T> Iterable<T> reverseIterable(final List<T> list) {
		return iterable(reverseIterator(list));
	}

	/**
	 * Creates an Iterable instance that just returns
	 * the given Iterator from its iterator() method.
	 * 
	 * This is useful for using an iterator in a foreach loop directly.
	 * 
	 * e.g.
	 * 
	 * for(Object o : iterable(list.listIterator())) {
	 *     // do something
	 * }
	 * 
	 * @throws NullPointerException if list is null
	 */
	public static <T> Iterable<T> iterable(final Iterator<T> iter) {
		if (iter == null)
			throw new NullPointerException("iter parameter is null"); //$NON-NLS-1$
			
		return new Iterable<T>() {
			@Override
			public Iterator<T> iterator() {
				return iter;
			}
		};
	}

	/**
	 * Finds the first object in the heterogeneous list that is an instance of 
	 * the given class, removes it from the list, and returns it.
	 * If there is not object in the list of the given type the list is left
	 * unmodified and null is returned.
	 * 
	 * @throws NullPointerException if list or clazz is null
	 * @throws UnsupportedOperationException if the list's Iterator does not support the remove() method
	 */
	@SuppressWarnings("unchecked")
	public static <T> T findFirstAndRemove(List<?> list, Class<T> clazz) {
		for (Iterator<?> iter = list.iterator(); iter.hasNext();) {
			Object o = iter.next();
			if (clazz.isInstance(o)) {
				iter.remove();
				return (T) o; // safe
			}
		}
		return null;
	}
}

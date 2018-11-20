/*******************************************************************************
 * Copyright (c) 2006, 2014 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Mike Kucera (IBM Corporation) - initial API and implementation
 *     Sergey Prigogin (Google)
 *     Nathan Ridge
 *******************************************************************************/
package org.eclipse.cdt.core.parser.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

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
	 * @throws NullPointerException if list is {@code null}
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
	 * Allows a foreach loop to iterate backwards over a list from the end to the start.
	 *
	 * <p>
	 * Example use:
	 * <pre>
	 *     for (Object o : reverseIterable(list)) { ... }
	 * </pre>
	 *
	 * @throws NullPointerException if list is null
	 */
	public static <T> Iterable<T> reverseIterable(final List<T> list) {
		return iterable(reverseIterator(list));
	}

	/**
	 * Creates an Iterable instance that just returns the given Iterator from its iterator() method.
	 *
	 * This is useful for using an iterator in a foreach loop directly.
	 *
	 * <p>
	 * Example use:
	 * <pre>
	 *     for (Object o : iterable(iterator)) { ... }
	 * </pre>
	 *
	 * @throws NullPointerException if list is {@code null}
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
	 * @throws UnsupportedOperationException if the list's Iterator does not support the remove()
	 *     method
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

	/**
	 * Combines two collections into one.
	 * @param c1 The first collection. May be modified as a result of the call. May be {@code null}.
	 * @param c2 The second collection. May be {@code null}.
	 * @return A collection containing elements from both input collections,
	 *     or {@code null} if both, {@code c1} and {@code c2} are {@code null}.
	 * @since 5.4
	 */
	public static <T, U extends Collection<T>> U merge(U c1, U c2) {
		if (c1 == null)
			return c2;
		if (c2 == null)
			return c1;
		if (c1.isEmpty())
			return c2;
		if (c2.isEmpty())
			return c1;
		c1.addAll(c2);
		return c1;
	}

	/**
	 * Returns a List<U> corresponding to a T in a Map<T, List<U>>. If the mapping doesn't exist,
	 * creates it with an empty list as the initial value.
	 * @since 5.6
	 */
	public static <T, U> List<U> listMapGet(Map<T, List<U>> m, T t) {
		List<U> result = m.get(t);
		if (result == null) {
			result = new ArrayList<>();
			m.put(t, result);
		}
		return result;
	}

	/**
	 * Filter the elements of a collection down to just the ones that match the given predicate.
	 * @since 5.6
	 */
	public static <T> Collection<T> filter(Collection<T> collection, IUnaryPredicate<T> predicate) {
		if (collection.isEmpty())
			return collection;
		Collection<T> result = null;
		int n = 0;
		for (T t : collection) {
			if (predicate.apply(t)) {
				if (result != null) {
					result.add(t);
				} else {
					++n;
				}
			} else if (result == null) {
				result = new ArrayList<>(collection.size() - 1);
				for (T u : collection) {
					if (--n < 0)
						break;
					result.add(u);
				}
			}
		}
		return result == null ? collection : result;
	}
}

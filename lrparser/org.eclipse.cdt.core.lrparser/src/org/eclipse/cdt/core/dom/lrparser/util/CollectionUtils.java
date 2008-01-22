/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.lrparser.util;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import lpg.lpgjavaruntime.IToken;


/**
 * Useful utility methods for dealing with Collections.
 * 
 * @author Mike Kucera
 */
public final class CollectionUtils {

	private CollectionUtils() {
		// this class has just static utility methods
	}
	
	
	/**
	 * Returns an iterator that iterates backwards over the given list.
	 * The remove() method is not implemented and will throw UnsupportedOperationException.
	 * @throws NullPointerException if list is null
	 */
	public static <T> Iterator<T> reverseIterator(final List<T> list) {
		return new Iterator<T>() {	
			ListIterator<T> iterator = list.listIterator(list.size());
			
			public boolean hasNext() {
				return iterator.hasPrevious();
			}
			public T next() {
				return iterator.previous();
			}
			public void remove() {
				throw new UnsupportedOperationException("remove() not supported"); //$NON-NLS-1$
			}
		};
	}
	
	
	/**
	 * Allows a foreach loop to iterate backwards over a list
	 * from the end to the start.
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
	 * ex)
	 * 
	 * foreach(Object o : iterable(list.listIterator())) {
	 *     // do something
	 * }
	 * 
	 * @throws NullPointerException if list is null
	 */
	public static <T> Iterable<T> iterable(final Iterator<T> iter) {
		if(iter == null)
			throw new NullPointerException("iter parameter is null"); //$NON-NLS-1$
			
		return new Iterable<T>() {
			public Iterator<T> iterator() {
				return iter;
			}
		};
	}
	
	
	/**
	 * Allows simple pattern match testing of lists of tokens.
	 * 
	 * @throws NullPointerException if source or pattern is null
	 */
	public static boolean matchTokens(List<IToken> source, Integer ... pattern) {
		if(source.size() != pattern.length) // throws NPE if either param is null
			return false;
		
		for(int i = 0, n = pattern.length; i < n; i++) {
			if(source.get(i).getKind() != pattern[i].intValue())
				return false;
		}
		return true;
	}
	
	
	/**
	 * Finds the first object in the heterogeneous list that is an instance of 
	 * the given class, removes it from the list, and returns it.
	 * 
	 * @throws NullPointerException if list or clazz is null
	 * @throws UnsupportedOperationException if the list's Iterator does not support the remove() method
	 */
	@SuppressWarnings("unchecked")
	public static <T> T findFirstAndRemove(List<Object> list, Class<T> clazz) {
		// There's a name somewhere on the stack, find it
		for(Iterator<Object> iter = list.iterator(); iter.hasNext();) {
			Object o = iter.next();
			if(clazz.isInstance(o)) {
				iter.remove();
				return (T) o;
			}
		}
		return null;
	}
}

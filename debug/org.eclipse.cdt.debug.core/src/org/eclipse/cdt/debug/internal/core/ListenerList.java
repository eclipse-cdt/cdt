/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.core;

/**
 * Local version of org.eclipse.jface.util.ListenerList (modified)s
 */
public class ListenerList {
	/**
	 * The current number of listeners.
	 * Maintains invariant: 0 <= fSize <= listeners.length.
	 */
	private int fSize;

	/**
	 * The list of listeners.  Initially <code>null</code> but initialized
	 * to an array of size capacity the first time a listener is added.
	 * Maintains invariant: listeners != null if and only if fSize != 0
	 */
	private Object[] fListeners= null;

	/**
	 * The empty array singleton instance, returned by getListeners()
	 * when size == 0.
	 */
	private static final Object[] EmptyArray= new Object[0];

	/**
	 * Creates a listener list with the given initial capacity.
	 *
	 * @param capacity the number of listeners which this list can initially accept 
	 *    without growing its internal representation; must be at least 1
	 */
	public ListenerList(int capacity) {
		if (capacity < 1) {
			throw new IllegalArgumentException();
		}
		fListeners= new Object[capacity];
		fSize= 0;
	}

	/**
	 * Adds a listener to the list.
	 * Has no effect if an identical listener is already registered.
	 *
	 * @param listener a listener
	 */
	public synchronized void add(Object listener) {
		if (listener == null) {
			throw new IllegalArgumentException();
		}
		// check for duplicates using identity
		for (int i= 0; i < fSize; ++i) {
			if (fListeners[i] == listener) {
				return;
			}
		}
		// grow array if necessary
		if (fSize == fListeners.length) {
			Object[] temp= new Object[(fSize * 2) + 1];
			System.arraycopy(fListeners, 0, temp, 0, fSize);
			fListeners= temp;
		}
		fListeners[fSize++]= listener;
	}

	/**
	 * Returns an array containing all the registered listeners.
	 * The resulting array is unaffected by subsequent adds or removes.
	 * If there are no listeners registered, the result is an empty array
	 * singleton instance (no garbage is created).
	 * Use this method when notifying listeners, so that any modifications
	 * to the listener list during the notification will have no effect on the
	 * notification itself.
	 */
	public synchronized Object[] getListeners() {
		if (fSize == 0) {
			return EmptyArray;
		}
		Object[] result= new Object[fSize];
		System.arraycopy(fListeners, 0, result, 0, fSize);
		return result;
	}

	/**
	 * Removes a listener from the list.
	 * Has no effect if an identical listener was not already registered.
	 *
	 * @param listener a listener
	 */
	public synchronized void remove(Object listener) {
		if (listener == null) {
			throw new IllegalArgumentException();
		}

		for (int i= 0; i < fSize; ++i) {
			if (fListeners[i] == listener) {
				if (--fSize == 0) {
					fListeners= new Object[1];
				} else {
					if (i < fSize) {
						fListeners[i]= fListeners[fSize];
					}
					fListeners[fSize]= null;
				}
				return;
			}
		}
	}

	/**
	 * Removes all the listeners from the list.
	 */
	public void removeAll() {
		fListeners= new Object[0];
		fSize= 0;
	}

	/**
	 * Returns the number of registered listeners
	 *
	 * @return the number of registered listeners
	 */
	public int size() {
		return fSize;
	}
}


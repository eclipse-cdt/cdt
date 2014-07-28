/*******************************************************************************
 * Copyright (c) 2014 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Thomas Corbat (IFS) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text.contentassist;

/**
 * This class represents a field or variable which shall be initialized lazily when accessed the
 * first time. It's value is computed once by the <code>calculateValue()</code> method. The value is
 * accessed by <code>value()</code>.
 *
 * This implementation is NOT thread-safe!
 *
 * @param <E> The type of the lazy initialized variable.
 */
public abstract class Lazy<E> {
	private final static Object NOT_INITIALIZED = new Object();
	private Object value = NOT_INITIALIZED;

	/**
	 * @return The value of this object.
	 */
	@SuppressWarnings("unchecked")
	public E value() {
		if (value == NOT_INITIALIZED) {
			value = calculateValue();
		}
		return (E) value;
	}

	/**
	 * Calculates the value of this object. This method is called only once, when the value is
	 * accessed the first time.
	 *
	 * @return the value assigned to this object.
	 */
	protected abstract E calculateValue();
}
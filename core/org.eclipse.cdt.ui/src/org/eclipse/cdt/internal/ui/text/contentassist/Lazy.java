/*******************************************************************************
 * Copyright (c) 2014, 2015 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Thomas Corbat (IFS) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text.contentassist;

/**
 * This class represents a field or variable which shall be initialized lazily when accessed
 * the first time. It's value is computed once by the {@code calculateValue()} method. The value is
 * accessed by {@code value()}.
 *
 * This implementation is NOT thread-safe!
 *
 * @param <E> The type of the lazy initialized variable.
 */
public abstract class Lazy<E> {
	private static final Object NOT_INITIALIZED = new Object();
	private Object value = NOT_INITIALIZED;

	/**
	 * @return The value of this object.
	 */
	@SuppressWarnings("unchecked")
	public final E value() {
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
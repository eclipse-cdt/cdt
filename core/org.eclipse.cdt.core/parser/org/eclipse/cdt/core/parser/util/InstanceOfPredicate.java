/*******************************************************************************
 * Copyright (c) 2014 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser.util;

/**
 * Unary predicate returning {@code true} if the object is an instance of the given class
 * or interface.
 * @since 5.7
 */
public class InstanceOfPredicate<T> implements IUnaryPredicate<T> {
	private Class<?> type;

	public InstanceOfPredicate(Class<?> type) {
		this.type = type;
	}

	@Override
	public boolean apply(T obj) {
		return type.isInstance(obj);
	}
}

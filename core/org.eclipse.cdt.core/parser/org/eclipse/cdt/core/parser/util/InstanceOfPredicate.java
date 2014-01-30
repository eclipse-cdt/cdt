/*******************************************************************************
 * Copyright (c) 2014 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

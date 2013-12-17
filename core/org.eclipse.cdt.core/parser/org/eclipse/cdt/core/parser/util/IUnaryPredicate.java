/*******************************************************************************
 * Copyright (c) 2013 Nathan Ridge.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	  Nathan Ridge - initial API
 *******************************************************************************/
package org.eclipse.cdt.core.parser.util;

/**
 * A generic unary predicate interface.
 * Useful for operations that use unary predicates, like filtering an array.
 * @since 5.6
 */
public interface IUnaryPredicate<T> {
	boolean apply(T argument);
}
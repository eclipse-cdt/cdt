/*******************************************************************************
 * Copyright (c) 2013 Nathan Ridge.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
/*******************************************************************************
 * Copyright (c) 2017 Google, Inc and others.
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
package org.eclipse.cdt.internal.core.pdom.dom;

import org.eclipse.core.runtime.CoreException;

/**
 * An interface for iterating through lists that are stored in the PDOM without instantiating objects.
 */
@FunctionalInterface
public interface IRecordIterator {
	public static final IRecordIterator EMPTY = () -> 0;

	/**
	 * Returns the record of next element in the iteration, or zero if there are no elements left in
	 * the iteration.
	 */
	public long next() throws CoreException;
}

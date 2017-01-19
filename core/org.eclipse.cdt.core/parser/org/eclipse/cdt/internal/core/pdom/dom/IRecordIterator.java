/*******************************************************************************
 * Copyright (c) 2017 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

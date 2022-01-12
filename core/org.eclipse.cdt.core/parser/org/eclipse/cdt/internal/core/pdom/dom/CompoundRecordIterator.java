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
 * A record iterator that is a concatenation of multiple record iterators.
 */
public final class CompoundRecordIterator implements IRecordIterator {
	private final IRecordIterator[] iterators;
	private int currentOffset;

	/**
	 * Initializes the compound iterator.
	 *
	 * @param iterators the iterators to concatenate
	 */
	public CompoundRecordIterator(IRecordIterator... iterators) {
		if (iterators == null)
			throw new NullPointerException();
		this.iterators = iterators;
	}

	@Override
	public long next() throws CoreException {
		for (; currentOffset < iterators.length; currentOffset++) {
			IRecordIterator iterator = iterators[currentOffset];
			long record = iterator.next();
			if (record != 0)
				return record;
		}
		return 0;
	}
}

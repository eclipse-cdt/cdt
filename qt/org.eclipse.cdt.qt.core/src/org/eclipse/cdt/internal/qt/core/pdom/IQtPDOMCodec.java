/*
 * Copyright (c) 2013 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.cdt.internal.qt.core.pdom;

import org.eclipse.core.runtime.CoreException;

/**
 * A utility interface for encoding and decoding fixed-sized elements to and
 * from the Database.
 */
public interface IQtPDOMCodec<T> {
	/**
	 * Return the number of bytes needed to store a single element.
	 */
	public int getElementSize();

	/**
	 * Allocate and return a new array to hold the specified number of elements.
	 */
	public T[] allocArray(int count);

	/**
	 * Examine the database at the specified record to decode an element instance.
	 */
	public T decode(QtPDOMLinkage linkage, long record) throws CoreException;

	/**
	 * Encode the given element into the database at the specified record.  The codec is
	 * responsible for releasing storage that is about to be overwritten (if needed).
	 * The element will be null when the implementation should delete all memory used
	 * for storage at record.
	 */
	public void encode(QtPDOMLinkage linkage, long record, T element) throws CoreException;
}

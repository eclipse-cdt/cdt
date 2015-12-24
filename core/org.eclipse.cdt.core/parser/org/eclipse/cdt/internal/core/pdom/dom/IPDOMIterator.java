/*
 * Copyright (c) 2013, 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.cdt.internal.core.pdom.dom;

import java.util.NoSuchElementException;

import org.eclipse.core.runtime.CoreException;

/**
 * A generic interface for iterating through lists that are stored in the PDOM.  The
 * difference between this interface and the standard one in java.util is that this
 * one can throw a CoreException from either method.  Also, this one does not provide
 * a way to remove elements.
 */
public interface IPDOMIterator<T> {
	/**
	 * Return true if the next call to #next will yield a value and false otherwise.
	 *
	 * @see java.util.Iterator#hasNext()
	 */
	public boolean hasNext() throws CoreException;

	/**
	 * Return the next element in the iteration.  Throws {@link NoSuchElementException} if
	 * there are no elements left in the iteration.
	 *
	 * @see java.util.Iterator#next
	 */
	public T next() throws CoreException;
}

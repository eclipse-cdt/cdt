/*******************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.parser;

import java.util.Iterator;
import java.util.NoSuchElementException;

public final class EmptyIterator<T> implements Iterator<T> {
	public static final EmptyIterator<?> EMPTY_ITERATOR = new EmptyIterator<Object>();
	
	@SuppressWarnings("unchecked")
	public static <T> EmptyIterator<T> empty() {
		return (EmptyIterator<T>) EMPTY_ITERATOR;
	}
	
	private EmptyIterator() {
	}
	
    /* (non-Javadoc)
     * @see java.util.Iterator#hasNext()
     */
    @Override
	public final boolean hasNext() {
        return false;
    }

    /* (non-Javadoc)
     * @see java.util.Iterator#next()
     */
    @Override
	public final T next() {
        throw new NoSuchElementException();
    }

    /* (non-Javadoc)
     * @see java.util.Iterator#remove()
     */
    @Override
	public final void remove() {
		throw new UnsupportedOperationException();          
    }
}

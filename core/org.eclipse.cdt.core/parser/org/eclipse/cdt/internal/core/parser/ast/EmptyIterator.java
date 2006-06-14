/*******************************************************************************
 * Copyright (c) 2002, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.parser.ast;

import java.util.Iterator;
import java.util.NoSuchElementException;


public final class EmptyIterator implements Iterator 
{

	public static final EmptyIterator EMPTY_ITERATOR = new EmptyIterator();
	
	private EmptyIterator()
	{
	}
	
    /* (non-Javadoc)
     * @see java.util.Iterator#hasNext()
     */
    public final boolean hasNext()
    {
        return false;
    }

    /* (non-Javadoc)
     * @see java.util.Iterator#next()
     */
    public final Object next()
    {
        throw new NoSuchElementException();
    }

    /* (non-Javadoc)
     * @see java.util.Iterator#remove()
     */
    public final void remove()
    {
		throw new UnsupportedOperationException();          
    }
	
}

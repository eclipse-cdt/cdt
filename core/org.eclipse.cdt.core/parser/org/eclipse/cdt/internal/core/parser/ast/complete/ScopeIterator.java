/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.internal.core.parser.ast.complete;

import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import org.eclipse.cdt.internal.core.parser.pst.ISymbol;
import org.eclipse.cdt.internal.core.parser.pst.ISymbolASTExtension;

/**
 * @author jcamelon
 *
 */
public class ScopeIterator implements Iterator
{
	private final Map sourceMap; 
	private final Iterator keyIter;
	private Iterator subIterator = null;
	
	public ScopeIterator( Map in )
	{
		sourceMap = in;
		if( sourceMap != null )
			keyIter = in.keySet().iterator();
		else
			keyIter = null;
	}

	/* (non-Javadoc)
	 * @see java.util.Iterator#hasNext()
	 */
	public boolean hasNext() {
		if( keyIter  ==  null )
			return false;
		if( subIterator != null && subIterator.hasNext() )
			return true;
		subIterator = null;
		return keyIter.hasNext();
	}

	/* (non-Javadoc)
	 * @see java.util.Iterator#next()
	 */
	public Object next() {
		if( ! hasNext() )
			throw new NoSuchElementException();
		
		if( subIterator != null )
			return subIterator.next();
		
		
		ISymbolASTExtension symbol = ((ISymbol)sourceMap.get( keyIter.next() )).getASTExtension();
		subIterator = symbol.getAllDefinitions();
		return next();
	}

	/* (non-Javadoc)
	 * @see java.util.Iterator#remove()
	 */
	public void remove() {
		throw new UnsupportedOperationException();
	}
}

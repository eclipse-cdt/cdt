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
package org.eclipse.cdt.internal.core.parser.ast;

import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.internal.core.parser.pst.ParserSymbolTable;

/**
 * @author jcamelon
 *
 */
public class BaseIterator implements Iterator {

	private final Iterator rawIter; 
	public BaseIterator( List bases )
	{
		rawIter = bases.iterator();
	}

	/* (non-Javadoc)
	 * @see java.util.Iterator#hasNext()
	 */
	public boolean hasNext() {
		return rawIter.hasNext(); 
	}

	/* (non-Javadoc)
	 * @see java.util.Iterator#next()
	 */
	public Object next() {
		ParserSymbolTable.Declaration.ParentWrapper wrapper = (ParserSymbolTable.Declaration.ParentWrapper)rawIter.next(); 
		return new ASTBaseSpecifier( (IASTClassSpecifier)wrapper.getParent().getASTNode(), wrapper.getAccess(), wrapper.isVirtual());
	}

	/* (non-Javadoc)
	 * @see java.util.Iterator#remove()
	 */
	public void remove() {
		throw new UnsupportedOperationException();
	}

}

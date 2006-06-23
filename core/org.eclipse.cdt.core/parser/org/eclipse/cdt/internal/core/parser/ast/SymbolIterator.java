/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corp. - Rational Software - initial implementation
 *******************************************************************************/
/*
 * Created on Feb 25, 2004
 */
package org.eclipse.cdt.internal.core.parser.ast;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.eclipse.cdt.core.parser.ast.IASTOffsetableElement;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.parser.pst.IExtensibleSymbol;

/**
 * @author aniefer
 */
public class SymbolIterator implements Iterator {
	
	Iterator interalIterator;
	
	IExtensibleSymbol next = null;

	private static final char[] EMPTY_CHAR_ARRAY = "".toCharArray(); //$NON-NLS-1$
	
	public SymbolIterator( Iterator iter ){
		interalIterator = iter;
	}
	
	/* (non-Javadoc)
	 * @see java.util.Iterator#hasNext()
	 */
	public boolean hasNext(){
		if( next != null )
			return true;
		
		while( interalIterator.hasNext() ){
			IExtensibleSymbol symbol = (IExtensibleSymbol) interalIterator.next();
			
			if( symbol.getASTExtension() != null ){
				if( symbol.getASTExtension().getPrimaryDeclaration() instanceof IASTOffsetableElement && 
						CharArrayUtils.equals(((IASTOffsetableElement)symbol.getASTExtension().getPrimaryDeclaration()).getFilename(), EMPTY_CHAR_ARRAY ) )
					continue;

				next = symbol;
				return true;
			}
		} 
		return false;
	}
	
	/* (non-Javadoc)
	 * @see java.util.Iterator#next()
	 */
	public Object next(){
		IExtensibleSymbol temp = next;
		if( next != null ){
			next = null;
			return temp.getASTExtension().getPrimaryDeclaration();
		}
		while( interalIterator.hasNext() ){
			temp = (IExtensibleSymbol) interalIterator.next();

			if( temp.getASTExtension() != null ){
				if( temp.getASTExtension().getPrimaryDeclaration() instanceof IASTOffsetableElement && 
						CharArrayUtils.equals(((IASTOffsetableElement)temp.getASTExtension().getPrimaryDeclaration()).getFilename(), EMPTY_CHAR_ARRAY  ) )
					continue;

				return temp.getASTExtension().getPrimaryDeclaration();
			}
				
		}
		
		throw new NoSuchElementException();
	}
	
	/* (non-Javadoc)
	 * @see java.util.Iterator#remove()
	 */
	public void remove(){
		throw new UnsupportedOperationException();
	}
}


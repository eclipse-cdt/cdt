/*
 * Created on Feb 25, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.eclipse.cdt.internal.core.parser.ast;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.eclipse.cdt.internal.core.parser.pst.IExtensibleSymbol;

/**
 * @author aniefer
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class SymbolIterator implements Iterator {
	
	Iterator interalIterator;
	
	IExtensibleSymbol next = null;
	
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


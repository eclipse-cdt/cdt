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
package org.eclipse.cdt.internal.core.parser.token;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.ITokenDuple;
import org.eclipse.cdt.core.parser.ast.ASTNotImplementedException;
import org.eclipse.cdt.core.parser.ast.IASTFactory;
import org.eclipse.cdt.core.parser.ast.IASTNode;
import org.eclipse.cdt.core.parser.ast.IASTScope;

/**
 * @author jcamelon
 *
 */
public class TokenDuple implements ITokenDuple {

	public TokenDuple( IToken first, IToken last )
	{
//		assert ( first != null && last != null ) : this; 
		firstToken = first; 
		lastToken = last; 
		argLists = null;
	}
	
	public TokenDuple( IToken first, IToken last, List templateArgLists ){
		firstToken = first;
		lastToken = last;
		if( templateArgLists != null && !templateArgLists.isEmpty() ){
			argLists = (List[]) templateArgLists.toArray( new List [templateArgLists.size()] );
		} else {
			argLists = null;
		}
	}
	
	protected final IToken firstToken, lastToken;
	protected final List [] argLists;
	/**
	 * @return
	 */
	public IToken getFirstToken() {
		return firstToken;
	}

	/**
	 * @return
	 */
	public IToken getLastToken() {
		return lastToken;
	}
	
	public Iterator iterator()
	{
		return new TokenIterator(); 
	}
	
	private class TokenIterator implements Iterator
	{
		private IToken iter = TokenDuple.this.firstToken;

		/* (non-Javadoc)
		 * @see java.util.Iterator#hasNext()
		 */
		public boolean hasNext() {
			return ( iter != null );
		}

		/* (non-Javadoc)
		 * @see java.util.Iterator#next()
		 */
		public Object next() {
			if( ! hasNext() )
				throw new NoSuchElementException();
			IToken temp = iter;
			if( iter == lastToken )
				iter = null; 
			else
				iter = iter.getNext();
			return temp;
		}

		/* (non-Javadoc)
		 * @see java.util.Iterator#remove()
		 */
		public void remove() {
			throw new UnsupportedOperationException(); 			
		}
		
	}

	public String toString() 
	{
		StringBuffer buff = new StringBuffer(); 
		IToken prev = null;
		IToken iter = firstToken; 
		for( ; ; )
		{
			if( prev != null && 
			    prev.getType() != IToken.tCOLONCOLON && 
				prev.getType() != IToken.tIDENTIFIER && 
				prev.getType() != IToken.tLT &&
				prev.getType() != IToken.tCOMPL &&
				iter.getType() != IToken.tGT && 
				prev.getType() != IToken.tLBRACKET && 
				iter.getType() != IToken.tRBRACKET && 
				iter.getType() != IToken.tCOLONCOLON )
				buff.append( ' ');
			
				
			buff.append( iter.getImage() );
			if( iter == lastToken ) break;
			prev = iter;
			iter = iter.getNext();
		}
		return buff.toString().trim();
	}
	
	public boolean isIdentifier()
	{
		return ( (firstToken == lastToken ) && (firstToken.getType() == IToken.tIDENTIFIER ));
	}

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ITokenDuple#length()
     */
    public int length()
    {
        int count = 0; 
        Iterator i = iterator();
        while( i.hasNext() )
        {
        	++count;
        	i.next();
        }
        return count;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ITokenDuple#getSubDuple(int, int)
     */
    public ITokenDuple getSubrange(int startIndex, int endIndex)
    {
		return new TokenDuple( getToken( startIndex ), getToken( endIndex) );
    }

    /**
     * @param endIndex
     */
    public IToken getToken(int index)
    {
        if( index < 0 || index >= length() ) return null;
        Iterator i = iterator();
        int count = 0;  
        while( i.hasNext() )
        {
        	IToken r = (IToken)i.next();
        	if( count == index )
        		return r;
        	++count; 
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ITokenDuple#findLastTokenType(int)
     */
    public int findLastTokenType(int type)
    {
		int count = 0; 
		int lastFound = -1;
        Iterator i = iterator();
        while( i.hasNext() )
        {
        	IToken token = (IToken)i.next();
        	if( token.getType() == type )
        		lastFound = count; 
        	++count;
        }
        
        return lastFound;
    }

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ITokenDuple#lookup(org.eclipse.cdt.core.parser.ast.IASTFactory)
	 */
	public IASTNode lookup(IASTFactory factory, IASTScope scope) {
		// check syntax of the node
		
		try {
			return factory.lookupSymbolInContext(scope, this );
		} catch (ASTNotImplementedException e) {
			return null;
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ITokenDuple#getEndOffset()
	 */
	public int getEndOffset() {
		return getLastToken().getEndOffset();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ITokenDuple#getLineNumber()
	 */
	public int getLineNumber() {
		return getFirstToken().getLineNumber();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ITokenDuple#getStartOffset()
	 */
	public int getStartOffset() {
		return getFirstToken().getOffset();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ITokenDuple#getTemplateIdArgLists()
	 */
	public List[] getTemplateIdArgLists() {
		return argLists;
	}
}

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
package org.eclipse.cdt.internal.core.parser;

import java.util.Iterator;

import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.ITokenDuple;

/**
 * @author jcamelon
 *
 */
public class TokenDuple implements ITokenDuple {

	public TokenDuple( IToken first, IToken last )
	{
		firstToken = first; 
		lastToken = last; 
	}
	private final IToken firstToken, lastToken; 
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
			return ( iter != TokenDuple.this.lastToken);
		}

		/* (non-Javadoc)
		 * @see java.util.Iterator#next()
		 */
		public Object next() {
			IToken temp = iter;
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
		IToken iter = firstToken; 
		for( ; ; )
		{
			buff.append( iter.getImage() );
			if( iter == lastToken ) break;
			iter = iter.getNext();
		}
		return buff.toString();
	}
	
	public boolean isIdentifier()
	{
		return ( firstToken == lastToken );
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
	
}

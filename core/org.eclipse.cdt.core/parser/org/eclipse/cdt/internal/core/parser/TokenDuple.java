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

/**
 * @author jcamelon
 *
 */
public class TokenDuple {

	public TokenDuple( Token first, Token last )
	{
		firstToken = first; 
		lastToken = last; 
	}
	private final Token firstToken, lastToken; 
	/**
	 * @return
	 */
	public Token getFirstToken() {
		return firstToken;
	}

	/**
	 * @return
	 */
	public Token getLastToken() {
		return lastToken;
	}
	
	public Iterator iterator()
	{
		return new TokenIterator(); 
	}
	
	private class TokenIterator implements Iterator
	{
		private Token iter = TokenDuple.this.firstToken;

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
			Token temp = iter;
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
		Token iter = firstToken; 
		for( ; ; )
		{
			buff.append( iter.getImage() );
			if( iter == lastToken ) break;
			iter = iter.getNext();
		}
		return buff.toString();
	}
}

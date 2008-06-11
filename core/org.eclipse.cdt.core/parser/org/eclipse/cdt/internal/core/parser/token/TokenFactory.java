/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Rational Software - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.parser.token;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.ITokenDuple;

/**
 * @author johnc
 */
public class TokenFactory {
	protected static final char[] EMPTY_CHAR_ARRAY = "".toCharArray(); //$NON-NLS-1$

	private static class TokenWrapper implements ITokenDuple {
		private final IToken fToken;

		public TokenWrapper(IToken t) {
			fToken= t;
		}
        
        @Override
		public String toString() {
            return fToken.toString();
        }
        public char[] extractNameFromTemplateId(){
            return fToken.getCharImage();
        }
        public IToken getFirstToken() {
            return fToken;
        }
        public ITokenDuple getLastSegment() {
            return this;
        }
        public IToken getLastToken() {
            return fToken;
        }
        public ITokenDuple getLeadingSegments() {
            return null;
        }
        public int getSegmentCount() {
            return 1;
        }
        public int getStartOffset() {
            return fToken.getOffset();
        }
        public List[] getTemplateIdArgLists() {
            return null;
        }
        public IToken getToken(int index) {
            if( index == 0 ) return fToken;
            return null;
        }        
        public Iterator iterator() {
            return new Iterator() {
                private boolean hasNext = true;
                public void remove() {
                    throw new UnsupportedOperationException();
                }
                public boolean hasNext() {
                    return hasNext;
                }
                public Object next() {
                    hasNext = false;
                    return fToken;
                }
            };
        }
        public int length() {
            return 1;
        }
        public ITokenDuple[] getSegments() {
        	return new ITokenDuple[] {this};
        }
		public int getEndOffset() {
			return fToken.getEndOffset();
		}
		public char[] toCharArray() {
			return fToken.getCharImage();
		}
	}
	
	public static ITokenDuple createTokenDuple(IToken first, IToken last) {
		if (first == last) {
			if (first instanceof ITokenDuple) {
				return (ITokenDuple) first;
			}
			return new TokenWrapper(first);
		}
		return new BasicTokenDuple( first, last );
	}

	public static ITokenDuple createTokenDuple(IToken first, IToken last, List templateArgLists) {
		if (templateArgLists == null || templateArgLists.isEmpty()) {
			return createTokenDuple(first, last);
		}
		return new TemplateTokenDuple( first, last, templateArgLists );
	}

	public static ITokenDuple createTokenDuple( ITokenDuple firstDuple, ITokenDuple secondDuple ){
		if( secondDuple == null ) return firstDuple;
		if( firstDuple == null ) return secondDuple;
		List [] f1 = firstDuple.getTemplateIdArgLists();
		List [] f2 = secondDuple.getTemplateIdArgLists();
		if( f1 == null && f2 == null )
			return new BasicTokenDuple( firstDuple, secondDuple );
		return new TemplateTokenDuple( firstDuple, secondDuple );
	}
	
	public static IToken consumeTemplateIdArguments( IToken name, IToken last ){
	    IToken token = name;
	    
	    if( token.getType() == IToken.tLT )
	    {
	    	if( token == last )
	    		return token;
	    		    	
	    	BraceCounter scopes = BraceCounter.getCounter();
	    	try
			{		    	
		        scopes.addValue( IToken.tLT );
		        
		        while (!scopes.isEmpty() && token != last )
		        {
		        	int top;
		        	
		        	token = token.getNext();
		        	switch( token.getType() ){
		        		case IToken.tGT:
		        			if( scopes.getLast() == IToken.tLT ) {
								scopes.removeValue();
							}
		                    break;
		        		case IToken.tRBRACKET :
							do {
								top = scopes.removeValue();
							} while (!scopes.isEmpty() && top == IToken.tLT);
							break;
		        		case IToken.tRPAREN :
							do {
								top = scopes.removeValue();
							} while (!scopes.isEmpty() && top == IToken.tLT);
							break;
		                case IToken.tLT:		scopes.addValue( IToken.tLT );		break;
						case IToken.tLBRACKET:	scopes.addValue( IToken.tLBRACKET );	break;
						case IToken.tLPAREN:	scopes.addValue( IToken.tLPAREN );   break;
		        	}
		        }
			}
	    	finally
			{
	    		BraceCounter.returnCounter(scopes);
			}
	    }
	   
	    return token;
	}
	
	protected static class BraceCounter
	{
		private static final int POOLSIZE = 8;
		private static final BraceCounter [] pool;
		private static final boolean [] free;
		private static int newObjectCount = POOLSIZE;

		static
		{
			pool = new BraceCounter[ POOLSIZE ];
			free = new boolean[8];
			for( int i = 0; i < POOLSIZE; ++i )
			{
				pool[i] = new BraceCounter(i);
				free[i] = true;
			}
		}	

		public static synchronized BraceCounter getCounter()
		{
			for( int i = 0; i < POOLSIZE; ++i )
			{
				if( free[i] )
				{
					free[i] = false;
					return pool[i];
				}
			}
			//if there is nothing free, allocate a new one and return it
			return new BraceCounter(newObjectCount++);
		}
		
		public static synchronized void returnCounter( BraceCounter c )
		{
			if( c.getKey() > 0 && c.getKey() < POOLSIZE )
			{
				free[ c.getKey() ] = true;
				c.clear();
			}
			// otherwise, the object shall get garbage collected
		}

		/**
		 * 
		 */
		private void clear() {
			currentIndex = 0;
			Arrays.fill( array, 0, array.length, -1 );
		}

		private final int key;
		private int [] array = new int[ 8 ];
		int currentIndex = 0;
		
		private void resizeArray()
		{
			int [] newArray = new int[ (array.length * 2) ];
			System.arraycopy( array, 0, newArray, 0, array.length );
			array = newArray;
		}
		
		public void addValue( int value )
		{
			if( currentIndex == array.length )
				resizeArray();
			array[currentIndex] = value;
			++currentIndex;
		}
		
		public int removeValue()
		{
			int result = array[--currentIndex];
			array[currentIndex] = -1;
			return result;
		}
		
		public int getLast()
		{
			if( isEmpty() ) return -1;
			return array[ currentIndex - 1 ];
		}
		
		public boolean isEmpty() {
			return (currentIndex == 0 );
		}

		public BraceCounter(int i) {
			key = i; 
			clear();
		}
		/**
		 * @return Returns the key.
		 */
		public int getKey() {
			return key;
		}
		
		
	}

	public static char[] createCharArrayRepresentation(IToken first, IToken last) {
		return BasicTokenDuple.createCharArrayRepresentation(first, last);
	}
}

/**********************************************************************
 * Copyright (c) 2002-2004 IBM Canada and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.internal.core.parser.token;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.ITokenDuple;
import org.eclipse.cdt.internal.core.parser.scanner.IScannerContext;
import org.eclipse.cdt.internal.core.parser.scanner.IScannerData;

/**
 * @author johnc
 */
public class TokenFactory {
		
	public static IToken createToken( int tokenType, IScannerData scannerData )
	{
		if( scannerData.getContextStack().getCurrentContext().getKind() == IScannerContext.ContextKind.MACROEXPANSION )
			return new SimpleExpansionToken( tokenType, scannerData.getContextStack() );
		
		return new SimpleToken(	tokenType, scannerData.getContextStack() );
	}

	/**
	 * @param type
	 * @param image
	 * @param scannerData
	 * @return
	 */
	public static IToken createUniquelyImagedToken(int type, String image, IScannerData scannerData) {
		if( scannerData.getContextStack().getCurrentContext().getKind() == IScannerContext.ContextKind.MACROEXPANSION )
			return new ImagedExpansionToken( type, scannerData.getContextStack(), image );

		return new ImagedToken(type, scannerData.getContextStack(), image );
	}
	
	public static IToken createStandAloneToken( int type, String image )
	{
		return new ImagedToken( type, image);
	}

	public static ITokenDuple createTokenDuple( IToken first, IToken last )
	{
		if( (first == last) && ( first instanceof ITokenDuple )) return (ITokenDuple) first;
		return new BasicTokenDuple( first, last );
	}

	public static ITokenDuple createTokenDuple( IToken first, IToken last, List templateArgLists )
	{
		if( (first == last) && ( templateArgLists == null ) && ( first instanceof ITokenDuple )) 
			return (ITokenDuple) first;
		if( templateArgLists != null && !templateArgLists.isEmpty())
			return new TemplateTokenDuple( first, last, templateArgLists );
		return new BasicTokenDuple( first, last );
	}

	public static ITokenDuple createTokenDuple( ITokenDuple firstDuple, ITokenDuple secondDuple ){
		List [] f1 = firstDuple.getTemplateIdArgLists();
		List [] f2 = secondDuple.getTemplateIdArgLists();
		if( f1 == null && f2 == null )
			return new BasicTokenDuple( firstDuple, secondDuple );
		return new TemplateTokenDuple( firstDuple, secondDuple );
	}
	
	public static IToken consumeTemplateIdArguments( IToken name, Iterator iter ){
	    IToken token = name;
	    
	    if( token.getType() == IToken.tLT )
	    {
	    	if( ! iter.hasNext() )
	    		return token;
	    		    	
	    	BraceCounter scopes = BraceCounter.getCounter();
	    	try
			{		    	
		        scopes.addValue( IToken.tLT );
		        
		        while (!scopes.isEmpty() && iter.hasNext() )
		        {
		        	int top;
		        	
		        	token = (IToken) iter.next();
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
			int result = array[currentIndex];
			array[currentIndex] = -1;
			--currentIndex;
			return result;
		}
		
		public int getLast()
		{
			if( isEmpty() ) return -1;
			return array[ currentIndex - 1 ];
		}
		
		/**
		 * @return
		 */
		public boolean isEmpty() {
			return (currentIndex == 0 );
		}

		/**
		 * @param i
		 */
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

	/**
	 * @param first
	 * @param last
	 * @return
	 */
	public static String createStringRepresentation(IToken first, IToken last) {
		return BasicTokenDuple.createStringRepresentation(first, last);
	}
}

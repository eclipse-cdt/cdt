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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.ITokenDuple;

/**
 * @author jcamelon
 *
 */
public class TokenDuple implements ITokenDuple {

	TokenDuple( IToken first, IToken last )
	{
//		assert ( first != null && last != null ) : this; 
		firstToken = first; 
		lastToken = last; 
		argLists = null;
	}
	
	TokenDuple( IToken first, IToken last, List templateArgLists ){
		firstToken = first;
		lastToken = last;
		if( templateArgLists != null && !templateArgLists.isEmpty() ){
			argLists = (List[]) templateArgLists.toArray( new List [templateArgLists.size()] );
		} else {
			argLists = null;
		}
	}
	
	TokenDuple( ITokenDuple firstDuple, ITokenDuple secondDuple ){
		firstToken = firstDuple.getFirstToken();
		lastToken = secondDuple.getLastToken();
		
		List [] a1 = firstDuple.getTemplateIdArgLists();
		List [] a2 = secondDuple.getTemplateIdArgLists();
		
		if( a1 == null && a2 == null ){
			argLists = null;
		} else {
			int l1 = ( a1 != null ) ? a1.length : firstDuple.getSegmentCount();
			int l2 = ( a2 != null ) ? a2.length : firstDuple.getSegmentCount();

			argLists = new List[ l1 + l2 ];
			if( a1 != null )
				System.arraycopy( a1, 0, argLists, 0, l1 );
			if( a2 != null )
				System.arraycopy( a2, 0, argLists, l1, l2 );
		}
	}
	
	protected final IToken firstToken, lastToken;
	protected final List [] argLists;
	private int numSegments = -1;
	
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
	
	public ITokenDuple getLastSegment() {
		Iterator iter = iterator();
		if( !iter.hasNext() )
			return null;
		
		IToken first = null, last = null, token = null;
		while( iter.hasNext() ){
			token = (IToken) iter.next();
			if( first == null )
				first = token;
			if( token.getType() == IToken.tLT )
				token = consumeTemplateIdArguments( token, iter );
			else if( token.getType() == IToken.tCOLONCOLON ){
				first = null;
				continue;
			}
			last = token;
		}
		
		List [] args = getTemplateIdArgLists();
		if( args != null && args[ args.length - 1 ] != null ){
			List newArgs = new ArrayList( 1 );
			newArgs.add( args[ args.length - 1 ] );
			return new TokenDuple( first, last, newArgs );
		} 
		return new TokenDuple( first, last );
		
	}
	
	public ITokenDuple getLeadingSegments(){
		Iterator iter = iterator();
		if( !iter.hasNext() )
			return null;
		
		int num = getSegmentCount();
		
		if( num <= 1 )
			return null;
		
		IToken first = null, last = null;
		IToken previous = null, token = null;

		while( iter.hasNext() ){
			token = (IToken) iter.next();
			if( first == null )
				first = token;
			if( token.getType() == IToken.tLT )
				token = consumeTemplateIdArguments( token, iter );
			else if( token.getType() == IToken.tCOLONCOLON ){
				last = previous;
				continue;
			}
			
			previous = token;
		}
		
		if( last == null ){
			//"::A"
			return null;
		}
		
		if( getTemplateIdArgLists() != null ){
			List[] args = getTemplateIdArgLists();
			List newArgs = new ArrayList( args.length - 1 );
			boolean foundArgs = false;
			for( int i = 0; i < args.length - 1; i++ ){
				newArgs.add( args[i] );
				if( args[i] != null ) 
					foundArgs = true;
			}
			return new TokenDuple( first, last, ( foundArgs ? newArgs : null ) );
		} 
		return new TokenDuple( first, last );
	}
	
	public int getSegmentCount()
	{
		if( numSegments > -1 )
			return numSegments;
		
		numSegments = 1;
		
		if( firstToken == lastToken )
			return numSegments;
		
		Iterator iter = iterator();
		
		IToken token = null;
		while( iter.hasNext() ){
			token = (IToken) iter.next();
			if( token.getType() == IToken.tLT )
				token = consumeTemplateIdArguments( token, iter );
			if( token.getType() == IToken.tCOLONCOLON ){
				numSegments++;
				continue;
			}
		}
		return numSegments;
	}
	
	private static final Integer LT = new Integer( IToken.tLT );
	private static final Integer LBRACKET = new Integer( IToken.tLBRACKET );
	private static final Integer LPAREN = new Integer( IToken.tLPAREN );
	private String [] qualifiedName = null;
	private static final String EMPTY_STRING = ""; //$NON-NLS-1$
	private String stringRepresentation = null;

	public IToken consumeTemplateIdArguments( IToken name, Iterator iter ){
	    IToken token = name;
	    
	    if( token.getType() == IToken.tLT )
	    {
	    	if( ! iter.hasNext() )
	    		return token;
	    	
	    	LinkedList scopes = new LinkedList();
	        scopes.add( LT );
	        
	        while (!scopes.isEmpty() && iter.hasNext() )
	        {
	        	Integer top;
	        	
	        	token = (IToken) iter.next();
	        	switch( token.getType() ){
	        		case IToken.tGT:
	        			if( scopes.getLast() == LT ) {
							scopes.removeLast();
						}
	                    break;
	        		case IToken.tRBRACKET :
						do {
							top = (Integer)scopes.removeLast();
						} while (!scopes.isEmpty() && top == LT);
						break;
	        		case IToken.tRPAREN :
						do {
							top = (Integer)scopes.removeLast();
						} while (!scopes.isEmpty() && top == LT);
						break;
	                case IToken.tLT:		scopes.add( LT );		break;
					case IToken.tLBRACKET:	scopes.add( LBRACKET );	break;
					case IToken.tLPAREN:	scopes.add( LPAREN );   break;
	        	}
	        }
	    }
	   
	    return token;
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

	public static String createStringRepresentation( IToken f, IToken l)
	{
		StringBuffer buff = new StringBuffer(); 
		IToken prev = null;
		IToken iter = f; 
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
			
			if( iter == null ) return EMPTY_STRING;
			buff.append( iter.getImage() );
			if( iter == l ) break;
			prev = iter;
			iter = iter.getNext();
		}
		return buff.toString();
		
	}
	
	public String toString() 
	{
		if( stringRepresentation == null )
			stringRepresentation = createStringRepresentation(firstToken, lastToken);
		return stringRepresentation;
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

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ITokenDuple#syntaxOfName()
	 */
	public boolean syntaxOfName() {
		Iterator iter = iterator();
		if( ! iter.hasNext() ) return false; // empty is not good
		while( iter.hasNext() )
		{
			IToken token = (IToken) iter.next();
			if( token.isOperator() ) continue;
			switch( token.getType() )
			{
				case IToken.tCOMPL:
				case IToken.tIDENTIFIER:
				case IToken.tCOLONCOLON:
				case IToken.t_operator:
					continue;
				default:
					return false;
			}
		}
		return true;
	}
	
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object other) {
		if( !(other instanceof ITokenDuple ) ) return false;
		if( ((ITokenDuple) other).getFirstToken().equals( getFirstToken() ) &&
			((ITokenDuple) other).getLastToken().equals( getLastToken() ) )
			return true;
		return false;
	}
	
	 public String extractNameFromTemplateId(){
	 	ITokenDuple nameDuple = getLastSegment(); 
	 	
    	Iterator i = nameDuple.iterator();
    	
    	if( !i.hasNext() )
    		return "";//$NON-NLS-1$
    	
    	StringBuffer nameBuffer = new StringBuffer();
    	IToken token = (IToken) i.next();
    	nameBuffer.append( token.getImage() );
    	
    	if( !i.hasNext() )
    		return nameBuffer.toString();
		
    	//appending of spaces needs to be the same as in toString()
    	    	
    	//destructors
    	if( token.getType() == IToken.tCOMPL ){
    		token = (IToken) i.next();
    		nameBuffer.append( token.getImage() );
    	} 
    	//operators
    	else if( token.getType() == IToken.t_operator ){
    		token = (IToken) i.next();
    		nameBuffer.append( ' ' );
    		nameBuffer.append( token.getImage() );
    		
    		if( !i.hasNext() )
        		return nameBuffer.toString();
    		
    		//operator new [] and operator delete []
    		if( (token.getType() == IToken.t_new || token.getType() == IToken.t_delete) &&
    			(token.getNext().getType() == IToken.tLBRACKET ) )
    		{
    			nameBuffer.append( ' ' );
    			nameBuffer.append( ((IToken)i.next()).getImage() );
    			nameBuffer.append( ((IToken)i.next()).getImage() );
    		}
    		//operator []
    		else if( token.getType() == IToken.tLBRACKET )
			{
    			nameBuffer.append( ((IToken)i.next()).getImage() );
			}
    		//operator ( )
    		else if( token.getType() == IToken.tLBRACE )
    		{
    			nameBuffer.append( ' ' );
    			nameBuffer.append( ((IToken)i.next()).getImage() );
    		}
    	}
    	
    	return nameBuffer.toString();
    }

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ITokenDuple#contains(org.eclipse.cdt.core.parser.ITokenDuple)
	 */
	public boolean contains(ITokenDuple duple) {
		if( duple == null ) return false;
		boolean foundFirst = false;
		boolean foundLast = false;
		Iterator i = iterator();
		while( i.hasNext() )
		{
			IToken current = (IToken) i.next();
			if( current == firstToken ) foundFirst = true;
			if( current == lastToken ) foundLast = true;
			if( foundFirst && foundLast ) break;
		}

		return ( foundFirst && foundLast );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ITokenDuple#toQualifiedName()
	 */
	public String[] toQualifiedName() {
		if( qualifiedName == null )
			generateQualifiedName();
		return qualifiedName;
	}

	/**
	 * 
	 */
	private void generateQualifiedName() {
		List qn = new ArrayList();
		Iterator i = iterator();
		while( i.hasNext() )
		{
			IToken t = (IToken) i.next();
			boolean compl = false;
			if( t.getType() == IToken.tCOLONCOLON ) continue;
			if( t.getType() == IToken.tCOMPL )
			{
				compl = true;
				if( !i.hasNext() ) break;
				t = (IToken) i.next();
			}
			if( t.getType() == IToken.tIDENTIFIER )
			{
				if( compl )
					qn.add( "~" + t.getImage() ); //$NON-NLS-1$
				else
					qn.add( t.getImage() );
			}
		}
		qualifiedName = new String[ qn.size() ];
		qualifiedName = (String[]) qn.toArray( qualifiedName );
		
	}

	
}

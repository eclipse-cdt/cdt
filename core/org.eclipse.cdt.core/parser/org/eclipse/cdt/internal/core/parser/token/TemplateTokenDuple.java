/**********************************************************************
 * Copyright (c) 2002-2004 Rational Software Corporation and others.
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
import java.util.List;

import org.eclipse.cdt.core.parser.ISourceElementRequestor;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.ITokenDuple;
import org.eclipse.cdt.core.parser.ast.IASTExpression;

/**
 * @author jcamelon
 *
 */
public class TemplateTokenDuple extends BasicTokenDuple {

	protected final List [] argLists;
	private final int numSegments;

	/**
	 * @param first
	 * @param last
	 * @param templateArgLists
	 */
	public TemplateTokenDuple(IToken first, IToken last, List templateArgLists) {
		super(first, last);
		argLists = (List[]) templateArgLists.toArray( new List [templateArgLists.size()] );
		numSegments = calculateSegmentCount();
	}
	
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ITokenDuple#getSegmentCount()
	 */
	public int getSegmentCount() {
		return numSegments;
	}
	public ITokenDuple getLastSegment()
	{
		IToken first = null, last = null, token = null;
		for( ; ; ){
		    if( token == getLastToken() )
		        break;
			token = ( token != null ) ? token.getNext() : getFirstToken();
			if( first == null )
				first = token;
			if( token.getType() == IToken.tLT )
				token = TokenFactory.consumeTemplateIdArguments( token, getLastToken() );
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
			return TokenFactory.createTokenDuple( first, last, newArgs );
		} 
		return TokenFactory.createTokenDuple( first, last );
		
	}

	public TemplateTokenDuple( ITokenDuple first, ITokenDuple last )
	{
		super( first, last );
		List [] a1 = first.getTemplateIdArgLists();
		List [] a2 = last.getTemplateIdArgLists();
		
		int l1 = ( a1 != null ) ? a1.length : first.getSegmentCount();
		int l2 = ( a2 != null ) ? a2.length : first.getSegmentCount();
		argLists = new List[ l1 + l2 ];
		if( a1 != null )
			System.arraycopy( a1, 0, argLists, 0, l1 );
		if( a2 != null )
			System.arraycopy( a2, 0, argLists, l1, l2 );
		numSegments = calculateSegmentCount();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ITokenDuple#getTemplateIdArgLists()
	 */
	public List[] getTemplateIdArgLists() {
		return argLists;
	}
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ITokenDuple#freeReferences(org.eclipse.cdt.core.parser.ast.IReferenceManager)
	 */
	public void freeReferences() {
		if( argLists == null ) return;
		for( int i = 0; i < argLists.length; ++i )
		{
			if( argLists[i] == null ) continue;
			for( int j = 0; j < argLists[i].size(); ++ j )
			{
				IASTExpression e = (IASTExpression) argLists[i].get(j);
				if( e != null )
				    e.freeReferences();
				
			}
		}
	}
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ITokenDuple#acceptElement(org.eclipse.cdt.core.parser.ast.IReferenceManager)
	 */
	public void acceptElement(ISourceElementRequestor requestor) {
		if( argLists == null ) return;
		for( int i = 0; i < argLists.length; ++i )
		{
			if( argLists[i] == null ) continue;
			for( int j = 0; j < argLists[i].size(); ++ j )
			{
				IASTExpression e = (IASTExpression) argLists[i].get(j);
				e.acceptElement(requestor);
			}
		}
	}
	
	public ITokenDuple[] getSegments()
	{
		List r = new ArrayList();
		IToken token = null;
		IToken prev = null;
		IToken last = getLastToken();
		IToken startOfSegment = getFirstToken();
		int count = 0;
		for( ;; ){
		    if( token == last )
		        break;
		    prev = token;
			token = ( token != null ) ? token.getNext() : getFirstToken();
			if( token.getType() == IToken.tLT )
				token = TokenFactory.consumeTemplateIdArguments( token, last );
			if( token.getType() == IToken.tCOLONCOLON  ){
			    List newArgs = null;
			    if( argLists[count] != null )
			    {
			        newArgs = new ArrayList( 1 );
			        newArgs.add( argLists[count]);
			    }
			    ITokenDuple d = TokenFactory.createTokenDuple( startOfSegment, prev != null ? prev : startOfSegment, newArgs );
			    r.add( d );
			    startOfSegment = (token != last ) ? token.getNext() : last;
			    ++count;
				continue;
			}
		}
	    List newArgs = null;
	    //pointer to members could have a A::B<int>::
	    if( count < argLists.length && argLists[count] != null )
	    {
	        newArgs = new ArrayList( 1 );
	        newArgs.add( argLists[count]);
	    }
		ITokenDuple d = TokenFactory.createTokenDuple( startOfSegment, last, newArgs);
		r.add( d );
		return (ITokenDuple[]) r.toArray( new ITokenDuple[ r.size() ]);

	}

}

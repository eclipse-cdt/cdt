/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.parser.token;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.ITokenDuple;

/**
 * @author jcamelon
 *
 */
public class TemplateTokenDuple extends BasicTokenDuple {

	protected final List<IASTNode>[] argLists;

	/**
	 * @param first
	 * @param last
	 * @param templateArgLists
	 */
	public TemplateTokenDuple(IToken first, IToken last, List<List<IASTNode>> templateArgLists) {
		super(first, last);
		argLists = toArray(templateArgLists);
		numSegments = calculateSegmentCount();
	}

	@SuppressWarnings("unchecked")
	private <T> List<T>[] toArray(List<List<T>> templateArgLists) {
		return templateArgLists.toArray( new List[templateArgLists.size()] );
	}
	@SuppressWarnings("unchecked")
	private <T> List<T>[] newArrayOfLists(int size) {
		return new List[size];
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ITokenDuple#getSegmentCount()
	 */
	@Override
	public int getSegmentCount() {
		return numSegments;
	}
	@Override
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
		
		List<IASTNode>[] args = getTemplateIdArgLists();
		if( args != null && args[ args.length - 1 ] != null ){
			List<List<IASTNode>> newArgs = new ArrayList<List<IASTNode>>( 1 );
			newArgs.add( args[ args.length - 1 ] );
			return TokenFactory.createTokenDuple( first, last, newArgs );
		} 
		return TokenFactory.createTokenDuple( first, last );
		
	}

	public TemplateTokenDuple( ITokenDuple first, ITokenDuple last )
	{
		super( first, last );
		List<IASTNode>[] a1 = first.getTemplateIdArgLists();
		List<IASTNode>[] a2 = last.getTemplateIdArgLists();
		
		int l1 = ( a1 != null ) ? a1.length : first.getSegmentCount();
		int l2 = ( a2 != null ) ? a2.length : first.getSegmentCount();
		argLists = newArrayOfLists(l1 + l2);
		if( a1 != null )
			System.arraycopy( a1, 0, argLists, 0, l1 );
		if( a2 != null )
			System.arraycopy( a2, 0, argLists, l1, l2 );
		numSegments = calculateSegmentCount();
	}


	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ITokenDuple#getTemplateIdArgLists()
	 */
	@Override
	public List<IASTNode>[] getTemplateIdArgLists() {
		return argLists;
	}
	
	@Override
	public ITokenDuple[] getSegments()
	{
		List<ITokenDuple> r = new ArrayList<ITokenDuple>();
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
			    List<List<IASTNode>> newArgs = null;
			    if( argLists[count] != null )
			    {
			        newArgs = new ArrayList<List<IASTNode>>(1);
			        newArgs.add( argLists[count]);
			    }
			    ITokenDuple d = TokenFactory.createTokenDuple( startOfSegment, prev != null ? prev : startOfSegment, newArgs );
			    r.add( d );
			    startOfSegment = (token != last ) ? token.getNext() : last;
			    ++count;
				continue;
			}
		}
		List<List<IASTNode>> newArgs = null;
	    //pointer to members could have a A::B<int>::
	    if( count < argLists.length && argLists[count] != null )
	    {
	        newArgs = new ArrayList<List<IASTNode>>(1);
	        newArgs.add(argLists[count]);
	    }
		ITokenDuple d = TokenFactory.createTokenDuple( startOfSegment, last, newArgs);
		r.add( d );
		return r.toArray( new ITokenDuple[ r.size() ]);

	}

}

/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corp. - Rational Software - initial implementation
 ******************************************************************************/
/*
 * Created on Jul 11, 2003
 */
package org.eclipse.cdt.internal.core.search.matching;

import java.io.IOException;
import java.util.Iterator;

import org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate;
import org.eclipse.cdt.core.parser.ast.IASTFunction;
import org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement;
import org.eclipse.cdt.core.parser.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTSimpleTypeSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTTypeSpecifier;
import org.eclipse.cdt.core.search.ICSearchScope;
import org.eclipse.cdt.internal.core.index.IEntryResult;
import org.eclipse.cdt.internal.core.index.impl.IndexInput;
import org.eclipse.cdt.internal.core.search.CharOperation;
import org.eclipse.cdt.internal.core.search.IIndexSearchRequestor;
import org.eclipse.cdt.internal.core.search.indexing.AbstractIndexer;

/**
 * @author aniefer
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class FunctionDeclarationPattern extends CSearchPattern {

	protected char[] decodedSimpleName;
	protected char[] simpleName;
	
	protected char[][] parameterNames;

	

	public FunctionDeclarationPattern(char[] name, char [][] params, int matchMode, LimitTo limitTo, boolean caseSensitive) {
		super( matchMode, caseSensitive, limitTo );
		
		simpleName = name;
		parameterNames = params;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.search.ICSearchPattern#matchLevel(org.eclipse.cdt.core.parser.ast.IASTOffsetableElement)
	 */
	public int matchLevel(ISourceElementCallbackDelegate node) {
		if( !( node instanceof IASTFunction ) )
			return IMPOSSIBLE_MATCH;
			
		IASTFunction function = (IASTFunction) node;
		String nodeName = function.getName();
		
		//check name, if simpleName == null, its treated the same as "*"	
		if( simpleName != null && !matchesName( simpleName, nodeName.toCharArray() ) ){
			return IMPOSSIBLE_MATCH;
		}
		
		if( parameterNames != null ){
			Iterator params = function.getParameters();
			
			
			for( int i = 0; i < parameterNames.length; i++ ){
			
				//if this function doesn't have this many parameters, it is not a match.
				//or if this function has a parameter, but parameterNames only has null.
				if( !params.hasNext() || parameterNames[ i ] == null )
					return IMPOSSIBLE_MATCH;
					
				IASTParameterDeclaration param = (IASTParameterDeclaration) params.next();
				IASTTypeSpecifier typeSpec = param.getTypeSpecifier();
				String paramName = null;
				if( typeSpec instanceof IASTSimpleTypeSpecifier ){
					paramName = ((IASTSimpleTypeSpecifier)typeSpec).getTypename();
				} else if( typeSpec instanceof IASTOffsetableNamedElement ){
					paramName = ((IASTOffsetableNamedElement)typeSpec).getName();
				} else {
					//???
					return IMPOSSIBLE_MATCH;
				}
				
				if( !matchesName( parameterNames[i], paramName.toCharArray() ) )
					return IMPOSSIBLE_MATCH;
			}
			//if this function still has more parameters, it is not a match
			if( params.hasNext() )
				return IMPOSSIBLE_MATCH;
		}
		
		return ACCURATE_MATCH;
	}


	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.search.matching.CSearchPattern#feedIndexRequestor(org.eclipse.cdt.internal.core.search.IIndexSearchRequestor, int, int[], org.eclipse.cdt.internal.core.index.impl.IndexInput, org.eclipse.cdt.core.search.ICSearchScope)
	 */
	public void feedIndexRequestor(IIndexSearchRequestor requestor, int detailLevel, int[] references, IndexInput input, ICSearchScope scope) throws IOException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.search.matching.CSearchPattern#decodeIndexEntry(org.eclipse.cdt.internal.core.index.IEntryResult)
	 */
	protected void decodeIndexEntry(IEntryResult entryResult) {
		char[] word = entryResult.getWord();
		int size = word.length;
		
		int firstSlash = CharOperation.indexOf( SEPARATOR, word, 0 );
		
		int slash = CharOperation.indexOf( SEPARATOR, word, firstSlash + 1 );
		
		this.decodedSimpleName = CharOperation.subarray(word, firstSlash + 1, slash);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.search.matching.CSearchPattern#indexEntryPrefix()
	 */
	public char[] indexEntryPrefix() {
		return AbstractIndexer.bestFunctionPrefix( _limitTo, simpleName, _matchMode, _caseSensitive );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.search.matching.CSearchPattern#matchIndexEntry()
	 */
	protected boolean matchIndexEntry() {
		/* check simple name matches */
		if (simpleName != null){
			if( ! matchesName( simpleName, decodedSimpleName ) ){
				return false; 
			}
		}
		
		return true;
	}
}

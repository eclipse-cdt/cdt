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

import org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate;
import org.eclipse.cdt.core.parser.ast.*;
import org.eclipse.cdt.internal.core.search.indexing.AbstractIndexer;

/**
 * @author aniefer
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class MethodDeclarationPattern extends FunctionDeclarationPattern {

	private char[][] qualifications;


	public MethodDeclarationPattern(char[] name, char[][] qual, char [][] params, int matchMode, LimitTo limitTo, boolean caseSensitive) {
		super( name, params, matchMode, limitTo, caseSensitive );
		qualifications = qual;
	}


	public int matchLevel(ISourceElementCallbackDelegate node) {
		if( !(node instanceof IASTMethod) ){
			return IMPOSSIBLE_MATCH;
		}
		
		if( super.matchLevel( node ) == IMPOSSIBLE_MATCH ){
			return IMPOSSIBLE_MATCH;
		}
		
		//check containing scopes
		String [] fullyQualifiedName = ((IASTQualifiedNameElement) node).getFullyQualifiedName();
		if( !matchQualifications( qualifications, fullyQualifiedName ) ){
			return IMPOSSIBLE_MATCH;
		}
		
		return ACCURATE_MATCH;
	}
	
	public char[] indexEntryPrefix() {
		return AbstractIndexer.bestMethodPrefix( _limitTo, simpleName, qualifications, _matchMode, _caseSensitive );
	}
	
	protected boolean matchIndexEntry() {
		return true;
	}
}

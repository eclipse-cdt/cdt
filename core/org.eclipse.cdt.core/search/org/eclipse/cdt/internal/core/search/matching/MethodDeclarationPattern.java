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

import org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate;
import org.eclipse.cdt.core.parser.ast.*;
import org.eclipse.cdt.core.search.ICSearchScope;
import org.eclipse.cdt.internal.core.index.IEntryResult;
import org.eclipse.cdt.internal.core.index.impl.IndexInput;
import org.eclipse.cdt.internal.core.index.impl.IndexedFile;
import org.eclipse.cdt.internal.core.search.CharOperation;
import org.eclipse.cdt.internal.core.search.IIndexSearchRequestor;
import org.eclipse.cdt.internal.core.search.indexing.AbstractIndexer;

/**
 * @author aniefer
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class MethodDeclarationPattern extends FunctionDeclarationPattern {

	private char[][] decodedQualifications;


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
		
		//create char[][] out of full name, 
		String [] fullName = ((IASTQualifiedNameElement) node).getFullyQualifiedName();
		char [][] qualName = new char [ fullName.length - 1 ][];
		for( int i = 0; i < fullName.length - 1; i++ ){
			qualName[i] = fullName[i].toCharArray();
		}
		//check containing scopes
		if( !matchQualifications( qualifications, qualName ) ){
			return IMPOSSIBLE_MATCH;
		}
		
		return ACCURATE_MATCH;
	}
	
	public char[] indexEntryPrefix() {
		return AbstractIndexer.bestMethodPrefix( _limitTo, simpleName, qualifications, _matchMode, _caseSensitive );
	}
	
	protected void resetIndexInfo(){
		decodedSimpleName = null;
		decodedQualifications = null;
	}
	
	protected void decodeIndexEntry(IEntryResult entryResult) {
		char[] word = entryResult.getWord();
		int size = word.length;
		
		int firstSlash = CharOperation.indexOf( SEPARATOR, word, 0 );
		
		int slash = CharOperation.indexOf( SEPARATOR, word, firstSlash + 1 );
		
		this.decodedSimpleName = CharOperation.subarray(word, firstSlash + 1, slash);
		
		if( slash != -1 && slash+1 < size ){
			char [][] temp = CharOperation.splitOn('/', CharOperation.subarray(word, slash + 1, size));
			this.decodedQualifications = new char [ temp.length ][];
			for( int i = 0; i < temp.length; i++ ){
				this.decodedQualifications[ i ] = temp[ temp.length - i - 1 ];
			}
		}
	}

	protected boolean matchIndexEntry() {
		/* check simple name matches */
		if (simpleName != null){
			if( ! matchesName( simpleName, decodedSimpleName ) ){
				return false; 
			}
		}
		
		if( !matchQualifications( qualifications, decodedQualifications ) ){
			return false;
		}
		
		return true;
	}
	
	public void feedIndexRequestor(IIndexSearchRequestor requestor, int detailLevel, int[] references, IndexInput input, ICSearchScope scope) throws IOException {
		for (int i = 0, max = references.length; i < max; i++) {
			IndexedFile file = input.getIndexedFile(references[i]);
			String path;
			if (file != null && scope.encloses(path =file.getPath())) {
				requestor.acceptMethodDeclaration(path, decodedSimpleName, parameterNames.length, decodedQualifications);
			}
		}
	}
}

/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
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
import org.eclipse.cdt.core.parser.ast.IASTNamespaceDefinition;
import org.eclipse.cdt.core.search.ICSearchScope;
import org.eclipse.cdt.internal.core.CharOperation;
import org.eclipse.cdt.internal.core.index.IEntryResult;
import org.eclipse.cdt.internal.core.index.impl.IndexInput;
import org.eclipse.cdt.internal.core.index.impl.IndexedFile;
import org.eclipse.cdt.internal.core.index.sourceindexer.AbstractIndexer;
import org.eclipse.cdt.internal.core.search.IIndexSearchRequestor;

/**
 * @author aniefer
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class NamespaceDeclarationPattern extends CSearchPattern {


	/**
	 * @param name
	 * @param cs
	 * @param matchMode
	 * @param limitTo
	 * @param caseSensitive
	 */
	public NamespaceDeclarationPattern(char[] name, char[][] quals, int matchMode, LimitTo limitTo, boolean caseSensitive) {
		super( matchMode, caseSensitive, limitTo );
		
		simpleName = name;
		qualifications = quals;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.search.ICSearchPattern#matchLevel(org.eclipse.cdt.core.parser.ast.IASTOffsetableElement)
	 */
	public int matchLevel(ISourceElementCallbackDelegate node, LimitTo limit ) {
		if( !( node instanceof IASTNamespaceDefinition ) || !canAccept( limit ) )
			return IMPOSSIBLE_MATCH;
			
		IASTNamespaceDefinition namespace = (IASTNamespaceDefinition)node;
		
		if( simpleName != null && !matchesName( simpleName, namespace.getNameCharArray() ) ){
			return IMPOSSIBLE_MATCH;
		}

		//create char[][] out of full name, 
		char [] [] qualName = namespace.getFullyQualifiedNameCharArrays();
		
		if( !matchQualifications( qualifications, qualName, true ) ){
			return IMPOSSIBLE_MATCH;
		}

		return ACCURATE_MATCH;
	}

	private char[][] decodedContainingTypes;
	private char[] decodedSimpleName;
	private char[][] qualifications;
	private char[] simpleName;
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.search.matching.CSearchPattern#feedIndexRequestor(org.eclipse.cdt.internal.core.search.IIndexSearchRequestor, int, int[], org.eclipse.cdt.internal.core.index.impl.IndexInput, org.eclipse.cdt.core.search.ICSearchScope)
	 */
	public void feedIndexRequestor(IIndexSearchRequestor requestor, int detailLevel, int[] references, IndexInput input, ICSearchScope scope) throws IOException {
		for (int i = 0, max = references.length; i < max; i++) {
			IndexedFile file = input.getIndexedFile(references[i]);
			String path;
			if (file != null && scope.encloses(path =file.getPath())) {
				requestor.acceptNamespaceDeclaration(path, decodedSimpleName, decodedContainingTypes);
			}
		}
	}

	protected void resetIndexInfo(){
		decodedSimpleName = null;
		decodedContainingTypes = null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.search.matching.CSearchPattern#decodeIndexEntry(org.eclipse.cdt.internal.core.index.IEntryResult)
	 */
	protected void decodeIndexEntry(IEntryResult entryResult) {
		char[] word = entryResult.getWord();
		int size = word.length;

		int firstSlash = CharOperation.indexOf( SEPARATOR, word, 0 );
		
		int slash = CharOperation.indexOf(SEPARATOR, word, firstSlash + 1);
		
		this.decodedSimpleName = CharOperation.subarray(word, firstSlash+1, slash);
	
		if( slash != -1 && slash+1 < size ){
			char [][] temp = CharOperation.splitOn('/', CharOperation.subarray(word, slash+1, size));
			this.decodedContainingTypes = new char [ temp.length ][];
			for( int i = 0; i < temp.length; i++ ){
				this.decodedContainingTypes[ i ] = temp[ temp.length - i - 1 ];
			}
		} 

	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.search.matching.CSearchPattern#indexEntryPrefix()
	 */
	public char[] indexEntryPrefix() {
		return AbstractIndexer.bestNamespacePrefix(
				_limitTo,
				simpleName,
				qualifications,
				_matchMode, _caseSensitive
		);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.search.matching.CSearchPattern#matchIndexEntry()
	 */
	protected boolean matchIndexEntry() {
		/* check simple name matches */
		if( simpleName != null ){
			if( ! matchesName( simpleName, decodedSimpleName ) ){
				return false; 
			}
		}
	
		if( !matchQualifications( qualifications, decodedContainingTypes ) ){
			return false;
		}
	
		return true;
	}

}

/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corp. - Rational Software - initial implementation
 *******************************************************************************/
/*
 * Created on Jul 11, 2003
 */
package org.eclipse.cdt.internal.core.search.matching;

import java.io.IOException;

import org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate;
import org.eclipse.cdt.core.parser.ast.IASTEnumerationSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTEnumerator;
import org.eclipse.cdt.core.parser.ast.IASTField;
import org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement;
import org.eclipse.cdt.core.parser.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTQualifiedNameElement;
import org.eclipse.cdt.core.parser.ast.IASTTemplateParameter;
import org.eclipse.cdt.core.parser.ast.IASTVariable;
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
public class FieldDeclarationPattern extends CSearchPattern {

	/**
	 * @param name
	 * @param cs
	 * @param matchMode
	 * @param limitTo
	 * @param caseSensitive
	 */
	public FieldDeclarationPattern(char[] name, char[][] qual, int matchMode, SearchFor sfor, LimitTo limitTo, boolean caseSensitive) {
		super( matchMode, caseSensitive, limitTo );
		qualifications = qual;
		searchFor = sfor;
		simpleName = name;
	}


	public int matchLevel(ISourceElementCallbackDelegate node, LimitTo limit ) {
		if( node instanceof IASTField ){
			if( searchFor != FIELD || !canAccept( limit ) )
				return IMPOSSIBLE_MATCH;
		} else if ( node instanceof IASTVariable ){
			if( searchFor != VAR || !canAccept( limit ) )
				return IMPOSSIBLE_MATCH;			
		} else if ( node instanceof IASTEnumerator ){
			if( searchFor != ENUMTOR || !canAccept( limit ) )
				return IMPOSSIBLE_MATCH;
		} else if( node instanceof IASTParameterDeclaration ){
			if( searchFor != VAR || !canAccept( limit ) )
				return IMPOSSIBLE_MATCH;
		} else if( node instanceof IASTTemplateParameter ){
			if( searchFor != VAR || !canAccept( limit ) )
				return IMPOSSIBLE_MATCH;
		} else return IMPOSSIBLE_MATCH;
		
		
		char[] nodeName = ((IASTOffsetableNamedElement)node).getNameCharArray();
		
		//check name, if simpleName == null, its treated the same as "*"	
		if( simpleName != null && !matchesName( simpleName, nodeName ) ){
			return IMPOSSIBLE_MATCH;
		}
		
		//check containing scopes
		//create char[][] out of full name, 
		char [][] qualName = null; 
		if( node instanceof IASTEnumerator ){
			//Enumerators don't derive from IASTQualifiedElement, so make the fullName
			//from the enumerations name. 
			// 7.2 - 10 : each enumerator declared by an enum-specifier is declared in the
			//scope that immediately contains the enum-specifier. 
			IASTEnumerationSpecifier enumeration = ((IASTEnumerator)node).getOwnerEnumerationSpecifier();
			qualName = enumeration.getFullyQualifiedNameCharArrays();
		} else if( node instanceof IASTQualifiedNameElement ){
			qualName = ((IASTQualifiedNameElement) node).getFullyQualifiedNameCharArrays(); 
		} 
		
		if( qualName != null ){
			//check containing scopes
			if( !matchQualifications( qualifications, qualName, true ) ){
				return IMPOSSIBLE_MATCH;
			}
		}
		
		return ACCURATE_MATCH;
	}
	
	public char[] indexEntryPrefix() {
		if( searchFor == FIELD ){
			return AbstractIndexer.bestFieldPrefix( _limitTo, simpleName, qualifications, _matchMode, _caseSensitive );
		} else if( searchFor == VAR ) {
			return AbstractIndexer.bestVariablePrefix(
							_limitTo,
							simpleName, qualifications,
							_matchMode, _caseSensitive
			);
		} else if (searchFor == ENUMTOR) {
			return AbstractIndexer.bestEnumeratorPrefix(_limitTo, simpleName, qualifications, _matchMode, _caseSensitive );
		}
		return null;		
	}
	
	protected void resetIndexInfo(){
		decodedSimpleName = null;
		decodedQualifications = null;
	}
	
	protected void decodeIndexEntry(IEntryResult entryResult) {
		char[] word = entryResult.getWord();
		int size = word.length;
		int firstSlash = 0;
		int slash = 0;
		
		if( searchFor == FIELD ){
			firstSlash = CharOperation.indexOf( SEPARATOR, word, 0 );
			slash = CharOperation.indexOf(SEPARATOR, word, firstSlash + 1);
		} else if( searchFor == VAR ) {
			int realStart = CharOperation.indexOf( SEPARATOR, word, 0 );
			firstSlash = CharOperation.indexOf( SEPARATOR, word, realStart + 1);
			slash = CharOperation.indexOf(SEPARATOR, word, firstSlash + 1);
		} else if ( searchFor == ENUMTOR ){
			firstSlash = CharOperation.indexOf( SEPARATOR, word, 0 );
			slash = CharOperation.indexOf(SEPARATOR, word, firstSlash + 1);
		}
				
		this.decodedSimpleName = CharOperation.subarray(word, firstSlash + 1, slash);
		
		if( slash != -1 && slash+1 < size ){
			char [][] temp = CharOperation.splitOn('/', CharOperation.subarray(word, slash+1, size));
			this.decodedQualifications = new char [ temp.length ][];
			for( int i = 0; i < temp.length; i++ ){
				this.decodedQualifications[ i ] = temp[ temp.length - i - 1 ];
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.search.matching.CSearchPattern#feedIndexRequestor(org.eclipse.cdt.internal.core.search.IIndexSearchRequestor, int, int[], org.eclipse.cdt.internal.core.index.impl.IndexInput, org.eclipse.cdt.core.search.ICSearchScope)
	 */
	public void feedIndexRequestor(IIndexSearchRequestor requestor, int detailLevel, int[] references, int[] indexFlags, IndexInput input, ICSearchScope scope) throws IOException {
		for (int i = 0, max = references.length; i < max; i++) {
			IndexedFile file = input.getIndexedFile(references[i]);
			String path;
			if (file != null && scope.encloses(path =file.getPath())) {
				requestor.acceptFieldDeclaration(path, decodedSimpleName,decodedQualifications);
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
	
	private char [][] qualifications;
	private char [][] decodedQualifications;
	private char []   simpleName;
	private char []   decodedSimpleName;

	private SearchFor searchFor;
}

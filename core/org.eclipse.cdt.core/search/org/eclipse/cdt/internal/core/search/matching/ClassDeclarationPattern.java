/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
/*
 * Created on Jun 13, 2003
 */
package org.eclipse.cdt.internal.core.search.matching;

import java.io.IOException;

import org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate;
import org.eclipse.cdt.core.parser.ast.ASTClassKind;
import org.eclipse.cdt.core.parser.ast.IASTClassSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTEnumerationSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement;
import org.eclipse.cdt.core.parser.ast.IASTQualifiedNameElement;
import org.eclipse.cdt.core.search.ICSearchScope;
import org.eclipse.cdt.internal.core.index.IEntryResult;
import org.eclipse.cdt.internal.core.index.impl.IndexInput;
import org.eclipse.cdt.internal.core.index.impl.IndexedFile;
import org.eclipse.cdt.internal.core.search.CharOperation;
import org.eclipse.cdt.internal.core.search.IIndexSearchRequestor;
import org.eclipse.cdt.internal.core.search.indexing.AbstractIndexer;


/**
 * @author aniefer
 */

public class ClassDeclarationPattern extends CSearchPattern {

	public ClassDeclarationPattern( int matchMode, boolean caseSensitive ){
		super( matchMode, caseSensitive, DECLARATIONS );
	}
	
	public ClassDeclarationPattern( char[] name, char[][] containers, ASTClassKind kind, int mode, LimitTo limit, boolean caseSensitive ){
		super( mode, caseSensitive, limit );
		
		simpleName = caseSensitive ? name : CharOperation.toLowerCase( name );
		if( caseSensitive || containers == null ){
			qualifications = containers;
		} else {
			int len = containers.length;
			this.qualifications = new char[ len ][];
			for( int i = 0; i < len; i++ ){
				this.qualifications[i] = CharOperation.toLowerCase( containers[i] );
			}
		} 
		
		classKind = kind;
	}
	
	public int matchLevel( ISourceElementCallbackDelegate node, LimitTo limit ){
		
		if( !( node instanceof IASTClassSpecifier ) && !( node instanceof IASTEnumerationSpecifier ) )
			return IMPOSSIBLE_MATCH;
			
		if( ! canAccept( limit ) )
			return IMPOSSIBLE_MATCH;
		
		String nodeName = ((IASTOffsetableNamedElement)node).getName();
		
		//check name, if simpleName == null, its treated the same as "*"	
		if( simpleName != null && !matchesName( simpleName, nodeName.toCharArray() ) ){
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
		
		//check type
		if( classKind != null ){
			if( node instanceof IASTClassSpecifier ){
				IASTClassSpecifier clsSpec = (IASTClassSpecifier) node;
				return ( classKind == clsSpec.getClassKind() ) ? ACCURATE_MATCH : IMPOSSIBLE_MATCH;
			} else {
				return ( classKind == ASTClassKind.ENUM ) ? ACCURATE_MATCH : IMPOSSIBLE_MATCH;
			}
		}
		
		return ACCURATE_MATCH;
	}
	
	public char [] getName() {
		return simpleName;
	}
	public char[] [] getContainingTypes () {
		return qualifications;
	}
	public ASTClassKind getKind(){
		return classKind;
	}

	private char[] 	  simpleName;
	private char[][]  qualifications;
	private ASTClassKind classKind;
	
	protected char[] decodedSimpleName;
	private char[][] decodedContainingTypes;
	protected char decodedType;

	
	public void feedIndexRequestor(IIndexSearchRequestor requestor, int detailLevel, int[] references, IndexInput input, ICSearchScope scope) throws IOException {
		boolean isClass = decodedType == CLASS_SUFFIX;
		for (int i = 0, max = references.length; i < max; i++) {
			IndexedFile file = input.getIndexedFile(references[i]);
			String path;
			if (file != null && scope.encloses(path =file.getPath())) {
				//TODO: BOG Fix this up - even if it's not a class we still care 
				if (isClass) {
					requestor.acceptClassDeclaration(path, decodedSimpleName, decodedContainingTypes);
				}  else {
					requestor.acceptClassDeclaration(path, decodedSimpleName, decodedContainingTypes);
				}
			}
		}
	}

	protected void resetIndexInfo(){
		decodedType = 0;
		decodedSimpleName = null;
		decodedContainingTypes = null;
	}
	
	protected void decodeIndexEntry(IEntryResult entryResult) {	
		char[] word = entryResult.getWord();
		int size = word.length;
		
		int firstSlash = CharOperation.indexOf( SEPARATOR, word, 0 );
		
		this.decodedType = word[ firstSlash + 1 ];
		firstSlash += 2;
		
		int slash = CharOperation.indexOf( SEPARATOR, word, firstSlash + 1 );
		
		this.decodedSimpleName = CharOperation.subarray( word, firstSlash + 1, slash );
	
		if( slash != -1 && slash+1 < size ){
			char [][] temp = CharOperation.splitOn('/', CharOperation.subarray( word, slash + 1, size ));
			this.decodedContainingTypes = new char [ temp.length ][];
			for( int i = 0; i < temp.length; i++ ){
				this.decodedContainingTypes[ i ] = temp[ temp.length - i - 1 ];
			}
		} 
	}

	public char[] indexEntryPrefix() {
		return AbstractIndexer.bestTypePrefix(
				getLimitTo(),
				simpleName,
				qualifications,
				classKind,
				_matchMode,
				_caseSensitive
		);
	}

	protected boolean matchIndexEntry() {
		//check type matches
		if( classKind == null ){
			//don't match variable entries
			if( decodedType == VAR_SUFFIX ){
				return false;
			}
		} else if( classKind == ASTClassKind.CLASS ) {
			if( decodedType != CLASS_SUFFIX ){
				return false;
			} 
		} else if ( classKind == ASTClassKind.STRUCT ) {
			if( decodedType != STRUCT_SUFFIX ){
				return false;
			}
		} else if ( classKind == ASTClassKind.UNION ) {
			if( decodedType != UNION_SUFFIX ){
				return false;
			}
		} else if ( classKind == ASTClassKind.ENUM ) {
			if( decodedType != ENUM_SUFFIX ) {
				return false;
			}
		}
		
		/* check simple name matches */
		if (simpleName != null){
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

/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
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
import org.eclipse.cdt.core.parser.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTEnumerationSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement;
import org.eclipse.cdt.core.parser.ast.IASTQualifiedNameElement;
import org.eclipse.cdt.core.parser.ast.IASTTypedefDeclaration;
import org.eclipse.cdt.core.search.ICSearchScope;
import org.eclipse.cdt.internal.core.CharOperation;
import org.eclipse.cdt.internal.core.index.IEntryResult;
import org.eclipse.cdt.internal.core.index.impl.IndexInput;
import org.eclipse.cdt.internal.core.index.impl.IndexedFile;
import org.eclipse.cdt.internal.core.search.IIndexSearchRequestor;
import org.eclipse.cdt.internal.core.search.indexing.AbstractIndexer;


/**
 * @author aniefer
 */

public class ClassDeclarationPattern extends CSearchPattern {

//	public ClassDeclarationPattern( int matchMode, boolean caseSensitive ){
//		super( matchMode, caseSensitive, DECLARATIONS );
//	}
	
	public ClassDeclarationPattern( char[] name, char[][] containers, SearchFor searchFor, LimitTo limit, int mode, boolean caseSensitive, boolean isForward ){
		super( mode, caseSensitive, limit );
		
		this.isForward = isForward;
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
		
		this.searchFor = searchFor;
		
		if( searchFor == CLASS || searchFor == FWD_CLASS ){
			classKind = ASTClassKind.CLASS;
		} else if( searchFor == STRUCT || searchFor == FWD_STRUCT) {
			classKind = ASTClassKind.STRUCT;
		} else if ( searchFor == ENUM ) {
			classKind = ASTClassKind.ENUM;
		} else if ( searchFor == UNION || searchFor == FWD_UNION ) {
			classKind = ASTClassKind.UNION;
		} else {
			classKind = null;		
		}
		
	}
	
	public int matchLevel( ISourceElementCallbackDelegate node, LimitTo limit ){
		if ( !( node instanceof IASTClassSpecifier )          &&
		     !( node instanceof IASTElaboratedTypeSpecifier ) &&
		     !( node instanceof IASTTypedefDeclaration )      &&
		     !( node instanceof IASTEnumerationSpecifier)     )
		{
			return IMPOSSIBLE_MATCH;
		} else if( searchFor != TYPE && ((searchFor == TYPEDEF) ^ (node instanceof IASTTypedefDeclaration)) ) {
			return IMPOSSIBLE_MATCH;
		}
		
		if( ! canAccept( limit ) )
			return IMPOSSIBLE_MATCH;
		
		if ((node instanceof IASTElaboratedTypeSpecifier &&!isForward)||
			(node instanceof IASTClassSpecifier && isForward)){
				return IMPOSSIBLE_MATCH;
		}
		
		char[] nodeName = null;
		if (node instanceof IASTElaboratedTypeSpecifier)
		{
			nodeName = ((IASTElaboratedTypeSpecifier)node).getNameCharArray();
		}
		else if( node instanceof IASTOffsetableNamedElement )
		{
			nodeName = ((IASTOffsetableNamedElement)node).getNameCharArray();
		} else {
			return IMPOSSIBLE_MATCH;
		}
		
		//check name, if simpleName == null, its treated the same as "*"	
		if( simpleName != null && !matchesName( simpleName, nodeName ) ){
			return IMPOSSIBLE_MATCH;
		}

		if( node instanceof IASTQualifiedNameElement ){
			char [][] qualName = ((IASTQualifiedNameElement) node).getFullyQualifiedNameCharArrays();
			//check containing scopes
			if( !matchQualifications( qualifications, qualName, true ) ){
				return IMPOSSIBLE_MATCH;
			}
		}
		
		//check type
		if( classKind != null ){
			if( node instanceof IASTClassSpecifier ){
				IASTClassSpecifier clsSpec = (IASTClassSpecifier) node;
				return ( classKind == clsSpec.getClassKind() ) ? ACCURATE_MATCH : IMPOSSIBLE_MATCH;
			} else if (node instanceof IASTEnumerationSpecifier){
				return ( classKind == ASTClassKind.ENUM ) ? ACCURATE_MATCH : IMPOSSIBLE_MATCH;
			} else if (node instanceof IASTElaboratedTypeSpecifier ){
				IASTElaboratedTypeSpecifier elabTypeSpec = (IASTElaboratedTypeSpecifier) node;
				return ( classKind == elabTypeSpec.getClassKind() ) ? ACCURATE_MATCH : IMPOSSIBLE_MATCH;
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

	protected char[] 	  simpleName;
	protected char[][]  qualifications;
	protected ASTClassKind classKind;
	protected SearchFor    searchFor;
	
	protected char[] decodedSimpleName;
	private char[][] decodedContainingTypes;
	protected char decodedType;
	protected boolean isForward;

	
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
				searchFor,
				getLimitTo(),
				simpleName,
				qualifications,
				_matchMode,
				_caseSensitive
		);
	}

	protected boolean matchIndexEntry() {
		//check type matches
		if( classKind == null ){
			if( searchFor == TYPEDEF && decodedType != TYPEDEF_SUFFIX ){
				return false;
			}
			//don't match variable entries
			if( decodedType == VAR_SUFFIX ){
				return false;
			}
		} else if( classKind == ASTClassKind.CLASS ) {
			if( decodedType != CLASS_SUFFIX &&
				decodedType != FWD_CLASS_SUFFIX){
				return false;
			} 
		} else if ( classKind == ASTClassKind.STRUCT ) {
			if( decodedType != STRUCT_SUFFIX &&
				decodedType != FWD_STRUCT_SUFFIX){
				return false;
			}
		} else if ( classKind == ASTClassKind.UNION ) {
			if( decodedType != UNION_SUFFIX &&
				decodedType != FWD_UNION_SUFFIX){
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

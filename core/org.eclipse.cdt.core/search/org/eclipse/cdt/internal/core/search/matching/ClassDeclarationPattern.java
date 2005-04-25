/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
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

import org.eclipse.cdt.core.browser.PathUtil;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate;
import org.eclipse.cdt.core.parser.ast.ASTClassKind;
import org.eclipse.cdt.core.parser.ast.IASTClassSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTEnumerationSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement;
import org.eclipse.cdt.core.parser.ast.IASTQualifiedNameElement;
import org.eclipse.cdt.core.parser.ast.IASTTypedefDeclaration;
import org.eclipse.cdt.core.search.BasicSearchMatch;
import org.eclipse.cdt.core.search.ICSearchScope;
import org.eclipse.cdt.internal.core.CharOperation;
import org.eclipse.cdt.internal.core.index.IEntryResult;
import org.eclipse.cdt.internal.core.index.IIndex;
import org.eclipse.cdt.internal.core.index.cindexstorage.Index;
import org.eclipse.cdt.internal.core.index.cindexstorage.IndexedFileEntry;
import org.eclipse.cdt.internal.core.index.cindexstorage.IndexerOutput;
import org.eclipse.cdt.internal.core.index.cindexstorage.io.IndexInput;
import org.eclipse.cdt.internal.core.search.IIndexSearchRequestor;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;


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

	
	public void feedIndexRequestor(IIndexSearchRequestor requestor, int detailLevel, int[] fileRefs, int[][] offsets, int[][] offsetLengths,IndexInput input, ICSearchScope scope) throws IOException {
		boolean isClass = decodedType == IndexerOutput.CLASS_SUFFIX;
		
		for (int i = 0, max = fileRefs.length; i < max; i++) {
			IndexedFileEntry file = input.getIndexedFile(fileRefs[i]);
			String path;
			if (file != null && scope.encloses(path =file.getPath())) {
				//TODO: BOG Fix this up - even if it's not a class we still care 
				if (isClass) {
					requestor.acceptClassDeclaration(path, decodedSimpleName, decodedContainingTypes);
				}  else {
					requestor.acceptClassDeclaration(path, decodedSimpleName, decodedContainingTypes);
				}
				//For each file, create a new search match for each offset occurrence
				for (int j=0; j<offsets[i].length; j++){
					BasicSearchMatch match = new BasicSearchMatch();
					match.name = new String(this.decodedSimpleName);
					//Don't forget that offsets are encoded ICIndexStorageConstants
					//Offsets can either be LINE or OFFSET 
					int offsetType = Integer.valueOf(String.valueOf(offsets[i][j]).substring(0,1)).intValue();
					if (offsetType==IIndex.LINE){
						match.startOffset=Integer.valueOf(String.valueOf(offsets[i][j]).substring(1)).intValue();
						match.offsetType = IIndex.LINE;
					}else if (offsetType==IIndex.OFFSET){
						match.startOffset=Integer.valueOf(String.valueOf(offsets[i][j]).substring(1)).intValue();
						match.endOffset= match.startOffset + offsetLengths[i][j];
						match.offsetType=IIndex.OFFSET;
					}
					
					match.parentName = ""; //$NON-NLS-1$
					
					if (decodedType == IndexerOutput.CLASS_SUFFIX){
						match.type=ICElement.C_CLASS;
					} else if (decodedType == IndexerOutput.STRUCT_SUFFIX){
						match.type=ICElement.C_STRUCT;
					} else if (decodedType == IndexerOutput.UNION_SUFFIX){
						match.type=ICElement.C_UNION;
					} else if (decodedType == IndexerOutput.ENUM_SUFFIX) {
					    match.type=ICElement.C_ENUMERATION;
					} else if (decodedType == IndexerOutput.TYPEDEF_SUFFIX){
						match.type=ICElement.C_TYPEDEF;
					}
					                                 
				    IFile tempFile = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(path));
					if (tempFile != null && tempFile.exists())
						match.resource =tempFile;
					else {
						IPath tempPath = PathUtil.getWorkspaceRelativePath(file.getPath());
						match.path = tempPath;
						match.referringElement = tempPath;
					}
					requestor.acceptSearchMatch(match);
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
		
		int firstSlash = CharOperation.indexOf( IndexerOutput.SEPARATOR, word, 0 );
		
		this.decodedType = word[ firstSlash + 1 ];
		firstSlash += 2;
		
		int slash = CharOperation.indexOf( IndexerOutput.SEPARATOR, word, firstSlash + 1 );
		
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
		return Index.bestTypePrefix(
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
			if( searchFor == TYPEDEF && decodedType != IndexerOutput.TYPEDEF_SUFFIX ){
				return false;
			}
			//don't match variable entries
			if( decodedType == IndexerOutput.VAR_SUFFIX ){
				return false;
			}
		} else if( classKind == ASTClassKind.CLASS ) {
			if( decodedType != IndexerOutput.CLASS_SUFFIX &&
				decodedType != IndexerOutput.FWD_CLASS_SUFFIX){
				return false;
			} 
		} else if ( classKind == ASTClassKind.STRUCT ) {
			if( decodedType != IndexerOutput.STRUCT_SUFFIX &&
				decodedType != IndexerOutput.FWD_STRUCT_SUFFIX){
				return false;
			}
		} else if ( classKind == ASTClassKind.UNION ) {
			if( decodedType != IndexerOutput.UNION_SUFFIX &&
				decodedType != IndexerOutput.FWD_UNION_SUFFIX){
				return false;
			}
		} else if ( classKind == ASTClassKind.ENUM ) {
			if( decodedType != IndexerOutput.ENUM_SUFFIX ) {
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

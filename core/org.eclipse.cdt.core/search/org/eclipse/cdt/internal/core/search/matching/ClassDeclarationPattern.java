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
import org.eclipse.cdt.core.search.LineLocatable;
import org.eclipse.cdt.core.search.OffsetLocatable;
import org.eclipse.cdt.internal.core.CharOperation;
import org.eclipse.cdt.internal.core.index.IEntryResult;
import org.eclipse.cdt.internal.core.index.IIndex;
import org.eclipse.cdt.internal.core.index.cindexstorage.Index;
import org.eclipse.cdt.internal.core.index.cindexstorage.IndexedFileEntry;
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
	protected int decodedType;
	protected boolean isForward;

	
	public void feedIndexRequestor(IIndexSearchRequestor requestor, int detailLevel, int[] fileRefs, int[][] offsets, int[][] offsetLengths,IndexInput input, ICSearchScope scope) throws IOException {
		boolean isClass = decodedType == IIndex.TYPE_CLASS;
		
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
						match.locatable = new LineLocatable(Integer.valueOf(String.valueOf(offsets[i][j]).substring(1)).intValue(),0);
					}else if (offsetType==IIndex.OFFSET){
						int startOffset=Integer.valueOf(String.valueOf(offsets[i][j]).substring(1)).intValue();
						int endOffset= startOffset + offsetLengths[i][j];
						match.locatable = new OffsetLocatable(startOffset, endOffset);
					}
					
					match.parentName = ""; //$NON-NLS-1$
					
					switch (decodedType) {
					case IIndex.TYPE_CLASS: 
						match.type=ICElement.C_CLASS;
						break;
					case IIndex.TYPE_STRUCT :
						match.type=ICElement.C_STRUCT;
						break;
					case IIndex.TYPE_UNION :
						match.type=ICElement.C_UNION;
						break;
					case IIndex.TYPE_ENUM :
						match.type=ICElement.C_ENUMERATION;
						break;
					case IIndex.TYPE_TYPEDEF :
						match.type=ICElement.C_TYPEDEF;
						break;
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
		this.decodedType = entryResult.getKind();
		this.decodedSimpleName = entryResult.extractSimpleName().toCharArray();	
		String []missmatch = entryResult.getEnclosingNames();
		if(missmatch != null) {
			this.decodedContainingTypes = new char[missmatch.length][];
			for (int i = 0; i < missmatch.length; i++)
				this.decodedContainingTypes[i] = missmatch[i].toCharArray();
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
			if( searchFor == TYPEDEF && decodedType != IIndex.TYPE_TYPEDEF ){
				return false;
			}
			//don't match variable entries
			if( decodedType == IIndex.TYPE_VAR ){
				return false;
			}
		} else if( classKind == ASTClassKind.CLASS ) {
			if( decodedType != IIndex.TYPE_CLASS &&
				decodedType != IIndex.TYPE_FWD_CLASS){
				return false;
			} 
		} else if ( classKind == ASTClassKind.STRUCT ) {
			if( decodedType != IIndex.TYPE_STRUCT &&
				decodedType != IIndex.TYPE_FWD_STRUCT){
				return false;
			}
		} else if ( classKind == ASTClassKind.UNION ) {
			if( decodedType != IIndex.TYPE_UNION &&
				decodedType != IIndex.TYPE_FWD_UNION){
				return false;
			}
		} else if ( classKind == ASTClassKind.ENUM ) {
			if( decodedType != IIndex.TYPE_ENUM ) {
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

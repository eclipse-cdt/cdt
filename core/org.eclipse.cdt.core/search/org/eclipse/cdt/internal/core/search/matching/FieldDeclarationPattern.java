/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corp. - Rational Software - initial implementation
 *******************************************************************************/
/*
 * Created on Jul 11, 2003
 */
package org.eclipse.cdt.internal.core.search.matching;

import java.io.IOException;

import org.eclipse.cdt.core.browser.PathUtil;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate;
import org.eclipse.cdt.core.parser.ast.IASTEnumerationSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTEnumerator;
import org.eclipse.cdt.core.parser.ast.IASTField;
import org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement;
import org.eclipse.cdt.core.parser.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTQualifiedNameElement;
import org.eclipse.cdt.core.parser.ast.IASTTemplateParameter;
import org.eclipse.cdt.core.parser.ast.IASTVariable;
import org.eclipse.cdt.core.search.BasicSearchMatch;
import org.eclipse.cdt.core.search.ICSearchScope;
import org.eclipse.cdt.core.search.LimitTo;
import org.eclipse.cdt.core.search.SearchFor;
import org.eclipse.cdt.internal.core.index.IEntryResult;
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
			return Index.bestFieldPrefix( _limitTo, simpleName, qualifications, _matchMode, _caseSensitive );
		} else if( searchFor == VAR ) {
			return Index.bestVariablePrefix(
							_limitTo,
							simpleName, qualifications,
							_matchMode, _caseSensitive
			);
		} else if (searchFor == ENUMTOR) {
			return Index.bestEnumeratorPrefix(_limitTo, simpleName, qualifications, _matchMode, _caseSensitive );
		}
		return null;		
	}
	
	protected void resetIndexInfo(){
		decodedSimpleName = null;
		decodedQualifications = null;
	}
	
	protected void decodeIndexEntry(IEntryResult entryResult) {		
		this.decodedSimpleName = entryResult.extractSimpleName().toCharArray();	
		String []missmatch = entryResult.getEnclosingNames();
		if(missmatch != null) {
			this.decodedQualifications = new char[missmatch.length][];
			for (int i = 0; i < missmatch.length; i++)
				this.decodedQualifications[i] = missmatch[i].toCharArray();
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.search.matching.CSearchPattern#feedIndexRequestor(org.eclipse.cdt.internal.core.search.IIndexSearchRequestor, int, int[], org.eclipse.cdt.internal.core.index.impl.IndexInput, org.eclipse.cdt.core.search.ICSearchScope)
	 */
	public void feedIndexRequestor(IIndexSearchRequestor requestor, int detailLevel, int[] fileRefs, int[][] offsets, int[][] offsetLengths, IndexInput input, ICSearchScope scope) throws IOException {
	
		for (int i = 0, max = fileRefs.length; i < max; i++) {
			IndexedFileEntry file = input.getIndexedFile(fileRefs[i]);
			String path = null;
			if (file != null && scope.encloses(path =file.getPath())) {
				for (int j=0; j<offsets[i].length; j++){
					BasicSearchMatch match = new BasicSearchMatch();
					match.setName(new String(this.decodedSimpleName));
					//Decode the offsetse
					//Offsets can either be IIndex.LINE or IIndex.OFFSET 
					match.setLocatable(getMatchLocatable(offsets[i][j],offsetLengths[i][j]));
					match.setParentName(""); //$NON-NLS-1$
					if (searchFor ==  FIELD){
						match.setType(ICElement.C_FIELD);
					} else if (searchFor ==  VAR){
						match.setType(ICElement.C_VARIABLE);
					} else if (searchFor ==  ENUMTOR){
						match.setType(ICElement.C_ENUMERATOR);
					}
					
				    IFile tempFile = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(path));
					if (tempFile != null && tempFile.exists())
						match.setResource(tempFile);
					else {
						IPath tempPath = PathUtil.getWorkspaceRelativePath(file.getPath());
						match.setPath(tempPath);
						match.setReferringElement(tempPath);
					}
					requestor.acceptSearchMatch(match);
				}
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

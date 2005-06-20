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

import org.eclipse.cdt.core.browser.PathUtil;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate;
import org.eclipse.cdt.core.parser.ast.IASTNamespaceDefinition;
import org.eclipse.cdt.core.search.BasicSearchMatch;
import org.eclipse.cdt.core.search.ICSearchScope;
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
	public void feedIndexRequestor(IIndexSearchRequestor requestor, int detailLevel, int[] fileRefs, int[][] offsets, int[][] offsetLengths, IndexInput input, ICSearchScope scope) throws IOException {

		for (int i = 0, max = fileRefs.length; i < max; i++) {
			IndexedFileEntry file = input.getIndexedFile(fileRefs[i]);
			String path=null;
			if (file != null && scope.encloses(path =file.getPath())) {
				for (int j=0; j<offsets[i].length; j++){
					BasicSearchMatch match = new BasicSearchMatch();
					match.setName(new String(this.decodedSimpleName));
					//Decode the offsetse
					//Offsets can either be IIndex.LINE or IIndex.OFFSET 
					match.setLocatable(getMatchLocatable(offsets[i][j],offsetLengths[i][j]));
					match.setParentName(""); //$NON-NLS-1$
					match.setType(ICElement.C_NAMESPACE);
					
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

	protected void resetIndexInfo(){
		decodedSimpleName = null;
		decodedContainingTypes = null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.search.matching.CSearchPattern#decodeIndexEntry(org.eclipse.cdt.internal.core.index.IEntryResult)
	 */
	protected void decodeIndexEntry(IEntryResult entryResult) {
		this.decodedSimpleName = entryResult.extractSimpleName().toCharArray();	
		String []missmatch = entryResult.getEnclosingNames();
		if(missmatch != null) {
			this.decodedContainingTypes = new char[missmatch.length][];
			for (int i = 0; i < missmatch.length; i++)
				this.decodedContainingTypes[i] = missmatch[i].toCharArray();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.search.matching.CSearchPattern#indexEntryPrefix()
	 */
	public char[] indexEntryPrefix() {
		return Index.bestNamespacePrefix(
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

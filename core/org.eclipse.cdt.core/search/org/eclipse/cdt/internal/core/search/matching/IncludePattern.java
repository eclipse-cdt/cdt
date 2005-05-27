/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/

package org.eclipse.cdt.internal.core.search.matching;

import java.io.IOException;

import org.eclipse.cdt.core.browser.PathUtil;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate;
import org.eclipse.cdt.core.parser.ast.IASTInclusion;
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
 * @author bgheorgh
 */
public class IncludePattern extends CSearchPattern {
	protected char [] simpleName;
	protected char [] decodedSimpleName;
	/**
	 * 
	 */
	public IncludePattern(char[] name, int matchMode, LimitTo limitTo, boolean caseSensitive) {
		super( matchMode, caseSensitive, limitTo );	
		simpleName = name;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.search.matching.CSearchPattern#decodeIndexEntry(org.eclipse.cdt.internal.core.index.IEntryResult)
	 */
	protected void decodeIndexEntry(IEntryResult entryResult) {
		this.decodedSimpleName = entryResult.extractSimpleName().toCharArray();	
	}
		
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.search.matching.CSearchPattern#feedIndexRequestor(org.eclipse.cdt.internal.core.search.IIndexSearchRequestor, int, int[], org.eclipse.cdt.internal.core.index.impl.IndexInput, org.eclipse.cdt.core.search.ICSearchScope)
	 */
	public void feedIndexRequestor(IIndexSearchRequestor requestor, int detailLevel, int[] fileRefs, int[][] offsets, int[][] offsetLengths, IndexInput input, ICSearchScope scope) throws IOException {

		for (int i = 0, max = fileRefs.length; i < max; i++) {
			IndexedFileEntry file = input.getIndexedFile(fileRefs[i]);
			String path=null;
			if (file != null && scope.encloses(path =file.getPath())) {
				requestor.acceptIncludeDeclaration(path, decodedSimpleName);
			}
			
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
				match.type=ICElement.C_INCLUDE;
				
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

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.search.matching.CSearchPattern#resetIndexInfo()
	 */
	protected void resetIndexInfo() {
		decodedSimpleName = null;
	}


	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.search.matching.CSearchPattern#indexEntryPrefix()
	 */
	public char[] indexEntryPrefix() {
		return Index.bestIncludePrefix(
							_limitTo,
							simpleName,
							_matchMode, _caseSensitive
			);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.search.matching.CSearchPattern#matchIndexEntry()
	 */
	protected boolean matchIndexEntry() {
		/* check simple name matches */
		if (simpleName != null){
			if( ! matchesName( simpleName, decodedSimpleName ) ){
				return false; 
			}
		}
		
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.search.ICSearchPattern#matchLevel(org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate, org.eclipse.cdt.core.search.ICSearchConstants.LimitTo)
	 */
	public int matchLevel(ISourceElementCallbackDelegate node, LimitTo limit) {
		// TODO Auto-generated method stub
		if (!( node instanceof IASTInclusion )) {
			return IMPOSSIBLE_MATCH;
		}
		
		if( ! canAccept( limit ) )
			return IMPOSSIBLE_MATCH;
		
		IASTInclusion inc = (IASTInclusion) node;
		String fileName = inc.getFullFileName();
		
		if(CharOperation.equals(simpleName,fileName.toCharArray(),_caseSensitive)){
			return ACCURATE_MATCH;
		}
		
		return IMPOSSIBLE_MATCH;
	}

}

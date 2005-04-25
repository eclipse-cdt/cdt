/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corp. - Rational Software - initial implementation
 *******************************************************************************/
/*
 * Created on Aug 8, 2003
 */
package org.eclipse.cdt.internal.core.search.matching;

import java.io.IOException;

import org.eclipse.cdt.core.browser.PathUtil;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate;
import org.eclipse.cdt.core.parser.ast.IASTMacro;
import org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement;
import org.eclipse.cdt.core.search.BasicSearchMatch;
import org.eclipse.cdt.core.search.ICSearchScope;
import org.eclipse.cdt.internal.core.CharOperation;
import org.eclipse.cdt.internal.core.index.IEntryResult;
import org.eclipse.cdt.internal.core.index.IIndex;
import org.eclipse.cdt.internal.core.index.cindexstorage.ICIndexStorageConstants;
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
public class MacroDeclarationPattern extends CSearchPattern {

	/**
	 * @param name
	 * @param matchMode
	 * @param limitTo
	 * @param caseSensitive
	 */
	public MacroDeclarationPattern(char[] name, int matchMode, LimitTo limitTo, boolean caseSensitive) {
		super( matchMode, caseSensitive, limitTo );
		
		simpleName = name;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.search.ICSearchPattern#matchLevel(org.eclipse.cdt.core.parser.ast.IASTOffsetableElement)
	 */
	public int matchLevel(ISourceElementCallbackDelegate node, LimitTo limit ) {
		if( !(node instanceof IASTMacro) || !canAccept( limit ) ){
			return IMPOSSIBLE_MATCH;
		}
		
		char[] nodeName = ((IASTOffsetableNamedElement)node).getNameCharArray();
		
		//check name, if simpleName == null, its treated the same as "*"	
		if( simpleName != null && !matchesName( simpleName, nodeName ) ){
			return IMPOSSIBLE_MATCH;
		}
		
		return ACCURATE_MATCH;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.search.matching.CSearchPattern#feedIndexRequestor(org.eclipse.cdt.internal.core.search.IIndexSearchRequestor, int, int[], org.eclipse.cdt.internal.core.index.impl.IndexInput, org.eclipse.cdt.core.search.ICSearchScope)
	 */
	public void feedIndexRequestor(IIndexSearchRequestor requestor, int detailLevel, int[] fileRefs, int[][] offsets, int[][] offsetLengths, IndexInput input, ICSearchScope scope) throws IOException {
	
		for (int i = 0, max = fileRefs.length; i < max; i++) {
			IndexedFileEntry file = input.getIndexedFile(fileRefs[i]);
			String path=null;
			if (file != null && scope.encloses(path =file.getPath())) {
				requestor.acceptMacroDeclaration(path, decodedSimpleName);
			}
			
			for (int j=0; j<offsets[i].length; j++){
				BasicSearchMatch match = new BasicSearchMatch();
				match.name = new String(this.decodedSimpleName);
				//Don't forget that offsets are encoded ICIndexStorageConstants
				//Offsets can either be LINE or OFFSET 
				int offsetType = Integer.valueOf(String.valueOf(offsets[i][j]).substring(0,1)).intValue();
				if (offsetType==IIndex.LINE){
					match.startOffset=Integer.valueOf(String.valueOf(offsets[i][j]).substring(1)).intValue();
					match.offsetType=IIndex.LINE;
				}else if (offsetType==IIndex.OFFSET){
					match.startOffset=Integer.valueOf(String.valueOf(offsets[i][j]).substring(1)).intValue();
					match.endOffset= match.startOffset + offsetLengths[i][j];	
					match.offsetType=IIndex.OFFSET;	
				}
				match.parentName = ""; //$NON-NLS-1$
				match.type = ICElement.C_MACRO;
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

	protected void resetIndexInfo(){
		decodedSimpleName = null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.search.matching.CSearchPattern#decodeIndexEntry(org.eclipse.cdt.internal.core.index.IEntryResult)
	 */
	protected void decodeIndexEntry(IEntryResult entryResult) {
		char[] word = entryResult.getWord();
		
		int firstSlash = CharOperation.indexOf( ICIndexStorageConstants.SEPARATOR, word, 0 );
	
		this.decodedSimpleName = CharOperation.subarray(word, firstSlash + 1, -1);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.search.matching.CSearchPattern#indexEntryPrefix()
	 */
	public char[] indexEntryPrefix() {
		return Index.bestMacroPrefix(
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
	
	protected char [] simpleName;
	protected char [] decodedSimpleName;
	
}

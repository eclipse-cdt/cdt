/*******************************************************************************
 * Copyright (c) 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.parser.scanner;

import java.io.File;
import java.util.HashMap;

import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.parser.IMacroDictionary;
import org.eclipse.cdt.core.parser.IncludeFileContentProvider;
import org.eclipse.cdt.internal.core.dom.IIncludeFileResolutionHeuristics;
import org.eclipse.cdt.internal.core.parser.scanner.InternalFileContent.InclusionKind;

/**
 * Internal implementation of the file content providers
 */
public abstract class InternalFileContentProvider extends IncludeFileContentProvider {
	private static final Integer MAX_CONTENT_INCLUSIONS = 50;
	private IIncludeFileResolutionHeuristics fIncludeResolutionHeuristics;
    private final HashMap<Object, Integer> fIncludedFilesHistory= new HashMap<Object, Integer>();

	/**
	 * Checks whether the specified inclusion exists.
	 */
	public boolean getInclusionExists(String path) {
		return new File(path).exists();
	}

	/**
	 * Creates an InclusionContent object for the given location.
	 * @param filePath the absolute location of the file.
	 * @param macroDictionary macros defined at the inclusion point.
     * @return Returns an inclusion content, or <code>null</code> if the location does not exist.
	 * @see InternalFileContent
	 */
	public abstract InternalFileContent getContentForInclusion(String filePath,
			IMacroDictionary macroDictionary);

	/** 
	 * Called only when used as a delegate of the index file content provider.
	 */
	public abstract InternalFileContent getContentForInclusion(IIndexFileLocation ifl, String astPath);

	/**
	 * Returns a file-content object of kind {@link InclusionKind#FOUND_IN_INDEX}, representing
	 * the content from the context of the given file up to where the file actually gets included,
	 * or <code>null</code> if this cannot be done.
	 * @param filePath the absolute location of the file.
	 * @param macroDictionary macros defined at the inclusion point.
	 */
	public InternalFileContent getContentForContextToHeaderGap(String filePath,
			IMacroDictionary macroDictionary) {
		return null;
	}

	public final void resetInclusionCounting() {
		fIncludedFilesHistory.clear();
	}

	/** 
	 * Reports that a file is about to be parsed. Called for the root file and the included files.
	 * @param pragmaOnce whether the file has pragma once semantics.
	 */
	public void reportFile(String file, boolean pragmaOnce) {
		reportFileKey(file, pragmaOnce);
	}
	
	protected final void reportFileKey(Object fileKey, boolean pragmaOnce) {
		if (pragmaOnce) {
			fIncludedFilesHistory.put(fileKey, -1);
		} else {
			final Integer history= fIncludedFilesHistory.get(fileKey);
			fIncludedFilesHistory.put(fileKey, history == null ? 1 : history < 0 ? -1 : history + 1);
		}
	}
	
	/**
	 * Returns how many times the given file has been included in this translation unit, 
	 * or -1 if it was included with pragma once semantics.
	 */
	public int getFileInclusionCount(String filePath) {
		return getFileKeyInclusionCount(filePath);
	}
	
	protected final int getFileKeyInclusionCount(Object key) {
		final Integer history= fIncludedFilesHistory.get(key);
		return history == null ? 0 : history;
	}

	/**
	 * Checks for pragma once semantics or too many inclusions.
	 */
	protected boolean mustSkipFileInclusion(String filePath) {
		return mustSkipKeyInclusion(filePath);
	}
	
	protected final boolean mustSkipKeyInclusion(Object key) {
		Integer count= getFileKeyInclusionCount(key);
		return count != null && (count < 0 || count > MAX_CONTENT_INCLUSIONS);
	}


	/**
	 * Returns a strategy for heuristically resolving includes, or <code>null</code> if this shall
	 * not be done.
	 */
	public final IIncludeFileResolutionHeuristics getIncludeHeuristics() {
		return fIncludeResolutionHeuristics;
	}

	public final void setIncludeResolutionHeuristics(IIncludeFileResolutionHeuristics heuristics) {
		fIncludeResolutionHeuristics= heuristics;
	}
}

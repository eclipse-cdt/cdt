/*******************************************************************************
 * Copyright (c) 2009, 2011 Wind River Systems, Inc. and others.
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
import java.util.HashSet;
import java.util.Set;

import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.parser.IMacroDictionary;
import org.eclipse.cdt.core.parser.IncludeFileContentProvider;
import org.eclipse.cdt.internal.core.dom.IIncludeFileResolutionHeuristics;
import org.eclipse.cdt.internal.core.parser.scanner.InternalFileContent.InclusionKind;

/**
 * Internal implementation of the file content providers
 */
public abstract class InternalFileContentProvider extends IncludeFileContentProvider {
	private IIncludeFileResolutionHeuristics fIncludeResolutionHeuristics;
    private final Set<String> fPragmaOnce= new HashSet<String>();

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

	public void resetPragmaOnceTracking() {
		fPragmaOnce.clear();
	}

	/** 
	 * Reports detection of pragma once semantics.
	 */
	public void reportPragmaOnceSemantics(String file) {
		fPragmaOnce.add(file);
	}

	/**
	 * Returns whether the given file has been included with pragma once semantics.
	 */
	public boolean isIncludedWithPragmaOnceSemantics(String filePath) {
		return fPragmaOnce.contains(filePath);
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

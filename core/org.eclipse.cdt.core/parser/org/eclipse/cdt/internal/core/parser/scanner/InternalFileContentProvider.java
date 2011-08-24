/*******************************************************************************
 * Copyright (c) 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.parser.scanner;

import java.io.File;
import java.util.Map;

import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.parser.IncludeFileContentProvider;
import org.eclipse.cdt.internal.core.dom.IIncludeFileResolutionHeuristics;
import org.eclipse.cdt.internal.core.parser.scanner.InternalFileContent.InclusionKind;

/**
 * Internal implementation of the file content providers
 */
public abstract class InternalFileContentProvider extends IncludeFileContentProvider {
	private IIncludeFileResolutionHeuristics fIncludeResolutionHeuristics;

	/**
	 * Check whether the specified inclusion exists.
	 */
	public boolean getInclusionExists(String path) {
		return new File(path).exists();
	}

	/**
	 * Create an InclusionContent object for the given location.
     * return an inclusion content or <code>null</code> if the location does not exist.
	 * @param filePath the absolute location of the file.
	 * @param macroDictionary macros defined at the inclusion point.
	 * @see InternalFileContent
	 */
	public abstract InternalFileContent getContentForInclusion(String filePath,
			Map<String, String> macroDictionary);

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
			Map<String, String> macroDictionary) {
		return null;
	}

	/**
	 * Reports the path of the translation unit, such that it is known as included.
	 * @param filePath the absolute location of the file.
	 * @param hasPragmaOnceSemantics the name of the include guard macro for the file,
	 *     or <code>null</code> if the file doesn't have the include guard macro.
	 * @param significantMacros macros relevant to the file and their definitions.
	 */
	public void reportTranslationUnitFile(String filePath, boolean hasPragmaOnceSemantics,
			Map<String, String> significantMacros) {
	}
	
	/**
	 * Returns whether or not the file has been included, or <code>null</code> if the content provider
	 * does not track that.
	 * @param filePath the absolute location of the file.
	 * @param macroDictionary macros defined at the inclusion point.
	 */
	public Boolean hasFileBeenIncludedInCurrentTranslationUnit(String filePath,
			Map<String, String> macroDictionary) {
		return null;
	}

	/**
	 * Returns a strategy for heuristically resolving includes, or <code>null</code> if this shall not
	 * be done.
	 */
	public final IIncludeFileResolutionHeuristics getIncludeHeuristics() {
		return fIncludeResolutionHeuristics;
	}

	public final void setIncludeResolutionHeuristics(IIncludeFileResolutionHeuristics heuristics) {
		fIncludeResolutionHeuristics= heuristics;
	}
}

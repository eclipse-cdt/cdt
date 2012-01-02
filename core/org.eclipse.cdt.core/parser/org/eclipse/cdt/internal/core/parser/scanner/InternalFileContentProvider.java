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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IFileNomination;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.parser.ISignificantMacros;
import org.eclipse.cdt.core.parser.IncludeFileContentProvider;
import org.eclipse.cdt.internal.core.dom.IIncludeFileResolutionHeuristics;
import org.eclipse.cdt.internal.core.index.IIndexFragmentFile;
import org.eclipse.cdt.internal.core.parser.IMacroDictionary;
import org.eclipse.cdt.internal.core.parser.scanner.InternalFileContent.InclusionKind;

/**
 * Internal implementation of the file content providers
 */
public abstract class InternalFileContentProvider extends IncludeFileContentProvider {
	public static final class DependsOnOutdatedFileException extends Exception {
		public final Object fTu;
		public final IIndexFragmentFile fIndexFile;
		public DependsOnOutdatedFileException(Object tu, IIndexFragmentFile file) {
			fTu= tu;
			fIndexFile= file;
		}
	}

	private IIncludeFileResolutionHeuristics fIncludeResolutionHeuristics;
    private final Map<String, IFileNomination> fPragmaOnce= new HashMap<String, IFileNomination>();
    private final Map<String, List<ISignificantMacros>> fLoadedVersions= new HashMap<String, List<ISignificantMacros>>();

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
	 * @throws DependsOnOutdatedFileException 
	 */
	public InternalFileContent getContentForContextToHeaderGap(String filePath,
			IMacroDictionary macroDictionary) throws DependsOnOutdatedFileException {
		return null;
	}

	public void resetForTranslationUnit() {
		fPragmaOnce.clear();
		fLoadedVersions.clear();
	}

	/** 
	 * Reports detection of pragma once semantics.
	 */
	public void reportPragmaOnceSemantics(String file, IFileNomination nomination) {
		fPragmaOnce.put(file, nomination);
	}

	/**
	 * Returns {@link IASTPreprocessorIncludeStatement} or {@link IIndexFile}, in
	 * case the file has been included using pragma once semantics,
	 * or <code>null</code> otherwise.
	 */
	public IFileNomination isIncludedWithPragmaOnceSemantics(String filePath) {
		return fPragmaOnce.get(filePath);
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

	public List<ISignificantMacros> getLoadedVersions(String path) {
		List<ISignificantMacros> result = fLoadedVersions.get(path);
		return result == null ? Collections.<ISignificantMacros>emptyList() : result;
	}

	public void addLoadedVersions(String path, int reduceVersions, ISignificantMacros sig) {
		List<ISignificantMacros> list= fLoadedVersions.get(path);
		if (list == null || reduceVersions == 0) {
			fLoadedVersions.put(path, Collections.singletonList(sig));
		} else if (!list.contains(sig)) {
			if (list.size() == 1) {
				ISignificantMacros first = list.get(0);
				list= new ArrayList<ISignificantMacros>(2);
				list.add(first);
				fLoadedVersions.put(path, list);
			} else if (reduceVersions > 0 && reduceVersions < list.size()) {
				list.subList(reduceVersions, list.size()).clear();
			}
			list.add(sig);
		}
	}

	/** 
	 * Return the path of the context of <code>null</code>, if there is no context.
	 */
	public String getContextPath() {
		return null;
	}		
}

/*******************************************************************************
 * Copyright (c) 2012, 2013 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.includes;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.index.IIndexInclude;
import org.eclipse.cdt.core.index.IndexLocationFactory;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.parser.util.ArrayUtil;

import org.eclipse.cdt.internal.corext.codemanipulation.InclusionContext;

/**
 * Context for managing include statements.
 */
public class IncludeCreationContext extends InclusionContext {
	private final IIndex fIndex;
	private final Set<IPath> fHeadersToInclude;
	private final Set<IPath> fHeadersAlreadyIncluded;
	private final Set<IPath> fHeadersIncludedPreviously;

	public IncludeCreationContext(ITranslationUnit tu, IIndex index) {
		super(tu);
		fIndex = index;
		fHeadersToInclude = new HashSet<>();
		fHeadersAlreadyIncluded = new HashSet<>();
		fHeadersIncludedPreviously = new HashSet<>();
	}

	public final IIndex getIndex() {
		return fIndex;
	}

	public boolean isCXXLanguage() {
		return getTranslationUnit().isCXXLanguage();
	}

    /**
     * Removes headers that are exported by other headers that will be included.
     */
    public void removeExportedHeaders() throws CoreException {
    	// Index files keyed by their absolute paths.
    	Map<IPath, IIndexFile> filesByPath = new HashMap<>();
    	for (IIndexFile file : fIndex.getAllFiles()) {
    		IPath path = getPath(file);
    		filesByPath.put(path, file);
    	}

    	removeExportedHeaders(fHeadersAlreadyIncluded, filesByPath);
    	removeExportedHeaders(fHeadersToInclude, filesByPath);
    }

	private void removeExportedHeaders(Set<IPath> exportingHeaders,
			Map<IPath, IIndexFile> filesByPath) throws CoreException {
		Set<IPath> exportedHeaders = new HashSet<>();
		for (IPath path : exportingHeaders) {
			if (!exportedHeaders.contains(path)) {
				IIndexFile file = filesByPath.get(path);
				if (file != null) {  // file can be null if the header was not indexed.
					ArrayDeque<IIndexFile> queue = new ArrayDeque<>();
					queue.add(file);
					while ((file = queue.pollFirst()) != null) {
						for (IIndexInclude include : file.getIncludes()) {
							if (getPreferences().allowIndirectInclusion || isIncludedFileExported(include)) {
								file = fIndex.resolveInclude(include);
								if (file != null) {
									if (exportedHeaders.add(getPath(file)))
										queue.add(file);
								}
							}
						}
					}
				}
			}
		}
		fHeadersToInclude.removeAll(exportedHeaders);
	}

	private boolean isIncludedFileExported(IIndexInclude include) throws CoreException {
		if (include.isIncludedFileExported())
			return true;
		String name = include.getName();
		int index = name.lastIndexOf('.');
		String extension = index >= 0 ? name.substring(index + 1) : ""; //$NON-NLS-1$
		return ArrayUtil.containsEqual(getPreferences().extensionsOfAutoExportedFiles, extension);
	}

	private static IPath getPath(IIndexFile file) throws CoreException {
		return IndexLocationFactory.getAbsolutePath(file.getLocation());
	}
    
	public Set<IPath> getHeadersToInclude() {
		return fHeadersToInclude;
	}

	public final void addHeaderToInclude(IPath header) {
		fHeadersToInclude.add(header);
		fHeadersAlreadyIncluded.add(header);
	}

	public final boolean isToBeIncluded(IPath header) {
		return fHeadersToInclude.contains(header);
	}

	public Set<IPath> getHeadersAlreadyIncluded() {
		return fHeadersAlreadyIncluded;
	}

	public final void addHeaderAlreadyIncluded(IPath header) {
		fHeadersAlreadyIncluded.add(header);
	}

	public final boolean isAlreadyIncluded(IPath header) {
		return fHeadersAlreadyIncluded.contains(header);
	}

	public final boolean isIncluded(IPath header) {
		return fHeadersAlreadyIncluded.contains(header) || fHeadersToInclude.contains(header);
	}

	public void addHeadersIncludedPreviously(IASTPreprocessorIncludeStatement[] includes) {
		for (IASTPreprocessorIncludeStatement include : includes) {
			if (include.isPartOfTranslationUnitFile()) {
				String path = include.getPath();
				// An empty path means that the include was not resolved.
				if (!path.isEmpty())
					fHeadersIncludedPreviously.add(Path.fromOSString(path));
			}
		}
	}

	public final boolean wasIncludedPreviously(IPath header) {
		return fHeadersIncludedPreviously.contains(header);
	}
}

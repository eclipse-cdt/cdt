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

import java.io.File;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.index.IIndexInclude;
import org.eclipse.cdt.core.index.IndexLocationFactory;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.parser.IScannerInfo;

import org.eclipse.cdt.internal.core.parser.scanner.CPreprocessor;
import org.eclipse.cdt.internal.core.parser.scanner.IncludeSearchPath;
import org.eclipse.cdt.internal.core.parser.scanner.IncludeSearchPathElement;
import org.eclipse.cdt.internal.core.parser.scanner.ScannerUtility;

/**
 * Context for managing include statements.
 */
public class InclusionContext {
	private static final IPath UNRESOLVED_INCLUDE = Path.EMPTY;

	private final ITranslationUnit fTu;
	private final IProject fProject;
	private final IPath fCurrentDirectory;
	private final IncludePreferences fPreferences;
	private final IncludeSearchPath fIncludeSearchPath;
	private final Map<IncludeInfo, IPath> fIncludeResolutionCache;
	private final Map<IPath, IncludeInfo> fInverseIncludeResolutionCache;
	private final IIndex fIndex;
	private final Set<IPath> fHeadersToInclude;
	private final Set<IPath> fHeadersAlreadyIncluded;
	private final Set<IPath> fHeadersIncludedPreviously;

	public InclusionContext(ITranslationUnit tu, IIndex index) {
		fTu = tu;
		fIndex = index;
		ICProject cProject = fTu.getCProject();
		fPreferences = new IncludePreferences(cProject);
		fProject = cProject.getProject();
		fCurrentDirectory = fTu.getResource().getParent().getLocation();
		IScannerInfo scannerInfo = fTu.getScannerInfo(true);
		fIncludeSearchPath = CPreprocessor.configureIncludeSearchPath(fCurrentDirectory.toFile(), scannerInfo);
		fIncludeResolutionCache = new HashMap<IncludeInfo, IPath>();
		fInverseIncludeResolutionCache = new HashMap<IPath, IncludeInfo>();
		fHeadersToInclude = new HashSet<IPath>();
		fHeadersAlreadyIncluded = new HashSet<IPath>();
		fHeadersIncludedPreviously = new HashSet<IPath>();
	}

	public ITranslationUnit getTranslationUnit() {
		return fTu;
	}

	public IIndex getIndex() {
		return fIndex;
	}

	public IProject getProject() {
		return fProject;
	}

	public IncludePreferences getPreferences() {
		return fPreferences;
	}

	public boolean isCXXLanguage() {
		return fTu.isCXXLanguage();
	}

	public IPath getCurrentDirectory() {
		return fCurrentDirectory;
	}

	public IPath resolveInclude(IncludeInfo include) {
		IPath path = fIncludeResolutionCache.get(include);
		if (path == null) {
			String directory = fCurrentDirectory.toOSString();
			String filePath = CPreprocessor.getAbsoluteInclusionPath(include.getName(), directory);
	        if (filePath != null) {
	        	path = new Path(filePath);
	        } else if (!include.isSystem() && !fIncludeSearchPath.isInhibitUseOfCurrentFileDirectory()) {
	            // Check to see if we find a match in the current directory.
	    		filePath = ScannerUtility.createReconciledPath(directory, include.getName());
	    		if (fileExists(filePath)) {
	    			path = new Path(filePath);
	    		}
	        }

	        if (path == null) {
				for (IncludeSearchPathElement pathElement : fIncludeSearchPath.getElements()) {
					if (include.isSystem() && pathElement.isForQuoteIncludesOnly())
						continue;
					filePath = pathElement.getLocation(include.getName());
		    		if (fileExists(filePath)) {
		    			path = new Path(filePath);
		    			break;
		    		}
				}
	        }
	        if (path == null)
	        	path = UNRESOLVED_INCLUDE;
	        fIncludeResolutionCache.put(include, path);
	        fInverseIncludeResolutionCache.put(path, include);
		}
		return path == UNRESOLVED_INCLUDE ? null : path;
	}

	/**
	 * Returns the include directive that resolves to the given header file, or {@code null} if
	 * the file is not on the include search path. Current directory is not considered to be a part
	 * of the include path by this method.
	 */
    public IncludeInfo getIncludeForHeaderFile(IPath fullPath) {
    	IncludeInfo include = fInverseIncludeResolutionCache.get(fullPath);
    	if (include != null)
    		return include;
        String headerLocation = fullPath.toOSString();
        String shortestInclude = null;
        boolean isSystem = false;
		for (IncludeSearchPathElement pathElement : fIncludeSearchPath.getElements()) {
			String includeDirective = pathElement.getIncludeDirective(headerLocation);
			if (includeDirective != null &&
					(shortestInclude == null || shortestInclude.length() > includeDirective.length())) {
				shortestInclude = includeDirective;
				isSystem = !pathElement.isForQuoteIncludesOnly();
			}
		}
		if (shortestInclude == null) {
			if (fIncludeSearchPath.isInhibitUseOfCurrentFileDirectory() ||
					!fCurrentDirectory.isPrefixOf(fullPath)) {
				return null;
			}
			shortestInclude = fullPath.removeFirstSegments(fCurrentDirectory.segmentCount()).toString();
		}
		include = new IncludeInfo(shortestInclude, isSystem);
		// Don't put an include to fullPath to fIncludeResolutionCache since it may be wrong
		// if the header was included by #include_next.
		fInverseIncludeResolutionCache.put(fullPath, include);
		return include;
    }

    /**
     * Removes headers that are exported by other headers that will be included
     */
    public void removeExportedHeaders() throws CoreException {
    	// Index files keyed by their absolute paths.
    	Map<IPath, IIndexFile> filesByPath = new HashMap<IPath, IIndexFile>();
    	for (IIndexFile file : fIndex.getAllFiles()) {
    		IPath path = getPath(file);
    		filesByPath.put(path, file);
    	}

    	Set<IPath> exportedHeaders = new HashSet<IPath>();
		for (IPath path : fHeadersToInclude) {
			if (!exportedHeaders.contains(path)) {
				IIndexFile file = filesByPath.get(path);
				if (file != null) {  // file can be null if the header was not indexed.
					ArrayDeque<IIndexFile> queue = new ArrayDeque<IIndexFile>();
					queue.add(file);
					while ((file = queue.pollFirst()) != null) {
						for (IIndexInclude include : file.getIncludes()) {
							if (fPreferences.allowIndirectInclusion || include.isIncludedFileExported()) {
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

	private static IPath getPath(IIndexFile file) throws CoreException {
		return IndexLocationFactory.getAbsolutePath(file.getLocation());
	}
    
	private static boolean fileExists(String absolutePath) {
		return new File(absolutePath).exists();
	}

	public Set<IPath> getHeadersToInclude() {
		return fHeadersToInclude;
	}

	public final void addHeaderToInclude(IPath header) {
		fHeadersToInclude.add(header);
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

	public final void addHeaderIncludedPreviously(IPath header) {
		fHeadersIncludedPreviously.add(header);
	}

	public final boolean wasIncludedPreviously(IPath header) {
		return fHeadersIncludedPreviously.contains(header);
	}
}

/*******************************************************************************
 * Copyright (c) 2013, 2015 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.corext.codemanipulation;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import org.eclipse.cdt.core.CCorePreferenceConstants;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.utils.PathUtil;

import org.eclipse.cdt.internal.core.parser.scanner.CPreprocessor;
import org.eclipse.cdt.internal.core.parser.scanner.IncludeSearchPath;
import org.eclipse.cdt.internal.core.parser.scanner.IncludeSearchPathElement;
import org.eclipse.cdt.internal.core.parser.scanner.ScannerUtility;
import org.eclipse.cdt.internal.core.resources.ResourceLookup;

import org.eclipse.cdt.internal.ui.editor.SourceHeaderPartnerFinder;
import org.eclipse.cdt.internal.ui.refactoring.includes.IncludeGroupStyle;
import org.eclipse.cdt.internal.ui.refactoring.includes.IncludeGroupStyle.IncludeKind;
import org.eclipse.cdt.internal.ui.refactoring.includes.IncludePreferences;

public class InclusionContext {
	private static final IPath UNRESOLVED_INCLUDE = Path.EMPTY;

	private final ITranslationUnit fTu;
	private final IProject fProject;
	private final IPath fCurrentDirectory;
	private final IncludeSearchPath fIncludeSearchPath;
	private final Map<IncludeInfo, IPath> fIncludeResolutionCache;
	private final Map<IPath, IncludeInfo> fInverseIncludeResolutionCache;
	private final IncludePreferences fPreferences;
	private String fSourceContents;
	private String fLineDelimiter;
	private Pattern fKeepPragmaPattern;
	private IPath fTuLocation;

	public InclusionContext(ITranslationUnit tu) {
		fTu = tu;
		fTuLocation = fTu.getLocation();
		ICProject cProject = fTu.getCProject();
		fProject = cProject.getProject();
		fCurrentDirectory = fTuLocation == null ? null : fTuLocation.removeLastSegments(1);
		IScannerInfo scannerInfo = fTu.getScannerInfo(true);
		fIncludeSearchPath = CPreprocessor.configureIncludeSearchPath(fCurrentDirectory.toFile(), scannerInfo);
		fIncludeResolutionCache = new HashMap<>();
		fInverseIncludeResolutionCache = new HashMap<>();
		fPreferences = new IncludePreferences(cProject);
	}

	public final ITranslationUnit getTranslationUnit() {
		return fTu;
	}

	public final IProject getProject() {
		return fProject;
	}

	public final IPath getCurrentDirectory() {
		return fCurrentDirectory;
	}

	public final IncludePreferences getPreferences() {
		return fPreferences;
	}

	public IPath resolveInclude(IncludeInfo include) {
		IPath path = fIncludeResolutionCache.get(include);
		if (path == null) {
			String directory = fCurrentDirectory == null ? null : fCurrentDirectory.toOSString();
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
			if (fIncludeSearchPath.isInhibitUseOfCurrentFileDirectory() || fCurrentDirectory == null ||
					!fCurrentDirectory.isPrefixOf(fullPath)) {
				return null;
			}
			shortestInclude = fullPath.setDevice(null).removeFirstSegments(fCurrentDirectory.segmentCount()).toString();
		}
		include = new IncludeInfo(shortestInclude, isSystem);
		// Don't put an include to fullPath to fIncludeResolutionCache since it may be wrong
		// if the header was included by #include_next.
		fInverseIncludeResolutionCache.put(fullPath, include);
		return include;
    }

	/**
	 * Returns the include directive that resolves to the given header file, or {@code null} if
	 * the file is not on the include search path. Current directory is not considered to be a part
	 * of the include path by this method.
	 */
    public IncludeInfo getIncludeForHeaderFile(IPath fullPath, boolean isSystem) {
    	IncludeInfo include = fInverseIncludeResolutionCache.get(fullPath);
    	if (include != null)
    		return include;
        String headerLocation = fullPath.toOSString();
        String shortestInclude = null;
		for (IncludeSearchPathElement pathElement : fIncludeSearchPath.getElements()) {
			if (isSystem && pathElement.isForQuoteIncludesOnly())
				continue;
			String includeDirective = pathElement.getIncludeDirective(headerLocation);
			if (includeDirective != null &&
					(shortestInclude == null || shortestInclude.length() > includeDirective.length())) {
				shortestInclude = includeDirective;
			}
		}
		if (shortestInclude == null) {
			if (fIncludeSearchPath.isInhibitUseOfCurrentFileDirectory() || fCurrentDirectory == null ||
					!fCurrentDirectory.isPrefixOf(fullPath)) {
				return null;
			}
			shortestInclude = fullPath.setDevice(null).removeFirstSegments(fCurrentDirectory.segmentCount()).toString();
		}
		include = new IncludeInfo(shortestInclude, isSystem);
		// Don't put an include to fullPath to fIncludeResolutionCache since it may be wrong
		// if the header was included by #include_next.
		fInverseIncludeResolutionCache.put(fullPath, include);
		return include;
    }

    public IncludeGroupStyle getIncludeStyle(IPath headerPath) {
		IncludeKind includeKind;
		IncludeInfo includeInfo = getIncludeForHeaderFile(headerPath);
		if (includeInfo != null && includeInfo.isSystem()) {
			if (headerPath.getFileExtension() == null) {
				includeKind = IncludeKind.SYSTEM_WITHOUT_EXTENSION;
			} else {
				includeKind = IncludeKind.SYSTEM_WITH_EXTENSION;
			}
		} else if (isPartnerFile(headerPath)) {
			includeKind = IncludeKind.PARTNER;
		} else {
			IPath dir = getCurrentDirectory();
			if (dir.isPrefixOf(headerPath)) {
				if (headerPath.segmentCount() == dir.segmentCount() + 1) {
					includeKind = IncludeKind.IN_SAME_FOLDER;
				} else {
					includeKind = IncludeKind.IN_SUBFOLDER;
				}
			} else {
				IFile[] files = ResourceLookup.findFilesForLocation(headerPath);
				if (files.length == 0) {
					includeKind = IncludeKind.EXTERNAL;
				} else {
					IProject project = getProject();
					includeKind = IncludeKind.IN_OTHER_PROJECT;
					for (IFile file : files) {
						if (file.getProject().equals(project)) {
							includeKind = IncludeKind.IN_SAME_PROJECT;
							break;
						}
					}
				}
			}
		}
		return fPreferences.includeStyles.get(includeKind);
	}

	public IncludeGroupStyle getIncludeStyle(IncludeInfo includeInfo) {
		IncludeKind includeKind;
		IPath path = Path.fromPortableString(includeInfo.getName());
		if (includeInfo.isSystem()) {
			if (path.getFileExtension() == null) {
				includeKind = IncludeKind.SYSTEM_WITHOUT_EXTENSION;
			} else {
				includeKind = IncludeKind.SYSTEM_WITH_EXTENSION;
			}
		} else if (isPartnerFile(path)) {
			includeKind = IncludeKind.PARTNER;
		} else {
			includeKind = IncludeKind.EXTERNAL;
		}
		return fPreferences.includeStyles.get(includeKind);
	}

	private static boolean fileExists(String absolutePath) {
		return new File(absolutePath).exists();
	}

	/**
	 * Checks if the given path points to a partner header of the current translation unit.
	 * A header is considered a partner if its name without extension is the same as the name of
	 * the translation unit, or the name of the translation unit differs by one of the suffixes
	 * used for test files.
	 */
	public boolean isPartnerFile(IPath path) {
		return SourceHeaderPartnerFinder.isPartnerFile(getTranslationUnitLocation(), path,
				fPreferences.partnerFileSuffixes);
	}

	public IncludeInfo createIncludeInfo(IPath header, IncludeGroupStyle style) {
		String name = null;
		if (style.isRelativePath()) {
			name = getRelativePath(header);
		}
		if (name == null) {
			IncludeInfo includeInfo = getIncludeForHeaderFile(header);
			if (includeInfo != null) {
				name = includeInfo.getName();
			} else {
				name = getRelativePath(header);
			}
			if (name == null) {
				name = header.toPortableString();  // Last resort. 
			}
		}
		return new IncludeInfo(name, style.isAngleBrackets());
	}

	private String getRelativePath(IPath header) {
		IPath relativePath = PathUtil.makeRelativePath(header, getCurrentDirectory());
		if (relativePath == null)
			return null;
		return relativePath.toString();
	}

	public String getSourceContents() {
		if (fSourceContents == null) {
			fSourceContents = new String(fTu.getContents());
		}
		return fSourceContents;
	}

	public String getLineDelimiter() {
		if (fLineDelimiter == null) {
			try {
				fLineDelimiter = StubUtility.getLineDelimiterUsed(fTu);
			} catch (CModelException e) {
				fLineDelimiter = System.getProperty("line.separator", "\n"); //$NON-NLS-1$//$NON-NLS-2$
			}
		}
		return fLineDelimiter;
	}

	public Pattern getKeepPragmaPattern() {
		if (fKeepPragmaPattern == null) {
			String keepPattern = CCorePreferenceConstants.getPreference(
					CCorePreferenceConstants.INCLUDE_KEEP_PATTERN, fProject,
					CCorePreferenceConstants.DEFAULT_INCLUDE_KEEP_PATTERN);
			try {
				fKeepPragmaPattern = Pattern.compile(keepPattern);
			} catch (PatternSyntaxException e) {
				fKeepPragmaPattern = Pattern.compile(CCorePreferenceConstants.DEFAULT_INCLUDE_KEEP_PATTERN);
			}
		}
		return fKeepPragmaPattern;
	}

	/**
	 * Sets the effective translation unit location that overrides the default value obtained by
	 * calling {@code getTranslationUnit().getLocation()}.
	 *
	 * @param location the file system location to set 
	 */
	public void setTranslationUnitLocation(IPath location) {
		this.fTuLocation = location;
	}

	/**
	 * Returns the effective translation unit location.
	 */
	public IPath getTranslationUnitLocation() {
		return fTuLocation;
	}
}
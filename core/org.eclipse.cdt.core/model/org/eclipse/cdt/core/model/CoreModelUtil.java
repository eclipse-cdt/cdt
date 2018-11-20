/*******************************************************************************
 * Copyright (c) 2002, 2013 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    QNX Software Systems - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *    IBM Corporation - EFS support
 *******************************************************************************/
package org.eclipse.cdt.core.model;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.IBinaryParser;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.index.IndexLocationFactory;
import org.eclipse.cdt.core.settings.model.ICConfigExtensionReference;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICTargetPlatformSetting;
import org.eclipse.cdt.internal.core.model.BinaryParserConfig;
import org.eclipse.cdt.internal.core.resources.ResourceLookup;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

/**
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class CoreModelUtil {
	/**
	 * Returns whether the given path matches one of the exclusion patterns.
	 * @param resourcePath
	 * @param exclusionPatterns
	 * @return <code>true</code> if the given path matches one of the exclusion patterns.
	 */
	public static boolean isExcludedPath(IPath resourcePath, IPath[] exclusionPatterns) {
		int length = exclusionPatterns.length;
		char[][] fullCharExclusionPatterns = new char[length][];
		for (int i = 0; i < length; i++) {
			fullCharExclusionPatterns[i] = exclusionPatterns[i].toString().toCharArray();
		}
		return isExcluded(resourcePath, fullCharExclusionPatterns);
	}

	/**
	 * Returns whether the given resource matches one of the exclusion patterns.
	 */
	public final static boolean isExcluded(IResource resource, char[][] exclusionPatterns) {
		IPath path = resource.getFullPath();
		// Ensure that folders are only excluded if all of their children are excluded.
		if (resource.getType() == IResource.FOLDER)
			path = path.append("*"); //$NON-NLS-1$
		return isExcluded(path, exclusionPatterns);
	}

	/**
	 * Returns whether the given resource path matches one of the exclusion patterns.
	 */
	public final static boolean isExcluded(IPath resourcePath, char[][] exclusionPatterns) {
		if (exclusionPatterns == null)
			return false;
		char[] path = resourcePath.toString().toCharArray();
		for (char[] exclusionPattern : exclusionPatterns) {
			if (prefixOfCharArray(exclusionPattern, path)) {
				return true;
			}
			if (pathMatch(exclusionPattern, path, true, '/')) {
				return true;
			}
		}
		return false;
	}

	/*
	 * if b is a prefix of a return true.
	 */
	static boolean prefixOfCharArray(char[] a, char[] b) {
		if (a == b)
			return true;
		if (a == null || b == null)
			return false;
		int len = a.length;
		if (len > b.length)
			return false;
		int i = 0;
		for (; i < len; ++i) {
			if (a[i] != b[i])
				return false;
		}
		if (i < b.length && b[i] != '/') {
			return false;
		}
		return true;
	}

	/**
	 * Answers true if the pattern matches the given name, false otherwise. This char[] pattern matching accepts wild-cards '*' and
	 * '?'.
	 *
	 * When not case sensitive, the pattern is assumed to already be lowercased, the name will be lowercased character per character
	 * as comparing. If name is null, the answer is false. If pattern is null, the answer is true if name is not null. <br>
	 * <br>
	 * For example:
	 * <ol>
	 * <li>
	 *
	 * <pre>
	 *
	 *   pattern = { '?', 'b', '*' }
	 *   name = { 'a', 'b', 'c' , 'd' }
	 *   isCaseSensitive = true
	 *   result =&gt; true
	 *
	 * </pre>
	 *
	 * </li>
	 * <li>
	 *
	 * <pre>
	 *
	 *   pattern = { '?', 'b', '?' }
	 *   name = { 'a', 'b', 'c' , 'd' }
	 *   isCaseSensitive = true
	 *   result =&gt; false
	 *
	 * </pre>
	 *
	 * </li>
	 * <li>
	 *
	 * <pre>
	 *
	 *   pattern = { 'b', '*' }
	 *   name = { 'a', 'b', 'c' , 'd' }
	 *   isCaseSensitive = true
	 *   result =&gt; false
	 *
	 * </pre>
	 *
	 * </li>
	 * </ol>
	 *
	 * @param pattern
	 *            the given pattern
	 * @param name
	 *            the given name
	 * @param isCaseSensitive
	 *            flag to know whether or not the matching should be case sensitive
	 * @return true if the pattern matches the given name, false otherwise
	 */
	public static final boolean match(char[] pattern, char[] name, boolean isCaseSensitive) {
		if (name == null)
			return false; // null name cannot match
		if (pattern == null)
			return true; // null pattern is equivalent to '*'
		return match(pattern, 0, pattern.length, name, 0, name.length, isCaseSensitive);
	}

	/**
	 * Answers true if the a sub-pattern matches the subpart of the given name, false otherwise. char[] pattern matching, accepting
	 * wild-cards '*' and '?'. Can match only subset of name/pattern. end positions are non-inclusive. The subpattern is defined by
	 * the patternStart and pattternEnd positions. When not case sensitive, the pattern is assumed to already be lowercased, the
	 * name will be lowercased character per character as comparing. <br>
	 * <br>
	 * For example:
	 * <ol>
	 * <li>
	 *
	 * <pre>
	 *
	 *   pattern = { '?', 'b', '*' }
	 *   patternStart = 1
	 *   patternEnd = 3
	 *   name = { 'a', 'b', 'c' , 'd' }
	 *   nameStart = 1
	 *   nameEnd = 4
	 *   isCaseSensitive = true
	 *   result =&gt; true
	 *
	 * </pre>
	 *
	 * </li>
	 * <li>
	 *
	 * <pre>
	 *
	 *   pattern = { '?', 'b', '*' }
	 *   patternStart = 1
	 *   patternEnd = 2
	 *   name = { 'a', 'b', 'c' , 'd' }
	 *   nameStart = 1
	 *   nameEnd = 2
	 *   isCaseSensitive = true
	 *   result =&gt; false
	 *
	 * </pre>
	 *
	 * </li>
	 * </ol>
	 *
	 * @param pattern
	 *            the given pattern
	 * @param patternStart
	 *            the given pattern start
	 * @param patternEnd
	 *            the given pattern end
	 * @param name
	 *            the given name
	 * @param nameStart
	 *            the given name start
	 * @param nameEnd
	 *            the given name end
	 * @param isCaseSensitive
	 *            flag to know if the matching should be case sensitive
	 * @return true if the a sub-pattern matches the subpart of the given name, false otherwise
	 */
	public static final boolean match(char[] pattern, int patternStart, int patternEnd, char[] name, int nameStart,
			int nameEnd, boolean isCaseSensitive) {
		if (name == null)
			return false; // null name cannot match
		if (pattern == null)
			return true; // null pattern is equivalent to '*'
		int iPattern = patternStart;
		int iName = nameStart;
		if (patternEnd < 0)
			patternEnd = pattern.length;
		if (nameEnd < 0)
			nameEnd = name.length;
		/* check first segment */
		char patternChar = 0;
		while ((iPattern < patternEnd) && (patternChar = pattern[iPattern]) != '*') {
			if (iName == nameEnd)
				return false;
			if (patternChar != (isCaseSensitive ? name[iName] : Character.toLowerCase(name[iName]))
					&& patternChar != '?') {
				return false;
			}
			iName++;
			iPattern++;
		}
		/* check sequence of star+segment */
		int segmentStart;
		if (patternChar == '*') {
			segmentStart = ++iPattern; // skip star
		} else {
			segmentStart = 0; // force iName check
		}
		int prefixStart = iName;
		checkSegment: while (iName < nameEnd) {
			if (iPattern == patternEnd) {
				iPattern = segmentStart; // mismatch - restart current segment
				iName = ++prefixStart;
				continue checkSegment;
			}
			/* segment is ending */
			if ((patternChar = pattern[iPattern]) == '*') {
				segmentStart = ++iPattern; // skip start
				if (segmentStart == patternEnd) {
					return true;
				}
				prefixStart = iName;
				continue checkSegment;
			}
			/* check current name character */
			if ((isCaseSensitive ? name[iName] : Character.toLowerCase(name[iName])) != patternChar
					&& patternChar != '?') {
				iPattern = segmentStart; // mismatch - restart current segment
				iName = ++prefixStart;
				continue checkSegment;
			}
			iName++;
			iPattern++;
		}
		return (segmentStart == patternEnd) || (iName == nameEnd && iPattern == patternEnd)
				|| (iPattern == patternEnd - 1 && pattern[iPattern] == '*');
	}

	/**
	 * Answers true if the pattern matches the filepath using the pathSepatator, false otherwise.
	 *
	 * Path char[] pattern matching, accepting wild-cards '**', '*' and '?' (using Ant directory tasks conventions, also see
	 * "http://jakarta.apache.org/ant/manual/dirtasks.html#defaultexcludes"). Path pattern matching is enhancing regular pattern
	 * matching in supporting extra rule where '**' represent any folder combination. Special rule: - foo\ is equivalent to foo\**
	 * When not case sensitive, the pattern is assumed to already be lowercased, the name will be lowercased character per character
	 * as comparing.
	 *
	 * @param pattern
	 *            the given pattern
	 * @param filepath
	 *            the given path
	 * @param isCaseSensitive
	 *            to find out whether or not the matching should be case sensitive
	 * @param pathSeparator
	 *            the given path separator
	 * @return true if the pattern matches the filepath using the pathSepatator, false otherwise
	 */
	public static final boolean pathMatch(char[] pattern, char[] filepath, boolean isCaseSensitive,
			char pathSeparator) {
		if (filepath == null)
			return false; // null name cannot match
		if (pattern == null)
			return true; // null pattern is equivalent to '*'
		// offsets inside pattern
		int pSegmentStart = pattern[0] == pathSeparator ? 1 : 0;
		int pLength = pattern.length;
		int pSegmentEnd = indexOf(pathSeparator, pattern, pSegmentStart + 1);
		if (pSegmentEnd < 0)
			pSegmentEnd = pLength;
		// special case: pattern foo\ is equivalent to foo\**
		boolean freeTrailingDoubleStar = pattern[pLength - 1] == pathSeparator;
		// offsets inside filepath
		int fSegmentStart, fLength = filepath.length;
		if (filepath[0] != pathSeparator) {
			fSegmentStart = 0;
		} else {
			fSegmentStart = 1;
		}
		if (fSegmentStart != pSegmentStart) {
			return false; // both must start with a separator or none.
		}
		int fSegmentEnd = indexOf(pathSeparator, filepath, fSegmentStart + 1);
		if (fSegmentEnd < 0)
			fSegmentEnd = fLength;
		// first segments
		while (pSegmentStart < pLength
				&& !(pSegmentEnd == pLength && freeTrailingDoubleStar || (pSegmentEnd == pSegmentStart + 2
						&& pattern[pSegmentStart] == '*' && pattern[pSegmentStart + 1] == '*'))) {
			if (fSegmentStart >= fLength)
				return false;
			if (!match(pattern, pSegmentStart, pSegmentEnd, filepath, fSegmentStart, fSegmentEnd, isCaseSensitive)) {
				return false;
			}
			// jump to next segment
			pSegmentEnd = indexOf(pathSeparator, pattern, pSegmentStart = pSegmentEnd + 1);
			// skip separator
			if (pSegmentEnd < 0)
				pSegmentEnd = pLength;
			fSegmentEnd = indexOf(pathSeparator, filepath, fSegmentStart = fSegmentEnd + 1);
			// skip separator
			if (fSegmentEnd < 0)
				fSegmentEnd = fLength;
		}
		/* check sequence of doubleStar+segment */
		int pSegmentRestart;
		if ((pSegmentStart >= pLength && freeTrailingDoubleStar) || (pSegmentEnd == pSegmentStart + 2
				&& pattern[pSegmentStart] == '*' && pattern[pSegmentStart + 1] == '*')) {
			pSegmentEnd = indexOf(pathSeparator, pattern, pSegmentStart = pSegmentEnd + 1);
			// skip separator
			if (pSegmentEnd < 0)
				pSegmentEnd = pLength;
			pSegmentRestart = pSegmentStart;
		} else {
			if (pSegmentStart >= pLength)
				return fSegmentStart >= fLength; // true if filepath is done
			// too.
			pSegmentRestart = 0; // force fSegmentStart check
		}
		int fSegmentRestart = fSegmentStart;
		checkSegment: while (fSegmentStart < fLength) {
			if (pSegmentStart >= pLength) {
				if (freeTrailingDoubleStar)
					return true;
				// mismatch - restart current path segment
				pSegmentEnd = indexOf(pathSeparator, pattern, pSegmentStart = pSegmentRestart);
				if (pSegmentEnd < 0)
					pSegmentEnd = pLength;
				fSegmentRestart = indexOf(pathSeparator, filepath, fSegmentRestart + 1);
				// skip separator
				if (fSegmentRestart < 0) {
					fSegmentRestart = fLength;
				} else {
					fSegmentRestart++;
				}
				fSegmentEnd = indexOf(pathSeparator, filepath, fSegmentStart = fSegmentRestart);
				if (fSegmentEnd < 0)
					fSegmentEnd = fLength;
				continue checkSegment;
			}
			/* path segment is ending */
			if (pSegmentEnd == pSegmentStart + 2 && pattern[pSegmentStart] == '*'
					&& pattern[pSegmentStart + 1] == '*') {
				pSegmentEnd = indexOf(pathSeparator, pattern, pSegmentStart = pSegmentEnd + 1);
				// skip separator
				if (pSegmentEnd < 0)
					pSegmentEnd = pLength;
				pSegmentRestart = pSegmentStart;
				fSegmentRestart = fSegmentStart;
				if (pSegmentStart >= pLength)
					return true;
				continue checkSegment;
			}
			/* chech current path segment */
			if (!match(pattern, pSegmentStart, pSegmentEnd, filepath, fSegmentStart, fSegmentEnd, isCaseSensitive)) {
				// mismatch - restart current path segment
				pSegmentEnd = indexOf(pathSeparator, pattern, pSegmentStart = pSegmentRestart);
				if (pSegmentEnd < 0)
					pSegmentEnd = pLength;
				fSegmentRestart = indexOf(pathSeparator, filepath, fSegmentRestart + 1);
				// skip separator
				if (fSegmentRestart < 0) {
					fSegmentRestart = fLength;
				} else {
					fSegmentRestart++;
				}
				fSegmentEnd = indexOf(pathSeparator, filepath, fSegmentStart = fSegmentRestart);
				if (fSegmentEnd < 0)
					fSegmentEnd = fLength;
				continue checkSegment;
			}
			// jump to next segment
			pSegmentEnd = indexOf(pathSeparator, pattern, pSegmentStart = pSegmentEnd + 1);
			// skip separator
			if (pSegmentEnd < 0)
				pSegmentEnd = pLength;
			fSegmentEnd = indexOf(pathSeparator, filepath, fSegmentStart = fSegmentEnd + 1);
			// skip separator
			if (fSegmentEnd < 0)
				fSegmentEnd = fLength;
		}
		return (pSegmentRestart >= pSegmentEnd) || (fSegmentStart >= fLength && pSegmentStart >= pLength)
				|| (pSegmentStart == pLength - 2 && pattern[pSegmentStart] == '*' && pattern[pSegmentStart + 1] == '*')
				|| (pSegmentStart == pLength && freeTrailingDoubleStar);
	}

	/**
	 * Answers the first index in the array for which the corresponding character is equal to toBeFound. Answers -1 if no occurrence
	 * of this character is found. <br>
	 * <br>
	 * For example:
	 * <ol>
	 * <li>
	 *
	 * <pre>
	 *
	 *   toBeFound = 'c'
	 *   array = { ' a', 'b', 'c', 'd' }
	 *   result =&gt; 2
	 *
	 * </pre>
	 *
	 * </li>
	 * <li>
	 *
	 * <pre>
	 *
	 *   toBeFound = 'e'
	 *   array = { ' a', 'b', 'c', 'd' }
	 *   result =&gt; -1
	 *
	 * </pre>
	 *
	 * </li>
	 * </ol>
	 *
	 * @param toBeFound
	 *            the character to search
	 * @param array
	 *            the array to be searched
	 * @return the first index in the array for which the corresponding character is equal to toBeFound, -1 otherwise
	 * @throws NullPointerException
	 *             if array is null
	 */
	public static final int indexOf(char toBeFound, char[] array) {
		for (int i = 0; i < array.length; i++)
			if (toBeFound == array[i])
				return i;
		return -1;
	}

	/**
	 * Answers the first index in the array for which the corresponding character is equal to toBeFound starting the search at index
	 * start. Answers -1 if no occurrence of this character is found. <br>
	 * <br>
	 * For example:
	 * <ol>
	 * <li>
	 *
	 * <pre>
	 *
	 *   toBeFound = 'c'
	 *   array = { ' a', 'b', 'c', 'd' }
	 *   start = 2
	 *   result =&gt; 2
	 *
	 * </pre>
	 *
	 * </li>
	 * <li>
	 *
	 * <pre>
	 *
	 *   toBeFound = 'c'
	 *   array = { ' a', 'b', 'c', 'd' }
	 *   start = 3
	 *   result =&gt; -1
	 *
	 * </pre>
	 *
	 * </li>
	 * <li>
	 *
	 * <pre>
	 *
	 *   toBeFound = 'e'
	 *   array = { ' a', 'b', 'c', 'd' }
	 *   start = 1
	 *   result =&gt; -1
	 *
	 * </pre>
	 *
	 * </li>
	 * </ol>
	 *
	 * @param toBeFound
	 *            the character to search
	 * @param array
	 *            the array to be searched
	 * @param start
	 *            the starting index
	 * @return the first index in the array for which the corresponding character is equal to toBeFound, -1 otherwise
	 * @throws NullPointerException
	 *             if array is null
	 * @throws ArrayIndexOutOfBoundsException
	 *             if start is lower than 0
	 */
	public static final int indexOf(char toBeFound, char[] array, int start) {
		for (int i = start; i < array.length; i++) {
			if (toBeFound == array[i])
				return i;
		}
		return -1;
	}

	/**
	 * Searches for a translation unit within the cprojects. For external files the ones
	 * from the given project are preferred.
	 * @since 4.0
	 */
	public static ITranslationUnit findTranslationUnitForLocation(IPath location, ICProject preferredProject)
			throws CModelException {
		IFile[] files = ResourceLookup.findFilesForLocation(location);
		ResourceLookup.sortFilesByRelevance(files, preferredProject != null ? preferredProject.getProject() : null);
		boolean oneExisting = false;
		if (files.length > 0) {
			for (IFile file : files) {
				if (file.exists()) {
					oneExisting = true;
					ITranslationUnit tu = findTranslationUnit(file);
					if (tu != null) {
						return tu;
					}
				}
			}
			if (oneExisting)
				return null;
		}
		CoreModel coreModel = CoreModel.getDefault();
		ITranslationUnit tu = null;
		if (preferredProject != null) {
			tu = coreModel.createTranslationUnitFrom(preferredProject, location);
		}
		if (tu == null) {
			ICProject[] projects = coreModel.getCModel().getCProjects();
			for (int i = 0; i < projects.length && tu == null; i++) {
				ICProject project = projects[i];
				if (!project.equals(preferredProject)) {
					tu = coreModel.createTranslationUnitFrom(project, location);
				}
			}
		}
		return tu;
	}

	/**
	 * Searches for a translation unit within the cprojects. For external files the ones
	 * from the given project are preferred.
	 * @since 5.0
	 */
	public static ITranslationUnit findTranslationUnitForLocation(URI locationURI, ICProject preferredProject)
			throws CModelException {
		IFile[] files = ResourceLookup.findFilesForLocationURI(locationURI);
		if (files.length > 0) {
			for (IFile file : files) {
				ITranslationUnit tu = findTranslationUnit(file);
				if (tu != null) {
					return tu;
				}
			}
		} else {
			CoreModel coreModel = CoreModel.getDefault();
			ITranslationUnit tu = null;
			if (preferredProject != null) {
				tu = coreModel.createTranslationUnitFrom(preferredProject, locationURI);
			}
			if (tu == null) {
				ICProject[] projects = coreModel.getCModel().getCProjects();
				for (int i = 0; i < projects.length && tu == null; i++) {
					ICProject project = projects[i];
					if (!project.equals(preferredProject)) {
						tu = coreModel.createTranslationUnitFrom(project, locationURI);
					}
				}
			}
			return tu;
		}
		return null;
	}

	/**
	 * Returns the translation unit for the location given or <code>null</code>.
	 * @throws CModelException
	 */
	public static ITranslationUnit findTranslationUnitForLocation(IIndexFileLocation ifl, ICProject preferredProject)
			throws CModelException {
		String fullPath = ifl.getFullPath();
		if (fullPath != null) {
			IResource file = ResourcesPlugin.getWorkspace().getRoot().findMember(fullPath);
			if (file instanceof IFile) {
				return findTranslationUnit((IFile) file);
			}
			return null;
		}
		IPath location = IndexLocationFactory.getAbsolutePath(ifl);
		if (location != null) {
			CoreModel coreModel = CoreModel.getDefault();
			ITranslationUnit tu = null;
			if (preferredProject != null) {
				tu = coreModel.createTranslationUnitFrom(preferredProject, location);
			}
			if (tu == null) {
				ICProject[] projects = coreModel.getCModel().getCProjects();
				for (int i = 0; i < projects.length && tu == null; i++) {
					ICProject project = projects[i];
					if (!project.equals(preferredProject)) {
						tu = coreModel.createTranslationUnitFrom(project, location);
					}
				}
			}
			return tu;
		}
		return null;
	}

	/**
	 * Returns the translation unit for the file given or <code>null</code>.
	 */
	public static ITranslationUnit findTranslationUnit(IFile file) {
		if (CoreModel.isTranslationUnit(file) && file.exists()) {
			ICProject cp = CoreModel.getDefault().getCModel().getCProject(file.getProject().getName());
			if (cp != null) {
				ICElement tu;
				try {
					tu = cp.findElement(file.getProjectRelativePath());
					if (tu instanceof ITranslationUnit) {
						return (ITranslationUnit) tu;
					}
				} catch (CModelException e) {
					if (e.getStatus().getCode() == ICModelStatusConstants.INVALID_PATH) {
						return null;
					}
					CCorePlugin.log(e);
				}
			}
		}
		return null;
	}

	/**
	 * Returns the configuration descriptions referenced directly by the specified
	 * configuration description. The result will not contain duplicates. Returns
	 * an empty array if there are no referenced configuration descriptions.
	 *
	 * @param cfgDes
	 * @param writable - specifies whether the returned descriptions should be writable or read-only
	 * @since 4.0
	 * @return a list of configuration descriptions
	 * @see CoreModelUtil#getReferencingConfigurationDescriptions(ICConfigurationDescription, boolean)
	 */

	public static ICConfigurationDescription[] getReferencedConfigurationDescriptions(ICConfigurationDescription cfgDes,
			boolean writable) {
		List<ICConfigurationDescription> result = new ArrayList<>();

		if (cfgDes != null) {
			Map<String, String> map = cfgDes.getReferenceInfo();
			if (map.size() != 0) {
				IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
				CoreModel model = CoreModel.getDefault();
				for (Map.Entry<String, String> entry : map.entrySet()) {
					String projName = entry.getKey();
					String cfgId = entry.getValue();
					IProject project = root.getProject(projName);
					if (!project.isAccessible())
						continue;

					ICProjectDescription des = null;
					try {
						des = model.getProjectDescription(project, writable);
					} catch (Exception e) {
						// log the error except if the project got closed in another thread which is OK
						if (project.isAccessible()) {
							CCorePlugin.log(e);
						}
					}
					if (des == null)
						continue;

					ICConfigurationDescription refCfgDes;
					if (cfgId != null && cfgId.length() > 0) {
						refCfgDes = des.getConfigurationById(cfgId);
					} else {
						refCfgDes = des.getActiveConfiguration();
					}
					if (refCfgDes != null)
						result.add(refCfgDes);
				}
			}
		}

		return result.toArray(new ICConfigurationDescription[result.size()]);
	}

	/**
	 * Returns the list of all configuration descriptions which directly reference
	 * the specified configuration description. Returns an empty array if there are
	 * no referencing configuration descriptions.
	 *
	 * @since 4.0
	 * @param cfgDes
	 * @param writable - specifies whether the returned descriptions should be writable or read-only
	 * @return a list of configuration descriptions referencing this configuration description
	 * @see CoreModelUtil#getReferencedConfigurationDescriptions(ICConfigurationDescription, boolean)
	 */
	public static ICConfigurationDescription[] getReferencingConfigurationDescriptions(
			ICConfigurationDescription cfgDes, boolean writable) {
		List<ICConfigurationDescription> result = new ArrayList<>();

		if (cfgDes != null) {
			CoreModel core = CoreModel.getDefault();
			IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();

			for (IProject cproject : projects) {
				ICProjectDescription prjDes = core.getProjectDescription(cproject, writable);
				// In case this is not a CDT project the description will be null, so check for null
				if (prjDes != null) {
					ICConfigurationDescription[] cfgDscs = prjDes.getConfigurations();
					for (ICConfigurationDescription cfgDsc : cfgDscs) {
						ICConfigurationDescription[] references = getReferencedConfigurationDescriptions(cfgDsc, false);
						for (ICConfigurationDescription reference : references) {
							if (reference != null && reference.getId().equals(cfgDes.getId())) {
								result.add(cfgDsc);
								break;
							}
						}
					}
				}
			}
		}

		return result.toArray(new ICConfigurationDescription[result.size()]);
	}

	/**
	 * Returns binary parser IDs for configurations
	 * @param cfgs - array of configurations where we need search
	 * @return - array of binary parser ids (Strings)
	 */
	public static String[] getBinaryParserIds(ICConfigurationDescription[] cfgs) {
		if (cfgs == null || cfgs.length == 0)
			return null;
		ArrayList<String> pids = new ArrayList<>();
		for (ICConfigurationDescription cfg : cfgs) {
			ICTargetPlatformSetting tps = cfg.getTargetPlatformSetting();
			String[] ids = tps.getBinaryParserIds();
			for (int j = 0; j < ids.length; j++) {
				if (!pids.contains(ids[j]))
					pids.add(ids[j]);
			}
		}
		return pids.toArray(new String[pids.size()]);
	}

	/**
	 * Sets binary parser ID list to given configurations
	 * @param cfgs - array of configurations where we need search
	 * @param pids - array of binary parser ids (Strings)
	 */
	public static void setBinaryParserIds(ICConfigurationDescription[] cfgs, String[] pids) {
		if (cfgs == null || cfgs.length == 0)
			return;
		for (ICConfigurationDescription cfg : cfgs) {
			cfg.getTargetPlatformSetting().setBinaryParserIds(pids);
		}
	}

	/**
	 * Instantiate binary parser for given extension reference.
	 *
	 * @param ref  binary parser extension reference
	 * @return a binary parser instance
	 * @throws CoreException  if the parser could not be created
	 * @since 5.3
	 */
	public static IBinaryParser getBinaryParser(ICConfigExtensionReference ref) throws CoreException {
		return new BinaryParserConfig(ref).getBinaryParser();
	}
}

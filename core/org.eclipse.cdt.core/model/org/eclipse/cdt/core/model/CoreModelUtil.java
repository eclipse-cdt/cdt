package org.eclipse.cdt.core.model;

import org.eclipse.cdt.internal.core.CharOperation;
import org.eclipse.core.runtime.IPath;

public class CoreModelUtil {

	public static boolean isExcludedPath(IPath resourcePath, IPath[] exclusionPatterns) {
		char[] path = resourcePath.toString().toCharArray();
		for (int i = 0, length = exclusionPatterns.length; i < length; i++) {
			char[] pattern = exclusionPatterns[i].toString().toCharArray();
			if (CharOperation.pathMatch(pattern, path, true, '/')) {
				return true;
			}
		}
		return false;
	}

	/*
	 * Returns whether the given resource path matches one of the exclusion
	 * patterns.
	 * 
	 * @see IPathEntry#getExclusionPatterns
	 */
	public final static boolean isExcluded(IPath resourcePath, char[][] exclusionPatterns) {
		if (exclusionPatterns == null)
			return false;
		char[] path = resourcePath.toString().toCharArray();
		for (int i = 0, length = exclusionPatterns.length; i < length; i++)
			if (CharOperation.pathMatch(exclusionPatterns[i], path, true, '/'))
				return true;
		return false;
	}

}
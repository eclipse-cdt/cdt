package org.eclipse.cdt.core.settings.model;

import org.eclipse.core.runtime.IPath;

public interface ICExclusionPatternPathEntry extends ICPathEntry {
	/**
	 * Returns an array of inclusion paths affecting the
	 * source folder when looking for files recursively.
	 * @return IPath
	 */
	IPath[] getExclusionPatterns();
	
	/**
	 * Returns a char based representation of the exclusions patterns full path.
	 */
	public char[][] fullExclusionPatternChars();
}

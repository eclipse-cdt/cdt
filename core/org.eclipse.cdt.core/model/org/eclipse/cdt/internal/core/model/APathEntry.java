/**********************************************************************
 * Created on 25-Mar-2003
 *
 * Copyright (c) 2002,2003 QNX Software Systems Ltd. and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.internal.core.model;

import org.eclipse.core.runtime.IPath;

public abstract class APathEntry extends PathEntry {

	public static IPath[] NO_EXCLUSION_PATTERNS = {};
	IPath[] exclusionPatterns;
	IPath basePath;
	private final static char[][] UNINIT_PATTERNS = new char[][] { "Non-initialized yet".toCharArray() }; //$NON-NLS-1$
	char[][]fullCharExclusionPatterns = UNINIT_PATTERNS;

	public APathEntry (int kind, IPath path, IPath basePath, IPath[] exclusionPatterns, boolean isExported) {
		super(kind, path, isExported);
		this.basePath = basePath;
		if (exclusionPatterns == null) {
			this.exclusionPatterns = NO_EXCLUSION_PATTERNS;
		} else {
			this.exclusionPatterns = exclusionPatterns;
		}
	}

	/**
	 * Returns the exclusion patterns
	 * @return IPath[]
	 */
	public IPath[] getExclusionPatterns() {
		return exclusionPatterns;
	}

	/**
	 * Returns the base Path
	 * @return IPath
	 */
	public IPath getBasePath() {
		return basePath;
	}

	/**
	 * Returns a char based representation of the exclusions patterns full path.
	 */
	public char[][] fullExclusionPatternChars() {
		if (this.fullCharExclusionPatterns == UNINIT_PATTERNS) {
			int length = this.exclusionPatterns.length;
			this.fullCharExclusionPatterns = new char[length][];
			IPath prefixPath = this.path.removeTrailingSeparator();
			for (int i = 0; i < length; i++) {
				this.fullCharExclusionPatterns[i] = 
					prefixPath.append(this.exclusionPatterns[i]).toString().toCharArray();
			}
		}
		return this.fullCharExclusionPatterns;
	}

	public boolean equals(Object obj) {
		if (obj instanceof APathEntry) {
			APathEntry otherEntry = (APathEntry)obj;
			if (!super.equals(otherEntry)) {
				return false;
			}
			IPath[] otherExcludes = otherEntry.getExclusionPatterns();
			if (exclusionPatterns != otherExcludes) {
				int excludeLength = (exclusionPatterns == null) ? 0 : exclusionPatterns.length;
				if (otherExcludes.length != excludeLength) {
					return false;
				}
				for (int i = 0; i < excludeLength; i++) {
					// compare toStrings instead of IPaths
					// since IPath.equals is specified to ignore trailing separators
					String myPattern = exclusionPatterns[i].toString();
					if (!myPattern.equals(otherExcludes[i].toString())) {
						return false;
					}
				}
			}
			IPath otherBasePath = otherEntry.getBasePath();
			if (basePath != null) {
				if (otherBasePath != null && !basePath.equals(otherBasePath)) {
					return false;
				}
			}
			return true;
		}
		return super.equals(obj);
	}

}

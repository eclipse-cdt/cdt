/*******************************************************************************
 * Copyright (c) 2000, 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *     IBM Corporation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.model;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

public abstract class APathEntry extends PathEntry {

	public static IPath[] NO_EXCLUSION_PATTERNS = {};
	IPath[] exclusionPatterns;
	IPath basePath;
	IPath baseRef;
	private final static char[][] UNINIT_PATTERNS = new char[][] { "Non-initialized yet".toCharArray() }; //$NON-NLS-1$
	char[][]fullCharExclusionPatterns = UNINIT_PATTERNS;

	/**
	 * 
	 * @param kind
	 * @param basePath
	 * @param baseRef
	 * @param path
	 * @param exclusionPatterns
	 * @param isExported
	 */
	public APathEntry (int kind, IPath basePath, IPath baseRef, IPath path, IPath[] exclusionPatterns, boolean isExported) {
		super(kind, path, isExported);
		this.basePath = (basePath == null) ? Path.EMPTY : basePath;
		this.baseRef = (baseRef == null) ? Path.EMPTY : baseRef;
		this.exclusionPatterns = (exclusionPatterns == null) ? NO_EXCLUSION_PATTERNS : exclusionPatterns;
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

	public IPath getBaseReference() {
		return baseRef;
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((basePath == null) ? 0 : basePath.hashCode());
		result = prime * result + ((baseRef == null) ? 0 : baseRef.hashCode());
		result = prime * result + Arrays.hashCode(exclusionPatterns);
		return result;
	}

	@Override
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
				
				Set<String> excludeSet = new HashSet<String>();
				Set<String> otherSet = new HashSet<String>();
				for (int i=0; i < excludeLength; i++) {
					if (exclusionPatterns[i] != otherExcludes[i]) {
						// compare toStrings instead of IPaths
						// since IPath.equals is specified to ignore trailing separators
						excludeSet.add(exclusionPatterns[i].toString());
						otherSet.add(otherExcludes[i].toString());
					}
				}
				if (!excludeSet.equals(otherSet)) {
					return false;
				}
			}
			IPath otherBasePath = otherEntry.getBasePath();
			if (basePath != null) {
				if (otherBasePath != null && !basePath.equals(otherBasePath)) {
					return false;
				}
			}
			IPath otherBaseRef = otherEntry.getBaseReference();
			if (baseRef != null) {
				if (otherBaseRef != null && !baseRef.equals(otherBaseRef)) {
					return false;
				}
			}
			return true;
		}
		return super.equals(obj);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(super.toString());
		if (basePath != null && !basePath.isEmpty()) {
			sb.append(" base-path:").append(basePath.toString()); //$NON-NLS-1$
		}
		if (baseRef != null && !baseRef.isEmpty()) {
			sb.append(" base-ref:").append(baseRef.toString()); //$NON-NLS-1$
		}
		return sb.toString();
	}

}

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
	boolean isRecursive;

	public APathEntry (int kind, IPath path, boolean isRecursive, IPath[] exclusionPatterns, boolean isExported) {
		super(kind, path, isExported);
		this.exclusionPatterns = exclusionPatterns;
		this.isRecursive = isRecursive;
	}

	/**
	 * Returns the exclusion patterns
	 * @return IPath[]
	 */
	public IPath[] getExclusionPatterns() {
		return exclusionPatterns;
	}
 
	/**
	 * Whether or not it is recursive
	 * @return boolean
	 */
	public boolean isRecursive() {
		return isRecursive;
	}

	public boolean equals(Object obj) {
		if (obj instanceof APathEntry) {
			APathEntry otherEntry = (APathEntry)obj;
			if (!super.equals(otherEntry)) {
				return false;
			}
			if (isRecursive != otherEntry.isRecursive()) {
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
			return true;
		}
		return super.equals(obj);
	}

}

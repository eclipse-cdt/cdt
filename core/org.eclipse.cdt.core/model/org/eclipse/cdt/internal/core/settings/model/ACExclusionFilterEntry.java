/*******************************************************************************
 * Copyright (c) 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.settings.model;

import java.util.Arrays;

import org.eclipse.cdt.core.settings.model.ICExclusionPatternPathEntry;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.IPath;

public abstract class ACExclusionFilterEntry extends ACLanguageSettingPathEntry implements ICExclusionPatternPathEntry {
	private IPath[] exclusionPatterns;
	private final static char[][] UNINIT_PATTERNS = new char[][] { "Non-initialized yet".toCharArray() }; //$NON-NLS-1$
	char[][]fullCharExclusionPatterns = UNINIT_PATTERNS;


	public ACExclusionFilterEntry(IPath path, IPath exclusionPatterns[] , int flags) {
		super(path, flags);
		this.exclusionPatterns = exclusionPatterns != null ? (IPath[])exclusionPatterns.clone() : new IPath[0];
	}

	public ACExclusionFilterEntry(IFolder rc, IPath exclusionPatterns[], int flags) {
		super(rc, flags);
		this.exclusionPatterns = exclusionPatterns != null ? (IPath[])exclusionPatterns.clone() : new IPath[0];
	}

	public ACExclusionFilterEntry(String value, IPath exclusionPatterns[], int flags) {
		super(value, flags);
		this.exclusionPatterns = exclusionPatterns != null ? (IPath[])exclusionPatterns.clone() : new IPath[0];
	}

	protected final boolean isFile() {
		return false;
	}
	
	/**
	 * Returns the exclusion patterns
	 * @return IPath[]
	 */
	public IPath[] getExclusionPatterns() {
		return exclusionPatterns;
	}

	/**
	 * Returns a char based representation of the exclusions patterns full path.
	 */
	public char[][] fullExclusionPatternChars() {
		if (this.fullCharExclusionPatterns == UNINIT_PATTERNS) {
			int length = this.exclusionPatterns.length;
			this.fullCharExclusionPatterns = new char[length][];
			IPath path = getFullPath();
			if(path == null)
				path = getLocation();
			IPath prefixPath = path.removeTrailingSeparator();
			for (int i = 0; i < length; i++) {
				this.fullCharExclusionPatterns[i] = 
					prefixPath.append(this.exclusionPatterns[i]).toString().toCharArray();
			}
		}
		return this.fullCharExclusionPatterns;
	}
	
	public boolean equals(Object other) {
		if(!super.equals(other))
			return false;
		
		ACExclusionFilterEntry otherEntry = (ACExclusionFilterEntry)other;
		return Arrays.equals(exclusionPatterns, otherEntry.exclusionPatterns);
	}

	public int hashCode() {
		return super.hashCode() + exclusionPatterns.hashCode();
	}

	public boolean equalsByContents(ICLanguageSettingEntry entry) {
		if(!super.equalsByContents(entry))
			return false;
		
		ACExclusionFilterEntry otherEntry = (ACExclusionFilterEntry)entry;
		return Arrays.equals(exclusionPatterns, otherEntry.exclusionPatterns);
	}
}

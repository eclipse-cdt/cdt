/*******************************************************************************
 * Copyright (c) 2007, 2008 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.settings.model;

import java.util.Arrays;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.IPath;

public abstract class ACExclusionFilterEntry extends ACPathEntry implements ICExclusionPatternPathEntry {
	private final IPath[] exclusionPatterns;
	private final static char[][] UNINIT_PATTERNS = new char[][] { "Non-initialized yet".toCharArray() }; //$NON-NLS-1$
	/** calculated value, does not have to be final */
	char[][] fullCharExclusionPatterns = UNINIT_PATTERNS;


	ACExclusionFilterEntry(IPath path, IPath exclusionPatterns[] , int flags) {
		super(path, flags);
		this.exclusionPatterns = exclusionPatterns != null ? (IPath[])exclusionPatterns.clone() : new IPath[0];
	}

	ACExclusionFilterEntry(IFolder rc, IPath exclusionPatterns[], int flags) {
		super(rc, flags);
		this.exclusionPatterns = exclusionPatterns != null ? (IPath[])exclusionPatterns.clone() : new IPath[0];
	}

	ACExclusionFilterEntry(String value, IPath exclusionPatterns[], int flags) {
		super(value, flags);
		this.exclusionPatterns = exclusionPatterns != null ? (IPath[])exclusionPatterns.clone() : new IPath[0];
	}

	/**
	 * @since 5.4
	 */
	@Override
	public final boolean isFile() {
		return false;
	}

	/**
	 * Returns the exclusion patterns
	 * @return IPath[]
	 */
	@Override
	public IPath[] getExclusionPatterns() {
		return exclusionPatterns.length != 0 ? (IPath[])exclusionPatterns.clone() : exclusionPatterns;
	}

	/**
	 * Returns a char based representation of the exclusions patterns full path.
	 */
	@Override
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

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ACExclusionFilterEntry other = (ACExclusionFilterEntry) obj;
		if (!Arrays.equals(exclusionPatterns, other.exclusionPatterns))
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Arrays.hashCode(exclusionPatterns);
		return result;
	}

	@Override
	public boolean equalsByContents(ICSettingEntry entry) {
		if(!super.equalsByContents(entry))
			return false;

		ACExclusionFilterEntry otherEntry = (ACExclusionFilterEntry)entry;
		return Arrays.equals(exclusionPatterns, otherEntry.exclusionPatterns);
	}

	@Override
	protected String contentsToString() {
		String result = super.contentsToString();
		if(exclusionPatterns.length != 0){
			StringBuffer buf = new StringBuffer();
			buf.append(result);
			buf.append(" ; exclude: "); //$NON-NLS-1$
			for(int i = 0; i < exclusionPatterns.length; i++){
				if(i != 0)
					buf.append(", "); //$NON-NLS-1$
				buf.append(exclusionPatterns[i].toString());
			}
			result = buf.toString();
		}
		return result;
	}
}

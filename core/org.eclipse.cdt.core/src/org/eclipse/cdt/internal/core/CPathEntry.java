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
package org.eclipse.cdt.internal.core;

import org.eclipse.cdt.core.ICPathEntry;
import org.eclipse.core.runtime.IPath;

public class CPathEntry implements ICPathEntry {

	public int entryKind;

	public IPath path;

	public IPath[] exclusionPatterns;

	public IPath sourceAttachmentPath;

	public IPath sourceAttachmentRootPath;

	public IPath sourceAttachmentPrefixMapping;

	private char[][] fullCharExclusionPatterns;
	private final static char[][] UNINIT_PATTERNS = new char[][] { "Non-initialized yet".toCharArray()}; //$NON-NLS-1$

	/**
	 * Default exclusion pattern set
	 */
	public final static IPath[] NO_EXCLUSION_PATTERNS = {
	};

	public CPathEntry(
		int entryKind,
		IPath path,
		IPath[] exclusionPatterns,
		IPath sourceAttachmentPath,
		IPath sourceAttachmentRootPath,
		IPath sourceAttachmentPrefixMapping) {

		this.entryKind = entryKind;
		this.path = path;
		this.exclusionPatterns = exclusionPatterns;
		if (exclusionPatterns.length > 0) {
			this.fullCharExclusionPatterns = UNINIT_PATTERNS;
		}
		this.sourceAttachmentPath = sourceAttachmentPath;
		this.sourceAttachmentRootPath = sourceAttachmentRootPath;
		this.sourceAttachmentPrefixMapping = sourceAttachmentPrefixMapping;
	}

	/*
	 * Returns a char based representation of the exclusions patterns full path.
	 */
	public char[][] fullExclusionPatternChars() {

		if (this.fullCharExclusionPatterns == UNINIT_PATTERNS) {
			int length = this.exclusionPatterns.length;
			this.fullCharExclusionPatterns = new char[length][];
			IPath prefixPath = path.removeTrailingSeparator();
			for (int i = 0; i < length; i++) {
				this.fullCharExclusionPatterns[i] = prefixPath.append(this.exclusionPatterns[i]).toString().toCharArray();
			}
		}
		return this.fullCharExclusionPatterns;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.ICPathEntry#getEntryKind()
	 */
	public int getEntryKind() {
		return this.entryKind;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.ICPathEntry#getExclusionPatterns()
	 */
	public IPath[] getExclusionPatterns() {
		return this.exclusionPatterns;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.ICPathEntry#getPath()
	 */
	public IPath getPath() {
		return this.path;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.ICPathEntry#getSourceAttachmentPath()
	 */
	public IPath getSourceAttachmentPath() {
		return this.sourceAttachmentPath;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.ICPathEntry#getSourceAttachmentRootPath()
	 */
	public IPath getSourceAttachmentRootPath() {
		return this.sourceAttachmentRootPath;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.ICPathEntry#getSourceAttachmentPrefixMapping()
	 */
	public IPath getSourceAttachmentPrefixMapping() {
		return this.sourceAttachmentPrefixMapping;
	}

	/**
	 * Returns true if the given object is a classpath entry
	 * with equivalent attributes.
	 */
	public boolean equals(Object object) {
		if (this == object)
			return true;
		if (object instanceof ICPathEntry) {
			ICPathEntry otherEntry = (ICPathEntry) object;

			if (this.entryKind != otherEntry.getEntryKind())
				return false;

			if (!this.path.equals(otherEntry.getPath()))
				return false;

			IPath otherPath = otherEntry.getSourceAttachmentPath();
			if (this.sourceAttachmentPath == null) {
				if (otherPath != null)
					return false;
			} else {
				if (!this.sourceAttachmentPath.equals(otherPath))
					return false;
			}

			otherPath = otherEntry.getSourceAttachmentRootPath();
			if (this.sourceAttachmentRootPath == null) {
				if (otherPath != null)
					return false;
			} else {
				if (!this.sourceAttachmentRootPath.equals(otherPath))
					return false;
			}

			IPath[] otherExcludes = otherEntry.getExclusionPatterns();
			if (this.exclusionPatterns != otherExcludes) {
				int excludeLength = this.exclusionPatterns.length;
				if (otherExcludes.length != excludeLength)
					return false;
				for (int i = 0; i < excludeLength; i++) {
					// compare toStrings instead of IPaths 
					// since IPath.equals is specified to ignore trailing separators
					if (!this.exclusionPatterns[i].toString().equals(otherExcludes[i].toString()))
						return false;
				}
			}
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Returns the hash code for this classpath entry
	 */
	public int hashCode() {
		return this.path.hashCode();
	}
	/**
	 * Returns the kind of a <code>PackageFragmentRoot</code> from its <code>String</code> form.
	 */
	static int kindFromString(String kindStr) {

		if (kindStr.equalsIgnoreCase("prj")) //$NON-NLS-1$
			return ICPathEntry.CDT_PROJECT;
		if (kindStr.equalsIgnoreCase("var")) //$NON-NLS-1$
			return ICPathEntry.CDT_VARIABLE;
		if (kindStr.equalsIgnoreCase("src")) //$NON-NLS-1$
			return ICPathEntry.CDT_SOURCE;
		if (kindStr.equalsIgnoreCase("lib")) //$NON-NLS-1$
			return ICPathEntry.CDT_LIBRARY;
		if (kindStr.equalsIgnoreCase("inc")) //$NON-NLS-1$
			return ICPathEntry.CDT_INCLUDE;
		return -1;
	}

	/**
	 * Returns a <code>String</code> for the kind of a class path entry.
	 */
	static String kindToString(int kind) {

		switch (kind) {
			case ICPathEntry.CDT_PROJECT :
				return "prj";
			case ICPathEntry.CDT_SOURCE :
				return "src"; //$NON-NLS-1$
			case ICPathEntry.CDT_LIBRARY :
				return "lib"; //$NON-NLS-1$
			case ICPathEntry.CDT_VARIABLE :
				return "var"; //$NON-NLS-1$
			case ICPathEntry.CDT_INCLUDE :
				return "inc"; //$NON-NLS-1$
			default :
				return "unknown"; //$NON-NLS-1$
		}
	}
	/**
		 * Returns a printable representation of this classpath entry.
		 */
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(getPath().toString());
		buffer.append('[');
		switch (getEntryKind()) {
			case ICPathEntry.CDT_LIBRARY :
				buffer.append("CPE_LIBRARY"); //$NON-NLS-1$
				break;
			case ICPathEntry.CDT_PROJECT :
				buffer.append("CPE_PROJECT"); //$NON-NLS-1$
				break;
			case ICPathEntry.CDT_SOURCE :
				buffer.append("CPE_SOURCE"); //$NON-NLS-1$
				break;
			case ICPathEntry.CDT_VARIABLE :
				buffer.append("CPE_VARIABLE"); //$NON-NLS-1$
				break;
			case ICPathEntry.CDT_INCLUDE :
				buffer.append("CPE_INCLUDE"); //$NON-NLS-1$
				break;
		}
		buffer.append(']');
		if (getSourceAttachmentPath() != null) {
			buffer.append("[sourcePath:"); //$NON-NLS-1$
			buffer.append(getSourceAttachmentPath());
			buffer.append(']');
		}
		if (getSourceAttachmentRootPath() != null) {
			buffer.append("[rootPath:"); //$NON-NLS-1$
			buffer.append(getSourceAttachmentRootPath());
			buffer.append(']');
		}
		IPath[] patterns = getExclusionPatterns();
		int length;
		if ((length = patterns.length) > 0) {
			buffer.append("[excluding:"); //$NON-NLS-1$
			for (int i = 0; i < length; i++) {
				buffer.append(patterns[i]);
				if (i != length - 1) {
					buffer.append('|');
				}
			}
			buffer.append(']');
		}
		return buffer.toString();
	}
}

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

import org.eclipse.cdt.core.model.IIncludeEntry;
import org.eclipse.core.runtime.IPath;

public class IncludeEntry extends APathEntry implements IIncludeEntry {
	IPath includePath;
	boolean isSystemInclude;

	public IncludeEntry(IPath resourcePath, IPath includePath, IPath basePath, boolean isSystemInclude, IPath[] exclusionPatterns) {
		super(IIncludeEntry.CDT_INCLUDE, resourcePath, basePath, exclusionPatterns, resourcePath == null);
		this.includePath = includePath;
		this.isSystemInclude = isSystemInclude;
	}

	/**
	 * Returns the include path
	 * 
	 * @return IPath
	 */
	public IPath getIncludePath() {
		return includePath;
	}

	/**
	 * Whether or not it a system include path
	 * 
	 * @return boolean
	 */
	public boolean isSystemInclude() {
		return isSystemInclude;
	}

	public boolean equals(Object obj) {
		if (obj instanceof IIncludeEntry) {
			IIncludeEntry otherEntry = (IIncludeEntry) obj;
			if (!super.equals(otherEntry)) {
				return false;
			}
			if (path == null) {
				if (otherEntry.getPath() != null) {
					return false;
				}
			} else {
				if (!path.toString().equals(otherEntry.getPath().toString())) {
					return false;
				}
			}
			if (includePath == null) {
				if (otherEntry.getIncludePath() != null) {
					return false;
				}
			} else {
				if (!includePath.toString().equals(otherEntry.getIncludePath().toString())) {
					return false;
				}
			}
			if (isSystemInclude != otherEntry.isSystemInclude()) {
				return false;
			}
			return true;
		}
		return super.equals(obj);
	}
}

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

public class IncludeEntry extends ACPathEntry implements IIncludeEntry {

	IPath resourcePath;
	IPath includePath;
	boolean isSystemInclude;

	public IncludeEntry(IPath resourcePath, IPath includePath, boolean isSystemInclude,
		boolean isRecursive, IPath[] exclusionPatterns, boolean isExported) {
		super(IIncludeEntry.CDT_INCLUDE, isRecursive, exclusionPatterns, isExported);
		this.resourcePath = resourcePath;
		this.includePath = includePath;
		this.isSystemInclude = isSystemInclude;
	}

	/**
	 * Returns the affected resource by the include.
	 * @return IPath
	 */
	public IPath getResourcePath() {
		return resourcePath;
	}
 
	/**
	 * Returns the include path
	 * @return IPath
	 */
	public IPath getIncludePath() {
		return includePath;
	}
 
	/**
	 * Whether or not it a system include path
	 * @return boolean
	 */
	public boolean isSystemInclude() {
		return isSystemInclude;
	}

	public boolean equals(Object obj) {
		if (obj instanceof IIncludeEntry) {
			IIncludeEntry otherEntry = (IIncludeEntry)obj;
			if (!super.equals(otherEntry)) {
				return false;
			}
			if (resourcePath == null) {
				if (otherEntry.getResourcePath() != null) {
					return false;
				}
			} else {
				if (!resourcePath.toString().equals(otherEntry.getResourcePath().toString())) {
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

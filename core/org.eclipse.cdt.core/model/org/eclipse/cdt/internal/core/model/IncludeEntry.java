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
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

public class IncludeEntry extends APathEntry implements IIncludeEntry {
	IPath includePath;
	boolean isSystemInclude;

	public IncludeEntry(IPath resourcePath, IPath basePath, IPath baseRef, IPath includePath, boolean isSystemInclude,
			IPath[] exclusionPatterns, boolean isExported) {
		super(IIncludeEntry.CDT_INCLUDE, basePath, baseRef, resourcePath, exclusionPatterns, isExported);
		this.includePath = (includePath == null) ? Path.EMPTY : includePath;
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

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.IIncludeEntry#getFullIncludePath()
	 */
	public IPath getFullIncludePath() {
		IPath p = (!basePath.isEmpty()) ? basePath.append(includePath) : includePath;
		if (p.isAbsolute()) {
			return p;
		}
		IResource res = ResourcesPlugin.getWorkspace().getRoot().findMember(p);
		if (res != null) {
			IPath location = res.getLocation();
			if (location != null) {
				return location;
			}
		}
		return p;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(super.toString());
		sb.append(" isSystemInclude:").append(isSystemInclude); //$NON-NLS-1$
		sb.append(" includePath:").append(includePath); //$NON-NLS-1
		return sb.toString();
	}
}

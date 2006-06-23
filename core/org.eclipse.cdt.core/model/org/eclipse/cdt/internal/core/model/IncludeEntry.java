/*******************************************************************************
 * Copyright (c) 2000, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.model;

import org.eclipse.cdt.core.model.IIncludeEntry;
import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

public class IncludeEntry extends APathEntry implements IIncludeEntry {
	IPath includePath;
	boolean isSystemInclude;

	public IncludeEntry(IPath resourcePath, IPath basePath, IPath baseRef, IPath includePath, boolean isSystemInclude,
			IPath[] exclusionPatterns, boolean isExported) {
		super(IPathEntry.CDT_INCLUDE, basePath, baseRef, resourcePath, exclusionPatterns, isExported);
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
		IPath p;
		IPath inc = getIncludePath();
		if (!basePath.isEmpty()) {
			IPath loc = basePath;
			if (!loc.isAbsolute()) {
				IResource res = ResourcesPlugin.getWorkspace().getRoot().findMember(loc);
				if (res != null) {
					loc = res.getLocation();
				}
			}
			p = loc.append(inc);
			return p;
		}
		
		p = inc;

		if (!p.isAbsolute()) {
			IPath resPath = getPath();
			IResource res = ResourcesPlugin.getWorkspace().getRoot().findMember(resPath);
			if (res != null) {
				if (res.getType() == IResource.FILE) {
					res = res.getParent();
				}
				IPath location = res.getLocation();
				if (location != null) {
					p = location.append(p);
				}
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
		if (isSystemInclude) {
			sb.append(" isSystemInclude:").append(isSystemInclude); //$NON-NLS-1$
		}
		if (includePath != null && !includePath.isEmpty()) {
			sb.append(" includePath:").append(includePath); //$NON-NLS-1$
		}
		return sb.toString();
	}
}

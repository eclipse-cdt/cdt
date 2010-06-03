/*******************************************************************************
 * Copyright (c) 2000, 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Ed Swartz (Nokia)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.model;

import org.eclipse.cdt.core.model.IIncludeEntry;
import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.cdt.utils.PathUtil;
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
		this.includePath = (includePath == null) ? Path.EMPTY : PathUtil.getCanonicalPath(includePath);
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

@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((includePath == null) ? 0 : includePath.hashCode());
		result = prime * result + (isSystemInclude ? 1231 : 1237);
		return result;
	}

	@Override
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
		final IPath inc = getIncludePath();
		if (!basePath.isEmpty()) {
			IPath loc = basePath;
			if (!loc.isAbsolute()) {
				IResource res = ResourcesPlugin.getWorkspace().getRoot().findMember(loc.append(inc));
				if (res != null) {
					IPath location = res.getLocation();
					if (location != null) {
						return location;
					}
				}
			}
			return loc.append(inc);
		}
		
		if (!inc.isAbsolute()) {
			IPath resPath = getPath();
			IResource res = ResourcesPlugin.getWorkspace().getRoot().findMember(resPath.append(inc));
			if (res != null) {
				IPath location = res.getLocation();
				if (location != null) {
					return location;
				}
			}
		}
		return inc;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
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

/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.model;

import org.eclipse.cdt.core.model.IIncludeFileEntry;
import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

public class IncludeFileEntry extends APathEntry implements IIncludeFileEntry {

	IPath includeFilePath;

	public IncludeFileEntry(IPath resourcePath, IPath basePath, IPath baseRef, IPath includeFilePath,
			IPath[] exclusionPatterns, boolean isExported) {
		super(IPathEntry.CDT_INCLUDE_FILE, basePath, baseRef, resourcePath, exclusionPatterns, isExported);
		this.includeFilePath = (includeFilePath == null) ? Path.EMPTY : includeFilePath;
	}


	/**
	 * Returns the include path
	 * 
	 * @return IPath
	 */
	public IPath getIncludeFilePath() {
		return includeFilePath;
	}

	public boolean equals(Object obj) {
		if (obj instanceof IIncludeFileEntry) {
			IIncludeFileEntry otherEntry = (IIncludeFileEntry) obj;
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
			if (includeFilePath == null) {
				if (otherEntry.getIncludeFilePath() != null) {
					return false;
				}
			} else {
				if (!includeFilePath.toString().equals(otherEntry.getIncludeFilePath().toString())) {
					return false;
				}
			}
			return true;
		}
		return super.equals(obj);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.IIncludeEntry#getFullIncludePath()
	 */
	public IPath getFullIncludeFilePath() {
		IPath p;
		IPath inc = getIncludeFilePath();
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
		if (includeFilePath != null && !includeFilePath.isEmpty()) {
			sb.append(" includeFilePath:").append(includeFilePath); //$NON-NLS-1$
		}
		return sb.toString();
	}

}

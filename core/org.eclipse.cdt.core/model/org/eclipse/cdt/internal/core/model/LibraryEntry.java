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

import org.eclipse.cdt.core.model.ILibraryEntry;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

public class LibraryEntry extends APathEntry implements ILibraryEntry {

	IPath libraryPath;
	IPath sourceAttachmentPath;
	IPath sourceAttachmentRootPath;
	IPath sourceAttachmentPrefixMapping;

	/**
	 * 
	 * @param basePath
	 * @param baseRef
	 * @param libraryPath
	 * @param sourceAttachmentPath
	 * @param sourceAttachmentRootPath
	 * @param sourceAttachmentPrefixMapping
	 * @param isExported
	 */
	public LibraryEntry(IPath resourcePath, IPath basePath, IPath baseRef, IPath libraryPath, IPath sourceAttachmentPath,
		IPath sourceAttachmentRootPath, IPath sourceAttachmentPrefixMapping, boolean isExported) {
		super(ILibraryEntry.CDT_LIBRARY, basePath, baseRef, resourcePath, APathEntry.NO_EXCLUSION_PATTERNS, isExported);
		this.libraryPath = (libraryPath == null) ? Path.EMPTY : libraryPath;
		this.sourceAttachmentPath = sourceAttachmentPath;
		this.sourceAttachmentRootPath = sourceAttachmentRootPath;
		this.sourceAttachmentPrefixMapping = sourceAttachmentPrefixMapping;
	}

	/**
	 * Returns the path to the source archive or folder associated with this
	 * C path entry, or <code>null</code> if this C path entry has no
	 * source attachment.
	 * <p>
	 * Only library and variable C path entries may have source attachments.
	 * For library C path entries, the result path (if present) locates a source
	 * archive or folder. This archive or folder can be located in a project of the
	 * workspace or outside thr workspace. For variable c path entries, the
	 * result path (if present) has an analogous form and meaning as the
	 * variable path, namely the first segment is the name of a c path variable.
	 * </p>
	 *
	 * @return the path to the source archive or folder, or <code>null</code> if none
	 */
	public IPath getSourceAttachmentPath() {
		return sourceAttachmentPath;
	}

	/**
	 * Returns the path within the source archive or folder where source
	 * are located. An empty path indicates that packages are located at
	 * the root of the source archive or folder. Returns a non-<code>null</code> value
	 * if and only if <code>getSourceAttachmentPath</code> returns
	 * a non-<code>null</code> value.
	 *
	 * @return the path within the source archive or folder, or <code>null</code> if
	 *    not applicable
	 */
	public IPath getSourceAttachmentRootPath() {
		return sourceAttachmentRootPath;
	}
 
	/**
	 * Returns the path to map the source paths with to the source achive or folder
	 * An empty path indicates that the is a one-to-one mapping of source paths to the
	 * source achive or folder path. Returns a non-<code>null</code> value
	 * if and only if <code>getSourceAttachmentPath</code> returns
	 * a non-<code>null</code> value.
	 *
	 * @return the path mapping within the source archive or folder, or <code>null</code> if
	 *    not applicable
	 */
	public IPath getSourceAttachmentPrefixMapping() {
		return sourceAttachmentPrefixMapping;
	}


	public boolean equals(Object obj) {
		if (obj instanceof ILibraryEntry) {
			ILibraryEntry otherEntry = (ILibraryEntry)obj;
			if (!super.equals(obj)) {
				return false;
			}
			IPath otherPath = otherEntry.getLibraryPath();
			if (libraryPath == null) {
				if (otherPath != null) {
					return false;
				}
			} else {
				if (!libraryPath.equals(otherPath)) {
					return false;
				}
			}
			otherPath = otherEntry.getSourceAttachmentPath();
			if (sourceAttachmentPath == null) {
				if (otherPath != null) {
					return false;
				}
			} else {
				if (!sourceAttachmentPath.equals(otherPath)) {
					return false;
				}
			}
			otherPath = otherEntry.getSourceAttachmentRootPath();
			if (sourceAttachmentRootPath == null) {
				if (otherPath != null) {
					return false;
				}
			} else {
				if (!sourceAttachmentRootPath.equals(otherPath)) {
					return false;
				}
			}
			return true;
		}
		return super.equals(obj);
	}

	public IPath getFullLibraryPath() {
		IPath p;
		IPath lib = getLibraryPath();
		if (!basePath.isEmpty()) {
			IPath loc = basePath;
			if (!loc.isAbsolute()) {
				IResource res = ResourcesPlugin.getWorkspace().getRoot().findMember(loc);
				if (res != null) {
					loc = res.getLocation();
				}
			}
			p = loc.append(lib);
			return p;
		} else {
			p = lib;
		}
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
	 * @see org.eclipse.cdt.core.model.ILibraryEntry#getLibraryPath()
	 */
	public IPath getLibraryPath() {
		return libraryPath;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(super.toString());
		if (libraryPath != null && !libraryPath.isEmpty()) {
			sb.append(" librarypath:").append(libraryPath.toString()); //$NON-NLS-1$
		}
		return sb.toString();
	}
}

/*******************************************************************************
 * Copyright (c) 2007, 2012 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.settings.model;

import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * Representation in the project model of library file settings entries.
 * As an example, those are supplied by a gcc compiler with option "-l".
 */
public final class CLibraryFileEntry extends ACPathEntry implements ICLibraryFileEntry {
	private final IPath fSourceAttachmentPath;
	private final IPath fSourceAttachmentRootPath;
	private final IPath fSourceAttachmentPrefixMapping;

	/**
	 * This constructor is discouraged to be referenced by clients.
	 *
	 * Instead, use pooled entries with CDataUtil.createCLibraryFileEntry(name, flags).
	 *
	 * @param name - library file path. The path can be an absolute location on the local file-system
	 *    or with flag {@link #VALUE_WORKSPACE_PATH} it is treated as workspace full path.
	 * @param flags - bitwise combination of {@link ICSettingEntry} flags.
	 */
	public CLibraryFileEntry(String name, int flags) {
		this(name, flags, null, null, null);
	}

	/**
	 * This constructor is discouraged to be used directly.
	 *
	 * Instead, use pooled entries with CDataUtil.createCLibraryFileEntry(location.toString(), flags)
	 * or wrap it with CDataUtil.getPooledEntry(new CLibraryFileEntry(location, flags)).
	 *
	 * @param location - library file path. The path can be an absolute location on the local
	 *    file-system or with flag {@link #VALUE_WORKSPACE_PATH} it is treated as workspace full path.
	 * @param flags - bitwise combination of {@link ICSettingEntry} flags.
	 */
	public CLibraryFileEntry(IPath location, int flags) {
		this(location, flags, null, null, null);
	}

	/**
	 * This constructor is discouraged to be used directly.
	 *
	 * Instead, use pooled entries wrapping with CDataUtil.getPooledEntry(new CLibraryFileEntry(rc, flags)).
	 *
	 * @param rc - library file as a resource in the workspace.
	 * @param flags - bitwise combination of {@link ICSettingEntry} flags.
	 *    If {@link #VALUE_WORKSPACE_PATH} is missing it will be supplied.
	 */
	public CLibraryFileEntry(IFile rc, int flags) {
		this(rc, flags, null, null, null);
	}

	public CLibraryFileEntry(String name, int flags,
			IPath sourceAttachmentPath,
			IPath sourceAttachmentRootPath,
			IPath sourceAttachmentPrefixMapping) {
		super(name, flags);

		fSourceAttachmentPath = sourceAttachmentPath;
		fSourceAttachmentRootPath = sourceAttachmentRootPath != null ? sourceAttachmentRootPath : Path.EMPTY;
		fSourceAttachmentPrefixMapping = sourceAttachmentPrefixMapping != null ? sourceAttachmentPrefixMapping : Path.EMPTY;
	}

	public CLibraryFileEntry(IPath location, int flags,
			IPath sourceAttachmentPath,
			IPath sourceAttachmentRootPath,
			IPath sourceAttachmentPrefixMapping) {
		super(location, flags);

		fSourceAttachmentPath = sourceAttachmentPath;
		fSourceAttachmentRootPath = sourceAttachmentRootPath != null ? sourceAttachmentRootPath : Path.EMPTY;
		fSourceAttachmentPrefixMapping = sourceAttachmentPrefixMapping != null ? sourceAttachmentPrefixMapping : Path.EMPTY;
	}

	public CLibraryFileEntry(IFile rc, int flags,
			IPath sourceAttachmentPath,
			IPath sourceAttachmentRootPath,
			IPath sourceAttachmentPrefixMapping) {
		super(rc, flags);

		fSourceAttachmentPath = sourceAttachmentPath;
		fSourceAttachmentRootPath = sourceAttachmentRootPath != null ? sourceAttachmentRootPath : Path.EMPTY;
		fSourceAttachmentPrefixMapping = sourceAttachmentPrefixMapping != null ? sourceAttachmentPrefixMapping : Path.EMPTY;
	}

	@Override
	public final int getKind() {
		return LIBRARY_FILE;
	}

	@Override
	public final boolean isFile() {
		return true;
	}

	@Override
	public IPath getSourceAttachmentPath() {
		return fSourceAttachmentPath;
	}

	@Override
	public IPath getSourceAttachmentPrefixMapping() {
		return fSourceAttachmentPrefixMapping;
	}

	@Override
	public IPath getSourceAttachmentRootPath() {
		return fSourceAttachmentRootPath;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((fSourceAttachmentPath == null) ? 0 : fSourceAttachmentPath.hashCode());
		result = prime * result + ((fSourceAttachmentPrefixMapping == null) ? 0 : fSourceAttachmentPrefixMapping.hashCode());
		result = prime * result + ((fSourceAttachmentRootPath == null) ? 0 : fSourceAttachmentRootPath.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object other) {
		if(other == this)
			return true;

		if(!super.equals(other))
			return false;

		return sourceAttachmentSettingsEqual((CLibraryFileEntry)other);
	}

	@Override
	public boolean equalsByContents(ICSettingEntry entry) {
		if(entry == this)
			return true;

		if(!super.equalsByContents(entry))
			return false;

		return sourceAttachmentSettingsEqual((CLibraryFileEntry)entry);
	}

	private boolean sourceAttachmentSettingsEqual(CLibraryFileEntry otherEntry){
		if(!CDataUtil.objectsEqual(fSourceAttachmentPath, otherEntry.fSourceAttachmentPath))
			return false;
		if(!CDataUtil.objectsEqual(fSourceAttachmentRootPath, otherEntry.fSourceAttachmentRootPath))
			return false;
		if(!CDataUtil.objectsEqual(fSourceAttachmentPrefixMapping, otherEntry.fSourceAttachmentPrefixMapping))
			return false;
		return true;
	}

	@Override
	protected String contentsToString() {
		String result = super.contentsToString();

		if(fSourceAttachmentPath != null){
			StringBuffer buf = new StringBuffer();
			buf.append(result);
			buf.append(" ; srcPath=").append(fSourceAttachmentPath); //$NON-NLS-1$
			buf.append("; srcRoot=").append(fSourceAttachmentRootPath); //$NON-NLS-1$
			buf.append("; srcMapping=").append(fSourceAttachmentPrefixMapping); //$NON-NLS-1$

			result = buf.toString();
		}
		return result;
	}
}

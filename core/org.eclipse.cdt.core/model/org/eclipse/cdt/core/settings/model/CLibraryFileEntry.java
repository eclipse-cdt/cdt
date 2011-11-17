/*******************************************************************************
 * Copyright (c) 2007, 2009 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.settings.model;

import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

public final class CLibraryFileEntry extends ACPathEntry implements
		ICLibraryFileEntry {
	private IPath fSourceAttachmentPath;
	private IPath fSourceAttachmentRootPath;
	private IPath fSourceAttachmentPrefixMapping;

	public CLibraryFileEntry(String value, int flags) {
		this(value, flags, null, null, null);
	}

	public CLibraryFileEntry(IPath location, int flags) {
		this(location, flags, null, null, null);
	}

	public CLibraryFileEntry(IFile rc, int flags) {
		this(rc, flags, null, null, null);
	}

	public CLibraryFileEntry(String value,
			int flags,
			IPath sourceAttachmentPath,
			IPath sourceAttachmentRootPath,
			IPath sourceAttachmentPrefixMapping) {
		super(value, flags);
		setSourceAttachmentSettings(sourceAttachmentPath, sourceAttachmentRootPath, sourceAttachmentPrefixMapping);
	}

	public CLibraryFileEntry(IPath location,
			int flags,
			IPath sourceAttachmentPath,
			IPath sourceAttachmentRootPath,
			IPath sourceAttachmentPrefixMapping) {
		super(location, flags);
		setSourceAttachmentSettings(sourceAttachmentPath, sourceAttachmentRootPath, sourceAttachmentPrefixMapping);
	}

	public CLibraryFileEntry(IFile rc,
			int flags,
			IPath sourceAttachmentPath,
			IPath sourceAttachmentRootPath,
			IPath sourceAttachmentPrefixMapping) {
		super(rc, flags);
		setSourceAttachmentSettings(sourceAttachmentPath, sourceAttachmentRootPath, sourceAttachmentPrefixMapping);
	}

	private void setSourceAttachmentSettings(IPath sourceAttachmentPath,
			IPath sourceAttachmentRootPath,
			IPath sourceAttachmentPrefixMapping){
		if(sourceAttachmentPath == null)
			return;

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
		result = prime * result
				+ ((fSourceAttachmentPrefixMapping == null) ? 0 : fSourceAttachmentPrefixMapping.hashCode());
		result = prime * result
				+ ((fSourceAttachmentRootPath == null) ? 0 : fSourceAttachmentRootPath.hashCode());
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

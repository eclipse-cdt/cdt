package org.eclipse.cdt.internal.core.model;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */
 
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.resources.IFile;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IArchive;
import org.eclipse.cdt.core.model.IBinary;

public class Archive extends CFile implements IArchive {

	public Archive(ICElement parent, IFile file) {
		super(parent, file);
	}

	public Archive(ICElement parent, IPath path) {
		super (parent, path);
	}

	public boolean isReadOnly() {
		return true;
	}

	public boolean isArchive() {
		return true;
	}

	public IBinary[] getBinaries() {
		return (IBinary[])getChildren();
	}

	public CElementInfo createElementInfo() {
		return new ArchiveInfo(this);
	}

	protected ArchiveInfo getArchiveInfo() {
		return (ArchiveInfo)getElementInfo();
	}
}

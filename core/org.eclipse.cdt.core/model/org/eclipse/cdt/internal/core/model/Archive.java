package org.eclipse.cdt.internal.core.model;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */
 
import org.eclipse.cdt.core.model.IArchive;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;

public class Archive extends CFile implements IArchive {

	IResource archive;

	public Archive(ICElement parent, IFile file) {
		super(parent, file);
	}

	public Archive(ICElement parent, IPath path) {
		super (parent, path);
	}

	public IBinary[] getBinaries() {
		ICElement[] e = getChildren();
		IBinary[] b = new IBinary[e.length];
		System.arraycopy(e, 0, b, 0, e.length);
		return b;
	}

	public CElementInfo createElementInfo() {
		return new ArchiveInfo(this);
	}

	protected ArchiveInfo getArchiveInfo() {
		return (ArchiveInfo)getElementInfo();
	}
}

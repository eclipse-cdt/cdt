package org.eclipse.cdt.internal.core.model;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */
 
import java.util.Map;

import org.eclipse.cdt.core.IBinaryParser.IBinaryArchive;
import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.IArchive;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

public class Archive extends Openable implements IArchive {

	IBinaryArchive binaryArchive;

	public Archive(ICElement parent, IFile file, IBinaryArchive ar) {
		this(parent, file.getLocation(), ar);
	}

	public Archive(ICElement parent, IPath path, IBinaryArchive ar) {
		super (parent, path, ICElement.C_ARCHIVE);
		binaryArchive = ar;
	}

	public IBinary[] getBinaries() {
		ICElement[] e = getChildren();
		IBinary[] b = new IBinary[e.length];
		System.arraycopy(e, 0, b, 0, e.length);
		return b;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ICElement#isReadOnly()
	 */
	public boolean isReadOnly() {
		return true;
	}

	public CElementInfo createElementInfo() {
		return new ArchiveInfo(this);
	}

	protected ArchiveInfo getArchiveInfo() {
		return (ArchiveInfo)getElementInfo();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.model.Openable#generateInfos(org.eclipse.cdt.internal.core.model.OpenableInfo, org.eclipse.core.runtime.IProgressMonitor, java.util.Map, org.eclipse.core.resources.IResource)
	 */
	protected boolean generateInfos(OpenableInfo info, IProgressMonitor pm, Map newElements, IResource underlyingResource)
		throws CModelException {
		CModelManager.getDefault().putInfo(this, info);
		return computeChildren(info, underlyingResource);
	}


	public boolean computeChildren(OpenableInfo info, IResource res) {
		IBinaryArchive ar = getBinaryArchive();
		if (ar != null) {
			IBinaryObject[] objects = ar.getObjects();
			for (int i = 0; i < objects.length; i++) {
				final IBinaryObject obj = objects[i];
				Binary binary = new Binary(this, ar.getPath().append(obj.getName()), obj);
				info.addChild(binary);
			}
		} else {
			return false;
		}
		return true;
	}

	IBinaryArchive getBinaryArchive() {
		return binaryArchive;
	}

}

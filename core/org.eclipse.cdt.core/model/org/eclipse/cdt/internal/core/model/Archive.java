package org.eclipse.cdt.internal.core.model;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */
 
import java.io.IOException;
import java.util.Map;

import org.eclipse.cdt.core.IBinaryParser;
import org.eclipse.cdt.core.IBinaryParser.IBinaryArchive;
import org.eclipse.cdt.core.IBinaryParser.IBinaryFile;
import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.IArchive;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

public class Archive extends Openable implements IArchive {

	public Archive(ICElement parent, IFile file) {
		this(parent, file.getLocation());
	}

	public Archive(ICElement parent, IPath path) {
		super (parent, path, ICElement.C_ARCHIVE);
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
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.model.Openable#generateInfos(org.eclipse.cdt.internal.core.model.OpenableInfo, org.eclipse.core.runtime.IProgressMonitor, java.util.Map, org.eclipse.core.resources.IResource)
	 */
	protected boolean generateInfos(OpenableInfo info, IProgressMonitor pm, Map newElements, IResource underlyingResource)
		throws CModelException {
		CModelManager.getDefault().putInfo(this, info);
		return computeChildren(info, underlyingResource);
	}


	public boolean computeChildren(OpenableInfo info, IResource res) {
		IBinaryArchive ar = getBinaryArchive(res);
		if (ar != null) {
			IBinaryObject[] objects = ar.getObjects();
			for (int i = 0; i < objects.length; i++) {
				final IBinaryObject obj = objects[i];
				Binary binary = new Binary(this, res.getLocation().append(obj.getName()), obj);
				info.addChild(binary);
			}
		} else {
			return false;
		}
		return true;
	}

	IBinaryArchive getBinaryArchive(IResource res) {
		IBinaryArchive archive = null;
		IProject project = null;
		IBinaryParser parser = null;
		if (res != null) {
			project = res.getProject();
		}
		if (project != null) {
			parser = CModelManager.getDefault().getBinaryParser(project);
		}
		if (parser != null) {
			try {
				IPath path = res.getLocation();
				IBinaryFile bfile = parser.getBinary(path);
				if (bfile instanceof IBinaryArchive) {
					archive = (IBinaryArchive) bfile;
				}
			} catch (IOException e) {
			}
		}
		return archive;
	}

}

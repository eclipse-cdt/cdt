package org.eclipse.cdt.internal.core.model;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */
 
import java.util.Map;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.IArchive;
import org.eclipse.cdt.core.model.IArchiveContainer;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;

public class ArchiveContainer extends Openable implements IArchiveContainer {

	public ArchiveContainer (CProject cProject) {
		super (cProject, null, CCorePlugin.getResourceString("CoreModel.ArchiveContainer.Archives"), CElement.C_VCONTAINER); //$NON-NLS-1$
	}

	public IArchive[] getArchives() throws CModelException {
		((ArchiveContainerInfo)getElementInfo()).sync();
		ICElement[] e = getChildren();
		IArchive[] a = new IArchive[e.length];
		System.arraycopy(e, 0, a, 0, e.length);
		return a;
	}

	public CElementInfo createElementInfo() {
		return new ArchiveContainerInfo(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.model.Openable#buildStructure(org.eclipse.cdt.internal.core.model.OpenableInfo, org.eclipse.core.runtime.IProgressMonitor, java.util.Map, org.eclipse.core.resources.IResource)
	 */
	protected boolean buildStructure(OpenableInfo info, IProgressMonitor pm, Map newElements, IResource underlyingResource)
		throws CModelException {
		// this will bootstrap/start the runner for the project.
		CModelManager.getDefault().getBinaryRunner(getCProject());
		return true;
	}

}

package org.eclipse.cdt.internal.core.model;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */
 
import java.util.ArrayList;
import java.util.Map;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.IBinaryContainer;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;

public class BinaryContainer extends Openable implements IBinaryContainer {

	public BinaryContainer (CProject cProject) {
		super (cProject, null, CCorePlugin.getResourceString("CoreModel.BinaryContainer.Binaries"), CElement.C_VCONTAINER); //$NON-NLS-1$
	}

	public IBinary[] getBinaries() throws CModelException {
		((BinaryContainerInfo)getElementInfo()).sync();
		ICElement[] e = getChildren();
		ArrayList list = new ArrayList(e.length);
		for (int i = 0; i < e.length; i++) {
			if (e[i] instanceof IBinary) {
				IBinary bin = (IBinary)e[i];
				if (bin.isExecutable() || bin.isSharedLib()) {
					list.add(bin);
				}
			}
		}
		IBinary[] b = new IBinary[list.size()];
		list.toArray(b);
		return b;
	}

	public CElementInfo createElementInfo() {
		return new BinaryContainerInfo(this);
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

package org.eclipse.cdt.internal.core.model;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */
 
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IBinaryContainer;
import org.eclipse.cdt.core.model.IArchiveContainer;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICModelStatusConstants;

public class CProject extends CResource implements ICProject {

	boolean elfDone = false;

	public CProject (ICElement parent, IProject project) {
		super (parent, project, CElement.C_PROJECT);
	}

	public IBinaryContainer getBinaryContainer() {
		return getCProjectInfo().getBinaryContainer();
	}

	public IArchiveContainer getArchiveContainer() {
		return getCProjectInfo().getArchiveContainer();
	}

	public IProject getProject() {
		try {
			return getUnderlyingResource().getProject();
		} catch (CModelException e) {
			e.printStackTrace();
		}
		return null;
	}

	public ICProject getCProject() {
		return this;
	}

	public ICElement findElement(IPath path) throws CModelException {
		ICElement celem =  CModelManager.getDefault().create(path);
		if (celem == null)
			new CModelStatus(ICModelStatusConstants.INVALID_PATH, path);
		return celem;
	}

	synchronized protected boolean hasRunElf() {
		return elfDone;
	}

	synchronized protected void setRunElf(boolean done) {
		elfDone = done;
	}

	protected CProjectInfo getCProjectInfo() {
		return (CProjectInfo)getElementInfo();
	}

	protected CElementInfo createElementInfo() {
		return new CProjectInfo(this);
	}
}

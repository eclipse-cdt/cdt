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

	boolean runner = false;

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
		ICElement celem = null;
		if (path.isAbsolute()) {
			celem =  CModelManager.getDefault().create(path);
		} else {
			IProject project = getProject();
			if (project !=  null) {
				IPath p = project.getFullPath().append(path);
				celem = CModelManager.getDefault().create(p);
			}
		}
		if (celem == null) {
			CModelStatus status = new CModelStatus(ICModelStatusConstants.INVALID_PATH, path);
			throw new CModelException(status);
		}
		return celem;
	}

	synchronized protected boolean hasStartBinaryRunner() {
		return runner;
	}

	synchronized protected void setStartBinaryRunner(boolean done) {
		runner = done;
	}

	protected CProjectInfo getCProjectInfo() {
		return (CProjectInfo)getElementInfo();
	}

	protected CElementInfo createElementInfo() {
		return new CProjectInfo(this);
	}
	
	// CHECKPOINT: CProjects will return the hash code of their underlying IProject
	public int hashCode() {
		return getProject().hashCode();
	}

}

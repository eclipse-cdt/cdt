package org.eclipse.cdt.internal.core.model;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */
 
import java.util.ArrayList;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.core.ICDescriptor;
import org.eclipse.cdt.core.ICPathEntry;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.IArchiveContainer;
import org.eclipse.cdt.core.model.IBinaryContainer;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICModelStatusConstants;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ILibraryReference;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

public class CProject extends CContainer implements ICProject {

	public CProject (ICElement parent, IProject project) {
		super (parent, project, CElement.C_PROJECT);
	}

	public IBinaryContainer getBinaryContainer() {
		return ((CProjectInfo)getElementInfo()).getBinaryContainer();
	}

	public IArchiveContainer getArchiveContainer() {
		return ((CProjectInfo)getElementInfo()).getArchiveContainer();
	}

	public IProject getProject() {
		return getUnderlyingResource().getProject();
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

	public static boolean hasCNature (IProject p) {
		try {
			return p.hasNature(CProjectNature.C_NATURE_ID);
		} catch (CoreException e) {
			//throws exception if the project is not open.
		}
		return false;
	}

	protected CElementInfo createElementInfo() {
		return new CProjectInfo(this);
	}
	
	// CHECKPOINT: CProjects will return the hash code of their underlying IProject
	public int hashCode() {
		return getProject().hashCode();
	}

	public ILibraryReference[] getLibraryReferences() throws CModelException {
		ArrayList list = new ArrayList(5);
		try {
			ICDescriptor cdesc = CCorePlugin.getDefault().getCProjectDescription(getProject());
			ICPathEntry[] entries = cdesc.getPathEntries();
			for (int i = 0; i < entries.length; i++) {
				if (entries[i].getEntryKind() == ICPathEntry.CDT_LIBRARY) {
					ICPathEntry entry = entries[i];
					list.add(new LibraryReference(this, entry.getPath().lastSegment(),entry));
				}
			}
		} catch (CoreException e) {
			throw new CModelException(e);
		}
		return (ILibraryReference[])list.toArray(new ILibraryReference[0]);
	}
}

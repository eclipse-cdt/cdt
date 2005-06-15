/*******************************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.browser;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;


public class TypeReference implements ITypeReference {
	private IPath fPath;
	private IProject fProject;
	private IResource fResource;
	private IWorkingCopy fWorkingCopy;
	private int fOffset;
	private int fLength;
    public boolean offsetIsLineNumber = false;
	
	public TypeReference(IPath path, IProject project, int offset, int length) {
		fPath = path;
		fProject = project;
		fWorkingCopy = null;
		fResource = null;
		fOffset = offset;
		fLength = length;
	}

	public TypeReference(IResource resource, IProject project, int offset, int length) {
		fPath = null;
		fProject = project;
		fWorkingCopy = null;
		fResource = resource;
		fOffset = offset;
		fLength = length;
	}

	public TypeReference(IWorkingCopy workingCopy, IProject project, int offset, int length) {
		fPath = null;
		fProject = project;
		fWorkingCopy = workingCopy;
		fResource = null;
		fOffset = offset;
		fLength = length;
	}

	public TypeReference(IPath path, IProject project) {
		this(path, project, 0, 0);
	}

	public TypeReference(IResource resource, IProject project) {
		this(resource, project, 0, 0);
	}

	public TypeReference(IWorkingCopy workingCopy, IProject project) {
		this(workingCopy, project, 0, 0);
	}

	public IPath getPath() {
		if (fWorkingCopy != null) {
			return fWorkingCopy.getPath();
		} else if (fResource != null) {
			return fResource.getFullPath();
		} else {
			return fPath;
		}
	}
	
	public IPath getLocation() {
		if (fWorkingCopy != null) {
			IResource resource = fWorkingCopy.getUnderlyingResource();
			if (resource != null) {
				return resource.getLocation();
			}
			return null;
		} else if (fResource != null) {
			return fResource.getLocation();
		} else if (fPath != null) {
			return fPath;
		} else if (fProject != null) {
			return fProject.getLocation();
		} else {
			return null;
		}
	}
	
	public IResource getResource() {
		return fResource;
	}

	public IWorkingCopy getWorkingCopy() {
		return fWorkingCopy;
	}
	
	public IProject getProject() {
		if (fProject != null) {
			return fProject;
		}
		if (fWorkingCopy != null) {
			ICProject cProject = fWorkingCopy.getCProject();
			if (cProject != null) {
				return cProject.getProject();
			}
			return null;
		} else if (fResource != null) {
			return fResource.getProject();
		} else {
			return null;
		}
	}
	
	public ITranslationUnit getTranslationUnit() {
		ITranslationUnit unit = null;
		if (fWorkingCopy != null) {
			unit = fWorkingCopy.getTranslationUnit();
		} else if (fResource != null) {
			ICElement elem = CoreModel.getDefault().create(fResource);
			if (elem instanceof ITranslationUnit)
				unit = (ITranslationUnit) elem;
		} else {
			IPath path = getLocation();
			ICElement elem = CoreModel.getDefault().create(path);
			if (elem instanceof ITranslationUnit)
				unit = (ITranslationUnit) elem;
		}
		
		if (unit == null) {
			IProject project = getProject();
			if (project != null) {
				ICProject cProject = findCProject(project);
				if (cProject != null) {
					IPath path = getLocation();
					ICElement elem = CoreModel.getDefault().createTranslationUnitFrom(cProject, path);
					if (elem instanceof ITranslationUnit)
						unit = (ITranslationUnit) elem;
				}
			}
		}
		return unit;
	}
	
	private ICProject findCProject(IProject project) {
		try {
			ICProject[] cProjects = CoreModel.getDefault().getCModel().getCProjects();
			if (cProjects != null) {
				for (int i = 0; i < cProjects.length; ++i) {
					ICProject cProject = cProjects[i];
					if (project.equals(cProjects[i].getProject()))
						return cProject;
				}
			}
		} catch (CModelException e) {
		}
		return null;
	}

	public ICElement[] getCElements() {
		ITranslationUnit unit = getTranslationUnit();
		if (unit != null) {
			try {
                if( offsetIsLineNumber )
                {
                    ICElement [] result = new ICElement[1];
                    result[0] = unit.getElementAtLine(fOffset);
                    return result;
                }
                return unit.getElementsAtOffset(fOffset);
			} catch (CModelException e) {
			}
		}
		return null;
	}
	
	public int getOffset() {
		return fOffset;
	}
	
	public int getLength() {
		return fLength;
	}
	
	public IPath getRelativeIncludePath(IProject project) {
		IPath path = getLocation();
		if (path != null) {
		    IPath relativePath = PathUtil.makeRelativePathToProjectIncludes(path, project);
		    if (relativePath != null)
		        return relativePath;
		}
		return path;
	}
	
	public IPath getRelativePath(IPath relativeToPath) {
		IPath path = getPath();
		if (path != null) {
		    IPath relativePath = PathUtil.makeRelativePath(path, relativeToPath);
		    if (relativePath != null)
		        return relativePath;
		}
		return path;
	}
	
	public String toString() {
		IPath path = getLocation();
		if (path != null) {
			if (fLength == 0 && fOffset == 0) {
				return path.toString();
			}
			return path.toString() + ":" + fOffset + "-" + (fOffset + fLength);  //$NON-NLS-1$//$NON-NLS-2$
		}
		return ""; //$NON-NLS-1$
	}

	public int hashCode() {
		return toString().hashCode();
	}

	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof ITypeReference)) {
			return false;
		}
		ITypeReference ref = (ITypeReference)obj;
		return toString().equals(ref.toString());
	}

    public boolean isLineNumber() {
        return offsetIsLineNumber;
    }
}

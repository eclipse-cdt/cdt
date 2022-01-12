/*******************************************************************************
 * Copyright (c) 2004, 2012 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.browser;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.CoreModelUtil;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.utils.PathUtil;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;

/**
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
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

	@Override
	public IPath getPath() {
		if (fWorkingCopy != null) {
			return fWorkingCopy.getPath();
		} else if (fResource != null) {
			return fResource.getFullPath();
		} else {
			return fPath;
		}
	}

	@Override
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

	@Override
	public IResource getResource() {
		return fResource;
	}

	@Override
	public IWorkingCopy getWorkingCopy() {
		return fWorkingCopy;
	}

	@Override
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

	@Override
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
			else {
				try {
					unit = CoreModelUtil.findTranslationUnitForLocation(path, findCProject(getProject()));
				} catch (CModelException e) {
					CCorePlugin.log(e);
				}
			}
		}

		return unit;
	}

	private ICProject findCProject(IProject project) {
		if (project == null) {
			return null;
		}
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

	@Override
	public ICElement[] getCElements() {
		ITranslationUnit unit = getTranslationUnit();
		if (unit != null) {
			try {
				if (offsetIsLineNumber) {
					ICElement[] result = new ICElement[1];
					result[0] = unit.getElementAtLine(fOffset);
					return result;
				}
				return unit.getElementsAtOffset(fOffset);
			} catch (CModelException e) {
			}
		}
		return null;
	}

	@Override
	public int getOffset() {
		return fOffset;
	}

	@Override
	public int getLength() {
		return fLength;
	}

	@Override
	public IPath getRelativeIncludePath(IProject project) {
		IPath path = getLocation();
		if (path != null) {
			IPath relativePath = PathUtil.makeRelativePathToProjectIncludes(path, project);
			if (relativePath != null)
				return relativePath;
		}
		return path;
	}

	@Override
	public IPath getRelativePath(IPath relativeToPath) {
		IPath path = getPath();
		if (path != null) {
			IPath relativePath = PathUtil.makeRelativePath(path, relativeToPath);
			if (relativePath != null)
				return relativePath;
		}
		return path;
	}

	@Override
	public String toString() {
		IPath path = getLocation();
		if (path != null) {
			if (fLength == 0 && fOffset == 0) {
				return path.toString();
			}
			return path.toString() + ":" + fOffset + "-" + (fOffset + fLength); //$NON-NLS-1$//$NON-NLS-2$
		}
		return ""; //$NON-NLS-1$
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof ITypeReference)) {
			return false;
		}
		ITypeReference ref = (ITypeReference) obj;
		return toString().equals(ref.toString());
	}

	@Override
	public boolean isLineNumber() {
		return offsetIsLineNumber;
	}
}

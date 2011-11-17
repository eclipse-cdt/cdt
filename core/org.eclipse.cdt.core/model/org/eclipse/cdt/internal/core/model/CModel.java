/*******************************************************************************
 * Copyright (c) 2000, 2008 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.model;

import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.core.util.MementoTokenizer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;

public class CModel extends Openable implements ICModel {

	public CModel () {
		this(ResourcesPlugin.getWorkspace().getRoot());
	}

	public CModel(IWorkspaceRoot root) {
		super (null, root, ICElement.C_MODEL);
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof CModel)) {
			return false;
		}
		return super.equals(o);
	}

	@Override
	public ICProject[] getCProjects() throws CModelException {
		List<?> list = getChildrenOfType(C_PROJECT);
		ICProject[] array= new ICProject[list.size()];
		list.toArray(array);
		return array;
	}

	/**
	 * ICModel#getCProject(String)
	 */
	@Override
	public ICProject getCProject(String name) {
		IProject project = ((IWorkspaceRoot) getResource()).getProject(name);
		return CModelManager.getDefault().create(project);
	}

	/**
	 * Returns the active C project associated with the specified
	 * resource, or <code>null</code> if no C project yet exists
	 * for the resource.
	 *
	 * @exception IllegalArgumentException if the given resource
	 * is not one of an IProject, IFolder, or IFile.
	 */
	public ICProject getCProject(IResource resource) {
		switch (resource.getType()) {
		case IResource.FOLDER:
			return new CProject(this, ((IFolder) resource).getProject());
		case IResource.FILE:
			return new CProject(this, ((IFile) resource).getProject());
		case IResource.PROJECT:
			return new CProject(this, (IProject) resource);
		default:
			throw new IllegalArgumentException("element.invalidResourceForProject"); //$NON-NLS-1$
		}
	}

	/**
	 * Finds the given project in the list of the java model's children.
	 * Returns null if not found.
	 */
	public ICProject findCProject(IProject project) {
		try {
			ICProject[] projects = getOldCProjectsList();
			for (ICProject cProject : projects) {
				if (project.equals(cProject.getProject())) {
					return cProject;
				}
			}
		} catch (CModelException e) {
			// c model doesn't exist: cannot find any project
		}
		return null;
	}

	@Override
	public IWorkspace getWorkspace() {
		return getUnderlyingResource().getWorkspace();
	}

	@Override
	public void copy(ICElement[] elements, ICElement[] containers, ICElement[] siblings,
		String[] renamings, boolean replace, IProgressMonitor monitor) throws CModelException {
		if (elements != null && elements[0] != null && elements[0].getElementType() <= ICElement.C_UNIT ) {
			runOperation(new CopyResourceElementsOperation(elements, containers, replace), elements,
					siblings, renamings, monitor);
		} else {
			runOperation(new CopyElementsOperation(elements, containers, replace), elements, siblings,
					renamings, monitor);
		}
	}

	@Override
	public void delete(ICElement[] elements, boolean force, IProgressMonitor monitor)
		throws CModelException {
		CModelOperation op;
		if (elements != null && elements[0] != null && elements[0].getElementType() <= ICElement.C_UNIT) {
			op = new DeleteResourceElementsOperation(elements, force);
		} else {
			op = new DeleteElementsOperation(elements, force);
		}
		op.runOperation(monitor);
	}

	@Override
	public void move(ICElement[] elements, ICElement[] containers, ICElement[] siblings,
		String[] renamings, boolean replace, IProgressMonitor monitor) throws CModelException {
		if (elements != null && elements[0] != null && elements[0].getElementType() <= ICElement.C_UNIT) {
			runOperation(new MoveResourceElementsOperation(elements, containers, replace), elements,
					siblings, renamings, monitor);
		} else {
			runOperation(new MoveElementsOperation(elements, containers, replace), elements, siblings,
					renamings, monitor);
		}
	}

	@Override
	public void rename(ICElement[] elements, ICElement[] destinations, String[] renamings,
		boolean force, IProgressMonitor monitor) throws CModelException {
		CModelOperation op;
		if (elements != null && elements[0] != null && elements[0].getElementType() <= ICElement.C_UNIT) {
			op = new RenameResourceElementsOperation(elements, destinations, renamings, force);
		} else {
			op = new RenameElementsOperation(elements, destinations, renamings, force);
		}
		op.runOperation(monitor);
	}

	/**
	 * Configures and runs the <code>MultiOperation</code>.
	 */
	protected void runOperation(MultiOperation op, ICElement[] elements, ICElement[] siblings,
			String[] renamings, IProgressMonitor monitor) throws CModelException {
		op.setRenamings(renamings);
		if (siblings != null) {
			for (int i = 0; i < elements.length; i++) {
				op.setInsertBefore(elements[i], siblings[i]);
			}
		}
		op.runOperation(monitor);
	}

	@Override
	protected CElementInfo createElementInfo () {
		return new CModelInfo(this);
	}

	// CHECKPOINT: Roots will return the hashcode of their resource
	@Override
	public int hashCode() {
		return resource.hashCode();
	}

	/**
	 * Workaround for bug 15168 circular errors not reported
	 * Returns the list of java projects before resource delta processing
	 * has started.
	 */
	public ICProject[] getOldCProjectsList() throws CModelException {
		CModelManager manager = CModelManager.getDefault();
		return manager.cProjectsCache == null ?
				getCProjects() :
				manager.cProjectsCache;
	}

	/* (non-Javadoc)
	 * @see Openable#buildStructure(OpenableInfo, IProgressMonitor, Map, IResource)
	 */
	@Override
	protected boolean buildStructure(OpenableInfo info, IProgressMonitor pm, Map<ICElement,
			CElementInfo> newElements, IResource underlyingResource) throws CModelException {
		boolean validInfo = false;
		try {
			IResource res = getResource();
			if (res != null && (res instanceof IWorkspaceRoot || res.getProject().isOpen())) {
				validInfo = computeChildren(info, res);
			}
		} finally {
			if (!validInfo) {
				CModelManager.getDefault().removeInfo(this);
			}
		}
		return validInfo;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ICModel#getNonCResources()
	 */
	@Override
	public Object[] getNonCResources() throws CModelException {
		return ((CModelInfo) getElementInfo()).getNonCResources();
	}

	protected  boolean computeChildren(OpenableInfo info, IResource res) throws CModelException {
		// determine my children
		IWorkspaceRoot root = (IWorkspaceRoot) getResource();
		IProject[] projects = root.getProjects();
		for (IProject project : projects) {
			if (CoreModel.hasCNature(project) || CoreModel.hasCCNature(project)) {
				ICProject cproject = new CProject(this, project);
				info.addChild(cproject);
			}
		}
		((CModelInfo) getElementInfo()).setNonCResources(null);
		return true;
	}

	@Override
	public ICElement getHandleFromMemento(String token, MementoTokenizer memento) {
		switch (token.charAt(0)) {
		case CEM_CPROJECT:
			if (!memento.hasMoreTokens()) return this;
			String projectName = memento.nextToken();
			CElement project = (CElement) getCProject(projectName);
			if (project != null) {
				return project.getHandleFromMemento(memento);
			}
		}
		return null;
	}

	@Override
	public void getHandleMemento(StringBuilder buff) {
		buff.append(getElementName());
	}

	@Override
	protected char getHandleMementoDelimiter() {
		Assert.isTrue(false, "Should not be called"); //$NON-NLS-1$
		return 0;
	}
}

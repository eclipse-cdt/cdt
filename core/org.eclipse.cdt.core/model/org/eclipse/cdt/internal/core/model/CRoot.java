package org.eclipse.cdt.internal.core.model;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */
 
import java.util.ArrayList;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ICResource;
import org.eclipse.cdt.core.model.ICRoot;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.IProgressMonitor;

public class CRoot extends CResource implements ICRoot {

	public CRoot(IWorkspaceRoot root) {
		super (null, root, root.getName(), ICElement.C_ROOT);
	}
	
	public ICRoot getCModel() {
		return this;
	}
	public ICProject getCProject(String name) {
		CModelManager factory = CModelManager.getDefault();
		return (ICProject)factory.create(getWorkspace().getRoot().getProject(name));
	}

	public ICProject[] getCProjects() {
		ArrayList list = getChildrenOfType(C_PROJECT);
		ICProject[] array= new ICProject[list.size()];
		list.toArray(array);
		return array;
	}

	public IWorkspace getWorkspace() {
		try {
			return getUnderlyingResource().getWorkspace();
		} catch (CModelException e) {
			e.printStackTrace();
		}
		return null;
	}

	public IWorkspaceRoot getRoot() {
		try {
			return (IWorkspaceRoot)getUnderlyingResource();
		} catch (CModelException e) {
		}
		return null;
	}

	public void copy(ICElement[] elements, ICElement[] containers, ICElement[] siblings,
		String[] renamings, boolean replace, IProgressMonitor monitor) throws CModelException {
		if (elements != null && elements[0] != null && elements[0] instanceof ICResource ) {
			runOperation(new CopyResourceElementsOperation(elements, containers, replace), elements, siblings, renamings, monitor);
		} else {
			throw new CModelException (new CModelStatus());
			//runOperation(new CopyElementsOperation(elements, containers, force), elements, siblings, renamings, monitor);
		}
	}

	public void delete(ICElement[] elements, boolean force, IProgressMonitor monitor)
		throws CModelException {
		if (elements != null && elements[0] != null && elements[0] instanceof ICResource) {
			runOperation(new DeleteResourceElementsOperation(elements, force), monitor);
		} else {
			throw new CModelException (new CModelStatus());
			//runOperation(new DeleteElementsOperation(elements, force), monitor);
		}
	}

	public void move(ICElement[] elements, ICElement[] containers, ICElement[] siblings,
		String[] renamings, boolean replace, IProgressMonitor monitor) throws CModelException {
		if (elements != null && elements[0] != null && elements[0] instanceof ICResource ) {
			runOperation(new MoveResourceElementsOperation(elements, containers, replace), elements, siblings, renamings, monitor);
		} else {
			throw new CModelException (new CModelStatus());
			//runOperation(new MoveElementsOperation(elements, containers, force), elements, siblings, renamings, monitor);
		}
	}

	public void rename(ICElement[] elements, ICElement[] destinations, String[] renamings,
		boolean force, IProgressMonitor monitor) throws CModelException {
		if (elements != null && elements[0] != null && elements[0] instanceof ICResource) {
			runOperation(new RenameResourceElementsOperation(elements, destinations,
					renamings, force), monitor);
		} else {
			throw new CModelException (new CModelStatus());
			//runOperation(new RenameElementsOperation(elements, containers, renamings, force), monitor);
		}
	}

	/**
	 * Configures and runs the <code>MultiOperation</code>.
	 */
	protected void runOperation(MultiOperation op, ICElement[] elements, ICElement[] siblings, String[] renamings, IProgressMonitor monitor) throws CModelException {
		op.setRenamings(renamings);
		if (siblings != null) {
			for (int i = 0; i < elements.length; i++) {
				op.setInsertBefore(elements[i], siblings[i]);
			}
		}
		runOperation(op, monitor);
	}

	protected CElementInfo createElementInfo () {
		return new CRootInfo(this);
	}

	// CHECKPOINT: Roots will return the hashcode of their resource
	public int hashCode() {
		return resource.hashCode();
	}
	
}

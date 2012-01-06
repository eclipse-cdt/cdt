/*******************************************************************************
 * Copyright (c) 2002, 2008 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.cview;

import java.util.ArrayList;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.IWorkbenchPartSite;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.IArchive;
import org.eclipse.cdt.core.model.IArchiveContainer;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.IBinaryContainer;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IIncludeReference;
import org.eclipse.cdt.core.model.ILibraryReference;
import org.eclipse.cdt.ui.CElementContentProvider;

import org.eclipse.cdt.internal.ui.util.RemoteTreeContentManager;
import org.eclipse.cdt.internal.ui.util.RemoteTreeViewer;

/**
 * CViewContentProvider
 */
public class CViewContentProvider extends CElementContentProvider {
	private RemoteTreeContentManager fManager;

	public CViewContentProvider() {
		super();
	}

	/**
	 *
	 */
	public CViewContentProvider(TreeViewer viewer, IWorkbenchPartSite site) {
		super();
		fManager = createContentManager(viewer, site);
	}

	/**
	 * @param provideMembers
	 * @param provideWorkingCopy
	 */
	public CViewContentProvider(TreeViewer viewer, IWorkbenchPartSite site, boolean provideMembers, boolean provideWorkingCopy) {
		super(provideMembers, provideWorkingCopy);
		fManager = createContentManager(viewer, site);
	}

	protected RemoteTreeContentManager createContentManager(TreeViewer viewer, IWorkbenchPartSite site) {
		if (site == null) {
			return new RemoteTreeContentManager(this, (RemoteTreeViewer)viewer, null);
		}
		return new RemoteTreeContentManager(this, (RemoteTreeViewer)viewer, site);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
	 */
	@Override
	public Object[] getChildren(Object element) {
		Object[] objs = null;

		if (fManager != null) {
			// use the the deferred manager for some cases
			if (element instanceof IBinary) {
				// It takes sometimes to parse binaries deferred it
				objs = fManager.getChildren(element);
			} else if (element instanceof IArchive) {
				// It takes sometimes to parse archives deferred it
				objs = fManager.getChildren(element);
			}
		}

		if (objs == null) {
			objs = super.getChildren(element);
		}
		Object[] extras = null;
		try {
			if (element instanceof ICProject) {
				extras = getProjectChildren((ICProject)element);
			} else if (element instanceof IBinaryContainer) {
				extras = getExecutables((IBinaryContainer)element);
			} else if (element instanceof IArchiveContainer) {
				extras = getArchives((IArchiveContainer)element);
			} else if (element instanceof IIncludeReference) {
				extras = getIncludeReferenceChildren((IIncludeReference)element);
			}
			/*
			 * Do not to this for now, since ILibraryReference is an Archive.
			 else if (element instanceof ILibraryReference) {
				extras =  ((ILibraryReference)element).getChildren();
			}*/
		} catch (CModelException e) {
		}
		if (extras != null && extras.length > 0) {
			objs = concatenate(objs, extras);
		}
		return objs;
	}

	public Object[] getIncludeReferenceChildren(IIncludeReference ref) throws CModelException {
		// We do not want to show children for Include paths that are inside the workspace.
		// no need to that since they can access elsewhere and that simplifies the
		// CView code.
		IPath location = ref.getPath();
		IContainer[] containers = ref.getCModel().getWorkspace().getRoot().findContainersForLocation(location);
		for (int i = 0; i < containers.length; ++i) {
			if (containers[i].isAccessible()) {
				return NO_CHILDREN;
			}
		}
		return ref.getChildren();
	}

	private Object[] getProjectChildren(ICProject cproject) throws CModelException {
		Object[] extras = null;
		IArchiveContainer archive = cproject.getArchiveContainer();
		if (getArchives(archive).length > 0) {
			extras = new Object[] {archive};
		}
		IBinaryContainer bin = cproject.getBinaryContainer();
		if (getExecutables(bin).length > 0) {
			Object[] o = new Object[] {bin};
			if (extras != null && extras.length > 0) {
				extras = concatenate(extras, o);
			} else {
				extras = o;
			}
		}
		LibraryRefContainer libRefCont = new LibraryRefContainer(cproject);
		Object[] libRefs = libRefCont.getChildren(cproject);
		if (libRefs != null && libRefs.length > 0) {
			Object[] o = new Object[] {libRefCont};
			if (extras != null && extras.length > 0) {
				extras = concatenate(extras, o);
			} else {
				extras = o;
			}
		}

		IncludeRefContainer incRefCont = new IncludeRefContainer(cproject);
		Object[] incRefs = incRefCont.getChildren(cproject);
		if (incRefs != null && incRefs.length > 0) {
			Object[]  o = new Object[] {incRefCont};
			if (extras != null && extras.length > 0) {
				extras = concatenate(extras, o);
			} else {
				extras = o;
			}
		}
		return extras;
	}

	protected IBinary[] getExecutables(IBinaryContainer container) throws CModelException {
		ICElement[] celements = container.getChildren();
		ArrayList<IBinary> list = new ArrayList<IBinary>(celements.length);
		for (int i = 0; i < celements.length; i++) {
			if (celements[i] instanceof IBinary) {
				IBinary bin = (IBinary)celements[i];
				if (bin.showInBinaryContainer()) {
					list.add(bin);
				}
			}
		}
		IBinary[] bins = new IBinary[list.size()];
		list.toArray(bins);
		return bins;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.ui.BaseCElementContentProvider#internalGetParent(java.lang.Object)
	 */
	@Override
	public Object internalGetParent(Object element) {
		// since we insert logical containers we have to fix
		// up the parent for {IInclude,ILibrary}Reference so that they refer
		// to the container and containers refere to the project
		Object parent = super.internalGetParent(element);
		if (element instanceof IncludeReferenceProxy) {
			parent = ((IncludeReferenceProxy)element).getIncludeRefContainer();
		} else if (element instanceof IncludeRefContainer) {
			parent = ((IncludeRefContainer)element).getCProject();
		} else if (element instanceof ILibraryReference) {
			if (parent instanceof ICProject) {
				parent = new LibraryRefContainer((ICProject)parent);
			}
		} else if (element instanceof LibraryRefContainer) {
			parent = ((LibraryRefContainer)element).getCProject();
		}
		return parent;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
	 */
	@Override
	public boolean hasChildren(Object element) {
		if (fManager != null) {
			if (element instanceof IBinary) {
				return fManager.mayHaveChildren(element);
			} else if (element instanceof IArchive) {
				return fManager.mayHaveChildren(element);
			}
		}
		if (element instanceof IBinaryContainer) {
			try {
				IBinaryContainer cont = (IBinaryContainer)element;
				IBinary[] bins = getBinaries(cont);
				return (bins != null) && bins.length > 0;
			} catch (CModelException e) {
				return false;
			}
		} else if (element instanceof IArchiveContainer) {
			try {
				IArchiveContainer cont = (IArchiveContainer)element;
				IArchive[] ars = getArchives(cont);
				return (ars != null) && ars.length > 0;
			} catch (CModelException e) {
				return false;
			}
		} else if (element instanceof IncludeReferenceProxy) {
			IIncludeReference reference = ((IncludeReferenceProxy)element).getReference();
			IContainer container = ResourcesPlugin.getWorkspace().getRoot().getContainerForLocation(reference.getPath());
			if (container != null) {
				// do not allow to navigate to workspace containers inside "Includes" node
				return false;
			}

			return reference.hasChildren();
		}
		return super.hasChildren(element);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	@Override
	public void dispose() {
		if (fManager != null) {
			fManager.cancel();
		}
		super.dispose();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if (fManager != null) {
			fManager.cancel();
		}
		super.inputChanged(viewer, oldInput, newInput);
	}

}

/**********************************************************************
 * Copyright (c) 2002,2003,2004 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
 ***********************************************************************/

package org.eclipse.cdt.internal.ui.cview;

import java.util.ArrayList;

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
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.IPath;

/**
 * CViewContentProvider
 */
public class CViewContentProvider extends CElementContentProvider {
	/**
	 * 
	 */
	public CViewContentProvider() {
		super();
	}

	/**
	 * @param provideMembers
	 * @param provideWorkingCopy
	 */
	public CViewContentProvider(boolean provideMembers, boolean provideWorkingCopy) {
		super(provideMembers, provideWorkingCopy);
	}

	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
	 */
	public Object[] getChildren(Object element) {
		Object[] objs = super.getChildren(element);
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
			} else if (element instanceof ILibraryReference) {
				extras =  ((ILibraryReference)element).getChildren();
			}
		} catch (CModelException e) {
			extras = null;
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
	
	/**
	 * @return
	 */
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
		ArrayList list = new ArrayList(celements.length);
		for (int i = 0; i < celements.length; i++) {
			if (celements[i] instanceof IBinary) {
				IBinary bin = (IBinary)celements[i];
				if (bin.isExecutable() || bin.isSharedLib()) {
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
	public Object internalGetParent(Object element) {
		// since we insert logical containers we have to fix
		// up the parent for {IInclude,ILibrary}Reference so that they refer
		// to the container and containers refere to the project
		Object parent = super.internalGetParent(element);
		if (element instanceof IncludeReferenceProxy) {
			parent = ((IncludeReferenceProxy)element).getIncludeRefContainer();
		} else if (element instanceof IncludeRefContainer) {
			parent = ((IncludeRefContainer)element).getCProject();
		} if (element instanceof ILibraryReference) {
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
	public boolean hasChildren(Object element) {
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
		} else if (element instanceof IIncludeReference) {
			IIncludeReference ref = (IIncludeReference)element;
			IPath location = ref.getPath();
			IContainer[] containers = ref.getCModel().getWorkspace().getRoot().findContainersForLocation(location);
			for (int i = 0; i < containers.length; ++i) {
				if (containers[i].isAccessible()) {
					return false;
				}
			}

		}
		return super.hasChildren(element);
	}
}

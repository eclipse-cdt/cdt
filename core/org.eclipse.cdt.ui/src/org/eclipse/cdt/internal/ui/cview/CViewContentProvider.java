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

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.IArchiveContainer;
import org.eclipse.cdt.core.model.IBinaryContainer;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IIncludeReference;
import org.eclipse.cdt.core.model.ILibraryReference;
import org.eclipse.cdt.ui.CElementContentProvider;

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
		if (element instanceof ICProject) {
			extras = getProjectChildren((ICProject)element);
		} else if (element instanceof IBinaryContainer) {
			extras = getExecutables((IBinaryContainer)element);
		} else if (element instanceof IArchiveContainer) {
			extras = getArchives((IArchiveContainer)element);
		} else if (element instanceof LibraryRefContainer) {
			extras = ((LibraryRefContainer)element).getChildren(element);
		} else if (element instanceof IncludeRefContainer) {
			extras = ((IncludeRefContainer)element).getChildren(element);
		}

		if (extras != null && extras.length > 0) {
			objs = concatenate(objs, extras);
		}
		return objs;
	}

	/**
	 * @return
	 */
	private Object[] getProjectChildren(ICProject cproject) {
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
		try {
			ILibraryReference[] libRefs = cproject.getLibraryReferences();
			if (libRefs != null && libRefs.length > 0) {
				Object[] o = new Object[] {new LibraryRefContainer(cproject)};
				if (extras != null && extras.length > 0) {
					extras = concatenate(extras, o);
				} else {
					extras = o;
				}
			}
			IIncludeReference[] incRefs = cproject.getIncludeReferences();
			if (incRefs != null && incRefs.length > 0) {
				Object[] o = new Object[] {new IncludeRefContainer(cproject)};
				if (extras != null && extras.length > 0) {
					extras = concatenate(extras, o);
				} else {
					extras = o;
				}
			}
		} catch (CModelException e) {
		}
		return extras;
	}
}

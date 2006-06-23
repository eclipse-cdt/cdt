/*******************************************************************************
 * Copyright (c) 2002, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.cview;


import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IIncludeReference;
import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.ui.CElementGrouping;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * IncludeRefContainer
 */
public class IncludeRefContainer extends CElementGrouping {

	ICProject fCProject;

	/**
	 * 
	 */
	public IncludeRefContainer(ICProject cproject) {
		super(INCLUDE_REF_CONTAINER);
		fCProject = cproject;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {
		if (adapter == IWorkbenchAdapter.class) {
			return this;
		}
		if (adapter == ICProject.class) {
			return fCProject;
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.model.IWorkbenchAdapter#getChildren(java.lang.Object)
	 */
	public Object[] getChildren(Object o) {
		try {
			IIncludeReference[] references = fCProject.getIncludeReferences();
			IncludeReferenceProxy[] proxies = new IncludeReferenceProxy[references.length];
			for (int i = 0; i < proxies.length; ++i) {
				proxies[i] = new IncludeReferenceProxy(this, references[i]);
			}
			return proxies;
		} catch (CModelException e) {
		}
		return NO_CHILDREN;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.model.IWorkbenchAdapter#getImageDescriptor(java.lang.Object)
	 */
	public ImageDescriptor getImageDescriptor(Object object) {
		return CPluginImages.DESC_OBJS_INCLUDES_CONTAINER;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.model.IWorkbenchAdapter#getLabel(java.lang.Object)
	 */
	public String getLabel(Object o) {
		return CViewMessages.getString("IncludeRefContainer.Includes");  //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.model.IWorkbenchAdapter#getParent(java.lang.Object)
	 */
	public Object getParent(Object o) {
		return getCProject();
	}

	public ICProject getCProject() {
		return fCProject;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (obj instanceof IncludeRefContainer) {
			IncludeRefContainer other = (IncludeRefContainer)obj;
			return fCProject.equals(other.getCProject());
		}
		return super.equals(obj);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		if (fCProject != null) {
			return fCProject.hashCode();
		}
		return super.hashCode();
	}

}

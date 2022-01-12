/*******************************************************************************
 * Copyright (c) 2002, 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.cview;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IIncludeReference;
import org.eclipse.cdt.ui.CDTSharedImages;
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

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getAdapter(Class<T> adapter) {
		if (adapter == IWorkbenchAdapter.class) {
			return (T) this;
		}
		if (adapter == ICProject.class) {
			return (T) fCProject;
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.model.IWorkbenchAdapter#getChildren(java.lang.Object)
	 */
	@Override
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
	@Override
	public ImageDescriptor getImageDescriptor(Object object) {
		return CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_OBJS_INCLUDES_CONTAINER);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.model.IWorkbenchAdapter#getLabel(java.lang.Object)
	 */
	@Override
	public String getLabel(Object o) {
		return CViewMessages.IncludeRefContainer_Includes;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.model.IWorkbenchAdapter#getParent(java.lang.Object)
	 */
	@Override
	public Object getParent(Object o) {
		return getCProject();
	}

	public ICProject getCProject() {
		return fCProject;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof IncludeRefContainer) {
			IncludeRefContainer other = (IncludeRefContainer) obj;
			return fCProject.equals(other.getCProject());
		}
		return super.equals(obj);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		if (fCProject != null) {
			return fCProject.hashCode();
		}
		return super.hashCode();
	}

}

/*******************************************************************************
 * Copyright (c) 2000, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.cview;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.IIncludeReference;
import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.ui.CElementGrouping;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * @author User
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class IncludeReferenceProxy extends CElementGrouping {

	IncludeRefContainer includeRefContainer;
	IIncludeReference reference;
	
	public IncludeReferenceProxy(IncludeRefContainer parent, IIncludeReference reference) {
		super(0);
		this.reference = reference;
		this.includeRefContainer = parent;
	}

	public IIncludeReference getReference() {
		return reference;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.model.IWorkbenchAdapter#getChildren(java.lang.Object)
	 */
	public Object[] getChildren(Object object) {
		try {
			return reference.getChildren();
		} catch (CModelException e) {
			// We should log the error.
		}
		return NO_CHILDREN;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.model.IWorkbenchAdapter#getImageDescriptor(java.lang.Object)
	 */
	public ImageDescriptor getImageDescriptor(Object object) {
		return CPluginImages.DESC_OBJS_INCLUDES_FOLDER;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.model.IWorkbenchAdapter#getParent(java.lang.Object)
	 */
	public Object getParent(Object object) {
		return getIncludeRefContainer();
	}

	public IncludeRefContainer getIncludeRefContainer() {
		return includeRefContainer;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		return reference.equals(obj);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return reference.toString();
	}
}

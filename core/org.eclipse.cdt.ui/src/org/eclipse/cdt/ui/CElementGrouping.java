/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui;

import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.model.WorkbenchAdapter;

/**
 */
public abstract class CElementGrouping extends WorkbenchAdapter implements IAdaptable {

	public final static int INCLUDES_GROUPING = 0x00001;
	public final static int NAMESPACE_GROUPING = 0x00010;
	public final static int CLASS_GROUPING = 0x00100;
	public final static int LIBRARY_REF_CONTAINER = 0x01000;
	public final static int INCLUDE_REF_CONTAINER = 0x10000;
	
	int type;
	
	public CElementGrouping(int type) {
		this.type = type;
	}

	public int getType() {
		return type;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.model.IWorkbenchAdapter#getLabel(java.lang.Object)
	 */
	public String getLabel(Object object) {
		switch (type) {
			case INCLUDES_GROUPING:
				return "include directives"; //$NON-NLS-1$
		}
		return super.getLabel(object);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.model.IWorkbenchAdapter#getImageDescriptor(java.lang.Object)
	 */
	public ImageDescriptor getImageDescriptor(Object object) {
		switch (type) {
			case INCLUDES_GROUPING:
				return CPluginImages.DESC_OBJS_INCCONT;
			case NAMESPACE_GROUPING:
				return CPluginImages.DESC_OBJS_NAMESPACE;
		}
		return super.getImageDescriptor(object);
	}

	/*
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(Class)
	 */
	public Object getAdapter(Class clas) {
		if (clas == IWorkbenchAdapter.class)
			return this;
		return null;
	}

}

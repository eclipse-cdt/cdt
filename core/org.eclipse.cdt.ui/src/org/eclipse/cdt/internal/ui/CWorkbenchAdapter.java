package org.eclipse.cdt.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.model.IWorkbenchAdapter;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IParent;

/**
 * An imlementation of the IWorkbenchAdapter for CElements.
 */
public class CWorkbenchAdapter implements IWorkbenchAdapter {
	
	private static final Object[] fgEmptyArray= new Object[0];

	/**
	 * @see IWorkbenchAdapter#getChildren
	 */	
	public Object[] getChildren(Object o) {
		if (o instanceof IParent) {
			Object[] members= ((IParent)o).getChildren();
			if (members != null) {
				return members;
			}
		}
		return fgEmptyArray;
	}

	/**
	 * @see IWorkbenchAdapter#getImageDescriptor
	 */
	public ImageDescriptor getImageDescriptor(Object object) {
		return null;
	}

	/**
	 * @see IWorkbenchAdapter#getLabel
	 */
	public String getLabel(Object o) {
		if (o instanceof ICElement) {
			return ((ICElement)o).getElementName();
		}
		return null;
	}

	/**
	 * @see IWorkbenchAdapter#getParent
	 */
	public Object getParent(Object o) {
		if (o instanceof ICElement) {
			return ((ICElement)o).getParent();
		}
		return null;
	}
}

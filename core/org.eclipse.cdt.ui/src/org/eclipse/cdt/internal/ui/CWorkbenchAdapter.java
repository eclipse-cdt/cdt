package org.eclipse.cdt.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IParent;
import org.eclipse.cdt.ui.CElementLabelProvider;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * An imlementation of the IWorkbenchAdapter for CElements.
 */
public class CWorkbenchAdapter implements IWorkbenchAdapter {

	private static final Object[] fgEmptyArray = new Object[0];
	private CElementImageProvider fImageProvider;
	private CElementLabelProvider fLabelProvider;

	public CWorkbenchAdapter() {
		fImageProvider = new CElementImageProvider();
		fLabelProvider = new CElementLabelProvider();
	}

	/**
	 * @see IWorkbenchAdapter#getChildren
	 */
	public Object[] getChildren(Object o) {
		if (o instanceof IParent) {
			Object[] members = ((IParent) o).getChildren();
			if (members != null) {
				return members;
			}
		}
		return fgEmptyArray;
	}

	/**
	 * @see IWorkbenchAdapter#getImageDescriptor
	 */
	public ImageDescriptor getImageDescriptor(Object element) {
		if (element instanceof ICElement) {
			return fImageProvider.getCImageDescriptor(
				(ICElement) element,
				CElementImageProvider.OVERLAY_ICONS | CElementImageProvider.SMALL_ICONS);
		}
		return null;
	}

	/**
	 * @see IWorkbenchAdapter#getLabel
	 */
	public String getLabel(Object o) {
		if (o instanceof ICElement) {
			return fLabelProvider.getText((ICElement) o);
		}
		return null;
	}

	/**
	 * @see IWorkbenchAdapter#getParent
	 */
	public Object getParent(Object o) {
		if (o instanceof ICElement) {
			return ((ICElement) o).getParent();
		}
		return null;
	}
}

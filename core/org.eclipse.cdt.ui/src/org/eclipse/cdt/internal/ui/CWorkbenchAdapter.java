package org.eclipse.cdt.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IParent;
import org.eclipse.cdt.internal.ui.viewsupport.CElementImageProvider;
import org.eclipse.cdt.ui.CElementLabelProvider;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IActionFilter;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * An imlementation of the IWorkbenchAdapter for CElements.
 */
public class CWorkbenchAdapter implements IWorkbenchAdapter, IActionFilter {

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
			try {
				Object[] members = ((IParent) o).getChildren();
				if (members != null) {
					return members;
				}
			} catch (CModelException e) {
				CUIPlugin.getDefault().log(e);
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
			return fLabelProvider.getText(o);
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

	public boolean testAttribute(Object target, String name, String value) {
		ICElement element = (ICElement)target;
		IResource resource = element.getResource();
		if (resource != null) {
			IActionFilter filter = (IActionFilter)resource.getAdapter(IActionFilter.class);
			if (filter != null) {
				return filter.testAttribute(resource, name, value);
			}
		}
		return false;
	}
}

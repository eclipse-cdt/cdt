/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software System
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.model.IWorkbenchAdapter;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IParent;
import org.eclipse.cdt.ui.CElementLabelProvider;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.ui.viewsupport.CElementImageProvider;

/**
 * An implementation of the IWorkbenchAdapter for CElements.
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
	@Override
	public Object[] getChildren(Object o) {
		if (o instanceof IParent) {
			try {
				Object[] members = ((IParent) o).getChildren();
				if (members != null) {
					return members;
				}
			} catch (CModelException e) {
				CUIPlugin.log(e);
			}
		}
		return fgEmptyArray;
	}

	/**
	 * @see IWorkbenchAdapter#getImageDescriptor
	 */
	@Override
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
	@Override
	public String getLabel(Object o) {
		if (o instanceof ICElement) {
			return fLabelProvider.getText(o);
		}
		return null;
	}

	/**
	 * @see IWorkbenchAdapter#getParent
	 */
	@Override
	public Object getParent(Object o) {
		if (o instanceof ICElement) {
			return ((ICElement) o).getParent();
		}
		return null;
	}
}

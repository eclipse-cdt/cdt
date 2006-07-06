/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.navigator;

import org.eclipse.cdt.core.model.IArchiveContainer;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.IBinaryContainer;
import org.eclipse.cdt.core.model.IBinaryModule;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.ui.cview.CViewLabelProvider;
import org.eclipse.cdt.internal.ui.cview.CViewMessages;
import org.eclipse.cdt.internal.ui.viewsupport.AppearanceAwareLabelProvider;
import org.eclipse.cdt.internal.ui.viewsupport.CElementImageProvider;
import org.eclipse.core.resources.IResource;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.navigator.ICommonContentExtensionSite;
import org.eclipse.ui.navigator.ICommonLabelProvider;

/**
 * A label provider suitable for the Common Navigator providing also
 * status message text for the current selected item.
 * 
 * @see org.eclipse.cdt.internal.ui.cview.CView#createLabelProvider
 * @see org.eclipse.cdt.internal.ui.cview.CView#getStatusLineMessage
 */
public class CNavigatorLabelProvider extends CViewLabelProvider implements ICommonLabelProvider {

	/**
	 * Create a default label provider.
	 */
	public CNavigatorLabelProvider() {
		super(AppearanceAwareLabelProvider.DEFAULT_TEXTFLAGS,
				AppearanceAwareLabelProvider.DEFAULT_IMAGEFLAGS
						| CElementImageProvider.SMALL_ICONS);
	}

	/*
	 * @see org.eclipse.ui.navigator.ICommonLabelProvider#init(org.eclipse.ui.navigator.ICommonContentExtensionSite)
	 */
	public void init(ICommonContentExtensionSite extensionSite) {
		// no-op
	}

	/*
	 * @see org.eclipse.ui.navigator.IMementoAware#restoreState(org.eclipse.ui.IMemento)
	 */
	public void restoreState(IMemento memento) {
		// no-op
	}

	/*
	 * @see org.eclipse.ui.navigator.IMementoAware#saveState(org.eclipse.ui.IMemento)
	 */
	public void saveState(IMemento memento) {
		// no-op
	}

	/*
	 * @see org.eclipse.ui.navigator.IDescriptionProvider#getDescription(java.lang.Object)
	 */
	public String getDescription(Object element) {
		if (element instanceof IResource) {
			return ((IResource) element).getFullPath().makeRelative().toString();
		} else if (element instanceof ICElement) {
			ICElement celement = (ICElement) element;
			IResource res = (IResource) celement.getAdapter(IResource.class);
			if (res != null) {
				return res.getFullPath().toString();
			} else if (celement.getElementType() == ICElement.C_VCONTAINER) {
				if (celement instanceof IBinaryContainer) {
					ICProject cproj = celement.getCProject();
					if (cproj != null) {
						return cproj.getPath() + CViewMessages.getString("CView.binaries"); //$NON-NLS-1$
					}
				} else if (celement instanceof IArchiveContainer) {
					ICProject cproj = celement.getCProject();
					if (cproj != null) {
						return cproj.getPath() + CViewMessages.getString("CView.archives"); //$NON-NLS-1$
					}
				} else if (celement instanceof IBinaryModule) {
					IBinary bin = ((IBinaryModule) celement).getBinary();
					return bin.getPath() + ":" + celement.getElementName(); //$NON-NLS-1$
				}
			} else if (celement.getElementType() > ICElement.C_UNIT) {
				return celement.getPath().toString() + " - [" + celement.getElementName() + "]"; //$NON-NLS-1$ //$NON-NLS-2$
			}
			return celement.getElementName();
		} else if (element instanceof IWorkbenchAdapter) {
			IWorkbenchAdapter wAdapter = (IWorkbenchAdapter)element;
			return wAdapter.getLabel(element);
		}
		return null;
	}

}

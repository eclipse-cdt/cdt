/*******************************************************************************
 * Copyright (c) 2005, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software System
 *     Anton Leherbauer (Wind River Systems)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.ui.IActionFilter;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.progress.IDeferredWorkbenchAdapter;
import org.eclipse.ui.views.properties.FilePropertySource;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.ResourcePropertySource;

import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.ICElement;

/**
 * Implements basic UI support for C elements.
 */
public class CElementAdapterFactory implements IAdapterFactory {

	private static Class<?>[] PROPERTIES= new Class[] {
		IPropertySource.class,
		IResource.class,
		IWorkbenchAdapter.class,
		IPersistableElement.class,
		IDeferredWorkbenchAdapter.class,
		IActionFilter.class
	};

	private static CWorkbenchAdapter fgCWorkbenchAdapter;
	private static CActionFilter fgCActionFilter;

	/**
	 * @see CElementAdapterFactory#getAdapterList
	 */
	@Override
	public Class<?>[] getAdapterList() {
		return PROPERTIES;
	}

	/**
	 * @see CElementAdapterFactory#getAdapter
	 */
	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Object getAdapter(Object element, Class key) {
		ICElement celem = (ICElement) element;

		if (IPropertySource.class.equals(key)) {
			return getPropertySource(celem);
		} else if (IResource.class.isAssignableFrom(key)) {
			IResource resource= getResource(celem);
			if (resource != null && key.isAssignableFrom(resource.getClass())) {
				return resource;
			}
		} else if (IPersistableElement.class.equals(key)) {
			return new PersistableCElementFactory(celem);
		} else if (IDeferredWorkbenchAdapter.class.equals(key)) {
			return getDeferredWorkbenchAdapter(celem);
		} else if (IWorkbenchAdapter.class.equals(key)) {
			return getWorkbenchAdapter(celem);
		} else if (IActionFilter.class.equals(key)) {
			return getActionFilter(celem);
		}
		return null;
	}

	private IPropertySource getPropertySource(ICElement celement) {
		if (celement instanceof IBinary) {
			return new BinaryPropertySource((IBinary)celement);
		}
		IResource res = celement.getResource();
		if (res != null) {
			if (res instanceof IFile) {
				return new FilePropertySource((IFile)res);
			}
			return new ResourcePropertySource(res);
		}
		return new CElementPropertySource(celement);
	}

	private IResource getResource(ICElement celement) {
		return celement.getResource();
	}

	private IDeferredWorkbenchAdapter getDeferredWorkbenchAdapter(ICElement celement) {
		return new DeferredCWorkbenchAdapter(celement);
	}

	private IWorkbenchAdapter getWorkbenchAdapter(ICElement celement) {
		if (fgCWorkbenchAdapter == null) {
			fgCWorkbenchAdapter = new CWorkbenchAdapter();
		}
		return fgCWorkbenchAdapter;
	}

	private IActionFilter getActionFilter(ICElement celement) {
		if (fgCActionFilter == null) {
			fgCActionFilter = new CActionFilter();
		}
		return fgCActionFilter;
	}
}

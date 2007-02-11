/*******************************************************************************
 * Copyright (c) 2004, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.elements.adapters; 

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.debug.core.model.ICModule;
import org.eclipse.cdt.debug.core.model.IModuleRetrieval;
import org.eclipse.cdt.debug.internal.ui.views.modules.ModuleContentProvider;
import org.eclipse.cdt.debug.internal.ui.views.modules.ModuleProxyFactory;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementContentProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelProxyFactoryAdapter;
 
/**
 * Comment for .
 */
public class CDebugElementAdapterFactory implements IAdapterFactory {

    private static IElementContentProvider fgModuleContentProvider = new ModuleContentProvider();
	private static IModelProxyFactoryAdapter fgModuleProxyFactory = new ModuleProxyFactory();


	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object, java.lang.Class)
	 */
	public Object getAdapter( Object adaptableObject, Class adapterType ) {
	    if ( adapterType.isInstance( adaptableObject ) ) {
			return adaptableObject;
		}
		if ( adapterType.equals( IElementContentProvider.class ) ) {
			if ( adaptableObject instanceof IModuleRetrieval ) {
				return fgModuleContentProvider;
			}
			if ( adaptableObject instanceof ICModule ) {
				return fgModuleContentProvider;
			}
			if ( adaptableObject instanceof ICElement ) {
				return fgModuleContentProvider;
			}
		}
		if ( adapterType.equals( IModelProxyFactoryAdapter.class ) ) {
			if ( adaptableObject instanceof IModuleRetrieval ) {
				return fgModuleProxyFactory;
			}
		}
    	return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
	 */
	public Class[] getAdapterList() {
		return new Class[] {
				IElementContentProvider.class,
				IModelProxyFactoryAdapter.class
			};
	}
}

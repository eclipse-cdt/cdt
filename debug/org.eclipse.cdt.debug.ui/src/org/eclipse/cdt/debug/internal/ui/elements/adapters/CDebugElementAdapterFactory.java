/*******************************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
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
import org.eclipse.cdt.debug.internal.ui.views.modules.ModuleContentAdapter;
import org.eclipse.cdt.debug.internal.ui.views.modules.ModuleProxyFactory;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.debug.internal.ui.viewers.provisional.IAsynchronousContentAdapter;
import org.eclipse.debug.internal.ui.viewers.provisional.IAsynchronousLabelAdapter;
import org.eclipse.debug.internal.ui.viewers.provisional.IModelProxyFactoryAdapter;
 
/**
 * Comment for .
 */
public class CDebugElementAdapterFactory implements IAdapterFactory {

//	private static IAsynchronousLabelAdapter fgModuleLabelAdapter = new AsynchronousDebugLabelAdapter();
	private static IAsynchronousContentAdapter fgModuleContentAdapter = new ModuleContentAdapter();
	private static IModelProxyFactoryAdapter fgModuleProxyFactory = new ModuleProxyFactory();


	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object, java.lang.Class)
	 */
	public Object getAdapter( Object adaptableObject, Class adapterType ) {
	    if ( adapterType.isInstance( adaptableObject ) ) {
			return adaptableObject;
		}
		if ( adapterType.equals( IAsynchronousContentAdapter.class ) ) {
			if ( adaptableObject instanceof IModuleRetrieval ) {
				return fgModuleContentAdapter;
			}
			if ( adaptableObject instanceof ICModule ) {
				return fgModuleContentAdapter;
			}
			if ( adaptableObject instanceof ICElement ) {
				return fgModuleContentAdapter;
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
				IAsynchronousContentAdapter.class,
				IAsynchronousLabelAdapter.class,
				IModelProxyFactoryAdapter.class
			};
	}
}

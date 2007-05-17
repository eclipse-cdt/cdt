/*******************************************************************************
 * Copyright (c) 2004, 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 * IBM Corporation
 * ARM Limited - https://bugs.eclipse.org/bugs/show_bug.cgi?id=186981
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.elements.adapters; 

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.debug.core.model.ICModule;
import org.eclipse.cdt.debug.core.model.IModuleRetrieval;
import org.eclipse.cdt.debug.internal.ui.views.modules.ModuleContentProvider;
import org.eclipse.cdt.debug.internal.ui.views.modules.ModuleMementoProvider;
import org.eclipse.cdt.debug.internal.ui.views.modules.ModuleProxyFactory;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementContentProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementMementoProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelProxyFactory;
 
public class CDebugElementAdapterFactory implements IAdapterFactory {

    private static IElementContentProvider fgModuleContentProvider = new ModuleContentProvider();
	private static IModelProxyFactory fgModuleProxyFactory = new ModuleProxyFactory();
    private static IElementMementoProvider fgModuleMementoProvider = new ModuleMementoProvider();

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
		if ( adapterType.equals( IModelProxyFactory.class ) ) {
			if ( adaptableObject instanceof IModuleRetrieval ) {
				return fgModuleProxyFactory;
			}
		}
        if ( adapterType.equals( IElementMementoProvider.class ) ) {
			if ( adaptableObject instanceof IModuleRetrieval ) {
				return fgModuleMementoProvider;
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
				IModelProxyFactory.class,
        		IElementMementoProvider.class,
			};
	}
}

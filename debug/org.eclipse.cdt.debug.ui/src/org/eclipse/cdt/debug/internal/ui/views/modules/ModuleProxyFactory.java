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
package org.eclipse.cdt.debug.internal.ui.views.modules; 

import org.eclipse.cdt.debug.core.model.IModuleRetrieval;
import org.eclipse.cdt.debug.ui.ICDebugUIConstants;
import org.eclipse.debug.internal.ui.viewers.provisional.IModelProxy;
import org.eclipse.debug.internal.ui.viewers.provisional.IModelProxyFactoryAdapter;
import org.eclipse.debug.internal.ui.viewers.provisional.IPresentationContext;
import org.eclipse.ui.IWorkbenchPart;
 
public class ModuleProxyFactory implements IModelProxyFactoryAdapter {

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.provisional.IModelProxyFactoryAdapter#createModelProxy(java.lang.Object, org.eclipse.debug.internal.ui.viewers.provisional.IPresentationContext)
	 */
	public IModelProxy createModelProxy( Object element, IPresentationContext context ) {
		IWorkbenchPart part = context.getPart();
		if ( part != null ) {
			String id = part.getSite().getId();
			if ( ICDebugUIConstants.ID_MODULES_VIEW.equals( id ) ) {
				if ( element instanceof IModuleRetrieval ) {
						return new ModulesViewModelProxy();
				}
			}
		}
		return null;
	}
}

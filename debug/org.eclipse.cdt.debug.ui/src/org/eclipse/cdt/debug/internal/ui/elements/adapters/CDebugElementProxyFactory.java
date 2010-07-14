/*******************************************************************************
 * Copyright (c) 2004, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 * IBM Corporation
 * Wind River Systems - adapted to work with platform Modules view (bug 210558)
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.elements.adapters; 

import org.eclipse.cdt.debug.core.model.ICDebugTarget;
import org.eclipse.cdt.debug.core.model.IModuleRetrieval;
import org.eclipse.cdt.debug.internal.core.model.DisassemblyRetrieval;
import org.eclipse.cdt.debug.internal.ui.views.modules.ModulesViewModelProxy;
import org.eclipse.cdt.debug.ui.ICDebugUIConstants;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelProxy;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.update.DefaultModelProxyFactory;
import org.eclipse.debug.ui.IDebugUIConstants;
 
public class CDebugElementProxyFactory extends DefaultModelProxyFactory {

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.provisional.IModelProxyFactoryAdapter#createModelProxy(java.lang.Object, org.eclipse.debug.internal.ui.viewers.provisional.IPresentationContext)
	 */
	public IModelProxy createModelProxy( Object element, IPresentationContext context ) {
        if ( IDebugUIConstants.ID_MODULE_VIEW.equals( context.getId() ) ) {
            IModuleRetrieval mr = null;
            if ( element instanceof IAdaptable ) {
                ICDebugTarget target = (ICDebugTarget)((IAdaptable)element).getAdapter( ICDebugTarget.class );
                if ( target != null )
                    mr = (IModuleRetrieval)target.getAdapter( IModuleRetrieval.class );
            }
            if ( mr != null ) {
                return new ModulesViewModelProxy( mr );
            }
        }
        else if ( IDebugUIConstants.ID_REGISTER_VIEW.equals( context.getId() ) ) {
            if ( element instanceof CRegisterManagerProxy ) {
                return new CRegisterManagerModelProxy( (CRegisterManagerProxy)element );
            }
        }
        else if ( ICDebugUIConstants.ID_DEFAULT_DISASSEMBLY_EDITOR.equals( context.getId() ) ) {
            if ( element instanceof DisassemblyRetrieval ) {
                return new DisassemblyElementProxy( element );
            }
        }
        return super.createModelProxy( element, context );
    }
}

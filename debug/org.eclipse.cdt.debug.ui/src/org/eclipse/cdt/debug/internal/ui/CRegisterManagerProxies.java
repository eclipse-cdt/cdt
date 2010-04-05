/*******************************************************************************
 * Copyright (c) 2010 CodeSourcery and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * CodeSourcery - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.internal.ui;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.debug.core.model.ICDebugElement;
import org.eclipse.cdt.debug.core.model.ICDebugTarget;
import org.eclipse.cdt.debug.internal.core.model.CDebugTarget;
import org.eclipse.cdt.debug.internal.ui.elements.adapters.CRegisterManagerProxy;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.internal.ui.stringsubstitution.SelectedResourceManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.contexts.IDebugContextManager;
import org.eclipse.debug.ui.contexts.IDebugContextService;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * Singleton that keeps track of <code>CRegisterManagerProxy</code> objects
 */
public class CRegisterManagerProxies {

    private static CRegisterManagerProxies fgInstance = new CRegisterManagerProxies();

    private Map<ICDebugTarget, CRegisterManagerProxy> fMap;

    public static CRegisterManagerProxies getInstance() {
        return fgInstance;
    }

    public CRegisterManagerProxies() {
        super();
        fMap = new HashMap<ICDebugTarget, CRegisterManagerProxy>();
    }

    public void dispose() {
        for ( CRegisterManagerProxy proxy : fMap.values() ) {
            DebugPlugin.getDefault().removeDebugEventListener( proxy );
            proxy.dispose();
        }
        fMap.clear();
    }

    public CRegisterManagerProxy getRegisterManagerProxy( ICDebugTarget target ) {
        CRegisterManagerProxy proxy = fMap.get( target );
        if ( proxy == null ) {
            synchronized( this ) {
                proxy = fMap.get( target );
                if ( proxy == null ) {
                    proxy = new CRegisterManagerProxy( ((CDebugTarget)target).getRegisterManager() );
                    DebugPlugin.getDefault().addDebugEventListener( proxy );
                    IDebugContextService service = getContextService();
                    if ( service != null ) {
                        ISelection s = service.getActiveContext();
                        if ( s instanceof IStructuredSelection && ((IStructuredSelection)s).size() == 1 ) {
                            Object context = ((IStructuredSelection)s).getFirstElement();
                            proxy.setContext( ( context instanceof ICDebugElement ) ? (ICDebugElement)context : target );
                        }
                        service.addDebugContextListener( proxy );
                    }
                    fMap.put( target, proxy );
                }
            }
        }
        return proxy;
    }

    private IDebugContextService getContextService() {
        IWorkbenchWindow window = SelectedResourceManager.getDefault().getActiveWindow();
        if ( window != null ) {
            IDebugContextManager manager = DebugUITools.getDebugContextManager();
            return manager.getContextService( window );
        }
        return null;
    }
}

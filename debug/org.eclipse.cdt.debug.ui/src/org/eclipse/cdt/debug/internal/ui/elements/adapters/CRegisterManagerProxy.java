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

package org.eclipse.cdt.debug.internal.ui.elements.adapters;

import org.eclipse.cdt.debug.core.model.ICDebugElement;
import org.eclipse.cdt.debug.core.model.ICDebugTarget;
import org.eclipse.cdt.debug.core.model.ICStackFrame;
import org.eclipse.cdt.debug.core.model.ICThread;
import org.eclipse.cdt.debug.core.model.ICVariable;
import org.eclipse.cdt.debug.internal.core.CRegisterManager;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.model.IRegister;
import org.eclipse.debug.core.model.IRegisterGroup;
import org.eclipse.debug.ui.contexts.DebugContextEvent;
import org.eclipse.debug.ui.contexts.IDebugContextListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

/**
 * Acts as a viewer input for the Registers view
 */
public class CRegisterManagerProxy implements IDebugEventSetListener, IDebugContextListener {

    private CRegisterManager fRegisterManager;
    private ICDebugElement fContext;
    private CRegisterManagerModelProxy fModelProxy;
    
    public CRegisterManagerProxy( CRegisterManager registerManager ) {
        super();
        fRegisterManager = registerManager;
    }

    public void dispose() {
        fRegisterManager = null;
        fContext = null;
        fModelProxy = null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.contexts.IDebugContextListener#debugContextChanged(org.eclipse.debug.ui.contexts.DebugContextEvent)
     */
    @Override
	public void debugContextChanged( DebugContextEvent event ) {
        ICDebugElement context = null;
        ISelection s = event.getContext();
        if ( s instanceof IStructuredSelection ) {
            IStructuredSelection selection = (IStructuredSelection)s;
            context = 
                ( selection.size() == 1 ) ?  
                    ( selection.getFirstElement() instanceof ICDebugElement ) ? 
                            (ICDebugElement)selection.getFirstElement() : null 
                    : null;
        }
        setContext( context );
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.core.IDebugEventSetListener#handleDebugEvents(org.eclipse.debug.core.DebugEvent[])
     */
    @Override
	public void handleDebugEvents( DebugEvent[] events ) {
        CRegisterManagerModelProxy modelProxy = getModelProxy();
        for( int i = 0; i < events.length; i++ ) {
            DebugEvent event = events[i];
            if ( !containsEvent( event ) )
                continue;
            Object source = event.getSource();
            if ( source instanceof ICDebugTarget 
                    && (( event.getKind() == DebugEvent.SUSPEND ) 
                            || event.getKind() == DebugEvent.TERMINATE ) ) {
                if ( modelProxy != null )
                    modelProxy.update();
            }
            else if ( source instanceof ICDebugTarget 
                    && (( event.getKind() == DebugEvent.CHANGE ) 
                            && event.getDetail() == DebugEvent.CONTENT ) ) {
                if ( modelProxy != null )
                    modelProxy.update();
            }
            else if ( source instanceof IRegisterGroup 
                    && event.getKind() == DebugEvent.CHANGE ) {
                if ( modelProxy != null )
                    modelProxy.update();
            }
            else if ( source instanceof IRegister 
                    && event.getKind() == DebugEvent.CHANGE ) {
                if ( modelProxy != null )
                    modelProxy.update();
            }
            else if ( source instanceof ICVariable 
                    && event.getKind() == DebugEvent.CHANGE ) {
                if ( modelProxy != null )
                    modelProxy.update();
            }
            else if ( source instanceof ICThread 
                    && event.getKind() == DebugEvent.SUSPEND ) {
                if ( modelProxy != null )
                    modelProxy.update();
            }
        }
    }

    public IRegisterGroup[] getRegisterGroups() {
        return fRegisterManager.getRegisterGroups();
    }

    public void setModelProxy( CRegisterManagerModelProxy modelProxy ) {
        fModelProxy = modelProxy;
    }
    
    public CRegisterManagerModelProxy getModelProxy() {
        return fModelProxy;
    }

    public String getModelIdentifier() {
        return fRegisterManager.getDebugTarget().getModelIdentifier();
    }

    public void setContext( ICDebugElement context ) {
        if ( fContext == null || !fContext.equals( context ) ) {
            fContext = context;
            try {
                fRegisterManager.setCurrentFrame( ( fContext instanceof ICStackFrame ) ? (ICStackFrame)context : null );
            }
            catch( DebugException e ) {
                // TODO: should we pass the error up?
            }
            CRegisterManagerModelProxy modelProxy = getModelProxy();
            if ( modelProxy != null )
                modelProxy.update();
        }
    }

    private boolean containsEvent( DebugEvent event ) {
        Object source = event.getSource();
        if ( source instanceof ICDebugElement ) {
            return fRegisterManager.getDebugTarget().equals( ((ICDebugElement)source).getDebugTarget() );
        }
        return false;
    }
}

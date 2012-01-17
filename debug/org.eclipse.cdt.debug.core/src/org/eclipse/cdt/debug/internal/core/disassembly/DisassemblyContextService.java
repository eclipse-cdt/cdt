/*******************************************************************************
 * Copyright (c) 2008 ARM Limited and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * ARM Limited - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.internal.core.disassembly;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.eclipse.cdt.debug.core.disassembly.IDisassemblyContextListener;
import org.eclipse.cdt.debug.core.disassembly.IDisassemblyContextService;
import org.eclipse.core.runtime.ListenerList;

public class DisassemblyContextService implements IDisassemblyContextService {

    private ListenerList fListeners;
    private Set<Object> fContexts;

    public DisassemblyContextService() {
        fContexts = new CopyOnWriteArraySet<Object>();
        fListeners = new ListenerList();
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.disassembly.IDisassemblyContextService#addDisassemblyContextListener(org.eclipse.cdt.debug.core.disassembly.IDisassemblyContextListener)
     */
    @Override
	public void addDisassemblyContextListener( IDisassemblyContextListener listener ) {
        fListeners.add( listener );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.disassembly.IDisassemblyContextService#removeDisassemblyContextListener(org.eclipse.cdt.debug.core.disassembly.IDisassemblyContextListener)
     */
    @Override
	public void removeDisassemblyContextListener( IDisassemblyContextListener listener ) {
        fListeners.remove( listener );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.disassembly.IDisassemblyContextService#register(java.lang.Object)
     */
    @Override
	public void register( Object context ) {
        fContexts.add( context );
        for( Object listener : fListeners.getListeners() ) {
            ((IDisassemblyContextListener)listener).contextAdded( context );
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.disassembly.IDisassemblyContextService#unregister(java.lang.Object)
     */
    @Override
	public void unregister( Object context ) {
        fContexts.remove( context );
        for( Object listener : fListeners.getListeners() ) {
            ((IDisassemblyContextListener)listener).contextRemoved( context );
        }
    }

    public void dispose() {
        for( Object context : fContexts ) {
            for( Object listener : fListeners.getListeners() ) {
                ((IDisassemblyContextListener)listener).contextRemoved( context );
            }
        }
        fListeners.clear();
        fContexts.clear();
    }
}

/*******************************************************************************
 * Copyright (c) 2006, 2009 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.ui.viewmodel.datamodel;

import java.util.concurrent.RejectedExecutionException;

import org.eclipse.cdt.dsf.concurrent.ThreadSafe;
import org.eclipse.cdt.dsf.datamodel.IDMEvent;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.ui.viewmodel.AbstractVMAdapter;

/** 
 * Base implementation for DSF-based view model adapters.
 * 
 * @since 1.0
 */
@ThreadSafe
abstract public class AbstractDMVMAdapter extends AbstractVMAdapter
{
    private final DsfSession fSession;

    /**
     * It is theoretically possible for a VM adapter to be disposed before it 
     * has a chance to register itself as event listener.  This flag is used
     * to avoid removing itself as listener in such situation.
     */
    private boolean fRegisteredAsEventListener = false;

    /**
     * Constructor for the View Model session.  It is tempting to have the 
     * adapter register itself here with the session as the model adapter, but
     * that would mean that the adapter might get accessed on another thread
     * even before the deriving class is fully constructed.  So it it better
     * to have the owner of this object register it with the session.
     * @param session
     */
    public AbstractDMVMAdapter(DsfSession session) {
        super();
        fSession = session;
        // Add ourselves as listener for DM events events.
        try {
            session.getExecutor().execute(new Runnable() {
                @Override
				public void run() {
                    if (DsfSession.isSessionActive(getSession().getId())) {
                        getSession().addServiceEventListener(AbstractDMVMAdapter.this, null);
                        fRegisteredAsEventListener = true;
                    }
                }
            });
        } catch (RejectedExecutionException e) {
            // Session shut down, not much we can do but wait to be disposed.
        }
    }    

    @Override
    public void dispose() {
        try {
            getSession().getExecutor().execute(new Runnable() {
                @Override
				public void run() {
                    if (fRegisteredAsEventListener && getSession().isActive()) {
                        fSession.removeServiceEventListener(AbstractDMVMAdapter.this);
                    }
                }
            });
        } catch (RejectedExecutionException e) {
            // Session shut down.
        }
        super.dispose();
    }

    /**
     * Returns the DSF session that this adapter is associated with.
     * @return
     */
    protected DsfSession getSession() { return fSession; }
    
    /**
     * Handle "data model changed" event by generating a delta object for each 
     * view and passing it to the corresponding view model provider.  The view
     * model provider is then responsible for filling-in and sending the delta
     * to the viewer.
     * 
     * @param event
     * 
     * @since 1.1
     */
    @DsfServiceEventHandler
    public final void eventDispatched(final IDMEvent<?> event) {
    	// We're in session's executor thread (session in which the event originated). 
        if (isDisposed()) return;

        handleEvent(event);
    }

}

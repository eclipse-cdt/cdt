/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.dd.dsf.debug.ui.viewmodel.launch;

import org.eclipse.dd.dsf.concurrent.DsfRunnable;
import org.eclipse.dd.dsf.concurrent.GetDataDone;
import org.eclipse.dd.dsf.concurrent.ThreadSafe;
import org.eclipse.dd.dsf.service.DsfSession;
import org.eclipse.dd.dsf.ui.viewmodel.IVMRootLayoutNode;
import org.eclipse.dd.dsf.ui.viewmodel.VMProvider;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.internal.ui.viewers.provisional.IModelDelta;


/**
 * 
 */
@SuppressWarnings("restriction")
public class LaunchViewModelProvider extends VMProvider 
    implements IDebugEventSetListener
{
    @ThreadSafe
    public LaunchViewModelProvider(DsfSession session, IVMRootLayoutNode rootLayoutNode) {
        super(session, rootLayoutNode);
        DebugPlugin.getDefault().addDebugEventListener(this);
    }
    
    
    public void handleDebugEvents(final DebugEvent[] events) {
        getSession().getExecutor().execute(new DsfRunnable() {
            public void run() {
                for (DebugEvent event : events) { 
                    handleDebugEvent(event); 
                }
            }            
        });
    }

    private void handleDebugEvent(DebugEvent event) {
        /*
         * Just like with DMC events, go through all the layout nodes and 
         * collect delta information for the received event.
         */
        if (getRootLayoutNode() != null && getRootLayoutNode().hasDeltaFlags(event)) {
            getRootLayoutNode().createDelta(event, new GetDataDone<IModelDelta>() {
                public void run() {
                    if (getStatus().isOK()) {
                        getModelProxy().fireModelChangedNonDispatch(getData());
                    }
                }
            });
        }
    }
    
    @Override
    public void dispose() {
        DebugPlugin.getDefault().removeDebugEventListener(this);
        super.dispose();
    }
}

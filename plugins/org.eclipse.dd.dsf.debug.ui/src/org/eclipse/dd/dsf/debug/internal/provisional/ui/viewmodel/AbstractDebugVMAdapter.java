/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.dd.dsf.debug.internal.provisional.ui.viewmodel;

import org.eclipse.dd.dsf.concurrent.DsfRunnable;
import org.eclipse.dd.dsf.debug.internal.provisional.ui.viewmodel.SteppingController.ISteppingControlParticipant;
import org.eclipse.dd.dsf.debug.service.IRunControl;
import org.eclipse.dd.dsf.debug.service.IRunControl.ISuspendedDMEvent;
import org.eclipse.dd.dsf.service.DsfSession;
import org.eclipse.dd.dsf.ui.viewmodel.IVMProvider;
import org.eclipse.dd.dsf.ui.viewmodel.datamodel.AbstractDMVMAdapter;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;

/**
 * Base class for VM adapters used for implementing a debugger integration.
 * 
 * @since 1.1
 */
@SuppressWarnings("restriction")
public class AbstractDebugVMAdapter extends AbstractDMVMAdapter
    implements ISteppingControlParticipant 
{
    
    public AbstractDebugVMAdapter(DsfSession session, final SteppingController controller) {
        super(session);
        fController = controller;
        fController.getExecutor().execute(new DsfRunnable() {
            public void run() {
                fController.addSteppingControlParticipant(AbstractDebugVMAdapter.this);
            }
        });
    }

    private final SteppingController fController;
    
	@Override
    protected IVMProvider createViewModelProvider(IPresentationContext context) {
        return null;
    }

    @Override
    public void doneHandleEvent(Object event) {
        if (event instanceof IRunControl.ISuspendedDMEvent) {
            final ISuspendedDMEvent suspendedEvent= (IRunControl.ISuspendedDMEvent) event;
            fController.getExecutor().execute(new DsfRunnable() {
                public void run() {
                    fController.doneStepping(suspendedEvent.getDMContext(), AbstractDebugVMAdapter.this);
                };
            });
        }
    }

    @Override
    public void dispose() {
    	if (!fController.getExecutor().isShutdown()) {
	        fController.getExecutor().execute(new DsfRunnable() {
	            public void run() {
	                fController.removeSteppingControlParticipant(AbstractDebugVMAdapter.this);
	            }
	        });
    	}
        super.dispose();
    }
}

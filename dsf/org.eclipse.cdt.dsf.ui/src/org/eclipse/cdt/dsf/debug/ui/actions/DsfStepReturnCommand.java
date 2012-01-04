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
package org.eclipse.cdt.dsf.debug.ui.actions;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.concurrent.ImmediateExecutor;
import org.eclipse.cdt.dsf.concurrent.Immutable;
import org.eclipse.cdt.dsf.debug.service.IRunControl.StepType;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.SteppingController;
import org.eclipse.cdt.dsf.internal.ui.DsfUIPlugin;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.debug.core.commands.IDebugCommandRequest;
import org.eclipse.debug.core.commands.IEnabledStateRequest;
import org.eclipse.debug.core.commands.IStepReturnHandler;

/**
 * 
 * @since 1.0
 */
@Immutable
public class DsfStepReturnCommand implements IStepReturnHandler {

    private final DsfExecutor fExecutor;
    private final DsfServicesTracker fTracker;
    
    public DsfStepReturnCommand(DsfSession session) {
        fExecutor = session.getExecutor();
        fTracker = new DsfServicesTracker(DsfUIPlugin.getBundleContext(), session.getId());
    }    

    public void dispose() {
        fTracker.dispose();
    }
    
    @Override
	public void canExecute(final IEnabledStateRequest request) {
        if (request.getElements().length != 1) {
            request.setEnabled(false);
            request.done();
            return;
        }
        
        fExecutor.submit(new DsfCommandRunnable(fTracker, request.getElements()[0], request) { 
            @Override public void doExecute() {
                SteppingController steppingControl = getSteppingController();
                if (steppingControl == null) {
                    request.setEnabled(false);
                    request.done();
                    return;
                }
            	steppingControl.canEnqueueStep(
                    getContext(), StepType.STEP_RETURN,
                    new DataRequestMonitor<Boolean>(ImmediateExecutor.getInstance(), null) {
                        @Override
                        protected void handleCompleted() {
                            request.setEnabled(isSuccess() && getData());
                            request.done();
                        }
                    });
            }
        });
    }
    
    @Override
	public boolean execute(final IDebugCommandRequest request) {
        if (request.getElements().length != 1) {
            request.done();
            return false;
        }
        
        fExecutor.submit(new DsfCommandRunnable(fTracker, request.getElements()[0], request) { 
            @Override public void doExecute() {
            	getSteppingController().enqueueStep(getContext(), StepType.STEP_RETURN);
            }
        });
        return true;
    }
}

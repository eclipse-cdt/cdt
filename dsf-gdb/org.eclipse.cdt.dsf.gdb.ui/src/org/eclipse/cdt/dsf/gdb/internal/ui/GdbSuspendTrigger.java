/*******************************************************************************
 * Copyright (c) 2008, 2010 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui;

import java.util.concurrent.RejectedExecutionException;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.concurrent.ImmediateDataRequestMonitor;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IProcesses;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService;
import org.eclipse.cdt.dsf.debug.ui.contexts.DsfSuspendTrigger;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;

/**
 * @since 2.1
 */
public class GdbSuspendTrigger extends DsfSuspendTrigger {
    
    public GdbSuspendTrigger(DsfSession session, ILaunch launch) {
        super(session, launch);
    }
    
    @Override
    protected void getLaunchTopContainers(final DataRequestMonitor<IContainerDMContext[]> rm) {
        try {
            getSession().getExecutor().execute(new DsfRunnable() {
                @Override
				public void run() {
                    IProcesses processService = getServicesTracker().getService(IProcesses.class);
                    ICommandControlService controlService = getServicesTracker().getService(ICommandControlService.class);
                    if (processService == null || controlService == null) {
                        rm.setStatus(new Status(IStatus.ERROR, GdbUIPlugin.PLUGIN_ID, IDsfStatusConstants.INVALID_STATE, "Not available", null)); //$NON-NLS-1$
                        rm.done();
                        return;
                    }

                    processService.getProcessesBeingDebugged(
                        controlService.getContext(),
                        new ImmediateDataRequestMonitor<IDMContext[]>(rm) {
                            @Override
                            public void handleSuccess() {
                                IContainerDMContext[] containers = new IContainerDMContext[getData().length];
                                for (int i = 0; i < containers.length; i++) {
                                    if (getData()[i] instanceof IContainerDMContext) {
                                        containers[i] = (IContainerDMContext)getData()[i];
                                    } else {
                                        // By convention the processes should be containers, but the API
                                        // does not enforce this.
                                        assert false;
                                        rm.setData(new IContainerDMContext[0]);
                                        rm.done();
                                        return;
                                    }
                                    
                                }
                                rm.setData(containers);
                                rm.done();
                            }
                        });
                }
            });
        } catch (RejectedExecutionException e) {
            rm.setStatus(new Status(IStatus.ERROR, GdbUIPlugin.PLUGIN_ID, IDsfStatusConstants.INVALID_STATE, "Not available", e)); //$NON-NLS-1$
            rm.done();
        }
    }
}

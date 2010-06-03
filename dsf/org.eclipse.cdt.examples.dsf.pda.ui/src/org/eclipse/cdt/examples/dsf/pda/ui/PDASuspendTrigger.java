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
package org.eclipse.cdt.examples.dsf.pda.ui;

import java.util.concurrent.RejectedExecutionException;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.debug.ui.contexts.DsfSuspendTrigger;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.examples.dsf.pda.service.PDACommandControl;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;

/**
 * @since 2.1
 */
public class PDASuspendTrigger extends DsfSuspendTrigger {
    
    public PDASuspendTrigger(DsfSession session, ILaunch launch) {
        super(session, launch);
    }
    
    @Override
    protected void getLaunchTopContainers(final DataRequestMonitor<IContainerDMContext[]> rm) {
        try {
            getSession().getExecutor().execute(new DsfRunnable() {
                public void run() {
                    PDACommandControl control = 
                        getServicesTracker().getService(PDACommandControl.class);
                    if (control != null) {
                        rm.setData(new IContainerDMContext[] { control.getContext() });
                    } else {
                        rm.setStatus(new Status(IStatus.ERROR, PDAUIPlugin.PLUGIN_ID, IDsfStatusConstants.INVALID_STATE, "Not available", null));
                    } 
                    rm.done();
                    
                }
            });
        } catch (RejectedExecutionException e) {
            rm.setStatus(new Status(IStatus.ERROR, PDAUIPlugin.PLUGIN_ID, IDsfStatusConstants.INVALID_STATE, "Not available", e));
            rm.done();
        }
    }
}

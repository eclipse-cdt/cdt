/*******************************************************************************
 * Copyright (c) 2009, 2010 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ericsson - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.breakpoint.actions;

import org.eclipse.cdt.debug.core.breakpointactions.IResumeActionEnabler;
import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;

/**
 * @since 3.0
 */
public class MIResumeActionEnabler implements IResumeActionEnabler {

    private final DsfExecutor fExecutor;
    private final DsfServicesTracker fServiceTracker;
    private final IExecutionDMContext fContext;

    public MIResumeActionEnabler(DsfExecutor executor, DsfServicesTracker serviceTracker, IDMContext context) {
        fExecutor = executor;
        fServiceTracker = serviceTracker;
        fContext = (IExecutionDMContext) context;
    }

    public void resume() throws Exception {
    	fExecutor.execute(new DsfRunnable() { 
    		public void run() {
    			final IRunControl runControlService = fServiceTracker.getService(IRunControl.class);
    			if (runControlService != null) {
    				runControlService.resume(fContext, new RequestMonitor(fExecutor, null));
    			}	
    		}
    	});
    }
}

/*******************************************************************************
 * Copyright (c) 2008 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ericsson - Initial API and implementation
 *******************************************************************************/

package org.eclipse.dd.mi.service.breakpoint.actions;

import org.eclipse.cdt.debug.core.breakpointactions.IResumeActionEnabler;
import org.eclipse.dd.dsf.concurrent.DsfExecutor;
import org.eclipse.dd.dsf.concurrent.RequestMonitor;
import org.eclipse.dd.dsf.datamodel.IDMContext;
import org.eclipse.dd.dsf.debug.service.IRunControl;
import org.eclipse.dd.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.dd.dsf.service.DsfServicesTracker;

public class MIResumeActionEnabler implements IResumeActionEnabler {

    @SuppressWarnings("unused")
    private final DsfExecutor fExecutor;
    private final DsfServicesTracker fServiceTracker;
    private final IExecutionDMContext fContext;

    public MIResumeActionEnabler(DsfExecutor executor, DsfServicesTracker serviceTracker, IDMContext context) {
        fExecutor = executor;
        fServiceTracker = serviceTracker;
        // FIXME: Although it looks optimistic, we know it works
        // How can we guarantee that it always will?
        fContext = (IExecutionDMContext) context;
    }

    public void resume() throws Exception {
        final IRunControl runControlService = fServiceTracker.getService(IRunControl.class);
        if (runControlService != null) {
            runControlService.resume(fContext, new RequestMonitor(fExecutor, null));
        }
    }

}

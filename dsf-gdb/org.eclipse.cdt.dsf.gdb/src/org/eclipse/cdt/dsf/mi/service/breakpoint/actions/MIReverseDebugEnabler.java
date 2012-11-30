/*******************************************************************************
 * Copyright (c) 2008, 2012 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ericsson - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.breakpoint.actions;

import org.eclipse.cdt.debug.core.breakpointactions.IReverseDebugEnabler;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.cdt.dsf.gdb.service.IReverseRunControl;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;


/**
 * 
 * This class permits to enable, disable or toggle the reverse
 * debugging mode.
 *   
 * @since 4.2
 */
public class MIReverseDebugEnabler  implements IReverseDebugEnabler {
    private final DsfExecutor fExecutor;
    private final DsfServicesTracker fServiceTracker;
    private final ICommandControlDMContext fContext;
    private static enum REVERSE_DEBUG_MODE {ENABLE, DISABLE, TOGGLE};

    /**
     * @param executor
     * @param serviceTracker
     * @param context
     */
    public MIReverseDebugEnabler(DsfExecutor executor, DsfServicesTracker serviceTracker, IDMContext context) {
        fExecutor = executor;
        fServiceTracker = serviceTracker;
        fContext = DMContexts.getAncestorOfType(context, ICommandControlDMContext.class);
        assert fContext != null;
    }

	@Override
	public void enable() throws Exception {
		setMode(REVERSE_DEBUG_MODE.ENABLE);
	}

	@Override
	public void disable() throws Exception {
		setMode(REVERSE_DEBUG_MODE.DISABLE);
	}
	
	@Override
	public void toggle() throws Exception {
		setMode(REVERSE_DEBUG_MODE.TOGGLE);
	}

	private void setMode(final REVERSE_DEBUG_MODE enable) throws Exception {
		final REVERSE_DEBUG_MODE mode = enable;
		
		fExecutor.execute(new DsfRunnable() { 
    		@Override
    		public void run() {
    			final IReverseRunControl runControl = fServiceTracker.getService(IReverseRunControl.class);
    			final DataRequestMonitor<Boolean> rm = new DataRequestMonitor<Boolean>(fExecutor, null);
    			if (runControl != null) {
    				runControl.isReverseModeEnabled(fContext, new DataRequestMonitor<Boolean>(fExecutor, rm) {
       					@Override
       					public void handleSuccess() {
       						if(mode.equals(REVERSE_DEBUG_MODE.TOGGLE) ) {
       							// Toggle whatever the current state is 
       							runControl.enableReverseMode(fContext, !getData(), rm);
       						}
       					    // Set only if different from current state
       						else {
       							Boolean enabled = getData();
       							if ( (enabled.equals(false) && mode.equals(REVERSE_DEBUG_MODE.ENABLE) ) || 
       								 (enabled.equals(true) && mode.equals(REVERSE_DEBUG_MODE.DISABLE) ))
       							{
       								runControl.enableReverseMode(fContext, !enabled, rm);
       							}
       						}
       						rm.done();
       					}
       				});
    			}
    		}
    	});
	}
	
}

/*******************************************************************************
 * Copyright (c) 2015 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marc Khouzam (Ericsson) - Initial API and implementation 
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.service;

import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.eclipse.cdt.debug.core.model.ICBreakpoint;
import org.eclipse.cdt.dsf.concurrent.ImmediateRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointDMContext;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointsTargetDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IStartedDMEvent;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.cdt.dsf.mi.service.MIBreakpointsManager;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IBreakpoint;

/**
 * Version of BreakpointsManager for GDB version starting with 7.0.
 * 
 * @since 4.7
 */
public class GDBBreakpointsManager_7_0 extends MIBreakpointsManager {
    private String fDebugModelId;

	public GDBBreakpointsManager_7_0(DsfSession session, String debugModelId) {
		super(session, debugModelId);
		fDebugModelId = debugModelId;
	}

	@Override
	public void initialize(final RequestMonitor rm) {
		super.initialize(new ImmediateRequestMonitor(rm) {
			@Override
			protected void handleSuccess() {
				doInitialize(rm);
			}
		});
	}

	private void doInitialize(final RequestMonitor rm) {
		register(new String[] { GDBBreakpointsManager_7_0.class.getName() },
				 new Hashtable<String, String>());

		rm.done();
	}

    public void updateContextOnStartEvent(IStartedDMEvent e) {
    	
    	if (e.getDMContext() instanceof IContainerDMContext) {
    		IContainerDMContext container = (IContainerDMContext)e.getDMContext();
    		if (getPlatformToAttributesMaps().size() == 1) {
    			for (IBreakpointsTargetDMContext target : getPlatformToAttributesMaps().keySet()) {
    				if (target instanceof IContainerDMContext) {
    					if (!container.equals(target)) {
    						Map<ICBreakpoint,Map<String, Object>> platformBPs = getPlatformToAttributesMaps().remove(target);
    						getPlatformToAttributesMaps().put((IBreakpointsTargetDMContext)container, platformBPs);

    						Map<ICBreakpoint, Vector<IBreakpointDMContext>> breakpointIDs = getPlatformToBPsMaps().get(target);
    						getPlatformToBPsMaps().put((IBreakpointsTargetDMContext)container, breakpointIDs);

    						Map<IBreakpointDMContext, ICBreakpoint> targetIDs = getBPToPlatformMaps().get(target);
    						getBPToPlatformMaps().put((IBreakpointsTargetDMContext)container, targetIDs);

    						Map<ICBreakpoint, Set<String>> threadIDs = getPlatformToBPThreadsMaps().get(target);
    						getPlatformToBPThreadsMaps().put((IBreakpointsTargetDMContext)container, threadIDs);

    						IBreakpoint[] breakpoints = DebugPlugin.getDefault().getBreakpointManager().getBreakpoints(fDebugModelId);
    						for (IBreakpoint breakpoint : breakpoints) {
    							if (breakpoint instanceof ICBreakpoint && supportsBreakpoint(breakpoint)) {
    								try {
    									IContainerDMContext[] procTargets = getFilterExtension((ICBreakpoint)breakpoint).getTargetFilters();
    									for (IContainerDMContext procDmc : procTargets) {
    										ICommandControlDMContext bpControlDmc = DMContexts.getAncestorOfType(procDmc, ICommandControlDMContext.class);
    										ICommandControlDMContext sessionControlDmc = DMContexts.getAncestorOfType(container, ICommandControlDMContext.class);

    										if (sessionControlDmc.equals(bpControlDmc)) {
    											setTargetFilter((ICBreakpoint)breakpoint, container);
    										}
    									}
    								} catch (CoreException exception) {
    								}
    							}
    						}
    					}
    				}
    			}
    		}
    	}
	}
}

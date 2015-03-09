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

import org.eclipse.cdt.debug.core.model.ICBreakpoint;
import org.eclipse.cdt.dsf.concurrent.ImmediateRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.debug.service.IDsfBreakpointExtension;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointsTargetDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IStartedDMEvent;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.cdt.dsf.mi.service.MIBreakpointsManager;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
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

	@DsfServiceEventHandler
    public void eventDispatched_7_0(IStartedDMEvent e) {
		// With GDB 7.0 and 7.1, the pid of the process is used by GDB
		// as the thread-group id.  This is a problem because the pid does
		// not exist when we create breakpoints.
		// We are then having a problem when we try to compare a context
		// where the pid was set with the ones stored in this service which
		// don't have the pid.
		// What we do here is update all local contexts with the newly created
		// pid when we get the start event for the process.
		// Note that we don't support multi-process for GDB 7.0 and 7.1, so it
		// simplifies things.
    	updateContextOnStartEvent(e);
    }  

	protected void updateContextOnStartEvent(IStartedDMEvent e) {
		if (e.getDMContext() instanceof IContainerDMContext) {
			// Process created.
			IContainerDMContext containerWithPid = (IContainerDMContext)e.getDMContext();
			
			assert getPlatformToAttributesMaps().keySet().size() == 1;  // Only one process for GDB 7.0 and 7.1
			for (IBreakpointsTargetDMContext oldBpTarget : getPlatformToAttributesMaps().keySet()) {
				assert oldBpTarget instanceof IContainerDMContext;
				assert !containerWithPid.equals(oldBpTarget);  // BpTarget does not have pid, while new container does

				// Replace all BpTarget entries with the new container context containing the pid
				IBreakpointsTargetDMContext newBpTarget = (IBreakpointsTargetDMContext)containerWithPid;
				updateBpManagerMaps(newBpTarget, oldBpTarget);
				
				// Replace all target filters of this session with the new container context containing the pid
				updateTargetFilters(containerWithPid);
			}
		}
	}
	
	/**
	 * Updates all the maps of the MIBreakpointsManager to replace the old IBreakpointsTargetDMContext which
	 * does not have the pid, with the new one with does have the pid.
	 */
	private void updateBpManagerMaps(IBreakpointsTargetDMContext newBpTarget, IBreakpointsTargetDMContext oldBpTarget) {
		getPlatformToAttributesMaps().put(newBpTarget, getPlatformToAttributesMaps().remove(oldBpTarget));
		getPlatformToBPsMaps().put(newBpTarget, getPlatformToBPsMaps().get(oldBpTarget));
		getBPToPlatformMaps().put(newBpTarget, getBPToPlatformMaps().get(oldBpTarget));
		getPlatformToBPThreadsMaps().put(newBpTarget, getPlatformToBPThreadsMaps().get(oldBpTarget));
	}
	
	/**
	 * Updates all the target filter for this session to replace the old IBreakpointsTargetDMContext which
	 * does not have the pid, with the new one with does have the pid.
	 */
	private void updateTargetFilters(IContainerDMContext newContainer) {
		IBreakpoint[] breakpoints = DebugPlugin.getDefault().getBreakpointManager().getBreakpoints(fDebugModelId);
		for (IBreakpoint breakpoint : breakpoints) {
			if (breakpoint instanceof ICBreakpoint && supportsBreakpoint(breakpoint)) {
				try {
					IDsfBreakpointExtension filterExt = getFilterExtension((ICBreakpoint)breakpoint);

					IContainerDMContext[] filterContainers = filterExt.getTargetFilters();
					for (IContainerDMContext oldContainer : filterContainers) {
						// For each target filter, replace it if it is from our session
						ICommandControlDMContext controldForOld = DMContexts.getAncestorOfType(oldContainer, ICommandControlDMContext.class);
						ICommandControlDMContext controlForNew = DMContexts.getAncestorOfType(newContainer, ICommandControlDMContext.class);

						if (controldForOld.equals(controlForNew)) {
							filterExt.removeTargetFilter(oldContainer);
							filterExt.setTargetFilter(newContainer);
						}
					}
				} catch (CoreException exception) {
				}
			}
		}
	}
}

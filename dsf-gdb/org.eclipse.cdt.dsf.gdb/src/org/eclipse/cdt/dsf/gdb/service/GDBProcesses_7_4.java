/*******************************************************************************
 * Copyright (c) 2014 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Marc Khouzam (Ericsson) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.service;

import java.util.Map;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Sequence;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointsTargetDMContext;
import org.eclipse.cdt.dsf.debug.service.IMemory.IMemoryDMContext;
import org.eclipse.cdt.dsf.mi.service.IMIContainerDMContext;
import org.eclipse.cdt.dsf.service.DsfSession;

/**
 * Adapt to GDB 7.4 where breakpoints are for all inferiors at once.
 * 
 * @since 4.4
 */
public class GDBProcesses_7_4 extends GDBProcesses_7_2_1 {
    
	public GDBProcesses_7_4(DsfSession session) {
		super(session);
	}

	/**
	 * A container context that is not an IBreakpointsTargetDMContext.
	 */
	private static class GDBContainerDMC_7_4 extends MIContainerDMC 
	implements IMemoryDMContext
	{
		public GDBContainerDMC_7_4(String sessionId, IProcessDMContext processDmc, String groupId) {
			super(sessionId, processDmc, groupId);
		}
	}
	
	@Override
	protected Sequence getDebugNewProcessSequence(DsfExecutor executor, boolean isInitial, IDMContext dmc, String file, 
												  Map<String, Object> attributes, DataRequestMonitor<IDMContext> rm) {
		return new DebugNewProcessSequence_7_4(executor, isInitial, dmc, file, attributes, rm);
	}
	
	@Override
    public IMIContainerDMContext createContainerContext(IProcessDMContext processDmc,
    													String groupId) {
    	return new GDBContainerDMC_7_4(getSession().getId(), processDmc, groupId);
    }
	
	@Override
	void startTtrackingBreakpoints(IBreakpointsTargetDMContext bpTargetDmc, RequestMonitor rm) {
		if (isInitialProcess()) {
			// Starting with GDB 7.4, breakpoints are global and should only be set once.
			// Therefore, we only start tracking breakpoints when we are dealing with the
			// first inferior.
			super.startTtrackingBreakpoints(bpTargetDmc, rm);
		} else {
			rm.done();
		}
	}
}


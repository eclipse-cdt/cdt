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
import org.eclipse.cdt.dsf.datamodel.IDMContext;

/**
 * With GDB 7.4, breakpoints are again global instead of per-process,
 * so we remove that step from the sequence.
 * 
 * @since 4.4
 */
public class DebugNewProcessSequence_7_4 extends DebugNewProcessSequence_7_2 {

	private boolean fIsInitialProcess;
	
	public DebugNewProcessSequence_7_4(
			DsfExecutor executor, 
			boolean isInitial, 
			IDMContext dmc, 
			String file, 
			Map<String, Object> attributes, 
			DataRequestMonitor<IDMContext> rm) {
		super(executor, isInitial, dmc, file, attributes, rm);
		
		fIsInitialProcess = isInitial;
	}
	
	@Override
	@Execute
	public void stepStartTrackingBreakpoints(RequestMonitor rm) {
		if (fIsInitialProcess) {
			// Breakpoints are now global to all of GDB.
			// We therefore only need to start tracking them once.
			// We could do this in a FinalLaunchSequence, but that would
			// happen before we load the binary and we'd get a bunch of
			// error printouts.  Instead, if we do it here, the binary
			// is loaded already.
			super.stepStartTrackingBreakpoints(rm);
		} else {
			rm.done();
		}
	}
}

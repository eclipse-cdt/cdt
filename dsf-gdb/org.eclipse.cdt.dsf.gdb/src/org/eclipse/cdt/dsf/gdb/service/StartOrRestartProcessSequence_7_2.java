/*******************************************************************************
 * Copyright (c) 2012 Mentor Graphics and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Mentor Graphics - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.service;

import java.util.Map;

import org.eclipse.cdt.debug.core.CDebugUtils;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.gdb.IGDBLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;

/**
 * Implements support for multi-process debugging.
 * 
 * @since 4.2
 */
public class StartOrRestartProcessSequence_7_2 extends StartOrRestartProcessSequence_7_0 {

	private final Map<String, Object> fAttributes;

	public StartOrRestartProcessSequence_7_2(
			DsfExecutor executor, 
			IContainerDMContext containerDmc, 
			Map<String, Object> attributes, 
			boolean restart,
			DataRequestMonitor<IContainerDMContext> rm) {
		super(executor, containerDmc, attributes, restart, rm);
		fAttributes = attributes;
	}

	@Override
	protected boolean useContinueCommand() {
    	// Note that restart does not apply to remote sessions
		DsfServicesTracker tracker = new DsfServicesTracker(GdbPlugin.getBundleContext(), getContainerContext().getSessionId());
    	IGDBBackend backend = tracker.getService(IGDBBackend.class);
    	tracker.dispose();
		if (backend == null) {
			return false;
		}
		
    	// When doing remote non-attach debugging, we use -exec-run instead of -exec-continue
		// if gdbserver is running in the daemon mode (--multi).
		// For remote attach, if we get here it is that we are starting a new process
		// (multi-process), so we want to use -exec-run
		if (backend.getSessionType() == SessionType.REMOTE && !backend.getIsAttachSession()) {
			// Use '-exec-run' if gdbserver is running in daemon mode
			return !CDebugUtils.getAttribute(
						fAttributes, 
						IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_REMOTE_MULTI, 
						IGDBLaunchConfigurationConstants.DEBUGGER_REMOTE_MULTI_DEFAULT);
		}
		// For remote attach, if we get here it is that we are starting a new process
		// (multi-process), so we want to use -exec-run
    	return backend.getSessionType() == SessionType.REMOTE && !backend.getIsAttachSession();
	}
}

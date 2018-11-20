/*******************************************************************************
 * Copyright (c) 2015, 2016 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Intel Corporation - Added Reverse Debugging BTrace support
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.service;

import java.util.Map;

import org.eclipse.cdt.debug.core.model.IChangeReverseMethodHandler.ReverseDebugMethod;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Sequence;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.cdt.dsf.gdb.IGDBLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.gdb.IGdbDebugPreferenceConstants;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;

/**
 * Adding support for reverse trace method selection with GDB 7.10
 *
 * @since 5.0
 */
public class GDBProcesses_7_10 extends GDBProcesses_7_4 {

	public GDBProcesses_7_10(DsfSession session) {
		super(session);
	}

	@Override
	protected Sequence getStartOrRestartProcessSequence(DsfExecutor executor, IContainerDMContext containerDmc,
			Map<String, Object> attributes, boolean restart, DataRequestMonitor<IContainerDMContext> rm) {
		return new StartOrRestartProcessSequence_7_10(executor, containerDmc, attributes, restart, rm);
	}

	@Override
	protected void doReverseDebugStep(IProcessDMContext procCtx, RequestMonitor rm) {
		// Select reverse debugging mode to what was enabled as a launch option
		IReverseRunControl2 reverseService = getServicesTracker().getService(IReverseRunControl2.class);
		if (reverseService != null) {
			ILaunch launch = procCtx.getAdapter(ILaunch.class);
			if (launch != null) {
				try {
					boolean reverseEnabled = launch.getLaunchConfiguration().getAttribute(
							IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_REVERSE,
							IGDBLaunchConfigurationConstants.DEBUGGER_REVERSE_DEFAULT);
					if (reverseEnabled) {
						ICommandControlDMContext controlContext = DMContexts.getAncestorOfType(procCtx,
								ICommandControlDMContext.class);
						String reverseMode = launch.getLaunchConfiguration().getAttribute(
								IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_REVERSE_MODE,
								IGDBLaunchConfigurationConstants.DEBUGGER_REVERSE_MODE_DEFAULT);
						if (reverseMode.equals(IGDBLaunchConfigurationConstants.DEBUGGER_REVERSE_MODE_SOFTWARE)) {
							reverseService.enableReverseMode(controlContext, ReverseDebugMethod.SOFTWARE, rm);
						} else if (reverseMode
								.equals(IGDBLaunchConfigurationConstants.DEBUGGER_REVERSE_MODE_HARDWARE)) {
							String defaultValue = Platform.getPreferencesService().getString(GdbPlugin.PLUGIN_ID,
									IGdbDebugPreferenceConstants.PREF_REVERSE_TRACE_METHOD_HARDWARE,
									IGdbDebugPreferenceConstants.PREF_REVERSE_TRACE_METHOD_GDB_TRACE, null);

							ReverseDebugMethod traceMethod = ReverseDebugMethod.GDB_TRACE;
							if (defaultValue
									.equals(IGdbDebugPreferenceConstants.PREF_REVERSE_TRACE_METHOD_BRANCH_TRACE)) {
								traceMethod = ReverseDebugMethod.BRANCH_TRACE;
							} else if (defaultValue
									.equals(IGdbDebugPreferenceConstants.PREF_REVERSE_TRACE_METHOD_PROCESSOR_TRACE)) {
								traceMethod = ReverseDebugMethod.PROCESSOR_TRACE;
							}

							reverseService.enableReverseMode(controlContext, traceMethod, rm);
						} else {
							rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INTERNAL_ERROR,
									"Unexpected Reverse debugging mode " + reverseMode, null)); //$NON-NLS-1$
						}
					} else {
						rm.done();
					}
				} catch (CoreException e) {
					// Ignore, just don't set reverse method
					rm.done();
				}
			} else {
				// Ignore, just don't set reverse method
				rm.done();
			}
		} else {
			// If we don't have an IReverseRunControl2 service, fall-back to our previous behavior
			super.doReverseDebugStep(procCtx, rm);
		}
	}
}

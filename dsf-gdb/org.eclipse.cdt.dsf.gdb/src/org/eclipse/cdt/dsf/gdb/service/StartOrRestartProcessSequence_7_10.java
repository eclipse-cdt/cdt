/*******************************************************************************
 * Copyright (c) 2015 Intel Corporation and others.
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.debug.core.CDebugUtils;
import org.eclipse.cdt.debug.core.model.IChangeReverseMethodHandler.ReverseDebugMethod;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.concurrent.ImmediateRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.gdb.IGDBLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.gdb.IGdbDebugPreferenceConstants;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.gdb.service.command.IGDBControl;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

/**
 * @since 5.0
 */
public class StartOrRestartProcessSequence_7_10 extends StartOrRestartProcessSequence_7_3 {

	private IGDBControl fCommandControl;
	private IReverseRunControl2 fReverseService;
	private ReverseDebugMethod fReverseMode = ReverseDebugMethod.SOFTWARE;
	private final Map<String, Object> fAttributes;

	public StartOrRestartProcessSequence_7_10(DsfExecutor executor, IContainerDMContext containerDmc,
			Map<String, Object> attributes, boolean restart, DataRequestMonitor<IContainerDMContext> rm) {
		super(executor, containerDmc, attributes, restart, rm);

		fAttributes = attributes;
	}

	@Override
	protected String[] getExecutionOrder(String group) {
		if (GROUP_TOP_LEVEL.equals(group)) {
			// Initialize the list with the base class' steps
			// We need to create a list that we can modify, which is why we create our own ArrayList.
			List<String> orderList = new ArrayList<>(Arrays.asList(super.getExecutionOrder(GROUP_TOP_LEVEL)));

			// Insert the new stepSetReverseMode after stepSetReverseOff
			orderList.add(orderList.indexOf("stepSetReverseOff") + 1, "stepSetReverseMode"); //$NON-NLS-1$ //$NON-NLS-2$

			return orderList.toArray(new String[orderList.size()]);
		}

		return null;
	}

	/**
	 * Initialize the members of the StartOrRestartProcessSequence_7_10 class.
	 * This step is mandatory for the rest of the sequence to complete.
	 */
	@Override
	@Execute
	public void stepInitializeBaseSequence(final RequestMonitor rm) {
		super.stepInitializeBaseSequence(new ImmediateRequestMonitor(rm) {
			@Override
			protected void handleSuccess() {
				DsfServicesTracker tracker = new DsfServicesTracker(GdbPlugin.getBundleContext(),
						getContainerContext().getSessionId());
				fCommandControl = tracker.getService(IGDBControl.class);
				fReverseService = tracker.getService(IReverseRunControl2.class);
				tracker.dispose();

				if (fReverseService != null) {

					// Here we check for the reverse mode to be used for launching the reverse
					// debugging service.
					String reverseMode = CDebugUtils.getAttribute(fAttributes,
							IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_REVERSE_MODE,
							IGDBLaunchConfigurationConstants.DEBUGGER_REVERSE_MODE_DEFAULT);

					if (reverseMode.equals(IGDBLaunchConfigurationConstants.DEBUGGER_REVERSE_MODE_HARDWARE)) {
						String hwTracePref = Platform.getPreferencesService().getString(GdbPlugin.PLUGIN_ID,
								IGdbDebugPreferenceConstants.PREF_REVERSE_TRACE_METHOD_HARDWARE,
								IGdbDebugPreferenceConstants.PREF_REVERSE_TRACE_METHOD_GDB_TRACE, null);

						if (hwTracePref.equals(IGdbDebugPreferenceConstants.PREF_REVERSE_TRACE_METHOD_BRANCH_TRACE)) {
							fReverseMode = ReverseDebugMethod.BRANCH_TRACE;
						} else if (hwTracePref
								.equals(IGdbDebugPreferenceConstants.PREF_REVERSE_TRACE_METHOD_PROCESSOR_TRACE)) {
							fReverseMode = ReverseDebugMethod.PROCESSOR_TRACE;
						} else {
							fReverseMode = ReverseDebugMethod.GDB_TRACE;
						}
					} else if (reverseMode.equals(IGDBLaunchConfigurationConstants.DEBUGGER_REVERSE_MODE_SOFTWARE)) {
						fReverseMode = ReverseDebugMethod.SOFTWARE;
					} else {
						rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, IDsfStatusConstants.INTERNAL_ERROR,
								"Unexpected reverse debugging type: " + reverseMode, null)); //$NON-NLS-1$
					}
				}

				rm.done();
			}
		});
	}

	/**
	 * @since 5.2
	 */
	protected ReverseDebugMethod getReverseMode() {
		return fReverseMode;
	}

	/**
	 * Here we set the reverse debug mode
	 */
	@Execute
	public void stepSetReverseMode(RequestMonitor rm) {
		if (getReverseEnabled() && fReverseService != null) {
			fReverseService.enableReverseMode(fCommandControl.getContext(), fReverseMode, rm);
		} else {
			rm.done();
		}
	}
}

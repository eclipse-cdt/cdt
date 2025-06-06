/*******************************************************************************
 * Copyright (c) 2016, 2025 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 * Contributors:
 *     Ericsson - Initial implementation for LLDB
 *     John Dallaway - Avoid unsupported MI commands (#1186)
 *******************************************************************************/

package org.eclipse.cdt.llvm.dsf.lldb.core.internal.launching;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitorWithProgress;
import org.eclipse.cdt.dsf.gdb.launching.FinalLaunchSequence_7_2;
import org.eclipse.cdt.dsf.gdb.service.command.IGDBControl;
import org.eclipse.cdt.dsf.mi.service.command.output.CLIVersionInfo;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.llvm.dsf.lldb.core.internal.LLDBCorePlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

/**
 * A LLDB-specific launch sequence that was initially created to work around the
 * fact that LLDB always has to run in async mode, even in all-stop.
 */
public class LLDBFinalLaunchSequence extends FinalLaunchSequence_7_2 {

	private IGDBControl fCommandControl;

	/**
	 * Constructs the {@link LLDBFinalLaunchSequence}.
	 *
	 * @param session
	 *            The debugging session
	 * @param attributes
	 *            the launch configuration attributes
	 * @param rm
	 *            a request monitor that will indicate when the sequence is
	 *            completed
	 */
	public LLDBFinalLaunchSequence(DsfSession session, Map<String, Object> attributes, RequestMonitorWithProgress rm) {
		super(session, attributes, rm);
	}

	@Override
	protected String[] getExecutionOrder(String group) {
		if (GROUP_TOP_LEVEL.equals(group)) {
			List<String> orderList = new ArrayList<>(Arrays.asList(super.getExecutionOrder(GROUP_TOP_LEVEL)));

			orderList.add(orderList.indexOf("stepInitializeFinalLaunchSequence_7_2") + 1, //$NON-NLS-1$
					"stepInitializeLLDBFinalLaunchSequence"); //$NON-NLS-1$

			// Remove or replace steps that are not supported by LLDB-MI
			orderList.set(orderList.indexOf("stepGDBVersion"), "stepVersion"); //$NON-NLS-1$ //$NON-NLS-2$
			orderList.remove(orderList.indexOf("stepSetNonStop")); //$NON-NLS-1$
			orderList.remove(orderList.indexOf("stepSetPrintObject")); //$NON-NLS-1$

			return orderList.toArray(new String[orderList.size()]);
		}

		return null;
	}

	@Execute
	public void stepInitializeLLDBFinalLaunchSequence(RequestMonitor requestMonitor) {
		BundleContext bundleContext = FrameworkUtil.getBundle(LLDBFinalLaunchSequence.class).getBundleContext();
		DsfServicesTracker tracker = new DsfServicesTracker(bundleContext, getSession().getId());
		fCommandControl = tracker.getService(IGDBControl.class);
		tracker.dispose();
		if (fCommandControl == null) {
			requestMonitor.setStatus(
					new Status(IStatus.ERROR, LLDBCorePlugin.PLUGIN_ID, -1, "Cannot obtain control service", null)); //$NON-NLS-1$
			requestMonitor.done();
			return;
		}
		requestMonitor.done();
	}

	/**
	 * Print the version of LLDB reported by LLDB-MI.
	 */
	@Execute
	public void stepVersion(final RequestMonitor requestMonitor) {
		fCommandControl.queueCommand(fCommandControl.getCommandFactory().createCLIVersion(fCommandControl.getContext()),
				new DataRequestMonitor<CLIVersionInfo>(getExecutor(), requestMonitor) {
					@Override
					protected void handleCompleted() {
						// Accept failures
						requestMonitor.done();
					}
				});
	}

}

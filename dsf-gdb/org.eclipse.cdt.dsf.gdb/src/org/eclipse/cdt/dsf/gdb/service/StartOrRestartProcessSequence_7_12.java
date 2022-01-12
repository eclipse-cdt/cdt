/*******************************************************************************
 * Copyright (c) 2016 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.concurrent.ImmediateRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.mi.service.command.output.MIBreakpoint;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;

/**
 * @since 5.2
 */
public class StartOrRestartProcessSequence_7_12 extends StartOrRestartProcessSequence_7_10 {
	private GDBRunControl_7_12 fReverseService;

	public StartOrRestartProcessSequence_7_12(DsfExecutor executor, IContainerDMContext containerDmc,
			Map<String, Object> attributes, boolean restart, DataRequestMonitor<IContainerDMContext> rm) {
		super(executor, containerDmc, attributes, restart, rm);
	}

	/**
	 * Initialize the members of the StartOrRestartProcessSequence_7_12 class.
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
				fReverseService = tracker.getService(GDBRunControl_7_12.class);
				tracker.dispose();
				rm.done();
			}
		});
	}

	@Override
	protected String[] getExecutionOrder(String group) {
		if (GROUP_TOP_LEVEL.equals(group)) {
			// Initialize the list with the base class' steps
			// We need to create a list that we can modify, which is why we create our own ArrayList.
			List<String> orderList = new ArrayList<>(Arrays.asList(super.getExecutionOrder(GROUP_TOP_LEVEL)));

			// Need to insert reverse mode off before ordering the reverse start at a specified location
			orderList.add(orderList.indexOf("stepCreateConsole") + 1, "stepSetReverseOff2"); //$NON-NLS-1$ //$NON-NLS-2$

			// Order the activation of reverse debugging before starting the program, it will be executed once the
			// program stops at the specified location.
			orderList.add(orderList.indexOf("stepSetReverseOff2") + 1, "stepSetReverseModeAtLocation"); //$NON-NLS-1$ //$NON-NLS-2$

			return orderList.toArray(new String[orderList.size()]);
		}

		return null;
	}

	@Execute
	public void stepSetReverseOff2(RequestMonitor rm) {
		super.stepSetReverseOff(rm);
	}

	/**
	 * Request the enabling of reverse debugging, it will be applied once the program
	 * stops at the breakpoint inserted for reverse debugging
	 */
	@Execute
	public void stepSetReverseModeAtLocation(final RequestMonitor rm) {
		MIBreakpoint bp = getBreakPointForReverse();
		if (getReverseEnabled() && fReverseService != null && bp != null) {
			// Order to continue execution if there is no user break point inserted at main
			fReverseService.enableReverseModeAtBpLocation(getContainerContext(), getReverseMode(), bp,
					!getUserBreakpointIsOnMain());
		}
		rm.done();
	}

	/*
	 * We have scheduled the start of reverse debug, so we shall skip this method from super class
	 */
	@Override
	@Execute
	public void stepSetReverseMode(RequestMonitor rm) {
		rm.done();
	}

	/*
	 * We have scheduled the start of reverse debug, so we shall skip this method from super class
	 */
	@Override
	@Execute
	public void stepEnableReverse(RequestMonitor rm) {
		rm.done();
	}

	/*
	 * The order to continue or not has been included with the order to start reverse debugging at a specific location
	 * so we shall skip this method from super class
	 */
	@Override
	@Execute
	public void stepContinue(RequestMonitor rm) {
		rm.done();
	}

}

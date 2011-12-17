/*******************************************************************************
 * Copyright (c) 2011 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.concurrent.ImmediateDataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.gdb.service.command.IGDBControl;
import org.eclipse.cdt.dsf.mi.service.command.output.MIAddInferiorInfo;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * With GDB 7.2, to create a new process, we need to use the -add-inferior command first.
 * This allows to create multiple processes, unlike previous versions of GDB.
 * Note that GDB 7.1 does support multi-process but didn't have the MI commands (e.g., -add-inferior)
 * so we only support multi-process starting with 7.2
 * 
 * @since 4.0
 */
public class DebugNewProcessSequence_7_2 extends DebugNewProcessSequence {

	private IGDBControl fGdbControl;
	private IGDBProcesses fProcService;
	private String fSessionId;
	private final boolean fInitialProcess;

	public DebugNewProcessSequence_7_2(DsfExecutor executor, boolean isInitial, IDMContext dmc, String file, 
			                           Map<String, Object> attributes, DataRequestMonitor<IDMContext> rm) {
		super(executor, isInitial, dmc, file, attributes, rm);
		fSessionId = dmc.getSessionId();
		fInitialProcess = isInitial;
	}

	@Override
	protected String[] getExecutionOrder(String group) {
		if (GROUP_TOP_LEVEL.equals(group)) {
			// Initialize the list with the base class' steps
			// We need to create a list that we can modify, which is why we create our own ArrayList.
			List<String> orderList = new ArrayList<String>(Arrays.asList(super.getExecutionOrder(GROUP_TOP_LEVEL)));

			// Now insert our steps right after the initialization of the base class.
			orderList.add(orderList.indexOf("stepInitializeBaseSequence") + 1, "stepInitializeSequence_7_2"); //$NON-NLS-1$ //$NON-NLS-2$
			orderList.add(orderList.indexOf("stepInitializeSequence_7_2") + 1, "stepAddInferior"); //$NON-NLS-1$ //$NON-NLS-2$
			
			return orderList.toArray(new String[orderList.size()]);
		}

		return null;
	}
	
	/** 
	 * Initialize the members of the DebugNewProcessSequence_7_2 class.
	 * This step is mandatory for the rest of the sequence to complete.
	 */
	@Execute
	public void stepInitializeSequence_7_2(RequestMonitor rm) {
		DsfServicesTracker tracker = new DsfServicesTracker(GdbPlugin.getBundleContext(), fSessionId);
		fGdbControl = tracker.getService(IGDBControl.class);
		fProcService = tracker.getService(IGDBProcesses.class);
		tracker.dispose();
		
        if (fGdbControl == null || fProcService == null) {
			rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, IDsfStatusConstants.INTERNAL_ERROR, "Cannot obtain service", null)); //$NON-NLS-1$
			rm.done();
			return;
		}
		
		rm.done();
	}
	
	/**
	 * Add a new inferior.
	 */
	@Execute
	public void stepAddInferior(final RequestMonitor rm) {
		if (fInitialProcess) {
			// The first process is automatically created by GDB.  We actually want to use that first created process
			// instead of ignoring it and creating a new one for a couple of reasons:
			// 1- post-mortem and non-attach remote sessions don't support creating a new process
			// 2- commands that were part of the .gdbinit file will affect the initial process, which is what the user expects,
			//    but would not affect a new process we created instead.
			setContainerContext(fProcService.createContainerContextFromGroupId(fGdbControl.getContext(), "i1")); //$NON-NLS-1$
			rm.done();
			return;
		}
		
		fGdbControl.queueCommand(
				fGdbControl.getCommandFactory().createMIAddInferior(fGdbControl.getContext()),
				new ImmediateDataRequestMonitor<MIAddInferiorInfo>(rm) {
					@Override
					protected void handleSuccess() {
						final String groupId = getData().getGroupId();
						if (groupId == null || groupId.trim().length() == 0) {
							rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, IDsfStatusConstants.REQUEST_FAILED, "Invalid gdb group id.", null)); //$NON-NLS-1$
							rm.done();
							return;
						}

						setContainerContext(fProcService.createContainerContextFromGroupId(fGdbControl.getContext(), groupId));
						rm.done();
					}
				});
		}
	}

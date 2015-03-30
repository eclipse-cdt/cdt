/*******************************************************************************
 * Copyright (c) 2015 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Marc Khouzam (Ericsson) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.service;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateDataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.cdt.dsf.mi.service.IMICommandControl;
import org.eclipse.cdt.dsf.mi.service.command.CommandFactory;
import org.eclipse.cdt.dsf.mi.service.command.output.MIListThreadGroupsInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIListThreadGroupsInfo.IThreadGroupInfo;
import org.eclipse.cdt.dsf.service.DsfSession;

/**
 * Process service starting at GDB 7.9
 * 
 * @since 4.7
 */
public class GDBProcesses_7_9 extends GDBProcesses_7_4 {
    
    private CommandFactory fCommandFactory;

	public GDBProcesses_7_9(DsfSession session) {
		super(session);
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

	/**
	 * This method initializes this service after our superclass's initialize()
	 * method succeeds.
	 * 
	 * @param rm
	 *            The call-back object to notify when this service's
	 *            initialization is done.
	 */
	private void doInitialize(RequestMonitor rm) {
		fCommandFactory = getServicesTracker().getService(IMICommandControl.class).getCommandFactory();

        rm.done();
	}

	@Override
	public void shutdown(RequestMonitor rm) {
        super.shutdown(rm);
	}

	@Override
	public void getExecutionData(final IThreadDMContext dmc, final DataRequestMonitor<IThreadDMData> rm) {
		if (dmc instanceof MIExitedProcessDMC) {
			super.getExecutionData(dmc, new ImmediateDataRequestMonitor<IThreadDMData>(rm) {
				@Override
				protected void handleSuccess() {
					final IThreadDMData firstLevelData = getData();

					// Starting with GDB 7.9, we can obtain the exit-code of an exited process
					if (firstLevelData instanceof IGdbThreadExitedDMData) {

						ICommandControlDMContext controlDmc = DMContexts.getAncestorOfType(dmc, ICommandControlDMContext.class);
						final String groupId = ((MIExitedProcessDMC)dmc).getGroupId();

						getContainerCommandCache().execute(
							fCommandFactory.createMIListThreadGroups(controlDmc),
							new ImmediateDataRequestMonitor<MIListThreadGroupsInfo>(rm) {
								@Override
								protected void handleCompleted() {
									if (!isSuccess()) {
										rm.done(firstLevelData);
										return;
									}

									Integer exitCode = null;
									IThreadGroupInfo[] groups = getData().getGroupList();
									if (groups != null) {
										for (IThreadGroupInfo group : groups) {
											if (group.getGroupId().equals(groupId)) {
												exitCode = group.getExitCode();
												break;
											}
										}
									}
									rm.done(new MIExitedProcessDMData(firstLevelData.getName(),
											                          firstLevelData.getId(),
											                          exitCode));
								}
							});
					} else {
						rm.done(firstLevelData);
					}
				} 
			});
		} else {
			super.getExecutionData(dmc, rm);
		}
	}
}

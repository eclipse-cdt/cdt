/*******************************************************************************
 * Copyright (c) 2016 Ericsson.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.cdt.llvm.dsf.lldb.core.internal.service;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.IProcessInfo;
import org.eclipse.cdt.core.IProcessList;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IProcesses;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExitedDMEvent;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.cdt.dsf.gdb.service.GDBProcesses_7_4;
import org.eclipse.cdt.dsf.gdb.service.IGDBBackend;
import org.eclipse.cdt.dsf.gdb.service.SessionType;
import org.eclipse.cdt.dsf.mi.service.IMIContainerDMContext;
import org.eclipse.cdt.dsf.mi.service.IMIProcessDMContext;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.CoreException;

/**
 * Provides processes information see {@link IProcesses}
 *
 * This LLDB specific implementation was initially created in order to be able
 * to get the list of processes in the absence of the MI command
 *
 * <pre>
 * -list-thread-groups --available
 * </pre>
 *
 * This is used notably when attaching to processes.
 */
public class LLDBProcesses extends GDBProcesses_7_4 {

	// A map of pid to names. It is filled when we get all the
	// processes that are running
	private Map<Integer, String> fProcessNames = new HashMap<>();

	/**
	 * Constructs the {@link LLDBProcesses} service.
	 *
	 * @param session
	 *            The debugging session
	 */
	public LLDBProcesses(DsfSession session) {
		super(session);
	}

	@Override
	public void getRunningProcesses(IDMContext dmc, DataRequestMonitor<IProcessDMContext[]> rm) {
		// FIXME '-list-thread-groups --available' is not supported by lldm-mi so we
		// fall back to CDT Core's implementation of listing running processes.
		// This works just like GDBProcesses#getRunningProcesses.

		final ICommandControlDMContext controlDmc = DMContexts.getAncestorOfType(dmc, ICommandControlDMContext.class);
		IGDBBackend backend = getServicesTracker().getService(IGDBBackend.class);
		if (backend.getSessionType() == SessionType.LOCAL) {
			IProcessList list = null;
			try {
				list = CCorePlugin.getDefault().getProcessList();
			} catch (CoreException e) {
			}

			if (list == null) {
				// If the list is null, the prompter will deal with it
				fProcessNames.clear();
				rm.setData(null);
			} else {
				fProcessNames.clear();
				IProcessInfo[] procInfos = list.getProcessList();
				for (IProcessInfo procInfo : procInfos) {
					fProcessNames.put(procInfo.getPid(), procInfo.getName());
				}
				rm.setData(makeProcessDMCs(controlDmc, procInfos));
			}
			rm.done();
		} else {
			// Remote not supported for now
			fProcessNames.clear();
			rm.setData(new IProcessDMContext[0]);
			rm.done();
		}
	}

	private IProcessDMContext[] makeProcessDMCs(ICommandControlDMContext controlDmc, IProcessInfo[] processes) {
		IProcessDMContext[] procDmcs = new IMIProcessDMContext[processes.length];
		for (int i = 0; i < procDmcs.length; i++) {
			procDmcs[i] = createProcessContext(controlDmc, Integer.toString(processes[i].getPid()));
		}
		return procDmcs;
	}

	@Override
	public void getExecutionData(IThreadDMContext dmc, DataRequestMonitor<IThreadDMData> rm) {
		if (!(dmc instanceof IMIProcessDMContext)) {
			super.getExecutionData(dmc, rm);
			return;
		}

		String pidStr = ((IMIProcessDMContext) dmc).getProcId();
		int pid = -1;
		try {
			pid = Integer.parseInt(pidStr);
		} catch (NumberFormatException e) {
		}
		int pid2 = pid;

		// It's possible that we get here without getRunningProcesses called
		// yet so the process names map will be empty. This can happen when
		// doing local debugging but not attach mode.
		if (fProcessNames.isEmpty()) {
			getRunningProcesses(dmc, new DataRequestMonitor<IProcessDMContext[]>(getExecutor(), rm) {
				@Override
				protected void handleSuccess() {
					rm.setData(new LLDBMIThreadDMData(getProcessName(pid2), pidStr));
					super.handleSuccess();
				}
			});
		} else {
			rm.setData(new LLDBMIThreadDMData(getProcessName(pid2), pidStr));
			rm.done();
		}
	}

	private String getProcessName(int pid) {
		String name = fProcessNames.get(pid);
		return name != null ? name : Messages.LLDBProcesses_unknown_process_name;
	}

	private static class LLDBMIThreadDMData implements IThreadDMData {
		final String fName;
		final String fId;

		public LLDBMIThreadDMData(String name, String id) {
			fName = name;
			fId = id;
		}

		@Override
		public String getId() {
			return fId;
		}

		@Override
		public String getName() {
			return fName;
		}

		@Override
		public boolean isDebuggerAttached() {
			return true;
		}
	}

	@Override
	@DsfServiceEventHandler
	public void eventDispatched(IExitedDMEvent e) {
		if (e.getDMContext() instanceof IMIContainerDMContext && getNumConnected() == 0) {
			// FIXME: Work around bug http://bugs.llvm.org/show_bug.cgi?id=32053
			// And also https://bugs.eclipse.org/bugs/show_bug.cgi?id=510832
			// LLDB-MI sends an extra "=thread-group-exited" event.
			// This override should be completely removed once fixed.
			return;
		}
		super.eventDispatched(e);
	}
}

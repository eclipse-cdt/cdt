/*******************************************************************************
 * Copyright (c) 2012, 2015 Tilera Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     William R. Swanson (Tilera Corporation) - initial API and implementation
 *     Marc Dumais (Ericsson) - Add CPU/core load information to the multicore visualizer (Bug 396268)
 *     Xavier Raynaud (Kalray) - Bug 431935
 *     Alvaro Sanchez-Leon (Ericsson) - Bug 459114 - override construction of the data model
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.utils;

import java.util.ArrayList;

import org.eclipse.cdt.dsf.concurrent.ConfinedToDsfExecutor;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateCountingRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateDataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateRequestMonitor;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IProcesses;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IThreadDMContext;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IThreadDMData;
import org.eclipse.cdt.dsf.debug.service.IRunControl;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMData;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMData2;
import org.eclipse.cdt.dsf.debug.service.IRunControl.StateChangeReason;
import org.eclipse.cdt.dsf.debug.service.IStack;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMContext;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMData;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.model.VisualizerExecutionState;
import org.eclipse.cdt.dsf.gdb.service.IGDBHardwareAndOS;
import org.eclipse.cdt.dsf.gdb.service.IGDBHardwareAndOS.ICPUDMContext;
import org.eclipse.cdt.dsf.gdb.service.IGDBHardwareAndOS.ICoreDMContext;
import org.eclipse.cdt.dsf.gdb.service.IGDBHardwareAndOS.IHardwareTargetDMContext;
import org.eclipse.cdt.dsf.gdb.service.IGDBHardwareAndOS2;
import org.eclipse.cdt.dsf.gdb.service.IGDBHardwareAndOS2.ILoadInfo;
import org.eclipse.cdt.dsf.gdb.service.IGDBProcesses.IGdbThreadDMData;
import org.eclipse.cdt.dsf.mi.service.IMIExecutionDMContext;

/** Debugger state information accessors.</br>
 *
 *  NOTE: The methods on this class perform asynchronous operations
 *  and the result is reported back via the received request monitor
 */
public class DSFDebugModel implements IDSFTargetDataProxy {

	// --- static methods ---
	@Override
	@ConfinedToDsfExecutor("sessionState.getDsfSession().getExecutor()")
	public void getCPUs(DSFSessionState sessionState, final DataRequestMonitor<ICPUDMContext[]> rm) {
		ICommandControlService controlService = sessionState.getService(ICommandControlService.class);
		IGDBHardwareAndOS hwService = sessionState.getService(IGDBHardwareAndOS.class);
		if (controlService == null || hwService == null) {
			rm.done(new ICPUDMContext[0]);
			return;
		}

		IHardwareTargetDMContext contextToUse = DMContexts.getAncestorOfType(controlService.getContext(),
				IHardwareTargetDMContext.class);
		hwService.getCPUs(contextToUse, new ImmediateDataRequestMonitor<ICPUDMContext[]>(rm) {
			@Override
			protected void handleCompleted() {
				ICPUDMContext[] cpuContexts;
				if (isSuccess()) {
					cpuContexts = getData();
				} else {
					cpuContexts = new ICPUDMContext[0];
				}

				rm.done(cpuContexts);
			}
		});
	}

	@Override
	@ConfinedToDsfExecutor("sessionState.getDsfSession().getExecutor()")
	public void getLoad(DSFSessionState sessionState, final IDMContext context,
			final DataRequestMonitor<ILoadInfo> rm) {
		IGDBHardwareAndOS2 hwService = sessionState.getService(IGDBHardwareAndOS2.class);
		if (hwService == null) {
			rm.setData(null);
			rm.done();
			return;
		}

		hwService.getLoadInfo(context, rm);
	}

	@Override
	@ConfinedToDsfExecutor("sessionState.getDsfSession().getExecutor()")
	public void getCores(DSFSessionState sessionState, final DataRequestMonitor<ICoreDMContext[]> rm) {
		getCores(sessionState, null, rm);
	}

	@Override
	@ConfinedToDsfExecutor("sessionState.getDsfSession().getExecutor()")
	public void getCores(DSFSessionState sessionState, final ICPUDMContext cpuContext,
			final DataRequestMonitor<ICoreDMContext[]> rm) {
		IGDBHardwareAndOS hwService = sessionState.getService(IGDBHardwareAndOS.class);
		if (hwService == null) {
			rm.done(new ICoreDMContext[0]);
			return;
		}

		IDMContext targetContextToUse = cpuContext;
		if (targetContextToUse == null) {
			// if caller doesn't supply a specific cpu context,
			// use the hardware context (so we get all available cores)
			ICommandControlService controlService = sessionState.getService(ICommandControlService.class);
			targetContextToUse = DMContexts.getAncestorOfType(controlService.getContext(),
					IHardwareTargetDMContext.class);
		}

		hwService.getCores(targetContextToUse, new ImmediateDataRequestMonitor<ICoreDMContext[]>() {
			@Override
			protected void handleCompleted() {
				ICoreDMContext[] coreContexts = getData();

				if (!isSuccess() || coreContexts == null || coreContexts.length < 1) {
					// Unable to get any core data
					rm.done(new ICoreDMContext[0]);
				} else {
					rm.done(coreContexts);
				}
			}
		});
	}

	@Override
	@ConfinedToDsfExecutor("sessionState.getDsfSession().getExecutor()")
	public void getThreads(DSFSessionState sessionState, final ICPUDMContext cpuContext,
			final ICoreDMContext coreContext, final DataRequestMonitor<IDMContext[]> rm) {
		// Get control DM context associated with the core
		// Process/Thread Info service (GDBProcesses_X_Y_Z)
		final IProcesses procService = sessionState.getService(IProcesses.class);
		// Debugger control context (GDBControlDMContext)
		ICommandControlDMContext controlContext = DMContexts.getAncestorOfType(coreContext,
				ICommandControlDMContext.class);
		if (procService == null || controlContext == null) {
			rm.done(new IDMContext[0]);
			return;
		}

		// Get debugged processes
		procService.getProcessesBeingDebugged(controlContext, new ImmediateDataRequestMonitor<IDMContext[]>() {

			@Override
			protected void handleCompleted() {
				IDMContext[] processContexts = getData();

				if (!isSuccess() || processContexts == null || processContexts.length < 1) {
					// Unable to get any process data for this core
					// Is this an issue? A core may have no processes/threads, right?
					rm.done(new IDMContext[0]);
					return;
				}

				final ArrayList<IDMContext> threadContextsList = new ArrayList<>();

				final ImmediateCountingRequestMonitor crm1 = new ImmediateCountingRequestMonitor(
						new ImmediateRequestMonitor() {
							@Override
							protected void handleCompleted() {
								IDMContext[] threadContexts = threadContextsList
										.toArray(new IDMContext[threadContextsList.size()]);
								rm.done(threadContexts);
							}
						});
				crm1.setDoneCount(processContexts.length);

				for (IDMContext processContext : processContexts) {
					IContainerDMContext containerContext = DMContexts.getAncestorOfType(processContext,
							IContainerDMContext.class);

					procService.getProcessesBeingDebugged(containerContext,
							new ImmediateDataRequestMonitor<IDMContext[]>(crm1) {

								@Override
								protected void handleCompleted() {
									IDMContext[] threadContexts = getData();

									if (!isSuccess() || threadContexts == null || threadContexts.length < 1) {
										crm1.done();
										return;
									}

									final ImmediateCountingRequestMonitor crm2 = new ImmediateCountingRequestMonitor(
											crm1);
									crm2.setDoneCount(threadContexts.length);

									for (final IDMContext threadContext : threadContexts) {
										IThreadDMContext threadContext2 = DMContexts.getAncestorOfType(threadContext,
												IThreadDMContext.class);

										procService.getExecutionData(threadContext2,
												new ImmediateDataRequestMonitor<IThreadDMData>(crm2) {

													@Override
													protected void handleCompleted() {
														IThreadDMData data = getData();

														// Check whether we know about cores
														if (data != null && data instanceof IGdbThreadDMData) {
															String[] cores = ((IGdbThreadDMData) data).getCores();
															if (cores != null && cores.length == 1) {
																if (coreContext.getId().equals(cores[0])) {
																	// This thread belongs to the proper core
																	threadContextsList.add(threadContext);
																}
															}
														}
														crm2.done();
													}
												});
									}
								}
							});
				}
			}
		});
	}

	@Override
	@ConfinedToDsfExecutor("sessionState.getDsfSession().getExecutor()")
	public void getThreadData(DSFSessionState sessionState, final ICPUDMContext cpuContext,
			final ICoreDMContext coreContext, final IMIExecutionDMContext execContext,
			final DataRequestMonitor<IThreadDMData> rm) {
		IProcesses procService = sessionState.getService(IProcesses.class);

		if (procService == null) {
			rm.setData(null);
			rm.done();
			return;
		}

		final IThreadDMContext threadContext = DMContexts.getAncestorOfType(execContext, IThreadDMContext.class);
		procService.getExecutionData(threadContext, rm);
	}

	@Override
	@ConfinedToDsfExecutor("sessionState.getDsfSession().getExecutor()")
	public void getTopFrameData(final DSFSessionState sessionState, final IMIExecutionDMContext execContext,
			final DataRequestMonitor<IFrameDMData> rm) {
		final IFrameDMData nullFrameData = null;
		// For a suspended thread, retrieve the current stack
		final IStack stackService = sessionState.getService(IStack.class);
		if (stackService != null) {
			stackService.getTopFrame(execContext, new ImmediateDataRequestMonitor<IFrameDMContext>(null) {
				@Override
				protected void handleCompleted() {
					IFrameDMContext targetFrameContext = isSuccess() ? getData() : null;
					if (targetFrameContext != null) {
						stackService.getFrameData(targetFrameContext,
								new ImmediateDataRequestMonitor<IFrameDMData>(null) {
									@Override
									protected void handleCompleted() {
										IFrameDMData frameData = isSuccess() ? getData() : null;
										rm.done(frameData);
									}
								});
					} else {
						rm.done(nullFrameData);
					}
				}
			});
		} else {
			rm.done(nullFrameData);
		}
	}

	@Override
	@ConfinedToDsfExecutor("sessionState.getDsfSession().getExecutor()")
	public void getThreadExecutionState(final DSFSessionState sessionState, final ICPUDMContext cpuContext,
			final ICoreDMContext coreContext, final IMIExecutionDMContext execContext, final IThreadDMData threadData,
			final DataRequestMonitor<VisualizerExecutionState> rm) {
		IRunControl runControl = sessionState.getService(IRunControl.class);

		if (runControl == null) {
			rm.setData(null);
			rm.done();
			return;
		}

		if (runControl.isSuspended(execContext) == false) {
			// The thread is running
			rm.done(VisualizerExecutionState.RUNNING);
		} else {
			getThreadSuspendReason(sessionState, execContext, rm);
		}
	}

	/** For a suspended thread, let's see why it is suspended,
	 * to find out if the thread is crashed */
	@ConfinedToDsfExecutor("sessionState.getDsfSession().getExecutor()")
	private static void getThreadSuspendReason(DSFSessionState sessionState, IMIExecutionDMContext execContext,
			final DataRequestMonitor<VisualizerExecutionState> rm) {
		IRunControl runControl = sessionState.getService(IRunControl.class);
		if (runControl != null) {
			runControl.getExecutionData(execContext, new ImmediateDataRequestMonitor<IExecutionDMData>() {
				@Override
				protected void handleCompleted() {
					IExecutionDMData executionData = getData();
					VisualizerExecutionState state = VisualizerExecutionState.SUSPENDED;

					if (isSuccess() && executionData != null) {
						if (executionData.getStateChangeReason() == StateChangeReason.SIGNAL) {
							if (executionData instanceof IExecutionDMData2) {
								String details = ((IExecutionDMData2) executionData).getDetails();
								if (details != null) {
									if (isCrashSignal(details)) {
										state = VisualizerExecutionState.CRASHED;
									}
								}
							}
						}
					}

					rm.done(state);
				}
			});
		} else {
			rm.setData(null);
			rm.done();
		}
	}

	/**
	 * Return true if the string SIGNALINFO describes a signal
	 * that indicates a crash.
	 */
	public static boolean isCrashSignal(String signalInfo) {
		if (signalInfo.startsWith("SIGHUP") || //$NON-NLS-1$
				signalInfo.startsWith("SIGILL") || //$NON-NLS-1$
				signalInfo.startsWith("SIGABRT") || //$NON-NLS-1$
				signalInfo.startsWith("SIGBUS") || //$NON-NLS-1$
				signalInfo.startsWith("SIGSEGV")) { //$NON-NLS-1$
			// Not sure about the list of events here...
			// We are dealing with a crash
			return true;
		}

		return false;
	}
}

/*******************************************************************************
 * Copyright (c) 2012 Tilera Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     William R. Swanson (Tilera Corporation) - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.utils;

import java.util.ArrayList;

import org.eclipse.cdt.dsf.concurrent.ConfinedToDsfExecutor;
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
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.model.VisualizerExecutionState;
import org.eclipse.cdt.dsf.gdb.service.IGDBHardwareAndOS;
import org.eclipse.cdt.dsf.gdb.service.IGDBHardwareAndOS.ICPUDMContext;
import org.eclipse.cdt.dsf.gdb.service.IGDBHardwareAndOS.ICoreDMContext;
import org.eclipse.cdt.dsf.gdb.service.IGDBHardwareAndOS.IHardwareTargetDMContext;
import org.eclipse.cdt.dsf.gdb.service.IGDBProcesses.IGdbThreadDMData;
import org.eclipse.cdt.dsf.mi.service.IMIExecutionDMContext;


/** Debugger state information accessors.
 * 
 *  NOTE: The methods on this class perform asynchronous operations,
 *  and call back to a method on a provided DSFDebugModelListener instance
 *  when the operation is completed.
 *  
 *  The "arg" argument to each method can be used by the caller to
 *  pass through information (partial state, etc.) that will be needed
 *  by the callback method. This argument is ignored by the methods
 *  on this class, and is allowed to be null.
 */
public class DSFDebugModel {
	
	// --- static methods ---
	
	/** Requests list of CPUs.
	 *  Calls back to getCPUsDone() on listener. */
	@ConfinedToDsfExecutor("getSession().getExecutor()")
	public static void getCPUs(DSFSessionState sessionState,
							   final DSFDebugModelListener listener,
							   final Object arg)
	{
		ICommandControlService controlService = sessionState.getService(ICommandControlService.class);
		IGDBHardwareAndOS hwService = sessionState.getService(IGDBHardwareAndOS.class);
		if (controlService == null || hwService == null) {
			listener.getCPUsDone(null, arg);
			return;
		}
		
		IHardwareTargetDMContext contextToUse = DMContexts.getAncestorOfType(controlService.getContext(),
                                                                             IHardwareTargetDMContext.class);
		hwService.getCPUs(contextToUse,
			new ImmediateDataRequestMonitor<ICPUDMContext[]>() {
				@Override
				protected void handleCompleted() {
					ICPUDMContext[] cpuContexts = getData();
					if (! isSuccess()) cpuContexts = null;
					listener.getCPUsDone(cpuContexts, arg);
				}
			}
		);
	}
	
	/** Requests list of Cores.
	 *  Calls back to getCoresDone() on listener. */
	@ConfinedToDsfExecutor("getSession().getExecutor()")
	public static void getCores(DSFSessionState sessionState,
							    DSFDebugModelListener listener,
							    Object arg) 
	{
		getCores(sessionState, null, listener, arg);
	}

	/** Requests list of Cores.
	 *  Calls back to getCoresDone() on listener. */
	@ConfinedToDsfExecutor("getSession().getExecutor()")
	public static void getCores(DSFSessionState sessionState,
								final ICPUDMContext cpuContext,
							    final DSFDebugModelListener listener,
							    final Object arg)
	{
		IGDBHardwareAndOS hwService = sessionState.getService(IGDBHardwareAndOS.class);
		if (hwService == null) {
			listener.getCoresDone(cpuContext, null, arg);
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
		
		hwService.getCores(targetContextToUse,
			new ImmediateDataRequestMonitor<ICoreDMContext[]>() {
				@Override
				protected void handleCompleted() {
					ICoreDMContext[] coreContexts = getData();

					if (!isSuccess() || coreContexts == null || coreContexts.length < 1) {
						// Unable to get any core data
						listener.getCoresDone(cpuContext, null, arg);
						return;
					}

					ICPUDMContext cpuContextToUse = cpuContext;
					if (cpuContextToUse == null) {
						// If we didn't have a CPU context, lets use the ancestor of the first core context
						cpuContextToUse = DMContexts.getAncestorOfType(coreContexts[0], ICPUDMContext.class);
					}
					listener.getCoresDone(cpuContextToUse, coreContexts, arg);
				}
			}
		);
	}
	
	/** Requests list of Threads.
	 *  Calls back to getThreadsDone() on listener. */
	@ConfinedToDsfExecutor("getSession().getExecutor()")
	public static void getThreads(DSFSessionState sessionState,
								  final ICPUDMContext cpuContext,
								  final ICoreDMContext coreContext,
							      final DSFDebugModelListener listener,
							      final Object arg)
	{
		// Get control DM context associated with the core
		// Process/Thread Info service (GDBProcesses_X_Y_Z)
		final IProcesses procService = sessionState.getService(IProcesses.class);
		// Debugger control context (GDBControlDMContext)
		ICommandControlDMContext controlContext =
				DMContexts.getAncestorOfType(coreContext, ICommandControlDMContext.class);
		if (procService == null || controlContext == null) {
			listener.getThreadsDone(cpuContext, coreContext, null, arg);
			return;
		}
		
		// Get debugged processes
		procService.getProcessesBeingDebugged(controlContext,
			new ImmediateDataRequestMonitor<IDMContext[]>() {
		
				@Override
				protected void handleCompleted() {
					IDMContext[] processContexts = getData();
				
					if (!isSuccess() || processContexts == null || processContexts.length < 1) {
						// Unable to get any process data for this core
						// Is this an issue? A core may have no processes/threads, right?
						listener.getThreadsDone(cpuContext, coreContext, null, arg);
						return;
					}
					
					final ArrayList<IDMContext> threadContextsList = new ArrayList<IDMContext>();
				
					final ImmediateCountingRequestMonitor crm1 = new ImmediateCountingRequestMonitor(
						new ImmediateRequestMonitor() {
							@Override
							protected void handleCompleted() {
								IDMContext[] threadContexts = threadContextsList.toArray(new IDMContext[threadContextsList.size()]);
								listener.getThreadsDone(cpuContext, coreContext, threadContexts, arg);
							}
						});
					crm1.setDoneCount(processContexts.length);
					
					for (IDMContext processContext : processContexts) {
						IContainerDMContext containerContext =
							DMContexts.getAncestorOfType(processContext, IContainerDMContext.class);
						
						procService.getProcessesBeingDebugged(containerContext,
							new ImmediateDataRequestMonitor<IDMContext[]>(crm1) {

								@Override
								protected void handleCompleted() {
									IDMContext[] threadContexts = getData();
									
									if (!isSuccess() || threadContexts == null || threadContexts.length < 1) {
										crm1.done();
										return;
									}
									
									final ImmediateCountingRequestMonitor crm2 = new ImmediateCountingRequestMonitor(crm1);
									crm2.setDoneCount(threadContexts.length);
									
									for (final IDMContext threadContext : threadContexts) {
										IThreadDMContext threadContext2 =
												DMContexts.getAncestorOfType(threadContext, IThreadDMContext.class);

										procService.getExecutionData(threadContext2, 
											new ImmediateDataRequestMonitor<IThreadDMData>(crm2) {
											
												@Override
												protected void handleCompleted() {
													IThreadDMData data = getData();

													// Check whether we know about cores
													if (data != null && data instanceof IGdbThreadDMData) {
														String[] cores = ((IGdbThreadDMData)data).getCores();
														if (cores != null && cores.length == 1) {
															if (coreContext.getId().equals(cores[0])) {
																// This thread belongs to the proper core
																threadContextsList.add(threadContext);
															}
														}
													}
													crm2.done();
												}
											}
										);
									}
								}
							}
						);
					}
				}
			}
		);
	}
	
	/** Requests data of a thread.
	 *  Calls back to getThreadDataDone() on listener. */
	@ConfinedToDsfExecutor("getSession().getExecutor()")
	public static void getThreadData(DSFSessionState sessionState,
			                         final ICPUDMContext cpuContext,
			                         final ICoreDMContext coreContext,
			                         final IMIExecutionDMContext execContext,
			                         final DSFDebugModelListener listener,
			                         final Object arg)
	{
		IProcesses procService = sessionState.getService(IProcesses.class);

		if (procService == null) {
			listener.getThreadDataDone(cpuContext, coreContext, execContext, null, arg);
			return;
		}

		final IThreadDMContext threadContext = DMContexts.getAncestorOfType(execContext, IThreadDMContext.class);
		procService.getExecutionData(threadContext, 
				new ImmediateDataRequestMonitor<IThreadDMData>() {
			@Override
			protected void handleCompleted() {
				IThreadDMData threadData = isSuccess() ? getData() : null;
				listener.getThreadDataDone(cpuContext, coreContext, execContext, threadData, arg);
			}
		});

	}

	/** Requests execution state of a thread.
	 *  Calls back to getThreadExecutionStateDone() on listener. */
	@ConfinedToDsfExecutor("getSession().getExecutor()")
	public static void getThreadExecutionState(DSFSessionState sessionState,
			                                   final ICPUDMContext cpuContext,
			                                   final ICoreDMContext coreContext,
			                                   final IMIExecutionDMContext execContext,
			                                   final IThreadDMData threadData,
			                                   final DSFDebugModelListener listener,
			                                   final Object arg)
	{
		IRunControl runControl = sessionState.getService(IRunControl.class);

		if (runControl == null) {
			listener.getThreadExecutionStateDone(cpuContext, coreContext, execContext, threadData, null, arg);
			return;
		}

		if (runControl.isSuspended(execContext) == false) {
			// The thread is running
			listener.getThreadExecutionStateDone(cpuContext, coreContext, execContext, threadData,
					                               VisualizerExecutionState.RUNNING, arg);
		} else {
			// For a suspended thread, let's see why it is suspended,
			// to find out if the thread is crashed
			runControl.getExecutionData(execContext, 
					new ImmediateDataRequestMonitor<IExecutionDMData>() {
				@Override
				protected void handleCompleted() {
					IExecutionDMData executionData = getData();

					VisualizerExecutionState state = VisualizerExecutionState.SUSPENDED;
					
					if (isSuccess() && executionData != null) {
						if (executionData.getStateChangeReason() == StateChangeReason.SIGNAL) {
							if (executionData instanceof IExecutionDMData2) {
								String details = ((IExecutionDMData2)executionData).getDetails();
								if (details != null) {
									if (isCrashSignal(details)) {
										state = VisualizerExecutionState.CRASHED;
									}
								}
							}
						}
					}
					
					listener.getThreadExecutionStateDone(cpuContext, coreContext, execContext, threadData, state, arg);
				}
			});
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

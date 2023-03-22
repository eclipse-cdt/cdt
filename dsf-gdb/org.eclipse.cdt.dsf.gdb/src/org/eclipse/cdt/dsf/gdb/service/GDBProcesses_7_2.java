/*******************************************************************************
 * Copyright (c) 2010, 2017 TUBITAK BILGEM-ITI and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Onur Akdemir (TUBITAK BILGEM-ITI) - Multi-process debugging (Bug 237306)
 *     Marc Khouzam (Ericsson) - Workaround for Bug 352998
 *     Marc Khouzam (Ericsson) - Update breakpoint handling for GDB >= 7.4 (Bug 389945)
 *     Alvaro Sanchez-Leon (Ericsson) - Breakpoint Enable does not work after restarting the application (Bug 456959)
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.service;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.dsf.concurrent.CountingRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.concurrent.ImmediateDataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateExecutor;
import org.eclipse.cdt.dsf.concurrent.ImmediateRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Sequence;
import org.eclipse.cdt.dsf.datamodel.AbstractDMEvent;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointsTargetDMContext;
import org.eclipse.cdt.dsf.debug.service.IMemory.IMemoryDMContext;
import org.eclipse.cdt.dsf.debug.service.IMultiDetach;
import org.eclipse.cdt.dsf.debug.service.IMultiTerminate;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.ICreatedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExitedDMEvent;
import org.eclipse.cdt.dsf.debug.service.command.ICommand;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.gdb.service.IGDBTraceControl.ITraceRecordSelectedChangedDMEvent;
import org.eclipse.cdt.dsf.gdb.service.command.IGDBControl;
import org.eclipse.cdt.dsf.mi.service.IMICommandControl;
import org.eclipse.cdt.dsf.mi.service.IMIContainerDMContext;
import org.eclipse.cdt.dsf.mi.service.IMIExecutionDMContext;
import org.eclipse.cdt.dsf.mi.service.IMIProcessDMContext;
import org.eclipse.cdt.dsf.mi.service.IMIRunControl;
import org.eclipse.cdt.dsf.mi.service.IMIRunControl.MIRunMode;
import org.eclipse.cdt.dsf.mi.service.MIBreakpointsManager;
import org.eclipse.cdt.dsf.mi.service.MIProcesses;
import org.eclipse.cdt.dsf.mi.service.command.CommandFactory;
import org.eclipse.cdt.dsf.mi.service.command.events.MIThreadGroupAddedEvent;
import org.eclipse.cdt.dsf.mi.service.command.output.MIAddInferiorInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * Adding support for multi-process with GDB 7.2
 *
 * @since 4.0
 */
public class GDBProcesses_7_2 extends GDBProcesses_7_1 implements IMultiTerminate, IMultiDetach {

	abstract private class ConditionalRequestMonitor extends ImmediateDataRequestMonitor<Boolean> {

		private Iterator<? extends IDMContext> fIterator;
		private boolean fAll = true;
		private DataRequestMonitor<Boolean> fParentMonitor;

		private ConditionalRequestMonitor(Iterator<? extends IDMContext> it, boolean all,
				DataRequestMonitor<Boolean> parentMonitor) {
			super(parentMonitor);
			fAll = all;
			fParentMonitor = parentMonitor;
			fIterator = it;
		}

		@Override
		protected void handleCompleted() {
			if (!isSuccess()) {
				fParentMonitor.setStatus(getStatus());
				fParentMonitor.done();
				return;
			}

			if (getData() != fAll) {
				fParentMonitor.setData(getData());
				fParentMonitor.done();
			} else if (!fIterator.hasNext()) {
				fParentMonitor.setData(fAll);
				fParentMonitor.done();
			} else {
				proceed(fIterator, fAll, fParentMonitor);
			}
		}

		abstract protected void proceed(Iterator<? extends IDMContext> it, boolean all,
				DataRequestMonitor<Boolean> parentMonitor);
	}

	private class CanDetachRequestMonitor extends ConditionalRequestMonitor {

		private CanDetachRequestMonitor(Iterator<? extends IDMContext> it, boolean all,
				DataRequestMonitor<Boolean> parentMonitor) {
			super(it, all, parentMonitor);
		}

		@Override
		protected void proceed(Iterator<? extends IDMContext> it, boolean all,
				DataRequestMonitor<Boolean> parentMonitor) {
			canDetachDebuggerFromProcess(it.next(), new CanDetachRequestMonitor(it, all, parentMonitor));
		}

	}

	private class CanTerminateRequestMonitor extends ConditionalRequestMonitor {

		private CanTerminateRequestMonitor(Iterator<? extends IDMContext> it, boolean all,
				DataRequestMonitor<Boolean> parentMonitor) {
			super(it, all, parentMonitor);
		}

		@Override
		protected void proceed(Iterator<? extends IDMContext> it, boolean all,
				DataRequestMonitor<Boolean> parentMonitor) {
			canTerminate((IThreadDMContext) it.next(), new CanTerminateRequestMonitor(it, all, parentMonitor));
		}
	}

	/**
	 * Event indicating that a container (gdb inferior) has been created, but is not yet running.
	 * @since 5.1
	 */
	protected static class ContainerCreatedDMEvent extends AbstractDMEvent<IExecutionDMContext>
			implements ICreatedDMEvent {
		public ContainerCreatedDMEvent(IContainerDMContext context) {
			super(context);
		}
	}

	/**
	 * The first thread-group id used by GDB.
	 * GDB starts up with certain things already setup, and we need
	 * to prepare some things using this id.
	 * @since 5.1
	 */
	public static final String INITIAL_THREAD_GROUP_ID = "i1"; //$NON-NLS-1$

	/**
	 * The id of the single thread to be used during event visualization.
	 * @since 4.1
	 */
	protected static final String TRACE_VISUALIZATION_THREAD_ID = "1"; //$NON-NLS-1$

	private CommandFactory fCommandFactory;
	private IGDBControl fCommandControl;
	private IGDBBackend fBackend;

	/**
	 * Keep track if we need to reconnect to the target
	 * due to a workaround because of a GDB 7.2 bug.
	 * Bug 352998
	 */
	private boolean fNeedToReconnect;

	/**
	 * Set of processes that are currently being restarted.
	 * We use this set for such things as not removing breakpoints
	 * because we know the process will be restarted.
	 */
	private Set<IContainerDMContext> fProcRestarting = new HashSet<>();

	/**
	 * Indicates that we are currently visualizing trace data.
	 */
	private boolean fTraceVisualization;

	public GDBProcesses_7_2(DsfSession session) {
		super(session);
	}

	@Override
	public void initialize(final RequestMonitor requestMonitor) {
		super.initialize(new ImmediateRequestMonitor(requestMonitor) {
			@Override
			protected void handleSuccess() {
				doInitialize(requestMonitor);
			}
		});
	}

	/**
	 * This method initializes this service after our superclass's initialize()
	 * method succeeds.
	 *
	 * @param requestMonitor
	 *            The call-back object to notify when this service's
	 *            initialization is done.
	 */
	private void doInitialize(RequestMonitor requestMonitor) {
		register(new String[] { IMultiDetach.class.getName(), IMultiTerminate.class.getName() },
				new Hashtable<String, String>());

		fCommandControl = getServicesTracker().getService(IGDBControl.class);
		fCommandFactory = getServicesTracker().getService(IMICommandControl.class).getCommandFactory();
		fBackend = getServicesTracker().getService(IGDBBackend.class);

		// We know we missed the very first =thread-group-added event
		// because GDB sends it as soon as it starts, but we are not
		// ready to receive it at that time.  We send it now instead.
		IMIContainerDMContext initialContainer = createContainerContextFromGroupId(fCommandControl.getContext(),
				INITIAL_THREAD_GROUP_ID);
		getSession().dispatchEvent(new ContainerCreatedDMEvent(initialContainer), getProperties());

		requestMonitor.done();
	}

	@Override
	public void shutdown(RequestMonitor requestMonitor) {
		super.shutdown(requestMonitor);
	}

	/** @since 4.1 */
	protected boolean getTraceVisualization() {
		return fTraceVisualization;
	}

	/** @since 4.1 */
	protected void setTraceVisualization(boolean visualizing) {
		fTraceVisualization = visualizing;
	}

	@Override
	public IMIContainerDMContext createContainerContextFromGroupId(ICommandControlDMContext controlDmc,
			String groupId) {
		String pid = getGroupToPidMap().get(groupId);
		if (pid == null) {
			// For GDB 7.2, the groupId is no longer the pid, so use our wildcard pid instead
			pid = MIProcesses.UNKNOWN_PROCESS_ID;
		}
		IProcessDMContext processDmc = createProcessContext(controlDmc, pid);
		return createContainerContext(processDmc, groupId);
	}

	@Override
	protected boolean doIsDebuggerAttachSupported() {
		// Multi-process is not applicable to post-mortem sessions (core)
		// or to non-attach remote sessions.
		if (fBackend.getSessionType() == SessionType.CORE) {
			return false;
		}

		if (fBackend.getSessionType() == SessionType.REMOTE && !fBackend.getIsAttachSession()) {
			return false;
		}

		// Multi-process does not work for all-stop right now
		IMIRunControl runControl = getServicesTracker().getService(IMIRunControl.class);
		if (runControl != null && runControl.getRunMode() == MIRunMode.ALL_STOP) {
			// Only one process is allowed in all-stop (for now)
			return getNumConnected() == 0;
			// NOTE: when we support multi-process in all-stop mode,
			// we will need to interrupt the target to when doing the attach.
		}

		return true;
	}

	@Override
	public void attachDebuggerToProcess(IProcessDMContext procCtx, DataRequestMonitor<IDMContext> rm) {
		attachDebuggerToProcess(procCtx, null, rm);
	}

	/**
	 * @since 4.0
	 */
	@Override
	public void attachDebuggerToProcess(final IProcessDMContext procCtx, final String binaryPath,
			final DataRequestMonitor<IDMContext> dataRm) {
		if (procCtx instanceof IMIProcessDMContext) {
			if (!doIsDebuggerAttachSupported()) {
				dataRm.setStatus(
						new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INTERNAL_ERROR, "Attach not supported.", null)); //$NON-NLS-1$
				dataRm.done();
				return;
			}

			// Use a sequence for better control of each step
			ImmediateExecutor.getInstance().execute(new Sequence(getExecutor(), dataRm) {
				private IMIContainerDMContext fContainerDmc;

				private Step[] steps = new Step[] {
						// first check if requested process is already targetted
						new Step() {
							@Override
							public void execute(final RequestMonitor rm) {
								if (procCtx instanceof IMIProcessDMContext ctx
										&& MIProcesses.UNKNOWN_PROCESS_ID.equals(ctx.getProcId())) {
									rm.done();
									return;
								}

								getProcessesBeingDebugged(procCtx, new ImmediateDataRequestMonitor<IDMContext[]>(rm) {
									@Override
									protected void handleSuccess() {
										assert getData() != null;

										boolean found = false;
										for (IDMContext dmc : getData()) {
											IProcessDMContext procDmc = DMContexts.getAncestorOfType(dmc,
													IProcessDMContext.class);
											if (procCtx.equals(procDmc)) {
												found = true;
											}
										}
										if (found) {
											// abort the sequence
											Status failedStatus = new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID,
													REQUEST_FAILED,
													MessageFormat.format(Messages.Already_connected_process_err,
															((IMIProcessDMContext) procCtx).getProcId()),
													null);
											rm.done(failedStatus);
											return;
										}
										super.handleSuccess();
									}
								});
							}
						},

						// If this is not the very first inferior, we first need create the new inferior
						new Step() {
							@Override
							public void execute(final RequestMonitor rm) {
								if (isInitialProcess()) {
									// If it is the first inferior, GDB has already created it for us
									// We really should get the id from GDB instead of hard-coding it
									fContainerDmc = createContainerContext(procCtx, INITIAL_THREAD_GROUP_ID);
									rm.done();
									return;
								}

								ICommandControlDMContext controlDmc = DMContexts.getAncestorOfType(procCtx,
										ICommandControlDMContext.class);
								fCommandControl.queueCommand(fCommandFactory.createMIAddInferior(controlDmc),
										new ImmediateDataRequestMonitor<MIAddInferiorInfo>(rm) {
											@Override
											protected void handleSuccess() {
												final String groupId = getData().getGroupId();
												if (groupId == null || groupId.trim().length() == 0) {
													rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID,
															INTERNAL_ERROR, "Invalid gdb group id.", null)); //$NON-NLS-1$
												} else {
													fContainerDmc = createContainerContext(procCtx, groupId);
												}
												rm.done();
											}
										});
							}
						}, new Step() {
							@Override
							public void execute(final RequestMonitor rm) {
								// Because of a GDB 7.2 bug, for remote-attach sessions,
								// we need to be disconnected from the target
								// when we set the very first binary to be used.
								// So, lets disconnect.
								// Bug 352998
								if (needFixForGDB72Bug352998()) {
									// The bug only applies to remote sessions
									if (fBackend.getSessionType() == SessionType.REMOTE) {
										assert fBackend.getIsAttachSession();
										assert binaryPath != null;

										// We only need the workaround for the very first process we attach to
										if (isInitialProcess()) {
											ICommandControlDMContext controlDmc = DMContexts.getAncestorOfType(procCtx,
													ICommandControlDMContext.class);
											fCommandControl.queueCommand(
													fCommandFactory.createMITargetDisconnect(controlDmc),
													new ImmediateDataRequestMonitor<MIInfo>(rm) {
														@Override
														protected void handleSuccess() {
															fNeedToReconnect = true;
															rm.done();
														}
													});
											return;
										}
									}
								}

								rm.done();
							}
						}, new Step() {
							@Override
							public void execute(final RequestMonitor rm) {
								// Now, set the binary to be used.
								if (binaryPath != null) {
									fCommandControl.queueCommand(
											fCommandFactory.createMIFileExecAndSymbols(fContainerDmc, binaryPath),
											new ImmediateDataRequestMonitor<MIInfo>(rm) {
												@Override
												protected void handleCompleted() {
													// Because of a GDB 7.2 bug, for remote-attach sessions,
													// we need to be disconnected from the target
													// when we set the very first binary to be used.
													// Now that we have disconnected and set the binary,
													// we may need to reconnect to the target.
													// If we were unable to set the binary (e.g., if the specified path
													// is invalid) we also need to reconnect to the target before
													// aborting the rest of the sequence.
													// Bug 352998

													if (fNeedToReconnect) {
														fNeedToReconnect = false;

														// Set the status in case it is an error, so that when rm.done() is automatically
														// called, we continue to abort the sequence if we are dealing with a failure.
														rm.setStatus(getStatus());

														connectToTarget(procCtx, rm);
													} else {
														super.handleCompleted();
													}
												}
											});
									return;
								}

								assert fNeedToReconnect == false;
								rm.done();
							}
						},
						// Now, actually do the attach
						new Step() {
							@Override
							public void execute(RequestMonitor rm) {
								if (fBackend.getSessionType() == SessionType.REMOTE && isInitialProcess()) {
									// Uncomment following and remove rm.done() once FinalLaunchSequence.stepRemoteConnection() is removed
									//									connectToTarget(procCtx, rm);
									rm.done();
									return;
								}
								// For non-stop mode, we do a non-interrupting attach
								// Bug 333284
								boolean shouldInterrupt = true;
								IMIRunControl runControl = getServicesTracker().getService(IMIRunControl.class);
								if (runControl != null && runControl.getRunMode() == MIRunMode.NON_STOP) {
									shouldInterrupt = false;
								}

								boolean extraNewline = targetAttachRequiresTrailingNewline();
								ICommand<MIInfo> miTargetAttach = fCommandFactory.createMITargetAttach(fContainerDmc,
										((IMIProcessDMContext) procCtx).getProcId(), shouldInterrupt, extraNewline);
								fCommandControl.queueCommand(miTargetAttach,
										new ImmediateDataRequestMonitor<MIInfo>(rm));
							}

						},
						// Initialize memory data for this process.
						new Step() {
							@Override
							public void execute(RequestMonitor rm) {
								IGDBMemory memory = getServicesTracker().getService(IGDBMemory.class);
								IMemoryDMContext memContext = DMContexts.getAncestorOfType(fContainerDmc,
										IMemoryDMContext.class);
								if (memory == null || memContext == null) {
									rm.done();
									return;
								}
								memory.initializeMemoryData(memContext, rm);
							}
						},
						// Start tracking this process' breakpoints.
						new Step() {
							@Override
							public void execute(RequestMonitor rm) {
								MIBreakpointsManager bpmService = getServicesTracker()
										.getService(MIBreakpointsManager.class);
								bpmService.startTrackingBpForProcess(fContainerDmc, rm);
							}
						},
						// Turn on reverse debugging if it was enabled as a launch option
						new Step() {
							@Override
							public void execute(RequestMonitor rm) {
								doReverseDebugStep(procCtx, rm);
							}
						},
						// Store the fully formed container context so it can be returned to the caller
						// and mark that we are not dealing with the first process anymore.
						new Step() {
							@Override
							public void execute(RequestMonitor rm) {
								dataRm.setData(fContainerDmc);
								setIsInitialProcess(false);

								rm.done();
							}
						}, };

				@Override
				public Step[] getSteps() {
					return steps;
				}
			});
		} else {
			dataRm.setStatus(
					new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INTERNAL_ERROR, "Invalid process context.", null)); //$NON-NLS-1$
			dataRm.done();
		}
	}

	/**
	 * GDB 7.11 had a bug that -target-attach sometimes did not flush its error
	 * response. However sending a newline forced GDB to flush the buffer.
	 *
	 * See Bug 522367
	 *
	 * @return whether to add extra newline.
	 * @since 5.4
	 */
	protected boolean targetAttachRequiresTrailingNewline() {
		return false;
	}

	@Override
	public void detachDebuggerFromProcess(IDMContext dmc, final RequestMonitor rm) {

		MIExitedProcessDMC exitedProc = DMContexts.getAncestorOfType(dmc, MIExitedProcessDMC.class);
		if (exitedProc != null) {
			super.detachDebuggerFromProcess(dmc, rm);
			return;
		}

		ICommandControlDMContext controlDmc = DMContexts.getAncestorOfType(dmc, ICommandControlDMContext.class);
		final IMIContainerDMContext containerDmc = DMContexts.getAncestorOfType(dmc, IMIContainerDMContext.class);

		if (controlDmc != null && containerDmc != null) {
			if (!doCanDetachDebuggerFromProcess()) {
				rm.setStatus(
						new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INTERNAL_ERROR, "Detach not supported.", null)); //$NON-NLS-1$
				rm.done();
				return;
			}

			IMIRunControl runControl = getServicesTracker().getService(IMIRunControl.class);
			if (runControl != null && !runControl.isTargetAcceptingCommands()) {
				fBackend.interrupt();
			}

			// Remember that this process was detached so we don't show it as an exited process.
			// We must set this before sending the detach command to gdb to avoid race conditions
			getDetachedProcesses().add(containerDmc.getGroupId());
			fCommandControl.queueCommand(fCommandFactory.createMITargetDetach(controlDmc, containerDmc.getGroupId()),
					new DataRequestMonitor<MIInfo>(getExecutor(), rm) {
						@Override
						protected void handleCompleted() {
							if (isSuccess()) {
								// Bug in GDB 7.2 where removing an inferior will lead to a crash when running other processes.
								// I'm hoping it will be fixed in 7.2.1
								//    			        	fCommandControl.queueCommand(
								//    			        			fCommandFactory.createMIRemoveInferior(fCommandControl.getContext(), containerDmc.getGroupId()),
								//    			    				new DataRequestMonitor<MIInfo>(getExecutor(), rm));
								rm.done();
							} else {
								// This command fails with GDB 7.2 because of a GDB bug, which was fixed with GDB 7.2.1
								// In case we get here, we assume we are using GDB 7.2 (although we should not) and we work
								// around it.
								// Also, with GDB 7.2, removing the inferior does not work because of another bug, so we just don't do it.
								fCommandControl.queueCommand(fCommandFactory.createMITargetDetach(containerDmc),
										new DataRequestMonitor<MIInfo>(getExecutor(), rm) {
											@Override
											protected void handleFailure() {
												// Detach failed
												getDetachedProcesses().remove(containerDmc.getGroupId());
												super.handleFailure();
											}
										});
							}
						}
					});
		} else {
			rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INTERNAL_ERROR, "Invalid context.", null)); //$NON-NLS-1$
			rm.done();
		}
	}

	@Override
	protected boolean doIsDebugNewProcessSupported() {
		// Multi-process is not applicable to post-mortem sessions (core)
		// or to non-attach remote sessions.
		SessionType type = fBackend.getSessionType();

		if (type == SessionType.CORE) {
			return false;
		}

		if (type == SessionType.REMOTE && !fBackend.getIsAttachSession()) {
			return false;
		}

		// Multi-process does not work for all-stop right now
		IMIRunControl runControl = getServicesTracker().getService(IMIRunControl.class);
		if (runControl != null && runControl.getRunMode() == MIRunMode.ALL_STOP) {
			// Only one process is allowed in all-stop (for now)
			return getNumConnected() == 0;
			// NOTE: when we support multi-process in all-stop mode,
			// we will need to interrupt the target to when starting
			// the new process.
		}

		return true;
	}

	@Override
	protected Sequence getDebugNewProcessSequence(DsfExecutor executor, boolean isInitial, IDMContext dmc, String file,
			Map<String, Object> attributes, DataRequestMonitor<IDMContext> rm) {
		return new DebugNewProcessSequence_7_2(executor, isInitial, dmc, file, attributes, rm);
	}

	@Override
	public void getProcessesBeingDebugged(final IDMContext dmc, final DataRequestMonitor<IDMContext[]> rm) {
		if (getTraceVisualization()) {
			// If we are visualizing data during a live session, we should not ask GDB for the list of threads,
			// because we will get the list of active threads, while GDB only cares about thread 1 for visualization.
			final IMIContainerDMContext containerDmc = DMContexts.getAncestorOfType(dmc, IMIContainerDMContext.class);
			if (containerDmc != null) {
				IProcessDMContext procDmc = DMContexts.getAncestorOfType(containerDmc, IProcessDMContext.class);
				rm.setData(new IMIExecutionDMContext[] { createExecutionContext(containerDmc,
						createThreadContext(procDmc, TRACE_VISUALIZATION_THREAD_ID), TRACE_VISUALIZATION_THREAD_ID) });
				rm.done();
				return;
			}
		}

		super.getProcessesBeingDebugged(dmc, rm);
	}

	/**
	 * Creates the container context that is to be used for the new process that will
	 * be created by the restart operation.
	 * This container does not have its pid yet, while the container of the process
	 * that is being restarted does have its pid.
	 * Starting with GDB 7.2, the groupId stays the same when restarting a process, so
	 * we should re-use it; this is particularly important since we support multi-process
	 * and we need the proper groupId
	 *
	 * @since 4.0
	 */
	@Override
	protected IMIContainerDMContext createContainerContextForRestart(String groupId) {
		IProcessDMContext processDmc = createProcessContext(fCommandControl.getContext(),
				MIProcesses.UNKNOWN_PROCESS_ID);
		return createContainerContext(processDmc, groupId);
	}

	@Override
	public void restart(final IContainerDMContext containerDmc, Map<String, Object> attributes,
			DataRequestMonitor<IContainerDMContext> rm) {
		fProcRestarting.add(containerDmc);
		super.restart(containerDmc, attributes, new ImmediateDataRequestMonitor<IContainerDMContext>(rm) {
			@Override
			protected void handleCompleted() {
				if (!isSuccess()) {
					fProcRestarting.remove(containerDmc);
				}
				setData(getData());
				super.handleCompleted();
			}
		});
	}

	/**
	 * @since 5.1
	 */
	@DsfServiceEventHandler
	public void eventDispatched(MIThreadGroupAddedEvent e) {
		IProcessDMContext procDmc = e.getDMContext();
		IMIContainerDMContext containerDmc = e.getGroupId() != null ? createContainerContext(procDmc, e.getGroupId())
				: null;
		getSession().dispatchEvent(new ContainerCreatedDMEvent(containerDmc), getProperties());
	}

	/** @since 4.0 */
	@DsfServiceEventHandler
	@Override
	public void eventDispatched(IExitedDMEvent e) {
		IDMContext dmc = e.getDMContext();

		if (dmc instanceof IContainerDMContext) {
			MIBreakpointsManager bpmService = getServicesTracker().getService(MIBreakpointsManager.class);

			// Time to remove the tracking of a restarting process
			boolean restarting = fProcRestarting.remove(dmc);

			if (bpmService != null) {
				if (!restarting) {
					// Process exited, remove it from the thread break point filtering
					bpmService.removeTargetFilter((IContainerDMContext) dmc);

					if (dmc instanceof IBreakpointsTargetDMContext) {
						// A process has died, we should stop tracking its breakpoints, but only if it is not restarting
						// We only do this when the process is a breakpointTargetDMC itself (GDB < 7.4);
						// we don't want to stop tracking breakpoints when breakpoints are only set once
						// for all processes (GDB >= 7.4)
						if (fBackend.getSessionType() != SessionType.CORE) {
							IBreakpointsTargetDMContext bpTargetDmc = (IBreakpointsTargetDMContext) dmc;
							bpmService.stopTrackingBreakpoints(bpTargetDmc, new ImmediateRequestMonitor() {
								@Override
								protected void handleCompleted() {
									// Ok, no need to report any error because we may have already shutdown.
									// We need to override handleCompleted to avoid risking having a error printout in the log
								}
							});
						}
					}
				}
			}
		}

		super.eventDispatched(e);
	}

	/**
	 * GDB 7.2 has a bug which causes a gdbserver crash if we set the binary after we
	 * have connected to the target.  Because GDB 7.2.1 was not released when CDT 8.0
	 * was released, we need to workaround the bug in Eclipse.
	 *
	 * This method can be overridden to easily disable the workaround, for versions
	 * of GDB that no longer have the bug.
	 *
	 * See http://sourceware.org/ml/gdb-patches/2011-03/msg00531.html
	 * and Bug 352998
	 *
	 * @since 4.1
	 */
	protected boolean needFixForGDB72Bug352998() {
		return true;
	}

	/**
	 * @since 4.1
	 */
	@DsfServiceEventHandler
	public void eventDispatched(ITraceRecordSelectedChangedDMEvent e) {
		setTraceVisualization(e.isVisualizationModeEnabled());
	}

	/**
	 * @since 4.6
	 */
	@Override
	public void canDetachDebuggerFromSomeProcesses(IDMContext[] dmcs, final DataRequestMonitor<Boolean> rm) {
		canDetachFromProcesses(dmcs, false, rm);
	}

	/**
	 * @since 4.6
	 */
	@Override
	public void canDetachDebuggerFromAllProcesses(IDMContext[] dmcs, DataRequestMonitor<Boolean> rm) {
		canDetachFromProcesses(dmcs, true, rm);
	}

	/**
	 * @since 4.6
	 */
	protected void canDetachFromProcesses(IDMContext[] dmcs, boolean all, DataRequestMonitor<Boolean> rm) {
		Set<IMIContainerDMContext> contDmcs = new HashSet<>();
		for (IDMContext c : dmcs) {
			IMIContainerDMContext contDmc = DMContexts.getAncestorOfType(c, IMIContainerDMContext.class);
			if (contDmc != null) {
				contDmcs.add(contDmc);
			}
		}

		Iterator<IMIContainerDMContext> it = contDmcs.iterator();
		if (!it.hasNext()) {
			rm.setData(false);
			rm.done();
			return;
		}
		canDetachDebuggerFromProcess(it.next(), new CanDetachRequestMonitor(it, all, rm));
	}

	/**
	 * @since 4.6
	 */
	@Override
	public void detachDebuggerFromProcesses(IDMContext[] dmcs, final RequestMonitor rm) {
		Set<IMIContainerDMContext> contDmcs = new HashSet<>();
		for (IDMContext c : dmcs) {
			IMIContainerDMContext contDmc = DMContexts.getAncestorOfType(c, IMIContainerDMContext.class);
			if (contDmc != null) {
				contDmcs.add(contDmc);
			}
		}
		if (contDmcs.isEmpty()) {
			rm.done();
			return;
		}

		CountingRequestMonitor crm = new CountingRequestMonitor(ImmediateExecutor.getInstance(), rm);
		crm.setDoneCount(contDmcs.size());
		for (IMIContainerDMContext contDmc : contDmcs) {
			detachDebuggerFromProcess(contDmc, crm);
		}
	}

	/**
	 * @since 4.6
	 */
	@Override
	public void canTerminateSome(IThreadDMContext[] dmcs, DataRequestMonitor<Boolean> rm) {
		canTerminate(dmcs, false, rm);
	}

	/**
	 * @since 4.6
	 */
	@Override
	public void canTerminateAll(IThreadDMContext[] dmcs, DataRequestMonitor<Boolean> rm) {
		canTerminate(dmcs, true, rm);
	}

	/**
	 * @since 4.6
	 */
	protected void canTerminate(IThreadDMContext[] dmcs, boolean all, DataRequestMonitor<Boolean> rm) {
		Iterator<IThreadDMContext> it = Arrays.asList(dmcs).iterator();
		if (!it.hasNext()) {
			rm.setData(false);
			rm.done();
			return;
		}
		canTerminate(it.next(), new CanTerminateRequestMonitor(it, all, rm));
	}

	/**
	 * @since 4.6
	 */
	@Override
	public void terminate(IThreadDMContext[] dmcs, RequestMonitor rm) {
		if (dmcs.length == 0) {
			rm.done();
			return;
		}

		CountingRequestMonitor crm = new CountingRequestMonitor(ImmediateExecutor.getInstance(), rm);
		crm.setDoneCount(dmcs.length);
		for (IThreadDMContext threadDmc : dmcs) {
			terminate(threadDmc, crm);
		}
	}
}

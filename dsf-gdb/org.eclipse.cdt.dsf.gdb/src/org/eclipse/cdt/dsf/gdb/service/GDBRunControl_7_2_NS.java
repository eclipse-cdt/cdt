/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     Ericsson		      - Support for multi-process for Linux, GDB 7.2
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateExecutor;
import org.eclipse.cdt.dsf.concurrent.MultiRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Sequence;
import org.eclipse.cdt.dsf.concurrent.Sequence.Step;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.datamodel.IDMEvent;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointsTargetDMContext;
import org.eclipse.cdt.dsf.debug.service.IProcesses;
import org.eclipse.cdt.dsf.debug.service.IRunControl;
import org.eclipse.cdt.dsf.debug.service.IRunControl2;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.mi.service.IMICommandControl;
import org.eclipse.cdt.dsf.mi.service.IMIContainerDMContext;
import org.eclipse.cdt.dsf.mi.service.IMIExecutionDMContext;
import org.eclipse.cdt.dsf.mi.service.IMIRunControl;
import org.eclipse.cdt.dsf.mi.service.MIBreakpoints.MIBreakpointDMContext;
import org.eclipse.cdt.dsf.mi.service.command.CommandFactory;
import org.eclipse.cdt.dsf.mi.service.command.events.MIBreakpointHitEvent;
import org.eclipse.cdt.dsf.mi.service.command.events.MIRunningEvent;
import org.eclipse.cdt.dsf.mi.service.command.events.MISignalEvent;
import org.eclipse.cdt.dsf.mi.service.command.events.MIStoppedEvent;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * Version of the non-stop runControl for GDB 7.2
 * This class handles multi-process for Linux which requires
 * us to interrupt different processes at the same time to be
 * able to set breakpoints.
 * This class was created for bug 337893
 * @since 4.0
 */
public class GDBRunControl_7_2_NS extends GDBRunControl_7_0_NS
{

	private ICommandControlService fConnection;
	private CommandFactory fCommandFactory;

	/** 
	 * Set of threads for which the next MIRunning event should be silenced.
	 */
	private Set<IMIExecutionDMContext> fDisableNextRunningEventDmc = new HashSet<IMIExecutionDMContext>();
	/** 
	 * Set of threads for which the next MISignal (MIStopped) event should be silenced.
	 */
	private Set<IMIExecutionDMContext> fDisableNextSignalEventDmc = new HashSet<IMIExecutionDMContext>();
	/** 
	 * Map that stores the silenced MIStopped event for the specified thread, in case we need to use it for a failure.
	 */
	private Map<IMIExecutionDMContext,MIStoppedEvent> fSilencedSignalEvent = new HashMap<IMIExecutionDMContext, MIStoppedEvent>();

	private Map<IDMContext, ExecuteWithTargetAvailableOperation> execWithTargetAvailMap = new HashMap<IDMContext, ExecuteWithTargetAvailableOperation>();

	///////////////////////////////////////////////////////////////////////////
	// Initialization and shutdown
	///////////////////////////////////////////////////////////////////////////

	public GDBRunControl_7_2_NS(DsfSession session) {
		super(session);
	}

	@Override
	public void initialize(final RequestMonitor rm) {
		super.initialize(new RequestMonitor(ImmediateExecutor.getInstance(), rm) {
			@Override
			protected void handleSuccess() {
				doInitialize(rm);
			}
		});
	}

	private void doInitialize(final RequestMonitor rm) {
		register(new String[]{ IRunControl.class.getName(), 
				IRunControl2.class.getName(),
				IMIRunControl.class.getName(),
				GDBRunControl_7_0_NS.class.getName(),
				GDBRunControl_7_2_NS.class.getName(),
		}, 
		new Hashtable<String,String>());
		fConnection = getServicesTracker().getService(ICommandControlService.class);
		fCommandFactory = getServicesTracker().getService(IMICommandControl.class).getCommandFactory();
		getSession().addServiceEventListener(this, null);
		rm.done();
	}

	@Override
	public void shutdown(final RequestMonitor rm) {
		unregister();
		getSession().removeServiceEventListener(this);
		super.shutdown(rm);
	}

	@Override
	public void suspend(final IExecutionDMContext context, final RequestMonitor rm) {
		assert context != null;

		IMIExecutionDMContext thread = DMContexts.getAncestorOfType(context, IMIExecutionDMContext.class);
		IMIContainerDMContext container = DMContexts.getAncestorOfType(context, IMIContainerDMContext.class);
		if (thread == null && container == null) {
			rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, NOT_SUPPORTED, "Invalid context type.", null)); //$NON-NLS-1$
			rm.done();
			return;
		}

		canSuspend(context, new DataRequestMonitor<Boolean>(ImmediateExecutor.getInstance(), rm) {
			@Override
			protected void handleSuccess() {
				if (getData()) {
					fConnection.queueCommand(fCommandFactory.createMIExecInterrupt(context), new DataRequestMonitor<MIInfo>(getExecutor(), rm));
				} else {
					rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, NOT_SUPPORTED,
							"Given context: " + context + ", is already suspended.", null)); //$NON-NLS-1$ //$NON-NLS-2$
					rm.done();
				}
			}
		});
	}

	@Override
	public void resume(final IExecutionDMContext context, final RequestMonitor rm) {
		assert context != null;

		final IMIExecutionDMContext thread = DMContexts.getAncestorOfType(context, IMIExecutionDMContext.class);
		final IMIContainerDMContext container = DMContexts.getAncestorOfType(context, IMIContainerDMContext.class);
		if (thread == null && container == null) {
			rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, NOT_SUPPORTED, "Invalid context type.", null)); //$NON-NLS-1$
			rm.done();
			return;
		}

		canResume(context, new DataRequestMonitor<Boolean>(ImmediateExecutor.getInstance(), rm) {
			@Override
			protected void handleSuccess() {
				if (getData()) {
					if (thread != null) {
						doResumeThread(thread, rm);
						return;
					}

					if (container != null) {
						doResumeContainer(container, rm);
						return;
					}
				} else {
					rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, NOT_SUPPORTED,
							"Given context: " + context + ", is already running.", null)); //$NON-NLS-1$ //$NON-NLS-2$
					rm.done();
				}
			}
		});
	}

	private void doResumeThread(IMIExecutionDMContext context, final RequestMonitor rm) {
		final MIThreadRunState threadState = fThreadRunStates.get(context);
		if (threadState == null) {
            rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_STATE,
                "Given context: " + context + " can't be found.", null)); //$NON-NLS-1$ //$NON-NLS-2$
			rm.done();
			return;
		}
		
		threadState.fResumePending = true;
		fConnection.queueCommand(fCommandFactory.createMIExecContinue(context), new DataRequestMonitor<MIInfo>(getExecutor(), rm) {
			@Override
			protected void handleFailure() {
				threadState.fResumePending = false;
				super.handleFailure();
			}
		});
	}

	private void doResumeContainer(IMIContainerDMContext context, final RequestMonitor rm) {
		fConnection.queueCommand(fCommandFactory.createMIExecContinue(context), new DataRequestMonitor<MIInfo>(getExecutor(), rm));
	}
	
	@Override
	public void executeWithTargetAvailable(IDMContext ctx, final Sequence.Step[] steps, final RequestMonitor rm) {
		ExecuteWithTargetAvailableOperation operation = execWithTargetAvailMap.get(ctx);
		if (operation == null) {
			operation = new ExecuteWithTargetAvailableOperation(ctx);
			execWithTargetAvailMap.put(ctx, operation);
		}
		operation.executeWithTargetAvailable(steps, rm);
	}

	/* ******************************************************************************
	 * Section to support making operations even when the target is unavailable.
	 *
	 * Although one would expect to be able to make commands all the time when
	 * in non-stop mode, it turns out that GDB has trouble with some commands
	 * like breakpoints.  The safe way to do it is to make sure we have at least
	 * one thread suspended.
	 * 
	 * Basically, we must make sure one thread is suspended before making
	 * certain operations (currently breakpoints).  If that is not the case, we must 
	 * first suspend one thread, then perform the specified operations,
	 * and finally resume that thread..
	 * See https://bugs.eclipse.org/bugs/show_bug.cgi?id=242943
	 * and https://bugs.eclipse.org/bugs/show_bug.cgi?id=282273
	 * 
	 * Note that for multi-process on Linux, the correct container must be suspended for 
     * the breakpoint to be inserted on that container.  This means that we need to be
     * able to interrupt multiple processes at the same time to insert a breakpoint
     * in each one of them.
     * See http://bugs.eclipse.org/337893
	 * ******************************************************************************/

	protected class ExecuteWithTargetAvailableOperation {
		/**
		 * Utility class to store the parameters of the executeWithTargetAvailable() operations.
		 */
		public class TargetAvailableOperationInfo {
			public Sequence.Step[] steps;
			public RequestMonitor rm;

			public TargetAvailableOperationInfo(Step[] steps, RequestMonitor rm) {
				super();
				this.steps = steps;
				this.rm = rm;
			}
		};

		public IDMContext fCtx;
		// Keep track of if the target was available or not when we started the operation
		public boolean fTargetAvailable;
		// The container that needs to be suspended to perform the steps of the operation
		public IContainerDMContext fContainerDmcToSuspend;
		// The thread that we will actually suspend to make the container suspended.
		public IMIExecutionDMContext fExecutionDmcToSuspend;

		// Do we currently have an executeWithTargetAvailable() operation ongoing?
		public boolean fOngoingOperation;
		// Are we currently executing steps passed into executeWithTargetAvailable()?
		// This allows us to know if we can add more steps to execute or if we missed
		// our opportunity
		public boolean fCurrentlyExecutingSteps;

		// MultiRequestMonitor that allows us to track all the different steps we are
		// executing.  Once all steps are executed, we can complete this MultiRM and
		// allow the global sequence to continue.
		// Note that we couldn't use a CountingRequestMonitor because that type of RM
		// needs to know in advance how many subRms it will track; the MultiRM allows us
		// to receive more steps to execute continuously, and be able to upate the MultiRM.
		public MultiRequestMonitor<RequestMonitor> fExecuteQueuedOpsStepMonitor;
		// The number of batches of steps that are still being executing for potentially
		// concurrent executeWithTargetAvailable() operations.
		// Once this gets to zero, we know we have executed all the steps we were aware of
		// and we can complete the operation.
		public int fNumStepsStillExecuting;
		// Queue of executeWithTargetAvailable() operations that need to be processed.
		public LinkedList<TargetAvailableOperationInfo> fOperationsPending = new LinkedList<TargetAvailableOperationInfo>();

		public ExecuteWithTargetAvailableOperation(IDMContext ctx) {
			fCtx = ctx;
		}

		/**
		 * This method takes care of executing a batch of steps that were passed to
		 * ExecuteWithTargetAvailable().  The method is used to track the progress
		 * of all these batches of steps, so that we know exactly when all of them
		 * have been completed and the global sequence can be completed.
		 */
		protected void executeSteps(final TargetAvailableOperationInfo info) {
			fNumStepsStillExecuting++;

			// This RM propagates any error to the original rm of the actual steps.
			// Even in case of errors for these steps, we want to continue the overall sequence
			RequestMonitor stepsRm = new RequestMonitor(ImmediateExecutor.getInstance(), null) {
				@Override
				protected void handleCompleted() {
					info.rm.setStatus(getStatus());
					// It is important to call rm.done() right away.
					// This is because some other operation we are performing might be waiting
					// for this one to be done.  If we try to wait for the entire sequence to be
					// done, then we will never finish because one monitor will never show as
					// done, waiting for the second one.
					info.rm.done();

					fExecuteQueuedOpsStepMonitor.requestMonitorDone(this);
					fNumStepsStillExecuting--;
					if (fNumStepsStillExecuting == 0) {
						fExecuteQueuedOpsStepMonitor.doneAdding();
					}
				}
			};

			fExecuteQueuedOpsStepMonitor.add(stepsRm);

			getExecutor().execute(new Sequence(getExecutor(), stepsRm) {
				@Override public Step[] getSteps() { return info.steps; }
			});	
		}

		public void executeWithTargetAvailable(final Sequence.Step[] steps, final RequestMonitor rm) {
			if (!fOngoingOperation) {
				// We are the first operation of this kind currently requested
				// so we need to start the sequence
				fOngoingOperation = true;

				// We always go through our queue, even if we only have a single call to this method
				fOperationsPending.add(new TargetAvailableOperationInfo(steps, rm));

				// Steps that need to be executed to perform the operation
				final Step[] sequenceSteps = new Step[] {
						new IsTargetAvailableStep(),
						new MakeTargetAvailableStep(),
						new ExecuteQueuedOperationsStep(),
						new RestoreTargetStateStep(),
				};

				// Once all the sequence is completed, we need to see if we have received
				// another request that we now need to process
				RequestMonitor sequenceCompletedRm = new RequestMonitor(getExecutor(), null) {
					@Override
					protected void handleCompleted() {
						fOngoingOperation = false;

						if (fOperationsPending.size() > 0) {
							// Darn, more operations came in.  Trigger their processing
							// by calling executeWithTargetAvailable() on the last one
							TargetAvailableOperationInfo info = fOperationsPending.removeLast();
							executeWithTargetAvailable(info.steps, info.rm);
						}
						// no other rm.done() needs to be called, they have all been handled already
					}
				};

				getExecutor().execute(new Sequence(getExecutor(), sequenceCompletedRm) {
					@Override public Step[] getSteps() { return sequenceSteps; }
				});
			} else {
				// We are currently already executing such an operation
				// If we are still in the process of executing steps, let's include this new set of steps.
				// This is important because some steps may depend on these new ones.
				if (fCurrentlyExecutingSteps) {
					executeSteps(new TargetAvailableOperationInfo(steps, rm));
				} else {
					// Too late to execute the new steps, so queue them for later
					fOperationsPending.add(new TargetAvailableOperationInfo(steps, rm));
				}
			}
		}


		/**
		 * This part of the sequence verifies if the execution context of interest
		 * is suspended or not.
		 */
		public class IsTargetAvailableStep extends Sequence.Step {
			@Override
			public void execute(final RequestMonitor rm) {
				fContainerDmcToSuspend = DMContexts.getAncestorOfType(fCtx, IMIContainerDMContext.class);
				if (fContainerDmcToSuspend != null) {
					fTargetAvailable = isSuspended(fContainerDmcToSuspend);
					rm.done();
					return;
				}

				// If we get here, we have to get the list of processes to know if any of
				// them is suspended.
				ICommandControlDMContext controlDmc = DMContexts.getAncestorOfType(fCtx, ICommandControlDMContext.class);
				IProcesses processControl = getServicesTracker().getService(IProcesses.class);
				processControl.getProcessesBeingDebugged(
						controlDmc,
						new DataRequestMonitor<IDMContext[]>(getExecutor(), rm) {
							@Override
							protected void handleSuccess() {
								assert getData() != null;

								if (getData().length == 0) {
									// Happens at startup, starting with GDB 7.0.
									// This means the target is available
									fTargetAvailable = true;
								} else {
									fTargetAvailable = false;
									// Choose the first process as the one to suspend if needed
									fContainerDmcToSuspend = (IContainerDMContext)(getData()[0]);
									for (IDMContext containerDmc : getData()) {
										if (isSuspended((IContainerDMContext)containerDmc)) {
											fTargetAvailable = true;
											break;
										}
									}
								}
								rm.done();
							}
						});
			}
		};

		/**
		 * If the execution context of interest is not suspended, this step
		 * will interrupt it.
		 */
		public class MakeTargetAvailableStep extends Sequence.Step {
			@Override
			public void execute(final RequestMonitor rm) {
				if (!fTargetAvailable) {
					// Instead of suspending the entire process, let's find its first thread and use that
					IProcesses processControl = getServicesTracker().getService(IProcesses.class);
					processControl.getProcessesBeingDebugged(
							fContainerDmcToSuspend,
							new DataRequestMonitor<IDMContext[]>(getExecutor(), rm) {
								@Override
								protected void handleSuccess() {
									assert getData() != null;
									assert getData().length > 0;

									fExecutionDmcToSuspend = (IMIExecutionDMContext)getData()[0];

									assert !fDisableNextRunningEventDmc.contains(fExecutionDmcToSuspend);
									assert !fDisableNextSignalEventDmc.contains(fExecutionDmcToSuspend);

									// Don't broadcast the next stopped signal event
									fDisableNextSignalEventDmc.add(fExecutionDmcToSuspend);

									suspend(fExecutionDmcToSuspend,
											new RequestMonitor(getExecutor(), rm) {
										@Override
										protected void handleFailure() {
											// We weren't able to suspend, so abort the operation
											fDisableNextSignalEventDmc.remove(fExecutionDmcToSuspend);
											super.handleFailure();
										};
									});
								}
							});
				} else {
					rm.done();
				}
			}
			@Override
			public void rollBack(RequestMonitor rm) {
				Sequence.Step restoreStep = new RestoreTargetStateStep();
				restoreStep.execute(rm);
			}
		};

		/**
		 * This step of the sequence takes care of executing all the steps that
		 * were passed to ExecuteWithTargetAvailable().
		 */
		public class ExecuteQueuedOperationsStep extends Sequence.Step {
			@Override
			public void execute(final RequestMonitor rm) {
				fCurrentlyExecutingSteps = true;

				// It is important to use an ImmediateExecutor for this RM, to make sure we don't risk getting a new
				// call to ExecuteWithTargetAvailable() when we just finished executing the steps.
				fExecuteQueuedOpsStepMonitor = new MultiRequestMonitor<RequestMonitor>(ImmediateExecutor.getInstance(), rm) {
					@Override
					protected void handleCompleted() {
						assert fOperationsPending.size() == 0;

						// We don't handle errors here.  Instead, we have already propagated any
						// errors to each rm for each set of steps

						fCurrentlyExecutingSteps = false;
						// Continue the sequence
						rm.done();
					}
				};
				// Tell the RM that we need to confirm when we are done adding sub-rms
				fExecuteQueuedOpsStepMonitor.requireDoneAdding();

				// All pending operations are independent of each other so we can
				// run them concurrently.
				while (fOperationsPending.size() > 0) {
					executeSteps(fOperationsPending.poll());				
				}
			}
		};

		/**
		 * If the sequence had to interrupt the execution context of interest,
		 * this step will resume it again to reach the same state as when we started.
		 */
		public class RestoreTargetStateStep extends Sequence.Step {
			@Override
			public void execute(final RequestMonitor rm) {
				if (!fTargetAvailable) {
					assert !fDisableNextRunningEventDmc.contains(fExecutionDmcToSuspend);
					fDisableNextRunningEventDmc.add(fExecutionDmcToSuspend);

					// Can't use the resume() call because we 'silently' stopped
					// so resume() will not know we are actually stopped
					fConnection.queueCommand(
							fCommandFactory.createMIExecContinue(fExecutionDmcToSuspend),
							new DataRequestMonitor<MIInfo>(getExecutor(), rm) {
								@Override
								protected void handleSuccess() {
									fSilencedSignalEvent.remove(fExecutionDmcToSuspend);
									super.handleSuccess();
								}

								@Override
								protected void handleFailure() {
									// Darn, we're unable to restart the target.  Must cleanup!
									fDisableNextRunningEventDmc.remove(fExecutionDmcToSuspend);

									// We must also sent the Stopped event that we had kept silent
									MIStoppedEvent event = fSilencedSignalEvent.remove(fExecutionDmcToSuspend);
									if (event != null) {
										eventDispatched(event);
									} else {
										// Maybe the stopped event didn't arrive yet.
										// We don't want to silence it anymore
										fDisableNextSignalEventDmc.remove(fExecutionDmcToSuspend);
									}

									super.handleFailure();
								}
							});

				} else {
					// We didn't suspend the thread, so we don't need to resume it
					rm.done();
				}
			}
		};

	}
	/* ******************************************************************************
	 * End of section to support operations even when the target is unavailable.
	 * ******************************************************************************/

	///////////////////////////////////////////////////////////////////////////
	// Event handlers
	///////////////////////////////////////////////////////////////////////////

	/**
	 * @nooverride This method is not intended to be re-implemented or extended by clients.
	 * @noreference This method is not intended to be referenced by clients.
	 */
	@Override
	@DsfServiceEventHandler
	public void eventDispatched(final MIRunningEvent e) {
		if (fDisableNextRunningEventDmc.remove(e.getDMContext())) {
			// Don't broadcast the running event
			return;
		}
        getSession().dispatchEvent(new ResumedEvent(e.getDMContext(), e), getProperties());
	}

	/**
	 * @nooverride This method is not intended to be re-implemented or extended by clients.
	 * @noreference This method is not intended to be referenced by clients.
	 */
	@Override
	@DsfServiceEventHandler
	public void eventDispatched(final MIStoppedEvent e) {
		if (getRunToLineActiveOperation() != null) {
			// First check if it is the right thread that stopped
			IMIExecutionDMContext threadDmc = DMContexts.getAncestorOfType(e.getDMContext(), IMIExecutionDMContext.class);
			if (getRunToLineActiveOperation().getThreadContext().equals(threadDmc)) {
				int bpId = 0;
				if (e instanceof MIBreakpointHitEvent) {
					bpId = ((MIBreakpointHitEvent)e).getNumber();
				}
				String fileLocation = e.getFrame().getFile() + ":" + e.getFrame().getLine();  //$NON-NLS-1$
				String addrLocation = e.getFrame().getAddress();
				// Here we check three different things to see if we are stopped at the right place
				// 1- The actual location in the file.  But this does not work for breakpoints that
				//    were set on non-executable lines
				// 2- The address where the breakpoint was set.  But this does not work for breakpoints
				//    that have multiple addresses (GDB returns <MULTIPLE>.)  I think that is for multi-process
				// 3- The breakpoint id that was hit.  But this does not work if another breakpoint
				//    was also set on the same line because GDB may return that breakpoint as being hit.
				//
				// So this works for the large majority of cases.  The case that won't work is when the user
				// does a runToLine to a line that is non-executable AND has another breakpoint AND
				// has multiple addresses for the breakpoint.  I'm mean, come on!
				if (fileLocation.equals(getRunToLineActiveOperation().getFileLocation()) ||
						addrLocation.equals(getRunToLineActiveOperation().getAddrLocation()) ||
						bpId == getRunToLineActiveOperation().getBreakointId()) {
					// We stopped at the right place.  All is well.
					setRunToLineActiveOperation(null);
				} else {
					// The right thread stopped but not at the right place yet
					if (getRunToLineActiveOperation().shouldSkipBreakpoints() && e instanceof MIBreakpointHitEvent) {
						fConnection.queueCommand(
								fCommandFactory.createMIExecContinue(getRunToLineActiveOperation().getThreadContext()),
								new DataRequestMonitor<MIInfo>(getExecutor(), null));

						// Don't send the stop event since we are resuming again.
						return;
					} else {
						// Stopped for any other reasons.  Just remove our temporary one
						// since we don't want it to hit later
						//
						// Note that in Non-stop, we don't cancel a run-to-line when a new
						// breakpoint is inserted.  This is because the new breakpoint could
						// be for another thread altogether and should not affect the current thread.
						IBreakpointsTargetDMContext bpDmc = DMContexts.getAncestorOfType(getRunToLineActiveOperation().getThreadContext(),
								IBreakpointsTargetDMContext.class);

						fConnection.queueCommand(fCommandFactory.createMIBreakDelete(bpDmc, new int[] {getRunToLineActiveOperation().getBreakointId()}),
								new DataRequestMonitor<MIInfo>(getExecutor(), null));
						setRunToLineActiveOperation(null);
					}
				}
			}
		}
		
		IMIExecutionDMContext threadDmc = DMContexts.getAncestorOfType(e.getDMContext(), IMIExecutionDMContext.class);
		if (e instanceof MISignalEvent && fDisableNextSignalEventDmc.remove(threadDmc)) {			
			fSilencedSignalEvent.put(threadDmc, e);

			// Don't broadcast the stopped event
			return;
		}

		IDMEvent<?> event = null;
		MIBreakpointDMContext bp = null;
		if (e instanceof MIBreakpointHitEvent) {
			int bpId = ((MIBreakpointHitEvent)e).getNumber();
			IBreakpointsTargetDMContext bpsTarget = DMContexts.getAncestorOfType(e.getDMContext(), IBreakpointsTargetDMContext.class);
			if (bpsTarget != null && bpId >= 0) {
				bp = new MIBreakpointDMContext(getSession().getId(), new IDMContext[] {bpsTarget}, bpId); 
				event = new BreakpointHitEvent(e.getDMContext(), (MIBreakpointHitEvent)e, bp);
			}
		}
		if (event == null) {
			event = new SuspendedEvent(e.getDMContext(), e);
		}

		getSession().dispatchEvent(event, getProperties());
	}
}

/*******************************************************************************
 * Copyright (c) 2008, 2015 Wind River Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     Ericsson		      - Modified for additional functionality
 *     Ericsson           - Version 7.0
 *     Nokia              - create and use backend service.
 *     Ericsson           - Added IReverseControl support
 *     Marc Khouzam (Ericsson) - Added IReverseModeChangedDMEvent (Bug 399163)
 *     Marc Khouzam (Ericsson) - Started inheriting from GDBRunControl (Bug 405123)
 *     Intel Corporation - Added Reverse Debugging BTrace support
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.service;

import java.util.Hashtable;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.AbstractDMEvent;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl;
import org.eclipse.cdt.dsf.debug.service.IRunControl2;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMContext;
import org.eclipse.cdt.dsf.debug.service.command.ICommand;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.gdb.service.IGDBTraceControl.ITraceRecordSelectedChangedDMEvent;
import org.eclipse.cdt.dsf.mi.service.IMICommandControl;
import org.eclipse.cdt.dsf.mi.service.IMIExecutionDMContext;
import org.eclipse.cdt.dsf.mi.service.IMIProcesses;
import org.eclipse.cdt.dsf.mi.service.IMIRunControl;
import org.eclipse.cdt.dsf.mi.service.MIRunControl;
import org.eclipse.cdt.dsf.mi.service.MIStack;
import org.eclipse.cdt.dsf.mi.service.command.CommandFactory;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

public class GDBRunControl_7_0 extends GDBRunControl implements IReverseRunControl {

	/** @since 4.2 */
	protected static class GdbReverseModeChangedDMEvent extends AbstractDMEvent<ICommandControlDMContext>
			implements IReverseModeChangedDMEvent {
		private boolean fIsEnabled;

		public GdbReverseModeChangedDMEvent(ICommandControlDMContext context, boolean enabled) {
			super(context);
			fIsEnabled = enabled;
		}

		@Override
		public boolean isReverseModeEnabled() {
			return fIsEnabled;
		}
	}

	private IMICommandControl fCommandControl;
	private IGDBBackend fGdb;
	private IMIProcesses fProcService;
	private CommandFactory fCommandFactory;

	private boolean fReverseSupported = true;
	private boolean fReverseStepping = false;
	private boolean fReverseModeEnabled = false;

	/**
	 * This variable allows us to know if run control operation
	 * should be enabled or disabled.  Run control operations are
	 * always enabled except when dealing with post-mortem debug
	 * session, or when visualizing tracepoints.
	 */
	private boolean fRunControlOperationsEnabled = true;

	/**
	 * Indicates if reverse debugging is supported for the currend debug session.
	 * @since 5.0
	 */
	public boolean getReverseSupported() {
		return fReverseSupported;
	}

	public GDBRunControl_7_0(DsfSession session) {
		super(session);
	}

	@Override
	public void initialize(final RequestMonitor requestMonitor) {
		super.initialize(new ImmediateRequestMonitor(requestMonitor) {
			@Override
			public void handleSuccess() {
				doInitialize(requestMonitor);
			}
		});
	}

	private void doInitialize(final RequestMonitor requestMonitor) {

		fGdb = getServicesTracker().getService(IGDBBackend.class);
		fProcService = getServicesTracker().getService(IMIProcesses.class);
		fCommandControl = getServicesTracker().getService(IMICommandControl.class);
		fCommandFactory = fCommandControl.getCommandFactory();

		if (fGdb.getSessionType() == SessionType.CORE) {
			// No execution for core files, so no support for reverse
			fRunControlOperationsEnabled = false;
			fReverseSupported = false;
		}

		register(new String[] { IRunControl.class.getName(), IRunControl2.class.getName(),
				IMIRunControl.class.getName(), MIRunControl.class.getName(), IReverseRunControl.class.getName(),
				GDBRunControl.class.getName(), GDBRunControl_7_0.class.getName() }, new Hashtable<String, String>());
		requestMonitor.done();
	}

	@Override
	public void shutdown(final RequestMonitor requestMonitor) {
		unregister();
		super.shutdown(requestMonitor);
	}

	@Override
	protected boolean runControlOperationsEnabled() {
		return fRunControlOperationsEnabled;
	}

	@Override
	public void suspend(IExecutionDMContext context, final RequestMonitor rm) {
		canSuspend(context, new DataRequestMonitor<Boolean>(getExecutor(), rm) {
			@Override
			protected void handleSuccess() {
				if (getData()) {
					fGdb.interruptAndWait(getInterruptTimeout(), rm);
				} else {
					rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_STATE,
							"Context cannot be suspended.", null)); //$NON-NLS-1$
					rm.done();
				}
			}
		});
	}

	@Override
	public void getExecutionContexts(IContainerDMContext containerDmc,
			final DataRequestMonitor<IExecutionDMContext[]> rm) {
		fProcService.getProcessesBeingDebugged(containerDmc, new DataRequestMonitor<IDMContext[]>(getExecutor(), rm) {
			@Override
			protected void handleSuccess() {
				if (getData() instanceof IExecutionDMContext[]) {
					IExecutionDMContext[] execDmcs = (IExecutionDMContext[]) getData();
					rm.setData(execDmcs);
				} else {
					rm.setStatus(
							new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INTERNAL_ERROR, "Invalid contexts", null)); //$NON-NLS-1$
				}
				rm.done();
			}
		});
	}

	/** @since 2.0 */
	@Override
	public void canReverseResume(IExecutionDMContext context, DataRequestMonitor<Boolean> rm) {
		if (!runControlOperationsEnabled()) {
			rm.setData(false);
			rm.done();
			return;
		}

		rm.setData(fReverseModeEnabled && doCanResume(context));
		rm.done();
	}

	/** @since 2.0 */
	@Override
	public void canReverseStep(final IExecutionDMContext context, StepType stepType,
			final DataRequestMonitor<Boolean> rm) {
		if (!runControlOperationsEnabled()) {
			rm.setData(false);
			rm.done();
			return;
		}

		if (context instanceof IContainerDMContext) {
			rm.setData(false);
			rm.done();
			return;
		}

		if (stepType == StepType.STEP_RETURN) {

			// Check the stuff we know first, before going to the backend for
			// stack info
			if (!fReverseModeEnabled || !doCanResume(context)) {
				rm.setData(false);
				rm.done();
				return;
			}

			// A step return will always be done in the top stack frame.
			// If the top stack frame is the only stack frame, it does not make sense
			// to do a step return since GDB will reject it.
			MIStack stackService = getServicesTracker().getService(MIStack.class);
			if (stackService != null) {
				// Check that the stack is at least two deep.
				stackService.getStackDepth(context, 2, new DataRequestMonitor<Integer>(getExecutor(), rm) {
					@Override
					public void handleCompleted() {
						if (isSuccess() && getData() == 1) {
							rm.setData(false);
							rm.done();
						} else {
							canReverseResume(context, rm);
						}
					}
				});
				return;
			}
		}

		canReverseResume(context, rm);
	}

	/** @since 2.0 */
	@Override
	public boolean isReverseStepping(IExecutionDMContext context) {
		return !isTerminated() && fReverseStepping;
	}

	/** @since 2.0 */
	@Override
	public void reverseResume(final IExecutionDMContext context, final RequestMonitor rm) {
		if (fReverseModeEnabled && doCanResume(context)) {
			ICommand<MIInfo> cmd = null;
			if (context instanceof IContainerDMContext) {
				cmd = fCommandFactory.createMIExecReverseContinue(context);
			} else {
				IMIExecutionDMContext dmc = DMContexts.getAncestorOfType(context, IMIExecutionDMContext.class);
				if (dmc == null) {
					rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_STATE,
							"Given context: " + context + " is not an execution context.", null)); //$NON-NLS-1$ //$NON-NLS-2$
					rm.done();
					return;
				}
				cmd = fCommandFactory.createMIExecReverseContinue(dmc);
			}

			setResumePending(true);
			// Cygwin GDB will accept commands and execute them after the step
			// which is not what we want, so mark the target as unavailable
			// as soon as we send a resume command.
			getCache().setContextAvailable(context, false);

			getConnection().queueCommand(cmd, new DataRequestMonitor<MIInfo>(getExecutor(), rm) {
				@Override
				public void handleFailure() {
					setResumePending(false);
					getCache().setContextAvailable(context, true);
				}
			});
		} else {
			rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_STATE,
					"Given context: " + context + ", is already running or reverse not enabled.", null)); //$NON-NLS-1$ //$NON-NLS-2$
			rm.done();
		}

	}

	/** @since 2.0 */
	@Override
	public void reverseStep(final IExecutionDMContext context, StepType stepType, final RequestMonitor rm) {
		assert context != null;

		IMIExecutionDMContext dmc = DMContexts.getAncestorOfType(context, IMIExecutionDMContext.class);
		if (dmc == null) {
			rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, NOT_SUPPORTED,
					"Given context: " + context + " is not an execution context.", null)); //$NON-NLS-1$ //$NON-NLS-2$
			rm.done();
			return;
		}

		if (!fReverseModeEnabled || !doCanResume(context)) {
			rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_STATE, "Cannot resume context", null)); //$NON-NLS-1$
			rm.done();
			return;
		}

		ICommand<MIInfo> cmd = null;
		switch (stepType) {
		case STEP_INTO:
			cmd = fCommandFactory.createMIExecReverseStep(dmc, 1);
			break;
		case STEP_OVER:
			cmd = fCommandFactory.createMIExecReverseNext(dmc, 1);
			break;
		case STEP_RETURN:
			// The -exec-finish command operates on the selected stack frame, but here we always
			// want it to operate on the top stack frame.  So we manually create a top-frame
			// context to use with the MI command.
			// We get a local instance of the stack service because the stack service can be shut
			// down before the run control service is shut down.  So it is possible for the
			// getService() request below to return null.
			MIStack stackService = getServicesTracker().getService(MIStack.class);
			if (stackService != null) {
				IFrameDMContext topFrameDmc = stackService.createFrameDMContext(dmc, 0);
				cmd = fCommandFactory.createMIExecUncall(topFrameDmc);
			} else {
				rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, NOT_SUPPORTED,
						"Cannot create context for command, stack service not available.", null)); //$NON-NLS-1$
				rm.done();
				return;
			}
			break;
		case INSTRUCTION_STEP_INTO:
			cmd = fCommandFactory.createMIExecReverseStepInstruction(dmc, 1);
			break;
		case INSTRUCTION_STEP_OVER:
			cmd = fCommandFactory.createMIExecReverseNextInstruction(dmc, 1);
			break;
		default:
			rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INTERNAL_ERROR, "Given step type not supported", //$NON-NLS-1$
					null));
			rm.done();
			return;
		}

		setResumePending(true);
		fReverseStepping = true;
		getCache().setContextAvailable(context, false);

		getConnection().queueCommand(cmd, new DataRequestMonitor<MIInfo>(getExecutor(), rm) {
			@Override
			public void handleFailure() {
				setResumePending(false);
				fReverseStepping = false;
				getCache().setContextAvailable(context, true);
			}
		});
	}

	/** @since 2.0 */
	@Override
	public void canEnableReverseMode(ICommandControlDMContext context, DataRequestMonitor<Boolean> rm) {
		rm.setData(fReverseSupported);
		rm.done();
	}

	/** @since 2.0 */
	@Override
	public void isReverseModeEnabled(ICommandControlDMContext context, DataRequestMonitor<Boolean> rm) {
		rm.setData(fReverseModeEnabled);
		rm.done();
	}

	/** @since 2.0 */
	@Override
	public void enableReverseMode(ICommandControlDMContext context, final boolean enable, final RequestMonitor rm) {
		if (!fReverseSupported) {
			rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, NOT_SUPPORTED, "Reverse mode is not supported.", //$NON-NLS-1$
					null));
			rm.done();
			return;
		}

		if (fReverseModeEnabled == enable) {
			rm.done();
			return;
		}

		getConnection().queueCommand(fCommandFactory.createCLIRecord(context, enable),
				new DataRequestMonitor<MIInfo>(getExecutor(), rm) {
					@Override
					public void handleSuccess() {
						setReverseModeEnabled(enable);
						rm.done();
					}
				});
	}

	// Overridden to use the new MI command -exec-jump
	/** @since 3.0 */
	@Override
	public void resumeAtLocation(IExecutionDMContext context, String location, RequestMonitor rm) {
		assert context != null;

		final IMIExecutionDMContext dmc = DMContexts.getAncestorOfType(context, IMIExecutionDMContext.class);
		if (dmc == null) {
			rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INTERNAL_ERROR,
					"Given context: " + context + " is not an thread execution context.", null)); //$NON-NLS-1$  //$NON-NLS-2$
			rm.done();
			return;
		}

		if (doCanResume(dmc)) {
			setResumePending(true);
			getCache().setContextAvailable(dmc, false);
			getConnection().queueCommand(
					// The MI command -exec-jump was added in GDB 7.0
					fCommandFactory.createMIExecJump(dmc, location), new DataRequestMonitor<MIInfo>(getExecutor(), rm) {
						@Override
						protected void handleFailure() {
							setResumePending(false);
							getCache().setContextAvailable(dmc, true);

							super.handleFailure();
						}
					});
		} else {
			rm.setStatus(
					new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, NOT_SUPPORTED, "Cannot resume given DMC.", null)); //$NON-NLS-1$
			rm.done();
		}
	}

	/**
	 * @since 3.0
	 */
	@DsfServiceEventHandler
	public void eventDispatched(ITraceRecordSelectedChangedDMEvent e) {
		if (e.isVisualizationModeEnabled()) {
			// We have started looking at trace records.  We can no longer
			// do run control operations.
			fRunControlOperationsEnabled = false;
		} else {
			// We stopped looking at trace data and gone back to debugger mode
			fRunControlOperationsEnabled = true;
		}
	}

	/** @since 2.0 */
	public void setReverseModeEnabled(boolean enabled) {
		if (fReverseModeEnabled != enabled) {
			fReverseModeEnabled = enabled;
			getSession().dispatchEvent(
					new GdbReverseModeChangedDMEvent(fCommandControl.getContext(), fReverseModeEnabled),
					getProperties());
		}
	}
}

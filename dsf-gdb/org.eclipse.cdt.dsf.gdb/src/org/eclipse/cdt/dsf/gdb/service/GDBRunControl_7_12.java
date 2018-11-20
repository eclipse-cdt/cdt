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

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.cdt.debug.core.model.IChangeReverseMethodHandler.ReverseDebugMethod;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.concurrent.ImmediateDataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.mi.service.IMICommandControl;
import org.eclipse.cdt.dsf.mi.service.IMIExecutionDMContext;
import org.eclipse.cdt.dsf.mi.service.command.CommandFactory;
import org.eclipse.cdt.dsf.mi.service.command.events.IMIDMEvent;
import org.eclipse.cdt.dsf.mi.service.command.events.MIBreakpointHitEvent;
import org.eclipse.cdt.dsf.mi.service.command.events.MIStoppedEvent;
import org.eclipse.cdt.dsf.mi.service.command.output.MIBreakpoint;
import org.eclipse.cdt.dsf.mi.service.command.output.MIFrame;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

/**
 * @since 5.2
 */
public class GDBRunControl_7_12 extends GDBRunControl_7_10 {
	private IMICommandControl fCommandControl;
	private CommandFactory fCommandFactory;
	private IGDBBackend fGDBBackEnd;
	private Map<String, EnableReverseAtLocOperation> fBpIdToReverseOpMap = new HashMap<>();

	public GDBRunControl_7_12(DsfSession session) {
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

	private void doInitialize(final RequestMonitor rm) {
		fCommandControl = getServicesTracker().getService(IMICommandControl.class);
		fGDBBackEnd = getServicesTracker().getService(IGDBBackend.class);

		fCommandFactory = fCommandControl.getCommandFactory();

		register(new String[] { GDBRunControl_7_12.class.getName() }, new Hashtable<String, String>());

		rm.done();
	}

	@Override
	public void suspend(IExecutionDMContext context, final RequestMonitor rm) {
		canSuspend(context, new DataRequestMonitor<Boolean>(getExecutor(), rm) {
			@Override
			protected void handleSuccess() {
				if (getData()) {
					// Thread or Process
					doSuspend(context, rm);
				} else {
					rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_STATE,
							"Context cannot be suspended.", null)); //$NON-NLS-1$
				}
			}
		});
	}

	private void doSuspend(IExecutionDMContext context, final RequestMonitor rm) {
		// We use the MI interrupt command when working in async mode.
		// Since this run control service is specifically for all-stop mode,
		// the only possibility to be running asynchronously is if the Full GDB console
		// is being used.
		if (fGDBBackEnd.isFullGdbConsoleSupported()) {
			// Start the job before sending the interrupt command
			// to make sure we don't miss the *stopped event
			final MonitorSuspendJob monitorJob = new MonitorSuspendJob(0, rm);
			fCommandControl.queueCommand(fCommandFactory.createMIExecInterrupt(context),
					new ImmediateDataRequestMonitor<MIInfo>() {
						@Override
						protected void handleSuccess() {
							// Nothing to do in the case of success, the monitoring job
							// will take care of completing the RM once it gets the
							// *stopped event.
						}

						@Override
						protected void handleFailure() {
							// In case of failure, we must cancel the monitoring job
							// and indicate the failure in the rm.
							monitorJob.cleanAndCancel();
							rm.done(getStatus());
						}
					});
		} else {
			// Asynchronous mode is off
			super.suspend(context, rm);
		}
	}

	@Override
	public boolean isTargetAcceptingCommands() {
		// We shall directly return true if the async mode is ON,
		// Since this run control service is specifically for all-stop mode,
		//   The only possibility to be running asynchronously is if the Full GDB console
		// is being used.
		if (fGDBBackEnd.isFullGdbConsoleSupported()) {
			return true;
		}

		return super.isTargetAcceptingCommands();
	}

	/**
	 * @since 5.2
	 */
	@DsfServiceEventHandler
	public void eventDispatched(ISuspendedDMEvent event) {
		assert event instanceof IMIDMEvent;

		if (event instanceof IMIDMEvent) {
			Object evt = ((IMIDMEvent) event).getMIEvent();

			if (evt instanceof MIBreakpointHitEvent) {
				MIBreakpointHitEvent miEvt = (MIBreakpointHitEvent) evt;

				for (EnableReverseAtLocOperation enableReverse : fBpIdToReverseOpMap.values()) {
					if (breakpointHitMatchesLocation(miEvt, enableReverse)) {
						// We are now stopped at the right place to initiate the recording for reverse mode
						// Remove the operation from our internal map and process it
						fBpIdToReverseOpMap.remove(enableReverse.fBpId);
						IContainerDMContext containerContext = enableReverse.getContainerContext();
						ICommandControlDMContext controlDmc = DMContexts.getAncestorOfType(containerContext,
								ICommandControlDMContext.class);
						ReverseDebugMethod reverseMethod = enableReverse.getReverseDebugMethod();
						if (controlDmc != null && reverseMethod != null) {
							enableReverseMode(controlDmc, reverseMethod, new RequestMonitor(getExecutor(), null) {
								@Override
								protected void handleSuccess() {
									if (enableReverse.shouldTriggerContinue()) {
										fCommandControl.queueCommand(
												fCommandFactory.createMIExecContinue(containerContext),
												new ImmediateDataRequestMonitor<MIInfo>());
									}
								}
							});
						}

						// Not expecting more than one operation for the same location
						break;
					}
				}
			}
		}
	}

	private static class EnableReverseAtLocOperation {
		private final IContainerDMContext fContainerContext;
		private final ReverseDebugMethod fTraceMethod;
		private final String fBpId;
		private final String fFileLocation;
		private final String fAddrLocation;
		private final boolean fTriggerContinue;

		public EnableReverseAtLocOperation(IContainerDMContext containerContext, ReverseDebugMethod traceMethod,
				String bpId, String fileLoc, String addr, boolean tiggerContinue) {
			fContainerContext = containerContext;
			fTraceMethod = traceMethod;
			fBpId = bpId;
			fFileLocation = fileLoc;
			fAddrLocation = addr;
			fTriggerContinue = tiggerContinue;
		}

		public IContainerDMContext getContainerContext() {
			return fContainerContext;
		}

		public ReverseDebugMethod getReverseDebugMethod() {
			return fTraceMethod;
		}

		public String getBreakointId() {
			return fBpId;
		}

		public String getFileLocation() {
			return fFileLocation;
		}

		public String getAddrLocation() {
			return fAddrLocation;
		}

		public boolean shouldTriggerContinue() {
			return fTriggerContinue;
		}

		@Override
		public int hashCode() {
			return fBpId.hashCode();
		}

		@Override
		public boolean equals(Object other) {
			if (other instanceof EnableReverseAtLocOperation) {
				if (fContainerContext != null
						&& fContainerContext.equals(((EnableReverseAtLocOperation) other).fContainerContext)
						&& fTraceMethod != null
						&& fTraceMethod.equals(((EnableReverseAtLocOperation) other).fTraceMethod) && fBpId != null
						&& fBpId.equals(((EnableReverseAtLocOperation) other).fBpId) && fFileLocation != null
						&& fFileLocation.equals(((EnableReverseAtLocOperation) other).fFileLocation)
						&& fAddrLocation != null
						&& fAddrLocation.equals(((EnableReverseAtLocOperation) other).fAddrLocation)
						&& fTriggerContinue == ((EnableReverseAtLocOperation) other).fTriggerContinue) {
					return true;
				}
			}
			return false;
		}
	}

	/**
	 * Changes the reverse debugging method as soon as the program is suspended at the specified breakpoint location
	 *
	 * It is recommended to use this request before the program runs or restarts in order to prevent timing issues and
	 * miss a suspend event
	 *
	 * Note, using the break point id to determine the stop location would be sufficient although in the case where
	 * multiple break points are inserted in the same location, GDB will only report one of them (e.g. GDB 7.12)
	 *
	 * Having the MIBreakpoint will give us access to the address, file and line number as well which can be used as
	 * alternatives to determine a matched location.
	 *
	 * This method is specially useful when using async mode with i.e. with GDB 7.12.
	 * Activating reverse debugging when the target is running may trigger an unresponsive GDB, this triggered the
	 * creation of this method
	 *
	 */
	void enableReverseModeAtBpLocation(final IContainerDMContext containerContext, final ReverseDebugMethod traceMethod,
			MIBreakpoint bp, boolean triggerContinue) {

		// Using an internal convention for file location i.e. file:lineNumber
		String fileLoc = bp.getFile() + ":" + bp.getLine(); //$NON-NLS-1$

		fBpIdToReverseOpMap.put(bp.getNumber(), new EnableReverseAtLocOperation(containerContext, traceMethod,
				bp.getNumber(), fileLoc, bp.getAddress(), triggerContinue));
	}

	private boolean breakpointHitMatchesLocation(MIBreakpointHitEvent e, EnableReverseAtLocOperation enableReverse) {
		if (enableReverse != null) {
			String bpId = e.getNumber();

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
			boolean equalFileLocation = false;
			boolean equalAddrLocation = false;
			boolean equalBpId = bpId.equals(enableReverse.getBreakointId());
			MIFrame frame = e.getFrame();
			if (frame != null) {
				String fileLocation = frame.getFile() + ":" + frame.getLine(); //$NON-NLS-1$
				String addrLocation = frame.getAddress();
				equalFileLocation = fileLocation.equals(enableReverse.getFileLocation());
				equalAddrLocation = addrLocation.equals(enableReverse.getAddrLocation());
			}

			if (equalFileLocation || equalAddrLocation || equalBpId) {
				// We stopped at the right place
				return true;
			}
		}

		return false;
	}

	protected class MonitorSuspendJob extends Job {
		// Bug 310274.  Until we have a preference to configure timeouts,
		// we need a large enough default timeout to accommodate slow
		// remote sessions.
		private final static int TIMEOUT_DEFAULT_VALUE = 5000;

		private final RequestMonitor fRequestMonitor;

		public MonitorSuspendJob(int timeout, RequestMonitor rm) {
			super("Suspend monitor job."); //$NON-NLS-1$
			setSystem(true);
			fRequestMonitor = rm;

			if (timeout <= 0) {
				timeout = TIMEOUT_DEFAULT_VALUE; // default of 5 seconds
			}

			// Register to listen for the stopped event
			getSession().addServiceEventListener(this, null);

			schedule(timeout);
		}

		/**
		 * Cleanup job and cancel it.
		 * This method is required because super.canceling() is only called
		 * if the job is actually running.
		 */
		public boolean cleanAndCancel() {
			if (getExecutor().isInExecutorThread()) {
				getSession().removeServiceEventListener(this);
			} else {
				getExecutor().submit(new DsfRunnable() {
					@Override
					public void run() {
						getSession().removeServiceEventListener(MonitorSuspendJob.this);
					}
				});
			}
			return cancel();
		}

		@DsfServiceEventHandler
		public void eventDispatched(MIStoppedEvent e) {
			if (e.getDMContext() != null && e.getDMContext() instanceof IMIExecutionDMContext) {
				// For all-stop, this means all threads have stopped
				if (cleanAndCancel()) {
					fRequestMonitor.done();
				}
			}
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			// This will be called when the timeout is hit and no *stopped event was received
			getExecutor().submit(new DsfRunnable() {
				@Override
				public void run() {
					getSession().removeServiceEventListener(MonitorSuspendJob.this);
					fRequestMonitor.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID,
							IDsfStatusConstants.REQUEST_FAILED, "Suspend operation timeout.", null)); //$NON-NLS-1$
				}
			});
			return Status.OK_STATUS;
		}
	}
}

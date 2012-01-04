/*******************************************************************************
 * Copyright (c) 2006, 2011 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     Ericsson	AB		  - Modified for handling of multiple threads
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.dsf.concurrent.CountingRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.concurrent.ImmediateCountingRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateDataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateExecutor;
import org.eclipse.cdt.dsf.concurrent.ImmediateRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Immutable;
import org.eclipse.cdt.dsf.concurrent.MultiRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Sequence;
import org.eclipse.cdt.dsf.concurrent.Sequence.Step;
import org.eclipse.cdt.dsf.datamodel.AbstractDMEvent;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.datamodel.IDMEvent;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointDMContext;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointsTargetDMContext;
import org.eclipse.cdt.dsf.debug.service.IBreakpointsExtension.IBreakpointHitDMEvent;
import org.eclipse.cdt.dsf.debug.service.ICachingService;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IProcessDMContext;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IThreadDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl;
import org.eclipse.cdt.dsf.debug.service.IRunControl2;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMContext;
import org.eclipse.cdt.dsf.debug.service.command.ICommand;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlShutdownDMEvent;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.gdb.internal.service.command.events.MITracepointSelectedEvent;
import org.eclipse.cdt.dsf.gdb.service.IGDBTraceControl.ITraceRecordSelectedChangedDMEvent;
import org.eclipse.cdt.dsf.mi.service.IMICommandControl;
import org.eclipse.cdt.dsf.mi.service.IMIContainerDMContext;
import org.eclipse.cdt.dsf.mi.service.IMIExecutionDMContext;
import org.eclipse.cdt.dsf.mi.service.IMIProcesses;
import org.eclipse.cdt.dsf.mi.service.IMIRunControl;
import org.eclipse.cdt.dsf.mi.service.MIBreakpointDMData;
import org.eclipse.cdt.dsf.mi.service.MIBreakpoints;
import org.eclipse.cdt.dsf.mi.service.MIBreakpoints.MIBreakpointDMContext;
import org.eclipse.cdt.dsf.mi.service.MIRunControl;
import org.eclipse.cdt.dsf.mi.service.MIStack;
import org.eclipse.cdt.dsf.mi.service.command.CommandFactory;
import org.eclipse.cdt.dsf.mi.service.command.events.IMIDMEvent;
import org.eclipse.cdt.dsf.mi.service.command.events.MIBreakpointHitEvent;
import org.eclipse.cdt.dsf.mi.service.command.events.MICatchpointHitEvent;
import org.eclipse.cdt.dsf.mi.service.command.events.MIErrorEvent;
import org.eclipse.cdt.dsf.mi.service.command.events.MIEvent;
import org.eclipse.cdt.dsf.mi.service.command.events.MIFunctionFinishedEvent;
import org.eclipse.cdt.dsf.mi.service.command.events.MIInferiorExitEvent;
import org.eclipse.cdt.dsf.mi.service.command.events.MIRunningEvent;
import org.eclipse.cdt.dsf.mi.service.command.events.MISharedLibEvent;
import org.eclipse.cdt.dsf.mi.service.command.events.MISignalEvent;
import org.eclipse.cdt.dsf.mi.service.command.events.MISteppingRangeEvent;
import org.eclipse.cdt.dsf.mi.service.command.events.MIStoppedEvent;
import org.eclipse.cdt.dsf.mi.service.command.events.MIThreadCreatedEvent;
import org.eclipse.cdt.dsf.mi.service.command.events.MIThreadExitEvent;
import org.eclipse.cdt.dsf.mi.service.command.events.MIWatchpointTriggerEvent;
import org.eclipse.cdt.dsf.mi.service.command.output.MIBreakInsertInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIThread;
import org.eclipse.cdt.dsf.mi.service.command.output.MIThreadInfoInfo;
import org.eclipse.cdt.dsf.service.AbstractDsfService;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.osgi.framework.BundleContext;

/**
 * Implementation note: This class implements event handlers for the events that
 * are generated by this service itself. When the event is dispatched, these
 * handlers will be called first, before any of the clients. These handlers
 * update the service's internal state information to make them consistent with
 * the events being issued. Doing this in the handlers as opposed to when the
 * events are generated, guarantees that the state of the service will always be
 * consistent with the events. The purpose of this pattern is to allow clients
 * that listen to service events and track service state, to be perfectly in
 * sync with the service state.
 * @since 1.1
 */
public class GDBRunControl_7_0_NS extends AbstractDsfService implements IMIRunControl, ICachingService
{
	@Immutable
	private static class ExecutionData implements IExecutionDMData2 {
		private final StateChangeReason fReason;
		private final String fDetails;
		
		ExecutionData(StateChangeReason reason, String details) {
			fReason = reason;
			fDetails = details;
		}

		@Override
		public StateChangeReason getStateChangeReason() { return fReason; }

		@Override
		public String getDetails() { return fDetails; }
	}

	/**
	 * Base class for events generated by the MI Run Control service.  Most events
	 * generated by the MI Run Control service are directly caused by some MI event.
	 * Other services may need access to the extended MI data carried in the event.
	 * 
	 * @param <V> DMC that this event refers to
	 * @param <T> MIInfo object that is the direct cause of this event
	 * @see MIRunControl
	 */
	@Immutable
	private static class RunControlEvent<V extends IDMContext, T extends MIEvent<? extends IDMContext>> extends AbstractDMEvent<V>
	implements IDMEvent<V>, IMIDMEvent
	{
		final private T fMIInfo;
		public RunControlEvent(V dmc, T miInfo) {
			super(dmc);
			fMIInfo = miInfo;
		}

		@Override
		public T getMIEvent() { return fMIInfo; }
	}

	/**
	 * Indicates that the given thread has been suspended.
	 * @since 4.0
	 */
	@Immutable
	protected static class SuspendedEvent extends RunControlEvent<IExecutionDMContext, MIStoppedEvent>
	implements ISuspendedDMEvent
	{
		SuspendedEvent(IExecutionDMContext ctx, MIStoppedEvent miInfo) {
			super(ctx, miInfo);
		}

		@Override
		public StateChangeReason getReason() {
			if (getMIEvent() instanceof MICatchpointHitEvent) {	// must precede MIBreakpointHitEvent
				return StateChangeReason.EVENT_BREAKPOINT;
			} else if (getMIEvent() instanceof MITracepointSelectedEvent) {	// must precede MIBreakpointHitEvent
				return StateChangeReason.UNKNOWN;  // Don't display anything here, the details will take care of it
			} else if (getMIEvent() instanceof MIBreakpointHitEvent) {
				return StateChangeReason.BREAKPOINT;
			} else if (getMIEvent() instanceof MISteppingRangeEvent) {
				return StateChangeReason.STEP;
			} else if (getMIEvent() instanceof MIFunctionFinishedEvent) {
				return StateChangeReason.STEP;
			} else if (getMIEvent() instanceof MISharedLibEvent) {
				return StateChangeReason.SHAREDLIB;
			}else if (getMIEvent() instanceof MISignalEvent) {
				return StateChangeReason.SIGNAL;
			}else if (getMIEvent() instanceof MIWatchpointTriggerEvent) {
				return StateChangeReason.WATCHPOINT;
			}else if (getMIEvent() instanceof MIErrorEvent) {
				return StateChangeReason.ERROR;
			}else {
				return StateChangeReason.USER_REQUEST;
			}
		}
		
		public String getDetails() {
			MIStoppedEvent event = getMIEvent();
			if (event instanceof MICatchpointHitEvent) {	// must precede MIBreakpointHitEvent
				return ((MICatchpointHitEvent)event).getReason();
			} else if (event instanceof MITracepointSelectedEvent) {	// must precede MIBreakpointHitEvent
				return ((MITracepointSelectedEvent)event).getReason();
			} else if (event instanceof MISharedLibEvent) {
				 return ((MISharedLibEvent)event).getLibrary();
			} else if (event instanceof MISignalEvent) {
				return ((MISignalEvent)event).getName() + ':' + ((MISignalEvent)event).getMeaning(); 
			} else if (event instanceof MIWatchpointTriggerEvent) {
				return ((MIWatchpointTriggerEvent)event).getExpression();
			} else if (event instanceof MIErrorEvent) {
				return ((MIErrorEvent)event).getMessage();
			}

			return null;
		}
	}

    /**
     * Indicates that the given thread has been suspended on a breakpoint.
     * @since 4.0
     */
    @Immutable
    protected static class BreakpointHitEvent extends SuspendedEvent
    implements IBreakpointHitDMEvent
    {
        final private IBreakpointDMContext[] fBreakpoints;
        
        BreakpointHitEvent(IExecutionDMContext ctx, MIBreakpointHitEvent miInfo, IBreakpointDMContext bpCtx) {
            super(ctx, miInfo);
            
            fBreakpoints = new IBreakpointDMContext[] { bpCtx };
        }
        
    	@Override
        public IBreakpointDMContext[] getBreakpoints() {
            return fBreakpoints;
        }
    }

	/**
	 * @since 4.0
	 */
	@Immutable
	protected static class ResumedEvent extends RunControlEvent<IExecutionDMContext, MIRunningEvent>
	implements IResumedDMEvent
	{
		ResumedEvent(IExecutionDMContext ctx, MIRunningEvent miInfo) {
			super(ctx, miInfo);
		}

		@Override
		public StateChangeReason getReason() {
			if (getMIEvent() != null) {
				switch(getMIEvent().getType()) {
				case MIRunningEvent.CONTINUE:
					return StateChangeReason.USER_REQUEST;
				case MIRunningEvent.NEXT:
				case MIRunningEvent.NEXTI:
					return StateChangeReason.STEP;
				case MIRunningEvent.STEP:
				case MIRunningEvent.STEPI:
					return StateChangeReason.STEP;
				case MIRunningEvent.FINISH:
					return StateChangeReason.STEP;
				case MIRunningEvent.UNTIL:
				case MIRunningEvent.RETURN:
					break;
				}
			}
			return StateChangeReason.UNKNOWN;
		}
	}

	/**
	 * @since 4.0
	 */
	@Immutable
	protected static class StartedDMEvent extends RunControlEvent<IExecutionDMContext,MIThreadCreatedEvent>
	implements IStartedDMEvent
	{
		StartedDMEvent(IMIExecutionDMContext executionDmc, MIThreadCreatedEvent miInfo) {
			super(executionDmc, miInfo);
		}
	}

	/**
	 * @since 4.0
	 */
	@Immutable
	protected static class ExitedDMEvent extends RunControlEvent<IExecutionDMContext,MIThreadExitEvent>
	implements IExitedDMEvent
	{
		ExitedDMEvent(IMIExecutionDMContext executionDmc, MIThreadExitEvent miInfo) {
			super(executionDmc, miInfo);
		}
	}

	protected class MIThreadRunState {
		// State flags
		boolean fSuspended = false;
		boolean fResumePending = false;
		boolean fStepping = false;

		/**
		 * What caused the state change. E.g., a signal was thrown.
		 */
		StateChangeReason fStateChangeReason;

		/**
		 * Further detail on what caused the state change. E.g., the specific signal
		 * that was throw was a SIGINT. The exact string comes from gdb in the mi
		 * event. May be null, as not all types of state change have additional
		 * detail of interest.
		 */
		String fStateChangeDetails;
	}

	/**
	 * @since 4.0
	 */
	protected static class RunToLineActiveOperation {
		private IMIExecutionDMContext fThreadContext;
		private int fBpId;
		private String fFileLocation;
		private String fAddrLocation;
		private boolean fSkipBreakpoints;
		
		public RunToLineActiveOperation(IMIExecutionDMContext threadContext,
				int bpId, String fileLoc, String addr, boolean skipBreakpoints) {
			fThreadContext = threadContext;
			fBpId = bpId;
			fFileLocation = fileLoc;
			fAddrLocation = addr;
			fSkipBreakpoints = skipBreakpoints;
		}
		
		public IMIExecutionDMContext getThreadContext() { return fThreadContext; }
		public int getBreakointId() { return fBpId; }
		public String getFileLocation() { return fFileLocation; }
		public String getAddrLocation() { return fAddrLocation; }
		public boolean shouldSkipBreakpoints() { return fSkipBreakpoints; }
	}

	///////////////////////////////////////////////////////////////////////////
	// MIRunControlNS
	///////////////////////////////////////////////////////////////////////////

	private ICommandControlService fConnection;
	private CommandFactory fCommandFactory;
	private IGDBProcesses fProcessService;
	
	private boolean fTerminated = false;

	// ThreadStates indexed by the execution context
	protected Map<IMIExecutionDMContext, MIThreadRunState> fThreadRunStates = new HashMap<IMIExecutionDMContext, MIThreadRunState>();

	private RunToLineActiveOperation fRunToLineActiveOperation = null;

	/** @since 4.0 */
	protected RunToLineActiveOperation getRunToLineActiveOperation() {
		return fRunToLineActiveOperation;
	}

	/** @since 4.0 */
	protected void setRunToLineActiveOperation(RunToLineActiveOperation operation) {
		fRunToLineActiveOperation = operation;
	}

	/** 
	 * Set of threads for which the next MIRunning event should be silenced.
	 */
	private Set<IMIExecutionDMContext> fDisableNextRunningEventDmcSet = new HashSet<IMIExecutionDMContext>();
	/** 
	 * Set of threads for which the next MISignal (MIStopped) event should be silenced.
	 */
	private Set<IMIExecutionDMContext> fDisableNextSignalEventDmcSet = new HashSet<IMIExecutionDMContext>();
	/** 
	 * Map that stores the silenced MIStopped event for the specified thread, in case we need to use it for a failure.
	 */
	private Map<IMIExecutionDMContext,MIStoppedEvent> fSilencedSignalEventMap = new HashMap<IMIExecutionDMContext, MIStoppedEvent>();

	/**
	 * This variable allows us to know if run control operation
	 * should be enabled or disabled.  Run control operations are
	 * always enabled except when visualizing tracepoints.
	 */
	private boolean fRunControlOperationsEnabled = true;
	
	///////////////////////////////////////////////////////////////////////////
	// Initialization and shutdown
	///////////////////////////////////////////////////////////////////////////


	public GDBRunControl_7_0_NS(DsfSession session) {
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
        register(new String[]{ IRunControl.class.getName(), 
        					   IRunControl2.class.getName(),
        					   IMIRunControl.class.getName()}, 
        	     new Hashtable<String,String>());
		fConnection = getServicesTracker().getService(ICommandControlService.class);
		fCommandFactory = getServicesTracker().getService(IMICommandControl.class).getCommandFactory();
		fProcessService = getServicesTracker().getService(IGDBProcesses.class);
		
		getSession().addServiceEventListener(this, null);
		rm.done();
	}

	@Override
	public void shutdown(final RequestMonitor rm) {
        unregister();
		getSession().removeServiceEventListener(this);
		super.shutdown(rm);
	}

	/** @since 4.1 */
	protected boolean getRunControlOperationsEnabled() {
		return fRunControlOperationsEnabled;
	}

	/** @since 4.1 */
	protected void setRunControlOperationsEnabled(boolean runControlEnabled) {
		fRunControlOperationsEnabled = runControlEnabled;
	}
	
	///////////////////////////////////////////////////////////////////////////
	// AbstractDsfService
	///////////////////////////////////////////////////////////////////////////

	@Override
	protected BundleContext getBundleContext() {
		return GdbPlugin.getBundleContext();
	}

	///////////////////////////////////////////////////////////////////////////
	// IRunControl
	///////////////////////////////////////////////////////////////////////////

	// ------------------------------------------------------------------------
	// Suspend
	// ------------------------------------------------------------------------

	@Override
	public boolean isSuspended(IExecutionDMContext context) {

		// Thread case
		if (context instanceof IMIExecutionDMContext) {
			MIThreadRunState threadState = fThreadRunStates.get(context);
			return (threadState == null) ? false : !fTerminated && threadState.fSuspended;
		}

		// Container case.  The container is considered suspended as long
		// as one of its thread is suspended
		if (context instanceof IContainerDMContext) {
			boolean hasThread = false;
			for (IMIExecutionDMContext threadContext : fThreadRunStates.keySet()) {
				if (DMContexts.isAncestorOf(threadContext, context)) {
					hasThread = true;
					if (isSuspended(threadContext)) return true;
				}
			}
			// If this container does not have any threads, it means it wasn't started
			// yet or it was terminated, so we can consider it suspended
			if (hasThread == false) return true;
		}

		// Default case
		return false;
	}

	@Override
	public void canSuspend(IExecutionDMContext context, DataRequestMonitor<Boolean> rm) {
		if (fRunControlOperationsEnabled == false) {
			rm.setData(false);
			rm.done();
			return;
		}
		
		// Thread case
		if (context instanceof IMIExecutionDMContext) {
			rm.setData(doCanSuspend(context));
			rm.done();
			return;
		}

		// Container case
		if (context instanceof IContainerDMContext) {
			boolean canSuspend = false;
			for (IMIExecutionDMContext threadContext : fThreadRunStates.keySet()) {
				if (DMContexts.isAncestorOf(threadContext, context)) {
					canSuspend |= doCanSuspend(threadContext);
				}
			}
			rm.setData(canSuspend);
			rm.done();
			return;
		}

		// Default case
		rm.setData(false);
		rm.done();
	}

	private boolean doCanSuspend(IExecutionDMContext context) {
		MIThreadRunState threadState = fThreadRunStates.get(context);
		return (threadState == null) ? false : !fTerminated && !threadState.fSuspended;
	}

	@Override
	public void suspend(IExecutionDMContext context, final RequestMonitor rm) {

		assert context != null;

		// Thread case
		IMIExecutionDMContext thread = DMContexts.getAncestorOfType(context, IMIExecutionDMContext.class);
		if (thread != null) {
			doSuspendThread(thread, rm);
			return;
		}

		// Container case
		IMIContainerDMContext container = DMContexts.getAncestorOfType(context, IMIContainerDMContext.class);
		if (container != null) {
			doSuspendContainer(container, rm);
			return;
		}

		// Default case
		rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, NOT_SUPPORTED, "Invalid context type.", null)); //$NON-NLS-1$
		rm.done();
	}

	private void doSuspendThread(IMIExecutionDMContext context, final RequestMonitor rm) {

		if (!doCanSuspend(context)) {
			rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, NOT_SUPPORTED,
				"Given context: " + context + ", is already suspended.", null)); //$NON-NLS-1$ //$NON-NLS-2$
			rm.done();
			return;
		}

		fConnection.queueCommand(fCommandFactory.createMIExecInterrupt(context), new DataRequestMonitor<MIInfo>(getExecutor(), rm));
	}

	private void doSuspendContainer(IMIContainerDMContext context, final RequestMonitor rm) {
		String groupId = context.getGroupId();
		fConnection.queueCommand(fCommandFactory.createMIExecInterrupt(context, groupId), new DataRequestMonitor<MIInfo>(getExecutor(), rm));
	}

	// ------------------------------------------------------------------------
	// Resume
	// ------------------------------------------------------------------------

	@Override
	public void canResume(IExecutionDMContext context, DataRequestMonitor<Boolean> rm) {
		if (fRunControlOperationsEnabled == false) {
			rm.setData(false);
			rm.done();
			return;
		}
		
		// Thread case
		if (context instanceof IMIExecutionDMContext) {
			rm.setData(doCanResume(context));
			rm.done();
			return;
		}

		// Container case
		if (context instanceof IContainerDMContext) {
			boolean canSuspend = false;
			for (IMIExecutionDMContext threadContext : fThreadRunStates.keySet()) {
				if (DMContexts.isAncestorOf(threadContext, context)) {
					canSuspend |= doCanResume(threadContext);
				}
			}
			rm.setData(canSuspend);
			rm.done();
			return;
		}

		// Default case
		rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, NOT_SUPPORTED, "Invalid context type.", null)); //$NON-NLS-1$
		rm.done();
	}

	private boolean doCanResume(IExecutionDMContext context) {
		MIThreadRunState threadState = fThreadRunStates.get(context);
		return (threadState == null) ? false : !fTerminated && threadState.fSuspended && !threadState.fResumePending;
	}

	@Override
	public void resume(IExecutionDMContext context, final RequestMonitor rm) {

		assert context != null;

		// Thread case
		IMIExecutionDMContext thread = DMContexts.getAncestorOfType(context, IMIExecutionDMContext.class);
		if (thread != null) {
			doResumeThread(thread, rm);
			return;
		}

		// Container case
		IMIContainerDMContext container = DMContexts.getAncestorOfType(context, IMIContainerDMContext.class);
		if (container != null) {
			doResumeContainer(container, rm);
			return;
		}

		// Default case
		rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, NOT_SUPPORTED, "Invalid context type.", null)); //$NON-NLS-1$
		rm.done();
	}

	private void doResumeThread(IMIExecutionDMContext context, final RequestMonitor rm) {

		if (!doCanResume(context)) {
			rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_STATE,
				"Given context: " + context + ", is already running.", null)); //$NON-NLS-1$ //$NON-NLS-2$
			rm.done();
			return;
		}

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
		String groupId = context.getGroupId();
		fConnection.queueCommand(fCommandFactory.createMIExecContinue(context, groupId), new DataRequestMonitor<MIInfo>(getExecutor(), rm));
	}

	// ------------------------------------------------------------------------
	// Step
	// ------------------------------------------------------------------------

	@Override
	public boolean isStepping(IExecutionDMContext context) {

		// If it's a thread, just look it up
		if (context instanceof IMIExecutionDMContext) {
			MIThreadRunState threadState = fThreadRunStates.get(context);
			return (threadState == null) ? false : !fTerminated && threadState.fStepping;
		}

		// Default case
		return false;
	}

	@Override
	public void canStep(final IExecutionDMContext context, StepType stepType, final DataRequestMonitor<Boolean> rm) {
		if (fRunControlOperationsEnabled == false) {
			rm.setData(false);
			rm.done();
			return;
		}
		
		// If it's a thread, just look it up
		if (context instanceof IMIExecutionDMContext) {
	    	if (stepType == StepType.STEP_RETURN) {
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
	            	    		canResume(context, rm);
	            			}
	            		}
	            	});
	            	return;
	            }
	    	}

			canResume(context, rm);
			return;
		}

		// If it's a container, then we don't want to step it
		rm.setData(false);
		rm.done();
	}

	@Override
	public void step(IExecutionDMContext context, StepType stepType, final RequestMonitor rm) {

		assert context != null;

		IMIExecutionDMContext dmc = DMContexts.getAncestorOfType(context, IMIExecutionDMContext.class);
		if (dmc == null) {
			rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INTERNAL_ERROR,
				"Given context: " + context + " is not an MI execution context.", null)); //$NON-NLS-1$ //$NON-NLS-2$
			rm.done();
			return;
		}

		if (!doCanResume(context)) {
			rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_STATE,
				"Cannot resume context", null)); //$NON-NLS-1$
			rm.done();
			return;
		}

		final MIThreadRunState threadState = fThreadRunStates.get(context);
		if (threadState == null) {
			rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_STATE,
				"Given context: " + context + " can't be found.", null)); //$NON-NLS-1$ //$NON-NLS-2$
			rm.done();
			return;
		}

		ICommand<MIInfo> cmd = null;
		switch (stepType) {
		case STEP_INTO:
			cmd = fCommandFactory.createMIExecStep(dmc);
			break;
		case STEP_OVER:
			cmd = fCommandFactory.createMIExecNext(dmc);
			break;
		case STEP_RETURN:
			// The -exec-finish command operates on the selected stack frame, but here we always
			// want it to operate on the stop stack frame. So we manually create a top-frame
			// context to use with the MI command.
			// We get a local instance of the stack service because the stack service can be shut
			// down before the run control service is shut down. So it is possible for the
			// getService() request below to return null.
			MIStack stackService = getServicesTracker().getService(MIStack.class);
			if (stackService != null) {
				IFrameDMContext topFrameDmc = stackService.createFrameDMContext(dmc, 0);
				cmd = fCommandFactory.createMIExecFinish(topFrameDmc);
			} else {
				rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, NOT_SUPPORTED,
						"Cannot create context for command, stack service not available.", null)); //$NON-NLS-1$
				rm.done();
				return;
			}
			break;
		case INSTRUCTION_STEP_INTO:
			cmd = fCommandFactory.createMIExecStepInstruction(dmc);
			break;
		case INSTRUCTION_STEP_OVER:
			cmd = fCommandFactory.createMIExecNextInstruction(dmc);
			break;
		default:
			rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID,
					INTERNAL_ERROR, "Given step type not supported", null)); //$NON-NLS-1$
			rm.done();
			return;
		}
		
		threadState.fResumePending = true;
		threadState.fStepping = true;
		fConnection.queueCommand(cmd, new DataRequestMonitor<MIInfo>(getExecutor(), rm) {
			@Override
			public void handleFailure() {
				threadState.fResumePending = false;
				threadState.fStepping = false;

				super.handleFailure();
			}   
		});
	}

	// ------------------------------------------------------------------------
	// Run to line
	// ------------------------------------------------------------------------

	private void runToLocation(final IExecutionDMContext context, final String location, final boolean skipBreakpoints, final RequestMonitor rm){

		assert context != null;

		final IMIExecutionDMContext dmc = DMContexts.getAncestorOfType(context, IMIExecutionDMContext.class);
		if (dmc == null) {
			rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INTERNAL_ERROR,
				"Given context: " + context + " is not an MI execution context.", null)); //$NON-NLS-1$ //$NON-NLS-2$
			rm.done();
			return;
		}

		if (!doCanResume(dmc)) {
			rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_STATE,
				"Cannot resume context", null)); //$NON-NLS-1$
			rm.done();
			return;
		}

		MIThreadRunState threadState = fThreadRunStates.get(dmc);
		if (threadState == null) {
            rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_STATE,
                "Given context: " + context + " can't be found.", null)); //$NON-NLS-1$ //$NON-NLS-2$
			rm.done();
			return;
		}

    	IBreakpointsTargetDMContext bpDmc = DMContexts.getAncestorOfType(context, IBreakpointsTargetDMContext.class);
    	fConnection.queueCommand(
    			fCommandFactory.createMIBreakInsert(bpDmc, true, false, null, 0, 
    					          location, dmc.getThreadId()), 
    		    new DataRequestMonitor<MIBreakInsertInfo>(getExecutor(), rm) {
    				@Override
    				public void handleSuccess() {
    					// We must set are RunToLineActiveOperation *before* we do the resume
    					// or else we may get the stopped event, before we have set this variable.
       					int bpId = getData().getMIBreakpoints()[0].getNumber();
       					String addr = getData().getMIBreakpoints()[0].getAddress();
    		        	fRunToLineActiveOperation = new RunToLineActiveOperation(dmc, bpId, location, addr, skipBreakpoints);

    					resume(dmc, new RequestMonitor(getExecutor(), rm) {
            				@Override
            				public void handleFailure() {
            		    		IBreakpointsTargetDMContext bpDmc = DMContexts.getAncestorOfType(fRunToLineActiveOperation.getThreadContext(),
            		    				IBreakpointsTargetDMContext.class);
            		    		int bpId = fRunToLineActiveOperation.getBreakointId();

            		    		fConnection.queueCommand(fCommandFactory.createMIBreakDelete(bpDmc, new int[] {bpId}),
            		    				new DataRequestMonitor<MIInfo>(getExecutor(), null));
            		    		fRunToLineActiveOperation = null;

            		    		super.handleFailure();
            		    	}
    					});
    				}
    			});

	}

	// ------------------------------------------------------------------------
	// Resume at location
	// ------------------------------------------------------------------------

	private void resumeAtLocation(IExecutionDMContext context, String location, RequestMonitor rm) {
		assert context != null;

		final IMIExecutionDMContext dmc = DMContexts.getAncestorOfType(context, IMIExecutionDMContext.class);
		if (dmc == null) {
			rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INTERNAL_ERROR,
				"Given context: " + context + " is not an MI execution context.", null)); //$NON-NLS-1$ //$NON-NLS-2$
			rm.done();
			return;
		}

		if (!doCanResume(dmc)) {
			rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_STATE,
				"Cannot resume context", null)); //$NON-NLS-1$
			rm.done();
			return;
		}

		final MIThreadRunState threadState = fThreadRunStates.get(dmc);
		if (threadState == null) {
            rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_STATE,
                "Given context: " + context + " can't be found.", null)); //$NON-NLS-1$ //$NON-NLS-2$
			rm.done();
			return;
		}

		threadState.fResumePending = true;
		fConnection.queueCommand(
				fCommandFactory.createMIExecJump(dmc, location),
				new DataRequestMonitor<MIInfo>(getExecutor(), rm) {
					@Override
					protected void handleFailure() {
						threadState.fResumePending = false;
						super.handleFailure();
					}
				});
	}
	// ------------------------------------------------------------------------
	// Support functions
	// ------------------------------------------------------------------------

	@Override
	public void getExecutionContexts(final IContainerDMContext containerDmc, final DataRequestMonitor<IExecutionDMContext[]> rm) {
        IMIProcesses procService = getServicesTracker().getService(IMIProcesses.class);
		procService.getProcessesBeingDebugged(
				containerDmc,
				new DataRequestMonitor<IDMContext[]>(getExecutor(), rm) {
					@Override
					protected void handleSuccess() {
						if (getData() instanceof IExecutionDMContext[]) {
							rm.setData((IExecutionDMContext[])getData());
						} else {
							rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INTERNAL_ERROR, "Invalid contexts", null)); //$NON-NLS-1$
						}
						rm.done();
					}
				});
	}

	@Override
	public void getExecutionData(IExecutionDMContext dmc, DataRequestMonitor<IExecutionDMData> rm) {
		MIThreadRunState threadState = fThreadRunStates.get(dmc);
		if (threadState == null) {
			rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID,INVALID_HANDLE,
				"Given context: " + dmc + " is not a recognized execution context.", null)); //$NON-NLS-1$ //$NON-NLS-2$
			rm.done();
			return;
		}

		if (dmc instanceof IMIExecutionDMContext) {
			rm.setData(new ExecutionData(threadState.fSuspended ? threadState.fStateChangeReason : null, threadState.fStateChangeDetails));
		} else {
			rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_HANDLE,
				"Given context: " + dmc + " is not a recognized execution context.", null)); //$NON-NLS-1$ //$NON-NLS-2$
		}
		rm.done();
	}


	private IMIExecutionDMContext createMIExecutionContext(IContainerDMContext container, String threadId) {
        IMIProcesses procService = getServicesTracker().getService(IMIProcesses.class);

        IProcessDMContext procDmc = DMContexts.getAncestorOfType(container, IProcessDMContext.class);
        
        IThreadDMContext threadDmc = null;
        if (procDmc != null) {
        	// For now, reuse the threadId as the OSThreadId
        	threadDmc = procService.createThreadContext(procDmc, threadId);
        }

        return procService.createExecutionContext(container, threadDmc, threadId);
	}

	private void updateThreadState(IMIExecutionDMContext context, ResumedEvent event) {
		StateChangeReason reason = event.getReason();
		boolean isStepping = reason.equals(StateChangeReason.STEP);
		MIThreadRunState threadState = fThreadRunStates.get(context);
		if (threadState == null) {
			threadState = new MIThreadRunState();
			fThreadRunStates.put(context, threadState);
		}
		threadState.fSuspended = false;
		threadState.fResumePending = false;
		threadState.fStateChangeReason = reason;
		threadState.fStateChangeDetails = null;	// we have no details of interest for a resume
		threadState.fStepping = isStepping;
	}

	private void updateThreadState(IMIExecutionDMContext context, SuspendedEvent event) {
		StateChangeReason reason = event.getReason();
		MIThreadRunState threadState = fThreadRunStates.get(context);
		if (threadState == null) {
			threadState = new MIThreadRunState();
			fThreadRunStates.put(context, threadState);
		}
		threadState.fSuspended = true;
		threadState.fResumePending = false;
		threadState.fStepping = false;
		threadState.fStateChangeReason = reason;
		threadState.fStateChangeDetails = event.getDetails();		
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
 	 * Note that for multi-process, we need to interrupt all processes
 	 * that share the same binary before doing a breakpoint operation on any of
 	 * those processes.  For simplicity, the logic below interrupts one thread of
 	 * every process being debugged, without differentiating on the executable.
 	 * Although it may seem wasteful to interrupt all processes when not necessary,
 	 * in truth it is not so much; when making a breakpoint operation in Eclipse, that
 	 * operation is propagated to all processes anyway, so they will all need to be
 	 * interrupted.  The case where we are wasteful is when we start or stop debugging
 	 * a process (starting a process, attaching to one, auto-attaching to one,
 	 * detaching from one, terminating one); in those cases, we only want to apply the
 	 * breakpoint operation to that one process and any other using the same binary.
 	 * The wastefulness is not such a big deal for that case, and is worth the simpler
 	 * solution.
 	 * Of course, it can always be improved later on. 
	 * See http://bugs.eclipse.org/337893
	 * ******************************************************************************/
	
	/**
	 * Utility class to store the parameters of the executeWithTargetAvailable() operations.
	 * @since 4.0
	 */
	protected static class TargetAvailableOperationInfo {
		public IDMContext ctx;
		public Sequence.Step[] steps;
		public RequestMonitor rm;
		
		public TargetAvailableOperationInfo(IDMContext ctx, Step[] steps, RequestMonitor rm) {
			super();
			this.ctx = ctx;
			this.steps = steps;
			this.rm = rm;
		}
	};
	
	// The set of threads that we will actually be suspended to make the containers suspended.
	private Set<IMIExecutionDMContext> fExecutionDmcToSuspendSet = new HashSet<IMIExecutionDMContext>();

	// Do we currently have an executeWithTargetAvailable() operation ongoing?
	private boolean fOngoingOperation;
	// Are we currently executing steps passed into executeWithTargetAvailable()?
	// This allows us to know if we can add more steps to execute or if we missed
	// our opportunity
	private boolean fCurrentlyExecutingSteps;
	
	// MultiRequestMonitor that allows us to track all the different steps we are
	// executing.  Once all steps are executed, we can complete this MultiRM and
	// allow the global sequence to continue.
	// Note that we couldn't use a CountingRequestMonitor because that type of RM
	// needs to know in advance how many subRms it will track; the MultiRM allows us
	// to receive more steps to execute continuously, and be able to update the MultiRM.
	private MultiRequestMonitor<RequestMonitor> fExecuteQueuedOpsStepMonitor;
	// The number of batches of steps that are still being executing for potentially
	// concurrent executeWithTargetAvailable() operations.
	// Once this gets to zero, we know we have executed all the steps we were aware of
	// and we can complete the operation.
	private int fNumStepsStillExecuting;
	// Queue of executeWithTargetAvailable() operations that need to be processed.
	private LinkedList<TargetAvailableOperationInfo> fOperationsPending = new LinkedList<TargetAvailableOperationInfo>();
	
	/** 
	 * Returns whether there is currently an ExecuteWithTargetAvailable() operation ongoing. 
	 * @since 4.0 
	 */
	protected boolean isTargetAvailableOperationOngoing() {
		return fOngoingOperation;
	}
	
	/** @since 4.0 */
	protected void setTargetAvailableOperationOngoing(boolean ongoing) {
		fOngoingOperation = ongoing;
	}
	
	/**
	 * Returns whether we are current in the process of executing the steps
	 * that were passed to ExecuteWithTargetAvailable().
	 * When this value is true, we can send more steps to be executed.
	 * @since 4.0 
	 */
	protected boolean isCurrentlyExecutingSteps() {
		return fCurrentlyExecutingSteps;
	}

	/** @since 4.0 */
	protected void setCurrentlyExecutingSteps(boolean executing) {
		fCurrentlyExecutingSteps = executing;
	}

	/**
	 * Returns the requestMonitor that will be run once all steps sent to
	 * ExecuteWithTargetAvailable() have been executed. 
	 * @since 4.0 
	 */
	protected MultiRequestMonitor<RequestMonitor> getExecuteQueuedStepsRM() {
		return fExecuteQueuedOpsStepMonitor;
	}
	
	/** @since 4.0 */
	protected void setExecuteQueuedStepsRM(MultiRequestMonitor<RequestMonitor> rm) {
		fExecuteQueuedOpsStepMonitor = rm;
	}


	/**
	 * Returns the number of batches of steps sent to ExecuteWithTargetAvailable()
	 * that are still executing.  Once this number reaches zero, we can complete
	 * the overall ExecuteWithTargetAvailable() operation.
	 * @since 4.0 
	 */
	protected int getNumStepsStillExecuting() {
		return fNumStepsStillExecuting;
	}

	/** @since 4.0 */
	protected void setNumStepsStillExecuting(int num) {
		fNumStepsStillExecuting = num;
	}

	/**
	 * Returns the queue of executeWithTargetAvailable() operations that still need to be processed
	 * @since 4.0
	 */
	protected LinkedList<TargetAvailableOperationInfo> getOperationsPending() {
		return fOperationsPending;
	}

	/**
	 * This method takes care of executing a batch of steps that were passed to
	 * ExecuteWithTargetAvailable().  The method is used to track the progress
	 * of all these batches of steps, so that we know exactly when all of them
	 * have been completed and the global sequence can be completed.
	 * @since 4.0
	 */
	protected void executeSteps(final TargetAvailableOperationInfo info) {
		fNumStepsStillExecuting++;
		
		// This RM propagates any error to the original rm of the actual steps.
		// Even in case of errors for these steps, we want to continue the overall sequence
		RequestMonitor stepsRm = new ImmediateRequestMonitor() {
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
	
	/**
	 * @since 3.0
	 */
	@Override
	public void executeWithTargetAvailable(IDMContext ctx, final Sequence.Step[] steps, final RequestMonitor rm) {
		if (!fOngoingOperation) {
			// We are the first operation of this kind currently requested
			// so we need to start the sequence
			fOngoingOperation = true;

			// We always go through our queue, even if we only have a single call to this method
			fOperationsPending.add(new TargetAvailableOperationInfo(ctx, steps, rm));
			
			// Steps that need to be executed to perform the operation
			final Step[] sequenceSteps = new Step[] {
					new IsTargetAvailableStep(ctx),
					new MakeTargetAvailableStep(),
					new ExecuteQueuedOperationsStep(),
					new RestoreTargetStateStep(),
			};
			
			// Once all the sequence is completed, we need to see if we have received
			// another request that we now need to process
			RequestMonitor sequenceCompletedRm = new RequestMonitor(getExecutor(), null) {
				@Override
				protected void handleSuccess() {
					 fOngoingOperation = false;
					 
					 if (fOperationsPending.size() > 0) {
						 // Darn, more operations came in.  Trigger their processing
						 // by calling executeWithTargetAvailable() on the last one
						 TargetAvailableOperationInfo info = fOperationsPending.removeLast();
						 executeWithTargetAvailable(info.ctx, info.steps, info.rm);
					 }
					 // no other rm.done() needs to be called, they have all been handled already
				}
				@Override
				protected void handleFailure() {
					// If the sequence failed, we have to give up on the operation(s).
					// If we don't, we risk an infinite loop where we try, over and over
					// to perform an operation that keeps on failing.
					fOngoingOperation = false;

					// Complete each rm of the cancelled operations
					while (fOperationsPending.size() > 0) {
						RequestMonitor rm = fOperationsPending.poll().rm;
						rm.setStatus(getStatus());
						rm.done();
					}
					super.handleFailure();
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
				executeSteps(new TargetAvailableOperationInfo(ctx, steps, rm));
			} else {
				// Too late to execute the new steps, so queue them for later
				fOperationsPending.add(new TargetAvailableOperationInfo(ctx, steps, rm));
			}
		}
	}
	
	
	/**
	 * This part of the sequence looks for all threads that will need to be suspended.
	 * @since 3.0
	 */
	protected class IsTargetAvailableStep extends Sequence.Step {
		final IDMContext fCtx;
		
		public IsTargetAvailableStep(IDMContext ctx) {
			fCtx = ctx;
		}
		
		private void getThreadToSuspend(IContainerDMContext containerDmc, final RequestMonitor rm) {
			// If the process is running, get its first thread which we will need to suspend
			fProcessService.getProcessesBeingDebugged(
					containerDmc,
					new ImmediateDataRequestMonitor<IDMContext[]>(rm) {
						@Override
						protected void handleSuccess() {
							IDMContext[] threads = getData();
							if (threads != null && threads.length > 0) {
								// Choose the first thread as the one to suspend
								fExecutionDmcToSuspendSet.add((IMIExecutionDMContext)threads[0]);
							}
							rm.done();
						}
					});
		}

		@Override
		public void execute(final RequestMonitor rm) {
			// Clear any old data before we start
			fExecutionDmcToSuspendSet.clear();
			
			// Get all processes being debugged to see which one are running
			// and need to be interrupted
			fProcessService.getProcessesBeingDebugged(
					fConnection.getContext(),
					new ImmediateDataRequestMonitor<IDMContext[]>(rm) {
						@Override
						protected void handleSuccess() {
							assert getData() != null;

							if (getData().length == 0) {
								// Happens at startup, starting with GDB 7.0.
								// This means the target is available.  Nothing to do.
								rm.done();
							} else {
								// Go through every process to see if it is running.
								// If it is running, get its first thread so we can interrupt it.
								CountingRequestMonitor crm = new ImmediateCountingRequestMonitor(rm);
								
								int numThreadsToSuspend = 0;
								for (IDMContext dmc : getData()) {
									IContainerDMContext containerDmc = (IContainerDMContext)dmc;
									if (!isSuspended(containerDmc)) {
										numThreadsToSuspend++;
										getThreadToSuspend(containerDmc, crm);
									}
								}
								crm.setDoneCount(numThreadsToSuspend);
							}
						}
					});
		}
	};

	/**
	 * Suspended all the threads we have selected.
	 * @since 3.0
	 */
	protected class MakeTargetAvailableStep extends Sequence.Step {
		@Override
		public void execute(final RequestMonitor rm) {
			// Interrupt every first thread of the running processes
			CountingRequestMonitor crm = new ImmediateCountingRequestMonitor(rm);
			crm.setDoneCount(fExecutionDmcToSuspendSet.size());
			
			for (final IMIExecutionDMContext thread : fExecutionDmcToSuspendSet) {
				assert !fDisableNextRunningEventDmcSet.contains(thread);
				assert !fDisableNextSignalEventDmcSet.contains(thread);

				// Don't broadcast the next stopped signal event
				fDisableNextSignalEventDmcSet.add(thread);

				suspend(thread,
						new ImmediateRequestMonitor(crm) {
					@Override
					protected void handleFailure() {
						// We weren't able to suspend, so abort the operation
						fDisableNextSignalEventDmcSet.remove(thread);
						super.handleFailure();
					};
				});
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
	 * @since 4.0
	 */
	protected class ExecuteQueuedOperationsStep extends Sequence.Step {
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
	 * @since 3.0
	 */
	protected class RestoreTargetStateStep extends Sequence.Step {
		@Override
		public void execute(final RequestMonitor rm) {
			// Resume every thread we had interrupted
			CountingRequestMonitor crm = new ImmediateCountingRequestMonitor(rm);
			crm.setDoneCount(fExecutionDmcToSuspendSet.size());
			
			for (final IMIExecutionDMContext thread : fExecutionDmcToSuspendSet) {

				assert !fDisableNextRunningEventDmcSet.contains(thread);
				fDisableNextRunningEventDmcSet.add(thread);

				// Can't use the resume() call because we 'silently' stopped
				// so resume() will not know we are actually stopped
				fConnection.queueCommand(
						fCommandFactory.createMIExecContinue(thread),
						new ImmediateDataRequestMonitor<MIInfo>(crm) {
							@Override
							protected void handleSuccess() {
								fSilencedSignalEventMap.remove(thread);
								super.handleSuccess();
							}

							@Override
							protected void handleFailure() {
								// Darn, we're unable to restart the target.  Must cleanup!
								fDisableNextRunningEventDmcSet.remove(thread);

								// We must also sent the Stopped event that we had kept silent
								MIStoppedEvent event = fSilencedSignalEventMap.remove(thread);
								if (event != null) {
									eventDispatched(event);
								} else {
									// Maybe the stopped event didn't arrive yet.
									// We don't want to silence it anymore
									fDisableNextSignalEventDmcSet.remove(thread);
								}

								super.handleFailure();
							}
						});
			}
		}
	};

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
	@DsfServiceEventHandler
	public void eventDispatched(final MIRunningEvent e) {
		if (fDisableNextRunningEventDmcSet.remove(e.getDMContext())) {
			// Don't broadcast the running event
			return;
		}

        getSession().dispatchEvent(new ResumedEvent(e.getDMContext(), e), getProperties());
	}

    /**
     * @nooverride This method is not intended to be re-implemented or extended by clients.
     * @noreference This method is not intended to be referenced by clients.
     */
	@DsfServiceEventHandler
	public void eventDispatched(final MIStoppedEvent e) {
    	if (fRunToLineActiveOperation != null) {
    		// First check if it is the right thread that stopped
    		IMIExecutionDMContext threadDmc = DMContexts.getAncestorOfType(e.getDMContext(), IMIExecutionDMContext.class);
    		if (fRunToLineActiveOperation.getThreadContext().equals(threadDmc)) {
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
    			if (fileLocation.equals(fRunToLineActiveOperation.getFileLocation()) ||
    				addrLocation.equals(fRunToLineActiveOperation.getAddrLocation()) ||
    				bpId == fRunToLineActiveOperation.getBreakointId()) {
        			// We stopped at the right place.  All is well.
    				fRunToLineActiveOperation = null;
    			} else {
    				// The right thread stopped but not at the right place yet
    				if (fRunToLineActiveOperation.shouldSkipBreakpoints() && e instanceof MIBreakpointHitEvent) {
    					fConnection.queueCommand(
    							fCommandFactory.createMIExecContinue(fRunToLineActiveOperation.getThreadContext()),
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
    					IBreakpointsTargetDMContext bpDmc = DMContexts.getAncestorOfType(fRunToLineActiveOperation.getThreadContext(),
    							IBreakpointsTargetDMContext.class);

    					fConnection.queueCommand(fCommandFactory.createMIBreakDelete(bpDmc, new int[] {fRunToLineActiveOperation.getBreakointId()}),
    							new DataRequestMonitor<MIInfo>(getExecutor(), null));
    					fRunToLineActiveOperation = null;
    				}
    			}
    		}
    	}
    	
		IMIExecutionDMContext threadDmc = DMContexts.getAncestorOfType(e.getDMContext(), IMIExecutionDMContext.class);
		if (e instanceof MISignalEvent && fDisableNextSignalEventDmcSet.remove(threadDmc)) {			
			fSilencedSignalEventMap.put(threadDmc, e);

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


    /**
     * @nooverride This method is not intended to be re-implemented or extended by clients.
     * @noreference This method is not intended to be referenced by clients.
     */
	@DsfServiceEventHandler
	public void eventDispatched(final MIThreadCreatedEvent e) {
		IContainerDMContext containerDmc = e.getDMContext();
		IMIExecutionDMContext executionCtx = null;
		if (e.getStrId() != null) {
			executionCtx = createMIExecutionContext(containerDmc, e.getStrId());
		}
		getSession().dispatchEvent(new StartedDMEvent(executionCtx, e),	getProperties());
	}

    /**
     * @nooverride This method is not intended to be re-implemented or extended by clients.
     * @noreference This method is not intended to be referenced by clients.
     */
	@DsfServiceEventHandler
	public void eventDispatched(final MIThreadExitEvent e) {
		IContainerDMContext containerDmc = e.getDMContext();
		IMIExecutionDMContext executionCtx = null;
		if (e.getStrId() != null) {
			executionCtx = createMIExecutionContext(containerDmc, e.getStrId());
		}
		getSession().dispatchEvent(new ExitedDMEvent(executionCtx, e), getProperties());
	}

    /**
     * @nooverride This method is not intended to be re-implemented or extended by clients.
     * @noreference This method is not intended to be referenced by clients.
     */
	@DsfServiceEventHandler
	public void eventDispatched(ResumedEvent e) {
		IExecutionDMContext ctx = e.getDMContext();
		if (ctx instanceof IMIExecutionDMContext) {			
			updateThreadState((IMIExecutionDMContext)ctx, e);
		}
	}

    /**
     * @nooverride This method is not intended to be re-implemented or extended by clients.
     * @noreference This method is not intended to be referenced by clients.
     */
	@DsfServiceEventHandler
	public void eventDispatched(SuspendedEvent e) {
		IExecutionDMContext ctx = e.getDMContext();
		if (ctx instanceof IMIExecutionDMContext) {			
			updateThreadState((IMIExecutionDMContext)ctx, e);
		}
	}

    /**
     * @nooverride This method is not intended to be re-implemented or extended by clients.
     * @noreference This method is not intended to be referenced by clients.
     */
	@DsfServiceEventHandler
	public void eventDispatched(StartedDMEvent e) {
		IExecutionDMContext executionCtx = e.getDMContext();
		if (executionCtx instanceof IMIExecutionDMContext) {			
			if (fThreadRunStates.get(executionCtx) == null) {
				fThreadRunStates.put((IMIExecutionDMContext)executionCtx, new MIThreadRunState());
			}
		}
	}

    /**
     * @nooverride This method is not intended to be re-implemented or extended by clients.
     * @noreference This method is not intended to be referenced by clients.
     */
	@DsfServiceEventHandler
	public void eventDispatched(ExitedDMEvent e) {
		fThreadRunStates.remove(e.getDMContext());
	}
	
    /**
     * @nooverride This method is not intended to be re-implemented or extended by clients.
     * @noreference This method is not intended to be referenced by clients.
     */
	@DsfServiceEventHandler
	public void eventDispatched(ICommandControlShutdownDMEvent e) {
		fTerminated = true;
	}


    /**
     * @nooverride This method is not intended to be re-implemented or extended by clients.
     * @noreference This method is not intended to be referenced by clients.
     * 
     * @since 2.0
     */
    @DsfServiceEventHandler
    public void eventDispatched(MIInferiorExitEvent e) {
    	if (fRunToLineActiveOperation != null) {
    		IBreakpointsTargetDMContext bpDmc = DMContexts.getAncestorOfType(fRunToLineActiveOperation.getThreadContext(),
    				IBreakpointsTargetDMContext.class);
    		int bpId = fRunToLineActiveOperation.getBreakointId();

    		fConnection.queueCommand(fCommandFactory.createMIBreakDelete(bpDmc, new int[] {bpId}),
    				new DataRequestMonitor<MIInfo>(getExecutor(), null));
    		fRunToLineActiveOperation = null;
    	}
    }

    /**
     * @deprecated Tracing is only supported with GDB 7.2, so this logic
     * was moved to the GDBRunControl_7_2_NS class.
	 * @since 3.0
	 */
    @Deprecated
    @DsfServiceEventHandler 
    public void eventDispatched(ITraceRecordSelectedChangedDMEvent e) {
    }
    
	@Override
	public void flushCache(IDMContext context) {
		refreshThreadStates();
	}

	/**
	 * Gets the state of each thread from GDB and updates our internal map.
	 * @since 4.1
	 */
	protected void refreshThreadStates() {
		fConnection.queueCommand(
			fCommandFactory.createMIThreadInfo(fConnection.getContext()),
			new DataRequestMonitor<MIThreadInfoInfo>(getExecutor(), null) {
				@Override
				protected void handleSuccess() {
					MIThread[] threadList = getData().getThreadList(); 
					for (MIThread thread : threadList) {
						String threadId = thread.getThreadId();
						IMIContainerDMContext containerDmc = 
								fProcessService.createContainerContextFromThreadId(fConnection.getContext(), threadId);
						IProcessDMContext processDmc = DMContexts.getAncestorOfType(containerDmc, IProcessDMContext.class);
						IThreadDMContext threadDmc =
								fProcessService.createThreadContext(processDmc, threadId);
						IMIExecutionDMContext execDmc = fProcessService.createExecutionContext(containerDmc, threadDmc, threadId);

						MIThreadRunState threadState = fThreadRunStates.get(execDmc);
						if (threadState != null) {
							// We may not know this thread.  This can happen when dealing with a remote
							// where thread events are not reported immediately.
							// However, the -thread-info command we just sent will make
							// GDB send those events.  Therefore, we can just ignore threads we don't
							// know about, and wait for those events.
							if (MIThread.MI_THREAD_STATE_RUNNING.equals(thread.getState())) {
								if (threadState.fSuspended == true) {
									// We missed a resumed event!  Send it now.
									IResumedDMEvent resumedEvent = new ResumedEvent(execDmc, null);
									fConnection.getSession().dispatchEvent(resumedEvent, getProperties());
								}
							} else if (MIThread.MI_THREAD_STATE_STOPPED.equals(thread.getState())) {
								if (threadState.fSuspended == false) {
									// We missed a suspend event!  Send it now.
									ISuspendedDMEvent suspendedEvent = new SuspendedEvent(execDmc, null);
									fConnection.getSession().dispatchEvent(suspendedEvent, getProperties());
								}
							} else {
								assert false : "Invalid thread state: " + thread.getState(); //$NON-NLS-1$
							}
						}
					}
				}
			});
	}
	
	private void moveToLocation(final IExecutionDMContext context,
			final String location, final Map<String, Object> bpAttributes,
			final RequestMonitor rm) {

		// first create a temporary breakpoint to stop the execution at
		// the location we are about to jump to
		IBreakpoints bpService = getServicesTracker().getService(IBreakpoints.class);
		IBreakpointsTargetDMContext bpDmc = DMContexts.getAncestorOfType(context, IBreakpointsTargetDMContext.class);
		if (bpService != null && bpDmc != null) {
			bpService.insertBreakpoint(bpDmc, bpAttributes,
					new DataRequestMonitor<IBreakpointDMContext>(getExecutor(),rm) {
						@Override
						protected void handleSuccess() {
							// Now resume at the proper location
							resumeAtLocation(context, location, rm);
						}
					});
		} else {
			rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID,
					IDsfStatusConstants.NOT_SUPPORTED,
					"Unable to set breakpoint", null)); //$NON-NLS-1$
			rm.done();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.dsf.debug.service.IRunControl2#canRunToLine(org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext, java.lang.String, int, org.eclipse.cdt.dsf.concurrent.DataRequestMonitor)
	 */
	/**
	 * @since 3.0
	 */
	@Override
	public void canRunToLine(IExecutionDMContext context, String sourceFile,
			int lineNumber, DataRequestMonitor<Boolean> rm) {
		canResume(context, rm);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.dsf.debug.service.IRunControl2#runToLine(org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext, java.lang.String, int, boolean, org.eclipse.cdt.dsf.concurrent.RequestMonitor)
	 */
	/**
	 * @since 3.0
	 */
	@Override
	public void runToLine(IExecutionDMContext context, String sourceFile,
			int lineNumber, boolean skipBreakpoints, RequestMonitor rm) {
		
		// Hack around a MinGW bug; see 196154
		sourceFile = adjustDebuggerPath(sourceFile);
		
		runToLocation(context, sourceFile + ":" + Integer.toString(lineNumber), skipBreakpoints, rm); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.dsf.debug.service.IRunControl2#canRunToAddress(org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext, org.eclipse.cdt.core.IAddress, org.eclipse.cdt.dsf.concurrent.DataRequestMonitor)
	 */
	/**
	 * @since 3.0
	 */
	@Override
	public void canRunToAddress(IExecutionDMContext context, IAddress address,
			DataRequestMonitor<Boolean> rm) {
		canResume(context, rm);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.dsf.debug.service.IRunControl2#runToAddress(org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext, org.eclipse.cdt.core.IAddress, boolean, org.eclipse.cdt.dsf.concurrent.RequestMonitor)
	 */
	/**
	 * @since 3.0
	 */
	@Override
	public void runToAddress(IExecutionDMContext context, IAddress address,
			boolean skipBreakpoints, RequestMonitor rm) {
		runToLocation(context, "*0x" + address.toString(16), skipBreakpoints, rm); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.dsf.debug.service.IRunControl2#canMoveToLine(org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext, java.lang.String, int, boolean, org.eclipse.cdt.dsf.concurrent.DataRequestMonitor)
	 */
	/**
	 * @since 3.0
	 */
	@Override
	public void canMoveToLine(IExecutionDMContext context, String sourceFile,
			int lineNumber, boolean resume, DataRequestMonitor<Boolean> rm) {
		canResume(context, rm);	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.dsf.debug.service.IRunControl2#moveToLine(org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext, java.lang.String, int, boolean, org.eclipse.cdt.dsf.concurrent.RequestMonitor)
	 */
	/**
	 * @since 3.0
	 */
	@Override
	public void moveToLine(IExecutionDMContext context, String sourceFile,
			int lineNumber, boolean resume, RequestMonitor rm) {
		IMIExecutionDMContext threadExecDmc = DMContexts.getAncestorOfType(context, IMIExecutionDMContext.class);
		if (threadExecDmc == null) {
            rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, DebugException.REQUEST_FAILED, "Invalid thread context", null)); //$NON-NLS-1$
            rm.done();                        	
		}
		else
		{
			String location = sourceFile + ":" + lineNumber; //$NON-NLS-1$
			if (resume)
				resumeAtLocation(context, location, rm);
			else
			{
				// Create the breakpoint attributes
				Map<String,Object> attr = new HashMap<String,Object>();
		    	attr.put(MIBreakpoints.BREAKPOINT_TYPE, MIBreakpoints.BREAKPOINT);
		    	attr.put(MIBreakpoints.FILE_NAME, sourceFile);
		    	attr.put(MIBreakpoints.LINE_NUMBER, lineNumber);
		    	attr.put(MIBreakpointDMData.IS_TEMPORARY, true);
		    	attr.put(MIBreakpointDMData.THREAD_ID, Integer.toString(threadExecDmc.getThreadId()));
				// Now do the operation
				moveToLocation(context, location, attr, rm);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.dsf.debug.service.IRunControl2#canMoveToAddress(org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext, org.eclipse.cdt.core.IAddress, boolean, org.eclipse.cdt.dsf.concurrent.DataRequestMonitor)
	 */
	/**
	 * @since 3.0
	 */
	@Override
	public void canMoveToAddress(IExecutionDMContext context, IAddress address,
			boolean resume, DataRequestMonitor<Boolean> rm) {
		canResume(context, rm);
		}

	/** (non-Javadoc)
	 * @see org.eclipse.cdt.dsf.debug.service.IRunControl2#moveToAddress(org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext, org.eclipse.cdt.core.IAddress, boolean, org.eclipse.cdt.dsf.concurrent.RequestMonitor)
	 * @since 3.0
	 */
	@Override
	public void moveToAddress(IExecutionDMContext context, IAddress address,
			boolean resume, RequestMonitor rm) {
		IMIExecutionDMContext threadExecDmc = DMContexts.getAncestorOfType(context, IMIExecutionDMContext.class);
		if (threadExecDmc == null) {
			rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, DebugException.REQUEST_FAILED, "Invalid thread context", null)); //$NON-NLS-1$
			rm.done();                        	
		}
		else
		{
			String location = "*0x" + address.toString(16); //$NON-NLS-1$
			if (resume)
				resumeAtLocation(context, location, rm);
			else
			{
				// Create the breakpoint attributes
				Map<String,Object> attr = new HashMap<String,Object>();
				attr.put(MIBreakpoints.BREAKPOINT_TYPE, MIBreakpoints.BREAKPOINT);
				attr.put(MIBreakpoints.ADDRESS, "0x" + address.toString(16)); //$NON-NLS-1$
				attr.put(MIBreakpointDMData.IS_TEMPORARY, true);
				attr.put(MIBreakpointDMData.THREAD_ID,  Integer.toString(threadExecDmc.getThreadId()));

				// Now do the operation
				moveToLocation(context, location, attr, rm);
			}
		}
	}

	/** @since 4.0 */
	@Override
	public IRunMode getRunMode() {
		return MIRunMode.NON_STOP;
	}

	/** @since 4.0 */
	@Override
	public boolean isTargetAcceptingCommands() {
		// Always accepting commands in non-stop mode
		return true;
	}

	/**
	 * See bug 196154
	 * 
	 * @param path
	 *            the absolute path to the source file
	 * @return the simple filename if running on Windows and [path] is not an
	 *         absolute UNIX one. Otherwise, [path] is returned
	 */
    private static String adjustDebuggerPath(String path) {
    	String result = path;
    	// Make it MinGW-specific
    	if (Platform.getOS().startsWith("win")) { //$NON-NLS-1$
        	if (!path.startsWith("/")) { //$NON-NLS-1$
        		path = path.replace('\\', '/');
        		result = path.substring(path.lastIndexOf('/') + 1);
        	}
    	}
    	return result;
    }
}

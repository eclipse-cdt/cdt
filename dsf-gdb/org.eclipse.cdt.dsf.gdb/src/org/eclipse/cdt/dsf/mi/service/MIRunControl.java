/*******************************************************************************
 * Copyright (c) 2006, 2013 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     Ericsson	AB		  - Modified for handling of multiple threads
 *     Vladimir Prus (Mentor Graphics) - Add proper stop reason for step return (Bug 362274) 
 *     Indel AG           - [369622] fixed moveToLine using MinGW
 *     Marc Khouzam (Ericsson) - Make each thread an IDisassemblyDMContext (bug 352748)
 *     Alvaro Sanchez-Leon (Ericsson AB) - Support for Step into selection (bug 244865)
 *     Alvaro Sanchez-Leon (Ericsson AB) - Bug 415362
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.core.model.IFunctionDeclaration;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.concurrent.ImmediateDataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateExecutor;
import org.eclipse.cdt.dsf.concurrent.ImmediateRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Immutable;
import org.eclipse.cdt.dsf.concurrent.MultiRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Sequence;
import org.eclipse.cdt.dsf.concurrent.Sequence.Step;
import org.eclipse.cdt.dsf.datamodel.AbstractDMContext;
import org.eclipse.cdt.dsf.datamodel.AbstractDMEvent;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.datamodel.IDMEvent;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointDMContext;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointsTargetDMContext;
import org.eclipse.cdt.dsf.debug.service.IBreakpointsExtension.IBreakpointHitDMEvent;
import org.eclipse.cdt.dsf.debug.service.ICachingService;
import org.eclipse.cdt.dsf.debug.service.IDisassembly.IDisassemblyDMContext;
import org.eclipse.cdt.dsf.debug.service.IProcesses;
import org.eclipse.cdt.dsf.debug.service.IRunControl3;
import org.eclipse.cdt.dsf.debug.service.ISourceLookup;
import org.eclipse.cdt.dsf.debug.service.ISourceLookup.ISourceLookupDMContext;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMContext;
import org.eclipse.cdt.dsf.debug.service.command.BufferedCommandControl;
import org.eclipse.cdt.dsf.debug.service.command.CommandCache;
import org.eclipse.cdt.dsf.debug.service.command.ICommand;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlShutdownDMEvent;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.gdb.internal.service.command.events.MITracepointSelectedEvent;
import org.eclipse.cdt.dsf.mi.service.MIBreakpoints.MIBreakpointDMContext;
import org.eclipse.cdt.dsf.mi.service.command.CommandFactory;
import org.eclipse.cdt.dsf.mi.service.command.events.IMIDMEvent;
import org.eclipse.cdt.dsf.mi.service.command.events.MIBreakpointHitEvent;
import org.eclipse.cdt.dsf.mi.service.command.events.MICatchpointHitEvent;
import org.eclipse.cdt.dsf.mi.service.command.events.MIErrorEvent;
import org.eclipse.cdt.dsf.mi.service.command.events.MIEvent;
import org.eclipse.cdt.dsf.mi.service.command.events.MIFunctionFinishedEvent;
import org.eclipse.cdt.dsf.mi.service.command.events.MIRunningEvent;
import org.eclipse.cdt.dsf.mi.service.command.events.MISharedLibEvent;
import org.eclipse.cdt.dsf.mi.service.command.events.MISignalEvent;
import org.eclipse.cdt.dsf.mi.service.command.events.MISteppingRangeEvent;
import org.eclipse.cdt.dsf.mi.service.command.events.MIStoppedEvent;
import org.eclipse.cdt.dsf.mi.service.command.events.MIThreadCreatedEvent;
import org.eclipse.cdt.dsf.mi.service.command.events.MIThreadExitEvent;
import org.eclipse.cdt.dsf.mi.service.command.events.MIWatchpointTriggerEvent;
import org.eclipse.cdt.dsf.mi.service.command.output.CLIThreadInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIThreadListIdsInfo;
import org.eclipse.cdt.dsf.service.AbstractDsfService;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.osgi.framework.BundleContext;


/**
 * 
 * <p>
 * Implementation note:
 * This class implements event handlers for the events that are generated by
 * this service itself.  When the event is dispatched, these handlers will
 * be called first, before any of the clients.  These handlers update the
 * service's internal state information to make them consistent with the
 * events being issued.  Doing this in the handlers as opposed to when
 * the events are generated, guarantees that the state of the service will
 * always be consistent with the events.
 * The purpose of this pattern is to allow clients that listen to service
 * events and track service state, to be perfectly in sync with the service
 * state.
 * @since 3.0
 */
public class MIRunControl extends AbstractDsfService implements IMIRunControl, ICachingService, IRunControl3
{
	private static class MIExecutionDMC extends AbstractDMContext implements IMIExecutionDMContext, IDisassemblyDMContext
	{
		/**
		 * Integer ID that is used to identify the thread in the GDB/MI protocol.
		 */
		private final int fThreadId;

		/**
		 * Constructor for the context.  It should not be called directly by clients.
		 * Instead clients should call {@link MIRunControl#createMIExecutionContext(IContainerDMContext, int)}
		 * to create instances of this context based on the thread ID.
		 * <p/>
		 * Classes extending {@link MIRunControl} may also extend this class to include
		 * additional information in the context.
		 * 
		 * @param sessionId Session that this context belongs to.
		 * @param containerDmc The container that this context belongs to.
		 * @param threadId GDB/MI thread identifier.
		 */
		protected MIExecutionDMC(String sessionId, IContainerDMContext containerDmc, int threadId) {
			super(sessionId, containerDmc != null ? new IDMContext[] { containerDmc } : new IDMContext[0]);
			fThreadId = threadId;
		}

		/**
		 * Returns the GDB/MI thread identifier of this context.
		 * @return
		 */
		@Override
		public int getThreadId(){
			return fThreadId;
		}

		@Override
		public String toString() { return baseToString() + ".thread[" + fThreadId + "]"; }  //$NON-NLS-1$ //$NON-NLS-2$

		@Override
		public boolean equals(Object obj) {
			return super.baseEquals(obj) && ((MIExecutionDMC)obj).fThreadId == fThreadId;
		}

		@Override
		public int hashCode() { return super.baseHashCode() ^ fThreadId; }
	}

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
	protected static class RunControlEvent<V extends IDMContext, T extends MIEvent<? extends IDMContext>> extends AbstractDMEvent<V>
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
		
		/**
		 * @since 3.0
		 */
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
	 * @since 3.0
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

	
	@Immutable
	protected static class ContainerSuspendedEvent extends SuspendedEvent
	implements IContainerSuspendedDMEvent
	{
		final IExecutionDMContext[] triggeringDmcs;
		ContainerSuspendedEvent(IContainerDMContext containerDmc, MIStoppedEvent miInfo, IExecutionDMContext triggeringDmc) {
			super(containerDmc, miInfo);
			this.triggeringDmcs = triggeringDmc != null
			? new IExecutionDMContext[] { triggeringDmc } : new IExecutionDMContext[0];
		}

		@Override
		public IExecutionDMContext[] getTriggeringContexts() {
			return triggeringDmcs;
		}
	}

   /**
     * Indicates that the given container has been suspended on a breakpoint.
     * @since 3.0
     */
    @Immutable
    protected static class ContainerBreakpointHitEvent extends ContainerSuspendedEvent
    implements IBreakpointHitDMEvent
    {
        final private IBreakpointDMContext[] fBreakpoints;
        
        ContainerBreakpointHitEvent(IContainerDMContext containerDmc, MIBreakpointHitEvent miInfo, IExecutionDMContext triggeringDmc, IBreakpointDMContext bpCtx) {
            super(containerDmc, miInfo, triggeringDmc);
            
            fBreakpoints = new IBreakpointDMContext[] { bpCtx };
        }
        
    	@Override
        public IBreakpointDMContext[] getBreakpoints() {
            return fBreakpoints;
        }
    }

	@Immutable
	protected static class ResumedEvent extends RunControlEvent<IExecutionDMContext, MIRunningEvent>
	implements IResumedDMEvent
	{
		ResumedEvent(IExecutionDMContext ctx, MIRunningEvent miInfo) {
			super(ctx, miInfo);
		}

		@Override
		public StateChangeReason getReason() {
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
			return StateChangeReason.UNKNOWN;
		}
	}

	@Immutable
	protected static class ContainerResumedEvent extends ResumedEvent
	implements IContainerResumedDMEvent
	{
		final IExecutionDMContext[] triggeringDmcs;

		ContainerResumedEvent(IContainerDMContext containerDmc, MIRunningEvent miInfo, IExecutionDMContext triggeringDmc) {
			super(containerDmc, miInfo);
			this.triggeringDmcs = triggeringDmc != null
			? new IExecutionDMContext[] { triggeringDmc } : new IExecutionDMContext[0];
		}

		@Override
		public IExecutionDMContext[] getTriggeringContexts() {
			return triggeringDmcs;
		}
	}

	@Immutable
	protected static class StartedDMEvent extends RunControlEvent<IExecutionDMContext,MIThreadCreatedEvent>
	implements IStartedDMEvent
	{
		StartedDMEvent(IMIExecutionDMContext executionDmc, MIThreadCreatedEvent miInfo) {
			super(executionDmc, miInfo);
		}
	}

	@Immutable
	protected static class ExitedDMEvent extends RunControlEvent<IExecutionDMContext,MIThreadExitEvent>
	implements IExitedDMEvent
	{
		ExitedDMEvent(IMIExecutionDMContext executionDmc, MIThreadExitEvent miInfo) {
			super(executionDmc, miInfo);
		}
	}

	private ICommandControlService fConnection;
	private CommandCache fMICommandCache;
	private CommandFactory fCommandFactory;
    
    // State flags
	private boolean fSuspended = true;
    private boolean fResumePending = false;
	private boolean fStepping = false;
	private boolean fTerminated = false;
	/**
	 * @since 4.2
	 */
	protected RunControlEvent<IExecutionDMContext, ?> fLatestEvent = null;
	
	
	/**
	 * What caused the state change. E.g., a signal was thrown.
	 */
	private StateChangeReason fStateChangeReason;

	/**
	 * Further detail on what caused the state change. E.g., the specific signal
	 * that was throw was a SIGINT. The exact string comes from gdb in the mi
	 * event. May be null, as not all types of state change have additional
	 * detail of interest.
	 */
	private String fStateChangeDetails;
	
	private IExecutionDMContext fStateChangeTriggeringContext;
	/** 
	 * Indicates that the next MIRunning event should be silenced.
	 * @since 4.3
	 */
	protected boolean fDisableNextRunningEvent;
	/** 
	 * Indicates that the next MISignal (MIStopped) event should be silenced.
	 * @since 4.3
	 */
	protected boolean fDisableNextSignalEvent;
	/** 
	 * Stores the silenced MIStopped event in case we need to use it
	 * for a failure.
	 * @since 4.3
	 */
	protected MIStoppedEvent fSilencedSignalEvent;
	
	private static final int FAKE_THREAD_ID = 0;

    public MIRunControl(DsfSession session) {
        super(session);
    }
    
	@Override
    public void initialize(final RequestMonitor rm) {
        super.initialize(
            new ImmediateRequestMonitor(rm) {
                @Override
                protected void handleSuccess() {
                    doInitialize(rm);
                }});
    }

    private void doInitialize(final RequestMonitor rm) {
        fConnection = getServicesTracker().getService(ICommandControlService.class);
        BufferedCommandControl bufferedCommandControl = new BufferedCommandControl(fConnection, getExecutor(), 2);
        
        fCommandFactory = getServicesTracker().getService(IMICommandControl.class).getCommandFactory();
		// This cache stores the result of a command when received; also, this cache
		// is manipulated when receiving events.  Currently, events are received after
		// three scheduling of the executor, while command results after only one.  This
		// can cause problems because command results might be processed before an event
		// that actually arrived before the command result.
		// To solve this, we use a bufferedCommandControl that will delay the command
		// result by two scheduling of the executor.
		// See bug 280461
        fMICommandCache = new CommandCache(getSession(), bufferedCommandControl);
        fMICommandCache.setContextAvailable(fConnection.getContext(), true);
        getSession().addServiceEventListener(this, null);
        rm.done();
    }

    @Override
    public void shutdown(final RequestMonitor rm) {
        getSession().removeServiceEventListener(this);
        fMICommandCache.reset();
        super.shutdown(rm);
    }
    
    public boolean isValid() { return true; }
    
    /** @since 2.0 */
    protected boolean isResumePending() { return fResumePending; }
    /** @since 2.0 */
    protected void setResumePending(boolean pending) { fResumePending = pending; }
    /** @since 2.0 */
    protected boolean isTerminated() { return fTerminated; }
    /** @since 2.0 */
    protected void setTerminated(boolean terminated) { fTerminated = terminated; }
    
    public CommandCache getCache() { return fMICommandCache; }
    /** @since 2.0 */
    protected ICommandControlService getConnection() { return fConnection; }

    public IMIExecutionDMContext createMIExecutionContext(IContainerDMContext container, int threadId) {
        return new MIExecutionDMC(getSession().getId(), container, threadId);
    }
    
    /**
     * @nooverride This method is not intended to be re-implemented or extended by clients.
     * @noreference This method is not intended to be referenced by clients.
     */
    @DsfServiceEventHandler
    public void eventDispatched(final MIRunningEvent e) {
    	if (fDisableNextRunningEvent) {
    		fDisableNextRunningEvent = false;
    		// We don't broadcast this running event
    		return;
    	}

        IDMEvent<?> event = null;
        // Find the container context, which is used in multi-threaded debugging.
        IContainerDMContext containerDmc = DMContexts.getAncestorOfType(e.getDMContext(), IContainerDMContext.class);
        if (containerDmc != null) {
            // Set the triggering context only if it's different than the container context.
            IExecutionDMContext triggeringCtx = !e.getDMContext().equals(containerDmc) ? e.getDMContext() : null;
            event = new ContainerResumedEvent(containerDmc, e, triggeringCtx);
        } else {
            event = new ResumedEvent(e.getDMContext(), e);
        }
        getSession().dispatchEvent(event, getProperties());
    }

    /**
     * @nooverride This method is not intended to be re-implemented or extended by clients.
     * @noreference This method is not intended to be referenced by clients.
     */
    @DsfServiceEventHandler
    public void eventDispatched(final MIStoppedEvent e) {
    	if (fDisableNextSignalEvent && e instanceof MISignalEvent) {
    		fDisableNextSignalEvent = false;
    		fSilencedSignalEvent = e;
    		// We don't broadcast this stopped event
    		return;
    	}

    	MIBreakpointDMContext _bp = null;
    	if (e instanceof MIBreakpointHitEvent) {
    	    int bpId = ((MIBreakpointHitEvent)e).getNumber();
            IBreakpointsTargetDMContext bpsTarget = DMContexts.getAncestorOfType(e.getDMContext(), IBreakpointsTargetDMContext.class);
            if (bpsTarget != null && bpId >= 0) {
                _bp = new MIBreakpointDMContext(getSession().getId(), new IDMContext[] {bpsTarget}, bpId); 
            }
    	}
        final MIBreakpointDMContext bp = _bp;
    	
    	IDMEvent<?> event = null;
        // Find the container context, which is used in multi-threaded debugging.
        final IContainerDMContext containerDmc = DMContexts.getAncestorOfType(e.getDMContext(), IContainerDMContext.class);
        if (containerDmc != null) {
            // Set the triggering context only if it's not the container context, since we are looking for a thread.
            IExecutionDMContext triggeringCtx = !e.getDMContext().equals(containerDmc) ? e.getDMContext() : null;
            if (triggeringCtx == null) {
            	// Still no thread.  Let's ask the backend for one.
            	// We need a proper thread id for the debug view to select the right thread
            	// Bug 300096 comment #15 and Bug 302597
				getConnection().queueCommand(
						fCommandFactory.createCLIThread(containerDmc),
						new DataRequestMonitor<CLIThreadInfo>(getExecutor(), null) {
							@Override
							protected void handleCompleted() {
								IExecutionDMContext triggeringCtx2 = null;
								if (isSuccess() && getData().getCurrentThread() != null) {
									triggeringCtx2 = createMIExecutionContext(containerDmc, getData().getCurrentThread());
								}
								IDMEvent<?> event2 = bp != null
								    ? new ContainerBreakpointHitEvent(containerDmc, (MIBreakpointHitEvent)e, triggeringCtx2, bp)
								    : new ContainerSuspendedEvent(containerDmc, e, triggeringCtx2);
								getSession().dispatchEvent(event2, getProperties());
							}
						});
				return;
            }
            if (bp != null) {
                event = new ContainerBreakpointHitEvent(containerDmc, (MIBreakpointHitEvent)e, triggeringCtx, bp);
            } else {
                event = new ContainerSuspendedEvent(containerDmc, e, triggeringCtx);
            }
        } else {
            if (bp != null) {
                event = new BreakpointHitEvent(e.getDMContext(), (MIBreakpointHitEvent)e, bp);
            } else {
                event = new SuspendedEvent(e.getDMContext(), e);
            }
        }
        getSession().dispatchEvent(event, getProperties());
    }

    /**
     * Thread Created event handling
     * When a new thread is created - OOB Event fired ~"[New Thread 1077300144 (LWP 7973)]\n"
     * @nooverride This method is not intended to be re-implemented or extended by clients.
     * @noreference This method is not intended to be referenced by clients.
     */
    @DsfServiceEventHandler
    public void eventDispatched(final MIThreadCreatedEvent e) {
        IContainerDMContext containerDmc = e.getDMContext();
        IMIExecutionDMContext executionCtx = e.getStrId() != null ? createMIExecutionContext(containerDmc, e.getId()) : null;
        getSession().dispatchEvent(new StartedDMEvent(executionCtx, e), getProperties());
    }

    /**
     * Thread exit event handling
     * When a new thread is destroyed - OOB Event fired "
     * @nooverride This method is not intended to be re-implemented or extended by clients.
     * @noreference This method is not intended to be referenced by clients.
     */
    @DsfServiceEventHandler
    public void eventDispatched(final MIThreadExitEvent e) {
        IContainerDMContext containerDmc = e.getDMContext();
        IMIExecutionDMContext executionCtx = e.getStrId() != null ? createMIExecutionContext(containerDmc, e.getId()) : null;
    	getSession().dispatchEvent(new ExitedDMEvent(executionCtx, e), getProperties());
    }

    /**
     * @nooverride This method is not intended to be re-implemented or extended by clients.
     * @noreference This method is not intended to be referenced by clients.
     */
    @DsfServiceEventHandler
    public void eventDispatched(ContainerResumedEvent e) {
        fSuspended = false;
        fResumePending = false;
        fStateChangeReason = e.getReason();
        fStateChangeDetails = null; // we have no details of interest for a resume
        fMICommandCache.setContextAvailable(e.getDMContext(), false);
        fLatestEvent = e;
        
        //fStateChangeTriggeringContext = e.getTriggeringContext();
        if (e.getReason().equals(StateChangeReason.STEP)) {
            fStepping = true;
        } else {
            fMICommandCache.reset();
        }
    }

    /**
     * @nooverride This method is not intended to be re-implemented or extended by clients.
     * @noreference This method is not intended to be referenced by clients.
     */
    @DsfServiceEventHandler
    public void eventDispatched(ContainerSuspendedEvent e) {
        fMICommandCache.setContextAvailable(e.getDMContext(), true);
        fMICommandCache.reset();
        fStateChangeReason = e.getReason();
        fStateChangeDetails = e.getDetails();
        fStateChangeTriggeringContext = e.getTriggeringContexts().length != 0
            ? e.getTriggeringContexts()[0] : null;
        fSuspended = true;
        fStepping = false;
        fLatestEvent = e;

        fResumePending = false;
    }
    
    /**
     * @nooverride This method is not intended to be re-implemented or extended by clients.
     * @noreference This method is not intended to be referenced by clients.
     * @since 1.1
     */
    @DsfServiceEventHandler
    public void eventDispatched(ICommandControlShutdownDMEvent e) {
        fTerminated = true;
	}


    /**
     * Event handler when New thread is created
     * @nooverride This method is not intended to be re-implemented or extended by clients.
     * @noreference This method is not intended to be referenced by clients.
     */
    @DsfServiceEventHandler
    public void eventDispatched(StartedDMEvent e) {

	}

    /**
     * @nooverride This method is not intended to be re-implemented or extended by clients.
     * @noreference This method is not intended to be referenced by clients.
     */
    @DsfServiceEventHandler
    public void eventDispatched(IExitedDMEvent e) {
    	if (e.getDMContext() instanceof IContainerDMContext) {
    		// When the process terminates, we should consider it as suspended
    		// In fact, we did get a stopped event, but our processing of it
    		// needs some cleaning up.  Until then, let's trigger of this event
    		// Bug 342358
            fMICommandCache.setContextAvailable(e.getDMContext(), true);
            fMICommandCache.reset();
            
    		fSuspended = true;
            fStepping = false;            
            fResumePending = false;
    	} else {
    		fMICommandCache.reset(e.getDMContext());
    	}
    }

    ///////////////////////////////////////////////////////////////////////////
    // AbstractService
    @Override
    protected BundleContext getBundleContext() {
        return GdbPlugin.getBundleContext();
    }
    
    ///////////////////////////////////////////////////////////////////////////
    // IRunControl
	@Override
	public void canResume(IExecutionDMContext context, DataRequestMonitor<Boolean> rm) {
        rm.setData(doCanResume(context));
        rm.done();
	}

    /** @since 2.0 */
	protected boolean doCanResume(IExecutionDMContext context) {
	    return !fTerminated && isSuspended(context) && !fResumePending;
	}

	@Override
	public void canSuspend(IExecutionDMContext context, DataRequestMonitor<Boolean> rm) {
        rm.setData(doCanSuspend(context));
        rm.done();
	}
	
    private boolean doCanSuspend(IExecutionDMContext context) {
        return !fTerminated && !isSuspended(context);
    }

	@Override
	public boolean isSuspended(IExecutionDMContext context) {
		return !fTerminated && fSuspended;
	}

	@Override
	public boolean isStepping(IExecutionDMContext context) {
    	return !fTerminated && fStepping;
    }

	@Override
	public void resume(final IExecutionDMContext context, final RequestMonitor rm) {
		assert context != null;

		if (doCanResume(context)) {
            ICommand<MIInfo> cmd = null;
            if(context instanceof IContainerDMContext) {
            	cmd = fCommandFactory.createMIExecContinue(context);
            } else {
        		IMIExecutionDMContext dmc = DMContexts.getAncestorOfType(context, IMIExecutionDMContext.class);
    			if (dmc == null) {
    	            rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_STATE, "Given context: " + context + " is not an execution context.", null)); //$NON-NLS-1$ //$NON-NLS-2$
    	            rm.done();
    	            return;
    			}
            	cmd = fCommandFactory.createMIExecContinue(dmc);//, new String[0]);
            }
            
            fResumePending = true;
            // Cygwin GDB will accept commands and execute them after the step
            // which is not what we want, so mark the target as unavailable
            // as soon as we send a resume command.
            fMICommandCache.setContextAvailable(context, false);

            fConnection.queueCommand(
            	cmd,
            	new DataRequestMonitor<MIInfo>(getExecutor(), rm) {
                    @Override
                    protected void handleFailure() {
        	            fResumePending = false;
        	            fMICommandCache.setContextAvailable(context, true);

                        super.handleFailure();
                    }
            	}
            );
        } else {
            rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_STATE, "Given context: " + context + ", is already running.", null)); //$NON-NLS-1$ //$NON-NLS-2$
            rm.done();
        }
	}
	
	@Override
	public void suspend(IExecutionDMContext context, final RequestMonitor rm){
		assert context != null;

		if (doCanSuspend(context)) {
			ICommand<MIInfo> cmd = null;
			if(context instanceof IContainerDMContext){
				cmd = fCommandFactory.createMIExecInterrupt(context);
			}
			else {
				IMIExecutionDMContext dmc = DMContexts.getAncestorOfType(context, IMIExecutionDMContext.class);
				if (dmc == null){
		            rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, NOT_SUPPORTED, "Given context: " + context + " is not an execution context.", null)); //$NON-NLS-1$ //$NON-NLS-2$
		            rm.done();
		            return;
				}
				cmd = fCommandFactory.createMIExecInterrupt(dmc);
			}
            fConnection.queueCommand(cmd, new DataRequestMonitor<MIInfo>(getExecutor(), rm));
        } else {
            rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, NOT_SUPPORTED, "Given context: " + context + ", is already suspended.", null)); //$NON-NLS-1$ //$NON-NLS-2$
            rm.done();
        }
    }
    
	@Override
    public void canStep(IExecutionDMContext context, StepType stepType, DataRequestMonitor<Boolean> rm) {
    	if (context instanceof IContainerDMContext) {
    		rm.setData(false);
    		rm.done();
    		return;
    	}
        canResume(context, rm);
    }
    
	@Override
	public void step(IExecutionDMContext context, StepType stepType, final RequestMonitor rm) {
		step(context, stepType, true, rm);
	}
	
    /**
	 * @since 4.2
	 */
    protected void step(final IExecutionDMContext context, StepType stepType, boolean checkCanResume, final RequestMonitor rm) {
    	assert context != null;

    	IMIExecutionDMContext dmc = DMContexts.getAncestorOfType(context, IMIExecutionDMContext.class);
		if (dmc == null){
            rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, NOT_SUPPORTED, "Given context: " + context + " is not an execution context.", null)); //$NON-NLS-1$ //$NON-NLS-2$
            rm.done();
            return;
		}
    	
    	if (checkCanResume && !doCanResume(context)) {
            rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_STATE, "Cannot resume context", null)); //$NON-NLS-1$
            rm.done();
            return;
        }

        ICommand<MIInfo> cmd = null;
        switch(stepType) {
            case STEP_INTO:
                cmd = fCommandFactory.createMIExecStep(dmc, 1);
                break;
            case STEP_OVER:
                cmd = fCommandFactory.createMIExecNext(dmc);
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
                    cmd = fCommandFactory.createMIExecFinish(topFrameDmc);
                } else {
                    rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, NOT_SUPPORTED, "Cannot create context for command, stack service not available.", null)); //$NON-NLS-1$
                    rm.done();
                    return;
                }
                break;
            case INSTRUCTION_STEP_INTO:
                cmd = fCommandFactory.createMIExecStepInstruction(dmc, 1);
                break;
            case INSTRUCTION_STEP_OVER:
            	cmd = fCommandFactory.createMIExecNextInstruction(dmc, 1);
                break;
            default:
                rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INTERNAL_ERROR, "Given step type not supported", null)); //$NON-NLS-1$
                rm.done();
                return;
        }
        
        fResumePending = true;
        fStepping = true;
        fMICommandCache.setContextAvailable(context, false);

        fConnection.queueCommand(cmd, new DataRequestMonitor<MIInfo>(getExecutor(), rm) {
        	@Override
        	public void handleFailure() {
                fResumePending = false;
                fStepping = false;
                fMICommandCache.setContextAvailable(context, true);
                
                super.handleFailure();
        	}
        });

    }

	@Override
    public void getExecutionContexts(final IContainerDMContext containerDmc, final DataRequestMonitor<IExecutionDMContext[]> rm) {
		fMICommandCache.execute(
				fCommandFactory.createMIThreadListIds(containerDmc),
				new DataRequestMonitor<MIThreadListIdsInfo>(
						getExecutor(), rm) {
					@Override
					protected void handleSuccess() {
						rm.setData(makeExecutionDMCs(containerDmc, getData()));
						rm.done();
					}
				});
    }
    

	private IExecutionDMContext[] makeExecutionDMCs(IContainerDMContext containerCtx, MIThreadListIdsInfo info) {
		if (info.getThreadIds().length == 0) {
			// Main thread always exist even if it is not reported by GDB.
			// So create thread-id = 0 when no thread is reported.
			// This hack is necessary to prevent AbstractMIControl from issuing a thread-select
			// because it doesn't work if the application was not compiled with pthread.
			return new IMIExecutionDMContext[]{createMIExecutionContext(containerCtx, FAKE_THREAD_ID)};
		} else {
			IExecutionDMContext[] executionDmcs = new IMIExecutionDMContext[info.getThreadIds().length];
			for (int i = 0; i < info.getThreadIds().length; i++) {
				executionDmcs[i] = createMIExecutionContext(containerCtx, info.getThreadIds()[i]);
			}
			return executionDmcs;
		}
	}
	
	@Override
	public void getExecutionData(IExecutionDMContext dmc, DataRequestMonitor<IExecutionDMData> rm){
        if (dmc instanceof IContainerDMContext) {
            rm.setData( new ExecutionData(fStateChangeReason, fStateChangeDetails) );
        } else if (dmc instanceof IMIExecutionDMContext) {
        	boolean thisThreadCausedStateChange = dmc.equals(fStateChangeTriggeringContext);
    	    StateChangeReason reason = thisThreadCausedStateChange ? fStateChangeReason : StateChangeReason.CONTAINER;
    	    String details = thisThreadCausedStateChange ? fStateChangeDetails : null;
    		rm.setData(new ExecutionData(reason, details));
        } else {
            rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_HANDLE, "Given context: " + dmc + " is not an execution context.", null)); //$NON-NLS-1$ //$NON-NLS-2$
        }
        rm.done();
    }

	/** @since 3.0 */
	protected void runToLocation(IExecutionDMContext context, String location, boolean skipBreakpoints, final RequestMonitor rm){
	    // skipBreakpoints is not used at the moment. Implement later
	    
    	assert context != null;

    	IMIExecutionDMContext dmc = DMContexts.getAncestorOfType(context, IMIExecutionDMContext.class);
		if (dmc == null){
            rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, NOT_SUPPORTED, "Given context: " + context + " is not an execution context.", null)); //$NON-NLS-1$ //$NON-NLS-2$
            rm.done();
            return;
		}

        if (doCanResume(dmc)) {
            fResumePending = true;
            fMICommandCache.setContextAvailable(dmc, false);
    		fConnection.queueCommand(fCommandFactory.createMIExecUntil(dmc, location),
    				new DataRequestMonitor<MIInfo>(getExecutor(), rm));
        } else {
            rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, NOT_SUPPORTED,
            		"Cannot resume given DMC.", null)); //$NON-NLS-1$
            rm.done();
        }
	}

	/** @since 3.0 */
	protected void resumeAtLocation(IExecutionDMContext context, String location, RequestMonitor rm) {
		assert context != null;

		final IMIExecutionDMContext dmc = DMContexts.getAncestorOfType(context, IMIExecutionDMContext.class);
		if (dmc == null){
			rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INTERNAL_ERROR, "Given context: " + context + " is not an thread execution context.", null)); //$NON-NLS-1$  //$NON-NLS-2$
			rm.done();
			return;
		}

		if (doCanResume(dmc)) {
			fResumePending = true;
			fMICommandCache.setContextAvailable(dmc, false);
			fConnection.queueCommand(
					fCommandFactory.createCLIJump(dmc, location),
					new DataRequestMonitor<MIInfo>(getExecutor(), rm) {
						@Override
						protected void handleFailure() {
							fResumePending = false;
							fMICommandCache.setContextAvailable(dmc, true);

							super.handleFailure();
						}
					});
		} else {
			rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, NOT_SUPPORTED,
					"Cannot resume given DMC.", null)); //$NON-NLS-1$
					rm.done();
		}		
	}

	/* ******************************************************************************
	 * Section to support operations even when the target is unavailable.
	 *
	 * Basically, we must make sure the container is suspended before making
	 * certain operations (currently breakpoints).  If we don't, we must first 
	 * suspend the container, then perform the specified operations,
	 * and finally resume the container.
	 * See http://bugs.eclipse.org/242943
	 * and http://bugs.eclipse.org/282273
	 * 
	 * Note that for multi-process, the correct container must be suspended for the
	 * breakpoint to be inserted on that container.  Not a big deal though, since
	 * a breakpointDmc is mapped to a specific container.  Also, since we are in
	 * all-stop mode here, it does not really matter what we stop, everything will
	 * stop.
	 * See http://bugs.eclipse.org/337893
	 * 
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

	// Keep track of if the target was available or not when we started the operation
	private boolean fTargetAvailable;
	// The execution context that need to be available.
	private IExecutionDMContext fExecutionDmc;
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
	// to receive more steps to execute continuously, and be able to upate the MultiRM.
	private MultiRequestMonitor<RequestMonitor> fExecuteQueuedOpsStepMonitor;
	// The number of batches of steps that are still being executing for potentially
	// concurrent executeWithTargetAvailable() operations.
	// Once this gets to zero, we know we have executed all the steps we were aware of
	// and we can complete the operation.
	private int fNumStepsStillExecuting;
	// Queue of executeWithTargetAvailable() operations that need to be processed.
	private LinkedList<TargetAvailableOperationInfo> fOperationsPending = new LinkedList<TargetAvailableOperationInfo>();
	
	/**
	 * Returns whether the target is available to perform operations
	 * @since 3.0
	 */
	protected boolean isTargetAvailable() {
		return fTargetAvailable;
	}

	/** @since 4.0 */
	protected void setTargetAvailable(boolean available) {
		fTargetAvailable = available;
	}
	
	/**
	 * Returns the execution context that needs to be suspended to perform the
	 * required operation.
	 * @since 3.0
	 */
	protected IExecutionDMContext getContextToSuspend() {
		return fExecutionDmc;
	}

	/** @since 4.0 */
	protected void setContextToSuspend(IExecutionDMContext context) {
		fExecutionDmc = context;
	}

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
	 * This part of the sequence verifies if the execution context of interest
	 * is suspended or not.
	 * @since 3.0
	 */
	protected class IsTargetAvailableStep extends Sequence.Step {
		final IDMContext fCtx;
		
		public IsTargetAvailableStep(IDMContext ctx) {
			fCtx = ctx;
		}
		
		@Override
		public void execute(final RequestMonitor rm) {
			fExecutionDmc = DMContexts.getAncestorOfType(fCtx, IMIContainerDMContext.class);
			if (fExecutionDmc != null) {
				fTargetAvailable = isSuspended(fExecutionDmc);
				rm.done();
				return;
			}

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
								// In all-stop, if any process is suspended, then all of them are suspended
								// so we only need to check the first process.
								fExecutionDmc = (IExecutionDMContext)(getData()[0]);
								fTargetAvailable = isSuspended(fExecutionDmc);
							}
							rm.done();
						}
					});
		}
	};

	/**
	 * If the execution context of interest is not suspended, this step
	 * will interrupt it.
	 * @since 3.0
	 */
	protected class MakeTargetAvailableStep extends Sequence.Step {
		@Override
		public void execute(final RequestMonitor rm) {
			if (!isTargetAvailable()) {
				assert fDisableNextRunningEvent == false;
				assert fDisableNextSignalEvent == false;
				
				// Don't broadcast the coming stopped signal event
				fDisableNextSignalEvent = true;
				suspend(getContextToSuspend(), 
						new RequestMonitor(getExecutor(), rm) {
					@Override
					protected void handleFailure() {
						// We weren't able to suspend, so abort the operation
						fDisableNextSignalEvent = false;
						super.handleFailure();
					};
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
			if (!isTargetAvailable()) {
				assert fDisableNextRunningEvent == false;
				fDisableNextRunningEvent = true;
				
				// Can't use the resume() call because we 'silently' stopped
				// so resume() will not know we are actually stopped
				fConnection.queueCommand(
						fCommandFactory.createMIExecContinue(getContextToSuspend()),
						new DataRequestMonitor<MIInfo>(getExecutor(), rm) {
							@Override
							protected void handleSuccess() {
								fSilencedSignalEvent = null;
								super.handleSuccess();
							}

							@Override
							protected void handleFailure() {
								// Darn, we're unable to restart the target.  Must cleanup!
								fDisableNextRunningEvent = false;
								
								// We must also sent the Stopped event that we had kept silent
								if (fSilencedSignalEvent != null) {
									eventDispatched(fSilencedSignalEvent);
									fSilencedSignalEvent = null;
								} else {
									// Maybe the stopped event didn't arrive yet.
									// We don't want to silence it anymore
									fDisableNextSignalEvent = false;
								}

								super.handleFailure();
							}
						});
			} else {
				// We didn't suspend the container, so we don't need to resume it
				rm.done();
			}
		}
	 };

	 /* ******************************************************************************
	  * End of section to support operations even when the target is unavailable.
	  * ******************************************************************************/

	/**
	 * {@inheritDoc}
     * @since 1.1
     */
		@Override
	public void flushCache(IDMContext context) {
		fMICommandCache.reset(context);		
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
	public void runToLine(final IExecutionDMContext context, String sourceFile,
			final int lineNumber, final boolean skipBreakpoints, final RequestMonitor rm) {
		
        determineDebuggerPath(context, sourceFile, new ImmediateDataRequestMonitor<String>(rm) {
            @Override
            protected void handleSuccess() {
        		runToLocation(context, getData() + ":" + Integer.toString(lineNumber), skipBreakpoints, rm); //$NON-NLS-1$
            }
        });
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
		canResume(context, rm);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.dsf.debug.service.IRunControl2#moveToLine(org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext, java.lang.String, int, boolean, org.eclipse.cdt.dsf.concurrent.RequestMonitor)
	 */
	/**
	 * @since 3.0
	 */
	@Override
	public void moveToLine(final IExecutionDMContext context, String sourceFile,
			final int lineNumber, final boolean resume, final RequestMonitor rm) {
		final IMIExecutionDMContext threadExecDmc = DMContexts.getAncestorOfType(context, IMIExecutionDMContext.class);
		if (threadExecDmc == null) {
            rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, DebugException.REQUEST_FAILED, "Invalid thread context", null)); //$NON-NLS-1$
            rm.done();                        	
		}
		else
		{
			determineDebuggerPath(context, sourceFile, new ImmediateDataRequestMonitor<String>(rm) {
				@Override
				protected void handleSuccess() {
					String debuggerPath = getData();

					String location = debuggerPath + ":" + lineNumber; //$NON-NLS-1$
					if (resume) {
						resumeAtLocation(context, location, rm);
					} else {
						// Create the breakpoint attributes
						Map<String,Object> attr = new HashMap<String,Object>();
						attr.put(MIBreakpoints.BREAKPOINT_TYPE, MIBreakpoints.BREAKPOINT);
						attr.put(MIBreakpoints.FILE_NAME, debuggerPath);
						attr.put(MIBreakpoints.LINE_NUMBER, lineNumber);
						attr.put(MIBreakpointDMData.IS_TEMPORARY, true);
						attr.put(MIBreakpointDMData.THREAD_ID, Integer.toString(threadExecDmc.getThreadId()));

						// Now do the operation
						moveToLocation(context, location, attr, rm);
					}
				}
			});
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

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.dsf.debug.service.IRunControl2#moveToAddress(org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext, org.eclipse.cdt.core.IAddress, boolean, org.eclipse.cdt.dsf.concurrent.RequestMonitor)
	 */
	/**
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
			else {
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
		return MIRunMode.ALL_STOP;
	}
	
	/** @since 4.0 */
	@Override
	public boolean isTargetAcceptingCommands() {
		// For all-stop mode:
		// 1- if GDB is not terminated and
		// 2- if execution is suspended and 
		// 3- if we didn't just send a resume/stop command, then 
		// we know GDB is accepting commands
		return !fTerminated && fSuspended && !fResumePending;
	}
	
	/**
	 * Determine the path that should be sent to the debugger as per the source lookup service.
	 * 
	 * @param dmc A context that can be used to obtain the sourcelookup context.
	 * @param hostPath The path of the file on the host, which must be converted.
	 * @param rm The result of the conversion.
	 * @since 4.2
	 */
    protected void determineDebuggerPath(IDMContext dmc, String hostPath, final DataRequestMonitor<String> rm)
    {
    	final IBreakpoints breakpoints = getServicesTracker().getService(IBreakpoints.class);
    	if (!(breakpoints instanceof IMIBreakpointPathAdjuster)) {
    		rm.done(hostPath);
    		return;
    	}
    	ISourceLookup sourceLookup = getServicesTracker().getService(ISourceLookup.class);
    	ISourceLookupDMContext srcDmc = DMContexts.getAncestorOfType(dmc, ISourceLookupDMContext.class);
    	if (sourceLookup == null || srcDmc == null) {
    		// Source lookup not available for given context, use the host
    		// path for the debugger path.
    		// Hack around a MinGW bug; see 369622 (and also 196154 and 232415)
    		rm.done(((IMIBreakpointPathAdjuster)breakpoints).adjustDebuggerPath(hostPath));
    		return;
    	}

    	sourceLookup.getDebuggerPath(srcDmc, hostPath, new DataRequestMonitor<String>(getExecutor(), rm) {
    		@Override
    		protected void handleSuccess() {
    			// Hack around a MinGW bug; see 369622 (and also 196154 and 232415)
    			rm.done(((IMIBreakpointPathAdjuster)breakpoints).adjustDebuggerPath(getData()));
    		}
    	});
    }
    
	/**
	 * @since 4.2
	 */
	@Override
	public void canStepIntoSelection(IExecutionDMContext context, String sourceFile, int lineNumber, IFunctionDeclaration selectedFunction, DataRequestMonitor<Boolean> rm) {
		rm.done(false);
	}

	/**
	 * @since 4.2
	 */
	@Override
	public void stepIntoSelection(IExecutionDMContext context, String sourceFile, int lineNumber, boolean skipBreakpoints, IFunctionDeclaration selectedFunction, RequestMonitor rm) {
		IStatus status = new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, DebugException.REQUEST_FAILED, "Step Into Selection not supported", null);  //$NON-NLS-1$
		rm.done(status);
	}
    
}

/*******************************************************************************
 * Copyright (c) 2008 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson - initial API and implementation
 *******************************************************************************/
package org.eclipse.dd.mi.service;

import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.dd.dsf.concurrent.DataRequestMonitor;
import org.eclipse.dd.dsf.concurrent.Immutable;
import org.eclipse.dd.dsf.concurrent.RequestMonitor;
import org.eclipse.dd.dsf.datamodel.AbstractDMContext;
import org.eclipse.dd.dsf.datamodel.AbstractDMEvent;
import org.eclipse.dd.dsf.datamodel.DMContexts;
import org.eclipse.dd.dsf.datamodel.IDMContext;
import org.eclipse.dd.dsf.debug.service.ICachingService;
import org.eclipse.dd.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.dd.dsf.debug.service.IRunControl.IContainerResumedDMEvent;
import org.eclipse.dd.dsf.debug.service.IRunControl.IContainerSuspendedDMEvent;
import org.eclipse.dd.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.dd.dsf.debug.service.IRunControl.IExitedDMEvent;
import org.eclipse.dd.dsf.debug.service.IRunControl.IResumedDMEvent;
import org.eclipse.dd.dsf.debug.service.IRunControl.IStartedDMEvent;
import org.eclipse.dd.dsf.debug.service.IRunControl.ISuspendedDMEvent;
import org.eclipse.dd.dsf.debug.service.command.CommandCache;
import org.eclipse.dd.dsf.debug.service.command.ICommandControlService;
import org.eclipse.dd.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.dd.dsf.service.AbstractDsfService;
import org.eclipse.dd.dsf.service.DsfServiceEventHandler;
import org.eclipse.dd.dsf.service.DsfSession;
import org.eclipse.dd.mi.internal.MIPlugin;
import org.eclipse.dd.mi.service.command.commands.CLIAttach;
import org.eclipse.dd.mi.service.command.commands.CLIDetach;
import org.eclipse.dd.mi.service.command.commands.CLIInfoThreads;
import org.eclipse.dd.mi.service.command.commands.MIThreadListIds;
import org.eclipse.dd.mi.service.command.output.CLIInfoThreadsInfo;
import org.eclipse.dd.mi.service.command.output.MIInfo;
import org.eclipse.dd.mi.service.command.output.MIThreadListIdsInfo;
import org.osgi.framework.BundleContext;


/**
 * @since 1.1
 */
public class MIProcesses extends AbstractDsfService implements IMIProcesses, ICachingService {
	
	// Below is the context hierarchy that is implemented between the
	// MIProcesses service and the MIRunControl service for the MI 
	// implementation of DSF:
	//
	//                           MIControlDMContext
	//                                |
	//                           MIProcessDMC (IProcess)
	//    MIContainerDMC ______/      |
	//     (IContainer)               |
	//          |                MIThreadDMC (IThread)
	//    MIExecutionDMC  _____/
	//     (IExecution)
	//
	
	/**
	 * Context representing a thread in GDB/MI
	 */
	@Immutable
	private static class MIExecutionDMC extends AbstractDMContext 
	implements IMIExecutionDMContext
	{
		/**
		 * String ID that is used to identify the thread in the GDB/MI protocol.
		 */
		private final String fThreadId;

		/**
		 * Constructor for the context.  It should not be called directly by clients.
		 * Instead clients should call {@link IMIProcesses#createExecutionContext()}
		 * to create instances of this context based on the thread ID.
		 * <p/>
		 * 
		 * @param sessionId Session that this context belongs to.
		 * @param containerDmc The container that this context belongs to.
		 * @param threadDmc The thread context parents of this context.
		 * @param threadId GDB/MI thread identifier.
		 */
        protected MIExecutionDMC(String sessionId, IContainerDMContext containerDmc, IThreadDMContext threadDmc, String threadId) {
            super(sessionId, 
                  containerDmc == null && threadDmc == null ? new IDMContext[0] :  
                      containerDmc == null ? new IDMContext[] { threadDmc } :
                          threadDmc == null ? new IDMContext[] { containerDmc } :
                              new IDMContext[] { containerDmc, threadDmc });
            fThreadId = threadId;
        }

		/**
		 * Returns the GDB/MI thread identifier of this context.
		 * @return
		 */
		public int getThreadId(){
			try {
				return Integer.parseInt(fThreadId);
			} catch (NumberFormatException e) {
			}
			
			return 0;
		}

		public String getId(){
			return fThreadId;
		}

		@Override
		public String toString() { return baseToString() + ".thread[" + fThreadId + "]"; }  //$NON-NLS-1$ //$NON-NLS-2$

		@Override
		public boolean equals(Object obj) {
			return super.baseEquals(obj) && ((MIExecutionDMC)obj).fThreadId.equals(fThreadId);
		}

		@Override
		public int hashCode() { return super.baseHashCode() ^ fThreadId.hashCode(); }
	}

	/**
	 * Context representing a thread group of GDB/MI. 
	 */
    @Immutable
	protected static class MIContainerDMC extends AbstractDMContext
	implements IMIContainerDMContext
	{
		/**
		 * String ID that is used to identify the thread group in the GDB/MI protocol.
		 */
		private final String fId;

		/**
		 * Constructor for the context.  It should not be called directly by clients.
		 * Instead clients should call {@link IMIProcesses#createContainerContext
		 * to create instances of this context based on the group name.
		 * 
		 * @param sessionId Session that this context belongs to.
		 * @param processDmc The process context that is the parent of this context.
		 * @param groupId GDB/MI thread group identifier.
		 */
		public MIContainerDMC(String sessionId, IProcessDMContext processDmc, String groupId) {
			super(sessionId, processDmc == null ? new IDMContext[0] : new IDMContext[] { processDmc });
			fId = groupId;
		}

		/**
		 * Returns the GDB/MI thread group identifier of this context.
		 */
		public String getGroupId(){ return fId; }

		@Override
		public String toString() { return baseToString() + ".threadGroup[" + fId + "]"; }  //$NON-NLS-1$ //$NON-NLS-2$

		@Override
		public boolean equals(Object obj) {
			return super.baseEquals(obj) && 
			       (((MIContainerDMC)obj).fId == null ? fId == null : ((MIContainerDMC)obj).fId.equals(fId));
		}

		@Override
		public int hashCode() { return super.baseHashCode() ^ (fId == null ? 0 : fId.hashCode()); }
	}

	/**
	 * Context representing a thread. 
	 */
    @Immutable
    private static class MIThreadDMC extends AbstractDMContext
    implements IThreadDMContext
    {
    	/**
    	 * ID used by GDB to refer to threads.
    	 * We use the same id as the one used in {@link MIProcesses#MIExecutionDMC}
    	 */
    	private final String fId;

    	/**
    	 * Constructor for the context.  It should not be called directly by clients.
    	 * Instead clients should call {@link IMIProcesses#createThreadContext}
    	 * to create instances of this context based on the thread ID.
    	 * <p/>
    	 * 
    	 * @param sessionId Session that this context belongs to.
    	 * @param processDmc The process that this thread belongs to.
    	 * @param id thread identifier.
    	 */
    	public MIThreadDMC(String sessionId, IProcessDMContext processDmc, String id) {
			super(sessionId, processDmc == null ? new IDMContext[0] : new IDMContext[] { processDmc });
    		fId = id;
    	}

    	/**
    	 * Returns the thread identifier of this context.
    	 * @return
    	 */
    	public String getId(){ return fId; }

    	@Override
    	public String toString() { return baseToString() + ".OSthread[" + fId + "]"; }  //$NON-NLS-1$ //$NON-NLS-2$

		@Override
		public boolean equals(Object obj) {
			return super.baseEquals(obj) && 
			       (((MIThreadDMC)obj).fId == null ? fId == null : ((MIThreadDMC)obj).fId.equals(fId));
		}

		@Override
		public int hashCode() { return super.baseHashCode() ^ (fId == null ? 0 : fId.hashCode()); }
    }

    @Immutable
    private static class MIProcessDMC extends AbstractDMContext
    implements IMIProcessDMContext
    {
      	/**
    	 * ID given by the OS.
     	 * For practicality, we use the same id as the one used in {@link MIProcesses#MIContainerDMC}
    	 */
    	private final String fId;

    	/**
    	 * Constructor for the context.  It should not be called directly by clients.
    	 * Instead clients should call {@link IMIProcesses#createProcessContext}
    	 * to create instances of this context based on the PID.
    	 * <p/>
    	 * 
    	 * @param sessionId Session that this context belongs to.
         * @param controlDmc The control context parent of this process.
    	 * @param id process identifier.
    	 */
    	public MIProcessDMC(String sessionId, ICommandControlDMContext controlDmc, String id) {
			super(sessionId, controlDmc == null ? new IDMContext[0] : new IDMContext[] { controlDmc });
    		fId = id;
    	}
    	
    	public String getProcId() { return fId; }

    	@Override
    	public String toString() { return baseToString() + ".proc[" + fId + "]"; }  //$NON-NLS-1$ //$NON-NLS-2$

		@Override
		public boolean equals(Object obj) {
			return super.baseEquals(obj) && 
			       (((MIProcessDMC)obj).fId == null ? fId == null : ((MIProcessDMC)obj).fId.equals(fId));
		}

		@Override
		public int hashCode() { return super.baseHashCode() ^ (fId == null ? 0 : fId.hashCode()); }
    }
    
    /*
     * The data of a corresponding thread or process.
     */
    @Immutable
    protected static class MIThreadDMData implements IThreadDMData {
    	final String fName;
    	final String fId;
    	
    	public MIThreadDMData(String name, String id) {
    		fName = name;
    		fId = id;
    	}
    	
		public String getId() { return fId; }
		public String getName() { return fName; }
		public boolean isDebuggerAttached() {
			return true;
		}
    }
    
    /**
     * Event indicating that an execution group (debugged process) has started.  This event
     * implements the {@link IStartedMDEvent} from the IRunControl service. 
     */
    public static class ContainerStartedDMEvent extends AbstractDMEvent<IExecutionDMContext> 
        implements IStartedDMEvent
    {
        public ContainerStartedDMEvent(IContainerDMContext context) {
            super(context);
        }
    }        
    
    /**
     * Event indicating that an execution group is no longer being debugged.  This event
     * implements the {@link IExitedMDEvent} from the IRunControl service. 
     */
    public static class ContainerExitedDMEvent extends AbstractDMEvent<IExecutionDMContext> 
        implements IExitedDMEvent
    {
        public ContainerExitedDMEvent(IContainerDMContext context) {
            super(context);
        }
    }        

    private ICommandControlService fCommandControl;
	private CommandCache fContainerCommandCache;

	private static final String FAKE_THREAD_ID = "0"; //$NON-NLS-1$
	// The unique id should be an empty string so that the views know not to display the fake id
	public static final String UNIQUE_GROUP_ID = ""; //$NON-NLS-1$

    public MIProcesses(DsfSession session) {
    	super(session);
    }

    /**
     * This method initializes this service.
     * 
     * @param requestMonitor
     *            The request monitor indicating the operation is finished
     */
    @Override
    public void initialize(final RequestMonitor requestMonitor) {
    	super.initialize(new RequestMonitor(getExecutor(), requestMonitor) {
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
        
//		// Register this service.
//		register(new String[] { IProcesses.class.getName(),
//				MIProcesses.class.getName() },
//				new Hashtable<String, String>());

		fCommandControl = getServicesTracker().getService(ICommandControlService.class);
        fContainerCommandCache = new CommandCache(getSession(), fCommandControl);
        fContainerCommandCache.setContextAvailable(fCommandControl.getContext(), true);
        getSession().addServiceEventListener(this, null);

		requestMonitor.done();
	}

	/**
	 * This method shuts down this service. It unregisters the service, stops
	 * receiving service events, and calls the superclass shutdown() method to
	 * finish the shutdown process.
	 * 
	 * @return void
	 */
	@Override
	public void shutdown(RequestMonitor requestMonitor) {
//		unregister();
        getSession().removeServiceEventListener(this);
		super.shutdown(requestMonitor);
	}
	
	/**
	 * @return The bundle context of the plug-in to which this service belongs.
	 */
	@Override
	protected BundleContext getBundleContext() {
		return MIPlugin.getBundleContext();
	}
	
   public IThreadDMContext createThreadContext(IProcessDMContext processDmc, String threadId) {
        return new MIThreadDMC(getSession().getId(), processDmc, threadId);
    }

    public IProcessDMContext createProcessContext(ICommandControlDMContext controlDmc, String pid) {
        return new MIProcessDMC(getSession().getId(), controlDmc, pid);
    }
    
    public IMIExecutionDMContext createExecutionContext(IContainerDMContext containerDmc, 
                                                        IThreadDMContext threadDmc, 
                                                        String threadId) {
    	return new MIExecutionDMC(getSession().getId(), containerDmc, threadDmc, threadId);
    }

    public IMIContainerDMContext createContainerContext(IProcessDMContext processDmc,
    												    String groupId) {
    	return new MIContainerDMC(getSession().getId(), processDmc, groupId);
    }

	/**
	 * This method obtains the model data for a given IThreadDMContext object
	 * which can represent a thread or a process.
	 * 
	 * @param dmc
	 *            The context for which we are requesting the data
	 * @param rm
	 *            The request monitor that will contain the requested data
	 */
	@SuppressWarnings("unchecked")
	public void getModelData(IDMContext dmc, DataRequestMonitor<?> rm) {
		if (dmc instanceof IThreadDMContext) {
			getExecutionData((IThreadDMContext) dmc, 
					(DataRequestMonitor<IThreadDMData>) rm);
		} else {
            rm.setStatus(new Status(IStatus.ERROR, MIPlugin.PLUGIN_ID, INVALID_HANDLE, "Invalid DMC type", null)); //$NON-NLS-1$
            rm.done();
		}
	}

	public void getExecutionData(IThreadDMContext dmc, final DataRequestMonitor<IThreadDMData> rm) {
		if (dmc instanceof MIProcessDMC) {
			rm.setData(new MIThreadDMData("", ((MIProcessDMC)dmc).getProcId())); //$NON-NLS-1$
			rm.done();
		} else if (dmc instanceof MIThreadDMC) {
			final MIThreadDMC threadDmc = (MIThreadDMC)dmc;
			
			IProcessDMContext procDmc = DMContexts.getAncestorOfType(dmc, IProcessDMContext.class);
			getDebuggingContext(procDmc,
					new DataRequestMonitor<IDMContext>(getExecutor(), rm) {
				        @Override
				        protected void handleSuccess() {
				        	if (getData() instanceof IMIContainerDMContext) {
				        		IMIContainerDMContext contDmc = (IMIContainerDMContext)getData();
				        		fContainerCommandCache.execute(new CLIInfoThreads(contDmc),
				        				new DataRequestMonitor<CLIInfoThreadsInfo>(getExecutor(), rm) {
				        		        	@Override
				        		        	protected void handleSuccess() {
				        		        		IThreadDMData threadData = null;
				        		        		for (CLIInfoThreadsInfo.ThreadInfo thread : getData().getThreadInfo()) {
				        		        			if (thread.getId().equals(threadDmc.getId())) {
				        		        				threadData = new MIThreadDMData(thread.getName(), thread.getOsId());     
				        		        				break;
				        		        			}
				        		        		}
				            	        		if (threadData != null) {
				                	        		rm.setData(threadData);        	        			
				            	        		} else {
				            	        			rm.setStatus(new Status(IStatus.ERROR, MIPlugin.PLUGIN_ID, INVALID_HANDLE, "Could not get thread info", null)); //$NON-NLS-1$        	        			
				            	        		}
				            	        		rm.done();
				        		        	}
				        		});
				        	} else {
				    			rm.setStatus(new Status(IStatus.ERROR, MIPlugin.PLUGIN_ID, INVALID_HANDLE, "Invalid DMC type", null)); //$NON-NLS-1$
				    			rm.done();
				        	}
				        }
			});
		}
	}
	
    public void getDebuggingContext(IThreadDMContext dmc, DataRequestMonitor<IDMContext> rm) {
    	if (dmc instanceof MIProcessDMC) {
    		MIProcessDMC procDmc = (MIProcessDMC)dmc;
    		rm.setData(createContainerContext(procDmc, procDmc.getProcId()));
    	} else if (dmc instanceof MIThreadDMC) {
    		MIThreadDMC threadDmc = (MIThreadDMC)dmc;
    		IMIProcessDMContext procDmc = DMContexts.getAncestorOfType(dmc, IMIProcessDMContext.class);
    		IMIContainerDMContext containerDmc = createContainerContext(procDmc, procDmc.getProcId()); 
    		rm.setData(createExecutionContext(containerDmc, threadDmc, threadDmc.getId()));
    	} else {
            rm.setStatus(new Status(IStatus.ERROR, MIPlugin.PLUGIN_ID, INTERNAL_ERROR, "Invalid thread context.", null)); //$NON-NLS-1$
    	}

    	rm.done();
    }
    
    public void isDebuggerAttachSupported(IDMContext dmc, DataRequestMonitor<Boolean> rm) {
    	rm.setData(false);
    	rm.done();
    }

    public void attachDebuggerToProcess(final IProcessDMContext procCtx, final DataRequestMonitor<IDMContext> rm) {
		if (procCtx instanceof IMIProcessDMContext) {

			ICommandControlDMContext controlDmc = DMContexts.getAncestorOfType(procCtx, ICommandControlDMContext.class);
			fCommandControl.queueCommand(
					new CLIAttach(controlDmc, ((IMIProcessDMContext)procCtx).getProcId()),
					new DataRequestMonitor<MIInfo>(getExecutor(), rm) {
						@Override
						protected void handleSuccess() {
							IMIContainerDMContext containerDmc = createContainerContext(procCtx, 
									                                             ((IMIProcessDMContext)procCtx).getProcId());
			                getSession().dispatchEvent(new ContainerStartedDMEvent(containerDmc), 
			                                           getProperties());
			                rm.setData(containerDmc);
							rm.done();
						}
					});

	    } else {
            rm.setStatus(new Status(IStatus.ERROR, MIPlugin.PLUGIN_ID, INTERNAL_ERROR, "Invalid process context.", null)); //$NON-NLS-1$
            rm.done();
	    }
	}
	
    public void canDetachDebuggerFromProcess(IDMContext dmc, DataRequestMonitor<Boolean> rm) {
    	rm.setData(false);
    	rm.done();
    }

    public void detachDebuggerFromProcess(final IDMContext dmc, final RequestMonitor rm) {
    	ICommandControlDMContext controlDmc = DMContexts.getAncestorOfType(dmc, ICommandControlDMContext.class);

    	if (controlDmc != null) {
    		// This service version cannot use -target-detach because it didn't exist
    		// in versions of GDB up to and including GDB 6.8
    		fCommandControl.queueCommand(
    				new CLIDetach(controlDmc),
    				new DataRequestMonitor<MIInfo>(getExecutor(), rm) {
    					@Override
    					protected void handleSuccess() {
    				    	IContainerDMContext containerDmc = DMContexts.getAncestorOfType(dmc, IContainerDMContext.class);
    				    	if (containerDmc != null) {
    				    		getSession().dispatchEvent(new ContainerExitedDMEvent(containerDmc), 
    				    				getProperties());
    				    	}
    						rm.done();
    					}
    				});
    	} else {
            rm.setStatus(new Status(IStatus.ERROR, MIPlugin.PLUGIN_ID, INTERNAL_ERROR, "Invalid context.", null)); //$NON-NLS-1$
            rm.done();
	    }
	}

	public void canTerminate(IThreadDMContext thread, DataRequestMonitor<Boolean> rm) {
		rm.setData(true);
		rm.done();
	}

	public void isDebugNewProcessSupported(IDMContext dmc, DataRequestMonitor<Boolean> rm) {
		rm.setData(false);
		rm.done();	
	}

	public void debugNewProcess(IDMContext dmc, String file, 
			                    Map<String, Object> attributes, DataRequestMonitor<IDMContext> rm) {
		rm.setStatus(new Status(IStatus.ERROR, MIPlugin.PLUGIN_ID,
				NOT_SUPPORTED, "Not supported", null)); //$NON-NLS-1$
		rm.done();
	}
    
	public void getProcessesBeingDebugged(IDMContext dmc, final DataRequestMonitor<IDMContext[]> rm) {
		final IMIContainerDMContext containerDmc = DMContexts.getAncestorOfType(dmc, IMIContainerDMContext.class);
		if (containerDmc != null) {
			fContainerCommandCache.execute(
					new MIThreadListIds(containerDmc),
					new DataRequestMonitor<MIThreadListIdsInfo>(getExecutor(), rm) {
						@Override
						protected void handleSuccess() {
							rm.setData(makeExecutionDMCs(containerDmc, getData()));
							rm.done();
						}
					});
		} else {
			// This service version only handles a single process to debug, therefore, we can simply
			// create the context describing this process ourselves.
			ICommandControlDMContext controlDmc = DMContexts.getAncestorOfType(dmc, ICommandControlDMContext.class);
			String groupId = MIProcesses.UNIQUE_GROUP_ID;
			IProcessDMContext procDmc = createProcessContext(controlDmc, groupId);
			IMIContainerDMContext newContainerDmc = createContainerContext(procDmc, groupId);
			rm.setData(new IContainerDMContext[] {newContainerDmc});
			rm.done();
		}
	}

	private IExecutionDMContext[] makeExecutionDMCs(IContainerDMContext containerDmc, MIThreadListIdsInfo info) {
		final IProcessDMContext procDmc = DMContexts.getAncestorOfType(containerDmc, IProcessDMContext.class);

		if (info.getStrThreadIds().length == 0) {
			// Main thread always exist even if it is not reported by GDB.
			// So create thread-id = 0 when no thread is reported.
			// This hack is necessary to prevent AbstractMIControl from issuing a thread-select
			// because it doesn't work if the application was not compiled with pthread.
			return new IMIExecutionDMContext[]{createExecutionContext(containerDmc, 
					                                                  createThreadContext(procDmc, FAKE_THREAD_ID),
					                                                  FAKE_THREAD_ID)};
		} else {
			IExecutionDMContext[] executionDmcs = new IMIExecutionDMContext[info.getStrThreadIds().length];
			for (int i = 0; i < info.getStrThreadIds().length; i++) {
				String threadId = info.getStrThreadIds()[i];
				executionDmcs[i] = createExecutionContext(containerDmc, 
						                                  createThreadContext(procDmc, threadId),
						                                  threadId);
			}
			return executionDmcs;
		}
	}
	
    public void getRunningProcesses(IDMContext dmc, final DataRequestMonitor<IProcessDMContext[]> rm) {
		rm.setStatus(new Status(IStatus.ERROR, MIPlugin.PLUGIN_ID,
				NOT_SUPPORTED, "Not supported", null)); //$NON-NLS-1$
		rm.done();
	}

	public void isRunNewProcessSupported(IDMContext dmc, DataRequestMonitor<Boolean> rm) {
		rm.setData(false);
		rm.done();			
	}
	public void runNewProcess(IDMContext dmc, String file, 
			                  Map<String, Object> attributes, DataRequestMonitor<IProcessDMContext> rm) {
		rm.setStatus(new Status(IStatus.ERROR, MIPlugin.PLUGIN_ID,
				NOT_SUPPORTED, "Not supported", null)); //$NON-NLS-1$
		rm.done();
	}

	public void terminate(IThreadDMContext thread, RequestMonitor rm) {
		rm.setStatus(new Status(IStatus.ERROR, MIPlugin.PLUGIN_ID,
				NOT_SUPPORTED, "Not supported", null)); //$NON-NLS-1$
		rm.done();
	}

    /**
     * @nooverride This method is not intended to be re-implemented or extended by clients.
     * @noreference This method is not intended to be referenced by clients.
     */
    @DsfServiceEventHandler
    public void eventDispatched(IResumedDMEvent e) {
    	if (e instanceof IContainerResumedDMEvent) {
    		// This will happen in all-stop mode
    		fContainerCommandCache.setContextAvailable(e.getDMContext(), false);
    	} else {
       		// This will happen in non-stop mode
    		// Keep target available for Container commands
       	}
    }


    /**
     * @nooverride This method is not intended to be re-implemented or extended by clients.
     * @noreference This method is not intended to be referenced by clients.
     */
    @DsfServiceEventHandler
    public void eventDispatched(ISuspendedDMEvent e) {
       	if (e instanceof IContainerSuspendedDMEvent) {
    		// This will happen in all-stop mode
       		fContainerCommandCache.setContextAvailable(e.getDMContext(), true);
       	} else {
       		// This will happen in non-stop mode
       	}
    }

    /**
     * @nooverride This method is not intended to be re-implemented or extended by clients.
     * @noreference This method is not intended to be referenced by clients.
     */
    @DsfServiceEventHandler
    public void eventDispatched(IStartedDMEvent e) {
        fContainerCommandCache.reset();
	}

    /**
     * @nooverride This method is not intended to be re-implemented or extended by clients.
     * @noreference This method is not intended to be referenced by clients.
     */
    @DsfServiceEventHandler
    public void eventDispatched(IExitedDMEvent e) {
        fContainerCommandCache.reset();
    }

	public void flushCache(IDMContext context) {
        fContainerCommandCache.reset(context);
	}
}

/*******************************************************************************
 * Copyright (c) 2008, 2010 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.IProcessInfo;
import org.eclipse.cdt.core.IProcessList;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Immutable;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.AbstractDMContext;
import org.eclipse.cdt.dsf.datamodel.AbstractDMEvent;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.ICachingService;
import org.eclipse.cdt.dsf.debug.service.IMemory.IMemoryDMContext;
import org.eclipse.cdt.dsf.debug.service.IProcesses;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerResumedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerSuspendedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExitedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IResumedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IStartedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.ISuspendedDMEvent;
import org.eclipse.cdt.dsf.debug.service.command.BufferedCommandControl;
import org.eclipse.cdt.dsf.debug.service.command.CommandCache;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.cdt.dsf.debug.service.command.IEventListener;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.gdb.service.command.IGDBControl;
import org.eclipse.cdt.dsf.mi.service.IMICommandControl;
import org.eclipse.cdt.dsf.mi.service.IMIContainerDMContext;
import org.eclipse.cdt.dsf.mi.service.IMIExecutionDMContext;
import org.eclipse.cdt.dsf.mi.service.IMIProcessDMContext;
import org.eclipse.cdt.dsf.mi.service.IMIProcesses;
import org.eclipse.cdt.dsf.mi.service.MIProcesses;
import org.eclipse.cdt.dsf.mi.service.command.CommandFactory;
import org.eclipse.cdt.dsf.mi.service.command.events.MIThreadGroupCreatedEvent;
import org.eclipse.cdt.dsf.mi.service.command.events.MIThreadGroupExitedEvent;
import org.eclipse.cdt.dsf.mi.service.command.output.MIConst;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIListThreadGroupsInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIListThreadGroupsInfo.IThreadGroupInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MINotifyAsyncOutput;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOOBRecord;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOutput;
import org.eclipse.cdt.dsf.mi.service.command.output.MIResult;
import org.eclipse.cdt.dsf.mi.service.command.output.MIThread;
import org.eclipse.cdt.dsf.mi.service.command.output.MIThreadInfoInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIValue;
import org.eclipse.cdt.dsf.service.AbstractDsfService;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.BundleContext;

/**
 * This class implements the IProcesses interface for GDB 7.0
 * which supports the new -list-thread-groups command.
 */
public class GDBProcesses_7_0 extends AbstractDsfService 
    implements IGDBProcesses, ICachingService, IEventListener {

	// Below is the context hierarchy that is implemented between the
	// MIProcesses service and the MIRunControl service for the MI 
	// implementation of DSF:
	//
	//                        MIControlDMContext (ICommandControlDMContext)
	//                                |
	//                          MIProcessDMC (IProcess)
	//                             /     \
    //                            /       \
	//                 MIContainerDMC     MIThreadDMC (IThread)
	//                  (IContainer)         /
	//                          \           /
	//                         MIExecutionDMC
	//                          (IExecution)
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

		/* Unused; reintroduce if needed
		public String getId(){
			return fThreadId;
		}
		*/

		@Override
		public String toString() { return baseToString() + ".thread[" + fThreadId + "]"; }  //$NON-NLS-1$ //$NON-NLS-2$

		@Override
		public boolean equals(Object obj) {
			return baseEquals(obj) && ((MIExecutionDMC)obj).fThreadId.equals(fThreadId);
		}

		@Override
		public int hashCode() { return baseHashCode() ^ fThreadId.hashCode(); }
	}

	/**
	 * Context representing a thread group of GDB/MI. 
	 */
    @Immutable
	private static class MIContainerDMC extends AbstractDMContext
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
			return baseEquals(obj) && 
			       (((MIContainerDMC)obj).fId == null ? fId == null : ((MIContainerDMC)obj).fId.equals(fId));
		}

		@Override
		public int hashCode() { return baseHashCode() ^ (fId == null ? 0 : fId.hashCode()); }
	}

	private class GDBContainerDMC extends MIContainerDMC 
	implements IMemoryDMContext 
	{
		public GDBContainerDMC(String sessionId, IProcessDMContext processDmc, String groupId) {
			super(sessionId, processDmc, groupId);
		}
	}
	
	/**
	 * Context representing a thread. 
	 * @since 4.0
	 */
    @Immutable
    protected static class MIThreadDMC extends AbstractDMContext
    implements IThreadDMContext
    {
    	/**
    	 * ID used by GDB to refer to threads.
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
			return baseEquals(obj) && 
			       (((MIThreadDMC)obj).fId == null ? fId == null : ((MIThreadDMC)obj).fId.equals(fId));
		}

		@Override
		public int hashCode() { return baseHashCode() ^ (fId == null ? 0 : fId.hashCode()); }
    }

    @Immutable
    private static class MIProcessDMC extends AbstractDMContext
    implements IMIProcessDMContext
    {
      	/**
    	 * ID given by the OS.
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
			return baseEquals(obj) && 
			       (((MIProcessDMC)obj).fId == null ? fId == null : ((MIProcessDMC)obj).fId.equals(fId));
		}

		@Override
		public int hashCode() { return baseHashCode() ^ (fId == null ? 0 : fId.hashCode()); }
    }
    
    /**
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
     * This class provides an implementation of both a process context and process data.
     * It is used to be able to return a list of processes including their data all at once.
     * @since 4.0
     */
    @Immutable
    protected static class MIProcessDMCAndData extends MIProcessDMC implements IGdbThreadDMData {
    	final String fName;
    	// Note that cores are only available from GDB 7.1.
    	final String[] fCores;
    	final String fOwner;

    	public MIProcessDMCAndData(String sessionId, ICommandControlDMContext controlDmc, 
    			                   String id, String name, String[] cores, String owner) {
    		super(sessionId, controlDmc, id);
    		fName = name;
    		fCores = cores;
    		fOwner = owner;
    	}

		public String getId() { return getProcId(); }
		public String getName() { return fName; }
		public boolean isDebuggerAttached() {
			return true;
		}

		public String[] getCores() { return fCores; }

		public String getOwner() { return fOwner; }

		@Override
    	public String toString() { return baseToString() + 
			".proc[" + getId() + "," + getName() + "," + getOwner() + "]"; }  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$//$NON-NLS-4$

		@Override
		public boolean equals(Object obj) {
			return super.equals(obj) &&
			       (((MIProcessDMCAndData)obj).fName == null ? fName == null : ((MIProcessDMCAndData)obj).fName.equals(fName)) &&
			       (((MIProcessDMCAndData)obj).fOwner == null ? fOwner == null : ((MIProcessDMCAndData)obj).fOwner.equals(fOwner));
		}

		@Override
		public int hashCode() { return super.hashCode() ^ 
			(fName == null ? 0 : fName.hashCode()) ^
			(fOwner == null ? 0 : fOwner.hashCode()) ; }
    }
    
    /**
     * Event indicating that an container (debugged process) has started.  This event
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
     * Event indicating that an container is no longer being debugged.  This event
     * implements the {@link IExitedMDEvent} from the IRunControl service. 
     */
    public static class ContainerExitedDMEvent extends AbstractDMEvent<IExecutionDMContext> 
        implements IExitedDMEvent
    {
        public ContainerExitedDMEvent(IContainerDMContext context) {
            super(context);
        }
    }        

    /**
     *  A map of thread id to thread group id.  We use this to find out to which threadGroup a thread belongs.
     */
    private Map<String, String> fThreadToGroupMap = new HashMap<String, String>();

    private IGDBControl fCommandControl;
    private IGDBBackend fBackend;
    private CommandFactory fCommandFactory;

    // A cache for commands about the threadGroups
	private CommandCache fContainerCommandCache;

	//A cache for commands about the threads
	private CommandCache fThreadCommandCache;
	
	// A temporary cache to avoid using -list-thread-groups --available more than once at the same time.
	// We cannot cache this command because it lists all available processes, which can
	// change at any time.  However, it is inefficient to send more than one of this command at
	// the same time.  This cache will help us avoid that.  The idea is that we cache the command,
	// but as soon as it returns, we clear the cache.  So the cache will only trigger for those 
	// overlapping situations.
	private CommandCache fListThreadGroupsAvailableCache;

    // A map of process id to process names.  A name is fetched whenever we start
	// debugging a process, and removed when we stop.
	// This allows us to make sure that if a pid is re-used, we will not use an
	// old name for it.  Bug 275497
	// This map also serves as a list of processes we are currently debugging.
	// This is important because we cannot always ask GDB for the list, since it may
	// be running at the time.  Bug 303503
    private Map<String, String> fDebuggedProcessesAndNames = new HashMap<String, String>();
	
    private static final String FAKE_THREAD_ID = "0"; //$NON-NLS-1$

    public GDBProcesses_7_0(DsfSession session) {
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
        
		fCommandControl = getServicesTracker().getService(IGDBControl.class);
    	fBackend = getServicesTracker().getService(IGDBBackend.class);
    	BufferedCommandControl bufferedCommandControl = new BufferedCommandControl(fCommandControl, getExecutor(), 2);

        fCommandFactory = getServicesTracker().getService(IMICommandControl.class).getCommandFactory();

		// These caches store the result of a command when received; also, these caches
		// are manipulated when receiving events.  Currently, events are received after
		// three scheduling of the executor, while command results after only one.  This
		// can cause problems because command results might be processed before an event
		// that actually arrived before the command result.
		// To solve this, we use a bufferedCommandControl that will delay the command
		// result by two scheduling of the executor.
		// See bug 280461
        fContainerCommandCache = new CommandCache(getSession(), bufferedCommandControl);
        fContainerCommandCache.setContextAvailable(fCommandControl.getContext(), true);
        fThreadCommandCache = new CommandCache(getSession(), bufferedCommandControl);
        fThreadCommandCache.setContextAvailable(fCommandControl.getContext(), true);
        
        // No need to use the bufferedCommandControl for the listThreadGroups cache
        // because it is not being affected by events.
        fListThreadGroupsAvailableCache = new CommandCache(getSession(), fCommandControl);
        fListThreadGroupsAvailableCache.setContextAvailable(fCommandControl.getContext(), true);

        getSession().addServiceEventListener(this, null);
        fCommandControl.addEventListener(this);

		// Register this service.
		register(new String[] { IProcesses.class.getName(),
				IMIProcesses.class.getName(),
				IGDBProcesses.class.getName(),
				GDBProcesses_7_0.class.getName() },
				new Hashtable<String, String>());
        
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
		unregister();
        getSession().removeServiceEventListener(this);
        fCommandControl.removeEventListener(this);
		super.shutdown(requestMonitor);
	}
	
	/**
	 * @return The bundle context of the plug-in to which this service belongs.
	 */
	@Override
	protected BundleContext getBundleContext() {
		return GdbPlugin.getBundleContext();
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
    	return new GDBContainerDMC(getSession().getId(), processDmc, groupId);
    }

    public IMIContainerDMContext createContainerContextFromThreadId(ICommandControlDMContext controlDmc, String threadId) {
    	String groupId = fThreadToGroupMap.get(threadId);
    	if (groupId == null) {
    		// this can happen if the threadId was 'all'
    		// In such a case, we choose the first process we find
    		// This works when we run a single process
    		// but will break for multi-process!!!
    		if (fThreadToGroupMap.isEmpty()) {
    			groupId = MIProcesses.UNIQUE_GROUP_ID;
    		} else {
    			Collection<String> values = fThreadToGroupMap.values();
    			for (String value : values) {
    				groupId = value;
    				break;
    			}
    		}
    	}
    	IProcessDMContext processDmc = createProcessContext(controlDmc, groupId);
    	return createContainerContext(processDmc, groupId);
    }

    public IMIExecutionDMContext[] getExecutionContexts(IMIContainerDMContext containerDmc) {
    	String groupId = containerDmc.getGroupId();
    	List<IMIExecutionDMContext> execDmcList = new ArrayList<IMIExecutionDMContext>(); 
    	Iterator<Map.Entry<String, String>> iterator = fThreadToGroupMap.entrySet().iterator();
    	while (iterator.hasNext()){
    		Map.Entry<String, String> entry = iterator.next();
    		if (entry.getValue().equals(groupId)) {
    			String threadId = entry.getKey();
    			IProcessDMContext procDmc = DMContexts.getAncestorOfType(containerDmc, IProcessDMContext.class);
    			IMIExecutionDMContext execDmc = createExecutionContext(containerDmc, 
    																   createThreadContext(procDmc, threadId),
    																   threadId);
    			execDmcList.add(execDmc);
    		}
    	}
    	return execDmcList.toArray(new IMIExecutionDMContext[0]);
    }

	public void getExecutionData(IThreadDMContext dmc, final DataRequestMonitor<IThreadDMData> rm) {
		if (dmc instanceof IMIProcessDMContext) {
			String id = ((IMIProcessDMContext)dmc).getProcId();
			String name = null;
			if (fBackend.getSessionType() == SessionType.CORE || "42000".equals(id)) { //$NON-NLS-1$
				// For the Core session, the process is no longer running.
				// Therefore, we cannot get its name with the -list-thread-groups command.
				// As for id 42000, it is a special id used by GDB to indicate the real proc
				// id is not known.  This will happen in a Remote session, when we use
				// -target-select remote instead of -target-select extended-remote.
				//
				// So, we take the name from the binary we are using.
				name = fBackend.getProgramPath().lastSegment();
				// Also, the pid we get from GDB is 1 or 42000, which is not correct.
				// I haven't found a good way to get the pid yet, so let's not show it.
				id = null;
			} else {
				name = fDebuggedProcessesAndNames.get(id);
				if (name == null) {
					// We don't have the name in our map.  Should not happen.
					name = "Unknown name"; //$NON-NLS-1$
					assert false : "Don't have entry for process ID: " + id; //$NON-NLS-1$
				} else if (name.length() == 0) {
					// Probably will not happen, but just in case...use the
					// binary file name (absolute path)
					IGDBBackend backend = getServicesTracker().getService(IGDBBackend.class);
					name = backend.getProgramPath().toOSString();
					fDebuggedProcessesAndNames.put(id, name);
				}
			}
			rm.setData(new MIThreadDMData(name, id));
			rm.done();	
		} else if (dmc instanceof MIThreadDMC) {
			final MIThreadDMC threadDmc = (MIThreadDMC)dmc;
			
			ICommandControlDMContext controlDmc = DMContexts.getAncestorOfType(dmc, ICommandControlDMContext.class);
	        fThreadCommandCache.execute(fCommandFactory.createMIThreadInfo(controlDmc, threadDmc.getId()),
	        		new DataRequestMonitor<MIThreadInfoInfo>(getExecutor(), rm) {
        	        	@Override
        	        	protected void handleSuccess() {
        	        		IThreadDMData threadData = null;
        	        		if (getData().getThreadList().length != 0) {
        	        			MIThread thread = getData().getThreadList()[0];
        	        			if (thread.getThreadId().equals(threadDmc.getId())) {
        	        				threadData = new MIThreadDMData("", thread.getOsId());      //$NON-NLS-1$
        	        			}
        	        		}
        	        		
        	        		if (threadData != null) {
            	        		rm.setData(threadData);        	        			
        	        		} else {
        	        			rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_HANDLE, "Could not get thread info", null)); //$NON-NLS-1$        	        			
        	        		}
        	        		rm.done();
        	        	}
	        });
		} else {
			rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_HANDLE, "Invalid DMC type", null)); //$NON-NLS-1$
			rm.done();
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
            rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INTERNAL_ERROR, "Invalid thread context.", null)); //$NON-NLS-1$
    	}

    	rm.done();
    }
    
    private boolean doIsDebuggerAttachSupported() {
    	IGDBBackend backend = getServicesTracker().getService(IGDBBackend.class);
    	if (backend != null) {
    		return backend.getIsAttachSession();
    	}
    	return false;
    }
    
    public void isDebuggerAttachSupported(IDMContext dmc, DataRequestMonitor<Boolean> rm) {
    	rm.setData(doIsDebuggerAttachSupported());
    	rm.done();
    }

    public void attachDebuggerToProcess(final IProcessDMContext procCtx, final DataRequestMonitor<IDMContext> rm) {
		if (procCtx instanceof IMIProcessDMContext) {
	    	if (!doIsDebuggerAttachSupported()) {
	            rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INTERNAL_ERROR, "Attach not supported.", null)); //$NON-NLS-1$
	            rm.done();    		
	    		return;
	    	}
	    	
	    	ICommandControlDMContext controlDmc = DMContexts.getAncestorOfType(procCtx, ICommandControlDMContext.class);
			fCommandControl.queueCommand(
					fCommandFactory.createMITargetAttach(controlDmc, ((IMIProcessDMContext)procCtx).getProcId()),
					new DataRequestMonitor<MIInfo>(getExecutor(), rm) {
						@Override
						protected void handleSuccess() {
							IMIContainerDMContext containerDmc = createContainerContext(procCtx,
									                                                    ((IMIProcessDMContext)procCtx).getProcId());
			                rm.setData(containerDmc);
							rm.done();
						}
					});

	    } else {
            rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INTERNAL_ERROR, "Invalid process context.", null)); //$NON-NLS-1$
            rm.done();
	    }
	}

    private boolean doCanDetachDebuggerFromProcess() {
    	IGDBBackend backend = getServicesTracker().getService(IGDBBackend.class);
    	if (backend != null) {
    		return backend.getIsAttachSession() && fCommandControl.isConnected();
    	}
    	return false;
    }
    
    public void canDetachDebuggerFromProcess(IDMContext dmc, DataRequestMonitor<Boolean> rm) {
    	rm.setData(doCanDetachDebuggerFromProcess());
    	rm.done();
    }

    public void detachDebuggerFromProcess(final IDMContext dmc, final RequestMonitor rm) {
    	
    	ICommandControlDMContext controlDmc = DMContexts.getAncestorOfType(dmc, ICommandControlDMContext.class);
    	IMIProcessDMContext procDmc = DMContexts.getAncestorOfType(dmc, IMIProcessDMContext.class);

    	if (controlDmc != null && procDmc != null) {
        	if (!doCanDetachDebuggerFromProcess()) {
                rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INTERNAL_ERROR, "Detach not supported.", null)); //$NON-NLS-1$
                rm.done();
                return;
        	}

        	fCommandControl.queueCommand(
        			fCommandFactory.createMITargetDetach(controlDmc, procDmc.getProcId()),
    				new DataRequestMonitor<MIInfo>(getExecutor(), rm));
    	} else {
            rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INTERNAL_ERROR, "Invalid context.", null)); //$NON-NLS-1$
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
		rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID,
				NOT_SUPPORTED, "Not supported", null)); //$NON-NLS-1$
		rm.done();
	}
    
	public void getProcessesBeingDebugged(final IDMContext dmc, final DataRequestMonitor<IDMContext[]> rm) {
		final ICommandControlDMContext controlDmc = DMContexts.getAncestorOfType(dmc, ICommandControlDMContext.class);
		final IMIContainerDMContext containerDmc = DMContexts.getAncestorOfType(dmc, IMIContainerDMContext.class);
		if (containerDmc != null) {
			fThreadCommandCache.execute(
					fCommandFactory.createMIListThreadGroups(controlDmc, containerDmc.getGroupId()),
					new DataRequestMonitor<MIListThreadGroupsInfo>(getExecutor(), rm) {
						@Override
						protected void handleSuccess() {
							rm.setData(makeExecutionDMCs(containerDmc, getData().getThreadInfo().getThreadList()));
							rm.done();
						}
					});
		} else {
			fContainerCommandCache.execute(
					fCommandFactory.createMIListThreadGroups(controlDmc),
					new DataRequestMonitor<MIListThreadGroupsInfo>(getExecutor(), rm) {
						@Override
						protected void handleSuccess() {
							rm.setData(makeContainerDMCs(controlDmc, getData().getGroupList()));
							rm.done();
						}
						@Override
						protected void handleFailure() {
							// If the target is not available, generate the list ourselves
							IMIContainerDMContext[] containerDmcs = new IMIContainerDMContext[fDebuggedProcessesAndNames.size()];
							int i = 0;
							for (String groupId : fDebuggedProcessesAndNames.keySet()) {
								IProcessDMContext processDmc = createProcessContext(controlDmc, groupId);
								containerDmcs[i++] = createContainerContext(processDmc, groupId);
							}
							rm.setData(containerDmcs);
							rm.done();
						}
					});
		}
	}

	private IExecutionDMContext[] makeExecutionDMCs(IContainerDMContext containerDmc, MIThread[] threadInfos) {
		final IProcessDMContext procDmc = DMContexts.getAncestorOfType(containerDmc, IProcessDMContext.class);

		if (threadInfos.length == 0) {
			// Main thread always exist even if it is not reported by GDB.
			// So create thread-id = 0 when no thread is reported.
			// This hack is necessary to prevent AbstractMIControl from issuing a thread-select
			// because it doesn't work if the application was not compiled with pthread.
			return new IMIExecutionDMContext[]{createExecutionContext(containerDmc, 
					                                                  createThreadContext(procDmc, FAKE_THREAD_ID),
					                                                  FAKE_THREAD_ID)};
		} else {
			IExecutionDMContext[] executionDmcs = new IMIExecutionDMContext[threadInfos.length];
			for (int i = 0; i < threadInfos.length; i++) {
				String threadId = threadInfos[i].getThreadId();
				executionDmcs[i] = createExecutionContext(containerDmc, 
						                                  createThreadContext(procDmc, threadId),
						                                  threadId);
			}
			return executionDmcs;
		}
	}

	private IMIContainerDMContext[] makeContainerDMCs(ICommandControlDMContext controlDmc, IThreadGroupInfo[] groups) {
		// This is a workaround for post-mortem tracing because the early GDB release
		// does not report a process when we do -list-thread-group
		// GDB 7.2 will properly report the process so this
		// code can be removed when GDB 7.2 is released
		// START OF WORKAROUND
		if (groups.length == 0 && fBackend.getSessionType() == SessionType.CORE) {
			String groupId = MIProcesses.UNIQUE_GROUP_ID;
			IProcessDMContext processDmc = createProcessContext(controlDmc, groupId);
			return new IMIContainerDMContext[] {createContainerContext(processDmc, groupId)};
		}
		// END OF WORKAROUND to be removed when GDB 7.2 is available
		
		// With GDB 7.1, we can receive a bogus process when we are not debugging anything
        // -list-thread-groups
        // ^done,groups=[{id="0",type="process",pid="0"}]
		// As for GDB 7.2, the pid field is missing altogether in this case
		// -list-thread-groups
		// ^done,groups=[{id="i1",type="process"}]
		// Just ignore that entry
		List<IMIContainerDMContext> containerDmcs = new ArrayList<IMIContainerDMContext>(groups.length);
		for (IThreadGroupInfo group : groups) {
			if (group.getPid() == null || 
					group.getPid().equals("") || group.getPid().equals("0")) { //$NON-NLS-1$ //$NON-NLS-2$
				continue;
			}
			String groupId = group.getGroupId();
			IProcessDMContext procDmc = createProcessContext(controlDmc, groupId); 
			containerDmcs.add(createContainerContext(procDmc, groupId));
		}
		return containerDmcs.toArray(new IMIContainerDMContext[containerDmcs.size()]);
	}

	public void getRunningProcesses(final IDMContext dmc, final DataRequestMonitor<IProcessDMContext[]> rm) {
		final ICommandControlDMContext controlDmc = DMContexts.getAncestorOfType(dmc, ICommandControlDMContext.class);
		if (controlDmc != null) {
			fListThreadGroupsAvailableCache.execute(
				fCommandFactory.createMIListThreadGroups(controlDmc, true),
				new DataRequestMonitor<MIListThreadGroupsInfo>(getExecutor(), rm) {
					@Override
					protected void handleCompleted() {
						// We cannot actually cache this command since the process
						// list may change.  But this cache allows to avoid overlapping
						// sending of this command.
						fListThreadGroupsAvailableCache.reset();
						
						if (isSuccess()) {
							rm.setData(makeProcessDMCAndData(controlDmc, getData().getGroupList()));
						} else {
							// Looks like this gdb doesn't truly support
							// "-list-thread-groups --available". If we're
							// debugging locally, resort to getting the
							// list natively (as we do with gdb 6.8). If
							// we're debugging remotely, the user is out
							// of luck
							IGDBBackend backend = getServicesTracker().getService(IGDBBackend.class);
							if (backend.getSessionType() == SessionType.LOCAL) {
								IProcessList list = null;
								try {
									list = CCorePlugin.getDefault().getProcessList();
								} catch (CoreException e) {
								}
	
								if (list == null) {
									rm.setData(new IProcessDMContext[0]);
								} else {
									IProcessInfo[] procInfos = list.getProcessList();
									rm.setData(makeProcessDMCAndData(controlDmc, procInfos));
								}
							}
							else {
								rm.setData(new IProcessDMContext[0]);
							}
						}
						rm.done();
					}
				});
		} else {
			rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INTERNAL_ERROR, "Invalid context.", null)); //$NON-NLS-1$
			rm.done();
		}

	}
	
	/**
	 * Create the joint process DMC and data based on IProcessInfo, which is a local listing.
	 * @since 4.0
	 */
	protected MIProcessDMCAndData[] makeProcessDMCAndData(ICommandControlDMContext controlDmc, IProcessInfo[] processes) {
		MIProcessDMCAndData[] procDmcs = new MIProcessDMCAndData[processes.length];
		for (int i=0; i<procDmcs.length; i++) {
			procDmcs[i] = new MIProcessDMCAndData(controlDmc.getSessionId(),
					                              controlDmc, 
					                              Integer.toString(processes[i].getPid()),
					                              processes[i].getName(),
					                              null, null);
		}
		return procDmcs;
	}

	/**
	 * Create the joint process DMC and data based on IThreadGroupInfo, which is obtained from GDB.
	 * @since 4.0
	 */
	protected MIProcessDMCAndData[] makeProcessDMCAndData(ICommandControlDMContext controlDmc, IThreadGroupInfo[] processes) {
		MIProcessDMCAndData[] procDmcs = new MIProcessDMCAndData[processes.length];
		int i=0;
		for (IThreadGroupInfo process : processes) {
			procDmcs[i++] = new MIProcessDMCAndData(controlDmc.getSessionId(),
					                                controlDmc, 
					                                process.getGroupId(),
					                                process.getName(),
					                                process.getCores(),
					                                process.getUser());
		}
		return procDmcs;
	}

	public void isRunNewProcessSupported(IDMContext dmc, DataRequestMonitor<Boolean> rm) {
		rm.setData(false);
		rm.done();			
	}
	
	public void runNewProcess(IDMContext dmc, String file, 
			                  Map<String, Object> attributes, DataRequestMonitor<IProcessDMContext> rm) {
		rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID,
				NOT_SUPPORTED, "Not supported", null)); //$NON-NLS-1$
		rm.done();
	}

	public void terminate(IThreadDMContext thread, RequestMonitor rm) {
		fCommandControl.terminate(rm);
	}
	
    @DsfServiceEventHandler
    public void eventDispatched(final MIThreadGroupCreatedEvent e) {
    	IProcessDMContext procDmc = e.getDMContext();
        IMIContainerDMContext containerDmc = e.getGroupId() != null ? createContainerContext(procDmc, e.getGroupId()) : null;
        getSession().dispatchEvent(new ContainerStartedDMEvent(containerDmc), getProperties());
    }

    @DsfServiceEventHandler
    public void eventDispatched(final MIThreadGroupExitedEvent e) {
    	IProcessDMContext procDmc = e.getDMContext();
        IMIContainerDMContext containerDmc = e.getGroupId() != null ? createContainerContext(procDmc, e.getGroupId()) : null;
		getSession().dispatchEvent(new ContainerExitedDMEvent(containerDmc), getProperties());
    }
    
    @DsfServiceEventHandler
    public void eventDispatched(IResumedDMEvent e) {
    	if (e instanceof IContainerResumedDMEvent) {
    		// This will happen in all-stop mode
    		fContainerCommandCache.setContextAvailable(e.getDMContext(), false);
    		fThreadCommandCache.setContextAvailable(e.getDMContext(), false);
    	} else {
       		// This will happen in non-stop mode
    		// Keep target available for Container commands
       	}
    }


    @DsfServiceEventHandler
    public void eventDispatched(ISuspendedDMEvent e) {
       	if (e instanceof IContainerSuspendedDMEvent) {
    		// This will happen in all-stop mode
       		fContainerCommandCache.setContextAvailable(fCommandControl.getContext(), true);
       		fThreadCommandCache.setContextAvailable(fCommandControl.getContext(), true);
       	} else {
       		// This will happen in non-stop mode
       	}

       	// If user is debugging a gdb target that doesn't send thread
		// creation events, make sure we don't use cached thread
		// information. Reset the cache after every suspend. See bugzilla
		// 280631
   		try {
			if (fBackend.getUpdateThreadListOnSuspend()) {
				fThreadCommandCache.reset(e.getDMContext());
			}
		} catch (CoreException exc) {}
    }
    
    // Event handler when a thread or threadGroup starts
    @DsfServiceEventHandler
    public void eventDispatched(IStartedDMEvent e) {
    	if (e instanceof ContainerStartedDMEvent) {
    		fContainerCommandCache.reset();
    	} else {
    		fThreadCommandCache.reset();
    	}
	}

    // Event handler when a thread or a threadGroup exits
    @DsfServiceEventHandler
    public void eventDispatched(IExitedDMEvent e) {
    	if (e instanceof ContainerExitedDMEvent) {
    		fContainerCommandCache.reset();
    	} else {
    		fThreadCommandCache.reset();
    	}
    }

	public void flushCache(IDMContext context) {
		fContainerCommandCache.reset(context);
		fThreadCommandCache.reset(context);
	}

	/*
	 * Catch =thread-created/exited and =thread-group-exited events to update our
	 * groupId to threadId map. 
	 */
	public void eventReceived(Object output) {
    	for (MIOOBRecord oobr : ((MIOutput)output).getMIOOBRecords()) {
			if (oobr instanceof MINotifyAsyncOutput) {
    			MINotifyAsyncOutput exec = (MINotifyAsyncOutput) oobr;
    			String miEvent = exec.getAsyncClass();
    			if ("thread-created".equals(miEvent) || "thread-exited".equals(miEvent)) { //$NON-NLS-1$ //$NON-NLS-2$
    				String threadId = null;
    				String groupId = null;

    				MIResult[] results = exec.getMIResults();
    				for (int i = 0; i < results.length; i++) {
    					String var = results[i].getVariable();
    					MIValue val = results[i].getMIValue();
    					if (var.equals("group-id")) { //$NON-NLS-1$
    						if (val instanceof MIConst) {
    							groupId = ((MIConst) val).getString();
    						}
    					} else if (var.equals("id")) { //$NON-NLS-1$
    		    			if (val instanceof MIConst) {
    							threadId = ((MIConst) val).getString();
    		    			}
    		    		}
    				}

    		    	if ("thread-created".equals(miEvent)) { //$NON-NLS-1$
    		    		// Update the thread to groupId map with the new groupId
    		    		fThreadToGroupMap.put(threadId, groupId);
    		    	} else {
    		    		fThreadToGroupMap.remove(threadId);
    		    	}
    			} else if ("thread-group-created".equals(miEvent) || "thread-group-exited".equals(miEvent)) { //$NON-NLS-1$ //$NON-NLS-2$
    				
    				String groupId = null;

    				MIResult[] results = exec.getMIResults();
    				for (int i = 0; i < results.length; i++) {
    					String var = results[i].getVariable();
    					MIValue val = results[i].getMIValue();
    					if (var.equals("id")) { //$NON-NLS-1$
    						if (val instanceof MIConst) {
    							groupId = ((MIConst) val).getString().trim();
    						}
    					}
    				}

    				if (groupId != null) {
    					if ("thread-group-created".equals(miEvent)) { //$NON-NLS-1$
    						fDebuggedProcessesAndNames.put(groupId, ""); //$NON-NLS-1$
    					
							// GDB is debugging a new process. Let's fetch its
							// name and remember it. In order to get the name,
							// we have to request all running processes, not
							// just the ones being debugged. We got a lot more 
    						// information when we request all processes.
        					final String finalGroupId = groupId;
    						fListThreadGroupsAvailableCache.execute(
    								fCommandFactory.createMIListThreadGroups(fCommandControl.getContext(), true),
    								new DataRequestMonitor<MIListThreadGroupsInfo>(getExecutor(), null) {
    									@Override
    									protected void handleCompleted() {
    										// We cannot actually cache this command since the process
    										// list may change.  But this cache allows to avoid overlapping
    										// sending of this command.
    										fListThreadGroupsAvailableCache.reset();

    										if (isSuccess()) {
    											for (IThreadGroupInfo groupInfo : getData().getGroupList()) {
    												if (groupInfo.getPid().equals(finalGroupId)) {
    													fDebuggedProcessesAndNames.put(finalGroupId, groupInfo.getName());
    												}
    											}
    										}
    										else {
    											// Looks like this gdb doesn't truly support
    											// "-list-thread-groups --available". Get the
    											// process list natively if we're debugging locally
    											IGDBBackend backend = getServicesTracker().getService(IGDBBackend.class);
    											if (backend.getSessionType() == SessionType.LOCAL) {
	    											IProcessList list = null;
	    											try {
	    												list = CCorePlugin.getDefault().getProcessList();
	        											int groupId_int = Integer.parseInt(finalGroupId);
	        											for (IProcessInfo procInfo : list.getProcessList()) {
	        												if (procInfo.getPid() == groupId_int) {
	        													fDebuggedProcessesAndNames.put(finalGroupId, procInfo.getName());
	        												}
	        											}
	    											} catch (CoreException e) {
	    											}
    											}
    										}
    									}
    								});
    					} else if ("thread-group-exited".equals(miEvent)) { //$NON-NLS-1$
    						// GDB is no longer debugging this process.  Remove it from our list.
    						fDebuggedProcessesAndNames.remove(groupId);
    						
    						// Remove any entries for that group from our thread to group map
    						// When detaching from a group, we won't have received any thread-exited event
    						// but we don't want to keep those entries.
    						if (fThreadToGroupMap.containsValue(groupId)) {
    							Iterator<Map.Entry<String, String>> iterator = fThreadToGroupMap.entrySet().iterator();
    							while (iterator.hasNext()){
    								if (iterator.next().getValue().equals(groupId)) {
    									iterator.remove();
    								}
    							}
    						}
    					}
    				}
    			}
    		}
    	}	
	}
}

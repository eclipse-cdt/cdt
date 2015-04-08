/*******************************************************************************
 * Copyright (c) 2012-2013 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Marc Khouzam (Ericsson) - initial API and implementation
 *     Marc Khouzam (Ericsson) - Updated to use /proc/cpuinfo for remote targets (Bug 374024)
 *     Marc Dumais (Ericsson) - Add CPU/core load information to the multicore visualizer (Bug 396268)
 *     Marc Dumais (Ericsson) - Bug 434889
 *     Teodor Madan (Freescale) - Activate multicore visualizer on non-linux hosts for remote case
 *     Marc Dumais (Ericsson) - Bug 464184
 *     
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.concurrent.ImmediateDataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateExecutor;
import org.eclipse.cdt.dsf.concurrent.Immutable;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.AbstractDMContext;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.DataModelInitializedEvent;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.datamodel.IDMData;
import org.eclipse.cdt.dsf.debug.service.ICachingService;
import org.eclipse.cdt.dsf.debug.service.command.CommandCache;
import org.eclipse.cdt.dsf.debug.service.command.ICommand;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControl;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.cdt.dsf.debug.service.command.ICommandListener;
import org.eclipse.cdt.dsf.debug.service.command.ICommandResult;
import org.eclipse.cdt.dsf.debug.service.command.ICommandToken;
import org.eclipse.cdt.dsf.debug.service.command.IEventListener;
import org.eclipse.cdt.dsf.gdb.internal.CoreInfo;
import org.eclipse.cdt.dsf.gdb.internal.CoreList;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.gdb.internal.ProcStatCoreLoads;
import org.eclipse.cdt.dsf.gdb.internal.ProcStatParser;
import org.eclipse.cdt.dsf.gdb.internal.service.command.commands.MIMetaGetCPUInfo;
import org.eclipse.cdt.dsf.gdb.internal.service.command.output.MIMetaGetCPUInfoInfo;
import org.eclipse.cdt.dsf.gdb.service.command.IGDBControl;
import org.eclipse.cdt.dsf.mi.service.IMICommandControl;
import org.eclipse.cdt.dsf.mi.service.command.CommandFactory;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIListThreadGroupsInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIListThreadGroupsInfo.IThreadGroupInfo;
import org.eclipse.cdt.dsf.service.AbstractDsfService;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.internal.core.ICoreInfo;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.BundleContext;

/**
 * This class implements the {@link IGDBHardwareAndOS} interface which gives access
 * to hardware information about the target.
 * 
 * @since 4.1
 */
public class GDBHardwareAndOS extends AbstractDsfService implements IGDBHardwareAndOS2, ICachingService {

	static String sTempFolder;
	static {
		try {
			sTempFolder = Files.createTempDirectory(GdbPlugin.PLUGIN_ID).toString() + '/';
		} catch (IOException | IllegalArgumentException | UnsupportedOperationException  e ) {
			sTempFolder = System.getProperty("java.io.tmpdir"); //$NON-NLS-1$
		}
	}

	@Immutable
	protected static class GDBCPUDMC extends AbstractDMContext 
	implements ICPUDMContext
	{
		/**
		 * String ID that is used to identify the thread in the GDB/MI protocol.
		 */
		private final String fId;

		/**
		 */
        protected GDBCPUDMC(String sessionId, IHardwareTargetDMContext targetDmc, String id) {
            super(sessionId, targetDmc == null ? new IDMContext[0] : new IDMContext[] { targetDmc });
            fId = id;
        }

		@Override
		public String getId(){
			return fId;
		}

		@Override
		public String toString() { return baseToString() + ".CPU[" + fId + "]"; }  //$NON-NLS-1$ //$NON-NLS-2$

		@Override
		public boolean equals(Object obj) {
			return baseEquals(obj) && ((GDBCPUDMC)obj).fId.equals(fId);
		}

		@Override
		public int hashCode() { return baseHashCode() ^ fId.hashCode(); }
	}

    @Immutable
	protected static class GDBCoreDMC extends AbstractDMContext
	implements ICoreDMContext
	{
		private final String fId;

		public GDBCoreDMC(String sessionId, ICPUDMContext CPUDmc, String id) {
			super(sessionId, CPUDmc == null ? new IDMContext[0] : new IDMContext[] { CPUDmc });
			fId = id;
		}

		@Override
		public String getId(){ return fId; }

		@Override
		public String toString() { return baseToString() + ".core[" + fId + "]"; }  //$NON-NLS-1$ //$NON-NLS-2$

		@Override
		public boolean equals(Object obj) {
			return baseEquals(obj) && 
			       (((GDBCoreDMC)obj).fId == null ? fId == null : ((GDBCoreDMC)obj).fId.equals(fId));
		}

		@Override
		public int hashCode() { return baseHashCode() ^ (fId == null ? 0 : fId.hashCode()); }
	}
	
    @Immutable
    protected static class GDBCPUDMData implements ICPUDMData {
    	final int fNumCores;
    	
    	public GDBCPUDMData(int num) {
    		fNumCores = num;
    	}
    	
		@Override
		public int getNumCores() { return fNumCores; }
    }
    
    @Immutable
    protected static class GDBCoreDMData implements ICoreDMData {
    	final String fPhysicalId;
    	
    	public GDBCoreDMData(String id) {
    		fPhysicalId = id;
    	}
    	
		@Override
		public String getPhysicalId() { return fPhysicalId; }
    }


    /**
	 * @since 4.2
	 */
    @Immutable
    protected class GDBLoadInfo implements ILoadInfo {
    	private String fLoad;
    	private Map<String,String> fDetailedLoad;

    	public GDBLoadInfo(String load, Map<String,String> detailedLoad) {
    		fLoad = load;
   			fDetailedLoad = detailedLoad;
    	}
    	public GDBLoadInfo(String load) {
    		this(load, null);
    	}
		@Override
		public String getLoad() {
			return fLoad;
		}
		@Override
		public Map<String,String> getDetailedLoad() {
			return fDetailedLoad;
		}
    }

    // to save queued load info requests for later processing
    private Map<IDMContext, DataRequestMonitor<ILoadInfo>> fLoadInfoRequestCache;
    
    private IGDBControl fCommandControl;
    private IGDBBackend fBackend;
    private CommandFactory fCommandFactory;

    // A command cache to cache the data gotten from /proc/cpuinfo
    // Because we obtain the data differently for a local target
    // than a remote target, we can't buffer an actual MI command,
    // so instead, we use a MetaMICommand to "fetch the cpu info"
    // Since the CPU info does not change, this cache does not need
    // to be cleared.
	private CommandCache fFetchCPUInfoCache;

	// Track if the debug session has been fully initialized.
	// Until then, we may not be connected to the remote target
	// yet, and not be able to properly fetch the information we need.
    // Bug 374293
	private boolean fSessionInitializationComplete;

	// used to keep track when we last computed the load
	private long fLastCpuLoadRefresh = 0;
	// to keep track if we are already seeking to get the load
	private boolean fLoadRequestOngoing = false;
	// Length of the measured sample in ms
	private final static int LOAD_SAMPLE_DELAY = 250;
	// To avoid bombarding the remote GDB server, we cache the measured load
	// and serve it again if requested within a short period of time.
	private ProcStatCoreLoads fCachedLoads = null;
	// lifetime of the load cache, in ms
	private final static int LOAD_CACHE_LIFETIME = 500; 
	
	
    public GDBHardwareAndOS(DsfSession session) {
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
    	super.initialize(new RequestMonitor(ImmediateExecutor.getInstance(), requestMonitor) {
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
		fSessionInitializationComplete = false;

		fCommandControl = getServicesTracker().getService(IGDBControl.class);
		fCommandFactory = getServicesTracker().getService(IMICommandControl.class).getCommandFactory();
		
		fBackend = getServicesTracker().getService(IGDBBackend.class);		

		// The cache does not go directly to the commandControl service.
		// Instead is goes through a CPUInfoManager which will decide how to
		// handle getting the required cpu info
		fFetchCPUInfoCache = new CommandCache(getSession(), new CPUInfoManager());
        fFetchCPUInfoCache.setContextAvailable(fCommandControl.getContext(), true);
        fLoadInfoRequestCache = new HashMap<IDMContext, DataRequestMonitor<ILoadInfo>>();

        getSession().addServiceEventListener(this, null);

        // Register this service.
		register(new String[] { IGDBHardwareAndOS.class.getName(),
								IGDBHardwareAndOS2.class.getName(),
				                GDBHardwareAndOS.class.getName() },
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
        getSession().removeServiceEventListener(this);
        fFetchCPUInfoCache.reset();
        fLoadInfoRequestCache.clear();
		unregister();
		super.shutdown(requestMonitor);
	}
	
	/**
	 * @return The bundle context of the plug-in to which this service belongs.
	 */
	@Override
	protected BundleContext getBundleContext() {
		return GdbPlugin.getBundleContext();
	}

	protected boolean getSessionInitializationComplete() {
		return fSessionInitializationComplete;
	}

	@Override
	public void getCPUs(final IHardwareTargetDMContext dmc, final DataRequestMonitor<ICPUDMContext[]> rm) {
		if (!fSessionInitializationComplete) {
			// We are not ready to answer yet
			rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_STATE, "Debug session not initialized yet", null)); //$NON-NLS-1$
			return;
		}

		if (supportsProcPseudoFS()) {
			fFetchCPUInfoCache.execute(
				new MIMetaGetCPUInfo(fCommandControl.getContext()),
				new ImmediateDataRequestMonitor<MIMetaGetCPUInfoInfo>() {
					@Override
					protected void handleSuccess() {
						rm.done(parseCoresInfoForCPUs(dmc, getData().getInfo()));
					}
				});
		} else {
			// No way to know the CPUs for Windows session.
			rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, NOT_SUPPORTED, "Operation not supported", null)); //$NON-NLS-1$
		}
	}

	/**
	 * @return true if supports fetching OS info from /proc pseudo-filesystem
	 */
	private boolean supportsProcPseudoFS() {
		if (Platform.getOS().equals(Platform.OS_LINUX))
			return true;

		// for non-linux platform, support only remote (linux? ) targets 
		if (SessionType.REMOTE == fBackend.getSessionType()) {
			return true;
		}

		return false;
	}

	@Override
	public void getCores(IDMContext dmc, final DataRequestMonitor<ICoreDMContext[]> rm) {
		if (!fSessionInitializationComplete) {
			// We are not ready to answer yet
			rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_STATE, "Debug session not initialized yet", null)); //$NON-NLS-1$
			return;
		}

		if (dmc instanceof ICPUDMContext) {
			// Get the cores under this particular CPU
			final ICPUDMContext cpuDmc = (ICPUDMContext)dmc;
			
			if (supportsProcPseudoFS()) {
				fFetchCPUInfoCache.execute(
						new MIMetaGetCPUInfo(fCommandControl.getContext()),
						new ImmediateDataRequestMonitor<MIMetaGetCPUInfoInfo>() {
							@Override
							protected void handleSuccess() {
								rm.done(parseCoresInfoForCores(cpuDmc, getData().getInfo()));
							}
						});
			} else {
				// No way to know the cores for Windows session.
				rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, NOT_SUPPORTED, "Operation not supported", null)); //$NON-NLS-1$
			}
		} else {
			rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_HANDLE, "Invalid DMC type", null)); //$NON-NLS-1$
		}
	}

	/**
	 * Parse the CoreInfo and create the CPU Contexts for the hardwareTarget context.
	 */
	ICPUDMContext[] parseCoresInfoForCPUs(IHardwareTargetDMContext dmc, ICoreInfo[] coresInfo) {
		Set<String> cpuIds = new HashSet<String>();
		ICPUDMContext[] CPUs;

		for (ICoreInfo core : coresInfo) {
			cpuIds.add(core.getPhysicalId());
		}

		String[] cpuIdsArray = cpuIds.toArray(new String[cpuIds.size()]);
		CPUs = new ICPUDMContext[cpuIdsArray.length];
		for (int i = 0; i < cpuIdsArray.length; i++) {
			CPUs[i] = createCPUContext(dmc, cpuIdsArray[i]);
		}
		return CPUs;		
	}

	/**
	 * Parse the CoreInfo and create the Core Contexts for the specified CPU context.
	 */
	ICoreDMContext[] parseCoresInfoForCores(ICPUDMContext cpuDmc, ICoreInfo[] coresInfo) {

		Vector<ICoreDMContext> coreDmcs = new Vector<ICoreDMContext>();
		for (ICoreInfo core : coresInfo) {
			if (core.getPhysicalId().equals(cpuDmc.getId())){
				// This core belongs to the right CPU
				coreDmcs.add(createCoreContext(cpuDmc, core.getId()));
			}
		}

		return coreDmcs.toArray(new ICoreDMContext[coreDmcs.size()]);
	}

	@Override
	public void getExecutionData(IDMContext dmc, DataRequestMonitor<IDMData> rm) {
		if (dmc instanceof ICoreDMContext) {
			rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_HANDLE, "Not done yet", null)); //$NON-NLS-1$
		} else if (dmc instanceof ICPUDMContext) {
			rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_HANDLE, "Not done yet", null)); //$NON-NLS-1$

		} else {
			rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_HANDLE, "Invalid DMC type", null)); //$NON-NLS-1$
		}
	}
	
	@Override
	public ICPUDMContext createCPUContext(IHardwareTargetDMContext targetDmc, String CPUId) {
		return new GDBCPUDMC(getSession().getId(), targetDmc, CPUId);
	}

	@Override
	public ICoreDMContext createCoreContext(ICPUDMContext cpuDmc, String coreId) {
		return new GDBCoreDMC(getSession().getId(), cpuDmc, coreId);
	}
	
    @DsfServiceEventHandler 
    public void eventDispatched(DataModelInitializedEvent e) {
    	// The launch sequence is complete, so we can start providing information.
    	// If we don't wait for this event, we may provide results before we are
    	// connected to the remote target which would be wrong.
    	// Bug 374293
    	fSessionInitializationComplete = true;
    }
    
	@Override
	public void flushCache(IDMContext context) {
		// Although the CPUInfo does not change,
		// this allows us to have a way to forcibly clear the cache.
		// We would need to call this method from the UI somehow.
		fFetchCPUInfoCache.reset(context);
	}
	
	/**
	 * A commandControl that will decide what to do when needing to find the CPUInfo.
	 * The class is used together with a CommandCache an MIMetaCommands to fetch
	 * information we need.
	 */
	private class CPUInfoManager implements ICommandControl {
	    private final List<ICommandListener> fCommandProcessors = new ArrayList<ICommandListener>();

		@Override
	    public <V extends ICommandResult> ICommandToken queueCommand(final ICommand<V> command, DataRequestMonitor<V> rm) {
	    	
	        final ICommandToken token = new ICommandToken() {
	        	@Override
	            public ICommand<? extends ICommandResult> getCommand() {
	                return command;
	            }
	        };
	        	    	
	    	// The class does not buffer commands itself, but sends them directly to the real
	    	// MICommandControl service.  Therefore, we must immediately tell our calling cache that the command
	    	// has been sent, since we can never cancel it.
	    	processCommandSent(token);

	    	if (command instanceof MIMetaGetCPUInfo) {
	            @SuppressWarnings("unchecked")            
	    		final DataRequestMonitor<MIMetaGetCPUInfoInfo> drm = (DataRequestMonitor<MIMetaGetCPUInfoInfo>)rm;
	            final ICommandControlDMContext dmc = (ICommandControlDMContext)command.getContext();
	            
	    		if (fBackend.getSessionType() == SessionType.REMOTE) {
	    			// Ask GDB to fetch /proc/cpuinfo from the remote target, and then we parse it.
	    			String remoteFile = "/proc/cpuinfo"; //$NON-NLS-1$
	    			final String localFile = sTempFolder + "proc.cpuinfo." + getSession().getId(); //$NON-NLS-1$
	    			fCommandControl.queueCommand(
	    					fCommandFactory.createCLIRemoteGet(dmc, remoteFile, localFile),
	    					new ImmediateDataRequestMonitor<MIInfo>(rm) {
	    						@Override
	    						protected void handleSuccess() {
	    							ICoreInfo[] info = new CoreList(localFile).getCoreList();
	    							// Now that we processed the file, remove it to avoid polluting the file system
	    							new File(localFile).delete();
	    							drm.done(new MIMetaGetCPUInfoInfo(info));
		                            processCommandDone(token, drm.getData());
	    						}
	    						@Override
	    						protected void handleError() {
	    							// On some older linux versions, gdbserver is not able to read from /proc
	    							// because it is a pseudo filesystem.
	    							// We need to find some other method of getting the info we need.
	    							
	    							// For a remote session, we can use GDB's -list-thread-groups --available
	    							// command, which shows on which cores a process is running.  This does
	    							// not necessarily give the exhaustive list of cores, but that is the best
	    							// we have in this case.
	    							//
	    							// In this case, we don't have knowledge about CPUs, so we lump all cores
	    							// into a single CPU.
	    							
	    							fCommandControl.queueCommand(
	    									fCommandFactory.createMIListThreadGroups(dmc, true),
	    									new ImmediateDataRequestMonitor<MIListThreadGroupsInfo>(drm) {
	    										@Override
	    										protected void handleSuccess() {
	    											// First extract the string id for every core GDB reports
	    											Set<String> coreIds = new HashSet<String>();
	    											IThreadGroupInfo[] groups = getData().getGroupList();
	    											for (IThreadGroupInfo group : groups) {
	    												coreIds.addAll(Arrays.asList(group.getCores()));
	    											}
	    											
	    											// Now create the context for each distinct core
	    											//
	    											// We don't have CPU info in this case so let's put them all under a single CPU
	    											final String defaultCPUId = "0";  //$NON-NLS-1$
	    											ICoreInfo[] info = new ICoreInfo[coreIds.size()];
	    											int i = 0;
	    											for (String id : coreIds) {
	    												info[i++] = new CoreInfo(id, defaultCPUId);
	    											}
	    			    							drm.done(new MIMetaGetCPUInfoInfo(info));
	    				                            processCommandDone(token, drm.getData());
	    										}
	    									});
	    						}
	    					});
	    		} else {
	    			// For a local session, parse /proc/cpuinfo directly.
	    			ICoreInfo[] info = new CoreList("/proc/cpuinfo").getCoreList(); //$NON-NLS-1$
					drm.done(new MIMetaGetCPUInfoInfo(info));
                    processCommandDone(token, drm.getData());
	    		}
	    	} else {
				rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, IDsfStatusConstants.INTERNAL_ERROR, 
						"Unexpected Meta command", null)); //$NON-NLS-1$
				rm.done();
	    	}
	    	return token;
	    }
		
		// Need to support these as they are used by the commandCache
	    @Override
	    public void addCommandListener(ICommandListener processor) { fCommandProcessors.add(processor); }
	    @Override
	    public void removeCommandListener(ICommandListener processor) { fCommandProcessors.remove(processor); }    

	    
	    private void processCommandSent(ICommandToken token) {
	        for (ICommandListener processor : fCommandProcessors) {
	            processor.commandSent(token);
	        }
	    }

	    private void processCommandDone(ICommandToken token, ICommandResult result) {
	        for (ICommandListener processor : fCommandProcessors) {
	            processor.commandDone(token, result);
	        }
	    }

	    @Override
	    public void addEventListener(IEventListener processor) { assert false : "Not supported"; } //$NON-NLS-1$
		@Override
	    public void removeEventListener(IEventListener processor) { assert false : "Not supported"; } //$NON-NLS-1$
		@Override
		public void removeCommand(ICommandToken token) { assert false : "Not supported"; } //$NON-NLS-1$

	}

	/**
	 * @since 4.2
	 */
	@Override
	public boolean isAvailable() {
		return getSessionInitializationComplete();
	}

	/**
	 * @since 4.2
	 */
	@Override
	public void getResourceClasses(IDMContext dmc,
			DataRequestMonitor<IResourceClass[]> rm) {
		rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, NOT_SUPPORTED, "Operation not supported", null)); //$NON-NLS-1$
	}

	/**
	 * @since 4.2
	 */
	@Override
	public void getResourcesInformation(IDMContext dmc, String resourceClassId,
			DataRequestMonitor<IResourcesInformation> rm) {
		rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, NOT_SUPPORTED, "Operation not supported", null)); //$NON-NLS-1$
	}


    /**
     * This method processes "load info" requests.  The load is computed using a
     * sampling method; two readings of a local or remote /proc/stat file are done
     * with a delay in between.  Then the load is computed from the two samples,
     * for all CPUs/cores known in the system.  
     * 
     * Because of the method used, it's possible that fast variations in CPU usage will
     * be missed.  However longer load trends should be reflected in the results.   
     * 
     * To avoid generating too much load in the remote case, there is a cache that will 
     * return the already computed load, if requested multiple times in a short period. 
     * There is also a mechanism to queue subsequent requests if one is ongoing.  Upon 
     * completion of the ongoing request, any queued request is answered with the load 
     * that was just computed.
     *  
     * @since 4.2
     */
	@Override
	public void getLoadInfo(final IDMContext context, final DataRequestMonitor<ILoadInfo> rm) {
		if (!(context instanceof ICoreDMContext) && !(context instanceof ICPUDMContext)) {
			// we only support getting the load for a CPU or a core 
			rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_HANDLE, "Load information not supported for this context type", null)); //$NON-NLS-1$
			return;
		}

		// The measurement interval should be of a minimum length to be meaningful
		assert (LOAD_SAMPLE_DELAY >= 100);
		// so the cache is useful
		assert (LOAD_CACHE_LIFETIME >= LOAD_SAMPLE_DELAY);
		
		// This way of computing the CPU load is only applicable to Linux
		if (!supportsProcPseudoFS()) {
			rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, NOT_SUPPORTED, "Operation not supported", null)); //$NON-NLS-1$
			return;
		}
		
		// Is a request is already ongoing?  
		if(fLoadRequestOngoing) {
			// queue current new request
			fLoadInfoRequestCache.put(context, rm);
			return;
		}
		// no request ongoing, so proceed
		fLoadRequestOngoing = true;
		
		// caching mechanism to keep things sane, even if the views(s) 
		// request load information very often.   
		long currentTime = System.currentTimeMillis();
		
		// time to fetch fresh load information?
		if (fLastCpuLoadRefresh + LOAD_CACHE_LIFETIME < currentTime) {
			fLastCpuLoadRefresh = currentTime;
		}
		else {
			// not time yet... re-use cached load data
			processLoads(context, rm, fCachedLoads);
			fLoadRequestOngoing = false;
			return;
		}

		final ProcStatParser procStatParser = new ProcStatParser();
        final ICommandControlDMContext dmc = DMContexts.getAncestorOfType(context, ICommandControlDMContext.class);
        final String statFile = "/proc/stat"; //$NON-NLS-1$
        final String localFile = sTempFolder + "proc.stat." + getSession().getId(); //$NON-NLS-1$
        
        // Remote debugging? We will ask GDB to get us the /proc/stat file from target, twice, with a delay between.
        if (fBackend.getSessionType() == SessionType.REMOTE) {
            fCommandControl.queueCommand(
                    fCommandFactory.createCLIRemoteGet(dmc, statFile, localFile),
                    new ImmediateDataRequestMonitor<MIInfo>(rm) {
                        @Override
                        protected void handleCompleted() {
                        	if (! isSuccess()) {
                        		fLoadRequestOngoing = false;
                        		rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INTERNAL_ERROR, "Can't get load info for CPU", null)); //$NON-NLS-1$
                        		return;
                        	}
                        	
                            // Success - parse first set of stat counters
                        	try {
								procStatParser.parseStatFile(localFile);
							} catch (Exception e) {
								rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INTERNAL_ERROR, "Can't get load info for CPU", null)); //$NON-NLS-1$
								fLoadRequestOngoing = false;
								return;
							}
                        	// delete temp file
							new File(localFile).delete();
                            
                            getExecutor().schedule(new Runnable() {
                                @Override
                                public void run() {
                                    fCommandControl.queueCommand(
                                            fCommandFactory.createCLIRemoteGet(dmc, statFile, localFile),
                                            new ImmediateDataRequestMonitor<MIInfo>(rm) {
                                                @Override
                                                protected void handleCompleted() {
                                                	if (! isSuccess()) {
                                                		fLoadRequestOngoing = false;
                                                		rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INTERNAL_ERROR, "Can't get load info for CPU", null)); //$NON-NLS-1$
                                                		return;
                                                	}
                                                	
                                                	// Success - parse the second set of stat counters and compute loads
                                                	try {
														procStatParser.parseStatFile(localFile);
													} catch (Exception e) {
														rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INTERNAL_ERROR, "Can't get load info for CPU", null)); //$NON-NLS-1$
														fLoadRequestOngoing = false;
														return;
													}
                                                	// delete temp file
                        							new File(localFile).delete();
                        							
                        							// Compute load
                                                	fCachedLoads = procStatParser.getCpuLoad();
                                                	processLoads(context, rm, fCachedLoads);
                                                	
                                                	// done with request
                                                	fLoadRequestOngoing = false;                                                	
                                                	// process any queued request
                                                	for(Entry<IDMContext, DataRequestMonitor<ILoadInfo>> e : fLoadInfoRequestCache.entrySet()) {
                                                		processLoads(e.getKey(), e.getValue(), fCachedLoads);
                                                	}
                                                	fLoadInfoRequestCache.clear();
                                                }
                                            });
                                }
                            }, LOAD_SAMPLE_DELAY, TimeUnit.MILLISECONDS);
                        }
                    });
        // Local debugging?  Then we can read /proc/stat directly 
        } else {
        	// Read /proc/stat file for the first time
        	try {
				procStatParser.parseStatFile(statFile);
			} catch (Exception e) {
				rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INTERNAL_ERROR, "Can't get load info for CPU", null)); //$NON-NLS-1$
				fLoadRequestOngoing = false;
				return;
			}
        	
        	// Read /proc/stat file again after a delay
        	getExecutor().schedule(new Runnable() {
        		@Override
        		public void run() {
        			try {
        				procStatParser.parseStatFile(statFile);
        			} catch (Exception e) {
        				rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INTERNAL_ERROR, "Can't get load info for CPU", null)); //$NON-NLS-1$
        				fLoadRequestOngoing = false;
        				return;
        			}
        			// compute load
        			fCachedLoads = procStatParser.getCpuLoad();
        			processLoads(context, rm, fCachedLoads);
        			
        			// done with request
                	fLoadRequestOngoing = false;                                                	
                	// process any queued request
                	for(Entry<IDMContext, DataRequestMonitor<ILoadInfo>> e : fLoadInfoRequestCache.entrySet()) {
                		processLoads(e.getKey(), e.getValue(), fCachedLoads);
                	}
                	fLoadInfoRequestCache.clear();
        		}
        	}, LOAD_SAMPLE_DELAY, TimeUnit.MILLISECONDS);
        }
    }
	
	/**
	 * For a given "load info" request, this method processes the load obtained from the 
	 * proc stat parser and creates/sends the response.
	 * @param context
	 * @param rm
	 * @param loads
	 */
	private void processLoads(final IDMContext context, final DataRequestMonitor<ILoadInfo> rm, final ProcStatCoreLoads loads) {

		// problem with fetching load info
		if (loads == null) {
			rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INTERNAL_ERROR, "Can't get load info", null)); //$NON-NLS-1$
			return;
		}
		// core context?
		if (context instanceof ICoreDMContext) {
			String coreId = ((ICoreDMContext) context).getId();
			// Integer precision sufficient for our purpose
			float load = loads.getLoad(coreId);
			rm.done(new GDBLoadInfo(Integer.toString((int)load)));
		}
		else if (context instanceof ICPUDMContext) {
			// get the list of cores in that CPU
			getCores(context,
					new ImmediateDataRequestMonitor<ICoreDMContext[]>() {
						@Override
						protected void handleCompleted() {
							ICoreDMContext[] coreContexts = getData();

							if (!isSuccess() || coreContexts == null || coreContexts.length < 1) {
								// Unable to get any core data
								rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INTERNAL_ERROR, "Can't get load info for CPU", null)); //$NON-NLS-1$
								return;
							}

							int i = 0;
							float load = 0.0f;
							// compute the average load of cores in that CPU
							for (ICoreDMContext coreCtx : coreContexts) {
								String coreId = coreCtx.getId();
								load += loads.getLoad(coreId);
								i++;
							}
							load /= i;
							rm.done(new GDBLoadInfo(Integer.toString((int)load)));
						}
					}
					);
		}
	}
}

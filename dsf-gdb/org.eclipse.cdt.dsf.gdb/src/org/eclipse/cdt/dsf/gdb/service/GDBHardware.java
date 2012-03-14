/*******************************************************************************
 * Copyright (c) 2012 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Marc Khouzam (Ericsson) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.Vector;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateExecutor;
import org.eclipse.cdt.dsf.concurrent.Immutable;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.AbstractDMContext;
import org.eclipse.cdt.dsf.datamodel.DataModelInitializedEvent;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.datamodel.IDMData;
import org.eclipse.cdt.dsf.debug.service.ICachingService;
import org.eclipse.cdt.dsf.gdb.internal.CoreList;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.gdb.service.command.IGDBControl;
import org.eclipse.cdt.dsf.mi.service.IMICommandControl;
import org.eclipse.cdt.dsf.mi.service.command.CommandFactory;
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
 * This class implements the IGDBHardware interface which gives access
 * to hardware information about the target.
 * 
 * @since 4.1
 */
@SuppressWarnings("restriction")
public class GDBHardware extends AbstractDsfService implements IGDBHardware, ICachingService {

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


    private IGDBControl fCommandControl;
    private IGDBBackend fBackend;
    private CommandFactory fCommandFactory;
	
	// The list of cores should not change, so we can store
	// it once we figured it out.
	private ICPUDMContext[] fCPUs;
    private ICoreDMContext[] fCores;

	// Track if the debug session has been fully initialized.
	// Until then, we may not be connected to the remote target
	// yet, and not be able to properly fetch the information we need.
    // Bug 374293
	private boolean fSessionInitializationComplete;

    public GDBHardware(DsfSession session) {
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
    	fBackend = getServicesTracker().getService(IGDBBackend.class);

        fCommandFactory = getServicesTracker().getService(IMICommandControl.class).getCommandFactory();

        getSession().addServiceEventListener(this, null);

        // Register this service.
		register(new String[] { IGDBHardware.class.getName(),
				                GDBHardware.class.getName() },
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
	public void getCPUs(IHardwareTargetDMContext dmc, DataRequestMonitor<ICPUDMContext[]> rm) {
		if (!fSessionInitializationComplete) {
			// We are not ready to answer yet
			rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_STATE, "Debug session not initialized yet", null)); //$NON-NLS-1$
			return;
		}

		if (fCPUs != null) {
			rm.done(fCPUs);
			return;
		}
		
		if (fBackend.getSessionType() == SessionType.REMOTE) {
			// Until we can get /proc/cpuinfo from the remote, we can't do anything
			fCPUs = new ICPUDMContext[0];
			rm.done(fCPUs);
		} else {
			// For a local session, let's use /proc/cpuinfo on linux
			if (Platform.getOS().equals(Platform.OS_LINUX)) {
				Set<String> cpuIds = new HashSet<String>();

				ICoreInfo[] cores = new CoreList().getCoreList();
				for (ICoreInfo core : cores) {
					cpuIds.add(core.getPhysicalId());
				}

				String[] cpuIdsArray = cpuIds.toArray(new String[cpuIds.size()]);
				fCPUs = new ICPUDMContext[cpuIdsArray.length];
				for (int i = 0; i < cpuIdsArray.length; i++) {
					fCPUs[i] = createCPUContext(dmc, cpuIdsArray[i]);
				}
			} else {
				// No way to know the CPUs on a local Windows session.
				fCPUs = new ICPUDMContext[0];
			}
			rm.done(fCPUs);
		}
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
			ICPUDMContext cpuDmc = (ICPUDMContext)dmc;
			
			if (fBackend.getSessionType() == SessionType.REMOTE) {
				rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, NOT_SUPPORTED, "Operation not supported", null)); //$NON-NLS-1$
			} else {
				if (Platform.getOS().equals(Platform.OS_LINUX)) {
					// Use /proc/cpuinfo to find the cores and match them to the specified CPU
					ICoreInfo[] cores = new CoreList().getCoreList();
					
					Vector<ICoreDMContext> coreDmcs = new Vector<ICoreDMContext>();
					for (ICoreInfo core : cores) {
						if (core.getPhysicalId().equals(cpuDmc.getId())){
							// This core belongs to the right CPU
						    coreDmcs.add(new GDBCoreDMC(getSession().getId(), cpuDmc, core.getId()));
						}
					}
					
					rm.done(coreDmcs.toArray(new ICoreDMContext[coreDmcs.size()]));
				} else {
					// No way to know the cores for a specific CPU on a remote Windows session.
					rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, NOT_SUPPORTED, "Operation not supported", null)); //$NON-NLS-1$
				}
			}
		} else if (dmc instanceof IHardwareTargetDMContext) {
			// Get all the cores for this target

			final IHardwareTargetDMContext targetDmc = (IHardwareTargetDMContext)dmc;
			
			// We already know the list of cores.  Just return it.
			if (fCores != null) {
				rm.done(fCores);
				return;
			}
			
			if (fBackend.getSessionType() == SessionType.REMOTE) {
				// For a remote session, we can use GDB's -list-thread-groups --available
				// command, which shows on which cores a process is running.  This does
				// not necessarily give the exhaustive list of cores, but that is the best
				// we have right now.
				//
				// In this case, we don't have knowledge about CPUs, so we lump all cores
				// into a single CPU.
				fCommandControl.queueCommand(
						fCommandFactory.createMIListThreadGroups(fCommandControl.getContext(), true),
						new DataRequestMonitor<MIListThreadGroupsInfo>(ImmediateExecutor.getInstance(), rm) {
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
								// We don't have CPU info in this case so let's put them all under
								// a single CPU
								ICPUDMContext cpuDmc = createCPUContext(targetDmc, "0"); //$NON-NLS-1$
								Set<ICoreDMContext> coreDmcs = new HashSet<ICoreDMContext>();
								for (String id : coreIds) {
									coreDmcs.add(new GDBCoreDMC(getSession().getId(), cpuDmc, id));
								}
								fCores = coreDmcs.toArray(new ICoreDMContext[coreDmcs.size()]);
								
								rm.done(fCores);
							}
						});
			} else {
				// For a local session, -list-thread-groups --available does not return
				// the cores field.  Let's use /proc/cpuinfo on linux instead
				if (Platform.getOS().equals(Platform.OS_LINUX)) {
					ICoreInfo[] cores = new CoreList().getCoreList();
					fCores = new ICoreDMContext[cores.length];
					for (int i = 0; i < cores.length; i++) {
						ICPUDMContext cpuDmc = createCPUContext(targetDmc, cores[i].getPhysicalId());
						fCores[i] = createCoreContext(cpuDmc, cores[i].getId());
					}
				} else {
					// No way to know the cores on a local Windows session.
					fCores = new ICoreDMContext[0];
				}
				rm.done(fCores);
			}
		} else {
			rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_HANDLE, "Invalid DMC type", null)); //$NON-NLS-1$
		}
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
		fCPUs = null;
		fCores = null;
	}
}

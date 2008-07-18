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
package org.eclipse.dd.gdb.internal.provisional.service;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.IProcessInfo;
import org.eclipse.cdt.core.IProcessList;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.dd.dsf.concurrent.DataRequestMonitor;
import org.eclipse.dd.dsf.concurrent.Immutable;
import org.eclipse.dd.dsf.concurrent.RequestMonitor;
import org.eclipse.dd.dsf.datamodel.AbstractDMContext;
import org.eclipse.dd.dsf.datamodel.IDMContext;
import org.eclipse.dd.dsf.debug.service.IProcesses;
import org.eclipse.dd.dsf.service.AbstractDsfService;
import org.eclipse.dd.dsf.service.DsfSession;
import org.eclipse.dd.gdb.internal.GdbPlugin;
import org.eclipse.dd.gdb.internal.provisional.service.command.GDBControl;
import org.eclipse.dd.gdb.internal.provisional.service.command.GDBControl.SessionType;
import org.eclipse.dd.mi.service.IMIProcessDMContext;
import org.eclipse.dd.mi.service.command.commands.CLIAttach;
import org.eclipse.dd.mi.service.command.commands.CLIMonitorListProcesses;
import org.eclipse.dd.mi.service.command.output.CLIMonitorListProcessesInfo;
import org.eclipse.dd.mi.service.command.output.MIInfo;
import org.osgi.framework.BundleContext;


public class GDBProcesses extends AbstractDsfService implements IProcesses {

    @Immutable
    protected class GdbThreadDMC extends AbstractDMContext
    implements IThreadDMContext
    {
    	/**
    	 * ID given by the OS.
    	 */
    	private final String fOSId;

    	/**
    	 * Constructor for the context.  It should not be called directly by clients.
    	 * Instead clients should call {@link GDBProcesses#createThreadContext}
    	 * to create instances of this context based on the thread ID.
    	 * <p/>
    	 * 
    	 * @param sessionId Session that this context belongs to.
    	 * @param processDmc The process that this thread belongs to.
    	 * @param id thread identifier.
    	 */
    	protected GdbThreadDMC(String sessionId, IProcessDMContext processDmc, String id) {
    		super(sessionId, processDmc != null ? new IDMContext[] { processDmc } : new IDMContext[0]);
    		fOSId = id;
    	}

    	/**
    	 * Returns the thread identifier of this context.
    	 * @return
    	 */
    	public String getId(){ return fOSId; }

    	@Override
    	public String toString() { return baseToString() + ".thread[" + fOSId + "]"; }  //$NON-NLS-1$ //$NON-NLS-2$

    	@Override
    	public boolean equals(Object obj) {
    		return super.baseEquals(obj) && ((GdbThreadDMC)obj).fOSId == fOSId;
    	}

    	@Override
    	public int hashCode() { return super.baseHashCode() ^ fOSId.hashCode(); }
    }

    @Immutable
    protected class GdbProcessDMC extends GdbThreadDMC
    implements IMIProcessDMContext
    {
    	/**
    	 * Constructor for the context.  It should not be called directly by clients.
    	 * Instead clients should call {@link GDBProcesses#createProcessContext}
    	 * to create instances of this context based on the PID.
    	 * <p/>
    	 * 
    	 * @param sessionId Session that this context belongs to.
    	 * @param id process identifier.
    	 */
    	protected GdbProcessDMC(String sessionId, String id) {
    		super(sessionId, null, id);
    	}
    	
    	public String getProcId() { return getId(); }
    	
    	@Override
    	public String toString() { return baseToString() + ".proc[" + getId() + "]"; }  //$NON-NLS-1$ //$NON-NLS-2$

    	@Override
    	public boolean equals(Object obj) {
    		return super.equals(obj);
    	}

    	@Override
    	public int hashCode() { return super.hashCode(); }
    }

    /*
     * The data of a corresponding thread or process.
     */
    @Immutable
    private static class GdbThreadDMData implements IThreadDMData {
    	final String fName;
    	final String fId;
    	
    	GdbThreadDMData(String name, String id) {
    		fName = name;
    		fId = id;
    	}

		public String getId() { return fId; }
		public String getName() { return fName; }
		public boolean isDebuggerAttached() {
			return true;
		}
    }
    
    private GDBControl fCommandControl;
    
    // A map of pid to names.  It is filled when we get all the
    // processes that are running
    private Map<Integer, String> fProcessNames = new HashMap<Integer, String>();

    public GDBProcesses(DsfSession session) {
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
        
		// Register this service.
		register(new String[] { IProcesses.class.getName(),
				GDBProcesses.class.getName() },
				new Hashtable<String, String>());
		
		fCommandControl = getServicesTracker().getService(GDBControl.class);
        
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
		super.shutdown(requestMonitor);
	}
	
	/**
	 * @return The bundle context of the plug-in to which this service belongs.
	 */
	@Override
	protected BundleContext getBundleContext() {
		return GdbPlugin.getBundleContext();
	}
	
	/**
	 * Create a thread context.
	 * 
	 * @param process The parent process context
	 * @param threadId The OS Id of the thread
	 */
    public IThreadDMContext createThreadContext(IProcessDMContext process, String threadId) {
        return new GdbThreadDMC(getSession().getId(), process, threadId);
    }

	/**
	 * Create a process context.
	 * 
	 * @param pid The OS Id of the process
	 */
    public IProcessDMContext createProcessContext(String pid) {
        return new GdbProcessDMC(getSession().getId(), pid);
    }

	/**
	 * This method obtains the model data for a given GdbThreadDMC object
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
            rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_HANDLE, "Unknown DMC type", null)); //$NON-NLS-1$
            rm.done();
		}
	}


	public void getExecutionData(IThreadDMContext dmc, DataRequestMonitor<IThreadDMData> rm) {
		// We must first check for GdbProcessDMC because it is also a GdbThreadDMC
		if (dmc instanceof GdbProcessDMC) {
			String pidStr = ((GdbProcessDMC)dmc).getId();
			int pid = 0;
			try {
				pid = Integer.parseInt(pidStr);
			} catch (NumberFormatException e) {
			}
			
			String name = fProcessNames.get(pid);
			// If we don't find the name in our list, return the default name of our program
			if (name == null) name = fCommandControl.getExecutablePath().lastSegment();
			rm.setData(new GdbThreadDMData(name, pidStr));
			rm.done();
		} else if (dmc instanceof GdbThreadDMC) {
			rm.setData(new GdbThreadDMData("", ((GdbThreadDMC)dmc).getId())); //$NON-NLS-1$
			rm.done();
		} else {
            rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_HANDLE, "Unknown DMC type", null)); //$NON-NLS-1$
            rm.done();
		}
	}
	
    public void getDebuggingContext(IThreadDMContext dmc, DataRequestMonitor<IDMContext> rm) {
		rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID,
				NOT_SUPPORTED, "Not supported", null)); //$NON-NLS-1$
		rm.done();
    }

    public void attachDebuggerToProcess(IProcessDMContext procCtx, final DataRequestMonitor<IDMContext> rm) {
		if (procCtx instanceof IMIProcessDMContext) {
			fCommandControl.queueCommand(
					new CLIAttach((IMIProcessDMContext)procCtx),
					new DataRequestMonitor<MIInfo>(getExecutor(), rm) {
						@Override
						protected void handleSuccess() {
							rm.setData(fCommandControl.getGDBDMContext());
							rm.done();
						}
					});

	    } else {
            rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INTERNAL_ERROR, "Invalid process context.", null)); //$NON-NLS-1$
            rm.done();
	    }
	}
	
	public void detachDebuggerFromProcess(IProcessDMContext procCtx, final RequestMonitor rm) {
//		if (procCtx instanceof GdbProcessDMC) {
//			int pid;
//			try {
//				pid = Integer.parseInt(((GdbProcessDMC)procCtx).getId());
//			} catch (NumberFormatException e) {
//	            rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_HANDLE, "Invalid process id.", null)); //$NON-NLS-1$
//	            rm.done();
//	            return;
//			}
//			
//			fCommandControl.queueCommand(
//					new CLIDetach(procCtx, pid),
//					new DataRequestMonitor<MIInfo>(getExecutor(), rm));
//	    } else {
//            rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INTERNAL_ERROR, "Invalid process context.", null)); //$NON-NLS-1$
//            rm.done();
//	    }
		rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID,
				NOT_SUPPORTED, "Not supported", null)); //$NON-NLS-1$
		rm.done();
	}

	public void canTerminate(IThreadDMContext thread, DataRequestMonitor<Boolean> rm) {
		rm.setData(true);
		rm.done();
	}

	public void debugNewProcess(String file, DataRequestMonitor<IProcessDMContext> rm) {
		rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID,
				NOT_SUPPORTED, "Not supported", null)); //$NON-NLS-1$
		rm.done();
	}
    
	public void getProcessesBeingDebugged(IDMContext dmc, DataRequestMonitor<IDMContext[]> rm) {
		// use -list-thread-groups
		rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID,
				NOT_SUPPORTED, "Not supported", null)); //$NON-NLS-1$
		rm.done();
	}

	public void getRunningProcesses(IDMContext dmc, final DataRequestMonitor<IProcessDMContext[]> rm) {
		if (fCommandControl.getSessionType() == SessionType.LOCAL) {
			IProcessList list = null;
			try {
				list = CCorePlugin.getDefault().getProcessList();
			} catch (CoreException e) {
			}

			if (list == null) {
				// If the list is null, the prompter will deal with it
				fProcessNames.clear();
				rm.setData(null);
			} else {
				fProcessNames.clear();
				for (IProcessInfo procInfo : list.getProcessList()) {
					fProcessNames.put(procInfo.getPid(), procInfo.getName());
				}
				rm.setData(makeProcessDMCs(list.getProcessList()));
			}
			rm.done();
		} else {
			// monitor list processes is only for remote session
			fCommandControl.queueCommand(
					new CLIMonitorListProcesses(dmc), 
					new DataRequestMonitor<CLIMonitorListProcessesInfo>(getExecutor(), rm) {
						@Override
						protected void handleCompleted() {
							if (isSuccess()) {
								for (IProcessInfo procInfo : getData().getProcessList()) {
									fProcessNames.put(procInfo.getPid(), procInfo.getName());
								}
								rm.setData(makeProcessDMCs(getData().getProcessList()));
							} else {
								// The monitor list command is not supported.
								// Just return an empty list and let the caller deal with it.
								fProcessNames.clear();
								rm.setData(new IProcessDMContext[0]);
							}
							rm.done();
						}

					});
		}
	}

	private IProcessDMContext[] makeProcessDMCs(IProcessInfo[] processes) {
		IProcessDMContext[] procDmcs = new GdbProcessDMC[processes.length];
		for (int i=0; i<procDmcs.length; i++) {
			procDmcs[i] = createProcessContext(Integer.toString(processes[i].getPid())); 
		}
		return procDmcs;
	}
    	

	public void runNewProcess(String file, DataRequestMonitor<IProcessDMContext> rm) {
		rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID,
				NOT_SUPPORTED, "Not supported", null)); //$NON-NLS-1$
		rm.done();
	}

	public void terminate(IThreadDMContext thread, RequestMonitor rm) {
		if (thread instanceof GdbProcessDMC) {
			fCommandControl.terminate(rm);
	    } else {
            rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INTERNAL_ERROR, "Invalid process context.", null)); //$NON-NLS-1$
            rm.done();
	    }
	}
}

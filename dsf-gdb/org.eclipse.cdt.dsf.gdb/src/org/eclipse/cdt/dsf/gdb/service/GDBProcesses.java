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

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.IProcessInfo;
import org.eclipse.cdt.core.IProcessList;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IProcesses;
import org.eclipse.cdt.dsf.debug.service.IMemory.IMemoryDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.gdb.service.command.IGDBControl;
import org.eclipse.cdt.dsf.mi.service.IMIContainerDMContext;
import org.eclipse.cdt.dsf.mi.service.IMIProcessDMContext;
import org.eclipse.cdt.dsf.mi.service.IMIProcesses;
import org.eclipse.cdt.dsf.mi.service.MIProcesses;
import org.eclipse.cdt.dsf.mi.service.command.MIInferiorProcess;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.BundleContext;


public class GDBProcesses extends MIProcesses {
    
	private class GDBContainerDMC extends MIContainerDMC
	implements IMemoryDMContext 
	{
		public GDBContainerDMC(String sessionId, IProcessDMContext processDmc, String groupId) {
			super(sessionId, processDmc, groupId);
		}
	}
	
    private IGDBControl fGdb;
    
    // A map of pid to names.  It is filled when we get all the
    // processes that are running
    private Map<Integer, String> fProcessNames = new HashMap<Integer, String>();

    public GDBProcesses(DsfSession session) {
    	super(session);
    }

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
        
        fGdb = getServicesTracker().getService(IGDBControl.class);
        
		// Register this service.
		register(new String[] { IProcesses.class.getName(),
				IMIProcesses.class.getName(),
				MIProcesses.class.getName(),
				GDBProcesses.class.getName() },
				new Hashtable<String, String>());
        
		ICommandControlService commandControl = getServicesTracker().getService(ICommandControlService.class);
		IProcessDMContext procDmc = createProcessContext(commandControl.getContext(), MIProcesses.UNIQUE_GROUP_ID);
		IContainerDMContext containerDmc = createContainerContext(procDmc, MIProcesses.UNIQUE_GROUP_ID);
		fGdb.getInferiorProcess().setContainerContext(containerDmc);

		requestMonitor.done();
	}

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

	@Override
	public IMIContainerDMContext createContainerContext(IProcessDMContext processDmc,
			                                            String groupId) {
		return new GDBContainerDMC(getSession().getId(), processDmc, groupId);
	}
	
	@Override
	public void getExecutionData(IThreadDMContext dmc, DataRequestMonitor<IThreadDMData> rm) {
		if (dmc instanceof IMIProcessDMContext) {
			String pidStr = ((IMIProcessDMContext)dmc).getProcId();
			// In our context hierarchy we don't actually use the pid in this version, because in this version,
			// we only debug a single process.  This means we will not have a proper pid in all cases
			// inside the context, so must find it another way.  Note that this method is also called to find the name
			// of processes to attach to, and in this case, we do have the proper pid. 
			if (pidStr == null || pidStr.length() == 0) {
				MIInferiorProcess inferiorProcess = fGdb.getInferiorProcess();
			    if (inferiorProcess != null) {
			    	pidStr = inferiorProcess.getPid();
			    }
			}
			int pid = -1;
			try {
				pid = Integer.parseInt(pidStr);
			} catch (NumberFormatException e) {
			}
			
			String name = fProcessNames.get(pid);
			if (name == null) {
				// Hm. Strange. But if the pid is our inferior's, we can just use the binary name
				MIInferiorProcess inferior = fGdb.getInferiorProcess();
				if (inferior != null) {
					String inferiorPidStr = inferior.getPid();
					if (inferiorPidStr != null && Integer.parseInt(inferiorPidStr) == pid) {
						IGDBBackend backend = getServicesTracker().getService(IGDBBackend.class);
						name = backend.getProgramPath().lastSegment();
					}
				}
			}
			if (name == null) {
				// Should not happen.
				name = "Unknown name"; //$NON-NLS-1$
				assert false : "Don't have entry for process ID: " + pid; //$NON-NLS-1$
			}
		
			rm.setData(new MIThreadDMData(name, pidStr));
			rm.done();
		} else {
			super.getExecutionData(dmc, rm);
		}
	}

	@Override
	public void isDebuggerAttachSupported(IDMContext dmc, DataRequestMonitor<Boolean> rm) {
        MIInferiorProcess inferiorProcess = fGdb.getInferiorProcess();
	    if (!fGdb.isConnected() &&
	    	inferiorProcess != null && 
	    	inferiorProcess.getState() != MIInferiorProcess.State.TERMINATED) {
	    	
	    	rm.setData(true);
	    } else {
	    	rm.setData(false);
	    }
		rm.done();
	}

	@Override
    public void attachDebuggerToProcess(final IProcessDMContext procCtx, final DataRequestMonitor<IDMContext> rm) {
		super.attachDebuggerToProcess(
			procCtx, 
			new DataRequestMonitor<IDMContext>(getExecutor(), rm) {
				@Override
				protected void handleSuccess() {
					fGdb.setConnected(true);

					MIInferiorProcess inferiorProcess = fGdb.getInferiorProcess();
				    if (inferiorProcess != null) {
				    	inferiorProcess.setPid(((IMIProcessDMContext)procCtx).getProcId());
				    }

					rm.setData(getData());
					rm.done();
				}
			});
	}

	@Override
    public void canDetachDebuggerFromProcess(IDMContext dmc, DataRequestMonitor<Boolean> rm) {
    	rm.setData(false); // don't turn on yet, as we need to generate events to use this properly
    	rm.done();
    }

	@Override
    public void detachDebuggerFromProcess(IDMContext dmc, final RequestMonitor rm) {
		super.detachDebuggerFromProcess(
			dmc, 
			new RequestMonitor(getExecutor(), rm) {
				@Override
				protected void handleSuccess() {
					fGdb.setConnected(false);
					
					MIInferiorProcess inferiorProcess = fGdb.getInferiorProcess();
				    if (inferiorProcess != null) {
				    	inferiorProcess.setPid(null);
				    }

					rm.done();
				}
			});
	}
	
	@Override
	public void getProcessesBeingDebugged(IDMContext dmc, DataRequestMonitor<IDMContext[]> rm) {
        MIInferiorProcess inferiorProcess = fGdb.getInferiorProcess();
	    if (fGdb.isConnected() &&
	    	inferiorProcess != null && 
	    	inferiorProcess.getState() != MIInferiorProcess.State.TERMINATED) {

   	    	super.getProcessesBeingDebugged(dmc, rm);
	    } else {
	    	rm.setData(new IDMContext[0]);
	    	rm.done();
	    }
	}
	
	@Override
	public void getRunningProcesses(IDMContext dmc, final DataRequestMonitor<IProcessDMContext[]> rm) {
		final ICommandControlDMContext controlDmc = DMContexts.getAncestorOfType(dmc, ICommandControlDMContext.class);
		IGDBBackend backend = getServicesTracker().getService(IGDBBackend.class);
		if (backend.getSessionType() == SessionType.LOCAL) {
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
				IProcessInfo[] procInfos = list.getProcessList();
				for (IProcessInfo procInfo : procInfos) {
					fProcessNames.put(procInfo.getPid(), procInfo.getName());
				}
				rm.setData(makeProcessDMCs(controlDmc, procInfos));
			}
			rm.done();
		} else {
			// Pre-GDB 7.0, there is no way to list processes on a remote host
			// Just return an empty list and let the caller deal with it.
			fProcessNames.clear();
			rm.setData(new IProcessDMContext[0]);
			rm.done();
		}
	}

	private IProcessDMContext[] makeProcessDMCs(ICommandControlDMContext controlDmc, IProcessInfo[] processes) {
		IProcessDMContext[] procDmcs = new IMIProcessDMContext[processes.length];
		for (int i=0; i<procDmcs.length; i++) {
			procDmcs[i] = createProcessContext(controlDmc, Integer.toString(processes[i].getPid())); 
		}
		return procDmcs;
	}
	
	@Override
    public void terminate(IThreadDMContext thread, RequestMonitor rm) {
		if (thread instanceof IMIProcessDMContext) {
			fGdb.terminate(rm);
	    } else {
            rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INTERNAL_ERROR, "Invalid process context.", null)); //$NON-NLS-1$
            rm.done();
	    }
	}
}

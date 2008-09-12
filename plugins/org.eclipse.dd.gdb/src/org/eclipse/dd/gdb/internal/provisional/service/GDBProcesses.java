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
import org.eclipse.dd.dsf.concurrent.RequestMonitor;
import org.eclipse.dd.dsf.datamodel.DMContexts;
import org.eclipse.dd.dsf.datamodel.IDMContext;
import org.eclipse.dd.dsf.debug.service.IProcesses;
import org.eclipse.dd.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.dd.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.dd.dsf.service.DsfSession;
import org.eclipse.dd.gdb.internal.GdbPlugin;
import org.eclipse.dd.gdb.internal.provisional.service.command.IGDBControl;
import org.eclipse.dd.mi.service.IMIExecutionGroupDMContext;
import org.eclipse.dd.mi.service.IMIProcessDMContext;
import org.eclipse.dd.mi.service.IMIProcesses;
import org.eclipse.dd.mi.service.MIProcesses;
import org.eclipse.dd.mi.service.command.MIInferiorProcess;
import org.eclipse.dd.mi.service.command.commands.CLIMonitorListProcesses;
import org.eclipse.dd.mi.service.command.output.CLIMonitorListProcessesInfo;
import org.osgi.framework.BundleContext;


public class GDBProcesses extends MIProcesses {
    
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
	public void getExecutionData(IThreadDMContext dmc, DataRequestMonitor<IThreadDMData> rm) {
		if (dmc instanceof IMIProcessDMContext) {
			String pidStr = ((IMIProcessDMContext)dmc).getProcId();
			int pid = -1;
			try {
				pid = Integer.parseInt(pidStr);
			} catch (NumberFormatException e) {
			}
			
			String name = fProcessNames.get(pid);
			// If we don't find the name in our list, return the default name of our program
			if (name == null) name = fGdb.getExecutablePath().lastSegment();
			rm.setData(new MIThreadDMData(name, pidStr));
			rm.done();
		} else {
			super.getExecutionData(dmc, rm);
		}
	}

	@Override
	public void isDebuggerAttachSupported(IDMContext dmc, DataRequestMonitor<Boolean> rm) {
		rm.setData(!fGdb.isConnected());
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

	    	final IMIExecutionGroupDMContext groupDmc = DMContexts.getAncestorOfType(dmc, IMIExecutionGroupDMContext.class);
			if (groupDmc == null) {
				// This service version only handles a single process to debug, therefore, we can simply
				// create the context describing this process ourselves.
				ICommandControlDMContext controlDmc = DMContexts.getAncestorOfType(dmc, ICommandControlDMContext.class);
				// Get the groupId properly for the case of an attach
				String groupId = getExecutionGroupIdFromThread(null);
				IProcessDMContext procDmc = createProcessContext(controlDmc, groupId);
				IMIExecutionGroupDMContext newGroupDmc = createExecutionGroupContext(procDmc, groupId);
				rm.setData(new IContainerDMContext[] {newGroupDmc});
				rm.done();
			} else {
				// List of threads
    	    	super.getProcessesBeingDebugged(dmc, rm);
			}
	    } else {
	    	rm.setData(new IDMContext[0]);
	    	rm.done();
	    }
	}
	
	@Override
	public void getRunningProcesses(IDMContext dmc, final DataRequestMonitor<IProcessDMContext[]> rm) {
		final ICommandControlDMContext controlDmc = DMContexts.getAncestorOfType(dmc, ICommandControlDMContext.class);
		if (fGdb.getSessionType() == SessionType.LOCAL) {
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
				rm.setData(makeProcessDMCs(controlDmc, list.getProcessList()));
			}
			rm.done();
		} else {
			// monitor list processes is only for remote session
			fGdb.queueCommand(
					new CLIMonitorListProcesses(dmc), 
					new DataRequestMonitor<CLIMonitorListProcessesInfo>(getExecutor(), rm) {
						@Override
						protected void handleCompleted() {
							if (isSuccess()) {
								for (IProcessInfo procInfo : getData().getProcessList()) {
									fProcessNames.put(procInfo.getPid(), procInfo.getName());
								}
								rm.setData(makeProcessDMCs(controlDmc, getData().getProcessList()));
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
	
    @Override
	public String getExecutionGroupIdFromThread(String threadId) {
    	// We need to properly return the groupId based on the pid
    	// to properly handle the case of an attach.  See bug 244749
    	String groupId = null;
		MIInferiorProcess inferiorProcess = fGdb.getInferiorProcess();
	    if (inferiorProcess != null) {
	    	groupId = inferiorProcess.getPid();
	    }
	    if (groupId != null) {
	    	return groupId;
	    } else {
	    	return super.getExecutionGroupIdFromThread(threadId);
	    }
    }

}

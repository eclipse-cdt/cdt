/*******************************************************************************
 * Copyright (c) 2008, 2009 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.actions;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;

import org.eclipse.cdt.dsf.concurrent.CountingRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IProcesses;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IProcessDMContext;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IThreadDMData;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.cdt.dsf.gdb.actions.IConnect;
import org.eclipse.cdt.dsf.gdb.internal.ui.GdbUIPlugin;
import org.eclipse.cdt.dsf.gdb.launching.IProcessExtendedInfo;
import org.eclipse.cdt.dsf.gdb.launching.LaunchMessages;
import org.eclipse.cdt.dsf.gdb.service.IGDBProcesses.IGdbThreadDMData;
import org.eclipse.cdt.dsf.mi.service.IMIProcesses;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IStatusHandler;

public class GdbConnectCommand implements IConnect {
    
	private final DsfExecutor fExecutor;
    private final DsfServicesTracker fTracker;
    
    public GdbConnectCommand(DsfSession session) {
        fExecutor = session.getExecutor();
        fTracker = new DsfServicesTracker(GdbUIPlugin.getBundleContext(), session.getId());
    }    

    public void dispose() {
        fTracker.dispose();
    }

    public boolean canConnect() {
       	Query<Boolean> canConnectQuery = new Query<Boolean>() {
            @Override
            public void execute(DataRequestMonitor<Boolean> rm) {
       			IProcesses procService = fTracker.getService(IProcesses.class);
       			ICommandControlService commandControl = fTracker.getService(ICommandControlService.class);

       			if (procService != null && commandControl != null) {
       				procService.isDebuggerAttachSupported(commandControl.getContext(), rm);
       			} else {
       				rm.setData(false);
       				rm.done();
       			}
       		}
       	};
    	try {
    		fExecutor.execute(canConnectQuery);
			return canConnectQuery.get();
		} catch (InterruptedException e) {
		} catch (ExecutionException e) {
        } catch (RejectedExecutionException e) {
        	// Can be thrown if the session is shutdown
        }

		return false;
     }

    // Need a job because prompter.handleStatus will block
    class PromptForPidJob extends Job {

    	// The list of processes used in the case of an ATTACH session
    	IProcessExtendedInfo[] fProcessList = null;
    	DataRequestMonitor<Integer> fRequestMonitor;

    	public PromptForPidJob(String name, IProcessExtendedInfo[] procs, DataRequestMonitor<Integer> rm) {
    		super(name);
    		fProcessList = procs;
    		fRequestMonitor = rm;
    	}

    	@Override
    	protected IStatus run(IProgressMonitor monitor) {
    		IStatus promptStatus = new Status(IStatus.INFO, "org.eclipse.debug.ui", 200/*STATUS_HANDLER_PROMPT*/, "", null); //$NON-NLS-1$//$NON-NLS-2$
    		final IStatus processPromptStatus = new Status(IStatus.INFO, "org.eclipse.cdt.dsf.gdb.ui", 100, "", null); //$NON-NLS-1$//$NON-NLS-2$

    		final IStatusHandler prompter = DebugPlugin.getDefault().getStatusHandler(promptStatus);

    		final Status NO_PID_STATUS = new Status(IStatus.ERROR, GdbUIPlugin.PLUGIN_ID, -1,
    				LaunchMessages.getString("LocalAttachLaunchDelegate.No_Process_ID_selected"), //$NON-NLS-1$
    				null);

    		if (prompter == null) {
    			fRequestMonitor.setStatus(NO_PID_STATUS);
    			fRequestMonitor.done();
    			return Status.OK_STATUS;
    		} 				

    		try {
    			Object result = prompter.handleStatus(processPromptStatus, fProcessList);
    			if (result instanceof Integer) {
    				fRequestMonitor.setData((Integer)result);
    			} else {
    				fRequestMonitor.setStatus(NO_PID_STATUS);
    			}
    		} catch (CoreException e) {
    			fRequestMonitor.setStatus(NO_PID_STATUS);
    		}
    		fRequestMonitor.done();

    		return Status.OK_STATUS;
    	}
    };
    
    public void connect(RequestMonitor requestMonitor)
    {
    	// Create a fake rm to avoid null pointer exceptions
    	final RequestMonitor rm;
    	if (requestMonitor == null) {
    		rm = new RequestMonitor(fExecutor, null);
    	} else {
    		rm = requestMonitor;
    	}
    	
    	// Don't wait for the operation to finish because this
    	// method can be called from the UI thread, and it will
    	// block it, which is bad, because we need to use the UI
    	// thread to prompt the user for the process to choose.
    	// This is why we simply use a DsfRunnable.
    	fExecutor.execute(new DsfRunnable() {
    		public void run() {
    			final IProcesses procService = fTracker.getService(IProcesses.class);
    			ICommandControlService commandControl = fTracker.getService(ICommandControlService.class);

    			if (procService != null && commandControl != null) {
        			final ICommandControlDMContext controlCtx = commandControl.getContext();        
    				procService.getRunningProcesses(
    						controlCtx,        
    						new DataRequestMonitor<IProcessDMContext[]>(fExecutor, rm) {
    							@Override
    							protected void handleSuccess() {

    								final List<IProcessExtendedInfo> procInfoList = new ArrayList<IProcessExtendedInfo>();

									final CountingRequestMonitor countingRm = 
										new CountingRequestMonitor(fExecutor, rm) {
										@Override
										protected void handleSuccess() {
											new PromptForPidJob(
													"Prompt for Process", procInfoList.toArray(new IProcessExtendedInfo[0]),   //$NON-NLS-1$
													new DataRequestMonitor<Integer>(fExecutor, rm) {
														@Override
														protected void handleSuccess() {
															// New cycle, look for service again
															final IMIProcesses procService = fTracker.getService(IMIProcesses.class);
															if (procService != null) {
																IProcessDMContext procDmc = procService.createProcessContext(controlCtx,
																		Integer.toString(getData()));
																procService.attachDebuggerToProcess(procDmc, new DataRequestMonitor<IDMContext>(fExecutor, rm));
															}
														}
													}).schedule();
										}
									};

    								if (getData().length > 0 && getData()[0] instanceof IThreadDMData) {
    									// The list of running processes also contains the name of the processes
    									// This is much more efficient.  Let's use it.
										for (IProcessDMContext processCtx : getData()) {
											IThreadDMData processData = (IThreadDMData) processCtx;
											int pid = 0;
											try {
												pid = Integer.parseInt(processData.getId());
											} catch (NumberFormatException e) {
											}
											String[] cores = null;
											String owner = null;
											if (processData instanceof IGdbThreadDMData) {
												cores = ((IGdbThreadDMData)processData).getCores();
												owner = ((IGdbThreadDMData)processData).getOwner();
											}
											procInfoList.add(new ProcessInfo(pid, processData.getName(), cores, owner));
										}

										// Re-use the counting monitor and trigger it right away.
										// No need to call done() in this case.
										countingRm.setDoneCount(0);
    								} else {
    									// The list of running processes does not contain the names, so
    									// we must obtain it individually

    									// For each process, obtain its name
    									// Once all the names are obtained, prompt the user for the pid to use

    									// New cycle, look for service again
    									final IProcesses procService = fTracker.getService(IProcesses.class);

    									if (procService != null) {
    										countingRm.setDoneCount(getData().length);

    										for (IProcessDMContext processCtx : getData()) {
    											procService.getExecutionData(
    													processCtx,
    													new DataRequestMonitor<IThreadDMData> (fExecutor, countingRm) {
    														@Override
    														protected void handleSuccess() {
    															IThreadDMData processData = getData();
    															int pid = 0;
    															try {
    																pid = Integer.parseInt(processData.getId());
    															} catch (NumberFormatException e) {
    															}
    															String[] cores = null;
    															String owner = null;
    															if (processData instanceof IGdbThreadDMData) {
    																cores = ((IGdbThreadDMData)processData).getCores();
    																owner = ((IGdbThreadDMData)processData).getOwner();
    															}
    															procInfoList.add(new ProcessInfo(pid, processData.getName(), cores, owner));
    															countingRm.done();
    														}
    													});
    										}
    									} else {
    										// Trigger right away.  No need to call done() in this case.
    										countingRm.setDoneCount(0);
    									}
    								}
    							}
    						});
    			} else {
    				rm.done();
    			}
    		}
    	});
    }
}

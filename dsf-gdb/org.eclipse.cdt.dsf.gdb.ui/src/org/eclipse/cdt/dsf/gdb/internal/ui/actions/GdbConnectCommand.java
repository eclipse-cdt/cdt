/*******************************************************************************
 * Copyright (c) 2008, 2011 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson - initial API and implementation
 *     Marc Khouzam (Ericsson) - Add support for multi-attach (Bug 293679)
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.actions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;

import org.eclipse.cdt.dsf.concurrent.CountingRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.concurrent.ImmediateDataRequestMonitor;
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
import org.eclipse.cdt.dsf.gdb.internal.ui.launching.LaunchUIMessages;
import org.eclipse.cdt.dsf.gdb.internal.ui.launching.ProcessPrompter.PrompterInfo;
import org.eclipse.cdt.dsf.gdb.launching.IProcessExtendedInfo;
import org.eclipse.cdt.dsf.gdb.launching.LaunchMessages;
import org.eclipse.cdt.dsf.gdb.service.IGDBBackend;
import org.eclipse.cdt.dsf.gdb.service.IGDBProcesses;
import org.eclipse.cdt.dsf.gdb.service.IGDBProcesses.IGdbThreadDMData;
import org.eclipse.cdt.dsf.gdb.service.SessionType;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IStatusHandler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.progress.UIJob;

public class GdbConnectCommand implements IConnect {
    
	private final DsfExecutor fExecutor;
    private final DsfServicesTracker fTracker;
    
    // A map of processName to path, that allows us to remember the path to the binary file
    // for a process with a particular name.  We can then re-use the same binary for another
    // process with the same name.  This allows a user to connect to multiple processes
    // with the same name without having to be prompted each time for a path.
    // This map is associated to the current debug session only, therefore the user can
    // reset it by using a new debug session.
    // This map is only needed for remote sessions, since we don't need to specify
    // the binary location for a local attach session.
    private Map<String, String> fProcessNameToBinaryMap = new HashMap<String, String>();
    
    public GdbConnectCommand(DsfSession session) {
        fExecutor = session.getExecutor();
        fTracker = new DsfServicesTracker(GdbUIPlugin.getBundleContext(), session.getId());
    }    

    public void dispose() {
        fTracker.dispose();
    }

    @Override
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

    /**
     * This job will prompt the user to select a set of processes
     * to attach too.
     * We need a job because prompter.handleStatus will block and
     * we don't want to block the executor.
     */
    protected class PromptForPidJob extends Job {

    	// The list of processes used in the case of an ATTACH session
    	IProcessExtendedInfo[] fProcessList = null;
    	DataRequestMonitor<Object> fRequestMonitor;
    	boolean fNewProcessSupported;

    	public PromptForPidJob(String name, boolean newProcessSupported, IProcessExtendedInfo[] procs, DataRequestMonitor<Object> rm) {
    		super(name);
    		fNewProcessSupported = newProcessSupported;
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
    			PrompterInfo info = new PrompterInfo(fNewProcessSupported, fProcessList);
    			Object result = prompter.handleStatus(processPromptStatus, info);
    			 if (result == null) {
 					fRequestMonitor.cancel();
 				} else if (result instanceof IProcessExtendedInfo[] || result instanceof String) {
    				fRequestMonitor.setData(result);
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
    
    /**
     * This job will prompt the user for a path to the binary to use,
     * and then will attach to the process.
     * We need a job to free the executor while we prompt the user for 
     * a binary path. Bug 344892
     */
    private class PromptAndAttachToProcessJob extends UIJob {
    	private final String fPid;
    	private final RequestMonitor fRm;
    	private final String fTitle;
    	private final String fProcName;
    	
    	public PromptAndAttachToProcessJob(String pid, String title, String procName, RequestMonitor rm) {
    		super(""); //$NON-NLS-1$
    		fPid = pid;
    		fTitle = title;
    		fProcName = procName;
    		fRm = rm;
    	}

    	@Override
    	public IStatus runInUIThread(IProgressMonitor monitor) {

    		// Have we already see the binary for a process with this name?
    		String binaryPath = fProcessNameToBinaryMap.get(fProcName);

    		if (binaryPath == null) {
    			// prompt for the binary path
    			Shell shell = Display.getCurrent().getActiveShell();
    			if (shell != null) {
    				FileDialog fd = new FileDialog(shell, SWT.NONE);
    				fd.setText(fTitle);
    				binaryPath = fd.open();
    			}
    		}
    		
    		if (binaryPath == null) {
    			// The user pressed the cancel button, so we cancel the attach gracefully
    			fRm.done();
    		} else {

    			final String finalBinaryPath = binaryPath;
    			fExecutor.execute(new DsfRunnable() {
                    @Override
    				public void run() {
    					IGDBProcesses procService = fTracker.getService(IGDBProcesses.class);
    					ICommandControlService commandControl = fTracker.getService(ICommandControlService.class);

    					if (procService != null && commandControl != null) {
    						IProcessDMContext procDmc = procService.createProcessContext(commandControl.getContext(), fPid);
                            procService.attachDebuggerToProcess(procDmc, finalBinaryPath, new DataRequestMonitor<IDMContext>(fExecutor, fRm) {
                                @Override
                                protected void handleSuccess() {
                                    // Store the path of the binary so we can use it again for another process
                                    // with the same name.  Only do this on success, to avoid being stuck with
                                    // a path that is invalid
                                    fProcessNameToBinaryMap.put(fProcName, finalBinaryPath);
                                    fRm.done();
                                };
                            });

    					} else {
    						fRm.setStatus(new Status(IStatus.ERROR, GdbUIPlugin.PLUGIN_ID, IDsfStatusConstants.INTERNAL_ERROR, "Cannot find services", null)); //$NON-NLS-1$
    						fRm.done();
    					}
    				}
    			});
    		}
			
    		return Status.OK_STATUS;
    	}
    }

    @Override
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
            @Override
    		public void run() {
    			final IProcesses procService = fTracker.getService(IProcesses.class);
    			ICommandControlService commandControl = fTracker.getService(ICommandControlService.class);

    			if (procService != null && commandControl != null) {
        			final ICommandControlDMContext controlCtx = commandControl.getContext();
        			procService.isDebugNewProcessSupported(controlCtx, new DataRequestMonitor<Boolean>(fExecutor, null) {
        			@Override	
        			protected void handleCompleted() {
        				final boolean newProcessSupported = isSuccess() && getData();
        				
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
													LaunchUIMessages.getString("ProcessPrompter.PromptJob"), newProcessSupported, procInfoList.toArray(new IProcessExtendedInfo[0]),   //$NON-NLS-1$
													new DataRequestMonitor<Object>(fExecutor, rm) {
														@Override
														protected void handleCancel() {
															rm.cancel();
															rm.done();
														}
														@Override
														protected void handleSuccess() {
															Object data = getData();
															if (data instanceof String) {
																// User wants to start a new process
																startNewProcess(controlCtx, (String)data, rm);
															} else if (data instanceof IProcessExtendedInfo[]) {
																attachToProcesses(controlCtx, (IProcessExtendedInfo[])data, rm);
															} else {
																rm.setStatus(new Status(IStatus.ERROR, GdbUIPlugin.PLUGIN_ID, IDsfStatusConstants.INTERNAL_ERROR, "Invalid return type for process prompter", null)); //$NON-NLS-1$
																rm.done();
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
        			}
        			});
    			} else {
    				rm.done();
    			}
    		}
    	});
    }
    
    private void startNewProcess(ICommandControlDMContext controlDmc, String binaryPath, RequestMonitor rm) {
		final IGDBProcesses procService = fTracker.getService(IGDBProcesses.class);
		procService.debugNewProcess(
				controlDmc, binaryPath, 
				new HashMap<String, Object>(), new DataRequestMonitor<IDMContext>(fExecutor, rm));
    }
    
    private void attachToProcesses(final ICommandControlDMContext controlDmc, IProcessExtendedInfo[] processes, final RequestMonitor rm) {

    	// For a local attach, GDB can figure out the binary automatically,
    	// so we don't need to prompt for it.
    	final IGDBProcesses procService = fTracker.getService(IGDBProcesses.class);
    	final IGDBBackend backend = fTracker.getService(IGDBBackend.class);

    	if (procService != null && backend != null) {
    		// Attach to each process in a sequential fashion.  We must do this
    		// to be able to check if we are allowed to attach to the next process.
    		// Attaching to all of them in parallel would assume that all attach are supported.

    		// Create a list of all our processes so we can attach to one at a time.
    		// We need to create a new list so that we can remove elements from it.
    		final List<IProcessExtendedInfo> procList = new ArrayList<IProcessExtendedInfo>(Arrays.asList(processes));

    		class AttachToProcessRequestMonitor extends ImmediateDataRequestMonitor<IDMContext> {
    			public AttachToProcessRequestMonitor() {
    				super();
    			}
    			
    			@Override
    			protected void handleCompleted() {
    				if (!isSuccess()) {
    					// Failed to attach to a process.  Just ignore it and move on.
    				}

    				// Check that we have a process to attach to
    				if (procList.size() > 0) {

    					// Check that we can actually attach to the process.
    					// This is because some backends may not support multi-process.
    					// If the backend does not support multi-process, we only attach to the first process.
    					procService.isDebuggerAttachSupported(controlDmc, new ImmediateDataRequestMonitor<Boolean>() {
    						@Override
    						protected void handleCompleted() {
    							if (isSuccess() && getData()) {
    								// Can attach to process

    								// Remove process from list and attach to it. 
    								IProcessExtendedInfo process = procList.remove(0);
    								String pidStr = Integer.toString(process.getPid());

    								if (backend.getSessionType() == SessionType.REMOTE) {
    									// For remote attach, we must set the binary first so we need to prompt the user.
    									
    									// If this is the very first attach of a remote session, check if the user
    									// specified the binary in the launch.  If so, let's add it to our map to 
    						    		// avoid having to prompt the user for that binary.
    						    		// This would be particularly annoying since we didn't use to have
    									// to do that before we supported multi-process.
    									// Must do this here to be in the executor
    									// Bug 350365
    									if (fProcessNameToBinaryMap.isEmpty()) {
   											IPath binaryPath = backend.getProgramPath();
   											if (binaryPath != null && !binaryPath.isEmpty()) {
   												fProcessNameToBinaryMap.put(binaryPath.lastSegment(), binaryPath.toOSString());
   											}
    									}
    									
    									// Because the prompt is a very long operation, we need to run outside the
    									// executor, so we don't lock it.
    									// Bug 344892
    									IPath processPath = new Path(process.getName());
    									String processShortName = processPath.lastSegment();
    									new PromptAndAttachToProcessJob(pidStr, 
    											                        LaunchUIMessages.getString("ProcessPrompterDialog.TitlePrefix") + process.getName(), //$NON-NLS-1$
    											                        processShortName, new AttachToProcessRequestMonitor()).schedule();
    								} else {
    									IProcessDMContext procDmc = procService.createProcessContext(controlDmc, pidStr);
    									procService.attachDebuggerToProcess(procDmc, null, new AttachToProcessRequestMonitor());
    								}
    							} else {
    								// Not allowed to attach to another process.  Just stop.
    								rm.done();
    							}
    						}
    					});
    				} else {
    					// No other process to attach to
    					rm.done();
    				}
    			}
    		};

    		// Trigger the first attach.
    		new AttachToProcessRequestMonitor().done();

    	} else {
    		rm.setStatus(new Status(IStatus.ERROR, GdbUIPlugin.PLUGIN_ID, IDsfStatusConstants.INTERNAL_ERROR, "Cannot find service", null)); //$NON-NLS-1$
    		rm.done();
    	}

    }
}



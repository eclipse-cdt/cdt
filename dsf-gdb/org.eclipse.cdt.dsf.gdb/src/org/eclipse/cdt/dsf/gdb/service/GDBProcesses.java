/*******************************************************************************
 * Copyright (c) 2008, 2011 Ericsson and others.
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
import org.eclipse.cdt.debug.core.CDebugUtils;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.concurrent.ImmediateExecutor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Sequence;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointsTargetDMContext;
import org.eclipse.cdt.dsf.debug.service.IMemory.IMemoryDMContext;
import org.eclipse.cdt.dsf.debug.service.IProcesses;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExitedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IStartedDMEvent;
import org.eclipse.cdt.dsf.debug.service.command.ICommand;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.cdt.dsf.gdb.IGdbDebugPreferenceConstants;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.gdb.launching.GDBProcess;
import org.eclipse.cdt.dsf.gdb.service.command.IGDBControl;
import org.eclipse.cdt.dsf.mi.service.IMICommandControl;
import org.eclipse.cdt.dsf.mi.service.IMIContainerDMContext;
import org.eclipse.cdt.dsf.mi.service.IMIExecutionDMContext;
import org.eclipse.cdt.dsf.mi.service.IMIProcessDMContext;
import org.eclipse.cdt.dsf.mi.service.IMIProcesses;
import org.eclipse.cdt.dsf.mi.service.MIBreakpointsManager;
import org.eclipse.cdt.dsf.mi.service.MIProcesses;
import org.eclipse.cdt.dsf.mi.service.command.CommandFactory;
import org.eclipse.cdt.dsf.mi.service.command.MIInferiorProcess;
import org.eclipse.cdt.dsf.mi.service.command.MIInferiorProcess.State;
import org.eclipse.cdt.dsf.mi.service.command.events.MIStoppedEvent;
import org.eclipse.cdt.dsf.mi.service.command.output.MIBreakInsertInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IProcess;
import org.osgi.framework.BundleContext;


public class GDBProcesses extends MIProcesses implements IGDBProcesses {
    
	private class GDBContainerDMC extends MIContainerDMC
	implements IMemoryDMContext 
	{
		public GDBContainerDMC(String sessionId, IProcessDMContext processDmc, String groupId) {
			super(sessionId, processDmc, groupId);
		}
	}
	
    private IGDBControl fGdb;
    private IGDBBackend fBackend;
    private CommandFactory fCommandFactory;
    
    // Indicates if we are currently connected to an inferior
    // We only need a boolean type since we only support single process debugging
    // in this version of the service
    private boolean fConnected;

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
    	fBackend = getServicesTracker().getService(IGDBBackend.class);
		fCommandFactory = getServicesTracker().getService(IMICommandControl.class).getCommandFactory();

		// Register this service.
		register(new String[] { IProcesses.class.getName(),
				IMIProcesses.class.getName(),
				IGDBProcesses.class.getName(),
				MIProcesses.class.getName(),
				GDBProcesses.class.getName() },
				new Hashtable<String, String>());
        
		getSession().addServiceEventListener(this, null);		

		requestMonitor.done();
	}

	@Override
	public void shutdown(RequestMonitor requestMonitor) {
		unregister();
		getSession().removeServiceEventListener(this);		
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
						name = fBackend.getProgramPath().lastSegment();
					}
				}
			}
			if (name == null) {
				// This could happen if a process has terminated but the 
				// debug session is not terminated because the preference
				// to keep GDB running has been selected.
				name = "Unknown name"; //$NON-NLS-1$

				// Until bug 305385 is fixed, the above code will not work, so we assume we
				// are looking for our own process
				name = fBackend.getProgramPath().lastSegment();
			}
		
			rm.setData(new MIThreadDMData(name, pidStr));
			rm.done();
		} else {
			super.getExecutionData(dmc, rm);
		}
	}

	@Override
	public void isDebuggerAttachSupported(IDMContext dmc, DataRequestMonitor<Boolean> rm) {
	    if (fBackend.getIsAttachSession() && !fConnected) {
	    	rm.setData(true);
	    } else {
	    	rm.setData(false);
	    }
		rm.done();
	}

	@Override
    public void attachDebuggerToProcess(final IProcessDMContext procCtx, final DataRequestMonitor<IDMContext> rm) {
		// For remote attach, we must set the binary first
		// For a local attach, GDB can figure out the binary automatically,
		// so we don't specify it.
		
		final IMIContainerDMContext containerDmc = createContainerContext(procCtx, MIProcesses.UNIQUE_GROUP_ID);

		DataRequestMonitor<MIInfo> attachRm = new DataRequestMonitor<MIInfo>(ImmediateExecutor.getInstance(), rm) {
			@Override
			protected void handleSuccess() {
				GDBProcesses.super.attachDebuggerToProcess(
						procCtx, 
						new DataRequestMonitor<IDMContext>(ImmediateExecutor.getInstance(), rm) {
							@Override
							protected void handleSuccess() {
								MIInferiorProcess inferiorProcess = fGdb.getInferiorProcess();
								if (inferiorProcess != null) {
									inferiorProcess.setContainerContext(containerDmc);
									inferiorProcess.setPid(((IMIProcessDMContext)procCtx).getProcId());
								}

								IDMContext containerDmc = getData();
								rm.setData(containerDmc);

								// Start tracking breakpoints.
								MIBreakpointsManager bpmService = getServicesTracker().getService(MIBreakpointsManager.class);
								IBreakpointsTargetDMContext bpTargetDmc = DMContexts.getAncestorOfType(containerDmc, IBreakpointsTargetDMContext.class);
								bpmService.startTrackingBreakpoints(bpTargetDmc, rm);
							}
						});
			}
		};
		
		if (fBackend.getSessionType() == SessionType.REMOTE) {
			final IPath execPath = fBackend.getProgramPath();
			if (execPath != null && !execPath.isEmpty()) {
				fGdb.queueCommand(
					fCommandFactory.createMIFileExecAndSymbols(containerDmc, execPath.toPortableString()), 
					attachRm);
				return;
			}
		}

		// If we get here, let's do the attach by completing the requestMonitor
		attachRm.done();
	}

	@Override
    public void canDetachDebuggerFromProcess(IDMContext dmc, DataRequestMonitor<Boolean> rm) {
	    if (fBackend.getIsAttachSession() && fConnected) {
	    	rm.setData(true);
	    } else {
	    	rm.setData(false);
	    }
    	rm.done();
    }

	@Override
    public void detachDebuggerFromProcess(IDMContext dmc, final RequestMonitor rm) {
		super.detachDebuggerFromProcess(
			dmc, 
			new RequestMonitor(getExecutor(), rm) {
				@Override
				protected void handleSuccess() {					
					MIInferiorProcess inferiorProcess = fGdb.getInferiorProcess();
				    if (inferiorProcess != null) {
				    	inferiorProcess.setPid(null);
				    }

					rm.done();
				}
			});
	}
	
	@Override
	public void debugNewProcess(IDMContext dmc, String file, 
			                    Map<String, Object> attributes, DataRequestMonitor<IDMContext> rm) {
		ImmediateExecutor.getInstance().execute(
				getDebugNewProcessSequence(getExecutor(), true, dmc, file, attributes, rm));
	}
	
	/**
	 * Return the sequence that is to be used to create a new process.
	 * Allows others to extend more easily.
	 * @since 4.0
	 */
	protected Sequence getDebugNewProcessSequence(DsfExecutor executor, boolean isInitial, IDMContext dmc, String file, 
												  Map<String, Object> attributes, DataRequestMonitor<IDMContext> rm) {
		return new DebugNewProcessSequence(executor, isInitial, dmc, file, attributes, rm);
	}
	
	@Override
	public void getProcessesBeingDebugged(IDMContext dmc, DataRequestMonitor<IDMContext[]> rm) {
        MIInferiorProcess inferiorProcess = fGdb.getInferiorProcess();
	    if (fConnected && 
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
		if (fBackend.getSessionType() == SessionType.LOCAL) {
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
    public void terminate(IThreadDMContext thread, final RequestMonitor rm) {
		// If we will terminate GDB as soon as the inferior terminates, then let's
		// just terminate GDB itself.  This is more robust since we actually monitor
		// the success of terminating GDB.
   		if (Platform.getPreferencesService().getBoolean("org.eclipse.cdt.dsf.gdb.ui",  //$NON-NLS-1$
				IGdbDebugPreferenceConstants.PREF_AUTO_TERMINATE_GDB,
				true, null)) {
			fGdb.terminate(new RequestMonitor(ImmediateExecutor.getInstance(), null));
		} else if (thread instanceof IMIProcessDMContext) {
			getDebuggingContext(
					thread, 
					new DataRequestMonitor<IDMContext>(ImmediateExecutor.getInstance(), rm) {
						@Override
						protected void handleSuccess() {
							if (getData() instanceof IMIContainerDMContext) {
								if (fGdb.getInferiorProcess().getState() == State.RUNNING) {
									fBackend.interrupt();
								}

								final IMIContainerDMContext container = (IMIContainerDMContext)getData();
								fGdb.queueCommand(
										fCommandFactory.createMIInterpreterExecConsoleKill(container),
										new DataRequestMonitor<MIInfo>(ImmediateExecutor.getInstance(), rm) {
											@Override
											protected void handleSuccess() {
												// Before GDB 7.0, we must send a container exited event ourselves
									            getSession().dispatchEvent(
									                       new ContainerExitedDMEvent(container), getProperties());

												rm.done();
											}
										});
							} else {
					            rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INTERNAL_ERROR, "Invalid process context.", null)); //$NON-NLS-1$
					            rm.done();								
							}
						}
					});         
	    } else {
            rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INTERNAL_ERROR, "Invalid process context.", null)); //$NON-NLS-1$
            rm.done();
	    }
	}
	
    /** @since 4.0 */
    public IMIExecutionDMContext[] getExecutionContexts(IMIContainerDMContext containerDmc) {
    	assert false; // This is not being used before GDB 7.0
    	return null;
    }
    
	/** @since 4.0 */
	public void canRestart(IContainerDMContext containerDmc, DataRequestMonitor<Boolean> rm) {		
    	if (fBackend.getIsAttachSession() || fBackend.getSessionType() == SessionType.CORE) {
        	rm.setData(false);
        	rm.done();
        	return;
    	}
    	
    	// Before GDB6.8, the Linux gdbserver would restart a new
    	// process when getting a -exec-run but the communication
    	// with GDB had a bug and everything hung.
    	// with GDB6.8 the program restarts properly one time,
    	// but on a second attempt, gdbserver crashes.
    	// So, lets just turn off the Restart for Remote debugging
    	if (fBackend.getSessionType() == SessionType.REMOTE) {
        	rm.setData(false);
        	rm.done();
        	return;
    	}
    	
    	rm.setData(true);
    	rm.done();
	}
	
	/** @since 4.0 */
	public void restart(IContainerDMContext containerDmc, Map<String, Object> attributes, DataRequestMonitor<IContainerDMContext> rm) {
		startOrRestart(containerDmc, attributes, true, rm);
	}
	
	/** @since 4.0 */
	public void start(IContainerDMContext containerDmc, Map<String, Object> attributes, DataRequestMonitor<IContainerDMContext> rm) {
		startOrRestart(containerDmc, attributes, false, rm);
	}
	
	/**
	 * @since 4.0
	 */
	protected void createConsole(final IContainerDMContext containerDmc, final boolean restart, final RequestMonitor rm) {
		fGdb.initInferiorInputOutput(new RequestMonitor(ImmediateExecutor.getInstance(), rm) {
			@Override
			protected void handleSuccess() {
				fGdb.createInferiorProcess();				
				final MIInferiorProcess inferior = fGdb.getInferiorProcess();
				inferior.setContainerContext(containerDmc);

				final String label = fBackend.getProgramPath().lastSegment();
				final ILaunch launch = (ILaunch)getSession().getModelAdapter(ILaunch.class);

				// Add the inferior to the launch.
				// This cannot be done on the executor or things deadlock.
				DebugPlugin.getDefault().asyncExec(new Runnable() {
					public void run() {
						if (restart) {
							// For a restart, remove the old inferior
							IProcess[] launchProcesses = launch.getProcesses();
							for (IProcess p : launchProcesses) {
								// We know there is only one inferior, so just find it.
								if ((p instanceof GDBProcess) == false) {
									launch.removeProcess(p);
									break;
								}
							}
						}

						// Add the inferior
						IProcess process = DebugPlugin.newProcess(launch, inferior, label);

						// Register as an IProcess so that the console is brought to the front
						// when the inferior is selected
						getSession().registerModelAdapter(IProcess.class, process);
						rm.done();
					}
				});
			}
		});
	}

    /**
     * Insert breakpoint at entry if set, and start or restart the program.
     *
     * @since 4.0 
     */
    protected void startOrRestart(final IContainerDMContext containerDmc, final Map<String, Object> attributes,
    		                      boolean restart, final DataRequestMonitor<IContainerDMContext> requestMonitor) {
    	if (fBackend.getIsAttachSession()) {
    		// When attaching to a running process, we do not need to set a breakpoint or
    		// start the program; it is left up to the user.
    		requestMonitor.setData(containerDmc);
    		requestMonitor.done();
    		return;
    	}
    	
    	createConsole(containerDmc, restart, new RequestMonitor(ImmediateExecutor.getInstance(), requestMonitor) {
    		@Override
    		protected void handleSuccess() {

    			final DataRequestMonitor<MIInfo> execMonitor = new DataRequestMonitor<MIInfo>(getExecutor(), requestMonitor) {
    				@Override
    				protected void handleSuccess() {
    					if (fBackend.getSessionType() != SessionType.REMOTE) {
    						// Don't send the ContainerStarted event for a remote session because
    						// it has already been done by MIRunControlEventProcessor when receiving
    						// the ^connect
    						getSession().dispatchEvent(new ContainerStartedDMEvent(containerDmc), getProperties());
    					}
    					requestMonitor.setData(containerDmc);
    					requestMonitor.done();
    				}
    			};

    			final ICommand<MIInfo> execCommand;
    			if (useContinueCommand()) {
    				execCommand = fCommandFactory.createMIExecContinue(containerDmc);
    			} else {
    				execCommand = fCommandFactory.createMIExecRun(containerDmc);	
    			}

    			boolean stopInMain = CDebugUtils.getAttribute(attributes, 
    					ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_STOP_AT_MAIN,
    					false);

    			if (!stopInMain) {
    				// Just start the program.
    				fGdb.queueCommand(execCommand, execMonitor);
    			} else {
    				String stopSymbol = CDebugUtils.getAttribute(attributes, 
    						ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_STOP_AT_MAIN_SYMBOL,
    						ICDTLaunchConfigurationConstants.DEBUGGER_STOP_AT_MAIN_SYMBOL_DEFAULT);

    				// Insert a breakpoint at the requested stop symbol.
    				IBreakpointsTargetDMContext bpTarget = DMContexts.getAncestorOfType(containerDmc, IBreakpointsTargetDMContext.class);
    				fGdb.queueCommand(
    						fCommandFactory.createMIBreakInsert(bpTarget, true, false, null, 0, stopSymbol, 0), 
    						new DataRequestMonitor<MIBreakInsertInfo>(getExecutor(), requestMonitor) { 
    							@Override
    							protected void handleSuccess() {
    								// After the break-insert is done, execute the -exec-run or -exec-continue command.
    								fGdb.queueCommand(execCommand, execMonitor);
    							}
    						});
    			}
    		}
    	});
    }

    /**
     * This method indicates if we should use the -exec-continue command
     * instead of the -exec-run command.
     * This method can be overridden to allow for customization.
     * @since 4.0
     */
    protected boolean useContinueCommand() {
    	// When doing remote debugging, we use -exec-continue instead of -exec-run
    	// Restart does not apply to remote sessions
    	return fBackend.getSessionType() == SessionType.REMOTE;
    }

    @Override
    @DsfServiceEventHandler
    public void eventDispatched(IStartedDMEvent e) {
    	if (e.getDMContext() instanceof IContainerDMContext) {
    		fConnected = true;
    	}
    	super.eventDispatched(e);
	}

    @Override
    @DsfServiceEventHandler
    public void eventDispatched(IExitedDMEvent e) {
    	if (e.getDMContext() instanceof IContainerDMContext) {
    		fConnected = false;
    		
    		if (Platform.getPreferencesService().getBoolean("org.eclipse.cdt.dsf.gdb.ui",  //$NON-NLS-1$
    				IGdbDebugPreferenceConstants.PREF_AUTO_TERMINATE_GDB,
    				true, null)) {
    			// If the inferior finishes, let's terminate GDB
    			fGdb.terminate(new RequestMonitor(ImmediateExecutor.getInstance(), null));
    		}
    	}
    	super.eventDispatched(e);
    }
    
    /**
	 * @since 3.0
	 */
    @DsfServiceEventHandler
    public void eventDispatched(MIStoppedEvent e) {

// Post-poned because 'info program' yields different result on different platforms.
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=305385#c20
//
//    	// Get the PID of the inferior through gdb (if we don't have it already) 
//    	
//    	
//    	fGdb.getInferiorProcess().update();
    	
    }
}

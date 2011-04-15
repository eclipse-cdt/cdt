/*******************************************************************************
 * Copyright (c) 2010, 2011 TUBITAK BILGEM-ITI and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Onur Akdemir (TUBITAK BILGEM-ITI) - Multi-process debugging (Bug 237306)
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.service;

import java.util.Map;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.concurrent.ImmediateExecutor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Sequence;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointsTargetDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExitedDMEvent;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.cdt.dsf.gdb.IGDBLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.gdb.service.command.IGDBControl;
import org.eclipse.cdt.dsf.mi.service.IMICommandControl;
import org.eclipse.cdt.dsf.mi.service.IMIContainerDMContext;
import org.eclipse.cdt.dsf.mi.service.IMIProcessDMContext;
import org.eclipse.cdt.dsf.mi.service.IMIRunControl;
import org.eclipse.cdt.dsf.mi.service.IMIRunControl.MIRunMode;
import org.eclipse.cdt.dsf.mi.service.MIBreakpointsManager;
import org.eclipse.cdt.dsf.mi.service.MIProcesses;
import org.eclipse.cdt.dsf.mi.service.command.CommandFactory;
import org.eclipse.cdt.dsf.mi.service.command.output.MIAddInferiorInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;

/**
 * Adding support for multi-process with GDB 7.2
 * 
 * @since 4.0
 */
public class GDBProcesses_7_2 extends GDBProcesses_7_1 {
    
    private CommandFactory fCommandFactory;
    private IGDBControl fCommandControl;
    private IGDBBackend fBackend;

	public GDBProcesses_7_2(DsfSession session) {
		super(session);
	}

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
		fCommandControl = getServicesTracker().getService(IGDBControl.class);
        fCommandFactory = getServicesTracker().getService(IMICommandControl.class).getCommandFactory();
    	fBackend = getServicesTracker().getService(IGDBBackend.class);
    	
    	requestMonitor.done();
	}

	@Override
	public void shutdown(RequestMonitor requestMonitor) {
		super.shutdown(requestMonitor);
	}
	
	@Override
    public IMIContainerDMContext createContainerContextFromGroupId(ICommandControlDMContext controlDmc, String groupId) {
    	String pid = getGroupToPidMap().get(groupId);
    	if (pid == null) {
    		// For GDB 7.2, the groupId is no longer the pid, so use our wildcard pid instead
    		pid = MIProcesses.UNKNOWN_PROCESS_ID;
    	}
    	IProcessDMContext processDmc = createProcessContext(controlDmc, pid);
    	return createContainerContext(processDmc, groupId);
    }
    
    @Override
	protected boolean doIsDebuggerAttachSupported() {
		// Multi-process is not applicable to post-mortem sessions (core)
		// or to non-attach remote sessions.
		if (fBackend.getSessionType() == SessionType.CORE) {
			return false;
		}

		if (fBackend.getSessionType() == SessionType.REMOTE && !fBackend.getIsAttachSession()) {
			return false;
		}

		return true;
	}

	@Override
    public void attachDebuggerToProcess(IProcessDMContext procCtx, DataRequestMonitor<IDMContext> rm) {
		attachDebuggerToProcess(procCtx, null, rm);
	}
	
    /**
	 * @since 4.0
	 */
	@Override
	public void attachDebuggerToProcess(final IProcessDMContext procCtx, final String binaryPath, final DataRequestMonitor<IDMContext> dataRm) {
		if (procCtx instanceof IMIProcessDMContext) {
	    	if (!doIsDebuggerAttachSupported()) {
	            dataRm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INTERNAL_ERROR, "Attach not supported.", null)); //$NON-NLS-1$
	            dataRm.done();    		
	    		return;
	    	}
	    	
	    	// Use a sequence for better control of each step
	    	ImmediateExecutor.getInstance().execute(new Sequence(getExecutor(), dataRm) {
	    		private IMIContainerDMContext fContainerDmc;

		        private Step[] steps = new Step[] {
		        		// If this is not the very first inferior, we first need create the new inferior
		                new Step() { 
		                    @Override
		                    public void execute(final RequestMonitor rm) {
		                    	if (isInitialProcess()) {
		                    		// If it is the first inferior, GDB has already created it for us
		                    		// We really should get the id from GDB instead of hard-coding it
		                    		setIsInitialProcess(false);
		        					fContainerDmc = createContainerContext(procCtx, "i1"); //$NON-NLS-1$
		                    		rm.done();
		                    		return;
		                    	}
		                    	
		            	    	ICommandControlDMContext controlDmc = DMContexts.getAncestorOfType(procCtx, ICommandControlDMContext.class);
		            	        fCommandControl.queueCommand(
		            	        		fCommandFactory.createMIAddInferior(controlDmc),
		            	        		new DataRequestMonitor<MIAddInferiorInfo>(ImmediateExecutor.getInstance(), rm) {
		            	        			@Override
		            	        			protected void handleSuccess() {
		            	        				final String groupId = getData().getGroupId();
		            	        				if (groupId == null || groupId.trim().length() == 0) {
		            	        					rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INTERNAL_ERROR, "Invalid gdb group id.", null)); //$NON-NLS-1$
		            	        				} else {
		            	        					fContainerDmc = createContainerContext(procCtx, groupId);
		            	        				}
		            	        				rm.done();
		            	        			}
		            	        		});
		                    }
		                },
	    				new Step() { 
	    					@Override
	    					public void execute(RequestMonitor rm) {
	    						if (binaryPath != null) {
    				    			fCommandControl.queueCommand(
    				    					fCommandFactory.createMIFileExecAndSymbols(fContainerDmc, binaryPath), 
			    							new DataRequestMonitor<MIInfo>(ImmediateExecutor.getInstance(), rm));
    				    			return;
	    						}

	    				    	rm.done();
	    					}
	    				},		                
		                // Now, actually do the attach
		                new Step() { 
		                    @Override
		                    public void execute(RequestMonitor rm) {
	    						// For non-stop mode, we do a non-interrupting attach
	    						// Bug 333284
	    						boolean shouldInterrupt = true;
								IMIRunControl runControl = getServicesTracker().getService(IMIRunControl.class);
								if (runControl != null && runControl.getRunMode() == MIRunMode.NON_STOP) {
									shouldInterrupt = false;
								}

	    						fCommandControl.queueCommand(
	    								fCommandFactory.createMITargetAttach(fContainerDmc, ((IMIProcessDMContext)procCtx).getProcId(), shouldInterrupt),
		    							new DataRequestMonitor<MIInfo>(ImmediateExecutor.getInstance(), rm));
		                    }
		                },
                    	// Start tracking this process' breakpoints.
		                new Step() { 
		                    @Override
		                    public void execute(RequestMonitor rm) {
		                    	MIBreakpointsManager bpmService = getServicesTracker().getService(MIBreakpointsManager.class);
		                    	IBreakpointsTargetDMContext bpTargetDmc = DMContexts.getAncestorOfType(fContainerDmc, IBreakpointsTargetDMContext.class);
		                    	bpmService.startTrackingBreakpoints(bpTargetDmc, rm);
		                    }
		                },
		                // Turn on reverse debugging if it was enabled as a launch option
		                new Step() { 
		                    @Override
		                    public void execute(RequestMonitor rm) {								
								IReverseRunControl reverseService = getServicesTracker().getService(IReverseRunControl.class);
								if (reverseService != null) {
									ILaunch launch = (ILaunch)procCtx.getAdapter(ILaunch.class);
									if (launch != null) {
										try {
											boolean reverseEnabled = 
												launch.getLaunchConfiguration().getAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_REVERSE,
														                                     IGDBLaunchConfigurationConstants.DEBUGGER_REVERSE_DEFAULT);
											if (reverseEnabled) {
												reverseService.enableReverseMode(fCommandControl.getContext(), true, rm);
												return;
											}
										} catch (CoreException e) {
											// Ignore, just don't set reverse
										}
									}
								}
								rm.done();
		                    }
		                },
                    	// Store the fully formed container context so it can be returned to the caller.
		                new Step() { 
		                    @Override
		                    public void execute(RequestMonitor rm) {
								dataRm.setData(fContainerDmc);
								
								rm.done();
		                    }
		                },
		    	};
		        
	    		@Override public Step[] getSteps() { return steps; }
	    	});
	    } else {
            dataRm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INTERNAL_ERROR, "Invalid process context.", null)); //$NON-NLS-1$
            dataRm.done();
	    }
	}
	
	@Override
    public void detachDebuggerFromProcess(IDMContext dmc, final RequestMonitor rm) {
    	
		ICommandControlDMContext controlDmc = DMContexts.getAncestorOfType(dmc, ICommandControlDMContext.class);
		final IMIContainerDMContext containerDmc = DMContexts.getAncestorOfType(dmc, IMIContainerDMContext.class);
		
    	if (controlDmc != null && containerDmc != null) {
        	if (!doCanDetachDebuggerFromProcess()) {
                rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INTERNAL_ERROR, "Detach not supported.", null)); //$NON-NLS-1$
                rm.done();
                return;
        	}

			IMIRunControl runControl = getServicesTracker().getService(IMIRunControl.class);
			if (runControl != null && !runControl.isTargetAcceptingCommands()) {
				fBackend.interrupt();
			}

        	fCommandControl.queueCommand(
        			fCommandFactory.createMITargetDetach(controlDmc, containerDmc.getGroupId()),
    				new DataRequestMonitor<MIInfo>(getExecutor(), rm) {
    					@Override
    					protected void handleCompleted() {
    						if (isSuccess()) {
    						// Bug in GDB 7.2 where removing an inferior will lead to a crash when running other processes.
    						// I'm hoping it will be fixed in 7.2.1
//    			        	fCommandControl.queueCommand(
//    			        			fCommandFactory.createMIRemoveInferior(fCommandControl.getContext(), containerDmc.getGroupId()),
//    			    				new DataRequestMonitor<MIInfo>(getExecutor(), rm));
    						} else {
    							// This command fails with GDB 7.2 because of a GDB bug, which was fixed with GDB 7.2.1
    							// In case we get here, we assume we are using GDB 7.2 (although we should not) and we work
    							// around it.
    							// Also, with GDB 7.2, removing the inferior does not work because of another bug, so we just don't do it.
    					       	fCommandControl.queueCommand(
    				        			fCommandFactory.createMITargetDetach(containerDmc),
    				    				new DataRequestMonitor<MIInfo>(getExecutor(), rm));
    						}
    					}
    				});
    	} else {
            rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INTERNAL_ERROR, "Invalid context.", null)); //$NON-NLS-1$
            rm.done();
	    }
	}
	
	@Override
	protected boolean doIsDebugNewProcessSupported() {
		// Multi-process is not applicable to post-mortem sessions (core)
		// or to non-attach remote sessions.
		SessionType type = fBackend.getSessionType();
		
		if (type == SessionType.CORE) {
			return false;
		}

		if (type == SessionType.REMOTE && !fBackend.getIsAttachSession()) {
			return false;
		}

		return true;
	}

	@Override
	protected Sequence getDebugNewProcessSequence(DsfExecutor executor, boolean isInitial, IDMContext dmc, String file, 
												  Map<String, Object> attributes, DataRequestMonitor<IDMContext> rm) {
		return new DebugNewProcessSequence_7_2(executor, isInitial, dmc, file, attributes, rm);
	}
	
    /**
     * @since 4.0
      */
    @DsfServiceEventHandler
    @Override
    public void eventDispatched(IExitedDMEvent e) {
    	IDMContext dmc = e.getDMContext();
    	if (dmc instanceof IContainerDMContext) {
    		// A process has died, we should stop tracking its breakpoints
    		if (fBackend.getSessionType() != SessionType.CORE) {
    			IBreakpointsTargetDMContext bpTargetDmc = DMContexts.getAncestorOfType(dmc, IBreakpointsTargetDMContext.class);
            	MIBreakpointsManager bpmService = getServicesTracker().getService(MIBreakpointsManager.class);
            	if (bpmService != null) {
            		bpmService.stopTrackingBreakpoints(bpTargetDmc, new RequestMonitor(ImmediateExecutor.getInstance(), null));
            	}
    		}
    	}
    	
    	super.eventDispatched(e);
    }
}


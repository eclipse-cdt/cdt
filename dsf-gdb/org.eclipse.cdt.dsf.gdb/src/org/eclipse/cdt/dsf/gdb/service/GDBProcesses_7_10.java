/*******************************************************************************
 * Copyright (c) 2015, 2016 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Intel Corporation - Added Reverse Debugging BTrace support
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.service;

import java.util.Hashtable;
import java.util.Map;

import org.eclipse.cdt.debug.core.model.IChangeReverseMethodHandler.ReverseTraceMethod;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.concurrent.ImmediateDataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateExecutor;
import org.eclipse.cdt.dsf.concurrent.ImmediateRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Sequence;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IMultiDetach;
import org.eclipse.cdt.dsf.debug.service.IMultiTerminate;
import org.eclipse.cdt.dsf.debug.service.IMemory.IMemoryDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.cdt.dsf.gdb.IGDBLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.gdb.IGdbDebugPreferenceConstants;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.gdb.service.command.IGDBControl;
import org.eclipse.cdt.dsf.mi.service.IMICommandControl;
import org.eclipse.cdt.dsf.mi.service.IMIContainerDMContext;
import org.eclipse.cdt.dsf.mi.service.IMIProcessDMContext;
import org.eclipse.cdt.dsf.mi.service.IMIRunControl;
import org.eclipse.cdt.dsf.mi.service.IMIRunControl.MIRunMode;
import org.eclipse.cdt.dsf.mi.service.MIBreakpointsManager;
import org.eclipse.cdt.dsf.mi.service.command.CommandFactory;
import org.eclipse.cdt.dsf.mi.service.command.output.CLIShowRemotePacketInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIAddInferiorInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;

import com.ibm.icu.text.MessageFormat;

/**
 * Adding support for reverse trace method selection with GDB 7.10
 *
 * @since 5.0
 */
public class GDBProcesses_7_10 extends GDBProcesses_7_4 {

    private CommandFactory fCommandFactory;
    private IGDBControl fCommandControl;
	private IGDBBackend fBackend;

    public GDBProcesses_7_10(DsfSession session) {
        super(session);
    }
    
	@Override
	public void initialize(final RequestMonitor requestMonitor) {
		super.initialize(new ImmediateRequestMonitor(requestMonitor) {
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
        register(new String[]{ IMultiDetach.class.getName(), IMultiTerminate.class.getName() }, new Hashtable<String,String>());

		fCommandControl = getServicesTracker().getService(IGDBControl.class);
        fCommandFactory = getServicesTracker().getService(IMICommandControl.class).getCommandFactory();
    	fBackend = getServicesTracker().getService(IGDBBackend.class);
    	
    	requestMonitor.done();
	}
    @Override
    public void attachDebuggerToProcess(IProcessDMContext procCtx, DataRequestMonitor<IDMContext> rm) {
        attachDebuggerToProcess(procCtx, null, rm);
    }

    @Override
    protected Sequence getStartOrRestartProcessSequence(DsfExecutor executor, IContainerDMContext containerDmc,
            Map<String, Object> attributes, boolean restart,
            DataRequestMonitor<IContainerDMContext> rm) {
        return new StartOrRestartProcessSequence_7_10(executor, containerDmc, attributes, restart, rm);
    }

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
                		// first check if requested process is already targeted
						new Step() {
							@Override
							public void execute(final RequestMonitor rm) {
								getProcessesBeingDebugged(procCtx, new ImmediateDataRequestMonitor<IDMContext[]>(rm) {
									@Override
									protected void handleSuccess() {
										assert getData() != null;

										boolean found = false;
										for (IDMContext dmc : getData()) {
											IProcessDMContext procDmc = DMContexts.getAncestorOfType(dmc,
													IProcessDMContext.class);
											if (procCtx.equals(procDmc)) {
												found = true;
											}
										}
										if (found) {
											// abort the sequence
											Status failedStatus = new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID,
													REQUEST_FAILED,
													MessageFormat.format(Messages.Already_connected_process_err,
															((IMIProcessDMContext) procCtx).getProcId()),
													null);
											rm.done(failedStatus);
											return;
										}
										super.handleSuccess();
									}
								});
							}
						},
                        // If this is not the very first inferior, we first need create the new inferior
                        new Step() {
                            @Override
                            public void execute(final RequestMonitor rm) {
                                if (isInitialProcess()) {
                                    // If it is the first inferior, GDB has already created it for us
                                    // We really should get the id from GDB instead of hard-coding it
                                    fContainerDmc = createContainerContext(procCtx, "i1"); //$NON-NLS-1$
                                    rm.done();
                                    return;
                                }

                                ICommandControlDMContext controlDmc = DMContexts.getAncestorOfType(procCtx, ICommandControlDMContext.class);
                                fCommandControl.queueCommand(
                                        fCommandFactory.createMIAddInferior(controlDmc),
                                        new ImmediateDataRequestMonitor<MIAddInferiorInfo>(rm) {
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
                            public void execute(final RequestMonitor rm) {
                                // Now, set the binary to be used.
                                if (binaryPath != null) {
                                    fCommandControl.queueCommand(
                                            fCommandFactory.createMIFileExecAndSymbols(fContainerDmc, binaryPath),
                                            new ImmediateDataRequestMonitor<MIInfo>(rm) {
                                                @Override
                                                protected void handleCompleted() {
                                                    super.handleCompleted();
                                                };
                                            });
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
                                        new ImmediateDataRequestMonitor<MIInfo>(rm));
                            }
                        },
                        // Initialize memory data for this process.
                        new Step() {
                            @Override
                            public void execute(RequestMonitor rm) {
                                IGDBMemory memory = getServicesTracker().getService(IGDBMemory.class);
                                IMemoryDMContext memContext = DMContexts.getAncestorOfType(fContainerDmc, IMemoryDMContext.class);
                                if (memory == null || memContext == null) {
                                    rm.done();
                                    return;
                                }
                                memory.initializeMemoryData(memContext, rm);
                            }
                        },
                        // Start tracking this process' breakpoints.
                        new Step() {
                            @Override
                            public void execute(RequestMonitor rm) {
                                MIBreakpointsManager bpmService = getServicesTracker().getService(MIBreakpointsManager.class);
                                bpmService.startTrackingBpForProcess(fContainerDmc, rm);
                            }
                        },
                        // Select reverse debugging mode to what was enabled as a launch option
                        new Step() {
                            @Override
                            public void execute(RequestMonitor rm) {
                                IReverseRunControl2 reverseService = getServicesTracker().getService(IReverseRunControl2.class);
                                if (reverseService != null) {
                                    ILaunch launch = procCtx.getAdapter(ILaunch.class);
                                    if (launch != null) {
                                        try {
                                            String reverseMode =
                                                launch.getLaunchConfiguration().getAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_REVERSE_MODE,
                                                                                             IGDBLaunchConfigurationConstants.DEBUGGER_REVERSE_MODE_DEFAULT);
                                            if (reverseMode.equals(IGDBLaunchConfigurationConstants.DEBUGGER_REVERSE_MODE_SOFTWARE)) {
                                                reverseService.enableReverseMode(fCommandControl.getContext(), ReverseTraceMethod.FULL_TRACE, rm);
                                                return;
                                            }
                                            else if (reverseMode.equals(IGDBLaunchConfigurationConstants.DEBUGGER_REVERSE_MODE_HARDWARE)) {
                                            	if (Platform.getPreferencesService().getString(GdbPlugin.PLUGIN_ID,
                                                        IGdbDebugPreferenceConstants.PREF_REVERSE_TRACE_METHOD_HARDWARE,
                                                        IGdbDebugPreferenceConstants.PREF_REVERSE_TRACE_METHOD_GDB_TRACE,
                                                        null).equals(IGdbDebugPreferenceConstants.PREF_REVERSE_TRACE_METHOD_BRANCH_TRACE)) {
                                            		reverseService.enableReverseMode(fCommandControl.getContext(), ReverseTraceMethod.BRANCH_TRACE, rm); // Branch Trace
                                                } else if (Platform.getPreferencesService().getString(GdbPlugin.PLUGIN_ID,
                                                        IGdbDebugPreferenceConstants.PREF_REVERSE_TRACE_METHOD_HARDWARE,
                                                        IGdbDebugPreferenceConstants.PREF_REVERSE_TRACE_METHOD_GDB_TRACE,
                                                        null).equals(IGdbDebugPreferenceConstants.PREF_REVERSE_TRACE_METHOD_PROCESSOR_TRACE)) {
                                                	reverseService.enableReverseMode(fCommandControl.getContext(), ReverseTraceMethod.PROCESSOR_TRACE, rm); // Processor Trace
                                                } else {
                                                	reverseService.enableReverseMode(fCommandControl.getContext(), ReverseTraceMethod.GDB_TRACE, rm); // GDB Selected Option
                                                }
                                                return;
                                            }
                                            else {
                                                rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INTERNAL_ERROR, "Invalid Trace Method Selected", null)); //$NON-NLS-1$
                                                rm.done();
                                                return;
                                            }
                                        } catch (CoreException e) {
                                            // Ignore, just don't set reverse method
                                        }
                                    }
                                }
                                rm.done();
                            }
                        },
                        // Store the fully formed container context so it can be returned to the caller
                        // and mark that we are not dealing with the first process anymore.
                        new Step() {
                            @Override
                            public void execute(RequestMonitor rm) {
                                dataRm.setData(fContainerDmc);
                                setIsInitialProcess(false);

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
	public void canAttachWithoutBinary(final IDMContext dmc, final DataRequestMonitor<Boolean> drm) {
		if (fBackend.getSessionType() == SessionType.LOCAL) {
			// do not need to check for remote capabilities. Always supported
			drm.done(Boolean.TRUE);
			return;
		}
		
		fCommandControl.queueCommand(fCommandFactory.createCLIShowRemotePacket(dmc, "pid-to-exec-file-packet"), //$NON-NLS-1$
				new DataRequestMonitor<CLIShowRemotePacketInfo>(ImmediateExecutor.getInstance(), drm) {
					@Override
					protected void handleSuccess() {
						drm.setData(Boolean.valueOf(CLIShowRemotePacketInfo.State.ENABLED == getData().getPacketState()));
						drm.done();
					}
				});
	}
}


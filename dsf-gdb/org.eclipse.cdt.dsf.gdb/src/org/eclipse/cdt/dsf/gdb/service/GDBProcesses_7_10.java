/*******************************************************************************
 * Copyright (c) 2015 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Intel Corporation - Added Reverse Debugging BTrace support
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.service;

import java.util.Map;

import org.eclipse.cdt.debug.internal.ui.preferences.ICDebugPreferenceConstants;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.concurrent.ImmediateDataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateExecutor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Sequence;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IMemory.IMemoryDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.cdt.dsf.gdb.IGDBLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.mi.service.IMIContainerDMContext;
import org.eclipse.cdt.dsf.mi.service.IMIProcessDMContext;
import org.eclipse.cdt.dsf.mi.service.IMIRunControl;
import org.eclipse.cdt.dsf.mi.service.IMIRunControl.MIRunMode;
import org.eclipse.cdt.dsf.mi.service.MIBreakpointsManager;
import org.eclipse.cdt.dsf.mi.service.command.output.MIAddInferiorInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;

/**
 * Adding support for reverse trace method selection with GDB 7.10
 *
 * @since 4.8
 */
@SuppressWarnings("restriction")
public class GDBProcesses_7_10 extends GDBProcesses_7_4 {

    public GDBProcesses_7_10(DsfSession session) {
        super(session);
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
                                // Because of a GDB 7.2 bug, for remote-attach sessions,
                                // we need to be disconnected from the target
                                // when we set the very first binary to be used.
                                // So, lets disconnect.
                                // Bug 352998
                                if (needFixForGDB72Bug352998()) {
                                    // The bug only applies to remote sessions
                                    if (fBackend.getSessionType() == SessionType.REMOTE) {
                                        assert fBackend.getIsAttachSession();
                                        assert binaryPath != null;

                                        // We only need the workaround for the very first process we attach to
                                        if (isInitialProcess()) {
                                            ICommandControlDMContext controlDmc = DMContexts.getAncestorOfType(procCtx, ICommandControlDMContext.class);
                                            fCommandControl.queueCommand(
                                                    fCommandFactory.createMITargetDisconnect(controlDmc),
                                                    new ImmediateDataRequestMonitor<MIInfo>(rm) {
                                                        @Override
                                                        protected void handleSuccess() {
                                                            fNeedToReconnect = true;
                                                            rm.done();
                                                        }
                                                    });
                                            return;
                                        }
                                    }
                                }

                                rm.done();
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
                                                    // Because of a GDB 7.2 bug, for remote-attach sessions,
                                                    // we need to be disconnected from the target
                                                    // when we set the very first binary to be used.
                                                    // Now that we have disconnected and set the binary,
                                                    // we may need to reconnect to the target.
                                                    // If we were unable to set the binary (e.g., if the specified path
                                                    // is invalid) we also need to reconnect to the target before
                                                    // aborting the rest of the sequence.
                                                    // Bug 352998

                                                    if (fNeedToReconnect) {
                                                        fNeedToReconnect = false;

                                                        // Set the status in case it is an error, so that when rm.done() is automatically
                                                        // called, we continue to abort the sequence if we are dealing with a failure.
                                                        rm.setStatus(getStatus());

                                                        connectToTarget(procCtx, rm);
                                                    } else {
                                                        super.handleCompleted();
                                                    }
                                                };
                                            });
                                    return;
                                }

                                assert fNeedToReconnect == false;
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
                                            if (reverseMode.equals(IGDBLaunchConfigurationConstants.DEBUGGER_REVERSE_MODE_SOFT)) {
                                                reverseService.enableReverseMode(fCommandControl.getContext(), 0, rm);
                                                return;
                                            }
                                            else if (reverseMode.equals(IGDBLaunchConfigurationConstants.DEBUGGER_REVERSE_MODE_HARD)) {
                                                    if(CDebugUIPlugin.getDefault().getPreferenceStore().getString(
                                                            ICDebugPreferenceConstants.PREF_REVERSE_TRACE_METHOD_HARDWARE).equals("UseBranchTrace")) //$NON-NLS-1$
                                                    reverseService.enableReverseMode(fCommandControl.getContext(), 1, rm);
                                                    else if(CDebugUIPlugin.getDefault().getPreferenceStore().getString(
                                                            ICDebugPreferenceConstants.PREF_REVERSE_TRACE_METHOD_HARDWARE).equals("UseProcessorTrace")) //$NON-NLS-1$
                                                    reverseService.enableReverseMode(fCommandControl.getContext(), 2, rm);
                                                    else
                                                    reverseService.enableReverseMode(fCommandControl.getContext(), 3, rm);
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
}


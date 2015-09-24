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

import org.eclipse.cdt.debug.core.CDebugUtils;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.cdt.debug.ui.ReverseTraceMethod;
import org.eclipse.cdt.debug.internal.ui.preferences.ICDebugPreferenceConstants;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.gdb.IGDBLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * @since 4.8
 */
@SuppressWarnings("restriction")
public class StartOrRestartProcessSequence_7_10 extends StartOrRestartProcessSequence_7_3 {

    protected IReverseRunControl2 fReverseService = null;
    protected ReverseTraceMethod fReverseMode = ReverseTraceMethod.FULL_TRACE;
    public StartOrRestartProcessSequence_7_10(DsfExecutor executor, IContainerDMContext containerDmc,
            Map<String, Object> attributes, boolean restart, DataRequestMonitor<IContainerDMContext> rm) {
        super(executor, containerDmc, attributes, restart, rm);
    }

    @Override
    protected String[] getExecutionOrder(String group) {
        if (GROUP_TOP_LEVEL.equals(group)) {
            return new String[] {
                    "stepInitializeBaseSequence",  //$NON-NLS-1$
                    "stepInsertStopOnMainBreakpoint",  //$NON-NLS-1$
                    "stepSetBreakpointForReverse",   //$NON-NLS-1$
                    "stepInitializeInputOutput",   //$NON-NLS-1$
                    "stepCreateConsole",    //$NON-NLS-1$
                    "stepRunProgram",   //$NON-NLS-1$
                    "stepSetReverseOff",   //$NON-NLS-1$
                    "stepSetReverseMode", //$NON-NLS-1$
                    "stepEnableReverse",   //$NON-NLS-1$
                    "stepContinue",   //$NON-NLS-1$
                    "stepCleanupBaseSequence",   //$NON-NLS-1$
            };
        }
        return null;
    }

    /**
     * Initialize the members of the StartOrRestartProcessSequence_7_0 class.
     * This step is mandatory for the rest of the sequence to complete.
     */
    @Override
    @Execute
    public void stepInitializeBaseSequence(final RequestMonitor rm) {
        super.stepInitializeBaseSequence(new RequestMonitor (getExecutor(), rm){
            @Override
            protected void handleSuccess() {
                fReverseService = fTracker.getService(IReverseRunControl2.class);
                if (fReverseService != null) {

                    // Here we check for the reverse mode to be used for launching the reverse
                    // debugging service.
                    String fReverseModeString = CDebugUtils.getAttribute(fAttributes,
                            IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_REVERSE_MODE,
                            IGDBLaunchConfigurationConstants.DEBUGGER_REVERSE_MODE_DEFAULT);
                    if (fReverseModeString.equals(IGDBLaunchConfigurationConstants.DEBUGGER_REVERSE_MODE_HARD)) {
                        if(CDebugUIPlugin.getDefault().getPreferenceStore().getString(
                                ICDebugPreferenceConstants.PREF_REVERSE_TRACE_METHOD_HARDWARE).equals(ICDebugPreferenceConstants.PREF_REVERSE_TRACE_METHOD_BRANCH_TRACE))
                        fReverseMode = ReverseTraceMethod.BRANCH_TRACE; // Branch Trace
                        else if(CDebugUIPlugin.getDefault().getPreferenceStore().getString(
                                ICDebugPreferenceConstants.PREF_REVERSE_TRACE_METHOD_HARDWARE).equals(ICDebugPreferenceConstants.PREF_REVERSE_TRACE_METHOD_PROCESSOR_TRACE))
                        fReverseMode = ReverseTraceMethod.PROCESSOR_TRACE; // Processor Trace
                        else
                        fReverseMode = ReverseTraceMethod.GDB_TRACE; // GDB Selected Option
                    }
                    else if (fReverseModeString.equals(IGDBLaunchConfigurationConstants.DEBUGGER_REVERSE_MODE_SOFT)) {
                        fReverseMode = ReverseTraceMethod.FULL_TRACE; // Full Trace
                    }
                    else {
                        rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, IDsfStatusConstants.INTERNAL_ERROR, "Invalid Trace Method Selected", null)); //$NON-NLS-1$
                    }
                }

                rm.done();
            }
        });
    }


    /**
     * Here we set the reverse debug mode
     * @since 4.8
     */
    @Execute
    public void stepSetReverseMode(RequestMonitor rm) {
        if (fReverseEnabled && fReverseService != null ) {
            fReverseService.enableReverseMode(fCommandControl.getContext(), fReverseMode, rm);
        } else {
            rm.done();
        }
    }

}

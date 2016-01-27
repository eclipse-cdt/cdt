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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.debug.core.CDebugUtils;
import org.eclipse.cdt.debug.core.model.IChangeReverseMethodHandler.ReverseTraceMethod;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.gdb.IGDBLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.gdb.IGdbDebugPreferenceConstants;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.gdb.service.command.IGDBControl;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

/**
 * @since 5.0
 */
public class StartOrRestartProcessSequence_7_10 extends StartOrRestartProcessSequence_7_3 {

    private IGDBControl fCommandControl;
    private IReverseRunControl2 fReverseService;
    private ReverseTraceMethod fReverseMode = ReverseTraceMethod.FULL_TRACE;
    private final Map<String, Object> fAttributes;

    public StartOrRestartProcessSequence_7_10(DsfExecutor executor, IContainerDMContext containerDmc,
            Map<String, Object> attributes, boolean restart, DataRequestMonitor<IContainerDMContext> rm) {
        super(executor, containerDmc, attributes, restart, rm);
        
        fAttributes = attributes;
    }

    @Override
    protected String[] getExecutionOrder(String group) {
        if (GROUP_TOP_LEVEL.equals(group)) {
            // Initialize the list with the base class' steps
            // We need to create a list that we can modify, which is why we create our own ArrayList.
            List<String> orderList = new ArrayList<String>(Arrays.asList(super.getExecutionOrder(GROUP_TOP_LEVEL)));

            // Insert the new stepSetReverseMode after stepSetReverseOff
            orderList.add(orderList.indexOf("stepSetReverseOff") + 1, "stepSetReverseMode"); //$NON-NLS-1$ //$NON-NLS-2$
            
            return orderList.toArray(new String[orderList.size()]);
        }

        return null;
    }

    /**
     * Initialize the members of the StartOrRestartProcessSequence_7_10 class.
     * This step is mandatory for the rest of the sequence to complete.
     */
    @Override
    @Execute
    public void stepInitializeBaseSequence(final RequestMonitor rm) {
        super.stepInitializeBaseSequence(new RequestMonitor (getExecutor(), rm) {
            @Override
            protected void handleSuccess() {
                DsfServicesTracker tracker = new DsfServicesTracker(GdbPlugin.getBundleContext(), getContainerContext().getSessionId());
                fCommandControl = tracker.getService(IGDBControl.class);
                fReverseService = tracker.getService(IReverseRunControl2.class);
                tracker.dispose();
                
                if (fReverseService != null) {

                    // Here we check for the reverse mode to be used for launching the reverse
                    // debugging service.
                    String fReverseModeString = CDebugUtils.getAttribute(fAttributes,
                            IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_REVERSE_MODE,
                            IGDBLaunchConfigurationConstants.DEBUGGER_REVERSE_MODE_DEFAULT);
                    if (fReverseModeString.equals(IGDBLaunchConfigurationConstants.DEBUGGER_REVERSE_MODE_HARDWARE)) {
                        if (Platform.getPreferencesService().getString(GdbPlugin.PLUGIN_ID,
                                IGdbDebugPreferenceConstants.PREF_REVERSE_TRACE_METHOD_HARDWARE,
                                IGdbDebugPreferenceConstants.PREF_REVERSE_TRACE_METHOD_GDB_TRACE,
                                null).equals(IGdbDebugPreferenceConstants.PREF_REVERSE_TRACE_METHOD_BRANCH_TRACE)) {
                            fReverseMode = ReverseTraceMethod.BRANCH_TRACE; // Branch Trace
                        } else if (Platform.getPreferencesService().getString(GdbPlugin.PLUGIN_ID,
                                IGdbDebugPreferenceConstants.PREF_REVERSE_TRACE_METHOD_HARDWARE,
                                IGdbDebugPreferenceConstants.PREF_REVERSE_TRACE_METHOD_GDB_TRACE,
                                null).equals(IGdbDebugPreferenceConstants.PREF_REVERSE_TRACE_METHOD_PROCESSOR_TRACE)) {
                            fReverseMode = ReverseTraceMethod.PROCESSOR_TRACE; // Processor Trace
                        } else {
                            fReverseMode = ReverseTraceMethod.GDB_TRACE; // GDB Selected Option
                        }
                    }
                    else if (fReverseModeString.equals(IGDBLaunchConfigurationConstants.DEBUGGER_REVERSE_MODE_SOFTWARE)) {
                        fReverseMode = ReverseTraceMethod.FULL_TRACE; // Full Trace
                    } else {
                        rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, IDsfStatusConstants.INTERNAL_ERROR, "Invalid Trace Method Selected", null)); //$NON-NLS-1$
                    }
                }

                rm.done();
            }
        });
    }


    /**
     * Here we set the reverse debug mode
     */
    @Execute
    public void stepSetReverseMode(RequestMonitor rm) {
        if (getReverseEnabled() && fReverseService != null ) {
            fReverseService.enableReverseMode(fCommandControl.getContext(), fReverseMode, rm);
        } else {
            rm.done();
        }
    }
}

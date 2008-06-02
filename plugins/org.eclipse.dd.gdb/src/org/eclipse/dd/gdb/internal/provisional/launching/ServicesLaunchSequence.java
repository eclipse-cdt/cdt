/*******************************************************************************
 * Copyright (c) 2006, 2008 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.dd.gdb.internal.provisional.launching;

import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.internal.core.sourcelookup.CSourceLookupDirector;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.dd.dsf.concurrent.RequestMonitor;
import org.eclipse.dd.dsf.concurrent.Sequence;
import org.eclipse.dd.dsf.debug.service.StepQueueManager;
import org.eclipse.dd.dsf.service.DsfSession;
import org.eclipse.dd.gdb.internal.provisional.IGDBLaunchConfigurationConstants;
import org.eclipse.dd.gdb.internal.provisional.service.GDBRunControl;
import org.eclipse.dd.gdb.internal.provisional.service.command.GDBControl;
import org.eclipse.dd.gdb.internal.provisional.service.command.GDBControl.SessionType;
import org.eclipse.dd.mi.service.CSourceLookup;
import org.eclipse.dd.mi.service.ExpressionService;
import org.eclipse.dd.mi.service.MIBreakpoints;
import org.eclipse.dd.mi.service.MIBreakpointsManager;
import org.eclipse.dd.mi.service.MIDisassembly;
import org.eclipse.dd.mi.service.MIMemory;
import org.eclipse.dd.mi.service.MIModules;
import org.eclipse.dd.mi.service.MIRegisters;
import org.eclipse.dd.mi.service.MIStack;

public class ServicesLaunchSequence extends Sequence {

    Step[] fSteps = new Step[] {
        // Create and initialize the Connection service.
        new Step() { 
            @Override
            public void execute(RequestMonitor requestMonitor) {
                //
                // Create the connection.
                //
                fCommandControl = new GDBControl(fSession, getGDBPath(), fExecPath, fSessionType, fAttach, 30);
                fCommandControl.initialize(requestMonitor);
            }
        },
        new Step() { @Override
        public void execute(RequestMonitor requestMonitor) {
        	new GDBRunControl(fSession).initialize(requestMonitor);
        }},
        new Step() { @Override
        public void execute(RequestMonitor requestMonitor) {
            new StepQueueManager(fSession).initialize(requestMonitor);
        }},
        new Step() { @Override
        public void execute(RequestMonitor requestMonitor) {
            new MIMemory(fSession).initialize(requestMonitor);
        }},
        new Step() { @Override
        public void execute(RequestMonitor requestMonitor) {
            new MIModules(fSession).initialize(requestMonitor);
        }},
        new Step() { @Override
        public void execute(RequestMonitor requestMonitor) {
            new MIStack(fSession).initialize(requestMonitor);
        }},
        new Step() { @Override
        public void execute(RequestMonitor requestMonitor) {
            new ExpressionService(fSession).initialize(requestMonitor);
        }},
        new Step() { @Override
        public void execute(RequestMonitor requestMonitor) {
            fSourceLookup = new CSourceLookup(fSession);
            fSourceLookup.initialize(requestMonitor);
        }},
        new Step() { @Override
        public void execute(RequestMonitor requestMonitor) {
            fSourceLookup.setSourceLookupDirector(fCommandControl.getGDBDMContext(), (CSourceLookupDirector)fLaunch.getSourceLocator());
            requestMonitor.done();
        }},
        new Step() { @Override
        public void execute(final RequestMonitor requestMonitor) {
            // Create the low-level breakpoint service 
            final MIBreakpoints bpService = new MIBreakpoints(fSession);
            bpService.initialize(new RequestMonitor(getExecutor(), requestMonitor));
        }},
        new Step() { @Override
        public void execute(final RequestMonitor requestMonitor) {
            // Create high-level breakpoint service and install breakpoints 
            // for the GDB debug context.
            fBpmService = new MIBreakpointsManager(fSession, CDebugCorePlugin.PLUGIN_ID);
            fBpmService.initialize(new RequestMonitor(getExecutor(), requestMonitor)); 
        }},
        new Step() { @Override
        public void execute(RequestMonitor requestMonitor) {
            new MIRegisters(fSession).initialize(requestMonitor);
        }},
        new Step() { @Override
        public void execute(RequestMonitor requestMonitor) {
            new MIDisassembly(fSession).initialize(requestMonitor);
        }},
    };

    DsfSession fSession;
    GdbLaunch fLaunch;
    IPath fExecPath;

    SessionType fSessionType;
    boolean fAttach;

    GDBControl fCommandControl;
    CSourceLookup fSourceLookup;
    MIBreakpointsManager fBpmService;

    public ServicesLaunchSequence(DsfSession session, GdbLaunch launch, IPath execPath, SessionType sessionType, boolean attach) {
        super(session.getExecutor());
        fSession = session;
        fLaunch = launch;
        fExecPath = execPath;
        fSessionType = sessionType;
        fAttach = attach;
    }
    
    @Override
    public Step[] getSteps() {
        return fSteps;
    }
    
    private IPath getGDBPath() {
        IPath retVal = new Path(IGDBLaunchConfigurationConstants.DEBUGGER_DEBUG_NAME_DEFAULT);
        try {
            retVal = new Path(fLaunch.getLaunchConfiguration().getAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUG_NAME, 
            		                                                        IGDBLaunchConfigurationConstants.DEBUGGER_DEBUG_NAME_DEFAULT));
        } catch (CoreException e) {
        }
        return retVal;
    }

}

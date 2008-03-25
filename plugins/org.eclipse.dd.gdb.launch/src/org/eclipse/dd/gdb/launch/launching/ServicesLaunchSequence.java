/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.dd.gdb.launch.launching;

import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.internal.core.sourcelookup.CSourceLookupDirector;
import org.eclipse.cdt.debug.mi.core.IMILaunchConfigurationConstants;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.dd.dsf.concurrent.RequestMonitor;
import org.eclipse.dd.dsf.concurrent.Sequence;
import org.eclipse.dd.dsf.debug.service.StepQueueManager;
import org.eclipse.dd.dsf.service.DsfSession;
import org.eclipse.dd.gdb.IGDBLaunchConfigurationConstants;
import org.eclipse.dd.gdb.service.GDBRunControl;
import org.eclipse.dd.gdb.service.command.GDBControl;
import org.eclipse.dd.gdb.service.command.GDBControl.SessionType;
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
            	String debugMode = ICDTLaunchConfigurationConstants.DEBUGGER_MODE_RUN;
            	try {
            		debugMode = fLaunch.getLaunchConfiguration().getAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_START_MODE,
            				                                                  ICDTLaunchConfigurationConstants.DEBUGGER_MODE_RUN );
            	} catch (CoreException e) {
            	}

            	if (debugMode.equals(ICDTLaunchConfigurationConstants.DEBUGGER_MODE_RUN)) {
            		fSessionType = SessionType.RUN;
            	} else if (debugMode.equals(ICDTLaunchConfigurationConstants.DEBUGGER_MODE_ATTACH)) {
            		fSessionType = SessionType.ATTACH;
            	} else if (debugMode.equals(ICDTLaunchConfigurationConstants.DEBUGGER_MODE_CORE)) {
            		fSessionType = SessionType.CORE;
            	} else if (debugMode.equals(IGDBLaunchConfigurationConstants.DEBUGGER_MODE_REMOTE)) {
            		fSessionType = SessionType.REMOTE;
            	} else {
                	fSessionType = SessionType.RUN;
            	}

                //
                // Create the connection.
                //
                fCommandControl = new GDBControl(fSession, getGDBPath(), fExecPath, fSessionType, 30);
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

    GDBControl fCommandControl;
    CSourceLookup fSourceLookup;
    MIBreakpointsManager fBpmService;

    public ServicesLaunchSequence(DsfSession session, GdbLaunch launch, IPath execPath) {
        super(session.getExecutor());
        fSession = session;
        fLaunch = launch;
        fExecPath = execPath;
    }
    
    @Override
    public Step[] getSteps() {
        return fSteps;
    }
    
    private IPath getGDBPath() {
        IPath retVal = new Path("gdb.exe"); //$NON-NLS-1$
        try {
            retVal = new Path( fLaunch.getLaunchConfiguration().getAttribute( IMILaunchConfigurationConstants.ATTR_DEBUG_NAME, IMILaunchConfigurationConstants.DEBUGGER_DEBUG_NAME_DEFAULT ) );
        } catch (CoreException e) {
        }
        return retVal;
    }

}

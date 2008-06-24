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

import org.eclipse.cdt.debug.internal.core.sourcelookup.CSourceLookupDirector;
import org.eclipse.core.runtime.IPath;
import org.eclipse.dd.dsf.concurrent.RequestMonitor;
import org.eclipse.dd.dsf.concurrent.Sequence;
import org.eclipse.dd.dsf.debug.service.IBreakpoints;
import org.eclipse.dd.dsf.debug.service.IDisassembly;
import org.eclipse.dd.dsf.debug.service.IExpressions;
import org.eclipse.dd.dsf.debug.service.IMemory;
import org.eclipse.dd.dsf.debug.service.IModules;
import org.eclipse.dd.dsf.debug.service.IRegisters;
import org.eclipse.dd.dsf.debug.service.IRunControl;
import org.eclipse.dd.dsf.debug.service.ISourceLookup;
import org.eclipse.dd.dsf.debug.service.IStack;
import org.eclipse.dd.dsf.debug.service.StepQueueManager;
import org.eclipse.dd.dsf.service.DsfSession;
import org.eclipse.dd.gdb.internal.provisional.service.command.GDBControl;
import org.eclipse.dd.gdb.internal.provisional.service.command.GDBControl.SessionType;
import org.eclipse.dd.mi.service.CSourceLookup;
import org.eclipse.dd.mi.service.MIBreakpointsManager;

public class ServicesLaunchSequence extends Sequence {

    Step[] fSteps = new Step[] {
        // Create and initialize the Connection service.
        new Step() { 
            @Override
            public void execute(RequestMonitor requestMonitor) {
                //
                // Create the connection.
                //
                fCommandControl = new GDBControl(fSession, LaunchUtils.getGDBPath(fLaunch.getLaunchConfiguration()),
                		                         fExecPath, fSessionType, fAttach, 30);
                fCommandControl.initialize(requestMonitor);
            }
        },
        new Step() { @Override
        public void execute(RequestMonitor requestMonitor) {
        	fLaunch.getServiceFactory().createService(fSession, IRunControl.class).initialize(requestMonitor);
        }},
        new Step() { @Override
        public void execute(RequestMonitor requestMonitor) {
            new StepQueueManager(fSession).initialize(requestMonitor);
        }},
        new Step() { @Override
        public void execute(RequestMonitor requestMonitor) {
        	fLaunch.getServiceFactory().createService(fSession, IMemory.class).initialize(requestMonitor);
        }},
        new Step() { @Override
        public void execute(RequestMonitor requestMonitor) {
        	fLaunch.getServiceFactory().createService(fSession, IModules.class).initialize(requestMonitor);
        }},
        new Step() { @Override
        public void execute(RequestMonitor requestMonitor) {
        	fLaunch.getServiceFactory().createService(fSession, IStack.class).initialize(requestMonitor);
        }},
        new Step() { @Override
        public void execute(RequestMonitor requestMonitor) {
        	fLaunch.getServiceFactory().createService(fSession, IExpressions.class).initialize(requestMonitor);
        }},
        new Step() { @Override
        public void execute(RequestMonitor requestMonitor) {
        	fSourceLookup = (CSourceLookup)fLaunch.getServiceFactory().createService(fSession, ISourceLookup.class);
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
        	fLaunch.getServiceFactory().createService(fSession, IBreakpoints.class).initialize(new RequestMonitor(getExecutor(), requestMonitor));
        }},
        new Step() { @Override
        public void execute(final RequestMonitor requestMonitor) {
            // Create high-level breakpoint service and install breakpoints 
            // for the GDB debug context.
        	fLaunch.getServiceFactory().createService(fSession, MIBreakpointsManager.class).initialize(new RequestMonitor(getExecutor(), requestMonitor)); 
        }},
        new Step() { @Override
        public void execute(RequestMonitor requestMonitor) {
        	fLaunch.getServiceFactory().createService(fSession, IRegisters.class).initialize(requestMonitor);
        }},
        new Step() { @Override
        public void execute(RequestMonitor requestMonitor) {
        	fLaunch.getServiceFactory().createService(fSession, IDisassembly.class).initialize(requestMonitor);
        }},
    };

    DsfSession fSession;
    GdbLaunch fLaunch;
    IPath fExecPath;

    SessionType fSessionType;
    boolean fAttach;

    GDBControl fCommandControl;
    CSourceLookup fSourceLookup;

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
    


}

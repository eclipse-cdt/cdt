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
import org.eclipse.dd.dsf.concurrent.RequestMonitor;
import org.eclipse.dd.dsf.concurrent.Sequence;
import org.eclipse.dd.dsf.debug.service.IBreakpoints;
import org.eclipse.dd.dsf.debug.service.IDisassembly;
import org.eclipse.dd.dsf.debug.service.IExpressions;
import org.eclipse.dd.dsf.debug.service.IMemory;
import org.eclipse.dd.dsf.debug.service.IModules;
import org.eclipse.dd.dsf.debug.service.IProcesses;
import org.eclipse.dd.dsf.debug.service.IRegisters;
import org.eclipse.dd.dsf.debug.service.IRunControl;
import org.eclipse.dd.dsf.debug.service.ISourceLookup;
import org.eclipse.dd.dsf.debug.service.IStack;
import org.eclipse.dd.dsf.debug.service.ISourceLookup.ISourceLookupDMContext;
import org.eclipse.dd.dsf.debug.service.command.ICommandControlService;
import org.eclipse.dd.dsf.service.DsfSession;
import org.eclipse.dd.mi.service.CSourceLookup;
import org.eclipse.dd.mi.service.IMIProcesses;
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
                fCommandControl = fLaunch.getServiceFactory().createService(ICommandControlService.class, fSession, fLaunch.getLaunchConfiguration());
                fCommandControl.initialize(requestMonitor);
            }
        },
        new Step() { @Override
        public void execute(RequestMonitor requestMonitor) {
        	fProcService = (IMIProcesses)fLaunch.getServiceFactory().createService(IProcesses.class, fSession);
        	fProcService.initialize(requestMonitor);
        }},
        new Step() { @Override
        public void execute(RequestMonitor requestMonitor) {
        	fLaunch.getServiceFactory().createService(IRunControl.class, fSession).initialize(requestMonitor);
        }},
        new Step() { @Override
        public void execute(RequestMonitor requestMonitor) {
        	fLaunch.getServiceFactory().createService(IMemory.class, fSession).initialize(requestMonitor);
        }},
        new Step() { @Override
        public void execute(RequestMonitor requestMonitor) {
        	fLaunch.getServiceFactory().createService(IModules.class, fSession).initialize(requestMonitor);
        }},
        new Step() { @Override
        public void execute(RequestMonitor requestMonitor) {
        	fLaunch.getServiceFactory().createService(IStack.class, fSession).initialize(requestMonitor);
        }},
        new Step() { @Override
        public void execute(RequestMonitor requestMonitor) {
        	fLaunch.getServiceFactory().createService(IExpressions.class, fSession).initialize(requestMonitor);
        }},
        new Step() { @Override
        public void execute(RequestMonitor requestMonitor) {
        	fSourceLookup = (CSourceLookup)fLaunch.getServiceFactory().createService(ISourceLookup.class, fSession);
            fSourceLookup.initialize(requestMonitor);
        }},
        new Step() { @Override
        public void execute(RequestMonitor requestMonitor) {
       		ISourceLookupDMContext sourceLookupDmc = (ISourceLookupDMContext)fCommandControl.getContext();
            fSourceLookup.setSourceLookupDirector(sourceLookupDmc, (CSourceLookupDirector)fLaunch.getSourceLocator());
            requestMonitor.done();
        }},
        new Step() { @Override
        public void execute(final RequestMonitor requestMonitor) {
            // Create the low-level breakpoint service 
        	fLaunch.getServiceFactory().createService(IBreakpoints.class, fSession).initialize(new RequestMonitor(getExecutor(), requestMonitor));
        }},
        new Step() { @Override
        public void execute(final RequestMonitor requestMonitor) {
            // Create high-level breakpoint service and install breakpoints 
            // for the GDB debug context.
        	fLaunch.getServiceFactory().createService(MIBreakpointsManager.class, fSession).initialize(new RequestMonitor(getExecutor(), requestMonitor)); 
        }},
        new Step() { @Override
        public void execute(RequestMonitor requestMonitor) {
        	fLaunch.getServiceFactory().createService(IRegisters.class, fSession).initialize(requestMonitor);
        }},
        new Step() { @Override
        public void execute(RequestMonitor requestMonitor) {
        	fLaunch.getServiceFactory().createService(IDisassembly.class, fSession).initialize(requestMonitor);
        }},
    };

    DsfSession fSession;
    GdbLaunch fLaunch;

    ICommandControlService fCommandControl;
    IMIProcesses fProcService;
    CSourceLookup fSourceLookup;
    
    public ServicesLaunchSequence(DsfSession session, GdbLaunch launch) {
        super(session.getExecutor());
        fSession = session;
        fLaunch = launch;
    }
    
    @Override
    public Step[] getSteps() {
        return fSteps;
    }
    


}

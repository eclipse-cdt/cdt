/*******************************************************************************
 * Copyright (c) 2006, 2012 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     Nokia 			  - created GDBBackend service. Sep. 2008
 *     IBM Corporation 
 *     Ericsson           - Support for Tracing Control service
 *     Marc Khouzam (Ericsson) - Start IGDBHardware service for the multicore visualizer (Bug 335027)
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.launching;

import org.eclipse.cdt.debug.internal.core.sourcelookup.CSourceLookupDirector;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Sequence;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints;
import org.eclipse.cdt.dsf.debug.service.IDisassembly;
import org.eclipse.cdt.dsf.debug.service.IExpressions;
import org.eclipse.cdt.dsf.debug.service.IMemory;
import org.eclipse.cdt.dsf.debug.service.IModules;
import org.eclipse.cdt.dsf.debug.service.IProcesses;
import org.eclipse.cdt.dsf.debug.service.IRegisters;
import org.eclipse.cdt.dsf.debug.service.IRunControl;
import org.eclipse.cdt.dsf.debug.service.ISourceLookup;
import org.eclipse.cdt.dsf.debug.service.ISourceLookup.ISourceLookupDMContext;
import org.eclipse.cdt.dsf.debug.service.IStack;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService;
import org.eclipse.cdt.dsf.gdb.service.IGDBHardwareAndOS;
import org.eclipse.cdt.dsf.gdb.service.IGDBTraceControl;
import org.eclipse.cdt.dsf.mi.service.CSourceLookup;
import org.eclipse.cdt.dsf.mi.service.IMIBackend;
import org.eclipse.cdt.dsf.mi.service.MIBreakpointsManager;
import org.eclipse.cdt.dsf.mi.service.MIBreakpointsSynchronizer;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.IProgressMonitor;

public class ServicesLaunchSequence extends Sequence {

    DsfSession fSession;
    GdbLaunch fLaunch;

    ICommandControlService fCommandControl;
    CSourceLookup fSourceLookup;
    
    Step[] fSteps = new Step[] {
        new Step() { @Override
        public void execute(RequestMonitor requestMonitor) {
            fLaunch.getServiceFactory().createService(IMIBackend.class, fSession, fLaunch.getLaunchConfiguration()).initialize(requestMonitor);
        }},
        new Step() { @Override
        public void execute(RequestMonitor requestMonitor) {
            fCommandControl = fLaunch.getServiceFactory().createService(ICommandControlService.class, fSession, fLaunch.getLaunchConfiguration());
            fCommandControl.initialize(requestMonitor);
        }},
        new Step() { @Override
        public void execute(RequestMonitor requestMonitor) {
        	fLaunch.getServiceFactory().createService(IGDBHardwareAndOS.class, fSession, fLaunch.getLaunchConfiguration()).initialize(requestMonitor);
        }},
        new Step() { @Override
        public void execute(RequestMonitor requestMonitor) {
        	fLaunch.getServiceFactory().createService(IProcesses.class, fSession).initialize(requestMonitor);
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
        	fLaunch.getServiceFactory().createService(IBreakpoints.class, fSession).initialize(requestMonitor);
        }},
        new Step() { @Override
        public void execute(final RequestMonitor requestMonitor) {
            // Create high-level breakpoint service and install breakpoints 
            // for the GDB debug context.
        	fLaunch.getServiceFactory().createService(MIBreakpointsManager.class, fSession).initialize(requestMonitor); 
        }},
        new Step() { @Override
        public void execute(RequestMonitor requestMonitor) {
        	fLaunch.getServiceFactory().createService(IRegisters.class, fSession).initialize(requestMonitor);
        }},
        new Step() { @Override
        public void execute(RequestMonitor requestMonitor) {
        	fLaunch.getServiceFactory().createService(IDisassembly.class, fSession).initialize(requestMonitor);
        }},
        new Step() { @Override
        public void execute(RequestMonitor requestMonitor) {
           	IGDBTraceControl traceService = fLaunch.getServiceFactory().createService(IGDBTraceControl.class, fSession, fLaunch.getLaunchConfiguration());
           	// Note that for older versions of GDB, we don't support tracing, so there is no trace service.
           	if (traceService != null) {
           		traceService.initialize(requestMonitor);
           	} else {
           		requestMonitor.done();
           	}
        }},
        new Step() { @Override
        public void execute(final RequestMonitor requestMonitor) {
        	fLaunch.getServiceFactory().createService(MIBreakpointsSynchronizer.class, fSession).initialize(requestMonitor); 
        }},
    };
    
    public ServicesLaunchSequence(DsfSession session, GdbLaunch launch, IProgressMonitor pm) {
        super(session.getExecutor(), pm, LaunchMessages.getString("ServicesLaunchSequence_0"), LaunchMessages.getString("ServicesLaunchSequence_1"));   //$NON-NLS-1$ //$NON-NLS-2$
        fSession = session;
        fLaunch = launch;
    }

    @Override
    public Step[] getSteps() {
        return fSteps;
    }
}

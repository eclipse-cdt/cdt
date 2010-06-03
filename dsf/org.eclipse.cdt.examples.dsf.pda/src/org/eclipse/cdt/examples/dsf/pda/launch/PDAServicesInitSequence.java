/*******************************************************************************
 * Copyright (c) 2008, 2010 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.examples.dsf.pda.launch;

import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Sequence;
import org.eclipse.cdt.dsf.datamodel.DataModelInitializedEvent;
import org.eclipse.cdt.dsf.debug.service.BreakpointsMediator;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.examples.dsf.pda.service.PDABackend;
import org.eclipse.cdt.examples.dsf.pda.service.PDABreakpointAttributeTranslator;
import org.eclipse.cdt.examples.dsf.pda.service.PDABreakpoints;
import org.eclipse.cdt.examples.dsf.pda.service.PDACommandControl;
import org.eclipse.cdt.examples.dsf.pda.service.PDAExpressions;
import org.eclipse.cdt.examples.dsf.pda.service.PDARegisters;
import org.eclipse.cdt.examples.dsf.pda.service.PDARunControl;
import org.eclipse.cdt.examples.dsf.pda.service.PDAStack;

/**
 * The initialization sequence for PDA debugger services.  This sequence contains
 * the series of steps that are executed to properly initialize the PDA-DSF debug
 * session.  If any of the individual steps fail, the initialization will abort.   
 * <p>
 * The order in which services are initialized is important.  Some services depend
 * on other services and they assume that they will be initialized only if those
 * services are active.  Also the service events are prioritized and their priority
 * depends on the order in which the services were initialized.
 * </p>
 */
public class PDAServicesInitSequence extends Sequence {

    Step[] fSteps = new Step[] {
        new Step() 
        { 
            @Override
            public void execute(RequestMonitor requestMonitor) {
                // Start PDA back end debugger service.
                new PDABackend(fSession, fLaunch, fProgram).initialize(requestMonitor);
            }
        },
        new Step() 
        { 
            @Override
            public void execute(RequestMonitor requestMonitor) {
                // Start PDA command control service.
                fCommandControl = new PDACommandControl(fSession);
                fCommandControl.initialize(requestMonitor);
            }
        },
        new Step() { 
            @Override
            public void execute(RequestMonitor requestMonitor) {
                // Start the run control service.
                fRunControl = new PDARunControl(fSession);
                fRunControl.initialize(requestMonitor);
            }
        },
        new Step() { 
            @Override
            public void execute(final RequestMonitor requestMonitor) {
                // Start the low-level breakpoint service 
                new PDABreakpoints(fSession).initialize(new RequestMonitor(getExecutor(), requestMonitor));
            }
        },
        new Step() { 
            @Override
            public void execute(final RequestMonitor requestMonitor) {
                // Create the breakpoint mediator and start tracking PDA breakpoints.

                final BreakpointsMediator bpmService = new BreakpointsMediator(
                    fSession, new PDABreakpointAttributeTranslator());
                bpmService.initialize(new RequestMonitor(getExecutor(), requestMonitor) {
                    @Override
                    protected void handleSuccess() {
                        bpmService.startTrackingBreakpoints(fCommandControl.getContext(), requestMonitor);
                    }
                }); 
            }
        },
        new Step() { 
            @Override
            public void execute(RequestMonitor requestMonitor) {
                // Start the stack service.
                new PDAStack(fSession).initialize(requestMonitor);
            }
        },
        new Step() { 
            @Override
            public void execute(RequestMonitor requestMonitor) {
                // Start the service to track expressions.
                new PDAExpressions(fSession).initialize(requestMonitor);
            }
        },
        new Step() { 
            @Override
            public void execute(RequestMonitor requestMonitor) {
                // Start the service to track expressions.
                new PDARegisters(fSession).initialize(requestMonitor);
            }
        },
        /*
         * Indicate that the Data Model has been filled.  This will trigger the Debug view to expand.
         */
        new Step() {
            @Override
            public void execute(final RequestMonitor requestMonitor) {
                fSession.dispatchEvent(
                    new DataModelInitializedEvent(fCommandControl.getContext()),
                    fCommandControl.getProperties());
                requestMonitor.done();
            }
        }
    };

    // Sequence input parameters, used in initializing services.
    private PDALaunch  fLaunch;
    private DsfSession fSession;
    private String fProgram;

    // Service references, initialized when created and used in initializing other services.
    private PDACommandControl fCommandControl;
    private PDARunControl fRunControl;

    public PDAServicesInitSequence(DsfSession session, PDALaunch launch, String program, RequestMonitor rm) 
    {
        super(session.getExecutor(), rm);
        fLaunch = launch;
        fSession = session;
        fProgram = program;
    }

    @Override
    public Step[] getSteps() {
        return fSteps;
    }
}

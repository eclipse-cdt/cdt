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
package org.eclipse.dd.examples.pda.launch;

import org.eclipse.dd.dsf.concurrent.RequestMonitor;
import org.eclipse.dd.dsf.concurrent.Sequence;
import org.eclipse.dd.dsf.debug.service.BreakpointsMediator;
import org.eclipse.dd.dsf.debug.service.StepQueueManager;
import org.eclipse.dd.dsf.service.DsfSession;
import org.eclipse.dd.examples.pda.service.breakpoints.PDABreakpointAttributeTranslator;
import org.eclipse.dd.examples.pda.service.breakpoints.PDABreakpoints;
import org.eclipse.dd.examples.pda.service.command.PDACommandControl;
import org.eclipse.dd.examples.pda.service.expressions.PDAExpressions;
import org.eclipse.dd.examples.pda.service.runcontrol.PDARunControl;
import org.eclipse.dd.examples.pda.service.stack.PDAStack;
import org.eclipse.debug.examples.core.pda.sourcelookup.PDASourceLookupDirector;

public class PDAServicesInitSequence extends Sequence {

    Step[] fSteps = new Step[] {
        // Create and initialize the Connection service.
        new Step() { 
            @Override
            public void execute(RequestMonitor requestMonitor) {
                // Create the connection.
                fCommandControl = new PDACommandControl(fSession, fRequestPort, fEventPort);
                fCommandControl.initialize(requestMonitor);
            }
        },
        new Step() { 
            @Override
            public void execute(RequestMonitor requestMonitor) {
                fRunControl = new PDARunControl(fSession);
                fRunControl.initialize(requestMonitor);
            }
        },
        new Step() { 
            @Override
            public void execute(RequestMonitor requestMonitor) {
                new StepQueueManager(fSession).initialize(requestMonitor);
            }
        },
        new Step() { 
            @Override
            public void execute(final RequestMonitor requestMonitor) {
                // Create the low-level breakpoint service 
                new PDABreakpoints(fSession, fProgram).initialize(new RequestMonitor(getExecutor(), requestMonitor));
            }
        },
        new Step() { 
            @Override
            public void execute(final RequestMonitor requestMonitor) {
                final BreakpointsMediator bpmService = new BreakpointsMediator(
                    fSession, new PDABreakpointAttributeTranslator());
                bpmService.initialize(new RequestMonitor(getExecutor(), requestMonitor) {
                    @Override
                    protected void handleOK() {
                        bpmService.startTrackingBreakpoints(fCommandControl.getDMContext(), requestMonitor);
                    }
                }); 
            }
        },
        new Step() { @Override
            public void execute(RequestMonitor requestMonitor) {
                new PDAStack(fSession).initialize(requestMonitor);
            }
        },
        new Step() { @Override
            public void execute(RequestMonitor requestMonitor) {
                new PDAExpressions(fSession).initialize(requestMonitor);
            }
        },
        new Step() { @Override
            public void execute(RequestMonitor requestMonitor) {
                fRunControl.resume(fCommandControl.getDMContext(), requestMonitor);
            }
        },
    };

    private DsfSession fSession;
    private PDALaunch fLaunch;
    private String fProgram;
    private int fRequestPort;
    private int fEventPort;

    PDACommandControl fCommandControl;
    PDARunControl fRunControl;
    PDASourceLookupDirector fSourceLookup;

    public PDAServicesInitSequence(DsfSession session, PDALaunch launch, String program, int requestPort, 
        int eventPort, RequestMonitor rm) 
    {
        super(session.getExecutor(), rm);
        fSession = session;
        fLaunch = launch;
        fProgram = program;
        fRequestPort = requestPort;
        fEventPort = eventPort;
    }
    
    @Override
    public Step[] getSteps() {
        return fSteps;
    }
}

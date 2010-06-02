/*******************************************************************************
 * Copyright (c) 2006, 2010 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.breakpoints;

import java.lang.reflect.Constructor;
import java.util.concurrent.ExecutionException;

import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Sequence;
import org.eclipse.cdt.dsf.debug.service.BreakpointsMediator2;
import org.eclipse.cdt.dsf.debug.service.IBreakpointAttributeTranslator2;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.service.IDsfService;
import org.eclipse.cdt.tests.dsf.ServiceEventWaitor;
import org.eclipse.cdt.tests.dsf.TestDsfExecutor;
import org.eclipse.cdt.tests.dsf.breakpoints.DsfTestBreakpoints.BreakpointsAddedEvent;
import org.eclipse.cdt.tests.dsf.breakpoints.DsfTestBreakpoints.BreakpointsTargetDMContext;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class BreakpointMediatorTests {
    TestDsfExecutor fExecutor;
    DsfSession fSession;
    DsfTestBreakpoints fBreakpoints;
    BreakpointsMediator2 fMediator;
    IBreakpointAttributeTranslator2 fTranslator;
    BreakpointsTargetDMContext fTargetContext;
    
    abstract private class InitializeServiceStep<V extends IDsfService> extends Sequence.Step {
        Class<V> fServiceClass;
        
        InitializeServiceStep(Class<V> serviceClass) {
            fServiceClass = serviceClass;
        }

        @Override
        public void execute(RequestMonitor requestMonitor) {
            try {
                Constructor<V> c = fServiceClass.getConstructor(new Class[] {DsfSession.class}); 
                V service = c.newInstance(new Object[] {fSession});
                setService(service);
                service.initialize(requestMonitor);
            } catch (Exception e) {
                Assert.fail("Unexpected exception"); //$NON-NLS-1$
            } 
        }
        
        protected void setService(V service) {}
    }

    private class ShutdownServiceStep extends Sequence.Step {
        IDsfService fService;
        
        ShutdownServiceStep(IDsfService service) {
            fService = service;
        }

        @Override
        public void execute(RequestMonitor requestMonitor) {
            fService.shutdown(requestMonitor);
        }
    }
    
    @Before public void start() throws ExecutionException, InterruptedException {
        fExecutor = new TestDsfExecutor();
        
        Sequence seq = new Sequence(fExecutor) {
            @Override
            public Step[] getSteps() { 
                return new Step[] {
                    // Create session
                    new Sequence.Step() {
                        @Override
                        public void execute(RequestMonitor rm) {
                            fSession = DsfSession.startSession(fExecutor, "org.eclipse.cdt.dsf.tests"); //$NON-NLS-1$
                            rm.done();
                        }
                    },
                    
                    // Initialize breakpoints service 
                    new InitializeServiceStep<DsfTestBreakpoints>(DsfTestBreakpoints.class) {
                        @Override
                        protected void setService(DsfTestBreakpoints service) {
                            fBreakpoints = service;
                        }                        
                    },
                    
                    // Initialize breakpoint mediator
                    new Sequence.Step() { 
                        @Override
                        public void execute(RequestMonitor rm) {
                            fTranslator = new DsfTestBreakpointAttributeTranslator2();
                            fMediator = new BreakpointsMediator2(fSession, fTranslator);
                            fMediator.initialize(rm);
                        }
                    },
                    
                    // Start tracking breakpoints
                    new Sequence.Step() {
                        @Override
                        public void execute(RequestMonitor rm) {
                            fTargetContext = new BreakpointsTargetDMContext(fSession.getId());
                            fMediator.startTrackingBreakpoints(fTargetContext, rm);
                        }
                    },
                };
            }
        };
            
        fExecutor.execute(seq);
        seq.get();
    }   

    @After public void shutdown() throws ExecutionException, InterruptedException {
        Sequence seq = new Sequence(fExecutor) {
            @Override
            public Step[] getSteps() { 
                return new Step[] {
                    // Stop tracking breakpoints
                    new Sequence.Step() {
                        @Override
                        public void execute(RequestMonitor rm) {
                            fMediator.stopTrackingBreakpoints(fTargetContext, rm);
                            fTargetContext = null;
                        }
                    },
                    
                    // Shutdown services
                    new ShutdownServiceStep(fMediator), 
                    new ShutdownServiceStep(fBreakpoints),
                    
                    // Shutdown session
                    new Sequence.Step() {
                        @Override
                        public void execute(RequestMonitor rm) {
                            DsfSession.endSession(fSession);
                            rm.done();
                        }
                    },
                };
            }
        };
            
        fExecutor.execute(seq);
        seq.get();

        fExecutor.submit(new DsfRunnable() { public void run() {
            fExecutor.shutdown();
        }}).get();
        if (fExecutor.exceptionsCaught()) {
            Throwable[] exceptions = fExecutor.getExceptions();
            throw new ExecutionException(exceptions[0]);
        }
        fExecutor = null;
    }

    @Test 
    public void proofOfConceptTest() throws Exception {

        ServiceEventWaitor<BreakpointsAddedEvent> waitor = new ServiceEventWaitor<BreakpointsAddedEvent>(fSession, BreakpointsAddedEvent.class);

        new DsfTestBreakpoint();

        waitor.waitForEvent(50000);
    }
    

}

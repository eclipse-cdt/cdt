/*******************************************************************************
 * Copyright (c) 2006, 2009 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.service;

import java.lang.reflect.Constructor;
import java.util.concurrent.ExecutionException;

import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Sequence;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.service.IDsfService;
import org.eclipse.cdt.tests.dsf.DsfTestPlugin;
import org.eclipse.cdt.tests.dsf.TestDsfExecutor;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

public class ServiceTests {
    TestDsfExecutor fExecutor;
    
    @Before public void startExecutor() throws ExecutionException, InterruptedException {
        fExecutor = new TestDsfExecutor();
    }   
    
    @After public void shutdownExecutor() throws ExecutionException, InterruptedException {
        fExecutor.submit(new DsfRunnable() { public void run() {
            fExecutor.shutdown();
        }}).get();
        if (fExecutor.exceptionsCaught()) {
            Throwable[] exceptions = fExecutor.getExceptions();
            throw new ExecutionException(exceptions[0]);
        }
        fExecutor = null;
    }
    
    private class CreateSessionStep extends Sequence.Step {
        private DsfSession fSession;
        @Override public void execute(RequestMonitor requestMonitor) {
            fSession = DsfSession.startSession(fExecutor, "org.eclipse.cdt.dsf.tests"); //$NON-NLS-1$
            requestMonitor.done();
        }
        
        DsfSession getSession() { return fSession; }
    }

    private class ShutdownSessionStep extends Sequence.Step {
        private CreateSessionStep fCreateSessionStep;
        
        ShutdownSessionStep(CreateSessionStep createSessionStep) {
            fCreateSessionStep = createSessionStep;
        }
        
        @Override
        public void execute(RequestMonitor requestMonitor) {
            DsfSession.endSession(fCreateSessionStep.getSession());
            requestMonitor.done();
        }
    }

    private class InitializeServiceStep extends Sequence.Step {
        CreateSessionStep fCreateSessionStep;
        Class<? extends IDsfService> fServiceClass;
        IDsfService fService;
        
        InitializeServiceStep(CreateSessionStep createSessionStep, Class<? extends IDsfService> serviceClass) {
            fCreateSessionStep = createSessionStep;
            fServiceClass = serviceClass;
        }
        IDsfService getService() { return fService; }

        @Override
        public void execute(RequestMonitor requestMonitor) {
            try {
                Constructor<? extends IDsfService> c = fServiceClass.getConstructor(new Class[] {DsfSession.class}); 
                fService = c.newInstance(new Object[] {fCreateSessionStep.getSession()});
            } catch (Exception e) {
                Assert.fail("Unexpected exception"); //$NON-NLS-1$
            } 
            fService.initialize(requestMonitor);
        }
    }

    private class InitializeMultiInstanceServiceStep extends InitializeServiceStep {
        String fServiceId;
        
        InitializeMultiInstanceServiceStep(CreateSessionStep createSessionStep, Class<? extends IDsfService> serviceClass, String serviceId) {
            super(createSessionStep, serviceClass);
            fServiceId = serviceId;
        }
        @Override
        IDsfService getService() { return fService; }

        @Override
        public void execute(RequestMonitor requestMonitor) {
            try {
                Constructor<? extends IDsfService> c = 
                    fServiceClass.getConstructor(new Class[] {DsfSession.class, String.class}); 
                fService = c.newInstance(new Object[] {fCreateSessionStep.getSession(), fServiceId});
            } catch (Exception e) {
                Assert.fail("Unexpected exception"); //$NON-NLS-1$
            } 
            fService.initialize(requestMonitor);
        }
    }
    
    private class ShutdownServiceStep extends Sequence.Step {
        InitializeServiceStep fInitializeServiceStep;
        ShutdownServiceStep(InitializeServiceStep initStep) {
            fInitializeServiceStep = initStep;
        }

        @Override
        public void execute(RequestMonitor requestMonitor) {
            fInitializeServiceStep.getService().shutdown(requestMonitor);
        }
    }


    abstract private class TestRetrievingReferenceStep extends Sequence.Step {
        String fClass;
        boolean fShouldSucceed;

        TestRetrievingReferenceStep(Class<?> clazz, boolean shouldSucceed) {
            fClass = clazz.getName(); 
            fShouldSucceed = shouldSucceed;
        }
        
        abstract String getFilter();
        
        @Override
        public void execute(RequestMonitor requestMonitor) {
            ServiceReference[] refs = null;
            try {
                refs = DsfTestPlugin.getBundleContext().getServiceReferences(fClass, getFilter());
            } catch (InvalidSyntaxException e) {
                Assert.fail("Unexpected exception"); //$NON-NLS-1$
            }
            if (fShouldSucceed) {
                Assert.assertTrue(refs != null);
                Assert.assertTrue(refs.length == 1);
                IDsfService service = (IDsfService)DsfTestPlugin.getBundleContext().getService(refs[0]);
                Assert.assertTrue(service != null);
                DsfTestPlugin.getBundleContext().ungetService(refs[0]);
            } else {
                Assert.assertTrue(refs == null);
            }                
            requestMonitor.done();
        }
    }

    private class TestRetrievingSimpleServiceReferenceStep extends TestRetrievingReferenceStep {
        CreateSessionStep fCreateSessionStep;
        TestRetrievingSimpleServiceReferenceStep(Class<?> clazz, boolean shouldSucceed, CreateSessionStep createSessionStep) {
            super(clazz, shouldSucceed);
            fCreateSessionStep = createSessionStep;
        }
        @Override
        String getFilter() {
            return "(" + IDsfService.PROP_SESSION_ID + "=" + fCreateSessionStep.getSession().getId() + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }            
    }

    private class TestRetrievingMultiSessionServiceReferenceStep extends TestRetrievingSimpleServiceReferenceStep {
        String fServiceId;
        TestRetrievingMultiSessionServiceReferenceStep(Class<?> clazz, boolean shouldSucceed, CreateSessionStep createSessionStep, 
                                                       String serviceId) {
            super(clazz, shouldSucceed, createSessionStep);
            fServiceId = serviceId;
        }
        @Override
        String getFilter() {
            return "(&" +  //$NON-NLS-1$
                       "(" + IDsfService.PROP_SESSION_ID + "=" + fCreateSessionStep.getSession().getId() + ")" +   //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                       "(" + MultiInstanceTestService.PROP_INSTANCE_ID + "=" + fServiceId + ")" + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                   ")"; //$NON-NLS-1$
        }            
    }

    @Test 
    public void singleServiceTest() throws InterruptedException, ExecutionException {
        Sequence seq = new Sequence(fExecutor) {
            CreateSessionStep fSessionStep;
            InitializeServiceStep fServiceStep;
            
            @Override
            public Step[] getSteps() { return fSteps; }
            
            final private Step[] fSteps = new Step[] 
            {
                fSessionStep = new CreateSessionStep(),
                fServiceStep = new InitializeServiceStep(fSessionStep, SimpleTestService.class),
                new TestRetrievingSimpleServiceReferenceStep(SimpleTestService.class, true, fSessionStep),
                new ShutdownServiceStep(fServiceStep),
                new TestRetrievingSimpleServiceReferenceStep(SimpleTestService.class, false, fSessionStep),
                new ShutdownSessionStep(fSessionStep)
            };
        };
        fExecutor.execute(seq);
        seq.get();
    }
    
    /**
     * Creates two sessions and starts a single service within each session.  
     * Then it tests retrieving the reference to the service.
     */
    @Test 
    public void singleServiceMultiSessionTest() throws InterruptedException, ExecutionException {
        Sequence seq = new Sequence(fExecutor) {
            CreateSessionStep fSession1Step;
            CreateSessionStep fSession2Step;
            InitializeServiceStep fSession1ServiceStep;
            InitializeServiceStep fSession2ServiceStep;
            
            @Override
            public Step[] getSteps() { return fSteps; }
            
            final private Step[] fSteps = new Step[] 
            {
                fSession1Step = new CreateSessionStep(),
                fSession2Step = new CreateSessionStep(),
                fSession1ServiceStep = new InitializeServiceStep(fSession1Step, SimpleTestService.class),
                fSession2ServiceStep = new InitializeServiceStep(fSession2Step, SimpleTestService.class),
                new TestRetrievingSimpleServiceReferenceStep(SimpleTestService.class, true, fSession1Step),
                new TestRetrievingSimpleServiceReferenceStep(SimpleTestService.class, true, fSession2Step),
                new ShutdownServiceStep(fSession1ServiceStep),
                new ShutdownServiceStep(fSession2ServiceStep),
                new TestRetrievingSimpleServiceReferenceStep(SimpleTestService.class, false, fSession1Step),
                new TestRetrievingSimpleServiceReferenceStep(SimpleTestService.class, false, fSession2Step),
                new ShutdownSessionStep(fSession1Step),
                new ShutdownSessionStep(fSession2Step)
            };
        };
        fExecutor.execute(seq);
        seq.get();
    }
    
    @Test 
    public void multiServiceServiceTest() throws InterruptedException, ExecutionException {
        Sequence seq = new Sequence(fExecutor) {
            CreateSessionStep fSessionStep;
            InitializeServiceStep fService1Step;
            InitializeServiceStep fService2Step;
            
            @Override
            public Step[] getSteps() { return fSteps; }
            
            final private Step[] fSteps = new Step[] 
            {
                fSessionStep = new CreateSessionStep(),
                fService1Step = new InitializeMultiInstanceServiceStep(fSessionStep, MultiInstanceTestService.class, "1"), //$NON-NLS-1$
                fService2Step = new InitializeMultiInstanceServiceStep(fSessionStep, MultiInstanceTestService.class, "2"), //$NON-NLS-1$
                new TestRetrievingMultiSessionServiceReferenceStep(MultiInstanceTestService.class, true, fSessionStep, "1"), //$NON-NLS-1$
                new TestRetrievingMultiSessionServiceReferenceStep(MultiInstanceTestService.class, true, fSessionStep, "2"), //$NON-NLS-1$
                new ShutdownServiceStep(fService1Step),
                new ShutdownServiceStep(fService2Step),
                new TestRetrievingMultiSessionServiceReferenceStep(MultiInstanceTestService.class, false, fSessionStep, "1"), //$NON-NLS-1$
                new TestRetrievingMultiSessionServiceReferenceStep(MultiInstanceTestService.class, false, fSessionStep, "2"), //$NON-NLS-1$
                new ShutdownSessionStep(fSessionStep)
            };
        };
        fExecutor.execute(seq);
        seq.get();
    }

}

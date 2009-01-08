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
package org.eclipse.cdt.tests.dsf.events;

import java.util.concurrent.ExecutionException;

import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.tests.dsf.DsfTestPlugin;
import org.eclipse.cdt.tests.dsf.TestDsfExecutor;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class EventTest {
    
    DsfSession fSession;
    TestDsfExecutor fExecutor;
    DsfServicesTracker fTracker;
    Service1 fService1;
    Service2 fService2;
    Service3 fService3;
    
    @Before public void startServices() throws ExecutionException, InterruptedException {
        fExecutor = new TestDsfExecutor();
        
        fExecutor.submit(new DsfRunnable() { public void run() {
            fSession = DsfSession.startSession(fExecutor, "org.eclipse.cdt.tests.dsf"); //$NON-NLS-1$
        }}).get();
        
        StartupSequence startupSeq = new StartupSequence(fSession);
        fExecutor.execute(startupSeq);
        startupSeq.get();
        
        fExecutor.submit(new DsfRunnable() { public void run() {
            fTracker = new DsfServicesTracker(DsfTestPlugin.getBundleContext(), fSession.getId()); 
            fService1 = fTracker.getService(Service1.class);
            fService2 = fTracker.getService(Service2.class);
            fService3 = fTracker.getService(Service3.class);
        }}).get();
        Assert.assertNotNull(fService1);
        Assert.assertNotNull(fService2);
        Assert.assertNotNull(fService3);
    }   
    
    @After public void shutdownServices() throws ExecutionException, InterruptedException {
        ShutdownSequence shutdownSeq = new ShutdownSequence(fSession);
        fExecutor.execute(shutdownSeq);
        shutdownSeq.get();
        
        fExecutor.submit(new DsfRunnable() { public void run() {
            fService1 = null;
            fService2 = null;
            fService3 = null;
            fTracker.dispose();
            fTracker = null;
            DsfSession.endSession(fSession);
            fSession = null;
            fExecutor.shutdown();
        }}).get();
        
        if (fExecutor.exceptionsCaught()) {
            Throwable[] exceptions = fExecutor.getExceptions();
            throw new ExecutionException(exceptions[0]);
        }
        fExecutor = null;
    }
    
    /**
     * Test only the startup and shutdown sequences.
     */
    @Test public void startStopTest() {
    }

    /**
     * Tests dispatching event 1.  The goal of the test is to make sure that 
     * recipients are called in the correct order.   
     */
    @Test public void event1Test() throws ExecutionException, InterruptedException {
        fService1.dispatchEvent1();
        fExecutor.submit(new DsfRunnable() { public void run() {
            Assert.assertTrue(1 == fService1.fEvent1RecipientNumber);
            Assert.assertTrue(2 == fService2.fEvent1RecipientNumber);
            Assert.assertTrue(3 == fService3.fEvent1RecipientNumber);
            Assert.assertTrue(0 == fService1.fEvent2RecipientNumber);
            Assert.assertTrue(0 == fService2.fEvent2RecipientNumber);
            Assert.assertTrue(0 == fService3.fEvent2RecipientNumber);
            Assert.assertTrue(0 == fService1.fEvent3RecipientNumber);
            Assert.assertTrue(0 == fService2.fEvent3RecipientNumber);
            Assert.assertTrue(0 == fService3.fEvent3RecipientNumber);
        }}).get();
    }

    /**
     * Tests dispatching event 2.  The goal of the test is to make sure that 
     * recipients are called in the correct order, and that the other events
     * are not registered.   
     */
    @Test public void event2Test() throws ExecutionException, InterruptedException {
        fService1.dispatchEvent2();
        fExecutor.submit(new DsfRunnable() { public void run() {
            Assert.assertTrue(0 == fService1.fEvent1RecipientNumber);
            Assert.assertTrue(0 == fService2.fEvent1RecipientNumber);
            Assert.assertTrue(0 == fService3.fEvent1RecipientNumber);
            Assert.assertTrue(1 == fService1.fEvent2RecipientNumber);
            Assert.assertTrue(2 == fService2.fEvent2RecipientNumber);
            Assert.assertTrue(3 == fService3.fEvent2RecipientNumber);
            Assert.assertTrue(0 == fService1.fEvent3RecipientNumber);
            Assert.assertTrue(0 == fService2.fEvent3RecipientNumber);
            Assert.assertTrue(0 == fService3.fEvent3RecipientNumber);
        }}).get();
    }

    /**
     * Tests dispatching event 2.  The goal of the test is to make sure that 
     * both event 2 and even 3 recipients are called for this event.
     * <br>
     * Note: When a single listener object has more than one method that that
     * matches the event, both methods will be called.  But there is currently 
     * no guaranteed order in which they should be called.
     */
    @Test public void event3Test() throws ExecutionException, InterruptedException {
        fService1.dispatchEvent3();
        fExecutor.submit(new DsfRunnable() { public void run() {
            Assert.assertTrue(1 == fService1.fEvent1RecipientNumber || 2 == fService1.fEvent1RecipientNumber);
            Assert.assertTrue(3 == fService2.fEvent1RecipientNumber || 4 == fService2.fEvent1RecipientNumber);
            Assert.assertTrue(5 == fService3.fEvent1RecipientNumber || 6 == fService3.fEvent1RecipientNumber);
            Assert.assertTrue(0 == fService1.fEvent2RecipientNumber);
            Assert.assertTrue(0 == fService2.fEvent2RecipientNumber);
            Assert.assertTrue(0 == fService3.fEvent2RecipientNumber);
            Assert.assertTrue(1 == fService1.fEvent3RecipientNumber || 2 == fService1.fEvent3RecipientNumber);
            Assert.assertTrue(3 == fService2.fEvent3RecipientNumber || 4 == fService2.fEvent3RecipientNumber);
            Assert.assertTrue(5 == fService3.fEvent3RecipientNumber || 6 == fService3.fEvent3RecipientNumber);
        }}).get();
    }

}

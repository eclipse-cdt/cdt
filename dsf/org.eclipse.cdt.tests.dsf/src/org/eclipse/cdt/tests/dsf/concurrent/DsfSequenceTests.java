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
package org.eclipse.cdt.tests.dsf.concurrent;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import junit.framework.Assert;

import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.concurrent.ReflectionSequence;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Sequence;
import org.eclipse.cdt.tests.dsf.DsfTestPlugin;
import org.eclipse.cdt.tests.dsf.TestDsfExecutor;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests that exercise the Sequence object.
 */
public class DsfSequenceTests {
    TestDsfExecutor fExecutor;
    
    @Before 
    public void startExecutor() throws ExecutionException, InterruptedException {
        fExecutor = new TestDsfExecutor();
    }   
    
    @After 
    public void shutdownExecutor() throws ExecutionException, InterruptedException {
        if (!fExecutor.isShutdown()) {
            // Some tests shut down the executor deliberatly)
            
            fExecutor.submit(new DsfRunnable() { public void run() {
                fExecutor.shutdown();
            }}).get();
        }
        if (fExecutor.exceptionsCaught()) {
            Throwable[] exceptions = fExecutor.getExceptions();
            throw new ExecutionException(exceptions[0]);
        }
        fExecutor = null;
    }
    
    @Test 
    public void simpleTest() throws InterruptedException, ExecutionException {
        // Create a counter for tracking number of steps performed.
        class IntegerHolder { int fInteger; }
        final IntegerHolder stepCounter = new IntegerHolder();

        // Create the steps of the sequence
        final Sequence.Step[] steps = new Sequence.Step[] {
            new Sequence.Step() { 
                @Override
                public void execute(RequestMonitor requestMonitor) {
                    stepCounter.fInteger++;
                    requestMonitor.done(); 
                }
            },
            new Sequence.Step() { 
                @Override
                public void execute(RequestMonitor requestMonitor) {
                    stepCounter.fInteger++;
                    requestMonitor.done(); 
                }
            }
        };
        
        // Create, start, and wait for the sequence.
        Sequence sequence = new Sequence(fExecutor) {
            @Override public Step[] getSteps() { return steps; }
        };
        Assert.assertFalse(sequence.isDone());
        Assert.assertFalse(sequence.isCancelled());
        
        fExecutor.execute(sequence);
        sequence.get();

        // Check the count
        Assert.assertTrue(stepCounter.fInteger == 2);
        
        // Check post conditions
        Assert.assertTrue(sequence.isDone());
        Assert.assertFalse(sequence.isCancelled());
    }


    public class SimpleReflectionSequence extends ReflectionSequence {

        public int fStepCounter;
        
        public SimpleReflectionSequence() {
            super(fExecutor);
        }
        
        @Override
        protected String[] getExecutionOrder(String groupName) {
            return new String[] { "step1", "step2"};
        }
        
        @Execute()
        public void step1(RequestMonitor rm) {
            fStepCounter++;
            rm.done(); 
        }

        @Execute()
        public void step2(RequestMonitor rm) {
            fStepCounter++;
            rm.done(); 
        }
    }

    @Test 
    public void simpleReflectionTest() throws InterruptedException, ExecutionException {

        // Create, start, and wait for the sequence.
        SimpleReflectionSequence sequence = new SimpleReflectionSequence();

        //Sequence sequence = new SimpleReflectionSequence();
        Assert.assertFalse(sequence.isDone());
        Assert.assertFalse(sequence.isCancelled());
        
        fExecutor.execute(sequence);
        sequence.get();

        // Check the count
        Assert.assertTrue(sequence.fStepCounter == 2);
        
        // Check post conditions
        Assert.assertTrue(sequence.isDone());
        Assert.assertFalse(sequence.isCancelled());
    }

    
    @Test (expected = ExecutionException.class)
    public void rollbackTest() throws InterruptedException, ExecutionException {
        // Create a counter for tracking number of steps performed and steps 
        // rolled back.
        class IntegerHolder { int fInteger; }
        final IntegerHolder stepCounter = new IntegerHolder();
        final IntegerHolder rollBackCounter = new IntegerHolder();

        // Create the steps of the sequence
        final Sequence.Step[] steps = new Sequence.Step[] {
            new Sequence.Step() { 
                @Override public void execute(RequestMonitor requestMonitor) {
                    stepCounter.fInteger++;
                    requestMonitor.done(); 
                }
                @Override public void rollBack(RequestMonitor requestMonitor) {
                    rollBackCounter.fInteger++;
                    requestMonitor.done(); 
                }
            },
            new Sequence.Step() { 
                @Override public void execute(RequestMonitor requestMonitor) {
                    stepCounter.fInteger++;
                    requestMonitor.setStatus(new Status(IStatus.ERROR, DsfTestPlugin.PLUGIN_ID, -1, "", null));  //$NON-NLS-1$
                    requestMonitor.done(); 
                }
                @Override public void rollBack(RequestMonitor requestMonitor) {
                    rollBackCounter.fInteger++;
                    requestMonitor.done(); 
                }
            }
        };
        
        // Create and start.
        Sequence sequence = new Sequence(fExecutor) {
            @Override public Step[] getSteps() { return steps; }
        };
        fExecutor.execute(sequence);
     
        // Block and wait for sequence to complete.
        try {
            sequence.get();
        } finally {
            // Both steps should be performed
            Assert.assertTrue(stepCounter.fInteger == 2);
            // Only one step is rolled back, the first one.
            Assert.assertTrue(rollBackCounter.fInteger == 1);
            
            // Check state from Future interface
            Assert.assertTrue(sequence.isDone());
            Assert.assertFalse(sequence.isCancelled());            
        }
        Assert.assertTrue("Exception should have been thrown", false); //$NON-NLS-1$
    }

    @Test (expected = ExecutionException.class)
    public void rejectedTest() throws InterruptedException, ExecutionException {
        // Create a counter for tracking number of steps performed and steps 
        // rolled back.
        class IntegerHolder { int fInteger; }
        final IntegerHolder stepCounter = new IntegerHolder();
        final IntegerHolder rollBackCounter = new IntegerHolder();

        // Create the steps of the sequence
        final Sequence.Step[] steps = new Sequence.Step[] {
            new Sequence.Step() { 
                @Override public void execute(RequestMonitor requestMonitor) {
                    stepCounter.fInteger++;
                    requestMonitor.done(); 
                }
                @Override public void rollBack(RequestMonitor requestMonitor) {
                    rollBackCounter.fInteger++;
                    requestMonitor.done(); 
                }
            },
            new Sequence.Step() { 
                @Override public void execute(RequestMonitor requestMonitor) {
                    stepCounter.fInteger++;
                    // Shutdown exectutor to force a RejectedExecutionException
                    fExecutor.shutdown();
                    requestMonitor.done(); 
                }
                @Override public void rollBack(RequestMonitor requestMonitor) {
                    rollBackCounter.fInteger++;
                    requestMonitor.done(); 
                }
            }
        };
        
        // Create and start.
        Sequence sequence = new Sequence(fExecutor) {
            @Override public Step[] getSteps() { return steps; }
        };
        fExecutor.execute(sequence);
     
        // Block and wait for sequence to complete.
        try {
            sequence.get();
        } finally {
            // Both steps should be performed
            Assert.assertTrue(stepCounter.fInteger == 2);
            // No steps should be rolled back.
            Assert.assertTrue(rollBackCounter.fInteger == 0);
            
            // Check state from Future interface
            Assert.assertTrue(sequence.isDone());
            Assert.assertFalse(sequence.isCancelled());            
        }
        Assert.assertTrue("Exception should have been thrown", false); //$NON-NLS-1$
    }

    
    public class RollBackReflectionSequence extends ReflectionSequence {

        public int fStepCounter;
        public int fRollBackCounter;
        
        public RollBackReflectionSequence() {
            super(fExecutor);
        }

        @Override
        protected String[] getExecutionOrder(String groupName) {
            return new String[] { "step1", "step2"};
        }
        
        @Execute()
        public void step1(RequestMonitor rm) {
            fStepCounter++;
            rm.done(); 
        }

        @RollBack("step1")
        public void rollBack1(RequestMonitor rm) {
            fRollBackCounter++;
            rm.done(); 
        }

        @Execute()
        public void step2(RequestMonitor rm) {
            fStepCounter++;
            rm.setStatus(new Status(IStatus.ERROR, DsfTestPlugin.PLUGIN_ID, -1, "", null));  //$NON-NLS-1$
            rm.done(); 
        }

        @RollBack("step2")
        public void rollBack2(RequestMonitor rm) {
            fRollBackCounter++;
            rm.done(); 
        }
    }

    @Test (expected = ExecutionException.class)
    public void rollbackReflectionTest() throws InterruptedException, ExecutionException {
        // Create and start.
        RollBackReflectionSequence sequence = new RollBackReflectionSequence();
        fExecutor.execute(sequence);
     
        // Block and wait for sequence to complete.
        try {
            sequence.get();
        } finally {
            // Both steps should be performed
            Assert.assertEquals(2, sequence.fStepCounter);
            // Only one step is rolled back, the first one.
            Assert.assertEquals(1, sequence.fRollBackCounter);
            
            // Check state from Future interface
            Assert.assertTrue(sequence.isDone());
            Assert.assertFalse(sequence.isCancelled());            
        }
        Assert.assertTrue("Exception should have been thrown", false); //$NON-NLS-1$
    }
    
    public class RollBackReflectionSequence2 extends ReflectionSequence {

        public int fStepCounter;
        public int fRollBackCounter;
        
        public RollBackReflectionSequence2() {
            super(fExecutor);
        }

        @Override
        protected String[] getExecutionOrder(String groupName) {
            return new String[] { "step1", "step2", "step3" };
        }
        
        @Execute()
        public void step1(RequestMonitor rm) {
            fStepCounter++;
            rm.done(); 
        }

        @RollBack("step1")
        public void rollBack1(RequestMonitor rm) {
        	fRollBackCounter++;
        	rm.done(); 
        }
        @Execute()

        public void step2(RequestMonitor rm) {
            fStepCounter++;
            rm.done(); 
        }
        
        @Execute()
        public void step3(RequestMonitor rm) {
            fStepCounter++;
            rm.setStatus(new Status(IStatus.ERROR, DsfTestPlugin.PLUGIN_ID, -1, "", null));  //$NON-NLS-1$
            rm.done(); 
        }
    }
    
    @Test (expected = ExecutionException.class)
    public void rollbackReflectionWithoutRollBackMethodTest() throws InterruptedException, ExecutionException {
        // Create and start.
    	RollBackReflectionSequence2 sequence = new RollBackReflectionSequence2();
        fExecutor.execute(sequence);
     
        // Block and wait for sequence to complete.
        try {
            sequence.get();
        } finally {
            // All three steps should be performed
            Assert.assertEquals(3, sequence.fStepCounter);
            // Two steps are rolled back, but only the first one has
            // a rollback method.
            Assert.assertEquals(1, sequence.fRollBackCounter);
            
            // Check state from Future interface
            Assert.assertTrue(sequence.isDone());
            Assert.assertFalse(sequence.isCancelled());                        
        }
        Assert.assertTrue("Exception should have been thrown", false); //$NON-NLS-1$
    }

    
    /**
     * The goal of this test it to check that if an exception is thrown within
     * the Step.execute(), the step will return from the Future.get() method.
     */
    @Test (expected = ExecutionException.class)
    public void exceptionTest() throws InterruptedException, ExecutionException {
        final Sequence.Step[] steps = new Sequence.Step[] {
            new Sequence.Step() { 
                @Override public void execute(RequestMonitor requestMonitor) {
                    throw new Error("Exception part of unit test."); //$NON-NLS-1$
                }
            }
        };
        
        // Create and start.
        Sequence sequence = new Sequence(fExecutor) {
            @Override public Step[] getSteps() { return steps; }
        };
        fExecutor.execute(sequence);
     
        // Block and wait for sequence to bomplete.
        try {
            sequence.get();
        } finally {
            // Check state from Future interface
            Assert.assertTrue(sequence.isDone());
            Assert.assertFalse(sequence.isCancelled());            
        }
        Assert.assertTrue("Exception should have been thrown", false); //$NON-NLS-1$
    }

    
    @Test (expected = CancellationException.class)
    public void cancelBeforeWaitingTest() throws InterruptedException, ExecutionException {
        // Create the sequence
        final Sequence.Step[] steps = new Sequence.Step[] {
                new Sequence.Step() { 
                    @Override public void execute(RequestMonitor requestMonitor) {
                        Assert.assertTrue("Sequence was cancelled, it should not be called.", false); //$NON-NLS-1$
                    }
                }
            };
        Sequence sequence = new Sequence(fExecutor) {
            @Override public Step[] getSteps() { return steps; }
        };

        // Cancel before invoking the sequence.
        sequence.cancel(false);

        Assert.assertFalse(sequence.isDone());
        Assert.assertTrue(sequence.isCancelled());

        // Start the sequence
        fExecutor.execute(sequence);
        
        // Block and wait for sequence to bomplete.
        try {
            sequence.get();
        } finally {
            Assert.assertTrue(sequence.isDone());
            Assert.assertTrue(sequence.isCancelled());            
        }
        Assert.assertTrue("CancellationException should have been thrown", false); //$NON-NLS-1$
    }
    

    @Test (expected = CancellationException.class)
    public void cancelFromStepTest() throws InterruptedException, ExecutionException {
        // Create a counter for tracking number of steps performed and steps 
        // rolled back.
        class IntegerHolder { int fInteger; }
        final IntegerHolder stepCounter = new IntegerHolder();
        final IntegerHolder rollBackCounter = new IntegerHolder();

        // Create the steps of the sequence
        final Sequence.Step[] steps = new Sequence.Step[] {
            new Sequence.Step() { 
                @Override public void execute(RequestMonitor requestMonitor) {
                    stepCounter.fInteger++;
                    requestMonitor.done(); 
                }
                @Override public void rollBack(RequestMonitor requestMonitor) {
                    rollBackCounter.fInteger++;
                    requestMonitor.done(); 
                }
            },
            new Sequence.Step() { 
                @Override public void execute(RequestMonitor requestMonitor) {
                    stepCounter.fInteger++;
                    
                    // Perform the cancel!
                    getSequence().cancel(false); 
                    
                    requestMonitor.done(); 
                }
                @Override public void rollBack(RequestMonitor requestMonitor) {
                    rollBackCounter.fInteger++;
                    requestMonitor.done(); 
                }
            }
        };
        
        // Create and start sequence with a delay.  Delay so that we call get() before
        // cancel is called.
        final Sequence sequence = new Sequence(fExecutor) {
            @Override public Step[] getSteps() { return steps; }
        };
        fExecutor.schedule(sequence, 1, TimeUnit.MILLISECONDS);

        // Block to retrieve data
        try {
            sequence.get();
        } finally {
            // Both steps should be performed
            Assert.assertTrue(stepCounter.fInteger == 2);
            // Both roll-backs should be performed since cancel does not take effect until
            // after the step is completed.
            Assert.assertTrue(rollBackCounter.fInteger == 2);
            
            Assert.assertTrue(sequence.isDone());
            Assert.assertTrue(sequence.isCancelled());            
        }            
        Assert.assertTrue("CancellationException should have been thrown", false); //$NON-NLS-1$
    }
    
    @Test (expected = CancellationException.class)
    public void cancelBeforeWithProgressManagerTest() throws InterruptedException, ExecutionException {
        // Create the sequence
        final Sequence.Step[] steps = new Sequence.Step[] {
                new Sequence.Step() { 
                    @Override public void execute(RequestMonitor requestMonitor) {
                        Assert.assertTrue("Sequence was cancelled, it should not be called.", false); //$NON-NLS-1$
                    }
                }
            };
        
        // Create the progress monitor that we will cancel.
        IProgressMonitor pm = new NullProgressMonitor();
        
        // Create the seqeunce with our steps.
        Sequence sequence = new Sequence(fExecutor, pm, "", "") { //$NON-NLS-1$ //$NON-NLS-2$
            @Override public Step[] getSteps() { return steps; }
        };

        // Cancel the progress monitor before invoking the sequence.  Note 
        // that the state of the sequence doesn't change yet, because the 
        // sequence does not check the progress monitor until it is executed.
        pm.setCanceled(true);

        // Start the sequence
        fExecutor.execute(sequence);
        
        // Block and wait for sequence to bomplete.  Exception is thrown, 
        // which is expected.
        try {
            sequence.get();
        } finally {
            Assert.assertTrue(sequence.isDone());
            Assert.assertTrue(sequence.isCancelled());            
        }
    }

    
}
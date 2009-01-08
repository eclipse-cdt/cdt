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
package org.eclipse.cdt.tests.dsf.concurrent;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import junit.framework.Assert;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.tests.dsf.DsfTestPlugin;
import org.eclipse.cdt.tests.dsf.TestDsfExecutor;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests that exercise the Query object.
 */
public class DsfQueryTests {
    TestDsfExecutor fExecutor;
    
    @Before 
    public void startServices() throws ExecutionException, InterruptedException {
        fExecutor = new TestDsfExecutor();
    }   
    
    @After 
    public void shutdownServices() throws ExecutionException, InterruptedException {
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
    public void simpleGetTest() throws InterruptedException, ExecutionException {
        Query<Integer> q = new Query<Integer>() { 
            @Override
            protected void execute(DataRequestMonitor<Integer> rm) {
                rm.setData(1);
                rm.done();
            }
        };
        // Check initial state
        Assert.assertTrue(!q.isDone());
        Assert.assertTrue(!q.isCancelled());
        
        fExecutor.execute(q);
        Assert.assertEquals(1, (int)q.get());
        
        // Check final state
        Assert.assertTrue(q.isDone());
        Assert.assertTrue(!q.isCancelled());

    }

    @Test 
    public void getWithMultipleDispatchesTest() throws InterruptedException, ExecutionException {
        Query<Integer> q = new Query<Integer>() { 
            @Override
            protected void execute(final DataRequestMonitor<Integer> rm) {
                fExecutor.execute(new DsfRunnable() { 
                    public void run() {
                        rm.setData(1);
                        rm.done();
                    }
                    @Override
                    public String toString() { return super.toString() + "\n       getWithMultipleDispatchesTest() second runnable"; } //$NON-NLS-1$
                });
            }
            @Override
            public String toString() { return super.toString() + "\n       getWithMultipleDispatchesTest() first runnable (query)"; } //$NON-NLS-1$
        };
        fExecutor.execute(q);
        Assert.assertEquals(1, (int)q.get()); 
    }

    @Test (expected = ExecutionException.class)
    public void exceptionOnGetTest() throws InterruptedException, ExecutionException {
        Query<Integer> q = new Query<Integer>() { 
            @Override
            protected void execute(final DataRequestMonitor<Integer> rm) {
                rm.setStatus(new Status(IStatus.ERROR, DsfTestPlugin.PLUGIN_ID, -1, "", null)); //$NON-NLS-1$
                rm.done();
            }
        };
        
        fExecutor.execute(q);
        
        try {
            q.get();
        } finally {
            Assert.assertTrue(q.isDone());
            Assert.assertTrue(!q.isCancelled());
        }            
    }

    @Test
    public void cancelWhileWaitingTest() throws InterruptedException, ExecutionException {
        final Query<Integer> q = new Query<Integer>() { 
            @Override
            protected void execute(final DataRequestMonitor<Integer> rm) {
                // Call done with a delay of 1 second, to avoid stalling the tests.
                fExecutor.schedule(
                    new DsfRunnable() {
                        public void run() { rm.done(); }
                    }, 
                    1, TimeUnit.SECONDS);
            }
        };

        fExecutor.execute(q);

        // Note: no point in checking isDone() and isCancelled() here, because
        // the value could change on timing.
        
        // This does not really guarantee that the cancel will be called after
        // the call to Fugure.get(), but the 1ms delay in call to schedule should
        // help.
        new Job("DsfQueryTests cancel job") { @Override public IStatus run(IProgressMonitor monitor) { //$NON-NLS-1$
            q.cancel(false);
            return Status.OK_STATUS;
        }}.schedule(1);
        
        try {
            q.get();
        } catch (CancellationException e) {
            return; // Success
        } finally {
            Assert.assertTrue(q.isDone());
            Assert.assertTrue(q.isCancelled());            
        }            
        Assert.assertTrue("CancellationException should have been thrown", false); //$NON-NLS-1$
    }

    @Test
    public void cancelBeforeWaitingTest() throws InterruptedException, ExecutionException {
        final Query<Integer> q = new Query<Integer>() { 
            @Override protected void execute(final DataRequestMonitor<Integer> rm) {
                Assert.fail("Query was cancelled, it should not be called."); //$NON-NLS-1$
                rm.done();
            }
        };
        
        // Cancel before invoking the query.
        q.cancel(false);

        Assert.assertTrue(q.isDone());
        Assert.assertTrue(q.isCancelled());            

        // Start the query.
        fExecutor.execute(q);
        
        // Block to retrieve data
        try {
            q.get();
        } catch (CancellationException e) {
            return; // Success
        } finally {
            Assert.assertTrue(q.isDone());
            Assert.assertTrue(q.isCancelled());            
        }            
        Assert.assertTrue("CancellationException should have been thrown", false); //$NON-NLS-1$
    }
    
    @Test
    public void getTimeoutTest() throws InterruptedException, ExecutionException {
        final Query<Integer> q = new Query<Integer>() { 
            @Override
            protected void execute(final DataRequestMonitor<Integer> rm) {
                // Call done with a delay of 1 second, to avoid stalling the tests.
                fExecutor.schedule(
                    new DsfRunnable() {
                        public void run() { rm.done(); }
                    }, 
                    1, TimeUnit.SECONDS);
            }
        };

        fExecutor.execute(q);

        // Note: no point in checking isDone() and isCancelled() here, because
        // the value could change on timing.
        
        try {
            q.get(1, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            return; // Success
        } finally {
            Assert.assertFalse("Query should not be done yet, it should have timed out first.", q.isDone()); //$NON-NLS-1$
        }            
        Assert.assertTrue("TimeoutException should have been thrown", false); //$NON-NLS-1$
    }

}

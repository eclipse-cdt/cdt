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

import java.util.Arrays;
import java.util.concurrent.ExecutionException;

import junit.framework.Assert;

import org.eclipse.cdt.dsf.concurrent.RequestCache;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.concurrent.ImmediateInDsfExecutor;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.concurrent.Transaction;
import org.eclipse.cdt.tests.dsf.TestDsfExecutor;
import org.eclipse.core.runtime.CoreException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests that exercise the Transaction object.
 */
public class TransactionTests {
    final static private int NUM_CACHES = 5; 
        
    TestDsfExecutor fExecutor;
    TestCache[] fTestCaches = new TestCache[NUM_CACHES];
    DataRequestMonitor<?>[] fRetrieveRms = new DataRequestMonitor<?>[NUM_CACHES];

    class TestCache extends RequestCache<Integer> {
        
        final private int fIndex;
        
        public TestCache(int index) {
            super(new ImmediateInDsfExecutor(fExecutor));
            fIndex = index;
        }

        @Override
        protected void retrieve(DataRequestMonitor<Integer> rm) {
            synchronized(TransactionTests.this) {
                fRetrieveRms[fIndex] = rm;
                TransactionTests.this.notifyAll();
            }
        }
        
    }

    class TestSingleTransaction extends Transaction<Integer> {

        @Override
        protected Integer process() throws InvalidCacheException, CoreException {
            validate(fTestCaches[0]);
            return fTestCaches[0].getData();
        }
    }

    class TestSumTransaction extends Transaction<Integer> {
        @Override
        protected Integer process() throws InvalidCacheException, CoreException {
            validate(fTestCaches);
            
            int sum =  0;
            for (RequestCache<Integer> cache : fTestCaches) {
                sum += cache.getData();
            }
            return sum;
        }
    }
    
    /**
     * There's no rule on how quickly the cache has to start data retrieval
     * after it has been requested.  It could do it immediately, or it could
     * wait a dispatch cycle, etc..
     */
    private void waitForRetrieveRm(boolean all) {
        synchronized(this) {
            if (all) {
                while (Arrays.asList(fRetrieveRms).contains(null)) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        return;
                    }
                }
            } else {
                while (fRetrieveRms[0] == null) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        return;
                    }
                }
            }
        }
    }
    
    @Before 
    public void startExecutor() throws ExecutionException, InterruptedException {
        fExecutor = new TestDsfExecutor();
        for (int i = 0; i < fTestCaches.length; i++) {
            fTestCaches[i] = new TestCache(i);
        }
    }   
    
    @After 
    public void shutdownExecutor() throws ExecutionException, InterruptedException {
        fExecutor.submit(new DsfRunnable() { public void run() {
            fExecutor.shutdown();
        }}).get();
        if (fExecutor.exceptionsCaught()) {
            Throwable[] exceptions = fExecutor.getExceptions();
            throw new ExecutionException(exceptions[0]);
        }
        fRetrieveRms = new DataRequestMonitor<?>[NUM_CACHES];
        fTestCaches = new TestCache[NUM_CACHES];
        fExecutor = null;
    }

    @Test 
    public void singleTransactionTest() throws InterruptedException, ExecutionException {
        final TestSingleTransaction testTransaction = new TestSingleTransaction();
        // Request data from cache
        Query<Integer> q = new Query<Integer>() { 
            @Override
            protected void execute(DataRequestMonitor<Integer> rm) {
                testTransaction.request(rm);
            }
        };
        fExecutor.execute(q);
        
        // Wait until the cache starts data retrieval.
        waitForRetrieveRm(false);

        // Set the data without using an executor.  
        ((DataRequestMonitor<Integer>)fRetrieveRms[0]).setData(1);
        fRetrieveRms[0].done();
        
        Assert.assertEquals(1, (int)q.get());
    }
    
    @Test 
    public void sumTransactionTest() throws InterruptedException, ExecutionException {

        final TestSumTransaction testTransaction = new TestSumTransaction();
        // Request data from cache
        Query<Integer> q = new Query<Integer>() { 
            @Override
            protected void execute(DataRequestMonitor<Integer> rm) {
                testTransaction.request(rm);
            }
        };
        fExecutor.execute(q);
        
        // Wait until the cache starts data retrieval.
        waitForRetrieveRm(true);

        // Set the data without using an executor.
        for (DataRequestMonitor<?> rm : fRetrieveRms) {
            ((DataRequestMonitor<Integer>)rm).setData(1);
            rm.done();
        }
        
        fExecutor.execute(q);
        Assert.assertEquals(NUM_CACHES, (int)q.get());
    }

}

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

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import junit.framework.Assert;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.concurrent.ICache;
import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.concurrent.ImmediateInDsfExecutor;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.concurrent.RangeCache;
import org.eclipse.cdt.tests.dsf.DsfTestPlugin;
import org.eclipse.cdt.tests.dsf.TestDsfExecutor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests that exercise the DataCache object.
 */
public class RangeCacheTests {

    class TestRangeCache extends RangeCache<Integer> {
        
        public TestRangeCache() {
            super(new ImmediateInDsfExecutor(fExecutor));
        }
        
        @Override
        protected void retrieve(long offset, int count, DataRequestMonitor<List<Integer>> rm) {
            fRetrieveInfos.add(new RetrieveInfo(offset, count, rm));
        }
        
        @Override
        public void reset() {
            super.reset();
        }

        @Override
        public void set(long offset, int count, List<Integer> data, IStatus status) {
            super.set(offset, count, data, status);
        }
    }

    class TestQuery extends Query<List<Integer>> {
        long fOffset;
        int fCount;
        TestQuery(long offset, int count) {
            fOffset = offset;
            fCount = count;
        }        
        
        @Override
        protected void execute(DataRequestMonitor<List<Integer>> rm) {
            fRangeCache = fTestCache.getRange(fOffset, fCount);
            fRangeCache.update(rm);
        }
    }

    class RetrieveInfo implements Comparable<RetrieveInfo> {
        long fOffset;
        int fCount;
        DataRequestMonitor<List<Integer>> fRm;
        RetrieveInfo(long offset, int count, DataRequestMonitor<List<Integer>> rm) {
            fOffset = offset;
            fCount = count;
            fRm = rm;
        }
        
        public int compareTo(RetrieveInfo o) {
            if (fOffset > o.fOffset) {
                return 1;
            } else if (fOffset == o.fOffset) {
                return 0;
            } else /*if (fOffset < o.fOffset)*/ {
                return -1;
            }
        }
    }

    TestDsfExecutor fExecutor;
    TestRangeCache fTestCache;    
    SortedSet<RetrieveInfo> fRetrieveInfos;
    ICache<List<Integer>> fRangeCache;
    
    private List<Integer> makeList(long offset, int count) {
        List<Integer> list = new ArrayList<Integer>(count);
        for (int i = 0; i < count; i++) {
            list.add((int)(i + offset));
        }
        return list;
    }
    
    /**
     * There's no rule on how quickly the cache has to start data retrieval
     * after it has been requested.  It could do it immediately, or it could
     * wait a dispatch cycle, etc..
     */
    private void waitForRetrieveRm(int size) {
        synchronized(this) {
            while (fRetrieveInfos.size() < size) {
                try {
                    wait(100);
                } catch (InterruptedException e) {
                    return;
                } 
            }
        }
    }
    
    @Before 
    public void startExecutor() throws ExecutionException, InterruptedException {
        fExecutor = new TestDsfExecutor();
        fTestCache = new TestRangeCache();
        fRetrieveInfos = new TreeSet<RetrieveInfo>();
        fRangeCache = null;
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
        fTestCache = null;
        fExecutor = null;
    }

    private void assertCacheValidWithData(ICache<List<Integer>> cache, long offset, int count) {
        Assert.assertTrue(cache.isValid());
        Assert.assertEquals(makeList(offset, count), cache.getData());
        Assert.assertTrue(cache.getStatus().isOK());
    }

    private void assertCacheWaiting(ICache<List<Integer>> cache) {
        Assert.assertFalse(cache.isValid());
        try {
            cache.getData();
            Assert.fail("Expected an IllegalStateException");
        } catch (IllegalStateException e) {}
        try {
            cache.getStatus();
            Assert.fail("Expected an IllegalStateException");
        } catch (IllegalStateException e) {}
    }

    private void completeInfo(RetrieveInfo info, long offset, int count) {
        Assert.assertEquals(offset, info.fOffset);
        Assert.assertEquals(count, info.fCount);
        info.fRm.setData(makeList(offset, count));
        info.fRm.done();
    }
    
    private void getRange(long queryOffset, int queryCount, long[] retrieveOffsets, int retrieveCounts[]) throws InterruptedException, ExecutionException {
        assert retrieveOffsets.length == retrieveCounts.length;
        int retrieveCount = retrieveOffsets.length;
        
        // Request data from cache
        TestQuery q = new TestQuery(queryOffset, queryCount);

        fRangeCache = null;
        fRetrieveInfos.clear();
        
        fExecutor.execute(q);
        
        // Wait until the cache requests the data.
        waitForRetrieveRm(retrieveOffsets.length);
        
        if (retrieveCount != 0) {
            assertCacheWaiting(fRangeCache);
            
            // Set the data without using an executor.
            Assert.assertEquals(retrieveCount, fRetrieveInfos.size());
            int i = 0; 
            for (RetrieveInfo info : fRetrieveInfos) {
                completeInfo(info, retrieveOffsets[i], retrieveCounts[i]);
                i++;
            }
        }
        
        // Wait for data.
        Assert.assertEquals(makeList(queryOffset, queryCount), q.get());
        
        // Check state while waiting for data
        assertCacheValidWithData(fRangeCache, queryOffset, queryCount);
    }
    
    @Test 
    public void getOneRangeTest() throws InterruptedException, ExecutionException {
        getRange(0, 100, new long[] { 0 }, new int[] { 100 });
    }
    
    @Test 
    public void getMultipleRangesTest() throws InterruptedException, ExecutionException {
        // Retrieve a range in-between two cached ranges
        getRange(0, 100, new long[] { 0 }, new int[] { 100 });
        getRange(200, 100, new long[] { 200 }, new int[] { 100 });
        getRange(0, 300, new long[] { 100 }, new int[] { 100 });

        // Retrieve a range overlapping two cached ranges
        getRange(1000, 100, new long[] { 1000 }, new int[] { 100 });
        getRange(1200, 100, new long[] { 1200 }, new int[] { 100 });
        getRange(900, 500, new long[] { 900, 1100, 1300 }, new int[] { 100, 100, 100 });

        // Retrieve a range that's a subset of a cached range.
        getRange(2000, 100, new long[] { 2000 }, new int[] { 100 });
        getRange(2000, 50, new long[] {}, new int[] {});
        getRange(2025, 50, new long[] {}, new int[] {});
        getRange(2050, 50, new long[] {}, new int[] {});
    }

    private void cancelRange(long queryOffset, int queryCount, long[] retrieveOffsets, int retrieveCounts[]) throws Exception {
        int retrieveCount = retrieveOffsets.length;
        
        // Request data from cache
        TestQuery q = new TestQuery(queryOffset, queryCount);

        fRangeCache = null;
        fRetrieveInfos.clear();
        
        fExecutor.execute(q);
        
        // Wait until the cache requests the data.
        waitForRetrieveRm(retrieveCount);
        
        assertCacheWaiting(fRangeCache);

        // Set the data without using an executor.
        Assert.assertEquals(retrieveCount, fRetrieveInfos.size());
        int i = 0; 
        for (RetrieveInfo info : fRetrieveInfos) {
            Assert.assertEquals(retrieveOffsets[i], info.fOffset);
            Assert.assertEquals(retrieveCounts[i], info.fCount);
            Assert.assertFalse(info.fRm.isCanceled());
            i++;
        }
        
        q.cancel(true);
        try {
            q.get();
            Assert.fail("Expected a cancellation exception");
        } catch (CancellationException e) {} // Expected exception;
        
        for (RetrieveInfo info : fRetrieveInfos) {
            Assert.assertTrue(info.fRm.isCanceled());
        }
    }

    @Test 
    public void cancelOneRangeTest() throws Exception {
        cancelRange(0, 100, new long[] { 0 }, new int[] { 100 });
    }

    @Test 
    public void cancelMultipleRangesTest() throws Exception {
        // Cancel a couple of ranges.
        cancelRange(0, 100, new long[] { 0 }, new int[] { 100 });
        cancelRange(200, 100, new long[] { 200 }, new int[] { 100 });
        
        // Cancel a range overlapping two previously canceled ranges.
        cancelRange(0, 300, new long[] { 0 }, new int[] { 300 });
    }

    @Test
    public void getAndCancelMultipleRangesTest() throws Exception {
        // Cancel a range, then retrieve the same range 
        cancelRange(0, 100, new long[] { 0 }, new int[] { 100 });
        getRange(0, 100, new long[] { 0 }, new int[] { 100 });
        
        // Cancel a range overlapping a cached range.
        cancelRange(0, 200, new long[] { 100 }, new int[] { 100 });
    }

    @Test 
    public void resetOneRangeTest() throws InterruptedException, ExecutionException {
        getRange(0, 100, new long[] { 0 }, new int[] { 100 });
        
        fExecutor.submit(new DsfRunnable() {
            public void run() {
                fTestCache.reset();
            };
        }).get();
        
        getRange(0, 100, new long[] { 0 }, new int[] { 100 });
    }
    
    @Test 
    public void resetMultipleRangesTest() throws InterruptedException, ExecutionException {
        // Retrieve a range in-between two cached ranges
        getRange(0, 100, new long[] { 0 }, new int[] { 100 });
        getRange(200, 100, new long[] { 200 }, new int[] { 100 });
        getRange(0, 300, new long[] { 100 }, new int[] { 100 });

        // Retrieve a range overlapping two cached ranges
        getRange(1000, 100, new long[] { 1000 }, new int[] { 100 });
        getRange(1200, 100, new long[] { 1200 }, new int[] { 100 });
        getRange(900, 500, new long[] { 900, 1100, 1300 }, new int[] { 100, 100, 100 });

        // Retrieve a range that's a subset of a cached range.
        getRange(2000, 100, new long[] { 2000 }, new int[] { 100 });
        getRange(2000, 50, new long[] {}, new int[] {});
        getRange(2025, 50, new long[] {}, new int[] {});
        getRange(2050, 50, new long[] {}, new int[] {});
        
        fExecutor.submit(new DsfRunnable() {
            public void run() {
                fTestCache.reset();
            };
        }).get();
        
        // Retrieve a range in-between two cached ranges
        getRange(0, 100, new long[] { 0 }, new int[] { 100 });
        getRange(200, 100, new long[] { 200 }, new int[] { 100 });
        getRange(0, 300, new long[] { 100 }, new int[] { 100 });

        // Retrieve a range overlapping two cached ranges
        getRange(1000, 100, new long[] { 1000 }, new int[] { 100 });
        getRange(1200, 100, new long[] { 1200 }, new int[] { 100 });
        getRange(900, 500, new long[] { 900, 1100, 1300 }, new int[] { 100, 100, 100 });

        // Retrieve a range that's a subset of a cached range.
        getRange(2000, 100, new long[] { 2000 }, new int[] { 100 });
        getRange(2000, 50, new long[] {}, new int[] {});
        getRange(2025, 50, new long[] {}, new int[] {});
        getRange(2050, 50, new long[] {}, new int[] {});
    }

    @Test 
    public void resetWhileInvalidTest() throws InterruptedException, ExecutionException {
        // Request data from cache
        TestQuery q = new TestQuery(10, 100);

        fRangeCache = null;
        fRetrieveInfos.clear();
        
        fExecutor.execute(q);
        
        // Wait until the cache requests the data.
        waitForRetrieveRm(1);
        
        assertCacheWaiting(fRangeCache);
        
        fExecutor.submit(new DsfRunnable() {
            public void run() {
                fTestCache.reset();
            };
        }).get();
        
        // Set the data without using an executor.
        Assert.assertEquals(1, fRetrieveInfos.size());
        completeInfo(fRetrieveInfos.first(), 10, 100);
        
        // Wait for data.
        Assert.assertEquals(makeList(10, 100), q.get());
        
        // Check state while waiting for data
        assertCacheValidWithData(fRangeCache, 10, 100);
    }

    @Test 
    public void setOneRangeTest() throws InterruptedException, ExecutionException {
        getRange(0, 100, new long[] { 0 }, new int[] { 100 });
        
        fExecutor.submit(new DsfRunnable() {
            public void run() {
                fTestCache.set(0, 100, null, new Status(IStatus.ERROR, DsfTestPlugin.PLUGIN_ID, IDsfStatusConstants.INVALID_STATE, "Cache invalid", null));
            };
        }).get();
        
        // Request data from cache
        TestQuery q = new TestQuery(10, 100);

        fRangeCache = null;
        fRetrieveInfos.clear();
        
        fExecutor.execute(q);

        try {
            q.get();
            Assert.fail("Expected an ExecutionException");            
        } catch (ExecutionException e) {}
    }
    
    
}

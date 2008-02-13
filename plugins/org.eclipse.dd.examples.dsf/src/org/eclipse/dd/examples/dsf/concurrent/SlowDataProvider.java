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
package org.eclipse.dd.examples.dsf.concurrent;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.dd.dsf.concurrent.DsfRunnable;
import org.eclipse.dd.dsf.concurrent.RequestMonitor;
import org.eclipse.dd.dsf.concurrent.DsfExecutor;
import org.eclipse.dd.dsf.concurrent.DataRequestMonitor;
import org.eclipse.dd.examples.dsf.DsfExamplesPlugin;

/**
 * Example data provider which has a built-in delay when fetching data.  This 
 * data provider simulates a service which retrieves data from an external 
 * source such as a networked target, which incurs a considerable delay when 
 * retrieving data.  The data items are simulated values which consist of the 
 * time when data is being retrieved followed by the item's index.   
 */
public class SlowDataProvider implements DataProvider {
    
    /** Minimum count of data items */
    private final static int MIN_COUNT = 1000;
    
    /** Maximum count of data items */
    private final static int MAX_COUNT = 2000;
    
    /** Time interval how often random changes occur. */
    private final static int RANDOM_CHANGE_MILIS = 10000;
    
    /** Number of times random changes are made, before count is changed. */
    private final static int RANDOM_COUNT_CHANGE_INTERVALS = 3;
    
    /** Percentage of values that is changed upon random change (0-100). */
    private final static int RANDOM_CHANGE_SET_PERCENTAGE = 10;

    /** 
     * Amount of time (in miliseconds) how long the requests to provider, and 
     * events from provider are delayed by.
     */
    private final static int TRANSMISSION_DELAY_TIME = 500;
    
    /**
     * Amount of time (in milliseconds) how long the provider takes to process
     * a request.
     */
    private final static int PROCESSING_TIME = 100;
    
    /** Dispatch-thread executor that this provider uses. */
    private DsfExecutor fExecutor;
    
    /** List of listeners registered for events from provider. */
    private List<Listener> fListeners = new LinkedList<Listener>();
    
    /**  Thread that handles data requests. */
    private ProviderThread fProviderThread;
    
    /** Queue of currently pending data requests. */
    private final BlockingQueue<Request> fQueue = new DelayQueue<Request>();

    /**
     * Runnable to be submitted when the data provider thread is shut down.  
     * This variable acts like a flag: when client want to shut down the 
     * provider, it sets this runnable, and when the backgroun thread sees
     * that it's set, it shuts itself down, and posts this runnable with
     * the executor.
     */
    private RequestMonitor fShutdownRequestMonitor = null;
    
    /**
     * Base class for requests that are queued by the data provider.  It 
     * implements java.util.concurrent.Delayed to allow for use of DelayedQueue.
     * Every request into the queue is delayed by the simulated transmission 
     * time. 
     */
    private static abstract class Request implements Delayed {
        /** Sequence counter and number are used to ensure FIFO order **/
        private static int fSequenceCounter = 0;
        private int fSequenceNumber = fSequenceCounter++;
        
        /** Time delay tracks how items will be delayed. **/
        private long fTime = System.currentTimeMillis() + TRANSMISSION_DELAY_TIME;
        
        // @see java.util.concurrent.Delayed
        public long getDelay(TimeUnit unit) {
            return unit.convert(fTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
        }

        // @see java.lang.Comparable
        public int compareTo(Delayed other) {
            if (other == this) // compare zero ONLY if same object
                return 0;
            Request x = (Request)other;
            long diff = fTime - x.fTime;

            if (diff < 0) return -1;
            else if (diff > 0) return 1;
            else if (fSequenceNumber < x.fSequenceNumber) return -1;
            else return 1;
        }

        /** All requests have an associated RequestMonitor token **/
        abstract RequestMonitor getRequestMonitor();
    }

    /**
     * Object used to encapsulate the "getItemCount" requests.  Instances of it 
     * are queued till processed.
     */
    private static class CountRequest extends Request
    {
        DataRequestMonitor<Integer>  fRequestMonitor;
        CountRequest(DataRequestMonitor<Integer> rm) { fRequestMonitor = rm; }
        @Override
        DataRequestMonitor<?> getRequestMonitor() { return fRequestMonitor; }
    } 

    /**
     * Object used to encapsulate the "getItem" requests.  Instances of it 
     * are queued till processed.
     */
    private static class ItemRequest extends Request
    {
        DataRequestMonitor<String>  fRequestMonitor;
        int fIndex;
        ItemRequest(int index, DataRequestMonitor<String> rm) { fIndex = index; fRequestMonitor = rm; }
        @Override
        DataRequestMonitor<?> getRequestMonitor() { return fRequestMonitor; }
    } 

    /**
     * The background thread of data provider.  This thread retrieves the 
     * requests from the provider's queue and processes them.  It also 
     * initiates random changes in the data set and issues corresponding 
     * events.
     */
    private class ProviderThread extends Thread 
    {        
        /**
         * Current count of items in the data set.  It is changed 
         * periodically for simulation purposes.
         */
        private int    fCount = MIN_COUNT;
        
        /**
         * Incremented with every data change, it causes the count to be reset
         * every four random changes.
         */
        private int    fCountTrigger = 0;
        
        /** Time when the last change was performed. */
        private long   fLastChangeTime = System.currentTimeMillis();
        
        /** Random number generator */
        private Random fRandom = new java.util.Random();
 
        @Override
        public void run() {
            try {
                 // Initialize the count.
                randomCount();
                
                // Perform the loop until the shutdown runnable is set.
                while(fShutdownRequestMonitor == null) {
                    // Get the next request from the queue.  The time-out 
                    // ensures that that we get to process the random changes. 
                    final Request request = fQueue.poll(RANDOM_CHANGE_MILIS / 10, TimeUnit.MILLISECONDS);
                    
                    // If a request was dequeued, process it.
                    if (request != null) {
                        // Simulate a processing delay.
                        Thread.sleep(PROCESSING_TIME);
                        
                        if (request instanceof CountRequest) {
                            processCountRequest((CountRequest)request);
                        } else if (request instanceof ItemRequest) {
                            processItemRequest((ItemRequest)request);
                        }
                        // Whatever the result, post it to dispatch thread 
                        // executor (with transmission delay). 
                        fExecutor.schedule(
                            new DsfRunnable() { 
                                public void run() {
                                    request.getRequestMonitor().done();
                                }
                            }, 
                            TRANSMISSION_DELAY_TIME, TimeUnit.MILLISECONDS);
                    }
                    
                    // Simulate data changes.
                    randomChanges();
                }
            }
            catch (InterruptedException x) {
                DsfExamplesPlugin.getDefault().getLog().log( new Status(
                    IStatus.ERROR, DsfExamplesPlugin.PLUGIN_ID, 0, "Interrupted exception in slow data provider thread.", x )); //$NON-NLS-1$
            }
            
            // Notify the client that requested shutdown, that shutdown is complete.
            fShutdownRequestMonitor.done();
            fShutdownRequestMonitor = null;
        } 

        private void processCountRequest(CountRequest request) {
            // Calculate the simulated values.
            request.fRequestMonitor.setData(fCount);
        }

        private void processItemRequest(ItemRequest request) {
            // Calculate the simulated values.
            request.fRequestMonitor.setData(Long.toHexString(fLastChangeTime) + "." + request.fIndex); //$NON-NLS-1$
        } 
 
        /**
         * This method simulates changes in provider's data set.
         */
        private void randomChanges() 
        {
            if (System.currentTimeMillis() > fLastChangeTime + RANDOM_CHANGE_MILIS) {
                fLastChangeTime = System.currentTimeMillis();
                // once in every 30 seconds broadcast item count change
                if (++fCountTrigger % RANDOM_COUNT_CHANGE_INTERVALS == 0) randomCount();
                else randomDataChange();
            }
        }
         
         
        /**
         * Calculates new size for provider's data set.
         */
        private void randomCount() 
        {
            fCount = MIN_COUNT + Math.abs(fRandom.nextInt()) % (MAX_COUNT - MIN_COUNT);
            
            // Generate the event that the count has changed, and post it to 
            // dispatch thread with transmission delay.
            fExecutor.schedule(
                new Runnable() { public void run() {
                    for (Listener listener : fListeners) {
                        listener.countChanged();
                    }
                }}, 
                TRANSMISSION_DELAY_TIME, TimeUnit.MILLISECONDS);
        }
         
         
        /**
         * Invalidates a random range of indexes.
         */
        private void randomDataChange() 
        {
            final Set<Integer> set = new HashSet<Integer>();
            // Change one in ten values.
            for (int i = 0; i < fCount * RANDOM_CHANGE_SET_PERCENTAGE / 100; i++) {
                set.add( new Integer(Math.abs(fRandom.nextInt()) % fCount) );
            }                    

            // Generate the event that the data has changed.  
            // Post dispatch thread with transmission delay.
            fExecutor.schedule(
                new Runnable() { public void run() {
                    for (Listener listener : fListeners) {
                        listener.dataChanged(set);
                    }
                }},
                TRANSMISSION_DELAY_TIME, TimeUnit.MILLISECONDS);
        }
    }
    
    
    public SlowDataProvider(DsfExecutor executor) {
        fExecutor = executor;
        fProviderThread = new ProviderThread();
        fProviderThread.start();
    }
     
    /**
     * Requests shutdown of this data provider.
     * @param requestMonitor Request completion monitor.
     */
    public void shutdown(RequestMonitor requestMonitor) {
        fShutdownRequestMonitor = requestMonitor;
    }

    ///////////////////////////////////////////////////////////////////////////
    // DataProvider
    public DsfExecutor getDsfExecutor() {
        return fExecutor;
    }
    
    public void getItemCount(final DataRequestMonitor<Integer> rm) {
        fQueue.add(new CountRequest(rm));
    }
    
    public void getItem(final int index, final DataRequestMonitor<String> rm) { 
        fQueue.add(new ItemRequest(index, rm));
    } 

    public void addListener(Listener listener) {
        assert fExecutor.isInExecutorThread();
        fListeners.add(listener);
    }

    public void removeListener(Listener listener) {
        assert fExecutor.isInExecutorThread();
        fListeners.remove(listener);
    }

    // 
    ///////////////////////////////////////////////////////////////////////////
         
}

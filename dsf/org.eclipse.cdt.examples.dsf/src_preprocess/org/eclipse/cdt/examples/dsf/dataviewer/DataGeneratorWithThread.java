/*******************************************************************************
 * Copyright (c) 2006, 2011 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
//#ifdef exercises
package org.eclipse.cdt.examples.dsf.dataviewer;
//#else
//#package org.eclipse.cdt.examples.dsf.dataviewer.answers;
//#endif

import java.util.Collections;
import java.util.HashMap;
import java.util.Random;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Status;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.examples.dsf.DsfExamplesPlugin;

/**
 * Thread-based implementation of the data generator.
 * <p>
 * This generator is based around a queue of client requests and a thread which 
 * reads the requests from the queue and processes them. The distinguishing 
 * feature of this generator is that it uses a a blocking queue as the main 
 * synchronization object.  However, fListeners, fShutdown,  and fChangedIndexes 
 * fields also need to be thread-safe and so they implement their own 
 * synchronization.
 * </p>
 */
public class DataGeneratorWithThread extends Thread 
    implements IDataGenerator 
{

    // Request objects are used to serialize the interface calls into objects
    // which can then be pushed into a queue.
    abstract class Request {
        final RequestMonitor fRequestMonitor;
        
        Request(RequestMonitor rm) {
            fRequestMonitor = rm;
        }
    }
    
    class CountRequest extends Request {
        CountRequest(DataRequestMonitor<Integer> rm) { 
            super(rm); 
        }
    } 

    class ItemRequest extends Request {
        final int fIndex;
        ItemRequest(int index, DataRequestMonitor<Integer> rm) { 
            super(rm);
            fIndex = index; 
        }
    } 

    class ShutdownRequest extends Request {
        ShutdownRequest(RequestMonitor rm) { 
            super(rm);
        }
    }

    // Main request queue of the data generator.  The getValue(), getCount(), 
    // and shutdown() methods write into the queue, while the run() method 
    // reads from it.
    private final BlockingQueue<Request> fQueue = 
        new LinkedBlockingQueue<Request>();

    // ListenerList class provides thread safety.
    private ListenerList fListeners = new ListenerList();
    
    // Current number of elements in this generator.
    private int fCount = MIN_COUNT;
    
    // Counter used to determine when to reset the element count.
    private int fCountResetTrigger = 0;
    
    // Elements which were modified since the last reset.
    private Map<Integer, Integer> fChangedValues = 
        Collections.synchronizedMap(new HashMap<Integer, Integer>());
    
    // Used to determine when to make changes in data.
    private long fLastChangeTime = System.currentTimeMillis();

    // Flag indicating when the generator has been shut down.
    private AtomicBoolean fShutdown = new AtomicBoolean(false);
 
    public DataGeneratorWithThread() {
        // Immediately kick off the request processing thread.
        start();
    }
     
    public void shutdown(RequestMonitor rm) {
        // Mark the generator as shut down.  After the fShutdown flag is set,
        // all new requests should be shut down.
        if (!fShutdown.getAndSet(true)) {
            fQueue.add(new ShutdownRequest(rm));
        } else {
            // 
            rm.setStatus(new Status(IStatus.ERROR, DsfExamplesPlugin.PLUGIN_ID, 
                "Supplier shut down"));
            rm.done();
        }        
    }

    public void getCount(DataRequestMonitor<Integer> rm) {
        if (!fShutdown.get()) {
            fQueue.add(new CountRequest(rm));
        } else {
            rm.setStatus(new Status(IStatus.ERROR, DsfExamplesPlugin.PLUGIN_ID, 
                "Supplier shut down"));
            rm.done();
        }        
    }
    
    public void getValue(int index, DataRequestMonitor<Integer> rm) { 
        if (!fShutdown.get()) {
            fQueue.add(new ItemRequest(index, rm));
        } else {
            rm.setStatus(new Status(IStatus.ERROR, DsfExamplesPlugin.PLUGIN_ID, 
                "Supplier shut down"));
            rm.done();
        }        
    } 

    public void addListener(Listener listener) {
        fListeners.add(listener);
    }

    public void removeListener(Listener listener) {
        fListeners.remove(listener);
    }
    
    @Override
    public void run() {
        try {
            while(true) {
                // Get the next request from the queue.  The time-out 
                // ensures that that the random changes get processed. 
                final Request request = fQueue.poll(100, TimeUnit.MILLISECONDS);
                
                // If a request was dequeued, process it.
                if (request != null) {
                    // Simulate a processing delay.
                    
                    if (request instanceof CountRequest) {
                        processCountRequest((CountRequest)request);
                    } else if (request instanceof ItemRequest) {
                        processItemRequest((ItemRequest)request);
                    } else if (request instanceof ShutdownRequest) {
                        // If shutting down, just break out of the while(true) 
                        // loop and thread will exit.
                        request.fRequestMonitor.done();
                        break;
                    }
                } else {
                    Thread.sleep(PROCESSING_DELAY);
                }
                
                // Simulate data changes.
                randomChanges();
            }
        }
        catch (InterruptedException x) {}
    } 

    private void processCountRequest(CountRequest request) {
        @SuppressWarnings("unchecked") // Suppress warning about lost type info.
        DataRequestMonitor<Integer> rm = 
        (DataRequestMonitor<Integer>)request.fRequestMonitor;
        
        rm.setData(fCount);
        rm.done();
    }

    private void processItemRequest(ItemRequest request) {
        @SuppressWarnings("unchecked") // Suppress warning about lost type info.
        DataRequestMonitor<Integer> rm = 
        (DataRequestMonitor<Integer>)request.fRequestMonitor; 

        if (fChangedValues.containsKey(request.fIndex)) {
            rm.setData(fChangedValues.get(request.fIndex));
        } else {
            rm.setData(request.fIndex);
        }
        rm.done();
    } 
 
    
    private void randomChanges() {
        // Check if enough time is elapsed.
        if (System.currentTimeMillis() > 
            fLastChangeTime + RANDOM_CHANGE_INTERVAL) 
        {
            fLastChangeTime = System.currentTimeMillis();
            
            // Once every number of changes, reset the count, the rest of the 
            // times just change certain values.
            if (++fCountResetTrigger % RANDOM_COUNT_CHANGE_INTERVALS == 0) {
                randomCountReset();
            } else {
                randomDataChange();
            }
        }
    }
     
    private void randomCountReset() {
        // Calculate the new count.
        Random random = new java.util.Random();
        fCount = MIN_COUNT + Math.abs(random.nextInt()) % (MAX_COUNT - MIN_COUNT);

        // Reset the changed values.
        fChangedValues.clear();
        
        // Notify listeners  
        for (Object listener : fListeners.getListeners()) {
            ((Listener)listener).countChanged();
        }
    }
     
    private void randomDataChange() {
        // Calculate the indexes to change.
        Random random = new java.util.Random();
        Map<Integer, Integer> changed = new HashMap<Integer, Integer>();
        for (int i = 0; i < fCount * RANDOM_CHANGE_SET_PERCENTAGE / 100; i++) {
            int randomIndex = Math.abs(random.nextInt()) % fCount;
            int randomValue = Math.abs(random.nextInt()) % fCount;
            changed.put(randomIndex, randomValue);
        }                

        // Add the indexes to an overall set of changed indexes.
        fChangedValues.putAll(changed);
        
        // Notify listeners  
        for (Object listener : fListeners.getListeners()) {
            ((Listener)listener).valuesChanged(changed.keySet());
        }
    }    
}



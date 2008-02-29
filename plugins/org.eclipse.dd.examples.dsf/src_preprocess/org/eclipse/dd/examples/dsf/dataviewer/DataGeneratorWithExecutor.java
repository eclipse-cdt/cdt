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
//#ifdef excercises
package org.eclipse.dd.examples.dsf.dataviewer;
//#else
//#package org.eclipse.dd.examples.dsf.dataviewer.answers;
//#endif

import java.util.HashSet;
//#ifdef answers
//#import java.util.Iterator;
//#endif
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
//#ifdef answers
//#import org.eclipse.dd.dsf.concurrent.ConfinedToDsfExecutor;
//#import org.eclipse.dd.dsf.concurrent.Immutable;
//#import org.eclipse.dd.dsf.concurrent.ThreadSafe;
//#endif
import org.eclipse.dd.dsf.concurrent.DataRequestMonitor;
import org.eclipse.dd.dsf.concurrent.DefaultDsfExecutor;
import org.eclipse.dd.dsf.concurrent.DsfExecutor;
import org.eclipse.dd.dsf.concurrent.DsfRunnable;
import org.eclipse.dd.dsf.concurrent.RequestMonitor;
import org.eclipse.dd.examples.dsf.DsfExamplesPlugin;

/**
 * DSF Executor-based implementation of the data generator.
 * <p>
 * This generator uses a queue of client requests and processes these
 * requests periodically using a DSF executor.  The main feature of this
 * generator is that it uses the executor as its only synchronization object.
 * This means that all the fields with the exception of the executor can only
 * be accessed while running in the executor thread.
 * </p>
 */
//#ifdef excercises
// TODO Excercise 3 - Add an annotationindicating allowed concurrency access
//#else
//#@ThreadSafe
//#endif
public class DataGeneratorWithExecutor implements IDataGenerator {

    // Request objects are used to serialize the interface calls into objects
    // which can then be pushed into a queue.
    //#ifdef excercises
    // TODO Excercise 3 - Add an annotationindicating allowed concurrency access
    //#else
//#    @Immutable
    //#endif
    abstract class Request {
        final RequestMonitor fRequestMonitor;
        
        Request(RequestMonitor rm) {
            fRequestMonitor = rm;
        }
    }
    
    //#ifdef excercises
    // TODO Excercise 3 - Add an annotationindicating allowed concurrency access
    //#else
//#    @Immutable
    //#endif
    class CountRequest extends Request {
        CountRequest(DataRequestMonitor<Integer> rm) { 
            super(rm); 
        }
    } 

    //#ifdef excercises
    // TODO Excercise 3 - Add an annotationindicating allowed concurrency access
    //#else
//#    @Immutable
    //#endif
    class ItemRequest extends Request {
        final int fIndex;
        ItemRequest(int index, DataRequestMonitor<String> rm) { 
            super(rm);
            fIndex = index; 
        }
    } 

    // The executor used to access all internal data of the generator.
    //#ifdef excercises
    // TODO Excercise 3 - Add an annotation indicating allowed concurrency access
    //#endif
    private DsfExecutor fExecutor;
    
    // Main request queue of the data generator.  The getValue(), getCount(), 
    // and shutdown() methods write into the queue, while the serviceQueue()
    // method reads from it.
    // The executor used to access all internal data of the generator.
    //#ifdef excercises
    // TODO Excercise 3 - Add an annotationindicating allowed concurrency access
    //#else
//#    @ConfinedToDsfExecutor("fExecutor")
    //#endif
    private List<Request> fQueue = new LinkedList<Request>();

    // List of listeners is not synchronized, it also has to be accessed
    // using the executor.
    //#ifdef excercises
    // TODO Excercise 3 - Add an annotationindicating allowed concurrency access
    //#else
//#    @ConfinedToDsfExecutor("fExecutor")
    //#endif
    private List<Listener> fListeners = new LinkedList<Listener>();

    // Current number of elements in this generator.
    //#ifdef excercises
    // TODO Excercise 3 - Add an annotationindicating allowed concurrency access
    //#else
//#    @ConfinedToDsfExecutor("fExecutor")
    //#endif
    private int fCount = MIN_COUNT;
    
    // Counter used to determine when to reset the element count.
    //#ifdef excercises
    // TODO Excercise 3 - Add an annotationindicating allowed concurrency access
    //#else
//#    @ConfinedToDsfExecutor("fExecutor")
    //#endif
    private int fCountResetTrigger = 0;
    
    // Elements which were modified since the last reset.
    //#ifdef excercises
    // TODO Excercise 3 - Add an annotationindicating allowed concurrency access
    //#else
//#    @ConfinedToDsfExecutor("fExecutor")
    //#endif
    private Set<Integer> fChangedIndexes = new HashSet<Integer>();
    
    // Flag used to ensure that requests are processed sequentially.
    //#ifdef excercises
    // TODO Excercise 3 - Add an annotationindicating allowed concurrency access
    //#else
//#    @ConfinedToDsfExecutor("fExecutor")
    //#endif
    private boolean fServiceQueueInProgress = false;
 
    //#ifdef excercises
    // TODO Excercise 3 - Add an annotation indicating allowed concurrency access
    //#endif
    public DataGeneratorWithExecutor() {
        // Create the executor
        fExecutor = new DefaultDsfExecutor("Supplier Executor");
        
        // Schedule a runnable to make the random changes.
        fExecutor.scheduleAtFixedRate(
            new DsfRunnable() {
                public void run() {
                    randomChanges();
                }
            },
            RANDOM_CHANGE_INTERVAL, 
            RANDOM_CHANGE_INTERVAL, 
            TimeUnit.MILLISECONDS);
    }
     
    //#ifdef excercises
    // TODO Excercise 3 - Add an annotation indicating allowed concurrency access
    //#endif
    public void shutdown(final RequestMonitor rm) {
        try {
            fExecutor.execute( new DsfRunnable() {
                public void run() {
                    // Empty the queue of requests and fail them.
                    for (Request request : fQueue) {
                        request.fRequestMonitor.setStatus(
                            new Status(IStatus.ERROR, DsfExamplesPlugin.PLUGIN_ID, "Supplier shut down"));
                        request.fRequestMonitor.done();
                    }
                    fQueue.clear();
                    
                    // Kill executor.
                    fExecutor.shutdown();
                    rm.done();
                }
            });
        } catch (RejectedExecutionException e) {
            rm.setStatus(new Status(IStatus.ERROR, DsfExamplesPlugin.PLUGIN_ID, "Supplier shut down"));
            rm.done();
        }
    }

    //#ifdef excercises
    // TODO Excercise 3 - Add an annotation indicating allowed concurrency access
    //#endif
    public void getCount(final DataRequestMonitor<Integer> rm) {
        try {
            fExecutor.execute( new DsfRunnable() {
                public void run() {
                    fQueue.add(new CountRequest(rm));
                    serviceQueue();
                }
            });
        } catch (RejectedExecutionException e) {
            rm.setStatus(new Status(IStatus.ERROR, DsfExamplesPlugin.PLUGIN_ID, "Supplier shut down"));
            rm.done();
        }
    }
    
    //#ifdef excercises
    // TODO Excercise 3 - Add an annotation indicating allowed concurrency access
    //#endif
    public void getValue(final int index, final DataRequestMonitor<String> rm) { 
        try {
            fExecutor.execute( new DsfRunnable() {
                public void run() {
                    fQueue.add(new ItemRequest(index, rm));
                    serviceQueue();
                }
            });
        } catch (RejectedExecutionException e) {
            rm.setStatus(new Status(IStatus.ERROR, DsfExamplesPlugin.PLUGIN_ID, "Supplier shut down"));
            rm.done();
        }
    } 

    //#ifdef excercises
    // TODO Excercise 3 - Add an annotation indicating allowed concurrency access
    //#endif
    public void addListener(final Listener listener) {
        try {
            fExecutor.execute( new DsfRunnable() {
                public void run() {
                    fListeners.add(listener);
                }
            });
        } catch (RejectedExecutionException e) {}
    }

    //#ifdef excercises
    // TODO Excercise 3 - Add an annotation indicating allowed concurrency access
    //#endif
    public void removeListener(final Listener listener) {
        try {
            fExecutor.execute( new DsfRunnable() {
                public void run() {
                    fListeners.remove(listener);
                }
            });
        } catch (RejectedExecutionException e) {}
    }

    // Main processing function of this generator.
    //#ifdef excercises
    // TODO Excercise 3 - Add an annotationindicating allowed concurrency access
    //#else
//#    @ConfinedToDsfExecutor("fExecutor")
    //#endif
    private void serviceQueue() {
        
        //#ifdef excercises
        // TODO Excercise 4 - Add logic to discard requests from queue.
        //#else
//#        for (Iterator<Request> requestItr = fQueue.iterator(); requestItr.hasNext();) {
//#            Request request = requestItr.next();
//#            if (request.fRequestMonitor.isCanceled()) {
//#                request.fRequestMonitor.setStatus(
//#                    new Status(IStatus.CANCEL, DsfExamplesPlugin.PLUGIN_ID, "Request canceled"));
//#                request.fRequestMonitor.done();
//#                requestItr.remove();
//#            }
//#        }
        //#endif
        
        // If a queue servicing is already scheduled, do nothing.
        if (fServiceQueueInProgress) {
            return;
        }
        
        if (fQueue.size() != 0) {
            // If there are requests to service, remove one from the queue and 
            // schedule a runnable to process the request after a processing
            // delay.
            fServiceQueueInProgress = true;
            final Request request = fQueue.remove(0);
            fExecutor.schedule(
                new DsfRunnable() {
                    public void run() {
                        if (request instanceof CountRequest) {
                            processCountRequest((CountRequest)request);
                        } else if (request instanceof ItemRequest) {
                            processItemRequest((ItemRequest)request);
                        } 
                        
                        // Reset the processing flag and process next
                        // request.
                        fServiceQueueInProgress = false;
                        serviceQueue();
                    }
                }, 
                PROCESSING_DELAY, TimeUnit.MILLISECONDS);
        }
    }
    
    //#ifdef excercises
    // TODO Excercise 3 - Add an annotationindicating allowed concurrency access
    //#else
//#    @ConfinedToDsfExecutor("fExecutor")
    //#endif
    private void processCountRequest(CountRequest request) {
        @SuppressWarnings("unchecked") // Suppress warning about lost type info.
        DataRequestMonitor<Integer> rm = (DataRequestMonitor<Integer>)request.fRequestMonitor;
        
        rm.setData(fCount);
        rm.done();
    }

    //#ifdef excercises
    // TODO Excercise 3 - Add an annotationindicating allowed concurrency access
    //#else
//#    @ConfinedToDsfExecutor("fExecutor")
    //#endif
    private void processItemRequest(ItemRequest request) {
        @SuppressWarnings("unchecked") // Suppress warning about lost type info.
        DataRequestMonitor<String> rm = (DataRequestMonitor<String>)request.fRequestMonitor; 

        if (fChangedIndexes.contains(request.fIndex)) {
            rm.setData("Changed: " + request.fIndex);
        } else {
            rm.setData(Integer.toString(request.fIndex)); 
        }
        rm.done();
    } 
 
    /**
     * This method simulates changes in the supplier's data set.
     */
    //#ifdef excercises
    // TODO Excercise 3 - Add an annotationindicating allowed concurrency access
    //#else
//#    @ConfinedToDsfExecutor("fExecutor")
    //#endif
    private void randomChanges() {
        // Once every number of changes, reset the count, the rest of the 
        // times just change certain values.
        if (++fCountResetTrigger % RANDOM_COUNT_CHANGE_INTERVALS == 0){
            randomCountReset();
        } else {
            randomDataChange();
        }
    }
     
    /**
     * Calculates new size for provider's data set.
     */
    //#ifdef excercises
    // TODO Excercise 3 - Add an annotationindicating allowed concurrency access
    //#else
//#    @ConfinedToDsfExecutor("fExecutor")
    //#endif
    private void randomCountReset() {
        // Calculate the new count.
        Random random = new java.util.Random();
        fCount = MIN_COUNT + Math.abs(random.nextInt()) % (MAX_COUNT - MIN_COUNT);

        // Reset the changed values.
        fChangedIndexes.clear();
        
        // Notify listeners  
        for (Listener listener : fListeners) {
            listener.countChanged();
        }
    }
     
    /**
     * Invalidates a random range of indexes.
     */
    //#ifdef excercises
    // TODO Excercise 3 - Add an annotationindicating allowed concurrency access
    //#else
//#    @ConfinedToDsfExecutor("fExecutor")
    //#endif
    private void randomDataChange() {
        // Calculate the indexes to change.
        Random random = new java.util.Random();
        Set<Integer> set = new HashSet<Integer>();
        for (int i = 0; i < fCount * RANDOM_CHANGE_SET_PERCENTAGE / 100; i++) {
            set.add( new Integer(Math.abs(random.nextInt()) % fCount) );
        }                    

        // Add the indexes to an overall set of changed indexes.
        fChangedIndexes.addAll(set);
        
        // Notify listeners  
        for (Listener listener : fListeners) {
            listener.valuesChanged(set);
        }
    }    
}


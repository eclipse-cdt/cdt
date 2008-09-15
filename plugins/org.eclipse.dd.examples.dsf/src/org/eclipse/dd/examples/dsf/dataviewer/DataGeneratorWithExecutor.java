/*******************************************************************************
 * Copyright (c) 2006, 2008 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.dd.examples.dsf.dataviewer;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
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
//TODO Exercise 4 - Add an annotation (ThreadSafe/ConfinedToDsfExecutor) 
//indicating allowed thread access to this class/method/member
public class DataGeneratorWithExecutor implements IDataGenerator {

    // Request objects are used to serialize the interface calls into objects
    // which can then be pushed into a queue.
    // TODO Ecercise 4 - Add an annotationindicating allowed concurrency access
    // Hint: Request and its subclasses have all their fields declared as final.
    abstract class Request {
        final RequestMonitor fRequestMonitor;
        
        Request(RequestMonitor rm) {
            fRequestMonitor = rm;
        }
    }
    
    // TODO Exercise 4 - Add an annotation (ThreadSafe/ConfinedToDsfExecutor) 
    // indicating allowed thread access to this class/method/member
    class CountRequest extends Request {
        CountRequest(DataRequestMonitor<Integer> rm) { 
            super(rm); 
        }
    } 

    // TODO Exercise 4 - Add an annotation (ThreadSafe/ConfinedToDsfExecutor) 
    // indicating allowed thread access to this class/method/member
    class ItemRequest extends Request {
        final int fIndex;
        ItemRequest(int index, DataRequestMonitor<String> rm) { 
            super(rm);
            fIndex = index; 
        }
    } 

    // The executor used to access all internal data of the generator.
    // TODO Exercise 4 - Add an annotation (ThreadSafe/ConfinedToDsfExecutor) 
    // indicating allowed thread access to this class/method/member
    // Hint: If a member does not have an annotation, the programmer can assume
    // that the concurrency rule that applies to the class also applies to this
    // member.
    private DsfExecutor fExecutor;
    
    // Main request queue of the data generator.  The getValue(), getCount(), 
    // and shutdown() methods write into the queue, while the serviceQueue()
    // method reads from it.
    // The executor used to access all internal data of the generator.
    // TODO Exercise 4 - Add an annotation (ThreadSafe/ConfinedToDsfExecutor) 
    // indicating allowed thread access to this class/method/member
    private List<Request> fQueue = new LinkedList<Request>();

    // List of listeners is not synchronized, it also has to be accessed
    // using the executor.
    // TODO Exercise 4 - Add an annotation (ThreadSafe/ConfinedToDsfExecutor) 
    // indicating allowed thread access to this class/method/member
    private List<Listener> fListeners = new LinkedList<Listener>();

    // Current number of elements in this generator.
    // TODO Exercise 4 - Add an annotation (ThreadSafe/ConfinedToDsfExecutor) 
    // indicating allowed thread access to this class/method/member
    private int fCount = MIN_COUNT;
    
    // Counter used to determine when to reset the element count.
    // TODO Exercise 4 - Add an annotation (ThreadSafe/ConfinedToDsfExecutor) 
    // indicating allowed thread access to this class/method/member
    private int fCountResetTrigger = 0;
    
    // Elements which were modified since the last reset.
    // TODO Exercise 4 - Add an annotation (ThreadSafe/ConfinedToDsfExecutor) 
    // indicating allowed thread access to this class/method/member
    private Set<Integer> fChangedIndexes = new HashSet<Integer>();
    
    // Flag used to ensure that requests are processed sequentially.
    // TODO Exercise 4 - Add an annotation (ThreadSafe/ConfinedToDsfExecutor) 
    // indicating allowed thread access to this class/method/member
    private boolean fServiceQueueInProgress = false;
 
    // TODO Exercise 4 - Add an annotation (ThreadSafe/ConfinedToDsfExecutor) 
    // indicating allowed thread access to this class/method/member
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
     
    // TODO Exercise 4 - Add an annotation (ThreadSafe/ConfinedToDsfExecutor) 
    // indicating allowed thread access to this class/method/member
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

    // TODO Exercise 4 - Add an annotation (ThreadSafe/ConfinedToDsfExecutor) 
    // indicating allowed thread access to this class/method/member
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
    
    // TODO Exercise 4 - Add an annotation (ThreadSafe/ConfinedToDsfExecutor) 
    // indicating allowed thread access to this class/method/member
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

    // TODO Exercise 4 - Add an annotation (ThreadSafe/ConfinedToDsfExecutor) 
    // indicating allowed thread access to this class/method/member
    public void addListener(final Listener listener) {
        try {
            fExecutor.execute( new DsfRunnable() {
                public void run() {
                    fListeners.add(listener);
                }
            });
        } catch (RejectedExecutionException e) {}
    }

    // TODO Exercise 4 - Add an annotation (ThreadSafe/ConfinedToDsfExecutor) 
    // indicating allowed thread access to this class/method/member
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
    // TODO Exercise 4 - Add an annotation (ThreadSafe/ConfinedToDsfExecutor) 
    // indicating allowed thread access to this class/method/member
    private void serviceQueue() {
        
        // TODO Exercise 3 - Add logic to discard cancelled requests from queue.
        // Hint: Since serviceQueue() is called using the executor, and the 
        // fQueue list can only be modified when running in the executor 
        // thread.  This method can safely iterate and modify fQueue without 
        // risk of race conditions or concurrent modification exceptions.
        
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
    
    // TODO Exercise 4 - Add an annotation (ThreadSafe/ConfinedToDsfExecutor) 
    // indicating allowed thread access to this class/method/member
    private void processCountRequest(CountRequest request) {
        @SuppressWarnings("unchecked") // Suppress warning about lost type info.
        DataRequestMonitor<Integer> rm = (DataRequestMonitor<Integer>)request.fRequestMonitor;
        
        rm.setData(fCount);
        rm.done();
    }

    // TODO Exercise 4 - Add an annotation (ThreadSafe/ConfinedToDsfExecutor) 
    // indicating allowed thread access to this class/method/member
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
    // TODO Exercise 4 - Add an annotation (ThreadSafe/ConfinedToDsfExecutor) 
    // indicating allowed thread access to this class/method/member
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
    // TODO Exercise 4 - Add an annotation (ThreadSafe/ConfinedToDsfExecutor) 
    // indicating allowed thread access to this class/method/member
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
    // TODO Exercise 4 - Add an annotation (ThreadSafe/ConfinedToDsfExecutor) 
    // indicating allowed thread access to this class/method/member
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


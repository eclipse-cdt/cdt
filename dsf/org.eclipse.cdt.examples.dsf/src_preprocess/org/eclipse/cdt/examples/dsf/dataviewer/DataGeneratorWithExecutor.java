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

import java.util.HashMap;
//#ifdef answers
//#import java.util.Iterator;
//#endif
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Map;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
//#ifdef answers
//#import org.eclipse.cdt.dsf.concurrent.ConfinedToDsfExecutor;
//#import org.eclipse.cdt.dsf.concurrent.Immutable;
//#import org.eclipse.cdt.dsf.concurrent.ThreadSafe;
//#endif
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DefaultDsfExecutor;
import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.examples.dsf.DsfExamplesPlugin;

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
//#ifdef exercises
//TODO Exercise 4 - Add an annotation (ThreadSafe/ConfinedToDsfExecutor) 
//indicating allowed thread access to this class/method/member
//#else
//#@ThreadSafe
//#endif
public class DataGeneratorWithExecutor implements IDataGenerator {

    // Request objects are used to serialize the interface calls into objects
    // which can then be pushed into a queue.
    //#ifdef exercises
    // TODO Ecercise 4 - Add an annotationindicating allowed concurrency access
    // Hint: Request and its subclasses have all their fields declared as final.
    //#else
//#    @Immutable
    //#endif
    abstract class Request {
        final RequestMonitor fRequestMonitor;
        
        Request(RequestMonitor rm) {
            fRequestMonitor = rm;
            
            rm.addCancelListener(new RequestMonitor.ICanceledListener() {
                public void requestCanceled(RequestMonitor rm) {
                    fExecutor.execute(new DsfRunnable() {
                        public void run() {
                            fQueue.remove(Request.this);
                        }
                    });
                }
            });
        }
    }
    
    //#ifdef exercises
    // TODO Exercise 4 - Add an annotation (ThreadSafe/ConfinedToDsfExecutor) 
    // indicating allowed thread access to this class/method/member
    //#else
//#    @Immutable
    //#endif
    class CountRequest extends Request {
        CountRequest(DataRequestMonitor<Integer> rm) { 
            super(rm); 
        }
    } 

    //#ifdef exercises
    // TODO Exercise 4 - Add an annotation (ThreadSafe/ConfinedToDsfExecutor) 
    // indicating allowed thread access to this class/method/member
    //#else
//#    @Immutable
    //#endif
    class ItemRequest extends Request {
        final int fIndex;
        ItemRequest(int index, DataRequestMonitor<Integer> rm) { 
            super(rm);
            fIndex = index; 
        }
    } 

    // The executor used to access all internal data of the generator.
    //#ifdef exercises
    // TODO Exercise 4 - Add an annotation (ThreadSafe/ConfinedToDsfExecutor) 
    // indicating allowed thread access to this class/method/member
    // Hint: If a member does not have an annotation, the programmer can assume
    // that the concurrency rule that applies to the class also applies to this
    // member.
    //#endif
    private DsfExecutor fExecutor;
    
    // Main request queue of the data generator.  The getValue(), getCount(), 
    // and shutdown() methods write into the queue, while the serviceQueue()
    // method reads from it.
    // The executor used to access all internal data of the generator.
    //#ifdef exercises
    // TODO Exercise 4 - Add an annotation (ThreadSafe/ConfinedToDsfExecutor) 
    // indicating allowed thread access to this class/method/member
    //#else
//#    @ConfinedToDsfExecutor("fExecutor")
    //#endif
    private List<Request> fQueue = new LinkedList<Request>();

    // List of listeners is not synchronized, it also has to be accessed
    // using the executor.
    //#ifdef exercises
    // TODO Exercise 4 - Add an annotation (ThreadSafe/ConfinedToDsfExecutor) 
    // indicating allowed thread access to this class/method/member
    //#else
//#    @ConfinedToDsfExecutor("fExecutor")
    //#endif
    private List<Listener> fListeners = new LinkedList<Listener>();

    // Current number of elements in this generator.
    //#ifdef exercises
    // TODO Exercise 4 - Add an annotation (ThreadSafe/ConfinedToDsfExecutor) 
    // indicating allowed thread access to this class/method/member
    //#else
//#    @ConfinedToDsfExecutor("fExecutor")
    //#endif
    private int fCount = MIN_COUNT;
    
    // Counter used to determine when to reset the element count.
    //#ifdef exercises
    // TODO Exercise 4 - Add an annotation (ThreadSafe/ConfinedToDsfExecutor) 
    // indicating allowed thread access to this class/method/member
    //#else
//#    @ConfinedToDsfExecutor("fExecutor")
    //#endif
    private int fCountResetTrigger = 0;
    
    // Elements which were modified since the last reset.
    //#ifdef exercises
    // TODO Exercise 4 - Add an annotation (ThreadSafe/ConfinedToDsfExecutor) 
    // indicating allowed thread access to this class/method/member
    //#else
//#    @ConfinedToDsfExecutor("fExecutor")
    //#endif
    private Map<Integer, Integer> fChangedValues = 
        new HashMap<Integer, Integer>();
    
    public DataGeneratorWithExecutor() {
        // Create the executor
        this(new DefaultDsfExecutor("Supplier Executor"));
    }
    //#ifdef exercises
    // TODO Exercise 4 - Add an annotation (ThreadSafe/ConfinedToDsfExecutor) 
    // indicating allowed thread access to this class/method/member
    //#endif
    public DataGeneratorWithExecutor(DsfExecutor executor) {
        // Create the executor
        fExecutor = executor;
        
        // Schedule a runnable to make the random changes.
        fExecutor.scheduleAtFixedRate(
            new DsfRunnable() {
                public void run() {
                    randomChanges();
                }
            },
            new Random().nextInt() % RANDOM_CHANGE_INTERVAL, 
            RANDOM_CHANGE_INTERVAL, //Add a 10% variance to the interval. 
            TimeUnit.MILLISECONDS);
    }
     
    //#ifdef exercises
    // TODO Exercise 4 - Add an annotation (ThreadSafe/ConfinedToDsfExecutor) 
    // indicating allowed thread access to this class/method/member
    //#endif
    public void shutdown(final RequestMonitor rm) {
        try {
            fExecutor.execute( new DsfRunnable() {
                public void run() {
                    // Empty the queue of requests and fail them.
                    for (Request request : fQueue) {
                        request.fRequestMonitor.setStatus(new Status(
                            IStatus.ERROR, DsfExamplesPlugin.PLUGIN_ID, 
                            "Supplier shut down"));
                        request.fRequestMonitor.done();
                    }
                    fQueue.clear();
                    
                    // Kill executor.
                    fExecutor.shutdown();
                    rm.done();
                }
            });
        } catch (RejectedExecutionException e) {
            rm.setStatus(new Status(IStatus.ERROR, DsfExamplesPlugin.PLUGIN_ID, 
                "Supplier shut down"));
            rm.done();
        }
    }

    //#ifdef exercises
    // TODO Exercise 4 - Add an annotation (ThreadSafe/ConfinedToDsfExecutor) 
    // indicating allowed thread access to this class/method/member
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
            rm.setStatus(new Status(
                IStatus.ERROR, DsfExamplesPlugin.PLUGIN_ID, 
                "Supplier shut down"));
            rm.done();
        }
    }
    
    //#ifdef exercises
    // TODO Exercise 4 - Add an annotation (ThreadSafe/ConfinedToDsfExecutor) 
    // indicating allowed thread access to this class/method/member
    //#endif
    public void getValue(final int index, final DataRequestMonitor<Integer> rm) { 
        try {
            fExecutor.execute( new DsfRunnable() {
                public void run() {
                    fQueue.add(new ItemRequest(index, rm));
                    serviceQueue();
                }
            });
        } catch (RejectedExecutionException e) {
            rm.setStatus(new Status(IStatus.ERROR, DsfExamplesPlugin.PLUGIN_ID, 
                "Supplier shut down"));
            rm.done();
        }
    } 

    //#ifdef exercises
    // TODO Exercise 4 - Add an annotation (ThreadSafe/ConfinedToDsfExecutor) 
    // indicating allowed thread access to this class/method/member
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

    //#ifdef exercises
    // TODO Exercise 4 - Add an annotation (ThreadSafe/ConfinedToDsfExecutor) 
    // indicating allowed thread access to this class/method/member
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
    //#ifdef exercises
    // TODO Exercise 4 - Add an annotation (ThreadSafe/ConfinedToDsfExecutor) 
    // indicating allowed thread access to this class/method/member
    //#else
//#    @ConfinedToDsfExecutor("fExecutor")
    //#endif
    private void serviceQueue() {
        fExecutor.schedule(
            new DsfRunnable() {
                public void run() {
                    doServiceQueue();
                }
            }, 
            PROCESSING_DELAY, TimeUnit.MILLISECONDS);        
    }
    
    private void doServiceQueue() {
        //#ifdef exercises
        // TODO Exercise 3 - Add logic to discard cancelled requests from queue.
        // Hint: Since serviceQueue() is called using the executor, and the 
        // fQueue list can only be modified when running in the executor 
        // thread.  This method can safely iterate and modify fQueue without 
        // risk of race conditions or concurrent modification exceptions.
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
        
        while (fQueue.size() != 0) {
            // If there are requests to service, remove one from the queue and 
            // schedule a runnable to process the request after a processing
            // delay.
            Request request = fQueue.remove(0);
            if (request instanceof CountRequest) {
                processCountRequest((CountRequest)request);
            } else if (request instanceof ItemRequest) {
                processItemRequest((ItemRequest)request);
            } 
        }
    }
    
    //#ifdef exercises
    // TODO Exercise 4 - Add an annotation (ThreadSafe/ConfinedToDsfExecutor) 
    // indicating allowed thread access to this class/method/member
    //#else
//#    @ConfinedToDsfExecutor("fExecutor")
    //#endif
    private void processCountRequest(CountRequest request) {
        @SuppressWarnings("unchecked") // Suppress warning about lost type info.
        DataRequestMonitor<Integer> rm = 
        (DataRequestMonitor<Integer>)request.fRequestMonitor;
        
        rm.setData(fCount);
        rm.done();
    }

    //#ifdef exercises
    // TODO Exercise 4 - Add an annotation (ThreadSafe/ConfinedToDsfExecutor) 
    // indicating allowed thread access to this class/method/member
    //#else
//#    @ConfinedToDsfExecutor("fExecutor")
    //#endif
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
 
    /**
     * This method simulates changes in the supplier's data set.
     */
    //#ifdef exercises
    // TODO Exercise 4 - Add an annotation (ThreadSafe/ConfinedToDsfExecutor) 
    // indicating allowed thread access to this class/method/member
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
    //#ifdef exercises
    // TODO Exercise 4 - Add an annotation (ThreadSafe/ConfinedToDsfExecutor) 
    // indicating allowed thread access to this class/method/member
    //#else
//#    @ConfinedToDsfExecutor("fExecutor")
    //#endif
    private void randomCountReset() {
        // Calculate the new count.
        Random random = new java.util.Random();
        fCount = MIN_COUNT + 
            Math.abs(random.nextInt()) % (MAX_COUNT - MIN_COUNT);

        // Reset the changed values.
        fChangedValues.clear();
        
        // Notify listeners  
        for (Listener listener : fListeners) {
            listener.countChanged();
        }
    }
     
    /**
     * Invalidates a random range of indexes.
     */
    //#ifdef exercises
    // TODO Exercise 4 - Add an annotation (ThreadSafe/ConfinedToDsfExecutor) 
    // indicating allowed thread access to this class/method/member
    //#else
//#    @ConfinedToDsfExecutor("fExecutor")
    //#endif
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
        for (Object listener : fListeners) {
            ((Listener)listener).valuesChanged(changed.keySet());
        }
    }    
}


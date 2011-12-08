/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems and others.
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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.concurrent.ICache;
import org.eclipse.cdt.dsf.concurrent.ImmediateInDsfExecutor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Transaction;
import org.eclipse.core.runtime.CoreException;

/**
 * A data generator which performs a sum computation on data retrieved from a 
 * number of other data generators.  The data retrieval from other generators 
 * is performed using ACPM caches and the result is calculated once all caches 
 * are valid.
 * <p>
 * Unlike {@link AsyncSumDataGenerator}, this data generator listens to events
 * from the individual the data providers.  Theve events are used to 
 * invalidate caches to make sure that they don't return incorrect data.  This 
 * generator also sends out events to its clients to notify them to update, or 
 * invalidate their caches.
 * </p>
 */
public class ACPMSumDataGenerator 
    implements IDataGenerator, IDataGenerator.Listener 
{

    /**
     * DSF executor used to serialize data access within this data generator. 
     */
    final private DsfExecutor fExecutor;
    
    /**
     * Data generators to retrieve original data to perform calculations on.
     * The generators are accessed through the cache manager wrappers.
     */
    final private DataGeneratorCacheManager[] fDataGeneratorCMs;
    
    /**
     * List of listeners for this data generator.
     */
    final private List<Listener> fListeners = new LinkedList<Listener>();

    public ACPMSumDataGenerator(DsfExecutor executor, 
        IDataGenerator[] generators) 
    {
        fExecutor = executor;

        // Create wrappers for data generators and add ourselves as listener 
        // to their events.
        fDataGeneratorCMs = new DataGeneratorCacheManager[generators.length];
        ImmediateInDsfExecutor immediateExecutor = 
            new ImmediateInDsfExecutor(fExecutor); 
        for (int i = 0; i < generators.length; i++) {
            fDataGeneratorCMs[i] = new DataGeneratorCacheManager(
                immediateExecutor, generators[i]);
            generators[i].addListener(this);
        }
    }    
    
    public void getCount(final DataRequestMonitor<Integer> rm) {
        // Artificially delay the retrieval of the sum data to simulate
        // real processing time.
        fExecutor.schedule( new Runnable() {
                public void run() {
                    // Create the transaction here to put all the ugly
                    // code in one place.
                    new Transaction<Integer>() {
                        @Override
                        protected Integer process() 
                            throws Transaction.InvalidCacheException, 
                                   CoreException 
                       {
                            return processCount(this);
                        }
                    }.request(rm);
                }
            }, 
            PROCESSING_DELAY, TimeUnit.MILLISECONDS); 
    }

    /** 
     * Perform the calculation to get the max count for the given transaction.  
     * @param transaction The ACPM transaction to use for calculation.
     * @return Calculated count.
     * @throws Transaction.InvalidCacheException {@link Transaction#process}
     * @throws CoreException See {@link Transaction#process}
     */
    private Integer processCount(Transaction<Integer> transaction) 
        throws Transaction.InvalidCacheException, CoreException 
    {
        // Assemble all needed count caches into a collection.
        List<ICache<Integer>> countCaches = 
            new ArrayList<ICache<Integer>>(fDataGeneratorCMs.length);
        for (DataGeneratorCacheManager dataGeneratorCM : fDataGeneratorCMs) {
            countCaches.add(dataGeneratorCM.getCount());
        }
        // Validate all count caches at once.  This executes needed requests 
        // in parallel.
        transaction.validate(countCaches);
        
        // Calculate the max value and return.
        int maxCount = 0;
        for (ICache<Integer> countCache : countCaches) {
            maxCount = Math.max(maxCount, countCache.getData());
        }
        return maxCount;
    }

    public void getValue(final int index, final DataRequestMonitor<Integer> rm) 
    {
        // Add a processing delay.
        fExecutor.schedule( new Runnable() {
                public void run() {
                    new Transaction<Integer>() {
                        @Override
                        protected Integer process() 
                            throws Transaction.InvalidCacheException, 
                                   CoreException 
                        {
                            return processValue(this, index);
                        }
                    }.request(rm);
                }
            }, 
            PROCESSING_DELAY, TimeUnit.MILLISECONDS); 
    }

    /** 
     * Perform the calculation to get the sum of values at given index.  
     * @param transaction The ACPM transaction to use for calculation.
     * @param index Index of value to calculate.
     * @return Calculated value.
     * @throws Transaction.InvalidCacheException {@link Transaction#process}
     * @throws CoreException See {@link Transaction#process}
     */
    private Integer processValue(Transaction<Integer> transaction, int index) 
        throws Transaction.InvalidCacheException, CoreException 
    {
        List<ICache<Integer>> valueCaches = 
            new ArrayList<ICache<Integer>>(fDataGeneratorCMs.length);
        for (DataGeneratorCacheManager dataGeneratorCM : fDataGeneratorCMs) {
            valueCaches.add(dataGeneratorCM.getValue(index));
        }
        // Validate all value caches at once.  This executes needed requests 
        // in parallel.
        transaction.validate(valueCaches);
        
        int sum = 0;
        for (ICache<Integer> valueCache : valueCaches) {
            sum += valueCache.getData();
        }
        return sum;
    }
    
    public void shutdown(final RequestMonitor rm) {
        for (DataGeneratorCacheManager dataGeneratorCM : fDataGeneratorCMs) {
            dataGeneratorCM.getDataGenerator().removeListener(this);
            dataGeneratorCM.dispose();
            rm.done();
        }
        rm.done();
    }

    public void addListener(final Listener listener) {
        // Must access fListeners on executor thread.
        try {
            fExecutor.execute( new DsfRunnable() {
                public void run() {
                    fListeners.add(listener);
                }
            });
        } catch (RejectedExecutionException e) {}
    }

    public void removeListener(final Listener listener) {
        // Must access fListeners on executor thread.
        try {
            fExecutor.execute( new DsfRunnable() {
                public void run() {
                    fListeners.remove(listener);
                }
            });
        } catch (RejectedExecutionException e) {}
    }

    public void countChanged() {
        // Must access fListeners on executor thread.          
        try {
            fExecutor.execute( new DsfRunnable() {
                public void run() {
                    for (Listener listener : fListeners) {
                        listener.countChanged();
                    }
                }
            });
        } catch (RejectedExecutionException e) {}
    }
    
    public void valuesChanged(final Set<Integer> changed) {
        // Must access fListeners on executor thread.
        try {
            fExecutor.execute( new DsfRunnable() {
                public void run() {
                    for (Object listener : fListeners) {
                        ((Listener)listener).valuesChanged(changed);
                    }
                }
            });
        } catch (RejectedExecutionException e) {}
    }
}

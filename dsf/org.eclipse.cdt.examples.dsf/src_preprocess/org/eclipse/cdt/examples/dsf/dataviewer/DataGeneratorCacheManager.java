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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ICache;
import org.eclipse.cdt.dsf.concurrent.ImmediateInDsfExecutor;
import org.eclipse.cdt.dsf.concurrent.RequestCache;
import org.eclipse.cdt.examples.dsf.DsfExamplesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * A wrapper class for the {@link IDataGenerator} interface, which returns 
 * ACPM cache objects to use for data retrieval instead of calling
 * {@link IDataGenerator} asynchronous methods directly.
 */
public class DataGeneratorCacheManager implements IDataGenerator.Listener {

    /** Cache class for retrieving the data generator's count. */
    private class CountCache extends RequestCache<Integer> {
        
        public CountCache() {
            super(fExecutor);
        }
        
        @Override
        protected void retrieve(DataRequestMonitor<Integer> rm) {
            fDataGenerator.getCount(rm);
        }
        
        /**
         * Reset the cache when the count is changed.  
         */
        public void countChanged() {
            // Make sure that if clients are currently waiting for a count,
            // they are notified of the update (their request monitors will be 
            // completed with an error).  They shoudl then re-request data 
            // from provider again.
            setAndReset(null, new Status(IStatus.ERROR, DsfExamplesPlugin.PLUGIN_ID, "Count changed"));
        }
    }

    /** Cache class for retrieving the data generator's values. */
    private class ValueCache extends RequestCache<Integer> {
        private int fIndex;
        
        public ValueCache(int index) {
            super(fExecutor);
            fIndex = index;
        }
        
        @Override
        protected void retrieve(org.eclipse.cdt.dsf.concurrent.DataRequestMonitor<Integer> rm) {
            fDataGenerator.getValue(fIndex, rm);
        };
        
        /**
         * @see CountCache#countChanged()
         */
        public void valueChanged() {
            setAndReset(null, new Status(IStatus.ERROR, DsfExamplesPlugin.PLUGIN_ID, "Value changed"));
        }
    }

    /**
     * Executor used to synchronize data access in this cache manager.  
     * It has to be the same executor that is used by the data generators in 
     * order to guarantee data consistency. 
     */
    private ImmediateInDsfExecutor fExecutor;
    
    /**
     * Data generator that this cache manager is a wrapper for.
     */
    private IDataGenerator fDataGenerator;
    
    /** Cache for data generator's count */
    private CountCache fCountCache;
    
    /**
     * Map of caches for retrieving values.  Each value index has a separate 
     * cache value object.
     */
    private Map<Integer, ValueCache> fValueCaches = new HashMap<Integer, ValueCache>();
    
    public DataGeneratorCacheManager(ImmediateInDsfExecutor executor, IDataGenerator dataGenerator) {
        fExecutor = executor;
        fDataGenerator = dataGenerator;
        fDataGenerator.addListener(this);
    }

    public void dispose() {
        fDataGenerator.removeListener(this);
    }
    
    /**
     * Returns the data generator that this cache manager wraps.
     */
    public IDataGenerator getDataGenerator() {
        return fDataGenerator;
    }
    
    /**
     * Returns the cache for data generator count.
     */
    public ICache<Integer> getCount() {
        if (fCountCache == null) {
            fCountCache = new CountCache();
        }
        return fCountCache;
    }

    /**
     * Returns the cache for a value at given index.
     * 
     * @param index Index of value to return.
     * @return Cache object for given value.
     */
    public ICache<Integer> getValue(int index) {
        ValueCache value = fValueCaches.get(index);
        if (value == null) {
            value = new ValueCache(index);
            fValueCaches.put(index, value);
        }
        
        return value; 
    }
    
    public void countChanged() {
        // Reset the count cache and all the value caches.
        if (fCountCache != null) {
            fCountCache.countChanged();
        }
        for (ValueCache value : fValueCaches.values()) {
            value.valueChanged();
        }
    }
    
    public void valuesChanged(Set<Integer> indexes) {
        // Reset selected value caches.
        for (Integer index : indexes) {
            ValueCache value = fValueCaches.get(index);
            if (value != null) {
                value.valueChanged();
            }
        }
    }
}

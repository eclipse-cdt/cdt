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

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.eclipse.cdt.dsf.concurrent.CountingRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.concurrent.ImmediateExecutor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;

/**
 * A data generator which performs a sum computation on data retrieved from a 
 * number of other data generators.  The data retrieval from other generators 
 * is performed in parallel and the result is calculated once all data is 
 * received.  
 * <p>
 * This calculating generator does not listen to events from the data 
 * providers so it relies on the client to re-retrieve data as needed.
 * </p>  
 */
public class AsyncSumDataGenerator implements IDataGenerator {

    /**
     * DSF executor used to serialize data access within this data generator. 
     */
    final private DsfExecutor fExecutor;

    /**
     * Data generators to retrieve original data to perform calculations on.
     */
    final private IDataGenerator[] fDataGenerators;

    public AsyncSumDataGenerator(DsfExecutor executor, 
        IDataGenerator[] generators) 
    {
        fExecutor = executor;
        fDataGenerators = generators;
    }    
    
    public void getCount(final DataRequestMonitor<Integer> rm) {
        // Artificially delay the retrieval of the sum data to simulate
        // real processing time.
        fExecutor.schedule( new Runnable() {
                public void run() {
                    doGetCount(rm);
                }
            }, 
            PROCESSING_DELAY, TimeUnit.MILLISECONDS); 
    }
    
    /**
     * Performs the actual count retrieval and calculation.
     * @param rm Request monitor to complete with data.
     */
    private void doGetCount(final DataRequestMonitor<Integer> rm) {
        // Array to store counts retrieved asynchronously
        final int[] counts = new int[fDataGenerators.length];
        
        // Counting request monitor is called once all data is retrieved.
        final CountingRequestMonitor crm =
            new CountingRequestMonitor(fExecutor, rm) 
        {
            @Override
            protected void handleSuccess() {
                // Pick the highest count value.
                Arrays.sort(counts, 0, counts.length - 1);
                int maxCount = counts[counts.length - 1];
                rm.setData(maxCount);
                rm.done();
            };
        };
        
        // Each call to data generator fills in one value in array.
        for (int i = 0; i < fDataGenerators.length; i++) {
            final int finalI = i;
            fDataGenerators[i].getCount( 
                new DataRequestMonitor<Integer>(
                    ImmediateExecutor.getInstance(), crm) 
                {
                    @Override
                    protected void handleSuccess() {
                        counts[finalI] = getData();
                        crm.done();
                    }
                }); 
        }        
        crm.setDoneCount(fDataGenerators.length);
    }

    public void getValue(final int index, final DataRequestMonitor<Integer> rm) 
    {
        // Artificially delay the retrieval of the sum data to simulate
        // real processing time.
        fExecutor.schedule( new Runnable() {
                public void run() {
                    doGetValue(index, rm);
                }
            }, 
            PROCESSING_DELAY, TimeUnit.MILLISECONDS); 
    }
    
    /**
     * Performs the actual value retrieval and calculation.
     * @param rm Request monitor to complete with data.
     */
    private void doGetValue(int index, final DataRequestMonitor<Integer> rm) {
        // Array to store counts retrieved asynchronously
        final int[] values = new int[fDataGenerators.length];
        
        // Counting request monitor is called once all data is retrieved.
        final CountingRequestMonitor crm = 
            new CountingRequestMonitor(fExecutor, rm) 
        {
            @Override
            protected void handleSuccess() {
                // Sum up values in array.
                int sum = 0;
                for (int value : values) {
                    sum += value;
                }
                rm.setData(sum);
                rm.done();
            };
        };
        
        // Each call to data generator fills in one value in array.
        for (int i = 0; i < fDataGenerators.length; i++) {
            final int finalI = i;
            fDataGenerators[i].getValue(
                index, 
                new DataRequestMonitor<Integer>(
                    ImmediateExecutor.getInstance(), crm) 
                {
                    @Override
                    protected void handleSuccess() {
                        values[finalI] = getData();  
                        crm.done();
                    }
                }); 
        }        
        crm.setDoneCount(fDataGenerators.length);
    }

    public void shutdown(RequestMonitor rm) {
        rm.done();
    }

    public void addListener(final Listener listener) {
        // no events generated
    }

    public void removeListener(Listener listener) {
        // no events generated
    }

}

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

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.eclipse.cdt.dsf.concurrent.ConfinedToDsfExecutor;
import org.eclipse.cdt.dsf.concurrent.CountingRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DefaultDsfExecutor;
import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.concurrent.ImmediateExecutor;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.concurrent.ThreadSafe;
import org.eclipse.cdt.dsf.ui.concurrent.DisplayDsfExecutor;
import org.eclipse.jface.viewers.ILazyContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;

/**
 * Data viewer based on a table, which reads data from multiple data  
 * providers using asynchronous methods and performs a compultation 
 * on the retrieved data.
 * <p>
 * This example builds on the {@link AsyncDataViewer} example and 
 * demonstrates the pitfalls of retrieving data from multiple sources 
 * asynchronously:  The data is retrieved separate from a set of providers
 * as well as from a data provider that sums the values from the other 
 * providers.  The viewer then performs a check to ensure consistency of 
 * retrieved data.  If the retrieved data is inconsistent an "INCORRECT" 
 * label is added in the viewer.  
 * </p>
 * <p>
 * This viewer is updated periodically every 10 seconds, instead of being 
 * updated with every change in every data provider, which would overwhelm
 * the viewer.
 * </p>
 */
@ConfinedToDsfExecutor("fDisplayExecutor")
public class AsyncSumDataViewer implements ILazyContentProvider
{
    /** View update frequency interval. */
    final private static int UPDATE_INTERVAL = 10000;
    
    /** Executor to use instead of Display.asyncExec(). **/
    @ThreadSafe
    final private DsfExecutor fDisplayExecutor;
    
    // The viewer and generator that this content provider using.
    final private TableViewer fViewer;
    final private IDataGenerator[] fDataGenerators;
    final private IDataGenerator fSumGenerator;

    // Fields used in request cancellation logic.
    private List<ValueCountingRequestMonitor> fItemDataRequestMonitors = 
        new LinkedList<ValueCountingRequestMonitor>();
    private Set<Integer> fIndexesToCancel = new HashSet<Integer>();
    private int fCancelCallsPending = 0;
    private Future<?> fRefreshFuture;
    
    public AsyncSumDataViewer(TableViewer viewer, 
        IDataGenerator[] generators, IDataGenerator sumGenerator) 
    {
        fViewer = viewer;
        fDisplayExecutor = DisplayDsfExecutor.getDisplayDsfExecutor(
            fViewer.getTable().getDisplay());
        fDataGenerators = generators;
        fSumGenerator = sumGenerator;

        // Schedule a task to refresh the viewer periodically.
        fRefreshFuture = fDisplayExecutor.scheduleAtFixedRate(
            new Runnable() {
                public void run() {
                    queryItemCount();
                }
            }, 
            UPDATE_INTERVAL, UPDATE_INTERVAL, TimeUnit.MILLISECONDS);
    }    
    
    public void dispose() {
        // Cancel the periodic task of refreshing the view.
        fRefreshFuture.cancel(false);
        
        // Cancel any outstanding data requests.
        for (ValueCountingRequestMonitor rm : fItemDataRequestMonitors) {
            rm.cancel();
        }
        fItemDataRequestMonitors.clear();
    }

    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        // Set the initial count to the viewer after the input is set.
        queryItemCount();
    }

    public void updateElement(final int index) {
        // Calculate the visible index range.
        final int topIdx = fViewer.getTable().getTopIndex();
        final int botIdx = topIdx + getVisibleItemCount(topIdx);

        // Request the item for the given index.
        queryValue(index);

        // Invoke a cancel task with a delay.  The delay allows multiple cancel 
        // calls to be combined together improving performance of the viewer.
        fCancelCallsPending++;
        fDisplayExecutor.execute(
            new Runnable() { public void run() {
                cancelStaleRequests(topIdx, botIdx);
            }});
    }
        
    /**
     * Calculates the number of visible items based on the top item index and 
     * table bounds.
     * @param top Index of top item.
     * @return calculated number of items in viewer
     */
    private int getVisibleItemCount(int top) {
        Table table = fViewer.getTable();
        int itemCount = table.getItemCount();
        return Math.min(
            (table.getBounds().height / table.getItemHeight()) + 2, 
            itemCount - top);
    }   

    /**
     * Retrieve the up to date count. 
     */
    private void queryItemCount() {
        // Note:The count is retrieved from the sum generator only, the sum 
        // generator is responsible for calculating the count based on 
        // individual data providers' counts.
        fIndexesToCancel.clear();
        fSumGenerator.getCount( 
            new DataRequestMonitor<Integer>(fDisplayExecutor, null) {
                @Override
                protected void handleSuccess() {
                    setCountToViewer(getData());
                }
                @Override
                protected void handleRejectedExecutionException() {
                    // Shutting down, ignore.
                } 
            }); 
    }

    /** 
     * Set the givne count to the viewer.  This will cause the viewer will 
     * refresh all items' data as well.
     * @param count New count to set to viewer.
     */
    private void setCountToViewer(int count) {
        if (!fViewer.getTable().isDisposed()) {
            fViewer.setItemCount(count);
            fViewer.getTable().clearAll();
        }
    }
    
    /**
     * Retrieves value of an element at given index.  When complete the value
     * is written to the viewer.
     * @param index Index of value to retrieve.
     */
    private void queryValue(final int index) {
        // Values retrieved asynchronously from providers are stored in local 
        // arrays.
        final int[] values = new int[fDataGenerators.length];
        final int[] sum = new int[1];
        
        // Counting request monitor is invoked when the required number of 
        // value requests is completed.
        final ValueCountingRequestMonitor crm = 
            new ValueCountingRequestMonitor(index) 
        {
            @Override
            protected void handleCompleted() {
                fItemDataRequestMonitors.remove(this);

                // Check if the request completed successfully, otherwise 
                // ignore it.
                if (isSuccess()) {
                    StringBuilder result = new StringBuilder();
                    int calcSum = 0;
                    for (int value : values) {
                        if (result.length() != 0) result.append(" + ");
                        result.append(value);
                        calcSum += value;
                    }
                    result.append(" = ");
                    result.append(sum[0]);
                    if (calcSum != sum[0]) {
                        result.append(" !INCORRECT! ");
                    }
                    setValueToViewer(fIndex, result.toString());
                }
            };
        };
        
        // Request data from each data generator.
        for (int i = 0; i < fDataGenerators.length; i++) {
            final int finalI = i;
            fDataGenerators[i].getValue(
                index, 
                // Use the display executor to construct the request monitor, 
                // this will cause the handleCompleted() method to be 
                // automatically called on the display thread.
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
        
        // Separately request data from the sum data generator.
        fSumGenerator.getValue(
            index, 
            new DataRequestMonitor<Integer>(
                ImmediateExecutor.getInstance(), crm) 
            {
                @Override
                protected void handleSuccess() {
                    sum[0] = getData();
                    crm.done();
                }
            }); 
        
        crm.setDoneCount(fDataGenerators.length + 1);
        fItemDataRequestMonitors.add(crm);
    }
    
    /**
     * Write the view value to the viewer.  
     * <p>Note: This method must be called in the display thread. </p>
     * @param index Index of value to set.
     * @param value New value.
     */
    private void setValueToViewer(int index, String value) {
        if (!fViewer.getTable().isDisposed()) {
            fViewer.replace(value, index);
        }
    }
    
    /** 
     * Dedicated class for data item requests.  This class holds the index
     * argument so it can be examined when canceling stale requests.
     */
    private class ValueCountingRequestMonitor extends CountingRequestMonitor {
        /** Index is used when canceling stale requests. */
        int fIndex;
        
        ValueCountingRequestMonitor(int index) {
            super(fDisplayExecutor, null);
            fIndex = index; 
        }
        
        @Override
        protected void handleRejectedExecutionException() {
            // Shutting down, ignore.
        }
    }

    /**
     * Cancels any outstanding value requests for items which are no longer
     * visible in the viewer.
     *  
     * @param topIdx Index of top visible item in viewer.  
     * @param botIdx Index of bottom visible item in viewer.
     */
    private void cancelStaleRequests(int topIdx, int botIdx) {
        // Decrement the count of outstanding cancel calls.
        fCancelCallsPending--;

        // Must check again, in case disposed while re-dispatching.
        if (fDataGenerators == null || fViewer.getTable().isDisposed()) return;

        // Go through the outstanding requests and cancel any that 
        // are not visible anymore.
        for (Iterator<ValueCountingRequestMonitor> itr = 
            fItemDataRequestMonitors.iterator(); itr.hasNext();) 
        {
            ValueCountingRequestMonitor item = itr.next();
            if (item.fIndex < topIdx || item.fIndex > botIdx) {
                // Set the item to canceled status, so that the data provider 
                // will ignore it.
                item.cancel();
                
                // Add the item index to list of indexes that were canceled, 
                // which will be sent to the table widget. 
                fIndexesToCancel.add(item.fIndex);
                
                // Remove the item from the outstanding cancel requests.
                itr.remove(); 
            }
        }
        if (!fIndexesToCancel.isEmpty() && fCancelCallsPending == 0) {
            Set<Integer> canceledIdxs = fIndexesToCancel;
            fIndexesToCancel = new HashSet<Integer>();
            
            // Clear the indexes of the canceled request, so that the 
            // viewer knows to request them again when needed.  
            // Note: clearing using TableViewer.clear(int) seems very
            // inefficient, it's better to use Table.clear(int[]).
            int[] canceledIdxsArray = new int[canceledIdxs.size()];
            int i = 0;
            for (Integer index : canceledIdxs) {
                canceledIdxsArray[i++] = index;
            }
            fViewer.getTable().clear(canceledIdxsArray);
        }
    }
    
    /**
     * The entry point for the example.
     * @param args Program arguments.
     */
    public static void main(String[] args) {
        // Create the shell to hold the viewer.
        Display display = new Display();
        Shell shell = new Shell(display, SWT.SHELL_TRIM);
        shell.setLayout(new GridLayout());
        GridData data = new GridData(GridData.FILL_BOTH);
        shell.setLayoutData(data);
        Font font = new Font(display, "Courier", 10, SWT.NORMAL);

        // Create the table viewer.
        TableViewer tableViewer = 
            new TableViewer(shell, SWT.BORDER | SWT.VIRTUAL);
        tableViewer.getControl().setLayoutData(data);

        // Single executor (and single thread) is used by all data generators, 
        // including the sum generator.
        DsfExecutor executor = new DefaultDsfExecutor("Example executor");
        
        // Create the data generator.
        final IDataGenerator[] generators = new IDataGenerator[5];
        for (int i = 0; i < generators.length; i++) {
            generators[i] = new DataGeneratorWithExecutor(executor);
        }
        final IDataGenerator sumGenerator = 
            new AsyncSumDataGenerator(executor, generators);
        
        // Create the content provider which will populate the viewer.
        AsyncSumDataViewer contentProvider = 
            new AsyncSumDataViewer(tableViewer, generators, sumGenerator);
        tableViewer.setContentProvider(contentProvider);
        tableViewer.setInput(new Object());

        // Open the shell and service the display dispatch loop until user
        // closes the shell.
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch())
                display.sleep();
        }
        
        // The IDataGenerator.shutdown() method is asynchronous, this requires
        // using a query again in order to wait for its completion.
        Query<Object> shutdownQuery = new Query<Object>() {
            @Override
            protected void execute(DataRequestMonitor<Object> rm) {
                CountingRequestMonitor crm = new CountingRequestMonitor(
                    ImmediateExecutor.getInstance(), rm);
                for (int i = 0; i < generators.length; i++) {
                    generators[i].shutdown(crm);
                }
                sumGenerator.shutdown(crm);
                crm.setDoneCount(generators.length);
            }
        };

        executor.execute(shutdownQuery);
        try {
            shutdownQuery.get();
        } catch (Exception e) {} 
        
        // Shut down the display.
        font.dispose();    
        display.dispose();
    }
}

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
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.eclipse.cdt.dsf.concurrent.ConfinedToDsfExecutor;
import org.eclipse.cdt.dsf.concurrent.CountingRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DefaultDsfExecutor;
import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.concurrent.ICache;
import org.eclipse.cdt.dsf.concurrent.ImmediateExecutor;
import org.eclipse.cdt.dsf.concurrent.ImmediateInDsfExecutor;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.concurrent.ThreadSafe;
import org.eclipse.cdt.dsf.concurrent.Transaction;
import org.eclipse.cdt.dsf.ui.concurrent.DisplayDsfExecutor;
import org.eclipse.core.runtime.CoreException;
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
 * providers using ACPM methods and performs a computation on the 
 * retrieved data.  
 * <p>
 * This example builds on the {@link AsyncSumDataViewer} example.  It 
 * demonstrates using ACPM to solve the data consistency problem when  
 * retrieving data from multiple sources asynchronously.  
 * </p>
 */
@ConfinedToDsfExecutor("fDisplayExecutor")
public class ACPMSumDataViewer implements ILazyContentProvider
{
    /** View update frequency interval. */
    final private static int UPDATE_INTERVAL = 10000;
    
    /** Executor to use instead of Display.asyncExec(). **/
    @ThreadSafe
    final private DsfExecutor fDisplayExecutor;
    
    /** Executor to use when retrieving data from data providers */
    @ThreadSafe
    final private ImmediateInDsfExecutor fDataExecutor;
    
    // The viewer and generator that this content provider using.
    final private TableViewer fViewer;
    final private DataGeneratorCacheManager[] fDataGeneratorCMs;
    final private DataGeneratorCacheManager fSumGeneratorCM;

    // Fields used in request cancellation logic.
    private List<ValueRequestMonitor> fItemDataRequestMonitors = 
        new LinkedList<ValueRequestMonitor>();
    private Set<Integer> fIndexesToCancel = new HashSet<Integer>();
    private int fCancelCallsPending = 0;
    private Future<?> fRefreshFuture;
    
    public ACPMSumDataViewer(TableViewer viewer,
        ImmediateInDsfExecutor dataExecutor, IDataGenerator[] generators, 
        IDataGenerator sumGenerator) 
    {
        fViewer = viewer;
        fDisplayExecutor = DisplayDsfExecutor.getDisplayDsfExecutor(
            fViewer.getTable().getDisplay());
        fDataExecutor = dataExecutor;
        
        // Create wrappers for data generators.  Don't need to register as 
        // listeners to generator events because the cache managers ensure data
        // are already registered for them.
        fDataGeneratorCMs = new DataGeneratorCacheManager[generators.length];
        for (int i = 0; i < generators.length; i++) {
            fDataGeneratorCMs[i] = 
                new DataGeneratorCacheManager(fDataExecutor, generators[i]);
        }
        fSumGeneratorCM = 
            new DataGeneratorCacheManager(fDataExecutor, sumGenerator); 
        
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
        
        // Need to dispose cache managers that were created in this class.  This 
        // needs to be done on the cache manager's thread. 
        Query<Object> disposeCacheManagersQuery = new Query<Object>() {
            @Override
            protected void execute(DataRequestMonitor<Object> rm) {
                fSumGeneratorCM.dispose();
                for (DataGeneratorCacheManager dataGeneratorCM : 
                     fDataGeneratorCMs) 
                {
                    dataGeneratorCM.dispose();                
                }
                rm.setData(new Object());
                rm.done();
            }
        };
        fDataExecutor.execute(disposeCacheManagersQuery);
        try {
            disposeCacheManagersQuery.get();
        } 
        catch (InterruptedException e) {} 
        catch (ExecutionException e) {}
        
        // Cancel any outstanding data requests.
        for (ValueRequestMonitor rm : fItemDataRequestMonitors) {
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
     * Retrieve the current count.  When a new count is set to viewer, the viewer
     * will refresh all items as well.
     */
    private void queryItemCount() {
        // Create the request monitor to collect the count.  This request 
        // monitor will be completed by the following transaction.
        final DataRequestMonitor<Integer> rm = 
            new DataRequestMonitor<Integer>(fDisplayExecutor, null) 
        {
            @Override
            protected void handleSuccess() {
                setCountToViewer(getData());
            }
            @Override
            protected void handleRejectedExecutionException() {}     // Shutting down, ignore.
        };

        // Use a transaction, even with a single cache.  This will ensure that 
        // if the cache is reset during processing by an event.  The request 
        // for data will be re-issued. 
        fDataExecutor.execute(new Runnable() {
            public void run() {
                new Transaction<Integer>() {
                    @Override
                    protected Integer process() 
                        throws Transaction.InvalidCacheException, CoreException 
                    {
                        return processCount(this);
                    }
                }.request(rm);
            }
        });
    }
    
    /** 
     * Perform the count retrieval from the sum data generator.
     * @param transaction The ACPM transaction to use for calculation.
     * @return Calculated count.
     * @throws Transaction.InvalidCacheException {@link Transaction#process}
     * @throws CoreException See {@link Transaction#process}
     */
    private Integer processCount(Transaction<Integer> transaction) 
        throws Transaction.InvalidCacheException, CoreException 
    {
        ICache<Integer> countCache = fSumGeneratorCM.getCount();
        transaction.validate(countCache);
        return countCache.getData();
    }

    /** 
     * Set the givne count to the viewer.  This will cause the viewer will 
     * refresh all items' data as well.
     * <p>Note: This method must be called in the display thread. </p>
     * @param count New count to set to viewer.
     */
    private void setCountToViewer(int count) {
        if (!fViewer.getTable().isDisposed()) {
            fViewer.setItemCount(count);
            fViewer.getTable().clearAll();
        }
    }

    /**
     * Retrieve the current value for given index.
     */
    private void queryValue(final int index) {
        // Create the request monitor to collect the value.  This request 
        // monitor will be completed by the following transaction.  
        final ValueRequestMonitor rm = new ValueRequestMonitor(index) {
            @Override
            protected void handleCompleted() {
                fItemDataRequestMonitors.remove(this);
                if (isSuccess()) {
                    setValueToViewer(index, getData());
                }
            }
            @Override
            protected void handleRejectedExecutionException() {
                // Shutting down, ignore.  
            } 
        };

        // Save the value request monitor, to cancel it if the view is 
        // scrolled. 
        fItemDataRequestMonitors.add(rm);

        // Use a transaction, even with a single cache.  This will ensure that 
        // if the cache is reset during processing by an event.  The request 
        // for data will be re-issued. 
        fDataExecutor.execute(new Runnable() {
            public void run() {
                new Transaction<String>() {
                    @Override
                    protected String process() 
                        throws Transaction.InvalidCacheException, CoreException 
                    {
                        return processValue(this, index);
                    }
                }.request(rm);
            }
        });
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
     * Perform the calculation compose the string with data provider values 
     * and the sum.  This implementation also validates the result. 
     * @param transaction The ACPM transaction to use for calculation.
     * @param index Index of value to calculate.
     * @return Calculated value.
     * @throws Transaction.InvalidCacheException {@link Transaction#process}
     * @throws CoreException See {@link Transaction#process}
     */
    private String processValue(Transaction<String> transaction, int index) 
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
        
        // TODO: evaluate sum generator cache in parallel with value caches.
        ICache<Integer> sumCache = fSumGeneratorCM.getValue(index);
        transaction.validate(sumCache);
        
        // Compose the string with values, sum, and validation result.
        StringBuilder result = new StringBuilder();
        int calcSum = 0;
        for (ICache<Integer> valueCache : valueCaches) {
            if (result.length() != 0) result.append(" + ");
            result.append(valueCache.getData());
            calcSum += valueCache.getData();
        }
        result.append(" = ");
        result.append(sumCache.getData());
        if (calcSum != sumCache.getData()) {
            result.append(" !INCORRECT! ");
        }
        
        return result.toString(); 
    }
    
    /** 
     * Dedicated class for data item requests.  This class holds the index
     * argument so it can be examined when canceling stale requests.
     */
    private class ValueRequestMonitor extends DataRequestMonitor<String> {
        /** Index is used when canceling stale requests. */
        int fIndex;
        
        ValueRequestMonitor(int index) {
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
        if (fDataGeneratorCMs == null || fViewer.getTable().isDisposed()) {
            return;
        }

        // Go through the outstanding requests and cancel any that 
        // are not visible anymore.
        for (Iterator<ValueRequestMonitor> itr = 
            fItemDataRequestMonitors.iterator(); itr.hasNext();) 
        {
            ValueRequestMonitor item = itr.next();
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

        DsfExecutor executor = new DefaultDsfExecutor("Example executor");
        
        // Create the data generator.
        final IDataGenerator[] generators = new IDataGenerator[5];
        for (int i = 0; i < generators.length; i++) {
            generators[i] = new DataGeneratorWithExecutor(executor);
        }
        final IDataGenerator sumGenerator = 
            new ACPMSumDataGenerator(executor, generators);
        
        // Create the content provider which will populate the viewer.
        ACPMSumDataViewer contentProvider = new ACPMSumDataViewer(
            tableViewer, new ImmediateInDsfExecutor(executor), 
            generators, sumGenerator);
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

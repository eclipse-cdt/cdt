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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.dd.dsf.concurrent.DataRequestMonitor;
import org.eclipse.dd.dsf.concurrent.DsfExecutor;
import org.eclipse.dd.examples.dsf.DsfExamplesPlugin;
import org.eclipse.jface.viewers.ILazyContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;

public class CancellingSlowDataProviderContentProvider 
    implements ILazyContentProvider, DataProvider.Listener
{
    TableViewer fTableViewer;
    DataProvider fDataProvider;
    Display fDisplay;
    List<ItemGetDataRequestMonitor> fItemDataRequestMonitors = new LinkedList<ItemGetDataRequestMonitor>();
    Set<Integer> fCancelledIdxs = new HashSet<Integer>();
    AtomicInteger fCancelCallsPending = new AtomicInteger();
    
    ///////////////////////////////////////////////////////////////////////////
    // ILazyContentProvider
    public void dispose() {
        if (fDataProvider != null) {
            final DataProvider dataProvider = fDataProvider;
            dataProvider.getDsfExecutor().execute(
                new Runnable() { public void run() {
                    dataProvider.removeListener(CancellingSlowDataProviderContentProvider.this);
                    fTableViewer = null;
                    fDisplay = null;
                    fDataProvider = null;
                }});
        } else {
            fTableViewer = null;
            fDisplay = null;
        }
    }

    public void inputChanged(final Viewer viewer, Object oldInput, final Object newInput) {
        // If old data provider is not-null, unregister from it as listener.
        if (fDataProvider != null) {
            final DataProvider dataProvider = fDataProvider;
            dataProvider.getDsfExecutor().execute(
                new Runnable() { public void run() {
                    dataProvider.removeListener(CancellingSlowDataProviderContentProvider.this);
                }});
        }
        
        
        // Register as listener with new data provider.
        // Note: if old data provider and new data provider use different executors, 
        // there is a chance of a race condition here.
        if (newInput != null) {
            ((DataProvider)newInput).getDsfExecutor().execute(
                new Runnable() { public void run() {
                    if ( ((TableViewer)viewer).getTable().isDisposed() ) return;
                    fTableViewer = (TableViewer)viewer;
                    fDisplay = fTableViewer.getTable().getDisplay();
                    fDataProvider = (DataProvider)newInput;
                    fDataProvider.addListener(CancellingSlowDataProviderContentProvider.this);
                    queryItemCount();
                }});
        }
    }

    public void updateElement(final int index) {
        assert fTableViewer != null;
        if (fDataProvider == null) return;
        
        // Calculate the visible index range.
        final int topIdx = fTableViewer.getTable().getTopIndex();
        final int botIdx = topIdx + getVisibleItemCount(topIdx);
        
        fCancelCallsPending.incrementAndGet();
        fDataProvider.getDsfExecutor().execute(
            new Runnable() { public void run() {
                // Must check again, in case disposed while re-dispatching.
                if (fDataProvider == null || fTableViewer.getTable().isDisposed()) return;
                if (index >= topIdx && index <= botIdx) {
                    queryItemData(index);
                }
                cancelStaleRequests(topIdx, botIdx);
            }});
    }
        
    protected int getVisibleItemCount(int top) {
        Table table = fTableViewer.getTable();
        int itemCount = table.getItemCount();
        return Math.min((table.getBounds().height / table.getItemHeight()) + 2, itemCount - top);
    }   
    
    ///////////////////////////////////////////////////////////////////////////
    // DataProvider.Listener 
    public void countChanged() {
        // Check for dispose.
        if (fDataProvider == null) return;
        
        // Request new count.
        queryItemCount();
    }
    
    public void dataChanged(final Set<Integer> indexes) {
        // Check for dispose.
        if (fDataProvider == null) return;

        // Clear changed items in table viewer.
        final TableViewer tableViewer = fTableViewer;
        fDisplay.asyncExec(
            new Runnable() { public void run() {
                // Check again if table wasn't disposed when 
                // switching to the display thread.
                if (fTableViewer == null || fTableViewer.getTable().isDisposed()) return;
                for (Integer index : indexes) {
                    tableViewer.clear(index);
                }
            }});
    }
    //
    ///////////////////////////////////////////////////////////////////////////

    
    /** 
     * Convenience extension to standard data return runnable.  This extension 
     * automatically checks for errors and asynchronous dispose.
     * @param <V>
     */
    private abstract class CPGetDataRequestMonitor<V> extends DataRequestMonitor<V> {
        public CPGetDataRequestMonitor(DsfExecutor executor) { super(executor, null); }
        abstract protected void doRun(); 
        @Override
        public void handleCompleted() {
            // If there is an error processing request, return.
            if (!getStatus().isOK()) return;
            
            // If content provider was disposed, return.
            if (fTableViewer == null || fTableViewer.getTable().isDisposed()) return;
            
            // Otherwise execute runnable.
            doRun(); 
        }
    }

    /**
     * Executes the item count query with DataProvider.  Must be called on 
     * data provider's dispatch thread.
     */
    private void queryItemCount() {
        assert fDataProvider.getDsfExecutor().isInExecutorThread();

        // Request coumt from data provider.  When the count is returned, we 
        // have to re-dispatch into the display thread to avoid calling
        // the table widget on the DSF dispatch thread.
        fCancelledIdxs.clear();
        fDataProvider.getItemCount(
            new CPGetDataRequestMonitor<Integer>(fDataProvider.getDsfExecutor()) {
                @Override
                protected void doRun() {
                    final TableViewer tableViewer = fTableViewer;
                    tableViewer.getTable().getDisplay().asyncExec(
                            new Runnable() { public void run() {
                                // Check again if table wasn't disposed when 
                                // switching to the display thread.
                                if (tableViewer.getTable().isDisposed()) return; // disposed
                                tableViewer.setItemCount(getData());
                                tableViewer.getTable().clearAll();
                            }});
        }}); 
        
    }

    
    /**
     * Dedicated class for data item requests.  This class holds the index
     * argument so it can be examined when cancelling stale requests.
     */
    // Request data from data provider.  Likewise, when the data is 
    // returned, we have to re-dispatch into the display thread to 
    // call the table widget.
    class ItemGetDataRequestMonitor extends CPGetDataRequestMonitor<String> {
        
        /** Index is used when cancelling stale requests. */
        int fIndex;
        
        ItemGetDataRequestMonitor(DsfExecutor executor, int index) {
            super(executor); 
            fIndex = index; 
        }

        // Remove the request from list of outstanding requests.  This has 
        // to be done in run() because doRun() is not always called.
        @Override
        public void handleCompleted() {
            fItemDataRequestMonitors.remove(this);
            super.handleCompleted();
        }
        
        // Process the result as usual.
        @Override
        protected void doRun() {
            final TableViewer tableViewer = fTableViewer;
            tableViewer.getTable().getDisplay().asyncExec(
                new Runnable() { public void run() {
                    // Check again if table wasn't disposed when 
                    // switching to the display thread.
                    if (tableViewer.getTable().isDisposed()) return; // disposed
                    tableViewer.replace(getData(), fIndex);
                }}); 
        }
    }

    /**
     * Executes the data query with DataProvider.  Must be called on dispatch
     * thread.
     * @param index Index of item to fetch.
     */
    private void queryItemData(final int index) {
        assert fDataProvider.getDsfExecutor().isInExecutorThread();
        
        ItemGetDataRequestMonitor rm = new ItemGetDataRequestMonitor(fDataProvider.getDsfExecutor(), index);
        fItemDataRequestMonitors.add(rm);
        fDataProvider.getItem(index, rm); 
    }
    
    /**
     * Iterates through the outstanding requests to data provider and 
     * cancells any that are nto visible any more.
     * @param topIdx Top index of the visible items
     * @param botIdx Bottom index of the visible items
     */
    private void cancelStaleRequests(int topIdx, int botIdx) {
        // Go through the outstanding requests and cencel any that 
        // are not visible anymore.
        for (Iterator<ItemGetDataRequestMonitor> itr = fItemDataRequestMonitors.iterator(); itr.hasNext();) {
            ItemGetDataRequestMonitor item = itr.next();
            if (item.fIndex < topIdx || item.fIndex > botIdx) {
                // Set the item to cancelled status, so that the data provider 
                // will ignore it.
                item.setStatus(new Status(IStatus.CANCEL, DsfExamplesPlugin.PLUGIN_ID, 0, "Cancelled", null)); //$NON-NLS-1$
                
                // Add the item index to list of indexes that were cancelled, 
                // which will be sent to the table widget. 
                fCancelledIdxs.add(item.fIndex);
                
                // Remove the item from the outstanding cancel requests.
                itr.remove(); 
            }
        }
        int cancelRequestsPending = fCancelCallsPending.decrementAndGet();
        if (!fCancelledIdxs.isEmpty() && cancelRequestsPending == 0) {
            final Set<Integer> cancelledIdxs = fCancelledIdxs;
            fCancelledIdxs = new HashSet<Integer>();
            final TableViewer tableViewer = fTableViewer;
            tableViewer.getTable().getDisplay().asyncExec(
                new Runnable() { public void run() {
                    // Check again if table wasn't disposed when 
                    // switching to the display thread.
                    if (tableViewer.getTable().isDisposed()) return; 
                    
                    // Clear the indexes of the cancelled request, so that the 
                    // viewer knows to request them again when needed.  
                    // Note: clearing using TableViewer.clear(int) seems very
                    // inefficient, it's better to use Table.clear(int[]).
                    int[] cancelledIdxsArray = new int[cancelledIdxs.size()];
                    int i = 0;
                    for (Integer index : cancelledIdxs) {
                        cancelledIdxsArray[i++] = index;
                    }
                    tableViewer.getTable().clear(cancelledIdxsArray);
                }});
        }
    }
}

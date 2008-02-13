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

import java.util.Set;

import org.eclipse.dd.dsf.concurrent.DataRequestMonitor;
import org.eclipse.dd.dsf.concurrent.DsfExecutor;
import org.eclipse.jface.viewers.ILazyContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;

public class SlowDataProviderContentProvider 
    implements ILazyContentProvider, DataProvider.Listener
{
    
    TableViewer fTableViewer;
    DataProvider fDataProvider;
    
    ///////////////////////////////////////////////////////////////////////////
    // ILazyContentProvider
    public void dispose() {
        if (fDataProvider != null) {
            final DataProvider dataProvider = fDataProvider;
            dataProvider.getDsfExecutor().execute(
                new Runnable() { public void run() {
                    dataProvider.removeListener(SlowDataProviderContentProvider.this);
                    fTableViewer = null;
                    fDataProvider = null;
                }});
        } else {
            fTableViewer = null;
        }
    }

    public void inputChanged(final Viewer viewer, Object oldInput, final Object newInput) {
        // If old data provider is not-null, unregister from it as listener.
        if (fDataProvider != null) {
            final DataProvider dataProvider = fDataProvider;
            dataProvider.getDsfExecutor().execute(
                new Runnable() { public void run() {
                    dataProvider.removeListener(SlowDataProviderContentProvider.this);
                }});
        }
        
        
        // Register as listener with new data provider.
        // Note: if old data provider and new data provider use different executors, 
        // there is a chance of a race condition here.
        if (newInput != null) {
            ((DataProvider)newInput).getDsfExecutor().execute(
                new Runnable() { public void run() {
                    fTableViewer = (TableViewer)viewer;
                    fDataProvider = (DataProvider)newInput;
                    fDataProvider.addListener(SlowDataProviderContentProvider.this);
                    queryItemCount();
                }});
        }
    }

    public void updateElement(final int index) {
        assert fTableViewer != null;
        if (fDataProvider == null) return;

        fDataProvider.getDsfExecutor().execute(
            new Runnable() { public void run() {
                // Must check again, in case disposed while re-dispatching.
                if (fDataProvider == null) return;
                
                queryItemData(index);
            }});
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
        if (fTableViewer != null) {
            final TableViewer tableViewer = fTableViewer;
            tableViewer.getTable().getDisplay().asyncExec(
                new Runnable() { public void run() {
                    // Check again if table wasn't disposed when 
                    // switching to the display thread.
                    if (tableViewer.getTable().isDisposed()) return; // disposed
                    for (Integer index : indexes) {
                        tableViewer.clear(index);
                    }
                }});
        }
    }
    //
    ///////////////////////////////////////////////////////////////////////////

    
    /** 
     * Convenience extension to standard data return runnable.  This extension 
     * automatically checks for errors and asynchronous dipose.
     * @param <V>
     */
    private abstract class CPGetDataRequestMonitor<V> extends DataRequestMonitor<V> {
        CPGetDataRequestMonitor(DsfExecutor executor) { super(executor, null); }
        abstract protected void doRun(); 
        @Override
        final public void handleCompleted() {
            // If there is an error processing request, return.
            if (!getStatus().isOK()) return;
            
            // If content provider was disposed, return.
            if (fTableViewer == null) return;
            
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
     * Executes the data query with DataProvider.  Must be called on dispatch
     * thread.
     * @param index Index of item to fetch.
     */
    private void queryItemData(final int index) {
        assert fDataProvider.getDsfExecutor().isInExecutorThread();
        
        // Request data from data provider.  Likewise, when the data is 
        // returned, we have to re-dispatch into the display thread to 
        // call the table widget.
        fDataProvider.getItem(
            index, 
            new CPGetDataRequestMonitor<String>(fDataProvider.getDsfExecutor()) {
                @Override
                protected void doRun() {
                    final TableViewer tableViewer = fTableViewer;
                    tableViewer.getTable().getDisplay().asyncExec(
                            new Runnable() { public void run() {
                                // Check again if table wasn't disposed when 
                                // switching to the display thread.
                                if (tableViewer.getTable().isDisposed()) return; // disposed
                                tableViewer.replace(getData(), index);
                            }});
        }}); 
    }
    
}

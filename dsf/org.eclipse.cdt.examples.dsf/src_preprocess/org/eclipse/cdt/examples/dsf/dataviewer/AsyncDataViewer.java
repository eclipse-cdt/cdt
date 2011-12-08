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

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.eclipse.cdt.dsf.concurrent.ConfinedToDsfExecutor;
import org.eclipse.cdt.dsf.concurrent.ThreadSafe;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.concurrent.ImmediateExecutor;
import org.eclipse.cdt.dsf.concurrent.Query;
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
 * Data viewer based on a table, which reads data using asynchronous methods.
 * <p>
 * This viewer implements the {@link ILazyContentProvider} interface 
 * which is used by the JFace TableViewer class to populate a Table.  This 
 * interface contains separate asynchronous methods for requesting the count 
 * and values for individual indexes, which neatly correspond to the methods
 * in {@link IDataGenerator}.  As an added optimization, this viewer 
 * implementation checks for the range of visible items in the view upon each 
 * request, and it cancels old requests which scroll out of view but have not
 * been completed yet.  However, it is up to the data generator implementation 
 * to check the canceled state of the requests and ignore them.
 * </p>
 */
@ConfinedToDsfExecutor("fDisplayExecutor")
public class AsyncDataViewer 
    implements ILazyContentProvider, IDataGenerator.Listener
{
    // Executor to use instead of Display.asyncExec().
    @ThreadSafe
    final private DsfExecutor fDisplayExecutor;
    
    // The viewer and generator that this content provider using.
    final private TableViewer fViewer;
    final private IDataGenerator fDataGenerator;

    // Fields used in request cancellation logic.
    private List<ValueDataRequestMonitor> fItemDataRequestMonitors = 
        new LinkedList<ValueDataRequestMonitor>();
    private Set<Integer> fIndexesToCancel = new HashSet<Integer>();
    private int fCancelCallsPending = 0;
    
    public AsyncDataViewer(TableViewer viewer, IDataGenerator generator) {
        fViewer = viewer;
        fDisplayExecutor = DisplayDsfExecutor.getDisplayDsfExecutor(
            fViewer.getTable().getDisplay());
        fDataGenerator = generator;
        fDataGenerator.addListener(this);
    }    
    
    public void dispose() {
        fDataGenerator.removeListener(this);
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
        fDisplayExecutor.schedule(
            new Runnable() { public void run() {
                cancelStaleRequests(topIdx, botIdx);
            }}, 
            1, TimeUnit.MILLISECONDS);
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
    
    @ThreadSafe
    public void countChanged() {
        queryItemCount();
    }
    
    @ThreadSafe
    public void valuesChanged(final Set<Integer> indexes) {
        // Mark the changed items in table viewer as dirty, this will 
        // trigger update requests for these indexes if they are 
        // visible in the viewer.
        final TableViewer tableViewer = fViewer;
        fDisplayExecutor.execute( new Runnable() { 
            public void run() {
                if (!fViewer.getTable().isDisposed()) {
                    for (Integer index : indexes) {
                        tableViewer.clear(index);
                    }
                }
            }});
    }
    
    /**
     * Retrieve the up to date count.  When a new count is set to viewer, the 
     * viewer will refresh all items as well.
     */
    private void queryItemCount() {
        // Request count from data provider.  When the count is returned, we 
        // have to re-dispatch into the display thread to avoid calling
        // the table widget on the DSF dispatch thread.
        fIndexesToCancel.clear();
        fDataGenerator.getCount( 
            // Use the display executor to construct the request monitor, this 
            // will cause the handleCompleted() method to be automatically
            // called on the display thread.
            new DataRequestMonitor<Integer>(fDisplayExecutor, null) {
                @Override
                protected void handleCompleted() {
                    if (!fViewer.getTable().isDisposed()) {
                        fViewer.setItemCount(getData());
                        fViewer.getTable().clearAll();
                    }
                }
            }); 
    }


    /**
     * Retrieves value of an element at given index.  When complete the value
     * is written to the viewer. 
     * @param index Index of value to retrieve.
     */
    private void queryValue(final int index) {
        ValueDataRequestMonitor rm = new ValueDataRequestMonitor(index);
        fItemDataRequestMonitors.add(rm);
        fDataGenerator.getValue(index, rm); 
    }

    /** 
     * Dedicated class for data item requests.  This class holds the index
     * argument so it can be examined when canceling stale requests.
     */
    private class ValueDataRequestMonitor extends DataRequestMonitor<Integer> {
        
        /** Index is used when canceling stale requests. */
        int fIndex;
        
        ValueDataRequestMonitor(int index) {
            super(fDisplayExecutor, null);
            fIndex = index; 
        }

        @Override
        protected void handleCompleted() {
            fItemDataRequestMonitors.remove(this);

            // Check if the request completed successfully, otherwise ignore 
            // it.
            if (isSuccess()) {
                if (!fViewer.getTable().isDisposed()) {
                    fViewer.replace(getData(), fIndex);
                }
            }
        }
    }
    
    private void cancelStaleRequests(int topIdx, int botIdx) {
        // Decrement the count of outstanding cancel calls.
        fCancelCallsPending--;

        // Must check again, in case disposed while re-dispatching.
        if (fDataGenerator == null || fViewer.getTable().isDisposed()) return;

        // Go through the outstanding requests and cancel any that 
        // are not visible anymore.
        for (Iterator<ValueDataRequestMonitor> itr = 
                fItemDataRequestMonitors.iterator(); 
            itr.hasNext();) 
        {
            ValueDataRequestMonitor item = itr.next();
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

        // Create the data generator.
        final IDataGenerator generator = new DataGeneratorWithExecutor();
        
        // Create the content provider which will populate the viewer.
        AsyncDataViewer contentProvider = 
            new AsyncDataViewer(tableViewer, generator);
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
                generator.shutdown(rm);
            }
        };
        ImmediateExecutor.getInstance().execute(shutdownQuery);
        try {
            shutdownQuery.get();
        } catch (Exception e) {} 
        
        // Shut down the display.
        font.dispose();    
        display.dispose();
    }
}

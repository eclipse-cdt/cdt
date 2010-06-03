/*******************************************************************************
 * Copyright (c) 2008, 2009 Wind River Systems and others.
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

import java.util.Set;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateExecutor;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * Data viewer based on a table, which reads data using synchronous methods.
 * <p>
 * This viewer implements the {@link IStructuredContentProvider} interface 
 * which is used by the JFace TableViewer class to populate a Table.  This 
 * interface contains one principal methods for reading data {@link #getElements(Object)}, 
 * which synchronously returns an array of elements.  In order to implement this 
 * method using the asynchronous data generator, this provider uses the 
 * {@link Query} object. 
 * </p>
 */
public class SyncDataViewer 
    implements IStructuredContentProvider, IDataGenerator.Listener 
{
    // The viewer and generator that this content provider using.
    final private TableViewer fViewer;
    final private IDataGenerator fDataGenerator;
    
    public SyncDataViewer(TableViewer viewer, IDataGenerator generator) {
        fViewer = viewer;
        fDataGenerator = generator;
        fDataGenerator.addListener(this);
    }
    
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        // Not used
    }

    
    public Object[] getElements(Object inputElement) {
        
        // Create the query object for reading data count. 
        Query<Integer> countQuery = new Query<Integer>() {
            @Override
            protected void execute(DataRequestMonitor<Integer> rm) {
                fDataGenerator.getCount(rm);
            }
        };
        
        // Submit the query to be executed.  A query implements a runnable
        // interface and it has to be executed in order to do its work.
        ImmediateExecutor.getInstance().execute(countQuery);
        int count = 0;
        
        // Block until the query completes, which will happen when the request
        // monitor of the execute() method is marked done.
        try {
            count = countQuery.get();
        } catch (Exception e) { 
            // InterruptedException and ExecutionException can be thrown here.
            // ExecutionException containing a CoreException will be thrown 
            // if an error status is set to the Query's request monitor.
            return new Object[0]; 
        } 

        // Create the array that will be filled with elements.
        // For each index in the array execute a query to get the element at
        // that index.
        final Object[] elements = new Object[count];

        for (int i = 0; i < count; i++) {
            final int index = i;
            Query<String> valueQuery = new Query<String>() {
                @Override
                protected void execute(DataRequestMonitor<String> rm) {
                    fDataGenerator.getValue(index, rm);
                }
            };
            ImmediateExecutor.getInstance().execute(valueQuery);
            try {
                elements[i] = valueQuery.get();
            } catch (Exception e) { 
                elements[i] = "error";
            } 
        }
        return elements;
    }

    public void dispose() {
        fDataGenerator.removeListener(this);
    }

    public void countChanged() {
        // For any event from the generator, refresh the whole viewer.
        refreshViewer();
    }
    
    public void valuesChanged(Set<Integer> indexes) {
        // For any event from the generator, refresh the whole viewer.
        refreshViewer();
    }
    
    private void refreshViewer() {
        //#ifdef exercises
        // TODO Exercise 5 - Add a call to getElements() to force a deadlock.
        //#else
//#        getElements(null);
        //#endif
        
        // This method may be called on any thread, switch to the display 
        // thread before calling the viewer.
        Display display = fViewer.getControl().getDisplay(); 
        display.asyncExec( new Runnable() {
            public void run() {
                if (!fViewer.getControl().isDisposed()) {
                    fViewer.refresh();
                }
            }
        });
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
        TableViewer tableViewer = new TableViewer(shell, SWT.BORDER);
        tableViewer.getControl().setLayoutData(data);

        // Create the data generator.
        //#ifdef exercises
        // TODO Exercise 5 - Use the DataGeneratorWithExecutor() instead.
        final IDataGenerator generator = new DataGeneratorWithThread();
        //#else
//#        final IDataGenerator generator = new DataGeneratorWithExecutor();     
        //#endif
        
        // Create the content provider which will populate the viewer.
        SyncDataViewer contentProvider = new SyncDataViewer(tableViewer, generator);
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

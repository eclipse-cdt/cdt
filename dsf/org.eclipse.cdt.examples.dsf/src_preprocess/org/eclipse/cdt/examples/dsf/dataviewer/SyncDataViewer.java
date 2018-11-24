/*******************************************************************************
 * Copyright (c) 2008, 2011 Wind River Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
import java.util.List;
import java.util.Set;

import org.eclipse.cdt.dsf.concurrent.CountingRequestMonitor;
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
 * which synchronously returns an array of elements.  In order to implement
 * this method using the asynchronous data generator, this provider uses the
 * {@link Query} object.
 * </p>
 */
public class SyncDataViewer implements IStructuredContentProvider, IDataGenerator.Listener {
	// The viewer and generator that this content provider using.
	final private TableViewer fViewer;
	final private IDataGenerator fDataGenerator;

	public SyncDataViewer(TableViewer viewer, IDataGenerator generator) {
		fViewer = viewer;
		fDataGenerator = generator;
		fDataGenerator.addListener(this);
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// Not used
	}

	@Override
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

		final int finalCount = count;
		Query<List<Integer>> valueQuery = new Query<List<Integer>>() {
			@Override
			protected void execute(final DataRequestMonitor<List<Integer>> rm) {
				final Integer[] retVal = new Integer[finalCount];
				final CountingRequestMonitor crm = new CountingRequestMonitor(ImmediateExecutor.getInstance(), rm) {
					@Override
					protected void handleSuccess() {
						rm.setData(Arrays.asList(retVal));
						rm.done();
					};
				};
				for (int i = 0; i < finalCount; i++) {
					final int finalI = i;
					fDataGenerator.getValue(i, new DataRequestMonitor<Integer>(ImmediateExecutor.getInstance(), crm) {
						@Override
						protected void handleSuccess() {
							retVal[finalI] = getData();
							crm.done();
						}
					});
				}
				crm.setDoneCount(finalCount);
			}
		};
		ImmediateExecutor.getInstance().execute(valueQuery);
		try {
			return valueQuery.get().toArray(new Integer[0]);
		} catch (Exception e) {
		}
		return new Object[0];
	}

	@Override
	public void dispose() {
		fDataGenerator.removeListener(this);
	}

	@Override
	public void countChanged() {
		// For any event from the generator, refresh the whole viewer.
		refreshViewer();
	}

	@Override
	public void valuesChanged(Set<Integer> indexes) {
		// For any event from the generator, refresh the whole viewer.
		refreshViewer();
	}

	private void refreshViewer() {
		//#ifdef exercises
		// TODO Exercise 5 - Add a call to getElements() to force a deadlock.
		//#else
		//#getElements(null);
		//#endif

		// This method may be called on any thread, switch to the display
		// thread before calling the viewer.
		Display display = fViewer.getControl().getDisplay();
		display.asyncExec(new Runnable() {
			@Override
			public void run() {
				if (!fViewer.getControl().isDisposed()) {
					fViewer.refresh();
				}
			}
		});
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
		TableViewer tableViewer = new TableViewer(shell, SWT.BORDER);
		tableViewer.getControl().setLayoutData(data);

		// Create the data generator.
		//#ifdef exercises
		// TODO Exercise 5 - Use the DataGeneratorWithExecutor() instead.
		final IDataGenerator generator = new DataGeneratorWithThread();
		//#else
		//#final IDataGenerator generator = new DataGeneratorWithExecutor();
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
		} catch (Exception e) {
		}

		// Shut down the display.
		font.dispose();
		display.dispose();
	}

}

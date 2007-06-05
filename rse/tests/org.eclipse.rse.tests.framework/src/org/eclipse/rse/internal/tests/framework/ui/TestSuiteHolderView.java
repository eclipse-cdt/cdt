/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * David Dykstal (IBM) - initial contribution.
 *******************************************************************************/
package org.eclipse.rse.internal.tests.framework.ui;

import java.net.URL;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.Calendar;
import junit.framework.TestResult;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.rse.internal.tests.framework.TestFrameworkPlugin;
import org.eclipse.rse.tests.framework.AbstractTestSuiteHolder;
import org.eclipse.rse.tests.framework.DelegatingTestSuiteHolder;
import org.eclipse.rse.tests.framework.ITestSuiteHolder;
import org.eclipse.rse.tests.framework.ITestSuiteHolderListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Sash;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.part.ViewPart;
import org.osgi.framework.Bundle;

/**
 * Provides a view of the test suites installed in this workbench.
 */
public class TestSuiteHolderView extends ViewPart implements ITestSuiteHolderListener, ISelectionChangedListener {

	private class MyLabelProvider implements ITableLabelProvider {
		public Image getColumnImage(Object element, int columnIndex) {
			AbstractTestSuiteHolder holder = (AbstractTestSuiteHolder) element;
			Image columnImage = null;
			String columnId = getColumnId(columnIndex);
			if (columnId.equals("graphic")) { //$NON-NLS-1$
				TestResult result = holder.getTestResult();
				if (result != null) {
					if (result.wasSuccessful()) {
						columnImage = graphicPassed;
					} else {
						columnImage = graphicFailed;
					}
				} else {
					columnImage = graphicUnknown;
				}
			}
			return columnImage;
		}

		public String getColumnText(Object element, int columnIndex) {
			AbstractTestSuiteHolder holder = (AbstractTestSuiteHolder) element;
			String columnText = null;
			String columnId = getColumnId(columnIndex);
			if (columnId.equals("name")) { //$NON-NLS-1$
				columnText = holder.getName();
			} else if (columnId.equals("graphic")) { //$NON-NLS-1$
				columnText = ""; //$NON-NLS-1$
			} else if (columnId.equals("status")) { //$NON-NLS-1$
				TestResult result = holder.getTestResult();
				if (result != null) {
					Object[] values = { new Integer(result.runCount()), new Integer(result.failureCount()), new Integer(result.errorCount()) };
					String template = "{0,number,integer} run, {1,number,integer} failed, {2,number,integer} errors"; //$NON-NLS-1$
					columnText = MessageFormat.format(template, values);
				} else {
					columnText = ""; //$NON-NLS-1$
				}
			} else if (columnId.equals("stamp")) { //$NON-NLS-1$
				Calendar stamp = holder.getLastRunTime();
				if (stamp != null) {
					DateFormat formatter = DateFormat.getDateTimeInstance();
					columnText = formatter.format(stamp.getTime());
				} else {
					columnText = ""; //$NON-NLS-1$
				}
			}
			return columnText;
		}

		public void addListener(ILabelProviderListener listener) {
		}

		public void dispose() {
		}

		public boolean isLabelProperty(Object element, String property) {
			return false;
		}

		public void removeListener(ILabelProviderListener listener) {
		}
	}
	
	private abstract class ColumnSorter extends ViewerSorter {
		boolean ascending = false;
		public void reverse() {
			ascending = !ascending;
		}
		public int compare(Viewer viewer, Object e1, Object e2) {
			String key1 = getKey(e1);
			String key2 = getKey(e2);
			int result = key1.compareTo(key2);
			if (!ascending) result = -result; 
			return result;
		}
		public abstract String getKey(Object e);
	}
	
	private class GraphicSorter extends ColumnSorter {
		public String getKey(Object e) {
			AbstractTestSuiteHolder h = (AbstractTestSuiteHolder) e;
			TestResult r = h.getTestResult();
			if (r == null) return "0" + r; //$NON-NLS-1$
			if (r.failureCount() > 0) return "1" + r; //$NON-NLS-1$
			if (r.errorCount() > 0) return "1" + r; //$NON-NLS-1$
			return "2" + r; //$NON-NLS-1$
		}
	}
	
	private class NameSorter extends ColumnSorter {
		public String getKey(Object e) {
			return ((AbstractTestSuiteHolder)e).getName();
		}
	}
	
	private class StatusSorter extends ColumnSorter {
		public String getKey(Object e) {
			return ""; //$NON-NLS-1$
		}
	}
	
	private class StampSorter extends ColumnSorter {
		public String getKey(Object e) {
			AbstractTestSuiteHolder h = (AbstractTestSuiteHolder) e;
			Calendar c = h.getLastRunTime();
			long t = 0;
			if (c != null) t = c.getTimeInMillis();
			String k = "0000000000000000000000000" + Long.toString(t); //$NON-NLS-1$
			k = k.substring(k.length() - 25);
			k += h.getName();
			return k;
		}
	}
	
	private class ColumnListener implements SelectionListener {
		ColumnSorter sorter;
		public ColumnListener(ColumnSorter sorter) {
			this.sorter = sorter;
		}
		public void widgetSelected(SelectionEvent e) {
			if (holderViewer.getSorter() == sorter) {
				sorter.reverse();
				holderViewer.refresh();
			} else {
				holderViewer.setSorter(sorter);
			}
		}
		public void widgetDefaultSelected(SelectionEvent e) {
		}
	}
	
	private SelectionListener graphicListener = new ColumnListener(new GraphicSorter());
	private SelectionListener nameListener = new ColumnListener(new NameSorter());
	private SelectionListener statusListener = new ColumnListener(new StatusSorter());
	private SelectionListener stampListener = new ColumnListener(new StampSorter());

	private TableViewer holderViewer;
	private Text resultsText;
	private ArrayContentProvider contentProvider = new ArrayContentProvider();
	private MyLabelProvider labelProvider = new MyLabelProvider();
	private ITestSuiteHolder[] holders = DelegatingTestSuiteHolder.getHolders();
	private String[] columnIds = {"graphic", "name", "status", "stamp"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	private int[] columnWidths = {20, 200, 150, 150};
	private String[] columnTitles = {"", "Test Suite", "Summary", "Time Run"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	private SelectionListener[] columnListeners = {graphicListener, nameListener, statusListener, stampListener};
	private boolean[] columnResizable = {false, true, true, true};
	private boolean[] columnMoveable = {false, true, true, true};
	private Image graphicFailed = null;
	private Image graphicPassed = null;
	private Image graphicUnknown = null;
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createPartControl(Composite parent) {
		// create images
		graphicFailed = createImage("icons/RedX.gif"); //$NON-NLS-1$
		graphicPassed = createImage("icons/GreenCheck.gif"); //$NON-NLS-1$
		graphicUnknown = createImage("icons/YellowQuestion.gif"); //$NON-NLS-1$
		
		// holders viewer
		Table table = new Table(parent, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL | SWT.FULL_SELECTION);
		table.setHeaderVisible(true);
		table.setLinesVisible(false);
		int n = columnIds.length;
		for (int i = 0; i < n; i++) {
			TableColumn column = new TableColumn(table, SWT.NONE);
			column.setData("id", columnIds[i]); //$NON-NLS-1$
			column.setText(columnTitles[i]);
			column.setWidth(columnWidths[i]);
			column.setResizable(columnResizable[i]);
			column.setMoveable(columnMoveable[i]);
			column.addSelectionListener(columnListeners[i]);
		}
		holderViewer = new TableViewer(table);
		
		// menu for above viewer
		MenuManager menuManager = new MenuManager();
		getSite().registerContextMenu(menuManager, holderViewer);
		Control control = holderViewer.getControl();
		control.setMenu(menuManager.createContextMenu(control));
		IContributionItem item = new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS);
		menuManager.add(item);
		
		// sash
		Sash sash = new Sash(parent, SWT.HORIZONTAL);
		sash.addSelectionListener(
			new SelectionListener() {
				public void widgetSelected(SelectionEvent e) {
					Sash s = (Sash) e.widget;
					FormData fd = (FormData) s.getLayoutData();
					fd.top = new FormAttachment(0, e.y);
					s.getParent().layout();
				}
				public void widgetDefaultSelected(SelectionEvent e) {
				}
			}
		);
		
		// results viewer
		resultsText = new Text(parent, SWT.READ_ONLY | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		
		// control layout
		parent.setLayout(new FormLayout());
		FormData fd = null;
		
		// holder viewer layout
		fd = new FormData();
		fd.top = new FormAttachment(0, 0);
		fd.bottom = new FormAttachment(sash, 0);
		fd.left = new FormAttachment(0, 0);
		fd.right = new FormAttachment(100, 0);
		holderViewer.getControl().setLayoutData(fd);
		
		// sash layout
		fd = new FormData();
		fd.top = new FormAttachment(50, 0);
		fd.height = 3;
		fd.left = new FormAttachment(0, 0);
		fd.right = new FormAttachment(100, 0);
		sash.setLayoutData(fd);
		
		// results viewer layout
		fd = new FormData();
		fd.top = new FormAttachment(sash, 0);
		fd.bottom = new FormAttachment(100, 0);
		fd.left = new FormAttachment(0, 0);
		fd.right = new FormAttachment(100, 0);
		resultsText.setLayoutData(fd);
		
		// set up the contents of the holder viewer
		holderViewer.setContentProvider(contentProvider);
		holderViewer.setLabelProvider(labelProvider);
		holderViewer.setInput(holders);
		for (int i = 0; i < holders.length; i++) {
			ITestSuiteHolder holder = holders[i];
			holder.addListener(this);
		}
		holderViewer.setSelection(new StructuredSelection());
		holderViewer.addSelectionChangedListener(this);
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
	 */
	public void selectionChanged(SelectionChangedEvent event) {
		StructuredSelection sel = (StructuredSelection)event.getSelection();
		if (sel.size() == 1) {
			AbstractTestSuiteHolder holder = (AbstractTestSuiteHolder)sel.getFirstElement();
			resultsText.setText(holder.getResultString());
		} else {
			resultsText.setText(""); //$NON-NLS-1$
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchPart#setFocus()
	 */
	public void setFocus() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.tests.framework.ITestSuiteHolderListener#testEnded(org.eclipse.rse.tests.framework.ITestSuiteHolder)
	 */
	public void testEnded(ITestSuiteHolder holder) {
		updateHolderInView(holder);
		updateResultString(holder);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.tests.framework.ITestSuiteHolderListener#testHolderReset(org.eclipse.rse.tests.framework.ITestSuiteHolder)
	 */
	public void testHolderReset(ITestSuiteHolder holder) {
		updateHolderInView(holder);
		updateResultString(holder);
	}
	
	/**
	 * Columns in this table may be reordered. Given a column index retrieve its id.
	 * @param columnIndex the index of the column
	 * @return The string id of the column. Will be null if no id has been assigned or columnIndex is out
	 * of range.
	 */
	private String getColumnId(int columnIndex) {
		String columnId = null;
		if (holderViewer != null) {
			Table table = holderViewer.getTable();
			int n = table.getColumnCount();
			if (0 <= columnIndex && columnIndex < n) {
				TableColumn column = table.getColumn(columnIndex);
				if (column != null) {
					columnId = (String) column.getData("id"); //$NON-NLS-1$
				}
			}
		}
		return columnId;
	}
	
	/**
	 * Updates the view of the particular holder.  Can be run from a non-UI thread.
	 * @param holder
	 */
	private void updateHolderInView(final ITestSuiteHolder holder) {
		Runnable runnable = new Runnable() {
			public void run() {
				holderViewer.update(holder, null);
			}
		};
		Control control = holderViewer.getControl();
		if (!control.isDisposed()) {
			control.getDisplay().syncExec(runnable);
		}
	}
	
	/**
	 * Updates the result string for the holder if the holder is the only one in the
	 * current selection.  Can be run from a non-UI thread.
	 * @param holder
	 */
	private void updateResultString(final ITestSuiteHolder holder) {
		Runnable runnable = new Runnable() {
			public void run() {
				StructuredSelection sel = (StructuredSelection)holderViewer.getSelection();
				if (sel.size() == 1) {
					AbstractTestSuiteHolder holder = (AbstractTestSuiteHolder)sel.getFirstElement();
					resultsText.setText(holder.getResultString());
				} else {
					resultsText.setText(""); //$NON-NLS-1$
				}
			}
		};
		if (!resultsText.isDisposed()) {
			resultsText.getDisplay().syncExec(runnable);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchPart#dispose()
	 */
	public void dispose() {
		for (int i = 0; i < holders.length; i++) {
			ITestSuiteHolder holder = holders[i];
			holder.removeListener(this);
		}
		graphicFailed.dispose();
		graphicPassed.dispose();
		graphicUnknown.dispose();
	}

	public Image createImage(String imageName) {
		Plugin plugin = TestFrameworkPlugin.getDefault();
		IPath path = new Path(imageName);
		Bundle bundle = plugin.getBundle();
		URL url = FileLocator.find(bundle, path, null);
		ImageDescriptor descriptor = ImageDescriptor.createFromURL(url);
		Image image = descriptor.createImage();
		return image;
	}
	
	/**
	 * Selects all the holders in the view.
	 */
	public void selectAll() {
		if (!holderViewer.getControl().isDisposed()) {
			StructuredSelection selection = new StructuredSelection(holders);
			holderViewer.setSelection(selection);
		}
	}
}

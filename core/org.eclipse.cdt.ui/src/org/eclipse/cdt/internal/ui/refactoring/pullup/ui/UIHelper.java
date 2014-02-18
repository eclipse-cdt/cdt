/*******************************************************************************
 * Copyright (c) 2013 Simon Taddiken
 * University of Bremen.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 *     Simon Taddiken (University of Bremen)
 ******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.pullup.ui;


import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TreeItem;

public final class UIHelper {

	
	public static Button newButton(Composite parent, String text) {
		final Button button = new Button(parent, SWT.NONE);
		button.setText(text);
		final GridData grid = new GridData();
		grid.horizontalAlignment = SWT.FILL;
		button.setLayoutData(grid);
		return button;
	}
	
	
	
	public static GridData newLabel(Composite parent, String text, int hSpan) {
		final Label lbl = new Label(parent, SWT.NONE);
		lbl.setText(text);
		final GridData grid = new GridData();
		grid.horizontalAlignment = SWT.FILL;
		grid.horizontalSpan = hSpan;
		lbl.setLayoutData(grid);
		return grid;
	}
	
	
	
	public static TableViewer newTable(Composite parent, int hSpan) {
		final TableViewer viewer = new TableViewer(parent,
				SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
		
		viewer.setContentProvider(new ArrayContentProvider());
		final Table table = viewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		
		final GridData grid = new GridData();
		grid.horizontalAlignment = SWT.FILL;
		grid.verticalAlignment = SWT.FILL;
		grid.horizontalSpan = hSpan;
		grid.grabExcessHorizontalSpace = true;
		grid.grabExcessVerticalSpace = true;
		table.setLayoutData(grid);
		return viewer;
	}
	
	
	// adapted from: http://git.eclipse.org/c/platform/eclipse.platform.swt.git/tree/examples/org.eclipse.swt.snippets/src/org/eclipse/swt/snippets/Snippet274.java
	public static void checkPath(TreeItem item, boolean checked, boolean grayed) {
	    if (item == null) return;

	    if (grayed) {
	        checked = true;
	    } else {
	        int index = 0;
	        TreeItem[] items = item.getItems();
	        while (index < items.length) {
	            TreeItem child = items[index];
	            if (child.getGrayed() || checked != child.getChecked()) {
	                checked = grayed = true;
	                break;
	            }
	            index++;
	        }
	    }
	    item.setChecked(checked);
	    item.setGrayed(grayed);
	    checkPath(item.getParentItem(), checked, grayed);
	}

	// adapted from: http://git.eclipse.org/c/platform/eclipse.platform.swt.git/tree/examples/org.eclipse.swt.snippets/src/org/eclipse/swt/snippets/Snippet274.java
	public static void checkItems(TreeItem item, boolean checked) {
	    item.setGrayed(false);
	    item.setChecked(checked);
	    final TreeItem[] items = item.getItems();
	    for (int i = 0; i < items.length; i++) {
	        checkItems(items[i], checked);
	    }
	}
	
	
	
	private UIHelper() {}
}

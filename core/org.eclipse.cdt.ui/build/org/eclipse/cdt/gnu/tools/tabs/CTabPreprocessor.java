/**********************************************************************
 * Copyright (c) 2002,2003 Timesys Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * Timesys - Initial API and implementation
 **********************************************************************/

package org.eclipse.cdt.gnu.tools.tabs;

import java.util.Iterator;

import org.eclipse.cdt.core.builder.model.ICBuildConfig;
import org.eclipse.cdt.core.builder.model.ICBuildConfigWorkingCopy;
import org.eclipse.cdt.core.builder.model.ICPosixBuildConstants;
import org.eclipse.cdt.ui.builder.ACToolTab;
import org.eclipse.cdt.ui.builder.internal.CBuildVariableDialog;
import org.eclipse.cdt.ui.builder.internal.CNameValueDialog;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;


/**
 * The control for editing and viewing compiler options.
 */
public class CTabPreprocessor extends ACToolTab {
	private Table 	fDefineTable;
	private Table 	fIncludeTable;
	private Button	btnIncludeAdd;
	private Button	btnIncludeRemove;
	private Button	btnIncludeMoveUp;
	private Button	btnIncludeMoveDown;
	private Button	btnDefineAdd;
	private Button	btnDefineRemove;
	private Button	btnDefineEdit;
	private List fIncludePaths;
	private List fDefineMacros;
			
	/**
	 * Constructs the object
	 * 
	 * @param parent	owning window
	 * @param style 	modal or not
	 */
	public CTabPreprocessor() {
		super();
	}

	/**
	 * Helper function for creating a grid layout to spec.
	 * 
	 * @param columns		Number of columns in layout.
	 * @param equalWidth	True if columns are of equal width.
	 * @param marginHeight	Margin height for layout.
	 * @param marginWidth	Margin width for layout.
	 * 
	 * @return Newly created GridLayout with the specified properties.
	 */
	private GridLayout createGridLayout(int columns, boolean equalWidth, int marginHeight, int marginWidth) {
		GridLayout layout = new GridLayout(columns, equalWidth);
		layout.marginHeight = marginHeight;
		layout.marginWidth 	= marginWidth;
		return layout;
	}

	/**
	 * "Add Include" button handler.
	 */
	private void handleIncludeAdd(SelectionEvent e) {

		TableItem[] existingItems = fIncludeTable.getItems();
		fIncludePaths.removeAll();
		for (int i = 0; i < fIncludeTable.getItemCount(); i++) {
			fIncludePaths.add(existingItems[i].getText());
		}
				
		CBuildVariableDialog	dlg = new CBuildVariableDialog(getShell(), ("Select_Include_Path_12")); //$NON-NLS-1$

		if (dlg.open() == dlg.OK) {			
			TableItem item = new TableItem(fIncludeTable, SWT.NONE);					
			item.setText(dlg.getSelection().toString());
			fIncludeTable.select(fIncludeTable.indexOf(item));
			fIncludeTable.setFocus();
		}
		
		handleIncludeTableSelectionChanged();		
	}

	/**
	 * "Remove Include" button handler.
	 */
	private void handleIncludeRemove(SelectionEvent e) {
		handleTableRemove(fIncludeTable);
		handleIncludeTableSelectionChanged();
	}



	/**
	 * "Move Up Include" button handler.
	 */
	private void handleIncludeMoveUp(SelectionEvent e) {
		int 	itemIndex	= fIncludeTable.getSelectionIndex();
		String 	itemText	= fIncludeTable.getItem(itemIndex).getText();

		fIncludeTable.remove(itemIndex);

		new TableItem(fIncludeTable, SWT.NONE, itemIndex - 1).setText(itemText);

		fIncludeTable.setFocus();
		fIncludeTable.select(itemIndex - 1);
		
		handleIncludeTableSelectionChanged();
	}

	/**
	 * "Move Down Include" button handler.
	 */
	private void handleIncludeMoveDown(SelectionEvent e) {
		int 	itemIndex	= fIncludeTable.getSelectionIndex();
		String 	itemText	= fIncludeTable.getItem(itemIndex).getText();

		fIncludeTable.remove(itemIndex);

		new TableItem(fIncludeTable, SWT.NONE, itemIndex + 1).setText(itemText);

		fIncludeTable.setFocus();
		fIncludeTable.select(itemIndex + 1);
		
		handleIncludeTableSelectionChanged();
	}

	/**
	 * Include table selection change handler.
	 */
	private void handleIncludeTableSelectionChanged() {
		int itemIndex	= fIncludeTable.getSelectionIndex();
		int itemCount	= fIncludeTable.getItemCount();
		
		// Add always enabled
		btnIncludeAdd.setEnabled(true);

		// Remove enabled if > 1 item selected
		btnIncludeRemove.setEnabled(-1 != itemIndex);

		// Move up/down enabled if:
		// > 1 item in list
		// 1 item selected
		// Item is not first item (up) or last item (down)
		
		btnIncludeMoveUp.setEnabled((-1 != itemIndex) && (itemCount > 1) && (0 != itemIndex));
		btnIncludeMoveDown.setEnabled((-1 != itemIndex) && (itemCount > 1) && ((itemCount - 1) != itemIndex));
	}

	/**
	 * "Add Define" button handler.
	 */
	private void handleDefineAdd(SelectionEvent e) {
		
		TableItem[] existingItems = fDefineTable.getItems();
		fDefineMacros.removeAll();
		for (int i =0; i < fDefineTable.getItemCount(); i++) {
			fDefineMacros.add(existingItems[i].getText());
		}

		CNameValueDialog dlg = new CNameValueDialog(getShell(), fDefineMacros);

		dlg.setTitle(("New_Preprocessor_Definition_13")); //$NON-NLS-1$

		if (dlg.open() == dlg.OK) {
			TableItem item = new TableItem(fDefineTable, SWT.NONE);
			item.setText(0, dlg.getName());
			item.setText(1, dlg.getValue());
			fDefineTable.select(fDefineTable.indexOf(item));
			fDefineTable.setFocus();
		}

		handleDefineTableSelectionChanged();
	}
	
	/**
	 * "Remove Define" button handler.
	 */
	private void handleDefineRemove(SelectionEvent e) {
		handleTableRemove(fDefineTable);
		handleDefineTableSelectionChanged();
	}

	/**
	 * "Edit Define" button handler.
	 */
	private void handleDefineEdit(SelectionEvent e) {
		int itemIndex = fDefineTable.getSelectionIndex();
		
		if (-1 != itemIndex) {
			TableItem item = fDefineTable.getItem(itemIndex);

			CNameValueDialog dlg = new CNameValueDialog(getShell());

			dlg.setTitle(("Edit_Preprocessor_Definition_14")); //$NON-NLS-1$
			dlg.setName(item.getText(0));
			dlg.setValue(item.getText(1));

			if (dlg.open() == dlg.OK) {
				item.setText(0, dlg.getName());
				item.setText(1, dlg.getValue());
			}

			fDefineTable.select(itemIndex);
			fDefineTable.setFocus();
		}

		handleDefineTableSelectionChanged();
	}

	/**
	 * Define table selection change handler.
	 */
	private void handleDefineTableSelectionChanged() {
		TableItem[] items = fDefineTable.getSelection();
		
		// Add always enabled
		btnDefineAdd.setEnabled(true);

		// Remove enabled if > 1 item selected
		btnDefineRemove.setEnabled((null != items) && (1 >= items.length));

		// Edit enabled if exactly 1 item selected
		btnDefineEdit.setEnabled((null != items) && (1 == items.length));
	}

	/**
	 * Generic "remove selected entry from table" method.
	 * 
	 * @param Table to remove entry from.
	 */
	private void handleTableRemove(Table table) {
		int itemIndex = table.getSelectionIndex();
		if (-1 != itemIndex) {
			TableItem item = table.getItem(itemIndex);
			item.dispose();
			int itemCount = table.getItemCount();
			table.select(0 == itemIndex ? 0 : (itemIndex >= itemCount ? itemCount - 1 : itemIndex));
			table.setFocus();
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.builder.ICToolTab#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		Composite 	cmpGroup	= null;
		Composite	cmpCtrls	= null;
		Composite	cmpButtons	= null;
		Group		group		= null;
		Table		table		= null;
		Button		button		= null;
		TableColumn column		= null;
		GridLayout	gridLayout 	= null;
		GridData	gridData	= null;
		GC 			gc 			= null;

		Composite	ths			= new Composite(parent, SWT.NONE);

		// Panel
		
		ths.setLayout(new GridLayout());
		ths.setLayoutData(new GridData(GridData.FILL_BOTH));

		// Initialize our list containing existing paths,
		fIncludePaths = new List(parent, SWT.NONE);
		fDefineMacros = new List(parent, SWT.NONE);

		// Includes

		group = new Group(ths, SWT.SHADOW_NONE);
		group.setLayout(createGridLayout(1, true, 2, 2));
		group.setLayoutData(new GridData(GridData.FILL_BOTH));
		group.setText(("Include_Paths_1")); //$NON-NLS-1$

		cmpGroup = new Composite(group, SWT.NONE);
		cmpGroup.setLayout(createGridLayout(1, true, 2, 2));
		cmpGroup.setLayoutData(new GridData(GridData.FILL_BOTH));

		cmpCtrls = new Composite(cmpGroup, SWT.NONE);
		cmpCtrls.setLayout(createGridLayout(2, false, 2, 2));
		cmpCtrls.setLayoutData(new GridData(GridData.FILL_BOTH));

		table = new Table(cmpCtrls, SWT.BORDER | SWT.SINGLE | SWT.HIDE_SELECTION);
		table.setLayout(new GridLayout());
		table.setLayoutData(new GridData(GridData.FILL_BOTH));

		table.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleIncludeTableSelectionChanged();
			}
		});

		fIncludeTable 	= table;
		cmpButtons 		= new Composite(cmpCtrls, SWT.NONE);
		gridLayout 		= createGridLayout(1, true, 2, 2);
		gridData		= new GridData();
		gc 				= new GC(cmpButtons);
		
		gc.setFont(cmpButtons.getFont());
		gridData.widthHint = gc.getFontMetrics().getAverageCharWidth() * 20;
		gc.dispose();
		cmpButtons.setLayout(gridLayout);
		cmpButtons.setLayoutData(gridData);

		button = new Button(cmpButtons, SWT.PUSH);
		button.setLayoutData(new GridData(GridData.FILL_HORIZONTAL)); 
		button.setEnabled(true);
		button.setText(("Add_2")); //$NON-NLS-1$
		button.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					handleIncludeAdd(e);
				}
			});
		btnIncludeAdd = button;
		
		button = new Button(cmpButtons, SWT.PUSH);
		button.setLayoutData(new GridData(GridData.FILL_HORIZONTAL)); 
		button.setEnabled(true);
		button.setText(("Remove_3")); //$NON-NLS-1$
		button.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					handleIncludeRemove(e);
				}
			});
		btnIncludeRemove = button;

		button = new Button(cmpButtons, SWT.PUSH);
		button.setLayoutData(new GridData(GridData.FILL_HORIZONTAL)); 
		button.setEnabled(true);
		button.setText(("Move_Up_4")); //$NON-NLS-1$
		button.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					handleIncludeMoveUp(e);
				}
			});
		btnIncludeMoveUp = button;

		button = new Button(cmpButtons, SWT.PUSH);
		button.setLayoutData(new GridData(GridData.FILL_HORIZONTAL)); 
		button.setEnabled(true);
		button.setText(("Move_Down_5")); //$NON-NLS-1$
		button.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					handleIncludeMoveDown(e);
				}
			});
		btnIncludeMoveDown = button;

		// Defines

		group = new Group(ths, SWT.SHADOW_NONE);
		group.setLayout(createGridLayout(1, true, 2, 2));
		group.setLayoutData(new GridData(GridData.FILL_BOTH));
		group.setText(("Preprocessor_Definitions_6")); //$NON-NLS-1$

		cmpGroup = new Composite(group, SWT.NONE);
		cmpGroup.setLayout(createGridLayout(1, true, 2, 2));
		cmpGroup.setLayoutData(new GridData(GridData.FILL_BOTH));

		cmpCtrls = new Composite(cmpGroup, SWT.NONE);
		cmpCtrls.setLayout(createGridLayout(2, false, 2, 2));
		cmpCtrls.setLayoutData(new GridData(GridData.FILL_BOTH));

		table = new Table(cmpCtrls, SWT.BORDER | SWT.SINGLE | SWT.HIDE_SELECTION);
		TableLayout tblLayout = new TableLayout();
		table.setLayout(tblLayout);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.setLayoutData(new GridData(GridData.FILL_BOTH));

		column = new TableColumn(table, SWT.NULL);
		column.setText(("Name_7")); //$NON-NLS-1$
		tblLayout.addColumnData(new ColumnWeightData(30));

		column = new TableColumn(table, SWT.NULL);
		column.setText(("Value_8")); //$NON-NLS-1$
		tblLayout.addColumnData(new ColumnWeightData(30));

		table.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleDefineTableSelectionChanged();
			}
		});

		fDefineTable 	= table;
		cmpButtons 		= new Composite(cmpCtrls, SWT.NONE);
		gridLayout 		= createGridLayout(1, true, 2, 2);
		gridData		= new GridData();
		gc 				= new GC(cmpButtons);
		
		gc.setFont(cmpButtons.getFont());
		gridData.widthHint = gc.getFontMetrics().getAverageCharWidth() * 20;
		gc.dispose();
		cmpButtons.setLayout(gridLayout);
		cmpButtons.setLayoutData(gridData);

		button = new Button(cmpButtons, SWT.PUSH);
		button.setLayoutData(new GridData(GridData.FILL_HORIZONTAL)); 
		button.setEnabled(true);
		button.setText(("Add_9")); //$NON-NLS-1$
		button.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					handleDefineAdd(e);
				}
			});
		btnDefineAdd = button;

		button = new Button(cmpButtons, SWT.PUSH);
		button.setLayoutData(new GridData(GridData.FILL_HORIZONTAL)); 
		button.setEnabled(true);
		button.setText(("Remove_10")); //$NON-NLS-1$
		button.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					handleDefineRemove(e);
				}
			});
		btnDefineRemove = button;

		button = new Button(cmpButtons, SWT.PUSH);
		button.setLayoutData(new GridData(GridData.FILL_HORIZONTAL)); 
		button.setEnabled(true);
		button.setText(("Edit_11")); //$NON-NLS-1$
		button.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					handleDefineEdit(e);
				}
			});
		btnDefineEdit = button;
		
		// Since no paths or macros have been added yet, disabled the following buttons.
		btnDefineEdit.setEnabled(false);
		btnDefineRemove.setEnabled(false);
		btnIncludeMoveDown.setEnabled(false);
		btnIncludeMoveUp.setEnabled(false);
		btnIncludeRemove.setEnabled(false);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.builder.ICToolTab#initializeFrom(ICBuildConfig)
	 */
	public void initializeFrom(ICBuildConfig config) {
		
		java.util.List includes = null;
		java.util.List defines = null;
		
		try {
			includes = config.getAttribute(ICPosixBuildConstants.CPP_INCLUDES, (java.util.List) null);
			defines = config.getAttribute(ICPosixBuildConstants.CPP_DEFINITIONS, (java.util.List) null);
		} catch (CoreException e) {
		}

		if (includes != null) {
			for (Iterator iter = includes.iterator(); iter.hasNext();) {
				TableItem item = new TableItem(fIncludeTable, SWT.NONE);
				item.setText((String) iter.next());
			}
		}
		
		if (defines != null) {
			for (Iterator iter = defines.iterator(); iter.hasNext();) {
				TableItem item = new TableItem(fDefineTable, SWT.NONE);
	
				String	define	= (String) iter.next();
				int 	index	= define.indexOf('=');
	
				if (-1 == index) {
					item.setText(0, define.trim());
				} else {
					item.setText(0, define.substring(0, index).trim());
					if (index < define.length()) {
						item.setText(1, define.substring(index + 1).trim());
					}
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.builder.ICToolTab#performApply(ICBuildConfigWorkingCopy)
	 */
	public void performApply(ICBuildConfigWorkingCopy config) {
		TableItem[]	includes = fIncludeTable.getItems();
		TableItem[]	defines  = fDefineTable.getItems();
		java.util.List includeList = new java.util.Vector();
		java.util.List defineList = new java.util.Vector();

		for (int i = 0; i < includes.length; i++) {
			includeList.add(includes[i].getText());
		}

		for (int i = 0; i < defines.length; i++) {
			String name  = defines[i].getText(0);
			String value = defines[i].getText(1);
			if (value.length() > 0) {
				defineList.add(name + "=" + value);
			} else {
				defineList.add(name);
			}
		}

		config.setAttribute(ICPosixBuildConstants.CPP_INCLUDES, includeList);
		config.setAttribute(ICPosixBuildConstants.CPP_DEFINITIONS, defineList);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.builder.ICToolTab#setDefaults(ICBuildConfigWorkingCopy)
	 */
	public void setDefaults(ICBuildConfigWorkingCopy configuration) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.builder.ICToolTab#isValid(ICBuildConfigWorkingCopy)
	 */
	public boolean isValid(ICBuildConfigWorkingCopy config) {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.builder.ICToolTab#getName()
	 */
	public String getName() {
		return "Preprocessor";
	}
}
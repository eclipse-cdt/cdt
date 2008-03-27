/********************************************************************************
 * Copyright (c) 2008 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Rupen Mardirossian (IBM) - [210693] Created Dialog for enhancement defect  
 ********************************************************************************/


package org.eclipse.rse.internal.ui.dialogs;

import java.util.List;

import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.rse.internal.ui.SystemResources;
import org.eclipse.rse.internal.ui.dialogs.SystemCopyTableProvider;
import org.eclipse.rse.ui.SystemWidgetHelpers;
import org.eclipse.rse.ui.dialogs.SystemPromptDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

/**
 * Dialog for confirming overwriting of resources when copy collision occurs. 
 * 
 * This dialog is a mirror copy of the SystemDeleteDialog with a few changes 
 * 
 */

public class SystemCopyDialog extends SystemPromptDialog
{
	private String verbiage;
	private SystemCopyTableProvider sctp;
    private Table table;
    private TableViewer tableViewer;    
    private GridData tableData;
    private List collisions;
	
	// column layout
	private ColumnLayoutData columnLayouts[] = 
	{
		new ColumnPixelData(19, false),		
		new ColumnWeightData(150,150,true)
    };
	
    // column headers
	private String columnHeaders[] = {
		"", //$NON-NLS-1$
		SystemResources.RESID_COLLISION_COPY_COLHDG_OLDNAME
	};
	/**
	 * Constructor when you have your list of files and would like to use default title.
	 */
	public SystemCopyDialog(Shell shell, List files) 
	{
		this(shell, SystemResources.RESID_COPY_TITLE, files);				
	}
	/**
	 * Constructor when you have your own title and list of files.
	 */
	public SystemCopyDialog(Shell shell, String title, List files) 
	{
		super(shell, title);				 
		setOkButtonLabel(SystemResources.BUTTON_OVERWRITE_ALL);
		setOkButtonToolTipText(SystemResources.BUTTON_OVERWRITE_ALL_TOOLTIP);
		setCancelButtonLabel(SystemResources.BUTTON_CANCEL_ALL);
		setCancelButtonToolTipText(SystemResources.BUTTON_CANCEL_ALL_TOOLTIP);
		collisions=files;
	}
	/**
	 * @see SystemPromptDialog#createInner(Composite)
	 */
	protected Control createInner(Composite parent) 
	{
		// Inner composite
		int nbrColumns = 1;
		Composite composite = SystemWidgetHelpers.createComposite(parent, nbrColumns);	
		
		if (verbiage != null)
			SystemWidgetHelpers.createVerbiage(composite, verbiage, nbrColumns, false, 200);
		else
			SystemWidgetHelpers.createVerbiage(composite, SystemResources.RESID_COLLISION_COPY_VERBIAGE, nbrColumns, false, 200);
		
		// TABLE
        tableViewer = createTableViewer(composite, nbrColumns);        
        createColumns();    
                
        sctp = new SystemCopyTableProvider();
        
		int width = tableData.widthHint;
		int nbrRows = Math.min(collisions.size(),8);
		int rowHeight = table.getItemHeight() + table.getGridLineWidth();
		int sbHeight = table.getHorizontalBar().getSize().y;	    
		int height = (nbrRows * rowHeight) + sbHeight;
	    
		tableData.heightHint = height;
		table.setLayoutData(tableData);  	  	    
		table.setSize(width, height);         
 
        tableViewer.setLabelProvider(sctp);
        tableViewer.setContentProvider(sctp);

        tableViewer.setInput(collisions);  
        
		return composite;
	}
	/**
	 * Creates and returns TableViewer
	 */
	private TableViewer createTableViewer(Composite parent, int nbrColumns) 
    {
	   table = new Table(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.HIDE_SELECTION);
  	   table.setLinesVisible(true);
  	   tableViewer = new TableViewer(table);
	   tableData = new GridData();
	   tableData.horizontalAlignment = GridData.FILL;
	   tableData.grabExcessHorizontalSpace = true;
	   tableData.widthHint = 350;    
	   tableData.heightHint = 30;    
	   tableData.verticalAlignment = GridData.CENTER;
	   tableData.grabExcessVerticalSpace = true;
	   tableData.horizontalSpan = nbrColumns;
	   table.setLayoutData(tableData);  	  	   
	   return tableViewer;
    }	
	
	protected Control getInitialFocusControl() 
	{
		return null;
	}

	
	private void createColumns() 
	{
		TableLayout layout = new TableLayout();
		table.setLayout(layout);
		table.setHeaderVisible(true);
		for (int i = 0; i < columnHeaders.length; i++) 
		{
			layout.addColumnData(columnLayouts[i]);
			TableColumn tc = new TableColumn(table, SWT.NONE,i);
			tc.setResizable(columnLayouts[i].resizable);
			tc.setText(columnHeaders[i]);
		}    	
	}
}

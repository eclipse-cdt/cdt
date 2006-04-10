/********************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * {Name} (company) - description of contribution.
 ********************************************************************************/

package org.eclipse.rse.dstore.security.preference;


import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.TableColumn;

public class CertTableSorter
extends ViewerSorter
{
	private final static String ASC_SYMBOL = "^";
	private final static String DESC_SYMBOL = ".";
	
	private TableViewer tableViewer;
	private int currentColumn;
	private boolean asc;
	private boolean addDirectionSymbol;

	private SelectionListener headerListener;

	public CertTableSorter(TableViewer tableViewer, int defaultColumn, boolean asc, boolean addDirectionSymbol, boolean addHeaderListener)
	{
		this.tableViewer = tableViewer;
		this.addDirectionSymbol = addDirectionSymbol;
		
		setSort(defaultColumn, asc);
		
		tableViewer.setSorter(this);
		if(addHeaderListener)
			addColumnHeaderListeners();
	}
	
	public static void setTableSorter(TableViewer tableViewer, int defaultColumn, boolean asc)
	{
		new CertTableSorter(tableViewer, defaultColumn, asc, false, true);
	}
	
	private void initializeHeaderListener()
	{
		headerListener = new SelectionListener()
		{
			public void widgetDefaultSelected(SelectionEvent e)
			{
			}

			public void widgetSelected(SelectionEvent e)
			{
				String text = null;

				if(currentColumn >= 0)
				{
					TableColumn currentTableColumn = (TableColumn)tableViewer.getTable().getColumn(currentColumn);
					if(addDirectionSymbol && (currentTableColumn != null))
					{
						text = currentTableColumn.getText();
						if((text != null) && (text.startsWith(ASC_SYMBOL) || text.startsWith(DESC_SYMBOL)))
						{
							text = text.substring(1);
							currentTableColumn.setText(text);
						}
					}
				}
				
				TableColumn tableColumn = (TableColumn)e.widget;
				text = tableColumn.getText();
				
				int index = tableViewer.getTable().indexOf(tableColumn);
				if(index == currentColumn)
				{
					asc = !asc;
				}
				else
				{
					asc = true;
					currentColumn = index;
				}

				if(addDirectionSymbol && (text != null))
				{
					if(asc)
						text = ASC_SYMBOL + text;
					else
						text = DESC_SYMBOL + text;

					tableColumn.setText(text);
				}
				
				tableViewer.getTable().setRedraw(false);
				tableViewer.refresh();
				tableViewer.getTable().setRedraw(true);
			}
		};
	}
	
	public void addColumnHeaderListeners()
	{
		for(int i=0, length=tableViewer.getTable().getColumnCount(); i<length; i++)
		{
			TableColumn column = tableViewer.getTable().getColumn(i);

			column.removeSelectionListener(getHeaderListener());
			column.addSelectionListener(getHeaderListener());
		}
	}
	
	public void setSort(int currentColumn)
	{
		this.currentColumn = currentColumn;
	}
	
	public void setSort(int currentColumn, boolean asc)
	{
		setSort(currentColumn);
		setSort(asc);
	}
	public void setSort(boolean asc)
	{
		this.asc = asc;
	}
	
	public int getCurrentColumn()
	{
		return currentColumn;
	}
	
	public SelectionListener getHeaderListener()
	{
		if(headerListener == null)
			initializeHeaderListener();

		return headerListener;
	}
	
	public boolean isAsc()
	{
		return asc;
	}
	
	public int compare(Viewer viewer, Object e1, Object e2)
	{
		if(viewer != tableViewer)
			return super.compare(viewer, e1, e2);
			
		int ret = compareAsc(e1, e2);
		return (asc?ret:-1*ret);
	}
	
	protected int compareAsc(Object e1, Object e2)
	{
		int defaultRet = super.compare(tableViewer, e1, e2);
		
		Object o = tableViewer.getLabelProvider();
		if((o == null) || (!(o instanceof ITableLabelProvider)))
			return defaultRet;
			
		ITableLabelProvider labelProvider = (ITableLabelProvider)o;
		String value1 = labelProvider.getColumnText(e1, currentColumn);
		String value2 = labelProvider.getColumnText(e2, currentColumn);

		if(value1 == null)
			return -1;
			
		if(value2 == null)
			return 1;

		return compareAsc(value1, value2);
	}
	
	protected int compareAsc(String value1, String value2)
	{
		return value1.compareToIgnoreCase(value2);
	}
}
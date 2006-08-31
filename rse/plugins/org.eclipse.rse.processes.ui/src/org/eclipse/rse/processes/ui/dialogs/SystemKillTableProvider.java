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

package org.eclipse.rse.processes.ui.dialogs;

import java.util.Iterator;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.rse.ui.dialogs.SystemDeleteTableProvider;


public class SystemKillTableProvider extends SystemDeleteTableProvider
{
	
    static final int COLUMN_IMAGE = 0;
    static final int COLUMN_NAME = 1;   
    static final int COLUMN_TYPE = 2; 
    
	/**
	 * Return rows. Input must be an IStructuredSelection.
	 */
	public Object[] getElements(Object inputElement)
	{
        if (children == null)
        {
		  	IStructuredSelection iss = (IStructuredSelection)inputElement;
		  	children = new SystemKillTableRow[iss.size()];
		  	Iterator i = iss.iterator();
		  	int idx = 0;
		  	while (i.hasNext())
		  	{
		    	children[idx] = new SystemKillTableRow(i.next(), idx);		
		    	idx++;
		  	}
        }
		return children;
	}
	
	/**
	 * Return the 0-based row number of the given element.
	 */
	public int getRowNumber(SystemKillTableRow row)
	{
		return row.getRowNumber();
	}
	
	/**
	 * @see ITableLabelProvider#getColumnText(java.lang.Object, int)
	 */
	public String getColumnText(Object element, int column) 
	{
		String text = "";
		if (column == COLUMN_NAME)
		  	text = getTableRow(element).getName();
		else if (column == COLUMN_TYPE)
		  	text = getTableRow(element).getType();		  
		//System.out.println("INSIDE GETCOLUMNTEXT: " + column + ", " + text + ", " + getTableRow(element));
		return text;  
	}
	
	private SystemKillTableRow getTableRow(Object element)
	{
		return (SystemKillTableRow)element;
	}
}
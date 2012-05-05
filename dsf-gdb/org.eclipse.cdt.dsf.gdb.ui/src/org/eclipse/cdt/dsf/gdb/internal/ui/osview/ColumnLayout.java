/*******************************************************************************
 * Copyright (c) 2011 Mentor Graphics and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Vladimir Prus (Mentor Graphics) - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.internal.ui.osview;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.dsf.gdb.internal.ui.GdbUIPlugin;
import org.eclipse.jface.preference.IPreferenceStore;

/* Hold information about which columns in a table are visible, and what
 * width they have. Stores that information inside preferences store.
 */
class ColumnLayout
{
	private IPreferenceStore fPreferences;
	private String fResourceClass;
	private Map<String, Boolean> fVisible = new HashMap<String, Boolean>();
	private Map<String, Integer> fWidth = new HashMap<String, Integer>();
	private Integer fSortColumn = null;
	private Integer fSortDirection = null;

	public ColumnLayout(String resourceClass)
	{
		fPreferences = GdbUIPlugin.getDefault().getPreferenceStore();
		fResourceClass = resourceClass;			
	}

	public boolean getVisible(String column)
	{
		if (fVisible.containsKey(column))
			return fVisible.get(column);
		else
		{
			fPreferences.setDefault(columnKey(column, "v"), true); //$NON-NLS-1$
			boolean b = fPreferences.getBoolean(columnKey(column, "v")); //$NON-NLS-1$
			fVisible.put(column, b);
			return b;
		}
	}

	public void setVisible(String column, boolean visible)
	{
		fVisible.put(column, visible);
		fPreferences.setValue(columnKey(column, "v"), visible); //$NON-NLS-1$
	}

	public int getWidth(String column)
	{
		if (fWidth.containsKey(column))
			return fWidth.get(column);
		else
		{
			fPreferences.setDefault(columnKey(column, "w"), -1); //$NON-NLS-1$
			int w = fPreferences.getInt(columnKey(column, "w")); //$NON-NLS-1$
			fWidth.put(column, w);
			return w;
		}
	}

	public void setWidth(String column, int width)
	{
		fWidth.put(column, width);
		fPreferences.setValue(columnKey(column, "w"), width); //$NON-NLS-1$
	}
	
	public int getSortColumn()
	{
		if (fSortColumn == null)
		{
			fPreferences.setDefault(globalKey("sortColumn"), 0); //$NON-NLS-1$
			fSortColumn = fPreferences.getInt(globalKey("sortColumn"));	//$NON-NLS-1$		
		}
		return fSortColumn;
	}
	
	public void setSortColumn(int column)
	{
		fSortColumn = column;
		fPreferences.setValue(globalKey("sortColumn"), fSortColumn); //$NON-NLS-1$
	}
	
	public int getSortDirection()
	{
		if (fSortDirection == null)
		{
			fPreferences.setDefault(globalKey("sortDirection"), 1); //$NON-NLS-1$
			fSortDirection = fPreferences.getInt(globalKey("sortDirection")); //$NON-NLS-1$			
		}
		return fSortDirection;
	}
	
	public void setSortDirection(int direction)
	{
		fSortDirection = direction;
		fPreferences.setValue(globalKey("sortDirection"), fSortDirection); //$NON-NLS-1$
	}

	private String columnKey(String column, String what)
	{
		return "columnLayout." + fResourceClass + "." + column + "." + what; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
	
	private String globalKey(String what)
	{
		return "columnLayout." + fResourceClass + "." + what; //$NON-NLS-1$ //$NON-NLS-2$
	}
}

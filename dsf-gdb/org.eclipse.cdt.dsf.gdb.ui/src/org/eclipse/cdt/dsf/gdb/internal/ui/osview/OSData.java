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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.dsf.mi.service.command.output.MIInfoOsInfo;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;

/* Table data provider that exposes information about OS resources
 * of specific class. Constructed from MI output. Once constructed,
 * this class is immutable.
 */
class OSData 
extends LabelProvider 
implements ITableLabelProvider, IStructuredContentProvider 
{
	public OSData(String resourceClass, MIInfoOsInfo data) {
		this.data = data;		
		remap = new ArrayList<Integer>(data.getColumnNames().length);
		for (int i = 0; i < data.getColumnNames().length; ++i)
			remap.add(i);
		
		if (resourceClass.equals("processes"))  //$NON-NLS-1$
			sendToEnd("Command");  //$NON-NLS-1$
	}
	
	/* Make column named 'column' appear last in UI. */
	private void sendToEnd(String column)
	{
		// Find index in the remap array (which is equal to index in UI)
		// at which column named 'column' is found.
		int index = -1;
		for (int i = 0; i < remap.size(); ++i)
			if (data.getColumnNames()[remap.get(i)].equals(column)) {
				index = i;
				break;
			}
		if (index == -1)
			return;
		
		int saved = remap.get(index);
		for (int i = index; i < remap.size()-1; ++i)
			remap.set(i, remap.get(i+1));
		remap.set(remap.size()-1, saved);				
	}

	public int getColumnCount()
	{
		return remap.size();
	}
	
	public String getColumnName(int i) 
	{
		return data.getColumnNames()[remap.get(i)];
	}
	
	public boolean columnIsInteger(int j) {
		return data.isColumnInteger(j);
	}

	@Override
	public String getColumnText(Object obj, int index) {
		// This works around API deficiency. When we switch to
		// a different resource class, we might want to show
		// more columns in the table.
		// If we first set new content provider and then add
		// columns, the columns end up empty for some reason.
		// If we first add columns, the table will try to
		// get content of those additional columns from the current
		// content provider (which might have less columns)
		// And if we try to set content provider to null, we get
		// assertions. So, then only solution is to return null
		// when column index is out of range.
		if (index < data.getColumnNames().length)
			return ((String[]) obj)[remap.get(index)];
		else
			return null;
	}

	@Override
	public Image getColumnImage(Object obj, int index) {
		return getImage(obj);
	}

	@Override
	public Image getImage(Object obj) {
		return null;
	}

	@Override
	public void inputChanged(Viewer v, Object oldInput, Object newInput) {
	}

	@Override
	public void dispose() {
	}

	@Override
	public Object[] getElements(Object parent) {
		return data.getContent();
	}
	
	private MIInfoOsInfo data;
	private List<Integer> remap;
}

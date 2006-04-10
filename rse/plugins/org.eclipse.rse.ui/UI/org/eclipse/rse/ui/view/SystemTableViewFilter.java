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

package org.eclipse.rse.ui.view;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.rse.services.clientserver.StringCompare;


/**
 * This class is used for filtering in the SystemTableView.  The filter
 * determines what objects to show in the view.
 * 
 */
public class SystemTableViewFilter extends ViewerFilter
{

	private String[] _filters;

	public SystemTableViewFilter()
	{
		super();

	}

	public void setFilters(String[] filters)
	{
		_filters = filters;
	}
	
	public String[] getFilters()
	{
		return _filters;	
	}

	public boolean select(Viewer viewer, Object parent, Object element)
	{
		boolean result = true;
		if (viewer instanceof TableViewer)
		{
			if (_filters != null)
			{
				TableViewer tviewer = (TableViewer) viewer;
				ITableLabelProvider labelProvider = (ITableLabelProvider) tviewer.getLabelProvider();

				for (int i = 0; i < _filters.length && result; i++)
				{
					String filter = _filters[i];

					if (filter != null && filter.length() > 0)
					{
						String text = labelProvider.getColumnText(element, i);
						if (!StringCompare.compare(filter, text, true))
						{
							result = false;
						}
					}
				}
			}
		}

		return result;
	}

}
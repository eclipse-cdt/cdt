/*******************************************************************************
 * Copyright (c) 2002, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * David McKnight   (IBM)        - [331986] Sort in Remote System Details view shows wrong results
 * David McKnight   (IBM)        - [355467] The result of sorting resources that contains null blank cells is not correct in Remote System Details view.
 * David McKnight   (IBM)        - [357587] Custom sorter is changed to SystemTableViewSorter
 * David McKnight   (IBM)        - [398306] table sorting of RSE table views inconsistent with Eclipse
 * David McKnight   (IBM)        - [430900] RSE table enhancement to populate full column when clicking column for sorting purposes
 *******************************************************************************/

package org.eclipse.rse.internal.ui.view;

import java.util.Comparator;
import java.util.Date;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.util.Policy;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.rse.ui.view.ISystemViewElementAdapter;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.views.properties.IPropertyDescriptor;

/**
 * This class is used for sorting in the SystemTableView.  The sorter
 * determines what and how to sort based on property descriptors.
 * 
 */
public class SystemTableViewSorter extends ViewerSorter
{


	private boolean _reverseSort;

	private int _columnNumber;

	private StructuredViewer _view;
	private SystemTableViewColumnManager _columnManager;
	private Comparator _comparator;

	public SystemTableViewSorter(int columnNumber, StructuredViewer view, SystemTableViewColumnManager columnManager)
	{
		super();
		_reverseSort = true; // reverse sort means ascending order
		_columnNumber = columnNumber; 
		_view = view;
		_columnManager = columnManager;
		_comparator = Policy.getComparator();
	}
	
	public void setColumnNumber(int columnNumber){
		_columnNumber = columnNumber;
	}

	public boolean isSorterProperty(java.lang.Object element, java.lang.Object property)
	{
		return true;
	}

	public int category(Object element)
	{
		return 0;
	}

	public int getColumnNumber()
	{
		return _columnNumber;
	}

	public boolean isReversed()
	{
		return _reverseSort;
	}

	public void setReversed(boolean newReversed)
	{
		_reverseSort = newReversed;
	}

	public int compare(Viewer v, Object e1, Object e2)
	{
		Object name1 = getValueFor(e1, _columnNumber);
		Object name2 = getValueFor(e2, _columnNumber);

		// deal with equal objects
		if (name1 == name2){
			if (_columnNumber > 0){		
				name1 = getValueFor(e1, 0);
				name2 = getValueFor(e2, 0);
			}
			else {
				return 0;
			}
		}
		
		try
		{
			Object n1 = name1;
			Object n2 = name2;

			if (isReversed())
			{
				n1 = name2;
				n2 = name1;
			}

			// deal with nulls
			if (n1 == null){
				return -1;
			}
			else  if (n2 == null){
				return 1;
			}
			
			if (n1 instanceof Date)
			{
				return ((Date) n1).compareTo((Date) n2);
			}
			else if (n1 instanceof Long)
			{
				return ((Long) n1).compareTo((Long) n2);
			}
			else if (n1 instanceof Integer)
			{
				return ((Integer) n1).compareTo((Integer) n2);				
			}
			else
			{
				return _comparator.compare(n1, n2);
			}
		}
		catch (Exception e)
		{
			return 0;
		}

	}

	private Object getValueFor(Object obj, int index)
	{
		ISystemViewElementAdapter adapter = getAdapterFor(obj);
		if (index == 0)
		{
			return adapter.getText(obj);
		}

		Widget widget = _view.testFindItem(obj);
		if (widget != null)
		{

		}

		index = index - 1;
		IPropertyDescriptor[] descriptors = null;
		if (_columnManager != null)
		{
		    descriptors = _columnManager.getVisibleDescriptors(adapter);
		}
		else
		{
		    descriptors = adapter.getUniquePropertyDescriptors();
		}
		if (descriptors.length > index)
		{
			IPropertyDescriptor descriptor = descriptors[index];

			try
			{
				Object key = descriptor.getId();

				Object propertyValue = adapter.getPropertyValue(key, false);
				return propertyValue;

			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

		return ""; //$NON-NLS-1$
	}

	private ISystemViewElementAdapter getAdapterFor(Object object)
	{
		IAdaptable adapt = (IAdaptable) object;
		if (adapt != null)
		{
			ISystemViewElementAdapter result = (ISystemViewElementAdapter) adapt.getAdapter(ISystemViewElementAdapter.class);
			result.setPropertySourceInput(object);

			return result;
		}

		return null;
	}

}

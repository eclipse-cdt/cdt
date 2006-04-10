/********************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation. All rights reserved.
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.ListenerList;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.rse.core.SystemAdapterHelpers;
import org.eclipse.rse.model.ISystemContainer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.views.properties.IPropertyDescriptor;


/**
 * This is the content and label provider for the SystemTableTreeView.
 * This class is used both to populate the SystemTableTreeView but also
 * to resolve the icon and labels for the cells in the table/tree.
 * 
 */
public class SystemTableTreeViewProvider implements ILabelProvider, ITableLabelProvider, ITreeContentProvider
{


	private ListenerList listeners = new ListenerList(1);

	protected Object[] _lastResults = null;
	protected Object _lastObject = null;
	protected SimpleDateFormat _dateFormat = new SimpleDateFormat();
	protected Viewer _viewer = null;
	protected int _maxCharsInColumnZero = 0;

	/**
	 * The cache of images that have been dispensed by this provider.
	 * Maps ImageDescriptor->Image.
	 */
	private Map imageTable = new Hashtable(40);
	private SystemTableViewColumnManager _columnManager;
	private HashMap cache;
	/**
	 * Constructor for table view provider where a column manager is present.
	 * In this case, the columns are customizable by the user.
	 * @param columnManager
	 */
	public SystemTableTreeViewProvider(SystemTableViewColumnManager columnManager)
	{
		super();
		_columnManager= columnManager;
		cache = new HashMap();
	}

	public void inputChanged(Viewer visualPart, Object oldInput, Object newInput)
	{
	_viewer = visualPart;
	}

	public void setCache(Object[] newCache)
	{
		_lastResults = newCache;
	}

	public Object[] getCache()
	{
		return _lastResults;
	}

	public boolean flushCache()
	{
		if (_lastResults == null)
		{
			return false;
		}
		if (_lastObject instanceof ISystemContainer)
		{
			((ISystemContainer)_lastObject).markStale(true);
		}
		
		_lastResults = null;
		return true;
	}

	public boolean isDeleted(Object element)
	{
		return false;
	}

	public Object[] getChildren(Object object)
	{
		return getElements(object);
	}

	public Object getParent(Object object)
	{
		return getAdapterFor(object).getParent(object);
	}

	public boolean hasChildren(Object object)
	{
		return getAdapterFor(object).hasChildren(object);
		
	}

	public Object getElementAt(Object object, int i)
	{

		return null;
	}


	
	protected ISystemViewElementAdapter getAdapterFor(Object object)
	{
	    ISystemViewElementAdapter result = null;
	    if (_viewer != null) 
	    {
	        result = SystemAdapterHelpers.getAdapter(object, _viewer);
	    }
	    else 
	    {
	        result = SystemAdapterHelpers.getAdapter(object);
	    }
        result.setPropertySourceInput(object);
        return result;
	}

	public Object[] getElements(Object object)
	{
		Object[] results = null;
		if (object == _lastObject && _lastResults != null)
		{
			return _lastResults;
		}
		else
			if (object instanceof IAdaptable)
			{
				ISystemViewElementAdapter adapter = getAdapterFor(object);
				adapter.setViewer(_viewer);
				if (adapter != null && adapter.hasChildren(object))
				{
					results = adapter.getChildren(object);
					if (adapter instanceof SystemViewRootInputAdapter)
					{
						ArrayList filterredResults = new ArrayList();
						for (int i = 0; i < results.length; i++)
						{
							Object result = results[i];
							ISystemViewElementAdapter cadapter = getAdapterFor(result);
							if (!(cadapter instanceof SystemViewPromptableAdapter))
							{
								filterredResults.add(result);	
							}							
						}
						results = filterredResults.toArray();
					}
					
					_lastResults = results;
					_lastObject = object;
				}
			}
		if (results == null)
		{
			return new Object[0];
		}

		return results;
	}

	
	public String getText(Object object)
	{		
		String result = getAdapterFor(object).getText(object);
		int len = result.length();
		if (len > _maxCharsInColumnZero)
		{
			_maxCharsInColumnZero = len;
		}
		return result;
	}
	
	public int getMaxCharsInColumnZero()
	{
		return _maxCharsInColumnZero;
	}

	public Image getImage(Object object)
	{

		ImageDescriptor descriptor = getAdapterFor(object).getImageDescriptor(object);

		Image image = null;
		if (descriptor != null)
		{
			Object iobj = imageTable.get(descriptor);
			if (iobj == null)
			{
				image = descriptor.createImage();
				imageTable.put(descriptor, image);
			}
			else
			{
				image = (Image) iobj;
			}
		}

		return image;
	}

	
	public String getColumnText(Object obj, int index)
	{
		if (index == 0)
		{
			// get the first descriptor
			return getText(obj);
		}
		else
		{

			index = index - 1;
			ISystemViewElementAdapter adapter = getAdapterFor(obj);

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

					Object propertyValue = adapter.getPropertyValue(key);

					if (propertyValue instanceof String)
					{
						return (String) propertyValue;
					}
					else if (propertyValue instanceof Date)
					{
						return _dateFormat.format((Date)propertyValue);	
					}
					else
						if (propertyValue != null)
						{
							return propertyValue.toString();
						}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}

			return "";
		}

	}

	public Image getColumnImage(Object obj, int i)
	{
		if (i == 0)
		{
			return getImage(obj);
		}
		else
		{
			return null;
		}
	}

	public void addListener(ILabelProviderListener listener)
	{
		listeners.add(listener);
	}

	public boolean isLabelProperty(Object element, String property)
	{
		return true;
	}

	public void removeListener(ILabelProviderListener listener)
	{
		listeners.remove(listener);
	}
	
	/**
	 * Cache the objects for the given parent.
	 * @param parent the parent object.
	 * @param children the children to cache.
	 */
	public void setCachedObjects(Object parent, Object[] children) {
		cache.put(parent, children);
	}
	
	/**
	 * Returns the cached objects for the given parent.
	 * @param parent the parent object.
	 * @return the cached children.
	 */
	public Object[] getCachedObjects(Object parent) {
		return (Object[])(cache.get(parent));
	}
	
	 /**
     * The visual part that is using this content provider is about
     * to be disposed. Deallocate all allocated SWT resources.
     */
    public void dispose() {
    }
}
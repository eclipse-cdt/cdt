/*******************************************************************************
 * Copyright (c) 2002, 2009 IBM Corporation and others.
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
 * Martin Oberhuber (Wind River) - [186128] Move IProgressMonitor last in all API
 * David McKnight   (IBM)        - [216161] table view needs to handle context when filter reference is input
 * David McKnight   (IBM)        - [225506] [api][breaking] RSE UI leaks non-API types
 * David McKnight   (IBM)        - [278848] NPE in Remote System Details view
 * David McKnight   (IBM)        - [284917] Deleting a folder in a fresh workspace throws NPE
 *******************************************************************************/

package org.eclipse.rse.ui.view;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.rse.core.filters.ISystemFilterReference;
import org.eclipse.rse.core.model.ISystemContainer;
import org.eclipse.rse.core.model.ISystemViewInputProvider;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.internal.ui.view.SystemViewPromptableAdapter;
import org.eclipse.rse.internal.ui.view.SystemViewRootInputAdapter;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.views.properties.IPropertyDescriptor;



/**
 * This is the content and label provider for the SystemTableView. This class is
 * used both to populate the SystemTableView but also to resolve the icon and
 * labels for the cells in the table.
 * 
 * @since 3.0 Moved from internal to API
 */
public class SystemTableViewProvider implements ILabelProvider, ITableLabelProvider, ITreeContentProvider
{


	private ListenerList listeners = new ListenerList(1);

	protected Object[] _lastResults = null;
	protected Object _lastObject = null;
	protected SimpleDateFormat _dateFormat = new SimpleDateFormat();
	protected Viewer _viewer = null;
	protected int _maxCharsInColumnZero = 0;
	private boolean _sortOnly = false;

	/**
	 * The cache of images that have been dispensed by this provider.
	 * Maps ImageDescriptor->Image.
	 */
	private Map imageTable = new Hashtable(40);
	private ISystemTableViewColumnManager _columnManager;
	private HashMap cache;
	/**
	 * Constructor for table view provider where a column manager is present.
	 * In this case, the columns are customizable by the user.
	 * @param columnManager the column manager
	 */
	public SystemTableViewProvider(ISystemTableViewColumnManager columnManager)
	{
		super();
		_columnManager= columnManager;
		cache = new HashMap();
	}

	/**
	 * Constructor for table view provider where a column manager is not present.
	 * In this case, the column can not be customized.
	 */
	public SystemTableViewProvider()
	{
		super();
		_columnManager= null;
	}


	public void inputChanged(Viewer visualPart, Object oldInput, Object newInput)
	{
	    _viewer = visualPart;
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
		ISystemViewElementAdapter adapter = getAdapterFor(object);
		if (adapter != null)
			return adapter.getParent(object);
		return null;
	}

	public boolean hasChildren(Object object)
	{
		return false;
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
	        result = SystemAdapterHelpers.getViewAdapter(object, _viewer);
	    }
	    else
	    {
	        result = SystemAdapterHelpers.getViewAdapter(object);
	    }
	    if (result != null)
	    	result.setPropertySourceInput(object);
	    
	    // for bug 278848
	  	if (_viewer != null && _viewer.getInput() instanceof ISystemViewInputProvider)
	  	{
	    	ISystemViewInputProvider inputProvider = (ISystemViewInputProvider)_viewer.getInput();
	    	result.setInput(inputProvider);
	  	}
	  	
        return result;
	}

	public Object[] getElements(Object object)
	{
		Object[] results = null;
		if (_sortOnly && (object == _lastObject && (_lastResults != null && _lastResults.length > 0)))
		{
			// _sortOnly is used to by-pass a remote query when we're just sorting by columns
			_sortOnly = false; // after using the cache once, revert back to normal query
			return _lastResults;
		}
		else 
			{
				ISystemViewElementAdapter adapter = getAdapterFor(object);
				if (adapter != null)
				{
				    adapter.setViewer(_viewer);

				    // do we have context?
				    if (object instanceof ISystemFilterReference) {
				    	ISubSystem ss = adapter.getSubSystem(object);

				    	ContextObject context = new ContextObject(object, ss, (ISystemFilterReference)object);
				    	results = adapter.getChildren(context, new NullProgressMonitor());
				    }
				    else {
				    	results = adapter.getChildren((IAdaptable)object, new NullProgressMonitor());

				    }
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

			return ""; //$NON-NLS-1$
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
		//	((ISystemContainer)_lastObject).markStale(true);
		}

		_lastResults = null;
		return true;
	}

	public void dispose()
	{
	}
	
	public void setSortOnly(boolean flag){
		_sortOnly = flag;
	}
}

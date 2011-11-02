/********************************************************************************
 * Copyright (c) 2002, 2011 IBM Corporation and others. All rights reserved.
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
 * Martin Oberhuber (Wind River) - [186128] Move IProgressMonitor last in all API
 * David McKnight   (IBM)        - [362700] SystemTableTreeViewProvider should not use context object in adapter call to get subsystem
 ********************************************************************************/

package org.eclipse.rse.internal.ui.view;

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
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.rse.core.model.ISystemContainer;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.ui.view.IContextObject;
import org.eclipse.rse.ui.view.ISystemViewElementAdapter;
import org.eclipse.rse.ui.view.SystemAdapterHelpers;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.model.IWorkbenchAdapter;
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
	private SystemDeferredTableTreeContentManager manager;
	private boolean _disableDeferredQueries = false;

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
		if (_viewer instanceof AbstractTreeViewer) 
		{
			manager = new SystemDeferredTableTreeContentManager(this,  (SystemTableTreeView)_viewer);
		}
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
		ISystemViewElementAdapter adapter = getAdapterFor(object);
    	if (adapter != null) 
    	{
    		return adapter.getParent(object);
    	}
    	else
    	{
    		return null;
    	}
	}

	public boolean hasChildren(Object object)
	{
		ISystemViewElementAdapter adapter = getAdapterFor(object);
    	if (adapter != null) 
    	{
    		return adapter.hasChildren((IAdaptable)object);
    	}
		if (manager != null) {
			if (manager.isDeferredAdapter(object))
				return manager.mayHaveChildren(object);
		}
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
	    if (result == null)
	    {
	    	return null;
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
		{
			Object element = object;
			// object could either be a model object or a wrapper IContextObject 
	    	if (object instanceof IContextObject)
	    	{
	    		element = ((IContextObject)object).getModelObject();
	    	}
			{
				
				ISystemViewElementAdapter adapter = getAdapterFor(element);
				adapter.setViewer(_viewer);
				
		
				
				if (adapter.hasChildren((IAdaptable)element))
				{
					if (supportsDeferredQueries())
			    	{
				        if (manager != null) 
				        {
				            ISubSystem ss = adapter.getSubSystem(element); // should be element (not object) - since object could be context
				            if (ss != null && adapter.supportsDeferredQueries(ss))
				            {
				            	results = manager.getChildren(object);				                
				            }
						}
			    	}
					else
					{
						  if (object instanceof IContextObject)
				    	  {
				    		  results = adapter.getChildren((IContextObject)object, new NullProgressMonitor());
				    	  }
				    	  else 
				    	  {
				    		  results = adapter.getChildren((IAdaptable)object, new NullProgressMonitor());
				    	  }
					}
					if (adapter instanceof SystemViewRootInputAdapter && results != null)
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
		}
		if (results == null)
		{
			return new Object[0];
		}

		
		return results;
	}

	
	public String getText(Object object)
	{	
		String result = null;
		ISystemViewElementAdapter adapter = getAdapterFor(object);
		if (adapter != null)
		{
			result = adapter.getText(object);
		}
		else
		{
		    IWorkbenchAdapter wadapter = (IWorkbenchAdapter)((IAdaptable) object).getAdapter(IWorkbenchAdapter.class);
		   
			if (wadapter == null) 
			{
				  return object.toString();
			}
			return wadapter.getLabel(object);
		}
		if (result != null)
		{
			int len = result.length();
			if (len > _maxCharsInColumnZero)
			{
				_maxCharsInColumnZero = len;
			}
		}
		else
		{
			result = ""; //$NON-NLS-1$
		}
		return result;
	}
	
	public int getMaxCharsInColumnZero()
	{
		return _maxCharsInColumnZero;
	}

	public Image getImage(Object object)
	{
		ImageDescriptor descriptor = null;
		ISystemViewElementAdapter adapter = getAdapterFor(object);
		if (adapter != null)
		{
			descriptor = adapter.getImageDescriptor(object);
		}
		else
		{
		    IWorkbenchAdapter wadapter = (IWorkbenchAdapter)((IAdaptable) object).getAdapter(IWorkbenchAdapter.class);
		    if (wadapter == null)
		    {
		    	return null;
		    }
		    else
		    {
		    	descriptor = wadapter.getImageDescriptor(object);
		    }
		}

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
		    if (adapter == null)
		    {
		    	return null;
		    }
			

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
		_lastObject = parent;
		_lastResults = children;
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
    
    public void disableDeferredQueries(boolean disable)
    {
    	_disableDeferredQueries = disable;
    }

	protected boolean supportsDeferredQueries()
	{
		if (_disableDeferredQueries)
			return false;
	    //IPreferenceStore store = RSEUIPlugin.getDefault().getPreferenceStore();
	    //return store.getBoolean(ISystemPreferencesConstants.USE_DEFERRED_QUERIES);
		return true; // DKM now enforcing deferred queries
	}
}
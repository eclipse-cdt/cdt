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

package org.eclipse.rse.ui.filters;
import java.util.Vector;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.rse.core.subsystems.util.ISubSystemConfigurationAdapter;
import org.eclipse.rse.filters.ISystemFilter;
import org.eclipse.rse.filters.ISystemFilterPool;
import org.eclipse.rse.filters.ISystemFilterPoolManager;
import org.eclipse.rse.filters.ISystemFilterPoolManagerProvider;
import org.eclipse.rse.ui.ISystemIconConstants;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.dialogs.SystemSimpleContentElement;


/**
 * 
 */
public class SystemFilterUIHelpers 
{
	
	/**
	 * Find element corresponding to given data
	 */
	public static SystemSimpleContentElement getDataElement(SystemSimpleContentElement root, Object data)
	{
        SystemSimpleContentElement[] children = root.getChildren();
        SystemSimpleContentElement match = null;
        if ((children!=null)&&(children.length>0))
        {
        	for (int idx=0; (match==null)&&(idx<children.length); idx++)
        	   if (children[idx].getData() == data)
        	     match = children[idx];
        }
        if ((match==null)&&(children!=null)&&(children.length>0))
        {
        	for (int idx=0; (match==null)&&(idx<children.length); idx++)
        	   match = getDataElement(children[idx], data);
        }        
        return match;
	}
	
	/**
	 * Create and return data model to populate selection tree with
	 */
    public static SystemSimpleContentElement getFilterPoolModel(ISystemFilterPoolManagerProvider mgrProvider,
                                                                ISystemFilterPoolManager mgrs[])
    {
    	SystemSimpleContentElement veryRootElement = 
    	   new SystemSimpleContentElement("Filter pools",
    	                                  null, null, (Vector)null);	    	
    	veryRootElement.setRenamable(false);
    	veryRootElement.setDeletable(false);
    	                                  
    	if (mgrs == null)
    	  return veryRootElement;
    	 
    	Vector veryRootChildren = new Vector(); 
    	for (int idx=0; idx<mgrs.length; idx++)
    	{
           SystemSimpleContentElement rootElement = 
    	      new SystemSimpleContentElement(mgrs[idx].getName(),
    	                                     mgrs[idx], veryRootElement, (Vector)null);	
    	   rootElement.setRenamable(false);
    	   rootElement.setDeletable(false);
           rootElement.setImageDescriptor(getFilterPoolManagerImage(mgrProvider, mgrs[idx]));                	   
    	   Vector elements = new Vector();
    	   ISystemFilterPool[] pools = mgrs[idx].getSystemFilterPools();
           populateFilterPoolContentElementVector(pools, elements, rootElement);    	   
           rootElement.setChildren(elements);
           veryRootChildren.addElement(rootElement);
    	}    	
        veryRootElement.setChildren(veryRootChildren);    	
    	return veryRootElement;
    }
    
    /**
     * Internal use only
     */
    protected static void populateFilterPoolContentElementVector(ISystemFilterPool[] pools, 
                                                                 Vector elements, 
                                                                 SystemSimpleContentElement parentElement)
    {
        for (int idx=0; idx<pools.length; idx++)
        {
           ISystemFilterPool pool = pools[idx];
           SystemSimpleContentElement cElement = 
             new SystemSimpleContentElement(pool.getName(), pool, parentElement, (Vector)null);
           cElement.setImageDescriptor(getFilterPoolImage(pool.getProvider(),pool));
           cElement.setDeletable(pool.isDeletable());
           cElement.setRenamable(!pool.isNonRenamable());
           cElement.setReadOnly(pool.getOwningParentName()!=null);
           //cElement.setSelected(setFilterPoolSelection(pool));           
           elements.addElement(cElement);  
        }        
    }


    /**
     * Get the filter pool manager image
     */
    public static ImageDescriptor getFilterPoolManagerImage(ISystemFilterPoolManagerProvider poolMgrProvider, ISystemFilterPoolManager poolMgr)
    {
    	ImageDescriptor poolMgrImage = null;
    	if (poolMgrProvider == null)
    	  poolMgrProvider = poolMgr.getProvider();
    	if (poolMgrProvider != null)
    	{
    		ISubSystemConfigurationAdapter adapter = (ISubSystemConfigurationAdapter)poolMgrProvider.getAdapter(ISubSystemConfigurationAdapter.class);
    	  poolMgrImage = adapter.getSystemFilterPoolManagerImage(); 
    	}
    	if (poolMgrImage == null)
    	  poolMgrImage = RSEUIPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_FILTERPOOL_ID);
    	return poolMgrImage;  	
    }


    /**
     * Get the filter pool image
     */
    public static ImageDescriptor getFilterPoolImage(ISystemFilterPoolManagerProvider poolMgrProvider, ISystemFilterPool pool)
    {
    	ImageDescriptor poolImage = null;
    	if (poolMgrProvider == null)
    	  poolMgrProvider = pool.getProvider();
    	if (poolMgrProvider != null)
    	{
    		ISubSystemConfigurationAdapter adapter = (ISubSystemConfigurationAdapter)poolMgrProvider.getAdapter(ISubSystemConfigurationAdapter.class);
      	  poolImage = adapter.getSystemFilterPoolImage(pool); 
    	}
    	if (poolImage == null)
    	  poolImage = RSEUIPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_FILTERPOOL_ID);
    	return poolImage;  	
    }

    /**
     * Get the filter image
     */
    public static ImageDescriptor getFilterImage(ISystemFilterPoolManagerProvider poolMgrProvider, ISystemFilter filter)
    {
    	ImageDescriptor filterImage = null;
    	if (poolMgrProvider == null)
    	  poolMgrProvider = filter.getProvider();
    	if (poolMgrProvider != null)
    	{
    		ISubSystemConfigurationAdapter adapter = (ISubSystemConfigurationAdapter)poolMgrProvider.getAdapter(ISubSystemConfigurationAdapter.class);
      	  filterImage = adapter.getSystemFilterImage(filter); 
    	}
    	if (filterImage == null)
    	  filterImage = RSEUIPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_FILTER_ID);
    	return filterImage;  	
    }
         
    
}
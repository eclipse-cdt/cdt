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

package org.eclipse.rse.internal.filters;
import java.util.Iterator;
import java.util.Vector;

import org.eclipse.rse.core.SystemBasePlugin;
import org.eclipse.rse.filters.ISystemFilter;
import org.eclipse.rse.filters.ISystemFilterPool;
import org.eclipse.rse.ui.SystemResources;


/**
 * This class abstracts out some common code needed by any class that
 * implements ISystemFilterContainer.
 * <p>
 * This class supports two overloaded version of each method. One that 
 * takes a MOF java.util.List for the filter list, and one that takes a Vector for
 * the filter list. This is to offer seamless flexibility in how the filters
 * are stored internally.
 */
public class SystemFilterContainerCommonMethods
       //implements ISystemFilterContainer
{
    private Vector filterNameVector, filterVector;
    private ISystemFilter[] filterArray;
        	
	/**
	 * Constructor
	 */
	protected SystemFilterContainerCommonMethods() 
	{
		super();
	}

		
    /**
     * For performance reasons we have decided to store a cache of the
     * filters in vector and array form, so each request will not result
     * in a new temporary vector or array. However, this cache can get out
     * of date, so this method must be called religiously to invalidate it
     * after any change in the filters.
     */
    public void invalidateCache()
    {
    	filterNameVector = filterVector = null;
    	filterArray = null;
    }
    
    /**
     * Creates a new system filter within this pool or filter.
     * @param filters MOF list of filters the new filter is to be added to.
     * @param parentPool pool that contains this filter (directly or indirectly).
     * @param aliasName The name to give the new filter. Must be unique for this pool.
     * @param filterStrings The list of String objects that represent the filter strings.
     */    
    public ISystemFilter createSystemFilter(java.util.List filters,
                                           ISystemFilterPool parentPool, 
                                           String aliasName, Vector filterStrings)
    {    	
    	ISystemFilter newFilter = null;
    	
    	// FIXME - not using error message and null return
    	//       because I want to restore filters while not being hit with conflicts
    	newFilter = getSystemFilter(filters, aliasName);
    	if (newFilter != null)
    	{
    		return newFilter;
    	}
    	/* DKM
        boolean exists = getSystemFilter(filters, aliasName) != null;
        if (exists)
        {
          String msg = "Error creating filter: aliasName " + aliasName + " is not unique"; // no need to xlate, internal only
		  RSEUIPlugin.logError(msg);
          return null;
        }
        */
        newFilter = internalCreateSystemFilter(parentPool, aliasName, filterStrings);
        if (newFilter != null)
          internalAddSystemFilter(filters, newFilter);
    	return newFilter;
    }
    /**
     * Creates a new system filter within this pool or filter.
     * @param filters Vector of filters the new filter is to be added to.
     * @param parentPool pool that contains this filter (directly or indirectly)
     * @param aliasName The name to give the new filter. Must be unique for this pool.
     * @param filterStrings The list of String objects that represent the filter strings.
     */    
    public ISystemFilter createSystemFilter(Vector filters,
                                           ISystemFilterPool parentPool, 
                                           String aliasName, Vector filterStrings)
    {    	
    	ISystemFilter newFilter = null;
        boolean exists = getSystemFilter(filters, aliasName) != null;
        if (exists)
        {
          String msg = "Error creating filter: aliasName " + aliasName + " is not unique"; // no need to xlate, internal only
		  SystemBasePlugin.logError(msg);
          return null;
        }
        newFilter = internalCreateSystemFilter(parentPool, aliasName, filterStrings);
        if (newFilter != null)
          internalAddSystemFilter(filters, newFilter);
    	return newFilter;
    }
    

    /**
     * Internal encapsulation of mof effort to create new filter, and setting of
     * the core attributes.
     */    
    private ISystemFilter internalCreateSystemFilter(
                                           ISystemFilterPool parentPool, 
                                           String aliasName, Vector filterStrings)
    {    	
    	ISystemFilter newFilter = null;
        try
        {
          newFilter = new SystemFilter();
        	  // FIXME getMOFfactory().createSystemFilter();
    	  newFilter.setRelease(SystemResources.CURRENT_RELEASE);
          newFilter.setName(aliasName);
          newFilter.setParentFilterPool(parentPool);
          if (filterStrings != null)
            newFilter.setFilterStrings(filterStrings);
          //java.util.List filterStringList = newFilter.getFilterStrings();
          //for (int idx=0; idx<filterStrings.size(); idx++)
          //{
          	//SystemFilterString string = getMOFfactory().createSystemFilterString();
          	//string.setString((String)filterStrings.elementAt(idx));
          	//filterStringList.add(string);
          //}
        } catch (Exception e)
        {
			SystemBasePlugin.logError("Error creating filter: " + e.getClass().getName() + ": " + e.getMessage());
           SystemBasePlugin.logError("...Alias name.: " + aliasName);
		  
           //e.printStackTrace();
        }    	
    	return newFilter;
    }


    /**
     * Return Vector of String objects: the names of existing filters in this container.
     * Needed by name validators for New and Rename actions to verify new name is unique.
     */
    public Vector getSystemFilterNames(java.util.List filters)
    {
    	if ((filterNameVector == null) || (filterNameVector.size() != filters.size()))
    	{
    	  filterNameVector = new Vector();
    	  if (filters == null)
    	    return filterNameVector;
    	  Iterator i = filters.iterator();
    	  while (i.hasNext())
    	  {
    	    ISystemFilter next = (ISystemFilter)i.next();
    	    filterNameVector.addElement(next.getName());
    	  }
    	}    	     
    	return filterNameVector;
    }
    
    /**
     * Return Vector of String objects: the names of existing filters in this container.
     * Needed by name validators for New and Rename actions to verify new name is unique.
     */
    public Vector getSystemFilterNames(Vector filters)
    {
    	if ((filterNameVector == null) || (filterNameVector.size() != filters.size()))
    	{
    	  Vector filterNameVector = new Vector();
    	  if ((filters == null) || (filters.size()==0))
    	    return filterNameVector;
    	  for (int idx=0; idx<filters.size(); idx++)
    	  {
    	    ISystemFilter next = (ISystemFilter)filters.elementAt(idx);
    	    filterNameVector.addElement(next.getName());    		
    	  }
    	}
    	return filterNameVector;
    }
    
    
    /**
     * Return the nested filters as a Vector
     */
    public Vector getSystemFiltersVector(java.util.List filters)
    {
    	if ((filterVector == null) || (filterVector.size() != filters.size()))
    	{    	
    	  filterVector = new Vector();
    	  Iterator i = filters.iterator();
    	  while (i.hasNext())
    	    filterVector.addElement(i.next());
    	}
    	return filterVector;
    }
    /**
     * Return the nested filters as a Vector
     */
    public Vector getSystemFiltersVector(Vector filters)
    {
    	return filters;
    }

    /**
     * Return the nested filters as an array
     */
    public ISystemFilter[] getSystemFilters(java.util.List filters)
    {
    	if ((filterArray == null) || (filterArray.length != filters.size()))
    	{    	
    	  filterArray = new ISystemFilter[filters.size()];    	    	
    	  Iterator i = filters.iterator();
    	  int idx = 0;
    	  while (i.hasNext())
    		filterArray[idx++]=(ISystemFilter)(i.next());
    	}
    	return filterArray;
    	//return null;
    }    
    /**
     * Return the filters as an array
     */
    public ISystemFilter[] getSystemFilters(Vector filters)
    {
    	if (filters == null)
    	  return null;
    	if ((filterArray == null) || (filterArray.length != filters.size()))
    	{    	
    	  filterArray = new ISystemFilter[filters.size()];    	    	
    	  for (int idx=0; idx<filters.size(); idx++)
    		filterArray[idx]=(ISystemFilter)(filters.elementAt(idx));    		
    	}
    	return filterArray;
    }    

    /**
     * Return how many filters are defined in this filter container
     */
    public int getSystemFilterCount(java.util.List filters)
    {    	
		int nbrChildren = (filters == null) ? 0 : filters.size();    	
    	return nbrChildren;
    }      

    /**
     * Return how many filters are defined in this filter container
     */
    public int getSystemFilterCount(Vector filters)
    {    	
		int nbrChildren = (filters == null) ? 0 : filters.size();    	
    	return nbrChildren;
    }      
    
    /**
     * Return true if there are system filters
     */
    public boolean hasSystemFilters(java.util.List filters)
    {
    	return (filters!=null) && (filters.size()>0);
    }
    /**
     * Return true if there are system filters
     */
    public boolean hasSystemFilters(Vector filters)
    {
    	return (filters!=null) && (filters.size()>0);
    }

    
    /**
     * Return a filter object, given its aliasname.
     * Can be used to test if an aliasname is already used (non-null return).
     * @param aliasName unique aliasName (case insensitive) to search on.
     * @return SystemFilter object with unique aliasName, or null if
     *  no filter object with this name exists.
     */
    public ISystemFilter getSystemFilter(java.util.List filters, String aliasName)
    {
        ISystemFilter filter = null;
        ISystemFilter currFilter = null;
        Iterator i = filters.iterator();
        while (i.hasNext() && (filter==null))
        {
           currFilter = (ISystemFilter)i.next();
           if (currFilter.getName().equalsIgnoreCase(aliasName))
             filter = currFilter;
        }
        return filter;
    }
    /**
     * Return a filter object, given its aliasname.
     * Can be used to test if an aliasname is already used (non-null return).
     * @param aliasName unique aliasName (case insensitive) to search on.
     * @return SystemFilter object with unique aliasName, or null if
     *  no filter object with this name exists.
     */
    public ISystemFilter getSystemFilter(Vector filters, String aliasName)
    {
    	if ((filters == null) || (filters.size()==0))
    	  return null;
        ISystemFilter filter = null;
        ISystemFilter currFilter = null;
        for (int idx=0; (idx<filters.size()) && (filter==null); idx++)
        {        	
           currFilter = (ISystemFilter)filters.elementAt(idx);
           if (currFilter.getName().equalsIgnoreCase(aliasName))
             filter = currFilter;
        }
        return filter;
    }
    
    
    /**
     * Adds given filter to the list.
     * <p>PLEASE NOTE:
     * <ul>
     *  <li> createSystemFilter calls this method for you!
     *  <li> this is a no-op if a filter with the same aliasname already exists
     * </ul>
     * @param filter SystemFilter object to add
     * @return true if added, false if filter with this aliasname already existed.
     */
    public boolean addSystemFilter(java.util.List filters, ISystemFilter filter)
    {
        boolean exists = getSystemFilter(filters, filter.getName()) != null;
        if (!exists)
          return internalAddSystemFilter(filters, filter);
        else
          return false;
    }
    /**
     * Internally, we can skip the uniqueness checking.
     */
    protected boolean internalAddSystemFilter(java.util.List filters, ISystemFilter filter)
    {
        filters.add(filter);
        invalidateCache();
        return true;
    }
    /**
     * Adds given filter to the list.
     * <p>PLEASE NOTE:
     * <ul>
     *  <li> createSystemFilter calls this method for you!
     *  <li> this is a no-op if a filter with the same aliasname already exists
     * </ul>
     * @param filter SystemFilter object to add
     * @return true if added, false if filter with this aliasname already existed.
     */
    public boolean addSystemFilter(Vector filters, ISystemFilter filter)
    {
        boolean exists = getSystemFilter(filters, filter.getName()) != null;
        if (!exists)
          return internalAddSystemFilter(filters, filter);
        else
          return false;
    }
    /**
     * Internally, we can skip the uniqueness checking.
     */
    private boolean internalAddSystemFilter(Vector filters, ISystemFilter filter)
    {
        filters.add(filter);
        invalidateCache();
        return true;
    }
    
    

    /**
     * Removes a given filter from the list.
     * Does NOT follow references to remove them.
     * @param filters MOF list to remove from
     * @param filter SystemFilter object to remove
     */
    public void deleteSystemFilter(java.util.List filters, ISystemFilter filter)
    {
        filters.remove(filter);
        invalidateCache();
    }
    /**
     * Renames a given filter from the list.
     * @param filters java.util.List list
     * @param filter SystemFilter object to rename
     * @param newName new name to give filter
     */
    public void renameSystemFilter(java.util.List filters, ISystemFilter filter, String newName)
    {
    	filter.setName(newName);
    	invalidateCache();
    }    

    /**
     * Removes a given filter from the list.
     * Does NOT follow references to remove them.
     * @param filters Vector list to remove from
     * @param filter SystemFilter object to remove
     */
    public void deleteSystemFilter(Vector filters, ISystemFilter filter)
    {
        filters.remove(filter);
        invalidateCache();
    }
    /**
     * Renames a given filter from the list.
     * @param filters Vector list
     * @param filter SystemFilter object to rename
     * @param newName new name to give filter
     */
    public void renameSystemFilter(Vector filters, ISystemFilter filter, String newName)
    {
    	filter.setName(newName);
    	invalidateCache();    	
    }    
    /**
     * Updates a given filter in the list.
     * @param filters Vector list
     * @param filter SystemFilter object to update
     * @param newName new name to give filter
     * @param newString new strings to give filter
     */
    public void updateSystemFilter(Vector filters, ISystemFilter filter, String newName, String[] newStrings)
    {
    	filter.setName(newName);
    	filter.setFilterStrings(newStrings);
    	invalidateCache();    	    	
    }    
    /**
     * Updates a given filter in the list.
     * @param filters java.util.List list
     * @param filter SystemFilter object to update
     * @param newName new name to give filter
     * @param newString new strings to give filter
     */
    public void updateSystemFilter(java.util.List filters, ISystemFilter filter, String newName, String[] newStrings)
    {
    	filter.setName(newName);
    	filter.setFilterStrings(newStrings);
    	invalidateCache();    	    	
    }    
    /**
     * Duplicates a given filter in the list.
     * @param filters MOF list of filters into which to place the clone
     * @param filter SystemFilter object to clone
     * @param aliasName New, unique, alias name to give this filter. Clone will fail if this is not unique.
     */
    public ISystemFilter cloneSystemFilter(java.util.List filters, ISystemFilter filter, String aliasName)
    {

        ISystemFilter copy =
            createSystemFilter(filters, filter.getParentFilterPool(), 
                               aliasName, filter.getFilterStringsVector());
        internalAfterCloneSystemFilter(filter, copy);        
        // now clone nested filters...
        ISystemFilter[] nested = filter.getSystemFilters();
        if ((nested!=null) && (nested.length>0))
          for (int idx=0; idx<nested.length; idx++)
          {
          	  
          }
        return copy;
    }    
    /**
     * Duplicates a given filter in the list.
     * @param filters Vector of filters into which to place the clone
     * @param filter SystemFilter object to clone
     * @param aliasName New, unique, alias name to give this filter. Clone will fail if this is not unique.
     */
    public ISystemFilter cloneSystemFilter(Vector filters, ISystemFilter filter, String aliasName)
    {

        ISystemFilter copy =
            createSystemFilter(filters, filter.getParentFilterPool(), 
                               aliasName, filter.getFilterStringsVector());                               
        internalAfterCloneSystemFilter(filter, copy);        
        // now clone nested filters...
        ISystemFilter[] nested = filter.getSystemFilters();
        if ((nested!=null) && (nested.length>0))
          for (int idx=0; idx<nested.length; idx++)
          {
          	 
          }
        return copy;
    }    

    /**
     * Does core effort to populate a filter clone with non-core attributes
     * @param oldFilter SystemFilter original filter
     * @param newFilter SystemFilter that is new
     */
    public void internalAfterCloneSystemFilter(ISystemFilter oldFilter, ISystemFilter newFilter)
    {
    	newFilter.setParentFilterPool(oldFilter.getParentFilterPool());
    	newFilter.setSupportsNestedFilters(oldFilter.isSupportsNestedFilters());
    	newFilter.setType(oldFilter.getType());    	
    	newFilter.setDefault(oldFilter.isDefault());
    	newFilter.setRelativeOrder(oldFilter.getRelativeOrder());    	
    }    

    /**
     * Return a given filter's zero-based location
     */
    public int getSystemFilterPosition(java.util.List filters, ISystemFilter filter)
    {    	
    	int position = -1;
    	Iterator i = filters.iterator();    	
    	for (int idx=0; (position<0) && (idx < filters.size()); idx++)
    	{
           ISystemFilter currFilter = (ISystemFilter)i.next();
           if (currFilter.getName().equals(filter.getName()))
             position = idx;    	     
    	}
    	return position;
    }
    /**
     * Return a given filter's zero-based location
     */
    public int getSystemFilterPosition(Vector filters, ISystemFilter filter)
    {
    	int position = -1;
    	for (int idx=0; (position<0) && (idx < filters.size()); idx++)
    	{
           ISystemFilter currFilter = (ISystemFilter)filters.elementAt(idx);
           if (currFilter.getName().equals(filter.getName()))
             position = idx;
    	}
    	return position;
    }    

    
    /**
     * Move a given filter to a given zero-based location
     */
    public void moveSystemFilter(java.util.List filters, int pos, ISystemFilter filter)
    {
    	//FIXME filters.move(pos,filter);
    	invalidateCache();
    }
    /**
     * Move a given filter to a given zero-based location
     */
    public void moveSystemFilter(Vector filters, int pos, ISystemFilter filter)
    {
    	filters.remove(filter);
    	filters.insertElementAt(filter, pos);
    	invalidateCache();
    }    
    

} 
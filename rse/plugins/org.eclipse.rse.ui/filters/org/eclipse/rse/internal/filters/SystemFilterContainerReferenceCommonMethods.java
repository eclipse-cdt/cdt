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

import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.filters.ISystemFilter;
import org.eclipse.rse.filters.ISystemFilterContainer;
import org.eclipse.rse.filters.ISystemFilterContainerReference;
import org.eclipse.rse.filters.ISystemFilterPool;
import org.eclipse.rse.filters.ISystemFilterReference;
import org.eclipse.rse.filters.SystemFilterReference;



/**
 * Both SystemFilter and SystemFilterPool contain filters, so the
 *  common methods for filters are abstracted out in SystemFilterContainer,
 *  which both classes implement.
 * Both SystemFilterReference and SystemFilterPoolReference hold references
 *  to SystemFilterContainer objects (either SystemFilter or SystemFilterPool).
 *  There are a couple of methods that are common to both classes, related to
 *  getting an array of references to the filters that are held by the referenced
 *  object.
 * This class offers an implementation of those common methods, and both
 *  SystemFilterReferenceImpl and SystemFilterPoolReferenceImpl use this via
 *  containment.
 */
public class SystemFilterContainerReferenceCommonMethods
{
    private ISystemFilterContainerReference parentRef = null;
    private ISystemFilterReference[] referencedFilters = null;
        	
	/**
	 * Constructor
	 * @param parentRef the object we are helping.
	 */
	public SystemFilterContainerReferenceCommonMethods(ISystemFilterContainerReference parentRef) 
	{
		super();
		this.parentRef = parentRef;
	}

    /**
     * Return the name of the SystemFilter or SystemFilterPool that we reference.
     */	
	public String getName()
	{
    	ISystemFilterContainer parent = parentRef.getReferencedSystemFilterContainer();
    	String name = null;
    	if (parent instanceof ISystemFilterPool)
    	  name = ((ISystemFilterPool)parent).getName();
    	else
    	  name = ((ISystemFilter)parent).getName();		
    	return name;
	}
	
    /**
     * Return true if the referenced pool or filter has filters.
     */
    public boolean hasFilters()
    {
    	return getFilterCount() > 0;
    }    

    /**
     * Return count of the number of filters in the referenced pool or filter
     */
    public int getFilterCount()
    {
    	int count = 0;
    	ISystemFilterContainer parent = parentRef.getReferencedSystemFilterContainer();
    	java.util.List mofList = null;
    	if (parent instanceof ISystemFilterPool)
    	  mofList = ((ISystemFilterPool)parent).getFilters();
    	else
          mofList = ((ISystemFilter)parent).getNestedFilters();
        if (mofList != null)
          count = mofList.size();
        return count;
    }

    /**
     * Get the list of nested filters referenced by this ISystemFilterContainerReference.
     */
    public ISystemFilterReference[] getSystemFilterReferences(ISubSystem subSystem)
    {
    	
        // These reference objects are built on the fly, each time, rather than
        //  maintaining a persisted list of such references. The reason
        //  is we do not at this time allow users to subset the master list
        //  of filters maintained by a filterpool. Hence, we always simply
        //  return a complete list. However, to save memory we try to only
        //  re-gen the list if something has changed.
    	ISystemFilterContainer parent = parentRef.getReferencedSystemFilterContainer();
    	java.util.List mofList = null;
    	if (parent instanceof ISystemFilterPool)
    	  mofList = ((ISystemFilterPool)parent).getFilters();
    	else
          mofList = ((ISystemFilter)parent).getNestedFilters();
    	boolean needToReGen = compareFilters(mofList);
    	//System.out.println("In getSFRefs for " + getName() + ": regen? " + needToReGen);
    	
    	if (needToReGen)
    	{
    	  // first, need remove backward references...
    	  
    	  // second, build new references...
    	  referencedFilters = generateFilterReferences(subSystem, mofList);
    	}
    	return referencedFilters;
    }
    
    /**
     * To save memory, we try to only regenerate the referenced filter list
     * if something has changed.
     */
    private boolean compareFilters(java.util.List newFilters)
    {
    	boolean mustReGen = false;
    	if (newFilters == null)
    	{
    	  if (referencedFilters != null)
    	    return true;
    	  else
    	    return false;
    	}
    	else if (referencedFilters == null)
    	{
          return true; // newFilters != null && referencedFilters == null        	 
    	}
    	// both old and new are non-null
        if (newFilters.size() != referencedFilters.length)
          return true;
        Iterator i = newFilters.iterator();
        for (int idx=0; !mustReGen && (idx<referencedFilters.length); idx++)
        {
           ISystemFilter newFilter = (ISystemFilter)i.next();
           if (referencedFilters[idx].getReferencedFilter() != newFilter)
             mustReGen = true;
        }               	
    	return mustReGen;
    }
        

    /**
     * To save the memory of an intermediate array, we create the filter references 
     * directly from the MOF model...
     */
    private ISystemFilterReference[] generateFilterReferences(ISubSystem subSystem, java.util.List newFilters)
    {
    	if (newFilters == null)
    	  return null;
    	ISystemFilterReference[] oldRefs = referencedFilters;
    	ISystemFilterReference[] refs = new ISystemFilterReference[newFilters.size()];
        Iterator i = newFilters.iterator();
        int idx = 0;
        while (i.hasNext())
        {
            ISystemFilter filter = (ISystemFilter)i.next();
            
            boolean foundOldOne = false;
            
            // DKM - first check if we already have a reference for this
            if (oldRefs != null)
            {
               for (int o = 0; o < oldRefs.length && !foundOldOne; o++)
	            {
	                ISystemFilterReference oRef = oldRefs[o];
	                if (oRef.getReferencedFilter() == filter)
	                {
	                    refs[idx++] = oRef;
	                    foundOldOne = true;
	                }
	            }
            }
            if (!foundOldOne)
            {
                refs[idx++] = generateFilterReference(subSystem, filter);    		            
            }
        }               	
    	return refs;
    }
    

    /**
     * Create a single filter refererence
     */
    public ISystemFilterReference generateFilterReference( ISubSystem subSystem, ISystemFilter filter)
    {
    	return SystemFilterReference.createSystemFilterReference(subSystem, parentRef, filter, SystemFilterReference.PERSISTENT_NO);
    }
    /**
     * Create a single filter refererence and append it to the end of the list.
     * This will first check if there already is a reference to the given filter and if so
     * will simply return it.
     */
    public ISystemFilterReference generateAndRecordFilterReference(ISubSystem subSystem, ISystemFilter filter)
    {    	
    	getSystemFilterReferences(subSystem); // regenerate all references if needed
    	ISystemFilterReference sfr = getExistingSystemFilterReference(subSystem, filter);
    	if (sfr == null) // still no reference exist?
    	{    	  
    	  /*	*/
    	  sfr = generateFilterReference(subSystem, filter);
    	  int currLength = 0;
    	  if (referencedFilters != null)
    	    currLength = referencedFilters.length;
    	  int newLength = currLength + 1;
    	  ISystemFilterReference[] newRefs = new ISystemFilterReference[newLength];    	   
    	  for (int idx=0; idx<currLength; idx++)
    	  {
    	  	newRefs[idx] = referencedFilters[idx];
    	  }
    	  newRefs[newLength-1] = sfr;
    	  referencedFilters = newRefs;
    	  
    	}
        return sfr;
    }
    /**
     * Return an existing reference to a given system filter. 
     * If no reference currently exists to this filter, returns null.
     */
    public ISystemFilterReference getExistingSystemFilterReference(ISubSystem subSystem, ISystemFilter filter)
    {
    	ISystemFilterReference ref = null;
    	ISystemFilterReference[] refs = referencedFilters;
    	if ((refs != null) && (refs.length>0))
    	{
    		for (int idx=0; (ref==null) && (idx<refs.length); idx++)
    		{
    			ISystemFilterReference aref = refs[idx];
    		   if (aref.getReferencedFilter() == filter && aref.getSubSystem() == subSystem)
    		     ref = aref;
    		}
    	}
    	return ref;
    }	
        
} 
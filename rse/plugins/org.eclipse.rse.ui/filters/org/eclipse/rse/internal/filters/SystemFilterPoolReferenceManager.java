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
import java.util.Vector;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.rse.core.SystemResourceHelpers;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.filters.ISystemFilter;
import org.eclipse.rse.filters.ISystemFilterConstants;
import org.eclipse.rse.filters.ISystemFilterNamingPolicy;
import org.eclipse.rse.filters.ISystemFilterPool;
import org.eclipse.rse.filters.ISystemFilterPoolManager;
import org.eclipse.rse.filters.ISystemFilterPoolManagerProvider;
import org.eclipse.rse.filters.ISystemFilterPoolReference;
import org.eclipse.rse.filters.ISystemFilterPoolReferenceManager;
import org.eclipse.rse.filters.ISystemFilterPoolReferenceManagerProvider;
import org.eclipse.rse.filters.ISystemFilterReference;
import org.eclipse.rse.filters.ISystemFilterSavePolicies;
import org.eclipse.rse.internal.references.SystemPersistableReferenceManager;
import org.eclipse.rse.references.ISystemBasePersistableReferencingObject;
import org.eclipse.rse.references.ISystemPersistableReferencedObject;


/**
 * This class manages a persistable list of objects each of which reference
 * a filter pool. This class builds on the parent class SystemPersistableReferenceManager,
 * offering convenience versions of the parent methods that are typed to the
 * classes in the filters framework.
 */
/** 
 * @lastgen class SystemFilterPoolReferenceManagerImpl extends SystemPersistableReferenceManagerImpl implements SystemFilterPoolReferenceManager, SystemPersistableReferenceManager {}
 */
public class SystemFilterPoolReferenceManager extends SystemPersistableReferenceManager implements ISystemFilterPoolReferenceManager
{
	//private SystemFilterPoolManager[]                poolMgrs = null;
    private ISystemFilterPoolManagerProvider	         poolMgrProvider = null;
	private ISystemFilterPoolManager                  defaultPoolMgr = null;
	private ISystemFilterPoolReferenceManagerProvider caller = null;
	private ISystemFilterNamingPolicy                 namingPolicy = null;	
	private int                                      savePolicy = ISystemFilterSavePolicies.SAVE_POLICY_NONE;	
	private Object                                   mgrData = null;
	private IFolder                                  mgrFolder = null;
	private boolean                                  initialized = false;
    private boolean                                  noSave;
    private boolean                                  noEvents;
    private boolean                                  fireEvents = true;
    private ISystemFilterPoolReference[]              fpRefsArray = null;
    private static final ISystemFilterPoolReference[] emptyFilterPoolRefArray = new ISystemFilterPoolReference[0];

/**
	 * Default constructor. Typically called by MOF factory methods.
	 */
	public SystemFilterPoolReferenceManager() 
	{
		super();
	}
	/**
     * Create a SystemFilterPoolReferenceManager instance.
     * @param caller Objects which instantiate this class should implement the
     *   SystemFilterPoolReferenceManagerProvider interface, and pass "this" for this parameter.
     *   Given any filter framework object, it is possible to retrieve the caller's
     *   object via the getProvider method call.
     * @param relatedPoolManagers The managers that owns the master list of filter pools that 
     *   this manager will contain references to.
     * @param mgrFolder the folder that will hold the persisted file. This is used when
     *   the save policy is SAVE_POLICY_ONE_FILE_PER_MANAGER. For SAVE_POLICY_NONE, this
     *   is not used. If it is used, it is created if it does not already exist.
     * @param name the name of the filter pool reference manager. This is used when 
     *   the save policy is SAVE_POLICY_ONE_FILE_PER_MANAGER, to deduce the file name.
     * @param savePolicy The save policy for the filter pool references list. One of the
     *   following from the {@link org.eclipse.rse.filters.ISystemFilterConstants SystemFilterConstants} 
     *   interface:
     *   <ul>
     *     <li>SAVE_POLICY_NONE - no files, all save/restore handled elsewhere
     *     <li>SAVE_POLICY_ONE_FILE_PER_MANAGER - one file: mgrName.xmi
     *   </ul> 
     * @param namingPolicy The names to use for file and folders when persisting to disk. Pass
     *     null to just use the defaults, or if using SAVE_POLICY_NONE.
     */
    public static ISystemFilterPoolReferenceManager createSystemFilterPoolReferenceManager(
                                                    ISystemFilterPoolReferenceManagerProvider caller,
                                                    ISystemFilterPoolManagerProvider relatedPoolManagerProvider,
                                                    IFolder mgrFolder,
                                                    String name,
                                                    int savePolicy, 
                                                    ISystemFilterNamingPolicy namingPolicy)
    {
        SystemFilterPoolReferenceManager mgr = null;    	
     
        if (mgrFolder != null)           
   	      SystemResourceHelpers.getResourceHelpers().ensureFolderExists(mgrFolder);
    	if (namingPolicy == null)
    	  namingPolicy = SystemFilterNamingPolicy.getNamingPolicy();
    	try
    	{
    	   if (savePolicy != ISystemFilterSavePolicies.SAVE_POLICY_NONE)
             mgr = (SystemFilterPoolReferenceManager)restore(caller, mgrFolder, name, namingPolicy);
    	}
    	catch (Exception exc) // real error trying to restore, versus simply not found.
    	{
    	   // todo: something. Log the exception somewhere?
    	}
        if (mgr == null) // not found or some serious error.
        {
    	  mgr = createManager();    	
        }
    	if (mgr != null)
    	{    	   
    	  mgr.initialize(caller, mgrFolder, name, savePolicy, namingPolicy, relatedPoolManagerProvider);
    	}
    	
    	return mgr;        
    }

    /*
     * Private helper method.
     * Uses MOF to create an instance of this class.
     */
    protected static SystemFilterPoolReferenceManager createManager()
    {
    	ISystemFilterPoolReferenceManager mgr = new SystemFilterPoolReferenceManager();
    		// FIXME SystemFilterImpl.initMOF().createSystemFilterPoolReferenceManager();
    	return (SystemFilterPoolReferenceManager)mgr;    	
    }
    


    /*
     * Private helper method to initialize state
     */
    protected void initialize(ISystemFilterPoolReferenceManagerProvider caller,                                 
                              IFolder folder, 
                              String name, 
                              int savePolicy, 
                              ISystemFilterNamingPolicy namingPolicy,
                              ISystemFilterPoolManagerProvider relatedPoolManagerProvider)
    {
    	if (!initialized)
    	  initialize(caller, folder, name, savePolicy, namingPolicy); // core data
    	//setSystemFilterPoolManagers(relatedPoolManagers);
    	setSystemFilterPoolManagerProvider(relatedPoolManagerProvider);
    }

    /*
     * Private helper method to do core initialization.
     * Might be called from either the static factory method or the static restore method.
     */
    protected void initialize(ISystemFilterPoolReferenceManagerProvider caller, 
                              IFolder folder, 
                              String name, 
                              int savePolicy, 
                              ISystemFilterNamingPolicy namingPolicy)
    {
    	this.mgrFolder = folder;
    	setProvider(caller);
    	setName(name);
    	this.savePolicy = savePolicy;
    	setNamingPolicy(namingPolicy);
    	initialized = true;
    }
    
    private void invalidateFilterPoolReferencesCache()
    {
    	fpRefsArray = null;
    	invalidateCache();
    }

    // ------------------------------------------------------------
    // Methods for setting and querying attributes
    // ------------------------------------------------------------
    /**
     * Set the associated master pool manager provider. Note the provider
     *  typically manages multiple pool managers and we manage references
     *  across those.
     */
    public void setSystemFilterPoolManagerProvider(ISystemFilterPoolManagerProvider poolMgrProvider)
    {
    	this.poolMgrProvider = poolMgrProvider;
    }
    /**
     * Get the associated master pool manager provider. Note the provider
     *  typically manages multiple pool managers and we manage references
     *  across those.
     */
    public ISystemFilterPoolManagerProvider getSystemFilterPoolManagerProvider() 
    {
    	return poolMgrProvider;
    }   
    /*
     * Set the managers of the master list of filter pools, from which
     *  objects in this list reference.
     *
    public void setSystemFilterPoolManagers(SystemFilterPoolManager[] mgrs)
    {
    	this.poolMgrs = mgrs;
    }*/
    
    /**
     * Get the managers of the master list of filter pools, from which
     *  objects in this list reference.
     */
    public ISystemFilterPoolManager[] getSystemFilterPoolManagers()
    {
    	//return poolMgrs;
    	return poolMgrProvider.getSystemFilterPoolManagers();
    }
    /**
     * Get the managers of the master list of filter pools, from which
     *  objects in this list reference, but which are not in the list of
     *  managers our pool manager supplier gives us. That is, these are
     *  references to filter pools outside the expected list.
     */
    public ISystemFilterPoolManager[] getAdditionalSystemFilterPoolManagers()
    {
    	ISystemFilterPoolManager[] poolMgrs = getSystemFilterPoolManagers();
    	Vector v = new Vector();
    	
    	ISystemFilterPoolReference[] fpRefs = getSystemFilterPoolReferences();
    	for (int idx=0; idx<fpRefs.length; idx++)
    	{
    	   ISystemFilterPool pool = fpRefs[idx].getReferencedFilterPool();
    	   if (pool != null)
    	   {
    	     ISystemFilterPoolManager mgr = pool.getSystemFilterPoolManager();
             if (!managerExists(poolMgrs, mgr) &&
                 !v.contains(mgr))
             {
             	System.out.println("Found unmatched manager: " + mgr.getName());
             	v.addElement(mgr);
             }
    	   }
    	}    	
    	ISystemFilterPoolManager[] additionalMgrs = null;
    	if (v.size() > 0)
    	{
    	  additionalMgrs = new ISystemFilterPoolManager[v.size()];
    	  for (int idx=0; idx<v.size(); idx++)
    	     additionalMgrs[idx] = (ISystemFilterPoolManager)v.elementAt(idx);    
    	} 
    	return additionalMgrs;
    }
    
    private boolean managerExists(ISystemFilterPoolManager[] mgrs, ISystemFilterPoolManager mgr)
    {
    	boolean match = false;
    	for (int idx=0; !match && (idx<mgrs.length); idx++)
    	   if (mgr == mgrs[idx])
    	     match = true;
    	return match;
    }


    /**
     * Set the default manager of the master list of filter pools, from which
     *  objects in this list reference.
     */
    public void setDefaultSystemFilterPoolManager(ISystemFilterPoolManager mgr)
    {
    	defaultPoolMgr = mgr;
    }
    /**
     * Get the default manager of the master list of filter pools, from which
     *  objects in this list reference.
     */
    public ISystemFilterPoolManager getDefaultSystemFilterPoolManager()
    {
    	return defaultPoolMgr;
    }   

    /**
     * Get the object which instantiated this instance of the filter pool reference manager.
     * This is also available from any filter reference framework object.
     */
    public ISystemFilterPoolReferenceManagerProvider getProvider()
    {
        return caller;    	
    }
    /**
     * Set the object which instantiated this instance of the filter pool reference manager.
     * This makes it available to retrieve from any filter reference framework object,
     * via the ubiquitous getProvider interface method.
     */
    public void setProvider(ISystemFilterPoolReferenceManagerProvider caller)
    {
    	this.caller = caller;
    }    
    /**
     * Turn off callbacks to the provider until turned on again.
     */
    public void setProviderEventNotification(boolean fireEvents)
    {
    	this.fireEvents = fireEvents;
    }
    /**
     * Set the naming policy used when saving data to disk.
     * @see org.eclipse.rse.filters.ISystemFilterNamingPolicy
     */
    public void setNamingPolicy(ISystemFilterNamingPolicy namingPolicy)
    {
    	this.namingPolicy = namingPolicy;
    }

    /**
     * Get the naming policy currently used when saving data to disk.
     * @see org.eclipse.rse.filters.ISystemFilterNamingPolicy
     */
    public ISystemFilterNamingPolicy getNamingPolicy()
    {
    	return namingPolicy;
    }
    
	/**
	 * This is to set transient data that is subsequently queryable.
	 */
	public void setSystemFilterPoolReferenceManagerData(Object data)
	{
		this.mgrData = data;
	}
	
	/**
	 * Return transient data set via setFilterPoolData.
	 */
	public Object getSystemFilterPoolReferenceManagerData()
	{
		return mgrData;
	}    

    /**
     * Set the name. This is an override of mof-generated method
     * in order to potentially rename the disk file for a save
     * policy of SAVE_POLICY_ONE_FILE_PER_MANAGER.
     */
    public void setName(String name)
    {
    	/*
    	 * DWD Setting a name should schedule a save. Is this the same as a rename?
    	 */
//    	String oldName = getName();
    	if (savePolicy == ISystemFilterSavePolicies.SAVE_POLICY_ONE_FILE_PER_MANAGER)
    	{
    	  IFile file = getResourceHelpers().getFile(getFolder(), getSaveFileName());
    	  super.setName(name);
    	  String newFileName = getSaveFileName();
    	  try {
    	    getResourceHelpers().renameFile(file, newFileName);
    	  } catch (Exception exc) 
    	  {
    	  }
    	}
    	else
    	  super.setName(name);
    }
    
    // ---------------------------------------------------
    // Methods that work on FilterPool referencing objects
    // ---------------------------------------------------
    /**
     * Ask each referenced pool for its name, and update it.
     * Called after the name of the pool or its manager changes.
     */
    public void regenerateReferencedSystemFilterPoolNames()
    {
    	ISystemFilterPoolReference[] fpRefs = getSystemFilterPoolReferences();
    	for (int idx=0; idx<fpRefs.length; idx++)
    	{
    	   ISystemFilterPool pool = fpRefs[idx].getReferencedFilterPool();
    	   if (pool != null)
    	     fpRefs[idx].resetReferencedFilterPoolName(pool.getReferenceName());
    	}
        invalidateFilterPoolReferencesCache(); // just in case!
		quietSave();
    }
	/**
	 * Return array of SystemFilterPoolReference objects.
	 * Result will never be null, although it may be an array of length zero.
	 */
	public ISystemFilterPoolReference[] getSystemFilterPoolReferences()
	{
		ISystemBasePersistableReferencingObject[] refObjs = super.getReferencingObjects();
		if (refObjs.length == 0)
		  return emptyFilterPoolRefArray;
		else if ((fpRefsArray == null) || (fpRefsArray.length!=refObjs.length))
		{
		  	fpRefsArray = new ISystemFilterPoolReference[refObjs.length];
		  	for (int idx=0; idx<fpRefsArray.length; idx++)
		    	 fpRefsArray[idx] = (ISystemFilterPoolReference)refObjs[idx];
		}
		return fpRefsArray;
	}
 
	/**
	 * In one shot, set the filter pool references
	 * <p>Calls back to inform provider
	 * @param array of filter pool reference objects to set the list to.
     * @param deReference true to first de-reference all objects in the existing list.
	 */
    public void setSystemFilterPoolReferences(ISystemFilterPoolReference[] filterPoolReferences,
                                              boolean deReference)
    {
    	super.setReferencingObjects(filterPoolReferences, deReference);
        invalidateFilterPoolReferencesCache();
		// callback to provider so they can fire events in their GUI
		if (fireEvents && (caller != null))
		  caller.filterEventFilterPoolReferencesReset();
		quietSave();
    }

    /**
     * Create a filter pool referencing object, but do NOT add it to the list, do NOT call back.
     */
    public ISystemFilterPoolReference createSystemFilterPoolReference(ISystemFilterPool filterPool)
    {
        ISystemFilterPoolReference filterPoolReference = new SystemFilterPoolReference();
        	// FIXME SystemFilterImpl.initMOF().createSystemFilterPoolReference();
        invalidateFilterPoolReferencesCache();
        filterPoolReference.setReferencedObject((ISystemPersistableReferencedObject)filterPool);		
		return filterPoolReference;
    }	
	/**
	 * Add a filter pool referencing object to the list. 
	 * @return the new count of referencing objects
	 */
	public int addSystemFilterPoolReference(ISystemFilterPoolReference filterPoolReference)
	{
		int count = addReferencingObject(filterPoolReference);
		filterPoolReference.setParentReferenceManager(this); // DWD - should be done in the addReferencingObject method
        invalidateFilterPoolReferencesCache();
		quietSave();
		return count;
	}
	/**
	 * Reset the filter pool a reference points to. Called on a move-filter-pool operation
	 */
	public void resetSystemFilterPoolReference(ISystemFilterPoolReference filterPoolReference, ISystemFilterPool newPool)
	{
		filterPoolReference.removeReference();
		filterPoolReference.setReferencedObject(newPool);
		if (fireEvents && (caller != null))
		  caller.filterEventFilterPoolReferenceReset(filterPoolReference);		
		quietSave();
		// don't think we need to invalidate the cache
	}
	/**
	 * Remove a filter pool referencing object from the list.
	 * @param filterPool Reference the reference to remove
	 * @param deReference true if we want to dereference the referenced object (call removeReference on it)
	 * @return the new count of referencing objects
	 */
	public int removeSystemFilterPoolReference(ISystemFilterPoolReference filterPoolReference,
	                                           boolean deReference)
	{
		int count = 0;
		if (!deReference)
		  count = super.removeReferencingObject(filterPoolReference);
	    else
		  count = super.removeAndDeReferenceReferencingObject(filterPoolReference);
		filterPoolReference.setParentReferenceManager(null); // DWD should be done in remove
        invalidateFilterPoolReferencesCache();
		// callback to provider so they can fire events in their GUI
		if (fireEvents && (caller != null))
		  caller.filterEventFilterPoolReferenceDeleted(filterPoolReference);
		quietSave();
		return count;
	}
	/**
	 * Return count of referenced filter pools
	 */
	public int getSystemFilterPoolReferenceCount()
	{
		return super.getReferencingObjectCount();
	}	
    
    /**
     * Return the zero-based position of a SystemFilterPoolReference object within this list
     */
    public int getSystemFilterPoolReferencePosition(ISystemFilterPoolReference filterPoolRef)
    {
    	return super.getReferencingObjectPosition(filterPoolRef);
    }

    /**
     * Move a given filter pool reference to a given zero-based location
     * Calls back to inform provider of the event
     */
    public void moveSystemFilterPoolReference(ISystemFilterPoolReference filterPoolRef, int pos)
    {
    	int oldPos = super.getReferencingObjectPosition(filterPoolRef);
    	super.moveReferencingObjectPosition(pos, filterPoolRef);
        invalidateFilterPoolReferencesCache();
		// callback to provider so they can fire events in their GUI
		if (!noSave)
		  quietSave();
		if (fireEvents && (caller != null) && !noEvents)
		{
		  ISystemFilterPoolReference[] refs = new ISystemFilterPoolReference[1];
		  refs[0] = filterPoolRef;
		  caller.filterEventFilterPoolReferencesRePositioned(refs, pos-oldPos);
		}
    }
    /**
     * Move existing filter pool references a given number of positions.
     * If the delta is negative, they are all moved up by the given amount. If 
     * positive, they are all moved down by the given amount.<p>
     * <p>
     * Calls back to inform provider
     * @param filterPoolRefs Array of SystemFilterPoolReferences to move.
     * @param newPosition new zero-based position for the filter pool references.
     */
    public void moveSystemFilterPoolReferences(ISystemFilterPoolReference[] filterPoolRefs, int delta)
    {
    	int[] oldPositions = new int[filterPoolRefs.length];
    	noEvents = noSave = true;
    	for (int idx=0; idx<filterPoolRefs.length; idx++)
    	   oldPositions[idx] = getSystemFilterPoolReferencePosition(filterPoolRefs[idx]);
    	if (delta > 0) // moving down, process backwards
          for (int idx=filterPoolRefs.length-1; idx>=0; idx--)
             moveSystemFilterPoolReference(filterPoolRefs[idx], oldPositions[idx]+delta);	
        else
          for (int idx=0; idx<filterPoolRefs.length; idx++)
             moveSystemFilterPoolReference(filterPoolRefs[idx], oldPositions[idx]+delta);	        
		invalidateFilterPoolReferencesCache();
        noEvents = noSave = false;
        quietSave();
        if (fireEvents && (caller!=null))
          caller.filterEventFilterPoolReferencesRePositioned(filterPoolRefs, delta);        
    }


    // ----------------------------------------------
    // Methods that work on FilterPool master objects
    // ----------------------------------------------
    /**
     * Return array of filter pools currently referenced by this manager
	 * Result will never be null, although it may be an array of length zero.
     */
    public ISystemFilterPool[] getReferencedSystemFilterPools()
    {
    	ISystemFilterPoolReference[] refs = getSystemFilterPoolReferences();
    	ISystemFilterPool[] pools = new ISystemFilterPool[refs.length];
    	for (int idx=0; idx<pools.length; idx++)
    	   pools[idx] = refs[idx].getReferencedFilterPool();
    	return pools;
    }

	/**
	 * Return true if the given filter pool has a referencing object in this list.
	 */
	public boolean isSystemFilterPoolReferenced(ISystemFilterPool filterPool)
	{
		return super.isReferenced(filterPool);
	}

	/**
	 * Given a filter pool, locate the referencing object for it and return it.
	 * @return the referencing object if found, else null
	 */
	public ISystemFilterPoolReference getReferenceToSystemFilterPool(ISystemFilterPool filterPool)
	{
		return (ISystemFilterPoolReference)super.getReferencedObject(filterPool);
	}

	/**
	 * Given a filter pool, create a referencing object and add it to the list.
	 * Also add that reference to the filterPool itself, and calls back to provider when done.
	 * @param filterPool what to reference
	 * @return the new reference object
	 */
	public ISystemFilterPoolReference addReferenceToSystemFilterPool(ISystemFilterPool filterPool)
	{
		ISystemFilterPoolReference filterPoolReference = createSystemFilterPoolReference(filterPool);
		addReferencingObject(filterPoolReference); // DWD - should be done in addReferencingObject
		filterPoolReference.setParentReferenceManager(this);
        invalidateFilterPoolReferencesCache();
		quietSave();
		// callback to provider so they can fire events in their GUI
		if (fireEvents && (caller != null))
		  caller.filterEventFilterPoolReferenceCreated(filterPoolReference);
		return filterPoolReference;
	}

	/**
	 * Given a filter pool, locate the referencing object for it and remove it from the list.
	 * Also removes that reference from the filterPool itself, and calls back to provider when done.
	 * @return the new count of referencing objects
	 */
	public int removeReferenceToSystemFilterPool(ISystemFilterPool filterPool)
	{
		ISystemFilterPoolReference filterPoolReference = getReferenceToSystemFilterPool(filterPool);
		int newCount = 0;
		if (filterPoolReference != null)
		{
		  filterPoolReference.removeReference(); // getReferencedFilterPool().removeReference(this)
		  newCount = removeReferencingObject(filterPoolReference);
		  filterPoolReference.setParentReferenceManager(null); // DWD should be done in removeReferencingObject
          invalidateFilterPoolReferencesCache();
		  quietSave();
		  // callback to provider so they can fire events in their GUI
		  if (fireEvents && (caller!=null))
		    caller.filterEventFilterPoolReferenceDeleted(filterPoolReference);		  
		}
		else
		  newCount = getSystemFilterPoolReferenceCount();	    
        return newCount;		
	}
    /**
     * A reference filter pool has been renamed. Update our stored name...
     * <p> Calls back to inform provider
     */
    public void renameReferenceToSystemFilterPool(ISystemFilterPool pool)
    {
        ISystemFilterPoolReference poolRef = null;
        ISystemBasePersistableReferencingObject[] refs = getReferencingObjects();
        for (int idx=0; (poolRef==null) && (idx<refs.length); idx++)
           if (refs[idx].getReferencedObject()==pool)
             poolRef = (ISystemFilterPoolReference)refs[idx];
             
        if (poolRef != null)
        {
          String oldName = poolRef.getReferencedObjectName();
          poolRef.resetReferencedFilterPoolName(pool.getReferenceName());
          invalidateFilterPoolReferencesCache();
		  quietSave();
		  // callback to provider so they can fire events in their GUI
		  if (fireEvents && (caller!=null))
		    caller.filterEventFilterPoolReferenceRenamed(poolRef,oldName);         
        }    	
    }
	/**
	 * In one shot, set the filter pool references to new references to supplied filter pools.
	 * @param array of filter pool objects to create references for
     * @param deReference true to first de-reference all objects in the existing list.
	 */
    public void setSystemFilterPoolReferences(ISystemFilterPool[] filterPools,
                                              boolean deReference)
    {
    	if (deReference)
    	  super.removeAndDeReferenceAllReferencingObjects();
		else
		  removeAllReferencingObjects();
		  
    	// add current
    	if (filterPools != null)
    	{
    	  for (int idx=0; idx<filterPools.length; idx++)
    	  {
    	  	//addReferenceToSystemFilterPool(filterPools[idx]);
		    ISystemFilterPoolReference filterPoolReference = createSystemFilterPoolReference(filterPools[idx]);
		    addReferencingObject(filterPoolReference);
		    filterPoolReference.setParentReferenceManager(this); // DWD should be done in addReferencingObject
    	  }
          invalidateFilterPoolReferencesCache();
		  quietSave();    	  
		  // callback to provider so they can fire events in their GUI
		  if (fireEvents && (caller!=null))
		    caller.filterEventFilterPoolReferencesReset();
    	}
    }
    // -------------------------
    // SPECIAL CASE METHODS
    // -------------------------
    /**
     * Create a single filter refererence to a given filter. Needed when a filter
     *  is added to a pool, and the GUI is not showing pools but rather all filters
     *  in all pool references.
     */
    public ISystemFilterReference getSystemFilterReference(ISubSystem subSystem, ISystemFilter filter)
    {
    	// step 1: find the reference to the filter pool that contains this filter
    	ISystemFilterPool pool = filter.getParentFilterPool();
    	ISystemFilterPoolReference poolRef = getReferenceToSystemFilterPool(pool);
    	// step 2: generate a reference for it
    	if (poolRef != null)
    	  return poolRef.getSystemFilterReference(subSystem, filter);
    	else
    	  return null;
    }
    /**
     * Concatenate all filter references from all filter pools we reference, into one
     * big list.
     */
    public ISystemFilterReference[] getSystemFilterReferences(ISubSystem subSystem)
    {
    	ISystemFilterPoolReference[] poolRefs = getSystemFilterPoolReferences();
    	Vector v = new Vector();
        for (int idx=0; idx<poolRefs.length; idx++)
        {
           ISystemFilterReference[] filterRefs = poolRefs[idx].getSystemFilterReferences(subSystem);
    	   for (int jdx=0; jdx<filterRefs.length; jdx++)
    	      v.addElement(filterRefs[jdx]);
    	}
    	ISystemFilterReference[] allRefs = new ISystemFilterReference[v.size()];
    	for (int idx=0; idx<v.size(); idx++)
    	   allRefs[idx] = (ISystemFilterReference)v.elementAt(idx);
    	return allRefs;
    }
        
    /**
     * Given a filter reference, return its position within this reference manager
     *  when you think of all filter references from all filter pool references as 
     *  being concatenated
     */
    public int getSystemFilterReferencePosition(ISystemFilterReference filterRef)
    {
    	ISystemFilterPoolReference[] poolRefs = getSystemFilterPoolReferences();
    	int match = -1;
    	int totalCount = 0;
        for (int idx=0; (match==-1) && (idx<poolRefs.length); idx++)
        {
           ISystemFilterReference[] filterRefs = poolRefs[idx].getSystemFilterReferences(filterRef.getSubSystem());
    	   for (int jdx=0; (match==-1) && (jdx<filterRefs.length); jdx++)
    	   {
    	      if (filterRefs[jdx] == filterRef)
    	        match = totalCount;
    	      else
    	        totalCount++;
    	   }
    	}   
    	return match; 	
    }
    /**
     * Given a filter, return its position within this reference manager
     *  when you think of all filter references from all filter pool references as 
     *  being concatenated
     */
    public int getSystemFilterReferencePosition(ISubSystem subSystem, ISystemFilter filter)
    {
    	ISystemFilterPoolReference[] poolRefs = getSystemFilterPoolReferences();
    	int match = -1;
    	int totalCount = 0;
        for (int idx=0; (match==-1) && (idx<poolRefs.length); idx++)
        {
           ISystemFilterReference[] filterRefs = poolRefs[idx].getSystemFilterReferences(subSystem);
    	   for (int jdx=0; (match==-1) && (jdx<filterRefs.length); jdx++)
    	   {
    	      if (filterRefs[jdx].getReferencedFilter() == filter)
    	        match = totalCount;
    	      else
    	        totalCount++;
    	   }
    	}   
    	return match; 	
    }
    
    // -----------------------
    // SAVE/RESTORE METHODS...
    // -----------------------
    private void quietSave()
    {
    	try { save(); } 
    	catch (Exception exc) 
    	{
    	}
    }
    /**
     * Save all the filter pools to disk.     
     * Only called if the save policy is not none.
     */
    public void save()
        throws Exception
    {
    	System.out.println("Saving filter pool " + this.getName() + "?"); // DWD - debugging
        switch(savePolicy)
        {
          // ONE FILE PER FILTER POOL REFERENCE MANAGER
          case ISystemFilterSavePolicies.SAVE_POLICY_ONE_FILE_PER_MANAGER:
              saveToOneFile();
              break;
        }    	
    }    

    /**
     * Save this reference manager to disk.
     */
    protected boolean saveToOneFile()
        throws Exception
    {
    	/* FIXME
        String saveFileName = getSaveFilePathAndName();
        File saveFile = new File(saveFileName);
        boolean exists = saveFile.exists();
        saveFileName = saveFile.toURL().toString();
		Resource.Factory.Registry reg = Resource.Factory.Registry.INSTANCE;
        Resource.Factory resFactory = reg.getFactory(URI.createURI(saveFileName));
        //System.out.println("Saving filter pool ref mgr "+getName()+" to: " + saveFile);
        //java.util.List ext = resFactory.createExtent(); mof way
        //ext.add(this);
        Resource res = resFactory.createResource(URI.createURI(saveFileName));
		res.getContents().add(this);
        try
        {
          res.save(EMPTY_MAP);
        } catch (Exception e)
        {
           if (debug)
           {
             System.out.println("Error saving filter pool ref mgr "+getName() + " to "+saveFile+": " + e.getClass().getName() + ": " + e.getMessage());
             e.printStackTrace();
           }
           throw e;
        }    
        // if this is the first time we have created this file, we must update Eclipse
        // resource tree to know about it...
        if (!exists)
        {
          try {
             mgrFolder.getProject().refreshLocal(IResource.DEPTH_INFINITE, null);               
          } catch(Exception exc) {}
        }        
        */
        return true;	
    }


    /**
     * Restore the filter pools from disk.
     * After restoration, you must call resolveReferencesAfterRestore!
     * @param caller The object that is calling this, which must implement SystemFilterPoolReferenceManagerProvider
     * @param mgrFolder folder containing filter pool references file.
     * @param name the name of the manager to restore. File name is derived from it when saving to one file.
     * @param namingPolicy to get file name prefix, via getFilterPoolReferenceManagerFileNamePrefix(). Pass null to use default.
     * @return the restored manager, or null if it does not exist. If anything else went
     *  wrong, an exception is thrown.
     */
    public static ISystemFilterPoolReferenceManager restore(ISystemFilterPoolReferenceManagerProvider caller, 
                                                           IFolder mgrFolder, String name, 
                                                           ISystemFilterNamingPolicy namingPolicy)
        throws Exception
    {
    	if (namingPolicy == null)
    	  namingPolicy = SystemFilterNamingPolicy.getNamingPolicy();
    	ISystemFilterPoolReferenceManager mgr = restoreFromOneFile(mgrFolder, name, namingPolicy);
        if (mgr != null)
        {
    	  ((SystemFilterPoolReferenceManager)mgr).initialize(caller, mgrFolder, name, ISystemFilterSavePolicies.SAVE_POLICY_ONE_FILE_PER_MANAGER, namingPolicy); // core data
        }
        return mgr;	
    }    

    /**
     * Restore the filter pools from disk, assuming default for prefix of name.
     */
    public static ISystemFilterPoolReferenceManager restore(ISystemFilterPoolReferenceManagerProvider caller, IFolder mgrFolder, String name)
        throws Exception
    {
    	return restore(caller, mgrFolder, name, null);
    }    
    
    /**
     * Restore filter pools when all are stored in one file
     * @param mgrFolder The folder containing the file to restore from.
     * @param name The name of the manager, from which the file name is derived.
     * @param namingPolicy Naming prefix information for persisted data file names.
     */
    protected static ISystemFilterPoolReferenceManager restoreFromOneFile(IFolder mgrFolder, String name, ISystemFilterNamingPolicy namingPolicy)
       throws Exception
    {
        ISystemFilterPoolReferenceManager mgr = null;
		/* FIXME
		Resource.Factory.Registry reg = Resource.Factory.Registry.INSTANCE;
        //ResourceSet resourceSet = MOF WAY
        // Resource.Factory.Registry.getResourceSetFactory().makeResourceSet();
        Resource res = null;
        String saveFile = getSaveFilePathAndName(mgrFolder, name, namingPolicy);
        try
        {
           //res = resourceSet.load(saveFile); MOF Way
		   Resource.Factory resFactory = reg.getFactory(URI.createURI(saveFile));
		   res = resFactory.createResource(URI.createURI(saveFile));
		   res.load(EMPTY_MAP);
        }
        catch (java.io.FileNotFoundException e)
        {
           System.out.println("Restore error: Filter pool ref mgr "+name+" missing its file: "+saveFile);
           return null;
        }
        catch (Exception e)
        {
           if (debug)
           {
             System.out.println("Error restoring filter pool ref mgr "+name+" file "+saveFile+": " + e.getClass().getName() + ": " + e.getMessage());
             e.printStackTrace();
           }
           throw e;
        }

        java.util.List ext = res.getContents();

        // should be exactly one system filter pool manager...
        Iterator iList = ext.iterator();
        mgr = (SystemFilterPoolReferenceManager)iList.next();
        if (debug)
          System.out.println("Filter Pool Ref Mgr "+name+" loaded successfully.");
          */
        return mgr;	
    }




    /**
     * After restoring this from disk, there is only the referenced object name,
     * not the referenced object pointer, for each referencing object.
     * <p>
     * This method is called after restore and for each restored object in the list must:
     * <ol>
     *   <li>Do what is necessary to find the referenced object, and set the internal reference pointer.
     *   <li>Call addReference(this) on that object so it can maintain it's in-memory list
     *          of all referencing objects.
     *   <li>Set the important transient variables 
     * </ol>
     * @param relatedManagers the filter pool managers that hold filter pools we reference
     * @param provider the host of this reference manager, so you can later call getProvider
     * @return A Vector of SystemFilterPoolReferences that were not successfully resolved, or null if all
     *   were resolved.
     */
    public Vector resolveReferencesAfterRestore(ISystemFilterPoolManagerProvider relatedPoolMgrProvider,
                                                 ISystemFilterPoolReferenceManagerProvider provider)
    {
    	setSystemFilterPoolManagerProvider(relatedPoolMgrProvider); // sets poolMgrs = relatedManagers
    	setProvider(provider);
    	//com.ibm.etools.systems.subsystems.SubSystem ss = (com.ibm.etools.systems.subsystems.SubSystem)provider;
    	//System.out.println("Inside resolveReferencesAfterRestore for subsys " +getName() + " in conn " + ss.getSystemProfile() + "." + ss.getSystemConnection());
    	ISystemFilterPoolManager[] relatedManagers = getSystemFilterPoolManagers();
    	if (relatedManagers != null)
    	{
    		Vector badRefs = new Vector();
    		ISystemFilterPoolReference[] poolRefs = getSystemFilterPoolReferences();
    		if (poolRefs != null)
    		{
    		  for (int idx=0; idx<poolRefs.length; idx++)
    		  {
    		  	 String poolName = poolRefs[idx].getReferencedFilterPoolName();
    		  	 String mgrName = poolRefs[idx].getReferencedFilterPoolManagerName();
    		  	     		  	 
    		  	 ISystemFilterPool refdPool = getFilterPool(relatedManagers, mgrName, poolName);
                 if ((refdPool == null) && (getFilterPoolManager(relatedManagers, mgrName) == null))
                 {
    	           //System.out.println("...looking for broken reference for "+mgrName+"."+poolName);
                   refdPool = relatedPoolMgrProvider.getSystemFilterPoolForBrokenReference(this, mgrName, poolName);
                 }    		  	 
    		  	 
    		  	 if (refdPool != null)
    		  	 {
    		  	   poolRefs[idx].setReferenceToFilterPool(refdPool); // calls refdPool.addReference(poolRef)
    		  	 }
    		  	 else
    		  	 {
    		  	 	
    		  	   badRefs.addElement(poolRefs[idx]);
    		  	 }
    		  }
    		  if (badRefs.size() == 0)
    		    return null;
    		  else
    		  {
    		  	for (int idx=0; idx<badRefs.size(); idx++)
    		  	{
    		  		ISystemFilterPoolReference badRef = (ISystemFilterPoolReference)badRefs.elementAt(idx);
    		  		//badRef.setReferenceBroken(true);
		            super.removeReferencingObject(badRef);
    		  	}
                invalidateFilterPoolReferencesCache();    		  	
                quietSave();
    	        //System.out.println("End of resolveReferencesAfterRestore for provider " + getName());
                return badRefs;
    		  }
    		}
    	}
    	return null;
    }
    
    /**
     * Utility method to scan across all filter pools in a given named filter pool manager, for a match
     * on a given filter pool name.
     * <p>
     * @param mgrs The list of filter pool managers to scan for the given filter pool.
     * @param mgrName The name of the manager to restrict the search to
     * @param poolReferenceName The name of the filter pool as stored on disk. It may be qualified somehow
     *  to incorporate the manager name too.
     */
    public static ISystemFilterPool getFilterPool(ISystemFilterPoolManager[] mgrs, String mgrName, String poolName)
    {
    	ISystemFilterPoolManager mgr = getFilterPoolManager(mgrs, mgrName);
    	if (mgr == null)
    	  return null;
        return mgr.getSystemFilterPool(poolName);
    }
    /**
     * Utility method to scan across all filter pool managers for a match on a give name.
     * <p>
     * @param mgrs The list of filter pool managers to scan for the given name
     * @param mgrName The name of the manager to restrict the search to
     */
    public static ISystemFilterPoolManager getFilterPoolManager(ISystemFilterPoolManager[] mgrs, String mgrName)
    {
    	ISystemFilterPoolManager mgr = null;
    	for (int idx=0; (mgr==null)&&(idx<mgrs.length); idx++)
    	   if (mgrs[idx].getName().equals(mgrName))
    	     mgr = mgrs[idx];
        return mgr;
    }
    
    // ------------------
    // HELPER METHODS...
    // ------------------


    /**
     * If saving all info in one file, this returns the fully qualified name of that file,
     * given the unadorned manager name and the prefix (if any) to adorn with.
     */
    protected static String getSaveFilePathAndName(IFolder mgrFolder, String name, ISystemFilterNamingPolicy namingPolicy)
    {
        return SystemFilter.addPathTerminator(getFolderPath(mgrFolder)) 
                 + getSaveFileName(namingPolicy.getReferenceManagerSaveFileName(name));    	
    }  
    /**
     * Derive and return the unqualified file name used to store this to disk.
     * It is unqualified.
     */
    protected static String getSaveFileName(String fileNameNoSuffix)
    {
    	return fileNameNoSuffix + ISystemFilterConstants.SAVEFILE_SUFFIX;
    }    
    /**
     * non-static version.
     */
    protected String getSaveFilePathAndName()
    {
        return SystemFilter.addPathTerminator(getFolderPath(mgrFolder)) 
                 + getSaveFileName();    	
    }  
    /**
     * non-static version.
     */
    protected String getSaveFileName()
    {
    	return getSaveFileName(namingPolicy.getReferenceManagerSaveFileName(getName()));
    }    
    

    /**
     * Return the folder that this manager is contained in.
     */
    public IFolder getFolder()
    {
    	return mgrFolder;
    }

    /**
     * Reset the folder that this manager is contained in.
     */
    public void resetManagerFolder(IFolder newFolder)
    {
    	mgrFolder = newFolder;
    }

    /**
     * Return the path of the folder
     */
    public String getFolderPath()
    {
    	return getResourceHelpers().getFolderPath(mgrFolder);
    }
    /**
     * Return the path of the given folder
     */
    public static String getFolderPath(IFolder folder)
    {
    	return SystemResourceHelpers.getResourceHelpers().getFolderPath(folder);
    }

    /*
     * To reduce typing...
     */
    private SystemResourceHelpers getResourceHelpers()
    {
    	return SystemResourceHelpers.getResourceHelpers();
    }






    public String toString()
    {
    	return getName();
    }

}
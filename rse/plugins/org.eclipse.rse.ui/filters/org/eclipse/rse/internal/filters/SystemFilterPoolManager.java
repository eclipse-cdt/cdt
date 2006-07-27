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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.rse.filters.ISystemFilter;
import org.eclipse.rse.filters.ISystemFilterConstants;
import org.eclipse.rse.filters.ISystemFilterContainer;
import org.eclipse.rse.filters.ISystemFilterNamingPolicy;
import org.eclipse.rse.filters.ISystemFilterPool;
import org.eclipse.rse.filters.ISystemFilterPoolManager;
import org.eclipse.rse.filters.ISystemFilterPoolManagerProvider;
import org.eclipse.rse.filters.ISystemFilterPoolReference;
import org.eclipse.rse.filters.ISystemFilterPoolReferenceManager;
import org.eclipse.rse.filters.ISystemFilterString;
import org.eclipse.rse.logging.Logger;
import org.eclipse.rse.model.ISystemProfile;
import org.eclipse.rse.persistence.IRSEPersistenceManager;
import org.eclipse.rse.references.ISystemBaseReferencingObject;
import org.eclipse.rse.ui.RSEUIPlugin;

//
//

/**
 * A filter pool manager manages filter pools.
 * <p>
 * Each filter pool that is managed becomes a folder on disk.
 * <p>
 * To create a filter pool manager instance, use the factory method
 *  in SystemFilterPoolManagerImpl in the ...impl package. 
 * You must pass a folder that represents the anchor point for the 
 *  pools managed by this manager instance.
 * <p>
 * Depending on your tools' needs, you have four choices about how
 * the filter pools and filters are persisted to disk. The decision is
 * made at the time you instantiate the pool manager and is one of the
 * following constants from the SystemFilterConstants interface:
 * <ul>
 *   <li>SAVE_POLICY_ONE_FILE_PER_MANAGER - one file: mgrName.xmi
 *   <li>SAVE_POLICY_ONE_FILEANDFOLDER_PER_POOL - one file and folder per pool
 *   <li>SAVE_POLICY_ONE_FILE_PER_POOL_SAME_FOLDER - one file per pool, all files in one folder
 *   <li>SAVE_POLICY_ONE_FILE_PER_FILTER - one file per filter, one folder per pool
 * </ul> 
 * <p>
 * With the policy of one file per pool, there are two possibilities regarding
 * the folder structure:
 * <ul>
 *   <li>Each pool gets its own subfolder, and the pool's xmi file goes in 
 *         the pool's unique subfolder: SAVE_POLICY_ONE_FILEANDFOLDER_PER_POOL
 *   <li>There are no subfolders per pool, all the xmi pool files go in the 
 *         same folder as specified when creating this manager instance:
 *         SAVE_POLICY_ONE_FILE_PER_POOL_SAME_FOLDER
 * </ul>
 * <p>
 * With the policy of one file per filter, each filter pool must have its own folder.
 * <p>
 * With an instantiated filter pool manager (most tools will only need
 *  one such instance), you now simply call its methods to work with
 *  filter pools. For example, use it to:
 *  <ul>
 *    <li>Restore all filter pools from disk
 *    <li>Save all, or individual, filter pools to disk
 *    <li>Get a list of existing filter pools
 *    <li>Create filter pools
 *    <li>Delete filter pools
 *    <li>Clone filter pools
 *    <li>Rename filter pools
 *    <li>Save all, or individual, filter pools
 *  </ul>
 * All the underlying file system work is handled for you.
 * <p>
 * Further, this is the front door for working with filters too. By forcing all
 * filter related activity through a single point like this, we can ensure that
 * all changes are saved to disk, and events are fired properly.
 * <p>
 * The filter framework logs to a {@link org.eclipse.rse.logging.Logger Logger} file.
 * By default the log in the org.eclipse.rse.core plugin is used, but you can change this
 * by calling {@link #setLogger(org.eclipse.rse.logging.Logger)}.
 */
/** 
 * @lastgen class SystemFilterPoolManagerImpl Impl implements SystemFilterPoolManager {}
 */
public class SystemFilterPoolManager implements ISystemFilterPoolManager
{
	private ISystemFilterPool[] poolArray = null; // cache for performance
	private ISystemFilterPoolManagerProvider caller = null;
	private Object    poolMgrData;
	private Vector    poolNames;
	private boolean   initialized = false;

	private boolean   suspendCallbacks = false;
	private boolean   suspendSave = false;
	private Logger    logger = null;
	private ISystemProfile _profile;
	
	// persistence	
	protected boolean _isDirty = true;
	private boolean   _wasRestored = false;
		
	public static boolean debug = true;
	
	/**
	 * The default value of the '{@link #getName() <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getName()
	 * @generated
	 * @ordered
	 */
	protected static final String NAME_EDEFAULT = null;


	protected String name = NAME_EDEFAULT;
	/**
	 * The default value of the '{@link #isSupportsNestedFilters() <em>Supports Nested Filters</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSupportsNestedFilters()
	 * @generated
	 * @ordered
	 */
	protected static final boolean SUPPORTS_NESTED_FILTERS_EDEFAULT = false;

	/**
	 * @generated This field/method will be replaced during code generation.
	 */
	protected boolean supportsNestedFilters = SUPPORTS_NESTED_FILTERS_EDEFAULT;
	/**
	 * The default value of the '{@link #isStringsCaseSensitive() <em>Strings Case Sensitive</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isStringsCaseSensitive()
	 * @generated
	 * @ordered
	 */
	protected static final boolean STRINGS_CASE_SENSITIVE_EDEFAULT = false;

	/**
	 * @generated This field/method will be replaced during code generation.
	 */
	protected boolean stringsCaseSensitive = STRINGS_CASE_SENSITIVE_EDEFAULT;
	/**
	 * The default value of the '{@link #isSupportsDuplicateFilterStrings() <em>Supports Duplicate Filter Strings</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSupportsDuplicateFilterStrings()
	 * @generated
	 * @ordered
	 */
	protected static final boolean SUPPORTS_DUPLICATE_FILTER_STRINGS_EDEFAULT = false;

	/**
	 * @generated This field/method will be replaced during code generation.
	 */
	protected boolean supportsDuplicateFilterStrings = SUPPORTS_DUPLICATE_FILTER_STRINGS_EDEFAULT;
	/**
	 * This is true if the Supports Duplicate Filter Strings attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean supportsDuplicateFilterStringsESet = false;

	/**
	 * The default value of the '{@link #isSingleFilterStringOnly() <em>Single Filter String Only</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSingleFilterStringOnly()
	 * @generated
	 * @ordered
	 */
	protected static final boolean SINGLE_FILTER_STRING_ONLY_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isSingleFilterStringOnly() <em>Single Filter String Only</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSingleFilterStringOnly()
	 * @generated
	 * @ordered
	 */
	protected boolean singleFilterStringOnly = SINGLE_FILTER_STRING_ONLY_EDEFAULT;

	/**
	 * @generated This field/method will be replaced during code generation.
	 */
	protected java.util.List pools = null;
	
	
	/**
	 * Constructor
	 */
	protected SystemFilterPoolManager(ISystemProfile profile) 
	{
		super();
		_profile = profile;
	}
	
	public ISystemProfile getSystemProfile()
	{
		return _profile;
	}
	
	/**
     * Factory to create a filter pool manager.
     * @param logger A logging object into which to log errors as they happen in the framework
     * @param caller Objects which instantiate this class should implement the
     *   SystemFilterPoolManagerProvider interface, and pass "this" for this parameter.
     *   Given any filter framework object, it is possible to retrieve the caller's
     *   object via the getProvider method call.
     * @param mgrFolder the folder that will be the manager folder. This is 
     *   the parent of the filter pool folders the manager folder will hold, or the single
     *   xmi file for the save policy of one file per manager. This folder will be created
     *   if it does not already exist.
     * @param name the name of the filter pool manager. Typically this is also the name
     *   of the given folder, but this is not required. For the save policy of one file
     *   per manager, the name of the file is derived from this. For other save policies,
     *   the name is not used.
     * @param allowNestedFilters true if filters inside filter pools in this manager are
     *   to allow nested filters. This is the default, but can be overridden at the 
     *   individual filter pool level.
     * @param savePolicy The save policy for the filter pools and filters. One of the
     *   following constants from the 
     *   {@link org.eclipse.rse.filters.ISystemFilterConstants SystemFilterConstants} interface:
     *   <ul>
     *     <li>SAVE_POLICY_NONE - no files, all save/restore handled elsewhere
     *     <li>SAVE_POLICY_ONE_FILE_PER_MANAGER - one file: mgrName.xmi
     *     <li>SAVE_POLICY_ONE_FILEANDFOLDER_PER_POOL - one file and folder per pool
     *     <li>SAVE_POLICY_ONE_FILE_PER_POOL_SAME_FOLDER - one file per pool, all files in one folder
     *     <li>SAVE_POLICY_ONE_FILE_PER_FILTER - one file per filter, one folder per pool
     *   </ul> 
     * @param namingPolicy The names to use for file and folders when persisting to disk. Pass
     *     null to just use the defaults, or if using SAVE_POLICY_NONE.
     */
    public static ISystemFilterPoolManager 
                    createSystemFilterPoolManager(ISystemProfile profile, Logger logger,
                                                  ISystemFilterPoolManagerProvider caller,
                                                  String name,
                                                  boolean allowNestedFilters, 
                                                  int savePolicy, ISystemFilterNamingPolicy namingPolicy) 
    {
    
    	SystemFilterPoolManager mgr = null;
    	if (namingPolicy == null)
    	  namingPolicy = SystemFilterNamingPolicy.getNamingPolicy();
    	try
    	{
    		mgr = (SystemFilterPoolManager)RSEUIPlugin.getThePersistenceManager().restoreFilterPoolManager(profile, logger, caller, name);    		
    		/*
    	   if (savePolicy != SystemFilterConstants.SAVE_POLICY_NONE)
             mgr = (SystemFilterPoolManagerImpl)restore(;
             */
    	}
    	catch (Exception exc) // real error trying to restore, versus simply not found.
    	{
    	   // todo: something. Log the exception somewhere?
    	}
        if (mgr == null) // not found or some serious error.
        {
    	  mgr = createManager(profile);    	
        }
    	if (mgr != null)
    	{    	   
    	  mgr.initialize(logger, caller, name, allowNestedFilters);
    	}
    	return mgr;
    }

    /*
     * Private helper method.
     * Uses MOF to create an instance of this class.
     */
    public static SystemFilterPoolManager createManager(ISystemProfile profile)
    {
    	ISystemFilterPoolManager mgr = new SystemFilterPoolManager(profile);
    		
    		//FIXME SystemFilterImpl.initMOF().createSystemFilterPoolManager();
    	return (SystemFilterPoolManager)mgr;    	
    }
    
    /*
     * Private helper method to initialize state
     */
    public  void initialize(Logger logger, ISystemFilterPoolManagerProvider caller, String name, 
                              boolean allowNestedFilters)
    {
    	if (!initialized)
    	  initialize(logger, caller, name); // core data
     
            	{
      		java.util.List pools = getPools();
      		ISystemFilterPool pool = null;
      		Vector poolNames = getSystemFilterPoolNamesVector();
      		for (int idx=0; idx<poolNames.size(); idx++)
      		{
      			String poolName = (String)poolNames.elementAt(idx);
      			pool = RSEUIPlugin.getThePersistenceManager().restoreFilterPool(poolName);
      			pool.setSystemFilterPoolManager(this);
      			pools.add(pool);
      			if (((SystemFilterPool)pool).specialCaseNoDataRestored)
      			  {
      			  	pool.setDeletable(true); // what else to do?
      			  	//pool.setSupportsNestedFilters(allowNestedFilters); will be cascaded down anyway
      			  }
      		} 
      	
      	}
    	setSupportsNestedFilters(allowNestedFilters); // cascade it down    	
    	invalidatePoolCache();
    }

    /*
     * Private helper method to do core initialization.
     * Might be called from either the static factory method or the static restore method.
     */
    public void initialize(Logger logger, ISystemFilterPoolManagerProvider caller, String name)
    {
    	this.logger = logger;
    	setProvider(caller);
    	setNameGen(name);
    	setFilterPoolManager(); // cascade it down
    	initialized = true;
    }

    
        
    /**
     * Return the caller which instantiated the filter pool manager
     */
    public ISystemFilterPoolManagerProvider getProvider()
    {
    	return caller;
    }
    
    /**
     * Set the caller instance which instantiated the filter pool manager.
     * This is only recorded to enable getProvider from any filter framework object.
     */
    public void setProvider(ISystemFilterPoolManagerProvider caller)
    {
    	this.caller = caller;
    }

    /**
     * Set the name of this manager.
     * Intercepted so the file get be renamed for SAVE_POLICY_ONE_FILE_PER_MANAGER.
     */
	public void setName(String name)
    {
    	String oldName = getName();
    	if (oldName != null)
    	{
    	  if (!oldName.equals(name))
    	  {
    		  this.name = name;  
    	  }
    	}
    }
    /**
     * Return attribute indicating if filter pools managed by this manager support nested filters.
     * Same as isSupportsNestedFilters()
     */
    public boolean supportsNestedFilters()
    {
    	return isSupportsNestedFilters();
    }
    /**     
     * Return attribute indicating if filters managed by this manager support nested duplicate filter strings.
     * Same as isSupportsDuplicateFilterStrings()
     */
    public boolean supportsDuplicateFilterStrings()
    {
    	//return allowDuplicateFilterStrings;
    	return isSupportsDuplicateFilterStrings();
    }    
    /**
     * Set attribute indicating if filter pools managed by this manager support nested filters, by default.
     * Cascaded down to all pools, and all filters in all pools.
     * Alternatively, just call it on the particular pool or filter it applies to.
     */
	public void setSupportsNestedFilters(boolean newSupportsNestedFilters)
    {
    	// as generated by emf...
		setSupportsNestedFiltersGen(newSupportsNestedFilters);    	
    	// our own stuff..
    	ISystemFilterPool[] pools = getSystemFilterPools();
    	for (int idx=0; idx<pools.length; idx++)
    	{
    		pools[idx].setSupportsNestedFilters(newSupportsNestedFilters);
    	}
    }
    /**
     * Set attribute indicating if filters managed by this manager support duplicate filter strings, by default.
     * Cascaded down to all pools, and all filters in all pools. 
     * Alternatively, just call it on the particular pool or filter it applies to.
     */
    public void setSupportsDuplicateFilterStrings(boolean newSupportsDuplicateFilterStrings)
    {
		// as generated by emf... 
		setSupportsDuplicateFilterStringsGen(newSupportsDuplicateFilterStrings);
			
		// our own stuff..
    	ISystemFilterPool[] pools = getSystemFilterPools();
    	for (int idx=0; idx<pools.length; idx++)
    	{
    		pools[idx].setSupportsDuplicateFilterStrings(newSupportsDuplicateFilterStrings);
    	}
    }
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setSupportsDuplicateFilterStringsGen(boolean newSupportsDuplicateFilterStrings)
	{
		supportsDuplicateFilterStrings = newSupportsDuplicateFilterStrings;
		supportsDuplicateFilterStringsESet = true;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isStringsCaseSensitive()
	{
		return stringsCaseSensitive;
	}

	/**
	 * Same as isStringsCaseSensitive()
	 * Are filter strings in this filter case sensitive?
	 * @return The value of the StringsCaseSensitive attribute
	 */
	public boolean areStringsCaseSensitive()
	{
		return isStringsCaseSensitive();
	}
	/**
     * Set attribute indicating if filters managed by this manager support case-sensitive filter strings, by default.
     * Cascaded down to all pools, and all filters in all pools. 
     * Alternatively, just call it on the particular pool or filter it applies to.
     */
	public void setStringsCaseSensitive(boolean newStringsCaseSensitive)
    {
    	// as generated by emf...
		setStringsCaseSensitiveGen(newStringsCaseSensitive);
        // our special code...    	
    	ISystemFilterPool[] pools = getSystemFilterPools();
    	for (int idx=0; idx<pools.length; idx++)
    	{
    		pools[idx].setStringsCaseSensitive(newStringsCaseSensitive);
    	}
    }
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setStringsCaseSensitiveGen(boolean newStringsCaseSensitive)
	{
		stringsCaseSensitive = newStringsCaseSensitive;
	}

    /**
     * Set transient variable pointing back to us. Called after restoring.
     * Cascaded down to all pools, and all filters in all pools.
     */
    protected void setFilterPoolManager()
    {
    	ISystemFilterPool[] pools = getSystemFilterPools();
    	for (int idx=0; idx<pools.length; idx++)
    	{
    		pools[idx].setSystemFilterPoolManager(this);
    	}
    }
        
	/**
	 * This is to set transient data that is subsequently queryable.
	 */
	public void setSystemFilterPoolManagerData(Object data)
	{
		this.poolMgrData = data;
	}
	
	/**
	 * Return transient data set via setFilterPoolData.
	 */
	public Object getSystemFilterPoolManagerData()
	{
		return poolMgrData;
	}

    
    /**
     * Return array of SystemFilterPools managed by this manager.
     */
    public ISystemFilterPool[] getSystemFilterPools()
    {
    	//System.out.println("Inside getSFPools for mgr "+getName()+". poolArray null? "+(poolArray==null));
    	if ((poolArray == null) || (getPools().size()!=poolArray.length))
    	{
      	  java.util.List pools = getPools();
      	  poolArray = new ISystemFilterPool[pools.size()];
      	  Iterator i = pools.iterator();
      	  int idx=0;
      	  while (i.hasNext())
      	    poolArray[idx++] = (ISystemFilterPool)i.next();
    	  //System.out.println("Pool array created. length = "+poolArray.length);
    	}
    	return poolArray;
    }
    
    /**
     * Get list of filter pool names currently existing.
     */
    public String[] getSystemFilterPoolNames()
    {
    	Vector v = getSystemFilterPoolNamesVector();
    	String[] names = new String[v.size()];
    	for (int idx=0; idx<names.length; idx++)
    	   names[idx] = (String)v.elementAt(idx);
    	return names;
    }

    /**
     * Get list of filter pool names currently existing.
     */
    public Vector getSystemFilterPoolNamesVector()
    {
    	java.util.List pools = getPools();
    	if ((poolNames == null) || (poolNames.size() != pools.size())) // been invalidated?
    	{
    	  poolNames = new Vector();
      	  Iterator i = pools.iterator();
      	  while (i.hasNext())
      	    poolNames.addElement( ((ISystemFilterPool)i.next()).getName() );    	  
    	}
    	return poolNames;
    }

    /*
     * Call this to invalidate array cache after any activity 
     */
    private void invalidatePoolCache()
    {
    	poolArray = null;
    	poolNames = null;
    }
        
    /**
     * Create a new filter pool.
     * Inherits the following attributes from this manager:
     * <ul>
     *   <li>data ... the transient data to be associated with every filter pool and filter
     *   <li>supportsNestedFilters ... whether filters in the pool can themselves contain filters
     *   <li>save policy
     *   <li>filter pool folder and file name prefixes
     * </ul>
     * <p>
     * If a pool of this name already exists, null will be returned.
     * <p>
     * Depending on the save policy, a new folder to hold the pool may be created. Its name will
     *   be derived from the pool name.
     * <p>
     * If the operation is successful, the pool will be saved to disk.
     * <p>
     * If this operation fails unexpectedly, an exception will be thrown.
     * <p>
     * Calls back to inform provider of this event
     */
    public ISystemFilterPool createSystemFilterPool(String poolName, boolean isDeletable)
      throws Exception
    {
    	// always trim the pool name, MOF does not handle trailing or preceding spaces
    	poolName = poolName.trim();
    	
        if (getSystemFilterPool(poolName) != null)
          return null;        

        ISystemFilterPool pool = null;
     
        pool = SystemFilterPool.createSystemFilterPool(poolName, 
                                                           supportsNestedFilters(),
                                                           isDeletable,
		                                                   ISystemFilterConstants.TRY_TO_RESTORE_NO);
        
        if (pool != null)
        {
          pool.setSystemFilterPoolManager(this);
          pool.setStringsCaseSensitive(areStringsCaseSensitive());
          if (isSetSupportsDuplicateFilterStrings())
            pool.setSupportsDuplicateFilterStrings(supportsDuplicateFilterStrings());
    	  // add to model
    	  java.util.List pools = getPools();
    	  pools.add(pool);
    	  //System.out.println("Inside createSFPool for mgr "+getName()+". Pool "+name+" added");
          invalidatePoolCache();
    	  // save to disk...
          commit(pool); 
          // if caller provider, callback to inform them of this event
          if ((caller != null) && !suspendCallbacks)
            caller.filterEventFilterPoolCreated(pool);
        }
        return pool;    	
    }
    
    /**
     * Delete a given filter pool. Dependending on the save policy, the 
     *  appropriate file or folder on disk will also be deleted.
     * <p>
     * Does the following:
     * <ul>
     *   <li>Removes all references
     *   <li>Removes pool object from in-memory model
     *   <li>Removes folder from disk for policies of one folder per pool
     *   <li>Removes file from disk for policy of one file per pool
     *   <li>Saves model to disk for policy of one file per manager
     *   <li>Invalidates in-memory caches
     *   <li>Calls back to inform caller of this event
     * </ul>
     * @param pool The filter pool object to physically delete
     */
    public void deleteSystemFilterPool(ISystemFilterPool pool)
      throws Exception
    {
    	
       
        
    	// remove all references
    	ISystemBaseReferencingObject[] refs = pool.getReferencingObjects();
    	//boolean needsSave = false;
    	if (refs != null)
    	{
    	  for (int idx=0; idx < refs.length; idx++)
    	  {
    	  	 if (refs[idx] instanceof ISystemFilterPoolReference)
    	  	 {
    	  	 	ISystemFilterPoolReference fpRef = (ISystemFilterPoolReference)refs[idx];
    	  	 	ISystemFilterPoolReferenceManager fprMgr = fpRef.getFilterPoolReferenceManager();
    	  	 	if (fprMgr != null)
    	  	 	  fprMgr.removeSystemFilterPoolReference(fpRef,false);// false means don't dereference
    	  	 }
    	  }
    	}    	
    	// DWD removing a pool should mark its parent profile as dirty and cause a save to be "scheduled"
   
    	// remove from model
    	java.util.List pools = getPools();
    	pools.remove(pool);
    	
    	/* FIXME
		// now in EMF, the pools are "owned" by the Resource, and only referenced by this pool manager,
		//  so I don't think just removing it from the manager is enough... it must also be removed from its
		//  resource. Phil.
		Resource res = pool.eResource();
		if (res != null)
		  res.getContents().remove(pool);
		  
    	// remove from disk
    	if ( (savePolicy == SystemFilterConstants.SAVE_POLICY_ONE_FILEANDFOLDER_PER_POOL) ||
    	     (savePolicy == SystemFilterConstants.SAVE_POLICY_ONE_FILE_PER_FILTER) )
    	{
    	  String expectedFolderName = derivePoolFolderName(poolName);
    	  if (expectedFolderName.equals(poolFolder.getName()))
    	  {
    	  	// folder name equals what we would have named it if left to us.
    	  	// assumption is this folder only exists to hold this pool!
    	  	if (poolFolder.exists())
    	  	  getResourceHelpers().deleteResource(poolFolder);
    	  }
    	}
    	else if (savePolicy == SystemFilterConstants.SAVE_POLICY_ONE_FILE_PER_POOL_SAME_FOLDER)
    	{
    	  String poolFileName = SystemFilterPoolImpl.getSaveFileName(getMOFHelpers(),pool);
    	  IFile poolFile = SystemResourceHelpers.getResourceHelpers().getFile(poolFolder,poolFileName);
          if (poolFile.exists())
    	  	getResourceHelpers().deleteResource(poolFile);            
    	}
    	else // all pools in one file per manager. Just save it
    	{
    	  commit();
    	}    	
        invalidatePoolCache(); 	
        // if caller provider, callback to inform them of this event
        if ((caller != null) && !suspendCallbacks)
          caller.filterEventFilterPoolDeleted(pool);        
          */
    }
    
    /**
     * Delete all existing filter pools. Call this when you are about to delete this manager, say.
     */
    public void deleteAllSystemFilterPools()
    {
        ISystemFilterPool[] allPools = getSystemFilterPools();
        for (int idx=0; idx<allPools.length; idx++)
        {
        	String name = allPools[idx].getName();
        	try {        	
        	  deleteSystemFilterPool(allPools[idx]);
        	} catch (Exception exc)
        	{
        	  logError("Exception deleting filter pool " + name + " from mgr " + getName(),exc);
        	}
        }
    }
    
    /**
     * Pre-test if we are going to run into any trouble renaming any of the files or folders
     *  used to persist a filter pool.
     * @return true if everything seems ok, false if a file/folder is in use.
     */
    public boolean preTestRenameFilterPool(ISystemFilterPool pool) throws Exception
    {
    	boolean ok = true;
    	/*
    	 * DWD this looks like it needs to be modified so that it queries the persistence
    	 * manager to see if the pool can be renamed. The provider is in charge of determining
    	 * pool names in the persistent form. The Manager will have to construct a DOM 
    	 * object for this pool and query the appropriate provider.
    	 */
    	/* FIXME
    	if ( (savePolicy == SystemFilterConstants.SAVE_POLICY_ONE_FILEANDFOLDER_PER_POOL) ||
    	     (savePolicy == SystemFilterConstants.SAVE_POLICY_ONE_FILE_PER_FILTER) )
    	{
    	    String expectedFolderName = derivePoolFolderName(pool.getName());
            boolean ourFolderName = expectedFolderName.equals(pool.getFolder().getName());
    	  	// we must rename the old file...
    	    String poolFileName = SystemFilterPoolImpl.getSaveFileName(getMOFHelpers(),pool);    	  
    	  	IFile poolFile = getResourceHelpers().getFile(pool.getFolder(),poolFileName);
    	  	IFolder poolFolder = pool.getFolder();
    	  	    
    	    // first, pre-test for file-in-use error:
            boolean inUse = poolFile.exists() && SystemResourceHelpers.testIfResourceInUse(poolFile);
            if (inUse)
            {
        	    SystemMessage msg = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_FILE_INUSE);
        	    msg.makeSubstitution(poolFile.getFullPath());
        	    throw new SystemMessageException(msg);
            }
    	    // next, pre-test for folder-in-use error:
    	    if (ourFolderName)
    	    {
                inUse = poolFolder.exists() && SystemResourceHelpers.testIfResourceInUse(poolFolder);
                if (inUse)
                {
        	        SystemMessage msg = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_FOLDER_INUSE);
        	        msg.makeSubstitution(poolFolder.getFullPath());
        	        throw new SystemMessageException(msg);
                }
            }    	            
    	}
    	else if (savePolicy == SystemFilterConstants.SAVE_POLICY_ONE_FILE_PER_POOL_SAME_FOLDER)
    	{
    	    String poolFileName = SystemFilterPoolImpl.getSaveFileName(getMOFHelpers(),pool);    	  
    	    IFile poolFile = getResourceHelpers().getFile(pool.getFolder(),poolFileName);
    	    // first, pre-test for file-in-use error:
            boolean inUse = poolFile.exists() && SystemResourceHelpers.testIfResourceInUse(poolFile);
            if (inUse)
            {
        	    SystemMessage msg = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_FILE_INUSE);
        	    msg.makeSubstitution(poolFile.getFullPath());
        	    throw new SystemMessageException(msg);
            }
    	}
    	*/
    	return ok;
    }
    
    /**
     * Rename a given filter pool. Dependending on the save policy, the 
     *  appropriate file or folder on disk will also be renamed.
     * <p>
     * Does the following:
     * <ul>
     *   <li>Updates all referencing objects
     *   <li>Renames pool object in the in-memory model
     *   <li>Renames folder on disk for policies of one folder per pool
     *   <li>Renames file on disk for policy of one file per pool
     *   <li>Saves model to disk for policy of one file per manager
     *   <li>Invalidates in-memory caches
     *   <li>Calls back to provider to inform of this event.
     * </ul>
     * @param pool The filter pool object to physically rename
     * @param newName The new name to give the pool
     */
    public void renameSystemFilterPool(ISystemFilterPool pool, String newName)
      throws Exception
    {
    	/*
    	 *  DWD Renaming a filter pool should mark its parent profile as dirty and
    	 *  the pool itself as dirty. A rewrite of the profile should be scheduled.
    	 */
    	
    	String oldName = pool.getName();
        // rename on disk
        /* FIXME
    	int oldLen = oldName.length();
    	int newLen = newName.length();
    	if ( (savePolicy == SystemFilterConstants.SAVE_POLICY_ONE_FILEANDFOLDER_PER_POOL) ||
    	     (savePolicy == SystemFilterConstants.SAVE_POLICY_ONE_FILE_PER_FILTER) )
    	{
    	    String expectedFolderName = derivePoolFolderName(pool.getName());
            boolean ourFolderName = expectedFolderName.equals(pool.getFolder().getName());
    	  	// we must rename the old file...
    	    String poolFileName = SystemFilterPoolImpl.getSaveFileName(getMOFHelpers(),pool);    	  
    	    String poolFileNewName = SystemFilterPoolImpl.getSaveFileName(getMOFHelpers(),pool,newName);
    	  	IFile poolFile = getResourceHelpers().getFile(pool.getFolder(),poolFileName);
    	  	IFolder poolFolder = pool.getFolder();
    	  	    
    	    // first, pre-test for file-in-use error:
            boolean inUse = poolFile.exists() && SystemResourceHelpers.testIfResourceInUse(poolFile);
            if (inUse)
            {
        	    SystemMessage msg = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_FILE_INUSE);
        	    msg.makeSubstitution(poolFile.getFullPath());
        	    throw new SystemMessageException(msg);
            }
    	    // next, pre-test for folder-in-use error:
    	    if (ourFolderName)
    	    {
                inUse = poolFolder.exists() && SystemResourceHelpers.testIfResourceInUse(poolFolder);
                if (inUse)
                {
        	        SystemMessage msg = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_FOLDER_INUSE);
        	        msg.makeSubstitution(poolFolder.getFullPath());
        	        throw new SystemMessageException(msg);
                }
            }
    	            
            if (poolFile.exists())
            {
    	      // pre-test if the new name will be too long for MOF (256)
    	      if (nameLenDiff > 0)
    	      {
    	        if (ourFolderName)
    	          nameLenDiff *= 2; // new name affects folder and file
    	        int newNameLen = poolFile.getLocation().toOSString().length() + nameLenDiff; 
    	        if (newNameLen > 256)
    	          throw new Exception("Fully qualified filter pool name too long for "+newName+". Exceeds 256 characters");
    	      }
    	  	  getResourceHelpers().renameResource(poolFile, poolFileNewName);            
            }
    	    if (ourFolderName)
    	    {
    	  	    // folder name equals what we would have named it if left to us.
    	  	    // assumption is this folder only exists to hold this pool!
    	  	    if (poolFolder.exists())
    	  	    {
    	  	        String newFolderName = derivePoolFolderName(newName);
    	  	        getResourceHelpers().renameResource(poolFolder, newFolderName);
    	  	        // as we now know, the original IFolder still points to the old name!
    	  	        poolFolder = getResourceHelpers().getRenamedFolder(poolFolder, newFolderName);
    	  	        pool.setFolder(poolFolder);
    	  	    }
    	  }
    	}
    	else if (savePolicy == SystemFilterConstants.SAVE_POLICY_ONE_FILE_PER_POOL_SAME_FOLDER)
    	{
    	    String poolFileName = SystemFilterPoolImpl.getSaveFileName(getMOFHelpers(),pool);    	  
    	    IFile poolFile = getResourceHelpers().getFile(pool.getFolder(),poolFileName);
    	    // first, pre-test for file-in-use error:
            boolean inUse = poolFile.exists() && SystemResourceHelpers.testIfResourceInUse(poolFile);
            if (inUse)
            {
        	    SystemMessage msg = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_FILE_INUSE);
        	    msg.makeSubstitution(poolFile.getFullPath());
        	    throw new SystemMessageException(msg);
            }
            if (poolFile.exists())
            {
    	        String poolFileNewName = SystemFilterPoolImpl.getSaveFileName(getMOFHelpers(),pool,newName);
    	  	    getResourceHelpers().renameResource(poolFile, poolFileNewName);            
            }
    	}
    	*/
    	pool.setName(newName);
        invalidatePoolCache(); 	
        
        // inform all referencees
    	ISystemBaseReferencingObject[] refs = pool.getReferencingObjects();
    	if (refs != null)
    	{
    	  for (int idx=0; idx < refs.length; idx++)
    	  {
    	  	 ISystemBaseReferencingObject ref = refs[idx];
    	  	 if (ref instanceof ISystemFilterPoolReference)
    	   	 {
    	  	 	ISystemFilterPoolReference fpRef = (ISystemFilterPoolReference)ref;
    	  	 	ISystemFilterPoolReferenceManager fprMgr = fpRef.getFilterPoolReferenceManager();
    	  	 	fprMgr.renameReferenceToSystemFilterPool(pool);
    	  	 }
    	  }
    	}    	        
        
        // if caller provider, callback to inform them of this event
        if ((caller != null) && !suspendCallbacks)
          caller.filterEventFilterPoolRenamed(pool,oldName);
    }

    /**
     * Copy the specified filter pool from this manager to this manager or another manager.
     * <p>
     * Does the following:
     * <ul>
     *   <li>Clones all filters within the pool
     *   <li>Clones all filter strings within each filter
     *   <li>Asks target manager to save to disk
     *   <li>Calls back to target manager provider, unless callbacks are suspended
     * </ul>
     * @param targetMgr The target manager to copy our filter pool to. Can be this manager, but target pool name must be unique.
     * @param oldPool The filter pool to copy
     * @param newName The new name to give the copied pool
     * @return the new copy
     */
    public ISystemFilterPool copySystemFilterPool(ISystemFilterPoolManager targetMgr, ISystemFilterPool oldPool, String newName)
      throws Exception
    {
        ISystemFilterPool newPool = targetMgr.createSystemFilterPool(newName, oldPool.isDeletable());
        //System.out.println("In SystemFilterPoolManagerImpl#copySystemFilterPool: newPool "+newName+" null? " + (newPool == null));
        oldPool.cloneSystemFilterPool(newPool); 
        commit(newPool); // save it all to disk
        return newPool;
    }

    /**
     * Move the specified filter pool from this manager to another manager.
     * <p>
     * Does the following:
     * <ul>
     *   <li>Performs a {@link #copySystemFilterPool(ISystemFilterPoolManager, ISystemFilterPool, String) copySystemFilterPool} operation.
     *   <li>If copy is successful, updates all references to reference the new copy.
     *   <li>If copy is successful, deletes original filter pool in this manager 
     *   <li>If this final delete fails, deletes the copied version and restore original references
     *   <li>Asks target manager to save to disk
     *   <li>Saves this manager to disk
     *   <li>Calls back to both targer manager provider and this manager provider, unless callbacks are suspended
     * </ul>
     * @param targetMgr The target manager to move our filter pool to. Cannot be this manager.
     * @param oldPool The filter pool to move
     * @param newName The new name to give the moved pool
     * @return the new copy of the moved system filter pool
     */
    public ISystemFilterPool moveSystemFilterPool(ISystemFilterPoolManager targetMgr, ISystemFilterPool oldPool, String newName)
      throws Exception
    {
        ISystemFilterPool newPool = copySystemFilterPool(targetMgr, oldPool, newName);
        // find all references to original, and reset them to reference the new...
    	ISystemBaseReferencingObject[] refs = oldPool.getReferencingObjects();
    	if (refs != null)
    	{
    	  for (int idx=0; idx < refs.length; idx++)
    	  {
    	  	 if (refs[idx] instanceof ISystemFilterPoolReference)
    	  	 {
    	  	 	ISystemFilterPoolReference fpRef = (ISystemFilterPoolReference)refs[idx];
    	  	 	//SystemFilterPool fp = fpRef.getReferencedFilterPool();
    	  	 	ISystemFilterPoolReferenceManager fprMgr = fpRef.getFilterPoolReferenceManager();
                fprMgr.resetSystemFilterPoolReference(fpRef, newPool); // reset the referenced pool   	  	 	
    	  	 }
    	  }
    	}        
        try
        {
            deleteSystemFilterPool(oldPool);  	            
        }
        catch (Exception exc)
        {
    	    if (refs != null)
    	    {
    	      for (int idx=0; idx < refs.length; idx++)
    	      {
    	  	     if (refs[idx] instanceof ISystemFilterPoolReference)
    	  	     {
    	  	 	   ISystemFilterPoolReference fpRef = (ISystemFilterPoolReference)refs[idx];
    	  	 	   ISystemFilterPoolReferenceManager fprMgr = fpRef.getFilterPoolReferenceManager();
                   fprMgr.resetSystemFilterPoolReference(fpRef, oldPool); // reset the referenced pool   	  	 	
    	  	     }
    	      }
    	    }                	
        	targetMgr.deleteSystemFilterPool(newPool);
        	throw exc;
        }
        return newPool;
    }

    /**
     * Copy all filter pools from this manager to another manager.
     * <p>
     * Does the following:
     * <ul>
     *   <li>Clones all filter pools
     *   <li>Clones all filters within each pool
     *   <li>Clones all filter strings within each filter
     *   <li>Asks target manager to save to disk
     *   <li>Does not callback to caller to fire events, assumes caller doesn't want to know
     * </ul>
     * @param targetMgr The target manager to copy our filter pools to
     */
    public void copySystemFilterPools(ISystemFilterPoolManager targetMgr)
      throws Exception
    {
    	targetMgr.setStringsCaseSensitive(areStringsCaseSensitive());
        ISystemFilterPool[] pools = getSystemFilterPools();
        if ((pools!=null) && (pools.length>0))
        {
          targetMgr.suspendCallbacks(true);         
          //boolean oldSuspendCallbacks = suspendCallbacks;
          for (int idx=0;idx<pools.length;idx++)
          {
          	 ISystemFilterPool pool = pools[idx];
             copySystemFilterPool(targetMgr, pool, pool.getName());          	 
          }
          //suspendCallbacks = oldSuspendCallbacks;
          targetMgr.suspendCallbacks(false);         
        }
    }
    
    /**
     * Given a filter pool name, return that filter pool object.
     * If not found, returns null.
     */
    public ISystemFilterPool getSystemFilterPool(String name)
    {
    	ISystemFilterPool pool = null;
    	ISystemFilterPool[] pools = getSystemFilterPools();
    	if (pools != null)
    	{
    	  for (int idx=0; (idx<pools.length) && (pool==null); idx++)
    	  {
    	     if (pools[idx].getName().equals(name))
    	       pool = pools[idx];
    	  }
    	}
    	return pool;
    }

    /**
     * Return the first pool that has the default attribute set to true.
     * If none found, returns null.
     */
    public ISystemFilterPool getFirstDefaultSystemFilterPool()
    {
    	ISystemFilterPool pool = null;
    	ISystemFilterPool[] pools = getSystemFilterPools();
    	for (int idx=0; (pool==null) && (idx<pools.length); idx++)
    	   if (pools[idx].isDefault())
    	     pool = pools[idx];
    	return pool;
    }
    // ---------------------------------
    // FILTER METHODS
    // ---------------------------------
    /**
     * Creates a new system filter within the given filter container (either a filter pool, or
     *  a filter). This creates the filter, and then saves the filter pool. 
     * <p>Calls back to inform provider of this event (filterEventFilterCreated)
     * @param parent The parent which is either a SystemFilterPool or a SystemFilter
     * @param aliasName The name to give the new filter. Must be unique for this pool.
     * @param filterStrings The list of String objects that represent the filter strings.
     */    
    public ISystemFilter createSystemFilter(ISystemFilterContainer parent,
                                           String aliasName, Vector filterStrings)
        throws Exception
    {
    	ISystemFilter newFilter = null;    	
        ISystemFilterPool parentPool = null;
        if (parent instanceof ISystemFilterPool)
          parentPool = (ISystemFilterPool)parent;
        else
          parentPool = ((ISystemFilter)parent).getParentFilterPool();        
    	newFilter = parent.createSystemFilter(aliasName,filterStrings);                          
    	if (!suspendSave)
    	  commit(parentPool);
        // if caller provider, callback to inform them of this event
        if ((caller != null) && !suspendCallbacks)
          caller.filterEventFilterCreated(newFilter);    	
    	return newFilter;
    } 
    /**
     * Creates a new system filter that is typed.
     * Same as {@link #createSystemFilter(ISystemFilterContainer, String, Vector)} but 
     *  takes a filter type as an additional parameter.
     * <p>
     * A filter's type is an arbitrary string that is not interpreted or used by the base framework. This
     * is for use entirely by tools who wish to support multiple types of filters and be able to launch unique
     * actions per type, say.
     * 
     * @param parent The parent which is either a SystemFilterPool or a SystemFilter
     * @param aliasName The name to give the new filter. Must be unique for this pool.
     * @param filterStrings The list of String objects that represent the filter strings.
     * @param type The type of this filter
     */    
    public ISystemFilter createSystemFilter(ISystemFilterContainer parent,
                                           String aliasName, Vector filterStrings, String type)
        throws Exception
    {    	
    	boolean oldSuspendSave = suspendSave;
    	boolean oldSuspendCallbacks = suspendCallbacks;
    	suspendSave = true;
    	suspendCallbacks = true;
    	    	    	
    	ISystemFilter newFilter = createSystemFilter(parent, aliasName, filterStrings);    	
    	newFilter.setType(type);

    	suspendSave = oldSuspendSave;
    	suspendCallbacks = oldSuspendCallbacks;    	    	

        if (!suspendSave)
        {    	
          ISystemFilterPool parentPool = null;
          if (parent instanceof ISystemFilterPool)
            parentPool = (ISystemFilterPool)parent;
          else
            parentPool = ((ISystemFilter)parent).getParentFilterPool();                  
    	  commit(parentPool);
        }
        // if caller provider, callback to inform them of this event
        if ((caller != null) && !suspendCallbacks)
          caller.filterEventFilterCreated(newFilter);    	
    	return newFilter;    	
    }
    /**
     * Creates a new system filter that is typed and promptable
     * Same as {@link #createSystemFilter(ISystemFilterContainer, String ,Vector, String)} but 
     *  takes a boolean indicating if it is promptable.
     * <p>
     * A promptable filter is one in which the user is prompted for information at expand time.
     * There is no base filter framework support for this, but tools can query this attribute and
     * do their own thing at expand time.
     * 
     * @param parent The parent which is either a SystemFilterPool or a SystemFilter
     * @param aliasName The name to give the new filter. Must be unique for this pool.
     * @param filterStrings The list of String objects that represent the filter strings.
     * @param type The type of this filter
     * @param promptable Pass true if this is a promptable filter
     */    
    public ISystemFilter createSystemFilter(ISystemFilterContainer parent,
                                           String aliasName, Vector filterStrings, String type, boolean promptable)
        throws Exception
    {
    	boolean oldSuspendSave = suspendSave;
    	boolean oldSuspendCallbacks = suspendCallbacks;
    	suspendSave = true;
    	suspendCallbacks = true;
    	    	    	
    	ISystemFilter newFilter = createSystemFilter(parent, aliasName, filterStrings, type);    	
    	newFilter.setPromptable(promptable);

    	suspendSave = oldSuspendSave;
    	suspendCallbacks = oldSuspendCallbacks;    	    	

        if (!suspendSave)
        {    	
          ISystemFilterPool parentPool = null;
          if (parent instanceof ISystemFilterPool)
            parentPool = (ISystemFilterPool)parent;
          else
            parentPool = ((ISystemFilter)parent).getParentFilterPool();                  
    	  commit(parentPool);
        }
        // if caller provider, callback to inform them of this event
        if ((caller != null) && !suspendCallbacks)
          caller.filterEventFilterCreated(newFilter);    	
    	return newFilter;    	
    }

    /**
     * Delete an existing system filter.
     * Does the following:
     * <ul>
     *   <li>Removes filter from its parent in memory.
     *   <li>If appropriate for the save policy, deletes the filter's file from disk.
     *   <li>Save the SystemFilterPool which direct or indirectly contains the filter.
     *   <li>Calls back to provider to inform it of this event (filterEventFilterDeleted)
     * </ul>
     */  
    public boolean deleteSystemFilter(ISystemFilter filter)
                   throws Exception
    {
  	
        // ok to proceed...
    	boolean ok = true;    	
        ISystemFilterContainer parent = filter.getParentFilterContainer();
        parent.deleteSystemFilter(filter);
    	commit(filter.getParentFilterPool());
    
        // if caller provider, callback to inform them of this event
        if ((caller != null) && !suspendCallbacks)
          caller.filterEventFilterDeleted(filter);    	    	
    	return ok;    	
    }     
    /**
     * Renames a filter. This is better than filter.setName(String newName) as it 
     *  saves the parent pool to disk.
     * <p>
     * Does the following:
     * <ul>
     *   <li>Renames the object in the in-memory cache
     *   <li>If appropriate for the save policy, rename's the filter's file on disk.
     *   <li>Save parent filter pool's in-memory object to disk.
     *   <li>Calls back to provider to inform it of this event (filterEventFilterRenamed)
     * </ul>
     * Does fire an event.
     */
    public void renameSystemFilter(ISystemFilter filter, String newName)
           throws Exception
    {
       
        // ok to proceed
        ISystemFilterContainer parent = filter.getParentFilterContainer();
        String oldName = filter.getName();
        parent.renameSystemFilter(filter, newName);
    	// rename on disk
    	try 
    	{
 
    	   commit(filter.getParentFilterPool());
    	} 
    	catch (Exception exc) 
    	{
    	  parent.renameSystemFilter(filter, oldName); // rollback name change
    	  throw exc;
    	}
        // if caller provider, callback to inform them of this event
        if ((caller != null) && !suspendCallbacks)
          caller.filterEventFilterRenamed(filter,oldName);    	    	
    }

    /**
     * Updates a filter. This is better than doing it directly as it saves it to disk.
     * <p>
     * Does the following:
     * <ul>
     *   <li>Updates the object in the in-memory cache
     *   <li>Save parent filter pool's in-memory object to disk.
     *   <li>Calls back to provider to inform it of this event (filterEventFilterUpdated).
     * </ul>
     */
    public void updateSystemFilter(ISystemFilter filter, String newName, String[] strings)
           throws Exception
    {
           
        // ok to proceed...
        ISystemFilterContainer parent = filter.getParentFilterContainer();
        String oldName = filter.getName();
        boolean rename = !oldName.equals(newName);
        if (rename)
        {
          renameSystemFilter(filter, newName);
        }
        parent.updateSystemFilter(filter, newName, strings);
        ISystemFilterPool parentPool = filter.getParentFilterPool();
    	commit(parentPool);
        if ((caller != null) && !suspendCallbacks)
          caller.filterEventFilterUpdated(filter);
    }

    /**
     * Sets a filter's type. This is better than calling filter.setType(String) directly as it saves the filter to disk after.
     * <p>
     * A filter's type is an arbitrary string that is not interpreted or used by the base framework. This
     * is for use entirely by tools who wish to support multiple types of filters and be able to launch unique
     * actions per type, say.
     * @param parent The parent which is either a SystemFilterPool or a SystemFilter
     * @param type The type of this filter
     */
    public void setSystemFilterType(ISystemFilter filter, String newType)
           throws Exception
    {
    	filter.setType(newType);
    	commit(filter.getParentFilterPool());
    }

    /**
     * Copy a system filter to a pool in this or another filter manager.
     */
    public ISystemFilter copySystemFilter(ISystemFilterPool targetPool, ISystemFilter oldFilter, String newName)
           throws Exception
    {
    	ISystemFilterPoolManager targetMgr = targetPool.getSystemFilterPoolManager();
        ISystemFilterPool oldPool = oldFilter.getParentFilterPool();

        targetMgr.suspendCallbacks(true);         

        ISystemFilter newFilter = oldPool.copySystemFilter(targetPool, oldFilter, newName); // creates it in memory
        commit(targetPool); // save updated pool to disk

        targetMgr.suspendCallbacks(false);                 

        targetMgr.getProvider().filterEventFilterCreated(newFilter);    	
        return newFilter;
    }
    /**
     * Move a system filter to a pool in this or another filter manager.
     * Does this by first copying the filter, and only if successful, deleting the old copy.
     */
    public ISystemFilter moveSystemFilter(ISystemFilterPool targetPool, ISystemFilter oldFilter, String newName)
           throws Exception
    {
    	ISystemFilter newFilter = copySystemFilter(targetPool, oldFilter, newName);
    	if (newFilter != null)
    	{
    	  deleteSystemFilter(oldFilter);
    	}
        return newFilter;
    }
    
    /**
     * Return the zero-based position of a SystemFilter object within its container
     */
    public int getSystemFilterPosition(ISystemFilter filter)
    {
    	ISystemFilterContainer container = filter.getParentFilterContainer();
    	int position = -1;
    	boolean match = false;    	
    	ISystemFilter[] filters = container.getSystemFilters();
    	
    	for (int idx = 0; !match && (idx<filters.length); idx++)
    	{
           if (filters[idx].getName().equals(filter.getName()))
           {
           	 match = true;
           	 position = idx;
           }
    	}
    	return position;
    }
    /**
     * Move existing filters a given number of positions in the same container.
     * If the delta is negative, they are all moved up by the given amount. If 
     * positive, they are all moved down by the given amount.<p>
     * <p>
     * Does the following:
     * <ul>
     * <li>After the move, the pool containing the filter is saved to disk.
     * <li>Calls back to provider to inform of this event
     * </ul>
     * @param filters Array of SystemFilters to move.
     * @param newPosition new zero-based position for the filters (filterEventFiltersRePositioned)
     */
    public void moveSystemFilters(ISystemFilter filters[], int delta)
        throws Exception
    {
    	/*
    	 * DWD revisit this. Make sure that the pool is scheduled to be saved.
    	 */
//    	ISystemFilterContainer container = filters[0].getParentFilterContainer(); 
    	int[] oldPositions = new int[filters.length];
    	for (int idx=0; idx<filters.length; idx++)
    	   oldPositions[idx] = getSystemFilterPosition(filters[idx]);
    	if (delta > 0) // moving down, process backwards
          for (int idx=filters.length-1; idx>=0; idx--)
             moveFilter(filters[idx], oldPositions[idx]+delta);	
        else
          for (int idx=0; idx<filters.length; idx++)
             moveFilter(filters[idx], oldPositions[idx]+delta);	        
             
        commit(filters[0].getParentFilterPool());

        // if caller provider, callback to inform them of this event
        if ((caller != null) && !suspendCallbacks)
          caller.filterEventFiltersRePositioned(filters, delta);    	    	        
    }
    
    /**
     * Move one filter to new zero-based position.
     */
    private void moveFilter(ISystemFilter filter, int newPos)
    {
    	ISystemFilterContainer container = filter.getParentFilterContainer();
        container.moveSystemFilter(newPos, filter);
    }
 
     /**
     * Order filters according to user preferences.
     * <p>
     * While the framework has all the code necessary to arrange filters and save/restore
     * that arrangement, you may choose to use preferences instead of this support.
     * In this case, call this method and pass in the saved and sorted filter name list.
     * <p>
     * Called by someone after restore.
     */
    public void orderSystemFilters(ISystemFilterPool pool, String[] names) throws Exception
    {
        pool.orderSystemFilters(names);
        commit(pool);        	
    }
    	
    // -------------------------------
    // SYSTEM FILTER STRING METHODS...
    // -------------------------------
    /**
     * Append a new filter string to the given filter's list
     * <p>
     * Does the following:
     * <ul>
     *   <li>Adds the filter string to the in-memory cache
     *   <li>Saves parent filter pool to disk.
     *   <li>Calls back to provider to inform it of this event (filterEventFilterStringCreated)
     * </ul>
     */
    public ISystemFilterString addSystemFilterString(ISystemFilter filter, String newString) throws Exception
    {
    	ISystemFilterString newFilterString = filter.addFilterString(newString);
        ISystemFilterPool parentPool = filter.getParentFilterPool();
    	commit(parentPool);
        if ((caller != null) && !suspendCallbacks)
          caller.filterEventFilterStringCreated(newFilterString);
        return newFilterString;
    }
    /**
     * Insert a new filter string to the its filters' list, at the given zero-based position
     * <p>
     * Does the following:
     * <ul>
     *   <li>Adds the filter string to the in-memory cache
     *   <li>Saves parent filter pool to disk.
     *   <li>Calls back to provider to inform it of this event (filterEventFilterStringCreated)
     * </ul>
     */
    public ISystemFilterString addSystemFilterString(ISystemFilter filter, String newString, int position) throws Exception
    {
    	ISystemFilterString newFilterString = filter.addFilterString(newString, position);    	
        ISystemFilterPool parentPool = filter.getParentFilterPool();
    	commit(parentPool);
        if ((caller != null) && !suspendCallbacks)
          caller.filterEventFilterStringCreated(newFilterString);
        return newFilterString;
    }
    /**
     * Delete a filter string from the given filter's list
     * <p>
     * Does the following:
     * <ul>
     *   <li>Removes the filter string from the in-memory cache
     *   <li>Saves parent filter pool to disk.
     *   <li>Calls back to provider to inform it of this event (filterEventFilterStringDeleted)
     * </ul>
     * @return true if given string was found and hence was deleted.
     */
    public boolean removeSystemFilterString(ISystemFilter filter, String oldString) throws Exception
    {
    	ISystemFilterString oldFilterString = filter.removeFilterString(oldString);
    	if (oldFilterString == null)
    	  return false;
        ISystemFilterPool parentPool = filter.getParentFilterPool();
    	commit(parentPool);
        if ((caller != null) && !suspendCallbacks)
          caller.filterEventFilterStringDeleted(oldFilterString);
    	return true;
    }
    /**
     * Remove a filter string from this filter's list, given its SystemFilterString object.
     * <p>
     * Does the following:
     * <ul>
     *   <li>Removes the filter string from the in-memory cache
     *   <li>Saves parent filter pool to disk.
     *   <li>Calls back to provider to inform it of this event (filterEventFilterStringDeleted)
     * </ul>
     * @return true if the given string existed and hence was deleted.
     */
    public boolean removeSystemFilterString(ISystemFilter filter, ISystemFilterString filterString) throws Exception
    {
    	boolean ok = filter.removeFilterString(filterString);
    	if (!ok)
    	  return false;
        ISystemFilterPool parentPool = filter.getParentFilterPool();
    	commit(parentPool);
        if ((caller != null) && !suspendCallbacks)
          caller.filterEventFilterStringDeleted(filterString);
    	return ok;    	
    }
    /**
     * Remove a filter string from the given filter's list, given its zero-based position
     * <p>
     * Does the following:
     * <ul>
     *   <li>Removes the filter string from the in-memory cache
     *   <li>Saves parent filter pool to disk.
     *   <li>Calls back to provider to inform it of this event (filterEventFilterStringDeleted)
     * </ul>
     * @return true if a string existed at the given position and hence was deleted.
     */
    public boolean removeSystemFilterString(ISystemFilter filter, int position) throws Exception
    {
    	ISystemFilterString oldFilterString = filter.removeFilterString(position);
    	if (oldFilterString == null)
    	  return false;
        ISystemFilterPool parentPool = filter.getParentFilterPool();
    	commit(parentPool);
        if ((caller != null) && !suspendCallbacks)
          caller.filterEventFilterStringDeleted(oldFilterString);
    	return true;
    }
    /**
     * Update a filter string's string vale
     * <p>
     * Does the following:
     * <ul>
     *   <li>Update the filter string in the in-memory cache
     *   <li>Saves parent filter pool to disk.
     *   <li>Calls back to provider to inform it of this event (filterEventFilterStringUpdated)
     * </ul>
     */
    public void updateSystemFilterString(ISystemFilterString filterString, String newValue) throws Exception
    {
    	if (newValue.equals(filterString.getString()))
    	  return;
    	ISystemFilter filter = filterString.getParentSystemFilter();
    	filter.updateFilterString(filterString, newValue);
        ISystemFilterPool parentPool = filter.getParentFilterPool();
    	commit(parentPool);
        if ((caller != null) && !suspendCallbacks)
          caller.filterEventFilterStringUpdated(filterString);
    }
    /**
     * Return the zero-based position of a SystemFilterString object within its filter
     */
    public int getSystemFilterStringPosition(ISystemFilterString filterString)
    {
    	ISystemFilter filter = filterString.getParentSystemFilter();
    	int position = -1;
    	boolean match = false;    	
    	ISystemFilterString[] filterStrings = filter.getSystemFilterStrings();
    	
    	String matchString = filterString.getString();
    	for (int idx = 0; !match && (idx<filterStrings.length); idx++)
    	{
           if (filterStrings[idx].getString().equals(matchString))
           {
           	 match = true;
           	 position = idx;
           }
    	}
    	return position;
    }
    /**
     * Copy a system filter string to a filter in this or another filter pool manager.
     */
    public ISystemFilterString copySystemFilterString(ISystemFilter targetFilter, ISystemFilterString oldFilterString)
           throws Exception
    {
    	/*
    	 * DWD revisit this. make sure that pool is persisted.
    	 */
        ISystemFilterPool       targetPool = targetFilter.getParentFilterPool();
    	ISystemFilterPoolManager targetMgr = targetPool.getSystemFilterPoolManager();
        ISystemFilter     oldFilter = oldFilterString.getParentSystemFilter();
//        ISystemFilterPool oldPool = oldFilter.getParentFilterPool(); 

        targetMgr.suspendCallbacks(true);         

        ISystemFilterString newFilterString = oldFilter.copySystemFilterString(targetFilter, oldFilterString); // creates it in memory
        commit(targetPool); // save updated pool to disk

        targetMgr.suspendCallbacks(false);                 

        targetMgr.getProvider().filterEventFilterStringCreated(newFilterString);    	
        return newFilterString;
    }
    /**
     * Move a system filter string to a filter in this or another filter pool manager.
     * Does this by doing a copy operation, then if successful doing a delete operation.
     */
    public ISystemFilterString moveSystemFilterString(ISystemFilter targetFilter, ISystemFilterString oldFilterString)
           throws Exception
    {
    	ISystemFilterString newString = copySystemFilterString(targetFilter, oldFilterString);
    	if (newString != null)
    	{
    	  removeSystemFilterString(oldFilterString.getParentSystemFilter(), oldFilterString);
    	}
        return newString;
    }

    /**
     * Move existing filter strings a given number of positions in the same filter
     * If the delta is negative, they are all moved up by the given amount. If 
     * positive, they are all moved down by the given amount.<p>
     * <p>
     * Does the following:
     * <ul>
     *   <li>After the move, the filter pool containing the filter containing the filter strings is saved to disk.
     *   <li>Calls back to provider to inform of the event (filterEventFilterStringsRePositioned)
     * </ul>
     * @param filterStrings Array of SystemFilterStrings to move.
     * @param newPosition new zero-based position for the filter strings
     */
    public void moveSystemFilterStrings(ISystemFilterString filterStrings[], int delta)
              throws Exception
    {
    	ISystemFilter filter = filterStrings[0].getParentSystemFilter();
    	int[] oldPositions = new int[filterStrings.length];
    	for (int idx=0; idx<filterStrings.length; idx++)
    	   oldPositions[idx] = getSystemFilterStringPosition(filterStrings[idx]);
    	if (delta > 0) // moving down, process backwards
          for (int idx=filterStrings.length-1; idx>=0; idx--)
             moveFilterString(filter, filterStrings[idx], oldPositions[idx]+delta);	
        else
          for (int idx=0; idx<filterStrings.length; idx++)
             moveFilterString(filter, filterStrings[idx], oldPositions[idx]+delta);	        
             
        commit(filter.getParentFilterPool());

        // if caller provider, callback to inform them of this event
        if ((caller != null) && !suspendCallbacks)
          caller.filterEventFilterStringsRePositioned(filterStrings, delta);  
    }
    
    /**
     * Move one filter string to new zero-based position.
     */
    private void moveFilterString(ISystemFilter filter, ISystemFilterString filterString, int newPos)
    {
        filter.moveSystemFilterString(newPos, filterString);   	
    }

    // -----------------------------------
    // SUSPEND/RESUME CALLBACKS METHODS...
    // -----------------------------------
    /**
     * Suspend callbacks to the provider
     */
    public void suspendCallbacks(boolean suspend)
    {
    	suspendCallbacks = suspend;
    }

    // -----------------------
    // SAVE/RESTORE METHODS...
    // -----------------------
    /**
     * Return the save file that will be written for the given filter pool.
     * Will depend on this manager's save policy.
     */
    public IFile getSaveFile(ISystemFilterPool pool)
    {
    	/* FIXME
        switch(savePolicy)
        {
          // ONE FILE PER FILTER POOL MANAGER
          case SystemFilterConstants.SAVE_POLICY_ONE_FILE_PER_MANAGER:
              return SystemMOFHelpers.getSaveFile(getFolder(), getRootSaveFileName(this));
          // ONE FOLDER AND FILE PER FILTER POOL
          case SystemFilterConstants.SAVE_POLICY_ONE_FILEANDFOLDER_PER_POOL:
          // ONE FILE PER FILTER POOL, ONE FOLDER PER MANAGER
          case SystemFilterConstants.SAVE_POLICY_ONE_FILE_PER_POOL_SAME_FOLDER:
          // ONE FILE PER FILTER
          case SystemFilterConstants.SAVE_POLICY_ONE_FILE_PER_FILTER:
              return ((SystemFilterPoolImpl)pool).getSaveFile();
        }    	
        */    	
        return null;
    }

    
    /**
     * Save all the filter pools to disk.     
     * Uses the save policy specified in this manager's factory method.
     */
    public boolean commit()       
    {
    	IRSEPersistenceManager mgr = RSEUIPlugin.getThePersistenceManager();
    	
    	return mgr.commit(this); 
    }    
    
  
    /**
     * Save all the filter pools to disk.     
     * Uses the save policy specified in this manager's factory method.
     */
    public boolean commit(ISystemFilterPool pool)       
    {
    	IRSEPersistenceManager mgr = RSEUIPlugin.getThePersistenceManager();
    	return mgr.commit(pool);   
    }    
 

   
    
    /**
     * Restore filter pools when all are stored in one file
     * @param logger The logging object to log errors to
     * @param mgrFolder The folder containing the file to restore from.
     * @param name The name of the manager, from which the file name is derived.
     * @param namingPolicy Naming prefix information for persisted data file names.
     */
    protected static ISystemFilterPoolManager restoreFromOneFile(Logger logger, IFolder mgrFolder, String name, ISystemFilterNamingPolicy namingPolicy)
       throws Exception
    {
    	/* FIXME
        String fileName = getRootSaveFileName(namingPolicy, name);
        
    	java.util.List ext = getMOFHelpers(logger).restore(mgrFolder,fileName);
    	
        SystemFilterPoolManager mgr = null;

        // should be exactly one system filter pool manager...
        Iterator iList = ext.iterator();
        mgr = (SystemFilterPoolManager)iList.next();
        return mgr;	
        */
    	return null;
    }




    /**
     * Return our logger
     */
    public Logger getLogger()
    {
    	if (logger == null)
    	  logger = RSEUIPlugin.getDefault().getLogger();
    	return logger;
    }
    
    /**
     * Set our logger
     */
    public void setLogger(Logger logger)
    {
    	this.logger = logger;
    }

    /**
	 * Helper method for logging information
	 * 
	 * @param message Message to be written to the log file
     */
    public void logInfo(String message) 
    {
    	if (logger != null)
    	  logger.logInfo(message);
    	else
    	  System.out.println(getClass().getName() + ": INFO: " + message);
    }


    /**
	 * Helper method for logging warnings
	 * 
	 * @param message Message to be written to the log file
     */
    public void logWarning(String message) 
    {
    	if (logger != null)
    	  logger.logWarning(message);
    	else
    	  System.out.println(getClass().getName() + ": WARNING: " + message);
    }
    
    
    /**
	 * Helper method for logging errors (exceptions)
	 * 
	 * @param message Message to be written to the log file
	 * 
	 * @param exception Any exception that generated the error condition, 
	 *                  this will be used to print a stack trace in the log file.
     */
    public void logError(String message, Throwable exception) 
    {
        if (logger != null)
    	  logger.logError(message, exception);
    	else
    	{
          String msg = exception.getMessage();
          if (msg == null)
            msg = exception.getClass().getName();
          System.out.println(getClass().getName() + ": " + message + ": " + msg);
          exception.printStackTrace();
    	}
    }


    /**
	 * Helper method for logging debug messages
	 * 
	 * @param classname Class issuing the debug message.  Pass in either 
	 *                  retieved using this.getClass() (for non-static methods)
	 *                  or using MyClass.class (for static methods)
	 * 
	 * @param message Message to be written to the log file
     */
	public void logDebugMessage(String prefix, String message) 
	{		
		if ((logger!=null))
		{
			logger.logDebugMessage(prefix, message);
		}
    	else
    	  System.out.println(getClass().getName() + ": DEBUG: " + message);
	}
		     
    /**
	 * Helper method for logging trace information
	 * 
	 * @deprecated Use either logInfo, logWarning, logError, or logDebugMessage.  This
	 * method now calls logInfo.
	 */
    public void logMessage(String msg)
    {
        if (logger != null)
    	  logger.logInfo(msg);
    	else
    	  System.out.println(getClass().getName() + ": " + msg);
    }
    

    // OTHER
        
    public String toString()
    {
    	return getName();
    }
	/**
	 * @generated This field/method will be replaced during code generation 
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * @generated This field/method will be replaced during code generation 
	 */
	public boolean isSupportsNestedFilters()
	{
		return supportsNestedFilters;
	}

	/**
	 * @generated This field/method will be replaced during code generation 
	 */
	public java.util.List getPools()
	{
		if (pools == null)
		{
			pools = new ArrayList();
			//FIXME new EObjectResolvingeList(SystemFilterPool.class, this, FiltersPackage.SYSTEM_FILTER_POOL_MANAGER__POOLS);
		}
		return pools;
	}

	/**
	 * @generated This field/method will be replaced during code generation.
	 */
	public void setNameGen(String newName)
	{
		String oldName = name;
		if (oldName != newName)
		{
			name = newName;
			setDirty(true);
		}
	}

	/**
	 * @generated This field/method will be replaced during code generation.
	 */
	public void setSupportsNestedFiltersGen(boolean newSupportsNestedFilters)
	{
		supportsNestedFilters = newSupportsNestedFilters;
	}

	/**
	 * @generated This field/method will be replaced during code generation 
	 */
	public boolean isSupportsDuplicateFilterStrings()
	{
		return supportsDuplicateFilterStrings;
	}

	/**
	 * @generated This field/method will be replaced during code generation.
	 */
	public void unsetSupportsDuplicateFilterStrings()
	{
		supportsDuplicateFilterStrings = SUPPORTS_DUPLICATE_FILTER_STRINGS_EDEFAULT;
		supportsDuplicateFilterStringsESet = false;
	}

	/**
	 * @generated This field/method will be replaced during code generation.
	 */
	public boolean isSetSupportsDuplicateFilterStrings()
	{
		return supportsDuplicateFilterStringsESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isSingleFilterStringOnly()
	{
		return singleFilterStringOnly;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setSingleFilterStringOnly(boolean newSingleFilterStringOnly)
	{
		boolean oldSingleFilterStringOnly = singleFilterStringOnly;
		if (oldSingleFilterStringOnly != newSingleFilterStringOnly)
		{
			singleFilterStringOnly = newSingleFilterStringOnly;
			setDirty(true);
		}
	}
	
	public boolean isDirty()
	{
		return _isDirty;
	}
	
	public void setDirty(boolean flag)
	{
		if (_isDirty != flag)
		{
			_isDirty = flag;
		}		
	}

	public boolean wasRestored() 
	{
		return _wasRestored;
	}
	
	public void setWasRestored(boolean flag) 
	{
		_wasRestored = flag;
	}

}
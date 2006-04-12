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
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.rse.core.SystemResourceHelpers;
import org.eclipse.rse.filters.ISystemFilter;
import org.eclipse.rse.filters.ISystemFilterConstants;
import org.eclipse.rse.filters.ISystemFilterContainer;
import org.eclipse.rse.filters.ISystemFilterNamingPolicy;
import org.eclipse.rse.filters.ISystemFilterPool;
import org.eclipse.rse.filters.ISystemFilterPoolManager;
import org.eclipse.rse.filters.ISystemFilterPoolManagerProvider;
import org.eclipse.rse.filters.ISystemFilterSavePolicies;
import org.eclipse.rse.internal.references.SystemPersistableReferencedObject;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemResources;

/**
 * This is a system filter pool, which is a means of grouping filters
 * and managing them as a list.
 * <p>
 * To enable filters themselves to be automous and sharable, it is decided
 *  that no data will be persisted in the filter pool itself. Rather, all
 *  attributes other than the list of filters are transient and as such it is
 *  the responsibility of the programmer using a filter pool to set these
 *  attributes after creating or restoring a filter pool. Typically, this is
 *  what a filter pool manager (SystemFilterPoolManager) will do for you.
 */
/** 
 * @lastgen class SystemFilterPoolImpl extends SystemPersistableReferencedObjectImpl implements SystemFilterPool, SystemFilterSavePolicies, SystemFilterConstants, SystemFilterContainer, IAdaptable
 */
public class SystemFilterPool extends SystemPersistableReferencedObject 
       implements ISystemFilterPool, ISystemFilterSavePolicies, ISystemFilterConstants, ISystemFilterContainer, IAdaptable
{
	
	/**
	 * The default value of the '{@link #getName() <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getName()
	 * @generated
	 * @ordered
	 */
	protected static final String NAME_EDEFAULT = null;

    private String name;
	/**
	 * The default value of the '{@link #getType() <em>Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getType()
	 * @generated
	 * @ordered
	 */
	protected static final String TYPE_EDEFAULT = null;

    private int savePolicy;
    private ISystemFilterNamingPolicy namingPolicy = null;
    private ISystemFilterPoolManager mgr;
    //private Vector filters = new Vector();
    private SystemFilterContainerCommonMethods helpers = null;
    private Object filterPoolData = null;
    private boolean initialized = false;
    //private boolean isSharable = false;
    protected boolean specialCaseNoDataRestored = false;
    private boolean debug = false;
	protected static final String DELIMITER = SystemFilterPoolReference.DELIMITER;        	
	
	// persistence
	protected boolean _isDirty = true;
	protected boolean _wasRestored = false;
	
	/**
	 * @generated This field/method will be replaced during code generation.
	 */
	protected String type = TYPE_EDEFAULT;
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
	 * The default value of the '{@link #isDeletable() <em>Deletable</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isDeletable()
	 * @generated
	 * @ordered
	 */
	protected static final boolean DELETABLE_EDEFAULT = false;

	/**
	 * @generated This field/method will be replaced during code generation.
	 */
	protected boolean deletable = DELETABLE_EDEFAULT;
	/**
	 * The default value of the '{@link #isDefault() <em>Default</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isDefault()
	 * @generated
	 * @ordered
	 */
	protected static final boolean DEFAULT_EDEFAULT = false;

	/**
	 * @generated This field/method will be replaced during code generation.
	 */
	protected boolean default_ = DEFAULT_EDEFAULT;
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
	 * This is true if the Strings Case Sensitive attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean stringsCaseSensitiveESet = false;

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
	 * The default value of the '{@link #getRelease() <em>Release</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getRelease()
	 * @generated
	 * @ordered
	 */
	protected static final int RELEASE_EDEFAULT = 0;

	/**
	 * @generated This field/method will be replaced during code generation.
	 */
	protected int release = RELEASE_EDEFAULT;
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
	 * This is true if the Single Filter String Only attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean singleFilterStringOnlyESet = false;

	/**
	 * The default value of the '{@link #getOwningParentName() <em>Owning Parent Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getOwningParentName()
	 * @generated
	 * @ordered
	 */
	protected static final String OWNING_PARENT_NAME_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getOwningParentName() <em>Owning Parent Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getOwningParentName()
	 * @generated
	 * @ordered
	 */
	protected String owningParentName = OWNING_PARENT_NAME_EDEFAULT;

	/**
	 * The default value of the '{@link #isNonRenamable() <em>Non Renamable</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isNonRenamable()
	 * @generated
	 * @ordered
	 */
	protected static final boolean NON_RENAMABLE_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isNonRenamable() <em>Non Renamable</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isNonRenamable()
	 * @generated
	 * @ordered
	 */
	protected boolean nonRenamable = NON_RENAMABLE_EDEFAULT;

	/**
	 * @generated This field/method will be replaced during code generation.
	 */
	protected java.util.List filters = null;
/**
	 * Default constructor
	 */
	protected SystemFilterPool() 
	{
		super();
		helpers = new SystemFilterContainerCommonMethods();
	}
	/**
	 * Static factory method for creating a new filter pool. Will
	 * first try to restore it, and if that fails will create a new instance and
	 * return it.
	 * <p>
	 * Use this method only if you are not using a SystemFilterPoolManager, else
	 *  use the createSystemFilterPool method in that class.
	 * 
     * @param mofHelpers SystemMOFHelpers object with helper methods for saving and restoring via mof
     * @param poolFolder the folder that will hold the filter pool.
     *   This folder will be created if it does not already exist.
     * @param name the name of the filter pool. Typically this is also the name
     *   of the given folder, but this is not required. For the save policy of one file
     *   per pool, the name of the file is derived from this.
     * @param allowNestedFilters true if filters inside this filter pool are
     *   to allow nested filters. 
     * @param isDeletable true if this filter pool is allowed to be deleted by users.
     * @param tryToRestore true to attempt a restore first, false if a pure create operation.
     * @param savePolicy The save policy for the filter pool and filters. One of the
     *   following constants from the SystemFilterConstants interface:
     *   <ul>
     *     <li>SAVE_POLICY_ONE_FILEANDFOLDER_PER_POOL - one file and folder per pool
     *     <li>SAVE_POLICY_ONE_FILE_PER_POOL_SAME_FOLDER - one file per pool, all files in one folder
     *     <li>SAVE_POLICY_ONE_FILE_PER_FILTER - one file per filter, one folder per pool
     *   </ul> 
     * @param namingPolicy The names to use for file and folders when persisting to disk. Pass
     *     null to just use the defaults.
	 */
	public static ISystemFilterPool createSystemFilterPool(
	                                                      String name,
	                                                      boolean allowNestedFilters, 
	                                                      boolean isDeletable,
	                                                      boolean tryToRestore)
	{    	  
     	  
     	  
    	SystemFilterPool pool = null;
    	if (tryToRestore)
    	{
    	  try
    	  {
             pool = (SystemFilterPool)RSEUIPlugin.getThePersistenceManager().restoreFilterPool(name);
    	  }
    	  catch (Exception exc) // real error trying to restore, versus simply not found.
    	  {
    	     // todo: something? Log the exception somewhere?
    	  }
    	}
        if (pool == null) // not found or some serious error.
        {
    	  pool = createPool();    	
        }
    	if (pool != null)
    	{    	   
    	  pool.initialize(name, allowNestedFilters, isDeletable);
    	}
    	return pool;
	}
	
	// temporary!
	//public boolean isSharable() {return isSharable; }
	//public void setIsSharable(boolean is) { isSharable = is; }

    /*
     * Private helper method.
     * Uses MOF to create an instance of this class.
     */
    protected static SystemFilterPool createPool()
    {
    	ISystemFilterPool pool = new SystemFilterPool();
    		// FIXME SystemFilterImpl.initMOF().createSystemFilterPool();
    	pool.setRelease(SystemResources.CURRENT_RELEASE);
    	return (SystemFilterPool)pool;    	
    }

    /*
     * Private helper method to initialize attributes
     */
	protected void initialize(String name,
	                          boolean allowNestedFilters, 
	                          boolean isDeletable)
	{
		if (!initialized)
		  initialize(name, savePolicy, namingPolicy);
        setDeletable(isDeletable); // mof attribute
		//System.out.println("In initialize() for filter pool " + getName() + ". isDeletable= " + isDeletable);        
        setSupportsNestedFilters(allowNestedFilters); // cascades to each filter
	}

    /*
     * Private helper method to core initialization, from either createXXX or restore.
     */
	protected void initialize(String name,
	                          int savePolicy, ISystemFilterNamingPolicy namingPolicy)
	{
        setName(name); // mof attribute
        setSavePolicy(savePolicy);
        setNamingPolicy(namingPolicy);
        
        initialized = true;
	}


	//protected Vector internalGetFilters()
	protected java.util.List internalGetFilters()	
	{
		//return filters;
		return getFilters(); // mof-supplied in parent class
	}

    /**
     * Return the caller which instantiated the filter pool manager overseeing this filter framework instance
     */
    public ISystemFilterPoolManagerProvider getProvider()
    {
    	ISystemFilterPoolManager mgr = getSystemFilterPoolManager();
    	if (mgr != null)
    	  return mgr.getProvider();
    	else
    	  return null;
    }
    	
	/**
	 * Set the save file policy. See constants in {@link org.eclipse.rse.filters.ISystemFilterConstants SystemFilterConstants}. 
	 * One of:
     * <ul>
     *   <li>SAVE_POLICY_ONE_FILEANDFOLDER_PER_POOL - one file and folder per pool
     *   <li>SAVE_POLICY_ONE_FILE_PER_POOL_SAME_FOLDER - one file per pool, all files in one folder
     *   <li>SAVE_POLICY_ONE_FILE_PER_FILTER - one file per filter, one folder per pool
     * </ul> 
	 * This method is called by the SystemFilterPoolManager.
	 */
	public void setSavePolicy(int policy)
	{
		if (this.savePolicy != policy)
		{
			this.savePolicy = policy;
			setDirty(true);
		}
	}

    /**
     * Set the naming policy used when saving data to disk.
     * @see org.eclipse.rse.filters.ISystemFilterNamingPolicy
     */
    public void setNamingPolicy(ISystemFilterNamingPolicy namingPolicy)
    {
    	if (this.namingPolicy != namingPolicy)
    	{
    		this.namingPolicy = namingPolicy;
    		setDirty(true);
    	}
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
     * Set whether filters in this pool support nested filters.
     * Important to note this is stored in every filter as well as this filter pool.
     */
	public void setSupportsNestedFilters(boolean supports)
    {
    	this.setSupportsNestedFiltersGen(supports);
    	ISystemFilter[] filters = getSystemFilters();
    	if (filters != null)
    	  for (int idx=0; idx<filters.length; idx++)
    	     filters[idx].setSupportsNestedFilters(supports);   	
    }
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setSupportsNestedFiltersGen(boolean newSupportsNestedFilters)
	{
		supportsNestedFilters = newSupportsNestedFilters;
	}

    /**
     * Set whether filters in this pool support duplicate filter strings.
     * Important to note this is stored in every filter as well as this filter pool.
     */
    public void setSupportsDuplicateFilterStrings(boolean supports)
    {
    	setSupportsDuplicateFilterStringsGen(supports); 
    	ISystemFilter[] filters = getSystemFilters();
    	if (filters != null)
    	  for (int idx=0; idx<filters.length; idx++)
    	     filters[idx].setSupportsDuplicateFilterStrings(supports);   	
    }
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setSupportsDuplicateFilterStringsGen(boolean newSupportsDuplicateFilterStrings)
	{
		supportsDuplicateFilterStrings = newSupportsDuplicateFilterStrings;
	}

    /**
     * Set whether filters in this pool support case-sensitive filter strings.
     * Important to note this is stored in every filter as well as this filter pool.
     */
	public void setStringsCaseSensitive(boolean supports)
    {
    	this.setStringsCaseSensitiveGen(supports); 
    	ISystemFilter[] filters = getSystemFilters();
    	if (filters != null)
    	  for (int idx=0; idx<filters.length; idx++)
    	     filters[idx].setStringsCaseSensitive(supports);   	
    }
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setStringsCaseSensitiveGen(boolean newStringsCaseSensitive)
	{
		stringsCaseSensitive = newStringsCaseSensitive;
		stringsCaseSensitiveESet = true;
	}

    /**
     * Set the filter pool manager. Called by SystemFilterPoolManager
     */
    public void setSystemFilterPoolManager(ISystemFilterPoolManager mgr)
    {
    	this.mgr = mgr;
    }    
    /**
     * Return the filter pool manager managing this collection of filter pools and their filters.
     */
    public ISystemFilterPoolManager getSystemFilterPoolManager()
    {
    	return mgr;
    }

	/**
	 * While setData is for global data to set in all objects in the filter framework,
	 * this is to set transient data that only the filter pool holds.
	 */
	public void setSystemFilterPoolData(Object data)
	{
		this.filterPoolData = data;
	}
	
	/**
	 * Return transient data set via setFilterPoolData.
	 */
	public Object getSystemFilterPoolData()
	{
		return filterPoolData;
	}
	
	public String getId()
	{
		return getProvider().getId();
	}
	
	/**
	 * @see ISystemFilterPool#getName()
	 */
	public String getName() 
	{
		return name;
	}
	
	public String getDescription() 
	{
		return SystemResources.RESID_MODELOBJECTS_FILTERPOOL_DESCRIPTION;
	}
	
	/**
	 * Set the name of this filter pool.
	 */
	public void setName(String name) 
	{
		if (this.name == null || !this.name.equals(name) )
		{
			this.name = name;
			setDirty(true);
		}
	}

	/**
     * Returns the type attribute. Intercepted to return SystemFilterConstants.DEFAULT_TYPE if it is currently null
	 * Allows tools to have typed filter pools
     */
	public String getType()
    {
    	String type = this.getTypeGen();
    	if (type == null)
    	  return ISystemFilterConstants.DEFAULT_TYPE;
    	else
    	  return type;
    }
	/**
	 * @generated This field/method will be replaced during code generation 
	 * Allows tools to have typed filter pools
	 */
	public String getTypeGen()
	{
		return type;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isDeletable()
	{
		return deletable;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setDeletable(boolean newDeletable)
	{
		deletable = newDeletable;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isDefault()
	{
		if (!default_)
		{
			default_ = (getType().equals(DEFAULT_TYPE));
		}
		return default_;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setDefault(boolean newDefault)
	{
		default_ = newDefault;
	}

    /**
     * Does this support nested filters? Calls mof-generated isSupportsNestedFilters.
     */
    public boolean supportsNestedFilters()
    {
    	return isSupportsNestedFilters();
    }
    /**
     * Does this support duplicate filter strings? Calls mof-generated isSupportsDuplicateFilterStrings.
     */
    public boolean supportsDuplicateFilterStrings()
    {
    	return isSupportsDuplicateFilterStrings();
    }
	/**
	 * @return The value of the StringsCaseSensitive attribute
	 * Are filter strings in this filter case sensitive?
	 * If not set locally, queries the parent filter pool manager's atttribute.
	 */
	public boolean isStringsCaseSensitive()
	{
		if (!isSetStringsCaseSensitive())
		  return getSystemFilterPoolManager().isStringsCaseSensitive();
		else
		  return stringsCaseSensitive;
	}
	/**
	 * Same as isStringsCaseSensitive()
	 * @return The value of the StringsCaseSensitive attribute
	 */
	public boolean areStringsCaseSensitive()
	{
		return isStringsCaseSensitive();	
	}
    /**
     * Clone this filter pools' attributes and filters into another filter pool.
     * Assumes the core attributes were already set when filter pool was created:
     * <ul>
     *   <li>Name
     *   <li>Deletable
     *   <li>Supports nested filters
     *   <li>Manager
     * </ul>
     * Attributes we clone:
     * <ul>
     *   <li>Data
     *   <li>Type
     *   <li>Default
     *   <li>Case sensitive
     *   <li>Supports duplicate filter strings
     *   <li>Release
     * </ul>
     */
    public void cloneSystemFilterPool(ISystemFilterPool targetPool)
           throws Exception
    {
    	//System.out.println("In SystemFilterPoolImpl#cloneSystemFilterPool. targetPool null? " + (targetPool == null));
    	if (filterPoolData != null)
    	  targetPool.setSystemFilterPoolData(filterPoolData);

    	//String ourType = getTypeGen();
    	//if (ourType != null)
        //  targetPool.setType(ourType);
        targetPool.setType(getType());
        
       	targetPool.setDeletable(isDeletable());
       	targetPool.setSupportsNestedFilters(isSupportsNestedFilters());

    	//Boolean ourDefault = getDefault();
    	//if (ourDefault != null)
        //  targetPool.setDefault(ourDefault);
        targetPool.setDefault(isDefault());
           
        targetPool.setSupportsDuplicateFilterStrings(supportsDuplicateFilterStrings());
        targetPool.setRelease(getRelease());
        //targetPool.setNonDeletable(isNonDeletable());
        targetPool.setNonRenamable(isNonRenamable());
        targetPool.setOwningParentName(getOwningParentName());
        if (isSetSingleFilterStringOnly())
          targetPool.setSingleFilterStringOnly(isSingleFilterStringOnly());
        if (isSetStringsCaseSensitive())
          targetPool.setStringsCaseSensitive(isStringsCaseSensitive());
          
          
        ISystemFilter[] filters = getSystemFilters();
        if ((filters!=null) && (filters.length>0))
        {
          for (int idx=0; idx<filters.length; idx++)
          {
    		ISystemFilter newFilter = copySystemFilter(targetPool, filters[idx], filters[idx].getName());
          }
        }
    }
    /**
     * Copy a system filter to this or another filter pool.
     */
    public ISystemFilter copySystemFilter(ISystemFilterPool targetPool, ISystemFilter oldFilter, String newName)
           throws Exception
    {
    	ISystemFilter newFilter = targetPool.createSystemFilter(newName, null);
    	oldFilter.clone(newFilter);
    	return newFilter;
    }
	/**
	 * Return filters in this pool, as an array.
	 */
	public ISystemFilter[] getSystemFilters()
	{
		return helpers.getSystemFilters(internalGetFilters());
	}
    /**
     * From SystemFilterContainer.
     * Returns "this".
     */
    public ISystemFilterPool getSystemFilterPool()
    {
    	return this;
    }

    /**
     * Creates a new system filter within this pool. 
     * This filter will inherit/store the following attributes from this pool:
     * <ul>
     *   <li>supportsNestedFilters
     *   <li>supportsDuplicateFilterStrings
     *   <li>stringsCaseSensitive
     * </ul>
     * @param aliasName The name to give the new filter. Must be unique for this pool.
     * @param filterStrings The list of String objects that represent the filter strings.
     */    
    public ISystemFilter createSystemFilter(String aliasName, Vector filterStrings)
    {    	
    	ISystemFilter newFilter = helpers.createSystemFilter(internalGetFilters(), this, aliasName, filterStrings);
    	newFilter.setSupportsNestedFilters(supportsNestedFilters());
    	newFilter.setStringsCaseSensitive(areStringsCaseSensitive());
    	newFilter.setSupportsDuplicateFilterStrings(supportsDuplicateFilterStrings());
    	setDirty(true);
    	return newFilter;
    }

    /**
     * Return Vector of String objects: the names of existing filters in this container.
     * Needed by name validators for New and Rename actions to verify new name is unique.
     */
    public Vector getSystemFilterNames()
    {
    	return helpers.getSystemFilterNames(internalGetFilters());
    }
    
    /**
     * Return the nested filters as a Vector
     */
    public Vector getSystemFiltersVector()
    {
    	return helpers.getSystemFiltersVector(internalGetFilters());
    }
    /**
     * Return how many filters are defined in this filter container
     */
    public int getSystemFilterCount()
    {    	
    	return helpers.getSystemFilterCount(internalGetFilters());    	
    }      
    /**
     * Return a filter object, given its aliasname.
     * Can be used to test if an aliasname is already used (non-null return).
     * @param aliasName unique aliasName (case insensitive) to search on.
     * @return SystemFilter object with unique aliasName, or null if
     *  no filter object with this name exists.
     */
    public ISystemFilter getSystemFilter(String aliasName)
    {
    	return helpers.getSystemFilter(internalGetFilters(), aliasName);
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
    public boolean addSystemFilter(ISystemFilter filter)
    {
    	return helpers.addSystemFilter(internalGetFilters(),filter);
    }
    /**
     * Removes a given filter from the list.
     * @param filter SystemFilter object to remove
     */
    public void deleteSystemFilter(ISystemFilter filter)
    {
        helpers.deleteSystemFilter(internalGetFilters(),filter);
    }

    /**
     * Rename a given filter in the list.
     * @param filter SystemFilter object to remove
     */
    public void renameSystemFilter(ISystemFilter filter, String newName)
    {
        helpers.renameSystemFilter(internalGetFilters(),filter, newName);
    }

    /**
     * Updates a given filter in the list.
     * @param filter SystemFilter object to update
     * @param newName New name to assign it. Assumes unique checking already done.
     * @param newStrings New strings to assign it. Replaces current strings.
     */
    public void updateSystemFilter(ISystemFilter filter, String newName, String[] newStrings)
    {
    	helpers.updateSystemFilter(internalGetFilters(), filter, newName, newStrings);
    }

    /**
     * Duplicates a given filter in the list.
     * @param filter SystemFilter object to clone
     * @param alias New, unique, alias name to give this filter. Clone will fail if this is not unique.
     */
    public ISystemFilter cloneSystemFilter(ISystemFilter filter, String aliasName)
    {
        return helpers.cloneSystemFilter(internalGetFilters(), filter, aliasName);
    }    

    /**
     * Return a given filter's zero-based location
     */
    public int getSystemFilterPosition(ISystemFilter filter)
    {
    	return helpers.getSystemFilterPosition(internalGetFilters(),filter);
    }
    
    /**
     * Move a given filter to a given zero-based location
     */
    public void moveSystemFilter(int pos, ISystemFilter filter)
    {
    	helpers.moveSystemFilter(internalGetFilters(),pos,filter);
    }
        
    /**
	 * This is the method required by the IAdaptable interface.
	 * Given an adapter class type, return an object castable to the type, or
	 *  null if this is not possible.
	 */
    public Object getAdapter(Class adapterType)
    {
   	    return Platform.getAdapterManager().getAdapter(this, adapterType);	
    }   

    /**
     * Private helper method to deduce filter names from disk files.
     * Will populate and return a list.
     * Only makes sense to use if the save policy is one file per filter.
     */
    protected static Vector deduceFilterNames(IFolder folder, ISystemFilterNamingPolicy namingPolicy)
    {
    	Vector filterNames = SystemResourceHelpers.getResourceHelpers().convertToVectorAndStrip(
    	          SystemResourceHelpers.getResourceHelpers().listFiles(folder,
    	                                                               namingPolicy.getFilterSaveFileNamePrefix(),
    	                                                               SAVEFILE_SUFFIX),
    	          namingPolicy.getFilterSaveFileNamePrefix(), SAVEFILE_SUFFIX);    	          		
    	return filterNames;
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
    public void orderSystemFilters(String[] names)
    {
    	java.util.List filterList = internalGetFilters();
    	ISystemFilter[] filters = new ISystemFilter[names.length];
    	for (int idx=0; idx<filters.length; idx++)
    	   filters[idx] = getSystemFilter(names[idx]); // convert name to filter
    	filterList.clear();
    	for (int idx=0; idx<filters.length; idx++)
    	{
    	   filterList.add(filters[idx]); 
    	}
        helpers.invalidateCache();
    }

    
    // -----------------------
    // SAVE/RESTORE METHODS...
    // -----------------------
    /**
     * Return the save file that will be written for the given filter pool.
     * Will depend on this manager's save policy.
     */
    public IFile getSaveFile()
    {
        String fileName = getRootSaveFileName(this);
        return null;//FIXME SystemMOFHelpers.getSaveFile(getFolder(),fileName);
    }
    
   

 


    /**
     * Restore specific filter pool. You should not call this directly, as it is possible
     *  that certain data is not restored if the save policy is one file per filter. Rather,
     *  you should call the createSystemFilterPoolMethod.
     * @param mofHelpers SystemMOFHelpers object with helper methods for saving and restoring via mof
     * @param folder The filter pool folder. 
     * @param name name of pool to restore. Used to deduce file name for save policy of one file per pool.
     * @param savePolicy policy used to save the pool. One of the following from SystemFilterPoolManager:
     * <ul>
     *   <li>SAVE_POLICY_ONE_FILEANDFOLDER_PER_POOL - one file and folder per pool
     *   <li>SAVE_POLICY_ONE_FILE_PER_POOL_SAME_FOLDER - one file per pool, all files in one folder
     *   <li>SAVE_POLICY_ONE_FILE_PER_FILTER - one file per filter, one folder per pool
     * </ul> 
     * @param namingPolicy The names to use for file and folders when persisting to disk. Pass
     *     null to just use the defaults.
     * @return SystemFilterPool object if restored ok, null if error encountered. If null, call getLastException().
     * @see org.eclipse.rse.filters.ISystemFilterConstants
     */
    protected static ISystemFilterPool restore(String name, int savePolicy, 
                                              ISystemFilterNamingPolicy namingPolicy)
           throws Exception
    {
        if (namingPolicy == null)
          namingPolicy = SystemFilterNamingPolicy.getNamingPolicy();
        
      // FIXME  SystemFilterImpl.initMOF(); // to be safe
        
        ISystemFilterPool pool = null;
        SystemFilterPool poolImpl = null;
        /* FIXME
        switch(savePolicy)
        {
           case SAVE_POLICY_ONE_FILEANDFOLDER_PER_POOL:
             pool = restoreFromOneFile(mofHelpers, folder, name, namingPolicy, true);
             poolImpl = (SystemFilterPoolImpl)pool;
             break;

           case SAVE_POLICY_ONE_FILE_PER_POOL_SAME_FOLDER:
             pool = restoreFromOneFile(mofHelpers, folder, name, namingPolicy, true);
             poolImpl = (SystemFilterPoolImpl)pool;
             break;

           case SAVE_POLICY_ONE_FILE_PER_FILTER:
             pool = restoreFromOneFile(mofHelpers, folder, name, namingPolicy, false); // restore data part
             poolImpl = (SystemFilterPoolImpl)pool;
             //pool = createPool();
             
             poolImpl.setFolder(folder); // SystemFilter's restore will query it
             pool.setNamingPolicy(namingPolicy); // SystemFilter's restore will query it
             Vector filterNames = deduceFilterNames(folder, namingPolicy);
             if (filterNames != null)
             {
               Exception lastException = null;
               for (int idx=0; idx<filterNames.size(); idx++)
               {
               	  try {
                    SystemFilter filter = 
                      SystemFilterImpl.restore(mofHelpers, folder, (String)filterNames.elementAt(idx), pool, namingPolicy);
                    poolImpl.addSystemFilter(filter);
               	  } catch (Exception exc) {lastException = exc;}
               }               
               if (lastException != null)
               {
               	 // todo, something! We want to signal the error, but not prevent pool from being restored.
               	 // Only option: log it somewhere.
               }
               else
               	 poolImpl.sortFilters();                 
             }  
             break;        	
        }   
        */
        if (poolImpl != null)
        {        
          poolImpl.initialize(name,savePolicy,namingPolicy);
        }
        return pool;
    }
    
    /**
     * Do restore when all filters in this pool are stored in a single file.
     */
    protected static ISystemFilterPool restoreFromOneFile(IFolder folder, String name, 
                                                         ISystemFilterNamingPolicy namingPolicy,
                                                         boolean restoreFilters)
     throws Exception
    {
    	
        String fileName = getRootSaveFileName(namingPolicy, name);
        ISystemFilterPool pool = null;
        /*FIXME
        if (!restoreFilters && !SystemResourceHelpers.getResourceHelpers().getFile(folder, fileName+".xmi").exists())
        {
          // special case: data file might not exist, as only started saving it recently.
          pool = createPool();
          ((SystemFilterPoolImpl)pool).specialCaseNoDataRestored = true;
          RSEUIPlugin.logInfo("Created filter pool file " + fileName+".xmi during restore");
        }
        else
        {
			//Extent java.util.List = mofHelpers.restore(folder,fileName); MOF way
			java.util.List = mofHelpers.restore(folder,fileName);
			// should be exactly one system filter pool...
			Iterator iList = java.util.List.iterator();
			pool = (SystemFilterPool)iList.next();
        }
        if (pool != null)
        {
          pool.setName(name);
          if (restoreFilters)
          {
          	((SystemFilterPoolImpl)pool).initializeFilterStrings();
          }
        }
        else
          RSEUIPlugin.logInfo("Hmmm, pool is still null after restore: " + fileName+".xmi");
          */
        return pool;	        
    }

    /**
     * Internal use method
     */
    protected void initializeFilterStrings()
    {
    	java.util.List filters = getFilters();
    	Iterator i = filters.iterator();
    	while (i.hasNext())
    	  ((SystemFilter)i.next()).initializeFilterStrings();
    }
    
    /**
     * When saving one file per filter, we store the relative order of each filter
     * within each filter. After restoring, the filters are ordered by their file
     * system existence, and so we must now re-order the internal list according to
     * the relative order given in each filter
     */
    protected void sortFilters()
    {
    	java.util.List filterList = getFilters(); // get java.util.List
    	if (filterList.size() <= 1) // not enough to sort?
    	  return; // outa here!
    	if (debug)
    	  printFilterList("Before sorting");
        helpers.invalidateCache(); // force re-gen of array from java.util.List on next getSystemFilters() request
    	ISystemFilter[] filters = getSystemFilters(); // convert java.util.List to array    	
    	boolean[] processed = new boolean[filters.length];
    	//for (int idx=0;idx<processed.length;idx++)
    	   //processed[idx] = false;
    	filterList.clear(); // clear java.util.List
    	//boolean done = false;
    	int processedCount = 0;
    	int totalCount = filters.length;
    	int nextHighest = -1;
    	int firstMatchIdx = -1;
    	int round = 0;
    	boolean stop = false;
    	//int timeout = factorial(totalCount);
    	//System.out.println("Factorial of " + totalCount + " = " + timeout);
    	int timeout = totalCount + 1; 
    	while ((processedCount != totalCount) && (round < timeout))
    	{
    	  nextHighest = 9999;
    	  firstMatchIdx = -1;
    	  stop = false;
    	  // find next highest number
    	  for (int idx=0; !stop && (idx<totalCount); idx++)
    	  {
    	  	 if (!processed[idx])
    	  	 {
    	  	   int currOrder = filters[idx].getRelativeOrder();
    	  	   if (currOrder < nextHighest)
    	  	   {
    	  	   	 nextHighest = currOrder;
    	  	   	 firstMatchIdx = idx;
    	  	   	 //stop = true;
    	  	   }
    	  	   else if ((currOrder == nextHighest) && (firstMatchIdx==-1))
    	  	   {
    	  	   	 firstMatchIdx = idx;
    	  	   	 //stop = true;
    	  	   }
    	  	 }
    	  }
    	  ++round;
    	  //System.out.println("Round " + round + ": nextHighest = " + nextHighest + ", firstMatchIdx = " + firstMatchIdx);
    	  if (firstMatchIdx != -1)
    	  {
    	  	filterList.add(filters[firstMatchIdx]);
    	  	processed[firstMatchIdx] = true;
    	  	++processedCount;
    	  }
    	}
        helpers.invalidateCache(); // force re-gen of array from java.util.List on next getSystemFilters() request
        if (debug)
    	  printFilterList("After sorting");
    }
    
    private void printFilterList(String tagLine)
    {
    	ISystemFilter[] filters = getSystemFilters();
    	if (filters.length == 0)
    	  return;
    	System.out.println(tagLine+" for filter pool " + getName());
    	for (int idx=0; idx<filters.length; idx++)
    	{
    	   System.out.println("  "+filters[idx].getName()+" "+filters[idx].getRelativeOrder());
    	}
    	System.out.println();
    }



    /**
     * Return the root save file name without the extension .xmi
     */
    protected static String getRootSaveFileName(ISystemFilterPool pool)
    {
        return getRootSaveFileName(pool.getNamingPolicy(), pool.getName());
    }
    /**
     * Return the root save file name without the extension .xmi
     */
    protected static String getRootSaveFileName(ISystemFilterPool pool, String newName)
    {
        return getRootSaveFileName(pool.getNamingPolicy(), newName);
    }
    /**
     * Return the root save file name without the extension .xmi
     */
    protected static String getRootSaveFileName(ISystemFilterNamingPolicy namingPolicy, String poolName)
    {
    	return namingPolicy.getFilterPoolSaveFileName(poolName);
    }


	/**
	 * Return the unique reference name of this object.
	 * <p>
	 * As required by the {@link org.eclipse.rse.references.ISystemBasePersistableReferencedObject ISystemPersistableReferencedObject} 
	 * interface.
	 */
	public String getReferenceName()
	{
		return getSystemFilterPoolManager().getName()+DELIMITER+getName();
	}

    // -----------------------
    // HOUSEKEEPING METHODS...
    // -----------------------
    /* */
    public String toString()
    {
   	    return getName();
    }
	/**
	 * @generated This field/method will be replaced during code generation.
	 */
	public void setType(String newType)
	{
		type = newType;
	}

	/**
	 * @generated This field/method will be replaced during code generation 
	 */
	public boolean isSupportsNestedFilters()
	{
		return supportsNestedFilters;
	}

	/**
	 * @generated This field/method will be replaced during code generation.
	 */
	public void unsetStringsCaseSensitive()
	{
		stringsCaseSensitive = STRINGS_CASE_SENSITIVE_EDEFAULT;
		stringsCaseSensitiveESet = false;
		}

	/**
	 * @generated This field/method will be replaced during code generation.
	 */
	public boolean isSetStringsCaseSensitive()
	{
		return stringsCaseSensitiveESet;
	}

	/**
	 * @generated This field/method will be replaced during code generation 
	 */
	public java.util.List getFilters()
	{
		if (filters == null)
		{
			filters = new ArrayList();
			//FIXME new EObjectContainmenteList(SystemFilter.class, this, FiltersPackage.SYSTEM_FILTER_POOL__FILTERS);
		}
		return filters;
	}

	/**
	 * @generated This field/method will be replaced during code generation 
	 */
	public boolean isSupportsDuplicateFilterStrings()
	{
		return supportsDuplicateFilterStrings;
	}

	/**
	 * @generated This field/method will be replaced during code generation 
	 * In what release was this created? Typically, will be the version and release
	 * times 10, as in 40 or 51.
	 */
	public int getRelease()
	{
		return release;
	}

	/**
	 * @generated This field/method will be replaced during code generation.
	 */
	public void setRelease(int newRelease)
	{
		release = newRelease;
	}

	/**
	 * Returns true if this filter is limited to a single filter string. If not set here,
	 *  it is queried from the parent manager.
	 */
	public boolean isSingleFilterStringOnly()
	{
		if (isSetSingleFilterStringOnly())
		  return singleFilterStringOnly;
		else
		  return getSystemFilterPoolManager().isSingleFilterStringOnly();
	}
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isSingleFilterStringOnlyGen()
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
		singleFilterStringOnly = newSingleFilterStringOnly;
		singleFilterStringOnlyESet = true;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void unsetSingleFilterStringOnly()
	{
		singleFilterStringOnly = SINGLE_FILTER_STRING_ONLY_EDEFAULT;
		singleFilterStringOnlyESet = false;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isSetSingleFilterStringOnly()
	{
		return singleFilterStringOnlyESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getOwningParentName()
	{
		return owningParentName;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setOwningParentName(String newOwningParentName)
	{
		if (newOwningParentName != owningParentName)
		{
			owningParentName = newOwningParentName;
			setDirty(true);
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isNonRenamable()
	{
		return nonRenamable;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setNonRenamable(boolean newNonRenamable)
	{
		boolean oldNonRenamable = nonRenamable;
		if (oldNonRenamable!= newNonRenamable)
		{
			nonRenamable = newNonRenamable;
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
			_isDirty= flag;
		}
	}
	
	public boolean commit()
	{
		return RSEUIPlugin.getThePersistenceManager().commit(this);
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
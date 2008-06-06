/*******************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation and others.
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
 * David Dykstal (IBM) - removing implementation of ISystemFilterSavePolicies, ISystemFilterConstants
 * David Dykstal (IBM) - 142806: refactoring persistence framework
 * Martin Oberhuber (Wind River) - [184095] Replace systemTypeName by IRSESystemType
 *******************************************************************************/

package org.eclipse.rse.internal.core.filters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.filters.ISystemFilter;
import org.eclipse.rse.core.filters.ISystemFilterPool;
import org.eclipse.rse.core.filters.ISystemFilterPoolManager;
import org.eclipse.rse.core.filters.ISystemFilterPoolManagerProvider;
import org.eclipse.rse.core.model.IRSEPersistableContainer;
import org.eclipse.rse.core.model.ISystemProfile;
import org.eclipse.rse.internal.core.RSECoreMessages;
import org.eclipse.rse.internal.references.SystemPersistableReferencedObject;


/**
 * A system filter pool is a means of grouping filters and managing them as a
 * list.
 * <p>
 * To enable filters themselves to be autonomous and sharable, it is decided
 * that no data will be persisted in the filter pool itself. Rather, all
 * attributes other than the list of filters are transient and as such it is the
 * responsibility of the programmer using a filter pool to set these attributes
 * after creating or restoring a filter pool. Typically, this is what a filter
 * pool manager (SystemFilterPoolManager) will do for you.
 */
public class SystemFilterPool extends SystemPersistableReferencedObject
       implements ISystemFilterPool, IAdaptable
{

    /**
	 * The default value of the '{@link #getName() <em>Name</em>}' attribute.
	 *
	 * @see #getName()
	 */
	protected static final String NAME_EDEFAULT = null;

    private String name;

    /**
	 * The default value of the '{@link #getType() <em>Type</em>}' attribute.
	 *
	 * @see #getType()
	 */
	protected static final String TYPE_EDEFAULT = null;

    private ISystemFilterPoolManager mgr;
    private SystemFilterContainerCommonMethods helpers = null;
    private Object filterPoolData = null;
    private boolean initialized = false;
    protected boolean specialCaseNoDataRestored = false;
    private boolean debug = false;
    protected static final String DELIMITER = SystemFilterPoolReference.DELIMITER;

    	/**
	 * @generated This field/method will be replaced during code generation.
	 */
	protected String type = TYPE_EDEFAULT;

	/**
	 * The default value of the '{@link #isSupportsNestedFilters() <em>Supports Nested Filters</em>}' attribute.
	 * @see #isSupportsNestedFilters()
	 */
	protected static final boolean SUPPORTS_NESTED_FILTERS_EDEFAULT = false;

	/**
	 * @generated This field/method will be replaced during code generation.
	 */
	protected boolean supportsNestedFilters = SUPPORTS_NESTED_FILTERS_EDEFAULT;

	/**
	 * The default value of the '{@link #isDeletable() <em>Deletable</em>}' attribute.
	 * @see #isDeletable()
	 */
	protected static final boolean DELETABLE_EDEFAULT = false;

	/**
	 * @generated This field/method will be replaced during code generation.
	 */

	protected boolean deletable = DELETABLE_EDEFAULT;
	/**
	 * The default value of the '{@link #isDefault() <em>Default</em>}' attribute.
	 * @see #isDefault()
	 */

	protected static final boolean DEFAULT_EDEFAULT = false;

	/**
	 * @generated This field/method will be replaced during code generation.
	 */
	protected boolean default_ = DEFAULT_EDEFAULT;

	/**
	 * The default value of the '{@link #isStringsCaseSensitive() <em>Strings Case Sensitive</em>}' attribute.
	 * @see #isStringsCaseSensitive()
	 */
	protected static final boolean STRINGS_CASE_SENSITIVE_EDEFAULT = false;

	/**
	 * @generated This field/method will be replaced during code generation.
	 */
	protected boolean stringsCaseSensitive = STRINGS_CASE_SENSITIVE_EDEFAULT;

	/**
	 * This is true if the Strings Case Sensitive attribute has been set.
	 */
	protected boolean stringsCaseSensitiveESet = false;

	/**
	 * The default value of the '{@link #isSupportsDuplicateFilterStrings() <em>Supports Duplicate Filter Strings</em>}' attribute.
	 * @see #isSupportsDuplicateFilterStrings()
	 */
	protected static final boolean SUPPORTS_DUPLICATE_FILTER_STRINGS_EDEFAULT = false;

	/**
	 * @generated This field/method will be replaced during code generation.
	 */
	protected boolean supportsDuplicateFilterStrings = SUPPORTS_DUPLICATE_FILTER_STRINGS_EDEFAULT;

	/**
	 * The default value of the '{@link #getRelease() <em>Release</em>}' attribute.
	 * @see #getRelease()
	 */
	protected static final int RELEASE_EDEFAULT = 0;

	/**
	 * @generated This field/method will be replaced during code generation.
	 */
	protected int release = RELEASE_EDEFAULT;

	/**
	 * The default value of the '{@link #isSingleFilterStringOnly() <em>Single Filter String Only</em>}' attribute.
	 * @see #isSingleFilterStringOnly()
	 */
	protected static final boolean SINGLE_FILTER_STRING_ONLY_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isSingleFilterStringOnly() <em>Single Filter String Only</em>}' attribute.
	 * @see #isSingleFilterStringOnly()
	 */
	protected boolean singleFilterStringOnly = SINGLE_FILTER_STRING_ONLY_EDEFAULT;

	/**
	 * This is true if the Single Filter String Only attribute has been set.
	 */
	protected boolean singleFilterStringOnlyESet = false;

	/**
	 * The default value of the '{@link #getOwningParentName() <em>Owning Parent Name</em>}' attribute.
	 * @see #getOwningParentName()
	 */
	protected static final String OWNING_PARENT_NAME_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getOwningParentName() <em>Owning Parent Name</em>}' attribute.
	 * @see #getOwningParentName()
	 */
	protected String owningParentName = OWNING_PARENT_NAME_EDEFAULT;

	/**
	 * The default value of the '{@link #isNonRenamable() <em>Non Renamable</em>}' attribute.
	 * @see #isNonRenamable()
	 */
	protected static final boolean NON_RENAMABLE_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isNonRenamable() <em>Non Renamable</em>}' attribute.
	 * @see #isNonRenamable()
	 */
	protected boolean nonRenamable = NON_RENAMABLE_EDEFAULT;

	/**
	 * @generated This field/method will be replaced during code generation.
	 */
	protected List filters = new ArrayList(10);

	/**
	 * Default constructor
	 */
	public SystemFilterPool(String poolName, boolean allowNestedFilters, boolean isDeletable)
	{
		super();
		helpers = new SystemFilterContainerCommonMethods();
	   	setRelease(RSECorePlugin.CURRENT_RELEASE);
		if (!initialized) {
			initialize(poolName);
		}
        setDeletable(isDeletable); // mof attribute
        setSupportsNestedFilters(allowNestedFilters); // cascades to each filter
 	}

    /*
     * Private helper method to core initialization, from either createXXX or restore.
     */
	protected void initialize(String name)
	{
        setName(name); // mof attribute
        initialized = true;
	}


	protected List internalGetFilters()
	{
		return filters;
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
	 * @param newSupportsNestedFilters
	 */
	private void setSupportsNestedFiltersGen(boolean newSupportsNestedFilters)
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
	 * @param newSupportsDuplicateFilterStrings
	 */
	private void setSupportsDuplicateFilterStringsGen(boolean newSupportsDuplicateFilterStrings)
	{
		supportsDuplicateFilterStrings = newSupportsDuplicateFilterStrings;
    	setDirty(true);
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
    	setDirty(true);
    }

	private void setStringsCaseSensitiveGen(boolean newStringsCaseSensitive)
	{
		stringsCaseSensitive = newStringsCaseSensitive;
		stringsCaseSensitiveESet = true;
    	setDirty(true);
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
		return RSECoreMessages.RESID_MODELOBJECTS_FILTERPOOL_DESCRIPTION;
	}

	/**
	 * Set the name of this filter pool.
	 */
	public void setName(String name)
	{
		if (name.indexOf(SystemFilterPoolReference.DELIMITER) >= 0) {
			throw new IllegalArgumentException("Cannot have ___ in filter pool name.");
		}
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

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.filters.ISystemFilterPool#isDeletable()
	 */
	public boolean isDeletable()
	{
		return deletable;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.filters.ISystemFilterPool#setDeletable(boolean)
	 */
	public void setDeletable(boolean newDeletable)
	{
		deletable = newDeletable;
    	setDirty(true);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.filters.ISystemFilterPool#isDefault()
	 */
	public boolean isDefault()
	{
		return default_;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.filters.ISystemFilterPool#setDefault(boolean)
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
    		copySystemFilter(targetPool, filters[idx], filters[idx].getName());
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
    public ISystemFilter createSystemFilter(String aliasName, String[] filterStrings)
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
    public String[] getSystemFilterNames()
    {
    	List filters = internalGetFilters();
    	List names = helpers.getSystemFilterNames(filters);
    	String[] result = new String[names.size()];
    	names.toArray(result);
    	return result;
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
    	boolean result = helpers.addSystemFilter(internalGetFilters(),filter);
    	if (result) setDirty(true);
    	return result;
    }
    /**
     * Removes a given filter from the list.
     * @param filter SystemFilter object to remove
     */
    public void deleteSystemFilter(ISystemFilter filter)
    {
        helpers.deleteSystemFilter(internalGetFilters(),filter);
    	setDirty(true);
    }

    /**
     * Rename a given filter in the list.
     * @param filter SystemFilter object to remove
     */
    public void renameSystemFilter(ISystemFilter filter, String newName)
    {
        helpers.renameSystemFilter(internalGetFilters(),filter, newName);
    	setDirty(true);
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
    	setDirty(true);
    }

    /**
     * Duplicates a given filter in the list.
     * @param filter SystemFilter object to clone
     * @param aliasName New, unique, alias name to give this filter. Clone will fail if this is not unique.
     */
    public ISystemFilter cloneSystemFilter(ISystemFilter filter, String aliasName)
    {
        ISystemFilter result = helpers.cloneSystemFilter(internalGetFilters(), filter, aliasName);
    	setDirty(true);
    	return result;
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
    	setDirty(true);
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
    	ISystemFilter[] filterArray = new ISystemFilter[names.length];
    	for (int idx=0; idx<filterArray.length; idx++)
    	   filterArray[idx] = getSystemFilter(names[idx]); // convert name to filter
    	filters.clear();
    	for (int idx=0; idx<filterArray.length; idx++)
    	{
    	   filters.add(filterArray[idx]);
    	}
        helpers.invalidateCache();
    }


    // -----------------------
    // SAVE/RESTORE METHODS...
    // -----------------------

    /**
     * Internal use method
     */
    protected void initializeFilterStrings()
    {
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
    	if (filters.size() <= 1) // not enough to sort?
    	  return; // outa here!
    	if (debug)
    	  printFilterList("Before sorting"); //$NON-NLS-1$
        helpers.invalidateCache(); // force re-gen of array from java.util.List on next getSystemFilters() request
    	ISystemFilter[] filterArray = getSystemFilters(); // convert java.util.List to array
    	boolean[] processed = new boolean[filterArray.length];
    	//for (int idx=0;idx<processed.length;idx++)
    	   //processed[idx] = false;
    	filters.clear(); // clear java.util.List
    	//boolean done = false;
    	int processedCount = 0;
    	int totalCount = filterArray.length;
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
    	  	   int currOrder = filterArray[idx].getRelativeOrder();
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
    	  	filters.add(filterArray[firstMatchIdx]);
    	  	processed[firstMatchIdx] = true;
    	  	++processedCount;
    	  }
    	}
        helpers.invalidateCache(); // force re-gen of array from java.util.List on next getSystemFilters() request
        if (debug)
    	  printFilterList("After sorting"); //$NON-NLS-1$
    }

    private void printFilterList(String tagLine)
    {
    	ISystemFilter[] filters = getSystemFilters();
    	if (filters.length == 0)
    	  return;
    	System.out.println(tagLine+" for filter pool " + getName()); //$NON-NLS-1$
    	for (int idx=0; idx<filters.length; idx++)
    	{
    	   System.out.println("  "+filters[idx].getName()+" "+filters[idx].getRelativeOrder()); //$NON-NLS-1$ //$NON-NLS-2$
    	}
    	System.out.println();
    }

	/**
	 * Return the unique reference name of this object.
	 * <p>
	 * As required by the {@link org.eclipse.rse.core.references.IRSEBasePersistableReferencedObject IRSEPersistableReferencedObject}
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
    	setDirty(true);
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
    	setDirty(true);
	}

	/**
	 * @generated This field/method will be replaced during code generation.
	 */
	public boolean isSetStringsCaseSensitive()
	{
		return stringsCaseSensitiveESet;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.filters.ISystemFilterPool#getFilters()
	 */
	public ISystemFilter[] getFilters()
	{
		ISystemFilter[] result = new ISystemFilter[filters.size()];
		filters.toArray(result);
		return result;
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
    	setDirty(true);
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

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.filters.ISystemFilterPool#setSingleFilterStringOnly(boolean)
	 */
	public void setSingleFilterStringOnly(boolean newSingleFilterStringOnly)
	{
		singleFilterStringOnly = newSingleFilterStringOnly;
		singleFilterStringOnlyESet = true;
    	setDirty(true);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.filters.ISystemFilterPool#unsetSingleFilterStringOnly()
	 */
	public void unsetSingleFilterStringOnly()
	{
		singleFilterStringOnly = SINGLE_FILTER_STRING_ONLY_EDEFAULT;
		singleFilterStringOnlyESet = false;
    	setDirty(true);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.filters.ISystemFilterPool#isSetSingleFilterStringOnly()
	 */
	public boolean isSetSingleFilterStringOnly()
	{
		return singleFilterStringOnlyESet;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.filters.ISystemFilterPool#getOwningParentName()
	 */
	public String getOwningParentName()
	{
		return owningParentName;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.filters.ISystemFilterPool#setOwningParentName(java.lang.String)
	 */
	public void setOwningParentName(String newOwningParentName)
	{
		if (newOwningParentName != owningParentName)
		{
			owningParentName = newOwningParentName;
			setDirty(true);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.filters.ISystemFilterPool#isNonRenamable()
	 */
	public boolean isNonRenamable()
	{
		return nonRenamable;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.filters.ISystemFilterPool#setNonRenamable(boolean)
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

	public boolean commit()
	{
		return getPersistableParent().commit();
	}

	public IRSEPersistableContainer getPersistableParent() {
		ISystemProfile profile = null;
		ISystemFilterPoolManager filterPoolManager = getSystemFilterPoolManager();
		if (filterPoolManager != null) {
			profile = filterPoolManager.getSystemProfile();
		}
		return profile;
	}

	public IRSEPersistableContainer[] getPersistableChildren() {
		List children = new ArrayList(10);
		children.addAll(Arrays.asList(getSystemFilters()));
		children.addAll(Arrays.asList(getPropertySets()));
		IRSEPersistableContainer[] result = new IRSEPersistableContainer[children.size()];
		children.toArray(result);
		return result;
	}

}

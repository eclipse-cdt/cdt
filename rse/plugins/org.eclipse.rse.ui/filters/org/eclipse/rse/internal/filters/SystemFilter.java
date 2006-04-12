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
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.rse.filters.ISystemFilter;
import org.eclipse.rse.filters.ISystemFilterConstants;
import org.eclipse.rse.filters.ISystemFilterContainer;
import org.eclipse.rse.filters.ISystemFilterNamingPolicy;
import org.eclipse.rse.filters.ISystemFilterPool;
import org.eclipse.rse.filters.ISystemFilterPoolManager;
import org.eclipse.rse.filters.ISystemFilterPoolManagerProvider;
import org.eclipse.rse.filters.ISystemFilterString;
import org.eclipse.rse.filters.SystemFilterSimple;
import org.eclipse.rse.internal.references.SystemReferencedObject;
import org.eclipse.rse.references.ISystemReferencedObject;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemResources;


/**
 * A filter is an encapsulation of a unique name, and a list
 *  of filter strings.
 * Filters can be referenced.
 */
/** 
 * @lastgen class SystemFilterImpl extends SystemReferencedObjectImpl implements SystemFilter, SystemReferencedObject, SystemFilterContainer, IAdaptable {}
 */
public class SystemFilter extends SystemReferencedObject implements ISystemFilter, ISystemReferencedObject, ISystemFilterContainer, IAdaptable
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

    private SystemFilterContainerCommonMethods helpers = null;
    private ISystemFilterPool                   parentPool = null;
    protected String[]                           filterStringArray = null;
    protected ISystemFilterString[]               filterStringObjectArray = null;    
    protected Vector                             filterStringVector = null;
    
    // persistence
    protected boolean 							_isDirty = true;
    protected boolean 							_wasRestored = false;
    
    //protected static String SAVEFILE_PREFIX = DEFAULT_FILENAME_PREFIX_FILTER;
    //protected static String SAVEFILE_SUFFIX = ".xmi";    
    protected static boolean debug = true;    
    /**
	 * @generated This field/method will be replaced during code generation.
	 */
	/**
	 * @generated This field/method will be replaced during code generation.
	 */
	protected String name = NAME_EDEFAULT;
	/**
	 * The default value of the '{@link #getType() <em>Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getType()
	 * @generated
	 * @ordered
	 */
	protected static final String TYPE_EDEFAULT = null;

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
	 * The default value of the '{@link #getRelativeOrder() <em>Relative Order</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getRelativeOrder()
	 * @generated
	 * @ordered
	 */
	protected static final int RELATIVE_ORDER_EDEFAULT = 0;

	/**
	 * @generated This field/method will be replaced during code generation.
	 */
	protected int relativeOrder = RELATIVE_ORDER_EDEFAULT;
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
	 * The default value of the '{@link #isPromptable() <em>Promptable</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isPromptable()
	 * @generated
	 * @ordered
	 */
	protected static final boolean PROMPTABLE_EDEFAULT = false;

	/**
	 * @generated This field/method will be replaced during code generation.
	 */
	protected boolean promptable = PROMPTABLE_EDEFAULT;
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
	 * The default value of the '{@link #isNonDeletable() <em>Non Deletable</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isNonDeletable()
	 * @generated
	 * @ordered
	 */
	protected static final boolean NON_DELETABLE_EDEFAULT = false;

	/**
	 * @generated This field/method will be replaced during code generation.
	 */
	protected boolean nonDeletable = NON_DELETABLE_EDEFAULT;
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
	 * @generated This field/method will be replaced during code generation.
	 */
	protected boolean nonRenamable = NON_RENAMABLE_EDEFAULT;
	/**
	 * The default value of the '{@link #isNonChangable() <em>Non Changable</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isNonChangable()
	 * @generated
	 * @ordered
	 */
	protected static final boolean NON_CHANGABLE_EDEFAULT = false;

	/**
	 * @generated This field/method will be replaced during code generation.
	 */
	protected boolean nonChangable = NON_CHANGABLE_EDEFAULT;
	/**
	 * The default value of the '{@link #isStringsNonChangable() <em>Strings Non Changable</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isStringsNonChangable()
	 * @generated
	 * @ordered
	 */
	protected static final boolean STRINGS_NON_CHANGABLE_EDEFAULT = false;

	/**
	 * @generated This field/method will be replaced during code generation.
	 */
	protected boolean stringsNonChangable = STRINGS_NON_CHANGABLE_EDEFAULT;
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
	 * @generated This field/method will be replaced during code generation.
	 */
	protected java.util.List nestedFilters = null;
	/**
	 * @generated This field/method will be replaced during code generation.
	 */
	protected java.util.List strings = null;
	
	
	// FIXME
	protected ISystemFilter _parentFilter;
	
/**
	 * Constructor. Do not instantiate directly, let MOF do it!
	 */
	protected SystemFilter() 
	{
		super();
		helpers = new SystemFilterContainerCommonMethods();		
	}
	/*
     * Private internal way to get filters. Makes it easy to change in future, if we don't use MOF.
     */
	protected java.util.List internalGetFilters()
	{
		return getNestedFilters();
	}

   /**
     * Returns the type attribute. Intercepted to return SystemFilterConstants.DEFAULT_TYPE if it is currently null
     */
	public String getType()
    {
    	String type = getTypeGen();
    	if (type == null)
    	  return ISystemFilterConstants.DEFAULT_TYPE;
    	else
    	  return type;
    }
	/**
	  * Returns the type attribute. Intercepted to return SystemFilterConstants.DEFAULT_TYPE if it is currently null
	  */
	 public String getTypeGen()
	 {
	     return type;
	 }
    /*
     * Creates a new nested system filter within this filter
     * @param parentPool the SystemFilterPool that owns the root filter.
     * @param data Optional transient data to be stored in the new filter. Can be null.
     * @param aliasName The name to give the new filter. Must be unique for this pool.
     * @param filterStrings The list of String objects that represent the filter strings.
     *
    public SystemFilter createSystemFilter(SystemFilterPool parentPool, Object data, String aliasName, Vector filterStrings)
    {    	
    	SystemFilter newFilter = helpers.createSystemFilter(internalGetFilters(), parentPool, data, aliasName, filterStrings);
    	newFilter.setSupportsNestedFilters(true); // presumably it does since it is nested itself.
    	return newFilter;
    }*/

    /**
     * Creates a new nested system filter within this filter. 
     * This filter will inherit/store the following attributes from this filter:
     * <ul>
     *   <li>supportsNestedFilters
     *   <li>supportsDuplicateFilterStrings
     *   <li>stringsCaseSensitive
     *   <li>data
     * </ul>
     * @param aliasName The name to give the new filter. Must be unique for this pool.
     * @param filterStrings The list of String objects that represent the filter strings.
     */    
    public ISystemFilter createSystemFilter(String aliasName, Vector filterStrings)
    {    	
    	ISystemFilter newFilter = helpers.createSystemFilter(internalGetFilters(), getParentFilterPool(), aliasName, filterStrings);
    	newFilter.setSupportsNestedFilters(true); // presumably it does since it is nested itself.
    	newFilter.setSupportsDuplicateFilterStrings(supportsDuplicateFilterStrings()); 
    	newFilter.setStringsCaseSensitive(areStringsCaseSensitive()); 
    	return newFilter;
    }
    
    /**
     * Internal use method
     */
    protected void initializeFilterStrings()
    {
    	java.util.List filterStrings = getStrings();
    	Iterator i = filterStrings.iterator();
    	while (i.hasNext())
    	  ((ISystemFilterString)i.next()).setParentSystemFilter(this);
    }

    /**
     * Clones a given filter to the given target filter.
     * All filter strings, and all nested filters, are copied.
     * @param targetFilter new filter into which we copy all our data
     */
    public void clone(ISystemFilter targetFilter)
    {
    	// clone attributes
    	//targetFilter.setName(getName());
    	targetFilter.setDefault(isDefault());
    	targetFilter.setType(getType());
    	targetFilter.setPromptable(isPromptable());    	
    	targetFilter.setRelativeOrder(getRelativeOrder());
    	targetFilter.setSupportsNestedFilters(isSupportsNestedFilters());
    	targetFilter.setSupportsDuplicateFilterStrings(isSupportsDuplicateFilterStrings());
    	targetFilter.setStringsNonChangable(isStringsNonChangable());
    	targetFilter.setNonChangable(isNonChangable());    	
    	targetFilter.setNonDeletable(isNonDeletable());
    	targetFilter.setNonRenamable(isNonRenamable());
    	if (isSetSingleFilterStringOnly())
    	  targetFilter.setSingleFilterStringOnly(isSingleFilterStringOnly());    	    	
		if (isSetStringsCaseSensitive())
		  targetFilter.setStringsCaseSensitive(isStringsCaseSensitive());
    	// clone filter strings
    	ISystemFilterString[] strings = getSystemFilterStrings();
    	ISystemFilterString newString = null;
    	if (strings != null)
    	  for (int idx=0; idx<strings.length; idx++)
    	     newString = copySystemFilterString(targetFilter, strings[idx]);
    	// clone nested filters...
    	ISystemFilter[] filters = getSystemFilters();
    	if (filters != null)
    	  for (int idx=0; idx<filters.length; idx++)
    	  {
    		 ISystemFilter newFilter = targetFilter.createSystemFilter(filters[idx].getName(), null);
    		 filters[idx].clone(newFilter); // recursive call
    	  }
    }
    /**
     * Copies a given filter string from this filter to another filter in this pool or another pool
     *  in this manager or another manager.
     */
    public ISystemFilterString copySystemFilterString(ISystemFilter targetFilter, ISystemFilterString oldFilterString)
    {
        ISystemFilterString newString = targetFilter.addFilterString(null);
        oldFilterString.clone(newString);   
        return newString;    	
    } 

    /**
     * From SystemFilterContainer.
     * Same as calling getParentFilterPool(). It walks the parent chain until the pool is found.
     */
    public ISystemFilterPool getSystemFilterPool() 
    {
    	return getParentFilterPool();
    }

    /**
     * Return true if this a transient or simple filter that is only created temporary "on the fly"
     *  and not intended to be saved or part of the filter framework. Eg it has no manager or provider.
     * <p>
     * We always return false.
     * @see SystemFilterSimple
     */
    public boolean isTransient()
    {
    	return false;
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
     * Return true if this filter is a nested filter or not. If not, its parent is the filter pool.
     */
    public boolean isNested()
    {
    	return (getParentFilter()!=null);
    }
	/**
	 * Are filter strings in this filter case sensitive?
	 * If not set locally, queries the parent filter pool's atttribute.
	 * @return The value of the StringsCaseSensitive attribute
	 */
	public boolean isStringsCaseSensitive()
	{
		if (!isSetStringsCaseSensitive())
		  return getParentFilterPool().isStringsCaseSensitive();
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
     * Return the nested filters as an array
     */
    public ISystemFilter[] getSystemFilters()
    {
    	return helpers.getSystemFilters(internalGetFilters());
    }    
    /**
     * Return how many filters are defined in this filter container
     */
    public int getSystemFilterCount()
    {
    	return internalGetFilters().size();
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
    	helpers.moveSystemFilter(internalGetFilters(), pos, filter);
    }
    /**
     * Return the parent pool of this filter. For nested filters, we walk up the parent chain
     * until we find the pool.
     */
    public ISystemFilterPool getParentFilterPool()
    {
    	return parentPool;    	
    }
    /**
     * Internal use method to set the parent filter pool.
     */
    public void setParentFilterPool(ISystemFilterPool parentPool)
    {
    	this.parentPool = parentPool;
    	ISystemFilter[] filters = getSystemFilters();
    	if (filters != null)
    	  for (int idx=0; idx<filters.length; idx++)
    	     filters[idx].setParentFilterPool(parentPool);   	
    	// todo: decide if SystemFilterString objects need it too
    }
    /**
     * Return the ISystemFilterContainer parent of this filter. Will be either
     * a SystemFilterPool or a SystemFilter if this is a nested filter.
     */    
    public ISystemFilterContainer getParentFilterContainer()
    {
    	ISystemFilter parentFilter = getParentFilter();
    	return (parentFilter != null) ? (ISystemFilterContainer)parentFilter : (ISystemFilterContainer)getParentFilterPool();
    }

    /**
     * Internal way to return emf-modelled list of filter strings.
     * We use this so we can easily change to non-mof if we decide to.
     */
    private java.util.List internalGetFilterStrings()
    {
    	return getStrings();
    }
    
    /**
     * Clear internal cache so it will be rebuilt on next request.
     */
    protected void invalidateCache()
    {
    	filterStringArray = null;
    	filterStringObjectArray = null;
    	filterStringVector = null;
    	setDirty(true);
    }
    
    /**
     * Return filter strings as an array of String objects.
     */
    public String[] getFilterStrings()
    {
    	if (filterStringArray == null)
    	{
    	  java.util.List el = internalGetFilterStrings();
    	  filterStringArray = new String[el.size()];
    	  Iterator i = el.iterator();
    	  int idx = 0;
    	  while (i.hasNext())
    	    filterStringArray[idx++] = ((ISystemFilterString)(i.next())).getString();
    	}
    	return filterStringArray;
    }
    /**
     * Return filter strings as a Vector of String objects
     */
    public Vector getFilterStringsVector()
    {
    	if (filterStringVector == null)
    	{
    	  java.util.List el = internalGetFilterStrings();
    	  ISystemFilterString[] strings = new ISystemFilterString[el.size()];
    	  Iterator i = el.iterator();
    	  filterStringVector = new Vector();
    	  while (i.hasNext())
    	    filterStringVector.addElement(((ISystemFilterString)(i.next())).getString());
    	}
    	return filterStringVector;
    }
	/**
	 * Get this filter's filter strings as a Vector of FilterString objects
	 */	
	public Vector getFilterStringObjectsVector()
	{
		java.util.List el = internalGetFilterStrings();
		Iterator i = el.iterator();
		Vector filterStringVector = new Vector();
		while (i.hasNext())
		  filterStringVector.addElement(i.next());		
		return filterStringVector;
	}
	    
    /**
     * Return how many filter strings are defined in this filter.
     */
    public int getFilterStringCount()
    {
    	return internalGetFilterStrings().size();
    }        
    /**
     * Get a filter string given its string value
     */
    public ISystemFilterString getSystemFilterString(String string)
    {
    	ISystemFilterString[] strings = getSystemFilterStrings();
    	ISystemFilterString match = null;
    	boolean cs = areStringsCaseSensitive();
    	if (strings != null)
    	{
    		for (int idx=0; (match==null) && (idx<strings.length); idx++)
    		{
    		   if (cs)
    		   {
                 if (string.equals(strings[idx].getString())) // todo: allow case-sensitivity to be definable
                   match = strings[idx];
    		   }
    		   else
    		   {
                 if (string.equalsIgnoreCase(strings[idx].getString())) // todo: allow case-sensitivity to be definable
                   match = strings[idx];
    		   }
    		}
    	}
    	return match;
    }
    /**
     * Set all the filter strings for this filter.
     * @param newStrings Vector of String objects
     */
    public void setFilterStrings(Vector newStrings)
    {
    	java.util.List strings = internalGetFilterStrings();
    	strings.clear();
    	for (int idx=0; idx<newStrings.size(); idx++)
    	{
           String currString = (String)newStrings.elementAt(idx);
           ISystemFilterString string = new SystemFilterString();
        	   // FIXME initMOF().createSystemFilterString();
           string.setString(currString);
           string.setParentSystemFilter(this);
           strings.add(string);               
    	}    	
    	invalidateCache();
    }
    /**
     * Get this filter's filter string objects as an array
     */
    public ISystemFilterString[] getSystemFilterStrings()
    {
    	if (filterStringObjectArray == null)
    	{
    	  java.util.List el = internalGetFilterStrings();
    	  filterStringObjectArray = new ISystemFilterString[el.size()];
    	  Iterator i = el.iterator();
    	  int idx = 0;
    	  while (i.hasNext())
    	    filterStringObjectArray[idx++] = (ISystemFilterString)(i.next());
    	}
    	return filterStringObjectArray;    	
    }    
    /**
     * Set all the filter strings for this filter.
     * @param newStrings array of String objects
     */
    public void setFilterStrings(String newStrings[])
    {
    	java.util.List strings = internalGetFilterStrings();
    	strings.clear();
    	for (int idx=0; idx<newStrings.length; idx++)
    	{
           addFilterString(newStrings[idx]);
    	}
    	//invalidateCache(); already done
    }
    private ISystemFilterString createFilterString(String string)
    {
        ISystemFilterString filterstring = new SystemFilterString();
        	
        	// FIXME initMOF().createSystemFilterString();
        filterstring.setString(string);
        filterstring.setParentSystemFilter(this);
        return filterstring;    	
    }
    /**
     * Append a new filter string to this filter's list
     */
    public ISystemFilterString addFilterString(String newString)
    {
    	java.util.List strings = internalGetFilterStrings();
    	ISystemFilterString newFilterString = createFilterString(newString);
        strings.add(newFilterString);
    	invalidateCache();    	
    	return newFilterString;    	
    }
    /**
     * Insert a new filter string to this filter's list, at the given zero-based position
     */
    public ISystemFilterString addFilterString(String newString, int position)
    {
    	java.util.List strings = internalGetFilterStrings();
    	ISystemFilterString newFilterString = createFilterString(newString);
    	strings.add(position, newFilterString);
    	invalidateCache();    	
    	return newFilterString;
    }
    /**
     * Update a new filter string's string value
     */
    public void updateFilterString(ISystemFilterString filterString, String newValue)
    {
    	filterString.setString(newValue);
    }

    /**
     * Delete a filter string from this filter's list.
     * @return the SystemFilterString object deleted, or null if not found
     */
    public ISystemFilterString removeFilterString(String oldString)
    {
    	java.util.List strings = internalGetFilterStrings();
    	ISystemFilterString match = null;
    	Iterator i = strings.iterator();
    	while ((match==null) && (i.hasNext()))
    	{
    		ISystemFilterString currstring = (ISystemFilterString)i.next();
    		if (currstring.getString().equals(oldString))
    		  match = currstring;
    	}
    	if (match!=null)
    	{
    	  strings.remove(match);
    	  invalidateCache();   
    	}
    	return match; 	
    }

    /**
     * Remove a filter string from this filter's list, given its zero-based position
     * @return the SystemFilterString object deleted, or null if not found
     */
    public ISystemFilterString removeFilterString(int position)
    {    	
    	java.util.List strings = internalGetFilterStrings();
    	if (position >= strings.size())
    	  return null;
    	ISystemFilterString filterString = (ISystemFilterString)strings.get(position);
    	strings.remove(position);
    	invalidateCache();    	
    	return filterString;
    }

    /**
     * Remove a filter string from this filter's list, given its SystemFilterString object.
     * @return true if the given string existed and hence was deleted.
     */
    public boolean removeFilterString(ISystemFilterString filterString)
    {    	
    	java.util.List strings = internalGetFilterStrings();
    	if (strings.contains(filterString))
    	{
    	  strings.remove(filterString);
    	  invalidateCache();
    	  return true;
    	}
    	else
    	  return false;
    }
    /**
     * Move a given filter string to a given zero-based location
     */
    public void moveSystemFilterString(int pos, ISystemFilterString filterString)
    {
    	//FIXME internalGetFilterStrings().move(pos,filterString);    	
    	invalidateCache();
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
     * Return the children of this filter.
     * This is all nested filters and all filter strings.
     */
    public Object[] getChildren()
    {
    	Vector strings = getFilterStringsVector();
    	Vector filters = getSystemFiltersVector();
    	Vector vChildren = new Vector();

        // start with nested filters...
    	for (int idx=0; idx < filters.size(); idx++)
    	   vChildren.addElement(filters.elementAt(idx));  
    	// continue with resolved filter string objects...
    	for (int idx=0; idx < strings.size(); idx++)
    	{
    	   String filterString = (String)strings.elementAt(idx);
    	   vChildren.addElement(filterString);
    	}
    	
    	// convert whole thing to an array...
    	Object[] children = new Object[vChildren.size()];
    	for (int idx=0; idx<vChildren.size(); idx++)
    	   children[idx] = vChildren.elementAt(idx);
    	   
    	return children;
    }
    
    /**
     * Returns true if this filter has any nested filters or any filter strings
     */
    public boolean hasChildren()
    {
    	if (internalGetFilterStrings().size() > 0)
    	  return true;
    	else 
    	  return helpers.hasSystemFilters(internalGetFilters());
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
     * Return the filter pool manager managing this collection of filter pools and their filters.
     * To save space, we delegate this query to our parent filter pool.
     */
    public ISystemFilterPoolManager getSystemFilterPoolManager()
    {
    	ISystemFilterPool pool = getParentFilterPool();
    	if (pool != null)
    	  return pool.getSystemFilterPoolManager();
    	else
    	  return null;
    }    

    // -----------------------
    // SAVE/RESTORE METHODS...
    // -----------------------
 
  
    


    /**
     * Restore specific filter. Used when save policy is SAVE_POLICY_ONE_FILE_PER_FILTER
     * @param mofHelpers helper object with routines for saving/restoring using mof.
     * @param folder the folder containing the saved file.
     * @param name The name of the saved filter. The file name is derived from this.
     * @param parentPool the SystemFilterPool that is the parent of this filter. Will be perpetuated to nested filters.
     * @param namingPolicy Tells us how to derive file name from filter name. Can be null for default prefix name.
     * @return SystemFilter object if restored ok, null if error encountered. If null, call getLastException().
     */
    public static ISystemFilter restore(IFolder folder, String name, ISystemFilterPool parentPool, 
                                       ISystemFilterNamingPolicy namingPolicy)
           throws Exception
    {
    	/* FIXME
        String fileName = getRootSaveFileName(namingPolicy, name);
        
    	java.util.List ext = mofHelpers.restore(folder,fileName);
    	
        // should be exactly one...
        Iterator iList = ext.iterator();
        SystemFilter filter = (SystemFilter)iList.next();
        if (parentPool != null)
          filter.setParentFilterPool(parentPool);                
        ((SystemFilterImpl)filter).initializeFilterStrings();
        return filter;
        */
    	return null;
    }


    /**
     * Return the root save file name without the extension .xmi
     */
    protected static String getRootSaveFileName(ISystemFilter filter)
    {
        return getRootSaveFileName(getNamingPolicy(filter), filter.getName());
    }
    /**
     * Return the root save file name without the extension .xmi
     */
    protected static String getRootSaveFileName(ISystemFilterNamingPolicy namingPolicy, String name)
    {
    	return namingPolicy.getFilterSaveFileName(name);
    }


    /**
     * Return naming policy
     */
    protected static ISystemFilterNamingPolicy getNamingPolicy(ISystemFilter filter)
    {
    	return filter.getParentFilterPool().getNamingPolicy();
    }

  

    /**
     * Ensure given path ends with path separator.
     */
    public static String addPathTerminator(String path)
    {
        if (!path.endsWith(File.separator))
          path = path + File.separatorChar;
        //else
        //  path = path;
        return path;
    }    

    /**
     * Return string identifying this filter
     */
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
	
	public String getDescription()
	{
		return SystemResources.RESID_MODELOBJECTS_FILTER_DESCRIPTION;
	}

	/**
	 * @generated This field/method will be replaced during code generation.
	 */
	public void setName(String newName)
	{
		name = newName;
	}

	/**
	 * @generated This field/method will be replaced during code generation.
	 */
	public void setType(String newType)
	{
		type = newType;
		_isDirty = true;
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
	public void setSupportsNestedFilters(boolean newSupportsNestedFilters)
	{
		boolean oldSupportsNestedFilters = supportsNestedFilters;
		if (oldSupportsNestedFilters != newSupportsNestedFilters)
		{
			supportsNestedFilters = newSupportsNestedFilters;
			setDirty(true);
		}
	}

	/**
	 * @generated This field/method will be replaced during code generation 
	 * When saving one filter per file, this captures this filter's relative order
	 * within the pool, as the file system cannot capture this.
	 */
	public int getRelativeOrder()
	{
		return relativeOrder;
	}

	/**
	 * @generated This field/method will be replaced during code generation.
	 */
	public void setRelativeOrder(int newRelativeOrder)
	{
		relativeOrder = newRelativeOrder;
	}

	/**
	 * @generated This field/method will be replaced during code generation 
	 * Is this a vendor-supplied filter versus a user-defined filter
	 */
	public boolean isDefault()
	{
		return default_;
	}

	/**
	 * @generated This field/method will be replaced during code generation.
	 */
	public void setDefault(boolean newDefault)
	{
		default_ = newDefault;
	}

	/**
	 * @generated This field/method will be replaced during code generation.
	 */
	public void setStringsCaseSensitive(boolean newStringsCaseSensitive)
	{
		boolean oldStringsCaseSensitive = stringsCaseSensitive;
		if (oldStringsCaseSensitive != newStringsCaseSensitive)
		{
			stringsCaseSensitive = newStringsCaseSensitive;
			setDirty(true);
		}

	}

	/**
	 * @generated This field/method will be replaced during code generation.
	 */
	public void unsetStringsCaseSensitive()
	{
		boolean oldStringsCaseSensitive = stringsCaseSensitive;
		if (oldStringsCaseSensitive != STRINGS_CASE_SENSITIVE_EDEFAULT)
		{
			stringsCaseSensitive = STRINGS_CASE_SENSITIVE_EDEFAULT;
			setDirty(true);
		}
	}

	/**
	 * @generated This field/method will be replaced during code generation.
	 */
	public boolean isSetStringsCaseSensitive()
	{
		return stringsCaseSensitive;
	}

	/**
	 * @generated This field/method will be replaced during code generation 
	 * If true, the user is prompted when this filter is expanded
	 */
	public boolean isPromptable()
	{
		return promptable;
	}

	/**
	 * @generated This field/method will be replaced during code generation.
	 */
	public void setPromptable(boolean newPromptable)
	{
		promptable = newPromptable;
	}

	/**
	 * @generated This field/method will be replaced during code generation 
	 */
	public ISystemFilter getParentFilter()
	{
		//FIXME
		return _parentFilter;
		//if (eContainerFeatureID != FiltersPackage.SYSTEM_FILTER__PARENT_FILTER) return null;
		//return (SystemFilter)eContainer;
	}

	/**
	 * @generated This field/method will be replaced during code generation.
	 */
	public void setParentFilter(ISystemFilter newParentFilter)
	{
		_parentFilter = newParentFilter;
		/* FIXME
		if (newParentFilter != eContainer || (eContainerFeatureID != FiltersPackage.SYSTEM_FILTER__PARENT_FILTER && newParentFilter != null))
		{
			if (EcoreUtil.isAncestor(this, newParentFilter))
				throw new IllegalArgumentException("Recursive containment not allowed for " + toString());
			NotificationChain msgs = null;
			if (eContainer != null)
				msgs = eBasicRemoveFromContainer(msgs);
			if (newParentFilter != null)
				msgs = ((InternalEObject)newParentFilter).eInverseAdd(this, FiltersPackage.SYSTEM_FILTER__NESTED_FILTERS, SystemFilter.class, msgs);
			msgs = eBasicSetContainer((InternalEObject)newParentFilter, FiltersPackage.SYSTEM_FILTER__PARENT_FILTER, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, FiltersPackage.SYSTEM_FILTER__PARENT_FILTER, newParentFilter, newParentFilter));
			*/
	}

	/**
	 * @generated This field/method will be replaced during code generation 
	 */
	public java.util.List getNestedFilters()
	{
		if (nestedFilters == null)
		{
			nestedFilters = new ArrayList();
			//FIXME new EObjectContainmentWithInversejava.util.List(SystemFilter.class, this, FiltersPackage.SYSTEM_FILTER__NESTED_FILTERS, FiltersPackage.SYSTEM_FILTER__PARENT_FILTER);
		}
		return nestedFilters;
	}

	/**
	 * @generated This field/method will be replaced during code generation 
	 */
	public java.util.List getStrings()
	{
		if (strings == null)
		{
			strings = new ArrayList();
			//FIXME new EObjectContainmenteList(SystemFilterString.class, this, FiltersPackage.SYSTEM_FILTER__STRINGS);
		}
		return strings;
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
	public void setSupportsDuplicateFilterStrings(boolean newSupportsDuplicateFilterStrings)
	{
		boolean oldSupportsDuplicateFilterStrings = supportsDuplicateFilterStrings;
		if (oldSupportsDuplicateFilterStrings != newSupportsDuplicateFilterStrings)
		{
			supportsDuplicateFilterStrings = newSupportsDuplicateFilterStrings;
			setDirty(true);
		}
	}

	/**
	 * @generated This field/method will be replaced during code generation 
	 */
	public boolean isNonDeletable()
	{
		return nonDeletable;
	}

	/**
	 * @generated This field/method will be replaced during code generation.
	 */
	public void setNonDeletable(boolean newNonDeletable)
	{
		nonDeletable = newNonDeletable;
	}

	/**
	 * @generated This field/method will be replaced during code generation 
	 */
	public boolean isNonRenamable()
	{
		return nonRenamable;
	}

	/**
	 * @generated This field/method will be replaced during code generation.
	 */
	public void setNonRenamable(boolean newNonRenamable)
	{
		nonRenamable = newNonRenamable;
	}

	/**
	 * @generated This field/method will be replaced during code generation 
	 */
	public boolean isNonChangable()
	{
		return nonChangable;
	}

	/**
	 * @generated This field/method will be replaced during code generation.
	 */
	public void setNonChangable(boolean newNonChangable)
	{
		nonChangable = newNonChangable;
	}

	/**
	 * @generated This field/method will be replaced during code generation 
	 * Are the filter strings within this filter non-changable by the user. If true,
	 * strings can be deleted, added, edited or reordered.
	 */
	public boolean isStringsNonChangable()
	{
		return stringsNonChangable;
	}

	/**
	 * @generated This field/method will be replaced during code generation.
	 */
	public void setStringsNonChangable(boolean newStringsNonChangable)
	{
		boolean oldStringsNonChangable = stringsNonChangable;
		if (oldStringsNonChangable != newStringsNonChangable)
		{
			stringsNonChangable = newStringsNonChangable;
			setDirty(true);
		}
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
	 *  it is queried from the parent pool.
	 */
	public boolean isSingleFilterStringOnly()
	{
		if (isSetSingleFilterStringOnly())
		  return singleFilterStringOnly;
		else
		  return getSystemFilterPool().isSingleFilterStringOnly();
	}
	
	public boolean isSingleFilterStringOnlyGen()
	{
		return singleFilterStringOnly;
	}

	public void setSingleFilterStringOnly(boolean newSingleFilterStringOnly)
	{
		boolean oldSingleFilterStringOnly = singleFilterStringOnly;
		if (oldSingleFilterStringOnly != newSingleFilterStringOnly)
		{
			singleFilterStringOnly = newSingleFilterStringOnly;
			setDirty(true);
		}	
	}


	public void unsetSingleFilterStringOnly()
	{
		boolean oldSingleFilterStringOnly = singleFilterStringOnly;
		if (oldSingleFilterStringOnly != SINGLE_FILTER_STRING_ONLY_EDEFAULT)
		{
			singleFilterStringOnly = SINGLE_FILTER_STRING_ONLY_EDEFAULT;
			setDirty(true);
		}

	}

	public boolean isSetSingleFilterStringOnly()
	{
		return singleFilterStringOnly;
	}
	
	/**
	 * Inidcates whether this filter needs to be saved
	 */
	public boolean isDirty()
	{
		return _isDirty;
	}
	
	public void setDirty(boolean dirtyFlag)
	{
		_isDirty = dirtyFlag;
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
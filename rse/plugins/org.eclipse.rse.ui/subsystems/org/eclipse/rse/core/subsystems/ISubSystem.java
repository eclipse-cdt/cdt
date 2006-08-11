/********************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.core.subsystems;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.rse.core.model.IRSEModelObject;
import org.eclipse.rse.core.persistance.IRSEPersistableContainer;
import org.eclipse.rse.filters.ISystemFilter;
import org.eclipse.rse.filters.ISystemFilterPoolReferenceManager;
import org.eclipse.rse.filters.ISystemFilterPoolReferenceManagerProvider;
import org.eclipse.rse.filters.ISystemFilterReference;
import org.eclipse.rse.filters.ISystemFilterString;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.model.ISystemProfile;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.PropertyPage;



/**
 * Interface implemented by SubSystem objects. While connections contain information to identify a 
 *   particular remote system, it is the subsystem objects within a connection that contain information
 *   unique to a particular tool, for that remote system, such as the port the tool uses and the 
 *   user ID for making the connection. There are a set of default properties, but these can be
 *   extended by subsystem providers, by extending {@link SubSystem}. 
 * <p>
 */
public interface ISubSystem extends ISystemFilterPoolReferenceManagerProvider, IRemoteObjectResolver, ISchedulingRule, IRSEModelObject, IRSEPersistableContainer
{	
	// -------------------------------------
	// Shortcut and helper methods...
	// -------------------------------------
    /**
     * Return the parent subsystem factory that owns this subsystem.
     */
    public ISubSystemConfiguration getSubSystemConfiguration();
    /**
     * Set the parent subsystem factory that owns this subsystem.
     */
    public void setSubSystemConfiguration(ISubSystemConfiguration ssf);
    /**
     * Set the parent connection that owns this subsystem.
     */
    public void setHost(IHost conn);
    
    /**
     * Set the connector service for this subsystem
     * @param connectorService
     */
	public void setConnectorService(IConnectorService connectorService);
	
	/**
	 * Return the system profile object this subsystem is associated with.
	 * @see #getName()
	 */
	public ISystemProfile getSystemProfile();
	/**
	 * Return the connection object this subsystem is associated with.
	 */
	public IHost getHost();
	
	/**
	 * Called on each subsystem associated with a particular ISystem after it connects
	 */
	public void initializeSubSystem(IProgressMonitor monitor);
	
	/**
	 * Called on each subsystem associated with a particular ISystem after it disconnects
	 */
	public void uninitializeSubSystem(IProgressMonitor monitor);
	
	/**
	 * @return true if this subsystem's properties should take precedence over other subsystems that share the same ISystem
	 */
	public boolean isPrimarySubSystem();
	/**
	 * Return the primary subsystem associated with this subsystem's ISystem
	 */
	public ISubSystem getPrimarySubSystem();
	
	/**
	 * @return The name of the connection that owns this. Same as getSystemConnection().getAliasName()
	 */
	public String getHostAliasName();
	/**
	 * @return The value of the profile that owns the connection that owns this subsystem. Fastpath.
	 */
	public String getSystemProfileName();	
	/**
	 * Private method called when the parent profile is being renamed, so
	 * the subsystem can do any cleanup it needs to. Called after the profile is actually renamed.
	 */
	public void renamingProfile(String oldName, String newName);	
	/**
	 * Private method called when the parent connection is being renamed, so
	 * the subsystem can do any cleanup it needs to.
	 */
	public void renamingConnection(String newName);	
	/**
	 * Private method called when the parent connection is being deleted, so
	 * the subsystem can do any pre-death cleanup it needs to.
	 */
	public void deletingConnection();
    /**
     * This is a helper method you can call when performing actions that must be certain there
     * is a connection. If there is no connection it will attempt to connect, and if that fails
     * will throw a SystemMessageException you can easily display to the user by using a method
     * in it.
     */
    public void checkIsConnected() throws SystemMessageException;
    
	// -------------------------------------
	// GUI methods 
	// -------------------------------------
	/**
	 * Return the single property page to show in the tabbed notebook for the
	 *  for SubSystem property of the parent Connection. Return null if no 
	 *  page is to be contributed for this. You are limited to a single page,
	 *  so you may have to compress. It is recommended you prompt for the port
	 *  if applicable since the common base subsystem property page is not shown
	 *  To help with this you can use the SystemPortPrompt widget.
	 */
    public PropertyPage getPropertyPage(Composite parent);

	// ---------------------------------------------------
	// Methods for business partners to add their own 
	//  persisted attributes to the subsystem object...
	// ---------------------------------------------------    
	
	/**
	 * For business partners defining their own subsystems.
	 * This method allows an attribute to be persisted in this
	 * subsystem, given the following information:
	 * <ul>
	 *   <li>Vendor name. This name should uniquely differentiate one
	 *           vendor's attributes from anothers.
	 *   <li>Attribute name. The name of the attribute to set.
	 *   <li>attribute value. The value to give the named attribute. It must 
	 *           be resolved into a string to use this. Eg, for boolean use
	 *           something like "true" or "false". To clear the attribute
	 *           value pass null for the value.
	 * </ul>
	 * <b>Warning</b> do not use any of the following characters in any of
	 *  given parameters, or it will cause problems with parsing:
	 * <ul>
	 *   <li>Pound sign ('#')
	 *   <li>Three underscores ("___")
	 *   <li>Three equals signs ("===")
	 *   <li>Three semicolons (";;;")
	 * </ul>
	 */
	public void setVendorAttribute(String vendorName, 
	                                String attributeName, String attributeValue);
	/**
	 * For business partners defining their own subsystems.
	 * This method allows retrieval of a persisted attribute in this
	 * subsystem, given the following information:
	 * <ul>
	 *   <li>Vendor name. This name should uniquely differentiate one
	 *           vendor's attributes from anothers.
	 *   <li>Attribute name. The name of the attribute whose value is being queried.
	 * </ul>
	 * @return value of the attribute being queried, or null if not found
	 */
	public String getVendorAttribute(String vendorName, String attributeName);
    

		
	// -------------------------------------
	// Context and attributue information...
	// -------------------------------------

    /**
     * Return true if userId and password should be forced to uppercase.
     * Shortcut to calling same method in parent SubSystemConfiguration.
     */
    public boolean forceUserIdToUpperCase();
    /**
     * Alternative to getUserId when we don't want to resolve it from parent connection.
     * This is used when showing the properties.
     * <p>
     * Unlike getUserId() this one does not defer to the connection's default user Id if
     * the subsystem's userId attribute is null.
     * <p>
     * To set the local user Id, simply call setUserId(String id). To clear it, call
     * {@link #clearLocalUserId()}.
     * <p>
	 * @see org.eclipse.rse.model.IHost#getDefaultUserId()
	 * @see #clearLocalUserId()
	 * @see #getUserId()
	 * @see #setUserId(String)
     */
    public String getLocalUserId();
    /**
     * Called to clear the local user Id such that subsequent requests to getUserId() will
     * return the parent connection's default user Id. Sets the user Id attribute for this
     * subsystem to null.
     * <p>
	 * @see org.eclipse.rse.model.IHost#getDefaultUserId()
	 * @see #getUserId()
	 * @see #getLocalUserId()
	 * @see #setUserId(String)
     */
    public void clearLocalUserId();
	/**
	 * Return the children for this subsystem.
	 * This is used to populate the Remote System View explorer.
	 * <p>
	 * By default, if the parent subsystem factory supports filters, then
	 *  we return getSystemFilterPoolReferencesArray. If filters are not
	 *  supported (supportsFilters() returns false from factory) then we
	 *  return null. In this case you should override this.
	 */
	public Object[] getChildren();
	/**
	 * Return true if this subsystem has children objects to
	 *  be displayed in the Remote System View explorer.
	 */
	public boolean hasChildren();	

	/**
     * Return true if the given filter lists the contents of the given remote object.
     *  For example, if given a folder, return true if any of the filter strings in this filter 
     *  lists the contents of that folder. Used in impact analysis when a remote object is 
     *  created, deleted, renamed, copied or moved, so as to establish which filters need to be
     *  refreshed or collapsed (if the folder is deleted, say).
     * <p>
     * The default algorithm calls doesFilterStringListContentsOf for each filter string.
     */
    public boolean doesFilterListContentsOf(ISystemFilter filter, String remoteObjectAbsoluteName);
    /**
     * Return true if the given filter string lists the contents of the given remote object.
     *  For example, if given a folder, return true if the given filter string
     *  lists the contents of that folder. Used in impact analysis when a remote object is 
     *  created, deleted, renamed, copied or moved, so as to establish which filters need to be
     *  refreshed or collapsed (if the folder is deleted, say).
     */
    public boolean doesFilterStringListContentsOf(ISystemFilterString filterString, String remoteObjectAbsoluteName);
    
    /**
     * Return true if the given remote object name will pass the filtering criteria for any of 
     *  the filter strings in this filter.
     */
    public boolean doesFilterMatch(ISystemFilter filter, String remoteObjectAbsoluteName);
    /**
     * Return true if the given remote object name will pass the filtering criteria for the 
     *  given filter string in this filter.
     */
    public boolean doesFilterStringMatch(String filterString, String remoteObjectAbsoluteName, boolean caseSensitive);
    	
	// -------------------------
	// Filter Pool References...
	// -------------------------
	/**
	 * Return the system filter pool reference manager, as per the
	 * interface SystemFilterPoolReferenceManagerProvider
	 */
	public ISystemFilterPoolReferenceManager getSystemFilterPoolReferenceManager();

	/**
	 * @return The value of the Name attribute
	 */
	public String getName();

	/**
	 * @param value The new value of the Name attribute
	 */
	public void setName(String value);

	/**
	 * Returns the value of this subsystem's user id if it is not null. If it
	 * is null, it returns the parent connection object's default user Id.
     * <p>
     * In fact, we now don't store the user Id in the subsystem object itself, but rather store it in the
     * user preferences, so that such things are not shared among the team on a synchronize operation.
     * This is transparent to callers of this method however, as this method resolves from the preferences.
     *
	 * @see org.eclipse.rse.model.IHost#getDefaultUserId()
	 * @see #setUserId(String)
	 * @see #getLocalUserId()
	 * @see #clearLocalUserId()
	 * @return The value of the UserId attribute
	 */
	public String getUserId();

	/**
	 * @return The value of the FactoryId attribute
	 * Ties this subsystem to its owning subsystemconfiguration, via the
	 * id key string of the factory
	 */
	public String getConfigurationId();

	/**
	 * @param value The new value of the FactoryId attribute
	 */
	public void setConfigurationId(String value);

	// ---------------------------------------------------
	// The following methods relate to the live connection
	// ---------------------------------------------------
	/**
	 * Return the ISystem object that represents the live connection for this system.
	 */
	public IConnectorService getConnectorService();

	/**
	 * Check if the SubSystem supports caching.
	 */
	public boolean supportsCaching();
	
	/**
	 * Return the CacheManager for this subsystem.  If the SubSystem returns true for 
	 * supportsCaching() then it must return a valid CacheManager, otherwise it is free
	 * to return null.
	 * 
	 * @see #supportsCaching() 
	 */
	public ICacheManager getCacheManager();

	/**
	 * Return true if this subsystem is currently connected to its remote system.
	 */
	public boolean isConnected();
	
	/**
	 * Return true if the last attempt to connect this subsystem to its remote system failed.
	 */
	public boolean isConnectionError();
	
	/**
	 * Sets whether the last attempt to connect this subsystem to its remote system failed.
	 */
	public void setConnectionError(boolean error);
	
	/**
	 * Return true if this subsystem is currently being used in "offline" mode. Not necessarily supported
	 *  by all subsystems in which case this will always return false.
	 */
	public boolean isOffline();
	/**
	 * Connect to the remote system.
	 * In addition to calling getSystem().connect(),this might fire events.
	 * 
     * @param Shell parent shell used to show error message. Null means you will handle showing the error message.
	 */
	public void connect(Shell shell) throws Exception;	
	/**
	 * Connect to the remote system.
	 * This uses Display.syncExec to get an active Shell and then calls connect(Shell)
	 */
	public void connect() throws Exception;	
			
	/**
	 * Connect to the remote system, optionally forcing a signon prompt even if the password
	 * is cached in memory or on disk.
	 * 
     * @param Shell parent shell used to show error message. Null means you will handle showing the error message.
     * @param forcePrompt forces the prompt dialog to be displayed even if the password is currently
	 * in memory.
	 */
	public void connect(Shell shell, boolean forcePrompt) throws Exception;	
	

	/**
	 * Disconnect from the remote system.
	 * In addition to calling getSystem().disconnect(),this must fire an
	 *  event to collapse the expanded nodes in the tree under this node.
	 */
	public void disconnect(Shell shell) throws Exception;
	
	/**
	 * Disconnect from the remote system.
	 * In addition to calling getSystem().disconnect(),this may fire an
	 * event to collapse the expanded nodes in the tree under this node
	 * depending on the value of collapseTree.
	 */
	public void disconnect(Shell shell, boolean collapseTree) throws Exception;
	
	
	
    /**
     * Modal thread version of resolve filter strings
     * Resolve an absolute filter string. This is only applicable if the subsystem
     *  factory reports true for supportsFilters().
     * <p>
     * When a user expands a filter containing filter strings, this method is
     *  invoked for each filter string.
     * <p>
     * The resulting objects are displayed in the remote system view tree. They
     *  can be anything, but at a minimum must support IAdaptable in order to
     *  drive the property sheet. You can just defer the getAdapter request to
     *  the platform's Adapter manager if desired.
     * <p>
     * You should supply an adapter class for the returned object's class,
     *  to render objects in the Remote System Explorer view. It will uses a
     *  label and content provider that defers all requests to the adapter,
     *  which it gets by querying the platform's adapter manager for the object
     *  type. Be sure to register your adapter factory.
     *
     * @param monitor the process monitor associated with this operation
     * @param filterString filter pattern for objects to return.
     * @return Array of objects that are the result of this filter string
     */
    public Object[] resolveFilterString(IProgressMonitor monitor, String filterString)
           throws Exception;

    /**
     * Modal thread version of resolve filter strings
     * Resolve an absolute filter string. This is only applicable if the subsystem
     *  factory reports true for supportsFilters().
     * <p>
     * When a user expands a filter containing filter strings, this method is
     *  invoked for each filter string.
     * <p>
     * The resulting objects are displayed in the remote system view tree. They
     *  can be anything, but at a minimum must support IAdaptable in order to
     *  drive the property sheet. You can just defer the getAdapter request to
     *  the platform's Adapter manager if desired.
     * <p>
     * You should supply an adapter class for the returned object's class,
     *  to render objects in the Remote System Explorer view. It will uses a
     *  label and content provider that defers all requests to the adapter,
     *  which it gets by querying the platform's adapter manager for the object
     *  type. Be sure to register your adapter factory.
     *
     * @param monitor the process monitor associated with this operation
     * @param filterStrings filter patterns for objects to return.
     * @return Array of objects that are the result of this filter string
     */
    public Object[] resolveFilterStrings(IProgressMonitor monitor, String[] filterStrings)
           throws Exception;

    /**
     * Modal thread version of resolve filter strings
     * Resolve an absolute filter string. This is only applicable if the subsystem
     *  factory reports true for supportsFilters().
     * <p>
     * When a user expands a filter containing filter strings, this method is
     *  invoked for each filter string.
     * <p>
     * The resulting objects are displayed in the remote system view tree. They
     *  can be anything, but at a minimum must support IAdaptable in order to
     *  drive the property sheet. You can just defer the getAdapter request to
     *  the platform's Adapter manager if desired.
     * <p>
     * You should supply an adapter class for the returned object's class,
     *  to render objects in the Remote System Explorer view. It will uses a
     *  label and content provider that defers all requests to the adapter,
     *  which it gets by querying the platform's adapter manager for the object
     *  type. Be sure to register your adapter factory.
     *
     * @param monitor the process monitor associated with this operation
     * @param parent the parent object to query
     * @param filterString filter pattern for objects to return.
     * @return Array of objects that are the result of this filter string
     */
    public Object[] resolveFilterString(IProgressMonitor monitor, Object parent, String filterString)
           throws Exception;
	
    /**
     * Resolve an absolute filter string. This is only applicable if the subsystem
     *  factory reports true for supportsFilters().
     * <p>
     * When a user expands a filter containing filter strings, this method is
     *  invoked for each filter string.
     * <p>
     * The resulting objects are displayed in the remote system view tree. They
     *  can be anything, but at a minimum must support IAdaptable in order to
     *  drive the property sheet. You can just defer the getAdapter request to
     *  the platform's Adapter manager if desired.
     * <p>
     * You should supply an adapter class for the returned object's class,
     *  to render objects in the Remote System Explorer view. It will uses a
     *  label and content provider that defers all requests to the adapter,
     *  which it gets by querying the platform's adapter manager for the object
     *  type. Be sure to register your adapter factory.
     *
     * @param filterString filter pattern for objects to return.
     * @param Shell parent shell used to show error message. Null means you will handle showing the error message.
     * @return Array of objects that are the result of this filter string
     */
    public Object[] resolveFilterString(String filterString, Shell shell)
           throws Exception;
    /**
     * Resolve multiple absolute filter strings. This is only applicable if the subsystem
     *  factory reports true for supportsFilters().
     * <p>
     * This is the same as {@link #resolveFilterString(String,Shell)} but takes an array of
     *  filter strings versus a single filter string.
     *
     * @param filterStrings array of filter patterns for objects to return.
     * @param Shell parent shell used to show error message. Null means you will handle showing the error message.
     * @return Array of objects that are the result of resolving all the filter strings
     */
    public Object[] resolveFilterStrings(String[] filterStrings, Shell shell)
           throws Exception;

    /**
     * Resolve an relative filter string. This is only applicable if the subsystem
     *  factory reports true for supportsFilters().
     * <p>
     * When a user expands an object that came from a previous filter string expansion,
     *  (such as expanding a folder), this method is invoked to get the children of
     *  that object. The user can choose to expand all, or choose a pre-defined
     *  relative filter string to subset/filter the children. In either case, the
     *  relative filter string is passed in as well as the to-be-expanded parent object.
     * <p>
     * The resulting objects are displayed in the remote system view tree. They
     *  can be anything, but at a minimum must support IAdaptable in order to
     *  drive the property sheet. You can just defer the getAdapter request to
     *  the platform's Adapter manager if desired.
     * <p>
     * You should supply an adapter class for the returned object's class,
     *  to render objects in the Remote System Explorer view. It will uses a
     *  label and content provider that defers all requests to the adapter,
     *  which it gets by querying the platform's adapter manager for the object
     *  type. Be sure to register your adapter factory.
     *
     * @param parent Object that is being expanded.
     * @param filterString filter pattern for children of parent.
     * @param Shell parent shell used to show error message. Null means you will handle showing the error message.
     * @return Array of objects that are the result of this filter string
     */
    public Object[] resolveFilterString(Object parent, String filterString, Shell shell)
           throws Exception;

    /*
     * Execute a remote command. This is only applicable if the subsystem factory reports
     *  true for supportsCommands().
     * @param command Command to be executed remotely.
     * @param Shell parent shell used to show error message. Null means you will handle showing the error message.
     * @param Object context context of a command (i.e. working directory).  Null is valid and means to use the default context.
     * @return Array of objects that are the result of running this command. Typically, these
     *   are messages logged by the command.
     *
    public Object[] runCommand(String command, Shell shell, Object context)
           throws Exception;

	/*
	 * Provide list of executed commands on subsystem.This is only applicable if the subsystem factory reports
     *  true for supportsCommands().
	 *
	public String[] getExecutedCommands();
	*/
	
    /**
     * Set a remote property. Subsystems interpret as they wish. Eg, this might be to set
     *  a remote environment variable. This is only applicable if the subsystem factory reports
     *  true for supportsProperties().
     * @param subject Identifies which object to get the properties of
     * @param key Identifies property to set
     * @param value Value to set property to
     * @param Shell parent shell used to show error message. Null means you will handle showing the error message.
     * @return Object interpretable by subsystem. Might be a Boolean, or the might be new value for confirmation.
     */
    public Object setProperty(Object subject, String key, String value, Shell shell)
           throws Exception;

    /**
     * Get a remote property. Subsystems interpret as they wish. Eg, this might be to get
     *  a remote environment variable. This is only applicable if the subsystem factory reports
     *  true for supportsProperties().
     * @param subject Identifies which object to get the properties of
     * @param key Identifies property to get value of
     * @param Shell parent shell used to show error message. Null means you will handle showing the error message.
     * @return String The value of the requested key.
     */
    public String getProperty(Object subject, String key, Shell shell)
           throws Exception;

    /**
     * Set multiple remote properties. Subsystems interpret as they wish. Eg, this might be to set
     *  a number of remote environment variables. This is only applicable if the subsystem factory reports
     *  true for supportsProperties().
     * @param subject Identifies which object to get the properties of
     * @param key Identifies property to set
     * @param value Values to set properties to. One to one mapping to keys by index number
     * @param Shell parent shell used to show error message. Null means you will handle showing the error message.
     * @return Object interpretable by subsystem. Might be a Boolean, or the might be new values for confirmation.
     */
    public Object setProperties(Object subject, String[] keys, String[] values, Shell shell)
           throws Exception;

    /**
     * Get a remote property. Subsystems interpret as they wish. Eg, this might be to get
     *  a remote environment variable. This is only applicable if the subsystem factory reports
     *  true for supportsProperties().
     * @param subject Identifies which object to get the properties of
     * @param key Identifies property to get value of
     * @param Shell parent shell used to show error message. Null means you will handle showing the error message.
     * @return Object The values of the requested keys.
     */
    public String[] getProperties(Object subject, String[] keys, Shell shell)
           throws Exception;
           
  	/**
	 * <i>Generated persistent property method</i><br>
	 * Return true if this subsystem is to be hidden so it doesn't show in the Remote Systems
	 * view when a connection is expanded. If so, this subsystem is for programmatic use only,
	 * or is exposed in alternative view. Such is the case for command subsystems, for example.
	 * @generated This field/method will be replaced during code generation 
	 * @return The value of the Hidden attribute
	 */
	boolean isHidden();

	/**
	 * <i>Generated persistent property method</i><br>
	 * Specify true if this subsystem is to be hidden so it doesn't show in the Remote Systems
	 * view when a connection is expanded. If so, this subsystem is for programmatic use only,
	 * or is exposed in alternative view. Such is the case for command subsystems, for example.
	 * @generated This field/method will be replaced during code generation 
	 * @param value The new value of the Hidden attribute
	 */
	void setHidden(boolean value);

	/**
	 * <i>Generated persistent property method</i><br>
	 * Return the object that manages the list of 
	 * filter pools referenced by this subsystem.
	 * @generated This field/method will be replaced during code generation 
	 * @return The FilterPoolReferenceManager reference
	 */
	ISystemFilterPoolReferenceManager getFilterPoolReferenceManager();

	/**
	 * <i>Generated persistent property method</i><br>
	 * Set the object that manages the list of 
	 * filter pools referenced by this subsystem. This is called by the subsystem factory
	 * when creating or restoring subsystems.
	 * @generated This field/method will be replaced during code generation 
	 * @param l The new value of the FilterPoolReferenceManager reference
	 */
	void setFilterPoolReferenceManager(ISystemFilterPoolReferenceManager value);

	

	

	
	/**
	 * Returns the parent object associated with a filter reference.  It's up to the
	 * subsystem implementation to decide what "parent object" means for a filter reference.
	 * @param filterRef the filter reference to determine a target object from.
	 * @return
	 */
	Object getTargetForFilter(ISystemFilterReference filterRef);
	
}
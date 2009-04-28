/********************************************************************************
 * Copyright (c) 2006, 2009 IBM Corporation and others. All rights reserved.
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
 * Martin Oberhuber (Wind River) - [182454] improve getAbsoluteName() documentation
 * Martin Oberhuber (Wind River) - [186128] Move IProgressMonitor last in all API
 * Martin Oberhuber (Wind River) - [187218] Fix error reporting for connect()
 * David McKnight (IBM) - [207095] Implicit connect needs to run in the same job as caller
 * David McKnight (IBM) - [186363] get rid of obsolete calls to ISubSystem.connect()
 * David Dykstal (IBM) - [217556] remove service subsystem types
 * Martin Oberhuber (Wind River) - [cleanup] Add API "since" Javadoc tags
 * David Dykstal (IBM) - [225089][ssh][shells][api] Canceling connection leads to exception
 * Martin Oberhuber (Wind River) - [218304] Improve deferred adapter loading
 * David McKnight   (IBM)        - [272882] [api] Handle exceptions in IService.initService()
 ********************************************************************************/

package org.eclipse.rse.core.subsystems;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.rse.core.filters.ISystemFilter;
import org.eclipse.rse.core.filters.ISystemFilterPoolReferenceManager;
import org.eclipse.rse.core.filters.ISystemFilterPoolReferenceManagerProvider;
import org.eclipse.rse.core.filters.ISystemFilterReference;
import org.eclipse.rse.core.filters.ISystemFilterString;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.IPropertySet;
import org.eclipse.rse.core.model.IRSECallback;
import org.eclipse.rse.core.model.IRSEModelObject;
import org.eclipse.rse.core.model.ISystemProfile;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;

/**
 * Interface implemented by SubSystem objects.
 *
 * While connections contain information to identify a particular remote system,
 * it is the subsystem objects within a connection that contain information
 * unique to a particular tool for that remote system, such as the port the tool
 * uses and the user ID for making the connection. There are a set of default
 * properties, but these can be extended by subsystem providers, by extending
 * SubSystem.
 *
 * @noimplement This interface is not intended to be implemented by clients.
 *              Clients must extend the abstract <code>SubSystem</code> class
 *              instead.
 */
public interface ISubSystem extends ISystemFilterPoolReferenceManagerProvider, IRemoteObjectResolver, ISchedulingRule, IRSEModelObject {
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
	 *
	 * @param connectorService connector service object to set
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
	 * Called on each subsystem associated with a particular
	 * {@link IConnectorService} after it connects successfully. This call is
	 * always made on a background Thread, so it's allowed to be long-running.
	 *
	 * @param monitor a progress monitor that can be used to show progress
	 *            during long-running operation. Cancellation is typically not
	 *            supported since it might leave the system in an inconsistent
	 *            state.
	 *            
	 * @throws SystemMessageException if an error occurs during initialization.
	 */
	public void initializeSubSystem(IProgressMonitor monitor) throws SystemMessageException;

	/**
	 * Called on each subsystem associated with a particular
	 * {@link IConnectorService} after it disconnects
	 *
	 * @param monitor a progress monitor that can be used to show progress
	 *            during long-running operation. Cancellation is typically not
	 *            supported since it might leave the system in an inconsistent
	 *            state.
	 */
	public void uninitializeSubSystem(IProgressMonitor monitor);

	/**
	 * @return true if this subsystem's properties should take precedence over other
	 * subsystems that share the same {@link IConnectorService}
	 */
	public boolean isPrimarySubSystem();

	/**
	 * Return the primary subsystem associated with this subsystem's IConnectorService
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
	 * Check if the subsystem is connected, and connect if it's not.
	 * 
	 * This is a convenience method which first checks whether the subsystem is
	 * already connected. If not, it automatically checks if it's running on the
	 * dispatch thread or not, and calls the right <code>connect()</code> method
	 * as appropriate. It also performs some exception parsing, converting
	 * Exceptions from connect() into SystemMessageException that can be
	 * displayed to the user by using a method in it.
	 *
	 * If the subsystem is marked offline, or supports caching and is currently
	 * restoring from its memento, no connect will be performed.
	 * 
	 * @throws SystemMessageException in case of an error connecting
	 * @since org.eclipse.rse.core 3.0
	 */
	public void checkIsConnected(IProgressMonitor monitor) throws SystemMessageException;

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
	 * @deprecated Subsystems can now take named property sets. These should be used instead.
	 * See {@link IPropertySet}
	 */
	public void setVendorAttribute(String vendorName, String attributeName, String attributeValue);

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
	 * @deprecated Subsystems can now take named property sets. These should be used instead.
	 * See {@link IPropertySet}
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
	 * @see IHost#getDefaultUserId()
	 * @see #clearLocalUserId()
	 * @see #getUserId()
	 */
	public String getLocalUserId();

	/**
	 * Called to clear the local user Id such that subsequent requests to getUserId() will
	 * return the parent connection's default user Id. Sets the user Id attribute for this
	 * subsystem to null.
	 * <p>
	 * @see IHost#getDefaultUserId()
	 * @see #getUserId()
	 * @see #getLocalUserId()
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
	 * @see IHost#getDefaultUserId()
	 * @see #getLocalUserId()
	 * @see #clearLocalUserId()
	 * @return The value of the UserId attribute
	 */
	public String getUserId();

	/**
	 * @return The value of the ConfigurationId attribute
	 * Ties this subsystem to its owning subsystem configuration, via the
	 * id key string of the configuration.
	 */
	public String getConfigurationId();

	/**
	 * @param value The new value of the ConfigurationId attribute
	 */
	public void setConfigurationId(String value);

	// ---------------------------------------------------
	// The following methods relate to the live connection
	// ---------------------------------------------------
	/**
	 * Return the IConnectorService object that represents the live connection for this system.
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
	 * Synchronously connect to the remote system.
	 *
	 * Clients are expected to call this method on a background
	 * thread with an existing progress monitor. A signon prompt
	 * may optionally be forced even if the password is cached
	 * in memory or on disk.
	 *
	 * The framework will take care of switching to the UI thread
	 * for requesting a password from the user if necessary.
	 *
	 * @param monitor the progress monitor. Must not be <code>null</code>.
	 * @param forcePrompt forces the prompt dialog to be displayed
	 * even if the password is currently in memory.
	 * @throws Exception an exception if there is a failure to connect.
	 * Typically, this will be a {@link SystemMessageException}.
	 * An {@link OperationCanceledException} will be thrown if the user cancels the connect.
	 */
	public void connect(IProgressMonitor monitor, boolean forcePrompt) throws Exception;

	/**
	 * Asynchronously connect to the remote system, optionally forcing a signon prompt
	 * even if the password is cached in memory or on disk.
	 * <p/>
	 * This method must be called on the UI Thread! An Eclipse background job with a
	 * progress monitor will be created automatically. If the optional callback is
	 * given, it will be called when the connect is complete.
	 * You do not need to override this, as it does the progress monitor reporting
	 * for you.
	 * <p/>
	 * Override internalConnect if you want, but by default it calls
	 * <code>getConnectorService().connect(IProgressMonitor)</code>.
	 *
	 * @param forcePrompt forces the prompt dialog even if the password is in mem
	 * @param callback to call after connect is complete.
	 *     May be <code>null</code>.
	 * @throws Exception an exception if there is a failure to connect.
	 * Typically, this will be a {@link SystemMessageException}.
	 * An {@link OperationCanceledException} will be thrown if the user cancels the connect.
	 */
	public void connect(boolean forcePrompt, IRSECallback callback) throws Exception;

	/**
	 * Disconnect from the remote system.
	 * In addition to calling getSystem().disconnect(),this must fire an
	 *  event to collapse the expanded nodes in the tree under this node.
	 */
	public void disconnect() throws Exception;

	/**
	 * Disconnect from the remote system.
	 * In addition to calling getSystem().disconnect(),this may fire an
	 * event to collapse the expanded nodes in the tree under this node
	 * depending on the value of collapseTree.
	 */
	public void disconnect(boolean collapseTree) throws Exception;

	/**
	 * Resolve an absolute filter string. This is only applicable if the subsystem
	 * factory reports true for supportsFilters().
	 * <p>
	 * When a user expands a filter containing filter strings, this method is
	 * invoked for each filter string.
	 * <p>
	 * The resulting objects are displayed in the remote system view tree. They
	 * can be anything, but at a minimum must support IAdaptable in order to
	 * drive the property sheet. You can just defer the getAdapter request to
	 * the platform's Adapter manager if desired.
	 * <p>
	 * You should supply an adapter class for the returned object's class,
	 * to render objects in the Remote System Explorer view. It will uses a
	 * label and content provider that defers all requests to the adapter,
	 * which it gets by querying the platform's adapter manager for the object
	 * type. Be sure to register your adapter factory.
	 *
	 * @param filterString filter pattern for objects to return.
	 * @param monitor the process monitor associated with this operation
	 * @return Array of objects that are the result of this filter string
	 */
	public Object[] resolveFilterString(String filterString, IProgressMonitor monitor) throws Exception;

	/**
	 * Resolve multiple absolute filter strings. This is only applicable if the subsystem
	 *  factory reports true for supportsFilters().
	 * <p>
	 * This is the same as {@link #resolveFilterString(String, IProgressMonitor)} but takes an array of
	 * filter strings versus a single filter string.
	 *
	 * @param filterStrings array of filter patterns for objects to return.
	 * @param monitor the process monitor associated with this operation
	 *
	 * @return Array of objects that are the result of this filter string
	 */
	public Object[] resolveFilterStrings(String[] filterStrings, IProgressMonitor monitor) throws Exception;

	/**
	 * Resolve a relative filter string. This is only applicable if the subsystem
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
	 * @param monitor the process monitor associated with this operation
	 *
	 * @return Array of objects that are the result of this filter string
	 */
	public Object[] resolveFilterString(Object parent, String filterString, IProgressMonitor monitor) throws Exception;

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
	 * @return Object interpretable by subsystem. Might be a Boolean, or the might be new value for confirmation.
	 *
	 * @deprecated this shouldn't be used
	 */
	public Object setProperty(Object subject, String key, String value) throws Exception;

	/**
	 * Get a remote property. Subsystems interpret as they wish. Eg, this might be to get
	 *  a remote environment variable. This is only applicable if the subsystem factory reports
	 *  true for supportsProperties().
	 * @param subject Identifies which object to get the properties of
	 * @param key Identifies property to get value of
	 * @return String The value of the requested key.
	 *
	 * @deprecated this shouldn't be used
	 */
	public String getProperty(Object subject, String key) throws Exception;

	/**
	 * Set multiple remote properties. Subsystems interpret as they wish. Eg, this might be to set
	 * a number of remote environment variables. This is only applicable if the subsystem factory reports
	 * true for supportsProperties().
	 * @param subject Identifies which object to get the properties of
	 * @param keys Identifies the properties to set
	 * @param values Values to set properties to. One to one mapping to keys by index number
	 * @return Object interpretable by subsystem. Might be a Boolean, or the might be new values for confirmation.
	 *
	 * @deprecated this shouldn't be used
	 */
	public Object setProperties(Object subject, String[] keys, String[] values) throws Exception;

	/**
	 * Get a remote property. Subsystems interpret as they wish. Eg, this might be to get
	 *  a remote environment variable. This is only applicable if the subsystem factory reports
	 *  true for supportsProperties().
	 * @param subject Identifies which object to get the properties of
	 * @param keys Identifies properties to get value of
	 * @return The values of the requested keys.
	 *
	 * @deprecated this shouldn't be used
	 */
	public String[] getProperties(Object subject, String[] keys) throws Exception;

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
	 * @return An object representing the parent
	 */
	Object getTargetForFilter(ISystemFilterReference filterRef);

	/**
	 * Returns the interface type (i.e. a Class object that is an Interface) of
	 * a service subsystem.
	 *
	 * @return the service interface on which this service subsystem is
	 *         implemented. If this subsystem is not a service subsystem it must
	 *         return <code>null</code>.
	 * @since org.eclipse.rse.core 3.0
	 */
	public Class getServiceType();

	/**
	 * Requests a service subsystem to switch to a new configuration. If the
	 * configuration is compatible with this subsystem then it must disconnect,
	 * possibly reset its filter pool references, and request new services and
	 * parameters from its new configuration. It must also answer true to
	 * {@link #canSwitchTo(ISubSystemConfiguration)}. If the configuration is
	 * not compatible with this subsystem then this must do nothing and must
	 * answer false to {@link #canSwitchTo(ISubSystemConfiguration)}.
	 *
	 * @param configuration the configuration to which to switch.
	 * @since org.eclipse.rse.core 3.0
	 */
	public void switchServiceFactory(ISubSystemConfiguration configuration);

	/**
	 * Determine is this subsystem is compatible with this specified
	 * configuration.
	 *
	 * @param configuration the configuration which may be switched to
	 * @return true if the subsystem can switch to this configuration, false
	 *         otherwise. Subsystems which are not service subsystems must
	 *         return false.
	 * @since org.eclipse.rse.core 3.0
	 */
	public boolean canSwitchTo(ISubSystemConfiguration configuration);



	////	 -------------------------------------
	//	// GUI methods
	//	// -------------------------------------
	//	/**
	//	 * Return the single property page to show in the tabbed notebook for the
	//	 *  for SubSystem property of the parent Connection. Return null if no
	//	 *  page is to be contributed for this. You are limited to a single page,
	//	 *  so you may have to compress. It is recommended you prompt for the port
	//	 *  if applicable since the common base subsystem property page is not shown
	//	 *  To help with this you can use the SystemPortPrompt widget.
	//	 */
	//    public PropertyPage getPropertyPage(Composite parent);

}
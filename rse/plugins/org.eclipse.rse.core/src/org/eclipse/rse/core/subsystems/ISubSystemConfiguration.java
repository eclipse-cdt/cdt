/********************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others. All rights reserved.
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
 * David Dykstal (IBM) - 168870: move core function from UI to core
 * Martin Oberhuber (Wind River) - [184095] Replace systemTypeName by IRSESystemType
 * Martin Oberhuber (Wind River) - [186748] Move ISubSystemConfigurationAdapter from UI/rse.core.subsystems.util
 * Martin Oberhuber (Wind River) - [189123] Move renameSubSystemProfile() from UI to Core
 * David Dykstal (IBM) - [197036] change signature of getFilterPoolManager method to be able to control the creation of filter pools
 * David Dykstal (IBM) - [217556] remove service subsystem types
 * Xuan Chen (IBM) - [223126] [api][breaking] Remove API related to User Actions in RSE Core/UI
 * Martin Oberhuber (Wind River) - [cleanup] Add API "since" Javadoc tags
 * David Dykstal (IBM) - [168976][api] move ISystemNewConnectionWizardPage from core to UI
 * Martin Oberhuber (Wind River) - [226574][api] Add ISubSystemConfiguration#supportsEncoding()
 * Martin Oberhuber (Wind River) - [218309] ConcurrentModificationException during workbench startup
 * David McKinght   (IBM)        - [249245] Go Into on a filter then back brings up prompt of any expanded promptable filter
 ********************************************************************************/

package org.eclipse.rse.core.subsystems;

import java.util.List;

import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.core.filters.ISystemFilter;
import org.eclipse.rse.core.filters.ISystemFilterPool;
import org.eclipse.rse.core.filters.ISystemFilterPoolManager;
import org.eclipse.rse.core.filters.ISystemFilterPoolManagerProvider;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.IRSEPersistableContainer;
import org.eclipse.rse.core.model.ISubSystemConfigurator;
import org.eclipse.rse.core.model.ISystemProfile;
import org.eclipse.rse.services.IService;

/**
 * Subsystem Configuration interface.
 *
 * @noimplement This interface is not intended to be implemented by clients.
 *              Subsystem configuration implementations must subclass
 *              <code>SubSystemConfiguration</code> rather than implementing
 *              this interface directly.
 */
public interface ISubSystemConfiguration extends ISystemFilterPoolManagerProvider, IRSEPersistableContainer {
	// ---------------------------------
	// CONSTANTS...
	// ---------------------------------
	public static final boolean FORCE_INTO_MEMORY = true;
	public static final boolean LAZILY = false;

	/**
	 * Reset for a full refresh from disk, such as after a team synch.
	 */
	public void reset();

	/**
	 * Retrieves all the filter pool managers for all the profiles, active or not.
	 * This allows cross references from
	 * one subsystem in one profile to filter pools in any other profile.
	 */
	public ISystemFilterPoolManager[] getAllSystemFilterPoolManagers();

	// ---------------------------------
	// CRITICAL METHODS...
	// ---------------------------------

	/**
	 * Test whether subsystems managed by this configuration support custom
	 * encodings.
	 *
	 * Encodings specify the way how binary data on the remote system is
	 * translated into Java Unicode Strings. RSE provides some means for the
	 * User to specify a particular encoding to use; typically, all subsystems
	 * that do support custom encodings specified should use the same encoding
	 * such that they can interoperate. Therefore, encodings are usually
	 * obtained from {@link IHost#getDefaultEncoding(boolean)}.
	 *
	 * It's possible, however, that a particular subsystem "knows" that its
	 * resources are always encoded in a particular way, and there is no
	 * possibility to ever change that. The Subsystem Configuration would return
	 * <code>false</code> here in this case. Another possibility is that
	 * encodings for a particular subsystem can be changed, but in a way that's
	 * different than what RSE usually does. The default case, however, should
	 * be that subsystems fall back to the setting specified by the host or its
	 * underlying system type such that existing subsystem configurations can be
	 * re-used in an environment where the encoding to use is pre-defined by the
	 * system type or host connection.
	 *
	 * If no subsystem registered against a given host supports encodings, the
	 * corresponding UI controls on the IHost level are disabled in order to
	 * avoid confusion to the user.
	 *
	 * @return <code>true<code> if the RSE mechanisms for specifying custom
	 *     encodings are observed and supported by the subsystems managed
	 *     by this configuration for the given host.
	 * @see IRSESystemType#PROPERTY_SUPPORTS_ENCODING
	 * @since org.eclipse.rse.core 3.0
	 */
	public boolean supportsEncoding(IHost host);

	/**
	 * Return true if the subsystem supports more than one filter string
	 * <p>RETURNS true BY DEFAULT
	 */
	public boolean supportsMultiStringFilters();

	/**
	 * Return true if the subsystem supports the exporting of filter strings from it's filters
	 * <p>RETURNS true BY DEFAULT
	 */
	public boolean supportsFilterStringExport();

	/**
	 * Return true if subsystem instances from this factory support connect and disconnect actions
	 * <p>Returns true in default implementation.
	 */
	public boolean supportsSubSystemConnect();

	/**
	 * Return true (default) or false to indicate if subsystems of this factory support user-editable
	 *  port numbers.
	 * <p>Returns true in default implementation.
	 */
	public boolean isPortEditable();

	/**
	 * Return true if subsystem instances from this factory support remote command execution
	 * <p>Returns false in default implementation, and is usually only true for command subsystems.
	 * 
	 * NOTE: command subsystems are special because their filters don't yield children or allow
	 * drilling into them via Show In Table, Open In New Window or Go Into.  See bug 249245 and 
	 * bug 254605 for further explanation.
	 */
	public boolean supportsCommands();

	/**
	 * Return true if subsystem instances from this factory support getting and setting properties
	 * <p>Returns false in default implementation, and not actually used yet.
	 */
	public boolean supportsProperties();

	/**
	 * Required method for subsystem factory child classes. Return true if you support filters, false otherwise.
	 * If you support filters, then some housekeeping will be done for you automatically. Specifically, they
	 * will be saved and restored for you automatically.
	 * <p>Returns true in default implementation.
	 */
	public boolean supportsFilters();

	/**
	 * Indicates whether the subsystem supports displaying children under
	 * its filters.  By default, this will return true, but if filters that can't
	 * be expanded are desired, this can be overridden to return false.
	 */
	public boolean supportsFilterChildren();

	/**
	 * Required method for subsystem factory child classes. Return true if you filter caching.
	 * If you support filter caching, then the views will always check the in-memory cache for
	 * filter results before attempting a query.
	 * <p>Returns true in default implementation.
	 */
	public boolean supportsFilterCaching();

	/**
	 * Required method for subsystem factory child classes. Return true if you support filters, and you support
	 *  multiple filter strings per filter. Return false to restrict the user to one string per filter.
	 * <p>Returns true in default implementation.
	 */
	public boolean supportsMultipleFilterStrings();

	/**
	 * Required method for subsystem factory child classes if returning true from supportsFilters.
	 * Return true if you support filters within filters, false otherwise.
	 * <p>Returns false in default implementation.
	 */
	public boolean supportsNestedFilters();

	/**
	 * Return true if you support quick filters. These allow the user to subset a remote system object at
	 *  the time they expand it in the remote system explorer tree view.
	 * <p>
	 * Not supported yet
	 */
	public boolean supportsQuickFilters();

	/**
	 * Return true if filters of this subsystem factory support dropping into.
	 */
	public boolean supportsDropInFilters();

	/**
	 * Return true if deferred queries are supported.
	 *
	 * Deferred queries work such that when a filter or element
	 * children query is made, a WorkbenchJob is started to
	 * perform the query in a background thread. The query can
	 * take time to complete, but a negative side-effect of this
	 * is that it will always take time to complete.
	 *
	 * Alternative models can use asynchronous calls to populate
	 * their model with data from the remote side, and refresh
	 * the views when new data is in the model. Such subsystem
	 * configurations should return <code>false</code> here.
	 *
	 * The default implementation returns <code>true</code>, indicating
	 * that deferred queries are supported for filters, and delegates
	 * the check for model elements to the ISystemViewElementAdapter.
	 *
	 * @return <code>true</code> if deferred queries are supported.
	 */
	public boolean supportsDeferredQueries();

	/**
	 * Return true if filters of this subsystem factory provide a custom implementation of drop support.
	 * By default, the filter reference adapter treats a drop on a filter as an update to the list of filter
	 * strings for a filter.  For things like files, it is more desirable to treat the drop as a physical
	 * resource copy, so in that case, custom drop makes sense.
	 *
	 * By default this returns false.
	 */
	public boolean providesCustomDropInFilters();

	/**
	 * Return true if you support user-defined/managed named file types
	 * <p>Returns false in default implementation.
	 */
	public boolean supportsFileTypes();

	/**
	 * Tell us if this subsystem factory supports targets, which are destinations for
	 *   pushes and builds. Normally only true for file system factories.
	 */
	public boolean supportsTargets();

	/**
	 * Tell us if this subsystem factory supports server launch properties, which allow the user
	 *  to configure how the server-side code for these subsystems are started. There is a Server
	 *  Launch Setting property page, with a pluggable composite, where users can configure these
	 *  properties.
	 * <br> By default we return false here. This is overridden in UniversalFileSubSystemConfiguration though.
	 */
	public boolean supportsServerLaunchProperties(IHost host);

	/**
	 * If {@link #supportsServerLaunchProperties(IHost)} returns true, this method may be called by
	 * the server launcher to decide if a given remote server launch type is supported or not.
	 * <br> We return true by default.
	 * @see org.eclipse.rse.core.subsystems.ServerLaunchType
	 */
	public boolean supportsServerLaunchType(ServerLaunchType serverLaunchType);

	/**
	 * Tell us if filter strings are case sensitive.
	 * <p>Returns false in default implementation.
	 */
	public boolean isCaseSensitive();

	// ---------------------------------
	// USER-PREFERENCE METHODS...
	// ---------------------------------
	/**
	 * If we support filters, should we show filter pools in the remote system explorer?
	 * Typically retrieved from user preferences.
	 */
	public boolean showFilterPools();

	/*
	 * If we support filters, should we show filter strings in the remote system explorer?
	 * Typically retrieved from user preferences.
	 *
	 public boolean showFilterStrings();
	 */
	/**
	 * If we support filters, should we show filter pools in the remote system explorer?
	 * This is to set it after the user changes it in the user preferences. It may require
	 *  refreshing the current view.
	 */
	public void setShowFilterPools(boolean show);

	/*
	 * If we support filters, should we show filter strings in the remote system explorer?
	 * This is to set it after the user changes it in the user preferences. It may require
	 *  refreshing the current view.
	 *
	 public void setShowFilterStrings(boolean show);
	 */

	// ---------------------------------
	// PROXY METHODS. USED INTERNALLY...
	// ---------------------------------
	/**
	 * Private method called by RSEUIPlugin
	 */
	public void setSubSystemConfigurationProxy(ISubSystemConfigurationProxy proxy);

	/**
	 * Private method
	 */
	public ISubSystemConfigurationProxy getSubSystemConfigurationProxy();

	// ---------------------------------
	// FACTORY ATTRIBUTE METHODS...
	// ---------------------------------
	/**
	 * Return vendor of this factory.
	 * This comes from the xml "vendor" attribute of the extension point.
	 */
	public String getVendor();

	/**
	 * Return name of this factory. Matches value in name attribute in extension point xml
	 */
	public String getName();

	/**
	 * Return description of this factory. Comes from translated description string in extension point xml
	 */
	public String getDescription();

	/**
	 * Return unique id of this factory. Matches value in id attribute in extension point xml
	 */
	public String getId();

	/**
	 * Return the category this subsystem factory subscribes to.
	 * @see org.eclipse.rse.core.model.ISubSystemConfigurationCategories
	 */
	public String getCategory();

	/**
	 * Return the system types this subsystem factory supports.
	 */
	public IRSESystemType[] getSystemTypes();

	// ---------------------------------
	// PROFILE METHODS...
	// ---------------------------------
	/**
	 * Called by SystemRegistry when we are about to delete a profile.
	 * <p>
	 * Our only mission is to delete the filter pool associated with it,
	 * because the registry has already called deleteSubSystemsByConnection
	 * for every subsystem of every connection owned by this profile.
	 */
	public void deletingSystemProfile(ISystemProfile profile);

	/**
	 * Called by SystemRegistry when we have toggled the active-status of a profile
	 */
	public void changingSystemProfileActiveStatus(ISystemProfile profile, boolean newStatus);

	/**
	 * Get owning profile object given a filter pool object
	 */
	public ISystemProfile getSystemProfile(ISystemFilterPool pool);

	/**
	 * Callback method called after renaming a subsystem profile.
	 * <p>
	 * This is called by SystemRegistry's renameSystemProfile method
	 * after it is complete, , in order to allow the subsystem configuration
	 * perform any required cleanup. For instance, subsystem configurations
	 * must ensure that they update their filter pool manager names
	 * (and their folders).
	 * </p><p>
	 * Must be called AFTER changing the profile's name!!
	 * </p>
	 * @param oldProfileName the old profile name.
	 * @param newProfileName the new profile name.
	 */
	public void renameSubSystemProfile(String oldProfileName, String newProfileName);


	// ---------------------------------
	// SUBSYSTEM METHODS...
	// ---------------------------------

	/**
	 * Called by SystemRegistry's renameSystemProfile method to ensure we update our
	 * subsystem names within each subsystem.
	 * This should be invoked after changing the profile's name.
	 * @param subsystem the subsystem to be updated
	 * @param oldProfileName the old profile name
	 * @param newProfileName the new profile name
	 */
	public void renameSubSystemProfile(ISubSystem subsystem, String oldProfileName, String newProfileName);

	/**
	 * Called by SystemRegistry's renameSystemProfile method to pre-test if we are going to run into errors on a
	 *  profile rename, due to file or folder in use.
	 */
	public void preTestRenameSubSystemProfile(String oldProfileName) throws Exception;

	/**
	 * Called by SystemRegistry's renameConnection method to ensure we update our
	 *  connection names within each subsystem.
	 * <p>
	 * Must be called prior to changing the connection's name!!
	 */
	public void renameSubSystemsByConnection(IHost conn, String newConnectionName);

	/**
	 * Called by SystemRegistry's deleteConnection method to ensure we delete all our
	 *  subsystems for a given connection.
	 */
	public void deleteSubSystemsByConnection(IHost conn);

	/**
	 * Creates a new subsystem instance that is associated with the given connection object.
	 * SystemRegistryImpl calls this when a new connection is created, and appliesToSystemType returns true.
	 * @param conn The connection to create a subsystem for
	 * @param creatingConnection true if we are creating a connection, false if just creating
	 *          another subsystem for an existing connection.
	 * @param configurators The configurators that will be applied to a new subsystem or null if there are none.
	 * @since 3.0
	 */
	public ISubSystem createSubSystem(IHost conn, boolean creatingConnection, ISubSystemConfigurator[] configurators);

	// used in the case where newsubsystems are added after a connection exists
	public ISubSystem createSubSystemAfterTheFact(IHost conn);

	public ISubSystem createSubSystemInternal(IHost conn);

	/**
	 * Get the connector service for a particular host.
	 * This may create the connector service if necessary.
	 * If the configuration is a service subsystem configuration, this should
	 * return the connector service specified in {@link #setConnectorService(IHost, IConnectorService)}.
	 * @param host the host for which to create or retrieve the connector service
	 * @return the connector service associated with this host. This can return null if there
	 * is no connector service associated with this configuration. It is recommended that
	 * there be a connector service if {@link #supportsSubSystemConnect()} is true.
	 */
	public IConnectorService getConnectorService(IHost host);

	/**
	 * Set the connector service for a particular host. This is usually managed
	 * by a connector service manager known to this configuration. This must be
	 * implemented by service subsystem configurations. Service subsystems allow
	 * a connector service to be changed.
	 *
	 * @param host the host for which to set this connector service.
	 * @param connectorService the connector service associated with this host.
	 * @since org.eclipse.rse.core 3.0
	 */
	public void setConnectorService(IHost host, IConnectorService connectorService);

	/**
	 * Get the service type associated with this subsystem configuration. If the
	 * configuration is not a service subsystem configuration it must return
	 * <code>null</code>, otherwise it must return the interface class that
	 * the underlying service layer implements.
	 *
	 * @return an interface class that is implemented by the service layer used
	 *         by subsystems that have this configuration, or <code>null</code>
	 *         if this is not a service subsystem configuration.
	 * @since org.eclipse.rse.core 3.0
	 */
	public Class getServiceType();

	/**
	 * Get the implementation type of the service associated with this subsystem
	 * configuration. If the configuration is not a service subsystem
	 * configuration then this must return <code>null</code>, otherwise it
	 * must return the class that implements the interface specified in
	 * {@link #getServiceType()}.
	 *
	 * @return an implementation class that implements the interface specified
	 *         in {@link #getServiceType()}, or <code>null</code> if this is
	 *         not a service subsystem configuration.
	 * @since org.eclipse.rse.core 3.0
	 */
	public Class getServiceImplType();

	/**
	 * Get the actual service associated with a particular host. If the
	 * configuration is not a service subsystem this must return null. Otherwise
	 * this must return the particular instance of the class returned by
	 * {@link #getServiceImplType()} that is associated with this host instance.
	 *
	 * @param host The host for which to retrieve the service.
	 * @return The instance of {@link IService} which is associated with this
	 *         host, or <code>null</code> if this is not a service subsystem
	 *         configuration.
	 * @since org.eclipse.rse.core 3.0
	 */
	public IService getService(IHost host);

	/**
	 * Overridable entry for child classes to contribute a server launcher instance
	 *  for a given subsystem.
	 * <p>
	 * Create an instance of ServerLauncher, and add it to the given subsystem.
	 * When a subsystem is created, and {@link #supportsServerLaunchProperties(IHost)}
	 * returns true, this method is called to create the server launcher instance
	 * associated with the subsystem. The default implementation is to create an
	 * instance of {@link IRemoteServerLauncher}, but override to create your own
	 * ServerLauncher instance if you have your own class.
	 */
	public IServerLauncherProperties createServerLauncher(IConnectorService connectorService);

	/**
	 * Updates user-editable attributes of an existing subsystem instance.
	 * These attributes typically affect the live connection, so the subsystem will be forced to
	 *  disconnect.
	 * <p>
	 * The subsystem will be saved to disk.
	 * @param subsystem target of the update action
	 * @param updateUserId true if we are updating the userId, else false to ignore userId
	 * @param userId new local user Id. Ignored if updateUserId is false
	 * @param updatePort true if we are updating the port, else false to ignore port
	 * @param port new local port value. Ignored if updatePort is false
	 */
	public void updateSubSystem(ISubSystem subsystem, boolean updateUserId, String userId, boolean updatePort, int port);

	/**
	 * Update the port for the given subsystem instance.
	 */
	public void setSubSystemPort(ISubSystem subsystem, int port);

	/**
	 * Update the user ID for the given subsystem instance.
	 */
	public void setSubSystemUserId(ISubSystem subsystem, String userId);

	/**
	 * Returns true if this factory allows users to delete instances of subsystem objects.
	 * Would only be true if users are allowed to create multiple instances of subsystem objects
	 *  per connection.
	 */
	public boolean isSubSystemsDeletable();

	/**
	 * Deletes a given subsystem instance from the list maintained by this factory.
	 * SystemRegistryImpl calls this when the user selects to delete a subsystem object,
	 *  or delete the parent connection this subsystem is associated with.
	 * In former case, this is only called if the factory supports user-deletable subsystems.
	 */
	public boolean deleteSubSystem(ISubSystem subsystem);

	/**
	 * Clone a given subsystem into the given connection.
	 * Called when user does a copy-connection action.
	 * @param oldSubsystem The subsystem to be cloned
	 * @param newConnection The connection into which to create and clone the old subsystem
	 * @param copyProfileOperation Pass true if this is an profile-copy operation versus a connection-copy operation
	 * @return New subsystem within the new connection
	 */
	public ISubSystem cloneSubSystem(ISubSystem oldSubsystem, IHost newConnection, boolean copyProfileOperation) throws Exception;

	/**
	 * Returns a list of subsystem objects existing for the given connection.
	 * @param conn System connection to retrieve subsystems for
	 * @param force true if we should force all the subsystems to be restored from disk if not already
	 */
	public ISubSystem[] getSubSystems(IHost conn, boolean force);

	/**
	 * Returns a list of all subsystem objects for all connections.
	 */
	public ISubSystem[] getSubSystems(boolean force);

	/**
	 * Renames a subsystem. This is better than ss.setName(String newName) as it saves the subsystem to disk.
	 */
	public void renameSubSystem(ISubSystem subsystem, String newName);

	/**
	 * Disconnect all subsystems currently connected.
	 * Called by shutdown() of RSEUIPlugin.
	 */
	public void disconnectAllSubSystems() throws Exception;

	// ---------------------------------
	// FILTER POOL METHODS...
	// ---------------------------------
	/**
	 * Get the filter pool manager for the given profile. A subsystem
	 * configuration has a filter pool manager for each profile.
	 *
	 * @param profile The system profile for which to get the manager.
	 * @param force if true then create the default filters for this subsystem
	 *            configuration in this profile. This should only be done during
	 *            initial subsystem creation, not during subsystem restore.
	 * @return a filter pool manager
	 * @since org.eclipse.rse.core 3.0
	 */
	public ISystemFilterPoolManager getFilterPoolManager(ISystemProfile profile, boolean force);

	/**
	 * Get the filter pool manager for the given profile. A subsystem
	 * configuration has a filter pool manager for each profile. Do not force
	 * the creation of default filter pools. Fully equivalent to
	 * getFilterPoolManager(profile, false).
	 *
	 * @param profile The system profile for which to get the manager.
	 * @return a filter pool manager
	 */
	public ISystemFilterPoolManager getFilterPoolManager(ISystemProfile profile);

	/**
	 * Copy the filter pool manager and return a new one. Called during profile-copy operations.
	 * Will also copy all of the filter pools and their respective data.
	 */
	public ISystemFilterPoolManager copyFilterPoolManager(ISystemProfile oldProfile, ISystemProfile newProfile) throws Exception;

	/**
	 * Given a subsystem, return the first (hopefully only) default pool for this
	 * subsystem's profile.
	 */
	public ISystemFilterPool getDefaultSystemFilterPool(ISubSystem subsys);

	/**
	 * Test if any filter pools in the given profile are referenced by other profiles,
	 *  which are active.
	 * <p>
	 * Called when user tries to make a profile inactive. We prevent this if there exists
	 *  active references.
	 * @param profile The profile being tested
	 * @return An array of the active subsystems which reference filter pools in this profile,
	 *            or null if none are found.
	 */
	public ISubSystem[] testForActiveReferences(ISystemProfile profile);

	// ---------------------------------
	// FILTER METHODS
	// ---------------------------------
	/**
	 * Return the translated string to show in the property sheet for the type property when a filter is selected.
	 */
	public String getTranslatedFilterTypeProperty(ISystemFilter selectedFilter);

	/**
	 * Return true if we should show the refresh action in the popup for the given element.
	 */
	public boolean showRefreshOnFilter();

	/**
	 * Return true if we should show the show in table action in the popup for the given element.
	 */
	public boolean showGenericShowInTableOnFilter();

	/**
	 * Given a filter, decide whether to show the Filter Strings property page
	 *  for this filter. Default is true.
	 */
	public boolean showChangeFilterStringsPropertyPage(ISystemFilter filter);

	/**
	 * Determines whether this factory is responsible for the creation of subsytems of the specified type
	 * Subsystem factories should override this to indicate which subsystems they support.
	 *
	 * @param subSystemType type of subsystem
	 * @return whether this factory is for the specified subsystemtype
	 */
	public boolean isFactoryFor(Class subSystemType);

	// ---------------------------------
	// FILTER REFERENCE METHODS
	// ---------------------------------

	// ---------------------------------
	// STATE METHODS...
	// ---------------------------------
	/**
	 * Called by adapters prior to asking for actions, in case the connection of the currently selected
	 *  object is required by the action.
	 */
	public void setConnection(IHost connection);

	/**
	 * Called by adapters prior to asking for actions. For cases when current selection is needed.
	 */
	public void setCurrentSelection(Object[] selection);

	// ---------------------------------
	// SAVE METHODS...
	// ---------------------------------
	/**
	 * Saves absolutely everything to disk. This is called as a safety
	 * measure when the workbench shuts down.
	 * <p>
	 * Totally handled for you!
	 * <p>
	 * Calls saveSubSystems() and saveFilterPools()
	 * <p>
	 * Exceptions are swallowed since we cannot deal with them on shutdown anyway!
	 */
	public boolean commit();

	/**
	 * Save one subsystem to disk.
	 * Called by each subsystem when their data changes.
	 */
	public void saveSubSystem(ISubSystem subsys) throws Exception;

	/**
	 * Return the internal list of subsystems instantiated for this
	 * configuration. Internal use only, do not call or use. Use
	 * {@link #getSubSystems(boolean)} with a <code>false</code> argument
	 * instead.
	 *
	 * @noreference This method is not intended to be referenced by clients.
	 * @return The internal list of SubSystem instances for this configuration.
	 *         Any operations on this list (such as iterating, adding or
	 *         removing members) must always be synchronized against the list,
	 *         in order to protect against concurrent modification.
	 */
	List getSubSystemList();

	/**
	 * Return the internal list of filter pool managers associated with this
	 * configuration. Internal use only, do not call or use.
	 *
	 * @noreference This method is not intended to be referenced by clients.
	 * @return The internal list of filter pool Managers associated with this
	 *         configuration. Any operations on this list (such as iterating,
	 *         adding or removing members) must always be synchronized against
	 *         the list, in order to protect against concurrent modification.
	 */
	List getFilterPoolManagerList();

	public ISystemFilterPool getDefaultFilterPool(ISystemProfile profile, String oldProfileName);

	public ISystemProfile getSystemProfile(String name);

	public void renameFilterPoolManager(ISystemProfile profile);

	//	/**
	//	 * Return the validator for the userId.
	//	 * A default is supplied.
	//	 * Note this is only used for the subsystem's properties, so will not
	//	 * be used by the connection's default. Thus, is only of limited value.
	//	 * <p>
	//	 * This must be castable to ICellEditorValidator for the property sheet support.
	//	 */
	//	public ISystemValidator getUserIdValidator();
	//	/**
	//	 * Return the validator for the password which is prompted for at runtime.
	//	 * No default is supplied.
	//	 */
	//	public ISystemValidator getPasswordValidator();
	//	/**
	//	 * Return the validator for the port.
	//	 * A default is supplied.
	//	 * This must be castable to ICellEditorValidator for the property sheet support.
	//	 */
	//	public ISystemValidator getPortValidator();
}
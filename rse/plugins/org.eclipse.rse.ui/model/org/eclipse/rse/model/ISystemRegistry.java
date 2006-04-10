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

package org.eclipse.rse.model;

import java.util.List;
import java.util.Vector;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.core.subsystems.ISubSystemConfigurationProxy;
import org.eclipse.rse.internal.model.SystemScratchpad;
import org.eclipse.rse.ui.view.ISystemViewInputProvider;
import org.eclipse.rse.ui.wizards.ISystemNewConnectionWizardPage;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.widgets.Shell;


/**
 * Registry or front door for all remote system connections.
 * There is a singleton of the class implementation of this interface.
 * To get it, call the {@link org.eclipse.rse.core.SystemPlugin#getTheSystemRegistry() getTheSystemRegistry}
 * method in the SystemPlugin object.
 * <p>
 * The idea here is that connections are grouped by system profile. At any 
 *  time, there is a user-specified number of profiles "active" and connections
 *  from each active profile are worked with.
 */
public interface ISystemRegistry extends ISystemViewInputProvider, ISchedulingRule
{
    // ----------------------------------
    // UI METHODS...
    // ----------------------------------
    /**
     * Show the RSE perspective if it is not already showing
     */
    public void showRSEPerspective();
    /**
     * Expand the given connection in the RSE, if the RSE is the active perspective.
     */
    public void expandHost(IHost conn);
    /**
     * Expand the given subsystem in the RSE, if the RSE is the active perspective.
     */
    public void expandSubSystem(ISubSystem subsystem);
    // ----------------------------------
    // ACTIVE PROGRESS MONITOR METHODS...
    // ----------------------------------
    /**
     * Set the current active runnable context to be used for a progress monitor
     *  by the subsystem methods that go to the host. Called by wizards and dialogs
     *  that have a built-in progress monitor and hence removes the need to popup
     *  an intrusive pm dialog.
     * <p><b>You must call clearRunnableContext when your dialog/wizard is disposed!</b>
     * @param shell The shell of the wizard/dialog. This is recorded so it can be tested if
     *  it is disposed before attempting to use the context
     * @param context The dialog/wizard/view that implements IRunnableContext
     */
    public void setRunnableContext(Shell shell, IRunnableContext context);
    /**
     * Clear the current active runnable context to be used for a progress monitor. 
     * Be sure to call this from you dispose method.
     */
    public void clearRunnableContext();
    /**
     * Return the current registered runnable context, or null if none registered. Use this
     *  for long running operations instead of an intrusive progress monitor dialog as it is
     *  more user friendly. Many dialogs/wizards have these built in so it behooves us to use it.
     */
    public IRunnableContext getRunnableContext();
    // ----------------------------
    // SUBSYSTEM FACTORY METHODS...
    // ----------------------------            
	/**
	 * Private method used by SystemPlugin to tell registry all registered subsystem
	 *  factories. This way, all code can use this registry to access them versus the
	 *  SystemPlugin.
	 */
	public void setSubSystemConfigurationProxies(ISubSystemConfigurationProxy[] proxies);
	/**
	 * Public method to retrieve list of subsystem factory proxies registered by extension points.
	 */
	public ISubSystemConfigurationProxy[]  getSubSystemConfigurationProxies();
    /**
     * Return all subsystem factory proxies matching a subsystem factory category.
     * @see ISubSystemFactoryCategories
     */
    public ISubSystemConfigurationProxy[] getSubSystemConfigurationProxiesByCategory(String factoryCategory);
	/**
	 * Return all subsystem factories. Be careful when you call this, as it activates all 
	 *   subsystem factories.
	 */
	public ISubSystemConfiguration[] getSubSystemConfigurations();
	
    /**
     * Return the parent subsystemconfiguration given a subsystem object.
     */
    public ISubSystemConfiguration getSubSystemConfiguration(ISubSystem subsystem);
    /**
     * Return the subsystemconfiguration, given its plugin.xml-declared id.
     */
    public ISubSystemConfiguration getSubSystemConfiguration(String id);    
       /**
     * Return all subsystem factories which have declared themselves part of the given category.
     * <p>
     * This looks for a match on the "category" of the subsystem factory's xml declaration
     *  in its plugin.xml file. Thus, it is effecient as it need not bring to life a 
     *  subsystem factory just to test its parent class type.
     * 
     * @see ISubSystemFactoryCategories
     */
    public ISubSystemConfiguration[] getSubSystemConfigurationsByCategory(String factoryCategory);
    /**
     * Return all subsystem factories which support the given system type. If the type is null,
     *  returns all.
     * 
     */
    public ISubSystemConfiguration[] getSubSystemConfigurationsBySystemType(String systemType);
    
	/**
	 * Return all subsystem factories which support the given system type. If the type is null,
	 *  returns all.
	 */
	public ISubSystemConfiguration[] getSubSystemConfigurationsBySystemType(String systemType, boolean filterDuplicateServiceSubSystemFactories);


    // ----------------------------
    // USER PREFERENCE METHODS...
    // ----------------------------
    /**
     * Are connection names to be qualified by profile name?
     */
    public boolean getQualifiedHostNames();
    /**
     * Set if connection names are to be qualified by profile name
     */
    public void setQualifiedHostNames(boolean set);
    /**
     * Reflect the user changing the preference for showing filter pools.
     */
    public void setShowFilterPools(boolean show);
    /*
     * Reflect the user changing the preference for showing filter strings.
     *
    public void setShowFilterStrings(boolean show);
    */
    /**
     * Reflect the user changing the preference for showing new connection prompt
     */
    public void setShowNewHostPrompt(boolean show);
    
    // ----------------------------
    // PROFILE METHODS...
    // ----------------------------
	/**
	 * Return singleton profile manager
	 */
	public ISystemProfileManager getSystemProfileManager();
	/**
	 * Return the profiles currently selected by the user as his "active" profiles
	 */
	public ISystemProfile[] getActiveSystemProfiles();
	/**
	 * Return the profile names currently selected by the user as his "active" profiles
	 */
	public String[] getActiveSystemProfileNames();
	/**
	 * Return all defined profiles
	 */
	public ISystemProfile[] getAllSystemProfiles();
	/**
	 * Return all defined profile names
	 */
	public String[] getAllSystemProfileNames();	
	/**
	 * Return all defined profile names as a vector
	 */
	public Vector getAllSystemProfileNamesVector();
    /**
     * Get a SystemProfile given its name
     */
    public ISystemProfile getSystemProfile(String profileName);
    /**
     * Create a SystemProfile given its name and whether or not to make it active
     */
    public ISystemProfile createSystemProfile(String profileName, boolean makeActive)
           throws Exception;
    /**
     * Copy a SystemProfile. All connections connection data is copied.
     * @param monitor Progress monitor to reflect each step of the operation    
     * @param profile Source profile to copy
     * @param newName Unique name to give copied profile
     * @param makeActive whether to make the copied profile active or not
     * @return new SystemProfile object
     */
    public ISystemProfile copySystemProfile(IProgressMonitor monitor, ISystemProfile profile, String newName, boolean makeActive)
           throws Exception;
    /**
     * Rename a SystemProfile. Rename is propogated to all subsystem factories so
     * they can rename their filter pool managers and whatever else is required.
     */
    public void renameSystemProfile(ISystemProfile profile, String newName)
           throws Exception;    
    /**
     * Delete a SystemProfile. Prior to physically deleting the profile, we delete all
     * the connections it has, all the subsystems they have.
     * <p>
     * As well, all the filter pools for this profile are deleted, and subsequently any
     * cross references from subsystems in connections in other profiles are removed.
     * <p>
     * A delete event is fired for every connection deleted.
     */
    public void deleteSystemProfile(ISystemProfile profile) 
           throws Exception;
    /**
     * Make or unmake the given profile active
     */
    public void setSystemProfileActive(ISystemProfile profile, boolean makeActive);
  
    
    /**
     * Return the list of connector services provided for the given host
     * @param conn the host
     * @return the list of connector services
     */
    public IConnectorService[] getConnectorServices(IHost conn);
    
    // ----------------------------
    // SUBSYSTEM METHODS...
    // ----------------------------
            
    /**
     * Return list of subsystem objects for a given connection.  If the subsystems have
     * not all been read into memory, this loads them up
     */
    public ISubSystem[] getSubSystems(IHost conn);    
    
    /**
     * Return list of subsystem objects for a given connection.  Use the force
     * flag to indicate whether or not to restore from disk
     */
	public ISubSystem[] getSubSystems(IHost conn, boolean force);
    
	public ISubSystem[] getServiceSubSystems(Class serviceType, IHost connection);
	
    /**
     * Resolve a subsystem from it's profile, connection and subsystem name.
     * 
     * @param srcProfileName the name of the profile
     * @param srcConnectionName the name of the connection
     * @param subsystemFactoryId the id of the subsystem
     * 
     * @return the subsystem
     */
     public ISubSystem getSubSystem(String srcProfileName, String srcConnectionName, String subsystemFactoryId);

    /**
     * Resolve a subsystem from it's absolute name
     * 
     * @param absoluteSubSystemName the name of the subsystem
     * 
     * @return the subsystem
     */
     public ISubSystem getSubSystem(String absoluteSubSystemName);

	/**
	 * Return the absolute name for the specified subsystem
	 * @param the subsystem
	 * @return the absolute name of the subsystem
	 */
	 public String getAbsoluteNameForSubSystem(ISubSystem subsystem);	 
	 
	 /**
	 * Return the absolute name for the specified connection
	 * @param the connection
	 * @return the absolute name of the connection
	 */
	 public String getAbsoluteNameForConnection(IHost connection);

    /**
     * Get a list of subsystem objects owned by the subsystem factory identified by 
     *  its given plugin.xml-described id. Array is never null, but may be of length 0.
     * <p>
     * This is a list that of all subsystems for all connections owned by the factory.
     */
    public ISubSystem[] getSubSystems(String factoryId);
    /**
     * Get a list of subsystem objects for given connection, owned by the subsystem factory 
     * identified by its given plugin.xml-described id. Array will never be null but may be length zero.
     */
    public ISubSystem[] getSubSystems(String factoryId, IHost connection);
    /**
     * Get a list of subsystem objects for given connection, owned by a subsystem factory 
     * that is of the given category. Array will never be null but may be length zero.
     * <p>
     * This looks for a match on the "category" of the subsystem factory's xml declaration
     *  in its plugin.xml file. 
     * 
     * @see org.eclipse.rse.model.ISubSystemFactoryCategories
     */
    public ISubSystem[] getSubSystemsBySubSystemConfigurationCategory(String factoryCategory, IHost connection);

    /**
     * Delete a subsystem object. This code finds the factory that owns it and
     *  delegates the request to that factory.
     */
    public boolean deleteSubSystem(ISubSystem subsystem);

    // ----------------------------
    // CONNECTION METHODS...
    // ----------------------------
	/**
	 * Return the first connection to localhost we can find. While we always create a default one in
	 *  the user's profile, it is possible that this profile is not active or the connection was deleted.
	 *  However, since any connection to localHost will usually do, we just search all active profiles
	 *  until we find one, and return it. <br>
	 * If no localhost connection is found, this will return null. If one is needed, it can be created 
	 *  easily by calling {@link #createLocalHost(ISystemProfile, String, String)}.
	 */
	public IHost getLocalHost();
	
    /**
     * Return all connections in all active profiles.
     */
    public IHost[] getHosts();
    /**
     * Return all connections in a given profile name.
     */
    public IHost[] getHostsByProfile(ISystemProfile profile);
    /**
     * Return all connections in a given profile.
     */
    public IHost[] getHostsByProfile(String profileName);    
    /**
     * Return all connections for which there exists one or more subsystems owned
     *  by a given subsystem factory.
     * @see #getSubSystemConfiguration(String)
     */
    public IHost[] getHostsBySubSystemConfiguration(ISubSystemConfiguration factory);
    /**
     * Return all connections for which there exists one or more subsystems owned
     *  by a given subsystem factory, identified by factory Id
     */
    public IHost[] getHostsBySubSystemConfigurationId(String factoryId);
    /**
     * Return all connections for which there exists one or more subsystems owned
     *  by any a given subsystem factory that is of the given category.
     * <p>
     * This looks for a match on the "category" of the subsystem factory's xml declaration
     *  in its plugin.xml file. Thus, it is effecient as it need not bring to life a 
     *  subsystem factory just to test its parent class type.
     * 
     * @see org.eclipse.rse.model.ISubSystemFactoryCategories
     */
    public IHost[] getHostsBySubSystemConfigurationCategory(String factoryCategory);
    /**
     * Return all connections for all active profiles, for the given system type.
     */
    public IHost[] getHostsBySystemType(String systemType);
    /**
     * Return all connections for all active profiles, for the given system types.
     */
    public IHost[] getHostsBySystemTypes(String[] systemTypes);
    /**
     * Return a SystemConnection object given a system profile containing it, 
     *   and a connection name uniquely identifying it.
     */
    public IHost getHost(ISystemProfile profile, String connectionName);
    /**
     * Return the zero-based position of a SystemConnection object within its profile.
     */
    public int getHostPosition(IHost conn);
    /**
     * Return the number of SystemConnection objects within the given profile
     */
    public int getHostCount(String profileName);
    /**
     * Return the number of SystemConnection objects within the given connection's owning profile
     */
    public int getHostCountWithinProfile(IHost conn);
    /**
     * Return the number of SystemConnection objects within all active profiles
     */
    public int getHostCount();
    /**
     * Return a vector of previously-used connection names in the given named profile.
     * @return Vector of String objects.
     */
    public Vector getHostAliasNames(String profileName);
    /**
     * Return a vector of previously-used connection names in the given profile.
     * @return Vector of String objects.
     */
    public Vector getHostAliasNames(ISystemProfile profile);
    /**
     * Return a vector of previously-used connection names in all active profiles.
     */
    public Vector getHostAliasNamesForAllActiveProfiles();

    /**
     * Return array of all previously specified hostnames.
     */
    public String[] getHostNames();
    /**
     * Return array of previously specified hostnames for a given system type.
     */
    public String[] getHostNames(String systemType);

    /**
     * Returns the clipboard used for copy actions
     */
    public Clipboard getSystemClipboard();
    
    /**
	 * Returns the list of objects on the system clipboard
	 * @param srcType the transfer type
	 * @return the list of clipboard objects
	 */
	public List getSystemClipboardObjects(int srcType);
	
    /*
     * Returns the remote systems scratchpad root
     */
    public SystemScratchpad getSystemScratchPad();

	/**
	 * Convenience method to create a local connection, as it often that one is needed
	 *  for access to the local file system.
	 * @param profile - the profile to create this connection in. If null is passed, we first
	 *   try to find the default private profile and use it, else we take the first active profile.
	 * @param name - the name to give this profile. Must be unique and non-null.
	 * @param userId - the user ID to use as the default for the subsystems. Can be null.
	 */
	public IHost createLocalHost(ISystemProfile profile, String name, String userId);
	
    /**
     * Create a connection object, given the connection pool and given all the possible attributes.
     * <p>
     * THE RESULTING CONNECTION OBJECT IS ADDED TO THE LIST OF EXISTING CONNECTIONS FOR YOU, IN
     *  THE POOL YOU SPECIFY. THE POOL IS ALSO SAVED TO DISK.
     * <p>
     * This method:
     * <ul>
     *  <li>creates and saves a new connection within the given profile
     *  <li>calls all subsystem factories to give them a chance to create a subsystem instance
     *  <li>fires an ISystemResourceChangeEvent event of type EVENT_ADD to all registered listeners
     * </ul>
     * <p>
     * @param profileName Name of the system profile the connection is to be added to.
     * @param systemType system type matching one of the system type names defined via the
     *                    systemtype extension point.
     * @param connectionName unique connection name.
     * @param hostName ip name of host.
     * @param description optional description of the connection. Can be null.
     * @param defaultUserId userId to use as the default for the subsystems.
     * @param defaultUserIdLocation one of the constants in {@link org.eclipse.rse.core.ISystemUserIdConstants ISystemUserIdConstants}
     *   that tells us where to set the user Id
     * @param newConnectionWizardPages when called from the New Connection wizard this is union of the list of additional
     *          wizard pages supplied by the subsystem factories that pertain to the specified system type. Else null.
     * @return SystemConnection object, or null if it failed to create. This is typically
     *   because the connectionName is not unique. Call getLastException() if necessary.
     */
    public IHost createHost(String profileName, String systemType,
                                             String connectionName, String hostName,
                                             String description,String defaultUserId, int defaultUserIdLocation,
                                             ISystemNewConnectionWizardPage[] newConnectionWizardPages)
           throws Exception;
	/**
	 * Create a connection object. This is a simplified version
	 * <p>
	 * THE RESULTING CONNECTION OBJECT IS ADDED TO THE LIST OF EXISTING CONNECTIONS FOR YOU, IN
	 *  THE PROFILE YOU SPECIFY. THE PROFILE IS ALSO SAVED TO DISK.
	 * <p>
	 * This method:
	 * <ul>
	 *  <li>creates and saves a new connection within the given profile
	 *  <li>calls all subsystem factories to give them a chance to create a subsystem instance
	 *  <li>fires an ISystemResourceChangeEvent event of type EVENT_ADD to all registered listeners
	 * </ul>
	 * <p>
	 * @param profileName Name of the system profile the connection is to be added to.
	 * @param systemType system type matching one of the system type names defined via the
	 *                    systemtype extension point.
	 * @param connectionName unique connection name.
	 * @param hostName ip name of host.
	 * @param description optional description of the connection. Can be null.
	 * @return SystemConnection object, or null if it failed to create. This is typically
	 *   because the connectionName is not unique. Call getLastException() if necessary.
	 */
	public IHost createHost(String profileName, String systemType, String connectionName, String hostName, String description)
		throws Exception;

	/**
	 * Create a connection object. This is a very simplified version that defaults to the user's
	 *  private profile, or the first active profile if there is no private profile.
	 * <p>
	 * THE RESULTING CONNECTION OBJECT IS ADDED TO THE LIST OF EXISTING CONNECTIONS FOR YOU, IN
	 *  THE DEFAULT PRIVATE PROFILE, WHICH IS SAVED TO DISK.
	 * <p>
	 * This method:
	 * <ul>
	 *  <li>creates and saves a new connection within the given profile
	 *  <li>calls all subsystem factories to give them a chance to create a subsystem instance
	 *  <li>fires an ISystemResourceChangeEvent event of type EVENT_ADD to all registered listeners
	 * </ul>
	 * <p>
	 * @param systemType system type matching one of the system type names defined via the
	 *                    systemtype extension point.
	 * @param connectionName unique connection name.
	 * @param hostName ip name of host.
	 * @param description optional description of the connection. Can be null.
	 * @return SystemConnection object, or null if it failed to create. This is typically
	 *   because the connectionName is not unique. Call getLastException() if necessary.
	 */
	public IHost createHost(String systemType, String connectionName, String hostName, String description)
		throws Exception;
		
    /**
     * Update an existing connection given the new information.
     * This method:
     * <ul>
     *  <li>calls the setXXX methods on the given connection object, updating the information in it.
     *  <li>save the connection's connection pool to disk
     *  <li>fires an ISystemResourceChangeEvent event of type EVENT_CHANGE to all registered listeners
     *  <li>if the systemtype or hostname is changed, calls disconnect on each associated subsystem.
     *       We must do this because a hostname changes fundamentally affects the connection, 
     *       rendering any information currently displayed under
     *       that connection obsolete. That is, the user will have to reconnect.
     * </ul>
     * <p>
     * @param conn SystemConnection to be updated
     * @param systemType system type matching one of the system type names defined via the
     *                    systemtype extension point.
     * @param connectionName unique connection name.
     * @param hostName ip name of host.
     * @param description optional description of the connection. Can be null.
     * @param defaultUserIdLocation one of the constants in {@link org.eclipse.rse.core.ISystemUserIdConstants ISystemUserIdConstants}
     *   that tells us where to set the user Id
     * @param defaultUserId userId to use as the default for the subsystems.
     */
    public void updateHost(Shell shell, IHost conn, String systemType,
                                 String connectionName, String hostName,
                                 String description,String defaultUserId, int defaultUserIdLocation);
                                 
	/**
	 * Update the workoffline mode for a connection.
	 * 
	 * @param conn SystemConnection to change
	 * @param offline true if connection should be set offline, false if it should be set online
	 */
	public void setHostOffline(IHost conn, boolean offline);
                                 
    /**
     * Delete an existing connection. 
     * <p>
     * Lots to do here:
     * <ul>
     *   <li>Delete all subsystem objects for this connection, including their file's on disk.
     *   <li>Delete the connection from memory.
     *   <li>Delete the connection's folder from disk.
     * </ul>
     * Assumption: firing the delete event is done elsewhere. Specifically, the doDelete method of SystemView.
     */
    public void deleteHost(IHost conn);
    /**
     * Renames an existing connection. 
     * <p>
     * Lots to do here:
     * <ul>
     *   <li>Reset the conn name for all subsystem objects for this connection
     *   <li>Rename the connection in memory.
     *   <li>Rename the connection's folder on disk.
     * </ul>
     * Assumption: firing the rename event is done elsewhere. Specifically, the doRename method of SystemView.
     */
    public void renameHost(IHost conn, String newName) throws Exception;
    /**
     * Move existing connections a given number of positions in the same profile.
     * If the delta is negative, they are all moved up by the given amount. If 
     * positive, they are all moved down by the given amount.<p>
     * <ul>
     * <li>After the move, the pool containing the moved connection is saved to disk.
     * <li>The connection's name must be unique in pool.
     * <li>Fires a single ISystemResourceChangeEvent event of type EVENT_MOVE, if the pool is the private pool.
     * </ul>
     * <b>TODO PROBLEM: CAN'T RE-ORDER FOLDERS SO CAN WE SUPPORT THIS ACTION?</b>
     * @param conns Array of SystemConnections to move.
     * @param newPosition new zero-based position for the connection
     */
    public void moveHosts(String profileName, IHost conns[], int delta);
    /**
     * Copy a SystemConnection. All subsystems are copied, and all connection data is copied.
     * @param monitor Progress monitor to reflect each step of the operation
     * @param conn The connection to copy
     * @param targetProfile What profile to copy into
     * @param newName Unique name to give copied profile
     * @return new SystemConnection object
     */
    public IHost copyHost(IProgressMonitor monitor, IHost conn, 
                                           ISystemProfile targetProfile, String newName)
           throws Exception;
    /**
     * Move a SystemConnection to another profile. All subsystems are copied, and all connection data is copied.
     * @param monitor Progress monitor to reflect each step of the operation
     * @param conn The connection to move
     * @param targetProfile What profile to move to
     * @param newName Unique name to give moved profile
     * @return new SystemConnection object
     */
    public IHost moveHost(IProgressMonitor monitor, IHost conn, 
                                           ISystemProfile targetProfile, String newName)
           throws Exception;

    /**
     * Return true if any of the subsystems for the given connection are currently connected
     */
    public boolean isAnySubSystemConnected(IHost conn);

    /**
     * Return true if all of the subsystems for the given connection are currently connected
     */
    public boolean areAllSubSystemsConnected(IHost conn);
    
    /**
     * Disconnect all subsystems for the given connection, if they are currently connected.
     */
    public void disconnectAllSubSystems(IHost conn);

    /**
     * Inform the world when the connection status changes for a subsystem within a connection
     */
    public void connectedStatusChange(ISubSystem subsystem, boolean connected, boolean wasConnected);

    /**
     * Inform the world when the connection status changes for a subsystem within a connection
     */
    public void connectedStatusChange(ISubSystem subsystem, boolean connected, boolean wasConnected, boolean collapseTree);
    
    // ----------------------------
    // EVENT METHODS...
    // ----------------------------            
    
    /**
     * Register your interest in being told when a system resource such as a connection is changed.
     */
    public void addSystemResourceChangeListener(ISystemResourceChangeListener l);
    /**
     * De-Register your interest in being told when a system resource such as a connection is changed.
     */
    public void removeSystemResourceChangeListener(ISystemResourceChangeListener l);
    /**
     * Query if the ISystemResourceChangeListener is already listening for SystemResourceChange events
     */
    public boolean isRegisteredSystemResourceChangeListener(ISystemResourceChangeListener l);
    /**
     * Notify all listeners of a change to a system resource such as a connection.
     * You would not normally call this as the methods in this class call it when appropriate.
     */
    public void fireEvent(ISystemResourceChangeEvent event);
    /**
     * Notify a specific listener of a change to a system resource such as a connection.
     */
    public void fireEvent(ISystemResourceChangeListener l, ISystemResourceChangeEvent event);
    /**
     * Notify all listeners of a change to a system resource such as a connection.
     * You would not normally call this as the methods in this class call it when appropriate.
     * <p>
     * This version calls fireEvent at the next reasonable opportunity, leveraging SWT's 
     * Display.asyncExec() method.
     */
    public void postEvent(ISystemResourceChangeEvent event);
    /**
     * Notify a specific listener of a change to a system resource such as a connection.
     * <p>
     * This version calls fireEvent at the next reasonable opportunity, leveraging SWT's 
     * Display.asyncExec() method.
     */
    public void postEvent(ISystemResourceChangeListener l, ISystemResourceChangeEvent event);

    // ----------------------------
    // PREFERENCE EVENT METHODS...
    // ----------------------------            
    
    /**
     * Register your interest in being told when a system preference changes
     */
    public void addSystemPreferenceChangeListener(ISystemPreferenceChangeListener l);
    /**
     * De-Register your interest in being told when a system preference changes
     */
    public void removeSystemPreferenceChangeListener(ISystemPreferenceChangeListener l);
    /**
     * Notify all listeners of a change to a system preference
     * You would not normally call this as the methods in this class call it when appropriate.
     */
    public void fireEvent(ISystemPreferenceChangeEvent event);
    /**
     * Notify a specific listener of a change to a system preference 
     */
    public void fireEvent(ISystemPreferenceChangeListener l, ISystemPreferenceChangeEvent event);

    // ----------------------------
    // MODEL RESOURCE EVENT METHODS...
    // ----------------------------            
    
    /**
     * Register your interest in being told when an RSE model resource is changed.
     * These are model events, not GUI-optimized events.
     */
    public void addSystemModelChangeListener(ISystemModelChangeListener l);

    /**
     * De-Register your interest in being told when an RSE model resource is changed.
     */
    public void removeSystemModelChangeListener(ISystemModelChangeListener l);

    /**
     * Notify all listeners of a change to a system model resource such as a connection.
     * You would not normally call this as the methods in this class call it when appropriate.
     */
    public void fireEvent(ISystemModelChangeEvent event);
    /**
     * Notify all listeners of a change to a system model resource such as a connection.
     * This one takes the information needed and creates the event for you.
     */
    public void fireModelChangeEvent(int eventType, int resourceType, Object resource, String oldName);
    
    /**
     * Notify a specific listener of a change to a system model resource such as a connection.
     */
    public void fireEvent(ISystemModelChangeListener l, ISystemModelChangeEvent event);

    // --------------------------------
    // REMOTE RESOURCE EVENT METHODS...
    // --------------------------------            
    
    /**
     * Register your interest in being told when a remote resource is changed.
     * These are model events, not GUI-optimized events.
     */
    public void addSystemRemoteChangeListener(ISystemRemoteChangeListener l);

    /**
     * De-Register your interest in being told when a remote resource is changed.
     */
    public void removeSystemRemoteChangeListener(ISystemRemoteChangeListener l);

    /**
     * Notify all listeners of a change to a remote resource such as a file.
     * You would not normally call this as the methods in this class call it when appropriate.
     */
    public void fireEvent(ISystemRemoteChangeEvent event);

    /**
     * Notify all listeners of a change to a remote resource such as a file.
     * This one takes the information needed and creates the event for you.
	 * @param eventType - one of the constants from {@link org.eclipse.rse.model.ISystemRemoteChangeEvents}
	 * @param resource - the remote resource object, or absolute name of the resource as would be given by calling getAbsoluteName on its remote adapter
	 * @param resourceParent - the remote resource's parent object, or absolute name, if that is known. If it is non-null, this will aid in refreshing occurences of that parent.
	 * @param subsystem - the subsystem which contains this remote resource. This allows the search for impacts to be 
	 *   limited to subsystems of the same parent factory, and to connections with the same hostname as the subsystem's connection.
	 * @param oldName - on a rename operation, this is the absolute name of the resource prior to the rename
	 * @param originatingViewer - optional. If set, this gives the viewer a clue that it should select the affected resource after refreshing its parent. 
	 *    This saves sending a separate event to reveal and select the new created resource on a create event, for example.
     */
    public void fireRemoteResourceChangeEvent(int eventType, Object resource, Object resourceParent, ISubSystem subsystem, String oldName, Viewer originatingViewer);
    
    /**
     * Notify a specific listener of a change to a remote resource such as a file.
     */
    public void fireEvent(ISystemRemoteChangeListener l, ISystemRemoteChangeEvent event);

    // ----------------------------
    // MISCELLANEOUS METHODS...
    // ----------------------------
            	
    /**
     * Returns filter references associated with this resource under the subsystem
     */
	public List findFilterReferencesFor(Object resource, ISubSystem subsystem);
	
	/**
	 * Marks all filters for this subsystem as stale to prevent caching
	 * @param subsystem
	 */
	public void invalidateFiltersFor(ISubSystem subsystem);
	
	/**
	 * Marks all filters for this subsystem the contain resourceParent as stale to prevent caching
	 * @param resourceParent 
	 * @param subsystem
	 */
	public void invalidateFiltersFor(Object resourceParent, ISubSystem subsystem);

   
	
    /**
     * Return last exception object caught in any method, or null if no exception.
     * This has the side effect of clearing the last exception.
     */
    public Exception getLastException();
    
    // ----------------------------
    // SAVE / RESTORE METHODS...
    // ----------------------------            

    /**
     * Save everything! 
     */
    public boolean save();
    /**
     * Save specific connection pool
     * @return true if saved ok, false if error encountered. If false, call getLastException().
     */
    public boolean saveHostPool(ISystemHostPool pool);
    /**
     * Save specific connection
     * @return true if saved ok, false if error encountered. If false, call getLastException().
     */
    public boolean saveHost(IHost conn);
    /**
     * Restore all connections within active profiles
     * @return true if restored ok, false if error encountered. If false, call getLastException().
     */
    public boolean restore();
} //SystemRegistry
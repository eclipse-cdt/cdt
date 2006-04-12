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


import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.rse.filters.ISystemFilter;
import org.eclipse.rse.filters.ISystemFilterPool;
import org.eclipse.rse.filters.ISystemFilterPoolManager;
import org.eclipse.rse.filters.ISystemFilterPoolManagerProvider;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.model.ISystemProfile;
import org.eclipse.rse.persistence.IRSEPersistableContainer;
import org.eclipse.rse.ui.SystemMenuManager;
import org.eclipse.rse.ui.messages.ISystemMessageLine;
import org.eclipse.rse.ui.validators.ISystemValidator;
import org.eclipse.rse.ui.wizards.ISystemNewConnectionWizardPage;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;




public interface ISubSystemConfiguration extends ISystemFilterPoolManagerProvider, IRSEPersistableContainer
{
	// ---------------------------------
	// CONSTANTS...
	// ---------------------------------
    public static final boolean FORCE_INTO_MEMORY = true;
    public static final boolean LAZILY = false;

	/**
	 * Reset for a full refresh from disk, such as after a team synch.
	 */
	public void reset();
	// ---------------------------------
	// CRITICAL METHODS...
	// ---------------------------------
    /**
     * Return true (default) or false to indicate if subsystems of this factory require a userId to
     *  do connection or not. If not, no GUI will be supplied related to user Ids in the remote systems
     *  explorer view.
	 * <p>Returns true in default implementation.
     */
    public boolean supportsUserId();
    
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
	 * Return true if filters of this subsystem factory provide a custom implementation of drop support.  
	 * By default, the filter reference adapter treats a drop on a filter as an update to the list of filter
	 * strings for a filter.  For things like files, it is more desirable to treat the drop as a physical 
	 * resource copy, so in that case, custom drop makes sense.
	 * 
	 * By default this returns false.
	 */
	public boolean providesCustomDropInFilters();
	
	/**
	 * Return true if you support user-defined actions for the remote system objects returned from expansion of
	 *  subsystems created by this subsystem factory
	 * @see #getActionSubSystem(ISubSystem)
	 * @see #supportsUserDefinedActions(ISelection)
	 * <p>Returns false in default implementation.
	 */
	public boolean supportsUserDefinedActions();
	/**
	 * Return true if you support user-defined actions for the remote system objects explicitly given. This
	 *  calls supportsUserDefinedActions() by default. It is called when decided whether or not to show
	 *  the User Actions menu for the current selection, if supportsUserDefinedActions() returns true. 
	 * @see #getActionSubSystem(ISubSystem)
	 * @see #createActionSubSystem()
	 * @see #addCommonRemoteActions(SystemMenuManager, IStructuredSelection, Shell, String, ISubSystem)
	 */
	public boolean supportsUserDefinedActions(ISelection selection);
	/**
	 * Return true if you support compile actions for the remote system objects returned from expansion of
	 *  subsystems created by this subsystem factory.
	 * @see #getCompileManager()
	 * <p>Returns false in default implementation.
	 */
	public boolean supportsCompileActions();
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
	 * <br> By default we return false here. This is overridden in UniversalFileSubSystemFactory though.
	 */
	public boolean supportsServerLaunchProperties(IHost host);
	/**
	 * If {@link #supportsServerLaunchProperties()} returns true, this method may be called by
	 * the server launcher to decide if a given remote server launch type is supported or not.
	 * <br> We return true by default.
	 * @see org.eclipse.rse.core.subsystems.ServerLaunchType
	 * @see #getServerLauncherForm(Shell, ISystemMessageLine)
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
    public void setSubSystemFactoryProxy(ISubSystemConfigurationProxy proxy);
    /**
     * Private method
     */
    public ISubSystemConfigurationProxy getSubSystemFactoryProxy();

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
     * Return image descriptor for subsystems created by this factory. Comes from icon attribute in extension point xml
     */
    public ImageDescriptor getImage();
    /**
     * Return actual graphics Image of this factory.
     * This is the same as calling getImage().createImage() but the resulting image is cached
     */
    public Image getGraphicsImage();
    /**
     * Return image to use when this susystem is connection. Comes from icon attribute in extension point xml
     */
    public ImageDescriptor getLiveImage();
    /**
     * Return actual graphics LiveImage of this factory.
     * This is the same as calling getLiveImage().createImage() but the resulting image is cached
     */
    public Image getGraphicsLiveImage();
    /**
     * Return the category this subsystem factory subscribes to.
     * @see org.eclipse.rse.model.ISubSystemFactoryCategories
     */
    public String getCategory();
    /**
     * Return the system types this subsystem factory supports.
     */
    public String[] getSystemTypes();

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

	// ---------------------------------
	// SUBSYSTEM METHODS...
	// ---------------------------------
	/**
	 * Return the validator for the userId.
	 * A default is supplied.
	 * Note this is only used for the subsystem's properties, so will not
	 * be used by the connection's default. Thus, is only of limited value.
	 * <p>
	 * This must be castable to ICellEditorValidator for the property sheet support.
	 */
	public ISystemValidator getUserIdValidator();
	/**
	 * Return the validator for the password which is prompted for at runtime.
	 * No default is supplied.
	 */
	public ISystemValidator getPasswordValidator();
	/**
	 * Return the validator for the port.
	 * A default is supplied.
	 * This must be castable to ICellEditorValidator for the property sheet support.
	 */
	public ISystemValidator getPortValidator();
	/**
	 * Called by SystemRegistry's renameSystemProfile method to ensure we update our
	 *  subsystem names within each subsystem.
	 * <p>
	 * This is called AFTER changing the profile's name!!
	 */
	public void renameSubSystemProfile(ISubSystem ss, String oldProfileName, String newProfileName);
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
     * @param yourNewConnectionWizardPages The wizard pages you supplied to the New Connection wizard, via the
     *            {@link #getNewConnectionWizardPages(IWizard)} method or null if you didn't override this method.
     */
    public ISubSystem createSubSystem(IHost conn, boolean creatingConnection, ISystemNewConnectionWizardPage[] yourNewConnectionWizardPages);

	// used in the case where newsubsystems are added after a connection exists
	public ISubSystem createSubsystemAfterTheFact(IHost conn);

	public ISubSystem createSubSystemInternal(IHost conn);
	
	
	/**
	 * Find or create a connector service for this host
	 * @param host
	 * @return
	 */
	public IConnectorService getConnectorService(IHost host);
	   
	/**
	 * Overridable entry for child classes to contribute a server launcher instance
	 *  for a given subsystem.
	 * <p>
	 * Create an instance of ServerLauncher, and add it to the given subsystem.
	 * When a subsystem is created, and {@link #supportsServerLaunchProperties()}
	 * returns true, this method is called to create the server launcher instance
	 * associated with the subsystem. The default implementation is to create an
	 * instance of {@link IIBMServerLauncher}, but override to create your own 
	 * ServerLauncher instance if you have your own class.
	 */
	public IServerLauncherProperties createServerLauncher(IConnectorService connectorService);
	
    /**
     * Updates user-editable attributes of an existing subsystem instance.
     * These attributes typically affect the live connection, so the subsystem will be forced to
     *  disconnect.
     * <p>
     * The subsystem will be saved to disk.
     * @param shell parent shell needed in case an error message is displayed
     * @param subsystem target of the update action
     * @param updateUserId true if we are updating the userId, else false to ignore userId
     * @param userId new local user Id. Ignored if updateUserId is false
     * @param updatePort true if we are updating the port, else false to ignore port
     * @param port new local port value. Ignored if updatePort is false
     */
    public void updateSubSystem(Shell shell, ISubSystem subsystem, boolean updateUserId, String userId, boolean updatePort, int port);
	/**
	 * Update the port for the given subsystem instance.
	 * Shortcut to {@link #updateSubSystem(Shell,ISubSystem,boolean,String,boolean,Integer)}
	 */
	public void setSubSystemPort(Shell shell, ISubSystem subsystem, int port);
	/**
	 * Update the user ID for the given subsystem instance.
	 * Shortcut to {@link #updateSubSystem(Shell,ISubSystem,boolean,String,boolean,Integer)}
	 */
	public void setSubSystemUserId(Shell shell, ISubSystem subsystem, String userId);

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
    public ISubSystem cloneSubSystem(ISubSystem oldSubsystem, IHost newConnection, boolean copyProfileOperation)
                      throws Exception;
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
    public void disconnectAllSubSystems()
                throws Exception;

	// ---------------------------------
	// FILTER POOL METHODS...
	// ---------------------------------
    /**
     * Get the filter pool manager for the given profile
     */
    public ISystemFilterPoolManager getFilterPoolManager(ISystemProfile profile);
    /**
     * Copy the filter pool manager and return a new one. Called during profile-copy operations.
     * Will also copy all of the filter pools and their respective data.
     */
    public ISystemFilterPoolManager copyFilterPoolManager(ISystemProfile oldProfile, ISystemProfile newProfile)
        throws Exception;

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
    public void saveSubSystem(ISubSystem subsys)
       throws Exception;

	/**
	 * <i><b>Private</b>. Do not call or use.</i><br>
	 * @generated This field/method will be replaced during code generation 
	 * @return The list of SubSystemList references
	 */
	java.util.List getSubSystemList();

	/**
	 * <i><b>Private</b>. Do not call or use.</i><br>
	 * @generated This field/method will be replaced during code generation 
	 * @return The list of FilterPoolManagerList references
	 */
	java.util.List getFilterPoolManagerList();
	
	public ISystemFilterPool getDefaultFilterPool(ISystemProfile profile, String oldProfileName);
	public ISystemProfile getSystemProfile(String name);
	public void renameFilterPoolManager(ISystemProfile profile);



}
/********************************************************************************
 * Copyright (c) 2002, 2011 IBM Corporation and others. All rights reserved.
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
 * David Dykstal (IBM) - 168870: moved SystemPreferencesManager to a new package
 * David Dykstal (IBM) - 168977: refactoring IConnectorService and ServerLauncher hierarchies
 * David Dykstal (IBM) - 168870: made use of adapters on the SubSystemConfigurationProxy
 * Martin Oberhuber (Wind River) - [175262] IHost.getSystemType() should return IRSESystemType
 * David Dykstal (IBM) - 142806: refactoring persistence framework
 * Martin Oberhuber (Wind River) - [168975] Move RSE Events API to Core
 * Martin Oberhuber (Wind River) - [184095] Replace systemTypeName by IRSESystemType
 * Martin Oberhuber (Wind River) - [186748] Move ISubSystemConfigurationAdapter from UI/rse.core.subsystems.util
 * Martin Oberhuber (Wind River) - [186773] split ISystemRegistryUI from ISystemRegistry
 * Martin Oberhuber (Wind River) - [189123] Move renameSubSystemProfile() from UI to Core
 * Martin Oberhuber (Wind River) - [190231] Remove UI-only code from SubSystemConfiguration
 * Rupen Mardirossian (IBM) - [189434] Move Up/Down on Filters Error
 * Kevin Doyle (IBM) - [190445] Set Position of cloned event in cloneEvent()
 * Martin Oberhuber (Wind River) - [195392] Avoid setting port 0 in initializeSubSystem()
 * David Dykstal (IBM) - [197036] rewrote getFilterPoolManager to delay the creation of default filter pools until the corresponding
 *                                a subsystem configuration is actually used for a host.
 * David Dykstal (IBM) - [217556] remove service subsystem types
 * Martin Oberhuber (Wind River) - [cleanup] Avoid using SystemStartHere in production code
 * David McKnight   (IBM)        - [220309] [nls] Some GenericMessages and SubSystemResources should move from UI to Core
 * David McKnight   (IBM)        - [220547] [api][breaking] SimpleSystemMessage needs to specify a message id and some messages should be shared
 * Xuan Chen        (IBM)        - [223126] [api][breaking] Remove API related to User Actions in RSE Core/UI
 * David McKnight   (IBM)        - [225506] [api][breaking] RSE UI leaks non-API types
 * David Dykstal (IBM) - [168976][api] move ISystemNewConnectionWizardPage from core to UI
 * Martin Oberhuber (Wind River) - [226574][api] Add ISubSystemConfiguration#supportsEncoding()
 * David Dykstal (IBM) - [236516] Bug in user code causes failure in RSE initialization
 * Martin Oberhuber (Wind River) - [218309] ConcurrentModificationException during workbench startup
 * David McKnight   (IBM)        - [338510] "Copy Connection" operation deletes the registered property set in the original connection
 ********************************************************************************/

package org.eclipse.rse.core.subsystems;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.util.NLS;
import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.events.ISystemModelChangeEvents;
import org.eclipse.rse.core.events.ISystemResourceChangeEvents;
import org.eclipse.rse.core.events.SystemResourceChangeEvent;
import org.eclipse.rse.core.filters.ISystemFilter;
import org.eclipse.rse.core.filters.ISystemFilterContainer;
import org.eclipse.rse.core.filters.ISystemFilterContainerReference;
import org.eclipse.rse.core.filters.ISystemFilterPool;
import org.eclipse.rse.core.filters.ISystemFilterPoolManager;
import org.eclipse.rse.core.filters.ISystemFilterPoolReference;
import org.eclipse.rse.core.filters.ISystemFilterPoolReferenceManager;
import org.eclipse.rse.core.filters.ISystemFilterPoolWrapperInformation;
import org.eclipse.rse.core.filters.ISystemFilterString;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.ILabeledObject;
import org.eclipse.rse.core.model.IProperty;
import org.eclipse.rse.core.model.IPropertySet;
import org.eclipse.rse.core.model.IPropertySetContainer;
import org.eclipse.rse.core.model.IRSEPersistableContainer;
import org.eclipse.rse.core.model.ISubSystemConfigurator;
import org.eclipse.rse.core.model.ISystemProfile;
import org.eclipse.rse.core.model.ISystemProfileManager;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.core.references.IRSEBaseReferencingObject;
import org.eclipse.rse.internal.core.RSECoreMessages;
import org.eclipse.rse.internal.core.SystemResourceConstants;
import org.eclipse.rse.internal.core.filters.SystemFilterPoolManager;
import org.eclipse.rse.internal.core.filters.SystemFilterPoolWrapperInformation;
import org.eclipse.rse.internal.core.filters.SystemFilterStartHere;
import org.eclipse.rse.internal.core.model.SystemProfileManager;
import org.eclipse.rse.internal.ui.SystemPropertyResources;
import org.eclipse.rse.internal.ui.SystemResources;
import org.eclipse.rse.logging.Logger;
import org.eclipse.rse.services.IService;
import org.eclipse.rse.services.clientserver.messages.CommonMessages;
import org.eclipse.rse.services.clientserver.messages.SimpleSystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.ui.SystemBasePlugin;
import org.eclipse.rse.ui.SystemPreferencesManager;
import org.eclipse.rse.ui.messages.SystemMessageDialog;

/**
 * Abstract base class for subsystem configuration extension points.
 * Child classes must implement the methods:
 * <ul>
 * <li>#createSubSystemInternal(SystemConnection conn)
 * </ul>
 * Child classes can optionally override:
 * <ul>
 *  <li>SubSystemConfiguration#supportsFilters() to indicate if filters are to be enabled for this subsystem configuration
 *  <li>SubSystemConfiguration#supportsNestedFilters() to indicate if filters can exist inside filters.
 *  <li>SubSystemConfiguration#supportsDuplicateFilterStrings() to indicate if filter strings can be duplicated within a filter
 *  <li>SubSystemConfiguration#isCaseSensitive() to indicate if filter strings are case sensitive or not
 *  <li>SubSystemConfiguration#supportsQuickFilters() to indicate if filters can be specified at contain expansion time.
 *  <li>SubSystemConfiguration#supportsUserActions() to indicate if users can define their own actions for your subsystems' child objects.
 *  <li>SubSystemConfiguration#supportsFileTypes() to indicate if users can define their own named file types.
 *  <li>SubSystemConfiguration#isSubSystemsDeletable() if they support user-deleting of subsystems. Default is false.
 *  <li>SubSystemConfiguration#supportsSubSystemConnect() to return false if the connect() action is not supported
 *  <li>SubSystemConfiguration#supportsTargets() to return true if this subsystem configuration supports the notions of targets. Normally, this is only for file system factories.
 *  <li>SubSystemConfiguration#getSubSystemActions() if they wish to supply actions for the right-click menu when
 *       the user right clicks on a subsystem object created by this subsystem configuration.
 *  <li>CreateDefaultFilterPool() to create any default filter pool when a new profile is created.
 * <li>#initializeSubSystem(SubSystem ss, configurarators[])
 * </ul>
 * <p>
 * A subsystem configuration will maintain in memory a list of all subsystem
 * objects it has. This list should be initialized from disk at restore time,
 * and maintained as the subsystems are created and deleted throughout the
 * session. At save time, each subsystem in the list is asked to save itself.
 * The getSubSystems method should return this list.
 * <p>
 * To help with maintaining this list, this base class contains a List instance
 * variable named subsystems. It is returned by the getSubSystems method in this
 * base class. For this to be accurate you though, you should:
 * <ul>
 *   <li>Not implement createSubSystem directly, but rather let this class handle it. Instead
 *         implement the method createSubSystemInternal. This is called by createSubSystem in this
 *         class.
 * </ul>
 * Should you prefer to maintain your own list, simply override getSubSystems.
 */

public abstract class SubSystemConfiguration  implements ISubSystemConfiguration
{
	// subsystem stuff...
	private Hashtable subSystemsRestoredFlags = new Hashtable();
	private ISubSystemConfigurationProxy proxy = null;
	private ISubSystem[] subsystems = null;
	private Hashtable subsystemsByConnection = new Hashtable();

	private boolean allSubSystemsRestored = false;
	private static final ISubSystem[] EMPTY_SUBSYSTEM_ARRAY = new ISubSystem[0];

	// filters stuff...
	protected ISystemFilterPoolManager[] filterPoolManagers = null;
	protected Hashtable filterPoolManagersPerProfile = new Hashtable();

	// other stuff...
	private String translatedFilterType = null;
	private static Hashtable brokenReferenceWarningsIssued = new Hashtable();
	protected IHost currentlySelectedConnection;
	protected Object[] currentlySelected;

	// support for default subclasses for non-mof users
	protected static IHost currentlyProcessingConnection;
	protected static SubSystemConfiguration currentlyProcessingSubSystemConfiguration;

	/**
	 * Internal list of subsystems. Must always be accessed in synchronized
	 * blocks to protect against concurrent modification. For API compliance,
	 * clients should always call {@link #getSubSystemList()} instead of
	 * accessing this field directly.
	 *
	 * @noreference This field is not intended to be referenced by clients.
	 */
	protected List subSystemList = new ArrayList();

	/**
	 * Internal list of filter pool managers. Must always be accessed in
	 * synchronized blocks. For API compliance, clients should always call
	 * {@link #getFilterPoolManagerList()} instead of accessing this field
	 * directly.
	 *
	 * @noreference This field is not intended to be referenced by clients.
	 */
	protected List filterPoolManagerList = new ArrayList();

//	protected boolean _isDirty;


	/**
	 * Constructor
	 */
	public SubSystemConfiguration()
	{
		super();
		//initSubSystems();
		SystemBasePlugin.logDebugMessage(this.getClass().getName(), "STARTED SSFACTORY"); //$NON-NLS-1$
	}

	/**
	 * Reset for a full refresh from disk, such as after a team synch.
	 * Override this as required, but please call super.reset()!!
	 */
	public void reset()
	{
		subSystemsRestoredFlags = new Hashtable();
		subsystems = null;
		subsystemsByConnection = new Hashtable();
		allSubSystemsRestored = false;
		filterPoolManagersPerProfile = new Hashtable();
		filterPoolManagers = null;
		brokenReferenceWarningsIssued = new Hashtable();
	}

	// ---------------------------------
	// CRITICAL METHODS...
	// ---------------------------------

	/**
	 * Test whether this subsystem configuration supports custom encodings. We
	 * fall back to the setting provided by the host, or its underlying system
	 * type by default.
	 *
	 * @see ISubSystemConfiguration#supportsEncoding(IHost)
	 * @since org.eclipse.rse.core 3.0
	 */
	public boolean supportsEncoding(IHost host) {
		// support encodings by default
		boolean rv = true;
		if (host.getSystemType().testProperty(IRSESystemType.PROPERTY_SUPPORTS_ENCODING, false)) {
			// switched off on system type level
			rv = false;
		}
		return rv;
	}

	/**
	 * Return true if instance of this subsystem configuration's subsystems support connect and disconnect actions.
	 * <b>By default, returns true</b>.
	 * Override if this is not the case.
	 */
	public boolean supportsSubSystemConnect()
	{
		return true;
	}
	/**
	 * Return true (default) or false to indicate if subsystems of this subsystem configuration support user-editable
	 *  port numbers.
	 */
	public boolean isPortEditable()
	{
		return true;
	}
	/**
	 * Return true if subsystem instances from this subsystem configuration support remote command execution
	 * <p>RETURNS FALSE BY DEFAULT.
	 */
	public boolean supportsCommands()
	{
		return false;
	}
	/**
	 * Return true if subsystem instances from this subsystem configuration support getting and setting properties
	 * <p>RETURNS FALSE BY DEFAULT.
	 *
	 * @return <code>false</code> to indicate that Properties are not supported by default.
	 */
	public boolean supportsProperties()
	{
		return false;
	}
	/**
	 * Return true if you support filters, false otherwise.
	 * If you support filters, then some housekeeping will be
	 * done for you automatically. Specifically, they
	 * will be saved and restored for you automatically.
	 * The default is to support filters.
	 *
	 * @return <code>true</code> to indicate that Filters are supported by default.
	 */
	public boolean supportsFilters() {
		return true;
	}

    /**
     * Indicates whether the subsystem supports displaying children under
     * its filters.  By default, this will return true, but if filters that can't
     * be expanded are desired, this can be overridden to return false.
     */
    public boolean supportsFilterChildren()
    {
    	return true;
    }

    /**
     * Required method for subsystem configuration child classes. Return true if you filter caching.
     * If you support filter caching, then the views will always check the in-memory cache for
     * filter results before attempting a query.
	 * <p>Returns true in default implementation.
     */
	public boolean supportsFilterCaching()
	{
		return true;
	}

	/**
	 * Required method for subsystem configuration child classes. Return true if you support filters, and you support
	 *  multiple filter strings per filter. Return false to restrict the user to one string per filter.
	 * <p>Returns TRUE by default.
	 */
	public boolean supportsMultipleFilterStrings()
	{
		return true;
	}

	/**
	 * Required method for subsystem configuration child classes if returning true from supportsFilters.
	 * Return true if you support filters within filters, false otherwise.
	 * <p>RETURNS supportsFilters() BY DEFAULT.
	 */
	public boolean supportsNestedFilters()
	{
		return supportsFilters();
	}
	/**
	 * Return true if you support quick filters. These allow the user to subset a remote system object at
	 *  the time they expand it in the remote system explorer tree view.
	 * <p>RETURNS supportsFilters() BY DEFAULT.
	 * <p>THIS IS NOT SUPPORTED BY THE FRAMEWORK YET
	 */
	public boolean supportsQuickFilters()
	{
		return supportsFilters();
	}

	/**
	 * Return true if filters of this subsystem configuration support dropping into.
	 * Override this method to provide drop support for filters.
	 */
	public boolean supportsDropInFilters()
	{
	    return false;
	}

	/**
	 * Return true if filters of this subsystem configuration provide a custom implementation of drop support.
	 * By default, the filter reference adapter treats a drop on a filter as an update to the list of filter
	 * strings for a filter.  For things like files, it is more desirable to treat the drop as a physical
	 * resource copy, so in that case, custom drop makes sense.
	 *
	 * By default this returns false.
	 */
	public boolean providesCustomDropInFilters()
	{
		return false;
	}

	/**
	 * Return true if you support user-defined/managed named file types
	 * <p>RETURNS false BY DEFAULT
	 */
	public boolean supportsFileTypes()
	{
		return false;
	}

	/**
	 * Return true if the subsystem supports more than one filter string
	 * <p>RETURNS true BY DEFAULT
	 */
	public boolean supportsMultiStringFilters()
	{
		return true;
	}

	/**
	  * Return true if the subsystem supports the exporting of filter strings from it's filters
		  * <p>RETURNS true BY DEFAULT
	  */
	public boolean supportsFilterStringExport()
	{
		return true;
	}

	/**
	 * Tell us if filter strings are case sensitive. The default is false.
	 */
	public boolean isCaseSensitive()
	{
		return false;
	}
	/**
	 * Tell us if duplicate filter strings are supported. The default is false.
	 */
	public boolean supportsDuplicateFilterStrings()
	{
		return false;
	}
	/**
	 * Tell us if this subsystem configuration supports targets, which are destinations for
	 *   pushes and builds. Normally only true for file system factories.
	 */
	public boolean supportsTargets()
	{
		return false;
	}
	/**
	 * Tell us if this subsystem configuration supports server launch properties, which allow the user
	 * to configure how the server-side code for these subsystems are started. There is a Server
	 * Launch Setting property page, with a pluggable composite, where users can configure these
	 * properties.
	 * <p>
	 * If you return true here, you may also want to override {@link #supportsServerLaunchType(ServerLaunchType)}.
	 * <br> By default we return false here. This is overridden in UniversalFileSubSystemConfiguration though.
	 */
	public boolean supportsServerLaunchProperties(IHost host) {
		return false;
	}

	/**
	 * If {@link #supportsServerLaunchProperties(IHost)} returns true, this method may be called by
	 * the server launcher to decide if a given remote server launch type is supported or not.
	 * <br> We return true by default.
	 * @see org.eclipse.rse.core.subsystems.ServerLaunchType
	 */
	public boolean supportsServerLaunchType(ServerLaunchType serverLaunchType)
	{
		return true;
	}

	/**
	 * Determines whether this subsystem configuration is responsible for the
	 * creation of subsystems of the specified type Subsystem factories should
	 * override this to indicate which subsystems they support.
	 *
	 * @param subSystemType type of subsystem
	 * @return whether this subsystem configuration is for the specified
	 *         subsystem type
	 */
	public boolean isFactoryFor(Class subSystemType)
	{
		//return SubSystem.class.isAssignableFrom(subSystemType);
		return false;
	}

	// ---------------------------------
	// USER-PREFERENCE METHODS...
	// ---------------------------------

	/**
	 * If we support filters, should we show filter pools in the remote system explorer?
	 * By default, this retrieves the setting from user preferences.
	 */
	public boolean showFilterPools()
	{
		return SystemPreferencesManager.getShowFilterPools();
	}
	/*
	 * If we support filters, should we show filter strings in the remote system explorer?
	 * By default, this retrieves the setting from user preferences.
	 *
	public boolean showFilterStrings()
	{
	    return SystemPreferencesManager.getPreferencesManager().getShowFilterStrings();
	}*/
	/**
	 * If we support filters, should we show filter pools in the remote system explorer?
	 * This is to set it after the user changes it in the user preferences. It may require
	 *  refreshing the current view.
	 */
	public void setShowFilterPools(boolean show)
	{
		ISubSystem[] subsystems = getSubSystems(false); // false=> lazy get; don't restore from disk if not already
		for (int idx = 0; idx < subsystems.length; idx++)
		{
			ISubSystem ss = subsystems[idx];
			RSECorePlugin.getTheSystemRegistry().fireEvent(new org.eclipse.rse.core.events.SystemResourceChangeEvent(ss, ISystemResourceChangeEvents.EVENT_CHANGE_CHILDREN, ss));
		}
	}

	// ---------------------------------
	// PROXY METHODS. USED INTERNALLY...
	// ---------------------------------

	/**
	 * The following is called for you by the SubSystemConfigurationProxy, after
	 *  starting this object via the extension point mechanism
	 */
	public void setSubSystemConfigurationProxy(ISubSystemConfigurationProxy proxy)
	{
		this.proxy = proxy;
		//FIXME initMOF();
	}
	/**
	 * The following is here for completeness but should never be needed.
	 */
	public ISubSystemConfigurationProxy getSubSystemConfigurationProxy()
	{
		return proxy;
	}

	// ---------------------------------
	// FACTORY ATTRIBUTE METHODS...
	// ---------------------------------

	/**
	 * Return vendor of this subsystem configuration. This comes from the xml
	 * "vendor" attribute of the extension point.
	 *
	 * @nooverride This method is not intended to be re-implemented or extended
	 * 	by clients. It will likely be declared <tt>final</tt> in the next
	 * 	release in order to ensure consistency with static xml markup in the
	 * 	extension.
	 */
	public String getVendor()
	{
		return proxy.getVendor();
	}

	/**
	 * Return name of this subsystem configuration. This comes from the xml
	 * "name" attribute of the extension point.
	 *
	 * @nooverride This method is not intended to be re-implemented or extended
	 * 	by clients. It will likely be declared <tt>final</tt> in the next
	 * 	release in order to ensure consistency with static xml markup in the
	 * 	extension.
	 */
	public String getName()
	{
		return proxy.getName();
	}

	/**
	 * Return name of this subsystem configuration. This comes from the xml
	 * "description" attribute of the extension point.
	 *
	 * @nooverride This method is not intended to be re-implemented or extended
	 * 	by clients. It will likely be declared <tt>final</tt> in the next
	 * 	release in order to ensure consistency with static xml markup in the
	 * 	extension.
	 */
	public String getDescription()
	{
		return proxy.getDescription();
	}

	/**
	 * Return unique id of this subsystem configuration. This comes from the xml
	 * "id" attribute of the extension point.
	 *
	 * @nooverride This method is not intended to be re-implemented or extended
	 * 	by clients. It will likely be declared <tt>final</tt> in the next
	 * 	release in order to ensure consistency with static xml markup in the
	 * 	extension.
	 */
	public String getId()
	{
		return proxy.getId();
	}

	/**
	 * Return the category this subsystem configuration subscribes to. This
	 * comes from the xml "category" attribute of the extension point.
	 *
	 * @see org.eclipse.rse.core.model.ISubSystemConfigurationCategories
	 *
	 * @nooverride This method is not intended to be re-implemented or extended
	 * 	by clients. It will likely be declared <tt>final</tt> in the next
	 * 	release in order to ensure consistency with static xml markup in the
	 * 	extension.
	 */
	public String getCategory()
	{
		return proxy.getCategory();
	}

	/**
	 * Return the system types this subsystem configuration supports. These come
	 * from static declaration in the
	 * <tt>org.eclipse.rse.core.subsystemConfigurations</tt> and
	 * <tt>org.eclipse.rse.core.systemTypes</tt> extension points.
	 *
	 * @nooverride This method is not intended to be re-implemented or extended
	 * 	by clients. It will likely be declared <tt>final</tt> in the next
	 * 	release in order to ensure consistency with static xml markup in the
	 * 	extension.
	 */
	public IRSESystemType[] getSystemTypes()
	{
		return proxy.getSystemTypes();
	}

	// ---------------------------------
	// PROFILE METHODS...
	// ---------------------------------

	// private methods...

	/**
	 * Get a profile object given its name
	 */
	public ISystemProfile getSystemProfile(String name)
	{
		return SystemProfileManager.getDefault().getSystemProfile(name);
	}

	/**
	 * Get a profile object given a filter pool manager object
	 */
	protected ISystemProfile getSystemProfile(ISystemFilterPoolManager poolMgr)
	{
		return getSystemProfile(getSystemProfileName(poolMgr));
	}

	/**
	 * Get owning profile object given a filter pool object
	 */
	public ISystemProfile getSystemProfile(ISystemFilterPool pool)
	{
		return getSystemProfile(pool.getSystemFilterPoolManager());
	}

	/**
	 * Get a profile object given a filter object
	 */
	protected ISystemProfile getSystemProfile(ISystemFilter filter)
	{
		return getSystemProfile(filter.getParentFilterPool());
	}

	/**
	 * Called by SystemRegistry when we are about to delete a profile.
	 * <p>
	 * Our only mission is to delete the filter pool associated with it,
	 * because the registry has already called deleteSubSystemsByConnection
	 * for every subsystem of every connection owned by this profile.
	 */
	public void deletingSystemProfile(ISystemProfile profile)
	{
		deleteFilterPoolManager(profile);
	}
	/**
	 * Called by SystemRegistry when we have toggled the active-status of a profile
	 */
	public void changingSystemProfileActiveStatus(ISystemProfile profile, boolean newStatus)
	{
		if (newStatus) // making a profile active/
		{
			allSubSystemsRestored = false; // next call to getSubSystems will restore the subsystems for the newly activated connections
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.ISubSystemConfiguration#renameSubSystemProfile(java.lang.String, java.lang.String)
	 */
	public void renameSubSystemProfile(String oldProfileName, String newProfileName)
	{
		//RSEUIPlugin.logDebugMessage(this.getClass().getName(), "Inside renameSubSystemProfile. newProfileName = "+newProfileName);
		ISystemProfile profile = getSystemProfile(newProfileName);
		renameFilterPoolManager(profile); // update filter pool manager name
		//if (profile.isDefaultPrivate()) // I don't remember why this was here, but it caused bad things, Phil.
		{
			// Rename the default filter pool for this profile, as it's name is derived from the profile.
			ISystemFilterPool defaultPoolForThisProfile = getDefaultFilterPool(profile, oldProfileName);
			if (defaultPoolForThisProfile != null)
				try
				{
					getFilterPoolManager(profile).renameSystemFilterPool(defaultPoolForThisProfile, SubSystemConfiguration.getDefaultFilterPoolName(newProfileName, getId()));
				}
				catch (Exception exc)
				{
					SystemBasePlugin.logError("Unexpected error renaming default filter pool " + SubSystemConfiguration.getDefaultFilterPoolName(newProfileName, getId()), exc); //$NON-NLS-1$
					System.out.println("Unexpected error renaming default filter pool " + SubSystemConfiguration.getDefaultFilterPoolName(newProfileName, getId()) + ": " + exc); //$NON-NLS-1$ //$NON-NLS-2$
				}
		}
	}

	// ---------------------------------
	// SUBSYSTEM METHODS...
	// ---------------------------------

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.ISubSystemConfiguration#renameSubSystemProfile(org.eclipse.rse.core.subsystems.ISubSystem, java.lang.String, java.lang.String)
	 */
	public void renameSubSystemProfile(ISubSystem subsystem, String oldProfileName, String newProfileName) {
		subsystem.renamingProfile(oldProfileName, newProfileName);
		ISystemFilterPoolReferenceManager sfprm = subsystem.getSystemFilterPoolReferenceManager();
		if (sfprm != null) {
			sfprm.regenerateReferencedSystemFilterPoolNames(); // ask it to re-ask each pool for its reference name
		}
		try {
			saveSubSystem(subsystem);
		} catch (Exception exc) {
			RSECorePlugin.getDefault().getLogger().logError("Unexpected error saving subsystem.", exc); //$NON-NLS-1$
		}
	}

	/**
	 * Called by SystemRegistry's renameSystemProfile method to pre-test if we are going to run into errors on a
	 *  profile rename, due to file or folder in use.
	 */
	public void preTestRenameSubSystemProfile(String oldProfileName) throws Exception
	{
		ISystemProfile profile = getSystemProfile(oldProfileName);
		if (profile.isDefaultPrivate())
		{
			ISystemFilterPool defaultPoolForThisProfile = getDefaultFilterPool(profile, oldProfileName);
			if (defaultPoolForThisProfile != null)
				getFilterPoolManager(profile).preTestRenameFilterPool(defaultPoolForThisProfile);
		}
	}

	/**
	 * Return the default filter pool for the given profile...
	 */
	public ISystemFilterPool getDefaultFilterPool(ISystemProfile profile, String oldProfileName)
	{
		ISystemFilterPool pool = null;
		ISystemFilterPoolManager mgr = getFilterPoolManager(profile);
		ISystemFilterPool[] pools = mgr.getSystemFilterPools();
		if (pools != null)
		{
			for (int idx = 0; (pool==null) && (idx < pools.length); idx++)
			{
				// first and best test
				if (pools[idx].isDefault() && pools[idx].getName().equals(getDefaultFilterPoolName(oldProfileName, getId())))
					pool = pools[idx];
			}
			if (pool == null) // no perfect match?
				pool = mgr.getFirstDefaultSystemFilterPool(); // settle for 2nd best. It may be that the filter was created in a different language.
		}

		return pool;
	}

	/**
	 * Called by SystemRegistry's renameConnection method to ensure we update our
	 *  connection names within each subsystem.
	 * <p>
	 * Must be called prior to changing the connection's name!!
	 */
	public void renameSubSystemsByConnection(IHost conn, String newConnectionName)
	{
		ISubSystem[] subsystems = getSubSystems(conn, ISubSystemConfiguration.FORCE_INTO_MEMORY);
		if (subsystems != null)
		{
			for (int idx = 0; idx < subsystems.length; idx++)
			{
				subsystems[idx].renamingConnection(newConnectionName);
			}
		}
		else
		{
			// strange situation..log this
			SystemBasePlugin.logInfo("renameSubSystemsByConnection for " + conn.getAliasName() + " has no subsystems" ); //$NON-NLS-1$ //$NON-NLS-2$
		}
		try
		{
			saveSubSystems(conn);
		}
		catch (Exception exc)
		{
			// already dealt with in save?
		}
	}
	/**
	 * Called by SystemRegistry's deleteConnection method to ensure we delete all our
	 *  subsystems for a given connection.
	 */
	public void deleteSubSystemsByConnection(IHost conn)
	{
		ISubSystem[] subsystems = conn.getSubSystems();

		//System.out.println("in deleteSubSystemsByConnection. Nbr subsystems = " + subsystems.length);
		for (int idx = 0; idx < subsystems.length; idx++)
		{
			if (subsystems[idx].isConnected())
			{
				try
				{
					subsystems[idx].disconnect(); // be nice if we had a shell to pass!
				}
				catch (Exception exc)
				{
				}
			}
			subsystems[idx].deletingConnection(); // let subsystem do any clean up needed prior to death
			deleteSubSystem(subsystems[idx]);
		}
		invalidateSubSystemCache(conn);
		try
		{
			saveSubSystems(conn);
		}
		catch (Exception exc)
		{
			// already dealt with?
		}
	}

	/**
	 * Invalidate internal cached array of subsystems. Call whenever
	 *  a new subsystem is created, repositioned or deleted.
	 */
	protected void invalidateSubSystemCache(IHost conn)
	{
		subsystems = null;
		if (conn != null)
			subsystemsByConnection.remove(conn); // remove key and value
	}

	/**
	 * Return list of all subsystems.
	 * @param force true if we should force all the subsystems to be restored from disk if not already
	 */
	public ISubSystem[] getSubSystems(boolean force)
	{
		if (force && !allSubSystemsRestored)
		{
			// the safest way is to re-use existing method that will restore for every defined connection
			//  in the active profiles (although if user makes another profile active, we'll have to revisit)
			IHost[] allActiveConnections = RSECorePlugin.getTheSystemRegistry().getHosts();
			if (allActiveConnections != null)
			{
				for (int idx = 0; idx < allActiveConnections.length; idx++)
					if (proxy.appliesToSystemType(allActiveConnections[idx].getSystemType()))
						getSubSystems(allActiveConnections[idx], force); // will load from disk if not already loaded
			}
			allSubSystemsRestored = true;
			subsystems = null; // force re-gen
		}
		// compute cache in local variable in order to avoid modification by other Thread
		ISubSystem[] result = subsystems;
		List subSysList = getSubSystemList();
		synchronized (subSysList) {
			if ((result == null) || (result.length != subSysList.size()))
			{
				// TODO Huh? I do not understand this...
				if (SystemProfileManager.getDefault().getSize() <= 0) // 42913
					return EMPTY_SUBSYSTEM_ARRAY;
				result = (ISubSystem[]) subSysList.toArray(new ISubSystem[subSysList.size()]);
				subsystems = result;
			}
		}
		return result;
	}

	/**
	 * Returns a list of subsystem objects existing for the given connection.
	 * For performance, the calculated array is cached until something changes.
	 * @param conn System connection to retrieve subsystems for
	 * @param force true if we should force all the subsystems to be restored from disk if not already
	 */
	public ISubSystem[] getSubSystems(IHost conn, boolean force)
	{
		ISubSystem[] subsystemArray = (ISubSystem[]) subsystemsByConnection.get(conn);
		if (subsystemArray == null || subsystemArray.length ==0)
		{
			//System.out.println("SubSystemConfigurationImpl.getSubSystems(conn): subSystemsHaveBeenRestored(conn): "+subSystemsHaveBeenRestored(conn));
			boolean subsystemsRestored = subSystemsHaveBeenRestored(conn);
			if (!subsystemsRestored && force)
			{
				SystemBasePlugin.logInfo("in SubSystemConfiguration.getSubSystems(conn, force) - not restored");  //$NON-NLS-1$
				/*FIXME - this should now be triggered by new persistence model
				try
				{
					//System.out.println("SubSystemConfigurationImpl.getSubSystems(conn): before restoreSubSystems");
					subsystemArray = RSEUIPlugin.getThePersistenceManager().restoreSubSystems(this, conn);
					//System.out.println("After restoreSubSystems: "+subsystemArray.length);
					if (subsystemArray != null)
					{
						for (int idx = 0; idx < subsystemArray.length; idx++)
							addSubSystem(subsystemArray[idx]);
						subsystemsByConnection.put(conn, subsystemArray);
					}
				}
				catch (Exception exc)
				{
					RSEUIPlugin.logError("Exception restoring subsystems for connection " + conn, exc);
				}
				*/
			}
			else if (!subsystemsRestored && !force)
			{
				SystemBasePlugin.logInfo("in SubSystemConfiguration.getSubSytems(conn, force) - returning empty array");  //$NON-NLS-1$
				return EMPTY_SUBSYSTEM_ARRAY;
			}
			else
			{
				//System.out.println("...calling internalGet...");
				subsystemArray = internalGetSubSystems(conn);
				//System.out.println("...back from calling internalGet...");
				subsystemsByConnection.put(conn, subsystemArray);
			}
		}
		return subsystemArray;
	}
	/**
	 * Private method to subset master list of all subsystems by a given connection
	 */
	protected ISubSystem[] internalGetSubSystems(IHost conn)
	{
		List subSysList = getSubSystemList();
		synchronized (subSysList) {
			List result = new ArrayList();
			for (Iterator i = subSysList.iterator(); i.hasNext();) {
				ISubSystem subsys = (ISubSystem) i.next();
				// TODO why == and not equals() here?
				if (subsys.getHost() == conn)
					result.add(subsys);
			}
			return (ISubSystem[]) result.toArray(new ISubSystem[result.size()]);
		}
	}
	/**
	 * Returns a list of subsystem objects existing for all the connections in the
	 *  given profile. Will force restoring all subsystems from disk.
	 */
	public ISubSystem[] getSubSystems(ISystemProfile profile)
	{
		ISubSystem[] allSubSystems = getSubSystems(true);
		List l = new ArrayList();
		for (int idx = 0; idx < allSubSystems.length; idx++)
		{
			ISubSystem ss = allSubSystems[idx];
			// TODO why == and not equals() here?
			if (ss.getSystemProfile() == profile)
				l.add(ss);
		}
		ISubSystem[] subsystems = (ISubSystem[]) l.toArray(new ISubSystem[l.size()]);
		return subsystems;
	}
	/**
	 * Returns a list of subsystem objects existing in memory,
	 * which contain a reference to the given filter pool.
	 */
	public ISubSystem[] getSubSystems(ISystemFilterPool pool)
	{
		ISubSystem[] allSubSystems = getSubSystems(false); // // false=> lazy get; don't restore from disk if not already
		List l = new ArrayList();
		for (int idx = 0; idx < allSubSystems.length; idx++)
		{
			ISystemFilterPoolReferenceManager mgr = allSubSystems[idx].getSystemFilterPoolReferenceManager();
			if ((mgr != null) && (mgr.isSystemFilterPoolReferenced(pool)))
			{
				l.add(allSubSystems[idx]);
			}
		}
		ISubSystem[] subsystems = (ISubSystem[]) l.toArray(new ISubSystem[l.size()]);
		return subsystems;
	}

	/**
	 * Helper method to allow child classes to add a subsystem object to the in-memory
	 *  list maintained and returned by this base class.
	 */
	protected void addSubSystem(ISubSystem subsys)
	{
		List subSysList = getSubSystemList();
		synchronized (subSysList) {
			subSysList.add(subsys);
		}
	}

	/**
	 * Helper method to allow child classes to remove a subsystem object from the in-memory
	 *  list maintained and returned by this base class.
	 */
	protected void removeSubSystem(ISubSystem subsys)
	{
		List subSysList = getSubSystemList();
		synchronized (subSysList) {
			subSysList.remove(subsys);
		}
	}

	/**
	 * Creates a new subsystem instance that is associated with the given
	 * connection object. SystemRegistry calls this when a new connection is
	 * created, and appliesToSystemType returns true.
	 * <p>
	 * This method does the following:
	 * <ul>
	 * <li>calls {@link #createSubSystemInternal(IHost)} to create the subsystem
	 * <li>does initialization of common attributes
	 * <li>if {@link #supportsFilters()}, creates a {@link
	 * org.eclipse.rse.core.filters.ISystemFilterPoolReferenceManager} for the
	 * subsystem to manage references to filter pools
	 * <li>if (@link #supportsServerLaunchProperties()}, calls {@link
	 * #createServerLauncher(IConnectorService)}, to create the server launcher
	 * instance to associate with this subsystem.}.
	 * <li>calls {@link #initializeSubSystem(ISubSystem,
	 * ISubSystemConfigurator[])} so subclasses can do their thing to initialize
	 * the subsystem.
	 * <li>finally, saves the subsystem to disk.
	 * </ul>
	 *
	 * @param conn The connection to create a subsystem for
	 * @param creatingConnection true if we are creating a connection, false if
	 * 		just creating another subsystem for an existing connection.
	 * @param configurators configurators that inject properties into this new
	 * 		subsystem or null if there are none. Used to take
	 * 		ISystemNewConnectionWizardPage[] before RSE 3.0.
	 * @return the created subsystem or null if none has been created.
	 * @since 3.0
	 */
	public ISubSystem createSubSystem(IHost conn, boolean creatingConnection, ISubSystemConfigurator[] configurators)
	{
		invalidateSubSystemCache(conn); // re-gen list of subsystems-by-connection on next call
		if (creatingConnection)
		{
			if (subSystemsRestoredFlags == null)
				reset();
			subSystemsRestoredFlags.put(conn, Boolean.TRUE); // do not try to restore subsequently. Nothing to restore!
		}
		ISubSystem subsys = null;
		try {
			subsys = createSubSystemInternal(conn);
		} catch (RuntimeException e) {
			RSECorePlugin.getDefault().getLogger().logError("Error creating subsystem", e); //$NON-NLS-1$
		}
		if (subsys != null)
		{
			internalInitializeNewSubSystem(subsys, conn);
			if (supportsFilters())
			{
				// We create a filter pool reference manager object to manage the filter pool references
				// that are stored with a subsystem.
				//SystemFilterPoolManager[] relatedFilterPoolManagers =
				//  getReferencableFilterPoolManagers(conn.getSystemProfile());
				ISystemFilterPoolReferenceManager fprMgr = SystemFilterStartHere.getInstance().createSystemFilterPoolReferenceManager(subsys, this, subsys.getName());
				subsys.setFilterPoolReferenceManager(fprMgr);
				ISystemFilterPoolManager defaultFilterPoolManager = getFilterPoolManager(conn.getSystemProfile());
				fprMgr.setDefaultSystemFilterPoolManager(defaultFilterPoolManager);
			}

			IConnectorService connectorService = subsys.getConnectorService();
			if (supportsServerLaunchProperties(conn))
			{
				IServerLauncherProperties sl = connectorService.getRemoteServerLauncherProperties();
				if (sl == null)
				{
					sl = createServerLauncher(connectorService);
					if (sl != null)
					{
						connectorService.setRemoteServerLauncherProperties(sl);
					}
				}
			}
			initializeSubSystem(subsys, configurators);
			try
			{
				saveSubSystem(subsys);
				RSECorePlugin.getTheSystemRegistry().fireModelChangeEvent(ISystemModelChangeEvents.SYSTEM_RESOURCE_ADDED, ISystemModelChangeEvents.SYSTEM_RESOURCETYPE_SUBSYSTEM, subsys, null);
			}
			catch (Exception exc)
			{
				SystemBasePlugin.logError("Error saving new subsystem " + subsys.getName(), exc); //$NON-NLS-1$
			}

			addSubSystem(subsys); // only add to list even if save was not successful.
			//if (lastExc != null)
			//throw lastExc;
		}
		return subsys;
	}

	/**
	 * Clone a given subsystem into the given connection.
	 * Called when user does a copy-connection action.
	 * @param oldSubsystem The subsystem to be cloned
	 * @param newConnection The connection into which to create and clone the old subsystem
	 * @param copyProfileOperation Pass true if this is an profile-copy operation versus a connection-copy operation
	 * @return New subsystem within the new connection
	 */
	public ISubSystem cloneSubSystem(ISubSystem oldSubsystem, IHost newConnection, boolean copyProfileOperation) throws Exception
	{
		Exception lastExc = null;
		invalidateSubSystemCache(newConnection); // re-gen list of subsystems-by-connection on next call
		subSystemsRestoredFlags.put(newConnection, Boolean.TRUE); // do not try to restore subsequently. Nothing to restore!
		ISubSystem subsys = createSubSystemInternal(newConnection);
		if (subsys != null)
		{
			internalInitializeNewSubSystem(subsys, newConnection);
			// copy common data
			subsys.setName(oldSubsystem.getName()); // just in case it was changed

			oldSubsystem.clonePropertySets(subsys);			

			subsys.setHidden(oldSubsystem.isHidden());


			// connector service
			IConnectorService oldConnectorService = oldSubsystem.getConnectorService();
			IConnectorService newConnectorService = subsys.getConnectorService();
			if (oldConnectorService != null)
			{
				if (newConnectorService != null)
				{
					newConnectorService.setPort(oldConnectorService.getPort());
					newConnectorService.setUserId(oldConnectorService.getUserId());
					newConnectorService.setIsUsingSSL(oldConnectorService.isUsingSSL());
				}
			}

			// server launcher
			IServerLauncherProperties sl = null;
			if (oldConnectorService != null)
				sl = oldConnectorService.getRemoteServerLauncherProperties();
			if ((sl != null) && supportsServerLaunchProperties(newConnection))
			{
				IServerLauncherProperties newSL = createServerLauncher(newConnectorService);
				if (newSL != null && newConnectorService != null)
				{
					newConnectorService.setRemoteServerLauncherProperties(sl.cloneServerLauncher(newSL));
				}
			}
			copySubSystemData(oldSubsystem, subsys); // let child classes copy their own data
			if (supportsFilters())
			{
				// We create a filter pool reference manager object to manage the filter pool references
				// that are stored with a subsystem.
				//SystemFilterPoolManager[] relatedFilterPoolManagers =
				//  getReferencableFilterPoolManagers(newConnection.getSystemProfile());
				ISystemFilterPoolReferenceManager newRefMgr = SystemFilterStartHere.getInstance().createSystemFilterPoolReferenceManager(subsys, this, subsys.getName());
				ISystemFilterPoolManager defaultFilterPoolManager = null;
				if (copyProfileOperation)
					defaultFilterPoolManager = getFilterPoolManager(newConnection.getSystemProfile());
				else
					defaultFilterPoolManager = oldSubsystem.getFilterPoolReferenceManager().getDefaultSystemFilterPoolManager();
				newRefMgr.setDefaultSystemFilterPoolManager(defaultFilterPoolManager);
				subsys.setFilterPoolReferenceManager(newRefMgr);
				// copy filter pool references...
				ISystemFilterPoolReferenceManager oldRefMgr = oldSubsystem.getSystemFilterPoolReferenceManager();
				newRefMgr.setProviderEventNotification(false);
				ISystemFilterPoolReference[] oldReferences = oldRefMgr.getSystemFilterPoolReferences();
				String oldSubSystemProfileName = oldSubsystem.getSystemProfileName();
				if ((oldReferences != null) && (oldReferences.length > 0))
				{
					for (int idx = 0; idx < oldReferences.length; idx++)
					{
						ISystemFilterPoolReference poolRef = oldReferences[idx];
						ISystemFilterPool pool = poolRef.getReferencedFilterPool();
						if (pool != null) {
							// if just copying a connnection, then copy references to pools as-is
							if (!copyProfileOperation)
							{
								newRefMgr.addReferenceToSystemFilterPool(pool);
							}
							// if copying a profile, update references to pools in old profile to become references to pools in new profile...
							else
							{
								ISystemFilterPoolManager poolMgr = pool.getSystemFilterPoolManager();
								String poolProfileName = getSystemProfileName(poolMgr);
								if (poolProfileName.equals(oldSubSystemProfileName))
								{
									//RSEUIPlugin.logDebugMessage(this.getClass().getName(),"found reference to copied filter pool " + pool.getName() + ", so changing to reference to new copy");
									ISystemFilterPoolManager newPoolMgr = getFilterPoolManager(newConnection.getSystemProfile());
									ISystemFilterPool newPool = newPoolMgr.getSystemFilterPool(pool.getName());
									//RSEUIPlugin.logDebugMessage(this.getClass().getName(),"...new pool = " + newPoolMgr.getName()+"."+newPool.getName());
									newRefMgr.addReferenceToSystemFilterPool(newPool);
								}
								else
								{
									//RSEUIPlugin.logDebugMessage(this.getClass().getName(),"found reference to filter pool from another profile " + poolProfileName+"."+pool.getName() + ", so not changing to reference to new copy");
									newRefMgr.addReferenceToSystemFilterPool(pool);
								}
							}
						}
					}
				}
				newRefMgr.setProviderEventNotification(true);
			}
			try
			{
				saveSubSystem(subsys);

				// fire model change event in case any BP code is listening...
				RSECorePlugin.getTheSystemRegistry().fireModelChangeEvent(ISystemModelChangeEvents.SYSTEM_RESOURCE_ADDED, ISystemModelChangeEvents.SYSTEM_RESOURCETYPE_SUBSYSTEM, subsys, null);
			}
			catch (Exception exc)
			{
				lastExc = exc;
				SystemBasePlugin.logError("Error saving cloned subsystem " + subsys.getName(), exc); //$NON-NLS-1$
			}
			addSubSystem(subsys); // only add to list even if save was not successful.
			if (lastExc != null)
				throw lastExc;
		}
		return subsys;
	}

	/**
	 * Called after successfully creating a new subsystem via createSubSystemInternal when creating a new connection, or
	 *  when cloning a subsystem
	 * @param ss The new subsystem
	 * @param conn The new connection containing this new subsystem
	 */
	private void internalInitializeNewSubSystem(ISubSystem subsys, IHost conn)
	{
		subsys.setSubSystemConfiguration(this);
		subsys.setHost(conn);
		subsys.setName(internalGetSubSystemName(subsys));
		subsys.setConfigurationId(getId());
	}

	/**
	 * Overridable.
	 * <p>
	 * Return the name to give a new subsystem. By default, it is given the name of this
	 *  subsystem configuration object. This is fine, unless you support multiple subsystem instances per
	 *  connection, in which case it is your responsibility to supply a unique name for
	 *  each.
	 * <p>
	 * By default returns getName()
	 */
	protected String internalGetSubSystemName(ISubSystem subsys)
	{
		return getName();
	}

	/**
	 * Method called by default implementation of createSubSystem method in AbstractSubSystemConfiguration.
	 */
	public abstract ISubSystem createSubSystemInternal(IHost conn);

	/**
	 * Initialize subsystems after creation (<i>Overridable</i>). The default
	 * behavior is to add a reference to the default filter pool for this
	 * subsystem configuration, if there is one. Typically subclasses call
	 * <samp>super().initializeSubSystem(...)</samp> to get this default
	 * behavior, then extend it.
	 *
	 * @param ss - The subsystem that was created via createSubSystemInternal
	 * @param configurators an array of {@link ISubSystemConfigurator} used to
	 * 		inject values into this subsystem or null if there are none. Used to
	 * 		take ISystemNewConnectionWizardPage[] before RSE 3.0
	 * @since 3.0
	 */
	protected void initializeSubSystem(ISubSystem ss, ISubSystemConfigurator[] configurators) {
		if (supportsFilters()) {
			// --------------------------------------------
			// add a reference to the default filter pool
			// --------------------------------------------
			ISystemFilterPool pool = getDefaultSystemFilterPool(ss);
			if (pool != null) {
				ISystemFilterPoolReferenceManager refMgr = ss.getSystemFilterPoolReferenceManager();
				refMgr.setProviderEventNotification(false);
				refMgr.addReferenceToSystemFilterPool(pool);
				refMgr.setProviderEventNotification(true);
			}
		}

		// apply properties from the configurators to the subsystem
		if (configurators != null) {
			ISubSystemConfigurator ourPage = null;
			for (int idx = 0; (ourPage == null) && (idx < configurators.length); idx++) {
				ourPage = configurators[idx];
				ourPage.applyValues(ss);
			}
		}
	}

	/**
	 * Copy unique subsystem data after a copy operation. Subclasses should override and call super.
	 */
	protected void copySubSystemData(ISubSystem oldSubSystem, ISubSystem newSubSystem)
	{
		return;
	}

	/**
	 * Updates userid and/or port of an existing subsystem instance.
	 * These attributes typically affect the live connection, so the subsystem will be forced to
	 * disconnect.
	 * <p>
	 * If you have your own attributes and own GUI to prompt for these, then call your own
	 * method to set your attributes, and call this method via super().
	 * <p>
	 * The changes to the subsystem configuration will be saved to disk.
	 * Further, it will be asked to disconnect as this data affects the connection.
	 * <p>
	 * @param subsystem target of the update action
	 * @param updateUserId true if we are updating the userId, else false to ignore userId
	 * @param userId new local user Id. Ignored if updateUserId is false
	 * @param updatePort true if we are updating the port, else false to ignore port
	 * @param port new local port value. Ignored if updatePort is false
	 */
	public void updateSubSystem(ISubSystem subsystem, boolean updateUserId, String userId, boolean updatePort, int port)
	{
		// we pushed down the code that checks if this change is real, such that
		//  in a multi-subsystem environment it will not enter an infinite loop
		//  when the event is fired.
		if (!needsUpdate(subsystem, updateUserId, userId, updatePort, port)) // port was changed or userId was changed
			return;

		IConnectorService connectorService = subsystem.getConnectorService();
		if (connectorService != null)
		{
			// do the actual update
			if (updateUserId)
			{
				if ((userId != null) && (userId.trim().length() > 0))
				{
					connectorService.setUserId(userId);
				}
				else
				{
					connectorService.setUserId(null);
				}
			}
			if (updatePort)
			{
				connectorService.setPort(port);
			}
			if (connectorService.isDirty()) {
				setDirty(true);
				subsystem.setDirty(true);
			}
		}
		else
		{

		}

		// inform interested listeners...
		fireEvent(subsystem, ISystemResourceChangeEvents.EVENT_PROPERTY_CHANGE, subsystem.getHost());

		// fire model change event in case any BP code is listening...
		RSECorePlugin.getTheSystemRegistry().fireModelChangeEvent(ISystemModelChangeEvents.SYSTEM_RESOURCE_CHANGED, ISystemModelChangeEvents.SYSTEM_RESOURCETYPE_SUBSYSTEM, subsystem, null);

		// if the updated subsystem is one of many that share a single IConnectorService, then
		// update all of them too...
		// DKM - now that ConnectorService is independent of subsystme, this should be unnecessary
	/*	AbstractConnectorServiceManager systemManager = subsystem.getConnectorService();
		if (systemManager != null)
			systemManager.updateSubSystems(shell, subsystem, updateUserId, userId, updatePort, port);*/
	}
	/**
	 * Update the port for the given subsystem instance.
	 * Shortcut to {@link #updateSubSystem(ISubSystem, boolean, String, boolean, int)}
	 */
	public void setSubSystemPort(ISubSystem subsystem, int port)
	{
		updateSubSystem( subsystem, false, null, true, port);
	}
	/**
	 * Update the user ID for the given subsystem instance.
	 * Shortcut to {@link #updateSubSystem(ISubSystem, boolean, String, boolean, int)}
	 */
	public void setSubSystemUserId(ISubSystem subsystem, String userId)
	{
		updateSubSystem(subsystem, true, userId, false, 0);
	}

	/**
	 * Used by child classes that override updateSubSystem to establish if anything really
	 * needs to be changed.
	 */
	protected boolean needsUpdate(ISubSystem subsystem, boolean updateUserId, String userId, boolean updatePort, int port)
	{
		IConnectorService connectorService = subsystem.getConnectorService();
		if (connectorService != null)
		{
			if (updatePort) // we pass this parameter for a reason!
			{

				int oldPort = connectorService.getPort();
				updatePort = oldPort != port;
			}
			if (!updatePort && updateUserId)
			{
				if ((userId == null) || (userId.trim().length() == 0)) // given empty
				{

					updateUserId = (connectorService.getUserId() != null);
				}
				else
				{

					String oldUserId = connectorService.getUserId();
					if (oldUserId != null)
					{ // if it is null, then we need to update it!
						// DY defect 43374
						if (subsystem.forceUserIdToUpperCase())
							updateUserId = !userId.equalsIgnoreCase(oldUserId);
						else
							updateUserId = !userId.equals(oldUserId);
					}
				}
			}
		}
		return updatePort || updateUserId;
	}

	/**
	 * Returns true if this subsystem configuration allows users to delete instances of subsystem objects.
	 * Would only be true if users are allowed to create multiple instances of subsystem objects
	 *  per connection.
	 * Returns false by default. Override this and deleteSubSystem(SubSystem subsystem) to
	 *  support user deletes
	 */
	public boolean isSubSystemsDeletable()
	{
		return false;
	}

	/**
	 * Deletes a given subsystem instance from the list maintained by this subsystem configuration.
	 * SystemRegistryImpl calls this when the user selects to delete a subsystem object,
	 *  or deletes the parent connection this subsystem is associated with.
	 * <p>
	 * In former case, this is only called if the subsystem configuration supports user-deletable subsystems.
	 * <p>
	 * Handled for you!
	 */
	public boolean deleteSubSystem(ISubSystem subsystem)
	{
		if (subsystem.isConnected())
		{
			try
			{
				subsystem.disconnect(); // just in case.
			}
			catch (Exception exc)
			{
			}
		}
		removeSubSystem(subsystem); // remove from our in-memory cache
		ISystemFilterPoolReferenceManager fpRefMgr = subsystem.getSystemFilterPoolReferenceManager();
		if (fpRefMgr != null)
		{
			ISystemFilterPoolReference[] fpRefs = fpRefMgr.getSystemFilterPoolReferences();
			if (fpRefs != null)
			{
				for (int idx = 0; idx < fpRefs.length; idx++)
					fpRefs[idx].removeReference();
			}
		}

		// fire model change event in case any BP code is listening...
		RSECorePlugin.getTheSystemRegistry().fireModelChangeEvent(ISystemModelChangeEvents.SYSTEM_RESOURCE_REMOVED, ISystemModelChangeEvents.SYSTEM_RESOURCETYPE_SUBSYSTEM, subsystem, null);
		return true;
	}

	/**
	 * Renames a subsystem. This is better than ss.setName(String newName) as it saves the subsystem to disk.
	 */
	public void renameSubSystem(ISubSystem subsystem, String newName)
	{
		subsystem.setName(newName);
		try
		{
			subsystem.commit();
		}
		catch (Exception exc)
		{
		}
	}

	/**
	 * Overridable entry for child classes to supply their own flavor of
	 * ISystemFilterPoolWrapperInformation for the new filter wizards.
	 *
	 * @return an ISystemFilterPoolWrapperInformation instead of a
	 * 	SystemFilterPoolWrapperInformation since 3.0
	 * @since 3.0
	 */
	protected ISystemFilterPoolWrapperInformation getNewFilterWizardPoolWrapperInformation()
	{
		return new SystemFilterPoolWrapperInformation(SystemResources.RESID_NEWFILTER_PAGE2_PROFILE_LABEL, SystemResources.RESID_NEWFILTER_PAGE2_PROFILE_TOOLTIP,
				SystemResources.RESID_NEWFILTER_PAGE2_PROFILE_VERBIAGE);
	}

	/**
	 * Disconnect all subsystems currently connected.
	 * Called by shutdown() of RSEUIPlugin.
	 */
	public void disconnectAllSubSystems() throws Exception
	{
		ISubSystem[] subsystems = getSubSystems(false); //// false=> lazy get; don't restore from disk if not already
		if (subsystems != null)
		{
			//System.out.println("DISCONNECT ALL FOR "+getClass().getName()+", #subsystems = " + subsystems.length);
			for (int idx = 0; idx < subsystems.length; idx++)
			{
				ISubSystem ss = subsystems[idx];
				if (ss.isConnected())
				{
					try
					{
						ss.getConnectorService().disconnect(new NullProgressMonitor());
					}
					catch (Exception exc)
					{
					}
				}
			}
		}
		else
		{
			//System.out.println("DISCONNECT ALL FOR "+getClass().getName()+", #subsystems = 0");
		}
	}



	// ---------------------------------
	// FILTER FRAMEWORK METHODS...
	// ---------------------------------

	/**
	 * <i>Overridable lifecycle method. Typically overridden to supply a default filter.</i><br>
	 * When the user creates a new profile in the RSE (which is mapped to a SystemFilterPoolManager
	 * by our parent class), each subsystem configuration that supports filters is asked if it wants to
	 * create a default system filter pool in that profile. <br>
	 * This is the method that is called to do that default filter pool creation in the new profile.
	 * <p>
	 * By default we create an <i>empty</i> filter pool with a generated name, and no pre-defined filters.
	 * If you don't want that behaviour, override this method and do one of the following:</p>
	 * <ul>
	 * <li>nothing if you don't want your subsystem configuration to have a default filter pool in the new profile</li>.
	 * <li>call super.createDefaultFilterPool(mgr) to get the default pool, and then than call <samp>mgr.createSystemFilter(pool,...)</samp> to create
	 * each filter and add it to the filter pool, if you want to pre-populate the default pool with
	 * default filters.
	 * </ul>
	 */
	protected ISystemFilterPool createDefaultFilterPool(ISystemFilterPoolManager mgr)
	{
		ISystemFilterPool pool = null;
		try {
		  // -----------------------------------------------------
		  // create a pool named filters
		  // -----------------------------------------------------
		  pool = mgr.createSystemFilterPool(getDefaultFilterPoolName(mgr.getName(), getId()), true); // true=>is deletable by user
		} catch (Exception exc)
		{
			SystemBasePlugin.logError("Error creating default filter pool in default subsystem configuration",exc); //$NON-NLS-1$
		}
		return pool;
	}



	/**
	 * Return true if the given filter pool manager maps to the private profile for this user.
	 */
	protected boolean isUserPrivateProfile(ISystemFilterPoolManager mgr)
	{
		ISystemProfile profile = mgr.getSystemProfile();
		boolean result = profile.isDefaultPrivate() || mgr.getName().equalsIgnoreCase("private"); //$NON-NLS-1$
		return result;
	}

	/**
	 * Given a subsystem, return the first (hopefully only) default pool for this
	 * subsystem's profile.
	 */
	public ISystemFilterPool getDefaultSystemFilterPool(ISubSystem subsys)
	{
		ISystemFilterPool pool = getDefaultSystemFilterPool(subsys.getSystemProfile());
		/* hopefully this is no longer needed, now that we are into our 2nd release!
		if (pool == null)
		{
			// temporary for pre-default users
			String defaultPoolName = "Filters";
			if (subsys.getName().equals("Local Files"))
			  defaultPoolName = "DefaultFilterPool";
			pool = mgr.getSystemFilterPool(defaultPoolName);
		}
		*/
		//RSEUIPlugin.logDebugMessage(this.getClass().getName(), "...inside getDefaultFilterPool for "+subsys.getName()+", default pool="+pool);
		return pool;
	}
	/**
	 * Given a profile, return the first (hopefully only) default pool for this
	 * profile.
	 */
	public ISystemFilterPool getDefaultSystemFilterPool(ISystemProfile profile)
	{
		ISystemFilterPool pool = null;
		ISystemFilterPoolManager mgr = getFilterPoolManager(profile);
		pool = mgr.getFirstDefaultSystemFilterPool(); // RETURN FIRST
		return pool;
	}
	/**
	 * Invalidate any internal caches related to filters
	 */
	public void invalidateFilterCache()
	{
		filterPoolManagers = null;
	}
	/**
	 * Return an array of all filter pool managers owned by this subsystem configuration.
	 * This is a runtime array that only captures those filter pools that have been restored
	 *  as a result of someone calling getFilterPoolManager(SystemProfile).
	 */
	public ISystemFilterPoolManager[] getFilterPoolManagers()
	{
		List fpManagers = getFilterPoolManagerList();
		synchronized (fpManagers) {
			if ((filterPoolManagers == null) || (filterPoolManagers.length != fpManagers.size())) {
				filterPoolManagers = (ISystemFilterPoolManager[]) fpManagers.toArray(new ISystemFilterPoolManager[fpManagers.size()]);
			}
		}
		return filterPoolManagers;
	}

	/**
	 * Get the filter pool managers for the active profiles.
	 */
	public ISystemFilterPoolManager[] getActiveFilterPoolManagers()
	{
		ISystemProfile[] activeProfiles = RSECorePlugin.getTheSystemRegistry().getActiveSystemProfiles();
		ISystemFilterPoolManager[] activeManagers = new ISystemFilterPoolManager[activeProfiles.length];
		for (int idx = 0; idx < activeProfiles.length; idx++)
		{
			activeManagers[idx] = getFilterPoolManager(activeProfiles[idx]);
		}
		return activeManagers;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.ISubSystemConfiguration#getFilterPoolManager(org.eclipse.rse.core.model.ISystemProfile)
	 */
	public ISystemFilterPoolManager getFilterPoolManager(ISystemProfile profile) {
		return getFilterPoolManager(profile, false);
	}

	/**
	 * {@inheritDoc}
	 * @since 3.0 added boolean argument
	 */
	public ISystemFilterPoolManager getFilterPoolManager(ISystemProfile profile, boolean force) {
		// it is important to key by profile object not profile name, since that name can change but the object never should for any one session.
		ISystemFilterPoolManager mgr = (ISystemFilterPoolManager) filterPoolManagersPerProfile.get(profile);
		if (mgr == null) {
			try {
				Logger logger = RSECorePlugin.getDefault().getLogger();
				String managerName = getFilterPoolManagerName(profile);
				boolean supportsNested = supportsNestedFilters();
				mgr = SystemFilterPoolManager.createSystemFilterPoolManager(profile, logger, this, managerName, supportsNested);// the filter pool manager name
				mgr.setSingleFilterStringOnly(!supportsMultipleFilterStrings());
				mgr.setWasRestored(false); // not yet initialized
			} catch (Exception exc) {
				SystemBasePlugin.logError("Restore/Creation of SystemFilterPoolManager " + getFilterPoolManagerName(profile) + " failed!", exc); //$NON-NLS-1$ //$NON-NLS-2$
				SystemMessageDialog.displayExceptionMessage(null, exc);
				return null; // something very bad happened!
			}
			addFilterPoolManager(profile, mgr);
		}
		boolean initialized = mgr.wasRestored();
		if (force && !initialized) {
			String defaultPoolName = getDefaultFilterPoolName(profile.getName(), getId());
			ISystemFilterPool defaultPool = mgr.getSystemFilterPool(defaultPoolName);
			if (defaultPool == null) {
				// allow subclasses to create default filter pool...
				defaultPool = createDefaultFilterPool(mgr); // createDefaultFilterPool(mgr) is typically overridden by subclasses
			}
			if (defaultPool != null) {
				defaultPool.setDefault(true);
				defaultPool.commit();
			}
			if (supportsDuplicateFilterStrings()) mgr.setSupportsDuplicateFilterStrings(true);
			if (isCaseSensitive()) mgr.setStringsCaseSensitive(true);
			mgr.setWasRestored(true);
		}
		return mgr;
	}

	/**
	 * Do post-restore-processing of an existing filter pool manager.
	 * This is where child classes do any required migration work. By default, we do nothing.
	 * <p>
	 * You can query the release of the filter pool managers, filter pools and filters, by querying the
	 * release attribute via getRelease().getValue(). You can compare to the current release number using
	 * the CURRENT_RELEASE constant in ISystemConstants.
	 * @return false if no changes made. True if changes made, and hence save required.
	 */
	protected boolean doPostRestoreProcessing(ISystemFilterPoolManager restoredFilterPoolMgr)
	{
		return false;
	}

	/**
	 * Copy the filter pool manager and return a new one. Called during profile-copy operations.
	 * Will also copy all of the filter pools and their respective data.
	 */
	public ISystemFilterPoolManager copyFilterPoolManager(ISystemProfile oldProfile, ISystemProfile newProfile) throws Exception
	{

		ISystemFilterPoolManager oldMgr = getFilterPoolManager(oldProfile); // will restore it if necessary

			ISystemFilterPoolManager mgr = SystemFilterPoolManager.createSystemFilterPoolManager(newProfile, RSECorePlugin.getDefault().getLogger(), this, getFilterPoolManagerName(newProfile), supportsNestedFilters());
		mgr.setStringsCaseSensitive(oldMgr.areStringsCaseSensitive());
		mgr.setSupportsDuplicateFilterStrings(oldMgr.supportsDuplicateFilterStrings());
		addFilterPoolManager(newProfile, mgr);
		oldMgr.copySystemFilterPools(mgr);
		return mgr;
	}

	/**
	 * Get the filter pool manager for the given profile.
	 * REQUIRED BY SYSTEMFILTERPOOLMANAGERPROVIDER INTERFACE
	 */
	public ISystemFilterPoolManager getSystemFilterPoolManager(String mgrName)
	{
		ISystemProfile profile = getSystemProfile(mgrName);
		if (profile != null)
		{
			return getFilterPoolManager(profile);
		}
		return null;
	}

	/**
	 * Get the filter pool manager for the given profile.
	 * REQUIRED BY SYSTEMFILTERPOOLMANAGERPROVIDER INTERFACE
	 */
	public ISystemFilterPoolManager[] getSystemFilterPoolManagers()
	{
		return getActiveFilterPoolManagers();
	}
	/**
	 * Return all the manager objects this provider owns, to which it wants
	 *  to support referencing from the given filter reference manager.
	 * <p>
	 * Called by SystemFilterPoolReferenceManager.
	 * <p>
	 * REQUIRED BY SYSTEMFILTERPOOLMANAGERPROVIDER INTERFACE
	 */
	public ISystemFilterPoolManager[] getReferencableSystemFilterPoolManagers(ISystemFilterPoolReferenceManager refMgr)
	{
		return getActiveFilterPoolManagers();
	}

	/**
	 * Last chance call, by a filter pool reference manager, when a reference to a filter
	 * pool is found but the referenced master filter pool is not found in those the reference
	 * manager by getSystemFilterPoolManagers().
	 * <p>
	 * If this returns null, then this broken reference will be deleted
	 * <p>
	 * REQUIRED BY SYSTEMFILTERPOOLMANAGERPROVIDER INTERFACE
	 */
	public ISystemFilterPool getSystemFilterPoolForBrokenReference(ISystemFilterPoolReferenceManager callingRefMgr, String missingPoolMgrName, String missingPoolName)
	{
		ISystemFilterPool match = null;
		ISystemRegistry sr = RSECorePlugin.getTheSystemRegistry();
		ISystemProfile profile = sr.getSystemProfile(missingPoolMgrName);
		if (profile != null)
		{
			match = getFilterPoolManager(profile).getSystemFilterPool(missingPoolName);
		}
		if (match != null) // log and issue warning
		{
			ISubSystem ss = ((ISubSystem) callingRefMgr.getProvider());
			// only issue the warning if it is NOT for a reference to a filter pool in the same profile as the
			// the one we are restoring. That is, we should not issue warnings about our own references when
			// restoring the subsystems for an inactive profile, as happens when an inactive profile is deleted,
			// for example. Defect 42675. Phil.
			if (ss.getSystemProfile() != profile) // if restoring subsystem's profile != found pool's profile
			{
				IHost conn = ss.getHost();
				String connectionName = conn.getSystemProfileName() + "." + conn.getAliasName(); //$NON-NLS-1$

				String msgTxt = NLS.bind(RSECoreMessages.MSG_LOADING_PROFILE_SHOULDBE_ACTIVATED, missingPoolMgrName, connectionName);
				SystemMessage sysMsg = new SimpleSystemMessage(RSECorePlugin.PLUGIN_ID,
						SystemResourceConstants.MSG_LOADING_PROFILE_SHOULDBE_ACTIVATED,
						IStatus.ERROR, msgTxt);
				SystemBasePlugin.logWarning(sysMsg.getFullMessageID() + ": " + sysMsg.getLevelOneText()); //$NON-NLS-1$
				if (brokenReferenceWarningsIssued.get(missingPoolMgrName) == null)
				{
					SystemMessageDialog msgDlg = new SystemMessageDialog(null, sysMsg);
					msgDlg.open();
					brokenReferenceWarningsIssued.put(missingPoolMgrName, Boolean.TRUE); // only issue once per inactive profile
				}
			}
		}
		return match;
	}


	/**
	 * Add the given filter pool manager object to internal lists
	 */
	protected void addFilterPoolManager(ISystemProfile profile, ISystemFilterPoolManager mgr)
	{
		List fpManagers = getFilterPoolManagerList();
		synchronized (fpManagers) {
			filterPoolManagersPerProfile.put(profile, mgr);
			fpManagers.add(mgr);
		}
		invalidateFilterCache(); // force regen of any cached lists
	}
	/**
	 * Get the filter pool manager for the given filter pool
	 */
	protected ISystemFilterPoolManager getFilterPoolManager(ISystemFilterPool pool)
	{
		return pool.getSystemFilterPoolManager();
	}
	/**
	 * Get the filter pool manager for the given filter pool or filter
	 */
	protected ISystemFilterPoolManager getFilterPoolManager(ISystemFilterContainer poolOrFilter)
	{
		return poolOrFilter.getSystemFilterPoolManager();
	}
	/**
	 * Get the filter pool manager for the given reference to a filter pool or filter
	 */
	protected ISystemFilterPoolManager getFilterPoolManager(ISystemFilterContainerReference poolOrFilterReference)
	{
		return getFilterPoolManager(poolOrFilterReference.getReferencedSystemFilterContainer());
	}
	/**
	 * Get the filter pool manager for the given connection
	 */
	protected ISystemFilterPoolManager getFilterPoolManager(IHost conn)
	{
		return getFilterPoolManager(conn.getSystemProfile());
	}

	/**
	 * Return the name of the filter pool manager, given the profile.
	 */
	protected String getFilterPoolManagerName(ISystemProfile profile)
	{
		return profile.getName();
	}
	/**
	 * Return the name of the profile, given the filter pool manager
	 */
	protected String getSystemProfileName(ISystemFilterPoolManager manager)
	{
		return manager.getName();
	}
	/**
	 * Delete the filter pool manager associated with the given profile
	 */
	protected void deleteFilterPoolManager(ISystemProfile profile)
	{
		ISystemFilterPoolManager mgr = (ISystemFilterPoolManager) filterPoolManagersPerProfile.get(profile);
		if (mgr != null)
		{
			mgr.deleteAllSystemFilterPools(); // blow 'em all away, and de-reference anybody referencing any of them
			List fpManagers = getFilterPoolManagerList();
			synchronized (fpManagers) {
				filterPoolManagersPerProfile.remove(profile);
				fpManagers.remove(mgr);
			}
			invalidateFilterCache();
		}
	}
	/**
	 * Rename the filter pool manager associated with the given profile
	 */
	public void renameFilterPoolManager(ISystemProfile profile)
	{
		ISystemFilterPoolManager mgr = (ISystemFilterPoolManager) filterPoolManagersPerProfile.get(profile);
		if (mgr != null)
		{
			mgr.setName(getFilterPoolManagerName(profile));
		}
	}

	// ------------------------------------------------
	// HELPER METHODS TO SIMPLY EVENT FIRING...
	// ------------------------------------------------

	/**
	 * Helper method to fire an event...
	 */
	protected void fireEvent(SystemResourceChangeEvent event)
	{
		RSECorePlugin.getTheSystemRegistry().fireEvent(event);
	}
	/**
	 * Helper method to create and then fire an event...
	 */
	protected void fireEvent(Object src, int eventId, Object parent)
	{
		fireEvent(createEvent(src, eventId, parent));
	}
	/**
	 * Helper method to create and then fire an event with a position or delta...
	 */
	protected void fireEvent(Object[] multiSrc, int eventId, Object parent, int delta)
	{
		fireEvent(createEvent(multiSrc, eventId, parent, delta));
	}

	/**
	 * Helper method to create a single-source event
	 */
	protected SystemResourceChangeEvent createEvent(Object src, int eventId, Object parent)
	{
		return new SystemResourceChangeEvent(src, eventId, parent);
	}
	/**
	 * Helper method to create a multi-source event
	 */
	protected SystemResourceChangeEvent createEvent(Object[] src, int eventId, Object parent)
	{
		return new SystemResourceChangeEvent(src, eventId, parent);
	}
	/**
	 * Helper method to create a multi-source event
	 */
	protected SystemResourceChangeEvent createEvent(Object[] src, int eventId, Object parent, int delta)
	{
		SystemResourceChangeEvent event = new SystemResourceChangeEvent(src, eventId, parent);
		event.setPosition(delta);
		return event;
	}

	/**
	 * Fire given event to the given subsystem
	 */
	protected void fireSubSystemEvent(SystemResourceChangeEvent event, ISubSystem subsystem)
	{
		event.setGrandParent(subsystem);
		fireEvent(event);
	}

	/**
	 * Fire given event to all currently known subsystems
	 */
	protected void fireSubSystemEvent(SystemResourceChangeEvent event)
	{
		ISubSystem[] subsystems = getSubSystems(false); // false=> lazy get; don't restore from disk if not already
		for (int idx = 0; idx < subsystems.length; idx++)
			fireSubSystemEvent(event, subsystems[idx]);
	}

	/**
	 * Fire given event to all subsystems in the given profile
	 */
	protected void fireSubSystemEvent(SystemResourceChangeEvent event, ISystemProfile profile)
	{
		String profileName = profile.getName();
		ISubSystem[] allSubSystems = getSubSystems(false); // false=> lazy get; don't restore from disk if not already
		for (int idx = 0; idx < allSubSystems.length; idx++)
		{
			if (allSubSystems[idx].getSystemProfile().getName().equals(profileName))
				fireSubSystemEvent(event, allSubSystems[idx]);
		}
	}
	/**
	 * Fire an event of a given id to subsystems that hold a reference to the given filter
	 */
	protected void fireSubSystemFilterEvent(int eventId, ISystemFilter filter)
	{
		SystemResourceChangeEvent event = createEvent(filter, eventId, null);
		fireSubSystemFilterEvent(event, filter);
	}

	/**
	 * Fire an event of a given id to subsystems that hold a reference to the given filter
	 */
	protected void fireSubSystemFilterEvent(int eventId, ISystemFilter[] filters)
	{
		if (filters.length > 0)
		{
			SystemResourceChangeEvent event = createEvent(filters, eventId, null);
			fireSubSystemFilterEvent(event, filters[0]);
		}
	}
	/**
	 * Fire an event of a given id to subsystems that hold a reference to the given filter
	 */
	protected void fireSubSystemFilterEvent(SystemResourceChangeEvent event, ISystemFilter filter)
	{
		// STEP 1: FIND ALL SUBSYSTEMS THAT CONTAIN A REFERENCE TO THIS FILTER'S POOL
		ISystemFilterPool pool = filter.getParentFilterPool();
		ISubSystem[] subsystems = getSubSystems(pool);
		if ((subsystems != null) && (subsystems.length > 0))
		{
			// STEP 2: FOR EACH AFFECTED SUBSYSTEM FIRE AN EVENT...
			// ... TRICKY PART ==> WHO IS THE PARENT? MIGHT BE A FILTER (IF NESTED), A FILTER POOL OR A SUBSYSTEM (IF NOT SHOWING FILTER POOLS)
			ISystemFilterContainer parent = filter.getParentFilterContainer();
			boolean nested = !(parent instanceof ISystemFilterPool);
			ISystemFilter nestedParentFilter = nested ? (ISystemFilter) parent : null;
			for (int idx = 0; idx < subsystems.length; idx++)
			{
				Object parentObj = null;
				// CASE 1: FILTER IS NOT NESTED, SO SIMPLY GET ITS FILTER POOL REFERENCE AND USE AS A PARENT...
				if (!nested)
				{
					// SPECIAL CASE 1A: it makes a difference if we are showing filter pools or not...
					if (showFilterPools())
					{
						parentObj = subsystems[idx].getSystemFilterPoolReferenceManager().getReferenceToSystemFilterPool(pool);
					}
					else
					{
						parentObj = subsystems[idx];
				}
				}
				// CASE 2: FILTER IS NESTED, THIS IS MORE DIFFICULT, AS EVERY FILTER CONTAINS A RANDOMLY
				//          GENERATED REFERENCE THAT ONLY THE GUI KNOWS ABOUT.
				//         ONLY OPTION IS TO LET THE GUI FIGURE IT OUT.
				else
				{
					parentObj = nestedParentFilter;
				}
				event = cloneEvent(event, parentObj);
				event.setParent(parentObj);
					fireSubSystemEvent(event, subsystems[idx]);
				}
			}
		}


	protected SystemResourceChangeEvent cloneEvent(SystemResourceChangeEvent event, Object parent)
	{
		SystemResourceChangeEvent result;
		if(event.getMultiSource()!=null)
		{
			result = new SystemResourceChangeEvent(event.getMultiSource(), event.getType(), parent);
		}
		else
		{
			result = new SystemResourceChangeEvent(event.getSource(), event.getType(), parent);
		}
		result.setPosition(event.getPosition());
		return result;
	}


	/*
	 * Fire an event of a given id to subsystems that hold a reference to the given filter string
	 */
	protected void fireSubSystemFilterEvent(int eventId, ISystemFilterString filterString)
	{
		SystemResourceChangeEvent event = createEvent(filterString, eventId, null);
		fireSubSystemFilterEvent(event, filterString);
	}

	/*
	 * Fire an event of a given id to subsystems that hold a reference to the given filter string
	 */
	protected void fireSubSystemFilterEvent(int eventId, ISystemFilterString[] filterStrings, int delta)
	{
		if (filterStrings.length > 0)
		{
			SystemResourceChangeEvent event = createEvent(filterStrings, eventId, null, delta);
			//event.setPosition(delta);
			fireSubSystemFilterEvent(event, filterStrings[0]);
		}
	}
	/*
	 * Fire an event of a given id to subsystems that hold a reference to the given filter string
	 */
	protected void fireSubSystemFilterEvent(SystemResourceChangeEvent event, ISystemFilterString filterString)
	{
		// STEP 1: FIND ALL SUBSYSTEMS THAT CONTAIN A REFERENCE TO THIS FILTER STRING FILTER'S POOL
		ISystemFilter filter = filterString.getParentSystemFilter();
		ISystemFilterPool pool = filter.getParentFilterPool();
		ISubSystem[] subsystems = getSubSystems(pool);
		if ((subsystems != null) && (subsystems.length > 0))
		{
			// STEP 2: FOR EACH AFFECTED SUBSYSTEM FIRE AN EVENT...
			// ... TRICKY PART ==> WHO IS THE PARENT? WELL, EASY REALLY, IT IS ALWAYS THE PARENT FILTER!
			event.setParent(filter);
			for (int idx = 0; idx < subsystems.length; idx++)
			{
				fireSubSystemEvent(event, subsystems[idx]);
			}
		}
	}

	// ------------------------------------------------
	// FILTER POOL MANAGER PROVIDER CALLBACK METHODS...
	// ------------------------------------------------

	// ---------------------
	// FILTER POOL EVENTS...
	// ---------------------
	/**
	 * A new filter pool has been created
	 */
	public void filterEventFilterPoolCreated(ISystemFilterPool newPool)
	{
		//fireEvent(newPool, EVENT_ADD, this); // hmm, might not need to do this since we only work on references?

		// fire model change event in case any BP code is listening...
		RSECorePlugin.getTheSystemRegistry().fireModelChangeEvent(ISystemModelChangeEvents.SYSTEM_RESOURCE_ADDED, ISystemModelChangeEvents.SYSTEM_RESOURCETYPE_FILTERPOOL, newPool, null);
	}
	/**
	 * A filter pool has been deleted
	 */
	public void filterEventFilterPoolDeleted(ISystemFilterPool oldPool)
	{
		//fireEvent(oldPool, EVENT_DELETE, this); currently called by SystemView's delete support

		// fire model change event in case any BP code is listening...
		RSECorePlugin.getTheSystemRegistry().fireModelChangeEvent(ISystemModelChangeEvents.SYSTEM_RESOURCE_REMOVED, ISystemModelChangeEvents.SYSTEM_RESOURCETYPE_FILTERPOOL, oldPool, null);
	}
	/**
	 * A filter pool has been renamed
	 */
	public void filterEventFilterPoolRenamed(ISystemFilterPool pool, String oldName)
	{
		//fireEvent(pool, EVENT_RENAME, this); subsystem handles in firing of reference rename

		// fire model change event in case any BP code is listening...
		RSECorePlugin.getTheSystemRegistry().fireModelChangeEvent(ISystemModelChangeEvents.SYSTEM_RESOURCE_RENAMED, ISystemModelChangeEvents.SYSTEM_RESOURCETYPE_FILTERPOOL, pool, oldName);
	}
	/**
	 * One or more filter pools have been re-ordered within their manager
	 */
	public void filterEventFilterPoolsRePositioned(ISystemFilterPool[] pools, int delta)
	{
		fireEvent(pools, ISystemResourceChangeEvents.EVENT_MOVE_MANY, this, delta);

		// fire model change event in case any BP code is listening...
		if (pools!=null)
			for (int idx=0; idx<pools.length; idx++)
				RSECorePlugin.getTheSystemRegistry().fireModelChangeEvent(ISystemModelChangeEvents.SYSTEM_RESOURCE_REORDERED, ISystemModelChangeEvents.SYSTEM_RESOURCETYPE_FILTERPOOL, pools[idx], null);
	}

	// ---------------------
	// FILTER EVENTS...
	// ---------------------
	/**
	 * A new filter has been created
	 */
	public void filterEventFilterCreated(ISystemFilter newFilter)
	{
		fireSubSystemFilterEvent(ISystemResourceChangeEvents.EVENT_ADD_FILTER_REFERENCE, newFilter);

		// fire model change event in case any BP code is listening...
		RSECorePlugin.getTheSystemRegistry().fireModelChangeEvent(ISystemModelChangeEvents.SYSTEM_RESOURCE_ADDED, ISystemModelChangeEvents.SYSTEM_RESOURCETYPE_FILTER, newFilter, null);
	}

	/**
	 * A filter has been deleted
	 */
	public void filterEventFilterDeleted(ISystemFilter oldFilter)
	{
		fireSubSystemFilterEvent(ISystemResourceChangeEvents.EVENT_DELETE_FILTER_REFERENCE, oldFilter);

		// fire model change event in case any BP code is listening...
		RSECorePlugin.getTheSystemRegistry().fireModelChangeEvent(ISystemModelChangeEvents.SYSTEM_RESOURCE_REMOVED, ISystemModelChangeEvents.SYSTEM_RESOURCETYPE_FILTER, oldFilter, null);
	}

	/**
	 * A filter has been renamed
	 */
	public void filterEventFilterRenamed(ISystemFilter filter, String oldName)
	{
		fireSubSystemFilterEvent(ISystemResourceChangeEvents.EVENT_RENAME_FILTER_REFERENCE, filter);

		// fire model change event in case any BP code is listening...
		RSECorePlugin.getTheSystemRegistry().fireModelChangeEvent(ISystemModelChangeEvents.SYSTEM_RESOURCE_RENAMED, ISystemModelChangeEvents.SYSTEM_RESOURCETYPE_FILTER, filter, oldName);
	}

	/**
	 * A filter's strings have been updated
	 */
	public void filterEventFilterUpdated(ISystemFilter filter)
	{
		fireSubSystemFilterEvent(ISystemResourceChangeEvents.EVENT_CHANGE_FILTER_REFERENCE, filter);

		// fire model change event in case any BP code is listening...
		RSECorePlugin.getTheSystemRegistry().fireModelChangeEvent(ISystemModelChangeEvents.SYSTEM_RESOURCE_CHANGED, ISystemModelChangeEvents.SYSTEM_RESOURCETYPE_FILTER, filter, null);
	}

	/**
	 * One or more filters have been re-ordered within their pool or filter (if nested)
	 */
	public void filterEventFiltersRePositioned(ISystemFilter[] filters, int delta)
	{
		if ((filters!=null) && (filters.length > 0))
		{
			SystemResourceChangeEvent event = createEvent(filters, ISystemResourceChangeEvents.EVENT_MOVE_FILTER_REFERENCES, null);
			event.setPosition(delta);
			fireSubSystemFilterEvent(event, filters[0]);
			// fire model change event in case any BP code is listening...
			for (int idx=0; idx<filters.length; idx++)
				RSECorePlugin.getTheSystemRegistry().fireModelChangeEvent(ISystemModelChangeEvents.SYSTEM_RESOURCE_REORDERED, ISystemModelChangeEvents.SYSTEM_RESOURCETYPE_FILTER, filters[idx], null);

		}
		//System.out.println("In SubSystemConfigurationImpl#filterEventFiltersRepositioned(). Firing EVENT_MOVE_FILTER_REFERENCES");
		//fireSubSystemFilterEvent(event);
	}
	// -----------------------
	// FILTER STRING EVENTS...
	// -----------------------
	/**
	 * A new filter string has been created
	 */
	public void filterEventFilterStringCreated(ISystemFilterString newFilterString)
	{
		fireSubSystemFilterEvent(ISystemResourceChangeEvents.EVENT_ADD_FILTERSTRING_REFERENCE, newFilterString);
		// fire model change event in case any BP code is listening...
		RSECorePlugin.getTheSystemRegistry().fireModelChangeEvent(ISystemModelChangeEvents.SYSTEM_RESOURCE_CHANGED, ISystemModelChangeEvents.SYSTEM_RESOURCETYPE_FILTER, newFilterString.getParentSystemFilter(), null);
	}
	/**
	 * A filter string has been deleted
	 */
	public void filterEventFilterStringDeleted(ISystemFilterString oldFilterString)
	{
		fireSubSystemFilterEvent(ISystemResourceChangeEvents.EVENT_DELETE_FILTERSTRING_REFERENCE, oldFilterString);
		// fire model change event in case any BP code is listening...
		RSECorePlugin.getTheSystemRegistry().fireModelChangeEvent(ISystemModelChangeEvents.SYSTEM_RESOURCE_CHANGED, ISystemModelChangeEvents.SYSTEM_RESOURCETYPE_FILTER, oldFilterString.getParentSystemFilter(), null);
	}
	/**
	 * A filter string has been updated
	 */
	public void filterEventFilterStringUpdated(ISystemFilterString filterString)
	{
		fireSubSystemFilterEvent(ISystemResourceChangeEvents.EVENT_CHANGE_FILTERSTRING_REFERENCE, filterString);
		// fire model change event in case any BP code is listening...
		RSECorePlugin.getTheSystemRegistry().fireModelChangeEvent(ISystemModelChangeEvents.SYSTEM_RESOURCE_CHANGED, ISystemModelChangeEvents.SYSTEM_RESOURCETYPE_FILTER, filterString.getParentSystemFilter(), null);
	}
	/**
	 * One or more filters have been re-ordered within their filter
	 */
	public void filterEventFilterStringsRePositioned(ISystemFilterString[] filterStrings, int delta)
	{
		fireSubSystemFilterEvent(ISystemResourceChangeEvents.EVENT_MOVE_FILTERSTRING_REFERENCES, filterStrings, delta);
		// fire model change event in case any BP code is listening...
		if ((filterStrings!=null) && (filterStrings.length>0))
			RSECorePlugin.getTheSystemRegistry().fireModelChangeEvent(ISystemModelChangeEvents.SYSTEM_RESOURCE_CHANGED, ISystemModelChangeEvents.SYSTEM_RESOURCETYPE_FILTER, filterStrings[0].getParentSystemFilter(), null);
	}

	// ---------------------------------
	// FILTER POOL METHODS...
	// ---------------------------------

	/**
	 * Returns a filter pool, given its profile and pool name
	 */
	public ISystemFilterPool getFilterPool(ISystemProfile profile, String name)
	{
		return getFilterPoolManager(profile).getSystemFilterPool(name);
	}
	/**
	 * Returns an array of filter pool objects within a profile.
	 */
	public ISystemFilterPool[] getFilterPools(ISystemProfile profile)
	{
		return getFilterPoolManager(profile).getSystemFilterPools();
	}

	/**
	 * Given a filter, decide whether to show the Filter Strings property page
	 *  for this filter. Default is true.
	 */
	public boolean showChangeFilterStringsPropertyPage(ISystemFilter filter)
	{
		return true;
	}
	// ---------------------------------
	// FILTER POOL REFERENCE METHODS...
	// ---------------------------------
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
	public ISubSystem[] testForActiveReferences(ISystemProfile profile)
	{
		List l = new ArrayList();
		ISystemProfileManager profileMgr = SystemProfileManager.getDefault();
		ISystemFilterPoolManager sfpm = getFilterPoolManager(profile);
		String profileName = profile.getName();
		if (sfpm != null)
		{
			ISystemFilterPool[] pools = sfpm.getSystemFilterPools();
			if ((pools != null) && (pools.length > 0))
			{
				for (int idx = 0; idx < pools.length; idx++)
				{
					IRSEBaseReferencingObject[] refs = pools[idx].getReferencingObjects();
					if ((refs != null) && (refs.length > 0))
					{
						for (int jdx = 0; jdx < refs.length; jdx++)
						{
							if (refs[jdx] instanceof ISystemFilterPoolReference)
							{
								ISystemFilterPoolReference fpRef = (ISystemFilterPoolReference) refs[jdx];
								//SystemFilterPoolReferenceManager fpRefMgr = fpRef.getFilterPoolReferenceManager();
								ISubSystem subsystem = (ISubSystem) fpRef.getProvider();
								String ssProfileName = subsystem.getSystemProfileName();
								if ((!ssProfileName.equals(profileName)) && (profileMgr.isSystemProfileActive(ssProfileName)))
								{
									l.add(subsystem);
								}
							}
						}
					}
				}
			}
		}

		ISubSystem[] referencingSubSystems = null;
		if (l.size() > 0)
		{
			referencingSubSystems = (ISubSystem[]) l.toArray(new ISubSystem[l.size()]);
		}

		return referencingSubSystems;
	}



	// ---------------------------------
	// FILTER METHODS
	// ---------------------------------

	/**
	 * Return the translated string to show in the property sheet for the type property.
	 */
	public String getTranslatedFilterTypeProperty(ISystemFilter selectedFilter)
	{
		if (translatedFilterType == null)
			//translatedFilterType = "Remote system filter";
			translatedFilterType = SystemPropertyResources.RESID_PROPERTY_FILTERTYPE_VALUE;
		return translatedFilterType;
	}
	/*
	 * Return the translated string to show in the property sheet for the type property when a filter string is selected.
	 *
	public String getTranslatedFilterStringTypeProperty(SystemFilterString selectedFilterString)
	{
		if (translatedFilterStringType == null)
		  //translatedFilterType = "Remote system filter string";
	      translatedFilterStringType = SystemResources.RESID_PROPERTY_FILTERSTRINGTYPE_VALUE);
		return translatedFilterStringType;
	}*/

	/**
	 * Return true if we should show the refresh action in the popup for the given element.
	 */
	public boolean showRefreshOnFilter()
	{
		return true;
	}

	/**
	  * Return true if we should show the show in table action in the popup for the given element.
	  */
	public boolean showGenericShowInTableOnFilter()
	{
		return true;
	}

	/*
	 * Overridable method to return the actions for creating a new filter string in a filter.
	 * By default returns one action created by calling {@link #getNewFilterStringAction(SystemFilter, Shell)}.
	 * <p>
	 * If you have multiple actions for creating new filter strings, override this.
	 * <p>
	 * If you have only a single action for creating new filter strings, override getNewFilterStringAction (without the 's').
	 * <p>
	 * @param selectedFilter the currently selected filter
	 * @param shell parent shell of viewer where the popup menu is being constructed
	 *
	protected IAction[] getNewFilterStringActions(SystemFilter selectedFilter, Shell shell)
	{
		IAction[] actions = null;
		IAction newAction = getNewFilterStringAction(selectedFilter, shell);
		if (newAction != null)
	    {
	    	actions = new IAction[1];
	    	actions[0] = newAction;
	    }
		return actions;
	}*/

	/*
	 * Overridable method to return the single action for creating a new filter string in a filter.
	 * By default returns a new SystemDefaultNewFilterAction.
	 * <p>
	 * If you have multiple actions for creating new filters, override getNewFilterStringActions (note the 's').
	 * <p>
	 * If you have only a single action for creating new filters, override this.
	 * <p>
	 * @param filter the currently selected filter
	 * @param shell parent shell of viewer where the popup menu is being constructed
	 *
	protected IAction getNewFilterStringAction(SystemFilter selectedFilter, Shell shell)
	{
		//SystemFilterDefaultNewFilterStringAction action =
		//  new SystemFilterDefaultNewFilterStringAction(shell, selectedFilter);
		//return action;
		return null;
	}*/

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
	public IServerLauncherProperties createServerLauncher(IConnectorService connectorService)
	{
		IRemoteServerLauncher sl = new RemoteServerLauncher("Remote Server Launcher", connectorService); //$NON-NLS-1$
		if (sl instanceof ILabeledObject) {
			((ILabeledObject)sl).setLabel(RSECoreMessages.RESID_PROPERTYSET_REMOTE_SERVER_LAUNCHER);
		}

		IRSESystemType systemType = connectorService.getHost().getSystemType();
		String systemTypeId = systemType.getId();

		if (systemTypeId.equals(IRSESystemType.SYSTEMTYPE_LINUX_ID)
			|| systemTypeId.equals(IRSESystemType.SYSTEMTYPE_POWER_LINUX_ID)
			|| systemTypeId.equals(IRSESystemType.SYSTEMTYPE_ZSERIES_LINUX_ID)
		) {
			sl.setServerScript(RemoteServerLauncherConstants.LINUX_REXEC_SCRIPT);
		}
		else if (systemTypeId.equals(IRSESystemType.SYSTEMTYPE_UNIX_ID)
			|| systemTypeId.equals(IRSESystemType.SYSTEMTYPE_AIX_ID)
		) {
			sl.setServerScript(RemoteServerLauncherConstants.UNIX_REXEC_SCRIPT);
		}

		sl.saveToProperties();
		return sl;
	}

	// ------------------------------------------
	// SAVE METHODS...
	// ------------------------------------------
	/**
	 * Saves absolutely everything to disk. This is called as a safety
	 * measure when the workbench shuts down.
	 * <p>
	 * Totally handled for you!
	 * <p>
	 * Calls saveSubSystems() and saveFilterPools()
	 * <p>
	 * Exceptions are swallowed since we can deal with them on shutdown anyway!
	 */
	public boolean commit()
	{
		boolean ok = false;
		try
		{
			saveSubSystems();
			saveFilterPools();
			ok = true;
		}
		catch (Exception exc)
		{
		}
		return ok;
	}

	/**
	 * Save all subsystems for all connections to disk.
	 * The default implementation for this iterates all subsystem instances,
	 *  and calls saveSubSystem for each.
	 * <p>
	 * If you handle your own save action versus using MOF, then override saveSubSystem(SubSystem)
	 *  versus this method.
	 * <p>
	 * Attempts to save all of them, swallowing exceptions, then at the end throws the last exception caught.
	 */
	public void saveSubSystems() throws Exception
	{
		saveSubSystems(null);
	}

	/**
	 * Save all subsystems for a given connection to disk.
	 * The default implementation for this iterates all subsystem instances for that connection,
	 *  and calls saveSubSystem for each.
	 * <p>
	 * If you handle your own save action versus using MOF, then override saveSubSystem(SubSystem)
	 *  versus this method.
	 * <p>
	 * Attempts to save all of them, swallowing exceptions, then at the end throws the last exception caught.
	 */
	public void saveSubSystems(IHost conn) throws Exception
	{
		Exception lastException = null;
		ISubSystem[] subsystems = null;
		if (conn != null)
			subsystems = getSubSystems(conn, ISubSystemConfiguration.LAZILY);
		else
			subsystems = getSubSystems(false); // false=> lazy get; don't restore from disk if not already
		if (subsystems == null)
			return;
		for (int idx = 0; idx < subsystems.length; idx++)
		{
			try
			{
				saveSubSystem(subsystems[idx]);
			}
			catch (Exception exc)
			{
				lastException = exc;
			}
		}
		if (lastException != null)
			throw lastException;
	}

	/**
	 * Attempt to save single subsystem to disk.
	 * Uses MOF to save the given subsystem object.
	 * <p>
	 * Calls get saveFileName on the subsystem object to determine what file name to save to.
	 * <p>
	 * You need only override if you do not use MOF!
	 */
	public void saveSubSystem(ISubSystem subsys) throws Exception
	{
		subsys.commit();
	}

	/**
	 * Saves all filter information to disk for all profiles.
	 * <p>
	 * This method is handled for you. If you do override, please call super.saveFilterPools.
	 */
	public boolean saveFilterPools() throws Exception
	{
		boolean ok = true;
		ISystemFilterPoolManager[] poolManagers = getFilterPoolManagers();
		Exception lastException = null;
		if (poolManagers != null)
			for (int idx = 0; idx < poolManagers.length; idx++)
			{
				try
				{
					saveFilterPools(poolManagers[idx]);
				}
				catch (Exception exc)
				{
					ok = false;
					lastException = exc;
				}
			}
		if (lastException != null)
			throw lastException;
		return ok;
	}
	/**
	 * Saves all filter information to disk for the given profile.
	 * <p>
	 * This method is handled for you. If you do override, please call super.saveFilterPools.
	 */
	public void saveFilterPools(ISystemFilterPoolManager mgr) throws Exception
	{
		try
		{
			mgr.commit();
		}
		catch (Exception exc)
		{
			handleException("Exception saving filter pools for manager " + mgr.getName(), exc); //$NON-NLS-1$
			throw exc;
		}
	}

	// used in the case where newsubsystems are added after a connection exists
	public ISubSystem createSubSystemAfterTheFact(IHost conn)
	{
		ISubSystem subsys = null;
		try {
			subsys = createSubSystemInternal(conn);
		} catch (RuntimeException e) {
			RSECorePlugin.getDefault().getLogger().logError("Error creating subsystem", e); //$NON-NLS-1$
		}
		if (subsys != null)
		{
			internalInitializeNewSubSystem(subsys, conn);
			if (supportsFilters())
			{
				// We create a filter pool reference manager object to manage the filter pool references
				// that are stored with a subsystem.
				//SystemFilterPoolManager[] relatedFilterPoolManagers =
				//  getReferencableFilterPoolManagers(conn.getSystemProfile());
				ISystemFilterPoolReferenceManager fprMgr = SystemFilterStartHere.getInstance().createSystemFilterPoolReferenceManager(subsys, this, subsys.getName());
				subsys.setFilterPoolReferenceManager(fprMgr);
				ISystemFilterPoolManager defaultFilterPoolManager = getFilterPoolManager(conn.getSystemProfile());
				fprMgr.setDefaultSystemFilterPoolManager(defaultFilterPoolManager);
			}
			initializeSubSystem(subsys, null);
			try
			{
				saveSubSystem(subsys);
			}
			catch (Exception exc)
			{
				SystemBasePlugin.logError("Error saving new subsystem " + subsys.getName(), exc); //$NON-NLS-1$
			}
			addSubSystem(subsys); // only add to list even if save was not successful.
			//if (lastExc != null)
			//throw lastExc;

		}
		return subsys;
	}

	/**
	 * Return true if the subsystems for a given connection have been restored yet or not
	 */
	protected boolean subSystemsHaveBeenRestored(IHost connection)
	{
		return (subSystemsRestoredFlags.get(connection) != null);
	}

	/**
	 * Get all the filter pool managers for all the profiles, active or not.
	 * This allows cross references from
	 * one subsystem in one profile to filter pools in any other profile.
	 */
	public ISystemFilterPoolManager[] getAllSystemFilterPoolManagers()
	{
		ISystemProfile[] profiles = RSECorePlugin.getTheSystemProfileManager().getSystemProfiles();
		ISystemFilterPoolManager[] allMgrs = new ISystemFilterPoolManager[profiles.length];
		for (int idx = 0; idx < profiles.length; idx++)
		{
			allMgrs[idx] = getFilterPoolManager(profiles[idx]);
		}
		return allMgrs;
	}




	/**
	 * Called by adapters prior to asking for actions, in case the connection of the currently selected
	 *  object is required by the action.
	 */
	public void setConnection(IHost connection)
	{
		this.currentlySelectedConnection = connection;
	}
	/**
	 * Called by adapters prior to asking for actions. For cases when current selection is needed.
	 */
	public void setCurrentSelection(Object[] selection)
	{
		this.currentlySelected = selection;
	}

	// ---------------------------------
	// PRIVATE METHODS...
	// ---------------------------------
	protected void handleException(String msg, Exception exc)
	{
		SystemBasePlugin.logError(msg, exc);
	}


	// ------------------
	// UTILITY METHODS...
	// ------------------
	/**
	 * Helper method to return the message "Connecting to &1..."
	 */
	public static String getConnectingMessage(String hostName, int port)
	{
		String msgTxt = null;
		if (port > 0)
		{
			msgTxt = NLS.bind(CommonMessages.MSG_CONNECTWITHPORT_PROGRESS, hostName, Integer.toString(port));
		}
		else
		{	msgTxt = NLS.bind(CommonMessages.MSG_CONNECT_PROGRESS, hostName);
		}
		return msgTxt;
	}
	/**
	 * Helper method to return the message "Disconnecting from &1..."
	 */
	public static String getDisconnectingMessage(String hostName, int port)
	{
		String msgTxt = null;
		if (port > 0)
		{
			msgTxt = NLS.bind(CommonMessages.MSG_DISCONNECTWITHPORT_PROGRESS, hostName, Integer.toString(port));
		}
		else
		{
			msgTxt = NLS.bind(CommonMessages.MSG_DISCONNECT_PROGRESS, hostName);

		}
		return msgTxt;
	}

	/**
	 * Return the translated name of a default filter pool for a given profile
	 */
	public static String getDefaultFilterPoolName(String profileName, String factoryId)
	{
		StringBuffer nameBuf = new StringBuffer();
		nameBuf.append(profileName);
		nameBuf.append(":"); //$NON-NLS-1$
		nameBuf.append(factoryId);
		/*
		String name = SystemResources.RESID_DEFAULT_FILTERPOOL;
		StringBuffer profileNameBuffer = new StringBuffer(profileName.toLowerCase());
		profileNameBuffer.setCharAt(0, Character.toUpperCase(profileNameBuffer.charAt(0)));
		name = SystemMessage.sub(name, "%1", profileNameBuffer.toString());
		*/
		return nameBuf.toString();
	}

	// -----------------
	// COMMON METHODS...
	// -----------------

	/**
	 * Return object as printable string.
	 * This is the id plus dot plus the name.
	 */
	public String toString()
	{
		return proxy.toString();
	}

	public List getSubSystemList()
	{
		return subSystemList;
	}

	public List getFilterPoolManagerList()
	{
		return filterPoolManagerList;
	}

	public Object getAdapter(Class adapterType)
	{
   	    return Platform.getAdapterManager().getAdapter(this, adapterType);
	}

	/**
	 * Subsystem configurations are never persisted.
	 * @return false
	 */
	public boolean isDirty()
	{
		return false;
	}

	/**
	 * Subsystem configurations are never marked dirty. This does nothing.
	 */
	public void setDirty(boolean flag)
	{
	}

	/**
	 * Subsystem configurations are never persisted.
	 * @return false
	 */
	public boolean isTainted()
	{
		return false;
	}

	/**
	 * Subsystem configurations are never marked dirty. This does nothing.
	 */
	public void setTainted(boolean flag)
	{
	}

	/**
	 * Subsystem configurations are never restored since they are not persisted.
	 * @return false
	 */
	public boolean wasRestored()
	{
		return false;
	}

	/**
	 * Subsystem configurations are never restored. This does nothing.
	 */
	public void setWasRestored(boolean flag)
	{
	}

	public void beginRestore() {
	}

	public void endRestore() {
	}

	/**
	 * Subsystem configurations are not persisted.
	 * @return null
	 */
	public IRSEPersistableContainer getPersistableParent() {
		return null;
	}

	public IRSEPersistableContainer[] getPersistableChildren() {
		return IRSEPersistableContainer.NO_CHILDREN;
	}

	/**
	 * Return true if deferred queries are supported. By default, they are
	 * supported. Override for different behavior.
	 *
	 * @return <code>true</code> if deferred queries are supported.
	 * @see ISubSystemConfiguration#supportsDeferredQueries()
	 */
	public boolean supportsDeferredQueries()
	{
		return true;
	}

	/*
	 * Service Subsystem Configuration methods - default implementations
	 */

	/**
	 * {@inheritDoc}
	 * <p>
	 * This default implementation does nothing. Service subsystems must
	 * override as defined in the interface.
	 *
	 * @see ISubSystemConfiguration#setConnectorService(IHost,
	 * 	IConnectorService)
	 * @since org.eclipse.rse.core 3.0
	 */
	public void setConnectorService(IHost host, IConnectorService connectorService) {
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This default implementation returns <code>null</code>. Service subsystem
	 * configurations must override as defined in the interface.
	 *
	 * @see ISubSystemConfiguration#getConnectorService(IHost)
	 * @since org.eclipse.rse.core 3.0
	 */
	public IConnectorService getConnectorService(IHost host) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This default implementation returns <code>null</code>. Service subsystem
	 * configurations must override as defined in the interface.
	 *
	 * @see ISubSystemConfiguration#getServiceType()
	 * @since org.eclipse.rse.core 3.0
	 */
	public Class getServiceType() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This default implementation returns <code>null</code>. Service subsystem
	 * configurations must override as defined in the interface.
	 *
	 * @see ISubSystemConfiguration#getServiceImplType()
	 * @since org.eclipse.rse.core 3.0
	 */
	public Class getServiceImplType() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This default implementation returns <code>null</code>. Service subsystem
	 * configurations must override as defined in the interface.
	 *
	 * @see ISubSystemConfiguration#getService(IHost)
	 * @since org.eclipse.rse.core 3.0
	 */
	public IService getService(IHost host) {
		return null;
	}

}
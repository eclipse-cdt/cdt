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

package org.eclipse.rse.core.subsystems;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.core.SystemBasePlugin;
import org.eclipse.rse.core.SystemPreferencesManager;
import org.eclipse.rse.core.internal.subsystems.IBMServerLauncherConstants;
import org.eclipse.rse.core.internal.subsystems.SubSystemFilterNamingPolicy;
import org.eclipse.rse.filters.ISystemFilter;
import org.eclipse.rse.filters.ISystemFilterContainer;
import org.eclipse.rse.filters.ISystemFilterContainerReference;
import org.eclipse.rse.filters.ISystemFilterPool;
import org.eclipse.rse.filters.ISystemFilterPoolManager;
import org.eclipse.rse.filters.ISystemFilterPoolReference;
import org.eclipse.rse.filters.ISystemFilterPoolReferenceManager;
import org.eclipse.rse.filters.ISystemFilterSavePolicies;
import org.eclipse.rse.filters.ISystemFilterString;
import org.eclipse.rse.filters.SystemFilterPoolWrapperInformation;
import org.eclipse.rse.filters.SystemFilterStartHere;
import org.eclipse.rse.internal.filters.SystemFilterPoolManager;
import org.eclipse.rse.internal.model.SystemProfileManager;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.model.ISystemModelChangeEvents;
import org.eclipse.rse.model.ISystemProfile;
import org.eclipse.rse.model.ISystemProfileManager;
import org.eclipse.rse.model.ISystemRegistry;
import org.eclipse.rse.model.ISystemResourceChangeEvents;
import org.eclipse.rse.model.SystemResourceChangeEvent;
import org.eclipse.rse.model.SystemStartHere;
import org.eclipse.rse.references.ISystemBaseReferencingObject;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.SystemMenuManager;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemPropertyResources;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.rse.ui.filters.actions.SystemNewFilterAction;
import org.eclipse.rse.ui.messages.ISystemMessageLine;
import org.eclipse.rse.ui.messages.SystemMessageDialog;
import org.eclipse.rse.ui.propertypages.ISystemSubSystemPropertyPageCoreForm;
import org.eclipse.rse.ui.propertypages.SystemSubSystemPropertyPageCoreForm;
import org.eclipse.rse.ui.validators.ISystemValidator;
import org.eclipse.rse.ui.validators.ValidatorPortInput;
import org.eclipse.rse.ui.validators.ValidatorSpecialChar;
import org.eclipse.rse.ui.view.ISystemRemoteElementAdapter;
import org.eclipse.rse.ui.widgets.IBMServerLauncherForm;
import org.eclipse.rse.ui.widgets.IServerLauncherForm;
import org.eclipse.rse.ui.wizards.ISubSystemPropertiesWizardPage;
import org.eclipse.rse.ui.wizards.ISystemNewConnectionWizardPage;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.PropertyPage;


/**
 * Abstract base class for subsystem factory extension points.
 * Child classes must implement the methods:
 * <ul>
 *  <li>#createSubSystemInternal(SystemConnection conn)
 * </ul>
 * Child classes can optionally override:
 * <ul>
 *  <li>SubSystemFactory#supportsFilters() to indicate if filters are to be enabled for this factory
 *  <li>SubSystemFactory#supportsNestedFilters() to indicate if filters can exist inside filters.
 *  <li>SubSystemFactory#supportsDuplicateFilterStrings() to indicate if filter strings can be duplicates within a filter
 *  <li>SubSystemFactory#isCaseSensitive() to indicate if filter strings are case sensitive or not
 *  <li>SubSystemFactory#supportsQuickFilters() to indicate if filters can be specified at contain expansion time.
 *  <li>SubSystemFactory#supportsUserActions() to indicate if users can define their own actions for your subsystems' child objects.
 *  <li>SubSystemFactory#supportsCompileActions() to indicate if users can compile remote objects using menu actions 
 *  <li>SubSystemFactory#supportsFileTypes() to indicate if users can define their own named file types.
 *  <li>SubSystemFactory#isSubSystemsDeletable() if they support user-deleting of subsystems. Default is false
 *  <li>SubSystemFactory#supportsSubSystemConnect() to return false if the connect() action is not supported
 *  <li>SubSystemFactory#supportsTargets() to return true if this factory supports the notions of targets. Normally, this is only for file system factories.
 *  <li>SubSystemFactory#getSubSystemActions() if they wish to supply actions for the right-click menu when
 *       the user right clicks on a subsystem object created by this factory.
 *  <li>CreateDefaultFilterPool() to create any default filter pool when a new profile is created.
 *  <li>#initializeSubSystem(SubSystem ss, ISystemNewConnectionWizardPage[])
 * </ul>
 * <p>
 * A factory will maintain in memory a list of all subsystem objects it has. This
 *  list should be initialize from disk at restore time, and maintained as the subsystems are
 *  created and deleted throughout the session. At save time, each subsystem in the list
 *  is asked to save itself. The getSubSystems method should return this list.
 * <p>
 * To help with maintaining this list, this base class contains a Vector instance variable
 *  named subsystems. It is returned by the getSubSystems method in this base class. For this
 *  to be accurate you though, you should:
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
	protected SubSystemFilterNamingPolicy filterNamingPolicy = new SubSystemFilterNamingPolicy();
	protected ISystemFilterPoolManager[] filterPoolManagers = null;
	protected Hashtable filterPoolManagersPerProfile = new Hashtable();


	// other stuff...
	private String translatedFilterType = null;
	private static Hashtable brokenReferenceWarningsIssued = new Hashtable();
	protected Hashtable imageTable = null;
	protected IHost currentlySelectedConnection;
	protected Object[] currentlySelected;

	// support for default subclasses for non-mof users
	protected static IHost currentlyProcessingConnection;
	protected static SubSystemConfiguration currentlyProcessingSubSystemFactory;


	protected java.util.List subSystemList = null;
	protected java.util.List filterPoolManagerList = null;

	protected boolean _isDirty;

	
	/**
	 * Constructor
	 */
	public SubSystemConfiguration()
	{
		super();
		//initSubSystems();
		SystemBasePlugin.logDebugMessage(this.getClass().getName(), "STARTED SSFACTORY");
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
	 * Return true (default) or false to indicate if subsystems of this factory require a userId to
	 *  do connection or not. If not, no GUI will be supplied related to user Ids in the remote systems
	 *  explorer view.
	 * <p>
	 * Returns TRUE by default.
	 */
	public boolean supportsUserId()
	{
		return true;
	}
	/**
	 * Return true if instance of this factory's subsystems support connect and disconnect actions.
	 * <b>By default, returns true</b>.
	 * Override if this is not the case.
	 */
	public boolean supportsSubSystemConnect()
	{
		return true;
	}
	/**
	 * Return true (default) or false to indicate if subsystems of this factory support user-editable
	 *  port numbers.
	 */
	public boolean isPortEditable()
	{
		return true;
	}
	/**
	 * Return true if subsystem instances from this factory support remote command execution
	 * <p>RETURNS FALSE BY DEFAULT.
	 */
	public boolean supportsCommands()
	{
		return false;
	}
	/**
	 * Return true if subsystem instances from this factory support getting and setting properties
	 * <p>RETURNS FALSE BY DEFAULT.
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
     * Required method for subsystem factory child classes. Return true if you filter caching.
     * If you support filter caching, then the views will always check the in-memory cache for
     * filter results before attempting a query.
	 * <p>Returns true in default implementation.
     */
	public boolean supportsFilterCaching()
	{
		return true;
	}

	/**
	 * Required method for subsystem factory child classes. Return true if you support filters, and you support
	 *  multiple filter strings per filter. Return false to restrict the user to one string per filter.
	 * <p>Returns TRUE by default.
	 */
	public boolean supportsMultipleFilterStrings()
	{
		return true;
	}

	/**
	 * Required method for subsystem factory child classes if returning true from supportsFilters.
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
	 * Return true if filters of this subsystem factory support dropping into.
	 * Override this method to provide drop support for filters.
	 */
	public boolean supportsDropInFilters()
	{
	    return false;
	}
	
	/** 
	 * Return true if filters of this subsystem factory provide a custom implementation of drop support.  
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
	 * Return true if you support user-defined actions for the remote system objects returned from expansion of
	 *  subsystems created by this subsystem factory
	 * <p>RETURNS false BY DEFAULT
	 * 
	 * @see #supportsUserDefinedActions(ISelection)
	 * @see #getActionSubSystem(ISubSystem)
	 * @see #createActionSubSystem() 
	 */
	public boolean supportsUserDefinedActions()
	{
		return false;
	}
	
	
	/**
	 * Return true if you support user-defined actions for the remote system objects explicitly given. This
	 *  calls supportsUserDefinedActions() by default. It is called when decided whether or not to show
	 *  the User Actions menu for the current selection, if supportsUserDefinedActions() returns true.
	 * 
	 * @see #getActionSubSystem(ISubSystem)
	 * @see #createActionSubSystem()
	 * @see #addCommonRemoteActions(SystemMenuManager, IStructuredSelection, Shell, String, ISubSystem)
	 */
	public boolean supportsUserDefinedActions(ISelection selection)
	{
		// no selection or empty selection, so default to subsystem factory
		if (selection == null || selection.isEmpty()) {
			return supportsUserDefinedActions();
		}
		else {
			
			// selection is a structured selection
			if (selection instanceof IStructuredSelection) {
				
				IStructuredSelection sel = (IStructuredSelection)selection;
				
				Iterator iter = sel.iterator();
				
				boolean supportsUserDefinedActions = true;
				
				// check if adapter of each object supports user defined actions
				while (iter.hasNext()) {
					Object obj = iter.next();
					
					// we query adapter as to whether it supports user defined actions only
					// if the adapter is a remote element adapter
					ISystemRemoteElementAdapter adapter = (ISystemRemoteElementAdapter)(Platform.getAdapterManager().getAdapter(obj, ISystemRemoteElementAdapter.class));
					
					if (adapter != null) {
						supportsUserDefinedActions = adapter.supportsUserDefinedActions(obj);
					}
					else {
						supportsUserDefinedActions = supportsUserDefinedActions();
					}
					
					// if one of the selections doesn't support user defined actions, we return false
					if (!supportsUserDefinedActions) {
						return false;
					}
				}
				
				// all adapters support user defined actions, so return whether the subsystem factory
				// supports user defined actions
				return supportsUserDefinedActions();
			}
			// not a structured selection, so default to asking subsystem factory 
			else {
				return supportsUserDefinedActions();
			}
		}
	}
	/**
	 * Return true if you support compile actions for the remote system objects returned from expansion of
	 *  subsystems created by this subsystem factory.
	 * <p>
	 * By returning true, user sees a "Work with->Compile Commands..." action item in the popup menu for this
	 *  subsystem. The action is supplied by the framework, but is populated using overridable methods in this subsystem.
	 * <p>RETURNS false BY DEFAULT
	 * @see #getCompileManager()
	 * @see #createCompileManager()
	 */
	public boolean supportsCompileActions()
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
	 * Tell us if this subsystem factory supports targets, which are destinations for 
	 *   pushes and builds. Normally only true for file system factories.
	 */
	public boolean supportsTargets()
	{
		return false;
	}
	/**
	 * Tell us if this subsystem factory supports server launch properties, which allow the user
	 *  to configure how the server-side code for these subsystems are started. There is a Server
	 *  Launch Setting property page, with a pluggable composite, where users can configure these 
	 *  properties. 
	 * <p>
	 * If you return true here, you may also want to override {@link #supportsServerLaunchType(ServerLaunchType)}. 
	 * <br> By default we return false here. This is overridden in UniversalFileSubSystemFactory though. 
	 */
	public abstract boolean supportsServerLaunchProperties(IHost host);

	/**
	 * If {@link #supportsServerLaunchProperties()} returns true, this method may be called by
	 * the server launcher to decide if a given remote server launch type is supported or not.
	 * <br> We return true by default.
	 * @see org.eclipse.rse.core.subsystems.ServerLaunchType
	 * @see #getServerLauncherForm(Shell, ISystemMessageLine)
	 */
	public boolean supportsServerLaunchType(ServerLaunchType serverLaunchType)
	{
		return true;
	}	
			
	/**
	 * Determines whether this factory is responsible for the creation of subsytems of the specified type
	 * Subsystem factories should override this to indicate which subsystems they support.
	 * 
	 * @param subSystemType type of subsystem
	 * @return whether this factory is for the specified subsystemtype
	 */
	public boolean isFactoryFor(Class subSystemType)
	{
		//return SubSystem.class.isAssignableFrom(subSystemType);
		return false;
	}

	/*
	 * Return the form used in the subsyste property page.  This default implementation returns Syste
	 */
	public ISystemSubSystemPropertyPageCoreForm getSubSystemPropertyPageCoreFrom(ISystemMessageLine msgLine, Object caller)
	{
	    return new SystemSubSystemPropertyPageCoreForm(msgLine, caller);
	}
	
	/**
	 * Gets the list of property pages applicable for a subsystem associated with this factory
	 * @return the list of subsystem property pages
	 */
	protected List getSubSystemPropertyPages()
	{
		List propertyPages= new ArrayList();
		// Get reference to the plug-in registry
		IExtensionRegistry registry = Platform.getExtensionRegistry();

		// Get configured property page extenders
		IConfigurationElement[] propertyPageExtensions =
			registry.getConfigurationElementsFor("org.eclipse.ui", "propertyPages");
			
		for (int i = 0; i < propertyPageExtensions.length; i++)
		{
			IConfigurationElement configurationElement = propertyPageExtensions[i];
			String objectClass = configurationElement.getAttribute("objectClass");
			String name = configurationElement.getAttribute("name");
			Class objCls = null;
			try
			{
			    ClassLoader loader = getClass().getClassLoader();
				objCls = Class.forName(objectClass, false, loader);
			}
			catch (Exception e)
			{
			}
			
			
			if (objCls != null && ISubSystem.class.isAssignableFrom(objCls) && isFactoryFor(objCls))			
			{
				try
				{
					PropertyPage page = (PropertyPage) configurationElement.createExecutableExtension("class");
					page.setTitle(name);
					propertyPages.add(page);
				}
				catch (Exception e)
				{
				}
			}
		}
		return propertyPages;
	}



	

// FIXME - compile actions no longer part of core	
//	// ---------------------------------
//	// COMPILE ACTIONS METHODS...
//	// ---------------------------------
//	/**
//	 * Get the singleton compile manager responsible for enabling the compile support
//	 *   for remote source objects.
//	 * <p>
//	 * Do not override this, as the implementation is complete. However,
//	 *  you must override createCompileManager()
//	 * 
//	 * @see #supportsCompileActions()
//	 * @see #createCompileManager()
//	 */
//	public SystemCompileManager getCompileManager()
//	{
//		if (compileManager == null)
//		{
//			compileManager = createCompileManager();
//			if (compileManager != null)
//				compileManager.setSubSystemFactory(this);
//		}
//		return compileManager;
//	}
//
//	/**
//	 * Overridable method to instantiate the SystemCompileManager for this factory.
//	 * This is typically your unique subclass of SystemCompileManager.
//	 * Called once only by getCompileManager (it is only instantiated once).
//	 * 
//	 * @see #supportsCompileActions()
//	 * @see #getCompileManager()
//	 */
//	protected SystemCompileManager createCompileManager()
//	{
//		return null;
//	}

	// ---------------------------------
	// USER-PREFERENCE METHODS...
	// ---------------------------------

	/**
	 * If we support filters, should we show filter pools in the remote system explorer?
	 * By default, this retrieves the setting from user preferences.
	 */
	public boolean showFilterPools()
	{
		return SystemPreferencesManager.getPreferencesManager().getShowFilterPools();
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
			RSEUIPlugin.getTheSystemRegistry().fireEvent(new org.eclipse.rse.model.SystemResourceChangeEvent(ss, ISystemResourceChangeEvents.EVENT_CHANGE_CHILDREN, ss));
		}
	}
	/*
	 * If we support filters, should we show filter strings in the remote system explorer?
	 * This is to set it after the user changes it in the user preferences. It may require
	 *  refreshing the current view.
	 *
	public void setShowFilterStrings(boolean show)
	{
		SubSystem[] subsystems = getSubSystems(false); // false=> lazy get; don't restore from disk if not already
		for (int idx=0; idx<subsystems.length; idx++)
	    {
	    	SubSystem ss = subsystems[idx];     	
		    RSEUIPlugin.getTheSystemRegistry().fireEvent(
		      new com.ibm.etools.systems.model.impl.SystemResourceChangeEvent(
		           ss,ISystemResourceChangeEvent.EVENT_CHANGE_CHILDREN,ss));    	
	    }    	
	}*/

	// ---------------------------------
	// PROXY METHODS. USED INTERNALLY...
	// ---------------------------------

	/**
	 * The following is called for you by the SubSystemFactoryProxy, after
	 *  starting this object via the extension point mechanism
	 */
	public void setSubSystemFactoryProxy(ISubSystemConfigurationProxy proxy)
	{
		this.proxy = proxy;
		//FIXME initMOF();
	}
	/**
	 * The following is here for completeness but should never be needed.
	 */
	public ISubSystemConfigurationProxy getSubSystemFactoryProxy()
	{
		return proxy;
	}

	// ---------------------------------
	// FACTORY ATTRIBUTE METHODS...
	// ---------------------------------

	/**
	 * Return vendor of this factory.
	 * This comes from the xml "vendor" attribute of the extension point.
	 */
	public String getVendor()
	{
		return proxy.getVendor();
	}

	/**
	 * Return name of this factory.
	 * This comes from the xml "name" attribute of the extension point.
	 */
	public String getName()
	{
		return proxy.getName();
	}
	
	/**
	 * Return name of this factory.
	 * This comes from the xml "description" attribute of the extension point.
	 */
	public String getDescription()
	{
		return proxy.getDescription();
	}
	
	/**
	 * Return unique id of this factory.
	 * This comes from the xml "id" attribute of the extension point.
	 */
	public String getId()
	{
		return proxy.getId();
	}
	/**
	 * Return image descriptor of this factory.
	 * This comes from the xml "icon" attribute of the extension point.
	 */
	public ImageDescriptor getImage()
	{
		return proxy.getImage();
	}
	/**
	 * Return actual graphics Image of this factory.
	 * This is the same as calling getImage().createImage() but the resulting
	 *  image is cached.
	 */
	public Image getGraphicsImage()
	{
		ImageDescriptor id = getImage();
		if (id != null)
		{
			Image image = null;
			if (imageTable == null)
				imageTable = new Hashtable();
			else
				image = (Image) imageTable.get(id);
			if (image == null)
			{
				image = id.createImage();
				imageTable.put(id, image);
			}
			return image;
		}
		return null;
	}

	/**
	 * Return image to use when this susystem is connection.
	 * This comes from the xml "iconlive" attribute of the extension point.
	 */
	public ImageDescriptor getLiveImage()
	{
		return proxy.getLiveImage();
	}

	/**
	 * Return actual graphics LiveImage of this factory.
	 * This is the same as calling getLiveImage().createImage() but the resulting
	 *  image is cached.
	 */
	public Image getGraphicsLiveImage()
	{
		ImageDescriptor id = getLiveImage();
		if (id != null)
		{
			Image image = null;
			if (imageTable == null)
				imageTable = new Hashtable();
			else
				image = (Image) imageTable.get(id);
			if (image == null)
			{
				image = id.createImage();
				imageTable.put(id, image);
			}
			return image;
		}
		return null;
	}

	/**
	 * Return the category this subsystem factory subscribes to.
	 * @see org.eclipse.rse.model.ISubSystemFactoryCategories
	 */
	public String getCategory()
	{
		return proxy.getCategory();
	}
	/**
	 * Return the system types this subsystem factory supports.
	 */
	public String[] getSystemTypes()
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
		return SystemProfileManager.getSystemProfileManager().getSystemProfile(name);
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
	public ISystemValidator getUserIdValidator()
	{
//		RSEUIPlugin sp = RSEUIPlugin.getDefault(); DWD - to be removed. Appears to be useless.
		ISystemValidator userIdValidator =
			new ValidatorSpecialChar(
				"=;",
				false,
				RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_VALIDATE_USERID_NOTVALID),
				RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_VALIDATE_USERID_EMPTY));
		// false => allow empty? No.		
		return userIdValidator;
	}
	/**
	 * Return the validator for the password which is prompted for at runtime.
	 * Returns null by default.
	 */
	public ISystemValidator getPasswordValidator()
	{
		return null;
	}
	/**
	 * Return the validator for the port.
	 * A default is supplied.
	 * This must be castable to ICellEditorValidator for the property sheet support.
	 */
	public ISystemValidator getPortValidator()
	{
		ISystemValidator portValidator = new ValidatorPortInput();
		return portValidator;
	}

	/**
	 * Called by SystemRegistry's renameSystemProfile method to ensure we update our
	 *  subsystem names within each subsystem.
	 * <p>
	 * This is called AFTER changing the profile's name!!
	 */
	public void renameSubSystemProfile(ISubSystem ss, String oldProfileName, String newProfileName)
	{
		//RSEUIPlugin.logDebugMessage(this.getClass().getName(), "Inside renameSubSystemProfile. newProfileName = "+newProfileName+", old ssName = "+ss.getName());
		//renameFilterPoolManager(getSystemProfile(newProfileName)); // update filter pool manager name
		ss.renamingProfile(oldProfileName, newProfileName);
		ISystemFilterPoolReferenceManager sfprm = ss.getSystemFilterPoolReferenceManager();
		if (sfprm != null)
		{
		    sfprm.regenerateReferencedSystemFilterPoolNames(); // ask it to re-ask each pool for its reference name
		}
		try
		{
			saveSubSystem(ss);
		}
		catch (Exception exc)
		{
			// already dealt with in save?
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
		for (int idx = 0; idx < subsystems.length; idx++)
		{
			subsystems[idx].renamingConnection(newConnectionName);
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
			try
			{
				subsystems[idx].disconnect((Shell) null); // be nice if we had a shell to pass!
			}
			catch (Exception exc)
			{
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
	 * @param true if we should force all the subsystems to be restored from disk if not already
	 */
	public ISubSystem[] getSubSystems(boolean force)
	{
		if (force && !allSubSystemsRestored)
		{
			// the safest way is to re-use existing method that will restore for every defined connection
			//  in the active profiles (although if user makes another profile active, we'll have to revisit)
			IHost[] allActiveConnections = RSEUIPlugin.getTheSystemRegistry().getHosts();
			if (allActiveConnections != null)
			{
				for (int idx = 0; idx < allActiveConnections.length; idx++)
					if (proxy.appliesToSystemType(allActiveConnections[idx].getSystemType()))
						getSubSystems(allActiveConnections[idx], force); // will load from disk if not already loaded
			}
			allSubSystemsRestored = true;
			subsystems = null; // force re-gen
		}
		if ((subsystems == null) || (subsystems.length != getSubSystemList().size()))
		{
			java.util.List alist = null;
			if (SystemProfileManager.getSystemProfileManager().getSystemProfileNamesVector().size() > 0) // 42913
				alist = getSubSystemList();
			if (alist == null)
				return new ISubSystem[0];
			Iterator i = alist.iterator();
			subsystems = new ISubSystem[alist.size()];
			int idx = 0;
			while (i.hasNext())
			{
				ISubSystem subsys = (ISubSystem) i.next();
				subsystems[idx++] = subsys;
			}
		}
		return subsystems;
	}

	/**
	 * Returns a list of subsystem objects existing for the given connection.
	 * For performance, the calculated array is cached until something changes.
	 * @param conn System connection to retrieve subsystems for
	 * @param true if we should force all the subsystems to be restored from disk if not already
	 */
	public ISubSystem[] getSubSystems(IHost conn, boolean force)
	{
		ISubSystem[] subsystemArray = (ISubSystem[]) subsystemsByConnection.get(conn);
		if (subsystemArray == null || subsystemArray.length ==0)
		{
			//System.out.println("SubSystemFactoryImpl.getSubSystems(conn): subSystemsHaveBeenRestored(conn): "+subSystemsHaveBeenRestored(conn));
			boolean subsystemsRestored = subSystemsHaveBeenRestored(conn);
			if (!subsystemsRestored && force)
			{
				/*FIXME - this should now be triggered by new persistence model
				try
				{
					//System.out.println("SubSystemFactoryImpl.getSubSystems(conn): before restoreSubSystems");
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
		java.util.List mofList = getSubSystemList();
		Iterator i = mofList.iterator();
		Vector v = new Vector();
//		String connProfileName = conn.getSystemProfile().getName(); DWD - to be removed, appears to be useless.
//		String connAliasName = conn.getAliasName(); DWD - to be removed.
		while (i.hasNext())
		{
			ISubSystem subsys = (ISubSystem) i.next();
			if (subsys.getHost() == conn)
				v.addElement(subsys);
		}
		ISubSystem[] array = new ISubSystem[v.size()];
		for (int idx = 0; idx < v.size(); idx++)
			array[idx] = (ISubSystem) v.elementAt(idx);
		return array;
	}
	/**
	 * Returns a list of subsystem objects existing for all the connections in the
	 *  given profile. Will force restoring all subsystems from disk.
	 */
	public ISubSystem[] getSubSystems(ISystemProfile profile)
	{
//		String profileName = profile.getName(); DWD - to be removed.
		ISubSystem[] allSubSystems = getSubSystems(true);
		Vector v = new Vector();
		for (int idx = 0; idx < allSubSystems.length; idx++)
		{
			ISubSystem ss = allSubSystems[idx];
			if (ss.getSystemProfile() == profile)
				v.addElement(ss);
		}
		ISubSystem[] subsystems = new ISubSystem[v.size()];
		for (int idx = 0; idx < v.size(); idx++)
			subsystems[idx] = (ISubSystem) v.elementAt(idx);
		return subsystems;
	}
	/**
	 * Returns a list of subsystem objects existing in memory,
	 * which contain a reference to the given filter pool.
	 */
	public ISubSystem[] getSubSystems(ISystemFilterPool pool)
	{
		ISubSystem[] allSubSystems = getSubSystems(false); // // false=> lazy get; don't restore from disk if not already
		Vector v = new Vector();
		for (int idx = 0; idx < allSubSystems.length; idx++)
		{
			ISystemFilterPoolReferenceManager mgr = subsystems[idx].getSystemFilterPoolReferenceManager();
			if ((mgr != null) && (mgr.isSystemFilterPoolReferenced(pool)))
			{
				v.addElement(allSubSystems[idx]);
			}
		}
		ISubSystem[] subsystems = new ISubSystem[v.size()];
		for (int idx = 0; idx < v.size(); idx++)
			subsystems[idx] = (ISubSystem) v.elementAt(idx);
		return subsystems;
	}

	/**
	 * Helper method to allow child classes to add a subsystem object to the in-memory
	 *  list maintained and returned by this base class.
	 */
	protected void addSubSystem(ISubSystem subsys)
	{
		getSubSystemList().add(subsys);
	}

	/**
	 * Helper method to allow child classes to remove a subsystem object from the in-memory
	 *  list maintained and returned by this base class.
	 */
	protected void removeSubSystem(ISubSystem subsys)
	{
		getSubSystemList().remove(subsys);
		/* FIXME
		// now in EMF, the profiles are "owned" by the Resource, and only referenced by the profile manager,
		//  so I don't think just removing it from the manager is enough... it must also be removed from its
		//  resource. Phil.
		Resource res = subsys.eResource();
		if (res != null)
			res.getContents().remove(subsys);
			*/
	}

	/**
	 * Creates a new subsystem instance that is associated with the given connection object.
	 * SystemRegistryImpl calls this when a new connection is created, and appliesToSystemType returns true.
	 * <p>
	 * This method doe sthe following:
	 * <ul>
	 *   <li>calls {@link #createSubSystemInternal(IHost)} to create the subsystem 
	 *   <li>does initialization of common attributes
	 *   <li>if {@link #supportsFilters()}, creates a {@link org.eclipse.rse.filters.ISystemFilterPoolReferenceManager} for the
	 *           subsystem to manage references to filter pools
	 *   <li>if (@link #supportsServerLaunchProperties()}, calls {@link #createServerLauncher(ISubSystem)}, to create
	 *           the server launcher instance to associate with this subsystem. This can be subsequently
	 *           retrieved via calling subsystem's {@link ISubSystem#getRemoteServerLauncher()}.
	 *   <li>calls {@link #initializeSubSystem(ISubSystem, ISystemNewConnectionWizardPage[])} so subclasses can
	 *           do their thing to initialize the subsystem.
	 *   <li>finally, saves the subsystem to disk.
	 * </ul>
	 * @param conn The connection to create a subsystem for
	 * @param creatingConnection true if we are creating a connection, false if just creating
	 *          another subsystem for an existing connection.
	 * @param yourNewConnectionWizardPages The wizard pages you supplied to the New Connection wizard, via the
	 *            {@link #getNewConnectionWizardPages(IWizard)} method or null if you didn't override this method.
	 *            Note there may be more pages than you originally supplied as it is all pages contributed by 
	 *            this factory object, including subclasses.
	 */
	public ISubSystem createSubSystem(IHost conn, boolean creatingConnection, ISystemNewConnectionWizardPage[] yourNewConnectionWizardPages)
	{
		invalidateSubSystemCache(conn); // re-gen list of subsystems-by-connection on next call
		if (creatingConnection)
			subSystemsRestoredFlags.put(conn, Boolean.TRUE); // do not try to restore subsequently. Nothing to restore!
		ISubSystem subsys = createSubSystemInternal(conn);
		if (subsys != null)
		{
			internalInitializeNewSubsystem(subsys, conn);
			if (supportsFilters())
			{
				// We create a filter pool reference manager object to manage the filter pool references
				// that are stored with a subsystem.
				//SystemFilterPoolManager[] relatedFilterPoolManagers =
				//  getReferencableFilterPoolManagers(conn.getSystemProfile());
				ISystemFilterPoolReferenceManager fprMgr = SystemFilterStartHere.createSystemFilterPoolReferenceManager(subsys, this, subsys.getName(), filterNamingPolicy);
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
			initializeSubSystem(subsys, yourNewConnectionWizardPages);
			try
			{
				saveSubSystem(subsys);  
				//DKM - save this event til all the processing is done!
				// fire model change event in case any BP code is listening...
				//RSEUIPlugin.getTheSystemRegistry().fireModelChangeEvent(ISystemModelChangeEvents.SYSTEM_RESOURCE_ADDED, ISystemModelChangeEvents.SYSTEM_RESOURCETYPE_SUBSYSTEM, subsys, null);						
			}
			catch (Exception exc)
			{
				SystemBasePlugin.logError("Error saving new subsystem " + subsys.getName(), exc);
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
			internalInitializeNewSubsystem(subsys, newConnection);
			// copy common data
			subsys.setName(oldSubsystem.getName()); // just in case it was changed
			subsys.addPropertySets(oldSubsystem.getPropertySets());	
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
			IServerLauncherProperties sl = oldConnectorService.getRemoteServerLauncherProperties();
			if ((sl != null) && supportsServerLaunchProperties(newConnection))
			{
				IServerLauncherProperties newSL = createServerLauncher(newConnectorService);
				if (newSL != null)					
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
				ISystemFilterPoolReferenceManager newRefMgr = SystemFilterStartHere.createSystemFilterPoolReferenceManager(subsys, this, subsys.getName(), filterNamingPolicy);
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
				newRefMgr.setProviderEventNotification(true);
			}
			try
			{
				saveSubSystem(subsys);

				// fire model change event in case any BP code is listening...
				RSEUIPlugin.getTheSystemRegistry().fireModelChangeEvent(ISystemModelChangeEvents.SYSTEM_RESOURCE_ADDED, ISystemModelChangeEvents.SYSTEM_RESOURCETYPE_SUBSYSTEM, subsys, null);						
			}
			catch (Exception exc)
			{
				lastExc = exc;
				SystemBasePlugin.logError("Error saving cloned subsystem " + subsys.getName(), exc);
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
	private void internalInitializeNewSubsystem(ISubSystem subsys, IHost conn)
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
	 *  factory object. This is fine, unless you support multiple subsystem instances per
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
	 * Method called by default implementation of createSubSystem method in AbstractSubSystemFactory.
	 */
	public abstract ISubSystem createSubSystemInternal(IHost conn);

	/**
	 * <i>Overridable</i> method to initialize subsystems after creation. The default behaviour here is to
	 *  set the subsystem's port property to 0, and to add to it a reference to the default filter pool for this
	 *  factory, if there is one. Typically subclasses call <samp>super().initializeSubSystem(...)</samp>
	 *  to get this default behaviour, then extend it.
	 * 
	 * <p>The reason for the connect wizard pages parm is in case your factory contributes a page to that wizard,
	 * whose values are needed to set the subsystem's initial state. For example, you might decide to add a 
	 * page to the connection wizard to prompt for a JDBC Driver name. If so, when this method is called at 
	 * the time a new connection is created apres the wizard, your page will have the user's value. You can
	 * thus use it here to initialize that subsystem property. Be use to use instanceof to find your particular
	 * page. 
	 * </p>
	 * 
	 * @param ss - The subsystem that was created via createSubSystemInternal
	 * @param yourNewConnectionWizardPages - The wizard pages you supplied to the New Connection wizard, via the
	 *            {@link #getNewConnectionWizardPages(IWizard)} method or null if you didn't override this method.
	 *            Note there may be more pages than you originally supplied, as you are passed all pages contributed
	 *            by this factory object, including subclasses. This is null when this method is called other than
	 *            for a New Connection operation.
	 */
	protected void initializeSubSystem(ISubSystem ss, ISystemNewConnectionWizardPage[] yourNewConnectionWizardPages)
	{
		IConnectorService connectorService = ss.getConnectorService();
		if (connectorService != null)
		{
			connectorService.setPort(0);
		}
		if (supportsFilters())
		{
			// --------------------------------------------
			// add a reference to the default filter pool
			// --------------------------------------------    	
			ISystemFilterPool pool = getDefaultSystemFilterPool(ss);
			if (pool != null)
			{
				ISystemFilterPoolReferenceManager refMgr = ss.getSystemFilterPoolReferenceManager();
				refMgr.setProviderEventNotification(false);
				refMgr.addReferenceToSystemFilterPool(pool);
				refMgr.setProviderEventNotification(true);
			}
		}
		
		// apply properties set in the wizard to the subsystem
	    if (yourNewConnectionWizardPages != null)
	    {
	    	ISubSystemPropertiesWizardPage ourPage = null;
	    	for (int idx=0; (ourPage==null) && (idx<yourNewConnectionWizardPages.length); idx++)
	    	{
	    	   if (yourNewConnectionWizardPages[idx] instanceof ISubSystemPropertiesWizardPage)
	    	   {
	    	     ourPage = (ISubSystemPropertiesWizardPage)yourNewConnectionWizardPages[idx];
	    	     ourPage.applyValues(ss);
	    	   }
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
	 * Updates user-editable attributes of an existing subsystem instance.
	 * These attributes typically affect the live connection, so the subsystem will be forced to
	 *  disconnect.
	 * <p>
	 * If you have your own attributes and own GUI to prompt for these, then call your own
	 * method to set your attributes, and call this method via super().xxx(...).
	 * <p>
	 * The subsystem will be saved to disk.
	 * Further, it will be asked to disconnect as this data affects the connection.
	 * <p>
	 * @param shell parent shell needed in case an error message is displayed
	 * @param subsystem target of the update action
	 * @param updateUserId true if we are updating the userId, else false to ignore userId
	 * @param userId new local user Id. Ignored if updateUserId is false
	 * @param updatePort true if we are updating the port, else false to ignore port
	 * @param port new local port value. Ignored if updatePort is false
	 */
	public void updateSubSystem(Shell shell, ISubSystem subsystem, boolean updateUserId, String userId, boolean updatePort, int port)
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
		}
		else
		{
			
		}

		// inform interested listeners...
		fireEvent(subsystem, ISystemResourceChangeEvents.EVENT_PROPERTY_CHANGE, subsystem.getHost());

		// fire model change event in case any BP code is listening...
		RSEUIPlugin.getTheSystemRegistry().fireModelChangeEvent(ISystemModelChangeEvents.SYSTEM_RESOURCE_CHANGED, ISystemModelChangeEvents.SYSTEM_RESOURCETYPE_SUBSYSTEM, subsystem, null);						

		// if the updated subsystem is one of many that share a single ISystem, then 
		// update all of them too...
		// DKM - now that ConnectorService is independent of subsystme, this should be unnecessary
	/*	AbstractConnectorServiceManager systemManager = subsystem.getConnectorService();
		if (systemManager != null)
			systemManager.updateSubSystems(shell, subsystem, updateUserId, userId, updatePort, port);*/
	}
	/**
	 * Update the port for the given subsystem instance.
	 * Shortcut to {@link #updateSubSystem(Shell,ISubSystem,boolean,String,boolean,Integer)}
	 */
	public void setSubSystemPort(Shell shell, ISubSystem subsystem, int port)
	{
		updateSubSystem(shell, subsystem, false, null, true, port);
	}
	/**
	 * Update the user ID for the given subsystem instance.
	 * Shortcut to {@link #updateSubSystem(Shell,ISubSystem,boolean,String,boolean,Integer)}
	 */
	public void setSubSystemUserId(Shell shell, ISubSystem subsystem, String userId)
	{
		updateSubSystem(shell, subsystem, true, userId, false, 0);
	}
		
	/**
	 * Used by child classes that override updateSubSystem to establish if anything really 
	 *  needs to be changed.
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
	 * Returns true if this factory allows users to delete instances of subsystem objects.
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
	 * Deletes a given subsystem instance from the list maintained by this factory.
	 * SystemRegistryImpl calls this when the user selects to delete a subsystem object,
	 *  or deletes the parent connection this subsystem is associated with.
	 * <p>
	 * In former case, this is only called if the factory supports user-deletable subsystems.
	 * <p>
	 * Handled for you!
	 */
	public boolean deleteSubSystem(ISubSystem subsystem)
	{
		try
		{
			subsystem.disconnect((Shell) null); // just in case.
		}
		catch (Exception exc)
		{
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
		RSEUIPlugin.getTheSystemRegistry().fireModelChangeEvent(ISystemModelChangeEvents.SYSTEM_RESOURCE_REMOVED, ISystemModelChangeEvents.SYSTEM_RESOURCETYPE_SUBSYSTEM, subsystem, null);						
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
	 * There is a reasonable amount of processing needed to configure filter wizards. To aid
	 *  in performance and memory usage, we extract that processing into this method, and then
	 *  use a callback contract with the filter wizard to call us back to do this processing 
	 *  only at the time the action is actually selected to be run.
	 * <p>
	 * The processing we do here is to specify the filter pools to prompt the user for, in the
	 *  second page of the New Filter wizards.
	 * <p>
	 * This method is from the ISystemNewFilterActionConfigurator interface
	 */
	public void configureNewFilterAction(SystemNewFilterAction newFilterAction, Object callerData)
	{
		//System.out.println("Inside configureNewFilterAction! It worked!");
		newFilterAction.setFromRSE(true);
		boolean showFilterPools = showFilterPools();
		
		// It does not make sense, when invoked from a filterPool, to ask the user
		//  for the parent filter pool, or to ask the user whether the filter is connection
		//  specific, as they user has explicitly chosen their pool...
		//if (!showFilterPools || (callerData instanceof SubSystem))
		if (!showFilterPools)
		{
			ISubSystem selectedSubSystem = (ISubSystem) callerData;
			// When not showing filter pools, we need to distinquish between an advanced user and a new user.
			// For a new user we simply want to ask them whether this filter is to be team sharable or private,
			//  and based on that, we will place the filter in the default filter pool for the appropriate profile.
			// For an advanced user who has simply turned show filter pools back off, we want to let them choose
			//  explicitly which filter pool they want to place the filter in. 
			// To approximate the decision, we will define an advanced user as someone who already has a reference
			//  to a filter pool other than the default pools in the active profiles.
			boolean advancedUser = false;
			ISystemFilterPoolReferenceManager refMgr = selectedSubSystem.getSystemFilterPoolReferenceManager();
			ISystemFilterPool[] refdPools = refMgr.getReferencedSystemFilterPools();
			if (refdPools.length == 0)
				SystemBasePlugin.logInfo("SubSystemFactoryImpl::getSubSystemActions - getReferencedSystemFilterPools returned array of length zero.");
			// so there already exists references to more than one filter pool, but it might simply be a reference
			//  to the default filter pool in the user's profile and another to reference to the default filter pool in
			//  the team profile... let's see...
			else if (refdPools.length > 1)
			{
				for (int idx = 0; !advancedUser && (idx < refdPools.length); idx++)
				{
					if (!refdPools[idx].isDefault() && (refdPools[idx].getOwningParentName()==null))
						advancedUser = true;
				}
			}
			if (advancedUser)
			{
				newFilterAction.setAllowFilterPoolSelection(refdPools); // show all pools referenced in this subsystem, and let them choose one
			}
			else
			{
				boolean anyAdded = false;
				SystemFilterPoolWrapperInformation poolWrapperInfo = getNewFilterWizardPoolWrapperInformation();
				ISystemProfile[] activeProfiles = RSEUIPlugin.getTheSystemRegistry().getActiveSystemProfiles();
				ISystemProfile activeProfile = selectedSubSystem.getHost().getSystemProfile();
				for (int idx = 0; idx < activeProfiles.length; idx++)
				{
					ISystemFilterPool defaultPool = getDefaultSystemFilterPool(activeProfiles[idx]);
					if (defaultPool != null)
					{
						poolWrapperInfo.addWrapper(activeProfiles[idx].getName(), defaultPool, (activeProfiles[idx] == activeProfile)); // display name, pool to wrap, whether to preselect
						anyAdded = true;
					}
				}
				if (anyAdded)
					newFilterAction.setAllowFilterPoolSelection(poolWrapperInfo);
			}
		}
	}
	/**
	 * Overridable entry for child classes to supply their own flavour of ISystemFilterPoolWrapperInformation for
	 *  the new filter wizards.
	 */
	protected SystemFilterPoolWrapperInformation getNewFilterWizardPoolWrapperInformation()
	{
		return new SystemFilterPoolWrapperInformation(SystemResources.RESID_NEWFILTER_PAGE2_PROFILE_LABEL, SystemResources.RESID_NEWFILTER_PAGE2_PROFILE_TOOLTIP, 
				SystemResources.RESID_NEWFILTER_PAGE2_PROFILE_VERBAGE);
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
	 * OVERRIDABLE METHOD FOR CHILD CLASSES TO ENABLE THEM TO CREATE A DEFAULT POOL
	 * WHENEVER A NEW FILTER POOL MANAGER IS CREATED (EG, WHEN PROFILE CREATED).
	 * <p>
	 * You should only pre-populate your default filter pool if this new manager
	 * (eg, its really a profile) is the user's private own. Call {@link #isUserPrivateProfile(ISystemFilterPoolManager)}
	 * to find out if it is.
	 */
	protected abstract ISystemFilterPool createDefaultFilterPool(ISystemFilterPoolManager mgr);

	/**
	 * Return true if the given filter pool manager maps to the private profile for this user.
	 */
	protected boolean isUserPrivateProfile(ISystemFilterPoolManager mgr)
	{
		//System.out.println("mgr name = " + mgr.getName());
		//String name = mgr.getName();
		//return name.equalsIgnoreCase("private");
		ISystemProfile profile = getSystemProfile(mgr);
		//System.out.println("Testing for user private profile for mgr " + mgr.getName() + ": " + profile.isDefaultPrivate());;
		return profile.isDefaultPrivate() || mgr.getName().equalsIgnoreCase("private");
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
	 * Return an array of all filter pool managers owned by this subsystem factory.
	 * This is a runtime array that only captures those filter pools that have been restored
	 *  as a result of someone calling getFilterPoolManager(SystemProfile).
	 */
	public ISystemFilterPoolManager[] getFilterPoolManagers()
	{
		if ((filterPoolManagers == null) || (filterPoolManagers.length != getFilterPoolManagerList().size()))
		{
			filterPoolManagers = new ISystemFilterPoolManager[getFilterPoolManagerList().size()];
			Iterator i = getFilterPoolManagerList().iterator();
			int idx = 0;
			while (i.hasNext())
				filterPoolManagers[idx++] = (ISystemFilterPoolManager) i.next();
		}
		return filterPoolManagers;
	}

	/**
	 * Get the filter pool managers for the active profiles.
	 */
	public ISystemFilterPoolManager[] getActiveFilterPoolManagers()
	{
		ISystemProfile[] activeProfiles = RSEUIPlugin.getTheSystemRegistry().getActiveSystemProfiles();
		ISystemFilterPoolManager[] activeManagers = new ISystemFilterPoolManager[activeProfiles.length];
		for (int idx = 0; idx < activeProfiles.length; idx++)
		{
			activeManagers[idx] = getFilterPoolManager(activeProfiles[idx]);
		}
		return activeManagers;
	}

	/**
	 * Get the filter pool manager for the given profile
	 */
	public ISystemFilterPoolManager getFilterPoolManager(ISystemProfile profile)
	{
		// it is important to key by profile object not profile name, since that
		//   name can change but the object never should for any one session.
		ISystemFilterPoolManager mgr = (ISystemFilterPoolManager) filterPoolManagersPerProfile.get(profile);
		//System.out.println("in getFilterPoolManager for ssfactory "+getId()+" for profile " + profile.getName() + ", mgr found? " + (mgr!=null));
		if (mgr == null)
		{
			try
			{
				mgr = SystemFilterPoolManager.createSystemFilterPoolManager(profile, RSEUIPlugin.getDefault().getLogger(), this, // the caller
							getFilterPoolManagerName(profile), // the filter pool manager name
							supportsNestedFilters(), // whether or not nested filters are allowed
							ISystemFilterSavePolicies.SAVE_POLICY_ONE_FILE_PER_FILTER, filterNamingPolicy);
				mgr.setSingleFilterStringOnly(!supportsMultipleFilterStrings());
			}
			catch (Exception exc)
			{
				SystemBasePlugin.logError("Restore/Creation of SystemFilterPoolManager " + getFilterPoolManagerName(profile) + " failed!", exc);
				SystemMessageDialog.displayExceptionMessage(null, exc);
				return null; // something very bad happend!           	  
			}
			if (mgr == null)
			{
				SystemBasePlugin.logError("Restore/Creation of SystemFilterPoolManager " + getFilterPoolManagerName(profile) + " failed!", null);
				return null; // something very bad happend!
			}

			addFilterPoolManager(profile, mgr);

			boolean restored = mgr.wasRestored();
			//System.out.println("...after createSystemFilterPoolManager for " + mgr.getName() + ", restored = " + restored);

			// allow subclasses to create default filter pool...
			if (!restored)
			{
				ISystemFilterPool defaultPool = createDefaultFilterPool(mgr);
				if (defaultPool != null)
				{
					defaultPool.setDefault(true);
					try
					{
						RSEUIPlugin.getThePersistenceManager().commit(defaultPool);
					}
					catch (Exception exc)
					{
					}
				}
			}
			// else filter pools restored for this profile. Allow subclasses chance to do post-processing,
			// such as any migration needed
			else
			{
				if (doPostRestoreProcessing(mgr))
				{
					try
					{
						mgr.commit();
					}
					catch (Exception exc)
					{
					}
				}
			}
			// these should be inside the above logic but we need them outside for now because they were
			//  added late and there are existing filter pool managers that need this to be set for.
			//  In a future release we should move them inside the if (!restored) logic. Phil.
			if (supportsDuplicateFilterStrings())
				mgr.setSupportsDuplicateFilterStrings(true);
			if (isCaseSensitive())
				mgr.setStringsCaseSensitive(isCaseSensitive());
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

			ISystemFilterPoolManager mgr = SystemFilterPoolManager.createSystemFilterPoolManager(newProfile, RSEUIPlugin.getDefault().getLogger(), this, // the caller
		getFilterPoolManagerName(newProfile), // the filter pool manager name
		supportsNestedFilters(), // whether or not nested filters are allowed
	ISystemFilterSavePolicies.SAVE_POLICY_ONE_FILE_PER_FILTER, filterNamingPolicy);
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
		ISystemRegistry sr = RSEUIPlugin.getTheSystemRegistry();
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
				String connectionName = conn.getSystemProfileName() + "." + conn.getAliasName();
				SystemMessage sysMsg = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_LOADING_PROFILE_SHOULDBE_ACTIVATED);
				sysMsg.makeSubstitution(missingPoolMgrName, connectionName);
				SystemBasePlugin.logWarning(sysMsg.getFullMessageID() + ": " + sysMsg.getLevelOneText());
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
		filterPoolManagersPerProfile.put(profile, mgr);
		getFilterPoolManagerList().add(mgr); // MOF generated list
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
			filterPoolManagersPerProfile.remove(profile);
		}
		getFilterPoolManagerList().remove(mgr);
		invalidateFilterCache();
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
		RSEUIPlugin.getTheSystemRegistry().fireEvent(event);
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
				// CASE 1: FILTER IS NOT NESTED, SO SIMPLY GET ITS FILTER POOL REFERENCE AND USE AS A PARENT...
				if (!nested)
				{
					// SPECIAL CASE 1A: it makes a difference if we are showing filter pools or not...
					if (showFilterPools())
						event.setParent(subsystems[idx].getSystemFilterPoolReferenceManager().getReferenceToSystemFilterPool(pool));
					else
						event.setParent(subsystems[idx]);
					fireSubSystemEvent(event, subsystems[idx]);
				}
				// CASE 2: FILTER IS NESTED, THIS IS MORE DIFFICULT, AS EVERY FILTER CONTAINS A RANDOMLY
				//          GENERATED REFERENCE THAT ONLY THE GUI KNOWS ABOUT.
				//         ONLY OPTION IS TO LET THE GUI FIGURE IT OUT.
				else
				{
					event.setParent(nestedParentFilter);
					fireSubSystemEvent(event, subsystems[idx]);
				}
			}
		}
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
		RSEUIPlugin.getTheSystemRegistry().fireModelChangeEvent(ISystemModelChangeEvents.SYSTEM_RESOURCE_ADDED, ISystemModelChangeEvents.SYSTEM_RESOURCETYPE_FILTERPOOL, newPool, null);		
	}
	/**
	 * A filter pool has been deleted
	 */
	public void filterEventFilterPoolDeleted(ISystemFilterPool oldPool)
	{
		//fireEvent(oldPool, EVENT_DELETE, this); currently called by SystemView's delete support

		// fire model change event in case any BP code is listening...
		RSEUIPlugin.getTheSystemRegistry().fireModelChangeEvent(ISystemModelChangeEvents.SYSTEM_RESOURCE_REMOVED, ISystemModelChangeEvents.SYSTEM_RESOURCETYPE_FILTERPOOL, oldPool, null);		
	}
	/**
	 * A filter pool has been renamed
	 */
	public void filterEventFilterPoolRenamed(ISystemFilterPool pool, String oldName)
	{
		//fireEvent(pool, EVENT_RENAME, this); subsystem handles in firing of reference rename

		// fire model change event in case any BP code is listening...
		RSEUIPlugin.getTheSystemRegistry().fireModelChangeEvent(ISystemModelChangeEvents.SYSTEM_RESOURCE_RENAMED, ISystemModelChangeEvents.SYSTEM_RESOURCETYPE_FILTERPOOL, pool, oldName);		
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
				RSEUIPlugin.getTheSystemRegistry().fireModelChangeEvent(ISystemModelChangeEvents.SYSTEM_RESOURCE_REORDERED, ISystemModelChangeEvents.SYSTEM_RESOURCETYPE_FILTERPOOL, pools[idx], null);		
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
		RSEUIPlugin.getTheSystemRegistry().fireModelChangeEvent(ISystemModelChangeEvents.SYSTEM_RESOURCE_ADDED, ISystemModelChangeEvents.SYSTEM_RESOURCETYPE_FILTER, newFilter, null);		
	}

	/**
	 * A filter has been deleted
	 */
	public void filterEventFilterDeleted(ISystemFilter oldFilter)
	{
		fireSubSystemFilterEvent(ISystemResourceChangeEvents.EVENT_DELETE_FILTER_REFERENCE, oldFilter);

		// fire model change event in case any BP code is listening...
		RSEUIPlugin.getTheSystemRegistry().fireModelChangeEvent(ISystemModelChangeEvents.SYSTEM_RESOURCE_REMOVED, ISystemModelChangeEvents.SYSTEM_RESOURCETYPE_FILTER, oldFilter, null);		
	}

	/**
	 * A filter has been renamed
	 */
	public void filterEventFilterRenamed(ISystemFilter filter, String oldName)
	{
		fireSubSystemFilterEvent(ISystemResourceChangeEvents.EVENT_RENAME_FILTER_REFERENCE, filter);

		// fire model change event in case any BP code is listening...
		RSEUIPlugin.getTheSystemRegistry().fireModelChangeEvent(ISystemModelChangeEvents.SYSTEM_RESOURCE_RENAMED, ISystemModelChangeEvents.SYSTEM_RESOURCETYPE_FILTER, filter, oldName);		
	}

	/**
	 * A filter's strings have been updated
	 */
	public void filterEventFilterUpdated(ISystemFilter filter)
	{
		fireSubSystemFilterEvent(ISystemResourceChangeEvents.EVENT_CHANGE_FILTER_REFERENCE, filter);

		// fire model change event in case any BP code is listening...
		RSEUIPlugin.getTheSystemRegistry().fireModelChangeEvent(ISystemModelChangeEvents.SYSTEM_RESOURCE_CHANGED, ISystemModelChangeEvents.SYSTEM_RESOURCETYPE_FILTER, filter, null);		
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
				RSEUIPlugin.getTheSystemRegistry().fireModelChangeEvent(ISystemModelChangeEvents.SYSTEM_RESOURCE_REORDERED, ISystemModelChangeEvents.SYSTEM_RESOURCETYPE_FILTER, filters[idx], null);		

		}
		//System.out.println("In SubSystemFactoryImpl#filterEventFiltersRepositioned(). Firing EVENT_MOVE_FILTER_REFERENCES");
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
		RSEUIPlugin.getTheSystemRegistry().fireModelChangeEvent(ISystemModelChangeEvents.SYSTEM_RESOURCE_CHANGED, ISystemModelChangeEvents.SYSTEM_RESOURCETYPE_FILTER, newFilterString.getParentSystemFilter(), null);		
	}
	/**
	 * A filter string has been deleted
	 */
	public void filterEventFilterStringDeleted(ISystemFilterString oldFilterString)
	{
		fireSubSystemFilterEvent(ISystemResourceChangeEvents.EVENT_DELETE_FILTERSTRING_REFERENCE, oldFilterString);
		// fire model change event in case any BP code is listening...
		RSEUIPlugin.getTheSystemRegistry().fireModelChangeEvent(ISystemModelChangeEvents.SYSTEM_RESOURCE_CHANGED, ISystemModelChangeEvents.SYSTEM_RESOURCETYPE_FILTER, oldFilterString.getParentSystemFilter(), null);		
	}
	/**
	 * A filter string has been updated
	 */
	public void filterEventFilterStringUpdated(ISystemFilterString filterString)
	{
		fireSubSystemFilterEvent(ISystemResourceChangeEvents.EVENT_CHANGE_FILTERSTRING_REFERENCE, filterString);
		// fire model change event in case any BP code is listening...
		RSEUIPlugin.getTheSystemRegistry().fireModelChangeEvent(ISystemModelChangeEvents.SYSTEM_RESOURCE_CHANGED, ISystemModelChangeEvents.SYSTEM_RESOURCETYPE_FILTER, filterString.getParentSystemFilter(), null);		
	}
	/**
	 * One or more filters have been re-ordered within their filter
	 */
	public void filterEventFilterStringsRePositioned(ISystemFilterString[] filterStrings, int delta)
	{
		fireSubSystemFilterEvent(ISystemResourceChangeEvents.EVENT_MOVE_FILTERSTRING_REFERENCES, filterStrings, delta);
		// fire model change event in case any BP code is listening...
		if ((filterStrings!=null) && (filterStrings.length>0))
			RSEUIPlugin.getTheSystemRegistry().fireModelChangeEvent(ISystemModelChangeEvents.SYSTEM_RESOURCE_CHANGED, ISystemModelChangeEvents.SYSTEM_RESOURCETYPE_FILTER, filterStrings[0].getParentSystemFilter(), null);		
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
		Vector v = new Vector();
		ISystemProfileManager profileMgr = SystemProfileManager.getSystemProfileManager();
		ISystemFilterPoolManager sfpm = getFilterPoolManager(profile);
		String profileName = profile.getName();
		if (sfpm != null)
		{
			ISystemFilterPool[] pools = sfpm.getSystemFilterPools();
			if ((pools != null) && (pools.length > 0))
			{
				for (int idx = 0; idx < pools.length; idx++)
				{
					ISystemBaseReferencingObject[] refs = pools[idx].getReferencingObjects();
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
									v.addElement(subsystem);
								}
							}
						}
					}
				}
			}
		}

		ISubSystem[] referencingSubSystems = null;
		if (v.size() > 0)
		{
			referencingSubSystems = new ISubSystem[v.size()];
			for (int idx = 0; idx < referencingSubSystems.length; idx++)
				referencingSubSystems[idx] = (ISubSystem) v.elementAt(idx);
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
	 * When a subsystem is created, and {@link #supportsServerLaunchProperties()}
	 * returns true, this method is called to create the server launcher instance
	 * associated with the subsystem. The default implementation is to create an
	 * instance of {@link IIBMServerLauncher}, but override to create your own 
	 * ServerLauncher instance if you have your own class.
	 */
	public IServerLauncherProperties createServerLauncher(IConnectorService connectorService)
	{
		IIBMServerLauncher sl = new IBMServerLauncher("IBM Server Launcher", connectorService);
		String systemType = connectorService.getHostType();
		
		if (systemType.equals(IRSESystemType.SYSTEMTYPE_LINUX) ||
				systemType.equals(IRSESystemType.SYSTEMTYPE_POWER_LINUX) ||
				systemType.equals(IRSESystemType.SYSTEMTYPE_ZSERIES_LINUX)) {
			sl.setServerScript(IBMServerLauncherConstants.LINUX_REXEC_SCRIPT);
		}
		else if (systemType.equals(IRSESystemType.SYSTEMTYPE_UNIX) ||
				systemType.equals(IRSESystemType.SYSTEMTYPE_AIX)) {
			sl.setServerScript(IBMServerLauncherConstants.UNIX_REXEC_SCRIPT);
		}
		
		sl.saveToProperties();
		return sl;
	}
	/**
	 * Return the form used in the property page, etc for this server launcher.
	 * Only called if {@link #supportsServerLaunchProperties()} returns true. 
	 * <p>
	 * We return {@link org.eclipse.rse.ui.widgets.ServerLauncherForm}.
	 * Override if appropriate.
	 */
	public IServerLauncherForm getServerLauncherForm(Shell shell, ISystemMessageLine msgLine)
	{
		return new IBMServerLauncherForm(shell, msgLine);
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
			handleException("Exception saving filter pools for manager " + mgr.getName(), exc);
			throw exc;
		}
	}

	// used in the case where newsubsystems are added after a connection exists
	public ISubSystem createSubsystemAfterTheFact(IHost conn)
	{
		ISubSystem subsys = createSubSystemInternal(conn);
		if (subsys != null)
		{
			internalInitializeNewSubsystem(subsys, conn);
			if (supportsFilters())
			{
				// We create a filter pool reference manager object to manage the filter pool references
				// that are stored with a subsystem.
				//SystemFilterPoolManager[] relatedFilterPoolManagers =
				//  getReferencableFilterPoolManagers(conn.getSystemProfile());
				ISystemFilterPoolReferenceManager fprMgr = SystemFilterStartHere.createSystemFilterPoolReferenceManager(subsys, this, subsys.getName(), filterNamingPolicy);
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
				SystemBasePlugin.logError("Error saving new subsystem " + subsys.getName(), exc);
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
	 * Why do this? Because we need to in order to allow cross references from
	 * one subsystem in one profile to filter pools in any other profile.
	 */
	public ISystemFilterPoolManager[] restoreAllFilterPoolManagersForAllProfiles()
	{
		ISystemProfile[] profiles = SystemStartHere.getSystemProfileManager().getSystemProfiles();
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
		SystemMessage msg = null;
		if (port > 0)
		{
			msg = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_CONNECTWITHPORT_PROGRESS);
			msg.makeSubstitution(hostName, Integer.toString(port));
		}
		else
		{
			msg = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_CONNECT_PROGRESS);
			msg.makeSubstitution(hostName);
		}
		return msg.getLevelOneText();
	}
	/**
	 * Helper method to return the message "Disconnecting from &1..."
	 */
	public static String getDisconnectingMessage(String hostName, int port)
	{
		SystemMessage msg = null;
		if (port > 0)
		{
			msg = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_DISCONNECTWITHPORT_PROGRESS);
			msg.makeSubstitution(hostName, Integer.toString(port));
		}
		else
		{
			msg = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_DISCONNECT_PROGRESS);
			msg.makeSubstitution(hostName);
		}
		return msg.getLevelOneText();
	}

	/**
	 * Return the translated name of a default filter pool for a given profile
	 */
	public static String getDefaultFilterPoolName(String profileName, String factoryId)
	{
		StringBuffer nameBuf = new StringBuffer();
		nameBuf.append(profileName);
		nameBuf.append(":");
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

	/**
	 * @generated This field/method will be replaced during code generation 
	 */
	public java.util.List getSubSystemList()
	{
		if (subSystemList == null)
		{
			subSystemList = new ArrayList();
			//FIXME new EObjectResolvingeList(SubSystem.class, this, SubsystemsPackage.SUB_SYSTEM_FACTORY__SUB_SYSTEM_LIST);
		}
		return subSystemList;
	}

	/**
	 * @generated This field/method will be replaced during code generation 
	 */
	public java.util.List getFilterPoolManagerList()
	{
		if (filterPoolManagerList == null)
		{
			filterPoolManagerList = new ArrayList();
			//FIXMEnew EObjectContainmenteList(SystemFilterPoolManager.class, this, SubsystemsPackage.SUB_SYSTEM_FACTORY__FILTER_POOL_MANAGER_LIST);
		}
		return filterPoolManagerList;
	}

	public Object getAdapter(Class adapterType)
	{
   	    return Platform.getAdapterManager().getAdapter(this, adapterType);	
	}
	
	public boolean isDirty()
	{
		return _isDirty;
	}
	
	public void setDirty(boolean flag)
	{
		_isDirty = flag;		
	}



	public boolean wasRestored() 
	{
		// factories are never restored from disk
		return false;
	}



	public void setWasRestored(boolean flag) 
	{
		// dummy impl - not required for factories		
	}

}
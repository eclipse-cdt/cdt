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

package org.eclipse.rse.internal.model;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.core.ISystemUserIdConstants;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.SystemAdapterHelpers;
import org.eclipse.rse.core.SystemBasePlugin;
import org.eclipse.rse.core.SystemPerspectiveHelpers;
import org.eclipse.rse.core.SystemPreferencesManager;
import org.eclipse.rse.core.servicesubsystem.IServiceSubSystem;
import org.eclipse.rse.core.servicesubsystem.IServiceSubSystemConfiguration;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.core.subsystems.ISubSystemConfigurationProxy;
import org.eclipse.rse.core.subsystems.util.ISubSystemConfigurationAdapter;
import org.eclipse.rse.filters.ISystemFilter;
import org.eclipse.rse.filters.ISystemFilterPoolReferenceManager;
import org.eclipse.rse.filters.ISystemFilterReference;
import org.eclipse.rse.internal.persistence.RSEPersistenceManager;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.model.ISubSystemConfigurationCategories;
import org.eclipse.rse.model.ISystemContainer;
import org.eclipse.rse.model.ISystemHostPool;
import org.eclipse.rse.model.ISystemModelChangeEvent;
import org.eclipse.rse.model.ISystemModelChangeEvents;
import org.eclipse.rse.model.ISystemModelChangeListener;
import org.eclipse.rse.model.ISystemPreferenceChangeEvent;
import org.eclipse.rse.model.ISystemPreferenceChangeListener;
import org.eclipse.rse.model.ISystemProfile;
import org.eclipse.rse.model.ISystemProfileManager;
import org.eclipse.rse.model.ISystemRegistry;
import org.eclipse.rse.model.ISystemRemoteChangeEvent;
import org.eclipse.rse.model.ISystemRemoteChangeListener;
import org.eclipse.rse.model.ISystemResourceChangeEvent;
import org.eclipse.rse.model.ISystemResourceChangeEvents;
import org.eclipse.rse.model.ISystemResourceChangeListener;
import org.eclipse.rse.model.SystemChildrenContentsType;
import org.eclipse.rse.model.SystemRemoteChangeEvent;
import org.eclipse.rse.model.SystemResourceChangeEvent;
import org.eclipse.rse.references.ISystemBaseReferencingObject;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.RSESystemTypeAdapter;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.messages.SystemMessageDialog;
import org.eclipse.rse.ui.view.ISystemRemoteElementAdapter;
import org.eclipse.rse.ui.view.SystemDNDTransferRunnable;
import org.eclipse.rse.ui.view.SystemView;
import org.eclipse.rse.ui.view.SystemViewDataDropAdapter;
import org.eclipse.rse.ui.wizards.ISystemNewConnectionWizardPage;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.part.PluginTransfer;
import org.eclipse.ui.part.PluginTransferData;
import org.eclipse.ui.part.ResourceTransfer;


/**
 * Registry for all connections.
 */
public class SystemRegistry implements ISystemRegistry, ISystemModelChangeEvents
{

	private static Exception lastException = null;
	private static ISystemRegistry registry = null;
	private SystemResourceChangeManager listenerManager = null;
	private SystemPreferenceChangeManager preferenceListManager = null;
	private SystemModelChangeEventManager modelListenerManager = null;
	private SystemModelChangeEvent modelEvent;
	private SystemRemoteChangeEventManager remoteListManager = null;
	private SystemRemoteChangeEvent remoteEvent;

	private int listenerCount = 0;
	private int modelListenerCount = 0;
	private int remoteListCount = 0;

	private ISubSystemConfigurationProxy[] subsystemFactoryProxies = null;
	private boolean errorLoadingFactory = false;
	private Viewer viewer = null;
	// progress monitor support
	private IRunnableContext currentRunnableContext;
	private Shell currentRunnableContextShell;
	private Vector previousRunnableContexts = new Vector();
	private Vector previousRunnableContextShells = new Vector();

	private Clipboard clipboard = null;
	private SystemScratchpad scratchpad = null;

	/**
	 * Constructor.
	 * This is protected as the singleton instance should be retrieved by
	 *  calling getSystemRegistry().
	 * @param logfilePath Root folder. Where to place the log file. 
	 */
	protected SystemRegistry(String logfilePath)
	{
		super();
	
		listenerManager = new SystemResourceChangeManager();
		modelListenerManager = new SystemModelChangeEventManager();
		remoteListManager = new SystemRemoteChangeEventManager();
		preferenceListManager = new SystemPreferenceChangeManager();

		// get initial shell
		//FIXME - this can cause problems - don't think we should do this here anyway
		//getShell(); // will quietly fail in headless mode. Phil

		registry = this;
		restore();
	}
	/**
	 * Reset for a full refresh from disk, such as after a team synch
	 */
	public void reset()
	{
		SystemHostPool.reset();
		restore();
	}

	// ----------------------------
	// PUBLIC STATIC METHODS...
	// ----------------------------

	/**
	 * Return singleton instance. Must be used on first instantiate.
	 * @param logfilePath Root folder. Where to place the log file. 
	 */
	public static ISystemRegistry getSystemRegistry(String logfilePath)
	{
		if (registry == null)
			new SystemRegistry(logfilePath);
		return registry;
	}
	/**
	 * Return singleton instance assuming it already exists.
	 */
	public static ISystemRegistry getSystemRegistry()
	{
		return registry;
	}

	/**
	 * Ensure given path ends with path separator.
	 */
	public static String addPathTerminator(String path)
	{
		if (!path.endsWith(File.separator))
		{
			path = path + File.separatorChar;
		}
		return path;
	}

	// ----------------------------------
	// UI METHODS...
	// ----------------------------------
	/**
	 * Show the RSE perspective if it is not already showing
	 */
	public void showRSEPerspective()
	{
		SystemPerspectiveHelpers.openRSEPerspective();
	}
	/**
	 * Expand the given connection in the RSE, if the RSE is the active perspective.
	 */
	public void expandHost(IHost conn)
	{
		if (SystemPerspectiveHelpers.isRSEPerspectiveActive())
		{
			// find the RSE tree view
			SystemView rseView = SystemPerspectiveHelpers.findRSEView();
			if (rseView != null)
			{
				// find and expand the given connection
				rseView.setExpandedState(conn, true); // expand this connection
				rseView.setSelection(new StructuredSelection(conn));
			}
		}
	}
	/**
	 * Expand the given subsystem in the RSE, if the RSE is the active perspective.
	 */
	public void expandSubSystem(ISubSystem subsystem)
	{
		if (SystemPerspectiveHelpers.isRSEPerspectiveActive())
		{
			// find the RSE tree view
			SystemView rseView = SystemPerspectiveHelpers.findRSEView();
			if (rseView != null)
			{
				// find and expand the given subsystem's connection, and then subsystem
				rseView.setExpandedState(subsystem.getHost(), true); // expand this connection
				rseView.setExpandedState(subsystem, true);
				rseView.setSelection(new StructuredSelection(subsystem));
			}
		}
	}
	// ----------------------------------
	// SYSTEMVIEWINPUTPROVIDER METHODS...
	// ----------------------------------
	/**
	 * Return the children objects to consistute the root elements in the system view tree.
	 * We return all connections for all active profiles.
	 */
	public Object[] getSystemViewRoots()
	{
		//DKM - only return enabled connections now
		IHost[] connections = getHosts();
		List result = new ArrayList(); 
		for (int i = 0; i < connections.length; i++) {
			IHost con = connections[i];
			IRSESystemType sysType = RSECorePlugin.getDefault().getRegistry().getSystemType(con.getSystemType());
			if (sysType != null) { // sysType can be null if workspace contains a host that is no longer defined by the workbench
				RSESystemTypeAdapter adapter = (RSESystemTypeAdapter)(sysType.getAdapter(IRSESystemType.class));
				if (adapter.isEnabled(sysType)) {
					result.add(con);
				}
			}
		}
		return result.toArray();
	}
	/**
	 * Return true if {@link #getSystemViewRoots()} will return a non-empty list
	 * We return true if there are any connections for any active profile.
	 */
	public boolean hasSystemViewRoots()
	{
		return (getHostCount() > 0);
	}
	/**
	 * This method is called by the connection adapter when the user expands
	 *  a connection. This method must return the child objects to show for that
	 *  connection.
	 */
	public Object[] getConnectionChildren(IHost selectedConnection)
	{
		return getSubSystems(selectedConnection);
	}
	/**
	 * This method is called by the connection adapter when deciding to show a plus-sign
	 * or not beside a connection. Return true if this connection has children to be shown.
	 */
	public boolean hasConnectionChildren(IHost selectedConnection)
	{
		return true; // much faster and safer
		/*
		boolean hasSubsystems = false;
		if (subsystemFactoryProxies != null)
		{
		  for (int idx = 0; (!hasSubsystems) && (idx < subsystemFactoryProxies.length); idx++)
		  {
		  	 if (subsystemFactoryProxies[idx].appliesToSystemType(selectedConnection.getSystemType()) &&
		  	     subsystemFactoryProxies[idx].isSubSystemConfigurationActive())
		  	 {
		  	   SubSystemConfiguration factory = subsystemFactoryProxies[idx].getSubSystemConfiguration();
		  	   if (factory != null)          	   
		  	   {
		         SubSystem[] sss = factory.getSubSystems(selectedConnection, SubSystemConfiguration.LAZILY);               
		         if ((sss != null) && (sss.length>0))
		           hasSubsystems = true;
		  	   }
		  	   else
		  	     hasSubsystems = false;
		  	 }
		  	 else
		  	   hasSubsystems = true;
		  }
		}  
		else
		  hasSubsystems = true;  	
		return hasSubsystems;
		*/
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
	 * Set the shell in case it is needed for anything.
	 * The label and content provider will call this.
	 */
	public void setShell(Shell shell)
	{
	}

	/**
	 * Return the shell of the current viewer
	 */

	// thread safe shell
	public Shell getShell()
	{
		IWorkbench workbench = RSEUIPlugin.getDefault().getWorkbench();
		if (workbench != null)
		{
			// first try to get the active workbench window
			IWorkbenchWindow ww = workbench.getActiveWorkbenchWindow();
			if (ww == null) // no active window so just get the first one
				ww = workbench.getWorkbenchWindows()[0];
			if (ww != null)
			{
				Shell shell = ww.getShell();
				if (!shell.isDisposed())
				{
					return shell;
				}
			}
		}
		return null;
	}

	/**
	 * Return true to show the action bar (ie, toolbar) above the viewer.
	 * The action bar contains connection actions, predominantly.
	 */
	public boolean showActionBar()
	{
		return true;
	}
	/**
	 * Return true to show the button bar above the viewer.
	 * The tool bar contains "Get List" and "Refresh" buttons and is typicall
	 * shown in dialogs that list only remote system objects.
	 */
	public boolean showButtonBar()
	{
		return false;
	}
	/**
	 * Return true to show right-click popup actions on objects in the tree.
	 */
	public boolean showActions()
	{
		return true;
	}
	/**
	 * Set the viewer in case it is needed for anything.
	 * The label and content provider will call this.
	 */
	public void setViewer(Viewer viewer)
	{
		this.viewer = viewer;
	}
	/**
	 * Return the viewer we are currently associated with
	 */
	public Viewer getViewer()
	{
		return viewer;
	}
	/**
	 * Return true if we are listing connections or not, so we know whether we are interested in 
	 *  connection-add events
	 */
	public boolean showingConnections()
	{
		return true;
	}

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
	public void setRunnableContext(Shell shell, IRunnableContext context)
	{
		//this.currentRunnableContext = context;
		//this.currentRunnableContextShell = shell;
		pushRunnableContext(shell, context);
	}
	/**
	 * Clear the current active runnable context to be used for a progress monitor. 
	 * Be sure to call this from you dispose method.
	 */
	public void clearRunnableContext()
	{
		//this.currentRunnableContext = null;
		//this.currentRunnableContextShell = null;    	
		popRunnableContext();
	}
	/**
	 * Return the current registered runnable context, or null if none registered. Use this
	 *  for long running operations instead of an intrusive progress monitor dialog as it is
	 *  more user friendly. Many dialogs/wizards have these built in so it behooves us to use it.
	 */
	public IRunnableContext getRunnableContext()
	{
		if ((currentRunnableContextShell != null) && currentRunnableContextShell.isDisposed())
			clearRunnableContext();
		if (currentRunnableContext != null)
			return currentRunnableContext;
		else
			return null;
	}

	private IRunnableContext popRunnableContext()
	{
		Shell shell = null;
		boolean found = false;
		Vector disposedShells = new Vector();
		Vector disposedContexts = new Vector();
		for (int idx = previousRunnableContextShells.size() - 1; !found && (idx >= 0); idx--)
		{
			shell = (Shell) previousRunnableContextShells.elementAt(idx);
			if ((shell == currentRunnableContextShell) || shell.isDisposed())
			{
				disposedShells.add(shell);
				disposedContexts.add(previousRunnableContexts.elementAt(idx));
			}
			else
			{
				found = true;
				currentRunnableContextShell = shell;
				currentRunnableContext = (IRunnableContext) previousRunnableContexts.elementAt(idx);
			}
		}
		if (!found)
		{
			currentRunnableContextShell = null;
			currentRunnableContext = null;
		}
		for (int idx = 0; idx < disposedShells.size(); idx++)
		{
			previousRunnableContextShells.remove(disposedShells.elementAt(idx));
			previousRunnableContexts.remove(disposedContexts.elementAt(idx));
		}

		return currentRunnableContext;
	}

	private IRunnableContext pushRunnableContext(Shell shell, IRunnableContext context)
	{
		previousRunnableContexts.addElement(context);
		previousRunnableContextShells.addElement(shell);
		currentRunnableContextShell = shell;
		currentRunnableContext = context;
		return currentRunnableContext;
	}

	// ----------------------------
	// SUBSYSTEM FACTORY METHODS...
	// ----------------------------

	/**
	 * Private method used by RSEUIPlugin to tell registry all registered subsystem
	 *  factories. This way, all code can use this registry to access them versus the
	 *  RSEUIPlugin.
	 */
	public void setSubSystemConfigurationProxies(ISubSystemConfigurationProxy[] proxies)
	{
		subsystemFactoryProxies = proxies;
		//for (int idx=0; idx<proxies.length; idx++)
		// proxies[idx].setLogFile(logFile);
	}
	/**
	 * Public method to retrieve list of subsystem factory proxies registered by extension points.
	 */
	public ISubSystemConfigurationProxy[] getSubSystemConfigurationProxies()
	{
		return subsystemFactoryProxies;
	}

	/**
	 * Return all subsystem factory proxies matching a subsystem factory category.
	 * @see ISubSystemConfigurationCategories
	 */
	public ISubSystemConfigurationProxy[] getSubSystemConfigurationProxiesByCategory(String factoryCategory)
	{
		Vector v = new Vector();
		if (subsystemFactoryProxies != null)
		{
			for (int idx = 0; idx < subsystemFactoryProxies.length; idx++)
				if (subsystemFactoryProxies[idx].getCategory().equals(factoryCategory))
					v.addElement(subsystemFactoryProxies[idx]);
		}
		ISubSystemConfigurationProxy[] proxies = new ISubSystemConfigurationProxy[v.size()];
		for (int idx = 0; idx < v.size(); idx++)
		{
			proxies[idx] = (ISubSystemConfigurationProxy) v.elementAt(idx);
		}
		return proxies;
	}

	/**
	 * Return the parent subsystemconfiguration given a subsystem object.
	 */
	public ISubSystemConfiguration getSubSystemConfiguration(ISubSystem subsystem)
	{
		return subsystem.getSubSystemConfiguration();
	}

	/**
	 * Return the subsystemconfiguration, given its plugin.xml-declared id.
	 */
	public ISubSystemConfiguration getSubSystemConfiguration(String id)
	{
		ISubSystemConfigurationProxy[] proxies = getSubSystemConfigurationProxies();
		ISubSystemConfiguration match = null;
		for (int idx = 0;(match == null) && idx < proxies.length; idx++)
		{
			if (proxies[idx].getId().equals(id))
				match = proxies[idx].getSubSystemConfiguration();
		}
		return match;
	}



	/**
	 * Return all subsystem factories which have declared themselves part of the given category.
	 * <p>
	 * This looks for a match on the "category" of the subsystem factory's xml declaration
	 *  in its plugin.xml file. Thus, it is effecient as it need not bring to life a 
	 *  subsystem factory just to test its parent class type.
	 * 
	 * @see ISubSystemConfigurationCategories
	 */
	public ISubSystemConfiguration[] getSubSystemConfigurationsByCategory(String factoryCategory)
	{
		Vector v = new Vector();
		if (subsystemFactoryProxies != null)
		{
			for (int idx = 0; idx < subsystemFactoryProxies.length; idx++)
			{
				if (subsystemFactoryProxies[idx].getCategory().equals(factoryCategory))
				{
					ISubSystemConfiguration factory = subsystemFactoryProxies[idx].getSubSystemConfiguration();
					if (factory != null)
						v.addElement(factory);
				}
			}
		}
		ISubSystemConfiguration[] factories = new ISubSystemConfiguration[v.size()];
		for (int idx = 0; idx < v.size(); idx++)
		{
			factories[idx] = (ISubSystemConfiguration) v.elementAt(idx);
		}
		return factories;
	}

	public ISubSystemConfiguration[] getSubSystemConfigurationsBySystemType(String systemType)
	{
		return getSubSystemConfigurationsBySystemType(systemType, false);
	}
	
	/**
	 * Return all subsystem factories which support the given system type. If the type is null,
	 *  returns all.
	 * 
	 */
	public ISubSystemConfiguration[] getSubSystemConfigurationsBySystemType(String systemType, boolean filterDuplicateServiceSubSystemFactories)
	{
		List serviceTypesAdded = new ArrayList();
		List serviceImplsAdded = new ArrayList();
		Vector v = new Vector();
		if (subsystemFactoryProxies != null)
		{
			for (int idx = 0; idx < subsystemFactoryProxies.length; idx++)
			{
				ISubSystemConfigurationProxy ssfProxy = subsystemFactoryProxies[idx];
				if (ssfProxy.appliesToSystemType(systemType))
				{
					ISubSystemConfiguration ssFactory = ssfProxy.getSubSystemConfiguration();
					if (ssFactory != null)
					{
						if (ssFactory instanceof IServiceSubSystemConfiguration && filterDuplicateServiceSubSystemFactories)
						{
							IServiceSubSystemConfiguration serviceFactory = (IServiceSubSystemConfiguration)ssFactory;
							Class serviceType = serviceFactory.getServiceType();
							Class serviceImplType = serviceFactory.getServiceImplType();
							boolean containsThisServiceType = serviceTypesAdded.contains(serviceType);
							boolean containsThisServiceImplType = serviceImplsAdded.contains(serviceImplType);
							
							if (!containsThisServiceType)
							{							
								serviceTypesAdded.add(serviceType);
								serviceImplsAdded.add(serviceImplType);
								v.addElement(ssFactory);
							}
							else if (containsThisServiceImplType)
							{
								// remove the other one
								for (int i = 0; i < v.size(); i++)
								{
									IServiceSubSystemConfiguration addedConfig = (IServiceSubSystemConfiguration)v.get(i);
									if (addedConfig.getServiceType() == serviceType)
									{
										v.remove(addedConfig);
									}
								}
								
								v.addElement(ssFactory);
							}
						}
						else
						{
							v.addElement(ssFactory);
						}
					}
				}
			}
		}
		ISubSystemConfiguration[] factories = new ISubSystemConfiguration[v.size()];
		for (int idx = 0; idx < v.size(); idx++)
			factories[idx] = (ISubSystemConfiguration) v.elementAt(idx);
		return factories;
	}

	// ----------------------------
	// USER PREFERENCE METHODS...
	// ----------------------------
	/**
	 * Are connection names to be qualified by profile name?
	 */
	public boolean getQualifiedHostNames()
	{
		return SystemPreferencesManager.getPreferencesManager().getQualifyConnectionNames();
	}
	/**
	 * Set if connection names are to be qualified by profile name
	 */
	public void setQualifiedHostNames(boolean set)
	{
		SystemPreferencesManager.getPreferencesManager().setQualifyConnectionNames(set);
		IHost[] conns = getHosts();
		if (conns != null)
		{
			for (int idx = 0; idx < conns.length; idx++)
			{
				fireEvent(new SystemResourceChangeEvent(conns[idx], ISystemResourceChangeEvents.EVENT_RENAME, this));
			}
		}
		if (SystemPreferencesManager.getPreferencesManager().getShowFilterPools())
		{
			fireEvent(new SystemResourceChangeEvent(this, ISystemResourceChangeEvents.EVENT_REFRESH, this));			
		}

	}

	/**
	 * Reflect the user changing the preference for showing filter pools.
	 */
	public void setShowFilterPools(boolean show)
	{
		if (subsystemFactoryProxies != null)
		{
			for (int idx = 0; idx < subsystemFactoryProxies.length; idx++)
			{
				if (subsystemFactoryProxies[idx].isSubSystemConfigurationActive())
				{
					ISubSystemConfiguration factory = subsystemFactoryProxies[idx].getSubSystemConfiguration();
					if ((factory != null) && factory.supportsFilters())
						factory.setShowFilterPools(show);
				}
			}
		}
	}
	/*
	 * Reflect the user changing the preference for showing filter strings.
	 *
	public void setShowFilterStrings(boolean show)
	{
	    if (subsystemFactoryProxies != null)
	    {
	      for (int idx = 0; idx < subsystemFactoryProxies.length; idx++)
	      {
	      	 if (subsystemFactoryProxies[idx].isSubSystemConfigurationActive())
	      	 {
	      	   SubSystemConfiguration factory = subsystemFactoryProxies[idx].getSubSystemConfiguration();
	      	   if ((factory!=null)&&factory.supportsFilters())
	      	     factory.setShowFilterStrings(show);
	      	 }
	      }    	
	    }    	
	}*/
	/**
	 * Reflect the user changing the preference for showing new connection prompt
	 */
	public void setShowNewHostPrompt(boolean show)
	{
		fireEvent(new org.eclipse.rse.model.SystemResourceChangeEvent(this, ISystemResourceChangeEvents.EVENT_REFRESH, null));
	}

	// ----------------------------
	// PROFILE METHODS...
	// ----------------------------
	/**
	 * Return singleton profile manager
	 */
	public ISystemProfileManager getSystemProfileManager()
	{
		return SystemProfileManager.getSystemProfileManager();
	}

	/**
	 * Return the profiles currently selected by the user as his "active" profiles
	 */
	public ISystemProfile[] getActiveSystemProfiles()
	{
		return getSystemProfileManager().getActiveSystemProfiles();
	}
	/**
	 * Return the profile names currently selected by the user as his "active" profiles
	 */
	public String[] getActiveSystemProfileNames()
	{
		return getSystemProfileManager().getActiveSystemProfileNames();
	}
	/**
	 * Return all defined profiles
	 */
	public ISystemProfile[] getAllSystemProfiles()
	{
		return getSystemProfileManager().getSystemProfiles();
	}
	/**
	 * Return all defined profile names
	 */
	public String[] getAllSystemProfileNames()
	{
		return getSystemProfileManager().getSystemProfileNames();
	}
	/**
	 * Return all defined profile names as a vector
	 */
	public Vector getAllSystemProfileNamesVector()
	{
		return getSystemProfileManager().getSystemProfileNamesVector();
	}

	/**
	 * Get a SystemProfile given its name
	 */
	public ISystemProfile getSystemProfile(String profileName)
	{
		return getSystemProfileManager().getSystemProfile(profileName);
	}

	/**
	 * Create a SystemProfile given its name and whether or not to make it active
	 */
	public ISystemProfile createSystemProfile(String profileName, boolean makeActive) throws Exception
	{
		ISystemProfileManager mgr = getSystemProfileManager();
		ISystemProfile profile = mgr.createSystemProfile(profileName, makeActive);
		if (makeActive)
		{
			//fireEvent(new SystemResourceChangeEvent(profile,ISystemResourceChangeEvent.EVENT_ADD,this));    	  
		}
		fireModelChangeEvent(SYSTEM_RESOURCE_ADDED, SYSTEM_RESOURCETYPE_PROFILE, profile, null);
		return profile;
	}

	/**
	 * Rename a SystemProfile. Rename is propogated to all subsystem factories so
	 * they can rename their filter pool managers and whatever else is required.
	 */
	public void renameSystemProfile(ISystemProfile profile, String newName) throws Exception
	{
		/* FIXME
		// first, pre-test for folder-in-use error:
		IResource testResource = SystemResourceManager.getProfileFolder(profile);
		boolean inUse = SystemResourceManager.testIfResourceInUse(testResource);
		if (inUse)
		{
			SystemMessage msg = RSEUIPlugin.getPluginMessage((testResource instanceof IFolder) ? ISystemMessages.MSG_FOLDER_INUSE : ISystemMessages.MSG_FILE_INUSE);
			msg.makeSubstitution(testResource.getFullPath());
			throw new SystemMessageException(msg);
		}
		*/

		// step 0: force everything into memory! Very important to do this!
		loadAll();
		// step 0_a: get the proxies and the relavent connections...
		ISubSystemConfigurationProxy[] proxies = getSubSystemConfigurationProxies();
		IHost[] connections = getHostsByProfile(profile);
		String oldName = profile.getName();

		// step 0_b: pre-test if any of the subfolder or file renames will fail...
		if (proxies != null)
		{
			for (int idx = 0; idx < proxies.length; idx++)
			{
				// the following call will throw an exception if any of the affected folders/files are in use.
				if (proxies[idx] != null && proxies[idx].getSubSystemConfiguration() != null)
					proxies[idx].getSubSystemConfiguration().preTestRenameSubSystemProfile(oldName);
			}
		}

		// step 1: update connection pool. This is simply the in-memory name of the pool.
		ISystemHostPool profilePool = getHostPool(profile);
		profilePool.renameHostPool(newName);

		// step 2: rename profile and its folder on disk
		getSystemProfileManager().renameSystemProfile(profile, newName);

		// step 3: for every subsystem factory, ask it to rename its filter pool manager,
		//  and more importantly the folder name that manager holds.
		if (proxies != null)
		{
			for (int idx = 0; idx < proxies.length; idx++)
			{
				// Hmm, in v4 we only did this for active factories. That can't be right, as it needs to be done
				//  for EVERY factory. Hence this commented line of code, new for v5 (and to fix a bug I found in
				//  profile renaming... the local connection's filter pool folder was not renamed). Phil...
				//if (proxies[idx].isSubSystemConfigurationActive())
				ISubSystemConfiguration factory = proxies[idx].getSubSystemConfiguration();
				if (factory != null)
				{
					ISubSystemConfigurationAdapter adapter = (ISubSystemConfigurationAdapter)factory.getAdapter(ISubSystemConfigurationAdapter.class);
					adapter.renameSubSystemProfile(factory,oldName, newName);
				}
			}
		}

		// step 4: update every subsystem for every connection in this profile.
		// important to do this AFTER the profile is renamed.
		for (int idx = 0; idx < connections.length; idx++)
		{
			ISubSystem[] subsystems = getSubSystems(connections[idx]);
			for (int jdx = 0; jdx < subsystems.length; jdx++)
			{
				ISubSystem ss = subsystems[jdx];
				ISubSystemConfiguration ssf = ss.getSubSystemConfiguration();
				ssf.renameSubSystemProfile(ss, oldName, newName);
			}
		}
		SystemPreferencesManager.getPreferencesManager().setConnectionNamesOrder(); // update preferences order list                
		boolean namesQualifed = getQualifiedHostNames();
		if (namesQualifed)
			setQualifiedHostNames(namesQualifed); // causes refresh events to be fired

		fireModelChangeEvent(SYSTEM_RESOURCE_RENAMED, SYSTEM_RESOURCETYPE_PROFILE, profile, oldName);
	}
	/**
	 * Copy a SystemProfile. All connections connection data is copied.
	 * @param monitor Progress monitor to reflect each step of the operation
	 * @param profile Source profile to copy
	 * @param newName Unique name to give copied profile
	 * @param makeActive whether to make the copied profile active or not
	 * @return new SystemProfile object
	 */
	public ISystemProfile copySystemProfile(IProgressMonitor monitor, ISystemProfile profile, String newName, boolean makeActive) throws Exception
	{
		Exception lastExc = null;
		boolean failed = false;
		String msg = null;
		String oldName = profile.getName();
		IHost[] newConns = null;

		//RSEUIPlugin.logDebugMessage(this.getClass().getName(), "Start of system profile copy. From: "+oldName+" to: "+newName+", makeActive: "+makeActive);             
		// STEP 0: BRING ALL IMPACTED SUBSYSTEM FACTORIES TO LIFE NOW, BEFORE CREATING THE NEW PROFILE.
		// IF WE DO NOT DO THIS NOW, THEN THEY WILL CREATE A FILTER POOL MGR FOR THE NEW PROFILE AS THEY COME
		// TO LIFE... SOMETHING WE DON'T WANT!
		loadAll(); // force the world into memory!
		IHost[] conns = getHostsByProfile(profile);
		Vector factories = getSubSystemFactories(conns);
		if (errorLoadingFactory)
			return null;

		// STEP 1: CREATE NEW SYSTEM PROFILE
		ISystemProfile newProfile = getSystemProfileManager().cloneSystemProfile(profile, newName);

		try
		{
			// STEP 2: CREATE NEW SYSTEM CONNECTION POOL
			ISystemHostPool oldPool = getHostPool(profile);
			ISystemHostPool newPool = getHostPool(newProfile);

			// STEP 3: COPY ALL CONNECTIONS FROM OLD POOL TO NEW POOL
			//try { java.lang.Thread.sleep(2000l); } catch (InterruptedException e) {}
			if ((conns != null) && (conns.length > 0))
			{
				newConns = new IHost[conns.length];
				SystemMessage msgNoSubs = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_COPYCONNECTION_PROGRESS);
				for (int idx = 0; idx < conns.length; idx++)
				{
					msgNoSubs.makeSubstitution(conns[idx].getAliasName());
					SystemBasePlugin.logDebugMessage(this.getClass().getName(), msgNoSubs.getLevelOneText());
					monitor.subTask(msgNoSubs.getLevelOneText());

					newConns[idx] = oldPool.cloneHost(newPool, conns[idx], conns[idx].getAliasName());

					monitor.worked(1);
					//try { java.lang.Thread.sleep(3000l); } catch (InterruptedException e) {}
				}
			}
			msg = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_COPYFILTERPOOLS_PROGRESS).getLevelOneText();
			monitor.subTask(msg);
			SystemBasePlugin.logDebugMessage(this.getClass().getName(), msg);

			// STEP 4: CREATE NEW FILTER POOL MANAGER
			// STEP 5: COPY ALL FILTER POOLS FROM OLD MANAGER TO NEW MANAGER
			for (int idx = 0; idx < factories.size(); idx++)
			{
				ISubSystemConfiguration factory = (ISubSystemConfiguration) factories.elementAt(idx);
				msg = "Copying filterPools for factory " + factory.getName();
				//monitor.subTask(msg);
				SystemBasePlugin.logDebugMessage(this.getClass().getName(), msg);
				factory.copyFilterPoolManager(profile, newProfile);
				//try { java.lang.Thread.sleep(3000l); } catch (InterruptedException e) {}
			}

			monitor.worked(1);

			// STEP 6: COPY ALL SUBSYSTEMS FOR EACH COPIED CONNECTION
			msg = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_COPYSUBSYSTEMS_PROGRESS).getLevelOneText();
			monitor.subTask(msg);
			SystemBasePlugin.logDebugMessage(this.getClass().getName(), msg);
			if ((conns != null) && (conns.length > 0))
			{
				ISubSystem[] subsystems = null;
				ISubSystemConfiguration factory = null;
				for (int idx = 0; idx < conns.length; idx++)
				{
					msg = "Copying subsystems for connection " + conns[idx].getAliasName();
					//monitor.subTask(msg);
					SystemBasePlugin.logDebugMessage(this.getClass().getName(), msg);
					subsystems = getSubSystems(conns[idx]); // get old subsystems for this connection
					if ((subsystems != null) && (subsystems.length > 0))
					{
						for (int jdx = 0; jdx < subsystems.length; jdx++)
						{
							msg += ": subsystem " + subsystems[jdx].getName();
							//monitor.subTask(msg);
							SystemBasePlugin.logDebugMessage(this.getClass().getName(), msg);
							factory = subsystems[jdx].getSubSystemConfiguration();
							factory.cloneSubSystem(subsystems[jdx], newConns[idx], true); // true=>copy profile op vs copy connection op
							//try { java.lang.Thread.sleep(3000l); } catch (InterruptedException e) {}
						}
					}
					//try { java.lang.Thread.sleep(1000l); } catch (InterruptedException e) {}
				}
			}
			monitor.worked(1);
		}
		catch (Exception exc)
		{
			failed = true;
			lastExc = exc;
		}
		// if anything failed, we have to back out what worked. Ouch!
		if (failed)
		{
			try
			{
				if (newConns != null)
					for (int idx = 0; idx < newConns.length; idx++)
						deleteHost(newConns[idx]);
				for (int idx = 0; idx < subsystemFactoryProxies.length; idx++)
				{
					ISubSystemConfiguration factory = subsystemFactoryProxies[idx].getSubSystemConfiguration();
					if (factory != null)
						factory.deletingSystemProfile(newProfile);
				}
				getSystemProfileManager().deleteSystemProfile(newProfile);
			}
			catch (Exception exc)
			{
				SystemBasePlugin.logError("Exception (ignored) cleaning up from copy-profile exception.", exc);
			}
			throw (lastExc);
		}

		// LAST STEP: MAKE NEW PROFILE ACTIVE IF SO REQUESTED: NO, CAN'T DO IT HERE BECAUSE OF THREAD VIOLATIONS!
		//if (makeActive)
		//setSystemProfileActive(newProfile, true);

		fireModelChangeEvent(SYSTEM_RESOURCE_ADDED, SYSTEM_RESOURCETYPE_PROFILE, newProfile, null);

		SystemBasePlugin.logDebugMessage(this.getClass().getName(), "Copy of system profile " + oldName + " to " + newName + " successful");
		return newProfile;
	}

	/**
	 * Delete a SystemProfile. Prior to physically deleting the profile, we delete all
	 * the connections it has (first disconnecting if they are connected), and all the subsystems they have.
	 * <p>
	 * As well, all the filter pools for this profile are deleted, and subsequently any
	 * cross references from subsystems in connections in other profiles are removed.
	 * <p>
	 * A delete event is fired for every connection deleted.
	 */
	public void deleteSystemProfile(ISystemProfile profile) throws Exception
	{
		// step 0: load the world!
		loadAll(); // force the world into memory!

		// step 1: delete subsystems and connections
		IHost[] connections = getHostsByProfile(profile);
		//SystemConnectionPool pool = getConnectionPool(profile);
		for (int idx = 0; idx < connections.length; idx++)
		{
			deleteHost(connections[idx]);
		}
		// step 2: bring to life every factory and ask it to delete all filter pools for this profile
		if (subsystemFactoryProxies != null)
		{
			for (int idx = 0; idx < subsystemFactoryProxies.length; idx++)
			{
				ISubSystemConfiguration factory = subsystemFactoryProxies[idx].getSubSystemConfiguration();
				if (factory != null)
					factory.deletingSystemProfile(profile);
			}
		}
		// last step... physically blow away the profile...
		getSystemProfileManager().deleteSystemProfile(profile);
		SystemPreferencesManager.getPreferencesManager().setConnectionNamesOrder(); // update preferences order list        
		if ((connections != null) && (connections.length > 0)) // defect 42112
			fireEvent(new org.eclipse.rse.model.SystemResourceChangeEvent(connections, ISystemResourceChangeEvents.EVENT_DELETE_MANY, this));

		fireModelChangeEvent(SYSTEM_RESOURCE_REMOVED, SYSTEM_RESOURCETYPE_PROFILE, profile, null);
	}

	/**
	 * Make or unmake the given profile active.
	 * If switching to inactive, we force a disconnect for all subsystems of all connections in this profile.
	 */
	public void setSystemProfileActive(ISystemProfile profile, boolean makeActive)
	{
		// Test if there are any filter pools in this profile that are referenced by another active profile...    	
		Vector activeReferenceVector = new Vector();
		if (!makeActive && (subsystemFactoryProxies != null))
		{
			for (int idx = 0; idx < subsystemFactoryProxies.length; idx++)
			{
				//if (subsystemFactoryProxies[idx].isSubSystemConfigurationActive()) // don't bother if not yet alive
				{
					ISubSystemConfiguration factory = subsystemFactoryProxies[idx].getSubSystemConfiguration();
					if (factory != null)
					{
						ISubSystem[] activeReferences = factory.testForActiveReferences(profile);
						if (activeReferences != null)
							for (int jdx = 0; jdx < activeReferences.length; jdx++)
								activeReferenceVector.addElement(activeReferences[jdx]);
					}
				}
			}
		}
		if (activeReferenceVector.size() > 0)
		{
			SystemBasePlugin.logWarning(
				ISystemMessages.MSG_LOADING_PROFILE_SHOULDNOTBE_DEACTIVATED
					+ ": De-Activativing profile "
					+ profile.getName()
					+ " for which there are subsystems containing references to filter pools:");
			for (int idx = 0; idx < activeReferenceVector.size(); idx++)
			{
				ISubSystem activeReference = (ISubSystem) activeReferenceVector.elementAt(idx);
				SystemBasePlugin.logWarning(
					"  " + activeReference.getName() + " in connection " + activeReference.getHost().getAliasName() + " in profile " + activeReference.getSystemProfileName());
			}
			ISubSystem firstSubSystem = (ISubSystem) activeReferenceVector.elementAt(0);
			String connectionName = firstSubSystem.getHost().getSystemProfileName() + "." + firstSubSystem.getHost().getAliasName();
			SystemMessage sysMsg = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_LOADING_PROFILE_SHOULDNOTBE_DEACTIVATED);
			sysMsg.makeSubstitution(profile.getName(), connectionName);
			SystemBasePlugin.logWarning(sysMsg.getFullMessageID() + ": " + sysMsg.getLevelOneText());
			SystemMessageDialog msgDlg = new SystemMessageDialog(null, sysMsg);
			msgDlg.open();
		}

		getSystemProfileManager().makeSystemProfileActive(profile, makeActive);

		// To be safe, we tell each subsystem factory about the change in status. 
		// At a minimum, each factory may have to load the subsystems for connections that
		//  are suddenly active.
		if (subsystemFactoryProxies != null)
		{
			for (int idx = 0; idx < subsystemFactoryProxies.length; idx++)
			{
				if (subsystemFactoryProxies[idx].isSubSystemConfigurationActive()) // don't bother if not yet alive
				{
					ISubSystemConfiguration factory = subsystemFactoryProxies[idx].getSubSystemConfiguration();
					if (factory != null)
						factory.changingSystemProfileActiveStatus(profile, makeActive);
				}
			}
		}

		IHost[] affectedConnections = getHostsByProfile(profile);
		//System.out.println("Affected Connection Count: " + affectedConnections.length);

		// delete...
		if (!makeActive) // better disconnect all connections before we lose sight of them
		{
			if ((affectedConnections != null) && (affectedConnections.length > 0))
			{
				for (int idx = 0; idx < affectedConnections.length; idx++)
				{
					disconnectAllSubSystems(affectedConnections[idx]);
				}
				SystemResourceChangeEvent event = new org.eclipse.rse.model.SystemResourceChangeEvent(affectedConnections, ISystemResourceChangeEvents.EVENT_DELETE_MANY, this);
				fireEvent(event);
			}
		}
		// add...
		else if ((affectedConnections != null) && (affectedConnections.length > 0))
		{
			SystemResourceChangeEvent event = new org.eclipse.rse.model.SystemResourceChangeEvent(affectedConnections, ISystemResourceChangeEvents.EVENT_ADD_MANY, this);
			fireEvent(event);
		}
		SystemPreferencesManager.getPreferencesManager().setConnectionNamesOrder(); // update preferences order list            	

		fireModelChangeEvent(SYSTEM_RESOURCE_CHANGED, SYSTEM_RESOURCETYPE_PROFILE, profile, null);
	}

	// private profile methods...

	/**
	 * Get a SystemProfile given a connection pool
	 */
	private ISystemProfile getSystemProfile(ISystemHostPool pool)
	{
		return pool.getSystemProfile();
	}
	/**
	 * Get a SystemProfile name given a connection pool
	 */
	private String getSystemProfileName(ISystemHostPool pool)
	{
		ISystemProfile profile = getSystemProfile(pool);
		if (profile == null) return null; // MJB: Defect 45678
		else return profile.getName();
	}
	public IConnectorService[] getConnectorServices(IHost conn)
	{
		List csList = new ArrayList();
		// DKM for now, I'll just use the subsystems to get at the systems
		//  but later with new model, we should be getting these directly
		ISubSystem[] sses = getSubSystems(conn);
		for (int i = 0; i < sses.length; i++)
		{
			ISubSystem ss = sses[i];
			IConnectorService service = ss.getConnectorService();
			if (!csList.contains(service))
			{
				csList.add(service);
			}
		}
		return (IConnectorService[])csList.toArray(new IConnectorService[csList.size()]);
	}
	
	/**
	 * Return list of subsystem objects for a given connection.
	 * Demand pages the subsystem factories into memory if they aren't already.
	 * <p>
	 * To protect against crashes, if there are no subsystems, an array of length zero is returned.
	 */
	public ISubSystem[] getSubSystems(IHost conn)
	{
		return getSubSystems(conn, ISubSystemConfiguration.FORCE_INTO_MEMORY);
	}
	
	/**
	 * Return list of subsystem objects for a given connection.
	 * Demand pages the subsystem factories into memory if they aren't already.
	 * <p>
	 * To protect against crashes, if there are no subsystems, an array of length zero is returned.
	 */
	public ISubSystem[] getSubSystems(IHost conn, boolean force)
	{
		ISubSystem[] subsystems = null;
		Vector v = new Vector();

		if (subsystemFactoryProxies != null)
		{
			for (int idx = 0; idx < subsystemFactoryProxies.length; idx++)
			{
				if (subsystemFactoryProxies[idx].appliesToSystemType(conn.getSystemType()))
				{
					ISubSystemConfiguration factory = subsystemFactoryProxies[idx].getSubSystemConfiguration();
					if (factory != null)
					{
						ISubSystem[] sss = factory.getSubSystems(conn, force);
						if (sss != null)
							for (int jdx = 0; jdx < sss.length; jdx++)
								v.addElement(sss[jdx]);
					}
				}
			}
			//if (v.size() > 0)
			//{
			subsystems = new ISubSystem[v.size()];
			for (int idx = 0; idx < v.size(); idx++)
				subsystems[idx] = (ISubSystem) v.elementAt(idx);
			//}
		}
		return subsystems;
	}

	/**
	 * Resolve a subsystem from it's absolute name.  The absolute name of a subsystem
	 * is denoted by <I>profileName</I>.<I>connectionName</I>:<I>subsystemFactoryId</I>
	 * 
	 * @param absoluteSubSystemName the name of the subsystem
	 * 
	 * @return the subsystem
	 */
	public ISubSystem getSubSystem(String absoluteSubSystemName)
	{
		// first extract subsystem id
		int profileDelim = absoluteSubSystemName.indexOf(".");
		int connectionDelim = absoluteSubSystemName.indexOf(":", profileDelim + 1);

		if (profileDelim > 0 && connectionDelim > profileDelim)
		{
			String srcProfileName = absoluteSubSystemName.substring(0, profileDelim);
			String srcConnectionName = absoluteSubSystemName.substring(profileDelim + 1, connectionDelim);
			String srcSubSystemConfigurationId = absoluteSubSystemName.substring(connectionDelim + 1, absoluteSubSystemName.length());

			return getSubSystem(srcProfileName, srcConnectionName, srcSubSystemConfigurationId);
		}

		return null;
	}

	/**
	 * Resolve a subsystem from it's profile, connection and subsystem name.
	 * 
	 * @param srcProfileName the name of the profile
	 * @param srcConnectionName the name of the connection
	 * @param subsystemFactoryId the factory Id of the subsystem
	 * 
	 * @return the subsystem
	 */
	public ISubSystem getSubSystem(String srcProfileName, String srcConnectionName, String subsystemFactoryId)
	{
		// find the src connection    	    	
		IHost[] connections = registry.getHostsByProfile(srcProfileName);
		if (connections == null)
		{
			// if the profile can't be found, get all connections
			connections = registry.getHosts();
		}

		for (int i = 0; i < connections.length; i++)
		{
			IHost connection = connections[i];
			String connectionName = connection.getAliasName();

			if (connectionName.equals(srcConnectionName))
			{
				ISubSystem[] subsystems = getSubSystems(connection);
				for (int s = 0; s < subsystems.length; s++)
				{
					ISubSystem subsystem = subsystems[s];
					String compareId = subsystem.getConfigurationId();
					if (compareId.equals(subsystemFactoryId))
					{
						return subsystem;
					}
					else
					{
						// for migration purposes, test the against the name
						// we used to use the subsystem name instead of the factory Id
						if (subsystem.getName().equals(subsystemFactoryId))
						{
							return subsystem;
						}
					}
				}
			}
		}

		return null;
	}

	/**
	 * Return the absolute name for the specified subsystem
	 * @param the subsystem
	 * @return the absolute name of the subsystem
	 */
	public String getAbsoluteNameForSubSystem(ISubSystem subSystem)
	{
		StringBuffer dataStream = new StringBuffer();

		String profileName = subSystem.getSystemProfileName();
		String connectionName = subSystem.getHostAliasName();
		String factoryId = subSystem.getConfigurationId();

		dataStream.append(profileName);
		dataStream.append(".");
		dataStream.append(connectionName);
		dataStream.append(":");
		dataStream.append(factoryId);
		return dataStream.toString();
	}

	 /**
	  * Return the absolute name for the specified connection
	  * @param the connection
	  * @return the absolute name of the connection
	  */
	 public String getAbsoluteNameForConnection(IHost connection)
	 {
	 	StringBuffer dataStream = new StringBuffer();

		String profileName = connection.getSystemProfileName();
		String connectionName = connection.getAliasName();  
		
		dataStream.append(profileName);
		dataStream.append(".");
		dataStream.append(connectionName);
		return dataStream.toString();
	 }
	 
	 
	/**
	 * Return list of subsystem objects for a given connection, but does not force 
	 *  as-yet-non-restored subsystems to come to life.
	 * <p>
	 * To protect against crashes, if there are no subsystems, an array of length zero is returned.
	 */
	public ISubSystem[] getSubSystemsLazily(IHost conn)
	{
		ISubSystem[] subsystems = null;
		Vector v = new Vector();

		if (subsystemFactoryProxies != null)
		{
			for (int idx = 0; idx < subsystemFactoryProxies.length; idx++)
			{
				if (subsystemFactoryProxies[idx].appliesToSystemType(conn.getSystemType()) && subsystemFactoryProxies[idx].isSubSystemConfigurationActive())
				{
					ISubSystemConfiguration factory = subsystemFactoryProxies[idx].getSubSystemConfiguration();
					if (factory != null)
					{
						ISubSystem[] sss = factory.getSubSystems(conn, ISubSystemConfiguration.LAZILY);
						if (sss != null)
							for (int jdx = 0; jdx < sss.length; jdx++)
								v.addElement(sss[jdx]);
					}
				}
			}
			//if (v.size() > 0)
			//{
			subsystems = new ISubSystem[v.size()];
			for (int idx = 0; idx < v.size(); idx++)
				subsystems[idx] = (ISubSystem) v.elementAt(idx);
			//}
		}
		return subsystems;
	}

	/**
	 * Get a list of subsystem objects owned by the subsystem factory identified by 
	 *  its given plugin.xml-described id. Array is never null, but may be of length 0.
	 * <p>
	 * This is a list that of all subsystems for all connections owned by the factory.
	 */
	public ISubSystem[] getSubSystems(String factoryId)
	{
		ISubSystemConfiguration factory = getSubSystemConfiguration(factoryId);
		if (factory == null)
			return (new ISubSystem[0]);
		//return factory.getSubSystems();
		return factory.getSubSystems(true); // true ==> force full restore from disk
	}

	public ISubSystem[] getServiceSubSystems(Class serviceType, IHost connection)
	{
		List matches = new ArrayList();
		ISubSystem[] allSS = connection.getSubSystems();
		for (int i = 0; i < allSS.length; i++)
		{
			ISubSystem ss = allSS[i];
			if (ss instanceof IServiceSubSystem)
			{
				IServiceSubSystem serviceSubSystem = (IServiceSubSystem)ss;
				if (serviceSubSystem.getServiceType() == serviceType)
				{
					matches.add(ss);
				}
			}						
		}
		return (ISubSystem[])matches.toArray(new ISubSystem[matches.size()]);
	}
	
	/**
	 * Get a list of subsystem objects for given connection, owned by the subsystem factory 
	 * identified by its given plugin.xml-described id. Array will never be null but may be length zero.
	 */
	public ISubSystem[] getSubSystems(String factoryId, IHost connection)
	{
		ISubSystemConfiguration factory = getSubSystemConfiguration(factoryId);
		if (factory == null)
			return (new ISubSystem[0]);
		return factory.getSubSystems(connection, ISubSystemConfiguration.FORCE_INTO_MEMORY);
	}
	/**
	 * Get a list of subsystem objects for given connection, owned by a subsystem factory 
	 * that is of the given category. Array will never be null but may be length zero.
	 * <p>
	 * This looks for a match on the "category" of the subsystem factory's xml declaration
	 *  in its plugin.xml file. 
	 * 
	 * @see org.eclipse.rse.model.ISubSystemConfigurationCategories
	 */
	public ISubSystem[] getSubSystemsBySubSystemConfigurationCategory(String factoryCategory, IHost connection)
	{
		ISubSystem[] subsystems = getSubSystems(connection);
		if ((subsystems != null) && (subsystems.length > 0))
		{
			Vector v = new Vector();
			for (int idx = 0; idx < subsystems.length; idx++)
				if (subsystems[idx].getSubSystemConfiguration().getCategory().equals(factoryCategory))
					v.addElement(subsystems[idx]);
			ISubSystem[] sss = new ISubSystem[v.size()];
			for (int idx = 0; idx < sss.length; idx++)
				sss[idx] = (ISubSystem) v.elementAt(idx);
			return sss;
		}
		else
			return (new ISubSystem[0]);
	}


	/**
	 * Return all subsystem factories. Be careful when you call this, as it activates all 
	 *   subsystem factories.
	 */
	public ISubSystemConfiguration[] getSubSystemConfigurations()
	{
		Vector v = new Vector();
		ISubSystemConfigurationProxy[] proxies = getSubSystemConfigurationProxies();

		if (proxies != null)
		{
	
			for (int idx = 0; idx < proxies.length; idx++)
			{
				v.add(proxies[idx].getSubSystemConfiguration());
			}
		}
		return (ISubSystemConfiguration[])v.toArray(new ISubSystemConfiguration[v.size()]);
	}
	/**
	 * Return Vector of subsystem factories that apply to a given system connection
	 */
	protected Vector getSubSystemFactories(IHost conn)
	{
		Vector factories = new Vector();
		errorLoadingFactory = false;
		return getSubSystemFactories(conn, factories);
	}

	/**
	 * Return Vector of subsystem factories that apply to a given system connection, updating given vector
	 */
	protected Vector getSubSystemFactories(IHost conn, Vector factories)
	{
		ISubSystem[] subsystems = getSubSystems(conn);
		if (subsystems != null)
			for (int idx = 0; idx < subsystems.length; idx++)
			{
				ISubSystemConfiguration ssFactory = subsystems[idx].getSubSystemConfiguration();
				if (ssFactory == null)
					errorLoadingFactory = true;
				if ((ssFactory != null) && !factories.contains(ssFactory))
					factories.add(ssFactory);
			}
		return factories;
	}

	/**
	 * Return Vector of subsystem factories that apply to a given system connection array
	 */
	protected Vector getSubSystemFactories(IHost[] conns)
	{
		Vector factories = new Vector();
		errorLoadingFactory = false;
		if (conns != null)
			for (int idx = 0; idx < conns.length; idx++)
			{
				getSubSystemFactories(conns[idx], factories);
			}
		return factories;
	}

	/**
	 * Delete a subsystem object. This code finds the factory that owns it and
	 *  delegates the request to that factory.
	 */
	public boolean deleteSubSystem(ISubSystem subsystem)
	{
		ISubSystemConfiguration ssFactory = getSubSystemConfiguration(subsystem);
		if (ssFactory == null)
			return false;
		boolean ok = ssFactory.deleteSubSystem(subsystem);
		return ok;
	}

	// ----------------------------
	// PRIVATE CONNECTION METHODS...
	// ----------------------------
	/**
	 * Return a connection pool given a profile name
	 */
	private ISystemHostPool getHostPool(String profileName)
	{
		ISystemProfile profile = getSystemProfileManager().getSystemProfile(profileName);
		if (profile == null)
		{
			return null;
		}
		return getHostPool(profile);
	}
	/**
	 * Return a connection pool given a profile
	 */
	private ISystemHostPool getHostPool(ISystemProfile profile)
	{
		lastException = null;
		try
		{
			return SystemHostPool.getSystemHostPool(profile);
		}
		catch (Exception exc)
		{
			SystemBasePlugin.logError("Exception in getConnectionPool for " + profile.getName(), exc);
			lastException = exc;
		}
		catch (Throwable t)
		{
			SystemBasePlugin.logError("Exception in getConnectionPool for " + profile.getName(), t);
		}
		return null;
	}
	/**
	 * Return connection pools for active profiles. One per.
	 */
	private ISystemHostPool[] getHostPools()
	{
		ISystemProfile[] profiles = getSystemProfileManager().getActiveSystemProfiles();
		ISystemHostPool[] pools = new ISystemHostPool[profiles.length];
		for (int idx = 0; idx < pools.length; idx++)
		{
			try
			{
				pools[idx] = SystemHostPool.getSystemHostPool(profiles[idx]);
			}
			catch (Exception exc)
			{
			}
		}
		return pools;
	}

	// ----------------------------
	// PUBLIC CONNECTION METHODS...
	// ----------------------------
	
	/**
	 * Return the first connection to localhost we can find. While we always create a default one in
	 *  the user's profile, it is possible that this profile is not active or the connection was deleted.
	 *  However, since any connection to localHost will usually do, we just search all active profiles
	 *  until we find one, and return it. <br>
	 * If no localhost connection is found, this will return null. If one is needed, it can be created 
	 *  easily by calling {@link #createLocalHost(ISystemProfile, String, String)}.
	 */
	public IHost getLocalHost()
	{
		IHost localConn = null;
		IHost[] conns = getHostsBySystemType(IRSESystemType.SYSTEMTYPE_LOCAL);
		if (conns != null && conns.length > 0) return conns[0];
		else return localConn;
	}

	/**
	 * Return all connections in all active profiles. Never returns null, but may return a zero-length array.
	 */
	public IHost[] getHosts()
	{
		ISystemHostPool[] pools = getHostPools();
		Vector v = new Vector();
		for (int idx = 0; idx < pools.length; idx++)
		{
			IHost[] conns = getHostsByProfile(getSystemProfileName(pools[idx]));
			if (conns != null)
				for (int jdx = 0; jdx < conns.length; jdx++)
					v.addElement(conns[jdx]);
		}
		IHost[] allConns = new IHost[v.size()];
		for (int idx = 0; idx < v.size(); idx++)
			allConns[idx] = (IHost) v.elementAt(idx);
		return allConns;
	}
	/**
	 * Return all connections in a given profile.
	 */
	public IHost[] getHostsByProfile(ISystemProfile profile)
	{
		ISystemHostPool pool = getHostPool(profile);
		return pool.getHosts();
	}
	/**
	 * Return all connections in a given profile name.
	 */
	public IHost[] getHostsByProfile(String profileName)
	{
		ISystemHostPool pool = getHostPool(profileName);
		if (pool != null)
		{
			return pool.getHosts();
		}
		else
		{
			return null;
		}
	}
	/**
	 * Return all connections for which there exists one or more subsystems owned
	 *  by a given subsystem factory.
	 * @see #getSubSystemConfiguration(String)
	 */
	public IHost[] getHostsBySubSystemConfiguration(ISubSystemConfiguration factory)
	{
		/* The following algorithm failed because factory.getSubSystems() only returns 
		 *  subsystems that have been restored, which are only those that have been
		 *  expanded.
		 */
		ISubSystem[] subsystems = factory.getSubSystems(true); // true ==> force full restore
		Vector v = new Vector();
		for (int idx = 0; idx < subsystems.length; idx++)
		{
			IHost conn = subsystems[idx].getHost();
			if (!v.contains(conn))
				v.addElement(conn);
		}
		IHost[] conns = new IHost[v.size()];
		for (int idx = 0; idx < v.size(); idx++)
		{
			conns[idx] = (IHost) v.elementAt(idx);
		}
		return conns;
	}
	/**
	 * Return all connections for which there exists one or more subsystems owned
	 *  by a given subsystem factory, identified by factory Id
	 */
	public IHost[] getHostsBySubSystemConfigurationId(String factoryId)
	{
		return getHostsBySubSystemConfiguration(getSubSystemConfiguration(factoryId));
	}
	/**
	 * Return all connections for which there exists one or more subsystems owned
	 *  by any child classes of a given subsystem factory category.
	 * <p>
	 * This looks for a match on the "category" of the subsystem factory's xml declaration
	 *  in its plugin.xml file. Thus, it is effecient as it need not bring to live a 
	 *  subsystem factory just to test its parent class type.
	 * 
	 * @see ISubSystemConfigurationCategories
	 */
	public IHost[] getHostsBySubSystemConfigurationCategory(String factoryCategory)
	{
		Vector v = new Vector();
		if (subsystemFactoryProxies != null)
		{
			for (int idx = 0; idx < subsystemFactoryProxies.length; idx++)
			{
				if (subsystemFactoryProxies[idx].getCategory().equals(factoryCategory))
				{
					ISubSystemConfiguration factory = subsystemFactoryProxies[idx].getSubSystemConfiguration();
					if (factory != null)
					{
						ISubSystem[] subsystems = factory.getSubSystems(true); // true ==> force full restore
						if (subsystems != null)
							for (int jdx = 0; jdx < subsystems.length; jdx++)
							{
								IHost conn = subsystems[jdx].getHost();
								if (!v.contains(conn))
									v.addElement(conn);
							}
					}
				}
			}
		}
		IHost[] conns = new IHost[v.size()];
		for (int idx = 0; idx < v.size(); idx++)
		{
			conns[idx] = (IHost) v.elementAt(idx);
		}
		return conns;
	}

	/**
	 * Return all connections for all active profiles, for the given system type.
	 * Never returns null!
	 */
	public IHost[] getHostsBySystemType(String systemType)
	{
		IHost[] connections = getHosts();
		Vector v = new Vector();
		for (int idx = 0; idx < connections.length; idx++)
		{
			if (connections[idx].getSystemType().equals(systemType))
				v.addElement(connections[idx]);
		}
		IHost[] conns = new IHost[v.size()];
		for (int idx = 0; idx < v.size(); idx++)
			conns[idx] = (IHost) v.elementAt(idx);
		return conns;
	}
	/**
	 * Return all connections for all active profiles, for the given system types.
	 * Never returns null!
	 */
	public IHost[] getHostsBySystemTypes(String[] systemTypes)
	{
		IHost[] connections = getHosts();
		Vector v = new Vector();
		for (int idx = 0; idx < connections.length; idx++)
		{
			String systemType = connections[idx].getSystemType();
			boolean match = false;
			for (int jdx = 0; !match && (jdx < systemTypes.length); jdx++)
				if (systemType.equals(systemTypes[jdx]))
					match = true;
			if (match)
				v.addElement(connections[idx]);
		}
		IHost[] conns = new IHost[v.size()];
		for (int idx = 0; idx < v.size(); idx++)
			conns[idx] = (IHost) v.elementAt(idx);
		return conns;
	}

	/**
	 * Return a SystemConnection object given a system profile containing it, 
	 *   and an connection name uniquely identifying it.
	 */
	public IHost getHost(ISystemProfile profile, String connectionName)
	{
		return getHostPool(profile).getHost(connectionName);
	}
	/**
	 * Return the zero-based position of a SystemConnection object within its profile.
	 */
	public int getHostPosition(IHost conn)
	{
		ISystemHostPool pool = conn.getHostPool();
		return pool.getHostPosition(conn);
	}
	/**
	 * Return the zero-based position of a SystemConnection object within all active profiles.
	 */
	public int getHostPositionInView(IHost conn)
	{
		IHost[] conns = getHosts();
		int pos = -1;
		for (int idx = 0;(pos == -1) && (idx < conns.length); idx++)
		{
			if (conns[idx] == conn)
				pos = idx;
		}
		return pos;
	}

	/**
	 * Return the number of SystemConnection objects within the given profile
	 */
	public int getHostCount(String profileName)
	{
		return getHostPool(profileName).getHostCount();
	}
	/**
	 * Return the number of SystemConnection objects within the given connection's owning profile
	 */
	public int getHostCountWithinProfile(IHost conn)
	{
		return conn.getHostPool().getHostCount();
	}

	/**
	 * Return the number of SystemConnection objects within all active profiles
	 */
	public int getHostCount()
	{
		ISystemHostPool[] pools = getHostPools();
		int total = 0;
		for (int idx = 0; idx < pools.length; idx++)
		{
			total += pools[idx].getHostCount();
		}
		return total;
	}

	/**
	 * Return a vector of previously-used connection names in the given named profile.
	 * @return Vector of String objects.
	 */
	public Vector getHostAliasNames(String profileName)
	{
		ISystemHostPool pool = getHostPool(profileName);
		Vector names = new Vector();
		IHost[] conns = pool.getHosts();
		for (int idx = 0; idx < conns.length; idx++)
		{
			names.addElement(conns[idx].getAliasName());
		}
		return names;
	}
	/**
	 * Return a vector of previously-used connection names in the given profile.
	 * @return Vector of String objects.
	 */
	public Vector getHostAliasNames(ISystemProfile profile)
	{
		return getHostAliasNames(profile.getName());
	}
	/**
	 * Return a vector of previously-used connection names in all active profiles.
	 */
	public Vector getHostAliasNamesForAllActiveProfiles()
	{
		ISystemHostPool[] allPools = getHostPools();
		Vector allNames = new Vector();
		for (int idx = 0; idx < allPools.length; idx++)
		{
			Vector v = getHostAliasNames(getSystemProfileName(allPools[idx]));
			for (int jdx = 0; jdx < v.size(); jdx++)
				allNames.addElement(v.elementAt(jdx));
		}
		return allNames;
	}

	/**
	 * Return array of all previously specified hostnames.
	 */
	public String[] getHostNames()
	{
		return getHostNames(null);
	}
	/**
	 * Return array of previously specified hostnames for a given system type.
	 * After careful consideration, it is decided that if the system type is null,
	 *  then no hostnames should be returned. Previously all for all types were returned.
	 */
	public String[] getHostNames(String systemType)
	{
		Vector v = new Vector();

		if (systemType != null)
		{
			IHost[] conns = getHosts();
			for (int idx = 0; idx < conns.length; idx++)
			{
				if (!v.contains(conns[idx].getHostName()))
				{
					if (conns[idx].getSystemType().equals(systemType))
						v.addElement(conns[idx].getHostName());
				}
			}
		}
		if ((systemType != null) && (systemType.equals(IRSESystemType.SYSTEMTYPE_LOCAL) && (v.size() == 0)))
			v.addElement("localhost");
		String[] names = new String[v.size()];
		for (int idx = 0; idx < names.length; idx++)
			names[idx] = (String) v.elementAt(idx);
		return names;
	}

	/**
	 * Returns the clipboard used for copy actions
	 */
	public Clipboard getSystemClipboard()
	{
		if (clipboard == null)
		{
			Display display = null;
			Shell shell = getShell();
			if (shell == null)
			{
				display = Display.getDefault();
			}
			else
			{
				display = shell.getDisplay();
			}
			clipboard = new Clipboard(display);
		}

		return clipboard;
	}
	
	/**
	  * Method for decoding an source object ID to the actual source object.
	  * We determine the profile, connection and subsystem, and then
	  * we use the SubSystem.getObjectWithKey() method to get at the
	  * object.
	  *
	  */
	private Object getObjectFor(String str)
	{
		// first extract subsystem id
		int connectionDelim = str.indexOf(":");
		int subsystemDelim = str.indexOf(":", connectionDelim + 1);

		String subSystemId = str.substring(0, subsystemDelim);
		String srcKey = str.substring(subsystemDelim + 1, str.length());

		ISystemRegistry registry = RSEUIPlugin.getTheSystemRegistry();
		ISubSystem subSystem = registry.getSubSystem(subSystemId);
		if (subSystem != null)
		{
			Object result = null;
			try
			{
				result = subSystem.getObjectWithAbsoluteName(srcKey);
			}
			catch (SystemMessageException e)
			{
				return e.getSystemMessage();
			}
			catch (Exception e)
			{
			}
			if (result != null)
			{
				return result;
			}
			else
			{
				SystemMessage msg = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_ERROR_FILE_NOTFOUND);
				msg.makeSubstitution(srcKey, subSystem.getHostAliasName());
				return msg;
			}
		}
		else
		{
			SystemMessage msg = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_ERROR_CONNECTION_NOTFOUND);
			msg.makeSubstitution(subSystemId);
			return msg;
		}
	}
	
	public List getSystemClipboardObjects(int srcType)
	{
		Clipboard clipboard = getSystemClipboard();
		ArrayList srcObjects = new ArrayList();
		Object object = null;

		if (srcType == SystemDNDTransferRunnable.SRC_TYPE_RSE_RESOURCE)
		{

			// determine the source objects
			object = clipboard.getContents(PluginTransfer.getInstance());

			if (object instanceof PluginTransferData)
			{
				// RSE transfer
				PluginTransferData data = (PluginTransferData) object;
				byte[] result = data.getData();

				//StringTokenizer tokenizer = new StringTokenizer(new String(result), SystemViewDataDropAdapter.RESOURCE_SEPARATOR);
				String[] tokens = (new String(result)).split("\\"+SystemViewDataDropAdapter.RESOURCE_SEPARATOR);
				
				for (int i = 0;i < tokens.length; i++)
				{
					String srcStr = tokens[i];

					Object srcObject = getObjectFor(srcStr);
					srcObjects.add(srcObject);
				}
			}
		}
		else if (srcType == SystemDNDTransferRunnable.SRC_TYPE_ECLIPSE_RESOURCE)
		{
			// Resource transfer
			ResourceTransfer resTransfer = ResourceTransfer.getInstance();
			object = clipboard.getContents(resTransfer);
			if (object != null)
			{
				IResource[] resourceData = (IResource[]) object;
				for (int i = 0; i < resourceData.length; i++)
				{
					srcObjects.add(resourceData[i]);
				}
			}
		}

		else if (srcType == SystemDNDTransferRunnable.SRC_TYPE_OS_RESOURCE)
		{
			// Local File transfer
			FileTransfer fileTransfer = FileTransfer.getInstance();
			object = clipboard.getContents(fileTransfer);
			if (object != null)
			{
				String[] fileData = (String[]) object;
				if (fileData != null)
				{
					for (int i = 0; i < fileData.length; i++)
					{
						srcObjects.add(fileData[i]);
					}
				}
			}
		}
		else if (srcType == SystemDNDTransferRunnable.SRC_TYPE_TEXT)
		{
			TextTransfer textTransfer = TextTransfer.getInstance();
			object = clipboard.getContents(textTransfer);
			if (object != null)
			{
				String textData = (String) object;
				if (textData != null)
				{
					srcObjects.add(textData);
				}
			}
		}
		return srcObjects;
	}
	
    /*
     * Returns the remote systems scratchpad root
     */
    public SystemScratchpad getSystemScratchPad()
    {
        if (scratchpad == null)
        {
            scratchpad = new SystemScratchpad();
        }
        return scratchpad;
    }

	
	/**
	 * Convenience method to create a local connection, as it often that one is needed
	 *  for access to the local file system.
	 * @param profile - the profile to create this connection in. If null is passed, we first
	 *   try to find the default private profile and use it, else we take the first active profile.
	 * @param name - the name to give this profile. Must be unique and non-null.
	 * @param userId - the user ID to use as the default for the subsystems. Can be null.
	 */
	public IHost createLocalHost(ISystemProfile profile, String name, String userId)
	{
		IHost localConn = null;
		if (profile == null)
			profile = getSystemProfileManager().getDefaultPrivateSystemProfile();
		if (profile == null)
			profile = getSystemProfileManager().getActiveSystemProfiles()[0];
		 
		try
		{
	  	 	localConn = createHost(
		  			profile.getName(), IRSESystemType.SYSTEMTYPE_LOCAL,
		  			name, // connection name
		  			"localhost", // hostname
		  			"", // description
		  			// DY:  defect 42101, description cannot be null
		  			// null, // description
		  			userId, // default user Id
		  			ISystemUserIdConstants.USERID_LOCATION_DEFAULT_SYSTEMTYPE, null);

		}
		catch (Exception exc)
		{
			SystemBasePlugin.logError("Error creating local connection", exc);
		}
		return localConn;
	}
	
	/**
	 * Create a connection object, given the connection pool and given all the possible attributes.
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
	 *                    systemTypes extension point.
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
	public IHost createHost(
		String profileName,
		String systemType,
		String connectionName,
		String hostName,
		String description,
		String defaultUserId,
		int defaultUserIdLocation,
		ISystemNewConnectionWizardPage[] newConnectionWizardPages)
		throws Exception
	{
		lastException = null;
		ISystemHostPool pool = getHostPool(profileName);
		IHost conn = null;
		boolean promptable = false; // systemType.equals(IRSESystemType.SYSTEMTYPE_PROMPT);
		try
		{
			// create, register and save new connection...
			if ((defaultUserId != null) && (defaultUserId.length() == 0))
				defaultUserId = null;
			conn = pool.createHost(systemType, connectionName, hostName, description, defaultUserId, defaultUserIdLocation);
			if (conn == null) // conn already exists
			{
				conn = pool.getHost(connectionName);
			}
			if (promptable)
				conn.setPromptable(true);
		}
		catch (Exception exc)
		{
			lastException = exc;
			SystemBasePlugin.logError("Exception in createConnection for " + connectionName, exc);
			throw exc;
		}
		if ((lastException == null) && !promptable)
		{
			// only 1 factory used per service type
			ISubSystemConfiguration[] factories = getSubSystemConfigurationsBySystemType(systemType, true);
			ISubSystem subSystems[] = new ISubSystem[factories.length];
			for (int idx = 0; idx < factories.length; idx++)
			{
				subSystems[idx] = factories[idx].createSubSystem(conn, true, getApplicableWizardPages(factories[idx], newConnectionWizardPages)); // give it the opportunity to create a subsystem
			}
			

			//int eventType = ISystemResourceChangeEvent.EVENT_ADD;       
			int eventType = ISystemResourceChangeEvents.EVENT_ADD_RELATIVE;
			SystemResourceChangeEvent event = new SystemResourceChangeEvent(conn, eventType, this);
			//event.setPosition(pool.getConnectionPosition(conn));
			//event.setPosition(getConnectionPositionInView(conn));
			event.setRelativePrevious(getPreviousHost(conn));
			fireEvent(event);
			fireModelChangeEvent(SYSTEM_RESOURCE_ADDED, SYSTEM_RESOURCETYPE_CONNECTION, conn, null);
		
			for (int s = 0; s < subSystems.length; s++)
			{
				ISubSystem ss = subSystems[s];
				fireModelChangeEvent(ISystemModelChangeEvents.SYSTEM_RESOURCE_ADDED, ISystemModelChangeEvents.SYSTEM_RESOURCETYPE_SUBSYSTEM, ss, null);						
			}
			

		}
		SystemPreferencesManager.getPreferencesManager().setConnectionNamesOrder(); // update preferences order list                
	
		RSEUIPlugin.getThePersistenceManager().commit(conn);
		return conn;
	}
	private ISystemNewConnectionWizardPage[] getApplicableWizardPages(ISubSystemConfiguration ssf, ISystemNewConnectionWizardPage[] allPages)
	{
		if ((allPages == null) || (allPages.length == 0))
			return null;
		int count = 0;
		for (int idx = 0; idx < allPages.length; idx++)
			if (allPages[idx].getSubSystemConfiguration() == ssf)
				++count;
		if (count == 0)
			return null;
		ISystemNewConnectionWizardPage[] subPages = new ISystemNewConnectionWizardPage[count];
		count = 0;
		for (int idx = 0; idx < allPages.length; idx++)
			if (allPages[idx].getSubSystemConfiguration() == ssf)
				subPages[count++] = allPages[idx];
		return subPages;
	}
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
	 *                    systemTypes extension point.
	 * @param connectionName unique connection name.
	 * @param hostName ip name of host.
	 * @param description optional description of the connection. Can be null.
	 * @return SystemConnection object, or null if it failed to create. This is typically
	 *   because the connectionName is not unique. Call getLastException() if necessary.
	 */
	public IHost createHost(String profileName, String systemType, String connectionName, String hostName, String description)
		throws Exception
	{
		return createHost(profileName, systemType, connectionName, hostName, description, null, ISystemUserIdConstants.USERID_LOCATION_CONNECTION, null);  
	}
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
	 *                    systemTypes extension point.
	 * @param connectionName unique connection name.
	 * @param hostName ip name of host.
	 * @param description optional description of the connection. Can be null.
	 * @return SystemConnection object, or null if it failed to create. This is typically
	 *   because the connectionName is not unique. Call getLastException() if necessary.
	 */
	public IHost createHost(String systemType, String connectionName, String hostName, String description)
		throws Exception
	{
		ISystemProfile profile = getSystemProfileManager().getDefaultPrivateSystemProfile();
		if (profile == null)
			profile = getSystemProfileManager().getActiveSystemProfiles()[0];
		return createHost(profile.getName(), systemType, connectionName, hostName, description, null, ISystemUserIdConstants.USERID_LOCATION_CONNECTION, null);  
	}
	/**
	 * Return the previous connection as would be shown in the view
	 */
	protected IHost getPreviousHost(IHost conn)
	{
		IHost prevConn = null;
		ISystemHostPool pool = conn.getHostPool();
		int pos = pool.getHostPosition(conn);
		if (pos > 0)
			prevConn = pool.getHost(pos - 1);
		else
		{
			IHost allConns[] = getHosts();
			if (allConns != null)
			{
				pos = getHostPositionInView(conn);
				if (pos > 0)
					prevConn = allConns[pos - 1];
			}
		}
		return prevConn;
	}
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
	 *                    systemTypes extension point.
	 * @param connectionName unique connection name.
	 * @param hostName ip name of host.
	 * @param description optional description of the connection. Can be null.
	 * @param defaultUserId userId to use as the default for the subsystems.
	 * @param defaultUserIdLocation one of the constants in {@link org.eclipse.rse.core.ISystemUserIdConstants ISystemUserIdConstants}
	 *   that tells us where to set the user Id
	 */
	public void updateHost(Shell shell, IHost conn, String systemType, String connectionName, String hostName, String description, String defaultUserId, int defaultUserIdLocation)
	{
		lastException = null;
		boolean connectionNameChanged = !connectionName.equalsIgnoreCase(conn.getAliasName());
		boolean hostNameChanged = !hostName.equalsIgnoreCase(conn.getHostName());
		String orgDefaultUserId = conn.getDefaultUserId();
		boolean defaultUserIdChanged = false;
		if ((defaultUserId == null) || (orgDefaultUserId == null))
		{
			if (orgDefaultUserId != defaultUserId)
				defaultUserIdChanged = true;
		}
		else
			defaultUserIdChanged = !conn.compareUserIds(defaultUserId, orgDefaultUserId); // d43219
		//!defaultUserId.equalsIgnoreCase(orgDefaultUserId);    	

		try
		{
			if (connectionNameChanged)
				renameHost(conn, connectionName);
			conn.getHostPool().updateHost(conn, systemType, connectionName, hostName, description, defaultUserId, defaultUserIdLocation);
		}
		catch (SystemMessageException exc)
		{
			SystemBasePlugin.logError("Exception in updateConnection for " + connectionName, exc);
			lastException = exc;
			return;
		}
		catch (Exception exc)
		{
			SystemBasePlugin.logError("Exception in updateConnection for " + connectionName, exc);
			lastException = exc;
			return;
		}
		boolean skipUpdate = (defaultUserIdChanged && !hostNameChanged && !connectionNameChanged);
		if (!skipUpdate) fireEvent(new org.eclipse.rse.model.SystemResourceChangeEvent(
		//conn,ISystemResourceChangeEvent.EVENT_CHANGE,this));
		conn, ISystemResourceChangeEvents.EVENT_PROPERTY_CHANGE, this)); // only update simple property sheet values here
		if (!skipUpdate) fireModelChangeEvent(SYSTEM_RESOURCE_CHANGED, SYSTEM_RESOURCETYPE_CONNECTION, conn, null);

		if (hostNameChanged || defaultUserIdChanged)
		{
			ISubSystem[] subsystems = getSubSystems(conn); // get list of related subsystems
			for (int idx = 0; idx < subsystems.length; idx++)
			{
				if (hostNameChanged || (subsystems[idx].getLocalUserId() == null))
				{
					try
					{
						if (subsystems[idx].isConnected()) subsystems[idx].disconnect(shell); // MJB: added conditional for defect 45754
						if (defaultUserIdChanged)
						{
							subsystems[idx].getConnectorService().clearUserIdCache();
						}
						subsystems[idx].getConnectorService().clearPasswordCache();
					}
					catch (Exception exc)
					{
					} // msg already shown    	  	   
				}
			}
		}
	}

	/**
	 * Update the workoffline attribute for a connection.
	 * 
	 * @param conn SystemConnection to change
	 * @param offline true if connection should be set offline, false if it should be set online
	 */
	public void setHostOffline(IHost conn, boolean offline)
	{
		if (conn.isOffline() != offline)
		{
			conn.setOffline(offline);
			saveHost(conn);
			fireEvent(new SystemResourceChangeEvent(conn, ISystemResourceChangeEvents.EVENT_PROPERTYSHEET_UPDATE, null));
		}
	}

	/**
	 * Delete an existing connection. 
	 * <p>
	 * Lots to do here:
	 * <ul>
	 *   <li>Delete all subsystem objects for this connection, including their file's on disk.
	 *   <li>Delete this connection's private filter pool, if exists
	 *   <li>Delete the connection from memory.
	 *   <li>Delete the connection's folder from disk.
	 * </ul>
	 * Assumption: firing the delete event is done elsewhere. Specifically, the doDelete method of SystemView.
	 */
	public void deleteHost(IHost conn)
	{
		Vector affectedSubSystemFactories = getSubSystemFactories(conn);
		for (int idx = 0; idx < affectedSubSystemFactories.size(); idx++)
		{
			 ((ISubSystemConfiguration) affectedSubSystemFactories.elementAt(idx)).deleteSubSystemsByConnection(conn);
		} 
		conn.getHostPool().deleteHost(conn); // delete from memory and from disk.       
		SystemPreferencesManager.getPreferencesManager().setConnectionNamesOrder(); // update preferences order list        
		fireModelChangeEvent(SYSTEM_RESOURCE_REMOVED, SYSTEM_RESOURCETYPE_CONNECTION, conn, null);
	}

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
	public void renameHost(IHost conn, String newName) throws Exception
	{
		// first, pre-test for folder-in-use error:

		// it looks good, so proceed...
		String oldName = conn.getAliasName();
		Vector affectedSubSystemFactories = getSubSystemFactories(conn);
		for (int idx = 0; idx < affectedSubSystemFactories.size(); idx++)
			 ((ISubSystemConfiguration) affectedSubSystemFactories.elementAt(idx)).renameSubSystemsByConnection(conn, newName);
		conn.getHostPool().renameHost(conn, newName); // rename in memory and disk
		SystemPreferencesManager.getPreferencesManager().setConnectionNamesOrder(); // update preferences order list        
		fireModelChangeEvent(SYSTEM_RESOURCE_RENAMED, SYSTEM_RESOURCETYPE_CONNECTION, conn, oldName);
	}

	/**
	 * Move existing connections a given number of positions in the same profile.
	 * If the delta is negative, they are all moved up by the given amount. If 
	 * positive, they are all moved down by the given amount.<p>
	 * <ul>
	 * <li>After the move, the pool containing the moved connection is saved to disk.
	 * <li>The connection's connection name must be unique in pool.
	 * <li>Fires a single ISystemResourceChangeEvent event of type EVENT_MOVE, if the pool is the private pool.
	 * </ul>
	 * <b>TODO PROBLEM: CAN'T RE-ORDER FOLDERS SO CAN WE SUPPORT THIS ACTION?</b>
	 * @param conns Array of SystemConnections to move.
	 * @param newPosition new zero-based position for the connection
	 */
	public void moveHosts(String profileName, IHost conns[], int delta)
	{
		ISystemHostPool pool = getHostPool(profileName);
		pool.moveHosts(conns, delta);
		SystemPreferencesManager.getPreferencesManager().setConnectionNamesOrder();
		//fireEvent(new SystemResourceChangeEvent(pool.getSystemConnections(),ISystemResourceChangeEvent.EVENT_MOVE_MANY,this));
		SystemResourceChangeEvent event = new SystemResourceChangeEvent(conns, ISystemResourceChangeEvents.EVENT_MOVE_MANY, this);
		event.setPosition(delta);
		fireEvent(event);
		// fire new model change event, which BPs might listen for...
		for (int idx=0; idx<conns.length; idx++)
			fireModelChangeEvent(SYSTEM_RESOURCE_REORDERED, SYSTEM_RESOURCETYPE_CONNECTION, conns[idx], null);
	}

	/**
	 * Copy a SystemConnection. All subsystems are copied, and all connection data is copied.
	 * @param monitor Progress monitor to reflect each step of the operation
	 * @param conn The connection to copy
	 * @param targetProfile What profile to copy into
	 * @param newName Unique name to give copied profile
	 * @return new SystemConnection object
	 */
	public IHost copyHost(IProgressMonitor monitor, IHost conn, ISystemProfile targetProfile, String newName) throws Exception
	{
		Exception lastExc = null;
		boolean failed = false;
		String msg = null;
		String oldName = conn.getAliasName();
		ISystemHostPool oldPool = conn.getHostPool();
		ISystemHostPool targetPool = getHostPool(targetProfile);
		IHost newConn = null;

		SystemBasePlugin.logDebugMessage(this.getClass().getName(), "Start of system connection copy. From: " + oldName + " to: " + newName);

		// STEP 0: BRING ALL IMPACTED SUBSYSTEM FACTORIES TO LIFE NOW, BEFORE DOING THE CLONE.
		getSubSystemFactories(conn);
		if (errorLoadingFactory)
			return null;

		try
		{
			// STEP 1: COPY CONNECTION ITSELF, MINUS ITS SUBSYSTEMS...
			newConn = oldPool.cloneHost(targetPool, conn, newName);

			// STEP 2: COPY ALL SUBSYSTEMS FOR THE COPIED CONNECTION
			msg = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_COPYSUBSYSTEMS_PROGRESS).getLevelOneText();
			//monitor.subTask(msg);
			SystemBasePlugin.logDebugMessage(this.getClass().getName(), msg);

			ISubSystem[] subsystems = null;
			ISubSystemConfiguration factory = null;
			msg = "Copying subsystems for connection " + conn.getAliasName();
			//monitor.subTask(msg);
			SystemBasePlugin.logDebugMessage(this.getClass().getName(), msg);
			subsystems = getSubSystems(conn); // get old subsystems for this connection
			if ((subsystems != null) && (subsystems.length > 0))
			{
				for (int jdx = 0; jdx < subsystems.length; jdx++)
				{
					msg += ": subsystem " + subsystems[jdx].getName();
					//monitor.subTask(msg);
					SystemBasePlugin.logDebugMessage(this.getClass().getName(), msg);
					factory = subsystems[jdx].getSubSystemConfiguration();
					factory.cloneSubSystem(subsystems[jdx], newConn, false); // false=>copy connection op vs copy profile op
					//try { java.lang.Thread.sleep(3000l); } catch (InterruptedException e) {}
				}
			}
			//monitor.worked(1);                
		}
		catch (Exception exc)
		{
			failed = true;
			lastExc = exc;
		}
		// if anything failed, we have to back out what worked. Ouch!
		if (failed)
		{
			try
			{
				if (newConn != null)
					deleteHost(newConn);
			}
			catch (Exception exc)
			{
				SystemBasePlugin.logError("Exception (ignored) cleaning up from copy-connection exception.", exc);
			}
			throw (lastExc);
		}
		SystemBasePlugin.logDebugMessage(this.getClass().getName(), "Copy of system connection " + oldName + " to " + newName + " successful");
		if (getSystemProfileManager().isSystemProfileActive(targetProfile.getName()))
		{
			int eventType = ISystemResourceChangeEvents.EVENT_ADD_RELATIVE;
			SystemResourceChangeEvent event = new SystemResourceChangeEvent(newConn, eventType, this);
			event.setRelativePrevious(getPreviousHost(newConn));
			//SystemResourceChangeEvent event = new SystemResourceChangeEvent(newConn,ISystemResourceChangeEvent.EVENT_ADD,this);
			//event.setPosition(getConnectionPositionInView(newConn));
			fireEvent(event);
		}
		fireModelChangeEvent(SYSTEM_RESOURCE_ADDED, SYSTEM_RESOURCETYPE_CONNECTION, newConn, null);
		return newConn;
	}

	/**
	 * Move a SystemConnection to another profile. All subsystems are moved, and all connection data is moved.
	 * This is actually accomplished by doing a copy operation first, and if successful deleting the original.
	 * @param monitor Progress monitor to reflect each step of the operation
	 * @param conn The connection to move
	 * @param targetProfile What profile to move into
	 * @param newName Unique name to give copied profile. Typically this is the same as the original name, but 
	 *                will be different on name collisions
	 * @return new SystemConnection object
	 */
	public IHost moveHost(IProgressMonitor monitor, IHost conn, ISystemProfile targetProfile, String newName) throws Exception
	{
		IHost newConn = null;
		try
		{
			newConn = copyHost(monitor, conn, targetProfile, newName);
			if (newConn != null)
			{
				deleteHost(conn); // delete old connection now that new one created successfully
				SystemBasePlugin.logDebugMessage(this.getClass().getName(), "Move of system connection " + conn.getAliasName() + " to profile " + targetProfile.getName() + " successful");
				fireEvent(new SystemResourceChangeEvent(conn, ISystemResourceChangeEvents.EVENT_DELETE, this));
			}
		}
		catch (Exception exc)
		{
			//RSEUIPlugin.logError("Exception moving system connection " + conn.getAliasName() + " to profile " + targetProfile.getName(), exc);
			throw exc;
		}
		return newConn;
	}

	/**
	 * Return true if any of the subsystems for the given connection are currently connected
	 */
	public boolean isAnySubSystemConnected(IHost conn)
	{
		boolean any = false;
		ISubSystem[] subsystems = getSubSystemsLazily(conn);
		if (subsystems == null)
			return false;
		for (int idx = 0; !any && (idx < subsystems.length); idx++)
		{
			ISubSystem ss = subsystems[idx];
			if (ss.isConnected())
				any = true;
		}
		return any;
	}
	
	/**
	 * Return true if all of the subsystems for the given connection are currently connected
	 */
	public boolean areAllSubSystemsConnected(IHost conn)
	{
		boolean all = true;
		ISubSystem[] subsystems = getSubSystemsLazily(conn);
		if (subsystems == null)
			return false;
		
		for (int idx = 0; all && (idx < subsystems.length); idx++)
		{
			ISubSystem ss = subsystems[idx];
			if (!ss.isConnected())
			{
			    return false;
			}
		}
		return all;
	}
	/**
	 * Disconnect all subsystems for the given connection, if they are currently connected.
	 */
	public void disconnectAllSubSystems(IHost conn)
	{
		// FIXME - save profile
		save();
		
		ISubSystem[] subsystems = getSubSystemsLazily(conn);
		if (subsystems == null)
			return;

		// dy:  defect 47281, user repeaetedly prompted to disconnect if there is an open file
		// and they keep hitting cancel. 
		boolean cancelled = false;
		for (int idx = 0; idx < subsystems.length && !cancelled; idx++)
		{
			ISubSystem ss = subsystems[idx];
			if (ss.isConnected())
			{
				try
				{
					//ss.getConnectorService().disconnect(); defect 40675         	 
					Shell shell = null;
					if (Display.getCurrent() != null)
						shell = Display.getCurrent().getActiveShell();
					ss.disconnect(shell);
				}
				catch (InterruptedException exc)
				{
					System.out.println("Cacnelled");
					cancelled = true;
				}
				catch (Exception exc)
				{
				}
			}
		}
	}

	/**
	 * Inform the world when the connection status changes for a subsystem within a connection.
	 * Update properties for the subsystem and its connection
	 */
	public void connectedStatusChange(ISubSystem subsystem, boolean connected, boolean wasConnected)
	{
		connectedStatusChange(subsystem, connected, wasConnected, true);
	}

	/**
	 * Inform the world when the connection status changes for a subsystem within a connection.
	 * Update properties for the subsystem and its connection
	 */
	public void connectedStatusChange(ISubSystem subsystem, boolean connected, boolean wasConnected, boolean collapseTree)
	{
		//System.out.println("INSIDE CONNECTEDSTATUSCHANGE: "+connected+" vs "+wasConnected);
		IHost conn = subsystem.getHost();
		//int eventId = ISystemResourceChangeEvent.EVENT_CHANGE;
		//int eventId = ISystemResourceChangeEvent.EVENT_PROPERTY_CHANGE;
		if (connected != wasConnected)
		{
			int eventId = ISystemResourceChangeEvents.EVENT_ICON_CHANGE;
			fireEvent(new SystemResourceChangeEvent(conn, eventId, this));
			
			SystemResourceChangeEvent event = new SystemResourceChangeEvent(subsystem, eventId, conn);
			fireEvent(event);
			
			// DKM
			// fire for each subsystem
			ISubSystem[] sses = getSubSystems(conn);
			for (int i = 0; i < sses.length; i++)
			{
			    ISubSystem ss = sses[i];
			    if (ss != subsystem)
			    {
			        SystemResourceChangeEvent sevent = new SystemResourceChangeEvent(ss, eventId, conn);
					fireEvent(sevent);
					
					sevent.setType(ISystemResourceChangeEvents.EVENT_PROPERTY_CHANGE); // update vrm
					fireEvent(sevent);
			    }
			}
			
			
			// DY:  Conditioning of property change event type has been removed so
			// that the connected property is updated on a disconnect.
			//if (connected)        
			event.setType(ISystemResourceChangeEvents.EVENT_PROPERTY_CHANGE); // update vrm

			fireEvent(event);
		}
		if (!connected && wasConnected && collapseTree)
		{
			invalidateFiltersFor(subsystem);
			fireEvent(new SystemResourceChangeEvent(subsystem, ISystemResourceChangeEvents.EVENT_MUST_COLLAPSE, this));
			
			ISubSystem[] sses = getSubSystems(conn);
			for (int i = 0; i < sses.length; i++)
			{
			    ISubSystem ss = sses[i];
			    if (ss != subsystem && !ss.isConnected())
			    {
			    	invalidateFiltersFor(ss);
			        SystemResourceChangeEvent sevent = new SystemResourceChangeEvent(ss, ISystemResourceChangeEvents.EVENT_MUST_COLLAPSE, conn);
					fireEvent(sevent);
			    }
			}
		}
	}

	// ----------------------------
	// RESOURCE EVENT METHODS...
	// ----------------------------            

	/**
	 * Register your interest in being told when a system resource such as a connection is changed.
	 */
	public void addSystemResourceChangeListener(ISystemResourceChangeListener l)
	{
		listenerManager.addSystemResourceChangeListener(l);
		listenerCount++;
	}
	/**
	 * De-Register your interest in being told when a system resource such as a connection is changed.
	 */
	public void removeSystemResourceChangeListener(ISystemResourceChangeListener l)
	{
		listenerManager.removeSystemResourceChangeListener(l);
		listenerCount--;
	}
	/**
	 * Query if the ISystemResourceChangeListener is already listening for SystemResourceChange events
	 */
	public boolean isRegisteredSystemResourceChangeListener(ISystemResourceChangeListener l)
	{
		return listenerManager.isRegisteredSystemResourceChangeListener(l);
	}
	/**
	 * Notify all listeners of a change to a system resource such as a connection.
	 * You would not normally call this as the methods in this class call it when appropriate.
	 */
	public void fireEvent(ISystemResourceChangeEvent event)
	{
	    Object src = event.getSource();
	    if (src instanceof ISystemFilter)
	    {
	        ISystemBaseReferencingObject[] references = ((ISystemFilter)src).getReferencingObjects();
	        for (int i = 0; i < references.length; i++)
	        {
	            ISystemBaseReferencingObject ref = references[i];
	            if (ref instanceof ISystemContainer)
	            {
	                ((ISystemContainer)ref).markStale(true);
	            }
	        }
	        
	    }
		listenerManager.notify(event);
	}
	/**
	 * Notify a specific listener of a change to a system resource such as a connection.
	 */
	public void fireEvent(ISystemResourceChangeListener l, ISystemResourceChangeEvent event)
	{
		l.systemResourceChanged(event);
	}
	/**
	 * Notify all listeners of a change to a system resource such as a connection.
	 * You would not normally call this as the methods in this class call it when appropriate.
	 * <p>
	 * This version calls fireEvent at the next reasonable opportunity, leveraging SWT's 
	 * Display.asyncExec() method.
	 */
	public void postEvent(ISystemResourceChangeEvent event)
	{
		listenerManager.postNotify(event);
	}
	
	/**
	 * Notify a specific listener of a change to a system resource such as a connection.
	 * <p>
	 * This version calls fireEvent at the next reasonable opportunity, leveraging SWT's 
	 * Display.asyncExec() method.
	 */
	public void postEvent(ISystemResourceChangeListener listener, ISystemResourceChangeEvent event)
	{
		new SystemPostableEventNotifier(listener, event); // create and run the notifier
	}

	// ----------------------------
	// MODEL RESOURCE EVENT METHODS...
	// ----------------------------            

	/**
	 * Register your interest in being told when an RSE model resource is changed.
	 * These are model events, not GUI-optimized events.
	 */
	public void addSystemModelChangeListener(ISystemModelChangeListener l)
	{
		modelListenerManager.addSystemModelChangeListener(l);
		modelListenerCount++;
	}
	/**
	 * De-Register your interest in being told when an RSE model resource is changed.
	 */
	public void removeSystemModelChangeListener(ISystemModelChangeListener l)
	{
		modelListenerManager.removeSystemModelChangeListener(l);
		modelListenerCount--;
	}
	/**
	 * Notify all listeners of a change to a system model resource such as a connection.
	 * You would not normally call this as the methods in this class call it when appropriate.
	 */
	public void fireEvent(ISystemModelChangeEvent event)
	{
		modelListenerManager.notify(event);
	}
	/**
	 * Notify all listeners of a change to a system model resource such as a connection.
	 * This one takes the information needed and creates the event for you.
	 */
	public void fireModelChangeEvent(int eventType, int resourceType, Object resource, String oldName)
	{
		if (modelEvent == null)
			modelEvent = new SystemModelChangeEvent();
		modelEvent.setEventType(eventType);
		modelEvent.setResourceType(resourceType);
		modelEvent.setResource(resource);
		modelEvent.setOldName(oldName);
		modelListenerManager.notify(modelEvent);
	}
	/**
	 * Notify a specific listener of a change to a system model resource such as a connection.
	 */
	public void fireEvent(ISystemModelChangeListener l, ISystemModelChangeEvent event)
	{
		l.systemModelResourceChanged(event);
	}

	// --------------------------------
	// REMOTE RESOURCE EVENT METHODS...
	// --------------------------------            

	/**
	 * Register your interest in being told when a remote resource is changed.
	 * These are model events, not GUI-optimized events.
	 */
	public void addSystemRemoteChangeListener(ISystemRemoteChangeListener l)
	{
		remoteListManager.addSystemRemoteChangeListener(l);
		remoteListCount++;
	}
	/**
	 * De-Register your interest in being told when a remote resource is changed.
	 */
	public void removeSystemRemoteChangeListener(ISystemRemoteChangeListener l)
	{
		remoteListManager.removeSystemRemoteChangeListener(l);
		remoteListCount--;
	}
	/**
	 * Notify all listeners of a change to a remote resource such as a file.
	 * You would not normally call this as the methods in this class call it when appropriate.
	 */
	public void fireEvent(ISystemRemoteChangeEvent event)
	{
		remoteListManager.notify(event);
	}
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
	public void fireRemoteResourceChangeEvent(int eventType, Object resource, Object resourceParent, ISubSystem subsystem, String oldName, Viewer originatingViewer)
	{
		if (resourceParent instanceof ISystemContainer)
		{
			((ISystemContainer)resourceParent).markStale(true);
		}
		// mark stale any filters that reference this object
		invalidateFiltersFor(resourceParent, subsystem);
		
		if (remoteEvent == null)
			remoteEvent = new SystemRemoteChangeEvent();
		remoteEvent.setEventType(eventType);
		remoteEvent.setResource(resource);
		remoteEvent.setResourceParent(resourceParent);
		remoteEvent.setOldName(oldName);
		remoteEvent.setSubSystem(subsystem);
		remoteEvent.setOriginatingViewer(originatingViewer);
		remoteListManager.notify(remoteEvent);
	}
	
    /**
     * Returns the implementation of ISystemRemoteElement for the given
     * object.  Returns null if this object does not adaptable to this.
     */
    protected ISystemRemoteElementAdapter getRemoteAdapter(Object o) 
    {
    	return SystemAdapterHelpers.getRemoteAdapter(o);
    }
    
	 private String getRemoteResourceAbsoluteName(Object remoteResource)
	    {
	    	if (remoteResource == null)
	    	  return null;
	    	String remoteResourceName = null;
	        if (remoteResource instanceof String)
	    	  remoteResourceName = (String)remoteResource;    	  
	    	else
	    	{
	    		ISystemRemoteElementAdapter ra = getRemoteAdapter(remoteResource);
	    		if (ra == null)
	    		  return null;
	    		remoteResourceName = ra.getAbsoluteName(remoteResource);
	    	}
	    	return remoteResourceName;    	
	    }
	 
	 private List findFilterReferencesFor(ISubSystem subsystem)
		{
		   List results = new ArrayList();
		    if (subsystem != null)
		    {
		    	ISystemFilterPoolReferenceManager refmgr = subsystem.getFilterPoolReferenceManager();
		    	if (refmgr != null)
		    	{
				    ISystemFilterReference[] refs = refmgr.getSystemFilterReferences(subsystem);
				    for (int i = 0; i < refs.length; i++)
				    {
				        ISystemFilterReference filterRef = refs[i];
				   
				        if (!filterRef.isStale() && filterRef.hasContents(SystemChildrenContentsType.getInstance()))
				        {
				        	results.add(filterRef);
				        }
				    }
				    
		    	}
		    }
		    return results;
		
	    }
	 
	public List findFilterReferencesFor(Object resource, ISubSystem subsystem)
	{
	    String elementName = getRemoteResourceAbsoluteName(resource);
	   List results = new ArrayList();
	    if (subsystem != null && elementName != null)
	    {
		    ISystemFilterReference[] refs = subsystem.getFilterPoolReferenceManager().getSystemFilterReferences(subsystem);
		    for (int i = 0; i < refs.length; i++)
		    {
		        ISystemFilterReference filterRef = refs[i];
		   
		        if (!filterRef.isStale() && filterRef.hasContents(SystemChildrenContentsType.getInstance()))
		        {
		    	    	// #1
		    	    	if (subsystem.doesFilterMatch(filterRef.getReferencedFilter(), elementName))
		    	    	{
		    	  	       results.add(filterRef); // found a match!
	
		    	    	}
		    	    	// #2
		    	    	else if (subsystem.doesFilterListContentsOf(filterRef.getReferencedFilter(),elementName))
		    	    	{
		    	    	    results.add(filterRef); // found a match!
		   	    	    } 
		        }
		    }
		    
		  
	    }
	    return results;
	
    }
	
	public void invalidateFiltersFor(ISubSystem subsystem)
	{	    
	    if (subsystem != null)
	    {
		    
	        List results = findFilterReferencesFor(subsystem);
	        for (int i = 0; i < results.size(); i++)
	        {
	            ((ISystemFilterReference)results.get(i)).markStale(true);
	        }
	    }
	}
	
	public void invalidateFiltersFor(Object resourceParent, ISubSystem subsystem)
	{	    
	    if (subsystem != null)
	    {
		    
	        List results = findFilterReferencesFor(resourceParent, subsystem);
	        for (int i = 0; i < results.size(); i++)
	        {
	            ((ISystemFilterReference)results.get(i)).markStale(true);
	        }
	    }
	}
	
	/**
	 * Notify a specific listener of a change to a remote resource such as a file.
	 */
	public void fireEvent(ISystemRemoteChangeListener l, ISystemRemoteChangeEvent event)
	{
		l.systemRemoteResourceChanged(event);
	}

	// ----------------------------
	// PREFERENCE EVENT METHODS...
	// ----------------------------            

	/**
	 * Register your interest in being told when a system preference changes
	 */
	public void addSystemPreferenceChangeListener(ISystemPreferenceChangeListener l)
	{
		preferenceListManager.addSystemPreferenceChangeListener(l);
	}
	/**
	 * De-Register your interest in being told when a system preference changes
	 */
	public void removeSystemPreferenceChangeListener(ISystemPreferenceChangeListener l)
	{
		preferenceListManager.removeSystemPreferenceChangeListener(l);
	}
	/**
	 * Notify all listeners of a change to a system preference
	 * You would not normally call this as the methods in this class call it when appropriate.
	 */
	public void fireEvent(ISystemPreferenceChangeEvent event)
	{
		preferenceListManager.notify(event);
	}
	/**
	 * Notify a specific listener of a change to a system preference 
	 */
	public void fireEvent(ISystemPreferenceChangeListener l, ISystemPreferenceChangeEvent event)
	{
		l.systemPreferenceChanged(event);
	}

	// ----------------------------
	// MISCELLANEOUS METHODS...
	// ----------------------------

	/**
	 * Load everything into memory. Needed for pervasive operations like rename and copy
	 */
	public void loadAll()
	{
		// step 0_a: force every subsystem factory to be active
		ISubSystemConfigurationProxy[] proxies = getSubSystemConfigurationProxies();
		if (proxies != null)
		{
			for (int idx = 0; idx < proxies.length; idx++)
				proxies[idx].getSubSystemConfiguration();
		}

		// step 0_b: force every subsystem of every connection to be active!
		IHost[] connections = getHosts();
		for (int idx = 0; idx < connections.length; idx++)
			getSubSystems(connections[idx]);
	}

	/**
	 * Return last exception object caught in any method, or null if no exception.
	 * This has the side effect of clearing the last exception.
	 */
	public Exception getLastException()
	{
		Exception last = lastException;
		lastException = null;
		return last;
	}

	// ----------------------------
	// SAVE / RESTORE METHODS...
	// ----------------------------            

	/**
	 * Save everything! 
	 */
	public boolean save()
	{
		ISystemProfileManager profileManager = getSystemProfileManager();
		return RSEUIPlugin.getThePersistenceManager().commit(profileManager);
	}

	/**
	 * Save specific connection pool
	 * @return true if saved ok, false if error encountered. If false, call getLastException().
	 */
	public boolean saveHostPool(ISystemHostPool pool)
	{
		return RSEUIPlugin.getThePersistenceManager().commit(pool);
	}

	/**
	 * Save specific connection
	 * @return true if saved ok, false if error encountered. If false, call getLastException().
	 */
	public boolean saveHost(IHost conn)
	{
		return RSEUIPlugin.getThePersistenceManager().commit(conn);
	}

	/**
	 * Restore all connections within active profiles
	 * @return true if restored ok, false if error encountered. If false, call getLastException().
	 */
	public boolean restore()
	{
		boolean ok = true;
		lastException = null;
		/*
		SystemProfileManager profileManager = SystemStartHere.getSystemProfileManager();

		SystemHostPool pool = null;
		SystemPreferencesManager prefmgr = SystemPreferencesManager.getPreferencesManager();
		if (!RSEUIPlugin.getThePersistenceManager().restore(profileManager))
		{
			SystemProfile[] profiles = profileManager.getActiveSystemProfiles();
			for (int idx = 0; idx < profiles.length; idx++)
			{
				try
				{
					pool = SystemHostPoolImpl.getSystemConnectionPool(profiles[idx]);
					Host[] conns = pool.getHosts();
					pool.orderHosts(prefmgr.getConnectionNamesOrder(conns, pool.getName()));
				}
				catch (Exception exc)
				{
					lastException = exc;
					RSEUIPlugin.logError("Exception in restore for connection pool " + profiles[idx].getName(), exc);
				}
			}
		}
		*/
		return ok;
	}
	public boolean contains(ISchedulingRule rule)
	{
		return rule == this;
	}
	public boolean isConflicting(ISchedulingRule rule)
	{
		return rule == this;
	} 
}//SystemRegistryImpl
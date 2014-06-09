/********************************************************************************
 * Copyright (c) 2006, 2014 IBM Corporation and others. All rights reserved.
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
 * Michael Scharf (Wind River) - patch for an NPE in getSubSystemConfigurations()
 * David Dykstal (IBM) - moved SystemsPreferencesManager to a new package
 * Uwe Stieber (Wind River) - bugfixing
 * David Dykstal (IBM) - 168977: refactoring IConnectorService and ServerLauncher hierarchies
 * Martin Oberhuber (Wind River) - [175262] IHost.getSystemType() should return IRSESystemType
 * David Dykstal (IBM) - 142806: refactoring persistence framework
 * Tobias Schwarz (Wind River) - [183134] getLocalHost() does not return Local
 * Martin Oberhuber (Wind River) - [168975] Move RSE Events API to Core
 * Martin Oberhuber (Wind River) - [184095] Replace systemTypeName by IRSESystemType
 * Martin Oberhuber (Wind River) - [177523] Unify singleton getter methods
 * Martin Oberhuber (Wind River) - [186128] Move IProgressMonitor last in all API
 * Martin Oberhuber (Wind River) - [186640] Add IRSESystemType.testProperty()
 * Martin Oberhuber (Wind River) - [186748] Move ISubSystemConfigurationAdapter from UI/rse.core.subsystems.util
 * Martin Oberhuber (Wind River) - [186773] split ISystemRegistryUI from ISystemRegistry
 * Martin Oberhuber (Wind River) - [186779] Fix IRSESystemType.getAdapter()
 * Martin Oberhuber (Wind River) - [186773] split SystemRegistryUI from SystemRegistry implementation
 * Martin Oberhuber (Wind River) - [189123] Prepare ISystemRegistry for move into non-UI
 * Martin Oberhuber (Wind River) - [189123] Move renameSubSystemProfile() from UI to Core
 * Martin Oberhuber (Wind River) - [175680] Deprecate obsolete ISystemRegistry methods
 * Martin Oberhuber (Wind River) - [190271] Move ISystemViewInputProvider to Core
 * Xuan Chen        (IBM)        - [194838] Move the code for comparing two objects by absolute name to a common location
 * David McKnight   (IBM)        - [165674] Sort subsystem configurations to be in deterministic order
 * Martin Oberhuber (Wind River) - [165674] Sort subsystem configurations by priority then Id
 * Martin Oberhuber (Wind River) - [194898] Avoid NPE when doing EVENT_REFRESH_REMOTE on a subsys without filters
 * David McKnight   (IBM)        - [207100] adding ISystemRegistry.isRegisteredSystemRemoteChangeListener
 * Martin Oberhuber (Wind River) - [206742] Make SystemHostPool thread-safe
 * David Dykstal    (IBM)        - [210537] removed exception handling for SystemHostPool, no longer needed
 * Martin Oberhuber (Wind River) - [216266] improved non-forced getSubSystems() code, removed getSubSystemsLazily()
 * David Dykstal (IBM) - [197036] wrapped createHost to commit changes only once
 *                                rewrote createHost to better pick default subsystem configurations to activate
 *                                rewrote getSubSystemConfigurationsBySystemType to be able to delay the creation (and loading) of subsystem configurations
 * David Dykstal (IBM) - [217556] remove service subsystem types
 * Martin Oberhuber (Wind River) - [215820] Move SystemRegistry implementation to Core
 * David Dykstal (IBM) - [202630] getDefaultPrivateProfile() and ensureDefaultPrivateProfile() are inconsistent
 * David McKnight   (IBM)        - [224313] [api] Create RSE Events for MOVE and COPY holding both source and destination fields
 * David Dykstal (IBM) - [200735][Persistence] Delete a profile that contains a connection and restart, profile is back without connections
 * David Dykstal (IBM) - [168976][api] move ISystemNewConnectionWizardPage from core to UI
 * Martin Oberhuber (Wind River) - [228774] Improve ElementComparer Performance
 * David McKnight (IBM) 		 - [225747] [dstore] Trying to connect to an "Offline" system throws an NPE
 * David McKnight   (IBM)		 - [229116] NPE in when editing remote file in new workspace
 * David McKnight   (IBM)        - [234057] Wrong or missing model change event
 * David Dykstal (IBM) - [227750] do not fire events if there are no listeners
 * David McKnight   (IBM)        - [238673] Expansion icon (plus sign) disappears from Work With Libraries entry
 * David McKnight   (IBM)        - [240991] RSE startup creates display on worker thread before workbench.
 * David Dykstal (IBM) - [236516] Bug in user code causes failure in RSE initialization
 * David McKnight   (IBM)        - [249247] Expand New Connections
 * David McKnight   (IBM)        - [254590] When disconnecting a subsystem with COLLAPSE option, subsystems of other connector services also get collapsed
 * Martin Oberhuber (Wind River) - [245154][api] add getSubSystemConfigurationProxiesBySystemType()
 * Zhou Renjian     (Kortide)    - [282238] NPE when copying host and overwrite itself
 * Martin Oberhuber (Wind River) - [359554] Avoid disconnect when changing default user id only
 * David McKnight   (IBM)        - [433541] profile duplication isn't copying profile or connection property sets
 * David McKnight   (IBM)        - [436970] connection alias with ':' causes problems
 ********************************************************************************/

package org.eclipse.rse.internal.core.model;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.osgi.util.NLS;
import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.core.IRSEUserIdConstants;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.events.ISystemModelChangeEvent;
import org.eclipse.rse.core.events.ISystemModelChangeEvents;
import org.eclipse.rse.core.events.ISystemModelChangeListener;
import org.eclipse.rse.core.events.ISystemPreferenceChangeEvent;
import org.eclipse.rse.core.events.ISystemPreferenceChangeListener;
import org.eclipse.rse.core.events.ISystemRemoteChangeEvent;
import org.eclipse.rse.core.events.ISystemRemoteChangeListener;
import org.eclipse.rse.core.events.ISystemResourceChangeEvent;
import org.eclipse.rse.core.events.ISystemResourceChangeEvents;
import org.eclipse.rse.core.events.ISystemResourceChangeListener;
import org.eclipse.rse.core.events.SystemRemoteChangeEvent;
import org.eclipse.rse.core.events.SystemResourceChangeEvent;
import org.eclipse.rse.core.filters.ISystemFilter;
import org.eclipse.rse.core.filters.ISystemFilterPoolReferenceManager;
import org.eclipse.rse.core.filters.ISystemFilterReference;
import org.eclipse.rse.core.filters.ISystemFilterStartHere;
import org.eclipse.rse.core.filters.SystemFilterReference;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.ISubSystemConfigurationCategories;
import org.eclipse.rse.core.model.ISubSystemConfigurator;
import org.eclipse.rse.core.model.ISystemContainer;
import org.eclipse.rse.core.model.ISystemHostPool;
import org.eclipse.rse.core.model.ISystemProfile;
import org.eclipse.rse.core.model.ISystemProfileManager;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.core.model.SystemChildrenContentsType;
import org.eclipse.rse.core.model.SystemMessageObject;
import org.eclipse.rse.core.references.IRSEBaseReferencingObject;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.core.subsystems.IRemoteObjectIdentifier;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.core.subsystems.ISubSystemConfigurationProxy;
import org.eclipse.rse.core.subsystems.ISystemDragDropAdapter;
import org.eclipse.rse.internal.core.RSECoreMessages;
import org.eclipse.rse.internal.core.filters.SystemFilterStartHere;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.swt.widgets.Display;

/**
 * Registry for all connections.
 */
public class SystemRegistry implements ISystemRegistry
{
	private static Exception lastException = null;
	private static SystemRegistry registry = null;
	private final SystemResourceChangeManager listenerManager = new SystemResourceChangeManager();
	private final SystemPreferenceChangeManager preferenceListManager = new SystemPreferenceChangeManager();
	private final SystemModelChangeEventManager modelListenerManager = new SystemModelChangeEventManager();
	private final SystemRemoteChangeEventManager remoteListManager = new SystemRemoteChangeEventManager();

	private ISubSystemConfigurationProxy[] subsystemConfigurationProxies = null;
	private boolean errorLoadingFactory = false;

	//For ISystemViewInputProvider
	private Object viewer = null;

	/**
	 * Constructor.
	 * This is protected as the singleton instance should be retrieved by
	 *  calling getSystemRegistry().
	 * @param logfilePath Root folder. Where to place the log file.
	 */
	protected SystemRegistry(String logfilePath)
	{
		super();

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
	public static SystemRegistry getInstance(String logfilePath)
	{
		if (registry == null)
			new SystemRegistry(logfilePath);
		return registry;
	}

	/**
	 * Return singleton instance assuming it already exists.
	 */
	public static SystemRegistry getInstance()
	{
		return registry;
	}

//	/**
//	 * Ensure given path ends with path separator.
//	 */
//	public static String addPathTerminator(String path)
//	{
//		if (!path.endsWith(File.separator))
//		{
//			path = path + File.separatorChar;
//		}
//		return path;
//	}

	/**
	 * This method is called by the connection adapter when the user expands
	 *  a connection. This method must return the child objects to show for that
	 *  connection.
	 */
	public Object[] getConnectionChildren(IHost selectedConnection)
	{
		// DWD shouldn't this be "getHostChildren"? Its part of the ISystemViewInputProvider interface.
		Object[] result = getSubSystems(selectedConnection);
		return result;
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
		if (subsystemConfigurationProxies != null)
		{
		  for (int idx = 0; (!hasSubsystems) && (idx < subsystemConfigurationProxies.length); idx++)
		  {
		  	 if (subsystemConfigurationProxies[idx].appliesToSystemType(selectedConnection.getSystemType().getName()) &&
		  	     subsystemConfigurationProxies[idx].isSubSystemConfigurationActive())
		  	 {
		  	   SubSystemConfiguration factory = subsystemConfigurationProxies[idx].getSubSystemConfiguration();
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

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapterType)
	{
		return Platform.getAdapterManager().getAdapter(this, adapterType);
	}

	// ----------------------------
	// SUBSYSTEM FACTORY METHODS...
	// ----------------------------

	/**
	 * Private method used by RSEUIPlugin to tell registry all registered subsystem
	 * factories. This way, all code can use this registry to access them versus the
	 * RSEUIPlugin.
	 *
	 * Proxies must be set sorted by priority, then ID in order to get deterministic
	 * results for all getSubSystemConfiguration*() queries.
	 */
	public void setSubSystemConfigurationProxies(ISubSystemConfigurationProxy[] proxies)
	{
		subsystemConfigurationProxies = proxies;
		//for (int idx=0; idx<proxies.length; idx++)
		// proxies[idx].setLogFile(logFile);
	}
	/**
	 * Public method to retrieve list of subsystem factory proxies registered by extension points.
	 */
	public ISubSystemConfigurationProxy[] getSubSystemConfigurationProxies()
	{
		return subsystemConfigurationProxies;
	}

	/**
	 * Return all subsystem factory proxies matching a subsystem factory category.
	 * @see ISubSystemConfigurationCategories
	 */
	public ISubSystemConfigurationProxy[] getSubSystemConfigurationProxiesByCategory(String factoryCategory)
	{
		Vector v = new Vector();
		if (subsystemConfigurationProxies != null)
		{
			for (int idx = 0; idx < subsystemConfigurationProxies.length; idx++)
				if (subsystemConfigurationProxies[idx].getCategory().equals(factoryCategory))
					v.addElement(subsystemConfigurationProxies[idx]);
		}
		ISubSystemConfigurationProxy[] proxies = new ISubSystemConfigurationProxy[v.size()];
		for (int idx = 0; idx < v.size(); idx++)
		{
			proxies[idx] = (ISubSystemConfigurationProxy) v.elementAt(idx);
		}
		return proxies;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.core.model.ISystemRegistry#getSubSystemConfigurationProxiesBySystemType(org.eclipse.rse.core.IRSESystemType)
	 */
	public ISubSystemConfigurationProxy[] getSubSystemConfigurationProxiesBySystemType(IRSESystemType systemType)
	{
		List l = new ArrayList();
		if (subsystemConfigurationProxies != null)
		{
			for (int idx = 0; idx < subsystemConfigurationProxies.length; idx++)
				if (Arrays.asList(subsystemConfigurationProxies[idx].getSystemTypes()).contains(systemType))
					l.add(subsystemConfigurationProxies[idx]);
		}
		return (ISubSystemConfigurationProxy[]) l.toArray(new ISubSystemConfigurationProxy[l.size()]);
	}

	/**
	 * Return the subsystem configuration, given its plugin.xml-declared id.
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



	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.core.model.ISystemRegistry#getSubSystemConfigurationsBySystemType(org.eclipse.rse.core.IRSESystemType, boolean)
	 */
	public ISubSystemConfiguration[] getSubSystemConfigurationsBySystemType(IRSESystemType systemType, boolean filterDuplicateServiceSubSystemFactories)
	{
		return getSubSystemConfigurationsBySystemType(systemType, filterDuplicateServiceSubSystemFactories, true);
	}
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.core.model.ISystemRegistry#getSubSystemConfigurationsBySystemType(org.eclipse.rse.core.IRSESystemType, boolean)
	 */
	public ISubSystemConfiguration[] getSubSystemConfigurationsBySystemType(IRSESystemType systemType, final boolean filterDuplicates, boolean activate) {
		List configurations = new ArrayList();
		if (subsystemConfigurationProxies != null) {
			Set serviceTypes = new HashSet();
//			Set serviceImplsAdded = new HashSet();
			for (int idx = 0; idx < subsystemConfigurationProxies.length; idx++) {
				ISubSystemConfigurationProxy subsystemConfigurationProxy = subsystemConfigurationProxies[idx];
				if (subsystemConfigurationProxy.appliesToSystemType(systemType)) {
					if (activate || subsystemConfigurationProxy.isSubSystemConfigurationActive()) {
						ISubSystemConfiguration configuration = subsystemConfigurationProxy.getSubSystemConfiguration();
						if (configuration != null) { // could happen if activate fails
							Class serviceType = configuration.getServiceType();
							if (filterDuplicates && serviceType != null) {
								if (!serviceTypes.contains(serviceType)) {
									serviceTypes.add(serviceType);
									configurations.add(configuration);
								}
							} else {
								configurations.add(configuration);
							}
						}
					}
				}
			}
		}
		ISubSystemConfiguration[] result = (ISubSystemConfiguration[]) configurations.toArray(new ISubSystemConfiguration[configurations.size()]);
		return result;
	}

	// ----------------------------
	// PROFILE METHODS...
	// ----------------------------
	/**
	 * Return singleton profile manager
	 */
	public ISystemProfileManager getSystemProfileManager()
	{
		return SystemProfileManager.getDefault();
	}

	/**
	 * Return the profiles currently selected by the user as his "active" profiles
	 */
	public ISystemProfile[] getActiveSystemProfiles()
	{
		return getSystemProfileManager().getActiveSystemProfiles();
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
		fireModelChangeEvent(
				ISystemModelChangeEvents.SYSTEM_RESOURCE_ADDED,
				ISystemModelChangeEvents.SYSTEM_RESOURCETYPE_PROFILE,
				profile, null);
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
					factory.renameSubSystemProfile(oldName, newName);
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
		////Listening to events now
		//SystemPreferencesManager.setConnectionNamesOrder(); // update preferences order list
		//boolean namesQualifed = SystemPreferencesManager.getQualifyConnectionNames();
		//if (namesQualifed)
		//	setQualifiedHostNames(namesQualifed); // causes refresh events to be fired

		fireModelChangeEvent(
				ISystemModelChangeEvents.SYSTEM_RESOURCE_RENAMED,
				ISystemModelChangeEvents.SYSTEM_RESOURCETYPE_PROFILE,
				profile, oldName);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.core.model.ISystemRegistry#copySystemProfile(org.eclipse.rse.core.model.ISystemProfile, java.lang.String, boolean, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public ISystemProfile copySystemProfile(ISystemProfile profile, String newName, boolean makeActive, IProgressMonitor monitor) throws Exception
	{
		Exception lastExc = null;
		boolean failed = false;
		String msg = null;
		String oldName = profile.getName();
		IHost[] newConns = null;

		//RSECorePlugin.getDefault().getLogger().logDebugMessage(this.getClass().getName(), "Start of system profile copy. From: "+oldName+" to: "+newName+", makeActive: "+makeActive);
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
				String msgNoSubs = RSECoreMessages.MSG_COPYCONNECTION_PROGRESS;
				for (int idx = 0; idx < conns.length; idx++)
				{
					msg = NLS.bind(msgNoSubs, conns[idx].getAliasName());
					RSECorePlugin.getDefault().getLogger().logDebugMessage(this.getClass().getName(), msg);
					monitor.subTask(msg);

					newConns[idx] = oldPool.cloneHost(newPool, conns[idx], conns[idx].getAliasName());

					monitor.worked(1);
					//try { java.lang.Thread.sleep(3000l); } catch (InterruptedException e) {}
				}
			}
			msg = RSECoreMessages.MSG_COPYFILTERPOOLS_PROGRESS;
			monitor.subTask(msg);
			RSECorePlugin.getDefault().getLogger().logDebugMessage(this.getClass().getName(), msg);

			// STEP 4: CREATE NEW FILTER POOL MANAGER
			// STEP 5: COPY ALL FILTER POOLS FROM OLD MANAGER TO NEW MANAGER
			for (int idx = 0; idx < factories.size(); idx++)
			{
				ISubSystemConfiguration factory = (ISubSystemConfiguration) factories.elementAt(idx);
				msg = "Copying filterPools for factory " + factory.getName(); //$NON-NLS-1$
				//monitor.subTask(msg);
				RSECorePlugin.getDefault().getLogger().logDebugMessage(this.getClass().getName(), msg);
				factory.copyFilterPoolManager(profile, newProfile);
				//try { java.lang.Thread.sleep(3000l); } catch (InterruptedException e) {}
			}

			monitor.worked(1);

			// STEP 6: COPY ALL SUBSYSTEMS FOR EACH COPIED CONNECTION
			msg = RSECoreMessages.MSG_COPYSUBSYSTEMS_PROGRESS;
			monitor.subTask(msg);
			RSECorePlugin.getDefault().getLogger().logDebugMessage(this.getClass().getName(), msg);
			if ((conns != null) && (conns.length > 0))
			{
				ISubSystem[] subsystems = null;
				ISubSystemConfiguration factory = null;
				for (int idx = 0; idx < conns.length; idx++)
				{
					IHost host = conns[idx];
					msg = "Copying subsystems for connection " + host.getAliasName(); //$NON-NLS-1$
					//monitor.subTask(msg);
					RSECorePlugin.getDefault().getLogger().logDebugMessage(this.getClass().getName(), msg);
					subsystems = getSubSystems(host); // get old subsystems for this connection
					if ((subsystems != null) && (subsystems.length > 0) && newConns != null)
					{
						for (int jdx = 0; jdx < subsystems.length; jdx++)
						{
							msg += ": subsystem " + subsystems[jdx].getName(); //$NON-NLS-1$
							//monitor.subTask(msg);
							RSECorePlugin.getDefault().getLogger().logDebugMessage(this.getClass().getName(), msg);
							factory = subsystems[jdx].getSubSystemConfiguration();
							factory.cloneSubSystem(subsystems[jdx], newConns[idx], true); // true=>copy profile op vs copy connection op
							//try { java.lang.Thread.sleep(3000l); } catch (InterruptedException e) {}
						}
					}
					//try { java.lang.Thread.sleep(1000l); } catch (InterruptedException e) {}
					host.clonePropertySets(newConns[idx]); // copy property sets from host
				}
			}
			
			profile.clonePropertySets(newProfile); // copy property sets from profile
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
				newProfile.suspend();
				if (newConns != null)
					for (int idx = 0; idx < newConns.length; idx++)
						deleteHost(newConns[idx]);
				for (int idx = 0; idx < subsystemConfigurationProxies.length; idx++)
				{
					ISubSystemConfiguration factory = subsystemConfigurationProxies[idx].getSubSystemConfiguration();
					if (factory != null)
						factory.deletingSystemProfile(newProfile);
				}
				getSystemProfileManager().deleteSystemProfile(newProfile, true);
			}
			catch (Exception exc)
			{
				RSECorePlugin.getDefault().getLogger().logError("Exception (ignored) cleaning up from copy-profile exception.", exc); //$NON-NLS-1$
			}
			throw (lastExc);
		}

		// LAST STEP: MAKE NEW PROFILE ACTIVE IF SO REQUESTED: NO, CAN'T DO IT HERE BECAUSE OF THREAD VIOLATIONS!
		//if (makeActive)
		//setSystemProfileActive(newProfile, true);

		fireModelChangeEvent(
				ISystemModelChangeEvents.SYSTEM_RESOURCE_ADDED,
				ISystemModelChangeEvents.SYSTEM_RESOURCETYPE_PROFILE,
				newProfile, null);

		RSECorePlugin.getDefault().getLogger().logDebugMessage(this.getClass().getName(), "Copy of system profile " + oldName + " to " + newName + " successful"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		return newProfile;
	}


	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.model.ISystemRegistry#deleteSystemProfile(org.eclipse.rse.core.model.ISystemProfile)
	 */
	public void deleteSystemProfile(ISystemProfile profile) throws Exception {
		ISystemProfileManager manager = getSystemProfileManager();
		ISystemProfile defaultProfile = manager.getDefaultPrivateSystemProfile();
		if (profile != defaultProfile) {
			// load everything
			profile.suspend();
			loadAll();
			// remove connections
			IHost[] connections = getHostsByProfile(profile);
			for (int idx = 0; idx < connections.length; idx++) {
				deleteHost(connections[idx]);
			}
			// remove filter pools for this profile
			if (subsystemConfigurationProxies != null) {
				for (int idx = 0; idx < subsystemConfigurationProxies.length; idx++) {
					ISubSystemConfiguration factory = subsystemConfigurationProxies[idx].getSubSystemConfiguration();
					if (factory != null) factory.deletingSystemProfile(profile);
				}
			}
			// remove the profile
			manager.deleteSystemProfile(profile, true);
			// fire events
			if (connections.length > 0) { // defect 42112
				SystemResourceChangeEvent event = new SystemResourceChangeEvent(connections, ISystemResourceChangeEvents.EVENT_DELETE_MANY, this);
				fireEvent(event);
			}
			fireModelChangeEvent(ISystemModelChangeEvents.SYSTEM_RESOURCE_REMOVED, ISystemModelChangeEvents.SYSTEM_RESOURCETYPE_PROFILE, profile, null);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.model.ISystemRegistry#setSystemProfileActive(org.eclipse.rse.core.model.ISystemProfile, boolean)
	 */
	public void setSystemProfileActive(ISystemProfile profile, boolean makeActive) {
		ISystemProfileManager manager = getSystemProfileManager();
		ISystemProfile defaultProfile = manager.getDefaultPrivateSystemProfile();
		if (profile != defaultProfile) {
			// Test if there are any filter pools in this profile that are referenced by another active profile...
			Vector activeReferenceVector = new Vector();
			if (!makeActive && (subsystemConfigurationProxies != null)) {
				for (int idx = 0; idx < subsystemConfigurationProxies.length; idx++) {
					ISubSystemConfiguration factory = subsystemConfigurationProxies[idx].getSubSystemConfiguration();
					if (factory != null) {
						ISubSystem[] activeReferences = factory.testForActiveReferences(profile);
						if (activeReferences != null) for (int jdx = 0; jdx < activeReferences.length; jdx++)
							activeReferenceVector.addElement(activeReferences[jdx]);
					}
				}
			}
			if (activeReferenceVector.size() > 0) {
				String msg = NLS.bind(RSECoreMessages.MSG_LOADING_PROFILE_WARNING_FILTERPOOL_REFS, profile.getName());
				RSECorePlugin.getDefault().getLogger().logWarning(msg);
				for (int idx = 0; idx < activeReferenceVector.size(); idx++) {
					ISubSystem activeReference = (ISubSystem) activeReferenceVector.elementAt(idx);
					msg = "  " + activeReference.getName(); //$NON-NLS-1$
					msg += NLS.bind(RSECoreMessages.MSG_LOADING_PROFILE_WARNING_FILTERPOOL_REF, activeReference.getHost().getAliasName(), activeReference.getSystemProfileName());
					RSECorePlugin.getDefault().getLogger().logWarning(msg);
				}
				ISubSystem firstSubSystem = (ISubSystem) activeReferenceVector.elementAt(0);
				String connectionName = firstSubSystem.getHost().getSystemProfileName() + "." + firstSubSystem.getHost().getAliasName(); //$NON-NLS-1$
				msg = NLS.bind(RSECoreMessages.MSG_LOADING_PROFILE_SHOULDNOTBE_DEACTIVATED, profile.getName(), connectionName);
				RSECorePlugin.getDefault().getLogger().logWarning(msg);
			}
			getSystemProfileManager().makeSystemProfileActive(profile, makeActive);
			// Each factory may have to load the subsystems for connections that are suddenly active.
			if (subsystemConfigurationProxies != null) {
				for (int idx = 0; idx < subsystemConfigurationProxies.length; idx++) {
					if (subsystemConfigurationProxies[idx].isSubSystemConfigurationActive()) { // don't bother if not yet alive
						ISubSystemConfiguration factory = subsystemConfigurationProxies[idx].getSubSystemConfiguration();
						if (factory != null) factory.changingSystemProfileActiveStatus(profile, makeActive);
					}
				}
			}
			// notify the hosts that are affected by this change
			IHost[] affectedConnections = getHostsByProfile(profile);
			// delete...
			if (!makeActive) { // better disconnect all connections before we lose sight of them
				if ((affectedConnections != null) && (affectedConnections.length > 0)) {
					for (int idx = 0; idx < affectedConnections.length; idx++) {
						disconnectAllSubSystems(affectedConnections[idx]);
					}
					SystemResourceChangeEvent event = new SystemResourceChangeEvent(affectedConnections, ISystemResourceChangeEvents.EVENT_DELETE_MANY, this);
					fireEvent(event);
				}
			}
			// add...
			else if ((affectedConnections != null) && (affectedConnections.length > 0)) {
				SystemResourceChangeEvent event = new SystemResourceChangeEvent(affectedConnections, ISystemResourceChangeEvents.EVENT_ADD_MANY, this);
				fireEvent(event);
			}
			fireModelChangeEvent(ISystemModelChangeEvents.SYSTEM_RESOURCE_CHANGED, ISystemModelChangeEvents.SYSTEM_RESOURCETYPE_PROFILE, profile, null);
		}
	}

	// private profile methods...

	/**
	 * Get a SystemProfile given a connection pool
	 */
	private ISystemProfile getSystemProfile(ISystemHostPool pool)
	{
		return pool.getSystemProfile();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.model.ISystemRegistry#getConnectorServices(org.eclipse.rse.core.model.IHost)
	 */
	public IConnectorService[] getConnectorServices(IHost host) {
		List services = new ArrayList();
		ISubSystem[] subsystems = getSubSystems(host);
		for (int i = 0; i < subsystems.length; i++) {
			ISubSystem subsystem = subsystems[i];
			IConnectorService service = subsystem.getConnectorService();
			if (!services.contains(service)) {
				services.add(service);
			}
		}
		return (IConnectorService[]) services.toArray(new IConnectorService[services.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.model.ISystemRegistry#getSubSystems(org.eclipse.rse.core.model.IHost, boolean)
	 */
	public ISubSystem[] getSubSystems(IHost host, boolean force) {
		return getSubSystems(host);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.model.ISystemRegistry#getSubSystems(org.eclipse.rse.core.model.IHost)
	 */
	public ISubSystem[] getSubSystems(IHost host) {
		IRSESystemType systemType = host.getSystemType();
		List subsystems = new ArrayList();
		if (subsystemConfigurationProxies != null) {
			for (int i = 0; i < subsystemConfigurationProxies.length; i++) {
				ISubSystemConfigurationProxy proxy = subsystemConfigurationProxies[i];
				if (proxy.appliesToSystemType(systemType)) {
					if (proxy.isSubSystemConfigurationActive()) {
						ISubSystemConfiguration config = proxy.getSubSystemConfiguration();
						ISubSystem[] ssArray = config.getSubSystems(host, false);
						if (ssArray == null) { // create a subsystem for this connection and config
							ssArray = this.createSubSystems(host, new ISubSystemConfiguration[] {config});
						}
						subsystems.addAll(Arrays.asList(ssArray));
					}
				}
			}
		}
		ISubSystem[] result = new ISubSystem[subsystems.size()];
		subsystems.toArray(result);
		return result;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.model.ISystemRegistry#getSubSystem(java.lang.String)
	 */
	public ISubSystem getSubSystem(String absoluteSubSystemName)
	{
		ISubSystem result = null;
		// first extract subsystem id
		int profileDelim = absoluteSubSystemName.indexOf("."); //$NON-NLS-1$
		int connectionDelim = absoluteSubSystemName.lastIndexOf(":"); //$NON-NLS-1$

		if (profileDelim > 0 && connectionDelim > profileDelim)
		{
			String srcProfileName = absoluteSubSystemName.substring(0, profileDelim);
			String srcConnectionName = absoluteSubSystemName.substring(profileDelim + 1, connectionDelim);
			String srcSubSystemConfigurationId = absoluteSubSystemName.substring(connectionDelim + 1, absoluteSubSystemName.length());

			ISystemProfile profile = getSystemProfile(srcProfileName);
			if (profile != null) {
				result = getSubSystem(profile, srcConnectionName, srcSubSystemConfigurationId);
			}
		}

		return result;
	}

	/**
	 * Resolve a subsystem from it's profile, connection and subsystem name.
	 *
	 * @param profile the profile to search
	 * @param srcConnectionName the name of the connection
	 * @param subsystemConfigurationId the factory Id of the subsystem
	 *
	 * @return the subsystem
	 */
	public ISubSystem getSubSystem(ISystemProfile profile, String srcConnectionName, String subsystemConfigurationId)
	{
		// find the src connection
		IHost[] connections = getHostsByProfile(profile);
		if (connections == null)
		{
			// if the profile can't be found, get all connections
			connections = getHosts();
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
					if (compareId.equals(subsystemConfigurationId))
					{
						return subsystem;
					}
					else
					{
						// for migration purposes, test the against the name
						// we used to use the subsystem name instead of the factory Id
						if (subsystem.getName().equals(subsystemConfigurationId))
						{
							return subsystem;
						}
					}
				}
			}
		}

		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.core.model.ISystemRegistry#getAbsoluteNameForSubSystem(org.eclipse.rse.core.subsystems.ISubSystem)
	 */
	public String getAbsoluteNameForSubSystem(ISubSystem subSystem)
	{
		StringBuffer dataStream = new StringBuffer();

		String profileName = subSystem.getSystemProfileName();
		String connectionName = subSystem.getHostAliasName();
		String factoryId = subSystem.getConfigurationId();

		dataStream.append(profileName);
		dataStream.append("."); //$NON-NLS-1$
		dataStream.append(connectionName);
		dataStream.append(":"); //$NON-NLS-1$
		dataStream.append(factoryId);
		return dataStream.toString();
	}

	/**
	 * Adapt the given element to an adapter that allows reading the element's
	 * absolute name and parent subsystem.
	 *
	 * @param element an element to adapt.
	 * @return the requested adapter, or <code>null</code> if the element is
	 *         not adaptable as needed.
	 * @since org.eclipse.rse.core 3.0
	 */
	public static ISystemDragDropAdapter getSystemDragDropAdapter(Object element) {
		if (element == null)
			return null;
		Object adapter = null;
		if (element instanceof IAdaptable) {
			IAdaptable adaptable = (IAdaptable) element;
			adapter = adaptable.getAdapter(ISystemDragDropAdapter.class);
			if (adapter == null) {
				adapter = Platform.getAdapterManager().getAdapter(element, "org.eclipse.rse.ui.view.ISystemViewElementAdapter"); //$NON-NLS-1$
				if (adapter == null) {
					return null;
				}
				assert false : "Found ISystemViewElementAdapter but no ISystemDragDropAdapter"; //$NON-NLS-1$
			}
		} else {
			IAdapterManager am = Platform.getAdapterManager();
			adapter = am.getAdapter(element, ISystemDragDropAdapter.class.getName());
			if (adapter == null) {
				adapter = am.getAdapter(element, "org.eclipse.rse.ui.view.ISystemViewElementAdapter"); //$NON-NLS-1$
				if (adapter == null) {
					return null;
				}
				assert false : "Found ISystemViewElementAdapter but no ISystemDragDropAdapter"; //$NON-NLS-1$
			}
		}
		// At this point, we know for sure that we can adapt!
		return (ISystemDragDropAdapter) adapter;
	}

	/**
	 * Check if two objects refers to the same system object by comparing their
	 * absolute Names and subsystem id's.
	 *
	 * @param firstObject the first object to compare
	 * @param firstObjectFullName the full name of the firstObject. If null, get
	 *            the full name from the firstObject
	 * @param secondObject the second object to compare
	 * @param secondObjectFullName the full name of the secondObject. If null,
	 *            get the full name from the secondObject
	 * @return <code>true</code> if the objects to be compared are the same
	 *         instance; or, if both objects are non-null and adaptable to an
	 *         RSE ISystemDragDropAdapter each, and those adapters do return a
	 *         valid absolute name that's the same for both elements, and both
	 *         elements belong to the same subsystem instance. Otherwise,
	 *         <code>false</code> in all other cases.
	 */
	public static boolean isSameObjectByAbsoluteName(Object firstObject, String firstObjectFullName, Object secondObject, String secondObjectFullName)
	{
		if (firstObject == secondObject) {
			return true;
		}
		// should never be comparing null objects
		if (firstObject == null || secondObject == null){
			return false;
		}

		// two different message objects should not be considered the same
		if (firstObject instanceof SystemMessageObject){
			return false;
		}

		ISystemDragDropAdapter adA = null;
		ISystemDragDropAdapter adB = null;
		if (firstObjectFullName == null) {
			adA = getSystemDragDropAdapter(firstObject);
			if (adA != null) {
				firstObjectFullName = adA.getAbsoluteName(firstObject);
			}
		}
		if (secondObjectFullName == null) {
			adB = getSystemDragDropAdapter(secondObject);
			if (adB != null) {
				secondObjectFullName = adB.getAbsoluteName(secondObject);
			}
		}
		if (firstObjectFullName != null && firstObjectFullName.equals(secondObjectFullName)) {
			// full names exist and are the same: compare the subsystems
			if (adA == null) { // firstFullName was passed in
				adA = getSystemDragDropAdapter(firstObject);
				assert adA != null : "full name \"" + firstObjectFullName + "\" has no ISystemDragDropAdapter!"; //$NON-NLS-1$ //$NON-NLS-2$
				assert firstObjectFullName.equals(adA.getAbsoluteName(firstObject)) : "full name \"" + firstObjectFullName + "\" differs from adapter response: " + adA.getAbsoluteName(firstObject); //$NON-NLS-1$ //$NON-NLS-2$
			}
			if (adB == null) { // secondFullName was passed in
				adB = getSystemDragDropAdapter(secondObject);
				assert adB != null : "full name \"" + secondObjectFullName + "\" has no ISystemDragDropAdapter!"; //$NON-NLS-1$ //$NON-NLS-2$
				assert secondObjectFullName.equals(adB.getAbsoluteName(secondObject)) : "full name \"" + firstObjectFullName + "\" differs from adapter response: " + adB.getAbsoluteName(secondObject); //$NON-NLS-1$ //$NON-NLS-2$
			}
			ISubSystem ssA = adA.getSubSystem(firstObject);
			ISubSystem ssB = adB.getSubSystem(secondObject);
			return ssA == ssB;
		}
		return false;
	}

	 /*
	  * (non-Javadoc)
	  * @see org.eclipse.rse.core.model.ISystemRegistry#getAbsoluteNameForConnection(org.eclipse.rse.core.model.IHost)
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

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.model.ISystemRegistry#getSubsystems(org.eclipse.rse.core.model.IHost, java.lang.Class)
	 */
	public ISubSystem[] getSubsystems(IHost host, Class subsystemInterface)
	{
		List matches = new ArrayList();
		ISubSystem[] allSS = getSubSystems(host);
		for (int i = 0; i < allSS.length; i++)
		{
			ISubSystem ss = allSS[i];
			if (subsystemInterface.isInstance(ss))
			{
				matches.add(ss);
			}
		}
		return (ISubSystem[])matches.toArray(new ISubSystem[matches.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.model.ISystemRegistry#getServiceSubSystems(org.eclipse.rse.core.model.IHost, java.lang.Class)
	 */
	public ISubSystem[] getServiceSubSystems(IHost host, Class serviceType)
	{
		List matches = new ArrayList();
		ISubSystem[] allSS = getSubSystems(host);
		for (int i = 0; i < allSS.length; i++)
		{
			ISubSystem ss = allSS[i];
			Class thisServiceType = ss.getServiceType();
			if (thisServiceType == serviceType)
			{
				matches.add(ss);
			}
		}
		return (ISubSystem[])matches.toArray(new ISubSystem[matches.size()]);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.core.model.ISystemRegistry#getSubSystemsBySubSystemConfigurationCategory(java.lang.String, org.eclipse.rse.core.model.IHost)
	 */
	public ISubSystem[] getSubSystemsBySubSystemConfigurationCategory(String factoryCategory, IHost host)
	{
		ISubSystem[] subsystems = getSubSystems(host);
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

	public ISubSystemConfiguration[] getSubSystemConfigurations() {
		// fixed Bugzilla Bug 160115 - added non-null guard for config
		Vector v = new Vector();
		ISubSystemConfigurationProxy[] proxies = getSubSystemConfigurationProxies();
		if (proxies != null) {
			for (int idx = 0; idx < proxies.length; idx++) {
				ISubSystemConfigurationProxy proxy = proxies[idx];
				ISubSystemConfiguration config = proxy.getSubSystemConfiguration();
				if (config != null) {
					v.add(proxies[idx].getSubSystemConfiguration());
				}
			}
		}
		ISubSystemConfiguration[] result = new ISubSystemConfiguration[v.size()];
		v.toArray(result);
		return result;
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
		ISubSystemConfiguration ssFactory = subsystem.getSubSystemConfiguration();
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
	private ISystemHostPool getHostPool(ISystemProfile profile) {
		ISystemHostPool result = SystemHostPool.getSystemHostPool(profile);
		return result;
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
		IRSESystemType localType = RSECorePlugin.getTheCoreRegistry().getSystemTypeById(IRSESystemType.SYSTEMTYPE_LOCAL_ID);
		IHost[] conns = getHostsBySystemType(localType);
		if (conns != null && conns.length > 0) return conns[0];
		else return localConn;
	}

	/**
	 * Return all connections in all active profiles.
	 * Never returns null, but may return a zero-length array.
	 * All array elements are valid hosts (never returns null elements).
	 */
	public IHost[] getHosts()
	{
		ISystemHostPool[] pools = getHostPools();
		List hosts = new ArrayList();
		for (int idx = 0; idx < pools.length; idx++) {
			IHost[] conns = pools[idx].getHosts();
			if (conns != null) {
				for (int jdx = 0; jdx < conns.length; jdx++) {
					//ISystemHostPool ensures that we never have "null" hosts.
					assert conns[jdx]!=null : "Null host in pool "+pools[idx].getName()+" at "+jdx;
					hosts.add(conns[jdx]);
				}
			}
		}
		IHost[] allConns = (IHost[])hosts.toArray(new IHost[hosts.size()]);
		return allConns;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.core.model.ISystemRegistry#getHostsByProfile(org.eclipse.rse.core.model.ISystemProfile)
	 */
	public IHost[] getHostsByProfile(ISystemProfile profile)
	{
		ISystemHostPool pool = getHostPool(profile);
		return pool.getHosts();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.core.model.ISystemRegistry#getHostsBySubSystemConfiguration(org.eclipse.rse.core.subsystems.ISubSystemConfiguration)
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

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.core.model.ISystemRegistry#getHostsBySubSystemConfigurationCategory(java.lang.String)
	 */
	public IHost[] getHostsBySubSystemConfigurationCategory(String factoryCategory)
	{
		Vector v = new Vector();
		if (subsystemConfigurationProxies != null)
		{
			for (int idx = 0; idx < subsystemConfigurationProxies.length; idx++)
			{
				if (subsystemConfigurationProxies[idx].getCategory().equals(factoryCategory))
				{
					ISubSystemConfiguration factory = subsystemConfigurationProxies[idx].getSubSystemConfiguration();
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

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.model.ISystemRegistry#getHostsBySystemType(org.eclipse.rse.core.IRSESystemType)
	 */
	public IHost[] getHostsBySystemType(IRSESystemType systemType) {
		List connections = new ArrayList();

		if (systemType != null) {
			IHost[] candidates = getHosts();
			for (int i = 0; i < candidates.length; i++) {
				IHost candidate = candidates[i];
				IRSESystemType candidateType = candidate.getSystemType();
				if (systemType.equals(candidateType)) {
					connections.add(candidate);
				}
			}
		}

		return (IHost[])connections.toArray(new IHost[connections.size()]);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.core.model.ISystemRegistry#getHostsBySystemTypes(org.eclipse.rse.core.IRSESystemType[])
	 */
	public IHost[] getHostsBySystemTypes(IRSESystemType[] systemTypes)
	{
		List systemTypesList = Arrays.asList(systemTypes);
		IHost[] connections = getHosts();
		Vector v = new Vector();
		for (int idx = 0; idx < connections.length; idx++)
		{
			IRSESystemType systemType = connections[idx].getSystemType();
			if (systemTypesList.contains(systemType)) {
				v.addElement(connections[idx]);
			}
		}
		return (IHost[])v.toArray(new IHost[v.size()]);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.core.model.ISystemRegistry#getHost(org.eclipse.rse.core.model.ISystemProfile, java.lang.String)
	 */
	public IHost getHost(ISystemProfile profile, String connectionName)
	{
		return getHostPool(profile).getHost(connectionName);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.core.model.ISystemRegistry#getHostPosition(org.eclipse.rse.core.model.IHost)
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

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.core.model.ISystemRegistry#getHostCount(org.eclipse.rse.core.model.ISystemProfile)
	 */
	public int getHostCount(ISystemProfile profile)
	{
		return getHostPool(profile).getHostCount();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.core.model.ISystemRegistry#getHostCountWithinProfile(org.eclipse.rse.core.model.IHost)
	 */
	public int getHostCountWithinProfile(IHost conn)
	{
		return conn.getHostPool().getHostCount();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.core.model.ISystemRegistry#getHostCount()
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

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.core.model.ISystemRegistry#getHostAliasNames(org.eclipse.rse.core.model.ISystemProfile)
	 */
	public Vector getHostAliasNames(ISystemProfile profile)
	{
		ISystemHostPool pool = getHostPool(profile);
		Vector names = new Vector();
		IHost[] conns = pool.getHosts();
		for (int idx = 0; idx < conns.length; idx++)
		{
			names.addElement(conns[idx].getAliasName());
		}
		return names;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.core.model.ISystemRegistry#getHostAliasNamesForAllActiveProfiles()
	 */
	public Vector getHostAliasNamesForAllActiveProfiles()
	{
		ISystemHostPool[] allPools = getHostPools();
		Vector allNames = new Vector();
		for (int idx = 0; idx < allPools.length; idx++)
		{
			Vector v = getHostAliasNames(getSystemProfile(allPools[idx]));
			for (int jdx = 0; jdx < v.size(); jdx++)
				allNames.addElement(v.elementAt(jdx));
		}
		return allNames;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.core.model.ISystemRegistry#getHostNames(org.eclipse.rse.core.IRSESystemType)
	 */
	public String[] getHostNames(IRSESystemType systemType)
	{
		Vector v = new Vector();

		if (systemType != null)
		{
			IHost[] conns = getHosts();
			for (int idx = 0; idx < conns.length; idx++)
			{
				// Note: IHost.getHostName() can return null if the connection is using
				//       any non-IP based connectivity (serial line, JTAG, ...). Adding
				//       null unchecked to the result list will trigger InvalidArgumentExceptions
				//       in SystemConnectionForm.
				if (conns[idx].getHostName() != null && !v.contains(conns[idx].getHostName()))
				{
					if (conns[idx].getSystemType().equals(systemType))
						v.addElement(conns[idx].getHostName());
				}
			}
		}
		if ((systemType != null) && (systemType.isLocal() && (v.size() == 0)))
			v.addElement("localhost");
		return (String[])v.toArray(new String[v.size()]);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.core.model.ISystemRegistry#createLocalHost(org.eclipse.rse.core.model.ISystemProfile, java.lang.String, java.lang.String)
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
			IRSESystemType localType = RSECorePlugin.getTheCoreRegistry().getSystemTypeById(IRSESystemType.SYSTEMTYPE_LOCAL_ID);
			localConn = createHost(
		  			profile.getName(), localType,
		  			name, // connection name
		  			"localhost", // hostname //$NON-NLS-1$
		  			"", // description
		  			// DY:  defect 42101, description cannot be null
		  			// null, // description
		  			userId, // default user Id
		  			IRSEUserIdConstants.USERID_LOCATION_DEFAULT_SYSTEMTYPE, null);

		}
		catch (Exception exc)
		{
			RSECorePlugin.getDefault().getLogger().logError("Error creating local connection", exc);
		}
		return localConn;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.model.ISystemRegistry#createHost(java.lang.String, org.eclipse.rse.core.IRSESystemType, java.lang.String, java.lang.String, java.lang.String, java.lang.String, int, org.eclipse.rse.core.model.ISubSystemConfigurator[])
	 */
	public IHost createHost(
		String profileName,
		IRSESystemType systemType,
		String connectionName,
		String hostName,
		String description,
		String defaultUserId,
		int defaultUserIdLocation,
		ISubSystemConfigurator[] configurators)
		throws Exception
	{
		return createHost(profileName, systemType, connectionName, hostName, description, defaultUserId, defaultUserIdLocation, true, configurators);
	}

	/**
	 * Create a host object, given its host pool and its attributes.
	 * <p>
	 * This method:
	 * <ul>
	 * <li>creates and saves a new connection within the given profile
	 * <li>calls all subsystem factories to give them a chance to create a subsystem instance
	 * <li>fires an ISystemResourceChangeEvent event of type EVENT_ADD to all registered listeners
	 * </ul>
	 * <p>
	 * @param profileName Name of the system profile the connection is to be added to.
	 * @param systemType system type matching one of the system types defined via the systemTypes extension point.
	 * @param hostName unique connection name.
	 * @param hostAddress IP name of host.
	 * @param description optional description of the connection. Can be null.
	 * @param defaultUserId userId to use as the default for the subsystems.
	 * @param defaultUserIdLocation one of the constants in {@link org.eclipse.rse.core.IRSEUserIdConstants}
	 * that tells us where to set the user Id
	 * @param createSubSystems <code>true</code> to create subsystems for the host, <code>false</code> otherwise.
	 * @param configurators the list of all configurators supplied by the subsystem configuration that pertain to the specified system type. Else null.
	 * @return SystemConnection object, or null if it failed to create. This is typically
	 * because the connectionName is not unique. Call getLastException() if necessary.
	 */
	public IHost createHost(final String profileName, final IRSESystemType systemType, final String hostName,
			final String hostAddress, final String description, final String defaultUserId,
			final int defaultUserIdLocation, final boolean createSubSystems,
			final ISubSystemConfigurator[] configurators) throws Exception {
		final ISystemRegistry sr = this;
		class CreateHostOperation implements ISystemProfileOperation {
			private IHost host = null;
			private ISubSystem[] subsystems = new ISubSystem[0];
			IHost getHost() {
				return host;
			}
			public ISubSystem[] getSubSystems() {
				return subsystems;
			}
			public IStatus run() {
				IStatus status = Status.OK_STATUS;
				ISystemHostPool pool = getHostPool(profileName);
				try {
					// create, register and save new connection...
					String uid = defaultUserId;
					if ((uid != null) && (uid.length() == 0)) {
						uid = null;
					}
					host = pool.createHost(systemType, hostName, hostAddress, description, uid, defaultUserIdLocation);
					if (host == null) { // did not create since host already exists
						host = pool.getHost(hostName);
					}
				} catch (Exception e) {
					String pluginId = RSECorePlugin.getDefault().getBundle().getSymbolicName();
					String message = NLS.bind(RSECoreMessages.MSG_CREATEHOST_EXCEPTION, hostName);
					status = new Status(IStatus.ERROR, pluginId, message, e);
				}
				if (status.isOK()) {
					if (createSubSystems) {
						// determine the list of configs to use to create subsystems from
						List configs = new ArrayList(10); // arbitrary but reasonable
						if (configurators != null) {
							// if there are configurators need to at least use those
							for (int i = 0; i < configurators.length; i++) {
								configs.add(configurators[i].getSubSystemConfiguration());
							}
							// add any non-service subsystem configs that aren't already there that apply to this systemtype
							ISubSystemConfiguration[] configsArray = getSubSystemConfigurationsBySystemType(systemType, false);
							for (int i = 0; i < configsArray.length; i++) {
								ISubSystemConfiguration config = configsArray[i];
								boolean isStrange = (config.getServiceType() == null);
								boolean isAbsent = !configs.contains(config);
								if (isStrange && isAbsent) {
									configs.add(config);
								}
							}
						} else {
							// just get the defaults with the service subsystems filtered
							ISubSystemConfiguration[] configsArray = getSubSystemConfigurationsBySystemType(systemType, true);
							configs = Arrays.asList(configsArray);
						}
						// only subsystem configuration is used per service type
						subsystems = new ISubSystem[configs.size()];
						ISystemProfile profile = host.getSystemProfile();
						int i = 0;
						for (Iterator z = configs.iterator(); z.hasNext();) {
							ISubSystemConfiguration config = (ISubSystemConfiguration) z.next();
							config.getFilterPoolManager(profile, true); // create the filter pool
							ISubSystemConfigurator[] interestingPages = getApplicableConfigurators(config, configurators);
							subsystems[i] = config.createSubSystem(host, true, interestingPages); // give it the opportunity to create a subsystem
							i++;
						}
					}
					host.commit();
				}
				return status;
			}
		}
		CreateHostOperation op = new CreateHostOperation();
		IStatus status = SystemProfileManager.run(op);
		lastException = (Exception) status.getException();
		if (lastException != null) {
			RSECorePlugin.getDefault().getLogger().logError(status.getMessage(), lastException);
			throw lastException;
		}
		IHost host = op.getHost();
		if (modelListenerManager.hasListeners()) {
			ISubSystem[] subsystems = op.getSubSystems();
			FireNewHostEvents fire = new FireNewHostEvents(host, subsystems, sr, configurators != null);
			// FIXME bug 240991: With the current workaround, we might miss events
			// in SystemPreferencesManager. Instead of Display.getDefault(),
			// we should use the IRSEInteractionProvider here.
			Display.getDefault().asyncExec(fire);
		}

		// //SystemPreferencesManager listens itself to FireNewHostEvents now
		//SystemPreferencesManager.setConnectionNamesOrder(); // update preferences order list
		return host;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.core.model.ISystemRegistry#createSubSystems(org.eclipse.rse.core.model.IHost, org.eclipse.rse.core.subsystems.ISubSystemConfiguration[])
	 */
	public ISubSystem[] createSubSystems(IHost host, ISubSystemConfiguration[] configurations) {

		ISubSystem[] subsystems = new ISubSystem[configurations.length];

		for (int i = 0; i < configurations.length; i++) {
			subsystems[i] = configurations[i].createSubSystem(host, true, null);
		}

		for (int j = 0; j < subsystems.length; j++) {
			if (subsystems[j] != null) {
				fireModelChangeEvent(ISystemModelChangeEvents.SYSTEM_RESOURCE_ADDED, ISystemModelChangeEvents.SYSTEM_RESOURCETYPE_SUBSYSTEM, subsystems[j], null);
			}
		}

		host.commit();
		return subsystems;
	}

	class NotifyModelChangedRunnable implements Runnable
	{
		private ISystemModelChangeEvent _event;
		public NotifyModelChangedRunnable(ISystemModelChangeEvent event)
		{
			_event = event;
		}

		public void run()
		{
			modelListenerManager.notify(_event);
		}
	}

	class NotifyResourceChangedRunnable implements Runnable
	{
		private ISystemResourceChangeEvent _event;
		public NotifyResourceChangedRunnable(ISystemResourceChangeEvent event)
		{
			_event = event;
		}

		public void run()
		{
			listenerManager.notify(_event);
		}
	}

	class NotifyPreferenceChangedRunnable implements Runnable
	{
		private ISystemPreferenceChangeEvent _event;
		public NotifyPreferenceChangedRunnable(ISystemPreferenceChangeEvent event)
		{
			_event = event;
		}

		public void run()
		{
			preferenceListManager.notify(_event);
		}
	}

	class PreferenceChangedRunnable implements Runnable
	{
		private ISystemPreferenceChangeEvent _event;
		private ISystemPreferenceChangeListener _listener;

		public PreferenceChangedRunnable(ISystemPreferenceChangeEvent event, ISystemPreferenceChangeListener listener)
		{
			_event = event;
			_listener = listener;
		}

		public void run()
		{
			_listener.systemPreferenceChanged(_event);
		}
	}

	class ModelResourceChangedRunnable implements Runnable
	{
		private ISystemModelChangeListener _listener;
		private ISystemModelChangeEvent _event;
		public ModelResourceChangedRunnable(ISystemModelChangeEvent event, ISystemModelChangeListener listener)
		{
			_event = event;
			_listener = listener;
		}

		public void run()
		{
			_listener.systemModelResourceChanged(_event);
		}
	}

	class ResourceChangedRunnable implements Runnable
	{
		private ISystemResourceChangeListener _listener;
		private ISystemResourceChangeEvent _event;
		public ResourceChangedRunnable(ISystemResourceChangeEvent event, ISystemResourceChangeListener listener)
		{
			_event = event;
			_listener = listener;
		}

		public void run()
		{
			_listener.systemResourceChanged(_event);
		}
	}

	class RemoteResourceChangedRunnable implements Runnable
	{
		private ISystemRemoteChangeListener _listener;
		private ISystemRemoteChangeEvent _event;
		public RemoteResourceChangedRunnable(ISystemRemoteChangeEvent event, ISystemRemoteChangeListener listener)
		{
			_event = event;
			_listener = listener;
		}

		public void run()
		{
			_listener.systemRemoteResourceChanged(_event);
		}
	}

	class RemoteChangedRunnable implements Runnable
	{
		private ISystemRemoteChangeEvent _event;
		public RemoteChangedRunnable(ISystemRemoteChangeEvent event)
		{
			_event = event;
		}

		public void run()
		{
			remoteListManager.notify(_event);
		}
	}


	class FireNewHostEvents implements Runnable
	{
		private ISubSystem[] subSystems;
		private IHost conn;
		private ISystemRegistry reg;
		private boolean expandHost;

		public FireNewHostEvents(IHost host, ISubSystem[] subSystems, ISystemRegistry registry, boolean expandHost)
		{
			this.subSystems= subSystems;
			this.conn = host;
			this.reg = registry;
			this.expandHost = expandHost;
		}

		public void run()
		{
			int eventType = ISystemResourceChangeEvents.EVENT_ADD_RELATIVE;
			SystemResourceChangeEvent event = new SystemResourceChangeEvent(conn, eventType, reg);
			//event.setPosition(pool.getConnectionPosition(conn));
			//event.setPosition(getConnectionPositionInView(conn));
			IHost previous = getPreviousHost(conn);
			if (previous != null)
			{
				event.setRelativePrevious(previous);
			}
			else
			{
				event.setType(ISystemResourceChangeEvents.EVENT_ADD);
			}
			fireEvent(event);
			fireModelChangeEvent(
					ISystemModelChangeEvents.SYSTEM_RESOURCE_ADDED,
					ISystemModelChangeEvents.SYSTEM_RESOURCETYPE_CONNECTION,
					conn, null);

			for (int s = 0; s < subSystems.length; s++)
			{
				ISubSystem ss = subSystems[s];
				fireModelChangeEvent(ISystemModelChangeEvents.SYSTEM_RESOURCE_ADDED, ISystemModelChangeEvents.SYSTEM_RESOURCETYPE_SUBSYSTEM, ss, null);
			}

			// for bug 249247 - expand the connection after completing the wizard
			if (expandHost){
				SystemResourceChangeEvent expandEvent = new SystemResourceChangeEvent(conn, ISystemResourceChangeEvents.EVENT_SELECT_EXPAND, reg);
				fireEvent(expandEvent);
			}
		}
	}

	private ISubSystemConfigurator[] getApplicableConfigurators(ISubSystemConfiguration ssf, ISubSystemConfigurator[] allPages)
	{
		if ((allPages == null) || (allPages.length == 0))
			return null;
		int count = 0;
		for (int idx = 0; idx < allPages.length; idx++)
			if (allPages[idx].getSubSystemConfiguration() == ssf)
				++count;
		if (count == 0)
			return null;
		ISubSystemConfigurator[] subPages = new ISubSystemConfigurator[count];
		count = 0;
		for (int idx = 0; idx < allPages.length; idx++)
			if (allPages[idx].getSubSystemConfiguration() == ssf)
				subPages[count++] = allPages[idx];
		return subPages;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.core.model.ISystemRegistry#createHost(java.lang.String, org.eclipse.rse.core.IRSESystemType, java.lang.String, java.lang.String, java.lang.String)
	 */
	public IHost createHost(String profileName, IRSESystemType systemType, String connectionName, String hostName, String description)
		throws Exception
	{
		return createHost(profileName, systemType, connectionName, hostName, description, true);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.model.ISystemRegistry#createHost(java.lang.String, org.eclipse.rse.core.IRSESystemType, java.lang.String, java.lang.String, java.lang.String, boolean)
	 */
	public IHost createHost(String profileName, IRSESystemType systemType, String connectionName, String hostName, String description, boolean createSubSystems) throws Exception
	{
		return createHost(profileName, systemType, connectionName, hostName, description, null, IRSEUserIdConstants.USERID_LOCATION_HOST, createSubSystems, null);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.core.model.ISystemRegistry#createHost(org.eclipse.rse.core.IRSESystemType, java.lang.String, java.lang.String, java.lang.String)
	 */
	public IHost createHost(IRSESystemType systemType, String connectionName, String hostName, String description)
		throws Exception
	{
		ISystemProfile profile = getSystemProfileManager().getDefaultPrivateSystemProfile();
		if (profile == null)
			profile = getSystemProfileManager().getActiveSystemProfiles()[0];
		return createHost(profile.getName(), systemType, connectionName, hostName, description);
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

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.core.model.ISystemRegistry#updateHost(org.eclipse.rse.core.model.IHost, org.eclipse.rse.core.IRSESystemType, java.lang.String, java.lang.String, java.lang.String, java.lang.String, int)
	 */
	public void updateHost(IHost conn, IRSESystemType systemType, String connectionName, String hostName, String description, String defaultUserId, int defaultUserIdLocation)
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
			RSECorePlugin.getDefault().getLogger().logError("Exception in updateConnection for " + connectionName, exc);
			lastException = exc;
			return;
		}
		catch (Exception exc)
		{
			RSECorePlugin.getDefault().getLogger().logError("Exception in updateConnection for " + connectionName, exc);
			lastException = exc;
			return;
		}
		boolean skipUpdate = (defaultUserIdChanged && !hostNameChanged && !connectionNameChanged);
		if (!skipUpdate) fireEvent(new SystemResourceChangeEvent(
		//conn,ISystemResourceChangeEvent.EVENT_CHANGE,this));
		conn, ISystemResourceChangeEvents.EVENT_PROPERTY_CHANGE, this)); // only update simple property sheet values here
		if (!skipUpdate) fireModelChangeEvent(
				ISystemModelChangeEvents.SYSTEM_RESOURCE_CHANGED,
				ISystemModelChangeEvents.SYSTEM_RESOURCETYPE_CONNECTION,
				conn, null);

		if (hostNameChanged || defaultUserIdChanged)
		{
			ISubSystem[] subsystems = getSubSystems(conn); // get list of related subsystems
			for (int idx = 0; idx < subsystems.length; idx++)
			{
				//Need to clear caches if host name changed or user ID is inherited from default
				if (hostNameChanged || (subsystems[idx].getConnectorService().supportsUserId() && subsystems[idx].getLocalUserId() == null))
				{
					try
					{
						//MOB - Bug 359554: There is no reason for disconnecting subsystems just because a default user ID changed
						if (hostNameChanged && subsystems[idx].isConnected())
						{
							subsystems[idx].disconnect(); // MJB: added conditional for defect 45754
						}
						if (defaultUserIdChanged && !subsystems[idx].isConnected())
						{
							subsystems[idx].getConnectorService().clearCredentials();
						}
						subsystems[idx].getConnectorService().clearPassword(false, true);
					}
					catch (Exception exc)
					{
					} // msg already shown
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.core.model.ISystemRegistry#setHostOffline(org.eclipse.rse.core.model.IHost, boolean)
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

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.core.model.ISystemRegistry#deleteHost(org.eclipse.rse.core.model.IHost)
	 */
	public void deleteHost(IHost conn)
	{
		Vector affectedSubSystemFactories = getSubSystemFactories(conn);
		for (int idx = 0; idx < affectedSubSystemFactories.size(); idx++)
		{
			 ((ISubSystemConfiguration) affectedSubSystemFactories.elementAt(idx)).deleteSubSystemsByConnection(conn);
		}
		conn.getHostPool().deleteHost(conn); // delete from memory and from disk.
		fireModelChangeEvent(
				ISystemModelChangeEvents.SYSTEM_RESOURCE_REMOVED,
				ISystemModelChangeEvents.SYSTEM_RESOURCETYPE_CONNECTION,
				conn, null);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.core.model.ISystemRegistry#renameHost(org.eclipse.rse.core.model.IHost, java.lang.String)
	 */
	public void renameHost(IHost conn, String newName) throws Exception
	{
		// first, pre-test for folder-in-use error:

		// it looks good, so proceed...
		String oldName = conn.getAliasName();


		// DKM - changing how this is done since there are services with different configurations now
		ISubSystem[] subsystems = conn.getSubSystems();
		for (int i = 0; i < subsystems.length; i++)
		{
			ISubSystem ss = subsystems[i];
			ss.getSubSystemConfiguration().renameSubSystemsByConnection(conn, newName);
		}

		/*
		Vector affectedSubSystemFactories = getSubSystemFactories(conn);
		for (int idx = 0; idx < affectedSubSystemFactories.size(); idx++)
			 ((ISubSystemConfiguration) affectedSubSystemFactories.elementAt(idx)).renameSubSystemsByConnection(conn, newName);
		*/
		conn.getHostPool().renameHost(conn, newName); // rename in memory and disk
		////Listening to events now
		//SystemPreferencesManager.setConnectionNamesOrder(); // update preferences order list
		fireModelChangeEvent(
				ISystemModelChangeEvents.SYSTEM_RESOURCE_RENAMED,
				ISystemModelChangeEvents.SYSTEM_RESOURCETYPE_CONNECTION,
				conn, oldName);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.core.model.ISystemRegistry#moveHosts(java.lang.String, org.eclipse.rse.core.model.IHost[], int)
	 * FIXME PROBLEM: CAN'T RE-ORDER FOLDERS SO CAN WE SUPPORT THIS ACTION?</b>
	 */
	public void moveHosts(String profileName, IHost conns[], int delta)
	{
		ISystemHostPool pool = getHostPool(profileName);
		pool.moveHosts(conns, delta);
		////Listening to Event now
		//SystemPreferencesManager.setConnectionNamesOrder();
		//fireEvent(new SystemResourceChangeEvent(pool.getSystemConnections(),ISystemResourceChangeEvent.EVENT_MOVE_MANY,this));
		SystemResourceChangeEvent event = new SystemResourceChangeEvent(conns, ISystemResourceChangeEvents.EVENT_MOVE_MANY, this);
		event.setPosition(delta);
		fireEvent(event);
		// fire new model change event, which BPs might listen for...
		for (int idx=0; idx<conns.length; idx++)
			fireModelChangeEvent(
					ISystemModelChangeEvents.SYSTEM_RESOURCE_REORDERED,
					ISystemModelChangeEvents.SYSTEM_RESOURCETYPE_CONNECTION,
					conns[idx], null);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.core.model.ISystemRegistry#copyHost(org.eclipse.core.runtime.IProgressMonitor, org.eclipse.rse.core.model.IHost, org.eclipse.rse.core.model.ISystemProfile, java.lang.String)
	 */
	public IHost copyHost(IHost conn, ISystemProfile targetProfile, String newName, IProgressMonitor monitor) throws Exception
	{
		Exception lastExc = null;
		boolean failed = false;
		String msg = null;
		String oldName = conn.getAliasName();
		ISystemHostPool oldPool = conn.getHostPool();
		ISystemHostPool targetPool = getHostPool(targetProfile);
		IHost newConn = null;

		RSECorePlugin.getDefault().getLogger().logDebugMessage(this.getClass().getName(), "Start of system connection copy. From: " + oldName + " to: " + newName);

		// STEP 0: BRING ALL IMPACTED SUBSYSTEM FACTORIES TO LIFE NOW, BEFORE DOING THE CLONE.
		getSubSystemFactories(conn);
		if (errorLoadingFactory)
			return null;

		try
		{
			// STEP 1: COPY CONNECTION ITSELF, MINUS ITS SUBSYSTEMS...
			newConn = oldPool.cloneHost(targetPool, conn, newName);
			// Fix bug#282238: NPE when copying host and overwrite itself
			if (newConn == null) {
				return null;
			}

			// STEP 2: COPY ALL SUBSYSTEMS FOR THE COPIED CONNECTION
			msg = RSECoreMessages.MSG_COPYSUBSYSTEMS_PROGRESS;
			//monitor.subTask(msg);
			RSECorePlugin.getDefault().getLogger().logDebugMessage(this.getClass().getName(), msg);

			ISubSystem[] subsystems = null;
			ISubSystemConfiguration factory = null;
			msg = "Copying subsystems for connection " + conn.getAliasName();
			//monitor.subTask(msg);
			RSECorePlugin.getDefault().getLogger().logDebugMessage(this.getClass().getName(), msg);
			subsystems = getSubSystems(conn); // get old subsystems for this connection
			if ((subsystems != null) && (subsystems.length > 0))
			{
				for (int jdx = 0; jdx < subsystems.length; jdx++)
				{
					msg += ": subsystem " + subsystems[jdx].getName();
					//monitor.subTask(msg);
					RSECorePlugin.getDefault().getLogger().logDebugMessage(this.getClass().getName(), msg);
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
				RSECorePlugin.getDefault().getLogger().logError("Exception (ignored) cleaning up from copy-connection exception.", exc);
			}
			throw (lastExc);
		}
		RSECorePlugin.getDefault().getLogger().logDebugMessage(this.getClass().getName(), "Copy of system connection " + oldName + " to " + newName + " successful");
		if (getSystemProfileManager().isSystemProfileActive(targetProfile.getName()))
		{
			int eventType = ISystemResourceChangeEvents.EVENT_ADD_RELATIVE;
			SystemResourceChangeEvent event = new SystemResourceChangeEvent(newConn, eventType, this);
			event.setRelativePrevious(getPreviousHost(newConn));
			//SystemResourceChangeEvent event = new SystemResourceChangeEvent(newConn,ISystemResourceChangeEvent.EVENT_ADD,this);
			//event.setPosition(getConnectionPositionInView(newConn));
			fireEvent(event);
		}
		fireModelChangeEvent(
				ISystemModelChangeEvents.SYSTEM_RESOURCE_ADDED,
				ISystemModelChangeEvents.SYSTEM_RESOURCETYPE_CONNECTION,
				newConn, null);
		return newConn;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.core.model.ISystemRegistry#moveHost(org.eclipse.core.runtime.IProgressMonitor, org.eclipse.rse.core.model.IHost, org.eclipse.rse.core.model.ISystemProfile, java.lang.String)
	 */
	public IHost moveHost(IHost conn, ISystemProfile targetProfile, String newName, IProgressMonitor monitor) throws Exception
	{
		IHost newConn = null;
		try
		{
			newConn = copyHost(conn, targetProfile, newName, monitor);
			if (newConn != null)
			{
				deleteHost(conn); // delete old connection now that new one created successfully
				RSECorePlugin.getDefault().getLogger().logDebugMessage(this.getClass().getName(), "Move of system connection " + conn.getAliasName() + " to profile " + targetProfile.getName() + " successful");
				fireEvent(new SystemResourceChangeEvent(conn, ISystemResourceChangeEvents.EVENT_DELETE, this));
			}
		}
		catch (Exception exc)
		{
			//RSECorePlugin.getDefault().getLogger().logError("Exception moving system connection " + conn.getAliasName() + " to profile " + targetProfile.getName(), exc);
			throw exc;
		}
		return newConn;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.core.model.ISystemRegistry#isAnySubSystemSupportsConnect(org.eclipse.rse.core.model.IHost)
	 */
	public boolean isAnySubSystemSupportsConnect(IHost conn) {
		Vector v = getSubSystemFactories(conn);

		if (v != null) {
			Iterator iter = v.iterator();

			while (iter.hasNext()) {
				Object obj = iter.next();

				if (obj instanceof ISubSystemConfiguration) {
					ISubSystemConfiguration config = (ISubSystemConfiguration)obj;

					if (config.supportsSubSystemConnect()) {
						return true;
					}
				}
			}
		}

		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.core.model.ISystemRegistry#isAnySubSystemConnected(org.eclipse.rse.core.model.IHost)
	 */
	public boolean isAnySubSystemConnected(IHost conn)
	{
		boolean any = false;
		ISubSystem[] subsystems = getSubSystems(conn);
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
	 * Check if there are any subsystem configurations that have not yet been instantiated
	 * and apply to the given system type.
	 * @param systemType the system type to check
	 * @return <code>true</code> if there are any matching subsystem configurations not yet instantiated.
	 */
	public boolean hasInactiveSubsystemConfigurations(IRSESystemType systemType)
	{
		if (subsystemConfigurationProxies != null)
		{
			for (int idx = 0; idx < subsystemConfigurationProxies.length; idx++)
			{
				if (!subsystemConfigurationProxies[idx].isSubSystemConfigurationActive()
				  && subsystemConfigurationProxies[idx].appliesToSystemType(systemType))
				{
					return true;
				}
			}
		}
		return false;

	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.core.model.ISystemRegistry#areAllSubSystemsConnected(org.eclipse.rse.core.model.IHost)
	 */
	public boolean areAllSubSystemsConnected(IHost conn)
	{
		boolean all = true;
		if (hasInactiveSubsystemConfigurations(conn.getSystemType())) {
			//any uninitialized subsystem configuration that applies to the system type can not be connected.
			//TODO this may change in the future: We might want to have markup in the plugin.xml
			//to check whether a subsystem configuration is actually connectable or not
			return false;
		}

		//May force load subsystem configurations here because there are no inactive ones for our system type.
		//Do we need to force load actual subsystems too, just to check if they are connected?
		ISubSystem[] subsystems = getSubSystems(conn);
		if (subsystems == null) {
			//If there are no subsystems, they are all connected.
			return true;
		}

		for (int idx = 0; all && (idx < subsystems.length); idx++)
		{
			ISubSystem ss = subsystems[idx];
			if (!ss.isConnected() && ss.getSubSystemConfiguration().supportsSubSystemConnect())
			{
				//we ignore unconnected subsystems that can not be connected anyways.
			    return false;
			}
		}
		return all;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.core.model.ISystemRegistry#disconnectAllSubSystems(org.eclipse.rse.core.model.IHost)
	 */
	public void disconnectAllSubSystems(IHost conn)
	{
		// get subsystems lazily, because not instantiated ones cannot be disconnected anyways.
		ISubSystem[] subsystems = getSubSystems(conn);
		if (subsystems == null)
			return;

		// dy:  defect 47281, user repeatedly prompted to disconnect if there is an open file
		// and they keep hitting cancel.
		boolean cancelled = false;
		for (int idx = 0; idx < subsystems.length && !cancelled; idx++)
		{
			ISubSystem ss = subsystems[idx];
			if (ss.isConnected() && ss.getSubSystemConfiguration().supportsSubSystemConnect())
			{
				try
				{
					//ss.getConnectorService().disconnect(); defect 40675
					ss.disconnect();
				}
				catch (InterruptedException exc)
				{
					System.out.println("Cancelled");
					cancelled = true;
				}
				catch (Exception exc)
				{
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.core.model.ISystemRegistry#connectedStatusChange(org.eclipse.rse.core.subsystems.ISubSystem, boolean, boolean)
	 */
	public void connectedStatusChange(ISubSystem subsystem, boolean connected, boolean wasConnected)
	{
		connectedStatusChange(subsystem, connected, wasConnected, true);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.core.model.ISystemRegistry#connectedStatusChange(org.eclipse.rse.core.subsystems.ISubSystem, boolean, boolean, boolean)
	 */
	public void connectedStatusChange(ISubSystem subsystem, boolean connected, boolean wasConnected, boolean collapseTree)
	{
		IHost conn = subsystem.getHost();

		IConnectorService effectedConnectorService = subsystem.getConnectorService();

		if (connected != wasConnected)
		{
			int eventId = ISystemResourceChangeEvents.EVENT_ICON_CHANGE;
			fireEvent(new SystemResourceChangeEvent(conn, eventId, this));

			SystemResourceChangeEvent event = new SystemResourceChangeEvent(subsystem, eventId, conn);
			fireEvent(event);


			// fire for each subsystem
			ISubSystem[] sses = getSubSystems(conn);
			for (int i = 0; i < sses.length; i++)
			{
			    ISubSystem ss = sses[i];

			    // only fire the event for subsystems that share the effected connector service
			    if (ss != subsystem && ss.getConnectorService().equals(effectedConnectorService))
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

			    // only fire the event for subsystems that share the effected connector service
			    if (ss != subsystem && ss.getConnectorService().equals(effectedConnectorService) && !ss.isConnected())
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
	}
	/**
	 * De-Register your interest in being told when a system resource such as a connection is changed.
	 */
	public void removeSystemResourceChangeListener(ISystemResourceChangeListener l)
	{
		listenerManager.removeSystemResourceChangeListener(l);
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
	        IRSEBaseReferencingObject[] references = ((ISystemFilter)src).getReferencingObjects();
	        for (int i = 0; i < references.length; i++)
	        {
	            IRSEBaseReferencingObject ref = references[i];
	            if (ref instanceof ISystemContainer)
	            {
	                ((ISystemContainer)ref).markStale(true);
	            }
	        }
	    }

		if (!listenerManager.hasListeners()) return;

	    if (onMainThread()) {
	    	listenerManager.notify(event);
	    }
	    else {
	    	runOnMainThread(new NotifyResourceChangedRunnable(event));
	    }

	}
	/**
	 * Notify a specific listener of a change to a system resource such as a connection.
	 */
	public void fireEvent(ISystemResourceChangeListener l, ISystemResourceChangeEvent event)
	{
		if (onMainThread()) {
			l.systemResourceChanged(event);
		}
		else {
			runOnMainThread(new ResourceChangedRunnable(event, l));
		}
	}

	/**
	 * Return the listener manager such that the SystemRegistryUI
	 * can re-use it for posting events that can only be posted
	 * in UI.
	 * @return the System resource change listener manager
	 *    used by the registry.
	 */
	public SystemResourceChangeManager getResourceChangeManager() {
		return listenerManager;
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
	}
	/**
	 * De-Register your interest in being told when an RSE model resource is changed.
	 */
	public void removeSystemModelChangeListener(ISystemModelChangeListener l)
	{
		modelListenerManager.removeSystemModelChangeListener(l);
	}

	private boolean onMainThread()
	{
		return Display.getCurrent() != null;
	}

	private void runOnMainThread(Runnable runnable)
	{
		Display.getDefault().asyncExec(runnable);
	}

	/**
	 * Notify all listeners of a change to a system model resource such as a connection.
	 * You would not normally call this as the methods in this class call it when appropriate.
	 */
	public void fireEvent(ISystemModelChangeEvent event)
	{
		if (!modelListenerManager.hasListeners()) return;
		if (onMainThread()) {
			modelListenerManager.notify(event);
		}
		else {
			// fire this on the main thread
			runOnMainThread(new NotifyModelChangedRunnable(event));
		}
	}
	/**
	 * Notify all listeners of a change to a system model resource such as a connection.
	 * This one takes the information needed and creates the event for you.
	 */
	public void fireModelChangeEvent(int eventType, int resourceType, Object resource, String oldName)
	{
		if (!modelListenerManager.hasListeners()) return;
		SystemModelChangeEvent modelEvent = new SystemModelChangeEvent();
		modelEvent.setEventType(eventType);
		modelEvent.setResourceType(resourceType);
		modelEvent.setResource(resource);
		modelEvent.setOldName(oldName);

		if (onMainThread()) {
			modelListenerManager.notify(modelEvent);
		}
		else {
			// fire this one the main thread
			runOnMainThread(new NotifyModelChangedRunnable(modelEvent));
		}
	}



	/**
	 * Notify a specific listener of a change to a system model resource such as a connection.
	 */
	public void fireEvent(ISystemModelChangeListener l, ISystemModelChangeEvent event)
	{
		if (onMainThread()) {
			l.systemModelResourceChanged(event);
		}
		else {
			runOnMainThread(new ModelResourceChangedRunnable(event, l));
		}
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
	}
	/**
	 * De-Register your interest in being told when a remote resource is changed.
	 */
	public void removeSystemRemoteChangeListener(ISystemRemoteChangeListener l)
	{
		remoteListManager.removeSystemRemoteChangeListener(l);
	}

	/**
	 * Query if the ISystemRemoteChangeListener is already listening for SystemRemoteChange events
	 */
	public boolean isRegisteredSystemRemoteChangeListener(ISystemRemoteChangeListener l)
	{
		return remoteListManager.isRegisteredSystemRemoteChangeListener(l);
	}

	/**
	 * Notify all listeners of a change to a remote resource such as a file.
	 * You would not normally call this as the methods in this class call it when appropriate.
	 */
	public void fireEvent(ISystemRemoteChangeEvent event)
	{
		if (!remoteListManager.hasListeners()) return;
		if (onMainThread()) {
			remoteListManager.notify(event);
		}
		else {
			runOnMainThread(new RemoteChangedRunnable(event));
		}
	}

	/**
	 * Notify all listeners of a change to a remote resource such as a file.
	 * This one takes the information needed and creates the event for you.
	 * @param eventType - one of the constants from {@link org.eclipse.rse.core.events.ISystemRemoteChangeEvents}
	 * @param resource - the remote resource object, or absolute name of the resource as would be given by calling getAbsoluteName on its remote adapter
	 * @param resourceParent - the remote resource's parent object, or absolute name, if that is known. If it is non-null, this will aid in refreshing occurences of that parent.
	 * @param subsystem - the subsystem which contains this remote resource. This allows the search for impacts to be
	 *   limited to subsystems of the same parent factory, and to connections with the same hostname as the subsystem's connection.
	 * @param oldNames - on a rename, copy or move operation, these are the absolute names of the resources prior to the operation
	 */
	public void fireRemoteResourceChangeEvent(int eventType, Object resource, Object resourceParent, ISubSystem subsystem, String[] oldNames)
	{
		if (resourceParent instanceof ISystemContainer)
		{
			((ISystemContainer)resourceParent).markStale(true);
		}
		// mark stale any filters that reference this object
		invalidateFiltersFor(resourceParent, subsystem);

		if (!remoteListManager.hasListeners()) return;

		SystemRemoteChangeEvent remoteEvent = new SystemRemoteChangeEvent();
		remoteEvent.setEventType(eventType);
		remoteEvent.setResource(resource);
		remoteEvent.setResourceParent(resourceParent);
		remoteEvent.setOldNames(oldNames);
		remoteEvent.setSubSystem(subsystem);

		if (onMainThread())
		{
			remoteListManager.notify(remoteEvent);
		}
		else
		{
			runOnMainThread(new RemoteChangedRunnable(remoteEvent));
		}
	}

	/**
	 * Notify all listeners of a change to a remote resource such as a file.
	 * This one takes the information needed and creates the event for you.
	 * @param eventType - one of the constants from {@link org.eclipse.rse.core.events.ISystemRemoteChangeEvents}
	 * @param resource - the remote resource object, or absolute name of the resource as would be given by calling getAbsoluteName on its remote adapter
	 * @param resourceParent - the remote resource's parent object, or absolute name, if that is known. If it is non-null, this will aid in refreshing occurences of that parent.
	 * @param subsystem - the subsystem which contains this remote resource. This allows the search for impacts to be
	 *   limited to subsystems of the same parent factory, and to connections with the same hostname as the subsystem's connection.
	 * @param oldNames - on a rename, copy or move operation, these are the absolute names of the resources prior to the operation
	 * @param originatingViewer - optional. If set, this gives the viewer a clue that it should select the affected resource after refreshing its parent.
	 *    This saves sending a separate event to reveal and select the new created resource on a create event, for example.
	 */
	public void fireRemoteResourceChangeEvent(int eventType, Object resource, Object resourceParent, ISubSystem subsystem, String[] oldNames, Object originatingViewer)
	{
		if (resourceParent instanceof ISystemContainer)
		{
			((ISystemContainer)resourceParent).markStale(true);
		}
		// mark stale any filters that reference this object
		invalidateFiltersFor(resourceParent, subsystem);

		if (!remoteListManager.hasListeners()) return;

		SystemRemoteChangeEvent	remoteEvent = new SystemRemoteChangeEvent();
		remoteEvent.setEventType(eventType);
		remoteEvent.setResource(resource);
		remoteEvent.setResourceParent(resourceParent);
		remoteEvent.setOldNames(oldNames);
		remoteEvent.setSubSystem(subsystem);
		remoteEvent.setOriginatingViewer(originatingViewer);

		if (onMainThread())
		{
			remoteListManager.notify(remoteEvent);
		}
		else
		{
			runOnMainThread(new RemoteChangedRunnable(remoteEvent));
		}
	}

	/**
	 * Notify all listeners of a change to a remote resource such as a file.
	 * This one takes the information needed and creates the event for you.
	 * @param operation - the operation for which this event was fired
	 * @param eventType - one of the constants from {@link org.eclipse.rse.core.events.ISystemRemoteChangeEvents}
	 * @param resource - the remote resource object, or absolute name of the resource as would be given by calling getAbsoluteName on its remote adapter
	 * @param resourceParent - the remote resource's parent object, or absolute name, if that is known. If it is non-null, this will aid in refreshing occurences of that parent.
	 * @param subsystem - the subsystem which contains this remote resource. This allows the search for impacts to be
	 *   limited to subsystems of the same parent factory, and to connections with the same hostname as the subsystem's connection.
     * @param oldNames - on a rename, copy or move operation, these are the absolute names of the resources prior to the operation
	 */
	public void fireRemoteResourceChangeEvent(String operation, int eventType, Object resource, Object resourceParent, ISubSystem subsystem, String[] oldNames)
	{
		if (resourceParent instanceof ISystemContainer)
		{
			((ISystemContainer)resourceParent).markStale(true);
		}
		// mark stale any filters that reference this object
		invalidateFiltersFor(resourceParent, subsystem);

		if (!remoteListManager.hasListeners()) return;

		SystemRemoteChangeEvent remoteEvent = new SystemRemoteChangeEvent();
		remoteEvent.setOperation(operation);
		remoteEvent.setEventType(eventType);
		remoteEvent.setResource(resource);
		remoteEvent.setResourceParent(resourceParent);
		remoteEvent.setOldNames(oldNames);
		remoteEvent.setSubSystem(subsystem);

		if (onMainThread())
		{
			remoteListManager.notify(remoteEvent);
		}
		else
		{
			runOnMainThread(new RemoteChangedRunnable(remoteEvent));
		}
	}

	/**
	 * Notify all listeners of a change to a remote resource such as a file.
	 * This one takes the information needed and creates the event for you.
	 * @param operation - the operation for which this event was fired
	 * @param eventType - one of the constants from {@link org.eclipse.rse.core.events.ISystemRemoteChangeEvents}
	 * @param resource - the remote resource object, or absolute name of the resource as would be given by calling getAbsoluteName on its remote adapter
	 * @param resourceParent - the remote resource's parent object, or absolute name, if that is known. If it is non-null, this will aid in refreshing occurences of that parent.
	 * @param subsystem - the subsystem which contains this remote resource. This allows the search for impacts to be
	 *   limited to subsystems of the same parent factory, and to connections with the same hostname as the subsystem's connection.
	 * @param oldNames - on a rename, copy or move operation, these are the absolute names of the resources prior to the operation
	 * @param originatingViewer - optional. If set, this gives the viewer a clue that it should select the affected resource after refreshing its parent.
	 *    This saves sending a separate event to reveal and select the new created resource on a create event, for example.
	 */
	public void fireRemoteResourceChangeEvent(String operation, int eventType, Object resource, Object resourceParent, ISubSystem subsystem, String[] oldNames, Object originatingViewer)
	{
		if (resourceParent instanceof ISystemContainer)
		{
			((ISystemContainer)resourceParent).markStale(true);
		}
		// mark stale any filters that reference this object
		invalidateFiltersFor(resourceParent, subsystem);

		if (!remoteListManager.hasListeners()) return;

		SystemRemoteChangeEvent remoteEvent = new SystemRemoteChangeEvent();
		remoteEvent.setOperation(operation);
		remoteEvent.setEventType(eventType);
		remoteEvent.setResource(resource);
		remoteEvent.setResourceParent(resourceParent);
		remoteEvent.setOldNames(oldNames);
		remoteEvent.setSubSystem(subsystem);
		remoteEvent.setOriginatingViewer(originatingViewer);

		if (onMainThread())
		{
			remoteListManager.notify(remoteEvent);
		}
		else
		{
			runOnMainThread(new RemoteChangedRunnable(remoteEvent));
		}
	}

    /**
     * Returns the implementation of ISystemRemoteElement for the given
     * object.  Returns null if this object does not adaptable to this.
     */
    protected IRemoteObjectIdentifier getRemoteObjectIdentifier(Object o)
    {
		//Try 1: element already an instance of IRemoteObjectIdentifier?
    	if (o instanceof IRemoteObjectIdentifier) {
    		return (IRemoteObjectIdentifier)o;
    	}
    	//Try 2: adapts to IRemoteObjectIdentifier (non-UI code only!)
    	IRemoteObjectIdentifier adapter = null;
    	if (o instanceof IAdaptable) {
    		adapter = (IRemoteObjectIdentifier)((IAdaptable)o).getAdapter(IRemoteObjectIdentifier.class);
    		if (adapter!=null) return adapter;
    	} else if (o==null) {
    		return null;
    	}
    	//Try 3: IRemoteObjectIdentifier via factories.
    	//TODO Try loadAdapter() to force lazy loading?
		adapter = (IRemoteObjectIdentifier)Platform.getAdapterManager().getAdapter(o, IRemoteObjectIdentifier.class);
		if (adapter==null) {
			//Try 4: ISystemDragDropAdapter, fallback to old factories provided via AbstractSystemViewRemoteAdapterFactory
			//This is a fallback for pre-RSE-3.0 code and may introduce UI dependency!
			if (o instanceof IAdaptable) {
				//TODO Try loadAdapter() to force lazy loading?
	    		adapter = (ISystemDragDropAdapter)((IAdaptable)o).getAdapter(ISystemDragDropAdapter.class);
	    		if (adapter!=null) return adapter;
			}
			adapter = (ISystemDragDropAdapter)Platform.getAdapterManager().getAdapter(o, ISystemDragDropAdapter.class);
    	}
		return adapter;
    }

	 private String getRemoteResourceAbsoluteName(Object remoteResource)
	    {
	    	if (remoteResource == null)
	    	  return null;
	    	String remoteResourceName = null;
	        if (remoteResource instanceof String)
	    	  remoteResourceName = (String)remoteResource;
	        else if (remoteResource instanceof SystemFilterReference)
	        {
	        	ISystemFilterReference ref = (ISystemFilterReference)remoteResource;
	        	ISubSystem ss = ref.getSubSystem();
	        	if (!ss.isOffline()){
	        		remoteResource = ss.getTargetForFilter(ref);
	        	}
	        	else {
	        		return null;
	        	}

	      		IRemoteObjectIdentifier rid = getRemoteObjectIdentifier(remoteResource);
	    		if (rid == null)
	    		  return null;
	    		remoteResourceName = rid.getAbsoluteName(remoteResource);
	        }
	        else
	    	{
	    		IRemoteObjectIdentifier rid = getRemoteObjectIdentifier(remoteResource);
	    		if (rid == null)
	    		  return null;
	    		remoteResourceName = rid.getAbsoluteName(remoteResource);
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
		return findFilterReferencesFor(resource, subsystem, true);
	}

	public List findFilterReferencesFor(Object resource, ISubSystem subsystem, boolean onlyCached)
	{
	    String elementName = getRemoteResourceAbsoluteName(resource);
	   List results = new ArrayList();
	    if (subsystem != null && elementName != null && subsystem.getSubSystemConfiguration().supportsFilters())
	    {
		    ISystemFilterReference[] refs = subsystem.getFilterPoolReferenceManager().getSystemFilterReferences(subsystem);
		    for (int i = 0; i < refs.length; i++)
		    {
		        ISystemFilterReference filterRef = refs[i];

		        if (!onlyCached || (!filterRef.isStale() && filterRef.hasContents(SystemChildrenContentsType.getInstance())))

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
		if (onMainThread()) {
			l.systemRemoteResourceChanged(event);
		}
		else {
			runOnMainThread(new RemoteResourceChangedRunnable(event, l));
		}

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
		if (!preferenceListManager.hasListeners()) return;
		if (onMainThread()) {
			preferenceListManager.notify(event);
		}
		else {
			runOnMainThread(new NotifyPreferenceChangedRunnable(event));
		}
	}
	/**
	 * Notify a specific listener of a change to a system preference
	 */
	public void fireEvent(ISystemPreferenceChangeListener l, ISystemPreferenceChangeEvent event)
	{
		if (!preferenceListManager.hasListeners()) return;
		if (onMainThread()) {
			l.systemPreferenceChanged(event);
		}
		else {
			runOnMainThread(new PreferenceChangedRunnable(event, l));
		}
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

		// step 0_b: force every subsystem of every connection to be active
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
		ISystemProfile[] notSaved = RSECorePlugin.getThePersistenceManager().commitProfiles(5000);
		return notSaved.length > 0;
	}

	/**
	 * Save specific connection pool
	 * @return true if saved ok, false if error encountered. If false, call getLastException().
	 */
	public boolean saveHostPool(ISystemHostPool pool)
	{
		return pool.commit();
	}

	/**
	 * Save specific connection
	 * @return true if saved ok, false if error encountered. If false, call getLastException().
	 */
	public boolean saveHost(IHost conn)
	{
		return conn.commit();
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
		SystemProfileManager profileManager = RSECorePlugin.getTheSystemProfileManager();

		SystemHostPool pool = null;
		SystemPreferencesManager prefmgr = SystemPreferencesManager.getPreferencesManager();
		if (!RSECorePlugin.getThePersistenceManager().restore(profileManager))
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
					RSECorePlugin.getDefault().getLogger().logError("Exception in restore for connection pool " + profiles[idx].getName(), exc);
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
	public ISystemFilterStartHere getSystemFilterStartHere() {
		return SystemFilterStartHere.getInstance();
	}

	// ----------------------------------
	// SYSTEMVIEWINPUTPROVIDER METHODS...
	// ----------------------------------

	/**
	 * Return the child objects to constitute the root elements in the system view tree.
	 * We return all connections that have an enabled system type.
	 */
	public Object[] getSystemViewRoots()
	{
		//DKM - only return enabled connections now
		IHost[] connections = getHosts();
		List result = new ArrayList();
		for (int i = 0; i < connections.length; i++) {
			IHost con = connections[i];
			IRSESystemType sysType = con.getSystemType();
			// sysType can be null if workspace contains a host that is no longer defined by the workbench
			if (sysType != null && sysType.isEnabled()) {
				// Note: System types without registered subsystems get disabled by the default
				// AbstractRSESystemType implementation itself! There is no need to re-check this here again.
				result.add(con);
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
	 * Return true if we are listing connections or not, so we know whether
	 * we are interested in connection-add events
	 */
	public boolean showingConnections()
	{
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.ui.view.ISystemViewInputProvider#setViewer(java.lang.Object)
	 */
	public void setViewer(Object viewer)
	{
		this.viewer = viewer;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.ui.view.ISystemViewInputProvider#getViewer()
	 */
	public Object getViewer()
	{
		return viewer;
	}


}//SystemRegistryImpl
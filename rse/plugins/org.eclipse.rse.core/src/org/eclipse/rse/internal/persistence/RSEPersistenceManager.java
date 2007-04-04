/********************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation. All rights reserved.
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
 * David Dykstal (IBM) - 142806: refactoring persistence framework
 ********************************************************************************/

package org.eclipse.rse.internal.persistence;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.SystemResourceManager;
import org.eclipse.rse.core.filters.ISystemFilterPool;
import org.eclipse.rse.core.filters.ISystemFilterPoolManager;
import org.eclipse.rse.core.filters.ISystemFilterPoolManagerProvider;
import org.eclipse.rse.core.filters.SystemFilterPoolManager;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.ISystemHostPool;
import org.eclipse.rse.core.model.ISystemProfile;
import org.eclipse.rse.core.model.ISystemProfileManager;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.internal.persistence.dom.RSEDOMExporter;
import org.eclipse.rse.internal.persistence.dom.RSEDOMImporter;
import org.eclipse.rse.logging.Logger;
import org.eclipse.rse.persistence.IRSEPersistenceManager;
import org.eclipse.rse.persistence.IRSEPersistenceProvider;
import org.eclipse.rse.persistence.dom.RSEDOM;

/**
 * The persistence manager controls all aspects of persisting the RSE data model. It will both
 * save and restore this model. There should be only persistence manager in existence. This instance
 * can be retrieved using RSEUIPlugin.getThePersistenceManager.
 * @see RSECorePlugin#getThePersistenceManager() 
 */
public class RSEPersistenceManager implements IRSEPersistenceManager {

	private static final int STATE_NONE = 0;
	private static final int STATE_IMPORTING = 1;
	private static final int STATE_EXPORTING = 2;

	private static IProject remoteSystemsProject = null;
	public static final String RESOURCE_PROJECT_NAME = "RemoteSystemsConnections"; //$NON-NLS-1$
	/**
	 * Get the default remote systems project.
	 * @return IProject handle of the project. Use exists() to test existence.
	 */
	public static IProject getRemoteSystemsProject() {
		if (remoteSystemsProject == null) 
		{
			remoteSystemsProject = SystemResourceManager.getRemoteSystemsProject();
		}

		return remoteSystemsProject;
	}
	private Map loadedProviders = new HashMap(10);

	private int _currentState = STATE_NONE;
	private RSEDOMExporter _exporter;

	private RSEDOMImporter _importer;

	public RSEPersistenceManager(ISystemRegistry registry) {
		//		_registry = registry;
		_exporter = RSEDOMExporter.getInstance();
		_exporter.setSystemRegistry(registry);
		_importer = RSEDOMImporter.getInstance();
		_importer.setSystemRegistry(registry);
	}

	public boolean commit(ISystemFilterPoolManager filterPoolManager) {
		if (filterPoolManager.isDirty()) {
			commit(filterPoolManager.getSystemProfile());
			filterPoolManager.setDirty(false);
		}
		return false;
	}

	public boolean commit(ISystemHostPool connectionPool) {
		if (connectionPool.isDirty()) {
			commit(connectionPool.getSystemProfile());
			connectionPool.setDirty(false);
		}
		/*
		 Host[] connections = connectionPool.getHosts();
		 for (int idx = 0; idx < connections.length; idx++)
		 {
		 if (!saveHost(connectionPool, connections[idx]))
		 {
		 return false;
		 }
		 }
		 return true;
		 */
		return false; // all persistence should be at profile level
	}

	/**
	 * Attempt to save single profile to disk.
	 */
	public boolean commit(ISystemProfile profile) {
		if (profile != null) {
			return save(profile, false);
		}
		return false;
	}

	/**
	 * Save all profiles to disk
	 */
	public boolean commit(ISystemProfileManager profileManager) {

		ISystemProfile[] profiles = profileManager.getSystemProfiles();
		for (int idx = 0; idx < profiles.length; idx++) {
			try {
				commit(profiles[idx]);
			} catch (Exception exc) {
				Logger logger = RSECorePlugin.getDefault().getLogger();
				String profileName = profiles[idx].getName();
				String message = "Error saving profile " + profileName; //$NON-NLS-1$
				logger.logError(message, exc);
				return false;
			}
		}

		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.persistence.IRSEPersistenceManager#deleteProfile(java.lang.String)
	 */
	public void deleteProfile(final String profileName) {
		Job job = new Job(Messages.RSEPersistenceManager_DeleteProfileJobName) {
			protected IStatus run(IProgressMonitor monitor) {
				IRSEPersistenceProvider provider = getRSEPersistenceProvider();
				IStatus result = provider.deleteProfile(profileName, monitor);
				return result;
			}
		};
		job.schedule();
	}

	private RSEDOM exportRSEDOM(ISystemProfile profile, boolean force) {
		RSEDOM dom = _exporter.createRSEDOM(profile, force);
		return dom;
	}

	/**
	 * Retrieves the persistence provider for this workbench configuration.
	 * Several persistence providers may be registered, but only one persistence provider can be used.
	 * This persistence provider's identifier is specified in the org.eclipse.rse.persistenceProvider
	 * preference and can be specified a product's config.ini file.
	 * It is retrieved using the platform's preference service.
	 * If no such preference exists the default "org.eclipse.rse.persistence.PropertyFileProvider"
	 * is used.
	 * @return the default IRSEPersistenceProvider for this installation.
	 */
	public IRSEPersistenceProvider getRSEPersistenceProvider() {
		IPreferencesService service = Platform.getPreferencesService();
		String qualifier = "org.eclipse.rse"; //$NON-NLS-1$
		String preferenceName = "persistenceProvider"; //$NON-NLS-1$
		String defaultProviderName = "org.eclipse.rse.persistence.PropertyFileProvider"; //$NON-NLS-1$
		String providerName = service.getString(qualifier, preferenceName, defaultProviderName, null);
		IRSEPersistenceProvider provider = getRSEPersistenceProvider(providerName);
		return provider;
	}

	/**
	 * Returns the persistence provider denoted by the id. Only one instance of this 
	 * persistence provider is created.
	 * @param id The id of the persistence provider, as denoted by the id attribute on its declaration.
	 * @return an IRSEPersistenceProvider which may be null if this id is not found.
	 */
	public IRSEPersistenceProvider getRSEPersistenceProvider(String id) {
		Logger logger = RSECorePlugin.getDefault().getLogger();
		IRSEPersistenceProvider provider = (IRSEPersistenceProvider) loadedProviders.get(id);
		if (provider == null) {
			IExtensionRegistry registry = Platform.getExtensionRegistry();
			IConfigurationElement[] providerCandidates = registry.getConfigurationElementsFor("org.eclipse.rse.core", "persistenceProviders"); //$NON-NLS-1$ //$NON-NLS-2$
			for (int j = 0; j < providerCandidates.length; j++) {
				IConfigurationElement providerCandidate = providerCandidates[j];
				if (providerCandidate.getName().equals("persistenceProvider")) { //$NON-NLS-1$
					String candidateId = providerCandidate.getAttribute("id"); //$NON-NLS-1$
					if (candidateId != null) {
						if (candidateId.equals(id)) {
							try {
								provider = (IRSEPersistenceProvider) providerCandidate.createExecutableExtension("class"); //$NON-NLS-1$
							} catch (CoreException e) {
								logger.logError("Exception loading persistence provider", e); //$NON-NLS-1$
							}
						}
					} else {
						logger.logError("Missing id attribute in persistenceProvider element", null); //$NON-NLS-1$
					}
				} else {
					logger.logError("Invalid element in persistenceProviders extension point", null); //$NON-NLS-1$
				}
			}
			if (provider == null) {
				logger.logError("Persistence provider not found.", null); //$NON-NLS-1$
			}
			loadedProviders.put(id, provider); // even if provider is null
		}
		return provider;
	}

	private RSEDOM importRSEDOM(String domName) {
		RSEDOM dom = null;
		IRSEPersistenceProvider provider = getRSEPersistenceProvider();
		if (provider != null) {
			dom = provider.loadRSEDOM(domName, null);
		} else {
			Logger logger = RSECorePlugin.getDefault().getLogger();
			logger.logError("Persistence provider is not available.", null); //$NON-NLS-1$
		}
		return dom;
	}

	public synchronized boolean isExporting() {
		return _currentState == STATE_EXPORTING;
	}

	public synchronized boolean isImporting() {
		return _currentState == STATE_IMPORTING;
	}

	/**
	 * Loads and restores RSE artifacts from the last session
	 * @param profileManager
	 * @return true if the profiles are loaded
	 */
	private boolean load(ISystemProfileManager profileManager) {
		boolean successful = true;
		synchronized(this) {
			if (isExporting() || isImporting()) {
				successful = false;
			} else {
				setState(STATE_IMPORTING);
			}
		}
		if(successful) {
			try {
				IProject project = getRemoteSystemsProject();
				if (!project.isSynchronized(IResource.DEPTH_ONE)) project.refreshLocal(IResource.DEPTH_ONE, null);
				IRSEPersistenceProvider persistenceProvider = getRSEPersistenceProvider();
				String[] profileNames = persistenceProvider.getSavedProfileNames();
				for (int i = 0; i < profileNames.length; i++) {
					String profileName = profileNames[i];
					RSEDOM dom = importRSEDOM(profileName);
					if (dom != null) {
						ISystemProfile restoredProfile = _importer.restoreProfile(profileManager, dom);
						if (restoredProfile == null) {
							successful = false;
						}
					} else {
						successful = false;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				successful = false;
			} finally {
				setState(STATE_NONE);
			}
		}
		return successful;
	}

	public void registerRSEPersistenceProvider(String id, IRSEPersistenceProvider provider) {
		loadedProviders.put(id, provider);
	}

	public boolean restore(ISystemFilterPool filterPool) {
		//System.out.println("restore filterpool");
		// DWD function Is this method really needed?
		return false;
	}

	public boolean restore(ISystemHostPool connectionPool) {
		return false;
	}

	public boolean restore(ISystemProfileManager profileManager) {
		return load(profileManager);
	}

	public ISystemFilterPool restoreFilterPool(String name) {
		//System.out.println("restore filter pool "+name);
		// DWD function is this method really needed?
		return null;
	}

	/**
	 * Creates a filter pool manager for a particular SubSystemConfiguration and SystemProfile. Called
	 * "restore" for historcal reasons.
	 * @param profile the profile that will own this ISystemFilterPoolManager. There is one of these per profile.
	 * @param logger the logging object for logging errors. Each ISystemFilterPoolManager has one of these.
	 * @param caller The creator/owner of this ISystemFilterPoolManager, this ends up being a SubSystemConfiguration. 
	 * @param name the name of the manager to restore. File name is derived from it when saving to one file.
	 * @return the "restored" manager.
	 */
	public ISystemFilterPoolManager restoreFilterPoolManager(ISystemProfile profile, Logger logger, ISystemFilterPoolManagerProvider caller, String name) {
		SystemFilterPoolManager mgr = SystemFilterPoolManager.createManager(profile);
		mgr.initialize(logger, caller, name); // core data
		mgr.setWasRestored(false); // managers are not "restored from disk" since they are not persistent of themselves
		return mgr;
	}

	/**
	 * Restore a connection of a given name from disk...
	 */
	protected IHost restoreHost(ISystemHostPool hostPool, String connectionName) throws Exception {
		/*
		 * FIXME //System.out.println("in SystemConnectionPoolImpl#restore for
		 * connection " + connectionName); String fileName =
		 * getRootSaveFileName(connectionName);
		 * //System.out.println(".......fileName = " + fileName);
		 * //System.out.println(".......folderName = " +
		 * getConnectionFolder(connectionName).getName()); java.util.List ext =
		 * getMOFHelpers().restore(getConnectionFolder(connectionName),fileName);
		 *  // should be exactly one profile... Iterator iList = ext.iterator();
		 * SystemConnection connection = (SystemConnection)iList.next(); if
		 * (connection != null) { if
		 * (!connection.getAliasName().equalsIgnoreCase(connectionName)) {
		 * RSEUIPlugin.logDebugMessage(this.getClass().getName(),"Incorrect
		 * alias name found in connections.xmi file for " + connectionName+".
		 * Name was reset"); connection.setAliasName(connectionName); // just in
		 * case! } internalAddConnection(connection); } return connection;
		 */
		return null;
	}

	/**
	 * Restore a profile of a given name from disk...
	 */
	protected ISystemProfile restoreProfile(ISystemProfileManager mgr, String name) throws Exception {
		/*
		 * FIXME String fileName = mgr.getRootSaveFileName(name); java.util.List
		 * ext = null;//FIXME
		 * getMOFHelpers().restore(SystemResourceManager.getProfileFolder(name),fileName);
		 *  // should be exactly one profile... Iterator iList = ext.iterator();
		 * SystemProfile profile = (SystemProfile)iList.next();
		 * mgr.initialize(profile, name); return profile;
		 */
		return null;
	}

	/**
	 * Writes the RSE model to a DOM and schedules writing of that DOM to disk.
	 * May, in fact, update an existing DOM instead of creating a new one.
	 * If in the process of importing, skip writing.
	 * @return true if the profile is written to a DOM
	 */
	private boolean save(ISystemProfile profile, boolean force) {
		boolean result = false;
		boolean acquiredLock = false;
		synchronized(this) {
			if (!isImporting()) {
				setState(STATE_EXPORTING);
				acquiredLock = true;
			}
		}
		if (acquiredLock) {
			try {
				RSEDOM dom = exportRSEDOM(profile, true); // DWD should do merge, but does not handle deletes properly yet
				result = true;
				if (dom.needsSave()) {
					Job job = dom.getSaveJob();
					if (job == null) {
						job = new SaveRSEDOMJob(dom, getRSEPersistenceProvider());
						dom.setSaveJob(job);
					}
					job.schedule(3000); // three second delay
				}
			} finally {
				setState(STATE_NONE);
			}
		}
		return result;
	}

	private synchronized void setState(int state) {
		_currentState = state;
	}

}
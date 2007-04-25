/********************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others. All rights reserved.
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
 * Martin Oberhuber (Wind River) - [184095] Replace systemTypeName by IRSESystemType
 ********************************************************************************/

package org.eclipse.rse.internal.persistence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.rse.core.IRSEPreferenceNames;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.SystemResourceManager;
import org.eclipse.rse.core.model.IRSEPersistableContainer;
import org.eclipse.rse.core.model.ISystemProfile;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.internal.core.RSECoreMessages;
import org.eclipse.rse.internal.core.model.SystemProfileManager;
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
	private static final int STATE_LOADING = 1;
	private static final int STATE_SAVING = 2;

	private static IProject remoteSystemsProject = null;

	/**
	 * Get the default remote systems project.
	 * @return IProject handle of the project.
	 */
	public static IProject getRemoteSystemsProject() {
		if (remoteSystemsProject == null) 
		{
			remoteSystemsProject = SystemResourceManager.getRemoteSystemsProject();
		}
		return remoteSystemsProject;
	}

	private Map knownProviders = new HashMap(10);
	private int _currentState = STATE_NONE;
	private RSEDOMExporter _exporter;
	private RSEDOMImporter _importer;

	public RSEPersistenceManager(ISystemRegistry registry) {
		_exporter = RSEDOMExporter.getInstance();
		_importer = RSEDOMImporter.getInstance();
		_importer.setSystemRegistry(registry);
		getProviderExtensions();
	}

	public boolean isExporting() {
		return _currentState == STATE_SAVING;
	}

	public boolean isImporting() {
		return _currentState == STATE_LOADING;
	}

	public void registerPersistenceProvider(String id, IRSEPersistenceProvider provider) {
		knownProviders.put(id, provider);
	}

	/**
	 * Attempt to save single profile to disk.
	 */
	public boolean commitProfile(ISystemProfile profile) {
		boolean result = false;
		if (profile != null) {
			result = save(profile, true);
		}
		return result;
	}

	public boolean commitProfiles() {
		boolean ok = true;
		ISystemProfile[] profiles = RSECorePlugin.getDefault().getSystemRegistry().getAllSystemProfiles();
		for (int idx = 0; idx < profiles.length && ok; idx++) {
			try {
				ok = commitProfile(profiles[idx]);
			} catch (Exception exc) {
				Logger logger = RSECorePlugin.getDefault().getLogger();
				String profileName = profiles[idx].getName();
				String message = "Error saving profile " + profileName; //$NON-NLS-1$
				logger.logError(message, exc);
				ok = false;
			}
		}
		return ok;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.persistence.IRSEPersistenceManager#migrateProfile(org.eclipse.rse.core.model.ISystemProfile, org.eclipse.rse.persistence.IRSEPersistenceProvider)
	 */
	public void migrateProfile(ISystemProfile profile, IRSEPersistenceProvider persistenceProvider) {
		IRSEPersistenceProvider oldProvider = profile.getPersistenceProvider();
		oldProvider = (oldProvider == null) ? getDefaultPersistenceProvider() : oldProvider;
		IRSEPersistenceProvider newProvider = persistenceProvider;
		newProvider = (newProvider == null) ? getDefaultPersistenceProvider() : newProvider;
		if (oldProvider != newProvider) {
			profile.setPersistenceProvider(newProvider);
			profile.commit();
			deleteProfile(oldProvider, profile.getName());
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.persistence.IRSEPersistenceManager#deleteProfile(java.lang.String)
	 */
	public void deleteProfile(final IRSEPersistenceProvider persistenceProvider, final String profileName) {
		Job job = new Job(RSECoreMessages.RSEPersistenceManager_DeleteProfileJobName) {
			protected IStatus run(IProgressMonitor monitor) {
				IRSEPersistenceProvider p = persistenceProvider != null ? persistenceProvider : getDefaultPersistenceProvider();
				IStatus result = p.deleteProfile(profileName, monitor);
				return result;
			}
		};
		job.schedule();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.persistence.IRSEPersistenceManager#restoreProfiles()
	 */
	public ISystemProfile[] restoreProfiles() {
		List profiles = loadProfiles();
		ISystemProfile[] result = new ISystemProfile[profiles.size()];
		profiles.toArray(result);
		return result;
	}
	
	public ISystemProfile restoreProfile(IRSEPersistenceProvider provider, String profileName) {
		ISystemProfile result = load(provider, profileName);
		return result;
	}

	/**
	 * Returns the persistence provider denoted by the id. Only one instance of this 
	 * persistence provider is created.
	 * @param id The id of the persistence provider, as denoted by the id attribute on its declaration.
	 * @return an IRSEPersistenceProvider which may be null if this id is not found.
	 */
	public IRSEPersistenceProvider getPersistenceProvider(String id) {
		IRSEPersistenceProvider provider = null;
		Object providerCandidate = knownProviders.get(id);
		if (providerCandidate instanceof IConfigurationElement) {
			IConfigurationElement element = (IConfigurationElement) providerCandidate;
			try {
				provider = (IRSEPersistenceProvider) element.createExecutableExtension("class"); //$NON-NLS-1$
			} catch (CoreException e) {
				Logger logger = RSECorePlugin.getDefault().getLogger();
				logger.logError("Exception loading persistence provider", e); //$NON-NLS-1$
			}
			if (provider != null) {
				knownProviders.put(id, provider);
			}
		} else if (providerCandidate instanceof IRSEPersistenceProvider) {
			provider = (IRSEPersistenceProvider) providerCandidate;
		}
		return provider;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.persistence.IRSEPersistenceManager#getPersistenceProviderIds()
	 */
	public String[] getPersistenceProviderIds() {
		Set ids = knownProviders.keySet();
		String[] result = new String[ids.size()];
		ids.toArray(result);
		return result;
	}
	
	/**
	 * Retrieves the default persistence provider for this workbench configuration.
	 * Several persistence providers may be registered, but the default one is used for all
	 * profiles that do not have one explicitly specified.
	 * This persistence provider's identifier is specified in the org.eclipse.rse.core/DEFAULT_PERSISTENCE_PROVIDER
	 * preference and can be specified a product's plugin_customization.ini file.
	 * @see IRSEPreferenceNames
	 * @return the default IRSEPersistenceProvider for this installation.
	 */
	private IRSEPersistenceProvider getDefaultPersistenceProvider() {
		Preferences preferences = RSECorePlugin.getDefault().getPluginPreferences();
		String providerId = preferences.getString(IRSEPreferenceNames.DEFAULT_PERSISTENCE_PROVIDER);
		IRSEPersistenceProvider provider = getPersistenceProvider(providerId);
		return provider;
	}

	/**
	 * Loads the map of known providers from the extensions made by all the plugins. 
	 * This is done once at initialization of the manager. As these ids are resolved to 
	 * their providers as needed, the configuration elements are replaced in the map
	 * by the persistence providers they reference.
	 */
	private void getProviderExtensions() {
		Logger logger = RSECorePlugin.getDefault().getLogger();
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IConfigurationElement[] providerCandidates = registry.getConfigurationElementsFor("org.eclipse.rse.core", "persistenceProviders"); //$NON-NLS-1$ //$NON-NLS-2$
		for (int j = 0; j < providerCandidates.length; j++) {
			IConfigurationElement configurationElement = providerCandidates[j];
			if (configurationElement.getName().equals("persistenceProvider")) { //$NON-NLS-1$
				String candidateId = configurationElement.getAttribute("id"); //$NON-NLS-1$
				if (candidateId != null) {
					knownProviders.put(candidateId, configurationElement);
				} else {
					logger.logError("Missing id attribute in persistenceProvider element", null); //$NON-NLS-1$
				}
			} else {
				logger.logError("Invalid element in persistenceProviders extension point", null); //$NON-NLS-1$
			}
		}
	}

	private List loadProfiles() {
		List profiles = new ArrayList(10);
		String[] ids = getPersistenceProviderIds();
		for (int i = 0; i < ids.length; i++) {
			String id = ids[i];
			IRSEPersistenceProvider provider = getPersistenceProvider(id);
			if (provider != null) {
				profiles.addAll(loadProfiles(provider));
			}
		}
		return profiles;
	}

	private List loadProfiles(IRSEPersistenceProvider persistenceProvider) {
		List profiles = new ArrayList(10);
		String[] profileNames = persistenceProvider.getSavedProfileNames();
		for (int i = 0; i < profileNames.length; i++) {
			String profileName = profileNames[i];
			ISystemProfile profile = load(persistenceProvider, profileName);
			profiles.add(profile);
		}
		return profiles;
	}

	/**
	 * Loads a profile of the given name using the given persistence provider. If the provider cannot 
	 * find a profile with that name, return null.
	 * @param provider the persistence provider that understands the name and can produce a profile.
	 * @param profileName the name of the profile to produce
	 * @return the profile or null
	 */
	private synchronized ISystemProfile load(IRSEPersistenceProvider provider, String profileName) {
		ISystemProfile profile = null;
		if (_currentState == STATE_NONE) {
			_currentState = STATE_LOADING;
			RSEDOM dom = provider.loadRSEDOM(profileName, null);
			if (dom != null) {
				SystemProfileManager.getDefault().setRestoring(true);
				profile = _importer.restoreProfile(dom);
				profile.setPersistenceProvider(provider);
				cleanTree(profile);
				SystemProfileManager.getDefault().setRestoring(false);
			}
			_currentState = STATE_NONE;
		}
		return profile;
	}
	
	/**
	 * Writes a profile to a DOM and schedules writing of that DOM to disk.
	 * May, in fact, update an existing DOM instead of creating a new one.
	 * If in the process of importing, skip writing.
	 * @return true if the profile is written to a DOM
	 */
	private synchronized boolean save(ISystemProfile profile, boolean force) {
		if (_currentState == STATE_NONE) {
			_currentState = STATE_SAVING;
			IRSEPersistenceProvider provider = profile.getPersistenceProvider();
			if (provider == null) {
				provider = getDefaultPersistenceProvider();
				profile.setPersistenceProvider(provider);
			}
			RSEDOM dom = _exporter.createRSEDOM(profile, force);
			cleanTree(profile);
			if (dom.needsSave()) {
				Job job = dom.getSaveJob();
				if (job == null) {
					job = new SaveRSEDOMJob(dom, getDefaultPersistenceProvider());
					dom.setSaveJob(job);
				}
				job.schedule(3000); // three second delay
			}
			_currentState = STATE_NONE;
		}
		return true;
	}
	
	private void cleanTree(IRSEPersistableContainer node) {
		node.setWasRestored(true);
		node.setTainted(false);
		node.setDirty(false);
		IRSEPersistableContainer[] children = node.getPersistableChildren();
		for (int i = 0; i < children.length; i++) {
			IRSEPersistableContainer child = children[i];
			cleanTree(child);
		}
	}

}
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
 * David Dykstal (IBM) - 142806: refactoring persistence framework
 * Martin Oberhuber (Wind River) - [184095] Replace systemTypeName by IRSESystemType
 * Martin Oberhuber (Wind River) - [177523] Unify singleton getter methods
 * Martin Oberhuber (Wind River) - [175680] Deprecate obsolete ISystemRegistry methods
 * Martin Oberhuber (Wind River) - [196919] Fix deadlock with workspace operations
 * Martin Oberhuber (Wind River) - [202416] Protect against NPEs when importing DOM
 * David Dykstal (IBM) - [189274] provide import and export operations for profiles
 * David Dykstal (IBM) - [225988] need API to mark persisted profiles as migrated
 * David Dykstal (IBM) - [226728] NPE during init with clean workspace
 * David Dykstal (IBM) - [197027] Can lose data if closing eclipse before profile is saved
 * Kevin Doyle	 (IBM) - [243821] Save occurring on Main Thread
 * David Dykstal (IBM) - [243128] Problem during migration - NPE if provider does save without using a job.
 * Martin Oberhuber (Wind River) - [261503][cleanup] Get rid of deprecated getPluginPreferences()
 * David McKnight (IBM)  -[425014] profile commit job don't always complete during shutdown
 ********************************************************************************/

package org.eclipse.rse.internal.persistence;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.eclipse.core.resources.ISaveContext;
import org.eclipse.core.resources.ISaveParticipant;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.rse.core.IRSEPreferenceNames;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.model.IRSEPersistableContainer;
import org.eclipse.rse.core.model.ISystemProfile;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.internal.core.RSECoreMessages;
import org.eclipse.rse.internal.persistence.dom.RSEDOMExporter;
import org.eclipse.rse.internal.persistence.dom.RSEDOMImporter;
import org.eclipse.rse.logging.Logger;
import org.eclipse.rse.persistence.IRSEPersistenceManager;
import org.eclipse.rse.persistence.IRSEPersistenceProvider;
import org.eclipse.rse.persistence.dom.RSEDOM;
import org.eclipse.rse.services.Mutex;

/**
 * The persistence manager controls all aspects of persisting the RSE data model. It will both
 * save and restore this model. There should be only persistence manager in existence. This instance
 * can be retrieved using RSEUIPlugin.getThePersistenceManager.
 * @see RSECorePlugin#getThePersistenceManager()
 */
public class RSEPersistenceManager implements IRSEPersistenceManager {

	private class RSESaveParticipant implements ISaveParticipant {

		public RSESaveParticipant() {
		}

		/* (non-Javadoc)
		 * @see org.eclipse.core.resources.ISaveParticipant#doneSaving(org.eclipse.core.resources.ISaveContext)
		 */
		public void doneSaving(ISaveContext context) {
			canScheduleSave = true;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.core.resources.ISaveParticipant#prepareToSave(org.eclipse.core.resources.ISaveContext)
		 */
		public void prepareToSave(ISaveContext context) throws CoreException {
			canScheduleSave = false;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.core.resources.ISaveParticipant#rollback(org.eclipse.core.resources.ISaveContext)
		 */
		public void rollback(ISaveContext context) {
			canScheduleSave = true;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.core.resources.ISaveParticipant#saving(org.eclipse.core.resources.ISaveContext)
		 */
		public void saving(ISaveContext context) throws CoreException {
			List jobs = new ArrayList(10);
			synchronized(saveJobs) {
				jobs.addAll(saveJobs);
			}
			for (Iterator z = jobs.iterator(); z.hasNext();) {
				Job job = (Job) z.next();
				try {
					job.join();
				} catch (InterruptedException e) {
					// do nothing
				}
			}
		}

	}

	private class RSESaveJobChangeListener implements IJobChangeListener {
		/* (non-Javadoc)
		 * @see org.eclipse.core.runtime.jobs.IJobChangeListener#aboutToRun(org.eclipse.core.runtime.jobs.IJobChangeEvent)
		 */
		public void aboutToRun(IJobChangeEvent event) {
			// do nothing
		}
		/* (non-Javadoc)
		 * @see org.eclipse.core.runtime.jobs.IJobChangeListener#awake(org.eclipse.core.runtime.jobs.IJobChangeEvent)
		 */
		public void awake(IJobChangeEvent event) {
			// do nothing
		}
		/* (non-Javadoc)
		 * @see org.eclipse.core.runtime.jobs.IJobChangeListener#done(org.eclipse.core.runtime.jobs.IJobChangeEvent)
		 */
		public void done(IJobChangeEvent event) {
			synchronized (saveJobs) {
				saveJobs.remove(event.getJob());
			}
		}
		/* (non-Javadoc)
		 * @see org.eclipse.core.runtime.jobs.IJobChangeListener#running(org.eclipse.core.runtime.jobs.IJobChangeEvent)
		 */
		public void running(IJobChangeEvent event) {
			// do nothing
		}
		/* (non-Javadoc)
		 * @see org.eclipse.core.runtime.jobs.IJobChangeListener#scheduled(org.eclipse.core.runtime.jobs.IJobChangeEvent)
		 */
		public void scheduled(IJobChangeEvent event) {
			synchronized (saveJobs) {
				saveJobs.add(event.getJob());
			}
		}
		/* (non-Javadoc)
		 * @see org.eclipse.core.runtime.jobs.IJobChangeListener#sleeping(org.eclipse.core.runtime.jobs.IJobChangeEvent)
		 */
		public void sleeping(IJobChangeEvent event) {
			// do nothing
		}
	}

	private class ProviderRecord {
		private String providerId = null;
		private IConfigurationElement configurationElement = null;
		private IRSEPersistenceProvider provider = null;
		private boolean restored = false;
		synchronized boolean isRestored() {
			return restored;
		}
		synchronized void setRestored(boolean restored) {
			this.restored = restored;
		}
		boolean isAutostart() {
			boolean isAutostart = (configurationElement != null && ("true".equals(configurationElement.getAttribute("autostart")))); //$NON-NLS-1$ //$NON-NLS-2$
			boolean isDefault = (providerId.equals(getDefaultPersistenceProviderId()));
			return isAutostart || isDefault;
		}
	}

	private Map knownProviders = new HashMap(10);
	private Map loadedProviders = new HashMap(10);
	private Set saveJobs = new HashSet(10);
	private RSEDOMExporter _exporter;
	private RSEDOMImporter _importer;
	private RSESaveParticipant saveParticipant = new RSESaveParticipant();
	private RSESaveJobChangeListener jobChangeListener = new RSESaveJobChangeListener();
	private Mutex mutex = new Mutex();
	private volatile boolean canScheduleSave = true;

	public RSEPersistenceManager(ISystemRegistry registry) {
		_exporter = RSEDOMExporter.getInstance();
		_importer = RSEDOMImporter.getInstance();
		_importer.setSystemRegistry(registry);
		try {
			ResourcesPlugin.getWorkspace().addSaveParticipant(RSECorePlugin.getDefault(), saveParticipant);
		} catch (CoreException e) {
			RSECorePlugin.getDefault().getLogger().logError("Could not register save participant.", e); //$NON-NLS-1$
		}
		getProviderExtensions();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.persistence.IRSEPersistenceManager#isExporting()
	 */
	public boolean isBusy() {
		return mutex.isLocked();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.persistence.IRSEPersistenceManager#registerPersistenceProvider(java.lang.String, org.eclipse.rse.persistence.IRSEPersistenceProvider)
	 */
	public void registerPersistenceProvider(String id, IRSEPersistenceProvider provider) {
		ProviderRecord pr = getProviderRecord(id);
		if (provider instanceof IRSEImportExportProvider) {
			IRSEImportExportProvider ieProvider = (IRSEImportExportProvider) provider;
			ieProvider.setId(id);
		}
		pr.provider = provider;
		loadedProviders.put(provider, id);
	}

	/**
	 * Returns the persistence provider denoted by the id. Only one instance of this
	 * persistence provider is created.
	 * @param id The id of the persistence provider, as denoted by the id attribute on its declaration.
	 * @return an IRSEPersistenceProvider which may be null if this id is not found.
	 */
	public IRSEPersistenceProvider getPersistenceProvider(String id) {
		ProviderRecord pr = getProviderRecord(id);
		if (pr.provider == null) {
			IRSEPersistenceProvider provider = loadProvider(pr.configurationElement);
			registerPersistenceProvider(id, provider);
		}
		return pr.provider;
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

	/* (non-Javadoc)
	 * @see org.eclipse.rse.persistence.IRSEPersistenceManager#commitProfile(org.eclipse.rse.core.model.ISystemProfile)
	 */
	public boolean commitProfile(ISystemProfile profile, long timeout) {
		boolean result = false;
		result = save(profile, true, timeout);
		return result;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.persistence.IRSEPersistenceManager#commitProfiles()
	 */
	public ISystemProfile[] commitProfiles(long timeout) {
		List failed = new ArrayList(10);
		ISystemProfile[] profiles = RSECorePlugin.getTheSystemRegistry().getSystemProfileManager().getSystemProfiles();
		for (int idx = 0; idx < profiles.length; idx++) {
			ISystemProfile profile = profiles[idx];
			try {
				boolean ok = commitProfile(profile, timeout);
				if (!ok) {
					failed.add(profile);
				}
			} catch (Exception exc) {
				Logger logger = RSECorePlugin.getDefault().getLogger();
				String profileName = profile.getName();
				String message = "Error saving profile " + profileName; //$NON-NLS-1$
				logger.logError(message, exc);
				failed.add(profile);
			}
		}
		ISystemProfile[] result = new ISystemProfile[failed.size()];
		failed.toArray(result);
		return result;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.persistence.IRSEPersistenceManager#migrateProfile(org.eclipse.rse.core.model.ISystemProfile, org.eclipse.rse.persistence.IRSEPersistenceProvider)
	 */
	public void migrateProfile(ISystemProfile profile, IRSEPersistenceProvider persistenceProvider) {
		migrateProfile(profile, persistenceProvider, true);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.persistence.IRSEPersistenceManager#migrateProfile(org.eclipse.rse.core.model.ISystemProfile, org.eclipse.rse.persistence.IRSEPersistenceProvider, boolean)
	 */
	public IStatus migrateProfile(ISystemProfile profile, IRSEPersistenceProvider persistenceProvider, boolean delete) {
		IStatus result = Status.OK_STATUS;
		IRSEPersistenceProvider oldProvider = profile.getPersistenceProvider();
		oldProvider = (oldProvider == null) ? getDefaultPersistenceProvider() : oldProvider;
		IRSEPersistenceProvider newProvider = persistenceProvider;
		newProvider = (newProvider == null) ? getDefaultPersistenceProvider() : newProvider;
		if (oldProvider != newProvider) {
			String profileName = profile.getName();
			profile.setPersistenceProvider(newProvider);
			profile.commit();
			if (delete) {
				deleteProfile(oldProvider, profileName);
			} else {
				result = oldProvider.setMigrationMark(profileName, true);
			}
		}
		return result;
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
	public ISystemProfile[] restoreProfiles(long timeout) {
		String[] ids = getPersistenceProviderIds();
		List selectedRecords = new ArrayList(10);
		for (int i = 0; i < ids.length; i++) {
			String id = ids[i];
			ProviderRecord pr = getProviderRecord(id);
			if (pr.isAutostart()) {
				IRSEPersistenceProvider provider = getPersistenceProvider(id);
				if (provider != null) {
					pr.setRestored(false);
					selectedRecords.add(pr);
				}
			}
		}
		List profiles = new ArrayList(10);
		for (Iterator z = selectedRecords.iterator(); z.hasNext();) {
			ProviderRecord pr = (ProviderRecord) z.next();
			ISystemProfile[] providerProfiles = restoreProfiles(pr.provider, timeout);
			profiles.addAll(Arrays.asList(providerProfiles));
		}
		ISystemProfile[] result = new ISystemProfile[profiles.size()];
		profiles.toArray(result);
		return result;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.persistence.IRSEPersistenceManager#restoreProfiles(org.eclipse.rse.persistence.IRSEPersistenceProvider)
	 */
	public ISystemProfile[] restoreProfiles(IRSEPersistenceProvider provider, long timeout) {
		ProviderRecord pr = getProviderRecord(provider);
		pr.setRestored(false); // may already be false or true
		List profiles = loadProfiles(provider, timeout);
		pr.setRestored(true);
		ISystemProfile[] result = new ISystemProfile[profiles.size()];
		profiles.toArray(result);
		return result;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.persistence.IRSEPersistenceManager#isRestoreComplete(java.lang.String)
	 */
	public boolean isRestoreComplete() {
		boolean isComplete = true;
		String[] ids = getPersistenceProviderIds();
		for (int i = 0; i < ids.length && isComplete; i++) {
			String id = ids[i];
			ProviderRecord pr = getProviderRecord(id);
			if (pr.isAutostart()) {
				isComplete = isComplete && pr.isRestored();
			}
		}
		return isComplete;
	}

	private ProviderRecord getProviderRecord(String providerId) {
		ProviderRecord providerRecord = (ProviderRecord) knownProviders.get(providerId);
		if (providerRecord == null) {
			providerRecord = new ProviderRecord();
			knownProviders.put(providerId, providerRecord);
		}
		return providerRecord;
	}

	/**
	 * Returns the provider record for a given persistence provider.
	 * @param provider the provider for which to retrieve the provider record.
	 * @return the provider record.
	 */
	private ProviderRecord getProviderRecord(IRSEPersistenceProvider provider) {
		ProviderRecord pr = null;
		String id = (String) loadedProviders.get(provider);
		if (id != null) {
			pr = getProviderRecord(id);
		} else {
			pr = new ProviderRecord();
		}
		return pr;
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
					ProviderRecord pr = getProviderRecord(candidateId);
					pr.configurationElement = configurationElement;
					pr.providerId = candidateId;
				} else {
					logger.logError("Missing id attribute in persistenceProvider element", null); //$NON-NLS-1$
				}
			} else {
				logger.logError("Invalid element in persistenceProviders extension point", null); //$NON-NLS-1$
			}
		}
	}

	/**
	 * Loads a provider given a configuration element.
	 * After loading, the provider will be initialized with any
	 * properties found in the extension.
	 * @param configurationElement the element that contains the class and properties to load
	 * @return the provider
	 */
	private IRSEPersistenceProvider loadProvider(IConfigurationElement configurationElement) {
		IRSEPersistenceProvider provider = null;
		try {
			provider = (IRSEPersistenceProvider) configurationElement.createExecutableExtension("class"); //$NON-NLS-1$
			Properties properties = new Properties();
			IConfigurationElement[] children = configurationElement.getChildren("property"); //$NON-NLS-1$
			for (int i = 0; i < children.length; i++) {
				IConfigurationElement child = children[i];
				String name = child.getAttribute("name"); //$NON-NLS-1$
				String value = child.getAttribute("value"); //$NON-NLS-1$
				properties.put(name, value);
			}
			provider.setProperties(properties);
		} catch (CoreException e) {
			Logger logger = RSECorePlugin.getDefault().getLogger();
			logger.logError("Exception loading persistence provider", e); //$NON-NLS-1$
		}
		return provider;
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
		String providerId = getDefaultPersistenceProviderId();
		IRSEPersistenceProvider provider = getPersistenceProvider(providerId);
		return provider;
	}

	/**
	 * Retrieves the default persistence provider id from the preferences. This
	 * persistence provider identifier is specified in the
	 * org.eclipse.rse.core/DEFAULT_PERSISTENCE_PROVIDER preference and can be
	 * specified a product's plugin_customization.ini file.
	 *
	 * @return the specified default persistence provider, or the empty string
	 *         <code>""</code> if not set.
	 */
	private String getDefaultPersistenceProviderId() {
		IPreferencesService ps = Platform.getPreferencesService();
		String providerId = ps.getString(RSECorePlugin.PLUGIN_ID, IRSEPreferenceNames.DEFAULT_PERSISTENCE_PROVIDER, "", null); //$NON-NLS-1$
		return providerId;
	}

	/**
	 * Loads the profiles for a given persistence provider.
	 * @param persistenceProvider
	 * @return a list of profiles
	 */
	private List loadProfiles(IRSEPersistenceProvider persistenceProvider, long timeout) {
		List profiles = new ArrayList(10);
		String[] profileNames = persistenceProvider.getSavedProfileNames();
		for (int i = 0; i < profileNames.length; i++) {
			String profileName = profileNames[i];
			ISystemProfile profile = load(persistenceProvider, profileName, timeout);
			if (profile!=null) {
				profiles.add(profile);
			}
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
	private ISystemProfile load(IRSEPersistenceProvider provider, String profileName, long timeout) {
		ISystemProfile profile = null;
		if (mutex.waitForLock(null, timeout)) {
			try {
				RSEDOM dom = provider.loadRSEDOM(profileName, new NullProgressMonitor());
				if (dom != null) {
					profile = _importer.restoreProfile(dom);
					if (profile!=null) {
						profile.setPersistenceProvider(provider);
						cleanTree(profile);
					}
				}
			} finally {
				mutex.release();
			}
		}
		return profile;
	}

	/**
	 * Writes a profile to a DOM and schedules writing of that DOM to disk.
	 * May, in fact, update an existing DOM instead of creating a new one.
	 * If in the process of importing, skip writing.
	 * @return true if the profile is written to a DOM
	 */
	private boolean save(ISystemProfile profile, boolean force, long timeout) {
		boolean result = false;
		if (mutex.waitForLock(null, timeout)) {
			try {
				IRSEPersistenceProvider provider = profile.getPersistenceProvider();
				if (provider == null) {
					provider = getDefaultPersistenceProvider();
					profile.setPersistenceProvider(provider);
				}
				RSEDOM dom = _exporter.createRSEDOM(profile, force);
				cleanTree(profile);
				if (dom.needsSave()) {
					Job job = provider.getSaveJob(dom);
					if (job != null && canScheduleSave 
							&& timeout > 0) { // timeout of zero indicates shutdown - no time for job scheduling
						job.addJobChangeListener(jobChangeListener);	
						job.schedule(2000); // two second delay
					} else {
						provider.saveRSEDOM(dom, new NullProgressMonitor());
					}
				}
				result = true;
			} finally {
				mutex.release();
			}
		}
		return result;
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
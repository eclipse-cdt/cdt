/*******************************************************************************
 * Copyright (c) 2009, 2011 Andrew Gvozdev and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Gvozdev - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.language.settings.providers;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.language.settings.providers.ICListenerAgent;
import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsChangeEvent;
import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsChangeListener;
import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsEditableProvider;
import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsProvider;
import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsProvidersKeeper;
import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsManager;
import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsSerializableProvider;
import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsStorage;
import org.eclipse.cdt.core.language.settings.providers.ScannerDiscoveryLegacySupport;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.core.settings.model.ICStorageElement;
import org.eclipse.cdt.internal.core.XmlUtil;
import org.eclipse.cdt.internal.core.settings.model.CConfigurationSpecSettings;
import org.eclipse.cdt.internal.core.settings.model.IInternalCCfgInfo;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ILock;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Helper class handling serialization and notifications for language settings entries {@link ICLanguageSettingEntry}.
 */
public class LanguageSettingsProvidersSerializer {
	public static final String PROVIDER_EXTENSION_POINT_ID = LanguageSettingsExtensionManager.PROVIDER_EXTENSION_POINT_ID;
	public static final String ATTR_ID = LanguageSettingsExtensionManager.ATTR_ID;
	public static final String ATTR_NAME = LanguageSettingsExtensionManager.ATTR_NAME;
	public static final String ATTR_CLASS = LanguageSettingsExtensionManager.ATTR_CLASS;
	public static final String ELEM_PROVIDER = LanguageSettingsExtensionManager.ELEM_PROVIDER;
	public static final String ELEM_LANGUAGE_SCOPE = LanguageSettingsExtensionManager.ELEM_LANGUAGE_SCOPE;

	private static final String PREFERENCE_WORSPACE_PROVIDERS_SET = "language.settings.providers.workspace.prefs.toggle"; //$NON-NLS-1$
	private static final String CPROJECT_STORAGE_MODULE = "org.eclipse.cdt.core.LanguageSettingsProviders"; //$NON-NLS-1$
	private static final String STORAGE_WORKSPACE_LANGUAGE_SETTINGS = "language.settings.xml"; //$NON-NLS-1$
	private static final String STORAGE_PROJECT_PATH = ".settings/language.settings.xml"; //$NON-NLS-1$

	private static final String ELEM_PLUGIN = "plugin"; //$NON-NLS-1$
	private static final String ELEM_EXTENSION = "extension"; //$NON-NLS-1$
	private static final String ATTR_EXTENSION_POINT = "point"; //$NON-NLS-1$
	private static final String ELEM_PROJECT = "project"; //$NON-NLS-1$
	private static final String ELEM_CONFIGURATION = "configuration"; //$NON-NLS-1$
	private static final String ELEM_PROVIDER_REFERENCE = "provider-reference"; //$NON-NLS-1$
	private static final String ATTR_STORE_ENTRIES_WITH_PROJECT = "store-entries-with-project"; //$NON-NLS-1$

	// those are for readability of xml only
	private static final String ATTR_REF = "ref"; //$NON-NLS-1$
	private static final String VALUE_REF_SHARED_PROVIDER = "shared-provider"; //$NON-NLS-1$
	private static final String ATTR_COPY_OF = "copy-of"; //$NON-NLS-1$
	private static final String VALUE_COPY_OF_EXTENSION = "extension"; //$NON-NLS-1$

	/** Cache of true (raw) workspace providers */
	private static Map<String, ILanguageSettingsProvider> rawGlobalWorkspaceProviders = new HashMap<String, ILanguageSettingsProvider>();
	/** Cache of workspace providers wrappers */
	private static Map<String, ILanguageSettingsProvider> globalWorkspaceProviders = new HashMap<String, ILanguageSettingsProvider>();

	private static ListenerList fLanguageSettingsChangeListeners = new ListenerList(ListenerList.IDENTITY);
	private static ILock serializingLock = Job.getJobManager().newLock();

	/**
	 * Dummy class to represent ill-defined provider.
	 */
	private static class NotAccessibleProvider implements ILanguageSettingsProvider {
		private final String id;
		private NotAccessibleProvider(String providerId) {
			this.id = providerId;
		}
		@Override
		public List<ICLanguageSettingEntry> getSettingEntries(ICConfigurationDescription cfgDescription, IResource rc, String languageId) {
			return null;
		}
		@Override
		public String getName() {
			return null;
		}
		@Override
		public String getId() {
			return id;
		}
	}

	/**
	 * language settings provider listener-cfgDescription association
	 */
	private static class ListenerAssociation {
		private ICListenerAgent listener;
		private ICConfigurationDescription cfgDescription;

		public ListenerAssociation(ICListenerAgent la, ICConfigurationDescription cfgd) {
			listener = la;
			cfgDescription = cfgd;
		}
	}

	/**
	 * Wrapper for workspace providers to ensure level of indirection. That way workspace providers
	 * can be changed/replaced without notifying/changing the configurations which keep the providers
	 * in their lists.
	 */
	private static class LanguageSettingsWorkspaceProvider implements ILanguageSettingsProvider, ICListenerAgent {
		private String providerId;
		private int projectCount = 0;

		private LanguageSettingsWorkspaceProvider(String id) {
			Assert.isNotNull(id);
			providerId = id;
		}

		@Override
		public String getId() {
			return providerId;
		}

		@Override
		public String getName() {
			ILanguageSettingsProvider rawProvider = getRawProvider();
			String name = rawProvider!=null ? rawProvider.getName() : null;
			return name;
		}

		@Override
		public List<ICLanguageSettingEntry> getSettingEntries(ICConfigurationDescription cfgDescription, IResource rc, String languageId) {
			ILanguageSettingsProvider rawProvider = getRawProvider();
			List<ICLanguageSettingEntry> entries = rawProvider!=null ? rawProvider.getSettingEntries(cfgDescription, rc, languageId) : null;
			return entries;
		}

		/**
		 * Do not cache the "raw" provider as workspace provider can be changed at any time.
		 */
		private ILanguageSettingsProvider getRawProvider() {
			return LanguageSettingsProvidersSerializer.getRawWorkspaceProvider(providerId);
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof LanguageSettingsWorkspaceProvider) {
				LanguageSettingsWorkspaceProvider that = (LanguageSettingsWorkspaceProvider) obj;
				return providerId.equals(that.providerId);
			}
			return false;
		}

		/**
		 * Method toString() for debugging purposes.
		 */
		@SuppressWarnings("nls")
		@Override
		public String toString() {
			return "id="+getId()+", name="+getName();
		}

		/**
		 * We count number of times <b>workspace</b> provider (not the raw one!) associated
		 * with a <b>project</b>. If a project includes it multiple times via different configurations
		 * it still counts as 1.
		 */
		private int getProjectCount() {
			return projectCount;
		}

		private synchronized int incrementProjectCount() {
			projectCount++;
			return projectCount;
		}

		private synchronized int decrementProjectCount() {
			projectCount--;
			return projectCount;
		}

		@Override
		public void registerListener(ICConfigurationDescription cfgDescription) {
			// keep in mind that rawProvider can change externally
			ILanguageSettingsProvider rawProvider = getRawProvider();
			if (rawProvider instanceof ICListenerAgent) {
				((ICListenerAgent) rawProvider).registerListener(null);
			}
		}

		@Override
		public void unregisterListener() {
			// keep in mind that rawProvider can change externally
			ILanguageSettingsProvider rawProvider = getRawProvider();
			if (rawProvider instanceof ICListenerAgent) {
				((ICListenerAgent) rawProvider).unregisterListener();
			}
		}
	}

	/**
	 * Language Settings Change Event implementation.
	 */
	private static class LanguageSettingsChangeEvent implements ILanguageSettingsChangeEvent {
		private String projectName = null;
		private Map<String/*cfg*/, LanguageSettingsDelta> deltaMap = new HashMap<String, LanguageSettingsDelta>();

		/**
		 * The act of creating event resets internal delta count in configuration state.
		 * That implies that when the event is retrieved it must be fired or delta will go missing.
		 * That side effect is here to ensure atomic processing of firing & resetting the delta.
		 */
		public LanguageSettingsChangeEvent(ICProjectDescription prjDescription) {
			if (!prjDescription.isReadOnly()) {
				// The logic goes that we send notifications only for acting description but not for currently being prepared to set
				String msg = "Project description " + prjDescription.getName() + " is expected to be read-only"; //$NON-NLS-1$ //$NON-NLS-2$
				CCorePlugin.log(new Status(IStatus.WARNING, CCorePlugin.PLUGIN_ID, msg, new Exception(msg)));
			}

			projectName = prjDescription.getName();
			ICConfigurationDescription[] cfgDescriptions = prjDescription.getConfigurations();
			for (ICConfigurationDescription cfgDescription : cfgDescriptions) {
				if (cfgDescription instanceof IInternalCCfgInfo) {
					CConfigurationSpecSettings specSettings = null;
					try {
						specSettings = ((IInternalCCfgInfo) cfgDescription).getSpecSettings();
					} catch (CoreException e) {
						CCorePlugin.log(e);
					}
					if (specSettings != null) {
						String cfgId = cfgDescription.getId();
						if (ScannerDiscoveryLegacySupport.isLanguageSettingsProvidersFunctionalityEnabled(prjDescription.getProject())) {
							LanguageSettingsDelta delta = specSettings.dropDelta();
							if (delta != null) {
								deltaMap.put(cfgId, delta);
							}
						} else {
							deltaMap.remove(cfgId);
						}
					} else {
						IStatus ss = new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, "Internal error: Missing specSettings for " //$NON-NLS-1$
								+ cfgDescription.getClass().getSimpleName());
						CCorePlugin.log(new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, ss.getMessage(), new CoreException(ss)));
					}
				}
			}
		}

		@Override
		public String getProjectName() {
			return projectName;
		}

		@Override
		public String[] getConfigurationDescriptionIds() {
			return deltaMap.keySet().toArray(new String[deltaMap.size()]);
		}

		@SuppressWarnings("nls")
		@Override
		public String toString() {
			return "LanguageSettingsChangeEvent for project=[" + getProjectName() + "]"
					+ ", configurations=" + deltaMap.keySet();
		}
	}

	/** static initializer */
	static {
		try {
			loadLanguageSettingsWorkspace();
		} catch (Throwable e) {
			// log but swallow any exception
			CCorePlugin.log("Error loading workspace language settings providers", e); //$NON-NLS-1$
		}
	}

	/**
	 * Tells if language settings entries of the provider are persisted with the project
	 * (under .settings/ folder) or in workspace area. Persistence in the project area lets
	 * the entries migrate with the project.
	 *
	 * @param provider - provider to check the persistence mode.
	 * @return {@code true} if LSE persisted with the project or {@code false} if in the workspace.
	 */
	public static boolean isStoringEntriesInProjectArea(LanguageSettingsSerializableProvider provider) {
		return provider.getPropertyBool(ATTR_STORE_ENTRIES_WITH_PROJECT);
	}

	/**
	 * Define where language settings are persisted for the provider.
	 *
	 * @param provider - provider to set the persistence mode.
	 * @param storeEntriesWithProject - {@code true} if with the project,
	 *    {@code false} if in workspace area.
	 */
	public static void setStoringEntriesInProjectArea(LanguageSettingsSerializableProvider provider, boolean storeEntriesWithProject) {
		provider.setPropertyBool(ATTR_STORE_ENTRIES_WITH_PROJECT, storeEntriesWithProject);
	}

	/**
	 * Determine location of the project store of language settings providers in the plug-in state area.
	 *
	 * @param store - name of the store.
	 * @return location of the store in the plug-in state area.
	 */
	private static IFile getStoreInProjectArea(IProject project) {
		return project.getFile(STORAGE_PROJECT_PATH);
	}

	/**
	 * Determine location of the store in the plug-in state area.
	 *
	 * @param store - name of the store.
	 * @return location of the store in the plug-in state area.
	 */
	private static URI getStoreInWorkspaceArea(String store) {
		IPath location = CCorePlugin.getDefault().getStateLocation().append(store);
		return URIUtil.toURI(location);
	}

	/**
	 * Set and store user defined providers in workspace area.
	 *
	 * @param providers - array of user defined providers
	 * @throws CoreException in case of problems
	 */
	public static void setWorkspaceProviders(List<ILanguageSettingsProvider> providers) throws CoreException {
		setWorkspaceProvidersInternal(providers);
		serializeLanguageSettingsWorkspace();
		// generate preference change event for preference change listeners (value is not intended to be used)
		IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(CCorePlugin.PLUGIN_ID);
		prefs.putBoolean(PREFERENCE_WORSPACE_PROVIDERS_SET, ! prefs.getBoolean(PREFERENCE_WORSPACE_PROVIDERS_SET, false));
	}

	/**
	 * Internal method to set user defined providers in memory.
	 *
	 * @param providers - list of user defined providers. If {@code null}
	 *    is passed user defined providers are cleared.
	 */
	private static void setWorkspaceProvidersInternal(List<ILanguageSettingsProvider> providers) {
		Map<String, ILanguageSettingsProvider> rawNewProviders = new HashMap<String, ILanguageSettingsProvider>();

		// add given providers
		if (providers != null) {
			for (ILanguageSettingsProvider provider : providers) {
				if (isWorkspaceProvider(provider)) {
					provider = rawGlobalWorkspaceProviders.get(provider.getId());
				}
				if (provider != null) {
					rawNewProviders.put(provider.getId(), provider);
				}
			}
		}

		// fill the rest from extension registry
		// this list is independent from the internal list of extensions in LanguageSettingsExtensionManager
		for (String id : LanguageSettingsExtensionManager.getExtensionProviderIds()) {
			if (!rawNewProviders.containsKey(id)) {
				ILanguageSettingsProvider provider = LanguageSettingsExtensionManager.getExtensionProviderCopy(id, true);
				if (provider == null) {
					provider = LanguageSettingsExtensionManager.loadProvider(id);
				}
				if (provider != null) {
					rawNewProviders.put(provider.getId(), provider);
				}
			}
		}

		// register listeners
		List<ICListenerAgent> oldListeners = selectListeners(rawGlobalWorkspaceProviders.values());
		List<ICListenerAgent> newListeners = selectListeners(rawNewProviders.values());

		for (ICListenerAgent oldListener : oldListeners) {
			if (!isInList(newListeners, oldListener)) {
				LanguageSettingsWorkspaceProvider wspProvider = (LanguageSettingsWorkspaceProvider) globalWorkspaceProviders.get(((ILanguageSettingsProvider)oldListener).getId());
				if (wspProvider != null && wspProvider.getProjectCount() > 0) {
					oldListener.unregisterListener();
				}
			}
		}

		for (ICListenerAgent newListener : newListeners) {
			if (!isInList(oldListeners, newListener)) {
				LanguageSettingsWorkspaceProvider wspProvider = (LanguageSettingsWorkspaceProvider) globalWorkspaceProviders.get(((ILanguageSettingsProvider)newListener).getId());
				if (wspProvider != null && wspProvider.getProjectCount() > 0) {
					newListener.registerListener(null);
				}
			}
		}

		rawGlobalWorkspaceProviders = rawNewProviders;
	}

	/**
	 * Create event for language settings changes of workspace providers in a project.
	 */
	private static LanguageSettingsChangeEvent createEvent(ICProjectDescription prjDescription, List<String> providerIds) {
		ICConfigurationDescription[] cfgDescriptions = prjDescription.getConfigurations();
		for (ICConfigurationDescription cfgDescription : cfgDescriptions) {
			if (cfgDescription instanceof ILanguageSettingsProvidersKeeper) {
				for (ILanguageSettingsProvider provider : ((ILanguageSettingsProvidersKeeper) cfgDescription).getLanguageSettingProviders()) {
					if (isWorkspaceProvider(provider) && providerIds.contains(provider.getId())) {
						LanguageSettingsChangeEvent event = new LanguageSettingsChangeEvent(prjDescription);
						if (event.getConfigurationDescriptionIds().length > 0) {
							return event;
						}
						return null;
					}
				}
			}
		}

		return null;
	}

	/**
	 * Compute events for language settings changes in workspace.
	 */
	private static List<LanguageSettingsChangeEvent> createLanguageSettingsChangeEvents(List<LanguageSettingsSerializableProvider> providers) {
		List<LanguageSettingsChangeEvent> events = new ArrayList<LanguageSettingsChangeEvent>();

		List<String> providerIds = new ArrayList<String>();
		for (LanguageSettingsSerializableProvider provider : providers) {
			providerIds.add(provider.getId());
		}

		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject[] projects = root.getProjects();

		for (IProject project : projects) {
			if (project.isAccessible()) {
				ICProjectDescription prjDescription = CCorePlugin.getDefault().getProjectDescription(project, false);
				if (prjDescription != null) {
					try {
						LanguageSettingsChangeEvent event = createEvent(prjDescription, providerIds);
						if (event != null) {
							events.add(event);
						}
					} catch (Throwable e) {
						// log and swallow any exception
						CCorePlugin.log("Error creating event about changes in workspace language settings providers, " //$NON-NLS-1$
								+ "project=" + project.getName(), e); //$NON-NLS-1$
					}
				}
			}

		}

		return events;
	}

	/**
	 * Save language settings providers of the workspace (global providers) to persistent storage.
	 * @throws CoreException
	 */
	public static void serializeLanguageSettingsWorkspace() throws CoreException {
		URI uriStoreWsp = getStoreInWorkspaceArea(STORAGE_WORKSPACE_LANGUAGE_SETTINGS);
		List<LanguageSettingsSerializableProvider> serializableWorkspaceProviders = new ArrayList<LanguageSettingsSerializableProvider>();
		for (ILanguageSettingsProvider provider : rawGlobalWorkspaceProviders.values()) {
			if (provider instanceof LanguageSettingsSerializableProvider) {
				if (!LanguageSettingsManager.isEqualExtensionProvider(provider, true)) {
					serializableWorkspaceProviders.add((LanguageSettingsSerializableProvider)provider);
				}
			}
		}
		try {
			List<LanguageSettingsChangeEvent> events = null;
			if (serializableWorkspaceProviders.isEmpty()) {
				java.io.File fileStoreWsp = new java.io.File(uriStoreWsp);
				try {
					serializingLock.acquire();
					fileStoreWsp.delete();
					// manufacture events while inside the lock
					events = createLanguageSettingsChangeEvents(serializableWorkspaceProviders);
				} finally {
					serializingLock.release();
				}
			} else {
				Document doc = XmlUtil.newDocument();
				Element rootElement = XmlUtil.appendElement(doc, ELEM_PLUGIN);
				Element elementExtension = XmlUtil.appendElement(rootElement, ELEM_EXTENSION,
						new String[] {ATTR_EXTENSION_POINT, PROVIDER_EXTENSION_POINT_ID});

				for (LanguageSettingsSerializableProvider provider : serializableWorkspaceProviders) {
					provider.serialize(elementExtension);
				}

				try {
					serializingLock.acquire();
					XmlUtil.serializeXml(doc, uriStoreWsp);
					// manufacture events while inside the lock
					events = createLanguageSettingsChangeEvents(serializableWorkspaceProviders);
				} finally {
					serializingLock.release();
				}
			}
			// notify the listeners outside the lock
			for (LanguageSettingsChangeEvent event : events) {
				notifyLanguageSettingsChangeListeners(event);
			}

		} catch (Exception e) {
			String msg = "Internal error while trying to serialize language settings"; //$NON-NLS-1$
			CCorePlugin.log(msg, e);
			throw new CoreException(new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, msg, e));
		}
	}

	/**
	 * Load language settings for workspace.
	 */
	public static void loadLanguageSettingsWorkspace() {
		List <ILanguageSettingsProvider> providers = null;

		URI uriStoreWsp = getStoreInWorkspaceArea(STORAGE_WORKSPACE_LANGUAGE_SETTINGS);

		Document doc = null;
		try {
			serializingLock.acquire();
			doc = XmlUtil.loadXml(uriStoreWsp);
		} catch (Exception e) {
			CCorePlugin.log("Can't load preferences from file " + uriStoreWsp, e); //$NON-NLS-1$
		} finally {
			serializingLock.release();
		}

		if (doc != null) {
			Element rootElement = doc.getDocumentElement();
			NodeList providerNodes = rootElement.getElementsByTagName(ELEM_PROVIDER);

			List<String> userDefinedProvidersIds = new ArrayList<String>(providerNodes.getLength());
			providers = new ArrayList<ILanguageSettingsProvider>(providerNodes.getLength());

			for (int i = 0; i < providerNodes.getLength(); i++) {
				Node providerNode = providerNodes.item(i);
				final String providerId = XmlUtil.determineAttributeValue(providerNode, ATTR_ID);
				if (userDefinedProvidersIds.contains(providerId)) {
					String msg = "Ignored an attempt to persist duplicate language settings provider, id=" + providerId; //$NON-NLS-1$
					CCorePlugin.log(new Status(IStatus.WARNING, CCorePlugin.PLUGIN_ID, msg, new Exception()));
					continue;
				}
				userDefinedProvidersIds.add(providerId);

				ILanguageSettingsProvider provider = null;
				try {
					provider = loadProvider(providerNode);
				} catch (Exception e) {
					CCorePlugin.log("Error initializing workspace language settings providers", e); //$NON-NLS-1$
				}
				if (provider == null) {
					provider = new NotAccessibleProvider(providerId);
				}
				providers.add(provider);
			}
		}
		setWorkspaceProvidersInternal(providers);
	}

	/**
	 * @noreference This method is not intended to be referenced by clients.
	 * It is public solely for benefit of JUnit testing.
	 */
	public static void serializeLanguageSettingsInternal(Element projectElementPrjStore, Element projectElementWspStore, ICProjectDescription prjDescription) {
		ICConfigurationDescription[] cfgDescriptions = prjDescription.getConfigurations();
		for (ICConfigurationDescription cfgDescription : cfgDescriptions) {
			if (!(cfgDescription instanceof ILanguageSettingsProvidersKeeper))
				continue;

			// no lazy initialization as we may need to save 0 providers when it is different from default
			Element elementConfiguration = XmlUtil.appendElement(projectElementPrjStore, ELEM_CONFIGURATION, new String[] {
					ATTR_ID, cfgDescription.getId(),
					ATTR_NAME, cfgDescription.getName(),
				});
			Element elementConfigurationWsp = null;

			List<ILanguageSettingsProvider> providers = ((ILanguageSettingsProvidersKeeper) cfgDescription).getLanguageSettingProviders();
			if (providers.size() > 0) {
				Element elementExtension = null;
				Element elementExtensionWsp = null;

				for (ILanguageSettingsProvider provider : providers) {
					if (isWorkspaceProvider(provider)) {
						if (elementExtension == null) {
							elementExtension = XmlUtil.appendElement(elementConfiguration, ELEM_EXTENSION, new String[] {
									ATTR_EXTENSION_POINT, PROVIDER_EXTENSION_POINT_ID});
						}
						// Element elementProviderReference =
						XmlUtil.appendElement(elementExtension, ELEM_PROVIDER_REFERENCE, new String[] {
								ATTR_ID, provider.getId(),
								ATTR_REF, VALUE_REF_SHARED_PROVIDER,
							});
						continue;
					}
					if (!(provider instanceof LanguageSettingsSerializableProvider)) {
						if (elementExtension == null) {
							elementExtension = XmlUtil.appendElement(elementConfiguration, ELEM_EXTENSION, new String[] {
									ATTR_EXTENSION_POINT, PROVIDER_EXTENSION_POINT_ID});
						}
						// Element elementProvider =
						XmlUtil.appendElement(elementExtension, ELEM_PROVIDER, new String[] {
								ATTR_ID, provider.getId(),
								ATTR_NAME, provider.getName(),
								ATTR_CLASS, provider.getClass().getCanonicalName(),
							});
					} else if (LanguageSettingsManager.isEqualExtensionProvider(provider, true)) {
						if (elementExtension == null) {
							elementExtension = XmlUtil.appendElement(elementConfiguration, ELEM_EXTENSION, new String[] {
									ATTR_EXTENSION_POINT, PROVIDER_EXTENSION_POINT_ID});
						}
						// Element elementProvider =
						XmlUtil.appendElement(elementExtension, ELEM_PROVIDER, new String[] {
								ATTR_ID, provider.getId(),
								ATTR_COPY_OF, VALUE_COPY_OF_EXTENSION,
							});
					} else {
						try {
							LanguageSettingsSerializableProvider lss = (LanguageSettingsSerializableProvider) provider;

							boolean isWspStorageAvailable = (projectElementWspStore != null) && (projectElementPrjStore != projectElementWspStore);
							if (isStoringEntriesInProjectArea(lss) || !isWspStorageAvailable) {
								if (elementExtension == null) {
									elementExtension = XmlUtil.appendElement(elementConfiguration, ELEM_EXTENSION, new String[] {
											ATTR_EXTENSION_POINT, PROVIDER_EXTENSION_POINT_ID});
								}
								lss.serialize(elementExtension);
							} else {
								if (elementExtension == null) {
									elementExtension = XmlUtil.appendElement(elementConfiguration, ELEM_EXTENSION, new String[] {
											ATTR_EXTENSION_POINT, PROVIDER_EXTENSION_POINT_ID});
								}
								if (elementExtensionWsp == null) {
									if (elementConfigurationWsp == null) {
										elementConfigurationWsp = XmlUtil.appendElement(projectElementWspStore, ELEM_CONFIGURATION, new String[] {
												ATTR_ID, cfgDescription.getId(),
												ATTR_NAME, cfgDescription.getName(),
										});
									}
									elementExtensionWsp = XmlUtil.appendElement(elementConfigurationWsp, ELEM_EXTENSION, new String[] {
											ATTR_EXTENSION_POINT, PROVIDER_EXTENSION_POINT_ID});
								}
								Element elementProviderWsp = XmlUtil.appendElement(elementExtensionWsp, ELEM_PROVIDER, new String[] {
										ATTR_ID, provider.getId() }); // no attributes kept in workspace storage

								// split storage
								lss.serializeAttributes(elementExtension);
								lss.serializeEntries(elementProviderWsp);
							}
						} catch (Throwable e) {
							// protect from any exceptions from implementers
							CCorePlugin.log("Exception trying serialize provider "+provider.getId(), e); //$NON-NLS-1$
						}
					}
				}
			}
		}
	}

	/**
	 * Check if all the language settings providers in the project match defaults.
	 */
	private static boolean isEqualToDefaultProviders(ICProjectDescription prjDescription) {
		ICConfigurationDescription[] cfgDescriptions = prjDescription.getConfigurations();
		for (ICConfigurationDescription cfgDescription : cfgDescriptions) {
			if (!(cfgDescription instanceof ILanguageSettingsProvidersKeeper)) {
				continue;
			}

			String[] defaultIds = ((ILanguageSettingsProvidersKeeper) cfgDescription).getDefaultLanguageSettingsProvidersIds();
			if (defaultIds == null) {
				defaultIds = new String[0];
			}

			// check size
			List<ILanguageSettingsProvider> providers = ((ILanguageSettingsProvidersKeeper) cfgDescription).getLanguageSettingProviders();
			if (providers.size() != defaultIds.length) {
				return false;
			}

			// check ids
			for (int i = 0; i < defaultIds.length; i++) {
				ILanguageSettingsProvider provider = providers.get(i);
				if (!provider.getId().equals(defaultIds[i])) {
					return false;
				}
			}

			// check equality (most expensive, so check last)
			for (ILanguageSettingsProvider provider : providers) {
				if (LanguageSettingsManager.isPreferShared(provider.getId())) {
					if (!isWorkspaceProvider(provider)) {
						return false;
					}
				} else {
					if (!LanguageSettingsManager.isEqualExtensionProvider(provider, true)) {
						return false;
					}
				}
			}
		}

		return true;
	}

	/**
	 * Save language settings providers of a project to persistent storage.
	 *
	 * @param prjDescription - project description of the project.
	 * @throws CoreException if something goes wrong.
	 */
	public static void serializeLanguageSettings(ICProjectDescription prjDescription) throws CoreException {
		IProject project = prjDescription.getProject();
		try {
			// Using side effect of adding the module to the storage
			prjDescription.getStorage(CPROJECT_STORAGE_MODULE, true);
		} catch (CoreException e) {
			CCorePlugin.log("Internal error while trying to serialize language settings", e); //$NON-NLS-1$
		}

		try {
			// The storage could be split in two, one for provider properties, another one for entries,
			// depending on provider flag

			// Document to store in project area
			Document docStorePrj = XmlUtil.newDocument();
			Element projectElementStorePrj = XmlUtil.appendElement(docStorePrj, ELEM_PROJECT);
			// Document to store in workspace area
			Document docStoreWsp = XmlUtil.newDocument();
			Element projectElementStoreWsp = XmlUtil.appendElement(docStoreWsp, ELEM_PROJECT);

			URI uriStoreWsp = getStoreInWorkspaceArea(project.getName()+'.'+STORAGE_WORKSPACE_LANGUAGE_SETTINGS);
			LanguageSettingsChangeEvent event = null;

			try {
				// Note that need for serialization may exist even if LS *entries* event delta is empty,
				// as set of providers or their properties may differ

				serializingLock.acquire();

				if (!isEqualToDefaultProviders(prjDescription)) {
					serializeLanguageSettingsInternal(projectElementStorePrj, projectElementStoreWsp, prjDescription);
				}

				// Absent store means default providers as specified in the toolchain
				IFile fileStorePrj = getStoreInProjectArea(project);
				boolean isProjectStoreEmpty = projectElementStorePrj.getChildNodes().getLength() == 0;
				if (isProjectStoreEmpty) {
					if (fileStorePrj.exists()) {
						fileStorePrj.delete(true, null);
					}
				} else {
					IContainer folder = fileStorePrj.getParent();
					if (folder instanceof IFolder && !folder.exists()) {
						((IFolder) folder).create(true, true, null);
					}
					XmlUtil.serializeXml(docStorePrj, fileStorePrj);
				}

				// project-specific location in workspace area
				boolean isWorkspaceStoreEmpty = projectElementStoreWsp.getChildNodes().getLength() == 0;
				if (isWorkspaceStoreEmpty) {
					new java.io.File(uriStoreWsp).delete();
				} else {
					XmlUtil.serializeXml(docStoreWsp, uriStoreWsp);
				}

				// manufacture the event only if serialization was successful
				event = new LanguageSettingsChangeEvent(prjDescription);
			} finally {
				serializingLock.release();
			}
			// notify the listeners outside the lock
			if (event.getConfigurationDescriptionIds().length > 0) {
				notifyLanguageSettingsChangeListeners(event);
			}

		} catch (Exception e) {
			String msg = "Internal error while trying to serialize language settings"; //$NON-NLS-1$
			CCorePlugin.log(msg, e);
			throw new CoreException(new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, msg, e));
		}
	}

	/**
	 * Load language settings to the project description from XML.
	 *
	 * @noreference This method is not intended to be referenced by clients.
	 * It is public solely for benefit of JUnit testing.
	 */
	public static void loadLanguageSettingsInternal(Element projectElementPrj, Element projectElementWsp, ICProjectDescription prjDescription) {
		/*
		<project>
			<configuration id="cfg.id">
				<extension point="org.eclipse.cdt.core.LanguageSettingsProvider">
					<provider .../>
					<provider-reference id="..."/>
				</extension>
			</configuration>
		</project>
		 */
		NodeList configurationNodes = projectElementPrj.getChildNodes();
		for (int ic = 0; ic < configurationNodes.getLength(); ic++) {
			Node cfgNode = configurationNodes.item(ic);
			if (!isElementWithName(cfgNode, ELEM_CONFIGURATION)) {
				continue;
			}

			List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
			String cfgId = XmlUtil.determineAttributeValue(cfgNode, ATTR_ID);

			NodeList extensionNodes = cfgNode.getChildNodes();
			for (int ie = 0; ie < extensionNodes.getLength(); ie++) {
				Node extNode = extensionNodes.item(ie);
				if (!isElementWithName(extNode, ELEM_EXTENSION)) {
					continue;
				}

				NodeList providerNodes = extNode.getChildNodes();
				for (int ip = 0; ip < providerNodes.getLength(); ip++) {
					ILanguageSettingsProvider provider = null;

					Node providerNode = providerNodes.item(ip);
					if (isElementWithName(providerNode, ELEM_PROVIDER_REFERENCE)) {
						String providerId = XmlUtil.determineAttributeValue(providerNode, ATTR_ID);
						provider = getWorkspaceProvider(providerId);
					} else if (isElementWithName(providerNode, ELEM_PROVIDER)) {
						String providerClass = XmlUtil.determineAttributeValue(providerNode, ATTR_CLASS);
						if (providerClass == null || providerClass.isEmpty()) {
							// provider is copied from extension if "class" is not supplied
							String providerId = XmlUtil.determineAttributeValue(providerNode, ATTR_ID);
							provider = LanguageSettingsManager.getExtensionProviderCopy(providerId, true);

							if (provider == null) {
								String msg = "Internal Error trying to retrieve copy of extension provider id=" + providerId; //$NON-NLS-1$
								CCorePlugin.log(new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, msg, new Exception(msg)));

								provider = LanguageSettingsManager.getWorkspaceProvider(providerId);
							}
						} else {
							try {
								provider = loadProvider(providerNode);
							} catch (CoreException e) {
								@SuppressWarnings("nls")
								String msg = "Error loading provider class=[" + providerClass + "] "
										+ "in project=" + prjDescription.getProject().getName() + ", cfg=[" + cfgId + "]";
								CCorePlugin.log(msg, e);
							}
							if (provider instanceof LanguageSettingsSerializableProvider) {
								LanguageSettingsSerializableProvider lss = (LanguageSettingsSerializableProvider) provider;
								if (!isStoringEntriesInProjectArea(lss) && projectElementWsp != null) {
									loadProviderEntries(lss, cfgId, projectElementWsp);
								}
							}
						}
					}
					if (provider != null) {
						providers.add(provider);
					}
				}
			}

			ICConfigurationDescription cfgDescription = prjDescription.getConfigurationById(cfgId);
			setProvidersWithoutNotification(cfgDescription, providers);
		}
	}

	/**
	 * Set providers into configuration description avoiding triggering an event.
	 */
	private static void setProvidersWithoutNotification(ICConfigurationDescription cfgDescription, List<ILanguageSettingsProvider> providers) {
		if (cfgDescription instanceof ILanguageSettingsProvidersKeeper) {
			((ILanguageSettingsProvidersKeeper) cfgDescription).setLanguageSettingProviders(providers);
			if (cfgDescription instanceof IInternalCCfgInfo) {
				try {
					// swallow delta
					((IInternalCCfgInfo) cfgDescription).getSpecSettings().dropDelta();
				} catch (CoreException e) {
					CCorePlugin.log(e);
				}
			}
		}
	}

	/**
	 * Check if the node is {@code Element} and that the name matches.
	 */
	private static boolean isElementWithName(Node cfgNode, String name) {
		return cfgNode instanceof Element && cfgNode.getNodeName().equals(name);
	}

	/**
	 * Load provider entries for the given configuration from XML Element.
	 */
	private static void loadProviderEntries(LanguageSettingsSerializableProvider provider,
			String cfgId, Element projectElement) {
		/*
		<project>
			<configuration id="cfg.id">
				<extension point="org.eclipse.cdt.core.LanguageSettingsProvider">
					<provider .../>
					<provider-reference id="..."/>
				</extension>
			</configuration>
		</project>
		 */
		NodeList configurationNodes = projectElement.getChildNodes();
		for (int ic = 0; ic < configurationNodes.getLength(); ic++) {
			Node cfgNode = configurationNodes.item(ic);
			if (!isElementWithName(cfgNode, ELEM_CONFIGURATION)) {
				continue;
			}
			String cfgIdXml = XmlUtil.determineAttributeValue(cfgNode, ATTR_ID);
			if (!cfgId.equals(cfgIdXml)) {
				continue;
			}

			NodeList extensionNodes = cfgNode.getChildNodes();
			for (int ie = 0; ie < extensionNodes.getLength(); ie++) {
				Node extNode = extensionNodes.item(ie);
				if (!isElementWithName(extNode, ELEM_EXTENSION)) {
					continue;
				}

				NodeList providerNodes = extNode.getChildNodes();
				for (int ip = 0; ip < providerNodes.getLength(); ip++) {
					Node providerNode = providerNodes.item(ip);
					if (!isElementWithName(providerNode, ELEM_PROVIDER)) {
						continue;
					}

					String id = XmlUtil.determineAttributeValue(providerNode, ATTR_ID);
					if (provider.getId().equals(id)) {
						provider.loadEntries((Element) providerNode);
						return;
					}
				}
			}
		}
	}

	/**
	 * Load provider from provider node.
	 */
	private static ILanguageSettingsProvider loadProvider(Node providerNode) throws CoreException {
		String attrClass = XmlUtil.determineAttributeValue(providerNode, ATTR_CLASS);
		ILanguageSettingsProvider provider = LanguageSettingsExtensionManager.instantiateProviderClass(attrClass);

		if (provider instanceof LanguageSettingsSerializableProvider) {
			((LanguageSettingsSerializableProvider)provider).load((Element) providerNode);
		}
		return provider;
	}

	/**
	 * Load language settings from workspace and project storages for the given project description.
	 * @param prjDescription - project description to load language settings.
	 */
	public static void loadLanguageSettings(ICProjectDescription prjDescription) {
		IProject project = prjDescription.getProject();
		IFile storeInPrjArea = getStoreInProjectArea(project);
		if (storeInPrjArea.exists()) {
			Document doc = null;
			try {
				doc = XmlUtil.loadXml(storeInPrjArea);
				Element rootElementPrj = doc.getDocumentElement(); // <project>

				URI uriStoreWsp = getStoreInWorkspaceArea(project.getName()+'.'+STORAGE_WORKSPACE_LANGUAGE_SETTINGS);
				Document docWsp = null;
				try {
					serializingLock.acquire();
					docWsp = XmlUtil.loadXml(uriStoreWsp);
				} finally {
					serializingLock.release();
				}

				Element rootElementWsp = null; // <project>
				if (docWsp != null) {
					rootElementWsp = docWsp.getDocumentElement();
				}

				loadLanguageSettingsInternal(rootElementPrj, rootElementWsp, prjDescription);
			} catch (Exception e) {
				CCorePlugin.log("Can't load preferences from file " + storeInPrjArea.getLocation(), e); //$NON-NLS-1$
			}

		} else {
			// Storage in project area does not exist
			ICStorageElement storageElement = null;
			try {
				storageElement = prjDescription.getStorage(CPROJECT_STORAGE_MODULE, false);
			} catch (CoreException e) {
				String msg = "Internal error while trying to load language settings"; //$NON-NLS-1$
				CCorePlugin.log(msg, e);
			}

			if (storageElement != null) {
				// set default providers defined in the tool-chain
				for (ICConfigurationDescription cfgDescription : prjDescription.getConfigurations()) {
					String[] ids = ((ILanguageSettingsProvidersKeeper) cfgDescription).getDefaultLanguageSettingsProvidersIds();
					if (ids != null) {
						List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>(ids.length);
						for (String id : ids) {
							if (LanguageSettingsExtensionManager.isPreferShared(id)) {
								providers.add(LanguageSettingsManager.getWorkspaceProvider(id));
							} else {
								providers.add(LanguageSettingsManager.getExtensionProviderCopy(id, true));
							}

						}
						((ILanguageSettingsProvidersKeeper) cfgDescription).setLanguageSettingProviders(providers);
					}
				}

			} else {
				// Older existing legacy projects unaware of Language Settings Providers and their persistence store
				ICConfigurationDescription[] cfgDescriptions = prjDescription.getConfigurations();
				for (ICConfigurationDescription cfgDescription : cfgDescriptions) {
					if (cfgDescription instanceof ILanguageSettingsProvidersKeeper) {
						((ILanguageSettingsProvidersKeeper) cfgDescription).setLanguageSettingProviders(ScannerDiscoveryLegacySupport.getDefaultProvidersLegacy());
					}
				}
			}

		}
	}

	/**
	 * Get Language Settings Provider from the list of workspace providers,
	 * see {@link #getWorkspaceProviders()}.
	 *
	 * @param id - ID of provider to find.
	 * @return the workspace provider. If provider is not defined - still workspace
	 *   provider wrapper is created and returned.
	 */
	public static ILanguageSettingsProvider getWorkspaceProvider(String id) {
		ILanguageSettingsProvider provider = globalWorkspaceProviders.get(id);
		if (provider == null) {
			provider = new LanguageSettingsWorkspaceProvider(id);
			globalWorkspaceProviders.put(id, provider);
		}
		return provider;
	}

	/**
	 * Helper method to get to real underlying provider collecting entries as opposed
	 * to wrapper which is normally used for workspace provider.
	 * @see #isWorkspaceProvider(ILanguageSettingsProvider)
	 *
	 * @param id - ID of the provider.
	 * @return raw underlying provider.
	 */
	public static ILanguageSettingsProvider getRawWorkspaceProvider(String id) {
		return rawGlobalWorkspaceProviders.get(id);
	}

	/**
	 * Get Language Settings Providers defined in the workspace. That includes
	 * user-defined providers and after that providers defined as extensions via
	 * {@code org.eclipse.cdt.core.LanguageSettingsProvider} extension point.
	 * Note that this returns wrappers around workspace provider so underlying
	 * provider could be replaced internally without need to change configuration.
	 * See also {@link #getRawWorkspaceProvider(String)}.
	 *
	 * @return list of workspace providers.
	 */
	public static List<ILanguageSettingsProvider> getWorkspaceProviders() {
		ArrayList<ILanguageSettingsProvider> workspaceProviders = new ArrayList<ILanguageSettingsProvider>();
		for (ILanguageSettingsProvider rawProvider : rawGlobalWorkspaceProviders.values()) {
			workspaceProviders.add(getWorkspaceProvider(rawProvider.getId()));
		}
		return workspaceProviders;
	}

	/**
	 * Checks if the provider is a workspace level provider.
	 * This method is intended to check providers retrieved from a configuration.
	 * Raw providers from {@link #getRawWorkspaceProvider(String)}
	 * are not considered as workspace providers.
	 *
	 * @param provider - provider to check.
	 * @return {@code true} if the given provider is workspace provider, {@code false} otherwise.
	 */
	public static boolean isWorkspaceProvider(ILanguageSettingsProvider provider) {
		return provider instanceof LanguageSettingsWorkspaceProvider;
	}

	/**
	 * Check that this particular element is in the list.
	 */
	private static <T> boolean isInList(Collection<T> list, T element) {
		// list.contains(element) won't do it as we are interested in exact object, not in equal object
		for (T elem : list) {
			if (elem == element)
				return true;
		}
		return false;
	}

	/**
	 * Check that this particular element is in the association list.
	 */
	private static boolean isListenerInTheListOfAssociations(Collection<ListenerAssociation> list, ICListenerAgent element) {
		for (ListenerAssociation la : list) {
			// we are interested in exact object, not in equal object
			if (la.listener == element)
				return true;
		}
		return false;
	}

	/**
	 * Get a providers list including only providers of type {@link ICListenerAgent}
	 * for a given project description - collecting from all configurations.
	 */
	private static List<ICListenerAgent> getListeners(ICProjectDescription prjDescription) {
		List<ICListenerAgent> listeners = new ArrayList<ICListenerAgent>();
		if (prjDescription != null) {
			for (ICConfigurationDescription cfgDescription : prjDescription.getConfigurations()) {
				if (cfgDescription instanceof ILanguageSettingsProvidersKeeper) {
					List<ILanguageSettingsProvider> providers = ((ILanguageSettingsProvidersKeeper) cfgDescription).getLanguageSettingProviders();
					for (ILanguageSettingsProvider provider : providers) {
						if (provider instanceof ICListenerAgent) {
							ICListenerAgent listener = (ICListenerAgent) provider;
							if (!isInList(listeners, listener)) {
								listeners.add(listener);
							}
						}
					}
				}
			}
		}
		return listeners;
	}

	/**
	 * Pick from the list providers which are listeners, i.e. instances of type {@link ICListenerAgent}.
	 */
	private static List<ICListenerAgent> selectListeners(Collection<ILanguageSettingsProvider> values) {
		List<ICListenerAgent> listeners = new ArrayList<ICListenerAgent>();
		for (ILanguageSettingsProvider provider : values) {
			if (provider instanceof ICListenerAgent)
				listeners.add((ICListenerAgent) provider);
		}
		return listeners;
	}

	/**
	 * Get a providers list including only providers of type {@link ICListenerAgent}
	 * for a given project description - collecting from all configurations.
	 */
	private static List<ListenerAssociation> getListenersAssociations(ICProjectDescription prjDescription) {
		List<ListenerAssociation> associations = new ArrayList<ListenerAssociation>();
		if (prjDescription != null) {
			for (ICConfigurationDescription cfgDescription : prjDescription.getConfigurations()) {
				if (cfgDescription instanceof ILanguageSettingsProvidersKeeper) {
					List<ILanguageSettingsProvider> providers = ((ILanguageSettingsProvidersKeeper) cfgDescription).getLanguageSettingProviders();
					List<ICListenerAgent> listeners = selectListeners(providers);
					for (ICListenerAgent listener : listeners) {
						if (!isListenerInTheListOfAssociations(associations, listener)) {
							associations.add(new ListenerAssociation(listener, cfgDescription));
						}
					}
				}
			}
		}
		return associations;
	}

	/**
	 * Unregister listeners which are not used anymore and register new listeners.
	 * The method is called when project description is applied to workspace.
	 *
	 * @param oldPrjDescription - old project descriptions being replaced in the workspace.
	 * @param newPrjDescription - new project description being applied to the workspace.
	 */
	public static void reRegisterListeners(ICProjectDescription oldPrjDescription, ICProjectDescription newPrjDescription) {
		if (oldPrjDescription == newPrjDescription) {
			return;
		}

		List<ICListenerAgent> oldListeners = getListeners(oldPrjDescription);
		List<ListenerAssociation> newAssociations = getListenersAssociations(newPrjDescription);

		// unregister old listeners
		for (ICListenerAgent oldListener : oldListeners) {
			if (!isListenerInTheListOfAssociations(newAssociations, oldListener)) {
				int count = 0;
				if (oldListener instanceof LanguageSettingsWorkspaceProvider) {
					count = ((LanguageSettingsWorkspaceProvider) oldListener).decrementProjectCount();
				}
				if (count <= 0) {
					try {
						oldListener.unregisterListener();
					} catch (Throwable e) {
						// protect from any exceptions from implementers
						CCorePlugin.log("Exception trying unregister listener "+((ILanguageSettingsProvider) oldListener).getId(), e); //$NON-NLS-1$
					}
				}
			}
		}

		// register new listeners
		for (ListenerAssociation newListenerAssociation : newAssociations) {
			ICListenerAgent newListener = newListenerAssociation.listener;
			if (!isInList(oldListeners, newListener)) {
				int count = 1;
				if (newListener instanceof LanguageSettingsWorkspaceProvider) {
					count = ((LanguageSettingsWorkspaceProvider) newListener).incrementProjectCount();
				}
				if (count == 1) {
					try {
						newListener.registerListener(newListenerAssociation.cfgDescription);
					} catch (Throwable e) {
						// protect from any exceptions from implementers
						CCorePlugin.log("Exception trying register listener "+((ILanguageSettingsProvider) newListener).getId(), e); //$NON-NLS-1$
					}
				}
			}
		}

	}

	/**
	 * Adds a listener that will be notified of changes in language settings.
	 *
	 * @param listener - the listener to add
	 */
	public static void registerLanguageSettingsChangeListener(ILanguageSettingsChangeListener listener) {
		fLanguageSettingsChangeListeners.add(listener);
	}

	/**
	 * Removes a language settings change listener.
	 *
	 * @param listener - the listener to remove.
	 */
	public static void unregisterLanguageSettingsChangeListener(ILanguageSettingsChangeListener listener) {
		fLanguageSettingsChangeListeners.remove(listener);
	}

	/**
	 * Notifies all language settings change listeners of a change in language settings entries.
	 *
	 * @param event - the {@link ILanguageSettingsChangeEvent} event to be broadcast.
	 */
	private static void notifyLanguageSettingsChangeListeners(ILanguageSettingsChangeEvent event) {
		for (Object listener : fLanguageSettingsChangeListeners.getListeners()) {
			((ILanguageSettingsChangeListener) listener).handleEvent(event);
		}
	}

	/**
	 * Get list of setting entries from the pool in {@link LanguageSettingsStorage}.
	 */
	private static List<ICLanguageSettingEntry> getSettingEntriesPooled(ILanguageSettingsProvider provider,
			ICConfigurationDescription cfgDescription, IResource rc, String languageId) {
		try {
			return LanguageSettingsStorage.getPooledList(provider.getSettingEntries(cfgDescription, rc, languageId));
		} catch (Throwable e) {
			String cfgId = cfgDescription!=null ? cfgDescription.getId() : null;
			@SuppressWarnings("nls")
			String msg = "Exception in provider "+provider.getId()+": getSettingEntries("+cfgId+", "+rc+", "+languageId+")";
			CCorePlugin.log(msg, e);
			// return empty list to prevent getting potentially non-empty list from up the resource tree
			return LanguageSettingsStorage.getPooledEmptyList();
		}
	}

	/**
	 * Returns the list of setting entries of the given provider
	 * for the given configuration description, resource and language.
	 * This method reaches to the parent folder of the resource recursively
	 * if the resource does not define the entries for the given provider.
	 *
	 * @param provider - language settings provider.
	 * @param cfgDescription - configuration description.
	 * @param rc - resource such as file or folder.
	 * @param languageId - language id.
	 *
	 * @return the list of setting entries which is unmodifiable. Never returns {@code null}
	 *     although individual providers mandated to return {@code null} if no settings defined.
	 */
	public static List<ICLanguageSettingEntry> getSettingEntriesUpResourceTree(ILanguageSettingsProvider provider, ICConfigurationDescription cfgDescription, IResource rc, String languageId) {
		Assert.isTrue( !(rc instanceof IWorkspaceRoot) );
		if (provider != null) {
			List<ICLanguageSettingEntry> entries = getSettingEntriesPooled(provider, cfgDescription, rc, languageId);
			if (entries != null) {
				return entries;
			}
			if (rc != null) {
				IResource parentFolder = (rc instanceof IProject) ? null : rc.getParent();
				if (parentFolder != null) {
					return getSettingEntriesUpResourceTree(provider, cfgDescription, parentFolder, languageId);
				}
				// if out of parent resources - get default entries
				entries = getSettingEntriesPooled(provider, null, null, languageId);
				if (entries != null) {
					return entries;
				}
			}
		}

		return LanguageSettingsStorage.getPooledEmptyList();
	}

	/**
	 * Test if the binary flag contains a particular bit.
	 */
	private static boolean checkBit(int flags, int bit) {
		return (flags & bit) == bit;
	}

	/**
	 * Returns the list of setting entries of a certain kind (such as include paths)
	 * for the given configuration description, resource and language. This is a
	 * combined list for all providers taking into account settings of parent folder
	 * if settings for the given resource are not defined.
	 *
	 * @param cfgDescription - configuration description.
	 * @param rc - resource such as file or folder.
	 * @param languageId - language id.
	 * @param kind - kind of language settings entries, such as
	 *     {@link ICSettingEntry#INCLUDE_PATH} etc. This is a binary flag
	 *     and it is possible to specify composite kind.
	 *     Use {@link ICSettingEntry#ALL} to get all kinds.
	 * @param checkLocality - specifies if parameter {@code isLocal} should be considered.
	 * @param isLocal - {@code true} if "local" entries should be provided and
	 *     {@code false} for "system" entries. This makes sense for include paths where
	 *     [#include "..."] is "local" and [#include <...>] is system.
	 *
	 * @return the list of setting entries found.
	 */
	private static List<ICLanguageSettingEntry> getSettingEntriesByKind(ICConfigurationDescription cfgDescription,
			IResource rc, String languageId, int kind, boolean checkLocality, boolean isLocal) {

		if (!(cfgDescription instanceof ILanguageSettingsProvidersKeeper)) {
			return null;
		}

		List<ICLanguageSettingEntry> entries = new ArrayList<ICLanguageSettingEntry>();
		List<String> alreadyAdded = new ArrayList<String>();

		List<ILanguageSettingsProvider> providers = ((ILanguageSettingsProvidersKeeper) cfgDescription).getLanguageSettingProviders();
		for (ILanguageSettingsProvider provider: providers) {
			List<ICLanguageSettingEntry> providerEntries = getSettingEntriesUpResourceTree(provider, cfgDescription, rc, languageId);
			for (ICLanguageSettingEntry entry : providerEntries) {
				if (entry != null) {
					String entryName = entry.getName();
					boolean isRightKind = checkBit(entry.getKind(), kind);
					// Only first entry is considered
					// Entry flagged as "UNDEFINED" prevents adding entry with the same name down the line
					if (isRightKind && !alreadyAdded.contains(entryName)) {
						int flags = entry.getFlags();
						boolean isRightLocal = !checkLocality || (checkBit(flags, ICSettingEntry.LOCAL) == isLocal);
						if (isRightLocal) {
							if (!checkBit(flags, ICSettingEntry.UNDEFINED)) {
								entries.add(entry);
							}
							alreadyAdded.add(entryName);
						}
					}
				}
			}
		}

		return entries;
	}

	/**
	 * Returns the list of setting entries of a certain kind (such as include paths)
	 * for the given configuration description, resource and language. This is a
	 * combined list for all providers taking into account settings of parent folder
	 * if settings for the given resource are not defined. For include paths both
	 * local (#include "...") and system (#include <...>) entries are returned.
	 *
	 * @param cfgDescription - configuration description.
	 * @param rc - resource such as file or folder.
	 * @param languageId - language id.
	 * @param kind - kind of language settings entries, such as
	 *     {@link ICSettingEntry#INCLUDE_PATH} etc. This is a binary flag
	 *     and it is possible to specify composite kind.
	 *     Use {@link ICSettingEntry#ALL} to get all kinds.
	 *
	 * @return the list of setting entries.
	 */
	public static List<ICLanguageSettingEntry> getSettingEntriesByKind(ICConfigurationDescription cfgDescription, IResource rc, String languageId, int kind) {
		return getSettingEntriesByKind(cfgDescription, rc, languageId, kind, /* checkLocality */ false, /* isLocal */ false);
	}

	/**
	 * Returns the list of "system" (such as [#include <...>]) setting entries of a certain kind
	 * for the given configuration description, resource and language. This is a
	 * combined list for all providers taking into account settings of parent folder
	 * if settings for the given resource are not defined.
	 *
	 * @param cfgDescription - configuration description.
	 * @param rc - resource such as file or folder.
	 * @param languageId - language id.
	 * @param kind - kind of language settings entries, such as
	 *     {@link ICSettingEntry#INCLUDE_PATH} etc. This is a binary flag
	 *     and it is possible to specify composite kind.
	 *     Use {@link ICSettingEntry#ALL} to get all kinds.
	 *
	 * @return the list of setting entries.
	 */
	public static List<ICLanguageSettingEntry> getSystemSettingEntriesByKind(ICConfigurationDescription cfgDescription, IResource rc, String languageId, int kind) {
		return getSettingEntriesByKind(cfgDescription, rc, languageId, kind, /* checkLocality */ true, /* isLocal */ false);
	}

	/**
	 * Returns the list of "local" (such as [#include "..."]) setting entries of a certain kind
	 * for the given configuration description, resource and language. This is a
	 * combined list for all providers taking into account settings of parent folder
	 * if settings for the given resource are not defined.
	 *
	 * @param cfgDescription - configuration description.
	 * @param rc - resource such as file or folder.
	 * @param languageId - language id.
	 * @param kind - kind of language settings entries, such as
	 *     {@link ICSettingEntry#INCLUDE_PATH} etc. This is a binary flag
	 *     and it is possible to specify composite kind.
	 *     Use {@link ICSettingEntry#ALL} to get all kinds.
	 *
	 * @return the list of setting entries.
	 */
	public static List<ICLanguageSettingEntry> getLocalSettingEntriesByKind(ICConfigurationDescription cfgDescription, IResource rc, String languageId, int kind) {
		return getSettingEntriesByKind(cfgDescription, rc, languageId, kind, /* checkLocality */ true, /* isLocal */ true);
	}

	/**
	 * Deep clone of a list of language settings providers.
	 *
	 * @param baseProviders - list of providers to clone.
	 * @return newly cloned list.
	 */
	public static List<ILanguageSettingsProvider> cloneProviders(List<ILanguageSettingsProvider> baseProviders) {
		List<ILanguageSettingsProvider> newProviders = new ArrayList<ILanguageSettingsProvider>();
		for (ILanguageSettingsProvider provider : baseProviders) {
			if (provider instanceof ILanguageSettingsEditableProvider) {
				ILanguageSettingsEditableProvider newProvider = LanguageSettingsManager.getProviderCopy((ILanguageSettingsEditableProvider) provider, true);
				if (newProvider != null) {
					provider = newProvider;
				}
			}
			newProviders.add(provider);
		}
		return new ArrayList<ILanguageSettingsProvider>(newProviders);
	}

}

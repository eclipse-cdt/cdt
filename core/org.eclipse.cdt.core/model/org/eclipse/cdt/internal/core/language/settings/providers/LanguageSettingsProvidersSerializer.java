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
import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsEditableProvider;
import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsProvider;
import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsManager;
import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsSerializableProvider;
import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsStorage;
import org.eclipse.cdt.core.language.settings.providers.ScannerDiscoveryLegacySupport;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.LanguageManager;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
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

public class LanguageSettingsProvidersSerializer {
	private static final String PREFERENCE_WORSPACE_PROVIDERS_SET = "language.settings.providers.set.for.workspace";
	private static final String STORAGE_WORKSPACE_LANGUAGE_SETTINGS = "language.settings.xml"; //$NON-NLS-1$
	private static final String SETTINGS_FOLDER_NAME = ".settings/"; //$NON-NLS-1$
	private static final String STORAGE_PROJECT_LANGUAGE_SETTINGS = "language.settings.xml"; //$NON-NLS-1$
	private static final String ELEM_PLUGIN = "plugin"; //$NON-NLS-1$
	private static final String ELEM_EXTENSION = "extension"; //$NON-NLS-1$
	private static final String ATTR_POINT = "point"; //$NON-NLS-1$
	private static final String ELEM_PROJECT = "project"; //$NON-NLS-1$
	private static final String ELEM_CONFIGURATION = "configuration"; //$NON-NLS-1$
	private static final String ELEM_PROVIDER = "provider"; //$NON-NLS-1$
	private static final String ELEM_PROVIDER_REFERENCE = "provider-reference"; //$NON-NLS-1$

	private static ILock serializingLock = Job.getJobManager().newLock();

	/** Cache of globally available providers to be consumed by calling clients */
	private static Map<String, ILanguageSettingsProvider> rawGlobalWorkspaceProviders = new HashMap<String, ILanguageSettingsProvider>();
	private static Map<String, ILanguageSettingsProvider> globalWorkspaceProviders = new HashMap<String, ILanguageSettingsProvider>();

	private static ListenerList fLanguageSettingsChangeListeners = new ListenerList(ListenerList.IDENTITY);

	private static class ListenerAssociation {
		private ICListenerAgent listener;
		private ICConfigurationDescription cfgDescription;

		public ListenerAssociation(ICListenerAgent la, ICConfigurationDescription cfgd) {
			listener = la;
			cfgDescription = cfgd;
		}
	}

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
			// keep in mind that rawProvider can change
			ILanguageSettingsProvider rawProvider = getRawProvider();
			if (rawProvider instanceof ICListenerAgent) {
				((ICListenerAgent) rawProvider).registerListener(null);
			}
		}

		@Override
		public void unregisterListener() {
			// keep in mind that rawProvider can change
			ILanguageSettingsProvider rawProvider = getRawProvider();
			if (rawProvider instanceof ICListenerAgent) {
				((ICListenerAgent) rawProvider).unregisterListener();
			}
		}
	}

	/**
	 * Language Settings Change Event implementation.
	 *
	 */
	private static class LanguageSettingsChangeEvent implements ILanguageSettingsChangeEvent {
		private String projectName = null;
		private Map<String /*cfg*/, LanguageSettingsDelta> deltaMap = new HashMap<String, LanguageSettingsDelta>();

		/**
		 * The act of creating event resets internal delta count in configuration state.
		 * That implies that when the event is retrieved it must be fired or delta will go missing.
		 * That side effect is here to ensure atomic processing of firing & resetting the delta.
		 */
		public LanguageSettingsChangeEvent(ICProjectDescription prjDescription) {
			if (!prjDescription.isReadOnly()) {
				String msg = "Project description " + prjDescription.getName() + " is expected to be read-only";
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
						LanguageSettingsDelta delta = specSettings.dropDelta();
						if (delta != null)
							deltaMap.put(cfgDescription.getId(), delta);
					} else {
						IStatus ss = new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, "Internal error: Missing specSettings for " + cfgDescription.getClass().getSimpleName());
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

		@Override
		public String toString() {
			return "LanguageSettingsChangeEvent for project=[" + getProjectName() + "]"
					+ ", configurations=" + deltaMap.keySet()
				;
		}
	}

	/** static initializer */
	static {
		try {
			loadLanguageSettingsWorkspace();
		} catch (Throwable e) {
			// log and swallow any exception
			CCorePlugin.log("Error loading workspace language settings providers", e); //$NON-NLS-1$
		}
	}

	private static IFile getStoreInProjectArea(IProject project) throws CoreException {
		IFolder folder = project.getFolder(SETTINGS_FOLDER_NAME);
		if (!folder.exists()) {
			folder.create(true, true, null);
		}
		IFile storage = folder.getFile(STORAGE_PROJECT_LANGUAGE_SETTINGS);
		return storage;
	}

	/**
	 * TODO: refactor with ErrorParserManager ?
	 *
	 * @param store - name of the store
	 * @return location of the store in the plug-in state area
	 */
	private static URI getStoreInWorkspaceArea(String store) {
		IPath location = CCorePlugin.getDefault().getStateLocation().append(store);
		URI uri = URIUtil.toURI(location);
		return uri;
	}

	/**
		 * Set and store in workspace area user defined providers.
		 *
		 * @param providers - array of user defined providers
		 * @throws CoreException in case of problems
		 */
		public static void setWorkspaceProviders(List<ILanguageSettingsProvider> providers) throws CoreException {
			setWorkspaceProvidersInternal(providers);
			serializeLanguageSettingsWorkspace();
			// generate preference change event
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
		Map<String, ILanguageSettingsProvider> rawWorkspaceProviders = new HashMap<String, ILanguageSettingsProvider>();
		List<ILanguageSettingsProvider> extensionProviders = new ArrayList<ILanguageSettingsProvider>(LanguageSettingsExtensionManager.getExtensionProvidersInternal());
		for (ILanguageSettingsProvider rawExtensionProvider : extensionProviders) {
			if (rawExtensionProvider!=null) {
				rawWorkspaceProviders.put(rawExtensionProvider.getId(), rawExtensionProvider);
			}
		}

		List<ILanguageSettingsProvider> rawProviders = new ArrayList<ILanguageSettingsProvider>();
		if (providers!=null) {
			for (ILanguageSettingsProvider provider : providers) {
				if (isWorkspaceProvider(provider)) {
					provider = rawGlobalWorkspaceProviders.get(provider.getId());
				}
				if (provider!=null) {
					rawProviders.add(provider);
				}
			}
			for (ILanguageSettingsProvider provider : rawProviders) {
				rawWorkspaceProviders.put(provider.getId(), provider);
			}
		}

		List<ICListenerAgent> oldListeners = selectListeners(rawGlobalWorkspaceProviders.values());
		List<ICListenerAgent> newListeners = selectListeners(rawProviders);

		for (ICListenerAgent oldListener : oldListeners) {
			if (!isObjectInTheList(newListeners, oldListener)) {
				LanguageSettingsWorkspaceProvider wspProvider = (LanguageSettingsWorkspaceProvider) globalWorkspaceProviders.get(((ILanguageSettingsProvider)oldListener).getId());
				if (wspProvider != null && wspProvider.getProjectCount() > 0) {
					oldListener.unregisterListener();
				}
			}
		}

		for (ICListenerAgent newListener : newListeners) {
			if (!isObjectInTheList(oldListeners, newListener)) {
				LanguageSettingsWorkspaceProvider wspProvider = (LanguageSettingsWorkspaceProvider) globalWorkspaceProviders.get(((ILanguageSettingsProvider)newListener).getId());
				if (wspProvider != null && wspProvider.getProjectCount() > 0) {
					newListener.registerListener(null);
				}
			}
		}

		rawGlobalWorkspaceProviders = rawWorkspaceProviders;
	}

	private static List<LanguageSettingsChangeEvent> createLanguageSettingsChangeEvents(List<LanguageSettingsSerializableProvider> serializableProviders) {
		List<LanguageSettingsChangeEvent> events = new ArrayList<LanguageSettingsChangeEvent>();

		List<String> serializableIds = new ArrayList<String>();
		for (LanguageSettingsSerializableProvider provider : serializableProviders) {
			serializableIds.add(provider.getId());
		}

		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject[] projects = root.getProjects();
projects:
		for (IProject project : projects) {
			if (project.isAccessible()) {
				ICProjectDescription prjDescription = CCorePlugin.getDefault().getProjectDescription(project, false);
				if (prjDescription != null) {
					ICConfigurationDescription[] cfgDescriptions = prjDescription.getConfigurations();
					for (ICConfigurationDescription cfgDescription : cfgDescriptions) {
						for (ILanguageSettingsProvider provider : cfgDescription.getLanguageSettingProviders()) {
							if (isWorkspaceProvider(provider) && serializableIds.contains(provider.getId())) {
								LanguageSettingsChangeEvent event = new LanguageSettingsChangeEvent(prjDescription);
								if (event.getConfigurationDescriptionIds().length > 0) {
									events.add(event);
								}
								continue projects;
							}
						}
					}
				}
			}

		}

		return events;
	}

	public static void serializeLanguageSettingsWorkspace() throws CoreException {
		LanguageSettingsLogger.logWarning("LanguageSettingsProvidersSerializer.serializeLanguageSettingsWorkspace()");

		URI uriStoreWsp = getStoreInWorkspaceArea(STORAGE_WORKSPACE_LANGUAGE_SETTINGS);
		List<LanguageSettingsSerializableProvider> serializableWorkspaceProviders = new ArrayList<LanguageSettingsSerializableProvider>();
		for (ILanguageSettingsProvider provider : rawGlobalWorkspaceProviders.values()) {
			if (provider instanceof LanguageSettingsSerializableProvider) {
				serializableWorkspaceProviders.add((LanguageSettingsSerializableProvider)provider);
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
				Element elementExtension = XmlUtil.appendElement(rootElement, ELEM_EXTENSION, new String[] {ATTR_POINT, LanguageSettingsExtensionManager.PROVIDER_EXTENSION_FULL_ID});

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
			CCorePlugin.log("Internal error while trying to serialize language settings", e); //$NON-NLS-1$
			IStatus s = new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, "Internal error while trying to serialize language settings", e);
			throw new CoreException(s);
		}
	}

	public static void loadLanguageSettingsWorkspace() throws CoreException {
		List <ILanguageSettingsProvider> providers = null;

		URI uriStoreWsp = getStoreInWorkspaceArea(STORAGE_WORKSPACE_LANGUAGE_SETTINGS);

		Document doc = null;
		serializingLock.acquire();
		try {
			doc = XmlUtil.loadXml(uriStoreWsp);
		} catch (Exception e) {
			CCorePlugin.log("Can't load preferences from file "+uriStoreWsp, e); //$NON-NLS-1$
		} finally {
			serializingLock.release();
		}

		if (doc!=null) {
			Element rootElement = doc.getDocumentElement();
			NodeList providerNodes = rootElement.getElementsByTagName(LanguageSettingsSerializableProvider.ELEM_PROVIDER);

			List<String> userDefinedProvidersIds = new ArrayList<String>();
			for (int i=0;i<providerNodes.getLength();i++) {
				Node providerNode = providerNodes.item(i);
				String providerId = XmlUtil.determineAttributeValue(providerNode, LanguageSettingsExtensionManager.ATTR_ID);
				if (userDefinedProvidersIds.contains(providerId)) {
					String msg = "Ignored repeatedly persisted duplicate language settings provider id=" + providerId;
					CCorePlugin.log(new Status(IStatus.WARNING, CCorePlugin.PLUGIN_ID, msg, new Exception()));
					continue;
				}
				userDefinedProvidersIds.add(providerId);

				ILanguageSettingsProvider provider = loadProvider(providerNode);
				if (provider!=null) {
					if (providers==null)
						providers= new ArrayList<ILanguageSettingsProvider>();

					if (!LanguageSettingsExtensionManager.equalsExtensionProvider(provider)) {
						providers.add(provider);
					}
				}
			}
		}
		setWorkspaceProvidersInternal(providers);
	}

	/**
	 * @noreference This method is not intended to be referenced by clients.
	 * It is public solely for benefit of JUnit testing.
	 */
	public static void serializeLanguageSettingsInternal(Element projectElementPrjStore, Element projectElementWspStore, ICProjectDescription prjDescription) throws CoreException {

		ICConfigurationDescription[] cfgDescriptions = prjDescription.getConfigurations();
		for (ICConfigurationDescription cfgDescription : cfgDescriptions) {
			Element elementConfiguration = XmlUtil.appendElement(projectElementPrjStore, ELEM_CONFIGURATION, new String[] {
					LanguageSettingsExtensionManager.ATTR_ID, cfgDescription.getId(),
					LanguageSettingsExtensionManager.ATTR_NAME, cfgDescription.getName(),
				});
			Element elementConfigurationWsp = null;
			List<ILanguageSettingsProvider> providers = cfgDescription.getLanguageSettingProviders();
			if (providers.size()>0) {
				Element elementExtension = XmlUtil.appendElement(elementConfiguration, ELEM_EXTENSION, new String[] {
						ATTR_POINT, LanguageSettingsExtensionManager.PROVIDER_EXTENSION_FULL_ID});
				Element elementExtensionWsp = null;
				for (ILanguageSettingsProvider provider : providers) {
					if (isWorkspaceProvider(provider)) {
						// Element elementProviderReference =
						XmlUtil.appendElement(elementExtension, ELEM_PROVIDER_REFERENCE, new String[] {
								LanguageSettingsExtensionManager.ATTR_ID, provider.getId()});
						continue;
					}
					if (provider instanceof LanguageSettingsSerializableProvider) {
						LanguageSettingsSerializableProvider lss = (LanguageSettingsSerializableProvider) provider;

						boolean useWsp = projectElementWspStore!=null && projectElementPrjStore!=projectElementWspStore;
						if (lss.isStoringEntriesInProjectArea() || !useWsp) {
							lss.serialize(elementExtension);
						} else {
							lss.serializeAttributes(elementExtension);

							// lazy initialization of elements - to avoid serialization of no-data file
							if (elementExtensionWsp==null) {
								if (elementConfigurationWsp==null) {
									elementConfigurationWsp = XmlUtil.appendElement(projectElementWspStore, ELEM_CONFIGURATION, new String[] {
											LanguageSettingsExtensionManager.ATTR_ID, cfgDescription.getId(),
											LanguageSettingsExtensionManager.ATTR_NAME, cfgDescription.getName(),
									});
								}
								elementExtensionWsp = XmlUtil.appendElement(elementConfigurationWsp, ELEM_EXTENSION, new String[] {
										ATTR_POINT, LanguageSettingsExtensionManager.PROVIDER_EXTENSION_FULL_ID});
							}
							Element elementProviderWsp = XmlUtil.appendElement(elementExtensionWsp, ELEM_PROVIDER, new String[] {
									LanguageSettingsExtensionManager.ATTR_ID, provider.getId()});
							lss.serializeEntries(elementProviderWsp);
						}
					} else {
						// Element elementProvider =
						XmlUtil.appendElement(elementExtension, LanguageSettingsExtensionManager.ELEM_PROVIDER, new String[] {
								LanguageSettingsExtensionManager.ATTR_ID, provider.getId(),
								LanguageSettingsExtensionManager.ATTR_NAME, provider.getName(),
								LanguageSettingsExtensionManager.ATTR_CLASS, provider.getClass().getCanonicalName(),
							});
					}
				}
			}
		}
	}

	public static void serializeLanguageSettings(ICProjectDescription prjDescription) throws CoreException {
		IProject project = prjDescription.getProject();
		LanguageSettingsLogger.logWarning("LanguageSettingsProvidersSerializer.serializeLanguageSettings() for " + project);

		try {
			// Document to store in project area
			Document docStorePrj = XmlUtil.newDocument();
			Element projectElementStorePrj = XmlUtil.appendElement(docStorePrj, ELEM_PROJECT);
			// Document to store in workspace area
			Document docStoreWsp = XmlUtil.newDocument();
			Element projectElementStoreWsp = XmlUtil.appendElement(docStoreWsp, ELEM_PROJECT);

			// The project store should not be absent. Absent store means legacy project, not 0 providers.
			IFile fileStorePrj = getStoreInProjectArea(project);

			URI uriStoreWsp = getStoreInWorkspaceArea(project.getName()+'.'+STORAGE_WORKSPACE_LANGUAGE_SETTINGS);
			LanguageSettingsChangeEvent event = null;

			try {
				serializingLock.acquire();

				// Note that need for serialization may exist even if LSE event delta is empty,
				// as number or properties of providers may differ
				serializeLanguageSettingsInternal(projectElementStorePrj, projectElementStoreWsp, prjDescription);
				XmlUtil.serializeXml(docStorePrj, fileStorePrj);

				// project-specific location in workspace area
				boolean isWorkspaceStoreEmpty = projectElementStoreWsp.getChildNodes().getLength() == 0;
				if (!isWorkspaceStoreEmpty) {
					XmlUtil.serializeXml(docStoreWsp, uriStoreWsp);
				} else {
					new java.io.File(uriStoreWsp).delete();
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
			IStatus s = new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, "Internal error while trying to serialize language settings", e);
			CCorePlugin.log(s);
			throw new CoreException(s);
		}
	}

	/**
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
		for (int ic=0;ic<configurationNodes.getLength();ic++) {
			Node cfgNode = configurationNodes.item(ic);
			if (!(cfgNode instanceof Element && cfgNode.getNodeName().equals(ELEM_CONFIGURATION)) )
				continue;
			List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
			String cfgId = XmlUtil.determineAttributeValue(cfgNode, LanguageSettingsExtensionManager.ATTR_ID);
			@SuppressWarnings("unused")
			String cfgName = XmlUtil.determineAttributeValue(cfgNode, LanguageSettingsExtensionManager.ATTR_NAME);

			NodeList extensionAndReferenceNodes = cfgNode.getChildNodes();
			for (int ie=0;ie<extensionAndReferenceNodes.getLength();ie++) {
				Node extNode = extensionAndReferenceNodes.item(ie);
				if (!(extNode instanceof Element))
					continue;

				if (extNode.getNodeName().equals(ELEM_EXTENSION)) {
					NodeList providerNodes = extNode.getChildNodes();

					for (int i=0;i<providerNodes.getLength();i++) {
						Node providerNode = providerNodes.item(i);
						if (!(providerNode instanceof Element))
							continue;

						ILanguageSettingsProvider provider=null;
						if (providerNode.getNodeName().equals(ELEM_PROVIDER_REFERENCE)) {
							String providerId = XmlUtil.determineAttributeValue(providerNode, LanguageSettingsExtensionManager.ATTR_ID);
							provider = getWorkspaceProvider(providerId);
						} else if (providerNode.getNodeName().equals(LanguageSettingsExtensionManager.ELEM_PROVIDER)) {
							provider = loadProvider(providerNode);
							if (provider instanceof LanguageSettingsSerializableProvider) {
								LanguageSettingsSerializableProvider lss = (LanguageSettingsSerializableProvider) provider;
								if (!lss.isStoringEntriesInProjectArea() && projectElementWsp!=null) {
									loadProviderEntries(lss, cfgId, projectElementWsp);
								}
							}
						}
						if (provider!=null) {
							providers.add(provider);
						}
					}
				}
			}

			ICConfigurationDescription cfgDescription = prjDescription.getConfigurationById(cfgId);
			if (cfgDescription!=null) {
				cfgDescription.setLanguageSettingProviders(providers);
				if (cfgDescription instanceof IInternalCCfgInfo) {
					try {
						((IInternalCCfgInfo) cfgDescription).getSpecSettings().dropDelta();
					} catch (CoreException e) {
						CCorePlugin.log(e);
					}
				}
			}
		}
	}

	private static void loadProviderEntries(LanguageSettingsSerializableProvider provider,
			String cfgId, Element projectElementWsp) {
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
		NodeList configurationNodes = projectElementWsp.getChildNodes();
		for (int ic=0;ic<configurationNodes.getLength();ic++) {
			Node cfgNode = configurationNodes.item(ic);
			if (!(cfgNode instanceof Element && cfgNode.getNodeName().equals(ELEM_CONFIGURATION)) )
				continue;
			String cfgIdXml = XmlUtil.determineAttributeValue(cfgNode, LanguageSettingsExtensionManager.ATTR_ID);
			if (!cfgId.equals(cfgIdXml))
				continue;

			NodeList extensionAndReferenceNodes = cfgNode.getChildNodes();
			for (int ie=0;ie<extensionAndReferenceNodes.getLength();ie++) {
				Node extNode = extensionAndReferenceNodes.item(ie);
				if (!(extNode instanceof Element))
					continue;

				if (extNode.getNodeName().equals(ELEM_EXTENSION)) {
					NodeList providerNodes = extNode.getChildNodes();

					for (int i=0;i<providerNodes.getLength();i++) {
						Node providerNode = providerNodes.item(i);
						if (!(providerNode instanceof Element))
							continue;
						if (!providerNode.getNodeName().equals(LanguageSettingsExtensionManager.ELEM_PROVIDER))
							continue;
						String providerIdXml = XmlUtil.determineAttributeValue(providerNode, LanguageSettingsExtensionManager.ATTR_ID);
						if (!provider.getId().equals(providerIdXml))
							continue;

						provider.loadEntries((Element) providerNode);
						return;


					}
				}
			}

		}
	}

	private static ILanguageSettingsProvider loadProvider(Node providerNode) {
		String attrClass = XmlUtil.determineAttributeValue(providerNode, LanguageSettingsExtensionManager.ATTR_CLASS);
		ILanguageSettingsProvider provider = LanguageSettingsExtensionManager.getProviderInstance(attrClass);

		if (provider instanceof LanguageSettingsSerializableProvider)
			((LanguageSettingsSerializableProvider)provider).load((Element) providerNode);

		return provider;
	}

	public static void loadLanguageSettings(ICProjectDescription prjDescription) {
		IProject project = prjDescription.getProject();
		IFile storePrj = project.getFile(SETTINGS_FOLDER_NAME+STORAGE_PROJECT_LANGUAGE_SETTINGS);
		// AG: FIXME not sure about that one
		// Causes java.lang.IllegalArgumentException: Attempted to beginRule: P/cdt312, does not match outer scope rule: org.eclipse.cdt.internal.ui.text.c.hover.CSourceHover$SingletonRule@6f34fb
		try {
			storePrj.refreshLocal(IResource.DEPTH_ZERO, null);
		} catch (CoreException e) {
			// ignore failure
		}
		if (storePrj.exists() && storePrj.isAccessible()) {
			Document doc = null;
			try {
				doc = XmlUtil.loadXml(storePrj);
				Element rootElementPrj = doc.getDocumentElement(); // <project/>

				URI uriStoreWsp = getStoreInWorkspaceArea(project.getName()+'.'+STORAGE_WORKSPACE_LANGUAGE_SETTINGS);
				Document docWsp = null;
				serializingLock.acquire();
				try {
					docWsp = XmlUtil.loadXml(uriStoreWsp);
				} finally {
					serializingLock.release();
				}

				Element rootElementWsp = null; // <project/>
				if (docWsp!=null) {
					rootElementWsp = docWsp.getDocumentElement();
				}


				loadLanguageSettingsInternal(rootElementPrj, rootElementWsp, prjDescription);
			} catch (Exception e) {
				CCorePlugin.log("Can't load preferences from file "+storePrj.getLocation(), e); //$NON-NLS-1$
			}

			if (doc!=null) {
			}

		} else {
			// Already existing legacy projects
			ICConfigurationDescription[] cfgDescriptions = prjDescription.getConfigurations();
			for (ICConfigurationDescription cfgDescription : cfgDescriptions) {
				if (cfgDescription!=null) {
					List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>(2);
					ILanguageSettingsProvider providerMBS = getWorkspaceProvider(ScannerDiscoveryLegacySupport.MBS_LANGUAGE_SETTINGS_PROVIDER);
					providers.add(providerMBS);
					cfgDescription.setLanguageSettingProviders(providers);
				}
			}

		}
	}

	/**
	 * FIXME Get Language Settings Provider defined in the workspace. That includes user-defined
	 * providers and after that providers defined as extensions via
	 * {@code org.eclipse.cdt.core.LanguageSettingsProvider} extension point.
	 * That returns actual object, any modifications will affect any configuration
	 * referring to the provider.
	 *
	 * @param id - ID of provider to find.
	 * @return the provider or {@code null} if provider is not defined.
	 */
	public static ILanguageSettingsProvider getWorkspaceProvider(String id) {
		ILanguageSettingsProvider provider = globalWorkspaceProviders.get(id);
		if (provider == null) {
			provider = new LanguageSettingsWorkspaceProvider(id);
			globalWorkspaceProviders.put(id, provider);
		}
		return provider;
	}

	public static ILanguageSettingsProvider getRawWorkspaceProvider(String id) {
		return rawGlobalWorkspaceProviders.get(id);
	}

	/**
	 * TODO
	 * @return ordered set of providers defined in the workspace which include contributed through extension + user defined ones
	 *
	 */
	public static List<ILanguageSettingsProvider> getWorkspaceProviders() {
		ArrayList<ILanguageSettingsProvider> workspaceProviders = new ArrayList<ILanguageSettingsProvider>();
		for (ILanguageSettingsProvider rawProvider : rawGlobalWorkspaceProviders.values()) {
			workspaceProviders.add(getWorkspaceProvider(rawProvider.getId()));
		}
		return workspaceProviders;
	}

	/**
	 * Checks if the provider is defined on the workspace level.
	 *
	 * @param provider - provider to check.
	 * @return {@code true} if the given provider is workspace provider, {@code false} otherwise.
	 *
	 */
	public static boolean isWorkspaceProvider(ILanguageSettingsProvider provider) {
		return provider instanceof LanguageSettingsWorkspaceProvider;
	}

	/**
	 * TODO - remove me
	 * Temporary method to report inconsistency in log.
	 */
	@Deprecated
	public static void assertConsistency(ICProjectDescription prjDescription) {
		if (prjDescription != null) {
			List<ILanguageSettingsProvider> prjProviders = new ArrayList<ILanguageSettingsProvider>();
			for (ICConfigurationDescription cfgDescription : prjDescription.getConfigurations()) {
				List<ILanguageSettingsProvider> providers = cfgDescription.getLanguageSettingProviders();
				for (ILanguageSettingsProvider provider : providers) {
					if (!LanguageSettingsManager.isWorkspaceProvider(provider)) {
						if (isObjectInTheList(prjProviders, provider)) {
							IStatus status = new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, "Inconsistent state, duplicate LSP in project description "
									+ "[" + System.identityHashCode(provider) + "] "
									+ provider);
							CoreException e = new CoreException(status);
							CCorePlugin.log(e);
						}
						prjProviders.add(provider);
					}
				}
			}
		}
	}

	private static <T> boolean  isObjectInTheList(Collection<T> list, T element) {
		// list.contains(element) won't do it as we are interested in exact object, not in equal object
		for (T elem : list) {
			if (elem == element)
				return true;
		}
		return false;
	}

	private static boolean isListenerInTheListOfAssociations(Collection<ListenerAssociation> list, ICListenerAgent element) {
		// list.contains(element) won't do it as we are interested in exact object, not in equal object
		for (ListenerAssociation la : list) {
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
				List<ILanguageSettingsProvider> providers = cfgDescription.getLanguageSettingProviders();
				for (ILanguageSettingsProvider provider : providers) {
					if (provider instanceof ICListenerAgent) {
						ICListenerAgent listener = (ICListenerAgent) provider;
						if (!isObjectInTheList(listeners, listener)) {
							listeners.add(listener);
						}
					}
				}
			}
		}
		return listeners;
	}

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
				List<ILanguageSettingsProvider> providers = cfgDescription.getLanguageSettingProviders();
				List<ICListenerAgent> listeners = selectListeners(providers);
				for (ICListenerAgent listener : listeners) {
					if (!isListenerInTheListOfAssociations(associations, listener)) {
						associations.add(new ListenerAssociation(listener, cfgDescription));
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
			assertConsistency(oldPrjDescription); // TODO - remove me
			return;
		}

		assertConsistency(oldPrjDescription); // TODO - remove me
		assertConsistency(newPrjDescription); // TODO - remove me

		List<ICListenerAgent> oldListeners = getListeners(oldPrjDescription);
		List<ListenerAssociation> newAssociations = getListenersAssociations(newPrjDescription);

		for (ICListenerAgent oldListener : oldListeners) {
			if (!isListenerInTheListOfAssociations(newAssociations, oldListener)) {
				int count = 0;
				if (oldListener instanceof LanguageSettingsWorkspaceProvider) {
					count = ((LanguageSettingsWorkspaceProvider) oldListener).decrementProjectCount();
				}
				if (count == 0) {
					try {
						oldListener.unregisterListener();
					} catch (Throwable e) {
						// protect from any exceptions from implementers
						CCorePlugin.log(e);
					}
				}
			}
		}

		for (ListenerAssociation newListenerAssociation : newAssociations) {
			ICListenerAgent newListener = newListenerAssociation.listener;
			if (!isObjectInTheList(oldListeners, newListener)) {
				int count = 1;
				if (newListener instanceof LanguageSettingsWorkspaceProvider) {
					count = ((LanguageSettingsWorkspaceProvider) newListener).incrementProjectCount();
				}
				if (count == 1) {
					try {
						newListener.registerListener(newListenerAssociation.cfgDescription);
					} catch (Throwable e) {
						// protect from any exceptions from implementers
						CCorePlugin.log(e);
					}
				}
			}
		}

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
				try {
					provider = ((ILanguageSettingsEditableProvider) provider).clone();
				} catch (CloneNotSupportedException e) {
					IStatus status = new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, Status.OK,
							"Not able to clone provider " + provider.getClass(), e);
					CCorePlugin.log(status);
				}
			}
			newProviders.add(provider);
		}
		return new ArrayList<ILanguageSettingsProvider>(newProviders);
	}

	/**
	 * Adds a listener that will be notified of changes in language settings.
	 *
	 * @param listener the ILanguageMappingChangeListener to add
	 */
	public static void registerLanguageSettingsChangeListener(ILanguageSettingsChangeListener listener) {
		fLanguageSettingsChangeListeners.add(listener);
	}

	/**
	 * Removes a language settings change listener.
	 *
	 * @param listener the ILanguageMappingChangeListener to remove.
	 */
	public static void unregisterLanguageSettingsChangeListener(ILanguageSettingsChangeListener listener) {
		fLanguageSettingsChangeListeners.remove(listener);
	}

	/**
	 * Notifies all language settings change listeners of a change.
	 *
	 * @param event the ILanguageSettingsChangeEvent event to be broadcast.
	 */
	public static void notifyLanguageSettingsChangeListeners(ILanguageSettingsChangeEvent event) {
		LanguageSettingsLogger.logWarning("Firing " + event);

		Object[] listeners = fLanguageSettingsChangeListeners.getListeners();
		for (Object obj : listeners) {
			ILanguageSettingsChangeListener listener = (ILanguageSettingsChangeListener) obj;
			listener.handleEvent(event);
		}
	}

	private static List<ICLanguageSettingEntry> getSettingEntriesPooled(ILanguageSettingsProvider provider,
			ICConfigurationDescription cfgDescription, IResource rc, String languageId) {

		try {
			return LanguageSettingsStorage.getPooledList(provider.getSettingEntries(cfgDescription, rc, languageId));
		} catch (Throwable e) {
			String cfgId = cfgDescription!=null ? cfgDescription.getId() : null;
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
	 * in case the resource does not define the entries for the given provider.
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
		if (provider!=null) {
			List<ICLanguageSettingEntry> entries = getSettingEntriesPooled(provider, cfgDescription, rc, languageId);
			if (entries!=null) {
				return entries;
			}
			if (rc!=null) {
				IResource parentFolder = (rc instanceof IProject) ? null : rc.getParent();
				if (parentFolder!=null) {
					return getSettingEntriesUpResourceTree(provider, cfgDescription, parentFolder, languageId);
				}
				// if out of parent resources - get default entries for the applicable language scope
				entries = getSettingEntriesPooled(provider, null, null, languageId);
				if (entries!=null) {
					return entries;
				}
			}
		}

		return LanguageSettingsStorage.getPooledEmptyList();
	}

	/**
	 * Builds for the provider a nice looking resource tree to present hierarchical view to the user.
	 * Note that it is not advisable to "compact" the tree because of potential loss of information
	 * which is especially important during partial or incremental builds.
	 *
	 * @param provider - language settings provider to build the tree for.
	 * @param cfgDescription - configuration description.
	 * @param languageId - language ID.
	 * @param folder - container where the tree roots.
	 */
	public static void buildResourceTree(LanguageSettingsSerializableProvider provider,
			ICConfigurationDescription cfgDescription, String languageId, IContainer folder) {
		IResource[] members = null;
		try {
			members = folder.members();
		} catch (Exception e) {
			CCorePlugin.log(e);
		}
		if (members==null)
			return;

		for (IResource rc : members) {
			if (rc instanceof IContainer) {
				buildResourceTree(provider, cfgDescription, languageId, (IContainer) rc);
			}
		}

		int rcNumber = members.length;

		Map<List<ICLanguageSettingEntry>, Integer> listMap = new HashMap<List<ICLanguageSettingEntry>, Integer>();

		// on the first pass find majority entries
		List<ICLanguageSettingEntry> majorityEntries = null;
		List<ICLanguageSettingEntry> candidate = null;
		int candidateCount = 0;
		for (IResource rc : members) {
			if (!isLanguageInScope(rc, cfgDescription, languageId)) {
				rcNumber--;
			} else {
				List<ICLanguageSettingEntry> entries = provider.getSettingEntries(null, rc, languageId);
				if (entries==null && rc instanceof IContainer) {
					rcNumber--;
				} else {
					Integer count = listMap.get(entries);
					if (count==null) {
						count = 0;
					}
					count++;

					if (count>candidateCount) {
						candidateCount = count;
						candidate = entries;
					}

					listMap.put(entries, count);
				}
			}

			if (candidateCount > rcNumber/2) {
				majorityEntries = candidate;
				break;
			}
		}

		if (majorityEntries!=null) {
			provider.setSettingEntries(cfgDescription, folder, languageId, majorityEntries);
		}

		// second pass - assign the entries to the folders
		for (IResource rc : members) {
			List<ICLanguageSettingEntry> entries = provider.getSettingEntries(null, rc, languageId);
			if (entries!=null && entries==majorityEntries) {
				if (!(rc instanceof IFile)) { // preserve information which files were collected
					provider.setSettingEntries(cfgDescription, rc, languageId, null);
				}
			}
		}
	}

	private static boolean isLanguageInScope(IResource rc, ICConfigurationDescription cfgDescription, String languageId) {
		if (rc instanceof IFile) {
			ILanguage lang = null;
			try {
				lang = LanguageManager.getInstance().getLanguageForFile((IFile) rc, cfgDescription);
			} catch (CoreException e) {
				CCorePlugin.log("Error loading language settings providers extensions", e); //$NON-NLS-1$
			}
			if (lang==null || (languageId!=null && !languageId.equals(lang.getId()))) {
				return false;
			}
		}
		return true;
	}

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

		List<ICLanguageSettingEntry> entries = new ArrayList<ICLanguageSettingEntry>();
		List<String> alreadyAdded = new ArrayList<String>();

		List<ILanguageSettingsProvider> providers = cfgDescription.getLanguageSettingProviders();
		for (ILanguageSettingsProvider provider: providers) {
			List<ICLanguageSettingEntry> providerEntries = getSettingEntriesUpResourceTree(provider, cfgDescription, rc, languageId);
			for (ICLanguageSettingEntry entry : providerEntries) {
				if (entry!=null) {
					String entryName = entry.getName();
					boolean isRightKind = (entry.getKind() & kind) != 0;
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

}

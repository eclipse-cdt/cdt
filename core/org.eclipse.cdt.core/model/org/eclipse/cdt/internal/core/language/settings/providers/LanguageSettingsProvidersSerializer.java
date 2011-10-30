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
import org.eclipse.cdt.core.language.settings.providers.ICListenerRegisterer;
import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsEditableProvider;
import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsProvider;
import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsManager;
import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsSerializable;
import org.eclipse.cdt.core.language.settings.providers.ScannerDiscoveryLegacySupport;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.internal.core.XmlUtil;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
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
	public static final char PROVIDER_DELIMITER = ';';
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

	private static class ListenerAssociation {
		private ICListenerRegisterer listener;
		private ICConfigurationDescription cfgDescription;
		
		public ListenerAssociation(ICListenerRegisterer li, ICConfigurationDescription cfgd) {
			listener = li;
			cfgDescription = cfgd;
		}
	}

	private static class LanguageSettingsWorkspaceProvider implements ILanguageSettingsProvider, ICListenerRegisterer {
		private String providerId;
		private int projectCount = 0;

		private LanguageSettingsWorkspaceProvider(String id) {
			Assert.isNotNull(id);
			providerId = id;
		}

		public String getId() {
			return providerId;
		}

		public String getName() {
			ILanguageSettingsProvider rawProvider = getRawProvider();
			String name = rawProvider!=null ? rawProvider.getName() : null;
			return name;
		}

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
		
		public void registerListener(ICConfigurationDescription cfgDescription) {
			// keep in mind that rawProvider can change
			ILanguageSettingsProvider rawProvider = getRawProvider();
			if (rawProvider instanceof ICListenerRegisterer) {
				((ICListenerRegisterer) rawProvider).registerListener(null);
			}
		}

		public void unregisterListener() {
			// keep in mind that rawProvider can change
			ILanguageSettingsProvider rawProvider = getRawProvider();
			if (rawProvider instanceof ICListenerRegisterer) {
				((ICListenerRegisterer) rawProvider).unregisterListener();
			}
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

		List<ICListenerRegisterer> oldListeners = selectListeners(rawGlobalWorkspaceProviders.values());
		List<ICListenerRegisterer> newListeners = selectListeners(rawProviders);
		
		for (ICListenerRegisterer oldListener : oldListeners) {
			if (!isObjectInTheList(newListeners, oldListener)) {
				LanguageSettingsWorkspaceProvider wspProvider = (LanguageSettingsWorkspaceProvider) globalWorkspaceProviders.get(((ILanguageSettingsProvider)oldListener).getId());
				if (wspProvider != null && wspProvider.getProjectCount() > 0) {
					oldListener.unregisterListener();
				}
			}
		}
		
		for (ICListenerRegisterer newListener : newListeners) {
			if (!isObjectInTheList(oldListeners, newListener)) {
				LanguageSettingsWorkspaceProvider wspProvider = (LanguageSettingsWorkspaceProvider) globalWorkspaceProviders.get(((ILanguageSettingsProvider)newListener).getId());
				if (wspProvider != null && wspProvider.getProjectCount() > 0) {
					newListener.registerListener(null);
				}
			}
		}
		
		rawGlobalWorkspaceProviders = rawWorkspaceProviders;
	}

	public static void serializeLanguageSettingsWorkspace() throws CoreException {
		URI uriStoreWsp = getStoreInWorkspaceArea(STORAGE_WORKSPACE_LANGUAGE_SETTINGS);
		List<LanguageSettingsSerializable> serializableExtensionProviders = new ArrayList<LanguageSettingsSerializable>();
		for (ILanguageSettingsProvider provider : rawGlobalWorkspaceProviders.values()) {
			if (provider instanceof LanguageSettingsSerializable) {
				// serialize all editable providers which are different from corresponding extension
				// and serialize all serializable ones that are not editable (those are singletons and we don't know whether they changed)
				if (!(provider instanceof ILanguageSettingsEditableProvider) || !LanguageSettingsExtensionManager.equalsExtensionProvider(provider)) {
					serializableExtensionProviders.add((LanguageSettingsSerializable)provider);
				}
			}
		}
		try {
			if (serializableExtensionProviders.isEmpty()) {
				java.io.File fileStoreWsp = new java.io.File(uriStoreWsp);
				serializingLock.acquire();
				try {
					fileStoreWsp.delete();
				} finally {
					serializingLock.release();
				}
			} else {
				Document doc = XmlUtil.newDocument();
				Element rootElement = XmlUtil.appendElement(doc, ELEM_PLUGIN);
				Element elementExtension = XmlUtil.appendElement(rootElement, ELEM_EXTENSION, new String[] {ATTR_POINT, LanguageSettingsExtensionManager.PROVIDER_EXTENSION_FULL_ID});
	
				for (LanguageSettingsSerializable provider : serializableExtensionProviders) {
					provider.serialize(elementExtension);
				}
	
				serializingLock.acquire();
				try {
					XmlUtil.serializeXml(doc, uriStoreWsp);
				} finally {
					serializingLock.release();
				}
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
			NodeList providerNodes = rootElement.getElementsByTagName(LanguageSettingsSerializable.ELEM_PROVIDER);

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
					if (provider instanceof LanguageSettingsSerializable) {
						LanguageSettingsSerializable lss = (LanguageSettingsSerializable) provider;

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
		try {
			// Document to store in project area
			Document docStorePrj = XmlUtil.newDocument();
			Element projectElementStorePrj = XmlUtil.appendElement(docStorePrj, ELEM_PROJECT);
			// Document to store in workspace area
			Document docStoreWsp = XmlUtil.newDocument();
			Element projectElementStoreWsp = XmlUtil.appendElement(docStoreWsp, ELEM_PROJECT);

			serializeLanguageSettingsInternal(projectElementStorePrj, projectElementStoreWsp, prjDescription);

			IFile fileStorePrj = getStoreInProjectArea(project);
			// The project store should not be absent. Absent store means legacy project, not 0 providers.
			XmlUtil.serializeXml(docStorePrj, fileStorePrj);

			URI uriStoreWsp = null;
			boolean isWorkspaceStoreEmpty = projectElementStoreWsp.getChildNodes().getLength() == 0;
			uriStoreWsp = getStoreInWorkspaceArea(project.getName()+'.'+STORAGE_WORKSPACE_LANGUAGE_SETTINGS);
			serializingLock.acquire();
			try {
				// project-specific location in workspace area
				if (!isWorkspaceStoreEmpty) {
					XmlUtil.serializeXml(docStoreWsp, uriStoreWsp);
				} else {
					new java.io.File(uriStoreWsp).delete();
				}
			} finally {
				serializingLock.release();
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
							if (provider instanceof LanguageSettingsSerializable) {
								LanguageSettingsSerializable lss = (LanguageSettingsSerializable) provider;
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
			if (cfgDescription!=null)
				cfgDescription.setLanguageSettingProviders(providers);
		}
	}

	private static void loadProviderEntries(LanguageSettingsSerializable provider, String cfgId, Element projectElementWsp) {
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

		if (provider instanceof LanguageSettingsSerializable)
			((LanguageSettingsSerializable)provider).load((Element) providerNode);

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
					ILanguageSettingsProvider userProvider = getWorkspaceProvider(ScannerDiscoveryLegacySupport.MBS_LANGUAGE_SETTINGS_PROVIDER);
					providers.add(userProvider);
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

	private static boolean isListenerInTheListOfAssociations(Collection<ListenerAssociation> list, ICListenerRegisterer element) {
		// list.contains(element) won't do it as we are interested in exact object, not in equal object
		for (ListenerAssociation la : list) {
			if (la.listener == element)
				return true;
		}
		return false;
	}

	/**
	 * Get a providers list including only providers of type ICListenerRegisterer
	 * for a given project description - collecting from all configurations.
	 */
	private static List<ICListenerRegisterer> getListeners(ICProjectDescription prjDescription) {
		List<ICListenerRegisterer> listeners = new ArrayList<ICListenerRegisterer>();
		if (prjDescription != null) {
			for (ICConfigurationDescription cfgDescription : prjDescription.getConfigurations()) {
				List<ILanguageSettingsProvider> providers = cfgDescription.getLanguageSettingProviders();
				for (ILanguageSettingsProvider provider : providers) {
					if (provider instanceof ICListenerRegisterer) {
						ICListenerRegisterer listener = (ICListenerRegisterer) provider;
						if (!isObjectInTheList(listeners, listener)) {
							listeners.add(listener);
						}
					}
				}
			}
		}
		return listeners;
	}
	
	private static List<ICListenerRegisterer> selectListeners(Collection<ILanguageSettingsProvider> values) {
		List<ICListenerRegisterer> listeners = new ArrayList<ICListenerRegisterer>();
		for (ILanguageSettingsProvider provider : values) {
			if (provider instanceof ICListenerRegisterer)
				listeners.add((ICListenerRegisterer) provider);
		}
		return listeners;
	}

	/**
	 * Get a providers list including only providers of type IResourceChangeListener
	 * for a given project description - collecting from all configurations.
	 */
	private static List<ListenerAssociation> getListenersAssociations(ICProjectDescription prjDescription) {
		List<ListenerAssociation> associations = new ArrayList<ListenerAssociation>();
		if (prjDescription != null) {
			for (ICConfigurationDescription cfgDescription : prjDescription.getConfigurations()) {
				List<ILanguageSettingsProvider> providers = cfgDescription.getLanguageSettingProviders();
				List<ICListenerRegisterer> listeners = selectListeners(providers);
				for (ICListenerRegisterer listener : listeners) {
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
	 * The method is used when project description is applied to workspace.
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
		
		List<ICListenerRegisterer> oldListeners = getListeners(oldPrjDescription);
		List<ListenerAssociation> newAssociations = getListenersAssociations(newPrjDescription);
		
		for (ICListenerRegisterer oldListener : oldListeners) {
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
			ICListenerRegisterer newListener = newListenerAssociation.listener;
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
					IStatus status = new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, "Not able to clone provider " + provider.getClass());
					CCorePlugin.log(new CoreException(status));
				}
			}
			newProviders.add(provider);
		}
		return new ArrayList<ILanguageSettingsProvider>(newProviders);
	}
	
}

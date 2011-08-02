package org.eclipse.cdt.internal.core.language.settings.providers;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsProvider;
import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsSerializable;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ILanguageSettingsEditableProvider;
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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class LanguageSettingsProvidersSerializer {

	private static final String STORAGE_WORKSPACE_LANGUAGE_SETTINGS = "language.settings.xml"; //$NON-NLS-1$
	private static final String SETTINGS_FOLDER_NAME = ".settings/"; //$NON-NLS-1$
	private static final String STORAGE_PROJECT_LANGUAGE_SETTINGS = "language.settings.xml"; //$NON-NLS-1$
	public static final char PROVIDER_DELIMITER = ';';
	private static final String MBS_LANGUAGE_SETTINGS_PROVIDER = "org.eclipse.cdt.managedbuilder.core.LanguageSettingsProvider";
	private static final String ELEM_PLUGIN = "plugin"; //$NON-NLS-1$
	private static final String ELEM_EXTENSION = "extension"; //$NON-NLS-1$
	private static final String ATTR_POINT = "point"; //$NON-NLS-1$
	private static final String ELEM_PROJECT = "project"; //$NON-NLS-1$
	private static final String ELEM_CONFIGURATION = "configuration"; //$NON-NLS-1$
	private static final String ELEM_PROVIDER_REFERENCE = "provider-reference"; //$NON-NLS-1$
	/** Cache of globally available providers to be consumed by calling clients */
	private static Map<String, ILanguageSettingsProvider> rawGlobalWorkspaceProviders = new HashMap<String, ILanguageSettingsProvider>();
	private static Object serializingLock = new Object();
	
	private static class LanguageSettingsWorkspaceProvider implements ILanguageSettingsProvider {
		private String providerId;

		public LanguageSettingsWorkspaceProvider(String id) {
			Assert.isNotNull(id);
			Assert.isTrue(id.length()>0);
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
	}

	
	/** static initializer */
	static {
		try {
			loadLanguageSettingsWorkspace();
		} catch (Throwable e) {
			CCorePlugin.log("Error loading workspace language settings providers", e); //$NON-NLS-1$
		} finally {
		}
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
		
		if (providers!=null) {
			List<ILanguageSettingsProvider> rawProviders = new ArrayList<ILanguageSettingsProvider>();
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
		
		rawGlobalWorkspaceProviders = rawWorkspaceProviders;
	}

	/**
	 * TODO: refactor with ErrorParserManager
	 *
	 * @param store - name of the store
	 * @return location of the store in the plug-in state area
	 */
	private static URI getStoreLocation(String store) {
		IPath location = CCorePlugin.getDefault().getStateLocation().append(store);
		URI uri = URIUtil.toURI(location);
		return uri;
	}

	public static void serializeLanguageSettingsWorkspace() throws CoreException {
		URI uriLocation = getStoreLocation(STORAGE_WORKSPACE_LANGUAGE_SETTINGS);
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
				java.io.File file = new java.io.File(uriLocation);
				synchronized (serializingLock) {
					file.delete();
				}
				return;
			}
	
			Document doc = XmlUtil.newDocument();
			Element rootElement = XmlUtil.appendElement(doc, ELEM_PLUGIN);
			Element elementExtension = XmlUtil.appendElement(rootElement, ELEM_EXTENSION, new String[] {ATTR_POINT, LanguageSettingsExtensionManager.PROVIDER_EXTENSION_FULL_ID});
	
			for (LanguageSettingsSerializable provider : serializableExtensionProviders) {
				provider.serialize(elementExtension);
			}
	
			synchronized (serializingLock) {
				XmlUtil.serializeXml(doc, uriLocation);
			}
	
		} catch (Exception e) {
			CCorePlugin.log("Internal error while trying to serialize language settings", e); //$NON-NLS-1$
			IStatus s = new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, "Internal error while trying to serialize language settings", e);
			throw new CoreException(s);
		}
	}

	public static void loadLanguageSettingsWorkspace() throws CoreException {
		List <ILanguageSettingsProvider> providers = null;
		
		URI uriLocation = getStoreLocation(STORAGE_WORKSPACE_LANGUAGE_SETTINGS);
	
		Document doc = null;
		try {
			synchronized (serializingLock) {
				doc = XmlUtil.loadXml(uriLocation);
			}
		} catch (Exception e) {
			CCorePlugin.log("Can't load preferences from file "+uriLocation, e); //$NON-NLS-1$
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

	public static void serializeLanguageSettings(Element parentElement, ICProjectDescription prjDescription) throws CoreException {
		ICConfigurationDescription[] cfgDescriptions = prjDescription.getConfigurations();
		for (ICConfigurationDescription cfgDescription : cfgDescriptions) {
			Element elementConfiguration = XmlUtil.appendElement(parentElement, ELEM_CONFIGURATION, new String[] {
					LanguageSettingsExtensionManager.ATTR_ID, cfgDescription.getId(),
					LanguageSettingsExtensionManager.ATTR_NAME, cfgDescription.getName(),
				});
			List<ILanguageSettingsProvider> providers = cfgDescription.getLanguageSettingProviders();
			if (providers.size()>0) {
				Element elementExtension = XmlUtil.appendElement(elementConfiguration, ELEM_EXTENSION, new String[] {
						ATTR_POINT, LanguageSettingsExtensionManager.PROVIDER_EXTENSION_FULL_ID});
				for (ILanguageSettingsProvider provider : providers) {
					if (isWorkspaceProvider(provider)) {
						// Element elementProviderReference =
						XmlUtil.appendElement(elementExtension, ELEM_PROVIDER_REFERENCE, new String[] {
								LanguageSettingsExtensionManager.ATTR_ID, provider.getId()});
						continue;
					}
					if (provider instanceof LanguageSettingsSerializable) {
						((LanguageSettingsSerializable) provider).serialize(elementExtension);
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

	private static IFile getStorage(IProject project) throws CoreException {
		IFolder folder = project.getFolder(SETTINGS_FOLDER_NAME);
		if (!folder.exists()) {
			folder.create(true, true, null);
		}
		IFile storage = folder.getFile(STORAGE_PROJECT_LANGUAGE_SETTINGS);
		return storage;
	}

	public static void serializeLanguageSettings(ICProjectDescription prjDescription) throws CoreException {
		IProject project = prjDescription.getProject();
		try {
			Document doc = XmlUtil.newDocument();
			Element rootElement = XmlUtil.appendElement(doc, ELEM_PROJECT);
			serializeLanguageSettings(rootElement, prjDescription);
	
			IFile file = getStorage(project);
			synchronized (serializingLock){
				XmlUtil.serializeXml(doc, file);
			}
	
		} catch (Exception e) {
			IStatus s = new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, "Internal error while trying to serialize language settings", e);
			CCorePlugin.log(s);
			throw new CoreException(s);
		}
	}

	public static void loadLanguageSettings(Element parentElement, ICProjectDescription prjDescription) {
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
		NodeList configurationNodes = parentElement.getChildNodes();
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

	private static ILanguageSettingsProvider loadProvider(Node providerNode) {
		String attrClass = XmlUtil.determineAttributeValue(providerNode, LanguageSettingsExtensionManager.ATTR_CLASS);
		ILanguageSettingsProvider provider = LanguageSettingsExtensionManager.getProviderInstance(attrClass);
		
		if (provider instanceof LanguageSettingsSerializable)
			((LanguageSettingsSerializable)provider).load((Element) providerNode);

		return provider;
	}

	public static void loadLanguageSettings(ICProjectDescription prjDescription) {
		IProject project = prjDescription.getProject();
		IFile file = project.getFile(SETTINGS_FOLDER_NAME+STORAGE_PROJECT_LANGUAGE_SETTINGS);
		// AG: FIXME not sure about that one
		// Causes java.lang.IllegalArgumentException: Attempted to beginRule: P/cdt312, does not match outer scope rule: org.eclipse.cdt.internal.ui.text.c.hover.CSourceHover$SingletonRule@6f34fb
		try {
			file.refreshLocal(IResource.DEPTH_ZERO, null);
		} catch (CoreException e) {
			// ignore failure
		}
		if (file.exists() && file.isAccessible()) {
			Document doc = null;
			try {
				synchronized (serializingLock) {
					doc = XmlUtil.loadXml(file);
				}
				Element rootElement = doc.getDocumentElement(); // <project/>
				loadLanguageSettings(rootElement, prjDescription);
			} catch (Exception e) {
				CCorePlugin.log("Can't load preferences from file "+file.getLocation(), e); //$NON-NLS-1$
			}
	
			if (doc!=null) {
			}
	
		} else {
			// Already existing legacy projects
			ICConfigurationDescription[] cfgDescriptions = prjDescription.getConfigurations();
			for (ICConfigurationDescription cfgDescription : cfgDescriptions) {
				if (cfgDescription!=null) {
					List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>(2);
					ILanguageSettingsProvider userProvider = getWorkspaceProvider(MBS_LANGUAGE_SETTINGS_PROVIDER);
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
		return new LanguageSettingsWorkspaceProvider(id);
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
			workspaceProviders.add(new LanguageSettingsWorkspaceProvider(rawProvider.getId()));
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
}
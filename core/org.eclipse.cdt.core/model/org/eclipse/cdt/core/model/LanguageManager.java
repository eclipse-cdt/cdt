/*******************************************************************************
 * Copyright (c) 2005, 2011 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Doug Schaefer (QNX) - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     IBM Corporation - Language management feature (see Bugzilla 151850)
 *******************************************************************************/
package org.eclipse.cdt.core.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.language.ProjectLanguageConfiguration;
import org.eclipse.cdt.core.language.WorkspaceLanguageConfiguration;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.internal.core.CContentTypes;
import org.eclipse.cdt.internal.core.language.LanguageMappingResolver;
import org.eclipse.cdt.internal.core.language.LanguageMappingStore;
import org.eclipse.cdt.internal.core.model.LanguageDescriptor;
import org.eclipse.cdt.internal.core.model.TranslationUnit;
import org.eclipse.cdt.internal.core.pdom.dom.IPDOMLinkageFactory;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager;

/**
 * @noextend This interface is not intended to be extended by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class LanguageManager {
	private static final String NAMESPACE_SEPARATOR = "."; //$NON-NLS-1$
	private static final String LANGUAGE_EXTENSION_POINT_ID = "org.eclipse.cdt.core.language"; //$NON-NLS-1$
	private static final String ELEMENT_LANGUAGE = "language"; //$NON-NLS-1$
	private static final String ELEMENT_CONTENT_TYPE = "contentType"; //$NON-NLS-1$
	private static final String ELEMENT_PDOM_LINKAGE_FACTORY = "pdomLinkageFactory"; //$NON-NLS-1$
	private static final String ATTRIBUTE_CLASS = "class"; //$NON-NLS-1$
	private static final String ATTRIBUTE_ID = "id"; //$NON-NLS-1$

	private static LanguageManager instance;
	private Map<String, ILanguage> fLanguageCache = new HashMap<String, ILanguage>();
	private Map<String, IPDOMLinkageFactory> fPDOMLinkageFactoryCache= new HashMap<String, IPDOMLinkageFactory>();
	private Map<String, ILanguage> fContentTypeToLanguageCache= new HashMap<String, ILanguage>();
	private Map<IProject, ProjectLanguageConfiguration> fLanguageConfigurationCache = new HashMap<IProject, ProjectLanguageConfiguration>();
	private boolean fIsFullyCached;
	private HashMap<String, ILanguageDescriptor> fIdToLanguageDescriptorCache;//= new HashMap();
	private HashMap<String, List<ILanguageDescriptor>> fContentTypeToDescriptorListCache;
	private ListenerList fLanguageChangeListeners = new ListenerList(ListenerList.IDENTITY);
	private WorkspaceLanguageConfiguration fWorkspaceMappings;

	public static LanguageManager getInstance() {
		if (instance == null)
			instance = new LanguageManager();
		return instance;
	}

	public ILanguageDescriptor getLanguageDescriptor(String id) {
		Map<String, ILanguageDescriptor> map = getDescriptorCache();
		return map.get(id);
	}

	private HashMap<String, ILanguageDescriptor> getDescriptorCache() {
		if (fIdToLanguageDescriptorCache == null) {
			fIdToLanguageDescriptorCache = createDescriptorCache();
		}
		return fIdToLanguageDescriptorCache;
	}

	public ILanguageDescriptor[] getLanguageDescriptors() {
		HashMap<String, ILanguageDescriptor> map = getDescriptorCache();
		return map.values().toArray(new ILanguageDescriptor[map.size()]);
	}

	private HashMap<String, ILanguageDescriptor> createDescriptorCache() {
		HashMap<String, ILanguageDescriptor> map = new HashMap<String, ILanguageDescriptor>();
		IConfigurationElement[] configs= Platform.getExtensionRegistry().getConfigurationElementsFor(LANGUAGE_EXTENSION_POINT_ID);
		for (int j = 0; j < configs.length; ++j) {
			final IConfigurationElement languageElem = configs[j];
			if (ELEMENT_LANGUAGE.equals(languageElem.getName())) {
				LanguageDescriptor lDes = new LanguageDescriptor(languageElem);
				map.put(lDes.getId(), lDes);
			}
		}
		return map;
	}

	private HashMap<String, List<ILanguageDescriptor>> getContentTypeToDescriptorCache() {
		if (fContentTypeToDescriptorListCache == null) {
			fContentTypeToDescriptorListCache = createContentTypeToDescriptorCache();
		}
		return fContentTypeToDescriptorListCache;
	}

	public Map<String, ILanguageDescriptor[]> getContentTypeIdToLanguageDescriptionsMap() {
		HashMap<String, ILanguageDescriptor[]> map = new HashMap<String, ILanguageDescriptor[]>();
		Map<String, List<ILanguageDescriptor>> cache = getContentTypeToDescriptorCache();

		for (Entry<String, List<ILanguageDescriptor>> entry : cache.entrySet()) {
			List<ILanguageDescriptor> list = entry.getValue();
			if (list.size() > 0) {
				ILanguageDescriptor[] dess = list.toArray(new ILanguageDescriptor[list.size()]);
				map.put(entry.getKey(), dess);
			}
		}

		return map;
	}


	private HashMap<String, List<ILanguageDescriptor>> createContentTypeToDescriptorCache() {
		HashMap<String, List<ILanguageDescriptor>> map = new HashMap<String, List<ILanguageDescriptor>>();
		Map<String, ILanguageDescriptor> dc = getDescriptorCache();

		List<ILanguageDescriptor> list;
		String id;
		for (ILanguageDescriptor des : dc.values()) {
			IContentType types[] = des.getContentTypes();
			for (IContentType type : types) {
				id = type.getId();
				list = map.get(id);
				if (list == null) {
					list = new ArrayList<ILanguageDescriptor>();
					map.put(id, list);
				}
				list.add(des);
			}
		}
		return map;
	}

	public ILanguage getLanguage(String id) {
		ILanguage language = fLanguageCache.get(id);
		if (language != null)
			return language;

		IConfigurationElement[] configs= Platform.getExtensionRegistry().getConfigurationElementsFor(LANGUAGE_EXTENSION_POINT_ID);
		for (int j = 0; j < configs.length; ++j) {
			final IConfigurationElement languageElem = configs[j];
			if (ELEMENT_LANGUAGE.equals(languageElem.getName())) {
				String langId = getLanguageID(languageElem);
				if (langId.equals(id)) {
					final ILanguage[] result= new ILanguage[]{null};
					SafeRunner.run(new ISafeRunnable() {
						@Override
						public void handleException(Throwable exception) {
							CCorePlugin.log(exception);
						}

						@Override
						public void run() throws Exception {
							result[0]= (ILanguage)languageElem.createExecutableExtension(ATTRIBUTE_CLASS);
						}
					});
					if (result[0] != null) {
						fLanguageCache.put(id, result[0]);
						return result[0];
					}
				}
			}
		}
		return null;
	}

	private String getLanguageID(final IConfigurationElement languageElem) {
		return languageElem.getNamespaceIdentifier() + NAMESPACE_SEPARATOR + languageElem.getAttribute(ATTRIBUTE_ID);
	}

	public ILanguage getLanguage(IContentType contentType) {
		String contentTypeID= contentType.getId();
		return getLanguageForContentTypeID(contentTypeID);
	}

	public ILanguage getLanguageForContentTypeID(String contentTypeID) {
		cacheAllLanguages();

		ILanguage language = fContentTypeToLanguageCache.get(contentTypeID);
		if (language != null || fContentTypeToLanguageCache.containsKey(contentTypeID))
			return language;

		IConfigurationElement[] configs= Platform.getExtensionRegistry().getConfigurationElementsFor(LANGUAGE_EXTENSION_POINT_ID);
		for (int j = 0; j < configs.length; ++j) {
			final IConfigurationElement languageElem = configs[j];
			if (ELEMENT_LANGUAGE.equals(languageElem.getName())) {
				IConfigurationElement[] assocContentTypes = languageElem.getChildren(ELEMENT_CONTENT_TYPE);
				for (int k = 0; k < assocContentTypes.length; ++k) {
					if (contentTypeID.equals(assocContentTypes[k].getAttribute(ATTRIBUTE_ID))) {
						String id= getLanguageID(languageElem);
						ILanguage lang= getLanguage(id);
						fContentTypeToLanguageCache.put(contentTypeID, lang);
						return lang;
					}
				}
			}
		}
		fContentTypeToLanguageCache.put(contentTypeID, null);
		return null;
	}

	/**
	 * @deprecated use getRegisteredContentTypes() instead.
	 */
	@Deprecated
	public ArrayList<String> getAllContentTypes() {
		ArrayList<String> allTypes = new ArrayList<String>();
		allTypes.add(CCorePlugin.CONTENT_TYPE_ASMSOURCE);
		allTypes.add(CCorePlugin.CONTENT_TYPE_CHEADER);
		allTypes.add(CCorePlugin.CONTENT_TYPE_CSOURCE);
		allTypes.add(CCorePlugin.CONTENT_TYPE_CXXHEADER);
		allTypes.add(CCorePlugin.CONTENT_TYPE_CXXSOURCE);

		IContentTypeManager manager = Platform.getContentTypeManager();
		IConfigurationElement[] configs= Platform.getExtensionRegistry().getConfigurationElementsFor(LANGUAGE_EXTENSION_POINT_ID);
		for (int j = 0; j < configs.length; ++j) {
			final IConfigurationElement languageElem = configs[j];
			if (ELEMENT_LANGUAGE.equals(languageElem.getName())) {
				IConfigurationElement[] contentTypes = languageElem.getChildren(ELEMENT_CONTENT_TYPE);
				for (int k = 0; k < contentTypes.length; ++k) {
					IContentType langContType = manager.getContentType(contentTypes[k].getAttribute(ATTRIBUTE_ID));
					allTypes.add(langContType.getId());
				}
			}
		}

		return allTypes;
	}

	/**
	 * Returns all content types that are registered with CDT.
	 * @since 3.1.1
	 */
	public String[] getRegisteredContentTypeIds() {
		Set<String> contentTypes= collectContentTypeIds();
		return contentTypes.toArray(new String[contentTypes.size()]);
	}

	private Set<String> collectContentTypeIds() {
		HashSet<String> allTypes = new HashSet<String>();
		allTypes.add(CCorePlugin.CONTENT_TYPE_ASMSOURCE);
		allTypes.add(CCorePlugin.CONTENT_TYPE_CHEADER);
		allTypes.add(CCorePlugin.CONTENT_TYPE_CSOURCE);
		allTypes.add(CCorePlugin.CONTENT_TYPE_CXXHEADER);
		allTypes.add(CCorePlugin.CONTENT_TYPE_CXXSOURCE);

		IContentTypeManager manager = Platform.getContentTypeManager();
		IConfigurationElement[] configs= Platform.getExtensionRegistry().getConfigurationElementsFor(LANGUAGE_EXTENSION_POINT_ID);
		for (int j = 0; j < configs.length; ++j) {
			final IConfigurationElement languageElem = configs[j];
			if (ELEMENT_LANGUAGE.equals(languageElem.getName())) {
				IConfigurationElement[] contentTypes = languageElem.getChildren(ELEMENT_CONTENT_TYPE);
				for (int k = 0; k < contentTypes.length; ++k) {
					IContentType langContType = manager.getContentType(contentTypes[k].getAttribute(ATTRIBUTE_ID));
					allTypes.add(langContType.getId());
				}
			}
		}

		return allTypes;
	}

	public boolean isContributedContentType(String contentTypeId) {
		return contentTypeId != null && getLanguageForContentTypeID(contentTypeId) != null;
	}

	/**
	 * @deprecated use {@link #getContributedModelBuilderFor(ITranslationUnit)}, instead.
	 * @noreference This method is not intended to be referenced by clients.
	 */
	@Deprecated
	public IContributedModelBuilder getContributedModelBuilderFor(TranslationUnit tu) {
		try {
			ILanguage lang = tu.getLanguage();
			return lang == null ? null : lang.createModelBuilder(tu);
		} catch (CoreException e) {
			return null;
		}
	}

	/**
	 * @since 5.1
	 */
	public IContributedModelBuilder getContributedModelBuilderFor(ITranslationUnit tu) {
		try {
			ILanguage lang = tu.getLanguage();
			return lang == null ? null : lang.createModelBuilder(tu);
		} catch (CoreException e) {
			return null;
		}
	}

	/**
	 * Returns mappings between IDs and IPDOMLinkageFactory. The IDs are defined in {@link ILinkage}.
	 * @return a map.
	 * @since 4.0
	 */
	public Map<String, IPDOMLinkageFactory> getPDOMLinkageFactoryMappings() {
		if (!fPDOMLinkageFactoryCache.isEmpty())
			return Collections.unmodifiableMap(fPDOMLinkageFactoryCache);

		fPDOMLinkageFactoryCache.clear();
		final IPDOMLinkageFactory[] result = new IPDOMLinkageFactory[] {null};

		// read configuration
		IConfigurationElement[] configs= Platform.getExtensionRegistry().getConfigurationElementsFor(LANGUAGE_EXTENSION_POINT_ID);
		for (final IConfigurationElement element : configs) {
			if (ELEMENT_PDOM_LINKAGE_FACTORY.equals(element.getName())) {
				SafeRunner.run(new ISafeRunnable() {
					@Override
					public void handleException(Throwable exception) {
						CCorePlugin.log(exception);
					}

					@Override
					public void run() throws Exception {
						result[0] = (IPDOMLinkageFactory) element.createExecutableExtension(ATTRIBUTE_CLASS);
					}}
				);
				fPDOMLinkageFactoryCache.put(element.getAttribute(ATTRIBUTE_ID), result[0]);
			}
		}
		return Collections.unmodifiableMap(fPDOMLinkageFactoryCache);
	}

	/**
	 * Returns all of the languages registered with the <code>Platform</code>.
	 * @return all of the languages registered with the <code>Platform</code>.
	 */
	public ILanguage[] getRegisteredLanguages() {
		cacheAllLanguages();
		ILanguage[] languages = new ILanguage[fLanguageCache.size()];
		Iterator<ILanguage> values = fLanguageCache.values().iterator();
		for (int i = 0; values.hasNext(); i++) {
			languages[i] = values.next();
		}
		return languages;
	}

	private void cacheAllLanguages() {
		if (fIsFullyCached) {
			return;
		}
		IConfigurationElement[] configs= Platform.getExtensionRegistry().getConfigurationElementsFor(LANGUAGE_EXTENSION_POINT_ID);
		for (int j = 0; j < configs.length; ++j) {
			final IConfigurationElement languageElem = configs[j];
			if (ELEMENT_LANGUAGE.equals(languageElem.getName())) {
				String langId = getLanguageID(languageElem);
				final ILanguage[] result= new ILanguage[] { null };
				SafeRunner.run(new ISafeRunnable() {
					@Override
					public void handleException(Throwable exception) {
						CCorePlugin.log(exception);
					}

					@Override
					public void run() throws Exception {
						result[0]= (ILanguage) languageElem.createExecutableExtension(ATTRIBUTE_CLASS);
					}
				});
				if (result[0] != null) {
					fLanguageCache.put(langId, result[0]);
				}
			}
		}
		fIsFullyCached = true;
	}

	/**
	 * Returns the language configuration for the workspace.
	 * @return the language configuration for the workspace
	 * @throws CoreException
	 * @since 4.0
	 */
	public WorkspaceLanguageConfiguration getWorkspaceLanguageConfiguration() throws CoreException {
		synchronized (this) {
			if (fWorkspaceMappings != null) {
				return fWorkspaceMappings;
			}

			LanguageMappingStore store = new LanguageMappingStore();
			fWorkspaceMappings = store.decodeWorkspaceMappings();
			return fWorkspaceMappings;
		}
	}

	/**
	 * Saves the workspace language configuration to persistent storage and notifies
	 * all <code>ILanguageMappingChangeListeners</code> of changes.
	 * @param affectedContentTypes
	 * @throws CoreException
	 * @since 4.0
	 */
	public void storeWorkspaceLanguageConfiguration(IContentType[] affectedContentTypes) throws CoreException {
		synchronized (this) {
			if (fWorkspaceMappings == null) {
				return;
			}

			LanguageMappingStore store = new LanguageMappingStore();
			store.storeMappings(fWorkspaceMappings);
		}

		// Notify listeners that the language mappings have changed.
		LanguageMappingChangeEvent event = new LanguageMappingChangeEvent();
		event.setType(ILanguageMappingChangeEvent.TYPE_WORKSPACE);
		event.setAffectedContentTypes(affectedContentTypes);
		notifyLanguageChangeListeners(event);
	}

	/**
	 * Returns the language configuration for the given project.
	 * @param project
	 * @return the language configuration for the given project
	 * @throws CoreException
	 * @since 4.0
	 */
	public ProjectLanguageConfiguration getLanguageConfiguration(IProject project) throws CoreException {
		synchronized (this) {
			ProjectLanguageConfiguration mappings = fLanguageConfigurationCache.get(project);
			if (mappings != null) {
				return mappings;
			}

			LanguageMappingStore store = new LanguageMappingStore();
			mappings = store.decodeMappings(project);
			fLanguageConfigurationCache.put(project, mappings);
			return mappings;
		}
	}

	/**
	 * Saves the language configuration for the given project to persistent
	 * storage and notifies all <code>ILanguageMappingChangeListeners</code>
	 * of changes.
	 * @param project
	 * @param affectedContentTypes
	 * @throws CoreException
	 * @since 4.0
	 */
	public void storeLanguageMappingConfiguration(IProject project, IContentType[] affectedContentTypes) throws CoreException {
		synchronized (this) {
			ProjectLanguageConfiguration mappings = fLanguageConfigurationCache.get(project);
			LanguageMappingStore store = new LanguageMappingStore();
			store.storeMappings(project, mappings);
		}

		// Notify listeners that the language mappings have changed.
		LanguageMappingChangeEvent event = new LanguageMappingChangeEvent();
		event.setType(ILanguageMappingChangeEvent.TYPE_PROJECT);
		event.setProject(project);
		event.setAffectedContentTypes(affectedContentTypes);
		notifyLanguageChangeListeners(event);
	}

	/**
	 * Returns an ILanguage representing the language to be used for the given file.
	 * @since 4.0
	 * @return an ILanguage representing the language to be used for the given file
	 * @param fullPathToFile the full path to the file for which the language is requested
	 * @param project the IProject that this file is in the context of.  This field cannot be null.
	 * @param configuration the active build configuration, or <code>null</code> if build configurations
	 *        are not relevant to determining the language.
	 * @throws CoreException
	 */
	public ILanguage getLanguageForFile(String fullPathToFile, IProject project, ICConfigurationDescription configuration) throws CoreException {
		if (project == null)
			throw new IllegalArgumentException("project must not be null in call to LanguageManager.getLanguageForFile(String, IProject)"); //$NON-NLS-1$

		IContentType contentType = CContentTypes.getContentType(project, fullPathToFile);

		if (contentType == null) {
			return null;
		}

		String contentTypeID = contentType.getId();

		return LanguageMappingResolver.computeLanguage(project, fullPathToFile, configuration, contentTypeID, false)[0].language;
	}

	/**
	 * Returns an ILanguage representing the language to be used for the given file.
	 * @return an ILanguage representing the language to be used for the given file
	 * @param pathToFile the path to the file for which the language is requested.
	 * The path can be either workspace or project relative.
	 * @param project the project that this file should be parsed in context of.  This field is optional and may
	 * be set to null.  If the project is null then this method tries to determine the project context via workspace APIs.
	 * @param configuration the active build configuration, or <code>null</code> if build configurations
	 *        are not relevant to determining the language.
	 * @throws CoreException
	 * @since 4.0
	 */
	public ILanguage getLanguageForFile(IPath pathToFile, IProject project, ICConfigurationDescription configuration) throws CoreException {
		return getLanguageForFile(pathToFile, project, configuration, null);
	}

	/**
	 * Returns an ILanguage representing the language to be used for the given file.
	 * @return an ILanguage representing the language to be used for the given file
	 * @param pathToFile the path to the file for which the language is requested.
	 * The path can be either workspace or project relative.
	 * @param project the project that this file should be parsed in context of.  This field is optional and may
	 * be set to null.  If the project is null then this method tries to determine the project context via workspace APIs.
	 * @param configuration the active build configuration, or <code>null</code> if build configurations
	 *        are not relevant to determining the language.
	 * @param contentTypeID id of the content type, may be <code>null</code>.
	 * @throws CoreException
	 * @since 4.0
	 */
	public ILanguage getLanguageForFile(IPath pathToFile, IProject project, ICConfigurationDescription configuration, String contentTypeID) throws CoreException {
		if (project == null) {
			IResource resource = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(pathToFile);
			if (resource == null) {
				return null;
			}
			project= resource.getProject();
		}
		if (contentTypeID==null) {
			IContentType ct= CContentTypes.getContentType(project, pathToFile.toString());
			if (ct == null) {
				return null;
			}
			contentTypeID= ct.getId();
		}

		return LanguageMappingResolver.computeLanguage(project, pathToFile.toPortableString(), configuration, contentTypeID, false)[0].language;
	}

	/**
	 * Returns an ILanguage representing the language to be used for the given file.
	 * @return an ILanguage representing the language to be used for the given file
	 * @param file the file for which the language is requested
	 * @param configuration the active build configuration, or <code>null</code> if build configurations
	 *        are not relevant to determining the language.
	 * @throws CoreException
	 * @since 4.0
	 */
	public ILanguage getLanguageForFile(IFile file, ICConfigurationDescription configuration) throws CoreException {
		return getLanguageForFile(file, configuration, null);
	}

	/**
	 * Returns an ILanguage representing the language to be used for the given file.
	 * @return an ILanguage representing the language to be used for the given file
	 * @param file the file for which the language is requested
	 * @param configuration the active build configuration, or <code>null</code> if build configurations
	 *        are not relevant to determining the language.
	 * @param contentTypeId id of the content type, may be <code>null</code>.
	 * @throws CoreException
	 * @since 4.0
	 */
	public ILanguage getLanguageForFile(IFile file, ICConfigurationDescription configuration,
			String contentTypeId) throws CoreException {
		IProject project = file.getProject();

		if (contentTypeId == null) {
			IPath location = file.getLocation();
			String filename;
			if (location != null) {
				filename = location.toString();
			} else {
				filename = file.getName();
			}
			IContentType contentType= CContentTypes.getContentType(project, filename);
			if (contentType == null) {
				return null;
			}
			contentTypeId= contentType.getId();
		}

		return LanguageMappingResolver.computeLanguage(project, file.getProjectRelativePath().toPortableString(), configuration, contentTypeId, false)[0].language;
	}

	/**
	 * Adds a listener that will be notified of changes in language mappings.
	 *
	 * @param listener the ILanguageMappingChangeListener to add
	 */
	public void registerLanguageChangeListener(ILanguageMappingChangeListener listener) {
		fLanguageChangeListeners.add(listener);
	}

	/**
	 * Removes a language mapping change listener.
	 *
	 * @param listener the ILanguageMappingChangeListener to remove.
	 */
	public void unregisterLanguageChangeListener(ILanguageMappingChangeListener listener) {
		fLanguageChangeListeners.remove(listener);
	}

	/**
	 * Notifies all language mappings change listeners of a change in the mappings.
	 *
	 * @param event the ILanguageMappingsChange event to be broadcast.
	 */
	public void notifyLanguageChangeListeners(ILanguageMappingChangeEvent event) {
		Object[] listeners = fLanguageChangeListeners.getListeners();

		for (Object obj : listeners) {
			ILanguageMappingChangeListener listener = (ILanguageMappingChangeListener) obj;
			listener.handleLanguageMappingChangeEvent(event);
		}
	}

	/**
	 * Saves the language configuration for the given file to persistent
	 * storage and notifies all <code>ILanguageMappingChangeListeners</code>
	 * of changes.
	 * @param file
	 * @throws CoreException
	 * @since 4.0
	 */
	public void storeLanguageMappingConfiguration(IFile file) throws CoreException {
		IProject project = file.getProject();
		synchronized (this) {
			ProjectLanguageConfiguration mappings = fLanguageConfigurationCache.get(project);
			LanguageMappingStore store = new LanguageMappingStore();
			store.storeMappings(project, mappings);
		}

		// Notify listeners that the language mappings have changed.
		LanguageMappingChangeEvent event = new LanguageMappingChangeEvent();
		event.setType(ILanguageMappingChangeEvent.TYPE_FILE);
		event.setProject(project);
		event.setFile(file);
		notifyLanguageChangeListeners(event);
	}

	/**
	 * Returns language binding to a particular content type for given project.
	 * This method will check project settings, workspace settings and default
	 * bindings (in that order)
	 *
	 * @param contentType content type of the file
	 * @param project C/C++ workspace project
	 * @return CDT language object
	 * @since 5.4
	 */
	public ILanguage getLanguage(IContentType contentType, IProject project) {
		return getLanguage(contentType, project, null);
	}

	/**
	 * Returns language binding to a particular content type for given project.
	 * This method will check project settings, workspace settings and default
	 * bindings (in that order)
	 *
	 * @param contentType content type of the file
	 * @param project C/C++ workspace project
	 * @param configurationDescription build configuration or <code>null</code>
	 * @return CDT language object
	 * @since 5.4
	 */
	public ILanguage getLanguage(IContentType contentType,
			IProject project, ICConfigurationDescription configurationDescription) {
		try {
			final ProjectLanguageConfiguration projectConfig = getLanguageConfiguration(project);
			final String contentTypeId = contentType.getId();
			String langId = projectConfig.getLanguageForContentType(configurationDescription,
					contentTypeId);
			if (langId == null) {
				WorkspaceLanguageConfiguration wsConfig = getWorkspaceLanguageConfiguration();
				langId = wsConfig.getLanguageForContentType(contentTypeId);
			}
			return langId != null ? getLanguage(langId) : getLanguage(contentType);
		} catch (CoreException e) {
			// Fall through to default language mapping
		}
		return getLanguage(contentType);
	}
}

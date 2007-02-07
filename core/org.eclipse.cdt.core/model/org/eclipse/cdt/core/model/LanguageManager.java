/*******************************************************************************
 * Copyright (c) 2005, 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/

package org.eclipse.cdt.core.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.internal.core.CContentTypes;
import org.eclipse.cdt.internal.core.language.LanguageMappingConfiguration;
import org.eclipse.cdt.internal.core.language.LanguageMappingStore;
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
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager;

/**
 * @author Doug Schaefer
 *
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
	private Map fLanguageCache = new HashMap();
	private Map fPDOMLinkageFactoryCache= new HashMap();
	private Map fContentTypeToLanguageCache= new HashMap();
	private Map fLanguageConfigurationCache = new HashMap();
	private boolean fIsFullyCached;
	
	public static LanguageManager getInstance() {
		if (instance == null)
			instance = new LanguageManager();
		return instance;
	}
	
	public ILanguage getLanguage(String id) {
		ILanguage language = (ILanguage)fLanguageCache.get(id);
		if (language != null)
			return language;

		IConfigurationElement[] configs= Platform.getExtensionRegistry().getConfigurationElementsFor(LANGUAGE_EXTENSION_POINT_ID);
		for (int j = 0; j < configs.length; ++j) {
			final IConfigurationElement languageElem = configs[j];
			if (ELEMENT_LANGUAGE.equals(languageElem.getName())) {
				String langId = getLanguageID(languageElem);  
				if (langId.equals(id)) {
					final ILanguage[] result= new ILanguage[]{null};
					SafeRunner.run(new ISafeRunnable(){
						public void handleException(Throwable exception) {
							CCorePlugin.log(exception);
						}

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
	
	private ILanguage getLanguageForContentTypeID(String contentTypeID) {
		cacheAllLanguages();
		
		ILanguage language = (ILanguage)fContentTypeToLanguageCache.get(contentTypeID);
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
	public ArrayList/*<String>*/ getAllContentTypes() {
		ArrayList/*<String>*/ allTypes = new ArrayList();
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
		Set contentTypes= collectContentTypeIds();
		return (String[]) contentTypes.toArray(new String[contentTypes.size()]);
	}
	
	private Set collectContentTypeIds() {
		HashSet/*<String>*/ allTypes = new HashSet();
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
	
	public IContributedModelBuilder getContributedModelBuilderFor(TranslationUnit tu) {
		try {
			ILanguage lang = tu.getLanguage();
			return lang == null ? null : lang.createModelBuilder(tu);
		} catch (CoreException e) {
			return null;
		}
	}
	
	/**
	 * Returns a factory for the given linkage ID. The IDs are defined in {@link ILinkage}. 
	 * @param linkageID an ID for a linkage.
	 * @return a factory or <code>null</code>.
	 * @since 4.0
	 */
	public IPDOMLinkageFactory getPDOMLinkageFactory(String linkageID) {
		final IPDOMLinkageFactory[] result= new IPDOMLinkageFactory[] {null}; 
		result[0]= (IPDOMLinkageFactory) fPDOMLinkageFactoryCache.get(linkageID);

		if (result[0] == null) {
			// read configuration
			IConfigurationElement[] configs= Platform.getExtensionRegistry().getConfigurationElementsFor(LANGUAGE_EXTENSION_POINT_ID);
			for (int i = 0; result[0] == null && i < configs.length; i++) {
				final IConfigurationElement element = configs[i];
				if (ELEMENT_PDOM_LINKAGE_FACTORY.equals(element.getName())) {
					if (linkageID.equals(element.getAttribute(ATTRIBUTE_ID))) {
						SafeRunner.run(new ISafeRunnable(){
							public void handleException(Throwable exception) {
								CCorePlugin.log(exception);
							}

							public void run() throws Exception {
								result[0]= (IPDOMLinkageFactory) element.createExecutableExtension(ATTRIBUTE_CLASS);
							}}
						);
					}
				}			
			}
			fPDOMLinkageFactoryCache.put(linkageID, result[0]);
		}
		return result[0];
	}
	
	public ILanguage[] getRegisteredLanguages() {
		cacheAllLanguages();
		ILanguage[] languages = new ILanguage[fLanguageCache.size()];
		Iterator values = fLanguageCache.values().iterator();
		for (int i = 0; values.hasNext(); i++) {
			languages[i] = (ILanguage) values.next();
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
				final ILanguage[] result= new ILanguage[]{null};
				SafeRunner.run(new ISafeRunnable(){
					public void handleException(Throwable exception) {
						CCorePlugin.log(exception);
					}

					public void run() throws Exception {
						result[0]= (ILanguage)languageElem.createExecutableExtension(ATTRIBUTE_CLASS);
					}
				});
				if (result[0] != null) {
					fLanguageCache.put(langId, result[0]);
				}
			}
		}
		fIsFullyCached = true;
	}
	
	public LanguageMappingConfiguration getLanguageMappingConfiguration(IProject project) throws CoreException {
		LanguageMappingConfiguration mappings = (LanguageMappingConfiguration) fLanguageConfigurationCache.get(project);
		if (mappings != null) {
			return mappings;
		}
		
		LanguageMappingStore store = new LanguageMappingStore(project);
		mappings = store.decodeMappings();
		fLanguageConfigurationCache.put(project, mappings);
		return mappings;
	}
	
	public void storeLanguageMappingConfiguration(IProject project) throws CoreException {
		LanguageMappingConfiguration mappings = (LanguageMappingConfiguration) fLanguageConfigurationCache.get(project);
		LanguageMappingStore store = new LanguageMappingStore(project);
		store.storeMappings(mappings);
	}
	
	/**
	 * @since 4.0
	 * @return an ILanguage representing the language to be used for the given file
	 * @param fullPathToFile the full path to the file for which the language is requested
	 * @param project the IProject that this file is in the context of.  This field cannot be null.
	 * @throws CoreException 
	 * TODO:  implement other mapping levels besides project level and content type level
	 */
	public ILanguage getLanguageForFile(String fullPathToFile, IProject project) throws CoreException {
		
		if(project == null)
			throw new IllegalArgumentException("project must not be null in call to LanguageManager.getLanguageForFile(String, IProject)");
		
		IContentType contentType = CContentTypes.getContentType(project, fullPathToFile);
		
		if(contentType == null)
		{
			return null;
		}
		
		String contentTypeID = contentType.getId();
		
		// TODO: other mappings would go here
		
		// Project-level mappings
		LanguageMappingConfiguration mappings = getLanguageMappingConfiguration(project);
		if (mappings != null) {
			String id = (String) mappings.getProjectMappings().get(contentTypeID);
			if (id != null) {
				return getLanguage(id);
			}
		}
		
		// Content type mappings
		return getLanguageForContentTypeID(contentTypeID);
	}

	/**
	 * @since 4.0
	 * @return an ILanguage representing the language to be used for the given file
	 * @param pathToFile the path to the file for which the language is requested.
	 * The path can be either workspace or project relative.
	 * @param project the project that this file should be parsed in context of.  This field is optional and may
	 * be set to null.  If the project is null then this method tries to determine the project context via workspace APIs.
	 * @throws CoreException
	 * * TODO:  implement other mapping levels besides project level and content type level 
	 */
	public ILanguage getLanguageForFile(IPath pathToFile, IProject project) throws CoreException {
		IResource resource = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(pathToFile);
		
			
		IContentType contentType = CContentTypes.getContentType(project, pathToFile.toString());
		
		if(contentType == null)
		{
			return null;
		}
		
		String contentTypeID = contentType.getId();
				
		// if we don't have a project but have an IResource then we can infer the project
		if (project == null && resource != null) {
			project = ResourcesPlugin.getWorkspace().getRoot().getProject(
					pathToFile.segment(0));

		}
		
		// TODO: other mappings would go here
		
		// Project-level mappings
		LanguageMappingConfiguration mappings = getLanguageMappingConfiguration(project);
		if (mappings != null) {
			String id = (String) mappings.getProjectMappings().get(contentTypeID);
			if (id != null) {
				return getLanguage(id);
			}
		}
		
		// Content type mappings
		return getLanguageForContentTypeID(contentTypeID);
	}

	/**
	 * @since 4.0
	 * @return an ILanguage representing the language to be used for the given file
	 * @param file the file for which the language is requested
	 * @throws CoreException
	 * TODO:  implement other mapping levels besides project level and content type level 
	 */
	public ILanguage getLanguageForFile(IFile file) throws CoreException {
		IProject project = file.getProject();
		
		IContentType contentType = CContentTypes.getContentType(project, file.getLocation().toString());
				
		// TODO: other mappings would go here
		
		// Project-level mappings
		LanguageMappingConfiguration mappings = getLanguageMappingConfiguration(project);
		if (mappings != null) {
			String id = (String) mappings.getProjectMappings().get(contentType.getId());
			if (id != null) {
				return getLanguage(id);
			}
		}
		
		// Content type mappings
		return getLanguageForContentTypeID(contentType.getId());
	}

}

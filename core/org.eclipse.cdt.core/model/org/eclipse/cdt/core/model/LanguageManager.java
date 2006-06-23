/*******************************************************************************
 * Copyright (c) 2005, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.core.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.internal.core.model.TranslationUnit;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager;

/**
 * @author Doug Schaefer
 *
 */
public class LanguageManager {

	private static LanguageManager instance;
	private Map cache = new HashMap();
	
	public static LanguageManager getInstance() {
		if (instance == null)
			instance = new LanguageManager();
		return instance;
	}
	
	public ILanguage getLanguage(String id) throws CoreException {
		ILanguage language = (ILanguage)cache.get(id);
		if (language != null)
			return language;
		IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint(CCorePlugin.PLUGIN_ID, ILanguage.KEY);
		IExtension[] extensions = point.getExtensions();
		for (int i = 0; i < extensions.length; ++i) {
			IExtension extension = extensions[i];
			IConfigurationElement[] languages = extension.getConfigurationElements();
			for (int j = 0; j < languages.length; ++j) {
				IConfigurationElement languageElem = languages[j];
				String langId = extension.getNamespace() + "." + languageElem.getAttribute("id"); //$NON-NLS-1$ $NON-NLS-2$
				if (langId.equals(id)) {
					language = (ILanguage)languageElem.createExecutableExtension("class"); //$NON-NLS-1$
					cache.put(id, language);
					return language;
				}
			}
		}
		
		return null;
	}
	
	public ILanguage getLanguage(IContentType contentType) throws CoreException {
		IContentTypeManager manager = Platform.getContentTypeManager(); 
		IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint(CCorePlugin.PLUGIN_ID, ILanguage.KEY);
		IExtension[] extensions = point.getExtensions();
		for (int i = 0; i < extensions.length; ++i) {
			IConfigurationElement[] languages = extensions[i].getConfigurationElements();
			for (int j = 0; j < languages.length; ++j) {
				IConfigurationElement language = languages[j];
				IConfigurationElement[] contentTypes = language.getChildren("contentType"); //$NON-NLS-1$
				for (int k = 0; k < contentTypes.length; ++k) {
					IContentType langContType = manager.getContentType(contentTypes[k].getAttribute("id")); //$NON-NLS-1$
					if (contentType.equals(langContType)) {
						return (ILanguage)language.createExecutableExtension("class"); //$NON-NLS-1$
					}
				}
			}
		}
		return null;
	}
	
	public ArrayList/*<String>*/ getAllContentTypes() {
		ArrayList/*<String>*/ allTypes = new ArrayList();
		allTypes.add(CCorePlugin.CONTENT_TYPE_ASMSOURCE);
		allTypes.add(CCorePlugin.CONTENT_TYPE_CHEADER);
		allTypes.add(CCorePlugin.CONTENT_TYPE_CSOURCE);
		allTypes.add(CCorePlugin.CONTENT_TYPE_CXXHEADER);
		allTypes.add(CCorePlugin.CONTENT_TYPE_CXXSOURCE);

		IContentTypeManager manager = Platform.getContentTypeManager(); 
		IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint(CCorePlugin.PLUGIN_ID, ILanguage.KEY);
		IExtension[] extensions = point.getExtensions();
		for (int i = 0; i < extensions.length; ++i) {
			IConfigurationElement[] languages = extensions[i].getConfigurationElements();
			for (int j = 0; j < languages.length; ++j) {
				IConfigurationElement language = languages[j];
				IConfigurationElement[] contentTypes = language.getChildren("contentType"); //$NON-NLS-1$
				for (int k = 0; k < contentTypes.length; ++k) {
					IContentType langContType = manager.getContentType(contentTypes[k].getAttribute("id")); //$NON-NLS-1$
					allTypes.add(langContType.getId());
				}
			}
		}
		
		return allTypes;
	}

	public boolean isContributedContentType(String contentTypeId) {
		return getAllContentTypes().contains(contentTypeId);
	}
	
	public IContributedModelBuilder getContributedModelBuilderFor(TranslationUnit tu) {
		try {
			ILanguage lang = tu.getLanguage();
			return lang == null ? null : lang.createModelBuilder(tu);
		} catch (CoreException e) {
			return null;
		}
	}
}

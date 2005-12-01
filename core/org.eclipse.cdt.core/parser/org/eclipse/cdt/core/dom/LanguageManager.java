/*******************************************************************************
 * Copyright (c) 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.core.dom;

import org.eclipse.cdt.core.CCorePlugin;
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
	
	public static LanguageManager getInstance() {
		if (instance == null)
			instance = new LanguageManager();
		return instance;
	}
	
	public ILanguage getLanguage(String id) throws CoreException {
		IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint(CCorePlugin.PLUGIN_ID, ILanguage.KEY);
		IExtension[] extensions = point.getExtensions();
		for (int i = 0; i < extensions.length; ++i) {
			IExtension extension = extensions[i];
			IConfigurationElement[] languages = extension.getConfigurationElements();
			for (int j = 0; j < languages.length; ++j) {
				IConfigurationElement language = languages[j];
				String langId = extension.getNamespace() + "." + language.getAttribute("id"); //$NON-NLS-1$ $NON-NLS-2$
				if (langId.equals(id))
					return (ILanguage)language.createExecutableExtension("class"); //$NON-NLS-1$
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
}

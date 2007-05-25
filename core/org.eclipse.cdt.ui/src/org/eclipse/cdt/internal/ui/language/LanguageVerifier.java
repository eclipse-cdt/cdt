/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.language;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;

import org.eclipse.cdt.core.language.ProjectLanguageConfiguration;
import org.eclipse.cdt.core.language.WorkspaceLanguageConfiguration;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.LanguageManager;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;

/**
 * Analyzes and repairs language mapping configurations.
 */
public class LanguageVerifier {
	
	public static Map computeAvailableLanguages() {
		ILanguage[] registeredLanguages = LanguageManager.getInstance().getRegisteredLanguages();
		Map languages = new TreeMap();
		for (int i = 0; i < registeredLanguages.length; i++) {
			languages.put(registeredLanguages[i].getId(), registeredLanguages[i]);
		}
		return languages;
	}
	
	public static String computeAffectedLanguages(Set missingLanguages) {
		Iterator languages = missingLanguages.iterator();
		StringBuffer buffer = new StringBuffer();
		while (languages.hasNext()) {
			buffer.append('\n');
			buffer.append(languages.next());
		}
		return buffer.toString();
	}

	public static Set removeMissingLanguages(ProjectLanguageConfiguration config, ICProjectDescription description, Map availableLanguages) {
		Set missingLanguages = new TreeSet();
		
		// Check file mappings
		Iterator fileConfigurationMappings = config.getFileMappings().entrySet().iterator();
		while (fileConfigurationMappings.hasNext()) {
			Entry entry = (Entry) fileConfigurationMappings.next();
			String path = (String) entry.getKey();
			Map configurationLanguageMappings = (Map) entry.getValue();
			Iterator mappings = configurationLanguageMappings.entrySet().iterator();
			while (mappings.hasNext()) {
				Entry mapping = (Entry) mappings.next();
				String configurationId = (String) mapping.getKey();
				String languageId = (String) mapping.getValue();
				if (!availableLanguages.containsKey(languageId)) {
					missingLanguages.add(languageId);
					ICConfigurationDescription configuration = description.getConfigurationById(configurationId);
					config.removeFileMapping(configuration, path);
				}
			}
		}
		
		// Check content type mappings
		Iterator configurationContentTypeMappings = config.getContentTypeMappings().entrySet().iterator();
		while (configurationContentTypeMappings.hasNext()) {
			Entry entry = (Entry) configurationContentTypeMappings.next();
			String configurationId = (String) entry.getKey();
			Map contentTypeLanguageMappings = (Map) entry.getValue();
			Iterator mappings = contentTypeLanguageMappings.entrySet().iterator();
			while (mappings.hasNext()) {
				Entry mapping = (Entry) mappings.next();
				String contentTypeId = (String) mapping.getKey();
				String languageId = (String) mapping.getValue();
				if (!availableLanguages.containsKey(languageId)) {
					missingLanguages.add(languageId);
					ICConfigurationDescription configuration = description.getConfigurationById(configurationId);
					config.removeContentTypeMapping(configuration, contentTypeId);
				}
			}
		}
		
		return missingLanguages;
	}

	public static Set removeMissingLanguages(WorkspaceLanguageConfiguration config, Map availableLanguages) {
		Set missingLanguages = new TreeSet();
		
		// Check content type mappings
		Iterator contentTypeMappings = config.getWorkspaceMappings().entrySet().iterator();
		while (contentTypeMappings.hasNext()) {
			Entry entry = (Entry) contentTypeMappings.next();
			String contentTypeId = (String) entry.getKey();
			String languageId = (String) entry.getValue();
			if (!availableLanguages.containsKey(languageId)) {
				missingLanguages.add(languageId);
				config.removeWorkspaceMapping(contentTypeId);
			}
		}
		
		return missingLanguages;
	}
}

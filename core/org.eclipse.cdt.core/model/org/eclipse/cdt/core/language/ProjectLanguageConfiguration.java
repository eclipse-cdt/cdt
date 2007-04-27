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
package org.eclipse.cdt.core.language;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.core.resources.IFile;

/**
 * Provides programmatic access to language mappings for a project.
 * 
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will work or
 * that it will remain the same. Please do not use this API without consulting
 * with the CDT team.
 * </p>
 * 
 * @since 4.0
 */
public class ProjectLanguageConfiguration {
	private static final String ALL_CONFIGURATIONS = ""; //$NON-NLS-1$
	
	/**
	 * Project-wide content type mappings (Configuration ID -> (Content Type ID -> Language ID)).
	 */
	private Map/*<String,Map<String,String>>*/ fConfigurationContentTypeMappings;

	/**
	 * Per-file mappings (Path -> (Configuration ID -> Language ID)).
	 */
	private Map/*<String,Map<String,String>>*/ fFileConfigurationMappings;
	
	/**
	 * Creates a new <code>ProjectLanguageConfiguration</code> with no
	 * language mappings defined.
	 */
	public ProjectLanguageConfiguration() {
		fConfigurationContentTypeMappings = new TreeMap();
		fFileConfigurationMappings = new TreeMap();
	}
	
	/**
	 * Returns the language id that is mapped to the given content type when
	 * the given configurationis active.
	 * If <code>configuration</code> is <code>null</code>, the configuration-agnostic
	 * mapping is returned.
	 * @return
	 */
	public String getLanguageForContentType(ICConfigurationDescription configuration, String contentTypeId) {
		String configurationId = getId(configuration);
		Map contentTypeMappings = (Map) fConfigurationContentTypeMappings.get(configurationId);
		if (contentTypeMappings == null) {
			return null;
		}
		return (String) contentTypeMappings.get(contentTypeId);
	}
	
	/**
	 * Returns the language id that is mapped to the given file when the given
	 * configuration is active.
	 * If <code>configuration</code> is <code>null</code>, the configuration-agnostic
	 * mapping is returned.
	 * @return
	 */
	public String getLanguageForFile(ICConfigurationDescription configuration, IFile file) {
		return getLanguageForFile(configuration, file.getProjectRelativePath().toPortableString());
	}
	
	/**
	 * Returns the language id that is mapped to the file located at the given path
	 * when the given configuration is active.
	 * If <code>configuration</code> is <code>null</code>, the configuration-agnostic
	 * mapping is returned.
	 * @param path
	 * @return
	 */
	public String getLanguageForFile(ICConfigurationDescription configuration, String path) {
		Map configurationMappings = (Map) fFileConfigurationMappings.get(path);
		if (configurationMappings == null) {
			return null;
		}
		String configurationId = getId(configuration);
		return (String) configurationMappings.get(configurationId);
	}
	
	
	/**
	 * Sets the language for a content type.
	 * If <code>configuration</code> is not <code>null</code>, the language mapping
	 * will only apply when that configuration is active.  Otherwise, the mapping
	 * will apply for all configurations.
	 * @param contentType
	 * @param language
	 */
	public void addContentTypeMapping(ICConfigurationDescription configuration, String contentType, String language) {
		String configurationId = getId(configuration);
		Map contentTypeMappings = (Map) fConfigurationContentTypeMappings.get(configurationId);
		if (contentTypeMappings == null) {
			contentTypeMappings = new TreeMap();
			fConfigurationContentTypeMappings.put(configurationId, contentTypeMappings);
		}
		contentTypeMappings.put(contentType, language);
	}

	/**
	 * Removes the given content type mapping (if it exists).
	 * If <code>configuration</code> is <code>null</code>, the configuration-agnostic
	 * mapping is removed.  Otherwise, the configuration-specific mapping is removed.
	 * @param contentType
	 */
	public void removeContentTypeMapping(ICConfigurationDescription configuration, String contentType) {
		String configurationId = getId(configuration);
		Map contentTypeMappings = (Map) fConfigurationContentTypeMappings.get(configurationId);
		if (contentTypeMappings == null) {
			return;
		}
		contentTypeMappings.remove(contentType);
		if (contentTypeMappings.size() == 0) {
			fConfigurationContentTypeMappings.remove(configurationId);
		}
	}
	
	/**
	 * Sets the language for a file.
	 * If <code>configuration</code> is not <code>null</code>, the language mapping
	 * will only apply when that configuration is active.  Otherwise, the mapping
	 * will apply for all configurations.
	 * @param file
	 * @param language
	 */
	public void addFileMapping(ICConfigurationDescription configuration, IFile file, String language) {
		addFileMapping(configuration, file.getProjectRelativePath().toPortableString(), language);
	}
	
	/**
	 * Sets the language for a file.
	 * If <code>configuration</code> is not <code>null</code>, the language mapping
	 * will only apply when that configuration is active.  Otherwise, the mapping
	 * will apply for all configurations.
	 * @param filePath
	 * @param language
	 */
	public void addFileMapping(ICConfigurationDescription configuration, String filePath, String language) {
		Map configurationMappings = (Map) fFileConfigurationMappings.get(filePath);
		if (configurationMappings == null) {
			configurationMappings = new TreeMap();
			fFileConfigurationMappings.put(filePath, configurationMappings);
		}
		String configurationId = getId(configuration);
		configurationMappings.put(configurationId, language);
	}
	
	/**
	 * Removes the given file mapping (if it exists).
	 * If <code>configuration</code> is <code>null</code>, the configuration-agnostic
	 * mapping is removed.  Otherwise, the configuration-specific mapping is removed.
	 * @param file
	 */
	public void removeFileMapping(ICConfigurationDescription configuration, IFile file) {
		removeFileMapping(configuration, file.getProjectRelativePath().toPortableString());
	}
	
	/**
	 * Removes the given file mapping (if it exists).
	 * If <code>configuration</code> is <code>null</code>, the configuration-agnostic
	 * mapping is removed.  Otherwise, the configuration-specific mapping is removed.
	 * @param filePath
	 */
	public void removeFileMapping(ICConfigurationDescription configuration, String filePath) {
		Map fileMappings = (Map) fFileConfigurationMappings.get(filePath);
		if (fileMappings == null) {
			return;
		}
		String configurationId = getId(configuration);
		fileMappings.remove(configurationId);
		if (fileMappings.size() == 0) {
			fFileConfigurationMappings.remove(configurationId);
		}
	}
	
	/**
	 * Removes all language mappings for the given file.
	 * @param filePath
	 */
	public void removeAllFileMappings(String filePath) {
		fFileConfigurationMappings.remove(filePath);
	}
	
	/**
	 * Removes all language mappings for the given file.
	 * @param filePath
	 */
	public void removeAllFileMappings(IFile file) {
		removeAllFileMappings(file.getProjectRelativePath().toPortableString());
	}

	/**
	 * This method is used internally by CDT and should not be used outside of the CDT framework.
	 * @return
	 */
	public Map getContentTypeMappings() {
		return copyLanguageMappings(fConfigurationContentTypeMappings, false);
	}

	/**
	 * This method is used internally by CDT and should not be used outside of the CDT framework.
	 */
	public void setContentTypeMappings(Map mappings) {
		fConfigurationContentTypeMappings = copyLanguageMappings(mappings, false);
	}

	/**
	 * This method is used internally by CDT and should not be used outside of the CDT framework.
	 * @return
	 */
	public Map getFileMappings() {
		return copyLanguageMappings(fFileConfigurationMappings, false);
	}

	/**
	 * This method is used internally by CDT and should not be used outside of the CDT framework.
	 * @param file 
	 * @return
	 */
	public void setFileMappings(IFile file, Map mappings) {
		fFileConfigurationMappings.put(file.getProjectRelativePath().toPortableString(), new TreeMap(mappings));
	}

	private Map copyLanguageMappings(Map mappings, boolean isReadOnly) {
		Map result = new TreeMap();
		Iterator entries = mappings.entrySet().iterator();
		while (entries.hasNext()) {
			Entry entry = (Entry) entries.next();
			Map map = (Map) entry.getValue();
			if (isReadOnly) {
				map = Collections.unmodifiableMap(map);
			} else {
				map = new TreeMap(map);
			}
			result.put(entry.getKey(), map);
		}
		if (isReadOnly) {
			result = Collections.unmodifiableMap(result);
		}
		return result;
	}

	/**
	 * This method is used internally by CDT and should not be used outside of the CDT framework.
	 * @param file 
	 * @return
	 */
	public void setFileMappings(Map mappings) {
		fFileConfigurationMappings = copyLanguageMappings(mappings, false);
	}
	
	private String getId(ICConfigurationDescription configuration) {
		if (configuration == null) {
			return ALL_CONFIGURATIONS;
		}
		return configuration.getId();
	}
}

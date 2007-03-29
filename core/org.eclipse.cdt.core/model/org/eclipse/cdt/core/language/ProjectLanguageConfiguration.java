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
import java.util.Map;
import java.util.TreeMap;

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

	/**
	 * Project-wide content type mappings.
	 */
	private Map fContentTypeMappings;

	/**
	 * Per-file mappings.
	 */
	private Map fFileMappings;
	
	/**
	 * Creates a new <code>ProjectLanguageConfiguration</code> with no
	 * language mappings defined.
	 */
	public ProjectLanguageConfiguration() {
		fContentTypeMappings = new TreeMap();
		fFileMappings = new TreeMap();
	}
	
	/**
	 * Returns a read-only copy of the project-wide content-type-specific language mappings.
	 * @return a read-only copy of the project-wide content-type-specific language mappings.
	 */
	public Map getContentTypeMappings() {
		return Collections.unmodifiableMap(fContentTypeMappings);
	}

	/**
	 * Returns the language id that is mapped to the given content type.
	 * @param contentTypeId
	 * @return
	 */
	public String getLanguageForContentType(String contentTypeId) {
		return (String) fContentTypeMappings.get(contentTypeId);
	}
	
	/**
	 * Returns the language id that is mapped to the given file.
	 * @param file
	 * @return
	 */
	public String getLanguageForFile(IFile file) {
		return (String) fFileMappings.get(file.getProjectRelativePath().toPortableString());
	}
	
	/**
	 * Returns the language id that is mapped to the given file.
	 * @param path
	 * @return
	 */
	public String getLanguageForFile(String path) {
		return (String) fFileMappings.get(path);
	}
	
	
	/**
	 * Replaces the existing content-type-specific language mappings with the given
	 * mappings.  The given mappings should be between content type ids
	 * (<code>String</code>) and language ids (<code>String</code>)
	 * @param mappings
	 */
	public void setContentTypeMappings(Map/*<String, String>*/ mappings) {
		fContentTypeMappings = new TreeMap(mappings);
	}

	/**
	 * Maps a content type id to a language id.
	 * @param contentType
	 * @param language
	 */
	public void addContentTypeMapping(String contentType, String language) {
		fContentTypeMappings.put(contentType, language);
	}

	/**
	 * Removes the given content type mapping (if it exists).
	 * @param contentType
	 */
	public void removeContentTypeMapping(String contentType) {
		fContentTypeMappings.remove(contentType);
	}
	
	/**
	 * Sets the language for a file.
	 * @param file
	 * @param language
	 */
	public void addFileMapping(IFile file, String language) {
		fFileMappings.put(file.getProjectRelativePath().toPortableString(), language);
	}
	
	/**
	 * Sets the language for a file.
	 * @param filePath
	 * @param language
	 */
	public void addFileMapping(String filePath, String language) {
		fFileMappings.put(filePath, language);
	}
	
	/**
	 * Removes the given file mapping (if it exists).
	 * @param file
	 */
	public void removeFileMapping(IFile file) {
		fFileMappings.remove(file.getProjectRelativePath().toPortableString());
	}
	
	/**
	 * Removes the given file mapping (if it exists).
	 * @param filePath
	 */
	public void removeFileMapping(String filePath) {
		fFileMappings.remove(filePath);
	}
	
	/**
	 * Returns a read-only copy of the file-specific language mappings.
	 * @return a read-only copy of the file-specific language mappings.
	 */
	public Map getFileMappings() {
		return Collections.unmodifiableMap(fFileMappings);
	}

	/**
	 * Replaces the existing file-specific language mappings with the given
	 * mappings.  The given mappings should be between full paths
	 * (<code>String</code>) and language ids (<code>String</code>)
	 * @param projectMappings
	 */
	public void setFileMappings(Map/*<String, String>*/ fileMappings) {
		fContentTypeMappings = new TreeMap(fileMappings);
	}
}

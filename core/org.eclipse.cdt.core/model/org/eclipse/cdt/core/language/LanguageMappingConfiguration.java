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
public class LanguageMappingConfiguration {

	/**
	 * Project-wide mappings.
	 */
	private Map fProjectMappings;

	/**
	 * Creates a new <code>LanguageMappingConfiguration</code> with no
	 * mappings defined.
	 */
	public LanguageMappingConfiguration() {
		fProjectMappings = new TreeMap();
	}
	
	/**
	 * Returns a read-only copy of the project-wide language mappings.
	 * @return a read-only copy of the project-wide language mappings.
	 */
	public Map getProjectMappings() {
		return Collections.unmodifiableMap(fProjectMappings);
	}

	/**
	 * Replaces the existing language mappings with the given
	 * mappings.  The given mappings should be between content type ids
	 * (<code>String</code>) and language ids (<code>String</code>)
	 * @param projectMappings
	 */
	public void setProjectMappings(Map/*<String, String>*/ projectMappings) {
		fProjectMappings = new TreeMap(projectMappings);
	}

	/**
	 * Maps a content type id to a language id.
	 * @param contentType
	 * @param language
	 */
	public void addProjectMapping(String contentType, String language) {
		fProjectMappings.put(contentType, language);
	}

	/**
	 * Removes the given content type mapping (if it exists).
	 * @param contentType
	 */
	public void removeProjectMapping(String contentType) {
		fProjectMappings.remove(contentType);
	}
}

/*******************************************************************************
 * Copyright (c) 2007, 2009 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.language;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/**
 * Provides programmatic access to language mappings for the workspace.
 *
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class WorkspaceLanguageConfiguration {

	/**
	 * Workspace-wide content type mappings.
	 */
	private Map<String, String> fMappings;

	/**
	 * Creates a new <code>WorkspaceLanguageConfiguration</code> with no
	 * language mappings defined.
	 */
	public WorkspaceLanguageConfiguration() {
		fMappings = new TreeMap<>();
	}

	/**
	 * Maps a content type id to a language id.
	 * @param contentType
	 * @param language
	 */
	public void addWorkspaceMapping(String contentType, String language) {
		fMappings.put(contentType, language);
	}

	/**
	 * Removes the given content type mapping (if it exists).
	 * @param contentType
	 */
	public void removeWorkspaceMapping(String contentType) {
		fMappings.remove(contentType);
	}

	/**
	 * Replaces the existing language mappings with the given
	 * mappings.  The given mappings should be between content type ids
	 * (<code>String</code>) and language ids (<code>String</code>)
	 * @param mappings
	 */
	public void setWorkspaceMappings(Map<String, String> mappings) {
		fMappings = new TreeMap<>(mappings);
	}

	/**
	 * Returns a read-only copy of the workspace-wide language mappings.
	 * @return a read-only copy of the workspace-wide language mappings.
	 */
	public Map<String, String> getWorkspaceMappings() {
		return Collections.unmodifiableMap(fMappings);
	}

	/**
	 * Returns the language id that is mapped to the given content type.
	 * @param contentTypeId
	 * @return the language id that is mapped to the given content type.
	 */
	public String getLanguageForContentType(String contentTypeId) {
		return fMappings.get(contentTypeId);
	}
}

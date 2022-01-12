/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.language;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.cdt.core.language.ProjectLanguageConfiguration;
import org.eclipse.cdt.core.language.WorkspaceLanguageConfiguration;
import org.eclipse.cdt.core.model.LanguageManager;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;

/**
 * Resolves the effective language for various resources such as
 * files and projects.
 */
public class LanguageMappingResolver {
	public static final int DEFAULT_MAPPING = 0;
	public static final int WORKSPACE_MAPPING = 1;
	public static final int PROJECT_MAPPING = 2;
	public static final int FILE_MAPPING = 3;

	/**
	 * Returns the effective language for the file specified by the given path.
	 * If <code>fetchAll</code> is <code>true</code> all inherited language
	 * mappings will be returned in order of precedence.  Otherwise, only the
	 * effective language will be returned.
	 *
	 * This method will always return at least one mapping.
	 *
	 * @param project the project that contains the given file
	 * @param filePath the path to the file
	 * @param contentTypeId the content type of the file (optional)
	 * @param fetchAll if <code>true</code>, returns all inherited language mappings.
	 *                 Otherwise, returns only the effective language.
	 * @return the effective language for the file specified by the given path.
	 * @throws CoreException
	 */
	public static LanguageMapping[] computeLanguage(IProject project, String filePath,
			ICConfigurationDescription configuration, String contentTypeId, boolean fetchAll) throws CoreException {
		LanguageManager manager = LanguageManager.getInstance();
		List<LanguageMapping> inheritedLanguages = new LinkedList<>();

		if (project != null) {
			ProjectLanguageConfiguration mappings = manager.getLanguageConfiguration(project);

			if (mappings != null) {
				// File-level mappings
				if (filePath != null) {

					String id = mappings.getLanguageForFile(configuration, filePath);
					if (id != null) {
						inheritedLanguages.add(new LanguageMapping(manager.getLanguage(id), FILE_MAPPING));
						if (!fetchAll) {
							return createLanguageMappingArray(inheritedLanguages);
						}
					}

					// Check for a file mapping that's global across all configurations in
					// the project.
					if (configuration != null) {
						id = mappings.getLanguageForFile(null, filePath);
						if (id != null) {
							inheritedLanguages.add(new LanguageMapping(manager.getLanguage(id), FILE_MAPPING));
							if (!fetchAll) {
								return createLanguageMappingArray(inheritedLanguages);
							}
						}

					}
				}

				// Project-level mappings
				String id = mappings.getLanguageForContentType(configuration, contentTypeId);
				if (id != null) {
					inheritedLanguages.add(new LanguageMapping(manager.getLanguage(id), PROJECT_MAPPING));
					if (!fetchAll) {
						return createLanguageMappingArray(inheritedLanguages);
					}
				}

				// Check for a content type mapping that's global across all configurations in
				// the project.
				if (configuration != null) {
					id = mappings.getLanguageForContentType(null, contentTypeId);
					if (id != null) {
						inheritedLanguages.add(new LanguageMapping(manager.getLanguage(id), PROJECT_MAPPING));
						if (!fetchAll) {
							return createLanguageMappingArray(inheritedLanguages);
						}
					}
				}
			}
		}

		// Workspace mappings
		WorkspaceLanguageConfiguration workspaceMappings = manager.getWorkspaceLanguageConfiguration();
		String id = workspaceMappings.getLanguageForContentType(contentTypeId);
		if (id != null) {
			inheritedLanguages.add(new LanguageMapping(manager.getLanguage(id), WORKSPACE_MAPPING));
			if (!fetchAll) {
				return createLanguageMappingArray(inheritedLanguages);
			}
		}

		// Platform mappings
		IContentType contentType = Platform.getContentTypeManager().getContentType(contentTypeId);
		inheritedLanguages.add(new LanguageMapping(manager.getLanguage(contentType), DEFAULT_MAPPING));
		return createLanguageMappingArray(inheritedLanguages);
	}

	private static LanguageMapping[] createLanguageMappingArray(List<LanguageMapping> inheritedLanguages) {
		LanguageMapping[] results = new LanguageMapping[inheritedLanguages.size()];
		Iterator<LanguageMapping> mappings = inheritedLanguages.iterator();
		int i = 0;
		while (mappings.hasNext()) {
			LanguageMapping mapping = mappings.next();
			results[i] = mapping;
			i++;
		}
		return results;
	}
}

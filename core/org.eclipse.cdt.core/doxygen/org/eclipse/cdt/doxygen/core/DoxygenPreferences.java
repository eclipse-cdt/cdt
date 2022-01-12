/*******************************************************************************
 * Copyright (c) 2020 ArSysOp and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alexander Fedorov <alexander.fedorov@arsysop.ru> - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.doxygen.core;

import org.eclipse.cdt.doxygen.DoxygenMetadata;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.preferences.IPreferenceMetadataStore;

public interface DoxygenPreferences {

	/**
	 * Returns the workspace storage for doxygen options to be used in UI, must not return <code>null</code>
	 *
	 * @return the workspace storage for doxygen options
	 */
	IPreferenceMetadataStore workspaceStorage();

	/**
	 * Returns the project-specific storage for doxygen options to be used in UI, must not return <code>null</code>
	 *
	 * @param project scope for the storage, must not be <code>null</code>
	 * @return the project-specific storage for doxygen options
	 */
	IPreferenceMetadataStore projectStorage(IProject project);

	/**
	 * Return the metadata for the options to be used in UI, must not return <code>null</code>
	 *
	 * @return the doxygen option metadata
	 */
	DoxygenMetadata metadata();

}

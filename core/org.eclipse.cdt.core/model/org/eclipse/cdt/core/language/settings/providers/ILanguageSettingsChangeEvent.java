/*******************************************************************************
 * Copyright (c) 2011, 2012 Andrew Gvozdev and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Gvozdev - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.language.settings.providers;

import java.util.Set;

import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.core.resources.IResource;

/**
 * Contains the details of changes that occurred as a result of modifying
 * language settings entries {@link ICLanguageSettingEntry}. This event is
 * intended to be fired for changes in entries, not necessarily providers.
 * The event is associated with a project.
 *
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class interface is not stable yet as
 * it is not currently clear how it may need to be used in future. Only bare
 * minimum is provided here at this point (CDT 8.1, Juno).
 * There is no guarantee that this API will work or that it will remain the same.
 * Please do not use this API without consulting with the CDT team.
 * </p>
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 *
 * @since 5.4
 */
public interface ILanguageSettingsChangeEvent {
	/**
	 * @return project name where the event occurred.
	 */
	public String getProjectName();

	/**
	 * @return configuration IDs which are affected by the language settings entries changes.
	 */
	public String[] getConfigurationDescriptionIds();

	/**
	 * @return list of resources affected by the language settings entries changes.
	 */
	public Set<IResource> getAffectedResources(String cfgId);
}

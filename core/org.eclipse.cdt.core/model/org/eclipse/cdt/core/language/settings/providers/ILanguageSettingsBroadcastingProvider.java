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

import java.util.List;

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.core.resources.IResource;

/**
 * This interface is to be implemented by providers which want to broadcast the changes in their setting entries
 * with {@link ILanguageSettingsChangeEvent}.
 *
 * @since 5.4
 */
public interface ILanguageSettingsBroadcastingProvider extends ILanguageSettingsProvider {
	@Override
	public String getId();

	@Override
	public String getName();

	@Override
	public List<ICLanguageSettingEntry> getSettingEntries(ICConfigurationDescription cfgDescription, IResource rc,
			String languageId);

	/**
	 * Return a copy of internal storage. This should be a deep copy/clone of the storage.
	 * It is used to calculate the delta and being kept in the last state object of configuration
	 * description to compare to a new state later.
	 *
	 * @return a copy of internal storage.
	 */
	public LanguageSettingsStorage copyStorage();
}

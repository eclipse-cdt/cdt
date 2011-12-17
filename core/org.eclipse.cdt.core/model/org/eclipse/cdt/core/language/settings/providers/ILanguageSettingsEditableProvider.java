/*******************************************************************************
 * Copyright (c) 2009, 2011 Andrew Gvozdev and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
 * This interface is used in UI to identify classes allowing user to modify settings externally
 * contrary to some subclasses of {@link LanguageSettingsSerializableProvider} managing
 * their settings themselves and not providing such option to the user.
 *
 * @since 5.4
 *
 */
public interface ILanguageSettingsEditableProvider extends ILanguageSettingsBroadcastingProvider, Cloneable {
	@Override
	public String getId();
	@Override
	public String getName();
	@Override
	public List<ICLanguageSettingEntry> getSettingEntries(ICConfigurationDescription cfgDescription, IResource rc, String languageId);

	/**
	 * Sets language settings entries for the provider.
	 *
	 * @param cfgDescription - configuration description.
	 * @param rc - resource such as file or folder. If {@code null} the entries are
	 *    considered to be being defined as default entries for resources.
	 * @param languageId - language id. If {@code null}, then entries are considered
	 *    to be defined as default entries for languages.
	 * @param entries - language settings entries to set.
	 */
	public void setSettingEntries(ICConfigurationDescription cfgDescription, IResource rc, String languageId, List<ICLanguageSettingEntry> entries);

	/**
	 * Shallow clone of the provider. "Shallow" is defined here as the exact copy except that
	 * the copy will have zero language settings entries.
	 *
	 * @return shallow copy of the provider.
	 * @throws CloneNotSupportedException in case {@link #clone()} throws the exception.
	 */
	public ILanguageSettingsEditableProvider cloneShallow() throws CloneNotSupportedException;

	/*
	 * @see Object#clone()
	 */
	public ILanguageSettingsEditableProvider clone() throws CloneNotSupportedException;
}

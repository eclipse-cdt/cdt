/*******************************************************************************
 * Copyright (c) 2009, 2010 Andrew Gvozdev (Quoin Inc.) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Gvozdev (Quoin Inc.) - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.core.language.settings.providers;

import java.util.List;

import org.eclipse.cdt.core.model.LanguageManager;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.core.resources.IResource;

/**
 * Base interface to provide list of {@link ICLanguageSettingEntry}. 
 * This interface is used to deliver additions to compiler options such as 
 * include paths (-I) or preprocessor defines (-D) and others (see
 * {@link ICSettingEntry#INCLUDE_PATH} and other kinds).
 * 
 * To define a provider like that use extension point
 * {@code org.eclipse.cdt.core.LanguageSettingsProvider} and implement this
 * interface. CDT provides a few general use implementations such as
 * {@link LanguageSettingsBaseProvider} which could be used out of the box or
 * extended. See extension point schema description LanguageSettingsProvider.exsd
 * for more details.
 * 
 * @since 6.0
 */
public interface ILanguageSettingsProvider {
	/**
	 * Id is used to keep track of the providers internally. Use unique id
	 * to represent the provider.
	 * 
	 * @return Id of the provider.
	 */
	public String getId();

	/**
	 * Name is used to present the provider to the end user in UI.
	 * 
	 * @return name of the provider.
	 */
	public String getName();

	/**
	 * Returns the list of setting entries for the given configuration description,
	 * resource and language.
	 * 
	 * @param cfgDescription - configuration description.
	 * @param rc - resource such as file or folder.
	 * @param languageId - language id
	 *     (see {@link LanguageManager#getLanguageForFile(org.eclipse.core.resources.IFile, ICConfigurationDescription)}).
	 * 
	 * @return the list of setting entries or {@code null} if no settings defined.
	 */
	public List<ICLanguageSettingEntry> getSettingEntries(ICConfigurationDescription cfgDescription, IResource rc, String languageId);
}

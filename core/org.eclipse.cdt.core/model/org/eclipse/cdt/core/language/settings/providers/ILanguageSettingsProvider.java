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
 * <br>
 * To define a provider like that use extension point
 * {@code org.eclipse.cdt.core.LanguageSettingsProvider} and implement this
 * interface. CDT provides a few general use implementations such as
 * {@link LanguageSettingsBaseProvider} or {@link LanguageSettingsSerializableProvider}
 * which could be used out of the box or extended. See also extension point
 * schema description LanguageSettingsProvider.exsd.
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
	 * <br><br>
	 * Note to implementers - this method should not be used to do any long running
	 * operations such as extensive calculations or reading files. If you need to do
	 * so, the recommended way is to do the calculations outside of
	 * this function call - in advance and on appropriate event. For example, Build
	 * Output Parser prepares the list and stores it in internal cache while parsing output.
	 * {@link #getSettingEntries(ICConfigurationDescription, IResource, String)} will
	 * return cached entries when asked. You can also implement {@link ICListenerAgent}
	 * interface to get registered and listen to arbitrary events.
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

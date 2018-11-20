/*******************************************************************************
 * Copyright (c) 2009, 2012 Andrew Gvozdev and others.
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
 * <br><br>
 * To define a provider like that use extension point
 * {@code org.eclipse.cdt.core.LanguageSettingsProvider} and implement this
 * interface. The recommended way of implementing is to extend
 * {@link LanguageSettingsSerializableProvider} and implement {@link ILanguageSettingsEditableProvider}.
 * That will give the ability to persist and edit/clean entries by user in UI.
 * The clone methods defined by {@link ILanguageSettingsEditableProvider} should be
 * chained as done for example by {@link LanguageSettingsGenericProvider}.
 * <br><br>
 * CDT provides a few general use implementations in the core such as {@link LanguageSettingsBaseProvider}
 * or {@link LanguageSettingsSerializableProvider} or {@link LanguageSettingsGenericProvider}
 * which could be used out of the box or built upon. There are also abstract classes in build
 * plugins {@code AbstractBuildCommandParser} and {@code AbstractBuiltinSpecsDetector} which
 * serve as a base for output parsers and built-in compiler language settings detectors.
 * See also extension point schema description LanguageSettingsProvider.exsd.
 *
 * @since 5.4
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
	 *    If {@code null}, the default entries for all resources are returned.
	 * @param languageId - language id.
	 *    If {@code null}, the default entries for all languages are returned.
	 *     (see {@link LanguageManager#getLanguageForFile(org.eclipse.core.resources.IFile, ICConfigurationDescription)}).
	 *
	 * @return the list of setting entries or {@code null} if no settings defined.
	 *    The list needs to be a pooled list created by {@link LanguageSettingsStorage#getPooledList(List)}
	 *    to save memory and avoid deep equality comparisons.
	 */
	public List<ICLanguageSettingEntry> getSettingEntries(ICConfigurationDescription cfgDescription, IResource rc,
			String languageId);
}

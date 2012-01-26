/*******************************************************************************
 * Copyright (c) 2011, 2012 Andrew Gvozdev and others.
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

import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
/**
 * Interface to express ability (of a configuration description) to handle Language Settings
 * Providers.
 * @see ILanguageSettingsProvider
 *
 * @since 5.4
 *
 */
public interface ILanguageSettingsProvidersKeeper {
	/**
	 * Sets the list of language settings providers. Language settings providers are
	 * used to supply language settings {@link ICLanguageSettingEntry} such as include paths
	 * or preprocessor macros.
	 *
	 * @param providers - the list of providers to assign to the owner (configuration description).
	 *    This method clones the internal list or otherwise ensures immutability of the internal
	 *    list before actual addition to the project model. That is to ensure that there is no
	 *    back-door access and all changes in the list done by this method which fires notifications
	 *    to the registered listeners about the accompanied changes in settings entries, see
	 *    {@link LanguageSettingsManager#registerLanguageSettingsChangeListener(ILanguageSettingsChangeListener)}.
	 */
	public void setLanguageSettingProviders(List<ILanguageSettingsProvider> providers);

	/**
	 * Returns the list of language settings providers. Language settings providers are
	 * used to supply language settings {@link ICLanguageSettingEntry} such as include paths
	 * or preprocessor macros.
	 *
	 * @return the list of providers to assign to the owner (configuration description). This
	 *    returns immutable list. Use {@link #setLanguageSettingProviders(List)} to change.
	 *    This method does not return {@code null}.
	 */
	public List<ILanguageSettingsProvider> getLanguageSettingProviders();

	/**
	 * Sets the list of IDs of default language settings providers.
	 * <br><br>
	 * The method is intended to be used by MBS to set the list from tool-chain definition.
	 * The default list from the tool-chain is used, for example, while resetting
	 * configuration providers to default in UI.
	 *
	 * @param ids - default provider IDs specified in the tool-chain.
	 */
	public void setDefaultLanguageSettingsProvidersIds(String[] ids);

	/**
	 * Retrieve the list of IDs of default language settings providers.
	 * Normally the list would come from the tool-chain definition.
	 *
	 * @return default provider IDs or {@code null} if default providers are not defined.
	 */
	public String[] getDefaultLanguageSettingsProvidersIds();

}

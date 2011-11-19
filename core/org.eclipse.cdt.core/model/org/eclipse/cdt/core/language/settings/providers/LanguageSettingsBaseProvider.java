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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.cdt.core.AbstractExecutableExtensionBase;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.internal.core.language.settings.providers.LanguageSettingsStorage;
import org.eclipse.cdt.internal.core.settings.model.SettingsModelMessages;
import org.eclipse.core.resources.IResource;

/**
 * {@code LanguageSettingsBaseProvider} is a basic implementation of {@link ILanguageSettingsProvider}
 * for the extensions defined by {@code org.eclipse.cdt.core.LanguageSettingsProvider} extension point.
 *
 * This implementation supports "static" list of entries for languages specified in
 * the extension point.
 *
 * @since 6.0
 */
public class LanguageSettingsBaseProvider extends AbstractExecutableExtensionBase implements ILanguageSettingsProvider {
	/** Language scope, i.e. list of languages the entries will be provided for. */
	protected List<String> languageScope = null;

	/** Custom parameter. Intended for providers extending this class. */
	protected String customParameter = null;

	/** List of entries defined by this provider. */
	private List<ICLanguageSettingEntry> entries = null;

	/**
	 * Default constructor.
	 */
	public LanguageSettingsBaseProvider() {
		super();
	}

	/**
	 * Constructor. Creates an "empty" non-configured provider.
	 *
	 * @param id - id of the provider.
	 * @param name - name of the provider to be presented to a user.
	 */
	public LanguageSettingsBaseProvider(String id, String name) {
		super(id, name);
	}

	/**
	 * Constructor.
	 *
	 * @param id - id of the provider.
	 * @param name - name of the provider to be presented to a user.
	 * @param languages - list of languages the {@code entries} provided for.
	 *    {@code languages} can be {@code null}, in this case the {@code entries}
	 *    are provided for any language.
	 * @param entries - the list of language settings entries this provider provides.
	 *    If {@code null} is passed, the provider creates an empty list.
	 */
	public LanguageSettingsBaseProvider(String id, String name, List<String> languages,
			List<ICLanguageSettingEntry> entries) {
		super(id, name);
		this.languageScope = languages!=null ? new ArrayList<String>(languages) : null;
		this.entries = getPooledList(entries);
	}

	/**
	 * Constructor.
	 *
	 * @param id - id of the provider.
	 * @param name - name of the provider to be presented to a user.
	 * @param languages - list of languages the {@code entries} provided for.
	 *    {@code languages} can be {@code null}, in this case the {@code entries}
	 *    are provided for any language.
	 * @param entries - the list of language settings entries this provider provides.
	 *    If {@code null} is passed, the provider creates an empty list.
	 * @param customParameter - a custom parameter as the means to customize
	 *    providers extending this class.
	 */
	public LanguageSettingsBaseProvider(String id, String name, List<String> languages,
			List<ICLanguageSettingEntry> entries, String customParameter) {
		super(id, name);
		this.languageScope = languages!=null ? new ArrayList<String>(languages) : null;
		this.entries = getPooledList(entries);
		this.customParameter = customParameter;
	}

	/**
	 * A method to configure the provider. The initialization of provider from
	 * the extension point is done in 2 steps. First, the class is created as
	 * an executable extension using the default provider. Then this method is
	 * used to configure the provider.
	 *<br><br>
	 * It is not allowed to reconfigure the provider.
	 *
	 * @param id - id of the provider.
	 * @param name - name of the provider to be presented to a user.
	 * @param languages - list of languages the {@code entries} provided for.
	 *    {@code languages} can be {@code null}, in this case the {@code entries}
	 *    are provided for any language.
	 * @param entries - the list of language settings entries this provider provides.
	 *    If {@code null} is passed, the provider creates an empty list.
	 * @param customParameter - a custom parameter as the means to customize
	 *    providers extending this class from extension definition in {@code plugin.xml}.
	 *
	 * @throws UnsupportedOperationException if an attempt to reconfigure provider is made.
	 */
	public void configureProvider(String id, String name, List<String> languages,
			List<ICLanguageSettingEntry> entries, String customParameter) {
		if (this.entries!=null)
			throw new UnsupportedOperationException(SettingsModelMessages.getString("LanguageSettingsBaseProvider.CanBeConfiguredOnlyOnce")); //$NON-NLS-1$

		setId(id);
		setName(name);
		this.languageScope = languages!=null ? new ArrayList<String>(languages) : null;
		this.entries = getPooledList(entries);
		this.customParameter = customParameter;
	}

	private List<ICLanguageSettingEntry> getPooledList(List<ICLanguageSettingEntry> entries) {
		if (entries != null) {
			return LanguageSettingsStorage.getPooledList(entries);
		}
		return LanguageSettingsStorage.getPooledEmptyList();
	}

	/**
	 * {@inheritDoc}
	 *
	 * @param languageId - language id. If {@code null}, then entries defined for
	 *    the language scope are returned. See {@link #getLanguageScope()}
	 *
	 * @return unmodifiable list of setting entries or {@code null} if no settings defined.
	 *    the list is internally pooled and guaranteed to be the same object for equal
	 *    lists.
	 */
	@Override
	public List<ICLanguageSettingEntry> getSettingEntries(ICConfigurationDescription cfgDescription,
			IResource rc, String languageId) {
		if (languageScope == null) {
			return entries;
		}
		for (String lang : languageScope) {
			if (lang.equals(languageId)) {
				return entries;
			}
		}
		return null;
	}

	/**
	 * @return the unmodifiable list of languages this provider provides for.
	 *    If {@code null}, the provider provides for any language.
	 */
	public List<String> getLanguageScope() {
		if (languageScope==null)
			return null;
		return Collections.unmodifiableList(languageScope);
	}

	/**
	 * @return the custom parameter defined in the extension in {@code plugin.xml}.
	 */
	public String getCustomParameter() {
		return customParameter;
	}
}

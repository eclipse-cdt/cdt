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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.core.AbstractExecutableExtensionBase;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.internal.core.settings.model.SettingsModelMessages;
import org.eclipse.core.resources.IResource;

/**
 * {@code LanguageSettingsBaseProvider} is a basic implementation of {@link ILanguageSettingsProvider}
 * for the extensions defined by {@code org.eclipse.cdt.core.LanguageSettingsProvider} extension point.
 *
 * This implementation supports "static" list of entries for languages specified in
 * the extension point.
 *
 * @since 5.4
 */
public class LanguageSettingsBaseProvider extends AbstractExecutableExtensionBase implements ILanguageSettingsProvider {
	/** Language scope, i.e. list of languages the entries will be provided for. */
	protected List<String> languageScope = null;

	/** Provider-specific properties */
	protected Map<String, String> properties = new HashMap<String, String>();

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
	 * @param properties - custom properties as the means to customize providers.
	 */
	public LanguageSettingsBaseProvider(String id, String name, List<String> languages,
			List<ICLanguageSettingEntry> entries, Map<String, String> properties) {
		super(id, name);
		this.languageScope = languages!=null ? new ArrayList<String>(languages) : null;
		this.entries = getPooledList(entries);
		if (properties != null)
			this.properties = new HashMap<String, String>(properties);
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
	 * @param properties - custom properties as the means to customize providers.
	 *
	 * @throws UnsupportedOperationException if an attempt to reconfigure provider is made.
	 */
	public void configureProvider(String id, String name, List<String> languages,
			List<ICLanguageSettingEntry> entries, Map<String, String> properties) {
		if (this.entries!=null || !this.properties.isEmpty())
			throw new UnsupportedOperationException(SettingsModelMessages.getString("LanguageSettingsBaseProvider.CanBeConfiguredOnlyOnce")); //$NON-NLS-1$

		setId(id);
		setName(name);
		this.languageScope = languages!=null ? new ArrayList<String>(languages) : null;
		this.entries = getPooledList(entries);
		if (properties != null)
			this.properties = new HashMap<String, String>(properties);
	}

	/**
	 * {@code LanguageSettingsBaseProvider} keeps the list of key-value pairs
	 * so extenders of this class can customize the provider. The properties
	 * of {@code LanguageSettingsBaseProvider} come from the extension in plugin.xml
	 * although the extenders can provide their own method.
	 * <br><br>
	 * Please note that empty string value is treated as "default" value and
	 * the same as {@code null} and the same as missing property, which allows
	 * {@link #equals(Object)} evaluate the property as equal while comparing providers.
	 *
	 * @param key - property to check the value.
	 * @return value of the property. If the property is missing returns empty string.
	 */
	public String getProperty(String key) {
		String value = properties.get(key);
		if (value == null) {
			value = ""; //$NON-NLS-1$
		}
		return value;
	}

	/**
	 * Convenience method to get boolean property.
	 * @see #getProperty(String)
	 *
	 * @param key - property to check the value.
	 * @return boolean value of the property. If the property is missing or cannot be
	 *    interpreted as boolean returns {@code false}.
	 */
	public boolean getPropertyBool(String key) {
		return Boolean.parseBoolean(properties.get(key));
	}

	private List<ICLanguageSettingEntry> getPooledList(List<ICLanguageSettingEntry> entries) {
		if (entries != null) {
			return LanguageSettingsStorage.getPooledList(entries);
		}
		return null;
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
		result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
		result = prime * result + ((entries == null) ? 0 : entries.hashCode());
		result = prime * result + ((languageScope == null) ? 0 : languageScope.hashCode());
		// exclude field "properties" because of special rules for equals()
		result = prime * result + getClass().hashCode();
		return result;
	}

	/**
	 * @return {@code true} if the objects are equal, {@code false } otherwise.
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LanguageSettingsBaseProvider other = (LanguageSettingsBaseProvider) obj;

		String id = getId();
		String otherId = other.getId();
		if (id == null) {
			if (otherId != null)
				return false;
		} else if (!id.equals(otherId))
			return false;

		String name = getName();
		String otherName = other.getName();
		if (name == null) {
			if (otherName != null)
				return false;
		} else if (!name.equals(otherName))
			return false;

		if (entries == null) {
			if (other.entries != null)
				return false;
		} else if (!entries.equals(other.entries))
			return false;

		if (languageScope == null) {
			if (other.languageScope != null)
				return false;
		} else if (!languageScope.equals(other.languageScope))
			return false;

		if (properties == null) {
			if (other.properties != null)
				return false;
		} else if (other.properties == null) {
			return false;
		} else {
			// The trouble to ensure default properties are equal to missing ones.
			Set<String> keys = new HashSet<String>(properties.keySet());
			keys.addAll(other.properties.keySet());
			for (String key : keys) {
				String value = properties.get(key);
				if (value == null || value.equals(Boolean.FALSE.toString()))
					value = ""; //$NON-NLS-1$
				String otherValue = other.properties.get(key);
				if (otherValue == null || otherValue.equals(Boolean.FALSE.toString()))
					otherValue = ""; //$NON-NLS-1$
				if (!value.equals(otherValue))
					return false;
			}
		}

		return true;
	}

}

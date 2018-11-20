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

/**
 * Generic implementation of language settings provider which can be edited in UI
 * with entries persisted between eclipse sessions.
 * The instances of this class can be used in plugin.xml to create a new provider
 * but this class is not intended to be extended. For more details how to create a
 * language settings provider see the description of {@link ILanguageSettingsProvider}.
 *
 * @since 5.4
 */
final public class LanguageSettingsGenericProvider extends LanguageSettingsSerializableProvider
		implements ILanguageSettingsEditableProvider {
	@Override
	public LanguageSettingsGenericProvider clone() throws CloneNotSupportedException {
		return (LanguageSettingsGenericProvider) super.clone();
	}

	@Override
	public LanguageSettingsGenericProvider cloneShallow() throws CloneNotSupportedException {
		return (LanguageSettingsGenericProvider) super.cloneShallow();
	}
}

/*******************************************************************************
 * Copyright (c) 2011, 2011 Andrew Gvozdev and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

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

import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;

/**
 * An interface for listeners to changes in language settings {@link ICLanguageSettingEntry}.
 *
 * @see LanguageSettingsManager#registerLanguageSettingsChangeListener(ILanguageSettingsChangeListener)
 * @see LanguageSettingsManager#unregisterLanguageSettingsChangeListener(ILanguageSettingsChangeListener)
 *
 * @since 5.4
 */
public interface ILanguageSettingsChangeListener {
	/**
	 * Indicates that language settings have been changed.
	 *
	 * @param event - details of the event.
	 */
	public void handleEvent(ILanguageSettingsChangeEvent event);
}
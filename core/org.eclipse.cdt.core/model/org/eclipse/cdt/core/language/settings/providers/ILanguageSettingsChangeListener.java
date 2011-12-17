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
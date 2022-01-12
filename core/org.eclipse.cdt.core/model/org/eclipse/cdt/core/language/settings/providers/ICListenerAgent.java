/*******************************************************************************
 * Copyright (c) 2011, 2013 Andrew Gvozdev and others.
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

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;

/**
 * Helper class to allow listeners of arbitrary events self-register/dispose.
 *
 * Called by CDT core when {@linkplain ICListenerAgent} added/removed to
 * the list of {@link ILanguageSettingsProvider}s  managed by the model.
 * {@linkplain ICListenerAgent} would commonly be implemented by a language
 * settings provider.
 * <br><br>
 * Implementers are to create a specific listener and register it to
 * appropriate event manager in {@link #registerListener(ICConfigurationDescription)}
 * then unregister and dispose in {@link #unregisterListener()}.
 *
 * @since 5.4
 */
public interface ICListenerAgent {
	/**
	 * Provides an opportunity for the provider to register specific listeners.
	 * Called by CDT core when {@linkplain ICListenerAgent} added
	 * to the list of {@link ILanguageSettingsProvider}s managed by the model.
	 *
	 * @param cfgDescription - configuration description for the listener.
	 */
	public void registerListener(ICConfigurationDescription cfgDescription);

	/**
	 * Unregister listeners and dispose all resources.
	 * Called by CDT core when {@linkplain ICListenerAgent} removed
	 * from the list of {@link ILanguageSettingsProvider}s managed by the model.
	 */
	public void unregisterListener();
}

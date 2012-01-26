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
	 * Registers a specific listener.
	 *
	 * @param cfgDescription - configuration description for the listener.
	 */
	public void registerListener(ICConfigurationDescription cfgDescription);

	/**
	 * Unregister listener and dispose all resources.
	 */
	public void unregisterListener();
}

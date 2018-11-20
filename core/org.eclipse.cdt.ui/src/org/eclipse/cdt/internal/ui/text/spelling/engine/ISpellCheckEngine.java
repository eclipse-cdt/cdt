/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.text.spelling.engine;

import java.util.Locale;

import org.eclipse.cdt.internal.ui.text.spelling.SpellingPreferences;

/**
 * Interface for a spell check engine.
 * <p>
 * This engine can be configured with multiple
 * dictionaries.
 * </p>
 */
public interface ISpellCheckEngine {

	/**
	 * Returns a spell checker configured with the global
	 * dictionaries and the locale dictionary that correspond to the current
	 * {@linkplain SpellingPreferences#getSpellingLocale() locale preference}.
	 * <p>
	 * <strong>Note:</strong> Changes to the spelling engine dictionaries
	 * are not propagated to this spell checker.</p>
	 *
	 * @return a configured instance of the spell checker or <code>null</code> if none
	 * @throws IllegalStateException if called after being shut down
	 */
	ISpellChecker getSpellChecker() throws IllegalStateException;

	/**
	 * Returns the locale of the current spell check engine.
	 *
	 * @return the locale of the current spell check engine
	 */
	Locale getLocale();

	/**
	 * Registers a global dictionary.
	 *
	 * @param dictionary the global dictionary to register
	 */
	void registerGlobalDictionary(ISpellDictionary dictionary);

	/**
	 * Registers a dictionary tuned for the specified locale with this engine.
	 *
	 * @param locale
	 *                   The locale to register the dictionary with
	 * @param dictionary
	 *                   The dictionary to register
	 */
	void registerDictionary(Locale locale, ISpellDictionary dictionary);

	/**
	 * Shuts down this spell check engine and its associated components.
	 * <p>
	 * Further calls to this engine result in exceptions.
	 * </p>
	 */
	void shutdown();

	/**
	 * Unregisters a dictionary previously registered either by a call to
	 * <code>registerDictionary(Locale,ISpellDictionary)</code> or <code>registerDictionary(ISpellDictionary)</code>.
	 * <p>
	 * If the dictionary was not registered before, nothing happens.</p>
	 *
	 * @param dictionary the dictionary to unregister
	 */
	void unregisterDictionary(ISpellDictionary dictionary);

}

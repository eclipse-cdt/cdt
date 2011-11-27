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
package org.eclipse.cdt.internal.core.language.settings.providers;

import java.util.LinkedHashMap;

import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsStorage;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;


/**
 * Contains the delta of changes that occurred as a result of modifying
 * language settings entries {@link ICLanguageSettingEntry}. The delta is
 * associated with a configuration description.
 *
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class interface is not stable yet as
 * it is not currently clear how it may need to be used in future. Only bare
 * minimum is provided here at this point (CDT 9.0).
 * There is no guarantee that this API will work or that it will remain the same.
 * Please do not use this API without consulting with the CDT team.
 * </p>
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class LanguageSettingsDelta {
	// maps need to be ordered by providers
	@SuppressWarnings("unused")
	private LinkedHashMap<String, // providerId
			LanguageSettingsStorage> oldLanguageSettingsState;
	@SuppressWarnings("unused")
	private LinkedHashMap<String, // providerId
			LanguageSettingsStorage> newLanguageSettingsState;

	/**
	 * Constructor.
	 *
	 * @param oldState - old language settings storage state.
	 * @param newState - new language settings storage state.
	 */
	public LanguageSettingsDelta(LinkedHashMap<String, LanguageSettingsStorage> oldState, LinkedHashMap<String, LanguageSettingsStorage> newState) {
		oldLanguageSettingsState = oldState;
		newLanguageSettingsState = newState;
	}

}

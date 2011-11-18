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


/**
 * This class currently is a placeholder holding old and new states.
 * If more details need to be pulled out of delta, it could be elaborated further.
 */
public class LanguageSettingsDelta {
	// maps need to be ordered by providers
	@SuppressWarnings("unused")
	private LinkedHashMap<String, // providerId
					LanguageSettingsStorage> oldLanguageSettingsState;
	@SuppressWarnings("unused")
	private LinkedHashMap<String, // providerId
					LanguageSettingsStorage> newLanguageSettingsState;
	
	public LanguageSettingsDelta(LinkedHashMap<String, LanguageSettingsStorage> oldState, LinkedHashMap<String, LanguageSettingsStorage> newState) {
		oldLanguageSettingsState = oldState;
		newLanguageSettingsState = newState;
	}

}

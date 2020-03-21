/*******************************************************************************
 * Copyright (c) 2020 Martin Weber.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.cmake.is.core.internal;

import org.eclipse.cdt.cmake.is.core.language.settings.providers.PreferenceConstants;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {
	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Plugin.getDefault().getPreferenceStore();
		store.setDefault(PreferenceConstants.P_WITH_CONSOLE, false);
		store.setDefault(PreferenceConstants.P_PATTERN_ENABLED, false);
		store.setDefault(PreferenceConstants.P_PATTERN, "-?\\d+(\\.\\d+)*"); //$NON-NLS-1$
	}
}

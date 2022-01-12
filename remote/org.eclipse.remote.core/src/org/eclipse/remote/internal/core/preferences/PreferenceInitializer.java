/*******************************************************************************
 * Copyright (c) 2012 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/

package org.eclipse.remote.internal.core.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.remote.core.IRemotePreferenceConstants;

/**
 * Class used to initialize default preference values.
 *
 * @since 6.0
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		Preferences.setDefaultString(IRemotePreferenceConstants.PREF_CONNECTION_TYPE_ID, "org.eclipse.remote.JSch"); //$NON-NLS-1$
	}
}

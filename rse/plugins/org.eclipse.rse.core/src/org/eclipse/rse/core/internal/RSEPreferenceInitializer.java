/********************************************************************************
 * Copyright (c) 2007 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * David Dykstal (IBM) - initial API and implementation
 ********************************************************************************/
package org.eclipse.rse.core.internal;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.rse.core.RSEPreferencesManager;

public class RSEPreferenceInitializer extends AbstractPreferenceInitializer {

	public void initializeDefaultPreferences() {
		RSEPreferencesManager.initDefaults();
	}

}

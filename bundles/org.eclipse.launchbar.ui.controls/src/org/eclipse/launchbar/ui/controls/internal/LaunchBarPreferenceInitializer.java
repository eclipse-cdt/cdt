/*******************************************************************************
 * Copyright (c) 2014, 2016 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Doug Schaefer
 *     Torkild U. Resheim - add preference to control target selector
 *     Vincent Guignot - Ingenico - add preference to control Build button
 *******************************************************************************/
package org.eclipse.launchbar.ui.controls.internal;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

public class LaunchBarPreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.setDefault(Activator.PREF_ENABLE_LAUNCHBAR, true);
		store.setDefault(Activator.PREF_ENABLE_BUILDBUTTON, true);
		store.setDefault(Activator.PREF_ALWAYS_TARGETSELECTOR, false);
		store.setDefault(Activator.PREF_LAUNCH_HISTORY_SIZE, 3);
	}

}

/***************************************************************************************************
 * Copyright (c) 2008 Mirko Raner and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Mirko Raner - [196337] Adapted from org.eclipse.tm.terminal.ssh/SshSettings
 **************************************************************************************************/

package org.eclipse.tm.internal.terminal.local;

import java.lang.reflect.Field;
import org.eclipse.tm.internal.terminal.provisional.api.ISettingsStore;
import org.eclipse.tm.internal.terminal.provisional.api.Logger;

/**
 * The class {@link LocalTerminalSettings} is the default implementation of the
 * {@link ILocalTerminalSettings} interface.
 *
 * @author Mirko Raner
 * @version $Revision: 1.3 $
 */
public class LocalTerminalSettings implements ILocalTerminalSettings {

	private String launchConfiguration;

	/**
	 * Loads the settings from the given {@link ISettingsStore}.
	 * This method loads the store contents by means of reflection. This is clearly overkill for
	 * the few settings supported by this class, but the code is much more reusable. Pretty much
	 * every implementation of a custom settings store is implemented in the same fashion and
	 * might be replace by a single centralized implementation.
	 *
	 * TODO: check for possibilities to reuse this code!
	 *
	 * @param store the {@link ISettingsStore}
	 * @see ILocalTerminalSettings#load(ISettingsStore)
	 */
	public void load(ISettingsStore store) {

		Field[] declaredField = getClass().getDeclaredFields();
		int numberOfFields = declaredField.length;
		for (int index = 0; index < numberOfFields; index++) {

			Field field = declaredField[index];
			Class type = field.getType();
			Object value = store.get(field.getName());
			if (type.equals(boolean.class)) {

				value = Boolean.valueOf((String)value);
			}
			// TODO: further conversions need to be added as new settings types are introduced
			try {

				field.set(this, value);
			}
			catch (IllegalAccessException illegalAccess) {

				Logger.logException(illegalAccess);
			}
		}
	}

	/**
	 * Saves the settings to the specified {@link ISettingsStore}.
	 * See {@link #load(ISettingsStore)} for further implementation notes.
	 *
	 * @param store the {@link ISettingsStore}
	 *
	 * @see ILocalTerminalSettings#save(ISettingsStore)
	 */
	public void save(ISettingsStore store) {

		Field[] declaredField = getClass().getDeclaredFields();
		int numberOfFields = declaredField.length;
		for (int index = 0; index < numberOfFields; index++) {

			Field field = declaredField[index];
			try {

				field.setAccessible(true);
				store.put(field.getName(), String.valueOf(field.get(this)));
			}
			catch (IllegalAccessException illegalAccess) {

				Logger.logException(illegalAccess);
			}
		}
	}

	/**
	 * @see ILocalTerminalSettings#getLaunchConfigurationName()
	 */
	public String getLaunchConfigurationName() {

		return launchConfiguration;
	}

	/**
	 * @see ILocalTerminalSettings#setLaunchConfigurationName(String)
	 */
	public void setLaunchConfigurationName(String launchConfiguration) {

		this.launchConfiguration = launchConfiguration;
	}
}

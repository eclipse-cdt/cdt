/*******************************************************************************
 * Copyright (c) 2009,2010 QNX Software Systems
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    QNX Software Systems (Alena Laskavaia)  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.param;

import org.eclipse.cdt.codan.core.model.CheckerLaunchMode;

/**
 * Problem preference for launch type of the checker
 * 
 * @since 2.0
 */
public class LaunchTypeProblemPreference extends MapProblemPreference {
	/**
	 * Propery key
	 */
	public static final String KEY = "launchModes"; //$NON-NLS-1$

	@Override
	public String getKey() {
		return KEY;
	}

	/**
	 * constructor
	 */
	public LaunchTypeProblemPreference() {
		CheckerLaunchMode[] values = CheckerLaunchMode.values();
		for (int i = 0; i < values.length; i++) {
			CheckerLaunchMode checkerLaunchMode = values[i];
			BasicProblemPreference desc = new BasicProblemPreference(checkerLaunchMode.name(), checkerLaunchMode.name(),
					PreferenceType.TYPE_BOOLEAN);
			IProblemPreference desc1 = addChildDescriptor(desc);
			if (checkerLaunchMode == CheckerLaunchMode.USE_PARENT)
				desc1.setValue(Boolean.TRUE);
		}
	}

	/**
	 * @return true if property is set to use parent mode
	 */
	public boolean isUsingParent() {
		return isRunningInMode(CheckerLaunchMode.USE_PARENT);
	}

	/**
	 * @param mode
	 * @return true if this mode enabled for this preference
	 */
	public boolean isRunningInMode(CheckerLaunchMode mode) {
		Object value = getChildValue(mode.name());
		if (value instanceof Boolean) {
			return (Boolean) value;
		}
		if (mode == CheckerLaunchMode.USE_PARENT && value == null)
			return true;
		return false;
	}

	/**
	 * @param mode
	 * @param value
	 */
	public void setRunningMode(CheckerLaunchMode mode, boolean value) {
		setChildValue(mode.name(), value);
	}
}

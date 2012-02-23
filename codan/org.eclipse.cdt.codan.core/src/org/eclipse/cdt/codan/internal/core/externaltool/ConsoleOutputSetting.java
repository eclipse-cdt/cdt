/*******************************************************************************
 * Copyright (c) 2012 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alex Ruiz (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.core.externaltool;

import static org.eclipse.cdt.codan.core.param.IProblemPreferenceDescriptor.PreferenceType.TYPE_BOOLEAN;
import static org.eclipse.cdt.codan.internal.core.externaltool.Messages.ConfigurationSettings_should_display_output;

import org.eclipse.cdt.codan.core.externaltool.SingleConfigurationSetting;
import org.eclipse.cdt.codan.core.param.BasicProblemPreference;
import org.eclipse.cdt.codan.core.param.IProblemPreferenceDescriptor;

/**
 * User-configurable setting that specifies whether the output of an external tool should be
 * displayed in an Eclipse console.
 *
 * @author alruiz@google.com (Alex Ruiz)
 */
public class ConsoleOutputSetting extends SingleConfigurationSetting<Boolean> {
	static final String KEY = "externalToolShouldDisplayOutput"; //$NON-NLS-1$

	/**
	 * Constructor.
	 * @param defaultValue the default value of the setting.
	 */
	public ConsoleOutputSetting(boolean defaultValue) {
		super(newPreferenceDescriptor(), defaultValue, Boolean.class);
	}

	private static IProblemPreferenceDescriptor newPreferenceDescriptor() {
		String label = ConfigurationSettings_should_display_output;
		return new BasicProblemPreference(KEY, label, TYPE_BOOLEAN);
	}
}

/*******************************************************************************
 * Copyright (c) 2012 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alex Ruiz  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.externaltool;

import static org.eclipse.cdt.codan.core.param.IProblemPreferenceDescriptor.PreferenceType.TYPE_STRING;

import org.eclipse.cdt.codan.core.param.BasicProblemPreference;

/**
 * User-configurable setting that specifies the arguments to pass when invoking the external tool.
 * The arguments are stored in a single {@code String}.
 *
 * @author alruiz@google.com (Alex Ruiz)
 */
public class ArgsSetting extends SingleConfigurationSetting<String> {
	private static final String KEY = "externalToolArgs"; //$NON-NLS-1$

	/**
	 * Constructor.
	 * @param label the label to be displayed in the UI.
	 * @param defaultValue the default value of the setting.
	 */
	public ArgsSetting(String label, String defaultValue) {
		super(new BasicProblemPreference(KEY, label, TYPE_STRING), defaultValue, String.class);
	}
}

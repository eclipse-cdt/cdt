/*******************************************************************************
 * Copyright (c) 2012 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alex Ruiz (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.cxx.internal.externaltool;

import static org.eclipse.cdt.codan.core.cxx.internal.externaltool.Messages.ConfigurationSettings_path_format;
import static org.eclipse.cdt.codan.core.param.IProblemPreferenceDescriptor.PreferenceType.TYPE_FILE;

import java.io.File;

import org.eclipse.cdt.codan.core.cxx.externaltool.SingleConfigurationSetting;
import org.eclipse.cdt.codan.core.param.BasicProblemPreference;
import org.eclipse.cdt.codan.core.param.IProblemPreferenceDescriptor;

/**
 * User-configurable setting that specifies the path and name of an external tool's executable.
 */
public class PathSetting extends SingleConfigurationSetting<File> {
	static final String KEY = "externalToolPath"; //$NON-NLS-1$

	/**
	 * Constructor.
	 * @param externalToolName the name of the external tool. The name of the external tool is
	 * used in the label of this setting's input field.
	 * @param defaultValue the default value of the setting.
	 */
	public PathSetting(String externalToolName, File defaultValue) {
		super(newPreferenceDescriptor(externalToolName), defaultValue, File.class);
	}

	private static IProblemPreferenceDescriptor newPreferenceDescriptor(String externalToolName) {
		String label = String.format(ConfigurationSettings_path_format, externalToolName);
		return new BasicProblemPreference(KEY, label, TYPE_FILE);
	}
}

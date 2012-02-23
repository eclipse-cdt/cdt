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

import static org.eclipse.cdt.codan.core.param.IProblemPreferenceDescriptor.PreferenceType.TYPE_FILE;

import java.io.File;

import org.eclipse.cdt.codan.core.param.BasicProblemPreference;

/**
 * User-configurable setting that specifies the path and name of an external tool's executable.
 *
 * @author alruiz@google.com (Alex Ruiz)
 */
public class PathSetting extends SingleConfigurationSetting<File> {
	private static final String KEY = "externalToolPath"; //$NON-NLS-1$

	/**
	 * Constructor.
	 * @param label the label to be displayed in the UI.
	 * @param defaultValue the default value of the setting.
	 */
	public PathSetting(String label, File defaultValue) {
		super(new BasicProblemPreference(KEY, label, TYPE_FILE), defaultValue, File.class);
	}
}
